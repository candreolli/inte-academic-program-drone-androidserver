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
/**
 * This class is in charge of sending a JPEG picture over the network.
 * @author Cédric Andreolli - Intel Corporation
 *
 */
public class SendJPGOverNetwork implements PictureCallback{
	/**
	 * This field indicates that a problem occurred
	 */
	private static boolean hasProblemOccured = false;

	/**
	 * 
	 * @return has a problem occurred ?
	 */
	public synchronized static boolean hasProblemOccured() {
		return hasProblemOccured;
	}
	/**
	 * 
	 * @param problemOccured Set that a problem occured.
	 */
	public synchronized static void setProblemOccured(boolean problemOccured) {
		SendJPGOverNetwork.hasProblemOccured = problemOccured;
	}
	/**
	 * The camera preview.
	 */
	private CameraPreview cameraPreview;
	/**
	 * The input stream. This stream is mainly used to receive acknoledgments from
	 * the other side.
	 */
	private InputStream inputStream;
	/**
	 * The output stream used to send the picture over the network.
	 */
	private OutputStream outputStream;

	/**
	 * The default constructor.
	 * @param inputStream The input stream.
	 * @param outputStream The output stream.
	 * @param cameraPreview The camera preview.
	 */
	public SendJPGOverNetwork(InputStream inputStream, OutputStream outputStream, CameraPreview cameraPreview){
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.cameraPreview = cameraPreview;
		setProblemOccured(false);
	}

	@Override
	public void onPictureTaken(final byte[] data, final Camera camera) {
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
