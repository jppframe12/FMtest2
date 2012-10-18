package com.example.fmtest2;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;

import org.jgroups.Channel;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.helpers.StartupWaitForObjects;
import org.nzdis.fragme.util.NetworkUtils;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Fmtest2MainActivity extends Activity implements Observer {

	EditText changeText;
	TextView recordText;
	Button submitBotton;
	TextView infoview;
	TextView changeview;
	Button starttest;
	RelativeLayout layout;
	//public static Long preTime=null;
	TestOb tob = new TestOb();

	int changeNum;
    String user;
	
	private MulticastLock mcLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fmtest2_main);
		// Get view elements
		layout=(RelativeLayout)findViewById(R.id.layout);
		changeText = (EditText) findViewById(R.id.editText1);
		changeview= (TextView) findViewById(R.id.textView3);
		recordText = (TextView) findViewById(R.id.textview1);
		submitBotton = (Button) findViewById(R.id.button1);
		infoview=(TextView) findViewById(R.id.textView2);
		infoview.setMovementMethod(new ScrollingMovementMethod()) ;
		//for test
		starttest =(Button) findViewById(R.id.button2);
		starttest.setEnabled(false);
		//submitBotton.setEnabled(false);
		
		// Set up channel;
		initialChannel();
	}

	private void initialChannel() {
		// TODO Auto-generated method stub
		//System.setProperty("JGroups.bind_addr", "IPV4");

		// check wifi available and unlock multicast
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			mcLock = wifi.createMulticastLock("mylock");
			mcLock.acquire();
			Log.i("fmtest2", "unlock multicastlock");
			//Random rd=new Random();
			 user = "test" + new Random().nextInt(100);
			String address = NetworkUtils.getNonLoopBackAddressByProtocol(NetworkUtils.IPV4);
			if (address == null) {
				System.out.println("Could not find a local ip address");
				return;
			}
			System.out.println("Using address: " + address);

			ControlCenter.setUpConnectionsWithHelper("Fragme Desktop test", user, address, new StartupWaitForObjects(1));
			//ControlCenter.setUpConnections("fmtest2", user);
			//ControlCenter.setUpConnections("Fragme Desktop test", user, address);
			//infoview.append("Number of peers: "+ControlCenter.getNoOfPeers());
			//System.err.println("Peers: " + ControlCenter.getNoOfPeers() + " Objects: " + shareObs.size());
			
			//ControlCenter.getMyAddress();
			//ControlCenter.getPeerName(ControlCenter.getMyAddress());
			//infoview.append("\n my peer name:"+ControlCenter.getPeerName(ControlCenter.getMyAddress()));
			//wait for peer member
			
			/*if (ControlCenter.getNoOfPeers() > 0) {
	            while (ControlCenter.getAllObjects().size() == 0) {
	                    try {
	                            Thread.sleep(5000);
	                    } catch (InterruptedException e) {
	                            // TODO Auto-generated catch block
	                            e.printStackTrace();
	                    }
	            }
	    }*/
			Vector shareObs = ControlCenter.getAllObjects();

			if (shareObs.size() == 0) {
				this.tob = (TestOb) ControlCenter.createNewObject(TestOb.class);
				Log.i("fmtest2Activity", "Create a new TestOb");
				infoview.append("\n Create a new TestOb");

			} else {
				this.tob = (TestOb) shareObs.get(0);
				Log.i("fmtest2Activity", "join and share the TestOb");
				infoview.append("\n join and share the TestOb ");
			}
			infoview.append("\n Peers: " + ControlCenter.getNoOfPeers() + " Objects: " + shareObs.size());
			this.tob.addObserver(this);
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
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
		infoview.append("\n object changed :" +tob.getChange()+"  "+tob.getRecord());
		this.tob.addObserver(this);
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		this.update(tob, null);

	}
	//for test
	
	/*public void onStartTest(View view) {

		int j = 0;
		while (j < 3) {
			// send 5 changes continually to see how long it will take
			preTime = System.nanoTime();//currentTimeMillis();
			for (int i = 0; i < 5; i++) {
				tob.setRecord(String.valueOf(i));
				tob.change();
				 System.out.println("fmtest2 : object changed");
			}
			j++;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}*/

	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
		final TestOb uptob=(TestOb)observable;
		
		Log.i("fmtest2","in Observer update()  "+uptob.getChange()+"  "+uptob.getRecord());
		//infoview.append("\n in Observer update()  "+uptob.getChange()+"  "+uptob.getRecord());
		
		layout.post(new Runnable(){
			public void run(){
				//infoview.append("\n"+uptob.getRecord()+"  "+uptob.getChange());
			changeview.setText(uptob.getChange());
			recordText.setText(uptob.getRecord());
			}
		});
		/*changeview.post(new Runnable() {
			public void run(){
				changeview.setText(uptob.getChange());
			}
		});
		recordText.post(new Runnable() {
			public void run(){
				recordText.setText(uptob.getRecord());
			}
		});*/
		/*changeview.setText(uptob.getChange());
		recordText.setText(uptob.getRecord());*/
		
					
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControlCenter.closeUpConnections();

		if (mcLock.isHeld()) {
			mcLock.release();
		}
		Toast.makeText(this, "Activity destroied",	Toast.LENGTH_LONG).show();
	}

}
