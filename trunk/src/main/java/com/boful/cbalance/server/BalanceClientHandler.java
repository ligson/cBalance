package com.boful.cbalance.server;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.cbalance.protocol.Operation;
import com.boful.cbalance.protocol.TaskStateProtocol;

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
			if (operation == Operation.TAG_TASK_STATE) {
				TaskStateProtocol taskStateProtocol = (TaskStateProtocol) message;
				logger.info(taskStateProtocol.getMessage());
			}
		}
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
