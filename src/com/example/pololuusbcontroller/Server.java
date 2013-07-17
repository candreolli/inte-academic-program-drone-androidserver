package com.example.pololuusbcontroller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import android.util.Log;

import com.example.pololuusbcontroller.implementations.PololuCard;

public class Server extends Thread{
	/**
	 * The index of the command.
	 */
	public static final int COMMAND = 0;

	/**
	 * The index of the servo identifier.
	 */
	public static final int SERVO = 1;

	/**
	 * The index of the value.
	 */
	public static final int VALUE = 2;

	/**
	 * Reference to the camera preview.
	 */
	private CameraPreview cameraPreview = null;

	/**
	 * Reference to the Pololu card.
	 */
	private PololuCard card = null;

	/**
	 * The socket handling the commands.
	 */
	private Socket clientCommand = null;

	/**
	 * The socket handling the video stream.
	 */
	private Socket clientVideo = null;

	private boolean isRunning = true;

	private int lastBreakValue = 100;

	private Date lastCommand = null;

	private int lastPositionLeft = 0;


	private int lastPositionRight = 0;

	/**
	 * The command port.
	 */
	private int portCommand;
	/**
	 * The video port.
	 */
	private int portVideo;
	/**
	 * The server socket handling the commands.
	 */
	private ServerSocket serverSocketCommands = null;
	/**
	 * The server socket handling the video stream.
	 */
	private ServerSocket serverSocketVideoStream = null;

	/**
	 * The runnable used to ask for a picture.
	 */
	private Runnable videoRunnable = new Runnable() {


		public void run() {
			BufferedInputStream in;
			BufferedReader reader;
			while(Server.this.isRunning()){
				try {
					Settings settings = Settings.getInstance(Server.this.cameraPreview.getContext());
					Server.this.clientVideo = new Socket(InetAddress.getByName(settings.getIPAddress()), settings.getVideoPortDrone());
					in = new BufferedInputStream(Server.this.clientVideo.getInputStream());
					reader = new BufferedReader(new InputStreamReader(in));
					SendJPGOverNetwork callback = new SendJPGOverNetwork(Server.this.clientVideo.getInputStream(),
							Server.this.clientVideo.getOutputStream(), Server.this.cameraPreview);
					while(!SendJPGOverNetwork.hasProblemOccured()){
						String command = reader.readLine();
						Log.i("server", "command received : "+command);
						Server.this.cameraPreview.takeAPicture(callback);
						synchronized (Server.this.cameraPreview) {
							Server.this.cameraPreview.wait();
						}
					}

				}catch(Exception e){
					e.printStackTrace();
					Log.i("server", "ERROR FOUND IN SERVER");
					break;
				}
			}
		}
	};

	private Watcher watcher = null;
	/**
	 * Default constructor. It builds a Server instance and the ServerSockets. It creates 2 server sockets. One for
	 * receiving the commands, an other for sending the video stream.
	 * You need to call the start() method to really listen on the 2 created sockets.
	 * @param commandPort The port used to send the commands.
	 * @param videoPort The port used to send the video stream.
	 * @param card The Pololu card.
	 * @param cameraPreview The camera Preview.
	 * @throws IOException
	 */
	public Server(int commandPort, int videoPort, PololuCard card, CameraPreview cameraPreview) throws IOException {
		this.portCommand = commandPort;
		this.portVideo = videoPort;
		this.card = card;
		this.isRunning = true;
		this.cameraPreview = cameraPreview;
		this.lastCommand = new Date();
		this.init();
	}
	/**
	 * Close the server.
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.setRunning(false);
		if(this.serverSocketCommands != null){
			if(!this.serverSocketCommands.isClosed())
				this.serverSocketCommands.close();
			this.serverSocketCommands = null;
		}
		if(this.serverSocketVideoStream != null){
			if(!this.serverSocketVideoStream.isClosed())
				this.serverSocketVideoStream.close();
			this.serverSocketVideoStream = null;
		}
	}
	private int computePositionLeft() {
		return this.lastPositionLeft < this.lastBreakValue ? this.lastPositionLeft : this.lastBreakValue;
	}

	private int computePositionRight() {
		return this.lastPositionRight > 100 - this.lastBreakValue ? this.lastPositionRight : 100 - this.lastBreakValue;

	}
	/**
	 * Execute a command. The command is a string array where
	 * fullCommand[COMMAND] stands for the Pololu command,  fullCommand[SERVO] represents the position
	 * of the servo, and fullCommand[VALUE] represents the new value.
	 * @param fullCommand an array that contains the command's elements.
	 * @return The command has been successfully sent to the pololu card.
	 */
	public synchronized boolean executeCommand(String[] fullCommand) {
		this.watcher.tick();
		int value = 0;
		int servo = 0;

		try{
			value = Integer.parseInt(fullCommand[VALUE]);
			servo = Integer.parseInt(fullCommand[SERVO]);
		}catch(Exception e){
			return false;
		}

		if(fullCommand[COMMAND].equals("MOVE")){
			if(servo == 1){
				if(value < 50){
					this.lastPositionLeft = value;
					this.lastPositionRight = 50;
				}else{
					this.lastPositionLeft = 50;
					this.lastPositionRight = value;
				}
				this.card.changeTarget(this.computePositionLeft(), 1);
				this.card.changeTarget(this.computePositionRight(), 2);
			}else if(servo == 5){
				this.lastBreakValue = value;
				this.card.changeTarget(this.computePositionLeft(), 1);
				this.card.changeTarget(this.computePositionRight(), 2);
			}else{
				this.card.changeTarget(value, servo);
			}
			return true;
		}else if(fullCommand[COMMAND].equals("SPEED")){
			this.card.changeSpeed(value, servo);
			return true;
		}else if(fullCommand[COMMAND].equals("ACC")){
			this.card.changeAcceleration(value, servo);
			return true;
		}

		return false;
	}
	public Socket getClientCommand() {
		return this.clientCommand;
	}


	public Socket getClientVideo() {
		return this.clientVideo;
	}

	private boolean handleRequest(String line) {
		String[] fullCommand = line.split(";");

		if(fullCommand.length != 3)return false;

		return this.executeCommand(fullCommand);
	}

	public void init() throws IOException{
		Log.i("server", "init() called");
		if(this.serverSocketCommands != null){
			if(!this.serverSocketCommands.isClosed())
				this.serverSocketCommands.close();
			this.serverSocketCommands = null;
		}
		if(this.serverSocketVideoStream != null){
			if(!this.serverSocketVideoStream.isClosed())
				this.serverSocketVideoStream.close();
			this.serverSocketVideoStream = null;
		}

		//		try {
		//			Log.i("server", "Creating the server socket");
		//			//serverSocketCommands = new ServerSocket(commandPort);
		//			this.serverSocketCommands = new ServerSocket();
		//			this.serverSocketCommands.setReuseAddress(true);
		//			this.serverSocketCommands.bind(new InetSocketAddress(this.portCommand));
		//
		//			//serverSocketVideoStream = new ServerSocket(videoPort);
		//			this.serverSocketVideoStream = new ServerSocket();
		//			this.serverSocketVideoStream.setReuseAddress(true);
		//			this.serverSocketVideoStream.bind(new InetSocketAddress(this.portVideo));
		//
		//			Log.i("server", "Server socket created");
		//		} catch (Exception e) {
		//			Log.i("server", "Exception : "+e.getMessage());
		//			e.printStackTrace();
		//		}

	}
	public synchronized boolean isRunning() {
		return this.isRunning;
	}
	@Override
	public void run() {
		this.watcher = Watcher.getWatcher(this);

		Thread t = new Thread(this.videoRunnable);
		t.start();



		while(this.isRunning()){
			try {
				Log.i("server", "Server command waiting for client");
				Settings settings = Settings.getInstance(this.cameraPreview.getContext());
				this.clientCommand = new Socket(InetAddress.getByName(settings.getIPAddress()), settings.getCommandPortDrone());
				Log.i("server", "Connection received");

				BufferedReader reader = new BufferedReader(new InputStreamReader(this.clientCommand.getInputStream()));
				PrintWriter writer = new PrintWriter(this.clientCommand.getOutputStream());
				String line = null;
				while((line = reader.readLine()) != null){
					boolean res = this.handleRequest(line);
					if(res)
						writer.write("OK\r\n");
					else
						writer.write("FAIL\r\n");
					writer.flush();
				}
				Log.i("server", "Nothing to do");

			} catch (Exception e) {
				Log.e("server", "Error Thread command:  "+e.getMessage());
				e.printStackTrace();
				break;
			}
		}
		this.watcher.setRunning(false);
	}



	public void setClientCommand(Socket clientCommand) {
		this.clientCommand = clientCommand;
	}


	public void setClientVideo(Socket clientVideo) {
		this.clientVideo = clientVideo;
	}

	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
}
