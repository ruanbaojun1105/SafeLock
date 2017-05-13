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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.broadcast.CommandReceiver;
import android_serialport_api.SerialPortServer;

public class HwxTestActivity extends AppCompatActivity {

	byte mValueToSend;
	boolean mByteReceivedBack;

	TextView length_tv;
	TextView content_tv;
	TextView count_tv;
	Button send_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hwxtest);
		length_tv = (TextView) findViewById(R.id.length_tv);
		content_tv = (TextView) findViewById(R.id.content_tv);
		send_btn=(Button) findViewById(R.id.send_btn);
		count_tv=(TextView) findViewById(R.id.count_tv);
		send_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SerialPortServer.getInstance().sendData("aa".getBytes());
				// TODO Auto-generated method stub
			}
		});
		new CommandReceiver() {
			@Override
			public void onDataReceived(byte[] buffer, final byte function, byte safeCod) {
				final StringBuilder sb=new StringBuilder("");
				for (byte a:buffer) {
					sb.append(a+"--");
				}

				runOnUiThread(new Runnable() {
					public void run() {
						length_tv.setText(String.valueOf(function));
						content_tv.setText(sb.toString());
					}
				});
			}

			@Override
			public void onFail(String str) {

			}
		}.regiest(null);
	}

}
