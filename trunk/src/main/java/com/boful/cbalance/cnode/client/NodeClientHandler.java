package com.boful.cbalance.cnode.client;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.server.BalanceServerHandler;
import com.boful.net.cnode.protocol.ConvertStateProtocol;
import com.boful.net.cnode.protocol.Operation;

public class NodeClientHandler extends IoHandlerAdapter {

    private Set<IoSession> sessions = new HashSet<IoSession>();
    private static Logger logger = Logger.getLogger(NodeClientHandler.class);

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        sessions.remove(session);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        Field field = null;
        try {
            field = message.getClass().getDeclaredField("OPERATION");
        } catch (NoSuchFieldException exception) {
            logger.debug(exception);
        }
        if (field != null) {
            int operation = field.getInt(message);
            if (operation == Operation.TAG_CONVERT_STATE) {
                // 取得连接Balance的session
                String ip = session.getAttribute("balanceIp").toString();
                int port = (int) session.getAttribute("balancePort");
                Set<IoSession> balanceSessions = BalanceServerHandler.getSessions();
                for (IoSession balanceSession : balanceSessions) {
                    String balanceIp = ((InetSocketAddress) balanceSession.getRemoteAddress()).getHostString();
                    int balancePort = ((InetSocketAddress) balanceSession.getRemoteAddress()).getPort();
                    if (balanceIp.equals(ip) && balancePort == port) {
                        ConvertStateProtocol convertStateProtocol = (ConvertStateProtocol) message;
                        ConvertStateProtocol balanceProtocol = new ConvertStateProtocol();
                        balanceProtocol.setState(convertStateProtocol.getState());
                        balanceProtocol.setMessage(convertStateProtocol.getMessage());
                        System.out.println("NodeClientHandler : " + balanceProtocol.getMessage());
                        // 向Balance发送消息
                        // balanceSession.write(balanceProtocol);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
