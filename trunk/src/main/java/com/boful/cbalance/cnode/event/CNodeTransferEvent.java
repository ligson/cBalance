package com.boful.cbalance.cnode.event;

import java.io.File;
import java.util.Map;

import com.boful.cbalance.cnode.client.CNodeClient;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.client.event.TransferEvent;
import com.boful.net.utils.CommandLineUtils;

public class CNodeTransferEvent implements TransferEvent {

    @Override
    public void onStart(File src, String dest) {
        System.out.println("文件" + src.getAbsolutePath() + "开始上传！");
    }

    @Override
    public void onSuccess(File src, String dest) {
        System.out.println("文件" + src.getAbsolutePath() + "上传完成！");
        try {
            Map<String, String> cmdMap = CommandLineUtils.parse(cmd);
            // 重新生成cmd
            String newCmd = "";
            newCmd += "-operation " + cmdMap.get("operation");
            newCmd += " -id " + cmdMap.get("jobid");
            newCmd += " -i " + dest;
            newCmd += " -o " + dest;
            newCmd += " -vb " + cmdMap.get("videoBitrate");
            newCmd += " -ab " + cmdMap.get("audioBitrate");
            if (cmdMap.containsKey("size")) {
                newCmd += " -size " + cmdMap.get("size");
            }

            System.out.println("newCmd:" + newCmd);

            // 转码任务
            CNodeClient cNodeClient = new CNodeClient();
            cNodeClient.connect(ip, cNodePort);
            cNodeClient.setTranscodeEvent(transcodeEvent);
            cNodeClient.send(newCmd);

        } catch (Exception e) {
            System.out.println("任务分发失败！");
        }
    }

    @Override
    public void onTransfer(File src, String dest, int process) {
        System.out.println("文件" + src.getAbsolutePath() + "上传进度:" + process + "%");
    }

    @Override
    public void onFail(File src, String dest, String message) {
        System.out.println("文件" + src.getAbsolutePath() + "上传失败！");
    }

    private String cmd;

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return this.cmd;
    }

    private TranscodeEvent transcodeEvent;

    public void setTranscodeEvent(TranscodeEvent event) {
        this.transcodeEvent = event;
    }

    private String ip;

    public void setIp(String ip) {
        this.ip = ip;
    }

    private int cNodePort;

    public void setCNodePort(int cNodePort) {
        this.cNodePort = cNodePort;
    }
}
