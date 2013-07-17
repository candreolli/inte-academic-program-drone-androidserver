package com.example.pololuusbcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Settings {
	private static final String COMMAND_PORT_DRONE = "command_port_drone";
	private static final String COMMAND_PORT_REMOTE = "command_port_remote";
	private static final int DEFAULT_COMMAND_PORT_DRONE_VALUE = 8800;
	private static final int DEFAULT_COMMAND_PORT_REMOTE_VALUE = 8887;
	private static final String DEFAULT_IP_ADDRESS = "82.225.152.198";
	private static final int DEFAULT_VIDEO_PORT_DRONE_VALUE = 8801;
	private static final int DEFAULT_VIDEO_PORT_REMOTE_VALUE = 8888;
	private static Settings instance = null;
	private static final String IP_ADDRESS = "ip_address";
	public static final String PREF_SETTING = "setting_file";
	private static final String VIDEO_PORT_DRONE = "video_port_drone";
	private static final String VIDEO_PORT_REMOTE = "video_port_remote";

	public static Settings getInstance(Context ctx){
		if(instance == null)
			instance = new Settings(ctx);
		return instance;
	}
	private int commandPortDrone = 0;
	private int commandPortRemote = 0;

	private Context context = null;

	private String ipAddress;

	private SharedPreferences settings = null;
	private int videoPortDrone = 0;
	private int videoPortRemote = 0;

	private Settings(Context ctx){
		this.context = ctx;
		this.settings = this.context.getSharedPreferences(PREF_SETTING, Context.MODE_PRIVATE);

	}

	public int getCommandPortDrone() {
		if(this.commandPortDrone == 0){
			this.commandPortDrone = this.settings.getInt(COMMAND_PORT_DRONE, DEFAULT_COMMAND_PORT_DRONE_VALUE);
		}
		return this.commandPortDrone;
	}

	public int getCommandPortRemote() {
		if(this.commandPortRemote == 0){
			this.commandPortRemote = this.settings.getInt(COMMAND_PORT_REMOTE, DEFAULT_COMMAND_PORT_REMOTE_VALUE);
		}
		return this.commandPortRemote;
	}

	public String getIPAddress() {
		if(this.ipAddress == "" || this.ipAddress == null){
			this.ipAddress = this.settings.getString(IP_ADDRESS, DEFAULT_IP_ADDRESS);
		}
		return this.ipAddress;
	}

	public int getVideoPortDrone() {
		if(this.videoPortDrone == 0){
			this.videoPortDrone = this.settings.getInt(VIDEO_PORT_DRONE, DEFAULT_VIDEO_PORT_DRONE_VALUE);
		}
		return this.videoPortDrone;
	}

	public int getVideoPortRemote() {
		if(this.videoPortRemote == 0){
			this.videoPortRemote = this.settings.getInt(VIDEO_PORT_REMOTE, DEFAULT_VIDEO_PORT_REMOTE_VALUE);
		}
		return this.videoPortRemote;
	}

	public void setCommandPortDrone(int commandPort) {
		this.commandPortDrone = commandPort;
		Editor editor = this.settings.edit();
		editor.putInt(COMMAND_PORT_DRONE, commandPort);
		editor.commit();
	}

	public void setCommandPortRemote(int commandPort) {
		this.commandPortRemote = commandPort;
		Editor editor = this.settings.edit();
		editor.putInt(COMMAND_PORT_REMOTE, commandPort);
		editor.commit();
	}

	public void setIPAddress(String ipAddress){
		Log.i("server", "saving address : "+ipAddress);
		this.ipAddress = ipAddress;
		Editor editor = this.settings.edit();
		editor.putString(IP_ADDRESS, ipAddress);
		editor.commit();
	}

	public void setVideoPortDrone(int videoPort) {
		this.videoPortDrone = videoPort;
		Editor editor = this.settings.edit();
		editor.putInt(VIDEO_PORT_DRONE, videoPort);
		editor.commit();
	}

	public void setVideoPortRemote(int videoPort) {
		this.videoPortRemote = videoPort;
		Editor editor = this.settings.edit();
		editor.putInt(VIDEO_PORT_REMOTE, videoPort);
		editor.commit();
	}
}
