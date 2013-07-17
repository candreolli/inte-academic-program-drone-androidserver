package com.example.pololuusbcontroller;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.pololuusbcontroller.implementations.PololuCard;
import com.example.pololuusbcontroller.implementations.PololuCard.PololuCardType;
import com.example.pololuusbcontroller3gserver.R;
/**
 * The drone server main activity. This activity displays the camera preview and is in charge managing
 * the server. The server sends the camera stream over the network and receive commands from the
 * remote.
 * @author Cédric Andreolli - Intel Corporation
 *
 */
public class MainActivity extends Activity {
	/**
	 * Returns the main camera.
	 * @return The main camera if it exists.
	 */
	public static Camera getCameraInstance()
	{
		Camera c = null;
		try {
			c = Camera.open(0);
			Parameters p = c.getParameters();
			p.setPictureFormat(ImageFormat.JPEG);
			c.setParameters(p);
		}catch(Exception e){}
		return c;
	}
	/**
	 * The camera.
	 */
	private Camera camera = null;
	/**
	 * The camera preview.
	 */
	private CameraPreview cameraPreview = null;
	/**
	 * The server.
	 */
	private Server server = null;

	/**
	 * Launches a service that restart the application if it happens
	 * to be killed by an unpredictable event.
	 */
	private void launchRestartService() {
		Intent intent = new Intent();
		intent.setAction("3GControllerService");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(intent);
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * This view displays the camera preview. For this reason, we prefer to use the landscape
		 * orientation. We also display the camera preview in complete full screen. This means that
		 * we need to remove the title bar.
		 */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.setContentView(R.layout.activity_main);

		this.launchRestartService();

		Settings settings = Settings.getInstance(this);
		this.registerPushURI(settings);

		try {
			this.camera = getCameraInstance();
			this.cameraPreview = new CameraPreview(this, this.camera);
			FrameLayout preview = (FrameLayout) this.findViewById(R.id.IDCameraPreview);
			preview.addView(this.cameraPreview);
			final PololuCard card = PololuCard.createCard(this, PololuCardType.MICRO_MAESTRO);
			this.server = new Server(settings.getCommandPortDrone(), settings.getVideoPortDrone(), card, this.cameraPreview);
			this.server.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intentSettings = new Intent(this, SettingsActivity.class);
			this.startActivity(intentSettings);
			return true;
		case R.id.action_main_activity:
			Intent intentMainActivity = new Intent(this, MainActivity.class);
			try {
				this.server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.startActivity(intentMainActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onPause()
	{
		super.onPause();
		this.camera.stopPreview();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.camera.startPreview();
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			this.server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try{
			this.camera.stopPreview();
		}catch(Exception e){}
	}

	/**
	 * Register a NFC push message. The message contains a string with the ip of the central
	 * server and the communications ports used by the remote. This message allows
	 * the remote to be automatically reconfigured on the fly.
	 * @param settings The settings.
	 */
	private void registerPushURI(Settings settings) {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		Log.i("server","IP Address : "+settings.getIPAddress());
		NdefRecord record = NdefRecord.createUri("http://"+settings.getIPAddress()+"/"+
				settings.getCommandPortRemote()+"/"+settings.getVideoPortRemote());
		NdefMessage message = new NdefMessage(new NdefRecord[]{record});
		nfcAdapter.setNdefPushMessage(message, this);
	}
}
