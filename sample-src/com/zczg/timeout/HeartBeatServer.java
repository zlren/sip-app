package com.zczg.timeout;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * 心跳服务器
 * 
 * @author zlren
 */
public class HeartBeatServer {

	public Map<String, Long> keepAliveMap;

	public HeartBeatServer(Map<String, Long> keepAliveMap) {
		this.keepAliveMap = keepAliveMap;
	}

	public void serve() {
		new Thread(new ServerTask()).start();
	}

	private class ServerTask implements Runnable {
		@Override
		public void run() {
			try {
				@SuppressWarnings("resource")
				ServerSocket server = new ServerSocket(8888);

				while (true) {
					Socket socket = server.accept();
					Reader reader = new InputStreamReader(socket.getInputStream());

					char[] chars = new char[64];
					int len;
					StringBuilder sb = new StringBuilder();
					while ((len = reader.read(chars)) != -1) {
						sb.append(new String(chars, 0, len));
					}
					String realmId = sb.toString();

					System.out.println("收到" + realmId);
					keepAliveMap.put(realmId, System.currentTimeMillis());
				}
			} catch (Exception ignored) {
			}
		}
	}
}
