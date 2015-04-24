package com.boful.cbalance.server;

public class CBalanceServerTest2 {
	public static void main(String[] args) {
		// 视频转码
		CBalanceClient test1 = new CBalanceClient();
		try {
			test1.connect("127.0.0.1", 11000);
			String cmd = "-id job_0002 -i e:/Koala.jpg -o e:/test/Koala1.jpg";
			test1.send(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
