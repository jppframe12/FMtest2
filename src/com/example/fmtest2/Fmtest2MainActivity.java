package com.example.fmtest2;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import org.jgroups.Channel;
import org.nzdis.fragme.ControlCenter;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Fmtest2MainActivity extends Activity implements Observer {

	EditText changeText;
	TextView recordText;
	Button submitBotton;

	TestOb tob = new TestOb();

	int changeNum;
	
	private MulticastLock mcLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fmtest2_main);
		// Get view elements
		changeText = (EditText) findViewById(R.id.editText1);
		recordText = (TextView) findViewById(R.id.textview1);
		submitBotton = (Button) findViewById(R.id.button1);

		// Set up channel;
		initialChannel();
	}

	private void initialChannel() {
		// TODO Auto-generated method stub
		System.setProperty("JGroups.bind_addr", "IPV4");

		// check wifi available and unlock multicast
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			mcLock = wifi.createMulticastLock("mylock");
			mcLock.acquire();
			Log.i("fmtest2", "unlock multicastlock");

			String user = "test" + Build.MODEL;
			ControlCenter.setUpConnections("fmtest2", user);
			
			Vector shareObs = ControlCenter.getAllObjects();

			if (shareObs.size() == 0) {
				this.tob = (TestOb) ControlCenter.createNewObject(TestOb.class);
				Log.i("fmtest2Activity", "Create a new TestOb");

			} else {
				this.tob = (TestOb) shareObs.get(0);
				Log.i("fmtest2Activity", "join and share the TestOb");
			}
			this.tob.addObserver(this);
			this.update(tob, null);

		} else {
			Log.e("fmtest2", "no wifi");
			Toast.makeText(this, "no wifi", Toast.LENGTH_LONG).show();
		}
	}

	public void onClickHandler(View view) {

		// view.getId();
		if (changeText.getText().length() == 0) {
			Toast.makeText(this, "Please enter a valid number",
					Toast.LENGTH_LONG).show();
			return;

		}
		if(tob.getRecord()==null){
			changeNum=0;
		}else{
		changeNum=Integer.valueOf(tob.getRecord());
		}
		changeNum++;
		tob.setChange(changeText.getText().toString());
		tob.setRecord(String.valueOf(changeNum));
		
		tob.change();
		Log.i("fmtest2", "object changed");
		this.update(tob, null);

	}

	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		recordText.setText(tob.getRecord());
		changeText.setText(tob.getChange());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControlCenter.closeUpConnections();

		if (mcLock.isHeld()) {
			mcLock.release();
		}
		Toast.makeText(this, "Activity destroied",
				Toast.LENGTH_LONG).show();
	}

}
