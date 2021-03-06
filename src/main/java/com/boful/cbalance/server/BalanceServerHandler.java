package com.boful.cbalance.server;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.utils.DistributeTaskUtils;
import com.boful.net.cbalance.protocol.DistributeServerProtocol;
import com.boful.net.cnode.protocol.ConvertTaskProtocol;
import com.boful.net.cnode.protocol.Operation;

public class BalanceServerHandler extends IoHandlerAdapter {

    private static Set<IoSession> sessions = new HashSet<IoSession>();
    private static Logger logger = Logger.getLogger(BalanceServerHandler.class);

    public static Set<IoSession> getSessions() {
        return sessions;
    }

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
            if (operation == Operation.TAG_CONVERT_TASK) {
                ConvertTaskProtocol convertTaskProtocol = (ConvertTaskProtocol) message;
                distributeTask(convertTaskProtocol, session);
            }
        }
    }

    /**
     * 分配任务
     * 
     * @param distributeTaskProtocol
     * @param session
     * @throws Exception
     */
    private void distributeTask(ConvertTaskProtocol convertTaskProtocol, IoSession session) throws Exception {

        DistributeServerProtocol distributeServerProtocol = new DistributeServerProtocol();
        Map<String, String> clientMap = DistributeTaskUtils.getClientMap();
        distributeServerProtocol.setServerIp(clientMap.get("address"));
        distributeServerProtocol.setfServerPort(Integer.parseInt(clientMap.get("serverPort")));
        distributeServerProtocol.setcNodePort(Integer.parseInt(clientMap.get("nodePort")));
        session.write(distributeServerProtocol);
        
/*
 ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        // 取得FServerClient和CNodeClient
        FServerClient client = DistributeTaskUtils.getClient();
        if (client == null) {
            convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
            convertStateProtocol.setMessage("客户端 不存在！");
            session.write(convertStateProtocol);
            return;
        }

        // 从命令行中取出diskFile和destFile
        Map<String, String> commandMap = CommandLineUtils.parse(convertTaskProtocol.getCmd());
        if (commandMap == null) {
            convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
            convertStateProtocol.setMessage("命令行" + convertTaskProtocol.getCmd() + "错误！");
            session.write(convertStateProtocol);
            return;
        }

        String msg = CommandLineUtils.checkCmdOnlyFile(commandMap);
        if (StringUtils.isNotEmpty(msg)) {
            convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
            convertStateProtocol.setMessage(msg);
            session.write(convertStateProtocol);
            return;
        }

        File diskFile = new File(commandMap.get("destFile"));
        String destFile = commandMap.get("diskFile");

        CNodeTransferEvent event = new CNodeTransferEvent(session);
        event.setCmd(convertTaskProtocol.getCmd());
        event.setCNodeClient(DistributeTaskUtils.getCNodeClient(client.getIndex()));

        // 向转码服务器发送文件
        client.send(diskFile, destFile, event);
        */
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
