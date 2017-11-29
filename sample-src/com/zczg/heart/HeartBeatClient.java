package com.zczg.heart;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zczg.app.MyTestApp;
import com.zczg.util.RandomCharUtil;

/**
 * 心跳客户端 客户端只做一件事，线程开启后每隔几秒向其他所有的SipServer发送心跳信息
 * 
 * @author zlren
 */
public class HeartBeatClient {

	private static Logger logger = LoggerFactory.getLogger(HeartBeatClient.class);

	public void serve() {
		new Thread(new ClientTask()).start();
	}

	private class ClientTask implements Runnable {

		/**
		 * 每隔 HeartBeatEnv.CYCLE 把自己的realmId报告给所有其他域
		 */
		@Override
		public void run() {
			while (true) {
				try {

					Thread.sleep(3000);

					for (Entry<String, Realm> entry : MyTestApp.otherServerMap.entrySet()) {

						// 1向2发消息，内容是1_xxxx
						String random4Number = RandomCharUtil.getRandomNumberChar(4);

						// 在content中存储 (2, xxxx)，以便当2回了消息后进行比对
						MyTestApp.content.put(entry.getKey(), random4Number);

						// 具体消息的内容是 GO_1_xxxx
						String content = MyTestApp.CONTENT_TYPE_GO + "_" + MyTestApp.realmId + "_" + random4Number;

						SocketClient socketClient = new SocketClient(entry.getValue().getServerIp(), content);
						socketClient.send();

						logger.info("给域[" + entry.getKey() + "]发送出了消息：" + content);

						System.out.println();
						System.out.println();
					}
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			}
		}
	}
}
