/*
 * Copyright (c) 2013, Cédric Andreolli. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.example.pololuusbcontroller.implementations;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.example.pololuusbcontroller.implementations.Servo.InterfaceType;
import com.example.pololuusbcontroller.interfaces.IMessageSender;

/**
 * Class representing a Pololu card.
 * @author Cédric Andreolli - Intel Corporation
 *
 */
public class PololuCard {
	//Defines the type of card
	/**
	 * Enum used to define all the supported Pololu cards.
	 * @author Cédric Andreolli - Intel Corporation
	 *
	 */
	public enum PololuCardType{MICRO_MAESTRO,
		MINI_MAESTRO_12,
		MINI_MAESTRO_18,
		MINI_MAESTRO_24};
		/**
		 * Create a Pololu card regarding the Android context and the card type.
		 * @param context The Android context.
		 * @param type The card type.
		 * @return A new Pololu card.
		 * @throws InterruptedException
		 */
		public static PololuCard createCard(Context context, PololuCardType type, InterfaceType itype)
				throws InterruptedException{
			switch(type){
			case MICRO_MAESTRO:
				return new PololuCard(context, 6, itype);
			case MINI_MAESTRO_12:
				return new PololuCard(context, 12, itype);
			case MINI_MAESTRO_18:
				return new PololuCard(context, 18, itype);
			case MINI_MAESTRO_24:
				return new PololuCard(context, 24, itype);
			default:
				return new PololuCard(context, 6, itype);
			}
		}
		//Reference to the USB connection
		private UsbDeviceConnection connection;
		//Reference to the USB device
		private UsbDevice device;
		IMessageSender messageSender;

		//The list of servo motors
		private List<Servo> servoList;

		private InterfaceType type;

		//Reference to the USB manager
		private UsbManager usbManager;

		/**
		 * Default constructor (private)
		 * To create a new instance of this class, use the {@link createCard(Context context, PololuCardType type) createCard} method.
		 * @param context The android context
		 * @param servoNumber The number of servos
		 * @throws InterruptedException
		 */
		private PololuCard(Context context, int servoNumber, InterfaceType itype){
			this.servoList = new ArrayList<Servo>();//Instantiate the list
			this.type = itype;
			switch (this.type) {
			case USB:
				this.messageSender = new USBMessageSender(context);
				break;
			case BLUETOOTH:
				this.messageSender = new BluetoothMessageSender();
				break;
			default:
				this.messageSender = new BluetoothMessageSender();
				break;
			}

			this.createServos(servoNumber);
			this.init();
		}

		/**
		 * Change the acceleration of the motor.
		 * @param acceleration The acceleration.
		 * @param channel The servo motor position.
		 */
		public void changeAcceleration(int acceleration, int channel){
			this.servoList.get(channel).setAcceleration(acceleration);
		}

		/**
		 * Change the speed of the motor.
		 * @param speed The speed.
		 * @param channel The servo motor position.
		 */
		public void changeSpeed(int speed, int channel){
			this.servoList.get(channel).setSpeed(speed);
		}

		/**
		 * Change the position of the servo motor identified by the number channel.
		 * The position must be sent through the target variable as a percentage.
		 * @param target The percentage representing the rotation angle of the servo motor. 50 represents the middle position.
		 * @param channel The servo motor position.
		 */
		public void changeTarget(int target, int channel){
			this.servoList.get(channel).setPosition(target);
		}

		/**
		 * Creates the servos regarding the servoNumber parameter.
		 * @param servoNumber The number of servo motors on the card.
		 */
		private void createServos(int servoNumber) {
			for(int i=0; i<servoNumber; i++){
				this.servoList.add(new Servo(i, this.messageSender));
			}
		}

		/**
		 * Must be called to initialize the servo motors.
		 */
		private void init() {
			try{
				for(Servo s : this.servoList)
					s.init();
			}catch(Exception e){
				Log.e("server", "Error while initializing servo...");
			}
		}
}
