package com.jl.hl.furnace.mysql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.RunningTimeVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.util.LoggerUtil;

public class StoreToDB extends Thread {

	private final static Logger logger = LoggerUtil.getLogger(StoreToDB.class
			.getSimpleName());

	private static SqlSessionFactory sessionFactory = null;

	private static String path = System.getProperty("user.dir")
			+ "\\resources\\conf.xml";

	static {
		try {
			if (sessionFactory == null) {
				InputStream is = new FileInputStream(new File(path));
				sessionFactory = new SqlSessionFactoryBuilder().build(is);
			}
		} catch (FileNotFoundException e) {
			logger.info("Connect to MYSQL failed! Check conf.xml");
		} catch (Exception e) {
			logger.info("Connect to MYSQL failed!");
		}
	}

	SqlSession session = null;

	String insertValveID = "addValve";

	String insertFurnaceID = "addFurnace";

	String insertFurnaceZoneID = "addFurnaceZone";

	FurnaceVO fvo = null;

	ArrayList<ValveVO> vlist = null;

	public StoreToDB() {
		try {
			if (sessionFactory == null) {
				InputStream is = new FileInputStream(new File(path));
				sessionFactory = new SqlSessionFactoryBuilder().build(is);
			}
		} catch (FileNotFoundException e) {
			logger.info("Connect to MYSQL failed! Check conf.xml");
		} catch (Exception e) {
			logger.info("Connect to MYSQL failed!");
		}
	}

	public SqlSession getSession() {
		return sessionFactory.openSession();
	}

	public void commitAndClose(SqlSession session) {
		session.commit();
		session.close();
	}

	public void setData(FurnaceVO fvo, ArrayList<ValveVO> vlist) {
		this.fvo = fvo;
		this.vlist = vlist;
	}

	public void run() {
		insertDB(fvo, vlist);
	}

	public void insertDB(FurnaceVO fvo, ArrayList<ValveVO> vlist) {
			
		long start = System.currentTimeMillis();

		session = sessionFactory.openSession();
		
		if (fvo != null) {
			session.insert(insertFurnaceID, fvo);
			session.insert(insertFurnaceZoneID, fvo.getZone1());
			session.insert(insertFurnaceZoneID, fvo.getZone2());
			session.insert(insertFurnaceZoneID, fvo.getZone3());
		}

		if (vlist != null && vlist.size() > 0) {
			for (ValveVO vo : vlist) {
				session.insert(insertValveID, vo);
			}
		}
		
		session.commit();
		session.close();

		logger.info("Data Store in MYSQL FOR {} mills",
				(System.currentTimeMillis() - start));
	}

	public void insertRunning(RunningTimeVO runningVO) {
		session = sessionFactory.openSession();
		session.insert("addRunning", runningVO);
		session.commit();
		session.close();
	}
}
