package com.zczg.timeout;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zczg.util.JDBCUtils;

public class HeartBeatEnv {

	public static boolean ENABLE = false;
	private static Logger logger = LoggerFactory.getLogger(JDBCUtils.class);

	static {
		try {
			InputStream in = new FileInputStream("/root/mss-3.1.633-jboss-as-7.2.0.Final/env/heart.properties");
			Properties p = new Properties();
			p.load(in);

			String enable = p.getProperty("enable");

			// 当且仅当enable属性配置成1的时候，心跳检测开启
			if ("1".equals(enable)) {
				ENABLE = true;
				logger.info("心跳检测开启");
			} else {
				logger.info("心跳检测关闭");
			}
		} catch (Exception e) {
		}
	}
}
