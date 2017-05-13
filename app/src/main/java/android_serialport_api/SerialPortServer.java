/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.activity.GetEnityActivity;
import com.hwx.safelock.safelock.util.LogUtils;

import okhttp3.OkHttpClient;

public class SerialPortServer {

    private ReadScanThread mReadThreadScan;
    private ReadThread mReadThread;
    private byte[] buffer = new byte[16];
    private byte[] data_buffer=new byte[0];
    private byte[] bufferScan = new byte[64];
    private int fact_size=0;
    private Bundle bundle=new Bundle();

    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private InputStream mInputStreamScan;
    private OutputStream mOutputStreamScan;


    private static SerialPortServer instance = null;
    public static SerialPortServer getInstance() {
        if (instance==null){
            synchronized (SerialPortServer.class) {
                if (instance==null){
                    instance=new SerialPortServer();
                }
            }
        }
        return instance;
    }
    public void closeScanPort() {
    }
    public boolean initScanPort() {
        try {
            builder=new StringBuilder(0);
            mInputStreamScan=Application.getInstance().getScanSerialPort().getInputStream();//扫描二维码读取串口数据
            mOutputStreamScan=Application.getInstance().getScanSerialPort().getOutputStream();
			/* Create a receiving thread */
            mReadThreadScan = new ReadScanThread();
            mReadThreadScan.start();
        } catch (SecurityException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (InvalidParameterException e) {
            return false;
        }catch (Exception e) {
            return false;
        }
        return true;
    }
    public boolean initPort() {
        try {
            mOutputStream = Application.getInstance().getConnSerialPort().getOutputStream();
            mInputStream = Application.getInstance().getConnSerialPort().getInputStream();
			/* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            bundle.putString("error","error_security");
            Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_FAIL,bundle);
            return false;
        } catch (IOException e) {
            bundle.putString("error","error_unknown");
            Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_FAIL,bundle);
            return false;
        } catch (InvalidParameterException e) {
            bundle.putString("error","error_configuration");
            Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_FAIL,bundle);
            return false;
        }catch (Exception e) {
            bundle.putString("error","error");
            Application.sendLocalBroadCast(Constants.SERIAL_PORT_CONNECT_FAIL,bundle);
            return false;
        }
        try {
            initScanPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    public void close() {
        if (mReadThread != null)
            mReadThread.interrupt();
        if (mReadThreadScan!=null)
            mReadThreadScan.interrupt();
        Application.getInstance().closeSerialPort();
        Application.getInstance().closeSerialPort2();
        instance=null;
        LogUtils.e("close");
    }
    private StringBuilder builder;
    private class ReadScanThread extends Thread {//解析二维码
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int sizeScan = 0;
                if (mInputStreamScan == null)
                    return;
                try {
                    sizeScan = mInputStreamScan.read(bufferScan);
                } catch (IOException e) {
                    e.printStackTrace();
                    Application.getInstance().mSerialPortScan.close();
                }
                if (sizeScan > 0) {
                    try {
                        byte[] b=Arrays.copyOfRange(bufferScan, 0, sizeScan);
                        String res = new String(b);
                        if (!res.equals("\n")) {
                            builder.append(res);
                        } else {
                            /*if (!TextUtils.isEmpty(builder.toString())) {
                                sendDataBrocastScan(builder.toString());
                                LogUtils.e("二维码:" + builder.toString());
                                builder.setLength(0);
                            }*/
                            String sign = builder.toString();
                            try {
                                sign.replace("\r","");
                                sign.replace("\n","");
                                sign.replace("*","");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!TextUtils.isEmpty(sign)) {
                                sendDataBrocastScan(sign.trim());
                                LogUtils.e("二维码:" + sign);
                            }
                            builder.setLength(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            //Arrays.fill(buffer, 0, 4096, (byte) 0);
            while (!isInterrupted()) {
                int size = 0;
                if (mInputStream == null)
                    return;
                try {
                    size = mInputStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    Application.getInstance().mSerialPort.close();
                }
                fact_size+=size;
                if (size==0&&fact_size>0){//clear
                    initNumber();
                }
                try {
                    if (size>0) {
                        byte[] b=Arrays.copyOfRange(buffer, 0, size);
                        data_buffer= byteMerger(data_buffer,b);
                        //sendData(b);
                        if (checkDataHead(data_buffer)){
                            initNumber();
                        }else if (getNumberData(fact_size,data_buffer)){//clear
                            initNumber();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    boolean checkDataHead(byte[] buffer){
        if (buffer[0] == starCode)
            return false;
        /*else {
            //此处可以继续判断，但一般不会有这样情况
            for (int i = 0; i <buffer.length ; i++) {
                if (buffer[i] == starCode){
                    data_buffer=Arrays.copyOfRange(buffer, i, buffer.length);
                    return false;
                }
            }
        }*/
        return true;
    }
    void initNumber(){
        data_buffer=new byte[0];
        fact_size=0;
        System.gc();
    }

    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
    static final String safeStr = "hwx";//校验码
    static final String endStr = "end";
    static final String stardStr= "3a 01 01 00 07 01 02 03 04 05 06 07 0b 0c 0d";
    private boolean getTextData(String result) {
        //StringBuilder sb=new StringBuilder(str);
        //byte[] head="3a 01 01 00 07 01 02 03 04 05 06 07 0b 0c 0d".getBytes();
        if (result.startsWith(stardStr)){
            return true;
        }
        return false;
    }

    /**
     * 十六进制接收解析
     * @param size
     * @param buffer
     */
    public static final int safeCode = 0x10;//校验码
    static final byte[] endCode = new byte[]{0x0c, 0x0d};//结束符
    static final int starCode = 0x3a;//头
    static final int addrCode = 0x01;//地址
    private boolean getNumberData(int size,byte[] buffer) {
        byte numberNo = 0;//功能编码
        int count_data = 0;//数据长度
        if (size > 8 ) {//一段数据至少9位
            if (addrCode != buffer[1])
                return true;//抛弃这段
            count_data = (int) buffer[3] * 256 + buffer[4];
            int a = 5 + count_data;
            if (a+2>=size)
                return false;
            numberNo = buffer[2];

            if (buffer[a + 1] == endCode[0] && buffer[a + 2] == endCode[1]) {
                byte[] content=Arrays.copyOfRange(buffer, 5, a);
                byte sa=checkSafeCod(content);
                if (numberNo==0x05){
                    sa=0x01;
                }
                sendData((byte) 0xff,new byte[]{0x00,0x00},true);//收到消息返回一条
                //onDataReceived(content, numberNo, buffer[a]);
                sendDataBrocast(content, numberNo, buffer[a]);
                /*if (sa==buffer[a]) {//测试暂时不开验证
                    onDataReceived(content, numberNo, buffer[a]);
                    return true;
                }*/
            }
            return true;
        }
        return  false;
    }
    private void sendDataBrocastScan(String qrcode) {
        Bundle bundle=new Bundle();
        bundle.putString("qrcode",qrcode);
        Application.sendLocalBroadCast(Constants.SERIAL_PORT_COMMAND_SCAN,bundle);
    }
    private void sendDataBrocast(byte[] buffer, byte function, byte safeCode) {
        Bundle bundle=new Bundle();
        bundle.putByteArray("buffer",buffer);
        bundle.putByte("function",function);
        bundle.putByte("safeCode",safeCode);
        Application.sendLocalBroadCast(Constants.SERIAL_PORT_COMMAND,bundle);
    }
    public synchronized void sendData(byte[] data) {
        // TODO Auto-generated method stub
        //mSendingThread = new SendingThread();
        //mSendingThread.start();
        if (mOutputStream != null) {
            try {
                mOutputStream.write(data);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    /**
     *发送数据
     */
    public synchronized void sendData(/*SerialPort mSerialPort , OutputStream mOutputStream, */byte function,String data,boolean isAutoSafeCode) {
        // TODO Auto-generated method stub
        sendData(function,data.getBytes(),isAutoSafeCode);
    }
    public synchronized void sendData2(byte function,byte[] content,boolean isAutoSafeCode) {
        sendData(function, content, isAutoSafeCode,mOutputStreamScan);
    }
    public synchronized void sendData(byte function,byte[] content,boolean isAutoSafeCode) {
        sendData(function, content, isAutoSafeCode,mOutputStream);
    }
    /**
     *发送数据
     */
    public synchronized void sendData(byte function,byte[] content,boolean isAutoSafeCode,OutputStream mOutputStream) {
        isAutoSafeCode = true;//所有都自动算出来
        // TODO Auto-generated method stub
        //mSendingThread = new SendingThread();
        //mSendingThread.start();
        if (mOutputStream != null) {
            try {
                byte[] head = new byte[]{starCode, addrCode, function, (byte) (content.length / 256), (byte) (content.length % 256)};
                byte safe = safeCode;
                if (isAutoSafeCode)
                    safe = checkSafeCod(content);
                byte[] end = new byte[]{safe, endCode[0], endCode[1]};
                List<byte[]> list = new ArrayList<>();
                list.add(head);
                list.add(content);
                list.add(end);
                mOutputStream.write(sysCopy(list));
                LogUtils.e("功能码：" + function + "的数据发送完毕！");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static  byte checkSafeCod(byte[] data){
        byte safeCode=0;
        for(byte at:data){
            safeCode^=at;
        }
        return safeCode;
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray:srcArrays) {
            len+= srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray:srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

}
