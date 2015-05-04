package com.boful.cbalance.fserver.client;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.utils.DistributeTaskUtils;
import com.boful.net.client.CNodeClient;
import com.boful.net.fserver.protocol.Operation;
import com.boful.net.fserver.protocol.SendStateProtocol;

public class FServerClientHandler extends IoHandlerAdapter {

    private Set<IoSession> sessions = new HashSet<IoSession>();
    private static Logger logger = Logger.getLogger(FServerClientHandler.class);

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        Field field = null;
        try {
            field = message.getClass().getDeclaredField("OPERATION");
        } catch (NoSuchFieldException exception) {
            logger.debug(exception);
        }
        if (field != null) {
            int operation = field.getInt(message);
            if (operation == Operation.TAG_SEND_STATE) {
                SendStateProtocol sendStateProtocol = (SendStateProtocol) message;
                if (sendStateProtocol.getState() == Operation.TAG_STATE_SEND_OK) {
                    logger.info("文件" + sendStateProtocol.getSrcFile().getAbsolutePath() + "传输成功！");
                    System.out.println("文件" + sendStateProtocol.getSrcFile().getAbsolutePath() + "传输成功！");
                    // 调用cnode
                    int clientIndex = (int) session.getAttribute("clientIndex");
                    CNodeClient client = DistributeTaskUtils.getCNodeClient(clientIndex);
                    if (client == null) {
                        logger.info("转码服务器连接失败！");
                        return;
                    }
                    client.send(session.getAttribute("cmd").toString());
                } else {
                    logger.info("文件" + sendStateProtocol.getSrcFile().getAbsolutePath() + "传输失败！");
                }
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}