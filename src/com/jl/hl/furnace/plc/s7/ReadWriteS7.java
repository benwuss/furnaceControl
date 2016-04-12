package com.jl.hl.furnace.plc.s7;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.model.FurnaceControlVO;
import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.plc.moka7.S7;
import com.jl.hl.furnace.plc.moka7.S7Client;
import com.jl.hl.furnace.util.LoggerUtil;

public class ReadWriteS7 {

	private final static Logger logger = LoggerUtil.getLogger(ReadWriteS7.class
			.getSimpleName());

	private String ipAddress = "192.168.0.2";
	private int rack = 0; // Default 0 for S7300
	private int slot = 2; // Default 2 for S7300
	private int readDB = 15;
	private int writeDB = 16;
	
	// 一定要和通信协议内容一致。最后一次更新是根据1.4版本，2016-1-23
	public final static int READ_BYTE_SIZE = 278;
	
	public final static int WRITE_BYTE_SIZE = 56;

	public ReadWriteS7() {

	}

	public byte[] readS7() {
		byte[] buffer = new byte[READ_BYTE_SIZE];
		S7Client client = new S7Client();
		client.ConnectTo(ipAddress, rack, slot);
		if (client.Connect() == 0) {
			client.ReadArea(S7.S7AreaDB, readDB, 0, buffer.length, buffer);
		}
		client.Disconnect();
		return buffer;
	}

	public void writeS7(byte[] buffer) {
		S7Client client = new S7Client();
		client.ConnectTo(ipAddress, rack, slot);
		if (client.Connect() == 0) {
			int r = client.WriteArea(S7.S7AreaDB, writeDB, 0, buffer.length, buffer);
			if(r != 0){
				logger.warn("写入S7失败！错误码为{}，请拨打电话18600049580，并将最近一份日志发给Ben", r);
			}
		}
		client.Disconnect();
	}

	public void writeHeartBeats(boolean heartBeats) {		
		byte[] buffer = new byte[1];
		S7.SetBitAt(buffer, 0, 0, heartBeats);		
		S7Client client = new S7Client();
		client.ConnectTo(ipAddress, rack, slot);
		if (client.Connect() == 0) {
			client.WriteArea(S7.S7AreaDB, writeDB, 56, 1, buffer);
		}		
		client.Disconnect();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		logger.entry();

		ReadWriteS7 s7 = new ReadWriteS7();

		byte[] buffer = s7.readS7();

		FurnaceVO fvo = new FurnaceVO();
		fvo.loadDataFromS7300(buffer);

		logger.info(fvo.toString());

		FurnaceControlVO fcvo = new FurnaceControlVO();
		fcvo.setAirValve(new BigDecimal(50.12345));
		fcvo.setGasValve(new BigDecimal(30.56789));
		fcvo.setTimeID(fvo.getTimeID());

		buffer = fcvo.getBytes();
		s7.writeS7(buffer);
		logger.info(fcvo.toString());
		
		logger.exit();
	}

}
