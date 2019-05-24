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

    
    // Get Json data from csv_file table
	var response = [];
	$.ajax({
		type: "GET",
		url: "/analysis/getPatientTimelines",
		async: false,
		success : function(text)
		{
			response = JSON.parse(text);
		}
	});

	var columns = [];

	// filtered Patient list
	var filteredPatient = [];

	// get list of worry patients
	var worryPatients = new Set();
	var querydata = [];
	$.ajax({
		type: "GET",
		url: "/analysis/getWrongPatients",
		async: false,
		success : function(text)
		{
			querydata = text.data;
			for(r in text.data){
				worryPatients.add(text.data[r].pid);
			}
		}
	});

	// check file suffix
	function checkSuffix(filename) {
		var regex = RegExp('[ -_][0]*[1-9]+[0]*(noar|ar).csv','g');
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
		.domain(["ar", "noar","problematic"])
		.range(["#aec7e8", "#98df8a", "#d62728"]);
		// .domain(["ar", "noar","problematic","wrong file name"])
		// .range(["#aec7e8", "#98df8a", "#d62728","#9467bd"]);

	// Graph setting
	TimeLineChart
		.width(document.getElementById("Timelinegraph-container").clientWidth  )
		.maxHeight(720)
		.maxLineHeight(16)
		.xTickFormat(function (n){ return +n })
		.timeFormat('%Q')
		.zColorScale(myColorScale)
		.onSegmentClick(function (s) {
			// window.location.href = '/analysis/medInfo/PUH-' + s.label.split('#')[0];
			findPatientFiles('PUH-' + s.label.split('#')[0]);
		})
		.onLegendClick(function (s) {
			$(".reset-zoom-btn").click();

			//get current data in the graph
			var currentdata = TimeLineChart.data();
			var categoryData = new Map();

			for (i in currentdata) {
				categoryData.set(currentdata[i].group, new Map());
				for (j in currentdata[i].data) {
					var patientsData = categoryData.get(currentdata[i].group);
					var fileData = currentdata[i].data[j].data;
					var filteredFileData = [];
					for (k in fileData) {
						if (fileData[k].val == s) {
							filteredFileData.push(fileData[k])
						}
					}
					if(filteredFileData.length != 0){patientsData.set(currentdata[i].data[j].label, filteredFileData)}
				}
			}

			var chartdata = [];

			// convert nested hash into data needed for graph
			categoryData.forEach(function (patientList, year) {
				var bardata = [];
				patientList.forEach(function (val, label) {

					bardata.push({
						'label': label,
						'data': val
					})
				});
				chartdata.push({
					'group': year,
					'data': bardata
				});
			});

			TimeLineChart.data(chartdata);
		})
		.onLabelClick(function (s1,s2) {
			if(!(s2 === undefined)){
				// window.location.href = '/analysis/medInfo/PUH-' + s1.split('#')[0];
				findPatientFiles('PUH-' + s1.split('#')[0]);
			}else{
				//get current data in the graph
				var currentdata = TimeLineChart.data();

				// get the patients in the specific year
				var yearData = [];
				for (i in currentdata){
					if (currentdata[i].group == s1){
						yearData.push(currentdata[i])
					}
				}
				TimeLineChart.data(yearData);
			}
		})
		(document.getElementById('Timelinegraph-container'));

	draw_graph();

    /*
    *  end of drawing the graph
    * */


	fetch_columns_data();

    // fetching filtered data
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
				type: "GET",
				url: "/analysis/selecIdByfilter/" + whereCondition.slice(0,-4),
				async: false,
				success : function(text)
				{
					filteredPatient = text;
				}
			});
		}

		if (filteredPatient.length == 0) {
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
				'No patient satisfies your conditions.');
		}else{
			draw_graph();
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
		var data = new Map();

		for (r in response)
		{
			// too many patients problem
			// if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 ){
			if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 || response[r].relativeStartTime > 1296000){
				continue;
			}

			// find out if the patient is problematic / mislabeled or not
			var fileType;
			if(worryPatients.has(response[r].pid)){
				fileType = "problematic";
			}else{
				fileType = response[r].filetype;
			}

			// if (! checkSuffix(response[r].filename,response[r].filetype)){
			// 	fileType = "wrong file name";
			// }


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
						val:  fileType,
						filename:response[r].filename,
						labelVal:tooltip
					}
					])
				}
			}else{
				var firstbar = new Map();
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
				firstbar.set(labelname,[{
					timeRange: [
						Math.round(response[r].relativeStartTime / 3600),
						Math.round(response[r].relativeEndTime / 3600)
					],
					val: fileType,
					filename:response[r].filename,
					labelVal:tooltip
				}
				]);
				data.set(response[r].filename.split('.')[0].split('-')[1], firstbar)
			}
		}

		// see if there are files Satisfy filter condition and relative end time constrain
		if(data.size != 0) {

			var chartdata = [];

			// convert nested hash into data needed for graph
			data.forEach(function (patientList, year) {
				var bardata = [];
				patientList.forEach(function (val, label) {

					bardata.push({
						'label': label,
						'data': val
					})
				});
				chartdata.push({
					'group': year,
					'data': bardata
				});
			});
		}
		TimeLineChart.data(chartdata)
	});


	/*
	*  assign data to the graph
	* */
	function draw_graph() {

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
			var fileType;
			if(worryPatients.has(response[r].pid)){
				fileType = "problematic";
			}else{
				fileType = response[r].filetype;
			}

			// if (! checkSuffix(response[r].filename,response[r].filetype)){
			// 	fileType = "wrong file name";
			// }

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
				firstbar.set(labelname,[{
					timeRange: [
						Math.round(response[r].relativeStartTime / 3600),
						Math.round(response[r].relativeEndTime / 3600)
					],
					val:  fileType,
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

			// reset the data for timeLine chart
			TimeLineChart.data(chartdata)
			
		}else{
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
				'No patient satisfies your conditions.');
		}

		// rewrite the function of reset button
		// right now it return to the filtered list
		$(".reset-zoom-btn").click(function() {
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
				var fileType;
				if(worryPatients.has(response[r].pid)){
					fileType = "problematic";
				}else{
					fileType = response[r].filetype;
				}

				// if (! checkSuffix(response[r].filename,response[r].filetype)){
				// 	fileType = "wrong file name";
				// }

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
					firstbar.set(labelname,[{
						timeRange: [
							Math.round(response[r].relativeStartTime / 3600),
							Math.round(response[r].relativeEndTime / 3600)
						],
						val: fileType,
						labelVal:tooltip
					}
					]);
					data.set(response[r].filename.split('.')[0].split('-')[1], firstbar)
				}
			}

			// see if there are files Satisfy filter condition and relative end time constrain
			if(data.size != 0) {

				var chartdata = [];

				// convert nested hash into data needed for graph
				data.forEach(function (patientList, year) {
					var bardata = [];
					patientList.forEach(function (val, label) {

						bardata.push({
							'label': label,
							'data': val
						})
					});
					chartdata.push({
						'group': year,
						'data': bardata
					});
				});
			}
			TimeLineChart.data(chartdata)
		});
		
	};

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
					data : "csvFile.filename",
				},
				{
					data : "csvFile.startTime",
				},
				{
					data : "csvFile.endTime"
				},
				{
					data : "csvFile.length"
				},
				{
					data : "csvFile.density"
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
						return "<button id=\"comment_file_button\" class=\"btn btn-info btn-sm\" data-row=\"" + meta.row
						+ "\" data-toggle=\"modal\" data-target=\"#comment-modal\" >NOTE</button>";
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
				},
				// }, {
				// 	targets : [
				// 		1, 2
				// 	],
				// 	render : $.fn.dataTable.moment("YYYY-MM-DDTHH:mm:ss", "MM/DD/YYYY HH:mm:ss")
				// },
				{
					targets : 4,
					createdCell : function(td, cellData, rowData, row, col) {
						if (cellData > 1 || cellData < 0.8) {
							var alpha = 1 - cellData > 0 ? 1 - cellData : 1
							var color = 'rgba(255, 107, 104, ' + alpha + ')'
							$(td).css('background-color', color)
						}
					}
				}, {
					targets : 5,
					createdCell : function(td, cellData, rowData, row, col) {
						if (cellData.counterpart.length != 1) {
							var color = 'rgba(255, 107, 104, 0.5)'
							$(td).css('background-color', color)
						}
					}
				}, {
					targets : 6,
					createdCell : function(td, cellData, rowData, row, col) {
						if (cellData.startsWith("-") || parseInt(cellData) > 4) {
							var color = 'rgba(255, 107, 104, 0.5)'
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


	$('#resolve-all-button').on("click",function () {
		if($("#resolve-all-pid-confirm").val() == $("#resolve-all-pid").val()) {
			$.ajax({
				type: "GET",
				url: "/apis/patient/resolveAllFiles" ,
				async: false,
				'data' : {
					pid : $("#card-patient-id").val()
				},
				'contentType' : "application/json",
				'dataType' : 'json',
				success : function()
				{
					$('#resolve-all-file-modal').modal('hide');
					notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", " marking Finished");
					$.ajax({
						"url" : "/apis/patient/files",
						"type" : "GET",
						'data' : {
							pid : $("#card-patient-id").val()
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
						},
						'error' : function() {
						}
					});
				}
			});
		}else {
			notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Patient ID validation failed!");
		}
	})

	 function findPatientFiles (pid) {
		$("#card-patient-id").val(pid);
		$("#card-patient-id").html(pid);
		$("#resolve-all-pid").val(pid);
		$("#resolve-all-pid").html(pid);
		$.ajax({
			"url" : "/apis/patient/files",
			"type" : "GET",
			'data' : {
				pid : $("#card-patient-id").val()
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

	// under construction: button for comment
	fileTable.on('click', '#comment_file_button', function(event) {
		var row = event.target.dataset.row;
		var csvFile = files[row].csvFile;
		$("#comment-file").html(csvFile.filename);
	});

	fileTable.on('click', '#resolve_file_button', function(event) {
		var row = event.target.dataset.row;
		var csvFile = files[row].csvFile;
		$("#resolve-file").html(csvFile.filename);

		var fileInfoHtml = "";
		fileInfoHtml += "<tr><td>Filename</td><td>" + csvFile.filename + "</td></tr>"
		fileInfoHtml += "<tr><td>Start Time</td><td>" + moment(csvFile.startTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>"
		fileInfoHtml += "<tr><td>End Time</td><td>" + moment(csvFile.endTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>"
		fileInfoHtml += "<tr><td>Row Count</td><td>" + csvFile.length + "</td></tr>"
		fileInfoHtml += "<tr><td>Density</td><td>" + csvFile.density + "</td></tr>"
		fileInfoHtml += "<tr><td>File UUID</td><td>" + csvFile.uuid + "</td></tr>"
		fileInfoHtml += "<tr><td>Import Path</td><td>" + csvFile.path + "</td></tr>"

		$("#resolve-file-info").html(fileInfoHtml);

		$("#resolve-button").click(function() {
			if ($("#resolve-file-pid-confirm").val() == csvFile.pid) {
				$.ajax({
					'url' : "/apis/patient/resolveFiles",
					'type' : 'POST',
					'async': false,
					'data' : JSON.stringify(csvFile),
					'contentType' : "application/json",
					'dataType' : 'json',
					'success' : function(data) {
						$('#resolve-file-modal').modal('hide');
						notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "marking Finished.");
						$.ajax({
							"url" : "/apis/patient/files",
							"type" : "GET",
							'data' : {
								pid : csvFile.pid
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
							},
							'error' : function() {
							}
						});
					},
					'error' : function() {
					}
				});
			} else {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Patient ID validation failed!");
			}
		});
	});

	fileTable.on('click', '#cancel_resolved_button', function(event) {
		var row = event.target.dataset.row;
		var csvFile = files[row].csvFile;
		$("#cancel-resolved-file").html(csvFile.filename);

		var fileInfoHtml = "";
		fileInfoHtml += "<tr><td>Filename</td><td>" + csvFile.filename + "</td></tr>"
		fileInfoHtml += "<tr><td>Start Time</td><td>" + moment(csvFile.startTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>"
		fileInfoHtml += "<tr><td>End Time</td><td>" + moment(csvFile.endTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>"
		fileInfoHtml += "<tr><td>Row Count</td><td>" + csvFile.length + "</td></tr>"
		fileInfoHtml += "<tr><td>Density</td><td>" + csvFile.density + "</td></tr>"
		fileInfoHtml += "<tr><td>File UUID</td><td>" + csvFile.uuid + "</td></tr>"
		fileInfoHtml += "<tr><td>Import Path</td><td>" + csvFile.path + "</td></tr>"

		$("#cancel-resolved-file-info").html(fileInfoHtml);

		$("#cancel-resolved-button").click(function() {
			if ($("#cancel-resolved-pid-confirm").val() == csvFile.pid) {
				$.ajax({
					'url' : "/apis/patient/resolveFiles",
					'type' : 'POST',
					'async': false,
					'data' : JSON.stringify(csvFile),
					'contentType' : "application/json",
					'dataType' : 'json',
					'success' : function(data) {
						$('#cancel-resolved-modal').modal('hide');
						notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "marking cancelled.");
						$.ajax({
							"url" : "/apis/patient/files",
							"type" : "GET",
							'data' : {
								pid : csvFile.pid
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
							},
							'error' : function() {
							}
						});
					},
					'error' : function() {
					}
				});
			} else {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Patient ID validation failed!");
			}
		});
	});


	fileTable.on('click', '#delete_file_button', function(event) {
		var row = event.target.dataset.row;
		var csvFile = files[row].csvFile;
		$("#delete-file").html(csvFile.filename);

		var fileInfoHtml = "";
		fileInfoHtml += "<tr><td>Filename</td><td>" + csvFile.filename + "</td></tr>"
		fileInfoHtml += "<tr><td>Start Time</td><td>" + moment(csvFile.startTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>"
		fileInfoHtml += "<tr><td>End Time</td><td>" + moment(csvFile.endTime).format("MM/DD/YYYY HH:mm:ss") + "</td></tr>"
		fileInfoHtml += "<tr><td>Row Count</td><td>" + csvFile.length + "</td></tr>"
		fileInfoHtml += "<tr><td>Density</td><td>" + csvFile.density + "</td></tr>"
		fileInfoHtml += "<tr><td>File UUID</td><td>" + csvFile.uuid + "</td></tr>"
		fileInfoHtml += "<tr><td>Import Path</td><td>" + csvFile.path + "</td></tr>"

		$("#file-info").html(fileInfoHtml);

		$("#delete-button").click(function() {
			if ($("#pid-confirm").val() == csvFile.pid) {
				notify("top", "center", null, "warning", "animated fadeIn", "animated fadeOut", "Deleting data...");
				$.ajax({
					'url' : "/apis/file",
					'type' : 'DELETE',
					'data' : JSON.stringify(csvFile),
					'contentType' : "application/json",
					'dataType' : 'json',
					'success' : function(data) {
						$('#delete-file-modal').modal('hide');
						notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Deletion complete.");
						console.log(data);
						$.ajax({
							"url" : "/apis/patient/files",
							"type" : "GET",
							'data' : {
								pid : csvFile.pid
							},
							'contentType' : "application/json",
							'dataType' : 'json',
							'success' : function(data) {
								files = data.data;
								fileTable.clear();
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
					},
					'error' : function() {
					}
				});
			} else {
				notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Patient ID validation failed!");
			}
		});
	});


	/*
		draw the table for the problematic patients
	 */
	$.fn.dataTable.moment('M/D/YYYY, h:mm:ss a');
	var table = $('#queryTable').DataTable({
		data: querydata,
		columnDefs: [{
			"targets": [0],
			"visible": true,
			"searchable": true
		}],
		columns: [{
			data: 'pid'
		}, {
			data:null,
			render:function (data){
				return booleanToStr(data.isoverlap);
			}
		},{
			data:'ar_miss'
		},{
			data:'noar_miss'
		},{
			data:null,
			render:function (data) {
				return booleanToStr(data.wrongname);
			}
		}],
		order: [[0, 'desc']],
	});

	function booleanToStr(flag){
		return flag ? 'T':' ';
	}

	$('#queryTable tbody').on('mouseover', 'tr', function () {
		$(this).attr("style", "background-color:#ffffdd");
	});

	$('#queryTable tbody').on('mouseout', 'tr', function () {
		$(this).removeAttr('style');
	});

	$('#ParientEEG').click(function() {
		window.location.href = '/analysis/medInfo/' + $("#card-patient-id").val();
	});

});