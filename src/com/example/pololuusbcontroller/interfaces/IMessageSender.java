package com.example.pololuusbcontroller.interfaces;

public interface IMessageSender {

	public void sendSetAcceleration(int num, int acceleration);

	public void sendSetPosition(int num, int position);

	public void sendSetSpeed(int num, int speed);

}
