package com.jl.hl.furnace.plc.s7;

import java.util.ArrayList;

import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.plc.moka7.S7;

public class ValveS7Helper {

	public static void writeS7(ArrayList<ValveVO> vos) {
		byte[] buffer = new byte[ReadWriteS7.WRITE_BYTE_SIZE];
		for (ValveVO vo : vos) {
			toBytes(buffer, vo);
		}
		ReadWriteS7 s7 = new ReadWriteS7();
		s7.writeS7(buffer);
	}

	public static void toBytes(byte[] buffer, ValveVO vo) {
		int zone = vo.getZone();
		String type = vo.getType();
		String func = vo.getFunction();
		float value = vo.getNewValue().floatValue();
		switch (zone) {
		case 1:
			if (func.equals("S")) {
				if (type.equals("A")) {
					S7.SetFloatAt(buffer, 0, value);
				} else if (type.equals("G")) {
					S7.SetFloatAt(buffer, 4, value);
				}
			} else if (func.equals("F")) {
				if (type.equals("A")) {
					S7.SetFloatAt(buffer, 8, value);
				} else if (type.equals("G")) {
					S7.SetFloatAt(buffer, 12, value);
				}
			}
			break;
		case 2:
			if (func.equals("S")) {
				if (type.equals("A")) {
					S7.SetFloatAt(buffer, 16, value);
				} else if (type.equals("G")) {
					S7.SetFloatAt(buffer, 20, value);
				}
			} else if (func.equals("F")) {
				if (type.equals("A")) {
					S7.SetFloatAt(buffer, 24, value);
				} else if (type.equals("G")) {
					S7.SetFloatAt(buffer, 28, value);
				}
			}
			break;
		case 3:
			if (func.equals("S")) {
				if (type.equals("A")) {
					S7.SetFloatAt(buffer, 32, value);
				} else if (type.equals("G")) {
					S7.SetFloatAt(buffer, 36, value);
				}
			} else if (func.equals("F")) {
				if (type.equals("A")) {
					S7.SetFloatAt(buffer, 40, value);
				} else if (type.equals("G")) {
					S7.SetFloatAt(buffer, 44, value);
				}
			}
			break;
		}
	}
}
