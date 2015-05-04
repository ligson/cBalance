package com.boful.cbalance.cnode.event;

import com.boful.cbalance.cnode.client.CNodeClient;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class CNodeTranscodeEvent implements TranscodeEvent {

    @Override
    public void onSubmitFail(DiskFile diskFile, String errorMessage, String jobId) {
        System.out.println("文件" + diskFile.getAbsolutePath() + "上传失败！");
    }

    @Override
    public void onSubmitSuccess(DiskFile diskFile, String jobId) {
        System.out.println("文件" + diskFile.getAbsolutePath() + "上传到任务分发服务器！");
        try {
            // cNodeClient.setTranscodeEvent(this);
            // 转码任务分配
            cNodeClient.send(cmd);
        } catch (Exception e) {
            System.out.println("任务分发失败！");
        }
    }

    @Override
    public void onStartTranscode(DiskFile diskFile, String jobId) {
        System.out.println("onStartTranscode");
    }

    @Override
    public void onTranscodeSuccess(DiskFile diskFile, DiskFile destFile, String jobId) {
        System.out.println("文件" + diskFile.getAbsolutePath() + "转码成功！");
    }

    @Override
    public void onTranscode(DiskFile diskFile, int process, String jobId) {
        System.out.println("onTranscode");
    }

    @Override
    public void onTranscodeFail(DiskFile diskFile, String errorMessage, String jobId) {
        System.out.println("文件" + diskFile.getAbsolutePath() + "转码失败！");
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
}
