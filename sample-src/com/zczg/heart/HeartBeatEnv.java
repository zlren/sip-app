package com.zczg.heart;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatEnv {

	private static Logger logger = LoggerFactory.getLogger(HeartBeatEnv.class);

	public static boolean ENABLE = false;
	public static int CYCLE = 3000;
	public static int TIMEOUT = 8000;
	public static int CHECK = 3000;

	static {
		try {
			InputStream in = new FileInputStream("/root/mss-3.1.633-jboss-as-7.2.0.Final/env/heart.properties");
			Properties p = new Properties();
			p.load(in);

			String enable = p.getProperty("enable");

			CYCLE = Integer.parseInt(p.getProperty("cycle")) * 1000;
			TIMEOUT = Integer.parseInt(p.getProperty("timeout")) * 1000;
			CHECK = Integer.parseInt(p.getProperty("check")) * 1000;

			// 当且仅当enable属性配置成1的时候，心跳检测开启
			if ("1".equals(enable)) {
				ENABLE = true;
				logger.error("周期：" + CYCLE);
				logger.error("超时：" + TIMEOUT);
				logger.error("检查间隔" + CHECK);
				logger.error("heartbeat enabled");
			} else {
				logger.error("heartbeat disabled");
			}
		} catch (Exception e) {
			logger.error("heartbeat disabled");
		}
	}
}
