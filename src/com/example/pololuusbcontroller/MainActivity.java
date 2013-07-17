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
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.pololuusbcontroller.implementations.PololuCard;
import com.example.pololuusbcontroller.implementations.PololuCard.PololuCardType;
import com.example.pololuusbcontroller3gserver.R;

public class MainActivity extends Activity {
	public static Camera getCameraInstance()
	{
		Camera c = null;

		try
		{
			Log.i("server", "Number of cameras : "+Camera.getNumberOfCameras());

			c = Camera.open(0);

			Parameters p = c.getParameters();
			p.setPictureFormat(ImageFormat.JPEG);
			c.setParameters(p);

			if(c!=null)
				Log.i("server", "CAMERA FOUND");
		}
		catch(Exception e){}
		return c;
	}
	private Camera mCamera = null;
	private CameraPreview mPreview = null;

	private TextureView mTextureView;
	private Server server = null;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


		this.setContentView(R.layout.activity_main);


		Log.i("server", "Before Intent");
		Intent intent = new Intent();
		intent.setAction("3GControllerService");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(intent);
		Log.i("server", "After intent");


		Settings settings = Settings.getInstance(this);

		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		//		NdefRecord record = NdefRecord.createUri("http://"+Utils.getLocalIpAddress()+"/"+
		//				settings.getCommandPort()+"/"+settings.getVideoPort());

		Log.i("server","IP Address : "+settings.getIPAddress());
		NdefRecord record = NdefRecord.createUri("http://"+settings.getIPAddress()+"/"+
				settings.getCommandPortRemote()+"/"+settings.getVideoPortRemote());

		NdefMessage message = new NdefMessage(new NdefRecord[]{record});
		nfcAdapter.setNdefPushMessage(message, this);

		try {

			this.mCamera = getCameraInstance();
			if(this.mCamera!=null)
				Log.i("server", "CAMERA IS NOT NULL");
			this.mPreview = new CameraPreview(this, this.mCamera);
			FrameLayout preview = (FrameLayout) this.findViewById(R.id.IDCameraPreview);
			preview.addView(this.mPreview);

			final PololuCard card = PololuCard.createCard(this, PololuCardType.MICRO_MAESTRO);


			this.server = new Server(settings.getCommandPortDrone(), settings.getVideoPortDrone(), card, this.mPreview);
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
			Intent intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.action_main_activity:
			Intent intent2 = new Intent(this, MainActivity.class);
			try {
				this.server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.startActivity(intent2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	public void onPause()
	{
		super.onPause();
		this.mCamera.stopPreview();
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.mCamera.startPreview();
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
			this.mCamera.stopPreview();
			//this.releaseCamera();
		}catch(Exception e){

		}
	}



}
