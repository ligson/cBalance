package com.boful.cbalance.cnode.event;

import java.io.File;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.cnode.client.CNodeClient;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.client.event.TransferEvent;
import com.boful.net.cnode.protocol.ConvertStateProtocol;
import com.boful.net.utils.CommandLineUtils;

public class CNodeTransferEvent implements TransferEvent {

    private IoSession session;

    public CNodeTransferEvent(IoSession session) {
        this.session = session;
    }

    @Override
    public void onStart(File src, String dest) {
        System.out.println("文件" + src.getAbsolutePath() + "开始上传！");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_CONVERTING);
        convertStateProtocol.setMessage("文件" + src.getAbsolutePath() + "开始上传！");
        session.write(convertStateProtocol);
    }

    @Override
    public void onSuccess(File src, String dest) {
        System.out.println("---------------------");
        System.out.println("文件" + src.getAbsolutePath() + "上传完成！");
        System.out.println("---------------------0");
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
            
            System.out.println("newCmd:"+newCmd);

            // 转码任务分配
            System.out.println("---------------------1");
            cNodeClient.setTranscodeEvent(transcodeEvent);
            System.out.println("---------------------2");
            cNodeClient.send(newCmd);
            
        } catch (Exception e) {
            System.out.println("任务分发失败！");
            ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
            convertStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
            convertStateProtocol.setMessage("任务分发失败！");
            session.write(convertStateProtocol);
        }
    }

    @Override
    public void onTransfer(File src, String dest, int process) {
        System.out.println("文件" + src.getAbsolutePath() + "上传进度:" + process + "%");
        ConvertStateProtocol convertStateProtocol = new ConvertStateProtocol();
        convertStateProtocol.setState(ConvertStateProtocol.STATE_CONVERTING);
        convertStateProtocol.setMessage("文件" + src.getAbsolutePath() + "上传进度:" + process + "%");
        session.write(convertStateProtocol);
    }

    @Override
    public void onFail(File src, String dest, String message) {
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
