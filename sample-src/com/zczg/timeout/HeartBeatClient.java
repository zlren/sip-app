package com.zczg.timeout;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 心跳客户端
 * 
 * @author zlren
 */
public class HeartBeatClient {

	private Map<String, Realm> otherServerMap;
	private String myRealmId;

	public HeartBeatClient(Map<String, Realm> otherServerList, String myRealmId) {
		this.otherServerMap = otherServerList;
		this.myRealmId = myRealmId;
	}

	public void serve() {
		new Thread(new ClientTask()).start();
	}

	private class ClientTask implements Runnable {

		/**
		 * 每隔3秒把自己的realmId报告给所有其他域
		 */
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(3000);
					for (Entry<String, Realm> entry : otherServerMap.entrySet()) {
						try {
							Socket client = new Socket(entry.getValue().getServerIp(), 8888);
							OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
							writer.write(myRealmId);
							writer.flush();
							writer.close();
							client.close();
						} catch (IOException ignored) {
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
