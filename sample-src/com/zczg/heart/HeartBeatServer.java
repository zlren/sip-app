package com.zczg.heart;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zczg.app.MyTestApp;

/**
 * 心跳服务器
 * 
 * @author zlren
 */
public class HeartBeatServer {
	
	private static Logger logger = LoggerFactory.getLogger(HeartBeatServer.class);

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
					String content = sb.toString();
					
					logger.info("收到消息：" + content);

					// 比如2域收到1域来的消息
					String[] args = content.split("_");
					String contentType = args[0];
					String remoteRealmId = args[1];
					String msg = args[2];

					// 消息有两种
					if (MyTestApp.CONTENT_TYPE_GO.equals(contentType)) {
						// 对于GO消息，直接回复即可
						String responseContent = MyTestApp.CONTENT_TYPE_BACK + "_" + MyTestApp.realmId + "_" + msg;
						SocketClient socketClient = new SocketClient(8888,
								MyTestApp.otherServerMap.get(remoteRealmId).getServerIp(), responseContent);
						socketClient.send();
						
						logger.info("回复了：" + responseContent);
						
					} else if (MyTestApp.CONTENT_TYPE_BACK.equals(contentType)) {
						// 对于BACK消息直接
						if (MyTestApp.content.get(remoteRealmId).equals(msg)) {
							MyTestApp.keepAliveMap.put(remoteRealmId, System.currentTimeMillis());
							logger.info("更新了时间戳：" + remoteRealmId);
						}
					}
				}
			} catch (Exception ignored) {
			}
		}
	}
}
