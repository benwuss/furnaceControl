package com.jl.hl.furnace.plc.moka7.demo;

import com.jl.hl.furnace.plc.moka7.*;

public class PLCTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String ipAddress = "192.168.0.2";
		int rack = 0; // Default 0 for S7300
		int slot = 2; // Default 2 for S7300
		int DBNum = 16; // Sample DB that must be present in the CPU
		IntByRef SizeRead = new IntByRef(0);

		S7Client Client = new S7Client();

		try {

			int Result = 0;
			Result = Client.ConnectTo(ipAddress, rack, slot);
			if (Result == 0) {
				System.out.println("Connected to   : " + ipAddress + " (Rack="
						+ rack + ", Slot=" + slot + ")");
				System.out.println("PDU negotiated : " + Client.PDULength()
						+ " bytes");
				System.out.println("1. 与PLC通信成功");
			}

			// 2 get DB
			byte[] Buffer3 = new byte[100];
			Client.DBGet(DBNum, Buffer3, SizeRead);
			System.out.println("数据长度：" + SizeRead.Value);

			// 3 read DB
			byte[] Buffer = new byte[20];
			// read the first data
			Result = Client.ReadArea(S7.S7AreaDB, DBNum, 0, SizeRead.Value,
					Buffer);
			System.out.println("ReadDB Result:" + Result);
			// read INT (signed 16 bit integer) from PLC
			for (int i = 0; i < SizeRead.Value / 2; i++) {
				System.out.println("read DB: " + S7.GetShortAt(Buffer, i * 2));
			}

			 // 4 write DB
			 byte[] Buffer2 = new byte[20];
			 S7.SetShortAt(Buffer2, 0, 2046);
			 Client.WriteArea(S7.S7AreaDB, DBNum, 6, 2, Buffer2);

			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Client.Disconnect();
			System.out.println("close!");
		}
	}
}
