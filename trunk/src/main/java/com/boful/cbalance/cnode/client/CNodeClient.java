package com.boful.cbalance.cnode.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.boful.cnode.server.codec.BofulCodec;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.client.FServerClient;
import com.boful.net.cnode.protocol.ConvertTaskProtocol;

public class CNodeClient {
    private ConnectFuture cf;
    private NioSocketConnector connector = new NioSocketConnector();
    private Logger logger = Logger.getLogger(CNodeClient.class);
    private IoSession ioSession;

    /***
     * 解码器定义
     */
    private static BofulCodec bofulCodec = new BofulCodec();
    private static NodeClientHandler clientHandler = new NodeClientHandler();

    public void connect(String address, int port) throws Exception {
        logger.debug("连接到：" + address + ":" + port);

        // 创建接受数据的过滤器
        DefaultIoFilterChainBuilder chain = connector.getFilterChain();

        // 设定这个过滤器将一行一行(/r/n)的读取数据
        chain.addLast("codec", new ProtocolCodecFilter(bofulCodec));

        // 客户端的消息处理器：一个SamplMinaServerHander对象
        connector.setHandler(clientHandler);

        // set connect timeout
        connector.setConnectTimeoutMillis(60 * 60 * 1000);
        // 连接到服务器：
        cf = connector.connect(new InetSocketAddress(address, port));
        cf.awaitUninterruptibly();
        try {
            ioSession = cf.getSession();
            logger.debug("服务器" + address + ":" + port + "连接成功！");
        } catch (Exception e) {
            logger.debug("服务器" + address + ":" + port + "未连接上！");
            throw e;
        }
    }

    public void send(String cmd, SocketAddress rootAddress) throws Exception {
        if (ioSession != null) {
            ConvertTaskProtocol convertTaskProtocol = new ConvertTaskProtocol();
            convertTaskProtocol.setCmd(cmd);
            String ip = ((InetSocketAddress) rootAddress).getHostString();
            int port = ((InetSocketAddress) rootAddress).getPort();
            ioSession.setAttribute("balanceIp", ip);
            ioSession.setAttribute("balancePort", port);
            ioSession.write(convertTaskProtocol);
        } else {
            throw new Exception("未连接上");
        }
    }

    public void send(String cmd) throws Exception {
        if (ioSession != null) {
            ConvertTaskProtocol convertTaskProtocol = new ConvertTaskProtocol();
            convertTaskProtocol.setCmd(cmd);
            ioSession.write(convertTaskProtocol);
        } else {
            throw new Exception("未连接上");
        }
    }

    public void setTranscodeEvent(TranscodeEvent event) {
        ioSession.setAttribute("transcodeEvent", event);
    }

    public void setCmdArgs(Map<String, String> cmdMap) {
        ioSession.setAttribute("jobid", cmdMap.get("jobid"));
        ioSession.setAttribute("diskFile", cmdMap.get("diskFile"));
        ioSession.setAttribute("destFile", cmdMap.get("destFile"));
    }

    public void setFServerClient(FServerClient fServerClient) {
        ioSession.setAttribute("fServerClient", fServerClient);
    }

    public void disconnect() {
        System.out.println("disconnect");
        ioSession.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
    }
}
