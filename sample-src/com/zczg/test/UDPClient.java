package com.zczg.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

	private String toHost;
	private int toPort = 8888;
	private String content;

	public UDPClient(String toHost, int toPort, String content) {
		this.toHost = toHost;
		this.toPort = toPort;
		this.content = content;
	}

	public void send() {

		try {
			InetAddress address = InetAddress.getByName(toHost);
			byte[] data = content.getBytes();
			DatagramPacket packet = new DatagramPacket(data, data.length, address, toPort);

			@SuppressWarnings("resource")
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}