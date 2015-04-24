package com.boful.cbalance.server;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.protocol.DistributeTaskProtocol;
import com.boful.cbalance.protocol.Operation;
import com.boful.cbalance.protocol.TaskStateProtocol;
import com.boful.cbalance.utils.DistributeTaskUtils;
import com.boful.cnode.protocol.ConvertStateProtocol;
import com.boful.cnode.server.CNodeClient;

public class BalanceServerHandler extends IoHandlerAdapter {

	private Set<IoSession> sessions = new HashSet<IoSession>();
	private static Logger logger = Logger.getLogger(BalanceServerHandler.class);

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		sessions.remove(session);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		sessions.add(session);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		Field field = null;
		try {
			field = message.getClass().getDeclaredField("OPERATION");
		} catch (NoSuchFieldException exception) {
			logger.debug(exception);
		}
		if (field != null) {
			int operation = field.getInt(message);
			if (operation == Operation.TAG_DISTRIBUTE_TASK) {
				DistributeTaskProtocol distributeTaskProtocol = (DistributeTaskProtocol) message;
				distributeTask(distributeTaskProtocol, session);
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
	private void distributeTask(DistributeTaskProtocol distributeTaskProtocol,
			IoSession session) throws Exception {

		TaskStateProtocol taskStateProtocol = new TaskStateProtocol();

		// 取得Client
		CNodeClient client = DistributeTaskUtils.getClient();
		if (client == null) {
			taskStateProtocol.setState(ConvertStateProtocol.STATE_FAIL);
			taskStateProtocol.setMessage("客户端 不存在！");
			session.write(taskStateProtocol);
			return;
		}

		// 向转码服务器发送命令
		client.send(distributeTaskProtocol.getCmd());

	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		cause.printStackTrace();
	}
}
