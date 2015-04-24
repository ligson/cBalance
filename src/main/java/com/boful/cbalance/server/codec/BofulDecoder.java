package com.boful.cbalance.server.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.boful.cbalance.protocol.DistributeTaskProtocol;
import com.boful.cbalance.protocol.Operation;
import com.boful.cbalance.protocol.TaskStateProtocol;



public class BofulDecoder extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(IoSession session, IoBuffer inBuffer, ProtocolDecoderOutput out) throws Exception {
        if (inBuffer.remaining() > 0) {
            inBuffer.mark();
            if (inBuffer.remaining() < 4) {
                inBuffer.reset();
                return false;
            }
            int operation = inBuffer.getInt();
            // 转码任务
            if (operation == Operation.TAG_DISTRIBUTE_TASK) {
                DistributeTaskProtocol distributeTaskProtocol = DistributeTaskProtocol.parse(inBuffer);
                if (distributeTaskProtocol == null) {
                    inBuffer.reset();
                    return false;
                } else {
                    out.write(distributeTaskProtocol);
                    return true;
                }

                // 转码状态
            } else if (operation == Operation.TAG_TASK_STATE) {
                TaskStateProtocol taskStateProtocol = TaskStateProtocol.parse(inBuffer);
                if (taskStateProtocol == null) {
                    inBuffer.reset();
                    return false;
                } else {
                    out.write(taskStateProtocol);
                    return true;
                }
            }
        }
        inBuffer.reset();
        return false;
    }

}