package com.example.pololuusbcontroller;

import java.util.Date;

import android.util.Log;

public class Watcher{
	private static Watcher instance = null;
	public final static long MAX_TIME = 5000;
	public static Watcher getWatcher(Server server){
		if(instance == null)
			instance = new Watcher(server);
		instance.server = server;
		instance.setRunning(true);
		return instance;
	}
	private boolean isRunning = true;
	private Date lastTime = null;
	private Server server = null;

	private Thread t;

	private Watcher(Server server) {
		this.server = server;
		this.lastTime = new Date();
		this.t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(Watcher.this.isRunning()){
					Date currentDate = new Date();
					long diff = currentDate.getTime() - Watcher.this.lastTime.getTime();
					if(diff > MAX_TIME){
						Log.i("server", "Executing STOP");
						Watcher.this.server.executeCommand(new String[]{"MOVE", "3", "0"});
						Watcher.this.server.executeCommand(new String[]{"MOVE", "1", "30"});
						Watcher.this.lastTime = currentDate;
					}else{
						try {
							Thread.sleep(MAX_TIME - diff);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				instance = null;
			}
		});
		this.t.start();
	}

	public synchronized boolean isRunning() {
		return this.isRunning;
	}

	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public void tick(){
		this.lastTime = new Date();
	}

}
