<!--
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
-->

<html>
	<head>
		<link rel="stylesheet" href="<%=request.getContextPath()%>/resources/css/bootstrap.css">
		<link rel="stylesheet" href="<%=request.getContextPath()%>/resources/css/style.css">
		<link href="https://fonts.googleapis.com/css2?family=Lato&display=swap" rel="stylesheet">
		<script>var ctx = "${pageContext.request.contextPath}"</script>
	</head>
	<body>
		<div class="wrapper">
			<header>
				<h5 class="pl-3"><Strong>Pooled Testing Classifier</Strong></h5>
			</header>
			<nav aria-label="breadcrumb">
			  <ol class="breadcrumb mb-0 pl-4">
			    <li class="breadcrumb-item"><a href="<%=request.getContextPath()%>/pooled-testing-classifier/showTestCaseGroups">Home</a></li>
			    <li class="breadcrumb-item active" aria-current="page">Data Collection</li>
			  </ol>
			</nav>
			<form action="<%=request.getContextPath()%>/covid/downloadAssessmentTemplate" method="get" class="none" id="downloadAssessmentTemplate">
			</form>
			<input type="hidden" name="groupName" value="${groupName}" id="groupName"></input>
			<section class="main-assess-data">
				<button class="btn btn-sm btn-dark templateButton" onclick="downloadTemplate()">Download Template</button>
				<h6 class="main-head"><strong>Data Collection Form</strong></h6>
				<ul class="nav nav-tabs m-4" id="nav-assess">
				  <li class="nav-item">
				    <a class="nav-link active" href="#indvData">Individual Data</a>
				  </li>
				  <li class="nav-item">
				    <a class="nav-link" href="#bulkUpload">Bulk Upload</a>
				  </li>
				 </ul>
				<div class="mt-2 main-section nav-div" id="indvData">
					<h6 class="font-weight-bold">Personal Details</h6>
					<div class="form-row mt-3">
						<div class="col-3 form-group mb-0">
							<label for="age">Sex</label>
						</div>
					</div>
					<div class="form-row" id="gen-sec">
						<div class="col-6 form-group">
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="male" name="gender" data-value="Male" class="custom-control-input" checked>
							  <label class="custom-control-label" for="male">Male</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="female" name="gender" data-value="Gender" class="custom-control-input">
							  <label class="custom-control-label" for="female">Female</label>
							</div>
						</div>
					</div>
					<div class="form-row mt-2">
						
						<div class="col-3 form-group">
							<label for="age">Enter Age (Yrs)</label>
							<input type="text" class="numeric form-control form-control-sm" placeholder="Enter age" id="age" name="age">
						</div>
						<div class="col-3 form-group">
							<label for="age">Enter Height (in cms)</label>
							<input type="text" class="numeric form-control form-control-sm" placeholder="Enter height" id="height" name="height">
						</div>
						<div class="col-3 form-group">
							<label for="age">Enter Weight (in lbs)</label>
							<input type="text" class="numeric form-control form-control-sm" placeholder="Enter weight" id="weight" name="weight">
						</div>
					</div>
					<br>
					<h6 class="font-weight-bold">Existing Medical Conditions (Check all that apply)</h6>
					<div class="form-row mt-3" id="exPreConds">
						<div class="col-12 form-group">
							<div class="custom-control custom-checkbox custom-control-inline">
							  <input type="checkbox" id="Hypertension" data-value="Hypertension" name="disease" class="custom-control-input" checked>
							  <label class="custom-control-label" for="Hypertension">Hypertension</label>
							</div>
							<div class="custom-control custom-checkbox custom-control-inline">
							  <input type="checkbox" id="Diabetes" name="disease" data-value="Diabetes" class="custom-control-input">
							  <label class="custom-control-label" for="Diabetes">Diabetes</label>
							</div>
							<div class="custom-control custom-checkbox custom-control-inline">
							  <input type="checkbox" id="COPD" name="disease" data-value="COPD" class="custom-control-input">
							  <label class="custom-control-label" for="COPD">COPD</label>
							</div>
							<div class="custom-control custom-checkbox custom-control-inline">
							  <input type="checkbox" id="CKD" name="disease" data-value="CKD" class="custom-control-input">
							  <label class="custom-control-label" for="CKD">CKD</label>
							</div>
							<div class="custom-control custom-checkbox custom-control-inline">
							  <input type="checkbox" id="CVD" name="disease" data-value="CVD" class="custom-control-input">
							  <label class="custom-control-label" for="CVD">CVD</label>
							</div>
							<div class="custom-control custom-checkbox custom-control-inline">
							  <input type="checkbox" id="Asthma" name="disease" data-value="Asthma" class="custom-control-input">
							  <label class="custom-control-label" for="Asthma">Asthma</label>
							</div>
						</div>
					</div>
					<br>
					<h6 class="font-weight-bold">Travel History</h6>
					<p>Has anyone in the household had inter-state or international travel in the last 30 days.?</p> 
					<div class="form-row mt-3" id="travel-sec">
						<div class="col-6 form-group">
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="yes" name="travel" data-value="Yes" class="custom-control-input" checked>
							  <label class="custom-control-label" for="yes">Yes</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="no" name="travel" data-value="No" class="custom-control-input">
							  <label class="custom-control-label" for="no">No</label>
							</div>
						</div>
					</div>
					<br>
					<h6 class="font-weight-bold">Residence Type</h6>
						<div class="form-row mt-3" id="res-sec">
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="indvHome" name="residence" data-value="Individual home" class="custom-control-input" checked>
							  <label class="custom-control-label" for="indvHome">Individual home</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="houseMoreThan10" name="residence" data-value="Housing in structures with less than 10 units" class="custom-control-input">
							  <label class="custom-control-label" for="houseMoreThan10">Housing in structures with less than 10 units</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="houseLessThan10" name="residence" data-value="Housing in structures with more than 10 units" class="custom-control-input">
							  <label class="custom-control-label" for="houseLessThan10">Housing in structures with more than 10 units</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="CommHome" name="residence" data-value="Community Homes" class="custom-control-input">
							  <label class="custom-control-label" for="CommHome">Community Homes</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
							  <input type="radio" id="mobHome" name="residence" data-value="Mobile Home" class="custom-control-input">
							  <label class="custom-control-label" for="mobHome">Mobile Home</label>
							</div>
						</div>
						<br>
					<h6 class="font-weight-bold">Location Details</h6>
						<div class="form-row mt-3">
							<div class="col-5">
								<label for="name">Zipcode</label>
							</div>
						</div>
						<div class="form-row mt-3">
							<div class="col-1">
								<input type="text" class="form-control form-control-sm" placeholder="XXXXX" id="pincode1" name="zipcode1">	
							</div>
							<span>-</span>
							<div class="col-1">
								<input type="text" class="form-control form-control-sm" placeholder="XXXX" id="pincode2" name="zipcode2">	
							</div>
						</div>
						<br>
						<button type="button" id="submitAssesment" onclick="submitAssessment()" class="btn btn-success btn-sm mt-4">Save</button><span class="pl-3 message font-weight-bold"></span>
					</div>
				<div class="mt-2 main-section nav-div" id="bulkUpload" style="display:none">
					<h6 class="font-weight-bold">Upload File</h6>
					<div class="row p-0">
						<div class="col-5">
							<div class="custom-file mt-2">
							  <input type="text" class="form-control form-control-sm" placeholder="Group ID" id="groupNameInput" value="${groupName}">
							</div>
						</div>
					</div>
					<div class="row p-1">
					</div>
					<div class="row p-0">
						<div class="col-5">
							<div class="custom-file mt-2">
							  <input type="file" class="custom-file-input" id="customFileAtGroup" accept="*.xlsx">
							  <label class="custom-file-label" for="customFile">Choose file</label>
							</div>
						</div>
					</div>
				</div>
				<form action="<%=request.getContextPath()%>/pooled-testing-classifier/runAlgo" method="POST" id="runPoolAlgo">
					<input type="hidden" value="Group1" name="groupName" id="groupNameForAlgo"></input>
				</form>
				<form action="<%=request.getContextPath()%>/pooled-testing-classifier/runAlgoAtGroup" method="POST" id="runPoolAlgoAtGroup">
					<input type="hidden" value="Group1" name="groupName" id="groupNameForAlgoAtGroup"></input>
				</form>
				<form action="<%=request.getContextPath()%>/pooled-testing-classifier/addData" method="get" id="addNewDataForm">
					<input type="hidden" name="groupName" id="groupNameForAdd"></input>
				</form>
				<div class="modal" id="saveAssessmentModal" tabindex="-1" role="dialog" aria-labelledby="workflow">
					<div class="modal-dialog modal-dialog-centered" role="document">
						<div class="modal-content" id="abId0.09644027224709917">
							<div class="modal-body" id="abId0.8537891757600151">
								<div class="form-group" id="abId0.12393818980578186">
									Successfully Saved Data
								</div>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-info btn-sm" id="addDataButton">Add Data</button>
							</div>
						</div>
					</div>
				</div>
				<div class="modal" id="uplaodAssessmentTemplateModal" tabindex="-1" role="dialog" aria-labelledby="workflow">
					<div class="modal-dialog modal-dialog-centered" role="document">
						<div class="modal-content" id="abId0.09644027224709917">
							<div class="modal-body" id="abId0.8537891757600151">
								<div class="form-group" id="abId0.12393818980578186">
									Successfully Updated Data
								</div>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-info btn-sm" id="uploadDataButton">Run Pooling Classifier</button>
							</div>
						</div>
					</div>
				</div>
			</section>
		</div>
		
	</body>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
	<script src="<%=request.getContextPath()%>/resources/js/app.js"></script>
	<script>
	$(function(){
		$('#saveAssessmentModal').modal('hide');
	});
	</script>
</html>
<!--
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
-->