package com.boful.cbalance.cnode.event;

import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.cnode.client.CNodeClient;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;
import com.boful.net.cnode.protocol.ConvertStateProtocol;

public class CNodeTranscodeEvent implements TranscodeEvent {

    private IoSession session;

    public CNodeTranscodeEvent(IoSession session) {
        this.session = session;
    }

    @Override
    public void onSubmitFail(DiskFile diskFile, String errorMessage, String jobId) {
        System.out.println("文件" + diskFile.getAbsolutePath() + "上传失败！");
    }

    @Override
    public void onSubmitSuccess(DiskFile diskFile, String jobId) {
        cNodeFile = diskFile.getAbsolutePath();
        System.out.println("文件" + cNodeFile + "上传到任务分发服务器！");

        try {
            System.out.println("CNodeTranscodeEvent : " + this);
            cNodeClient.setTranscodeEvent(this);
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
    public void onStartTranscode(DiskFile diskFile, String jobId) {
        System.out.println("onStartTranscode");
    }

    @Override
    public void onTranscodeSuccess(DiskFile diskFile, DiskFile destFile, String jobId) {
        System.out.println("文件" + cNodeFile + "转码成功！");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_SUCCESS);
        convertStateProtocol.setMessage("文件" + cNodeFile + "转码成功！");
        session.write(convertStateProtocol);
    }

    @Override
    public void onTranscode(DiskFile diskFile, int process, String jobId) {
        System.out.println("onTranscode");
    }

    @Override
    public void onTranscodeFail(DiskFile diskFile, String errorMessage, String jobId) {
        System.out.println("文件" + cNodeFile + "转码失败！");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
        convertStateProtocol.setMessage("文件" + cNodeFile + "转码失败！");
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

    private String cNodeFile;
}
