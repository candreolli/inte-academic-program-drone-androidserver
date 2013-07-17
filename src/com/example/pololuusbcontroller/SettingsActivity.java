package com.example.pololuusbcontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.pololuusbcontroller3gserver.R;

public class SettingsActivity extends Activity{

	private void loadSettings(){
		Settings settings = Settings.getInstance(this);
		EditText ipAddressTV = (EditText) this.findViewById(R.id.IDIPAddress_Text);
		try {
			ipAddressTV.setText(settings.getIPAddress());
		} catch (Exception e1) {
			ipAddressTV.setText("IP Adress not found");
		}

		EditText commandPortDET = (EditText) this.findViewById(R.id.IDCommandD_Text);
		commandPortDET.setText(settings.getCommandPortDrone()+"");

		EditText videoPortDET = (EditText) this.findViewById(R.id.IDVideoD_Text);
		videoPortDET.setText(settings.getVideoPortDrone()+"");

		EditText videoPortRET = (EditText) this.findViewById(R.id.IDVideoR_Text);
		videoPortRET.setText(settings.getVideoPortRemote()+"");

		EditText commandPortRET = (EditText) this.findViewById(R.id.IDCommandR_Text);
		commandPortRET.setText(settings.getCommandPortRemote()+"");
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		this.setContentView(R.layout.activity_settings);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(this, MainActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_main_activity:
			Intent intent = new Intent(this, MainActivity.class);
			this.startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		this.saveSettings();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.loadSettings();
	}

	@Override
	protected void onStop() {
		this.saveSettings();
		super.onStop();
	}

	private void saveSettings(){
		Settings settings = Settings.getInstance(this);
		EditText commandPortDET = (EditText) this.findViewById(R.id.IDCommandD_Text);
		EditText videoPortDET = (EditText) this.findViewById(R.id.IDVideoD_Text);
		EditText commandPortRET = (EditText) this.findViewById(R.id.IDCommandR_Text);
		EditText videoPortRET = (EditText) this.findViewById(R.id.IDVideoR_Text);
		EditText ipAddressET = (EditText) this.findViewById(R.id.IDIPAddress_Text);
		settings.setCommandPortDrone(Integer.parseInt(commandPortDET.getText().toString()));
		settings.setVideoPortDrone(Integer.parseInt(videoPortDET.getText().toString()));
		settings.setCommandPortRemote(Integer.parseInt(commandPortRET.getText().toString()));
		settings.setVideoPortRemote(Integer.parseInt(videoPortRET.getText().toString()));
		settings.setIPAddress(ipAddressET.getText().toString());
	}
}
