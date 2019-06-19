$(document).ready(function() {

	//notify function
	function notify(from, align, icon, type, animIn, animOut, msg) {
		$.notify({
			icon: icon,
			title: '',
			message: msg,
			url: ''
		}, {
			element: 'body',
			type: type,
			allow_dismiss: false,
			placement: {
				from: from,
				align: align
			},
			offset: {
				x: 20,
				y: 20
			},
			spacing: 10,
			z_index: 1000000000,
			delay: 1500,
			timer: 750,
			url_target: '_blank',
			mouse_over: false,
			animate: {
				enter: animIn,
				exit: animOut
			},
			template: '<div data-notify="container" class="alert alert-dismissible alert-{0} alert--notify" role="alert">' +
				'<span data-notify="icon"></span> ' +
				'<span data-notify="title">{1}</span> ' +
				'<span data-notify="message">{2}</span>' +
				'<div class="progress" data-notify="progressbar">' +
				'<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
				'</div>' +
				'<a href="{3}" target="{4}" data-notify="url"></a>' +
				'<button type="button" aria-hidden="true" data-notify="dismiss" class="alert--notify__close">Close</button>' +
				'</div>'
		});
	};


	// filtered Patient list
	var filteredPatient = [];
    
    // Get Json data from csv_file table
	var patientTimelines = [];

	$.ajax({
		type: "GET",
		url: "/analysis/getPatientTimelines",
		async: false,
		success : function(text)
		{
			patientTimelines = text.data;
		}
	});


	// get list of worry patients
	var worryPatients = new Set();
	$.ajax({
		type: "GET",
		url: "/analysis/getWrongPatients",
		async: false,
		success : function(text)
		{
			for(r in text.data){
				worryPatients.add(text.data[r].pid);
			}
		}
	});


	// placeholder for structuredData
	var structuredData = [];

	/*
	**  get patient comment function
 	*/
	// placeholder patient comment
	var patientComments = new Map();
	$.ajax({
		type: "GET",
		url: "/apis/patient/getAllPatientsComments",
		async: false,
		success : function(text)
		{
			for(r in text.data){
				patientComments.set(text.data[r].id,text.data[r].comment);
			}
		}
	});


	var currentPatientInfo;
	function getPatientInfoByPid(patientId) {
		$.ajax({
			type: "GET",
			url: "/apis/patients/" + patientId,
			async: false,
			success : function(text)
			{
				currentPatientInfo = text;
			},error: function () {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
					'find patient information failed!');
			}
		});
	}

	var columns = [];

	// check file suffix
	function checkSuffix(filename) {
		var regex = RegExp('[ -_][0]*[1-9]+[0]*(noar|ar|NOAR|AR).csv','g');
		return regex.test(filename);
	}

	// fetching column names and description for filter dropdown
	function fetch_columns_data() {
		$.ajax({ 
			type: "GET",
			url: "/apis/patients/columns",
			async: false,
			success : function(text)
			{
				columns = text;
			}
		});
	}

	/*
	*  initialize the graph
	* */

	// placeholder for drawing graph
	var TimeLineChart = TimelinesChart();

	// set different colors to different type of files
	var myColorScale = d3.scaleOrdinal()
		.domain(["normal", "resolved","commented","problematic"])
		.range(["#98df8a", "#2ca02c", "#1f77b4"," #d62728"]);

	function getDataSubset(currentCategory,currentYear){
		var currenrData = [];
		for (i in structuredData) {
			var currentYearData = [];
			if (structuredData[i].group == currentYear || currentYear == "ALL"){
				for (j in structuredData[i].data) {
					var filesData = [];
					var currentPatientFiles = structuredData[i].data[j].data;
					for(k in currentPatientFiles){
						if(currentPatientFiles[k].val == currentCategory || currentCategory == "ALL"){
							filesData.push(currentPatientFiles[k])
						}
					}
					if(filesData.length != 0){
						currentYearData.push({
							'label': structuredData[i].data[j].label,
							'data' : filesData
						});
					}
				}
			}
			if(currentYearData.length != 0){
				currenrData.push({
					'group': structuredData[i].group,
					'data' :currentYearData
				});
			}
		}
		return currenrData;
	}
	
	// update file status in the graph
	function updateFileInGraph( year , pidLabel, updatedFile,action) {
		for (i in structuredData) {
			if(structuredData[i].group == year){
				for (j in structuredData[i].data) {
					if(structuredData[i].data[j].label == pidLabel){
						for( k in structuredData[i].data[j].data){
							if(structuredData[i].data[j].data[k].filename == updatedFile){
								if(action == 'deleted'){
									structuredData[i].data[j].data.splice(k,1);
								}else{
									structuredData[i].data[j].data[k].val = action;
								}
								break
							}
						}
						if(structuredData[i].data[j].data.length == 0){
							structuredData[i].data.splice(j,1);
						}
						break
					}
				}
			}
			break
		}
		TimeLineChart.data(getDataSubset(currentCategory,currentYear));
	}


	// describe current range of the data
	var currentCategory = "ALL";
	var currentYear = "ALL";

	// Graph setting
	function graph_setting(){

		$('#Timelinegraph-container').empty();

		TimeLineChart
			.width(document.getElementById("Timelinegraph-container").clientWidth  )
			.maxHeight(720)
			.maxLineHeight(16)
			.xTickFormat(function (n){ return +n })
			.timeFormat('%Q')
			.zColorScale(myColorScale)
			.onSegmentClick(function (s) {findPatientFiles('PUH-' + s.label.split('#')[0]);})
			.onLegendClick(function (s) {

				if(currentCategory == s){
					currentCategory = "ALL";
					graph_setting();
					TimeLineChart.data(getDataSubset(currentCategory,currentYear));
				}else{
					currentCategory = s;
					var tempData = getDataSubset(currentCategory,currentYear);
					if(tempData.length == 0){
						notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
							'No patient currently in that category!');
					}else{
						graph_setting();
						TimeLineChart.data(tempData);
					}
				}
			})
			.onLabelClick(function (s1,s2) {

				if(!(s2 === undefined)){
					findPatientFiles('PUH-' + s1.split('#')[0]);
				} else {
					graph_setting();
					currentYear = currentYear == "ALL" ? s1 : "ALL";
					TimeLineChart.data(getDataSubset(currentCategory,currentYear));
				}
			})
			(document.getElementById('Timelinegraph-container'));

		resetBTNSetting();
	}

	graph_setting();
	structuredData  = getStructuredData(patientTimelines);
	TimeLineChart.data(structuredData);


    /*
    **  end of drawing the graph
    */
	fetch_columns_data();

    /*
    **  fetching filtered data function
     */
	function filtered_data() {
		filteredPatient = [];

		var fields = $(".field option:selected" );
		var operater = $(".operator option:selected" );
		var value = $(".value" );
		var whereCondition = "WHERE ";

		// Generate Where condition
		for (i in fields) {
			if (fields[i].value && operater[i].value && value[i].value) {
				if (operater[i].value == 'LIKE') {
					whereCondition += fields[i].value + " LIKE " + "\'%25" + value[i].value + "%25\' and ";
				} else {
					whereCondition += fields[i].value + operater[i].value + "\'" + value[i].value + "\' and ";
				}
			}
		}

		if(whereCondition == "WHERE"){
			return
		}else{
			$.ajax({
				'type': "GET",
				'url': "/analysis/selecIdByfilter/" + whereCondition.slice(0,-4),
				'async': false,
				'success' : function(text)
				{
					filteredPatient = text;
				},'error': function () {
					notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
						'Fetching required patients failed.');
				}
			});
		}

		if (filteredPatient.length == 0) {
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
				'No patient satisfies your conditions.');
		}else{
			var temp = getStructuredData(patientTimelines);
			if(temp){
				structuredData = temp;
				TimeLineChart.data(structuredData);
			}
		}

	}

	var columnData = $.map(columns, function (obj) {
        obj.text = obj.text || obj.field; // replace name with the property used for the text
        obj.id = obj.id || obj.field;
        return obj;
    });

    
    $(".field").select2({
        width: '100%',
        data : columnData
    });

    $(".operator").select2({
        width: '100%'
    });

    var wrapper = $("#filterForm"); //Fields wrapper
    var add_button = $("#addFilter"); //Add button ID

    var x = 1; //initlal text box count
    $(add_button).click(function(e) { //on add input button click
        e.preventDefault();
        var html = '<div class="row"><div class="col-sm-3 col-md-3"><select class="init-select2 field" data-placeholder="Filter Field" id="field[]" required><option disabled="disabled" selected="selected" value="">Filter Field</option></select></div><div class="col-sm-2 col-md-2"><select class="init-select2 operator" data-placeholder="Filter Method" id="operator[]" required><option value="=">=</option><option value="!=">&ne;</option><option value=">">&gt;</option><option value=">=">&ge;</option><option value="<">&lt;</option><option value="<=">&le;</option></select></div><div class="col-sm-2 col-md-2"><div class="input-group mb-3"><input type="text" class="form-control value" id="value[]" placeholder="Input value" required></div></div><div class="col-sm-1 col-md-1" style="margin-top:6px"><a href="#" class="remove_field btn btn-sm btn-outline-danger">remove</a></div></div>';
        $(wrapper).append(html);

        $(".field").select2({
            width: '100%',
            data : columnData
        });

        $(".operator").select2({
            width: '100%'
        });
    });

    $(wrapper).on("click", ".remove_field", function(e) { //user click on remove text
        e.preventDefault();
        $(this).parent('div').parent('div').remove();
    });

    // query button
    $("#queryFilter").click(function() {
    	filtered_data();
    });

    // overview button
	$("#overview").click(function() {
		filteredPatient = [];
		currentYear = "ALL";
		currentCategory  = "ALL";
		structuredData = getStructuredData(patientTimelines);
		TimeLineChart.data(structuredData);
	});


	/*
	*  assign data for the graph
	* */
	function getStructuredData(response) {

		var data = new Map();

		for (r in response)
		{
			// filter
			if(filteredPatient.length != 0 && filteredPatient.indexOf(response[r].pid) == -1){continue;}

			// too many patients problem
			// if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 ){
			if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 || response[r].relativeStartTime > 1296000){
				continue;
			}

			// find out if the patient is problematic / mislabeled or not
			// also ignore those resolved files
			var fileType = "normal";
			if(worryPatients.has(response[r].pid)  && response[r].resolved == false){fileType = "problematic"};
			if(response[r].comment != null || patientComments.has(response[r].pid) ){fileType = "commented"};
			if(response[r].resolved == true){fileType = "resolved"};


			// store all the information in nested Hash table
			if ( data.has(response[r].filename.split('.')[0].split('-')[1]) ){
				// left label
				var year = 	data.get(response[r].pid.split('-')[1]);
				// right label
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;

				// get time for this patient/file
				var arrestTime = new Date(response[r].arrestTime);
				var startTime = new Date(arrestTime.getTime() + response[r].relativeStartTime*1000);
				var endTime = new Date(arrestTime.getTime() + response[r].relativeEndTime*1000);

				// tooltip
				var tooltip = response[r].filename + '<br>'+ 'arrest Time:' + '<br>' + arrestTime.toLocaleString() + '<br>' +
					'Start Time:'+ '<br>' + startTime.toLocaleString() + '<br>' +
					'End Time:'+ '<br>' + endTime.toLocaleString();

				if( year.has(labelname) ){
					var patient = year.get(labelname);

					patient.push(
						{
							timeRange: [
								Math.round(response[r].relativeStartTime / 3600),
								Math.round(response[r].relativeEndTime / 3600)
							],
							val: fileType,
							filename:response[r].filename,
							labelVal:tooltip
						}
					);
					year.set(labelname,patient)
				}else{
					year.set(labelname,[{
						timeRange: [
							Math.round(response[r].relativeStartTime / 3600),
							Math.round(response[r].relativeEndTime / 3600)
						],
						val: fileType,
						filename:response[r].filename,
						labelVal:tooltip
					}
					])
				}
			}else{
				var firstbar = new Map();
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;

				// get time for this patient/file
				var arrestTime = new Date(response[r].arrestTime);
				var startTime = new Date(arrestTime.getTime() + response[r].relativeStartTime*1000);
				var endTime = new Date(arrestTime.getTime() + response[r].relativeEndTime*1000);

				// tooltip
				var tooltip = response[r].filename + '<br>'+ 'arrest Time:' + '<br>' + arrestTime.toLocaleString() + '<br>' +
					'Start Time:'+ '<br>' + startTime.toLocaleString() + '<br>' +
					'End Time:'+ '<br>' + endTime.toLocaleString();

				firstbar.set(labelname,[{
					timeRange: [
						Math.round(response[r].relativeStartTime / 3600),
						Math.round(response[r].relativeEndTime / 3600)
					],
					val:  fileType,
					filename:response[r].filename,
					labelVal:tooltip
				}
				]);
				data.set(response[r].filename.split('.')[0].split('-')[1], firstbar)
			}
		}

		// see if there are files Satisfy filter condition and relative end time constrain
		if(data.size != 0){

			var chartdata = [];

			// convert nested hash into data needed for graph
			data.forEach(function (patientList,year) {
				var bardata = [];
				patientList.forEach(function (val,label) {

					bardata.push({
						'label':label,
						'data' : val
					})
				});
				chartdata.push({
					'group': year,
					'data' : bardata
				});
			});
			return chartdata;

		}else{
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
				'There is no file in first two weeks for those patients.');
		}
	}


	// rewrite the function of reset button
	// right now it return to the filtered list
	function resetBTNSetting(){
		$(".reset-zoom-btn").click(function() {
			currentCategory = "ALL";
			currentYear = "ALL";
			TimeLineChart.data(getDataSubset(currentCategory,currentYear));
		});
	}

	/*
		table for detailed patients' files
	 */
	var files = [];

	var fileTable = $("#csv-file-table").DataTable(
		{
			data : files,
			language : {
				searchPlaceholder : "Search for files in the table..."
			},
			autoWidth : !1,
			responsive : !0,
			columns : [
				{
					data : "csvFile.filename"
				},
				{
					data : "csvFile.startTime"
				},
				{
					data : "csvFile.endTime"
				},
				{
					data : "csvFile.length"
				},
				{
					data:"csvFile.width"
				},
				{
					data:null,
					render: function(data, type, row, meta) {
						if(data.csvFile.headerTime != null){
							return moment(data.csvFile.headerTime).format("MM/DD/YYYY HH:mm:ss")
						}else {
							return ""
						}
					}
				},
				{
					data : null,
					render:function(data, type, row, meta) {return data.csvFile.density.toFixed(3)}
				},
				{
					data : null,
					render : function(data, type, row, meta) {
						if (data.counterpart.length == 1) {
							return "Valid"
						}
						if (data.counterpart.length == 0) {
							return "NONE"
						}
						var counterpartHtml = "";
						data.counterpart.forEach(function(counterpart) {
							counterpartHtml += counterpart.filename + "<br>"
						});
						return counterpartHtml.substr(0, counterpartHtml.length - 4);
					}
				},
				{
					data : "gap"
				},{
					data : null,
					render : function(data, type, row, meta) {
						if(data.problematic == true){
							if(data.csvFile.conflictResolved == true){
								return "Resolved"
							}else{
								return "UnSolved"
							}
						}else{
							return "No Problem"
						}
					}
				}, {
					data: null,
					render: function (data, type, row, meta) {
						var comment;
						if(data.csvFile.comment == null){
							comment = ""
						}else{
							comment = data.csvFile.comment;
						}

						return "<p id=\"comment_content\">" + comment + "</p><button id=\"comment_file_button\" class=\"btn btn-info btn-sm\" data-row=\"" + meta.row
						+ "\" data-toggle=\"modal\" data-target=\"#comment-modal\" >CHANGE</button>";
					}
				}, {
					data: null,
					render: function (data, type, row, meta) {

						if(data.csvFile.conflictResolved){
							var resolveBtn = "<button id=\"cancel_resolved_button\" class=\"btn btn-danger btn-sm\" data-row=\"" + meta.row
								+ "\" data-toggle=\"modal\" data-target=\"#cancel-resolved-modal\" >CANCEL</button>";
						}else{
							var resolveBtn = "<button id=\"resolve_file_button\" class=\"btn btn-info btn-sm\" data-row=\"" + meta.row
								+ "\" data-toggle=\"modal\" data-target=\"#resolve-file-modal\" >RESOLVED</button>";
						}

						var deleteBtn = "<button id=\"delete_file_button\" class=\"btn btn-danger btn-sm\" data-row=\"" + meta.row
							+ "\" data-toggle=\"modal\" data-target=\"#delete-file-modal\">DELETE</button>";

						return resolveBtn + deleteBtn;
					}
				}
			],
			order : [
				[
					1, 'asc'
				]
			],
			columnDefs : [
				{
					targets : 0,
					createdCell : function(td, cellData, rowData, row, col) {
						if (! checkSuffix(cellData)) {
							var color = 'rgba(255, 107, 104,0.5)';
							$(td).css('background-color', color)
						}
					}
				}, {
					targets : [
						1, 2
					],
					render : $.fn.dataTable.render.moment("YYYY-MM-DDTHH:mm:ss", "MM/DD/YYYY HH:mm:ss")
				},
				{
					targets : 6,
					createdCell : function(td, cellData, rowData, row, col) {
						if (cellData > 1 || cellData < 0.8) {
							var alpha = 1 - cellData > 0 ? 1 - cellData : 1;
							var color = 'rgba(255, 107, 104, ' + alpha + ')';
							$(td).css('background-color', color)
						}
					}
				}, {
					targets : 7,
					createdCell : function(td, cellData, rowData, row, col) {
						if (cellData.counterpart.length != 1) {
							var color = 'rgba(255, 107, 104, 0.5)';
							$(td).css('background-color', color)
						}
					}
				}, {
					targets : 8,
					createdCell : function(td, cellData, rowData, row, col) {
						if (cellData.startsWith("-") || parseInt(cellData) > 4) {
							var color = 'rgba(255, 107, 104, 0.5)';
							$(td).css('background-color', color)
						}
					}
				}
			],

			"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				if(aData.problematic && aData.csvFile.conflictResolved){
					$(nRow)
						.css("background-color", "#98df8a")
						.css("color", "black");

					$(nRow).children().each(function (i,n) {
						$(n).css("background-color", "")
					});
				}
			},
			paging : false
		});



	// patient comment save function
	$('#save-patient-comment-button').on("click", function () {
		if ($("#patient-comment-fleid").val() == "") {
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "There is no comment added!");
		}else{
			$('#patient-comment-modal').modal('hide');

			currentPatientInfo.comment = $("#patient-comment-fleid").val();

			$.ajax({
				'type': 'POST',
				'url': "/apis/patient/updatePatientInfo",
				'async': false,
				'data': JSON.stringify(currentPatientInfo),
				'contentType': "application/json",
				'dataType': 'json',
				'success': function () {

					patientComments.set(currentPatientInfo.id,currentPatientInfo.comment);
					$("#patientComment").html(": " + currentPatientInfo.comment);

					for (f in files) {
						var fileType = files[f].csvFile.ar? 'ar' : 'noar';
						var year = files[f].csvFile.filename.split('.')[0].split('-')[1];
						var labelname = files[f].csvFile.pid.split('-',3)[1]+'-' + files[f].csvFile.pid.split('-',3)[2]+ '#' + fileType;
						updateFileInGraph(year,labelname,files[f].csvFile.filename,"commented");
					}

					notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", " patient comment saved");
				},
				'error': function () {
					notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Patient comment save failed!");
				}
			})
		}
	});

	// patient comment delete function
	$('#delete-patient-comment-button').on("click", function () {
		if (currentPatientInfo.comment == null) {
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "There is no comment for this patient!");
		}else{
			$('#patient-comment-modal').modal('hide');

			currentPatientInfo.comment = null;

			$.ajax({
				'type': 'POST',
				'url': "/apis/patient/updatePatientInfo",
				'async': false,
				'data': JSON.stringify(currentPatientInfo),
				'contentType': "application/json",
				'dataType': 'json',
				'success': function () {
					patientComments.delete(currentPatientInfo.id);

					$("#patientComment").empty();
					$("#patient-comment-fleid").val("");

					for (f in files) {
						var action = "normal";
						if(worryPatients.has(currentPatientInfo.id)  && files[f].csvFile.conflictResolved == false){action = "problematic"};
						if(files[f].csvFile.comment != null){action = "commented"};
						if(files[f].csvFile.conflictResolved == true){action = "resolved"};

						var fileType = files[f].csvFile.ar? 'ar' : 'noar';
						var year = files[f].csvFile.filename.split('.')[0].split('-')[1];
						var labelname = files[f].csvFile.pid.split('-',3)[1]+'-' + files[f].csvFile.pid.split('-',3)[2]+ '#' + fileType;
						updateFileInGraph(year,labelname,files[f].csvFile.filename,action);
					}
					notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", " patient comment deleted");
				},
				'error': function () {
					notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Patient comment delete failed!");
				}
			})
		}
	});


	$('#resolve-all-button').on("click", function () {
		$.ajax({
			'type': "GET",
			'url': "/apis/patient/resolveAllFiles",
			'async': false,
			'data': {
				pid: currentPatientInfo.id
			},
			'contentType': "application/json",
			'dataType': 'json',
			'success': function () {
				$('#resolve-all-file-modal').modal('hide');

				$.ajax({
					"url": "/apis/patient/files",
					"type": "GET",
					'data': {
						pid: currentPatientInfo.id
					},
					'contentType': "application/json",
					'dataType': 'json',
					'success': function (data) {
						files = data.data;
						fileTable.clear();
						//check problematic files
						for (f in files) {

							var fileType = files[f].csvFile.ar? 'ar' : 'noar';
							var year = files[f].csvFile.filename.split('.')[0].split('-')[1];
							var labelname = files[f].csvFile.pid.split('-',3)[1]+'-' + files[f].csvFile.pid.split('-',3)[2]+ '#' + fileType;
							updateFileInGraph(year,labelname,files[f].csvFile.filename,"resolved");

							if (!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8) {
								files[f].problematic = true;
							} else {
								files[f].problematic = false;
							}
						}
						fileTable.rows.add(files);
						fileTable.draw();
						$("#csv-file-card").show();
					},
					'error': function () {
					}
				});
				notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", " Marking finished");
			},
			'error': function () {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Marking failed!");
			}
		});
	});

	 function findPatientFiles (pid) {
	 	getPatientInfoByPid(pid);

		$("#card-patient-id").html(pid + '<strong id="patientComment"></strong>');

		if(currentPatientInfo.arresttime != null){
			$("#arresttime").html( moment(currentPatientInfo.arresttime).format('MM/DD/YYYY HH:mm:ss'));
		}else {
			$("#arresttime").html("arrest time is not currently in the database.");
		}

		if(patientComments.has(pid)){
			$("#patientComment").html(": " + patientComments.get(pid));
			$("#patient-comment-fleid").val(patientComments.get(pid));
		}else {
			$("#patient-comment-fleid").val("");
		}

		 $("#comment-patient").html(pid);
		 $("#resolve-all-pid").html(pid);

		$.ajax({
			"url" : "/apis/patient/files",
			"type" : "GET",
			'data' : {
				pid : pid
			},
			'contentType' : "application/json",
			'dataType' : 'json',
			'success' : function(data) {
				files = data.data;
				fileTable.clear();
				//check problematic files
				for(f in files){
					if(!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8){
						files[f].problematic = true;
					}
					else{
						files[f].problematic = false;
					}
				}
				fileTable.rows.add(files);
				fileTable.draw();
				$("#csv-file-card").show();
				$('html, body').animate({
					scrollTop : ($("#csv-file-table").offset().top)
				}, 500);
			},
			'error' : function() {
			}
		});
	};

	// current event file
	var csvFile;

	// button for file comment
	fileTable.on('click', '#comment_file_button', function(event) {
		var row = event.target.dataset.row;
		csvFile = files[row].csvFile;

		$("#comment-file").html(csvFile.filename);

		if(csvFile.comment != null){
			$("#comment-fleid").val(csvFile.comment);
		}else{
			$("#comment-fleid").val("");
		}

	});

	$("#delete-comment-button").click(function () {
		if(csvFile.comment == null){
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "This file doesn't have comment!");
		}else{
			csvFile.comment = null;
			$.ajax({
				'url': "/apis/patient/changeComment",
				'type': 'POST',
				'data': JSON.stringify(csvFile),
				'async': false,
				'contentType': "application/json",
				'dataType': 'json',
				'success': function (data) {
					$('#comment-modal').modal('hide');

					var action = "normal";
					if(worryPatients.has(csvFile.pid)  && csvFile.conflictResolved == false){action = "problematic"};
					if(patientComments.has(csvFile.pid)){action = "commented"};
					if(csvFile.conflictResolved == true){action = "resolved"};


					var fileType = csvFile.ar? 'ar' : 'noar';
					var year = csvFile.filename.split('.')[0].split('-')[1];
					var labelname = csvFile.pid.split('-',3)[1]+'-' + csvFile.pid.split('-',3)[2]+ '#' + fileType;
					updateFileInGraph(year,labelname,csvFile.filename,action);

					$.ajax({
						"url": "/apis/patient/files",
						"type": "GET",
						'data': {
							pid: csvFile.pid
						},
						'contentType': "application/json",
						'dataType': 'json',
						'success': function (data) {
							files = data.data;
							fileTable.clear();
							//check problematic files
							for (f in files) {
								if (!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8) {
									files[f].problematic = true;
								} else {
									files[f].problematic = false;
								}
							}
							fileTable.rows.add(files);
							fileTable.draw();
							$("#csv-file-card").show();
						},
						'error': function () {
						}
					});

					notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Comment deleted.");
				},
				'error': function () {
					notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Comment delete failed");
				}
			});
		}
	});

	$("#save-comment-button").click(function () {
		if ($("#comment-fleid").val() == "") {
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "There is no comment added!");
		} else {
			var preCommented = csvFile.comment == null? false : true;
			csvFile.comment = $("#comment-fleid").val();
			$.ajax({
				'url': "/apis/patient/changeComment",
				'type': 'POST',
				'data': JSON.stringify(csvFile),
				'contentType': "application/json",
				'async': false,
				'dataType': 'json',
				'success': function (data) {
					$('#comment-modal').modal('hide');
					$.ajax({
						"url": "/apis/patient/files",
						"type": "GET",
						'data': {
							pid: csvFile.pid
						},
						'contentType': "application/json",
						'dataType': 'json',
						'success': function (data) {

							if(! preCommented){
								var fileType = csvFile.ar? 'ar' : 'noar';
								var year = csvFile.filename.split('.')[0].split('-')[1];
								var labelname = csvFile.pid.split('-',3)[1]+'-' + csvFile.pid.split('-',3)[2]+ '#' + fileType;
								updateFileInGraph(year,labelname,csvFile.filename,"commented");
							}

							files = data.data;
							fileTable.clear();
							//check problematic files
							for (f in files) {
								if (!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8) {
									files[f].problematic = true;
								} else {
									files[f].problematic = false;
								}
							}
							fileTable.rows.add(files);
							fileTable.draw();
							$("#csv-file-card").show();
						},
						'error': function () {
						}
					});
					notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Comment saved.");
				},
				'error': function () {
					notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Comment save failed");
				}
			})
		}
	});

	fileTable.on('click', '#resolve_file_button', function(event) {
		var row = event.target.dataset.row;
		csvFile = files[row].csvFile;
		$("#resolve-file").html(csvFile.filename);

		var fileInfoHtml = "";
		fileInfoHtml += "<tr><td>Filename</td><td>" + csvFile.filename + "</td></tr>";
		fileInfoHtml += "<tr><td>Start Time</td><td>" + moment(csvFile.startTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>";
		fileInfoHtml += "<tr><td>End Time</td><td>" + moment(csvFile.endTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>";
		fileInfoHtml += "<tr><td>Row Count</td><td>" + csvFile.length + "</td></tr>";
		fileInfoHtml += "<tr><td>Column Count</td><td>" + csvFile.width + "</td></tr>";
		fileInfoHtml += "<tr><td>Header Time</td><td>" + csvFile.headerTime + "</td></tr>";
		fileInfoHtml += "<tr><td>Density</td><td>" + csvFile.density + "</td></tr>";
		fileInfoHtml += "<tr><td>File UUID</td><td>" + csvFile.uuid + "</td></tr>";
		fileInfoHtml += "<tr><td>Import Path</td><td>" + csvFile.path + "</td></tr>";

		$("#resolve-file-info").html(fileInfoHtml);

	});

	$("#resolve-button").click(function () {
		$.ajax({
			'url': "/apis/patient/resolveFiles",
			'type': 'POST',
			'async': false,
			'data': JSON.stringify(csvFile),
			'contentType': "application/json",
			'dataType': 'json',
			'success': function (data) {
				$('#resolve-file-modal').modal('hide');

				$.ajax({
					"url": "/apis/patient/files",
					"type": "GET",
					'data': {
						pid: csvFile.pid
					},
					'contentType': "application/json",
					'dataType': 'json',
					'success': function (data) {

						var fileType = csvFile.ar? 'ar' : 'noar';
						var year = csvFile.filename.split('.')[0].split('-')[1];
						var labelname = csvFile.pid.split('-',3)[1]+'-' + csvFile.pid.split('-',3)[2]+ '#' + fileType;
						updateFileInGraph(year,labelname,csvFile.filename,"resolved");

						files = data.data;
						fileTable.clear();
						//check problematic files
						for (f in files) {
							if (!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8) {
								files[f].problematic = true;
							} else {
								files[f].problematic = false;
							}
						}
						fileTable.rows.add(files);
						fileTable.draw();
						$("#csv-file-card").show();
					},
					'error': function () {
					}
				});
				notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Marking finished.");
			},
			'error': function () {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Marking failed!");
			}
		});
	});

	fileTable.on('click', '#cancel_resolved_button', function(event) {
		var row = event.target.dataset.row;
		csvFile = files[row].csvFile;
		$("#cancel-resolved-file").html(csvFile.filename);

		var fileInfoHtml = "";
		fileInfoHtml += "<tr><td>Filename</td><td>" + csvFile.filename + "</td></tr>";
		fileInfoHtml += "<tr><td>Start Time</td><td>" + moment(csvFile.startTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>";
		fileInfoHtml += "<tr><td>End Time</td><td>" + moment(csvFile.endTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>";
		fileInfoHtml += "<tr><td>Row Count</td><td>" + csvFile.length + "</td></tr>";
		fileInfoHtml += "<tr><td>Column Count</td><td>" + csvFile.width + "</td></tr>";
		fileInfoHtml += "<tr><td>Header Time</td><td>" + csvFile.headerTime + "</td></tr>";
		fileInfoHtml += "<tr><td>Density</td><td>" + csvFile.density + "</td></tr>";
		fileInfoHtml += "<tr><td>File UUID</td><td>" + csvFile.uuid + "</td></tr>";
		fileInfoHtml += "<tr><td>Import Path</td><td>" + csvFile.path + "</td></tr>";

		$("#cancel-resolved-file-info").html(fileInfoHtml);

	});

	$("#cancel-resolved-button").click(function () {
		$.ajax({
			'url': "/apis/patient/resolveFiles",
			'type': 'POST',
			'async': false,
			'data': JSON.stringify(csvFile),
			'contentType': "application/json",
			'dataType': 'json',
			'success': function (data) {
				$('#cancel-resolved-modal').modal('hide');

				$.ajax({
					"url": "/apis/patient/files",
					"type": "GET",
					'data': {
						pid: csvFile.pid
					},
					'contentType': "application/json",
					'dataType': 'json',
					'success': function (data) {

						var action = "normal";
						if(worryPatients.has(csvFile.pid)){action = "problematic"};
						if(csvFile.comment != null || patientComments.has(csvFile.pid) ){action = "commented"};


						var fileType = csvFile.ar? 'ar' : 'noar';
						var year = csvFile.filename.split('.')[0].split('-')[1];
						var labelname = csvFile.pid.split('-',3)[1]+'-' + csvFile.pid.split('-',3)[2]+ '#' + fileType;
						updateFileInGraph(year,labelname,csvFile.filename,action);


						files = data.data;
						fileTable.clear();
						//check problematic files
						for (f in files) {
							if (!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8) {
								files[f].problematic = true;
							} else {
								files[f].problematic = false;
							}
						}
						fileTable.rows.add(files);
						fileTable.draw();
						$("#csv-file-card").show();
					},
					'error': function () {
					}
				});
				notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Marking cancelled.");
			},
			'error': function () {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Marking cancel failed!");
			}
		});
	});


	fileTable.on('click', '#delete_file_button', function(event) {
		var row = event.target.dataset.row;
		csvFile = files[row].csvFile;
		$("#delete-file").html(csvFile.filename);

		var fileInfoHtml = "";
		fileInfoHtml += "<tr><td>Filename</td><td>" + csvFile.filename + "</td></tr>";
		fileInfoHtml += "<tr><td>Start Time</td><td>" + moment(csvFile.startTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>";
		fileInfoHtml += "<tr><td>End Time</td><td>" + moment(csvFile.endTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>";
		fileInfoHtml += "<tr><td>Row Count</td><td>" + csvFile.length + "</td></tr>";
		fileInfoHtml += "<tr><td>Column Count</td><td>" + csvFile.width + "</td></tr>";
		fileInfoHtml += "<tr><td>Header Time</td><td>" + csvFile.headerTime + "</td></tr>";
		fileInfoHtml += "<tr><td>Density</td><td>" + csvFile.density + "</td></tr>";
		fileInfoHtml += "<tr><td>File UUID</td><td>" + csvFile.uuid + "</td></tr>";
		fileInfoHtml += "<tr><td>Import Path</td><td>" + csvFile.path + "</td></tr>";

		$("#file-info").html(fileInfoHtml);

	});

	$("#delete-file-button").click(function () {
		$.ajax({
			'url': "/apis/pseudoDeleteFile",
			'type': 'POST',
			'data': JSON.stringify(csvFile),
			'contentType': "application/json",
			'dataType': 'json',
			'success': function (data) {
				$('#delete-file-modal').modal('hide');

				$.ajax({
					"url": "/apis/patient/files",
					"type": "GET",
					'data': {
						pid: csvFile.pid
					},
					'contentType': "application/json",
					'dataType': 'json',
					'success': function (data) {

						var fileType = csvFile.ar? 'ar' : 'noar';
						var year = csvFile.filename.split('.')[0].split('-')[1];
						var labelname = csvFile.pid.split('-',3)[1]+'-' + csvFile.pid.split('-',3)[2]+ '#' + fileType;
						updateFileInGraph(year,labelname,csvFile.filename,"deleted");


						files = data.data;
						fileTable.clear();
						for (f in files) {
							if (!checkSuffix(files[f].csvFile.filename) || files[f].counterpart.length != 1 || files[f].gap.startsWith("-") || parseInt(files[f].gap) > 4 || files[f].csvFile.density < 0.8) {
								files[f].problematic = true;
							} else {
								files[f].problematic = false;
							}
						}
						fileTable.rows.add(files);
						fileTable.draw();
						$("#csv-file-card").show();
						$('html, body').animate({
							scrollTop: ($("#csv-file-table").offset().top)
						}, 500);
					},
					'error': function () {
					}
				});
				notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Deletion complete.");
			},
			'error': function () {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Delete failed!");
			}
		});
	});

	$('#ParientEEG').click(function() {
		window.location.href = '/analysis/medInfo/' + currentPatientInfo.id;
	});

});