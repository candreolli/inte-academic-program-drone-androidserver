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

import com.example.pololuusbcontroller.interfaces.ICommands;
import com.example.pololuusbcontroller.interfaces.IMessageSender;
/**
 * Represents a servo motor.
 * @author Cédric Andreolli - Intel Corporation
 *
 */
public class Servo implements ICommands{
	public enum InterfaceType{BLUETOOTH, USB}
	/**
	 * The usb device connection.
	 */
	private IMessageSender messageSender;
	/**
	 * The position of the servo on the card.
	 */
	private int num;;
	/**
	 * The default constructor.
	 * @param num The position of the servo motor on the card.
	 * @param connection The usb device connection.
	 */
	Servo(int num, IMessageSender messageSender){
		this.num = num;
		this.messageSender = messageSender;
	}

	/**
	 * Initialize the servo motor. The init method set the speed and the acceleration to 0.
	 */
	public void init() {
		this.setSpeed(0);
		this.setAcceleration(0);
	}

	/**
	 * Set the acceleration of the servo motor.
	 */
	public void setAcceleration(int acceleration) {
		this.messageSender.sendSetAcceleration(this.num, acceleration);
	}

	/**
	 * Set the position of the servo motor.
	 */
	public void setPosition(int position) {
		this.messageSender.sendSetPosition(this.num, position);
	}

	/**
	 * Set the speed of the servo motor.
	 */
	public void setSpeed(int speed) {
		this.messageSender.sendSetSpeed(this.num, speed);
	}

}
