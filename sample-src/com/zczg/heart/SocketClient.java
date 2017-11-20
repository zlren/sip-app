package com.zczg.heart;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zczg.app.MyTestApp;

/**
 * socket发送工具类
 * 
 * @author zlren
 */
public class SocketClient {

	private static Logger logger = LoggerFactory.getLogger(SocketClient.class);

	private int port;
	private String host;
	private String content;

	public SocketClient(int port, String host, String content) {
		this.port = port;
		this.host = host;
		this.content = content;

		logger.info(toString());
	}

	public void send() {
		try {
			Socket client = new Socket(host, port);
			OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
			writer.write(content);
			writer.flush();
			writer.close();
			client.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "SocketClient [port=" + port + ", host=" + host + ", content=" + content + "]";
	}
}
