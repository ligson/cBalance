package com.boful.cbalance.server;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.cnode.event.CNodeTransferEvent;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.cbalance.protocol.DistributeServerProtocol;
import com.boful.net.client.FServerClient;
import com.boful.net.cnode.protocol.Operation;
import com.boful.net.utils.CommandLineUtils;

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
        Field field = null;
        try {
            field = message.getClass().getDeclaredField("OPERATION");
        } catch (NoSuchFieldException exception) {
            logger.debug(exception);
        }
        if (field != null) {
            int operation = field.getInt(message);
            if (operation == Operation.DISTRIBUTE_CONVERT_SEVER) {
                DistributeServerProtocol distributeServerProtocol = (DistributeServerProtocol) message;
                System.out.println("-----------------");
                System.out.println("ServerIp : " + distributeServerProtocol.getServerIp());
                System.out.println("fServerPort : " + distributeServerProtocol.getfServerPort());
                System.out.println("cNodePort : " + distributeServerProtocol.getcNodePort());
                System.out.println("-----------------");

                // 解析命令行
                String cmd = (String) session.getAttribute("cmd");
                Map<String, String> cmdMap = CommandLineUtils.parse(cmd);

                // 传输文件
                String ip = distributeServerProtocol.getServerIp();
                int port = distributeServerProtocol.getfServerPort();

                FServerClient fServerClient = new FServerClient();
                fServerClient.connect(ip, port);
                CNodeTransferEvent event = new CNodeTransferEvent();
                event.setCmd(cmd);
                event.setTranscodeEvent((TranscodeEvent) session.getAttribute("transcodeEvent"));
                event.setIp(ip);
                event.setCNodePort(distributeServerProtocol.getcNodePort());
                event.setFServerClient(fServerClient);
                fServerClient.send(new File(cmdMap.get("diskFile")), cmdMap.get("destFile"), event);
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
