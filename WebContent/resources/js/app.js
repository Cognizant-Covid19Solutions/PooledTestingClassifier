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

$("#customFile").change(function(){
	var fileName = $(this).val().split('\\').pop();
	var oForm = new FormData();
	for(var i=0;i<$("#customFile")[0].files.length;i++){
		if($("#customFile")[0].files[i].type == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"){
			oForm.append("files", $("#customFile")[0].files[i]);
		}		
		alert($("#customFile")[0].files[i].type);
	}
	var fileName = $("#customFile").val();
    uploadFile(fileName,oForm);
});
$("#customFileAtGroup").change(function(){
	var fileName = $(this).val().split('\\').pop();
	var oForm = new FormData();
	for(var i=0;i<$("#customFileAtGroup")[0].files.length;i++){
		if($("#customFileAtGroup")[0].files[i].type == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"){
			oForm.append("files", $("#customFileAtGroup")[0].files[i]);
		}
	}
	var fileName = $("#customFileAtGroup").val();
    uploadFileAtGroup(fileName,oForm);
});
function uploadFileAtGroup(fileName,oForm){
	var groupName = $('#groupNameInput').val();
	console.log('Uploading file');	
	$.ajax({
	    url: ctx+'/pooled-testing-classifier/bulkUploadAtGroup?groupName='+groupName,
	    type: 'POST',
	    data: oForm,
	    processData: false,
	    contentType: false,
	    cache: false,
	    timeout: 600000,
	    success:function(data){
	    	$("#groupNameForAlgoAtGroup").val(groupName);
    		$('#runPoolAlgoAtGroup').submit();
	    }
	});
}
$(function(){
	getTestCaseGroups();
	$('#nav-assess').find('li').click(function(e){
		e.stopPropagation();
		e.preventDefault();
		$('.nav-div').hide();
		$('.active').removeClass('active');
		$(this).find('a').addClass('active');
		let id = $(this).find('a').attr('href');
		$(id).show();
	});
});
$('#addDataButton').click(function(){
	$('#groupNameForAdd').val($('#groupNameInput').val());
	$('#addNewDataForm').submit();
});
$('#uploadDataButton').click(function(){
	$('#groupNameForAlgo').val($('#groupNameInput').val());
	$('#runPoolAlgo').submit();
});

function createNewTestGroup(){
	var groupName = $('#groupNameForCreate').val();
	$.ajax({
	    url: ctx+'/pooled-testing-classifier/createGroup',
	    type: 'POST',
	    data:{
	    	"groupName":groupName,
	    },
	    success:function(data){
	    	if(data=='success'){
	    		window.location.reload(true);
	    	}
	    }
	});
}

function uploadFile(fileName,oForm){
	var groupName = $('#groupNameInput').val();
	console.log('Uploading file');	
	$.ajax({
	    url: ctx+'/pooled-testing-classifier/bulkUpload?groupName='+groupName,
	    type: 'POST',
	    data: oForm,
	    processData: false,
	    contentType: false,
	    cache: false,
	    timeout: 600000,
	    success:function(data){
    		$("#groupNameForAlgo").val(data);
    		$('#runPoolAlgo').submit();
	    }
	});
}
function getTestCaseGroups(){
	$.ajax({
		url : ctx +'/pooled-testing-classifier/getTestCaseGroups',
		type : 'get',
		success:function(data){
			updateTestGroups(data);
		}
	});
}

function downloadTemplate(){
	$('#downloadAssessmentTemplate').submit();
}

function exportPoolDataOutput(){
	$('#exportPoolDataOutput').submit();
}

function exportPoolData(){
	$('#exportPoolData').submit();
}

function deleteGroup(groupName){
	$.ajax({
		url : ctx +'/covid/deleteTestCaseGroups',
		type : 'post',
		data : {
			"groupName":groupName
		},
		success:function(data){
		}
	});
}

function updateTestGroups(data){
	$('#test-groups').empty();
	let obj = JSON.parse(data);
	console.log(obj);	
	let div = '';
	
	for(i = 0; i < obj.length; i++){
		let groupName = obj[i].name;
		div =
			`<div class="col-3">
				<div class="card bg-light mb-3 pointer group-info">
				  <div class="card-header btn-custom-dark">
			  	  	${obj[i].name}
			  	  	<span class="badge badge-warning rec-count count${i}">${obj[i].recordCount}</span>
			  	  </div>
				  <div class="card-body">
				    <div class="button-div" data-group-name="${obj[i].name}">
			    		<button class="btn btn-sm btn-outline-custom-dark" id="addRecordForGroup${i}">Add Data</button>
			    		<button class="btn btn-sm btn-outline-custom-dark ml-2" id="viewGroup${i}">View Data</button>
				    </div>
				  </div>
				</div>
			</div>`;
		$('#test-groups').append(div);
		$('#addRecordForGroup'+i).click(function(){
			$('#groupNameForAdd').val(groupName);
			$('#addNewDataForm').submit();
		});
		$('#viewGroup'+i).click(function(){
			let count = $(this).parent().parent().parent().find('div.card-header').contents().get(0).nodeValue
			$('#groupName').val(count);
			$('#viewData').submit();
		});
	}
}

let recordsDataTable = '', outputDataTable;
/*$('#saveAssessmentModal').modal({
    backdrop: 'static',
    keyboard: false
})*/

function addGroup(){
	$('#groupNameForAddGroup').val($('#groupNameInput').val())
	$('#addNewGroupForm').submit();
}

function submitAssessment(){
	 var groupName = $("#groupName").val();
	 var gender = $("input[name='gender']:checked").attr('data-value');
	 var age = $('#age').val();
	 var height = $('#height').val();
	 var weight = $('#weight').val();
	 var medicalConditions = $("input[name='disease']:checked").attr('data-value');
	 if(medicalConditions==null){
		 medicalConditions = "";
	 }
	 var travelHistory = $("input[name='travel']:checked").attr('data-value');
	 var household = $("input[name='residence']:checked").attr('data-value');
	 var addr1 = $('#addr1').val();
	 var addr2 = $('#addr2').val();
	 var city = $('#city').val();
	 var state = $('#state').val();
	 var address = addr1+','+addr2+','+city+','+state;
	 var zipcode1 = $('#pincode1').val();
	 var zipcode2 = $('#pincode2').val();
	 var zipcode = '';
	 if(zipcode2==''){
		 zipcode = zipcode1;
	 }else{
		 zipcode = zipcode1+'-'+zipcode2;
	 }
	 var location = $("input[name='location']:checked").attr('id');
	 $.ajax({
		    url: ctx+'/pooled-testing-classifier/saveAssessmentData',
		    type: 'POST',
		    data: {
		    	"groupName":groupName,
		    	"age":age,
		    	"gender":gender,
		    	"zipcode":zipcode,
		    	"height":height,
		    	"weight":weight,
		    	"household":household,
		    	"medicalConditions":medicalConditions,
		    	"travelHistory":travelHistory,
		    },
		    cache: false,
		    success:function(data){
		    	if(data="success"){
		    		$('#saveAssessmentModal').modal('show');
		    	}else{
		    		alter("FAILED");
		    	}
		    }
		});
}

function getDataResult(){
	let highRiskCount = 0, lowRiskCount = 0, mediumRiskCount = 0;
	outputDataTable = $('#outputDataTable').DataTable({
    	"lengthChange": false,
    	"ordering": true, 
    	"pageLength": 12,
    	"bDestroy": true,
    	"dom": 'lrtip',
    	"bScrollCollapse": true,
    	"ajax" : {
    			url : ctx + '/pooled-testing-classifier/getDataResult',
    			dataSrc:""
    	},
    	"columns": [
    		{ "data": 'id' , class:'text-center w-80'},
            { "data": 'gender', class:'text-center w-80' },
            { "data": 'zipcode', class:'text-center w-80' },
            { "data": 'household', class:'text-center w-80' },
            { "data": 'medicalConditions', class:'text-center w-80' },
            { "data": 'travelHistory', class:'text-center w-80' },
            { "data": 'score', class:'text-center w-80 font-weight-bold' },
            { "data": 'risk', class:'text-center w-80 font-weight-bold' , 
            	'createdCell':  function (td, cellData, rowData, row, col) {
            		if(cellData == 'High')
            			highRiskCount++;
            		if(cellData == 'Low')
            			lowRiskCount++;
            		if(cellData == 'Medium')
            			mediumRiskCount++;
            	} 
            },
            { "data": 'poolNo', class:'text-center w-80 font-weight-bold' },
        ],
    	
    	drawCallback: function() {
    	      const api = this.api();
    	      let rowCount = api.rows({page: 'current'}).count();
    	      
    	      for (let i = 0; i < api.page.len() - (rowCount === 0? 1 : rowCount); i++) {
    	        $('#outputDataTableBody tbody').append($("<tr ><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td><td></td></tr>"));
    	      }
    	    }
    });
	
}

function getCurrentData(){
	var groupName = $('#groupName').val();
	recordsDataTable = $('#recordsDataTable').DataTable({
    	"lengthChange": false,
    	"ordering": true, 
    	"pageLength": 12,
    	"bDestroy": true,
    	"dom": 'lrtip',
    	"bScrollCollapse": true,
    	"ajax" : {
    			url : ctx + '/pooled-testing-classifier/getDataFromDatabase?groupName='+groupName,
    			dataSrc:""
    	},
    	"columns": [
    		{ "data": 'id',class:'text-center w-80'},
    		{ "data": 'age',class:'text-center w-80'},
    		{ "data": 'height',class:'text-center w-80'},
    		{ "data": 'weight',class:'text-center w-80'},
            { "data": 'gender', class:'text-center w-80' },
            { "data": 'zipcode', class:'text-center w-80' },
            { "data": 'household', class:'text-center w-80' },
            { "data": 'medicalConditions', class:'text-center w-80' },
            { "data": 'travelHistory', class:'text-center w-80' },
        ],
    	
    	drawCallback: function() {
    	      const api = this.api();
    	      let rowCount = api.rows({page: 'current'}).count();
    	      
    	      for (let i = 0; i < api.page.len() - (rowCount === 0? 1 : rowCount); i++) {
    	        $('#groupRecordsBody tbody').append($("<tr ><td>&nbsp;</td><td></td><td></td><td></td><td></td></tr>"));
    	      }
    	    }
    });
}
function getResultGraphicalData(){
	var groupName = $('#groupName').val();
	$.ajax({
		url:ctx+'/pooled-testing-classifier/getGraphicalDetailData?groupName='+groupName,
		type:'get',
		success:function(data){
			updateGraphicalDetailData(groupName,data);
		}
	});
}

function updateGraphicalDetailData(groupName,data){
	let obj = JSON.parse(data);
	console.log(obj);
	let lowRiskPoolCount = obj.low_risk.pool_numbers.split(',').length;
	let mediumRiskPoolCount = obj.medium_risk.pool_numbers.split(',').length;
	let highRiskPoolCount = obj.high_risk.pool_numbers.split(',').length;
	let lowRiskDataArray = [], mediumRiskDataArray = [], highRiskDataArray = [];
	for(let data of obj.low_risk.pool_numbers.split(',')){
		let lowRiskPoolData = {};
		lowRiskPoolData.label = data.split("(")[0]+"\n ("+data.split("(")[1];
		lowRiskPoolData.color = '#2ecc71';
		lowRiskPoolData.value = 11.1;
		lowRiskPoolData.link = 'JavaScript:updateRiskDataTable("Low",\'' + data + '\')',
		lowRiskDataArray.push(lowRiskPoolData);
	}
	for(let data of obj.medium_risk.pool_numbers.split(',')){
		let mediumRiskPoolData = {};
		mediumRiskPoolData.label = data.split("(")[0]+"\n ("+data.split("(")[1];
		mediumRiskPoolData.color = '#e67e22';
		mediumRiskPoolData.value = 11.1;
		mediumRiskPoolData.link = 'JavaScript:updateRiskDataTable("Medium",\'' + data + '\')',
		mediumRiskDataArray.push(mediumRiskPoolData);
	}
	for(let data of obj.high_risk.pool_numbers.split(',')){
		let highRiskPoolData = {};
		highRiskPoolData.label = data.split("(")[0]+"\n ("+data.split("(")[1];
		highRiskPoolData.color = '#c0392b';
		highRiskPoolData.value = 11.1;
		highRiskPoolData.link = 'JavaScript:updateRiskDataTable("High",\'' + data + '\')',
		highRiskDataArray.push(highRiskPoolData);
	}
	const dataSource = {
			  chart: {
			    caption: "Pool Testing Classification - "+groupName,
			    captionFontSize: "15",
			    captionPadding:30,
			    chartRightMargin:80,
			    alignCaptionWithCanvas:"1",
			    showplotborder: "1",
			    showlegend:"1",
			    legendposition:"bottom",
			    baseFontSize: "15",
			    //plotfillalpha: "60",
			    hoverfillcolor: "#CCCCCC",
			    numberprefix: "$",
			    plottooltext:
			      "Click to get more details",
			    theme: "fusion",
			    innerradius:"0",
			    pieRadius:"320"
			  },
			  category: [
			    {
			      label: "Risk Level",
			      tooltext: "Please hover over a sub-category to see details",
			      color: "#ffffff",
			      link : "JavaScript:updateRiskDataTable(null,null)",
			      //value: lowRiskPoolCount,
			      category: [
			        {
			          label: "Low Risk \n ("+obj.low_risk.individual_count+")",
			          color: "#2ecc71",
			          value: lowRiskPoolCount,
			          link : "JavaScript:updateRiskDataTable('Low',null)",
			          category: lowRiskDataArray
			        },
			        {
			          label: "Medium Risk \n ("+obj.medium_risk.individual_count+")",
			          color: "#e67e22",
			          value: mediumRiskPoolCount,
			          link : "JavaScript:updateRiskDataTable('Medium',null)",
			          category: mediumRiskDataArray
			        },
			        {
			          label: "High Risk \n ("+obj.high_risk.individual_count+")",
			          color: "#c0392b",
			          value: highRiskPoolCount,
			          link : "JavaScript:updateRiskDataTable('High',null)",
			          category: highRiskDataArray
			        }
			      ]
			    }
			  ]
			};

			FusionCharts.ready(function() {
			  var myChart = new FusionCharts({
			    type: "multilevelpie",
			    renderAt: "chart-container",
			    width: "100%",
			    height: "100%",
			    dataFormat: "json",
			    dataSource
			  }).render();
			});
			updateRiskDataTable('High',null);

	let low_risk_detail = ``;
	low_risk_detail+=`<div class="row center-align">
							<span>Low Risk</span>
						</div>
					 	<div class="row">
							<div class="col-7" style="padding:0;">
								<span>Pool Numbers</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.low_risk.pool_numbers}</span>
							</div>
						</div>
						<div class="row">
							<div class="col-7" style="padding:0;">
								<span>Score Range</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.low_risk.score_range}</span>
							</div>
						</div>
						<div class="row">
							<div class="col-7" style="padding:0;">
								<span># Individuals</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.low_risk.individual_count}</span>
							</div>
						</div>`;
	$('#low-risk-detail').empty().append(low_risk_detail);
	let medium_risk_detail = ``;
	medium_risk_detail+=`<div class="row center-align">
							<span>Medium Risk</span>
						</div>
					 	<div class="row">
							<div class="col-7" style="padding:0;">
								<span>Pool Numbers</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.medium_risk.pool_numbers}</span>
							</div>
						</div>
						<div class="row">
							<div class="col-7" style="padding:0;">
								<span>Score Range</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.medium_risk.score_range}</span>
							</div>
						</div>
						<div class="row">
							<div class="col-7" style="padding:0;">
								<span># Individuals</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.medium_risk.individual_count}</span>
							</div>
						</div>`;
	$('#medium-risk-detail').empty().append(medium_risk_detail);
	let high_risk_detail = ``;
	high_risk_detail+=`<div class="row center-align">
							<span>High Risk</span>
						</div>
					 	<div class="row">
							<div class="col-7" style="padding:0;">
								<span>Pool Numbers</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.high_risk.pool_numbers}</span>
							</div>
						</div>
						<div class="row">
							<div class="col-7" style="padding:0;">
								<span>Score Range</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.high_risk.score_range}</span>
							</div>
						</div>
						<div class="row">
							<div class="col-7" style="padding:0;">
								<span># Individuals</span>
							</div>
							<div class="col-5" style="text-align:right;">
								<span>${obj.high_risk.individual_count}</span>
							</div>
						</div>`;
	$('#high-risk-detail').empty().append(high_risk_detail);
}

function getRiskData(type,category){
	$.ajax({
		url : ctx + '/pooled-testing-classifier/getRiskData?type='+type+'&category='+category,
		type: 'get',
		success:function(data){
			//updateRiskData(data);
		}
	});
}

function updateRiskDataTable(risk,poolNumber){
	$('#riskType').val(risk);
	$('#poolNumber').val(poolNumber);
	let highRiskCount = 0, lowRiskCount = 0, mediumRiskCount = 0;
	var groupName = $('#groupName').val();
	outputDataTable = $('#outputRiskDetails').DataTable({
    	"lengthChange": false,
    	"ordering": true, 
    	"pageLength": 12,
    	"bDestroy": true,
    	"dom": 'lrtip',
    	"bScrollCollapse": true,
    	"ajax" : {
    			url : ctx + '/pooled-testing-classifier/getFilteredDataResult?risk='+risk+'&poolNumber='+poolNumber+'&groupName='+groupName,
    			dataSrc:""
    	},
    	"columns": [
    		{ "data": 'id' , class:'text-center w-80'},
    		{ "data": 'age' , class:'text-center w-80'},
    		{ "data": 'height' , class:'text-center w-80'},
    		{ "data": 'weight' , class:'text-center w-80'},
            { "data": 'gender', class:'text-center w-80' },
            { "data": 'zipcode', class:'text-center w-80' },
            { "data": 'medicalConditions', class:'text-center w-80' },
            { "data": 'travelHistory', class:'text-center w-80' },
            { "data": 'score', class:'text-center w-80 font-weight-bold' },
            { "data": 'risk', class:'text-center w-80 font-weight-bold' , 
            	'createdCell':  function (td, cellData, rowData, row, col) {
            		if(cellData == 'High')
            			highRiskCount++;
            		if(cellData == 'Low')
            			lowRiskCount++;
            		if(cellData == 'Medium')
            			mediumRiskCount++;
            	} 
            },
            { "data": 'poolNo', class:'text-center w-80 font-weight-bold' },
        ],
    	
    	drawCallback: function() {
    	      const api = this.api();
    	      let rowCount = api.rows({page: 'current'}).count();
    	      
    	      for (let i = 0; i < api.page.len() - (rowCount === 0? 1 : rowCount); i++) {
    	        $('#outputDataTableBody tbody').append($("<tr ><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>"));
    	      }
    	    }
    });
}

function updateRiskData(data){
	let fusioncharts = '';
	let obj = JSON.parse(data);
	console.log(obj);
	let houseHoldData = [];
	for(let i = 0; i < obj.houseHoldDetails.length; i++){
		let data = {
				"label" : obj.houseHoldDetails[i].household,
				"value" : obj.houseHoldDetails[i].householdCount
		}
		houseHoldData.push(data);
	}
	let noMedicalIssue = 0, oneMedicalIssue, comarbidity = 0;
	for(let i = 0; i < obj.medicalDetails.length; i++){
		
		if(obj.medicalDetails[i].medicalConditions == null)
			noMedicalIssue++;
		else if(obj.medicalDetails[i].medicalConditions.includes(','))
			comarbidity++;
		else 
			oneMedicalIssue++;
	}
	let	riskPoolData = 
			`
				<div class="pool-details pt-2">
					<h6><strong>${obj.risk}</strong></h6><hr>
					<div class="row p-0">
						<div class="col-3"><h6>Score Range</h6></div>
						<div class="col-3">${obj.scoreRange.min_score} to ${obj.scoreRange.max_score}</div>
						<div class="col-3"><h6>Individual Count</h6></div>
						<div class="col-3">${obj.scoreRange.indv_count}</div>
					</div>	
					<div class="row p-0 mt-2">
						
					</div>
					<div class="row p-0 mt-2">
						<div class="col-6" id="chartHousehold">
						
						</div>
						<div class="col-6" id="chartMedical">
						
						</div>
					</div>
				</div>
			`
	$('#riskPoolDetails').empty().append(riskPoolData);
	
	let dataSource = {
		    type: 'doughnut2d',
		    renderAt: 'chartHousehold',
		    width: '100%',
		    height: '300',
		    dataFormat: 'json',
		    dataSource: {
		      "chart": {
		    	"plottooltext": "<b>$percentValue</b> of people are on $label",
			    "showlegend": "1",
			    "showpercentvalues": "1",
			    "legendposition": "bottom",
		        "theme": "fusion",
		        "caption": "Housing Distribution for "+obj.risk,
		      },
		      data: houseHoldData
		    }
	}
	
	let dataSourceMedical = {
		    type: 'doughnut2d',
		    renderAt: 'chartMedical',
		    width: '100%',
		    height: '300',
		    dataFormat: 'json',
		    dataSource: {
		      "chart": {
		    	"plottooltext": "<b>$percentValue</b> of people have $label",
			    "showlegend": "1",
			    "showpercentvalues": "1",
			    "legendposition": "bottom",
		        "theme": "fusion",
		        "caption": "Comorbidity Distribution for "+obj.risk,
		      },
		      data: [
				    {
				      label: "No Comorbidity",
				      value: noMedicalIssue
				    },
				    {
				      label: "Comorbidity",
				      value: comarbidity
				    },
				  ]
		    }
	}
	
			
	fusioncharts = new FusionCharts(dataSource);
    fusioncharts.render();
    fusioncharts = new FusionCharts(dataSourceMedical);
    fusioncharts.render();

}

function graphDataInGroup(){
	$.ajax({
		url : ctx + '/pooled-testing-classifier/getGraphDataForGroup',
		type: 'get',
		success:function(data){
			updateGraphDataForGroup(data);
		}
	});
}

function updateGraphDataForGroup(data){
	let fusioncharts = '';
	let maleGender = 0, femaleGender = 0, hasTraveled =0, notTraveled = 0, indHome = 0, commLiving = 0, housingGrtTen = 0, housingLessTen = 0, mobHome = 0; 
	let age_0_10=0,age_10_20=0,age_20_40=0,age_40_60=0,age_60=0;
	let obj = JSON.parse(data);
	for(let i = 0; i < obj.length; i++){
		if(obj[i].gender == 'Male')
			maleGender++;
		else
			femaleGender++;
		if(obj[i].travel == 'Yes')
			hasTraveled++;
		else
			notTraveled++;
		if(obj[i].household == 'Individual Home')
			indHome++;
		if(obj[i].household == 'Community living')
			commLiving++;
		if(obj[i].household == 'Housing in structures with less than 10 units')
			housingLessTen++;
		if(obj[i].household == 'Housing in structures with more than 10 units')
			housingGrtTen++;
		if(obj[i].household == 'Mobile home')
			mobHome++;
		if(obj[i].age=='0-10'){
			age_0_10++;
		}
		if(obj[i].age=='10-20'){
			age_10_20++;
		}
		if(obj[i].age=='20-40'){
			age_20_40++;
		}
		if(obj[i].age=='40-60'){
			age_40_60++;
		}
		if(obj[i].age=='>60'){
			age_60++;
		}
	}
	/*let dataSource = {
		    type: 'pie2d',
		    renderAt: 'chart-gender-container',
		    width: '100%',
		    height: '250',
		    dataFormat: 'json',
		    dataSource: {
		      "chart": {
		    	"plottooltext": "<b>$percentValue</b> of people are on $label",
			    "showlegend": "1",
			    "showpercentvalues": "1",
			    "legendposition": "bottom",
		        "theme": "fusion",
		        "caption": "Timeline of Deaths",
		      },
		      data: [
				    {
				      label: "Males",
				      value: maleGender
				    },
				    {
				      label: "Females",
				      value: femaleGender
				    },
				  ]
		    }
	}*/
	const dataSourceTravel = {
		    type: 'pie2d',
		    renderAt: 'chart-travel-container',
		    width: '100%',
		    height: '250',
		    dataFormat: 'json',
		    dataSource: {
		      "chart": {
		    	"plottooltext": "<b>$percentValue</b> of people are on $label",
			    "showlegend": "1",
			    "showpercentvalues": "1",
			    "legendposition": "bottom",
		        "theme": "fusion",
		        "caption": "Travel Ratio",
		      },
		      data: [
				    {
				      label: "Has traveled",
				      value: hasTraveled
				    },
				    {
				      label: "Have not traveled",
				      value: notTraveled
				    },
				  ]
		    }
	}

	fusioncharts = new FusionCharts(dataSourceTravel);
    fusioncharts.render();
    
    
    dataSource = {
    		chart: {
    		    caption: "Household Ratio",
    		    xaxisname: "Household Type",
    		    yaxisname: "Count",
    		    /*numbersuffix: "K",*/
    		    theme: "fusion"
    		  },
    		  data: [
    		    {
    		      label: "Individual Home",
    		      value: indHome
    		    },
    		    {
    		      label: "Community living",
    		      value: commLiving
    		    },
    		    {
    		      label: "Housing in structures with less than 10 units",
    		      value: housingLessTen
    		    },
    		    {
    		      label: "Housing in structures with more than 10 units",
    		      value: housingGrtTen
    		    },
    		    {
    		      label: "Mobile home",
    		      value: mobHome
    		    }
    		  ]
    		};

    		FusionCharts.ready(function() {
    		  var myChart = new FusionCharts({
    		    type: "column2d",
    		    renderAt: "chart-household-container",
    		    width: "100%",
    		    height: "100%",
    		    dataFormat: "json",
    		    dataSource
    		  }).render();
    		});
    		

		dataSource = {
				chart: {
    	    		    caption: "Household Ratio",
    	    		    xaxisname: "Household Type",
    	    		    yaxisname: "Count",
    	    		    /*numbersuffix: "K",*/
    	    		    theme: "fusion"
    	    		  },
    	    		  data: [
    	    		    {
    	    		      label: "Individual Home",
    	    		      value: indHome
    	    		    },
    	    		    {
    	    		      label: "Community living",
    	    		      value: commLiving
    	    		    },
    	    		    {
    	    		      label: "Housing in structures with less than 10 units",
    	    		      value: housingLessTen
    	    		    },
    	    		    {
    	    		      label: "Housing in structures with more than 10 units",
    	    		      value: housingGrtTen
    	    		    },
    	    		    {
    	    		      label: "Mobile home",
    	    		      value: mobHome
    	    		    }
    	    		  ]
    	    		};

    	    		FusionCharts.ready(function() {
    	    		  var myChart = new FusionCharts({
    	    		    type: "column2d",
    	    		    renderAt: "chart-household-container",
    	    		    width: "100%",
    	    		    height: "100%",
    	    		    dataFormat: "json",
    	    		    dataSource
    	    		  }).render();
    	    		});
	
}
/*
Authors: Siddhartha Yelavarthi
Copyright {2020} Cognizant Technology SolutionsLicensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License.
*/