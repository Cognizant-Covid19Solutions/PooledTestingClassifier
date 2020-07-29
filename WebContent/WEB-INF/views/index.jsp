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
		<link rel="stylesheet" href="<%=request.getContextPath()%>/resources/css/datatable.css" />
		<link rel="stylesheet" href="<%=request.getContextPath()%>/resources/css/style.css">
		<link href="https://fonts.googleapis.com/css2?family=Lato&display=swap" rel="stylesheet">
		<script>var ctx = "${pageContext.request.contextPath}"</script>
	</head>
	<body>
		<div class="wrapper">
			<form action="<%=request.getContextPath()%>/pooled-testing-classifier/runAlgo" method="post" id="runPoolAlgo">
				<input type="hidden" name="groupName" id="groupNameForAlgo" />
			</form>
			<header>
				<h5 class="pl-3"><Strong>Pooled Testing Classifier</Strong></h5>
			</header>
			<form action="<%=request.getContextPath()%>/pooled-testing-classifier/downloadAssessmentTemplate" method="get" class="none" id="downloadAssessmentTemplate">
			</form>
			<section class="main">
				<div class="row">
					<div class="col-1" style="margin-left:5%;">
					</div>
					<div class="col-4">
						<div class="card bg-light mb-3 pointer group-info">
						  	<div class="card-header btn-custom-dark">
						  		Create Blank Test Group
						  	</div>
						  	<div class="card-body">
						  		<div class="row p-0">
						  			<div class="col-12">
						  				<label>Group Name</label>
										<input type="text" class="form-control form-control-sm" id="groupNameForCreate" name="groupNameForCreate">	
						  			</div>
						  		</div>
						  		<div class="row p-0">
						  			<div class="col-12" style="text-align:right;">
										<button type="button" id="createNewTestGroup" onclick="createNewTestGroup()" class="btn btn-success btn-sm mt-4">Create</button>
						  			</div>
						  		</div>
						  	</div>
					  	</div>
					</div>
					<div class="col-1">
						<span style="position:absolute;top:40%;right:50%;"><strong>OR</strong></span>
					</div>
					<div class="col-4">
						<div class="card bg-light mb-3 pointer group-info">
						  	<div class="card-header btn-custom-dark">
						  		Upload File to Create New Test Group
						  	</div>
							<div class="card-body">
								<div class="mt-2" id="bulkUpload">
									<%-- <div class="row p-0">
										<div class="col-12">
											<div class="custom-file mt-2">
											  <input type="text" class="form-control form-control-sm" placeholder="Group Name" id="groupNameInput" value="${groupName}">
											</div>
										</div>
									</div> --%>
									<div class="row p-0">
										<div class="col-12">
											<div class="custom-file mt-2">
											  <input type="file" class="custom-file-input" id="customFile" accept="*.xlsx">
											  <label class="custom-file-label" for="customFile">Choose file</label>
											</div>
										</div>
									</div>
									<div class="row p-1">
									</div>
									<div class="row p-0" style="margin-top:1.4rem!important;">
										<div class="col-7"></div>
										<div class="col-5" style="text-align:right;">
											<button class="btn btn-sm btn-outline-custom-dark" onclick="downloadTemplate()">Download Template</button>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="col-1">
					</div>
				</div>
				<hr>
				<div class="row">
					<div class="col-10"><h6><strong>Test Groups</strong></h6></div>
				</div>
				<div class="row p-0 mt-4" id="test-groups">
				</div>
				<hr>
				<div class="row" style="margin-top:2rem!important;">
					<div class="col-2">
					</div>
					<div class="col-8">
						<div class="card bg-light mb-3 pointer group-info" style="height:100%;">
							<div class="card-body">
								<div class="row">
									<span style="width:100%;text-align:center;"><strong>Key Attributes considered for risk classification(High, Medium, Low) and test pool assignment:</strong></strong></span> 
								</div>
								<div class="row mt-4">
									<div class="col-3">
										<strong>Individual</strong>
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Age Group 
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;BMI
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Comorbidities
										<br> 
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Travel History
										<br> 
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Location 
									</div>
									<div class="col-9">
										<strong>Demographic / Community</strong>
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Number of Positive Cases
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Social Distancing Score 
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Socio-economic vulnerability- Poverty, Unemployment, Per-capita Income, Education, Health Insurance 
										<br> 
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Household Composition - Age (<17,>65), Disabilities 
										<br> 
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Minority Status and Language - Minority Population, Speak English "Less than well" 
										<br>
										<i class="fas fa-dot-circle"></i>&nbsp;&nbsp;&nbsp;Housing and Transportation - Multi-unit Structures, Mobile Homes, Group Quarters, No-Vehicles 
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</section>
		</div>
		<form action="<%=request.getContextPath()%>/pooled-testing-classifier/addData" method="get" id="addNewDataForm">
			<input type="hidden" name="groupName" id="groupNameForAdd"></input>
		</form>
		<form action="<%=request.getContextPath()%>/pooled-testing-classifier/addNewGroup" method="get" id="addNewGroupForm">
			<input type="hidden" name="groupName" id="groupNameForAddGroup"></input>
		</form>
		<form action="<%=request.getContextPath()%>/pooled-testing-classifier/viewTestCaseGroupsData" method="get" id="viewData">
			<input type="hidden" id="groupName" name="groupName"/>
		</form>
		<div class="modal" id="addGroupModal" tabindex="-1" role="dialog" aria-labelledby="workflow">
			<div class="modal-dialog modal-dialog-centered" role="document">
				<div class="modal-content" id="abId0.09644027224709917">
					<div class="modal-header">
				        <h5 class="modal-title">Add New Group</h5>
			        	<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				          	<span aria-hidden="true">&times;</span>
				        </button>
			      	</div>
					<div class="modal-body" id="abId0.8537891757600151">
						<div class="form-group" id="abId0.12393818980578186">
							 <input type="text" class="form-control form-control-sm" placeholder="Group Name" id="groupNameInput" name="groupName">
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-info btn-sm" id="uploadDataButton" onclick="addGroup()">Add Data</button>
					</div>
				</div>
			</div>
		</div>
	</body>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
	<script src="<%=request.getContextPath()%>/resources/js/datatable.js"></script>
	<script type="text/javascript" src="https://cdn.fusioncharts.com/fusioncharts/latest/fusioncharts.js"></script>
	<script type="text/javascript" src="https://cdn.fusioncharts.com/fusioncharts/latest/themes/fusioncharts.theme.fusion.js"></script>
	<script src="<%=request.getContextPath()%>/resources/js/app.js"></script>
	<script>
	$(function(){
		$('#runPoolAlgo').attr('method','post');
		$.ajax({
			url : ctx + '/pooled-testing-classifier/getMaxGroupName',
			type: 'get',
			success:function(data){
				$('#groupNameForCreate').val(data);
			}
		});
	})
	</script>
</html>
<!--
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
-->