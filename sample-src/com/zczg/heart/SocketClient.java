package com.zczg.heart;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * socket发送工具类
 * 
 * @author zlren
 */
public class SocketClient {

	private int port = 8888;
	private String host;
	private String content;

	public SocketClient(String host, String content) {
		this.host = host;
		this.content = content;
	}

	public void send() {
		try {
			InetAddress address = InetAddress.getByName(host);
			byte[] data = content.getBytes();
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

			@SuppressWarnings("resource")
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "SocketClient [port=" + port + ", host=" + host + ", content=" + content + "]";
	}
}
