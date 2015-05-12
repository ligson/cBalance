package com.boful.cbalance.server.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.boful.net.cbalance.protocol.DistributeServerProtocol;
import com.boful.net.cnode.protocol.ConvertTaskProtocol;
import com.boful.net.cnode.protocol.Operation;

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
            // 转码服务器分配任务
            if (operation == Operation.DISTRIBUTE_CONVERT_SEVER) {
                DistributeServerProtocol distributeServerProtocol = DistributeServerProtocol.parse(inBuffer);
                if (distributeServerProtocol == null) {
                    inBuffer.reset();
                    return false;
                } else {
                    out.write(distributeServerProtocol);
                    return true;
                }
            }
            // 转码任务
            if (operation == Operation.TAG_CONVERT_TASK) {
                ConvertTaskProtocol convertTaskProtocol = ConvertTaskProtocol.parse(inBuffer);
                if (convertTaskProtocol == null) {
                    inBuffer.reset();
                    return false;
                } else {
                    out.write(convertTaskProtocol);
                    return true;
                }
            }
        }
        inBuffer.reset();
        return false;
    }

}