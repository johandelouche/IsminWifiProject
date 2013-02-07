package fr.ismin;


import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	//the main activity
	
	//the ihm components
	private EditText username;
	private EditText password;
	private CheckBox rememberMe;
	private CheckBox autoConnection;
	
	//strings used in the code
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_REMEMBERME = "rememberme";
	private static final String PREF_AUTOCONNECTION = "autoconnection";
	private static final String EMPTY = "";
	public static String SSID = "INVITE";
	public static String SSID2 = "ELEVES";
	public static final String TAG = "ISMIN WIFI";
	
	//connection object which handle all about wifi
	Connection connection;
	
	//permit the remember me function
	SharedPreferences sharedPreferences;
	
	//permit to manage the wifi
	WifiManager wifiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connection_layout);
		wifiManager = (WifiManager)  getSystemService(Context.WIFI_SERVICE);
		connection = new Connection(this);
		setIHM();
		if (autoConnection.isChecked()) {
			connection.register();//if the user want automatic connection we start listening to network changed state
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		connection.unregister();
	}

	
	private void setIHM() {
		Log.i(TAG, "start setIHM function");
		username = (EditText) findViewById(R.id.editTextUserName);
		password = (EditText) findViewById(R.id.editTextPassword);
		rememberMe =  (CheckBox) findViewById(R.id.checkBoxRemeberMe);
		autoConnection = (CheckBox) findViewById(R.id.checkBoxAutoConnection);
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		//preferences are used to fill field, if any default value is used.
		rememberMe.setChecked(sharedPreferences.getBoolean(PREF_REMEMBERME, false));
		username.setText(sharedPreferences.getString(PREF_USERNAME, EMPTY));
		password.setText(sharedPreferences.getString(PREF_PASSWORD, EMPTY));
		autoConnection.setChecked(sharedPreferences.getBoolean(PREF_AUTOCONNECTION, false));
		
		
	}
	
	public void connectionButton(View view) {
		if (autoConnection.isChecked()) {
			Toast.makeText(this, getString(R.string.auto_connect_on), Toast.LENGTH_SHORT).show();
		} else {
			connection();
		}
	}
	
	public void connection() {
		Log.i(TAG, "start connection function");
			if (checkFields()) {	
			if (wifiManager.getConnectionInfo() == null || wifiManager.getConnectionInfo().getSSID() == null) {
				Toast.makeText(this, getString(R.string.warning), Toast.LENGTH_LONG).show();
			} else if ((wifiManager.getConnectionInfo().getSSID().compareTo(SSID) == 0) || (wifiManager.getConnectionInfo().getSSID().compareTo(SSID2) == 0)) {
				post();//verification of the wifi conditions before request post method
			}
			else {
				Toast.makeText(this, getString(R.string.warning), Toast.LENGTH_LONG).show();
				Toast.makeText(this, wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_LONG).show();
			}	
		}
	}
	
	//register the broadcast listener of connection class 
	public void register(View view) {
		connection.register();
	}
	
	
	//launch the post method to register to the wifi access system
	public void post() {
		Log.i(TAG, "start post function");
		String[] param = {username.getText().toString(), password.getText().toString()};
		new PostMethod(this).execute(param);
		
	}
	
	//check if the fields have been filled
	public boolean checkFields() {	
		Log.i(TAG, "start checkField function");
		if ((username.getText().toString().length() == 0) || (password.getText().toString().length() == 0)) {
			Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
			return false;
		} 
		
		//remember preferences
		if (rememberMe.isChecked()) {
			rememberMe();
		} else {
			forgetMe();
		}
		
		return true;
	}
	
	//set the preference file to remember user preferences after the application have been killed
	public void rememberMe() {
		Log.i(TAG, "start rememberMe function");
		sharedPreferences.edit().putString(PREF_USERNAME, username.getText().toString()).commit();
		sharedPreferences.edit().putString(PREF_PASSWORD, password.getText().toString()).commit();
		sharedPreferences.edit().putBoolean(PREF_REMEMBERME, rememberMe.isChecked()).commit();
		sharedPreferences.edit().putBoolean(PREF_AUTOCONNECTION, autoConnection.isChecked()).commit();
	}
	
	//empty the preferences
	public void forgetMe() {
		Log.i(TAG, "start forgetMe function");
		sharedPreferences.edit().putString(PREF_USERNAME, EMPTY).commit();
		sharedPreferences.edit().putString(PREF_PASSWORD, EMPTY).commit();
		sharedPreferences.edit().putBoolean(PREF_REMEMBERME, rememberMe.isChecked()).commit();
		sharedPreferences.edit().putBoolean(PREF_AUTOCONNECTION, false).commit();
		
	}

}
