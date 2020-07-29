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
			<form action="<%=request.getContextPath()%>/pooled-testing-classifier/exportPoolData" method="get" class="none" id="exportPoolData">
				<input type="hidden" name="groupName" value="${groupName}" id="groupName"></input>
			</form>
			<nav aria-label="breadcrumb">
			  <ol class="breadcrumb mb-0 pl-4">
			    <li class="breadcrumb-item"><a href="<%=request.getContextPath()%>/pooled-testing-classifier/showTestCaseGroups">Home</a></li>
			    <li class="breadcrumb-item active" aria-current="page">${groupName}</li>
			  </ol>
			</nav>
			<section class="pool-data-wrapper">
				<div class="row">
					<div class="col-8">
						<ul class="nav nav-tabs m-0" id="nav-pool-data">
						  	<li class="nav-item">
						    	<a class="nav-link active" href="#summary">Details</a>
						  	</li>
					 	</ul>
					</div>
					<div class="col-4">
						<button class="btn btn-sm btn-dark templateButton" onclick="exportPoolData()">Export Data</button>
					</div>
				</div>
				
				 <div class="nav-pool" id="summary">
					<table class="table table-sm table-striped table-bordered mt-4" id="recordsDataTable">
						<thead>
							<tr>
								<th>ID</th>
								<th>Age</th>
								<th>Height</th>
								<th>Weight</th>
								<th>Gender</th>
								<th>Zipcode</th>
								<th>Household</th>
								<th>Medical Conditions</th>
								<th>Travel History</th>
							</tr>
						</thead>
						<tbody id="groupRecordsBody"></tbody>
					</table>
				</div>
				<div class="nav-pool" id="chartDetails" style="display:none">
					<div class="row no-gutters">
						<div class="col-6">
							<div id="chart-household-container">
								<img src="<%=request.getContextPath()%>/resources/images/spinner.gif">
							</div>
						</div>
						<div class="col-6">
							<div id="chart-travel-container">
								<img src="<%=request.getContextPath()%>/resources/images/spinner.gif">
							</div>
						</div>
						<%-- <div class="col-6">
							<div id="chart-age-container">
								<img src="<%=request.getContextPath()%>/resources/images/spinner.gif">
							</div>
						</div> --%>
					</div>
					<!-- <div class="row no-gutters">
						
						
					</div> -->
				</div>
				<form action="<%=request.getContextPath()%>/pooled-testing-classifier/runAlgoFromDb" method="get">
				<div class="text-right">
					<button class="btn btn-sm btn-dark">Run Pool Testing Classifier</button>
					<input type="hidden" name="groupName" value="${groupName}" />
				</div>
				</form>
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
			getCurrentData();
			$('#nav-pool-data').find('li').click(function(e){
				e.stopPropagation();
				e.preventDefault();
				$('.nav-pool').hide();
				$('.active').removeClass('active');
				$(this).find('a').addClass('active');
				let id = $(this).find('a').attr('href');
				$(id).show();
				if(id == '#chartDetails'){
					graphDataInGroup();
				}
			});
		});
	</script>
</html>
<!--
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
-->