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

	// Initial load of data
	// function load_page_data() {
	// 	$.ajax({
	// 		type: "GET",
	// 		url: "/analysis/getPatientTimelines",
	// 		async: false,
	// 		success : function(text)
	// 		{
	// 			response = JSON.parse(text);
	// 			draw_graph(response);
	// 		}
	// 	});
	// }

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

		if(whereCondition == "WHERE "){
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

    // console.log(columns);

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

    $("#queryFilter").click(function() {
    	filtered_data();
    });

	function draw_graph() {

		var tasks = [];

		for (r in response)
		{
			if(filteredPatient.length != 0 && filteredPatient.indexOf(response[r].pid) == -1){continue;}

			if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 || response[r].relativeStartTime > 4000000){
				continue;
			}
			//response[r].arrestTime = Date.parse(response[r].arrestTime).toString("yyyy-MM-dd HH:mm:ss");
			response[r].status = response[r].filetype === "ar" ? "KILLED" : "FAILED";
			response[r].fname = response[r].filename;
			tasks.push(response[r]);
		}

		var taskStatus = {
			"SUCCEEDED" : "bar",
			"FAILED" : "bar-failed",
			"RUNNING" : "bar-running",
			"KILLED" : "bar-killed"
		};

		//var taskNames = tasks.map(a => a.filename + '#' + a.arrestTime);
		//var taskNames = tasks.map(a => a.arrestTime);
		//var taskNames = tasks.map(a => a.uuid + '#' + a.arrestTime + '#' + a.filetype);
		var taskNames = tasks.map(a => a.pid + '#' + a.arrestTime + '#' + a.filetype);
		var patientFile = tasks.map(a => a.fname);

		/*tasks.sort(function(a, b) {
            return a.relativeEndTime - b.relativeEndTime;
        });
        var maxDate = tasks[tasks.length - 1].endDate;

        tasks.sort(function(a, b) {
            return a.startDate - b.startDate;
        });
        var minDate = tasks[0].startDate;*/

		tasks.sort(function(a, b) {
			return new Date(a.arrestTime) - new Date(b.arrestTime);
		})
		var minDate = tasks[0].arrestTime;
		tasks.sort(function(a, b) {
			return a.relativeEndTime - b.relativeEndTime;
		});
		var maxDate = tasks[tasks.length - 1].relativeEndTime;

		var format = "%j";

		var gantt = d3.gantt(tasks).taskTypes(taskNames).taskStatus(taskStatus).tickFormat(format);
		gantt(tasks);

	};
});


 function draw_graph() {

	var tasks = [];

	for (r in response)
	{
		if(filteredPatient.length != 0 && filteredPatient.indexOf(response[r].pid) == -1){continue;}

		if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 || response[r].relativeStartTime > 4000000){
			continue;
		}
		//response[r].arrestTime = Date.parse(response[r].arrestTime).toString("yyyy-MM-dd HH:mm:ss");
		response[r].status = response[r].filetype === "ar" ? "KILLED" : "FAILED";
		response[r].fname = response[r].filename;
		tasks.push(response[r]);
	}

	var taskStatus = {
	    "SUCCEEDED" : "bar",
	    "FAILED" : "bar-failed",
	    "RUNNING" : "bar-running",
	    "KILLED" : "bar-killed"
	};

	//var taskNames = tasks.map(a => a.filename + '#' + a.arrestTime);
	//var taskNames = tasks.map(a => a.arrestTime);
	//var taskNames = tasks.map(a => a.uuid + '#' + a.arrestTime + '#' + a.filetype);
	var taskNames = tasks.map(a => a.pid + '#' + a.arrestTime + '#' + a.filetype);
	var patientFile = tasks.map(a => a.fname);

	/*tasks.sort(function(a, b) {
	    return a.relativeEndTime - b.relativeEndTime;
	});
	var maxDate = tasks[tasks.length - 1].endDate;

	tasks.sort(function(a, b) {
	    return a.startDate - b.startDate;
	});
	var minDate = tasks[0].startDate;*/

	tasks.sort(function(a, b) {
		return new Date(a.arrestTime) - new Date(b.arrestTime);
	})
	var minDate = tasks[0].arrestTime;
	tasks.sort(function(a, b) {
	    return a.relativeEndTime - b.relativeEndTime;
	});
	var maxDate = tasks[tasks.length - 1].relativeEndTime;

	var format = "%j";

	var gantt = d3.gantt(tasks).taskTypes(taskNames).taskStatus(taskStatus).tickFormat(format);
	gantt(tasks);

};