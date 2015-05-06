package com.boful.cbalance.server;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.net.cnode.protocol.ConvertStateProtocol;
import com.boful.net.cnode.protocol.Operation;

public class BalanceClientHandler extends IoHandlerAdapter {

    private Set<IoSession> sessions = new HashSet<IoSession>();
    private static Logger logger = Logger.getLogger(BalanceClientHandler.class);

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
        System.out.println("BalanceClientHandler : messageReceived");
        Field field = null;
        try {
            field = message.getClass().getDeclaredField("OPERATION");
        } catch (NoSuchFieldException exception) {
            logger.debug(exception);
        }
        if (field != null) {
            int operation = field.getInt(message);
            if (operation == Operation.TAG_CONVERT_STATE) {
                ConvertStateProtocol convertStateProtocol = (ConvertStateProtocol) message;
                /*
                logger.info(convertStateProtocol.getMessage());
                if (convertStateProtocol.getState() == ConvertStateProtocol.STATE_SUCCESS) {
                    System.out.println("转码成功！");
                } else if (convertStateProtocol.getState() == ConvertStateProtocol.STATE_CONVERTING) {
                    System.out.println("转码中！");
                } else if (convertStateProtocol.getState() == ConvertStateProtocol.STATE_FAIL) {
                    System.out.println("转码失败！");
                }*/
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
