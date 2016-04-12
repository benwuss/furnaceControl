package com.jl.hl.furnace.mysql;




import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.FurnaceUtil;
import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.FurnaceZoneVO;
import com.jl.hl.furnace.model.ResultVO;
import com.jl.hl.furnace.util.LoggerUtil;

public class AnalyzeThis {

	private final static Logger logger = LoggerUtil.getLogger(StoreToDB.class
			.getSimpleName());

	private SqlSession session = null;

	public AnalyzeThis() {

	}

	public void setSqlSession(SqlSession session) {
		this.session = session;
	}

	public void analyzeResult(String startTime, String endTime) {
		logger.entry();

		ArrayList<ResultVO> rlist = new ArrayList<ResultVO>();

		List<FurnaceVO> mlist = getFurnaceVOList(startTime + "%", endTime + "%");

		int c_3K5 = 0;
		int c_3K_3K5 = 0;
		int c_2K5_3K = 0;
		int c_2K_2K5 = 0;
		int c_2K = 0;

		int total = mlist.size();

		for (FurnaceVO fvo : mlist) {
			float gp = fvo.getGasPressure().floatValue();
			if (gp >= 3.5) {
				c_3K5++;
			} else if (gp < 3.5 && gp >= 3.0) {
				c_3K_3K5++;
			} else if (gp < 3 && gp >= 2.5) {
				c_2K5_3K++;
			} else if (gp < 2.5 && gp >= 2.0) {
				c_2K_2K5++;
			} else if (gp < 2) {
				c_2K++;
			}
		}

		String[] criteria = { "3K5<=G", "3K<=G<3K5", "2K5<=G<3K", "2K<=G<2K5",
				"G<2K" };
		int[] counts = { c_3K5, c_3K_3K5, c_2K5_3K, c_2K_2K5, c_2K };

		for (int i = 0; i < counts.length; i++) {
			ResultVO rvo = new ResultVO(FurnaceUtil.change2Format2(startTime),
					FurnaceUtil.change2Format2(endTime));
			rvo.setCriteria(criteria[i]);
			rvo.setCounts(counts[i]);
			rvo.setTotal(total);
			BigDecimal r = new BigDecimal(counts[i]).divide(new BigDecimal(
					total), 2, BigDecimal.ROUND_HALF_UP);
			rvo.setRatio(r.floatValue());
			rlist.add(rvo);
		}

		int[] zoneID = { 1, 2, 3 };

		for (int i = 0; i < zoneID.length; i++) {
			List<FurnaceZoneVO> zlist = getFurnaceZoneVOList(startTime + "%",
					endTime + "%", zoneID[i]);
			int a_03 = 0;
			int a_05 = 0;
			int a_08 = 0;
			int a_10 = 0;

			int a_N05 = 0;
			int a_P05 = 0;

			int t_3 = 0;
			int t_5 = 0;
			int t_8 = 0;
			int t_10 = 0;

			int t_P10 = 0;
			int t_N10 = 0;

			int a_total = 0;
			for (FurnaceZoneVO zvo : zlist) {
				float tempDiff = zvo.getTemp().subtract(zvo.getTempTarget())
						.floatValue();
				float agRaito = zvo.getAirGasRatioACTUAL()
						.subtract(zvo.getAirGasRatioGIVEN()).floatValue();

				if (zvo.getAirGasRatioACTUAL().floatValue() > 0) {
					a_total++;

					if (agRaito <= 0.03 && agRaito >= -0.03) {
						a_03++;
					}
					if (agRaito <= 0.05 && agRaito >= -0.05) {
						a_05++;
					}
					if (agRaito <= 0.08 && agRaito >= -0.08) {
						a_08++;
					}
					if (agRaito <= 0.1 && agRaito >= -0.1) {
						a_10++;
					}
					if (agRaito < 0 && agRaito > -0.05) {
						a_N05++;
					}
					if (agRaito > 0 && agRaito < 0.05) {
						a_P05++;
					}
				}
				if (tempDiff <= 3 && tempDiff >= -3) {
					t_3++;
				}
				if (tempDiff <= 5 && tempDiff >= -5) {
					t_5++;
				}
				if (tempDiff <= 8 && tempDiff >= -8) {
					t_8++;
				}
				if (tempDiff <= 10 && tempDiff >= -10) {
					t_10++;
				}

				if (tempDiff > 10) {
					t_P10++;
				}
				if (tempDiff < -10) {
					t_N10++;
				}
			}

			String[] descA = { "a_03", "a_05", "a_08", "a_10", "a_N05", "a_P05" };
			int[] a_counts = { a_03, a_05, a_08, a_10, a_N05, a_P05 };

			for (int j = 0; j < descA.length; j++) {
				ResultVO rvo = new ResultVO(
						FurnaceUtil.change2Format2(startTime),
						FurnaceUtil.change2Format2(endTime));
				rvo.setCriteria("Z" + zoneID[i] + "_" + descA[j]);
				rvo.setCounts(a_counts[j]);
				rvo.setTotal(a_total);  
				BigDecimal r = new BigDecimal(0);
				if (a_total != 0) {
					r = new BigDecimal(a_counts[j]).divide(new BigDecimal(
							a_total), 2, BigDecimal.ROUND_HALF_UP);
				}
				rvo.setRatio(r.floatValue());
				rlist.add(rvo);
			}

			String[] descT = { "t_3", "t_5", "t_8", "t_10", "t_P10", "t_N10" };
			int[] t_counts = { t_3, t_5, t_8, t_10, t_P10, t_N10 };

			for (int j = 0; j < descT.length; j++) {
				ResultVO rvo = new ResultVO(
						FurnaceUtil.change2Format2(startTime),
						FurnaceUtil.change2Format2(endTime));
				rvo.setCriteria("Z" + zoneID[i] + "_" + descT[j]);
				rvo.setCounts(t_counts[j]);
				rvo.setTotal(total);
				BigDecimal r = new BigDecimal(t_counts[j]).divide(
						new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP);
				rvo.setRatio(r.floatValue());
				rlist.add(rvo);
			}
		}

		for (ResultVO rvo : rlist) {
			logger.info(rvo);
		}
		insertResult(rlist);
		logger.exit();
	}

	public List<FurnaceVO> getFurnaceVOList(String startTime, String endTime) {
		List<FurnaceVO> flist = new ArrayList<FurnaceVO>();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);

		flist = session.selectList("selectMainList", map);

		return flist;
	}

	public List<FurnaceZoneVO> getFurnaceZoneVOList(String startTime,
			String endTime, int zoneID) {
		List<FurnaceZoneVO> flist = new ArrayList<FurnaceZoneVO>();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("zoneID", String.valueOf(zoneID));

		flist = session.selectList("selectZoneList", map);

		return flist;
	}

	public void insertResult(ArrayList<ResultVO> rlist) {
		for (ResultVO resultVO : rlist) {
			session.insert("addResult", resultVO);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StoreToDB db = new StoreToDB();
		SqlSession session = db.getSession();
		AnalyzeThis at = new AnalyzeThis();
		at.setSqlSession(session);
		at.analyzeResult("20160301023512", "20160301054011");
		db.commitAndClose(session);
	}

}
