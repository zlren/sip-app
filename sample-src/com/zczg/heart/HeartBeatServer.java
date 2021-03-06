package com.zczg.heart;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;

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
				
				InputStream in = new FileInputStream("/root/mss-3.1.633-jboss-as-7.2.0.Final/env/heart.properties");
				Properties p = new Properties();
				p.load(in);

				String ipaddr = p.getProperty("ip");
				
				InetAddress address = InetAddress.getByName(ipaddr);
				
				@SuppressWarnings("resource")
				DatagramSocket socket = new DatagramSocket(8888, address);
				DatagramPacket packet;
				byte[] data = new byte[100];

				while (true) {

					packet = new DatagramPacket(data, data.length);
					socket.receive(packet);

					String content = new String(packet.getData(), 0, packet.getLength());

					logger.info("Got：" + content);

					String[] args = content.split("_");
					String contentType = args[0];
					String remoteRealmId = args[1];
					String msg = args[2];

					// 消息有两种
					if (MyTestApp.CONTENT_TYPE_GO.equals(contentType)) {

						// 对于GO消息，直接回复即可
						String responseContent = MyTestApp.CONTENT_TYPE_BACK + "_" + MyTestApp.realmId + "_" + msg;
						responseContent += "_";
						for(int i = 1; i < 70; i++) {
							responseContent += "f";
						}
						
						SocketClient socketClient = new SocketClient(
								MyTestApp.otherServerMap.get(remoteRealmId).getServerIp(), responseContent);
						socketClient.send();

						logger.info("Response：" + responseContent);

					} else if (MyTestApp.CONTENT_TYPE_BACK.equals(contentType)) {

						// 对于BACK消息直接
						if (MyTestApp.content.get(remoteRealmId).equals(msg)) {
							MyTestApp.keepAliveMap.put(remoteRealmId, System.currentTimeMillis());
							logger.info("Update：" + remoteRealmId);
						}
					}

					System.out.println();
					System.out.println();
				}
			} catch (Exception ignored) {
			}
		}
	}
}
