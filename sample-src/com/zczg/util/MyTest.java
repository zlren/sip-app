package com.zczg.util;

import com.zczg.app.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyTest {
	private static Logger logger = LoggerFactory.getLogger(MyTest.class);
	private Integer id;
	private String name;
	 private static CurEnv cur_env = new CurEnv();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) {
		// logger.info("test");
		//
		// System.out.println(cur_env.getSettingsInt().get("user_idle"));
//		 Map<String,Object> map = JDBCUtils.queryForMap("select * from user where name = 'bob'");
//		 System.out.println(map.get("passwd"));
		//
		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String now = df.format(new Date());
		// System.out.println(now);
//		String s = "sip:alex@10.109.247.126";
//		String[] ss = s.split("[@:;]");
//
//		for (int i = 0; i < ss.length; i++) {
//			System.out.println(i + " " + ss[i]);
//		}
//		System.out.print(MediaConf.DTMF_0);
//		
//		URI uri = URI.create("mscontrol://10.109.247.126:2427/MediaSession1/NetworkConnection2");
		
//		Map<String, Object> check = JDBCUtils.queryForMap(
//				"select * from p2psession where active = 1 and belong = '" + "111" + "'");
//		
//		if(check.get("belong") == null) {
//			System.out.println(111);
//		}

		// for(Map.Entry<String, Object> entry: map.entrySet())
		// {
		// System.out.println(entry.getKey() + " " + entry.getValue());
		// }

		 String nonce = "V5KA615D83USO98NMLDHZNTXBZ2HP6M3";
		 String name = "alice";
		 String passwd = "alice";
		 String realm = "10.108.113.238";
		 String url = "sip:10.108.113.238:5080";
		 String method = "REGISTER";
		 System.out.println(cur_env.myDigest(name, realm, passwd, nonce,
		 method, url));

		// Iterator<String> headerNames = req.getHeaderNames();
		// while(headerNames.hasNext())
		// {
		// String headerName = (String)headerNames.next();
		// logger.info(headerName+" : ");
		// logger.info(req.getHeader(headerName));
		// }

		// String auth = "Digest
		// username=\"alice\",realm=\"10.109.247.126\",nonce=\"G0RHUN3A1970LQK0DIDFU7NB1VU96IRZ\",uri=\"sip:10.109.247.126\",response=\"c24a8623ba290dc39d03552dacf1420d\",algorithm=MD5";
		// int st = auth.indexOf("response=\"") + 10;
		// int ed = auth.indexOf("\"", st);
		// System.out.println("auth " + auth.substring(st, ed));
	}
}
