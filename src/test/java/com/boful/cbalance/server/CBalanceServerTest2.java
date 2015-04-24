package com.boful.cbalance.server;

public class CBalanceServerTest2 {
	public static void main(String[] args) {
		// 视频转码
		CBalanceClient test1 = new CBalanceClient();
		try {
			test1.connect("127.0.0.1", 11000);
			String cmd = "-id job_0001 -i e:/爱情公寓番外篇睡美人.f4v -o e:/test/bak2.mp4 -vb 30000 -ab 20000 -size 300x200";
			test1.send(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
