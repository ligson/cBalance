package com.boful.cbalance.cnode.client;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.cnode.event.DownloadTransferEvent;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;
import com.boful.net.client.FServerClient;
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
                ConvertStateProtocol convertStateProtocol = (ConvertStateProtocol) message;
                TranscodeEvent transcodeEvent = (TranscodeEvent) session.getAttribute("transcodeEvent");

                int state = convertStateProtocol.getState();
                // 转码失败
                if (state == ConvertStateProtocol.STATE_FAIL) {
                    transcodeEvent.onTranscodeFail(new DiskFile((String) session.getAttribute("diskFile")),
                            convertStateProtocol.getMessage(), (String) session.getAttribute("jobid"));
                    // 转码开始
                } else if (state == ConvertStateProtocol.STATE_START) {
                    transcodeEvent.onStartTranscode(new DiskFile((String) session.getAttribute("diskFile")),
                            (String) session.getAttribute("jobid"));

                    // 转码中
                } else if (state == ConvertStateProtocol.STATE_CONVERTING) {
                    String convertMessage = convertStateProtocol.getMessage();
                    int processIndex = convertMessage.indexOf("转码进度:") + 5;
                    String processString = convertMessage.substring(processIndex);
                    processString = processString.substring(0, processString.length() - 1);
                    int process = Integer.parseInt(processString);
                    if (process < 100) {
                        transcodeEvent.onTranscode(new DiskFile((String) session.getAttribute("diskFile")), process,
                                (String) session.getAttribute("jobid"));
                    } else {
                        transcodeEvent.onTranscodeSuccess(new DiskFile((String) session.getAttribute("diskFile")),
                                new DiskFile((String) session.getAttribute("destFile")),
                                (String) session.getAttribute("jobid"));
                        downloadFile(session);
                    }
                    // 转码成功
                } else if (state == ConvertStateProtocol.STATE_SUCCESS) {
                    transcodeEvent.onTranscodeSuccess(new DiskFile((String) session.getAttribute("diskFile")),
                            new DiskFile((String) session.getAttribute("destFile")),
                            (String) session.getAttribute("jobid"));
                    downloadFile(session);
                }
            }
        }
    }

    private void downloadFile(IoSession session) throws Exception {
        if (session.getAttribute("downloadFlag") == null) {
            // 设置标识
            session.setAttribute("downloadFlag", 1);

            // 取得FServerClient
            FServerClient fServerClient = (FServerClient) session.getAttribute("fServerClient");
            DownloadTransferEvent event = new DownloadTransferEvent();
            fServerClient.download(new File("E:/test/convert/7867C06EA8975704CA1B1D5DB87FC3CB.f4v"),
                    (String) session.getAttribute("destFile"), event);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
