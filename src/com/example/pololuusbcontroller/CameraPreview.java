package com.example.pololuusbcontroller;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/**
 * The camera preview allows to display.
 * @author Cédric Andreolli - Intel Corporation
 *
 */
@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
	/**
	 * A reference on the camera.
	 */
	private Camera camera;
	/**
	 * The surface holder.
	 */
	private SurfaceHolder holder;

	/**
	 * Default constructor.
	 * @param context The Android application context.
	 * @param camera The camera.
	 */
	public CameraPreview(Context context, Camera camera)
	{
		super(context);
		this.camera = camera;
		this.holder = this.getHolder();
		this.holder.addCallback(this);

		this.setPictureSize();

	}


	/**
	 * To limit the size of data sent over the network, we set the picture size to the minimum
	 * picture size available.
	 */
	private void setPictureSize() {
		//Retrieve the camera parameters. The picture size is set
		//through the camera parameters
		Camera.Parameters params = this.camera.getParameters();
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
			//Then we set the picture size
			params.setPictureSize(chosenSize.width, chosenSize.height);
			this.camera.setParameters(params);
		}
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			this.camera.setPreviewDisplay(holder);
			this.camera.startPreview();
		}
		catch(IOException e){}
	}

	public void surfaceDestroyed(SurfaceHolder holder){}

	/**
	 * Takes a picture. When the picture is taken, the callback is executed.
	 * @param callback The callback executed when the picture is taken.
	 */
	void takeAPicture(final Camera.PictureCallback callback){
		Log.i("server", "takeAPicture");
		Runnable runnable = new Runnable() {
			public void run() {
				try{
					Log.i("server", "Taking a picture in CameraPreview");
					CameraPreview.this.camera.takePicture(null, null, callback);
				}catch(Exception e){
					Log.i("server", "Error message : "+e.getMessage());
					e.printStackTrace();
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}