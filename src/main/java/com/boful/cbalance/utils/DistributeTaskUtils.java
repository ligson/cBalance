package com.boful.cbalance.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.boful.cnode.client.CNodeClient;
import com.boful.net.fserver.ClientMain;

public class DistributeTaskUtils {

	private static List<Map<String, String>> serverList = null;
	private static Logger logger = Logger.getLogger(DistributeTaskUtils.class);
	private static List<CNodeClient> cNodeClientList = null;
	private static List<ClientMain> fServerClientList = null;

	public static int[] initServerConfig() {
		logger.debug("开始配置文件初始化...........");
		int[] config = new int[3];
		try {
			URL url = ClassLoader.getSystemResource("conf/config.properties");
			if (url == null) {
				url = ClassLoader.getSystemResource("config.properties");
			}
			InputStream in = new BufferedInputStream(new FileInputStream(
					url.getPath()));
			Properties props = new Properties();
			props.load(in);

			// 取得内容
			int bufferSize = Integer.parseInt(props
					.getProperty("server.bufferSize"));
			int idleTime = Integer.parseInt(props
					.getProperty("server.idleTime"));
			int port = Integer.parseInt(props.getProperty("server.port"));

			config[0] = bufferSize;
			config[1] = idleTime;
			config[2] = port;

			return config;
		} catch (Exception e) {
			logger.debug("配置文件初始化失败...........");
			logger.debug("错误信息：" + e.getMessage());
			return config;
		}

	}

	@SuppressWarnings("unchecked")
	public static boolean initServerListConfig() {
		try {
			SAXReader SR = new SAXReader();
			URL url = ClassLoader.getSystemResource("conf/serverlist.xml");
			if (url == null) {
				url = ClassLoader.getSystemResource("serverlist.xml");
			}
			Document doc = SR.read(new File(url.getPath()));
			Element rootElement = doc.getRootElement();

			Element serverRootElement = rootElement.element("servers");
			List<Element> serverElementList = serverRootElement
					.elements("server");

			serverList = new ArrayList<Map<String, String>>();

			for (Element element : serverElementList) {
				Element serverIpElement = element.element("ip");
				Element serverCNodePortElement = element.element("cnodePort");
				Element serverFSeverPortElement = element
						.element("fserverPort");
				Map<String, String> serverMap = new HashMap<String, String>();
				serverMap.put("ip", serverIpElement.getText());
				serverMap.put("cnodePort", serverCNodePortElement.getText());
				serverMap.put("fserverPort", serverFSeverPortElement.getText());
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
		cNodeClientList = new ArrayList<CNodeClient>();
		fServerClientList = new ArrayList<ClientMain>();

		String address = "";
		int cnodePort = 0;
		int fserverPort = 0;
		for (Map<String, String> server : serverList) {
			try {
				address = server.get("ip");
				cnodePort = Integer.parseInt(server.get("cnodePort"));
				fserverPort = Integer.parseInt(server.get("fserverPort"));

				CNodeClient cNodeClient = new CNodeClient();
				cNodeClient.connect(address, cnodePort);
				cNodeClientList.add(cNodeClient);

				ClientMain fServerClientMain = new ClientMain();
				fServerClientMain.connect(address, fserverPort);
				fServerClientList.add(fServerClientMain);
			} catch (Exception e) {
				// 出现任何异常不处理，进行下一个客户端配置
				continue;
			}
		}

		maxCount = cNodeClientList.size();
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

	public static CNodeClient getCNodeClient() {

		if (maxCount == 0) {
			return null;
		}

		CNodeClient client = cNodeClientList.get(nowIndex);
		nowIndex++;
		if (nowIndex == maxCount) {
			nowIndex = 0;
		}

		return client;
	}

	public static ClientMain getFServerClient() {
		return fServerClientList.get(nowIndex);
	}
}
