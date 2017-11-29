package com.zczg.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {

	public static void main(String[] args) throws IOException {

		@SuppressWarnings("resource")
		DatagramSocket socket = new DatagramSocket(8888);
		DatagramPacket packet = null;
		byte[] data = null;

		while (true) {
			data = new byte[100];
			packet = new DatagramPacket(data, data.length);
			socket.receive(packet);
			
			String info = new String(packet.getData(), 0, packet.getLength());
			
			System.out.println(info);
		}
	}
}
