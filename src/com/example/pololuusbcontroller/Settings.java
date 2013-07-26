package com.example.pololuusbcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
/**
 * This class holds the application settings. This is a singleton.
 * @author Cédric Andreolli - Intel Corporation
 *
 */
public class Settings {
	private static final String COMMAND_PORT_DRONE = "command_port_drone";
	private static final String COMMAND_PORT_REMOTE = "command_port_remote";
	private static final int DEFAULT_COMMAND_PORT_DRONE_VALUE = 8800;
	private static final int DEFAULT_COMMAND_PORT_REMOTE_VALUE = 8887;
	private static final String DEFAULT_IP_ADDRESS = "82.225.152.198";
	private static final boolean DEFAULT_SWITCH_SERVO_DIRECTION = false;
	private static final int DEFAULT_VIDEO_PORT_DRONE_VALUE = 8801;
	private static final int DEFAULT_VIDEO_PORT_REMOTE_VALUE = 8888;
	private static Settings instance = null;
	private static final String IP_ADDRESS = "ip_address";
	public static final String PREF_SETTING = "setting_file";
	private static final String SWITCH_SERVO_DIRECTION = "switch_servo_direction";
	private static final String TIME_BETWEEN_SCREENSHOTS = "time_between_screenshots";
	private static final String VIDEO_PORT_DRONE = "video_port_drone";
	private static final String VIDEO_PORT_REMOTE = "video_port_remote";


	/**
	 * Access the singleton.
	 * @param ctx The Android application context.
	 * @return The singleton instance of this class.
	 */
	public static Settings getInstance(Context ctx){
		if(instance == null)
			instance = new Settings(ctx);
		return instance;
	}
	/**
	 * The command port used by the drone
	 */
	private int commandPortDrone = 0;
	/**
	 * The command port used by the remote
	 */
	private int commandPortRemote = 0;
	/**
	 * The android application context.
	 */
	private Context context = null;
	/**
	 * The IP address.
	 */
	private String ipAddress;
	/**
	 * Shared preference used to store and load preferences.
	 */
	private SharedPreferences settings = null;
	/**
	 * Indicates that the servo don't work in the natural direction.
	 */
	private boolean switchServoDirection = false;
	/**
	 * Time in seconds between 2 screenshots
	 */
	private float timeBetweenScreenshots = 1;

	/**
	 * The video port used by the drone
	 */
	private int videoPortDrone = 0;

	/**
	 * The video port used by the remote.
	 */
	private int videoPortRemote = 0;

	/**
	 * Default constructor.
	 * @param ctx The Android application context.
	 */
	private Settings(Context ctx){
		this.context = ctx;
		//We load the shared preferences.
		this.settings = this.context.getSharedPreferences(PREF_SETTING, Context.MODE_PRIVATE);

	}

	/**
	 * 
	 * @return The command port used by the drone.
	 */
	public int getCommandPortDrone() {
		if(this.commandPortDrone == 0){
			this.commandPortDrone = this.settings.getInt(COMMAND_PORT_DRONE, DEFAULT_COMMAND_PORT_DRONE_VALUE);
		}
		return this.commandPortDrone;
	}

	/**
	 * 
	 * @return The command port used by the remote.
	 */
	public int getCommandPortRemote() {
		if(this.commandPortRemote == 0){
			this.commandPortRemote = this.settings.getInt(COMMAND_PORT_REMOTE, DEFAULT_COMMAND_PORT_REMOTE_VALUE);
		}
		return this.commandPortRemote;
	}

	/**
	 * 
	 * @return The IP address.
	 */
	public String getIPAddress() {
		if(this.ipAddress == "" || this.ipAddress == null){
			this.ipAddress = this.settings.getString(IP_ADDRESS, DEFAULT_IP_ADDRESS);
		}
		return this.ipAddress;
	}

	/**
	 * 
	 * @return The state of switch servo direction
	 */
	public boolean getSwitchServoDirection() {
		this.switchServoDirection = this.settings.getBoolean(SWITCH_SERVO_DIRECTION, DEFAULT_SWITCH_SERVO_DIRECTION);
		return this.switchServoDirection;
	}

	public float getTimeBetweenScreenshots() {
		this.timeBetweenScreenshots = this.settings.getFloat(TIME_BETWEEN_SCREENSHOTS, 1f);
		Log.i("server", "loading float : "+this.timeBetweenScreenshots);
		return this.timeBetweenScreenshots;
	}

	/**
	 * 
	 * @return The video port used by the drone.
	 */
	public int getVideoPortDrone() {
		if(this.videoPortDrone == 0){
			this.videoPortDrone = this.settings.getInt(VIDEO_PORT_DRONE, DEFAULT_VIDEO_PORT_DRONE_VALUE);
		}
		return this.videoPortDrone;
	}

	/**
	 * 
	 * @return The video port used by the remote.
	 */
	public int getVideoPortRemote() {
		if(this.videoPortRemote == 0){
			this.videoPortRemote = this.settings.getInt(VIDEO_PORT_REMOTE, DEFAULT_VIDEO_PORT_REMOTE_VALUE);
		}
		return this.videoPortRemote;
	}
	/**
	 * 
	 * @param commandPort The command port used by the drone
	 */
	public void setCommandPortDrone(int commandPort) {
		this.commandPortDrone = commandPort;
		Editor editor = this.settings.edit();
		editor.putInt(COMMAND_PORT_DRONE, commandPort);
		editor.commit();
		editor.apply();
	}

	/**
	 * 
	 * @param commandPort The command port used by the remote
	 */
	public void setCommandPortRemote(int commandPort) {
		this.commandPortRemote = commandPort;
		Editor editor = this.settings.edit();
		editor.putInt(COMMAND_PORT_REMOTE, commandPort);
		editor.commit();
		editor.apply();
	}

	/**
	 * 
	 * @param ipAddress The IP address of the remote (or the tunnel server).
	 */
	public void setIPAddress(String ipAddress){
		Log.i("server", "saving address : "+ipAddress);
		this.ipAddress = ipAddress;
		Editor editor = this.settings.edit();
		editor.putString(IP_ADDRESS, ipAddress);
		editor.commit();
		editor.apply();
	}

	/**
	 * 
	 * @param videoPort The video port used by the drone
	 */
	public void setSwitchServoDirection(boolean dir) {
		this.switchServoDirection = dir;
		Editor editor = this.settings.edit();
		editor.putBoolean(SWITCH_SERVO_DIRECTION, dir);
		editor.commit();
		editor.apply();
	}

	public void setTimeBetweenScreenshots(float timeBetweenScreenshots) {
		Log.i("server", "saving float : "+timeBetweenScreenshots);
		this.timeBetweenScreenshots = timeBetweenScreenshots;
		Editor editor = this.settings.edit();
		editor.putFloat(TIME_BETWEEN_SCREENSHOTS, timeBetweenScreenshots);
		editor.commit();
		editor.apply();
	}

	/**
	 * 
	 * @param videoPort The video port used by the drone
	 */
	public void setVideoPortDrone(int videoPort) {
		this.videoPortDrone = videoPort;
		Editor editor = this.settings.edit();
		editor.putInt(VIDEO_PORT_DRONE, videoPort);
		editor.commit();
		editor.apply();
	}

	/**
	 * 
	 * @param videoPort The video port used by the remote
	 */
	public void setVideoPortRemote(int videoPort) {
		this.videoPortRemote = videoPort;
		Editor editor = this.settings.edit();
		editor.putInt(VIDEO_PORT_REMOTE, videoPort);
		editor.commit();
		editor.apply();
	}
}
