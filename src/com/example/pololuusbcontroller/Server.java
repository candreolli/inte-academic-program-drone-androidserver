package com.example.pololuusbcontroller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import com.example.pololuusbcontroller.implementations.PololuCard;
/**
 * The server is the elements that manage communications with the remote.
 * 
 * @author Cédric Andreolli - Intel Corporation
 *
 */
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

	/**
	 * Is the server running ?
	 */
	private boolean isRunning = true;
	/**
	 * The break value remember the last Y position sent by the remote.
	 */
	private int lastBreakValue = 100;
	/**
	 * The last position of the left servo motor. It shouldn't be here but due to bad performances
	 * on 3G, it is more secure to manage this on the server side.
	 */
	private int lastPositionLeft = 0;
	/**
	 * The last position of the right servo motor. It shouldn't be here but due to bad performances
	 * on 3G, it is more secure to manage this on the server side.
	 */
	private int lastPositionRight = 0;

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
							//When takeAPicture is called, the current thread is put in waiting state
							//It will be awakend when the picture has been received by the client.
							Server.this.cameraPreview.wait();
							Server.this.cameraPreview.wait((int)settings.getTimeBetweenScreenshots()*1000);
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
		this.card = card;
		this.isRunning = true;
		this.cameraPreview = cameraPreview;
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
		//It important to send a tick to the watcher.
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
			/**
			 * The following solution is not elegant but for safety reasons, it's a good solution.
			 * Some commands need to act on 2 servo motors. This means that 2 commands must be sent through the network.
			 * If one of the command is delayed because of network lag, the result can be very bad. This is why
			 * we must check which motor must be used. If we try to use the motor number 1 (one of the motion control command), we
			 * also need to interact with motor 2. The motor 3 is used to set the speed of the propeller engine.
			 */
			if(servo == 1){
				Settings settings = Settings.getInstance(this.cameraPreview.getContext());
				if(settings.getSwitchServoDirection())
					value = 100-value;
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
		}else if(fullCommand[COMMAND].equals("MOVE_PERC")){
			/**
			 * The following solution is not elegant but for safety reasons, it's a good solution.
			 * Some commands need to act on 2 servo motors. This means that 2 commands must be sent through the network.
			 * If one of the command is delayed because of network lag, the result can be very bad. This is why
			 * we must check which motor must be used. If we try to use the motor number 1 (one of the motion control command), we
			 * also need to interact with motor 2. The motor 3 is used to set the speed of the propeller engine.
			 */
			Settings settings = Settings.getInstance(this.cameraPreview.getContext());
			if(settings.getSwitchServoDirection())
				value = 100-value;
			/*	if(value < 50){
				this.lastPositionLeft = value;
				this.lastPositionRight = 50;
			}else{
				this.lastPositionLeft = 50;
				this.lastPositionRight = value;
			}*/
			this.card.changeTarget(value, servo);
			//	this.card.changeTarget(this.computePositionRight(), servo);

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

	/**
	 * 
	 * @return The client command socket.
	 */
	public Socket getClientCommand() {
		return this.clientCommand;
	}

	/**
	 * 
	 * @return The client video socket.
	 */
	public Socket getClientVideo() {
		return this.clientVideo;
	}

	/**
	 * Handles the request. This method parses the string retrieved on the socket and separate each
	 * component. Once each component are identified, this method execute the request.
	 * @param line The string retrieved from the socket.
	 * @return The request has been correctly executed.
	 */
	private boolean handleRequest(String line) {
		String[] fullCommand = line.split(";");

		if(fullCommand.length != 3)return false;

		return this.executeCommand(fullCommand);
	}

	/**
	 * Initialize the server. This step allows the server to close all open sockets.
	 * @throws IOException
	 */
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
	}

	/**
	 * 
	 * @return Is the server running ?
	 */
	public synchronized boolean isRunning() {
		return this.isRunning;
	}

	@Override
	public void run() {
		//Retrieve the watcher.
		this.watcher = Watcher.getWatcher(this);
		//Start the video streaming process.
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
