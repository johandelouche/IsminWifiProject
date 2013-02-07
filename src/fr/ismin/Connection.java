package fr.ismin;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class Connection implements Runnable {

	//the context (MainActivity)
	Context context;
	
	//It is the same instance because WifiManager is a singleton
	WifiManager wifiManager;
	
	//the broadcasts listener
	BroadcastReceiver scanResultsAvailableReceiver;
	BroadcastReceiver wifiStateChangedReceiver;
	BroadcastReceiver networkStateChangedAction;
	
	//to receive the results of wifi scan
	List <ScanResult> scanResults;
	
	//a scan result instance to memorize the one we are looking for
	ScanResult wifi;
	WifiConfiguration wifiConfiguration;
	
	//some network actions has to be launched in an other thread in order to not block the UI (user interface) thread
	Thread thread;
	
	//to know if the BroadcastReceiver are registered or not.
	boolean register = false;
	
	Connection(Context context) {
		
		Log.i(MainActivity.TAG, "constructeur connection");
		this.context = context;
		thread = new Thread(this);
		wifiManager = (WifiManager)  context.getSystemService(Context.WIFI_SERVICE);
	
		
		//creation of the broadcast listener for the NETWORK_STATE_CHANGED_ACTION
		 networkStateChangedAction = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(MainActivity.TAG, "on receive networkStateChangedAction");
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo != null) {
					if (wifiInfo.getSSID() != null) {
						if (wifiInfo.getSSID().compareTo(MainActivity.SSID) == 0 || wifiInfo.getSSID().compareTo(MainActivity.SSID2) == 0) {
							((MainActivity)context).connection();//try a connection if the wifi is one of the two we are looking for
						}
					}
				}
			}
			 
		 };
		 
		 //broadcast receiver SCAN_RESULTS_AVAILABLE_ACTION
		 scanResultsAvailableReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(MainActivity.TAG, "on receive results available");
				int wifiState = wifiManager.getWifiState();
				if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
					scanResults = wifiManager.getScanResults();
					connect();
				}
				
			}
         };
         
         
         //broadcast receiver WIFI_STATE_CHANGED_ACTION
         wifiStateChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				switch (wifiManager.getWifiState()) {
				
				case WifiManager.WIFI_STATE_DISABLED:
					Log.i(MainActivity.TAG, "wifi disabled");
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					Log.i(MainActivity.TAG, "wifi disabling");
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					Log.i(MainActivity.TAG, "receiver create the connexion");
						 wifiManager.startScan();
				         Log.i(MainActivity.TAG, "start scan");
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					Log.i(MainActivity.TAG, "wifi enabling");
					break;
				case WifiManager.WIFI_STATE_UNKNOWN:
					Log.i(MainActivity.TAG, "wifi unknown");
					break;
				default:
					break;
				}
				
			}
        	 
         };

      
	}
	
	
	//turn on the wifi
	void start() {
    	
    	if(!wifiManager.setWifiEnabled(true)) {
    		Log.i(MainActivity.TAG, "Impossible d'activer le wifi");
    		Toast.makeText(context, "Impossible d'activer le wifi", Toast.LENGTH_LONG).show();
    	}

	}
	
	//connect to the school wifi
	void connect() {
		 
         if (scanResults == null) {
         	Log.i(MainActivity.TAG, "scan results are null");
         }
         else {
         	 for (ScanResult sr : scanResults) {
              	Log.i(MainActivity.TAG, "scan results : " + sr.toString());
              	if ( sr.SSID.equals(MainActivity.SSID) || sr.SSID.equals(MainActivity.SSID2)) {
              		Log.i(MainActivity.TAG, "wifi founded !");
              		wifi = sr;
              		thread.run();
              		break;
              	}
              }
         	 if (wifi == null) {
         		Log.i(MainActivity.TAG, "wifi not found");
         	 }
         }
	}

	public void run() {
		// the connection (last line) has to be in a different thread than the UI one
		Log.i(MainActivity.TAG, "démarrage du thread");
		wifiConfiguration = new WifiConfiguration();
		wifiConfiguration.SSID = '\"' + wifi.SSID + '\"';
		wifiConfiguration.hiddenSSID = true;
		wifiConfiguration.status = WifiConfiguration.Status.ENABLED;        
		wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wifiManager.addNetwork(wifiConfiguration);
	    wifiManager.enableNetwork(wifiManager.addNetwork(wifiConfiguration), true);        		
	}
	
	/*
	 * THE POSSIBILITY TO TURN ON WIFI, FIND AND CONNECT TO THE SCHOOL WIFI IS NOT ACTIVED IN THIS VERSION
	 * BECAUSE THE FUNCTION IS NOT RELEVANTE FOR THE USER SO I DO NOT USE IT.
	 */
	//to register the listeners
	public void register() {
		if (!register) {
			//context.registerReceiver(scanResultsAvailableReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));	
			//context.registerReceiver(wifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
			context.registerReceiver(networkStateChangedAction, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
			register = true;
		}
	}
	
	//to unregister them
	public void unregister() {
		if (register) {
			//context.unregisterReceiver(scanResultsAvailableReceiver);
		    //context.unregisterReceiver(wifiStateChangedReceiver);
		    context.unregisterReceiver(networkStateChangedAction);
		    register = false;
		}	
	}	
}
