package com.boful.cbalance.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.boful.cbalance.cnode.client.CNodeClient;
import com.boful.net.client.FServerClient;

public class DistributeTaskUtils {

    private static Logger logger = Logger.getLogger(DistributeTaskUtils.class);
    private static List<FServerClient> fServerClientList = null;
    private static List<CNodeClient> cNodeClientList = null;

    public static int[] initServerConfig() {
        logger.debug("服务器配置文件初始化...........");
        int[] config = new int[3];
        try {
            URL url = ClassLoader.getSystemResource("conf/config.properties");
            if (url == null) {
                url = ClassLoader.getSystemResource("config.properties");
            }
            InputStream in = new BufferedInputStream(new FileInputStream(url.getPath()));
            // InputStream in = new BufferedInputStream(new FileInputStream(new
            // File("src/main/resources/config.properties")));
            Properties props = new Properties();
            props.load(in);

            // 取得内容
            int bufferSize = Integer.parseInt(props.getProperty("server.bufferSize"));
            int idleTime = Integer.parseInt(props.getProperty("server.idleTime"));
            int port = Integer.parseInt(props.getProperty("server.port"));

            config[0] = bufferSize;
            config[1] = idleTime;
            config[2] = port;

            return config;
        } catch (Exception e) {
            logger.debug("服务器配置文件初始化失败...........");
            logger.debug("错误信息：" + e.getMessage());
            return config;
        }

    }

    @SuppressWarnings("unchecked")
    public static boolean initClientList() {
        logger.debug("客户端列表初始化...........");
        try {
            SAXReader SR = new SAXReader();
            URL url = ClassLoader.getSystemResource("conf/serverlist.xml");
            if (url == null) {
                url = ClassLoader.getSystemResource("serverlist.xml");
            }
            Document doc = SR.read(new File(url.getPath()));
            // Document doc = SR.read(new
            // File("src/main/resources/serverlist.xml"));
            Element rootElement = doc.getRootElement();

            Element serverRootElement = rootElement.element("servers");
            List<Element> serverElementList = serverRootElement.elements("server");

            fServerClientList = new ArrayList<FServerClient>();
            cNodeClientList = new ArrayList<CNodeClient>();
            String address = "";
            int serverPort = 0;
            int nodePort = 0;
            for (Element element : serverElementList) {
                Element serverIpElement = element.element("ip");
                Element serverPortElement = element.element("fserverPort");
                Element nodePortElement = element.element("cnodePort");

                try {
                    address = serverIpElement.getText();
                    serverPort = Integer.parseInt(serverPortElement.getText());

                    FServerClient fServerClient = new FServerClient();
                    fServerClient.connect(address, serverPort);
                    fServerClientList.add(fServerClient);

                    nodePort = Integer.parseInt(nodePortElement.getText());
                    CNodeClient cNodeClient = new CNodeClient();
                    cNodeClient.connect(address, nodePort);
                    cNodeClientList.add(cNodeClient);

                } catch (Exception e) {
                    // 出现任何异常不处理，进行下一个客户端配置
                    continue;
                }
            }

            maxCount = fServerClientList.size();
            if (maxCount == 0) {
                logger.debug("客户端初始化失败...........");
                return false;
            }

            logger.debug("客户端列表初始化成功...........");
            return true;
        } catch (Exception e) {
            logger.debug("客户端列表初始化失败...........");
            logger.debug("错误信息：" + e.getMessage());
            return false;
        }
    }

    private static int maxCount = 0;
    private static int nowIndex = 0;

    public static FServerClient getClient() {

        if (maxCount == 0) {
            return null;
        }

        FServerClient client = fServerClientList.get(nowIndex);
        client.setIndex(nowIndex);

        nowIndex++;
        if (nowIndex == maxCount) {
            nowIndex = 0;
        }

        return client;
    }

    public static CNodeClient getCNodeClient(int clientIndex) {
        return cNodeClientList.get(clientIndex);
    }

}
