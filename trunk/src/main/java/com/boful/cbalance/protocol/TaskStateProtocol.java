package com.boful.cbalance.protocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.mina.core.buffer.IoBuffer;

public class TaskStateProtocol {
	public static int OPERATION = Operation.TAG_TASK_STATE;

	private int state;
	private String message;

	// 编码
	public IoBuffer toByteArray() throws IOException {
		int count = countLength();
		IoBuffer ioBuffer = IoBuffer.allocate(count);
		ioBuffer.putInt(OPERATION);
		byte[] messageBuffer = message.getBytes("UTF-8");
		ioBuffer.putInt(messageBuffer.length);
		ioBuffer.putInt(state);
		ioBuffer.put(messageBuffer);
		return ioBuffer;
	}

	// 解码
	public static TaskStateProtocol parse(IoBuffer ioBuffer) throws IOException {

		if (ioBuffer.remaining() < 8) {
			return null;
		}

		int messageLen = ioBuffer.getInt();
		if (ioBuffer.remaining() != (messageLen + 4)) {
			return null;
		}

		TaskStateProtocol taskStateProtocol = new TaskStateProtocol();
		taskStateProtocol.setState(ioBuffer.getInt());
		byte[] messageBuffer = new byte[messageLen];
		ioBuffer.get(messageBuffer);
		taskStateProtocol.setMessage(new String(messageBuffer, "UTF-8"));

		return taskStateProtocol;
	}

	public int countLength() {
		// TAG+MESSAGEBUFFERLEN+STATE+MESSAGEBUFFER
		try {
			byte[] messageBuffer = message.getBytes("UTF-8");
			return 4 + 4 + 4 + messageBuffer.length;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String toString() {
		return message;
	}

	public String getStateInfo() {
		switch (state) {
		case 0:
			return "转码错误";
		case 1:
			return "转码等待";
		case 2:
			return "转码开始";
		case 3:
			return "转码中";
		case 4:
			return "转码成功";
		default:
			break;
		}

		return "未知状态";
	}
}
