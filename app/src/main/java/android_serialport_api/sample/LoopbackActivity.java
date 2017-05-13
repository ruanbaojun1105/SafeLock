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

package android_serialport_api.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.broadcast.CommandReceiver;
import android_serialport_api.SerialPortServer;

public class LoopbackActivity extends AppCompatActivity {

	byte mValueToSend;
	boolean mByteReceivedBack;
	Object mByteReceivedBackSemaphore = new Object();
	Integer mIncoming = new Integer(0);
	Integer mOutgoing = new Integer(0);
	Integer mLost = new Integer(0);
	Integer mCorrupted = new Integer(0);

	SendingThread mSendingThread;
	TextView mTextViewOutgoing;
	TextView mTextViewIncoming;
	TextView mTextViewLost;
	TextView mTextViewCorrupted;

	private class SendingThread extends Thread {
		@Override
		public void run() {
			while (!isInterrupted()) {
				synchronized (mByteReceivedBackSemaphore) {
					mByteReceivedBack = false;
					SerialPortServer.getInstance().sendData(new byte[]{mValueToSend});
					mOutgoing++;
					// Wait for 100ms before sending next byte, or as soon as
					// the sent byte has been read back.
					try {
						mByteReceivedBackSemaphore.wait(100);
						if (mByteReceivedBack == true) {
							// Byte has been received
							mIncoming++;
						} else {
							// Timeout
							mLost++;
						}
						runOnUiThread(new Runnable() {
							public void run() {
								mTextViewOutgoing.setText(mOutgoing.toString());
								mTextViewLost.setText(mLost.toString());
								mTextViewIncoming.setText(mIncoming.toString());
								mTextViewCorrupted.setText(mCorrupted.toString());
							}
						});
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loopback);
		mTextViewOutgoing = (TextView) findViewById(R.id.TextViewOutgoingValue);
		mTextViewIncoming = (TextView) findViewById(R.id.TextViewIncomingValue);
		mTextViewLost = (TextView) findViewById(R.id.textViewLostValue);
		mTextViewCorrupted = (TextView) findViewById(R.id.textViewCorruptedValue);
		if (Application.getInstance().mSerialPort != null) {
			mSendingThread = new SendingThread();
			mSendingThread.start();
			new CommandReceiver() {
				@Override
				public void onDataReceived(byte[] buffer, byte function, byte safeCod) {
					synchronized (mByteReceivedBackSemaphore) {
						int i;
						for (i = 0; i < function; i++) {
							if ((buffer[i] == mValueToSend) && (mByteReceivedBack == false)) {
								mValueToSend++;
								// This byte was expected
								// Wake-up the sending thread
								mByteReceivedBack = true;
								mByteReceivedBackSemaphore.notify();
							} else {
								// The byte was not expected
								mCorrupted++;
							}
						}
					}
				}

				@Override
				public void onFail(String str) {

				}
			}.regiest(null);
		}
	}

	@Override
	protected void onDestroy() {
		if (mSendingThread != null)
			mSendingThread.interrupt();
		super.onDestroy();
	}
}
