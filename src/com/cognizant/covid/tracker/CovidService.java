/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.cognizant.covid.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVWriter;

@Service
public class CovidService {
	
	private static final Logger covidLogger = Logger.getLogger(CovidService.class);
	
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:tcp://localhost/C:/poolscoring/covid";
	static final String USER = "cm9vdA=="; 
	static final String PASS = "cm9vdA==";

	public String getMaxGroupName() {
		String groupId = getMaxGroupId();
		String groupName = "Test_Group"+groupId;
		return groupName;
	}
	public static String decodeBas64(String Value){
		byte[] base64decodedBytes = Base64.getDecoder().decode(Value);
		String val ="";
		try {
			val  = new String(base64decodedBytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return val;
	}
	public String createNewGroup(String groupName){
		String groupId = getMaxGroupId();
		Connection conn = null;
		PreparedStatement stmt = null;
		String status = "";
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("INSERT INTO TEST_GROUPS(GROUP_ID,NAME) VALUES(?,?)"); 
			try{
				int groupIdInt = Integer.parseInt(groupId);
				stmt.setInt(1, groupIdInt);
				stmt.setString(2, groupName);
				stmt.executeUpdate();
				status = "success";
			}catch(Exception e) {
				covidLogger.error("Invalid Group Id");
				status = "failed";
			}
		} catch (SQLException e) {
			covidLogger.error("SQL Exception while Inserting new test group");
			status = "failed";
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("SQL Exception while Inserting new test group");
			}
		}
		return status;
	}

	public boolean writeGroupDataToCsv(String groupId,String filePath) {
		Connection conn = null;
		Statement stmt = null;
		boolean returnVal = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.createStatement();
			boolean validateResult = validateString(filePath);
			if(validateResult)
				validateResult = validateString(filePath);
			if(validateResult) {
				try {
					String exportData = "call CSVWRITE('"+filePath+"','SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID="+groupId+"')";
					stmt.execute(exportData);
					returnVal = true;
				}catch(Exception e) {
					covidLogger.error("SQL Exception while Exporting Group Data to csv");
					returnVal = false;
				}
			}else {
				returnVal = false;
			}
		}catch(Exception e){
			covidLogger.error("SQL Exception while Exporting Group Data to csv");
			returnVal = false;
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("SQL Exception while Exporting Group Data to csv");
			}
		}
		return returnVal;
	}
	
	public boolean deleteGroupFromAssessmentTable(String groupId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean returnVal = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.prepareStatement("DELETE FROM ASSESSMENT_TABLE WHERE GROUP_ID=?");
			try {
				int groupIdInt = Integer.parseInt(groupId);
				stmt.setInt(1, groupIdInt);
				stmt.executeUpdate();
				returnVal = true;
			}catch(Exception e) {
				covidLogger.error("Exception while Deleting Group. Invalid Group Id");
				returnVal = false;
			}
			//returnVal = true;
		}catch(Exception e) {
			covidLogger.error("SQL Exception while Deleting Group.");
			returnVal = false;
		}finally {
			try {
				if(conn!=null) {
					conn.close();
				}
				if(stmt!=null) {
					stmt.close();
				}
			}catch(Exception e) {
				covidLogger.error("SQL Exception while Deleting Group.");
			}
		}
		return returnVal;
	}
	
	public boolean runAlgoFromDb(String groupName) {
		String batFileName = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"risk_scoring.bat";
		String inputPath = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"input"+File.separator+"pool_data.csv";
		String outputPath = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"output"+File.separator+"risk_calc_output.csv";
		String groupId = getGroupIdFromName(groupName);
		boolean writeStatus = writeGroupDataToCsv(groupId,inputPath);
		if(!writeStatus) {
			return false;
		}
		ProcessBuilder pb = new ProcessBuilder(batFileName, inputPath, outputPath);
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		try{
			Process p = pb.start();
			stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String str = null;
			while ((str = stdInput.readLine()) != null) {
				covidLogger.info(str);
			}
			while ((str = stdError.readLine()) != null) {
				covidLogger.info(str);
			}
		}catch(Exception e){
			covidLogger.error("Error while running python code");
		}finally{
			try{
				if(stdInput != null) {
					stdInput.close();
				}
				if(stdError != null) {
					stdError.close();
				}
			}catch(Exception e){
				covidLogger.error("Error while running python code");
			}
		}
		boolean deleteStatus = deleteGroupFromAssessmentTable(groupId);
		if(!deleteStatus) {
			return false;
		}
		boolean returnVal = true;
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.createStatement();
			String getHighRiskPoolNumbers = "INSERT INTO ASSESSMENT_TABLE(IND_ID,GROUP_ID,AGE,GENDER,ZIPCODE,HEIGHT,WEIGHT,HOUSEHOLD,MEDICAL_CONDITIONS,TRAVEL_HISTORY,SCORE,RISK,POOL_NUMBER)\r\n" + 
					"SELECT IND_ID,GROUP_ID,Age,Gender,Zipcode,Height,Weight,Household,Medical_Conditions,Travel_History,Score,Risk,Pool_Number FROM CSVREAD('"+outputPath+"')";
			stmt.execute(getHighRiskPoolNumbers);
			returnVal = true;
		}catch(Exception e){
			covidLogger.error("Error while inserting python input");
			returnVal = false;
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while inserting python input");
			}
		}
		return returnVal;
	}
	
	public String getMaxGroupId() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String groupId="";
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT MAX(GROUP_ID) AS MAX_ID FROM TEST_GROUPS"); 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				groupId = Integer.toString(rs.getInt("MAX_ID")+1);
			}
			if(StringUtils.isEmpty(groupId)) {
				groupId = "1";
			}
			rs.close();
		} catch (SQLException e) {
			covidLogger.error("Error while getting max group id");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting max group id");
			}
		}
		return groupId;
	}
	
	public boolean addGroup(String groupName) {
		String groupId = getMaxGroupId();
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean returnVal = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("INSERT INTO TEST_GROUPS(GROUP_ID,NAME) VALUES(?,?)");
			stmt.setString(1, groupId);
			stmt.setString(2, groupName);
			stmt.execute();
			returnVal = true;
		} catch (SQLException e) {
			covidLogger.error("Error while adding a new group");
			returnVal = false;
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while adding a new group");
			}
		}
		return returnVal;
	}
	
	public boolean saveAssessmentData(HashMap<String,String> assessmentData) {
		String ind_id = "";
		String groupName = assessmentData.get("groupName");
		String groupId = getGroupIdFromName(groupName);
		Connection conn = null;
		PreparedStatement stmt = null;
		String age = assessmentData.get("age").trim().equals("")?"NULL":assessmentData.get("age");
    	String gender = assessmentData.get("gender").trim().equals("")?"NULL":assessmentData.get("gender").trim();
    	String zipcode = assessmentData.get("zipcode").trim().equals("")?"NULL":assessmentData.get("zipcode").trim();
    	String height = assessmentData.get("height").trim().equals("")?"NULL":assessmentData.get("height").trim();
    	String weight = assessmentData.get("weight").trim().equals("")?"NULL":assessmentData.get("weight");
    	String household = assessmentData.get("household").trim().equals("")?"NULL":assessmentData.get("household").trim();
    	String medicalConditions = assessmentData.get("medicalConditions").trim().equals("")?"NULL":assessmentData.get("medicalConditions").trim();
    	String travelHistory = assessmentData.get("travelHistory").trim().equals("")?"NULL":assessmentData.get("travelHistory").trim();
		boolean returnVal = true;
    	try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT MAX(IND_ID) AS MAX_ID FROM ASSESSMENT_TABLE"); 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				ind_id = Integer.toString(rs.getInt("MAX_ID")+1);
			}
			rs.close();
    	}catch(Exception e) {
    		covidLogger.error("Error while getting max id for individual");
    	}finally {
    		try {
	    		if(conn!=null) {
	    			conn.close();
	    		}
	    		if(stmt!=null) {
	    			stmt.close();
	    		}
    		}catch(Exception e) {
    			covidLogger.error("Error while getting max id for individual");
    		}
    	}
    	Connection conn1 = null;
    	PreparedStatement stmt1= null;
    	try {
    		conn1 = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt1 = conn1.prepareStatement("INSERT INTO ASSESSMENT_TABLE(IND_ID,GROUP_ID,AGE,GENDER,ZIPCODE,HEIGHT,WEIGHT,HOUSEHOLD,MEDICAL_CONDITIONS,TRAVEL_HISTORY) VALUES(?,?,?,?,?,?,?,?,?,?)"); 
			stmt1.setString(1, ind_id);
			stmt1.setString(2, groupId);
			stmt1.setString(3, age);
			stmt1.setString(4, gender);
			stmt1.setString(5, zipcode);
			stmt1.setString(6, height);
			stmt1.setString(7, weight);
			stmt1.setString(8, household);
			stmt1.setString(9, medicalConditions);
			stmt1.setString(10, travelHistory);
			stmt1.execute();
			returnVal = true;
		}catch(SQLException se) { 
			covidLogger.error("Error while getting inserting individual data");
			returnVal = false;
		} catch(Exception e) { 
			covidLogger.error("Error while getting inserting individual data");
			returnVal = false;
		}finally {
			try {
				if(conn1 != null) {
					conn1.close();
				}
				if(stmt1 != null) {
					stmt1.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting inserting individual data");
			}
		}
		return returnVal;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getDataFromDatabase(JSONArray csvArray, HttpServletRequest request) {
		Connection conn = null; 
		PreparedStatement stmt = null;
		String groupName=request.getParameter("groupName");
		try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			String groupId = getGroupIdFromName(groupName);
			boolean validateResult = validateString(groupId);
			if(!validateResult) {
				JSONArray returnArr = new JSONArray();
				return returnArr;
			}
			stmt = conn.prepareStatement("SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID= ?"); 
			stmt.setString(1, groupId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				JSONObject assessmentGroup = new JSONObject();
				assessmentGroup.put("id",Integer.toString(rs.getInt("IND_ID")));
				assessmentGroup.put("age",rs.getString("AGE"));
				assessmentGroup.put("height",rs.getString("HEIGHT"));
				assessmentGroup.put("weight",rs.getString("WEIGHT"));
				assessmentGroup.put("gender", rs.getString("GENDER"));
				assessmentGroup.put("zipcode", rs.getString("ZIPCODE"));
				assessmentGroup.put("household", rs.getString("HOUSEHOLD"));
				assessmentGroup.put("medicalConditions", rs.getString("MEDICAL_CONDITIONS"));
				assessmentGroup.put("travelHistory", rs.getString("TRAVEL_HISTORY"));
				csvArray.add(assessmentGroup);
			}
			rs.close();
		}catch(SQLException se) { 
			covidLogger.error("Error while getting assessment data from db");
		} catch(Exception e) { 
			covidLogger.error("Error while getting assessment data from db");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting assessment data from db");
			}
		}
		return csvArray;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDataResult(JSONArray csvArray) {
		Connection conn = null; 
		PreparedStatement stmt = null;
		try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT * FROM ASSESSMENT_TABLE"); 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				JSONObject assessmentGroup = new JSONObject();
				assessmentGroup.put("id",Integer.toString(rs.getInt("IND_ID")));
				assessmentGroup.put("gender", rs.getString("GENDER"));
				assessmentGroup.put("zipcode", rs.getString("ZIPCODE"));
				assessmentGroup.put("household", rs.getString("HOUSEHOLD"));
				assessmentGroup.put("medicalConditions", rs.getString("MEDICAL_CONDITIONS"));
				assessmentGroup.put("travelHistory", rs.getString("TRAVEL_HISTORY"));
				assessmentGroup.put("score", rs.getString("SCORE"));
				assessmentGroup.put("risk", rs.getString("RISK"));
				assessmentGroup.put("poolNo", rs.getString("POOL_NUMBER"));
				csvArray.add(assessmentGroup);
			}
			rs.close();
		}catch(SQLException se) { 
			covidLogger.error("Error while getting assessment data from db");
		} catch(Exception e) { 
			covidLogger.error("Error while getting assessment data from db");
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting assessment data from db");
			}
		}
		return csvArray;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getFilteredDataResult(JSONArray csvArray, HttpServletRequest request) {
		Connection conn = null; 
		PreparedStatement stmt = null;
		boolean validateResult = true;
		try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			String risk = request.getParameter("risk");
			String poolNumber = request.getParameter("poolNumber");
			String groupName = request.getParameter("groupName");
			String groupId = getGroupIdFromName(groupName);
			if(risk==null || risk.equals("null")) {
				stmt = conn.prepareStatement("SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID=?"); 
				int groupIdInt = Integer.parseInt(groupId);
				stmt.setInt(1,groupIdInt);
				validateResult = validateString(groupId);
			}
			else if(poolNumber == null || poolNumber.equals("null")) {
				stmt = conn.prepareStatement("SELECT * FROM ASSESSMENT_TABLE WHERE RISK=? AND GROUP_ID=?"); 
				stmt.setString(1, risk);
				if(!(risk.equalsIgnoreCase("High") || risk.equalsIgnoreCase("Medium")||risk.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
				if(validateResult)
					validateResult = validateString(groupId);
				int groupIdInt = Integer.parseInt(groupId);
				stmt.setInt(2,groupIdInt);
			}else {
				poolNumber = poolNumber.split("[\\(\\)]")[0];
				stmt = conn.prepareStatement("SELECT * FROM ASSESSMENT_TABLE WHERE RISK=? AND POOL_NUMBER=? AND GROUP_ID=?");
				stmt.setString(1, risk);
				if(!(risk.equalsIgnoreCase("High") || risk.equalsIgnoreCase("Medium")||risk.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
				stmt.setString(2, poolNumber);
				if(validateResult)
					validateResult = validateString(poolNumber);
				if(validateResult)
					validateResult = validateString(groupId);
				int groupIdInt = Integer.parseInt(groupId);
				stmt.setInt(3,groupIdInt);
			}
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					JSONObject assessmentGroup = new JSONObject();
					assessmentGroup.put("id",Integer.toString(rs.getInt("IND_ID")));
					assessmentGroup.put("age",rs.getString("AGE"));
					assessmentGroup.put("height",rs.getString("HEIGHT"));
					assessmentGroup.put("weight",rs.getString("WEIGHT"));
					assessmentGroup.put("gender", rs.getString("GENDER"));
					assessmentGroup.put("zipcode", rs.getString("ZIPCODE"));
					assessmentGroup.put("household", rs.getString("HOUSEHOLD"));
					assessmentGroup.put("medicalConditions", rs.getString("MEDICAL_CONDITIONS").equals("nan")?"":rs.getString("MEDICAL_CONDITIONS"));
					assessmentGroup.put("travelHistory", rs.getString("TRAVEL_HISTORY"));
					assessmentGroup.put("score", rs.getString("SCORE"));
					assessmentGroup.put("risk", rs.getString("RISK"));
					assessmentGroup.put("poolNo", rs.getString("POOL_NUMBER"));
					csvArray.add(assessmentGroup);
				}
				rs.close();
			}else {
				csvArray = new JSONArray();
			}
			
		}catch(SQLException se) { 
			covidLogger.error("Error while getting filtered assessment data from db");
		} catch(Exception e) { 
			covidLogger.error("Error while getting filtered assessment data from db");
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting filtered assessment data from db");
			}
		}
		return csvArray;
	}

	
	
	@SuppressWarnings("unchecked")
	public JSONArray getTestCaseGroups(JSONArray testGroupArray) {
		Connection conn = null; 
		PreparedStatement stmt = null;
		List<HashMap<String,String>> groupIdList = new ArrayList<HashMap<String,String>>();
		try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			String getGroupId = "SELECT * FROM TEST_GROUPS";
			stmt = conn.prepareStatement(getGroupId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				HashMap<String,String> groupMap = new HashMap<String,String>();
				String groupId = Integer.toString(rs.getInt("GROUP_ID"));
				groupMap.put("groupId",groupId);
				groupMap.put("groupName",rs.getString("NAME"));
				groupIdList.add(groupMap);
			}
			rs.close();
		}catch(Exception e) {
			covidLogger.error("Error while getting assessment data from db");
		}finally {
			try {
				if(conn!=null) {
					conn.close();
				}
				if(stmt!=null) {
					stmt.close();
				}
			}catch(Exception e) {
				covidLogger.error("Error while getting assessment data from db");
			}
		}
		Connection conn1 = null;
		PreparedStatement stmt1 = null;
		try {
			conn1 = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			for(HashMap<String,String> groupMap : groupIdList) {
				String groupId = groupMap.get("groupId");
				stmt1 = conn1.prepareStatement("SELECT COUNT(*) AS RECORD_COUNT FROM ASSESSMENT_TABLE WHERE GROUP_ID=?"); 
				stmt1.setString(1, groupId);
				ResultSet rs2 = stmt1.executeQuery();
				while(rs2.next()) {
					JSONObject getTestCaseGroupsObj = new JSONObject();
					for(HashMap<String,String> groupMap1:groupIdList) {
						if(groupMap1.get("groupId")==groupId)
							getTestCaseGroupsObj.put("name", groupMap1.get("groupName"));
					}
					getTestCaseGroupsObj.put("recordCount", rs2.getInt("RECORD_COUNT"));
					testGroupArray.add(getTestCaseGroupsObj);
				}
				rs2.close();
			}
		}catch(SQLException se) { 
			covidLogger.error("Error while getting assessment data from db");
		} catch(Exception e) { 
			covidLogger.error("Error while getting assessment data from db");
		} finally {
			try {
				if(conn1 != null) {
					conn1.close();
				}
				if(stmt1 != null) {
					stmt1.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting assessment data from db");
			}
		}
		return testGroupArray;
	}
	
	public String getPoolNumbersByRisk(String groupId,String risk) {
		String pool_numbers = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean validateResult = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT COUNT(POOL_NUMBER) AS POOL_COUNT,POOL_NUMBER,RISK FROM (SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID=?) GROUP BY POOL_NUMBER,RISK HAVING RISK=?");
			stmt.setString(1, groupId);
			stmt.setString(2, risk);
			if(!(risk.equalsIgnoreCase("High") || risk.equalsIgnoreCase("Medium")||risk.equalsIgnoreCase("Low"))) {
				validateResult = false;
			}
			if(validateResult)
				validateResult = validateString(groupId);
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()) {
					String pool_number = rs.getString("pool_number");
					String pool_count = Integer.toString(rs.getInt("POOL_COUNT"));
					pool_numbers += pool_number+"("+pool_count+")"+",";
				}
				pool_numbers = pool_numbers.substring(0,pool_numbers.length()-1);
				rs.close();
			}else {
				pool_numbers = "NA";
			}
		}catch(Exception e){
			covidLogger.error("Error while getting pool numbers");
			pool_numbers = "NA";
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting pool numbers");
			}
		}
		return pool_numbers;
	}

	private String getIndividualCountByRisk(String groupId,String risk) {
		String individual_count = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean validateResult = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			System.out.println("Getting all countries from db"); 
			stmt = conn.prepareStatement("SELECT COUNT(*) AS IND_COUNT FROM ASSESSMENT_TABLE WHERE RISK= ? AND GROUP_ID= ?");
			stmt.setString(1, risk);
			stmt.setString(2, groupId);
			if(!(risk.equalsIgnoreCase("High") || risk.equalsIgnoreCase("Medium")||risk.equalsIgnoreCase("Low"))) {
				validateResult = false;
			}
			if(validateResult)
				validateResult = validateString(groupId);
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()) {
					individual_count = Integer.toString(rs.getInt("IND_COUNT"));
				}
				rs.close();
			}
			else {
				individual_count = "NA";
			}
		}catch(Exception e){
			covidLogger.error("Error while getting individual count by risk");
			individual_count = "NA";
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting individual count by risk");
			}
		}
		return individual_count;
	}
	
	private String getScoreRangeByRisk(String groupId,String risk) {
		String range = "";
		Connection conn = null;
		PreparedStatement stmt = null; 
		boolean validateResult = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT MIN(SCORE) AS MIN_SCORE,MAX(SCORE) AS MAX_SCORE FROM ASSESSMENT_TABLE WHERE RISK= ? AND GROUP_ID= ?");
			stmt.setString(1, risk);
			stmt.setString(2, groupId);
			if(!(risk.equalsIgnoreCase("High") || risk.equalsIgnoreCase("Medium")||risk.equalsIgnoreCase("Low"))) {
				validateResult = false;
			}
			if(validateResult)
				validateResult = validateString(groupId);
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()) {
					range = rs.getString("MIN_SCORE")+" - "+rs.getString("MAX_SCORE");
				}
				rs.close();
			}else {
				range = "NA";
			}
		}catch(Exception e){
			covidLogger.error("Error while getting score range by risk");
			range = "NA";
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting score range by risk");
			}
		}	
		return range;
		
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getGraphicalDetailData(HttpServletRequest request) {
		String groupName = request.getParameter("groupName");
		ArrayList<String> paramList = new ArrayList<String>();
		boolean validateUserInput = validateUserInput(request,paramList);
		if(!validateUserInput) {
			JSONObject returnObj = new JSONObject();
			return returnObj;
		}
		String groupId = getGroupIdFromName(groupName);
		boolean validateString = validateString(groupId);
		if(!validateString) {
			JSONObject returnObj = new JSONObject();
			return returnObj;
		}
		JSONObject riskDetails = new JSONObject();
		JSONObject highRiskObj = new JSONObject();
		highRiskObj.put("pool_numbers",getPoolNumbersByRisk(groupId,"High"));
		highRiskObj.put("score_range",getScoreRangeByRisk(groupId,"High"));
		highRiskObj.put("individual_count",getIndividualCountByRisk(groupId,"High"));
		riskDetails.put("high_risk",highRiskObj);
		JSONObject mediumRiskObj = new JSONObject();
		mediumRiskObj.put("pool_numbers",getPoolNumbersByRisk(groupId,"Medium"));
		mediumRiskObj.put("score_range",getScoreRangeByRisk(groupId,"Medium"));
		mediumRiskObj.put("individual_count",getIndividualCountByRisk(groupId,"Medium"));
		riskDetails.put("medium_risk",mediumRiskObj);
		JSONObject lowRiskObj = new JSONObject();
		lowRiskObj.put("pool_numbers",getPoolNumbersByRisk(groupId,"Low"));
		lowRiskObj.put("score_range",getScoreRangeByRisk(groupId,"Low"));
		lowRiskObj.put("individual_count",getIndividualCountByRisk(groupId,"Low"));
		riskDetails.put("low_risk",lowRiskObj);
		return riskDetails;
	}
	
	private int getMaxIndIdForGroup(String groupId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		int ind_id=1;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT MAX(IND_ID) AS MAX_ID FROM ASSESSMENT_TABLE WHERE GROUP_ID = ?"); 
			stmt.setString(1, groupId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				ind_id = rs.getInt("MAX_ID")+1;
			}
			if(ind_id==0) {
				ind_id = 1;
			}
			rs.close();
		} catch (SQLException e) {
			covidLogger.error("Error while getting max individual id");
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting max individual id");
			}
		}
		return ind_id;
	}
	
	private void convertXlsmToCsv(String groupId,String filePath) {
		ArrayList<String[]> rowList = new ArrayList<String[]>();
		String header = "IND_ID,GROUP_ID,AGE,GENDER,ZIPCODE,HEIGHT,WEIGHT,HOUSEHOLD,MEDICAL_CONDITIONS,TRAVEL_HISTORY,Score,Risk,Pool_Number";
		rowList.add(header.split(","));
		if(filePath.contains(".xlsx")) {
			File f = new File(filePath);
			int ind_id = getMaxIndIdForGroup(groupId);
			FileInputStream file =null;
			try {
				file = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook(file);
				XSSFSheet templateSheet = workbook.getSheetAt(0);
				XSSFCell cell;
				DataFormatter df= new DataFormatter();
				Iterator<Row> iterator = templateSheet.iterator();
				while (iterator.hasNext()) {
					ArrayList<String> row = new ArrayList<String>();
					Row templateRow = iterator.next();
					if(templateRow.getRowNum()<=2){
						continue; //just skip the rows if row number is 0 
					}
					cell = (XSSFCell) templateRow.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell = (XSSFCell) templateRow.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String age = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String sex = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String feetStr = df.formatCellValue(cell);
					double feet = 0.0;
					if(feetStr!="") {
						feet = Double.parseDouble(df.formatCellValue(cell));
					}
					cell = (XSSFCell) templateRow.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String inchesStr = df.formatCellValue(cell);
					double inches = 0.0;
					if(inchesStr!="") {
						inches = Double.parseDouble(df.formatCellValue(cell));
					}
					double height = (feet*30)+(inches*2.5);
					cell = (XSSFCell) templateRow.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String weight= df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(6,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String hypertension = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String diabetes = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String copd = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String ckd = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String cvd = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(11, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String asthma = df.formatCellValue(cell);
					String medical = "";
					if(hypertension.equalsIgnoreCase("Yes")) {
						medical = medical+"Hypertension,";
					}if(diabetes.equalsIgnoreCase("Yes")) {
						medical = medical+"Diabetes,";
					}if(copd.equalsIgnoreCase("Yes")) {
						medical = medical+"COPD,";
					}if(ckd.equalsIgnoreCase("Yes")) {
						medical = medical+"CKD,";
					}if(cvd.equalsIgnoreCase("Yes")) {
						medical = medical+"CVD,";
					}if(asthma.equalsIgnoreCase("Yes")) {
						medical = medical+"Asthma,";
					}
					if(medical!="")
						medical=medical.substring(0,medical.length()-1);
					cell = (XSSFCell) templateRow.getCell(12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String travelHistory = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(13, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String zip = df.formatCellValue(cell);
					cell = (XSSFCell) templateRow.getCell(14, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					String residenceType = df.formatCellValue(cell);
					row.add(Integer.toString(ind_id));
					ind_id++;
					row.add(groupId);
					row.add(age);
					row.add(sex);
					row.add(zip);
					row.add(Double.toString(height));
					row.add(weight);
					row.add(residenceType);
					row.add(medical);
					row.add(travelHistory);
					row.add("");
					row.add("");
					row.add("");
					rowList.add((String[]) row.toArray(new String[row.size()]));
				}
				 file.close();
				workbook.close();
			}catch(Exception e) {
				covidLogger.error("Error while converting excel to csv");
			}finally {
				 if (file != null) {
					 try {
						 file.close();
					 } catch (IOException e) {
					
					 }
				}
			}
			CSVWriter writer;
			try {
				FileWriter fwriter = new FileWriter("C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"input"+File.separator+"pool_data.csv");
				writer = new CSVWriter(fwriter);
				for (String[] array : rowList) {
			        writer.writeNext(array);
			    }
			    writer.close();
			    fwriter.close();
			} catch (IOException e) {
				covidLogger.error("Error while writing excel to csv output");
			}
		}
	}
	
	private String getGroupIdFromName(String groupName) {
		groupName = groupName.trim();
		Connection conn = null;
		PreparedStatement stmt = null;
		String groupId = "";
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT GROUP_ID FROM TEST_GROUPS WHERE NAME=?"); 
			stmt.setString(1, groupName);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				groupId = Integer.toString(rs.getInt("GROUP_ID"));
			}
			rs.close();
		} catch (SQLException e) {
			covidLogger.error("Error while getting group id from name");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting group id from name");
			}
		}
		return groupId;
	}
	
	public boolean uploadDataInGroup(String groupName, String filePath) {
		Connection conn = null;
		Statement stmt = null;
		String groupId = null;
		groupId = getGroupIdFromName(groupName);
		convertXlsmToCsv(groupId,filePath);
		String inputPath = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"input"+File.separator+"pool_data.csv";
		boolean returnVal = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.createStatement();
			String getHighRiskPoolNumbers = "INSERT INTO ASSESSMENT_TABLE(IND_ID,GROUP_ID,AGE,GENDER,ZIPCODE,HEIGHT,WEIGHT,HOUSEHOLD,MEDICAL_CONDITIONS,TRAVEL_HISTORY,SCORE,RISK,POOL_NUMBER)\r\n" + 
					"SELECT IND_ID,GROUP_ID,Age,Gender,Zipcode,Height,Weight,Household,Medical_Conditions,Travel_History,Score,Risk,Pool_Number FROM CSVREAD('"+inputPath+"')";
			stmt.execute(getHighRiskPoolNumbers);
			returnVal = true;
		}catch(Exception e){
			covidLogger.error("Error while uploading assessment data from template");
			returnVal = false;
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while uploading assessment data from template");
			}
		}
		return returnVal;
	}
	
	public boolean uploadData(String groupName, String filePath) {
		Connection conn = null;
		Statement stmt = null;
		boolean createGroup = addGroup(groupName);
		String groupId = null;
		if(createGroup) {
			groupId = getGroupIdFromName(groupName);
		}else {
			return false;
		}
		convertXlsmToCsv(groupId,filePath);
		String inputPath = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"input"+File.separator+"pool_data.csv";
		boolean deleteStatus = deleteGroupFromAssessmentTable(groupId);
		if(!deleteStatus) {
			return false;
		}
		boolean returnVal = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.createStatement();
			String getHighRiskPoolNumbers = "INSERT INTO ASSESSMENT_TABLE(IND_ID,GROUP_ID,AGE,GENDER,ZIPCODE,HEIGHT,WEIGHT,HOUSEHOLD,MEDICAL_CONDITIONS,TRAVEL_HISTORY,SCORE,RISK,POOL_NUMBER)\r\n" + 
					"SELECT IND_ID,GROUP_ID,Age,Gender,Zipcode,Height,Weight,Household,Medical_Conditions,Travel_History,Score,Risk,Pool_Number FROM CSVREAD('"+inputPath+"')";
			stmt.execute(getHighRiskPoolNumbers);
			returnVal = true;
		}catch(Exception e){
			covidLogger.error("Error while uploading assessment data from template");
			returnVal = false;
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while uploading assessment data from template");
			}
		}
		return returnVal;
	}
	
	public void exportPoolData(HttpServletRequest request,String fullPath) {
		Connection conn =null;
		Statement stmt= null;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.createStatement();
			String groupName = request.getParameter("groupName");
			String risk = request.getParameter("riskType");
			String poolNumber = request.getParameter("poolNumber");
			String group_id = getGroupIdFromName(groupName);
			String getTestCaseGroupsQuery = "";
			boolean validateResult = true;
			if((poolNumber == null || poolNumber.equals("null")) && risk!=null) {
				validateResult = validateString(fullPath);
				if(validateResult)
					validateResult = validateString(group_id);
				if(validateResult)
					validateResult = validateString(risk);
				getTestCaseGroupsQuery = "call CSVWRITE('"+fullPath+"','SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID="+group_id+" AND RISK=''"+risk+"''')";
			}else if(poolNumber == null && risk == null){
				validateResult = validateString(fullPath);
				if(validateResult)
					validateResult = validateString(group_id);
				getTestCaseGroupsQuery = "call CSVWRITE('"+fullPath+"','SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID="+group_id+"')";
			}
			else {
				poolNumber = poolNumber.split("[\\(\\)]")[0];
				validateResult = validateString(fullPath);
				if(validateResult)
					validateResult = validateString(group_id);
				if(validateResult)
					validateResult = validateString(risk);
				if(validateResult)
					validateResult = validateString(poolNumber);
				getTestCaseGroupsQuery = "call CSVWRITE('"+fullPath+"','SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID="+group_id+" AND RISK=''"+risk+"'' AND POOL_NUMBER=''"+poolNumber+"''')";
			}
			if(validateResult)
				stmt.execute(getTestCaseGroupsQuery);
		}catch(Exception e){
			covidLogger.error("Error while exporting pool risk output");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while exporting pool risk output");
			}
		}
	}
	
	public void exportPoolDataOutput(String groupName,String fullPath) {
		Connection conn =null;
		Statement stmt= null;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			stmt = conn.createStatement();
			String group_id = getGroupIdFromName(groupName);
			boolean validateString = validateString(fullPath);
			if(validateString)
				validateString = validateString(group_id);
			if(validateString)
				stmt.execute("call CSVWRITE('"+fullPath+"','SELECT * FROM ASSESSMENT_TABLE WHERE GROUP_ID='"+group_id+"'')");
		}catch(Exception e){
			covidLogger.error("Error while exporting pool risk output");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while exporting pool risk output");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getRiskPoolData(JSONArray riskPoolArray, HttpServletRequest request) {
		// TODO Auto-generated method stub
		Connection conn = null; 
		PreparedStatement stmt = null;
		try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("select pool_number, count(pool_number) AS poolCount, risk from assessment_table group by risk, pool_number"); 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				JSONObject riskPoolObj = new JSONObject();
				riskPoolObj.put("poolNo", rs.getInt("pool_number"));
				riskPoolObj.put("poolCount", rs.getString("poolCount"));
				riskPoolObj.put("risk", rs.getString("risk"));
				riskPoolArray.add(riskPoolObj);
			}
			rs.close();
		}catch(SQLException se) { 
			covidLogger.error("Error while getting pool risk data");
		} catch(Exception e) { 
			covidLogger.error("Error while getting pool risk data");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting pool risk data");
			}
		}
		return riskPoolArray;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getGraphDataForGroup(JSONArray graphArray) {
		Connection conn = null; 
		PreparedStatement stmt = null;
		try {
			Class.forName(JDBC_DRIVER); 
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));  
			stmt = conn.prepareStatement("SELECT AGE,GENDER,HOUSEHOLD,TRAVEL_HISTORY FROM ASSESSMENT_TABLE"); 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				JSONObject graphObj = new JSONObject();
				graphObj.put("gender", rs.getString("GENDER"));
				graphObj.put("household", rs.getString("HOUSEHOLD"));
				graphObj.put("travel", rs.getString("TRAVEL_HISTORY"));
				graphObj.put("age", rs.getString("AGE"));
				graphArray.add(graphObj);
			}
			rs.close();
		}catch(SQLException se) { 
			covidLogger.error("Error while getting graph data");
		} catch(Exception e) { 
			e.printStackTrace(); 
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting graph data");
			}
		}
		return graphArray;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getRiskDataDetails(String riskType, String category) {
		
		JSONObject RiskPoolData = new JSONObject();
		try {
			String riskName = "";
			if(category == null || category.equals("null")){
				riskName = riskType+" Risk Pool";
			}else{
				riskName = riskType+" Risk Pool - "+category;
			}
			RiskPoolData.put("risk",riskName);
			JSONObject scoreObj = getScoreRange(riskType,category);
			RiskPoolData.put("scoreRange", scoreObj);

			JSONArray houseHoldDetails = getHouseHoldDetails(riskType,category);
			RiskPoolData.put("houseHoldDetails", houseHoldDetails);

			JSONArray medicalDetails = getMedicalDetails(riskType,category);
			RiskPoolData.put("medicalDetails", medicalDetails);

		}catch(Exception e) { 
			covidLogger.error("Error while getting risk data details");
		} 
		return RiskPoolData;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getRiskDataDetailsByPool(String riskType, String category,String poolNumber) {
		JSONObject RiskPoolData = new JSONObject();
		try {
			System.out.println("Getting Graphical Detail Data from db");
			String riskName = "";
			System.out.println("category--"+category);
			if(category == null || category.equals("null")){
				riskName = riskType+" Risk Pool";
			}else{
				riskName = riskType+" Risk Pool - "+category;
			}
			RiskPoolData.put("risk",riskName);
			JSONObject scoreObj = getScoreRange(riskType,category);
			RiskPoolData.put("scoreRange", scoreObj);

			JSONArray houseHoldDetails = getHouseHoldDetails(riskType,category);
			RiskPoolData.put("houseHoldDetails", houseHoldDetails);

			JSONArray medicalDetails = getMedicalDetails(riskType,category);
			RiskPoolData.put("medicalDetails", medicalDetails);

		}catch(Exception e) { 
			covidLogger.error("Error while getting risk data details");
		} 
		return RiskPoolData;
	}


	@SuppressWarnings("unchecked")
	private JSONArray getMedicalDetails(String type, String category) {
		Connection conn =null;
		PreparedStatement stmt = null;
		JSONArray medicalArray = new JSONArray();
		boolean validateResult = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			if(category != null && !category.equals("null")){
				stmt = conn.prepareStatement("SELECT MEDICAL_CONDITIONS FROM ASSESSMENT_TABLE WHERE RISK = ? AND pool_number = ?");
				stmt.setString(1, type);
				stmt.setString(2, category);
				if(!(type.equalsIgnoreCase("High") || type.equalsIgnoreCase("Medium")||type.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
				if(validateResult)
					validateResult = validateString(category);
			}else {
				stmt = conn.prepareStatement("SELECT MEDICAL_CONDITIONS FROM ASSESSMENT_TABLE WHERE RISK = ?");
				stmt.setString(1, type);
				if(!(type.equalsIgnoreCase("High") || type.equalsIgnoreCase("Medium")||type.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
			}
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					JSONObject lowRiskMedical = new JSONObject();
					lowRiskMedical.put("medicalConditions", rs.getString("MEDICAL_CONDITIONS"));
					medicalArray.add(lowRiskMedical);
				}
				rs.close();
			}else {
				medicalArray = new JSONArray();
			}
		}catch(SQLException se) { 
			covidLogger.error("Error while getting medical details");
		} catch(Exception e) { 
			covidLogger.error("Error while getting medical details");
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting medical details");
			}
		}
		return medicalArray;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getHouseHoldDetails(String type, String category) {
		String queryAdd = "";
		Connection conn =null;
		PreparedStatement stmt= null;
		JSONArray houseHoldArray = new JSONArray();
		boolean validateResult = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			if(category != null && !category.equals("null")){
				stmt = conn.prepareStatement("SELECT HOUSEHOLD, COUNT(HOUSEHOLD) AS HOUSEHOLD_COUNT FROM ASSESSMENT_TABLE WHERE RISK = ? AND POOL_NUMBER = ? AND HOUSEHOLD IS NOT NULL GROUP BY HOUSEHOLD");
				stmt.setString(1, type);
				stmt.setString(2, category);
				if(!(type.equalsIgnoreCase("High") || type.equalsIgnoreCase("Medium")||type.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
				if(validateResult)
					validateResult = validateString(category);
			}else {
				stmt = conn.prepareStatement("SELECT HOUSEHOLD, COUNT(HOUSEHOLD) AS HOUSEHOLD_COUNT FROM ASSESSMENT_TABLE WHERE RISK = ? AND HOUSEHOLD IS NOT NULL GROUP BY HOUSEHOLD");
				stmt.setString(1, type);
				if(!(type.equalsIgnoreCase("High") || type.equalsIgnoreCase("Medium")||type.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
			}
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					JSONObject lowRiskHousehold = new JSONObject();
					lowRiskHousehold.put("household", rs.getString("HOUSEHOLD"));
					lowRiskHousehold.put("householdCount", rs.getInt("HOUSEHOLD_COUNT"));
					houseHoldArray.add(lowRiskHousehold);
				}
				rs.close();
			}else {
				houseHoldArray = new JSONArray();
			}
		}catch(SQLException se) { 
			covidLogger.error("Error while getting household details");
		} catch(Exception e) { 
			covidLogger.error("Error while getting household details");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting household details");
			}
		} 
		return houseHoldArray;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getScoreRange(String type, String category) {
		Connection conn =null;
		PreparedStatement stmt= null;
		JSONObject lowRiskScore = new JSONObject();
		boolean validateResult = true;
		try {
			conn = DriverManager.getConnection(DB_URL,decodeBas64(USER),decodeBas64(PASS));
			if(category != null && !category.equals("null")){
				stmt = conn.prepareStatement("SELECT COUNT(*) AS INDV_COUNT, MIN(SCORE) as min_score ,MAX(SCORE) as max_score FROM ASSESSMENT_TABLE WHERE RIKS = ? AND POOL_NUMBER = ?");
				stmt.setString(1, type);
				stmt.setString(2, category);
				if(!(type.equalsIgnoreCase("High") || type.equalsIgnoreCase("Medium")||type.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
				if(validateResult)
					validateResult = validateString(category);
			}else {
				stmt = conn.prepareStatement("SELECT COUNT(*) AS INDV_COUNT, MIN(SCORE) as min_score ,MAX(SCORE) as max_score FROM ASSESSMENT_TABLE WHERE RIKS = ?");
				stmt.setString(1, type);
				if(!(type.equalsIgnoreCase("High") || type.equalsIgnoreCase("Medium")||type.equalsIgnoreCase("Low"))) {
					validateResult = false;
				}
			}
			if(validateResult) {
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					lowRiskScore.put("indv_count", rs.getInt("INDV_COUNT"));
					lowRiskScore.put("max_score", rs.getInt("max_score"));
					lowRiskScore.put("min_score", rs.getInt("min_score"));
				}
				rs.close();
			}else {
				lowRiskScore = new JSONObject();
			}
		}catch(SQLException se) { 
			covidLogger.error("Error while getting score range details");
		} catch(Exception e) { 
			covidLogger.error("Error while getting score range details");
		}finally {
			try {
				if(conn != null) {
					conn.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			}catch (SQLException ex) {
				covidLogger.error("Error while getting score range details");
			}
		} 
		return lowRiskScore;
	}  
	
	public boolean validateUserInput(HttpServletRequest request,ArrayList<String> paramNames) {
		for(String paramName:paramNames) {
			String param = request.getParameter(paramName);
			param = param.trim();
			if(param==null) {
				return true;
			}
			boolean b = validateString(param);
			if(!b) {
				return b;
			}
		}
		return true;
	}
	
	public boolean validateString(String str) {
		if(str==null) {
			return true;
		}
		Pattern p = Pattern.compile("[\\[A-Za-z0-9{}:\\.\\-\"_,?&@\\\\()]]*");
		Matcher m = p.matcher(str);
		String testStr = m.replaceAll("");
		boolean b = false;
		if(testStr.trim().equals("")) {
			b = true;
		}
		System.out.println("Validation status for "+str+" is "+b);
		return b;
	}
	
	public boolean validateJSONOutput(String jsonString) {
		if(jsonString == null) {
			return true;
		}
		Pattern p = Pattern.compile("[\\[A-Za-z0-9{}:\\.\\-\"_,?&@\\\\()]]*");
		Matcher m = p.matcher(jsonString);
		String testStr = m.replaceAll("");
		boolean b = false;
		if(testStr.trim().equals("")) {
			b = true;
		}
		System.out.println("Validation status for "+jsonString+" is "+b);
		return b;
	}
}

/*
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
*/