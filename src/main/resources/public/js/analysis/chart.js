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
	function checkSuffix(filename,filetype) {
		if (filetype == 'ar'){
			var regex = RegExp('[-_][0-9]*[1-9]+[0-9]*ar.csv','g');
		}else{
			var regex = RegExp('[-_][0-9]*[1-9]+[0-9]*noar.csv','g');
		}
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
		.domain(["ar", "noar","problematic","wrong file name"])
		.range(["#aec7e8", "#ffbb78", "#d62728","#9467bd"]);

	// Graph setting
	TimeLineChart
		.width(document.getElementById("Timelinegraph-container").clientWidth  )
		.maxHeight(720)
		.maxLineHeight(16)
		.xTickFormat(function (n){ return +n })
		.timeFormat('%Q')
		.zColorScale(myColorScale)
		.onSegmentClick(function (s) {
			window.location.href = '/analysis/medInfo/PUH-' + s.label.split('#')[0];
		})
		.onLabelClick(function (s1,s2) {
			if(!(s2 === undefined)){
				window.location.href = '/analysis/medInfo/PUH-' + s1.split('#')[0];
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

			if (! checkSuffix(response[r].filename,response[r].filetype)){
				fileType = "wrong file name";
			}


			// store all the information in nested Hash table
			if ( data.has(response[r].filename.split('.')[0].split('-')[1]) ){
				// left label
				var year = 	data.get(response[r].pid.split('-')[1]);
				// right label
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
				// tooltip
				var tooltip = response[r].filename + '<br>'+ 'arrest Time:' + '<br>' + response[r].arrestTime + '<br>' + 'relative Time (hours):';

				if( year.has(labelname) ){
					var patient = year.get(labelname);
					patient.push(
						{
							timeRange: [
								response[r].relativeStartTime,
								response[r].relativeEndTime
							],
							val: fileType,
							labelVal:tooltip
						}
					);
					year.set(labelname,patient)
				}else{
					year.set(labelname,[{
						timeRange: [
							response[r].relativeStartTime,
							response[r].relativeEndTime
						],
						val:  fileType,
						labelVal:tooltip
					}
					])
				}
			}else{
				var firstbar = new Map();
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
				firstbar.set(labelname,[{
					timeRange: [
						response[r].relativeStartTime,
						response[r].relativeEndTime
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

					for (i in val) {
						// Divide by 3600 means relative time represent in hours
						// Divide by 60 means relative time represent in minutes
						val[i].timeRange[0] = Math.round(val[i].timeRange[0] / 3600);
						val[i].timeRange[1] = Math.round(val[i].timeRange[1] / 3600);
					}

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

			if (! checkSuffix(response[r].filename,response[r].filetype)){
				console.log(response[r].filename);
				fileType = "wrong file name";
			}

			// store all the information in nested Hash table
			if ( data.has(response[r].filename.split('.')[0].split('-')[1]) ){
				// left label
				var year = 	data.get(response[r].pid.split('-')[1]);
				// right label
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
				// tooltip
				var tooltip = response[r].filename + '<br>'+ 'arrest Time:' + '<br>' + response[r].arrestTime + '<br>' + 'relative Time (hours):';

				if( year.has(labelname) ){
					var patient = year.get(labelname);

					patient.push(
						{
							timeRange: [
								response[r].relativeStartTime,
								response[r].relativeEndTime
							],
							val: fileType,
							labelVal:tooltip
						}
					);
					year.set(labelname,patient)
				}else{
					year.set(labelname,[{
						timeRange: [
							response[r].relativeStartTime,
							response[r].relativeEndTime
						],
						val: fileType,
						labelVal:tooltip
					}
					])
				}
			}else{
				var firstbar = new Map();
				var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
				firstbar.set(labelname,[{
					timeRange: [
						response[r].relativeStartTime,
						response[r].relativeEndTime
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

					for (i in val){
						// Divide by 3600 means relative time represent in hours
						// Divide by 60 means relative time represent in minutes
						val[i].timeRange[0] = Math.round(val[i].timeRange[0] / 3600);
						val[i].timeRange[1] = Math.round( val[i].timeRange[1] / 3600);
					}

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

				if (! checkSuffix(response[r].filename,response[r].filetype)){
					fileType = "wrong file name";
				}

				// store all the information in nested Hash table
				if ( data.has(response[r].filename.split('.')[0].split('-')[1]) ){
					// left label
					var year = 	data.get(response[r].pid.split('-')[1]);
					// right label
					var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
					// tooltip
					var tooltip = response[r].filename + '<br>'+ 'arrest Time:' + '<br>' + response[r].arrestTime + '<br>' + 'relative Time (hours):';

					if( year.has(labelname) ){
						var patient = year.get(labelname);
						patient.push(
							{
								timeRange: [
									response[r].relativeStartTime,
									response[r].relativeEndTime
								],
								val: fileType,
								labelVal:tooltip
							}
						);
						year.set(labelname,patient)
					}else{
						year.set(labelname,[{
							timeRange: [
								response[r].relativeStartTime,
								response[r].relativeEndTime
							],
							val: fileType,
							labelVal:tooltip
						}
						])
					}
				}else{
					var firstbar = new Map();
					var labelname = response[r].pid.split('-',3)[1]+'-' + response[r].pid.split('-',3)[2]+ '#' + response[r].filetype;
					firstbar.set(labelname,[{
						timeRange: [
							response[r].relativeStartTime,
							response[r].relativeEndTime
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

						for (i in val) {
							// Divide by 3600 means relative time represent in hours
							// Divide by 60 means relative time represent in minutes
							val[i].timeRange[0] = Math.round(val[i].timeRange[0]/ 3600);
							val[i].timeRange[1] = Math.round(val[i].timeRange[1] / 3600);
						}

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

});