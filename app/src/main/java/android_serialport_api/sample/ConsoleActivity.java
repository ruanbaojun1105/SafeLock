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
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.broadcast.CommandReceiver;
import android_serialport_api.SerialPortServer;

public class ConsoleActivity extends AppCompatActivity {

	EditText mReception;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);

		mReception = (EditText) findViewById(R.id.EditTextReception);

		EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
		Emission.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				int i;
				CharSequence t = v.getText();
				char[] text = new char[t.length()];
				for (i=0; i<t.length(); i++) {
					text[i] = t.charAt(i);
				}
				SerialPortServer.getInstance().sendData(new String(text).getBytes());
				return false;
			}
		});
		new CommandReceiver() {
			@Override
			public void onDataReceived(final byte[] buffer, final byte function, byte safeCod) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mReception != null) {
							mReception.append(new String(buffer, 0, function));
						}
					}
				});
			}

			@Override
			public void onFail(String str) {

			}
		}.regiest(null);
	}

}
