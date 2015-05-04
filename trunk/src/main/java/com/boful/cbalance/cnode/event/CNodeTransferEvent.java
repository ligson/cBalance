package com.boful.cbalance.cnode.event;

import java.io.File;

import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.cnode.client.CNodeClient;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.client.event.TransferEvent;
import com.boful.net.cnode.protocol.ConvertStateProtocol;

public class CNodeTransferEvent implements TransferEvent {

    private IoSession session;

    public CNodeTransferEvent(IoSession session) {
        this.session = session;
    }

    @Override
    public void onStart(File src, File dest) {
        System.out.println("文件" + src.getAbsolutePath() + "开始上传！");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_CONVERTING);
        convertStateProtocol.setMessage("文件" + src.getAbsolutePath() + "开始上传！");
        session.write(convertStateProtocol);
    }

    @Override
    public void onSuccess(File src, File dest) {
        System.out.println("文件" + src.getAbsolutePath() + "上传完成！");
        try {
            System.out.println("CNodeTranscodeEvent : " + this);
            cNodeClient.setTranscodeEvent(transcodeEvent);
            // 转码任务分配
            cNodeClient.send(cmd);
        } catch (Exception e) {
            System.out.println("任务分发失败！");
            ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
            convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
            convertStateProtocol.setMessage("任务分发失败！");
            session.write(convertStateProtocol);
        }
    }

    @Override
    public void onTransfer(File src, File dest, int process) {
        System.out.println("文件" + src.getAbsolutePath() + "上传进度:" + process + "%");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_CONVERTING);
        convertStateProtocol.setMessage("文件" + src.getAbsolutePath() + "上传进度:" + process + "%");
        session.write(convertStateProtocol);
    }

    @Override
    public void onFail(File src, File dest, String message) {
        System.out.println("文件" + src.getAbsolutePath() + "上传失败！");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
        convertStateProtocol.setMessage("文件" + src.getAbsolutePath() + "上传失败！");
        session.write(convertStateProtocol);
    }

    private String cmd;

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return this.cmd;
    }

    private CNodeClient cNodeClient;

    public void setCNodeClient(CNodeClient cNodeClient) {
        this.cNodeClient = cNodeClient;
    }

    private TranscodeEvent transcodeEvent;

    public void setTranscodeEvent(TranscodeEvent event) {
        this.transcodeEvent = event;
    }
}
