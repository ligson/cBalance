package com.boful.cbalance.server;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.boful.cbalance.server.codec.BofulCodec;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.cnode.protocol.ConvertTaskProtocol;

public class CBalanceClient {
	private ConnectFuture cf;
	private NioSocketConnector connector = new NioSocketConnector();
	private Logger logger = Logger.getLogger(CBalanceClient.class);
	private IoSession ioSession;

	/***
	 * 解码器定义
	 */
	private static BofulCodec bofulCodec = new BofulCodec();
	private static BalanceClientHandler clientHandler = new BalanceClientHandler();

	public void connect(String address, int port) {
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
	}

	public void send(String cmd) throws Exception {
		ioSession = cf.getSession();
		if (ioSession != null) {
			ConvertTaskProtocol convertTaskProtocol = new ConvertTaskProtocol();
			convertTaskProtocol.setCmd(cmd);
			ioSession.write(convertTaskProtocol);
		} else {
			throw new Exception("未连接上");
		}
	}

	public void setTranscodeEvent(TranscodeEvent transcodeEvent) {
		clientHandler = (BalanceClientHandler) connector.getHandler();
		clientHandler.setTranscodeEvent(transcodeEvent);
	}

	public void disconnect() {
		System.out.println("disconnect");
		ioSession.getCloseFuture().awaitUninterruptibly();
		connector.dispose();
	}
}
