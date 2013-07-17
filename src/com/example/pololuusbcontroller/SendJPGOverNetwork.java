package com.example.pololuusbcontroller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

public class SendJPGOverNetwork implements PictureCallback{
	/**
	 * This field indicates that a problem occured
	 */
	private static boolean hasProblemOccured = false;
	public synchronized static boolean hasProblemOccured() {
		return hasProblemOccured;
	}

	public synchronized static void setProblemOccured(boolean problemOccured) {
		SendJPGOverNetwork.hasProblemOccured = problemOccured;
	}

	private CameraPreview cameraPreview;

	private InputStream inputStream;

	private OutputStream outputStream;

	public SendJPGOverNetwork(InputStream inputStream, OutputStream outputStream, CameraPreview cameraPreview){
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.cameraPreview = cameraPreview;
		setProblemOccured(false);
	}

	@Override
	public void onPictureTaken(final byte[] data, final Camera camera) {
		Log.i("server", "Picture has been taken");

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Log.i("server", "SendPictureOverNetwork");
				PrintWriter writer = new PrintWriter(SendJPGOverNetwork.this.outputStream);
				BufferedInputStream bufIn = new BufferedInputStream(SendJPGOverNetwork.this.inputStream);
				BufferedOutputStream bufOut = new BufferedOutputStream(SendJPGOverNetwork.this.outputStream);
				BufferedReader reader = new BufferedReader(new InputStreamReader(bufIn));
				if (data != null) {
					Camera.Parameters parameters = camera.getParameters();
					int width = parameters.getPreviewSize().width;
					int height = parameters.getPreviewSize().height;
					Bitmap bmp = null;
					bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					width = 300;
					height = 180;
					bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmp.compress(CompressFormat.JPEG, 60, stream);
					byte[] data2 = stream.toByteArray();
					bmp.recycle();
					bmp = null;
					//send the data (uncompressed)
					try {
						writer.println(data2.length);
						writer.flush();
						reader.readLine();
						writer.println(width);
						writer.flush();

						reader.readLine();
						writer.println(height);
						writer.flush();

						reader.readLine();

						bufOut.write(data2);
						bufOut.flush();

						reader.readLine();
					} catch (Exception e) {
						e.printStackTrace();
						setProblemOccured(true);
					}
				}
				try{
					camera.stopPreview();
					camera.startPreview();
				}catch(Exception e){}
				synchronized (SendJPGOverNetwork.this.cameraPreview) {
					SendJPGOverNetwork.this.cameraPreview.notifyAll();
				}
			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}

}
