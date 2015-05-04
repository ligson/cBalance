package com.boful.cbalance.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.boful.cbalance.server.codec.BofulCodec;
import com.boful.cbalance.utils.DistributeTaskUtils;

public class CBalanceServer {
    /***
     * 解码器定义
     */
    private static BofulCodec bofulCodec = new BofulCodec();
    /***
     * 服务器端业务处理
     */
    private static BalanceServerHandler serverHandler = new BalanceServerHandler();

    private static NioSocketAcceptor acceptor = new NioSocketAcceptor();
    private static Logger logger = Logger.getLogger(CBalanceServer.class);

    public static void main(String[] args) throws Exception {
        startServer();
    }

    public static void startServer() {
        logger.debug("服务器开始启动...........");
        try {
            int[] config = DistributeTaskUtils.initServerConfig();
            acceptor.getFilterChain().addLast("logger", new LoggingFilter());
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(bofulCodec));
            acceptor.setHandler(serverHandler);
            acceptor.getSessionConfig().setReadBufferSize(config[0]);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, config[1]);
            acceptor.bind(new InetSocketAddress(config[2]));
        } catch (IOException e) {
            logger.debug("服务器启动失败...........");
            logger.debug("错误信息：" + e.getMessage());
            System.exit(0);
        }
        // 初始化客户端列表
        boolean initState = DistributeTaskUtils.initClientList();
        if (!initState) {
            logger.debug("服务器启动失败...........");
            System.exit(0);
        }
        System.out.println("服务器启动成功...........");
        logger.debug("服务器启动成功...........");
    }
}
