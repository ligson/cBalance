package com.boful.cbalance.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.boful.cnode.client.CNodeClient;

public class DistributeTaskUtils {

	private static List<Map<String, String>> serverList = null;
	private static Logger logger = Logger.getLogger(DistributeTaskUtils.class);
	private static List<CNodeClient> clientList = null;

	public static boolean initServerConfig() {
		try {
			SAXReader SR = new SAXReader();
			Document doc = SR.read(new File("src/main/resources/server.xml"));
			Element rootElement = doc.getRootElement();

			Element serverRootElement = rootElement.element("servers");
			List<Element> serverElementList = serverRootElement
					.elements("server");

			serverList = new ArrayList<Map<String, String>>();

			for (Element element : serverElementList) {
				Element serverIpElement = element.element("ip");
				Element serverPortElement = element.element("port");
				Map<String, String> serverMap = new HashMap<String, String>();
				serverMap.put("ip", serverIpElement.getText());
				serverMap.put("port", serverPortElement.getText());
				serverList.add(serverMap);
			}

			logger.debug("配置文件初始化成功...........");
			return true;
		} catch (Exception e) {
			logger.debug("配置文件初始化失败...........");
			logger.debug("错误信息：" + e.getMessage());
			return false;
		}
	}

	public static boolean initClientList() {
		clientList = new ArrayList<CNodeClient>();

		String address = "";
		int port = 0;
		for (Map<String, String> server : serverList) {
			try {
				address = server.get("ip");
				port = Integer.parseInt(server.get("port"));

				CNodeClient client = new CNodeClient();
				client.connect(address, port);
				clientList.add(client);
			} catch (Exception e) {
				// 出现任何异常不处理，进行下一个客户端配置
				continue;
			}
		}

		maxCount = clientList.size();
		if (maxCount == 0) {
			logger.debug("客户端初始化失败...........");
			logger.debug("错误信息：服务器无法连接");
			return false;
		} else {
			logger.debug("客户端初始化成功...........");
			return true;
		}
	}

	private static int maxCount = 0;
	private static int nowIndex = 0;

	public static CNodeClient getClient() {

		if (maxCount == 0) {
			return null;
		}

		CNodeClient client = clientList.get(nowIndex);
		nowIndex++;
		if (nowIndex == maxCount) {
			nowIndex = 0;
		}

		return client;
	}
}
