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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CovidTracker {
	
	private static final Logger covidLogger = Logger.getLogger(CovidTracker.class);
	
	@Autowired CovidService covidservice;

	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getDataFromDatabase", method = RequestMethod.GET)
	public String getDataFromCSV(HttpServletRequest request) throws ParseException {
		JSONArray csvArray = new JSONArray();
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add("groupName");
		boolean validateUserInput = covidservice.validateUserInput(request,paramList);
		if(!validateUserInput) {
			return "";
		}
		csvArray = covidservice.getDataFromDatabase(csvArray,request);
		try {
			String returnString = csvArray.toString();
			boolean validateJsonOutput = covidservice.validateJSONOutput(returnString);
			if(validateJsonOutput) {
				return returnString;
			}else {
				return "";
			}
		}catch(Exception e) {
			return "";
		}	
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getDataResult", method = RequestMethod.GET)
	public String getDataResultFromCSV(HttpServletRequest request) throws ParseException {
		JSONArray csvArray = new JSONArray();
		csvArray = covidservice.getDataResult(csvArray);
		try{
			String returnString = StringUtils.EMPTY;
			if(csvArray.size() > 0) {
				returnString = csvArray.toString();
			}
			boolean validateJSONOutput = covidservice.validateJSONOutput(returnString);
			if(validateJSONOutput) {
				return returnString;
			}else {
				return "";
			}
		}catch(Exception e) {
			return "";
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getFilteredDataResult", method = RequestMethod.GET)
	public String getFilteredDataResultFromCSV(HttpServletRequest request) throws ParseException {
		JSONArray csvArray = new JSONArray();
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add("risk");
		paramList.add("poolNumber");
		paramList.add("groupName");
		boolean validateUserInput = covidservice.validateUserInput(request,paramList);
		if(!validateUserInput) {
			return "";
		}
		csvArray = covidservice.getFilteredDataResult(csvArray,request);
		try{
			String returnString = StringUtils.EMPTY;
			if(csvArray.size() > 0) {
				returnString = csvArray.toString();
			}
			boolean validateJSONOutput = covidservice.validateJSONOutput(returnString);
			if(validateJSONOutput) {
				return returnString;
			}else {
				return "";
			}
		}catch(Exception e) {
			return "";
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/addData", method = RequestMethod.GET)
	public ModelAndView addDataToTestGroup(HttpServletRequest request) throws ParseException {
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		ModelAndView mv = new ModelAndView("assessment_data");
		if(validateString)
			mv.addObject("groupName",groupName);
		else
			mv.addObject("groupName","");
		return mv;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/addNewGroup", method = RequestMethod.GET)
	public ModelAndView addNewGroup(HttpServletRequest request) throws ParseException {
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		ModelAndView mv = new ModelAndView("assessment_data");
		if(StringUtils.isNotBlank(groupName) && validateString) {
			covidservice.addGroup(groupName);
		}
		if(validateString)
			mv.addObject("groupName",groupName);
		else
			mv.addObject("groupName","");
		return mv;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/bulkUpload", method = RequestMethod.POST)
	public String bulkUpload(MultipartHttpServletRequest request,HttpServletRequest servletRequest) throws ParseException {
		javax.servlet.http.HttpSession session = request.getSession(false);
		Iterator<String> itr =  request.getFileNames();
		MultipartFile mpf = request.getFile(itr.next());		
		String filename = "";
		String fullPath = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"uploads";
		BufferedOutputStream stream =null;
		try {
			if(new File(fullPath+File.separator).exists()){
				FileUtils.cleanDirectory(new File(fullPath+ File.separator));
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (!mpf.isEmpty()) {
			filename = mpf.getOriginalFilename();
			if(filename.contains(".xlsx")&&fullPath.equalsIgnoreCase("C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"uploads")) {
				try {
					byte[] bytes = mpf.getBytes();
					filename = mpf.getOriginalFilename();

					File fpath = new File(fullPath);
					if (!fpath.exists()) {
						fpath.mkdirs();
					}
					stream = new BufferedOutputStream(new FileOutputStream(new File(fullPath,filename)));
					stream.write(bytes);
				}catch(Exception e){
					e.printStackTrace();
				}finally {
					try {
						if(stream != null) {
							stream.close();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else {
				covidLogger.info("Invalid File");
			}
		}
		String filePath = fullPath+File.separator+filename;
		session.setAttribute("poolFilePath",filePath);
		String groupName = "Test_Group"+covidservice.getMaxGroupId();
		return groupName;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/bulkUploadAtGroup", method = RequestMethod.POST)
	public String bulkUploadAtGroup(MultipartHttpServletRequest request,HttpServletRequest servletRequest) throws ParseException {
		javax.servlet.http.HttpSession session = request.getSession(false);
		String groupName = request.getParameter("groupName");
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add("groupName");
		boolean validateUserInput = covidservice.validateUserInput(request, paramList);
		if(!validateUserInput) {
			return "failure";
		}
		Iterator<String> itr =  request.getFileNames();
		MultipartFile mpf = request.getFile(itr.next());		
		String filename = "";
		String fullPath = "C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"uploads";
		BufferedOutputStream stream = null;
		try {
			if(new File(fullPath+File.separator).exists()){
				FileUtils.cleanDirectory(new File(fullPath+ File.separator));
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (!mpf.isEmpty()) {
			filename = mpf.getOriginalFilename();
			if(filename.contains(".xlsx")&&fullPath.equalsIgnoreCase("C:"+File.separator+File.separator+"covid_risk_scoring"+File.separator+"uploads")) {
				try {
					byte[] bytes = mpf.getBytes();
					filename = mpf.getOriginalFilename();

					File fpath = new File(fullPath);
					if (!fpath.exists()) {
						fpath.mkdirs();
					}
					stream = new BufferedOutputStream(new FileOutputStream(new File(fullPath,filename)));
					stream.write(bytes);
				}catch(Exception e){
					e.printStackTrace();
				}finally {
					try {
						if(stream != null) {
							stream.close();
						}	
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else {
				covidLogger.info("Invalid File");
			}
		}
		String filePath = fullPath+File.separator+filename;
		session.setAttribute("poolFilePath",filePath);
		return groupName;
	}
	
	@RequestMapping(value = "/pooled-testing-classifier/runAlgo", method = RequestMethod.POST)
	public ModelAndView runAlgo(HttpServletRequest request) throws ParseException {
		javax.servlet.http.HttpSession session = request.getSession(false);
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		ModelAndView mv = new ModelAndView("pool_data");
		if(StringUtils.isNotBlank(groupName)&&validateString) {
			boolean uploadStatus = covidservice.uploadData(groupName,session.getAttribute("poolFilePath").toString());
			if(uploadStatus)
				mv.addObject("groupName", groupName);
		}	
		return mv;
	}
	
	@RequestMapping(value = "/pooled-testing-classifier/runAlgoAtGroup", method = RequestMethod.POST)
	public ModelAndView runAlgoAtGroup(HttpServletRequest request) throws ParseException {
		javax.servlet.http.HttpSession session = request.getSession(false);
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		ModelAndView mv = new ModelAndView("pool_data");
		if(StringUtils.isNotBlank(groupName) && validateString) {
			boolean uploadStatus = covidservice.uploadDataInGroup(groupName,session.getAttribute("poolFilePath").toString());
			if(uploadStatus)
				mv.addObject("groupName", groupName);
		}	
		return mv;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/runAlgoFromDb", method = RequestMethod.GET)
	public ModelAndView runAlgoFrmDb(HttpServletRequest request) throws ParseException {
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		ModelAndView mv = new ModelAndView("pool_data_output");
		if(StringUtils.isNotBlank(groupName) && validateString) {
			boolean uploadStatus = covidservice.runAlgoFromDb(groupName);
			if(uploadStatus)
				mv.addObject("groupName", groupName);
		}
		return mv;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getGraphDataForGroup", method = RequestMethod.GET)
	public String getGraphDataForGroup(HttpServletRequest request) throws ParseException {
		JSONArray graphArray = new JSONArray();
		graphArray = covidservice.getGraphDataForGroup(graphArray);
		try{
			String returnString = StringUtils.EMPTY;
			if(graphArray.size() > 0) {
				returnString = graphArray.toString();
			}
			boolean validateJSONOutput = covidservice.validateJSONOutput(returnString);
			if(validateJSONOutput) {
				return returnString;
			}else {
				return "";
			}
		}catch(Exception e) {
			return "";
		}
	}
	
	@RequestMapping(value = "/pooled-testing-classifier/showTestCaseGroups", method = RequestMethod.GET)
	public ModelAndView showTestCaseGroups(HttpServletRequest request) throws ParseException {
		ModelAndView mv = new ModelAndView("index");
		return mv;
	}
	
	
	@RequestMapping(value = "/pooled-testing-classifier/viewTestCaseGroupsData", method = RequestMethod.GET)
	public ModelAndView viewTestCaseGroupsData(HttpServletRequest request) throws ParseException {
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		ModelAndView mv = new ModelAndView("pool_data");
		if(validateString)
			mv.addObject("groupName", groupName);
		else
			mv.addObject("groupName", "");
		return mv;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getTestCaseGroups", method = RequestMethod.GET)
	public String getTestCaseGroups(HttpServletRequest request) throws ParseException {
		JSONArray testGroupArray = new JSONArray();
		testGroupArray = covidservice.getTestCaseGroups(testGroupArray);
		try{
			String returnString = StringUtils.EMPTY;
			if(testGroupArray.size() > 0) {
				returnString = testGroupArray.toString();
			}
			boolean validateJSONOutput = covidservice.validateJSONOutput(returnString);
			if(validateJSONOutput) {
				return returnString;
			}else {
				return "";
			}
		}catch(Exception e) {
			return "";
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getMaxGroupName", method = RequestMethod.GET)
	public String getMaxGroupName(HttpServletRequest request) throws ParseException {
		String groupName = covidservice.getMaxGroupName();
		return groupName;
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/createGroup", method = RequestMethod.POST)
	public String createGroup(HttpServletRequest request) throws ParseException {
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		String status = "failed";
		if(StringUtils.isNotBlank(groupName) && validateString){
			status = covidservice.createNewGroup(groupName);
		}
		return status;
	}
	
	@RequestMapping(value = "/pooled-testing-classifier/downloadAssessmentTemplate", method = RequestMethod.GET)
	public void downloadAssessmentTemplate(HttpServletRequest request,HttpServletResponse response) {
		String root = request.getServletContext().getRealPath("/");
		String filePath = root+File.separator+"resources"+File.separator+"data"+File.separator+"assessment_template.xlsx";
		File templateFile = new File(filePath);
		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition",
				"attachment;filename=assessment_template.xlsx");
		try{
			fileInputStream = new FileInputStream(templateFile);
			responseOutputStream = response.getOutputStream();
			int bytes;
			while ((bytes = fileInputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}
			fileInputStream.close();
			responseOutputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fileInputStream != null) {
					fileInputStream.close();
				}
				if(responseOutputStream != null) {
					responseOutputStream.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@RequestMapping(value = "/pooled-testing-classifier/exportPoolDataOutput", method = RequestMethod.GET)
	public void exportPoolDataOutput(HttpServletRequest request,HttpServletResponse response) throws ParseException {
		String fullPath = "C:"+File.separator+File.separator+"pool-classifier-data"+File.separator+"exports";
		String fileName = "poolOutput.csv";
		File tempDir = new File(fullPath);
		if(!tempDir.exists()) {
			tempDir.mkdir();
		}
		File outputFile = new File(fullPath+File.separator+fileName);
		
		covidservice.exportPoolData(request, fullPath+File.separator+fileName);
		
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition",
				"attachment;filename=pool_data_output.csv");
		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;
		try{
			fileInputStream = new FileInputStream(outputFile);
			responseOutputStream = response.getOutputStream();
			int bytes;
			while ((bytes = fileInputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}
			fileInputStream.close();
			responseOutputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if(fileInputStream != null) {
					fileInputStream.close();
				}
				if(responseOutputStream != null) {
					responseOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}


		}
		try {
			if(new File(fullPath+File.separator).exists()){
				FileUtils.cleanDirectory(new File(fullPath+ File.separator));
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/pooled-testing-classifier/exportPoolData", method = RequestMethod.GET)
	public void exportPoolData(HttpServletRequest request,HttpServletResponse response) throws ParseException {
		String fullPath = "C:"+File.separator+File.separator+"pool-classifier-data"+File.separator+"exports";
		String fileName = "poolOutput.csv";
		File tempDir = new File(fullPath);
		if(!tempDir.exists()) {
			tempDir.mkdir();
		}
		File outputFile = new File(fullPath+File.separator+fileName);
		
		covidservice.exportPoolData(request,fullPath+File.separator+fileName);
		
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition",
				"attachment;filename=pool_data.csv");
		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;
		try{
			fileInputStream = new FileInputStream(outputFile);
			responseOutputStream = response.getOutputStream();
			int bytes;
			while ((bytes = fileInputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(fileInputStream != null) {
					fileInputStream.close();
				}
				if(responseOutputStream != null) {
					responseOutputStream.close();
				}
			}catch(IOException ie) {
				ie.printStackTrace();
			}
		}
		try {
			if(new File(fullPath+File.separator).exists()){
				FileUtils.cleanDirectory(new File(fullPath+ File.separator));
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/saveAssessmentData", method = RequestMethod.POST)
	public String saveAssessmentData(HttpServletRequest request) throws ParseException {
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add("groupName");
		paramList.add("age");
		paramList.add("gender");
		paramList.add("address");
		paramList.add("zipcode");
		paramList.add("height");
		paramList.add("weight");
		paramList.add("household");
		paramList.add("medicalConditions");
		paramList.add("travelHistory");
		boolean validateUserInput = covidservice.validateUserInput(request, paramList);
		if(!validateUserInput) {
			return "failure";
		}
		String groupName = request.getParameter("groupName");
    	String age = request.getParameter("age");
    	String gender = request.getParameter("gender");
    	String address = request.getParameter("address");
    	String zipcode = request.getParameter("zipcode");
    	String height = request.getParameter("height");
    	String weight = request.getParameter("weight");
    	String household = request.getParameter("household");
    	String medicalConditions = request.getParameter("medicalConditions");
    	String travelHistory = request.getParameter("travelHistory");
    	HashMap<String,String> assessmentData = new HashMap<String,String>();
    	assessmentData.put("groupName",groupName);
    	assessmentData.put("age",age);
    	assessmentData.put("gender",gender);
    	assessmentData.put("address",address);
    	assessmentData.put("zipcode",zipcode);
    	assessmentData.put("height",height);
    	assessmentData.put("weight",weight);
    	assessmentData.put("household",household);
    	assessmentData.put("medicalConditions",medicalConditions);
    	assessmentData.put("travelHistory",travelHistory);
    	boolean saveStatus = covidservice.saveAssessmentData(assessmentData);
    	if(saveStatus) {
    		return "success";
    	}else{
    		return "failure";
    	}
	}
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getGraphicalDetailData", method = RequestMethod.GET)
	public String getGraphicalDetailData(HttpServletRequest request) throws ParseException {
		String groupName = request.getParameter("groupName");
		boolean validateString = covidservice.validateString(groupName);
		if(!validateString) {
			return "";
		}
		JSONObject json  = covidservice.getGraphicalDetailData(request);
		try{
			String returnString = StringUtils.EMPTY;
			if(json.size() > 0) {
				returnString = json.toString();
			}
			boolean validateJSONOutput = covidservice.validateJSONOutput(returnString);
			if(validateJSONOutput) {
				return returnString;
			}else {
				return "";
			}
		}catch(Exception e) {
			return "";
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getRiskData", method = RequestMethod.GET)
	public String getRiskData(HttpServletRequest request) throws ParseException {
		String riskType = request.getParameter("type");
		String category = request.getParameter("category");
		ArrayList<String> validateList = new ArrayList<String>();
		validateList.add("type");
		validateList.add("category");
		boolean validateResult = covidservice.validateUserInput(request,validateList);
		if(!validateResult) {
			return "";
		}
		JSONObject json  = covidservice.getRiskDataDetails(riskType,category);
		try{
			String returnString = json.toString();
			boolean validateJsonResult = covidservice.validateJSONOutput(returnString);
			if(!validateJsonResult) {
				return "";
			}
			return returnString;
		}catch(Exception e) {
			return "";
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pooled-testing-classifier/getRiskDataByPool", method = RequestMethod.GET)
	public String getRiskDataByPool(HttpServletRequest request) throws ParseException {
		String riskType = request.getParameter("type");
		String category = request.getParameter("category");
		String poolNumber = request.getParameter("poolNumber");
		ArrayList<String> validateList = new ArrayList<String>();
		validateList.add("type");
		validateList.add("category");
		validateList.add("poolNumber");
		boolean validateResult = covidservice.validateUserInput(request,validateList);
		if(!validateResult) {
			return "";
		}
		JSONObject json  = covidservice.getRiskDataDetailsByPool(riskType,category,poolNumber);
		try{
			String returnString = json.toString();
			boolean validateJsonResult = covidservice.validateJSONOutput(returnString);
			if(!validateJsonResult) {
				return "";
			}
			return returnString;
		}catch(Exception e) {
			return "";
		}
	}
	
	public boolean validateInputFields(HttpServletRequest request) {
		List<String> houseHoldList = Arrays.asList("Mobile Home","Individual home","Housing in structures with more than 10 units","Housing in structures with less than 10 units","Community living");
		List<String> travelHistoryList = Arrays.asList("Yes","No");
		List<String> genderList = Arrays.asList("Male","Female");
		if(!houseHoldList.contains(request.getParameter("household")) || !travelHistoryList.contains(request.getParameter("travelHistory")) || !genderList.contains(request.getParameter("gender"))) {
			return false;
		}
		return true;
	}
}
/*
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
*/