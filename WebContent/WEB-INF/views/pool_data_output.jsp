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
				<h5 class="pl-3"><Strong>Test Group - ${groupName}</Strong></h5>
			</header>
			<form action="<%=request.getContextPath()%>/pooled-testing-classifier/exportPoolDataOutput" method="get" class="none" id="exportPoolDataOutput">
				<input type="hidden" name="groupName" value="${groupName}" id="groupName"></input>
				<input type="hidden" name="riskType" id="riskType"></input>
				<input type="hidden" name="poolNumber" id="poolNumber"></input>
			</form>
			<nav aria-label="breadcrumb">
			  <ol class="breadcrumb mb-0 pl-4">
			    <li class="breadcrumb-item"><a href="<%=request.getContextPath()%>/pooled-testing-classifier/showTestCaseGroups">Home</a></li>
			    <li class="breadcrumb-item"><a href="<%=request.getContextPath()%>/pooled-testing-classifier/viewTestCaseGroupsData?groupName=${groupName}">${groupName}</a></li>
			    <li class="breadcrumb-item active" aria-current="page">Results</li>
			  </ol>
			</nav>
			<section class="pool-data-wrapper">
				<div class="row mt-4 mb-4 text-right">
					<div class="col-12">
				 		<button class="btn btn-sm btn-dark" onclick="exportPoolDataOutput()">Export Data</button>
				 	</div>
			 	</div>
				 <div class="nav-pool" id="summary" style="display:none">
					<table class="table table-sm table-striped table-bordered mt-4" id="outputDataTable">
						<thead>
							<tr>
								<th>ID</th>
								<th>Gender</th>
								<th>Zipcode</th>
								<th>Household</th>
								<th>Medical Conditions</th>
								<th>Travel History</th>
								<th class="bg-warning text-dark">Score</th>
								<th class="bg-warning text-dark">Risk</th>
								<th class="bg-warning text-dark">Pool Number</th>
							</tr>
						</thead>
						<tbody id="outputDataTableBody"></tbody>
					</table>
				</div>
				<div class="nav-pool" id="chartDetails" >
					<div class="row no-gutters" style="height:695px;">
						<div class="col-7">
							<div id="chart-container"></div>
						</div>
						<div class="col-5">
							<div class="row">
								<table class="table table-sm table-striped table-bordered mt-4 w-100" id="outputRiskDetails">
									<thead>
										<tr>
											<th>ID</th>
											<th>Age</th>
											<th>Height</th>
											<th>Weight</th>
											<th>Gender</th>
											<th>Zipcode</th>
											<th>Medical Conditions</th>
											<th>Travel History</th>
											<th class="bg-warning text-dark">Score</th>
											<th class="bg-warning text-dark">Risk</th>
											<th class="bg-warning text-dark">Pool Number</th>
										</tr>
									</thead>
									<tbody id="outputRiskDetailsBody"></tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<div class="modal" id="showPoolingAlgoInfo" tabindex="-1" role="dialog" aria-labelledby="workflow">
					<div class="modal-dialog modal-dialog-centered" role="document">
						
						<div class="modal-content" id="abId0.09644027224709917">	
							<div class="container">
								<div class="row mt-4">
									<div class="col-12">
										To quantify the risk of an individual contracting COVID19, two categories of parameters are considered.
									</div>
								</div>
								<div class="row">
									<div class="col-12">
										<strong>Individual's parameters: </strong> Age, Gender, BMI, Medical Conditions, Travel History
									</div>
								</div>
								<div class="row">
									<div class="col-12">
										<strong>Community parameters: </strong> Social Distancing status, Household composition and Disablility, Minority Status and Language, Housing Type and Transportation
									</div>
								</div>
								<div class="row mb-4">
								</div>
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
	<script src="<%=request.getContextPath()%>/resources/js/datatable.js"></script>
	<script type="text/javascript" src="https://cdn.fusioncharts.com/fusioncharts/latest/fusioncharts.js"></script>
	<script type="text/javascript" src="https://cdn.fusioncharts.com/fusioncharts/latest/themes/fusioncharts.theme.fusion.js"></script>
	<script src="<%=request.getContextPath()%>/resources/js/app.js"></script>
	<script>
		$(function(){
			getResultGraphicalData();
			$('#nav-pool-data').find('li').click(function(e){
				e.stopPropagation();
				e.preventDefault();
				$('.nav-pool').hide();
				$('.active').removeClass('active');
				$(this).find('a').addClass('active');
				let id = $(this).find('a').attr('href');
				$(id).show();
				if(id == '#summary'){
					getDataResult();
				}
			});
		});
	</script>
</html>
<!--
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
-->