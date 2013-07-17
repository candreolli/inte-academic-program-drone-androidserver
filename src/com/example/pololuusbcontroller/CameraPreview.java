package com.example.pololuusbcontroller;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
	private boolean isSendingData;
	private Camera mCamera;
	private SurfaceHolder mHolder;


	public CameraPreview(Context context, Camera camera)
	{
		super(context);
		this.mCamera = camera;
		this.mHolder = this.getHolder();
		this.mHolder.addCallback(this);
		this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Camera.Parameters params = this.mCamera.getParameters();
		List<Size> sizes = params.getSupportedPictureSizes();

		// Iterate through all available resolutions and choose one.
		// The chosen resolution will be stored in mSize.
		Size chosenSize = null;
		int currentSize = 1280*768;
		for (Size size : sizes) {
			int newSize = size.height*size.width;
			if (newSize < currentSize) {
				chosenSize = size;
			}
		}
		if(chosenSize != null){
			params.setPictureSize(chosenSize.width, chosenSize.height);
			this.mCamera.setParameters(params);
		}

		//isSendingData = true;
	}

	/*public void takeAPicture(final InputStream inputStream, final OutputStream outputStream){
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				mCamera.takePicture(null, null, getJpegCallback());
			}

			private PictureCallback getJpegCallback() {

				PictureCallback jpeg=new PictureCallback() {
					@Override
					public void onPictureTaken(final byte[] data, final Camera camera) {
						Runnable runnable = new Runnable() {

							@Override
							public void run() {
								PrintWriter writer = new PrintWriter(outputStream);
								BufferedInputStream bufIn = new BufferedInputStream(inputStream);
								BufferedOutputStream bufOut = new BufferedOutputStream(outputStream);
								BufferedReader reader = new BufferedReader(new InputStreamReader(bufIn));
								if (data != null) {
									Camera.Parameters parameters = camera.getParameters();
									int width = parameters.getPreviewSize().width;
									int height = parameters.getPreviewSize().height;
									Bitmap bmp = null;
									bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
									try{
										mCamera.stopPreview();
										mCamera.startPreview();
									}catch(Exception e){

									}
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
										isSendingData = false;
									}
								}
								try{
									mCamera.stopPreview();
									mCamera.startPreview();
								}catch(Exception e){

								}
								synchronized (CameraPreview.this) {
									CameraPreview.this.notifyAll();
								}
							}
						};
						Thread t = new Thread(runnable);
						t.start();

					}
				};
				return jpeg;
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}*/



	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		//		if(this.mHolder.getSurface() == null)
		//			return;
		//
		//		try{
		//			this.mCamera.stopPreview();
		//		}catch(Exception e){}
		//
		//		try{
		//			this.mCamera.setPreviewDisplay(this.mHolder);
		//			this.mCamera.startPreview();
		//			//isSendingData = true;
		//		}catch(Exception e){}
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			this.mCamera.setPreviewDisplay(holder);
			this.mCamera.startPreview();
			//isSendingData = true;
		}
		catch(IOException e){}
	}

	public void surfaceDestroyed(SurfaceHolder holder){}

	void takeAPicture(final Camera.PictureCallback callback){
		Log.i("server", "takeAPicture");
		Runnable runnable = new Runnable() {
			public void run() {
				try{
					Log.i("server", "Taking a picture in CameraPreview");
					if(callback == null)
						Log.i("server", "Callbacknis null");
					else
						Log.i("server", "Callbacknis NOT null");

					CameraPreview.this.mCamera.takePicture(null, null, callback);
				}catch(Exception e){
					Log.i("server", "Exception in take a picture");
					Log.i("server", "Error message : "+e.getMessage());
					e.printStackTrace();
				}
			}
		};
		Log.i("server", "takeAPicture before thread");
		Thread t = new Thread(runnable);
		t.start();
		Log.i("server", "takeAPicture thread launched");
	}



	/*	public boolean isSendingData() {
		return isSendingData;
	}



	public void setIsSendingData(boolean b) {
		isSendingData = b;
	}*/

}