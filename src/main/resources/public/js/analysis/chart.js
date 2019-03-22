$(document).ready(function() {
	console.log("in charts");

	// Get Json data from database
	var response = '';
	var columns = [];

	// Initial load of data
	function load_page_data() {
		$.ajax({ 
			type: "GET",
			url: "/analysis/getPatientTimelines",
			async: false,
			success : function(text)
			{
				response = JSON.parse(text);
				draw_graph(response);
			}
		});
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

	// fetching filtered data
	function filtered_data() {
		console.log("triggering filtered")
		filter = {
			"age": 6
		};
		$.ajax({ 
			type: "POST",
			url: "/analysis/getPatientTimelines",
			data : JSON.stringify(filter),
            contentType : "application/json",
            dataType : "json",
			async: false,
			success : function(text)
			{
				response = JSON.parse(text);
				draw_graph(response);
			}
		});
	}
	
	load_page_data();
	fetch_columns_data();

    console.log(columns);

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
        var html = '<div class="row"><div class="col-sm-3 col-md-3"><select class="init-select2 field" data-placeholder="Filter Field" id="field[]" required><option disabled="disabled" selected="selected" value="">Filter Field</option></select></div><div class="col-sm-2 col-md-2"><select class="init-select2 operator" data-placeholder="Filter Method" id="operator[]" required><option value="=">=</option><option value="!=">&ne;</option><option value=">">&gt;</option><option value=">=">&ge;</option><option value="<">&lt;</option><option value="<=">&le;</option></select></div><div class="col-sm-2 col-md-2"><div class="input-group mb-3"><input type="text" class="form-control" id="value[]" placeholder="Input value" required></div></div><div class="col-sm-1 col-md-1" style="margin-top:6px"><a href="#" class="remove_field btn btn-sm btn-outline-danger">remove</a></div></div>';
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
    	load_page_data();
    });
	

});


draw_graph = function(response) {

	var tasks = new Array();

	for (r in response)
	{
		if(response[r].relativeStartTime < 0 || response[r].relativeEndTime < 0 || response[r].relativeStartTime > 4000000){
			continue;
		}
		//response[r].arrestTime = Date.parse(response[r].arrestTime).toString("yyyy-MM-dd HH:mm:ss");
		response[r].status = response[r].filetype === "ar" ? "KILLED" : "FAILED";
		response[r].fname = response[r].filename;
		tasks.push(response[r]);
	}


	/*var tasks = [
	{"startDate":new Date("Sun Dec 09 01:36:45 EST 2012"),"endDate":new Date("Sun Dec 09 02:36:45 EST 2012"),"taskName":"E Job","status":"RUNNING","fname":"file1"},
	{"startDate":new Date("Sun Dec 09 04:56:32 EST 2012"),"endDate":new Date("Sun Dec 09 06:35:47 EST 2012"),"taskName":"A Job","status":"RUNNING","fname":"file2"},
	{"startDate":new Date("Sun Dec 09 06:29:53 EST 2012"),"endDate":new Date("Sun Dec 09 06:34:04 EST 2012"),"taskName":"D Job","status":"RUNNING","fname":"file3"},
	{"startDate":new Date("Sun Dec 09 05:35:21 EST 2012"),"endDate":new Date("Sun Dec 09 06:21:22 EST 2012"),"taskName":"P Job","status":"RUNNING","fname":"file4"},
	{"startDate":new Date("Sun Dec 09 05:00:06 EST 2012"),"endDate":new Date("Sun Dec 09 05:05:07 EST 2012"),"taskName":"D Job","status":"RUNNING","fname":"file5"},
	{"startDate":new Date("Sun Dec 09 03:46:59 EST 2012"),"endDate":new Date("Sun Dec 09 04:54:19 EST 2012"),"taskName":"P Job","status":"RUNNING","fname":"file6"},
	{"startDate":new Date("Sun Dec 09 04:02:45 EST 2012"),"endDate":new Date("Sun Dec 09 04:48:56 EST 2012"),"taskName":"N Job","status":"RUNNING","fname":"file7"},
	{"startDate":new Date("Sun Dec 09 03:27:35 EST 2012"),"endDate":new Date("Sun Dec 09 03:58:43 EST 2012"),"taskName":"E Job","status":"SUCCEEDED","fname":"file8"},
	{"startDate":new Date("Sun Dec 09 01:40:11 EST 2012"),"endDate":new Date("Sun Dec 09 03:26:35 EST 2012"),"taskName":"A Job","status":"SUCCEEDED","fname":"file9"},
	{"startDate":new Date("Sun Dec 09 03:00:03 EST 2012"),"endDate":new Date("Sun Dec 09 03:09:51 EST 2012"),"taskName":"D Job","status":"SUCCEEDED","fname":"file10"},
	{"startDate":new Date("Sun Dec 09 01:21:00 EST 2012"),"endDate":new Date("Sun Dec 09 02:51:42 EST 2012"),"taskName":"P Job","status":"SUCCEEDED","fname":"file11"},
	{"startDate":new Date("Sun Dec 09 01:08:42 EST 2012"),"endDate":new Date("Sun Dec 09 01:33:42 EST 2012"),"taskName":"N Job","status":"FAILED","fname":"file12"},
	{"startDate":new Date("Sun Dec 09 00:27:15 EST 2012"),"endDate":new Date("Sun Dec 09 00:54:56 EST 2012"),"taskName":"E Job","status":"SUCCEEDED","fname":"file13"},
	{"startDate":new Date("Sun Dec 09 00:29:48 EST 2012"),"endDate":new Date("Sun Dec 09 00:44:50 EST 2012"),"taskName":"D Job","status":"SUCCEEDED","fname":"file14"},
	{"startDate":new Date("Sun Dec 09 07:39:21 EST 2012"),"endDate":new Date("Sun Dec 09 07:43:22 EST 2012"),"taskName":"P Job","status":"RUNNING","fname":"file15"},
	{"startDate":new Date("Sun Dec 09 07:00:06 EST 2012"),"endDate":new Date("Sun Dec 09 07:05:07 EST 2012"),"taskName":"D Job","status":"RUNNING","fname":"file16"},
	{"startDate":new Date("Sun Dec 09 08:46:59 EST 2012"),"endDate":new Date("Sun Dec 09 09:54:19 EST 2012"),"taskName":"P Job","status":"RUNNING","fname":"file17"},
	{"startDate":new Date("Sun Dec 09 09:02:45 EST 2012"),"endDate":new Date("Sun Dec 09 09:48:56 EST 2012"),"taskName":"N Job","status":"RUNNING","fname":"file18"},
	{"startDate":new Date("Sun Dec 09 08:27:35 EST 2012"),"endDate":new Date("Sun Dec 09 08:58:43 EST 2012"),"taskName":"E Job","status":"SUCCEEDED","fname":"file19"},
	{"startDate":new Date("Sun Dec 09 08:40:11 EST 2012"),"endDate":new Date("Sun Dec 09 08:46:35 EST 2012"),"taskName":"A Job","status":"SUCCEEDED","fname":"file20"},
	{"startDate":new Date("Sun Dec 09 08:00:03 EST 2012"),"endDate":new Date("Sun Dec 09 08:09:51 EST 2012"),"taskName":"D Job","status":"SUCCEEDED","fname":"file21"},
	{"startDate":new Date("Sun Dec 09 10:21:00 EST 2012"),"endDate":new Date("Sun Dec 09 10:51:42 EST 2012"),"taskName":"P Job","status":"SUCCEEDED","fname":"file22"},
	{"startDate":new Date("Sun Dec 09 11:08:42 EST 2012"),"endDate":new Date("Sun Dec 09 11:33:42 EST 2012"),"taskName":"N Job","status":"FAILED","fname":"file23"},
	{"startDate":new Date("Sun Dec 09 12:27:15 EST 2012"),"endDate":new Date("Sun Dec 09 12:54:56 EST 2012"),"taskName":"E Job","status":"SUCCEEDED","fname":"file24"},
	{"startDate":new Date("Sat Dec 08 23:12:24 EST 2012"),"endDate":new Date("Sun Dec 09 00:26:13 EST 2012"),"taskName":"A Job","status":"KILLED","fname":"file25"}
	];*/
	
	// var tasks = [
	// 		{"filename":"PUH-2013-119-01ar.csv", "arrestTime":"Mon Aug 12 22:39:00 EST 2013", "relativeStartTime": 37667, "relativeEndTime": 39217, "length": 1504, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2013-119-01ar.csv", "arrestTime":"Mon Aug 12 22:39:00 EST 2013", "relativeStartTime": 52067, "relativeEndTime": 53617, "length": 1504, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2013-122-01ar.csv", "arrestTime":"Mon Aug 19 08:00:00 EST 2013", "relativeStartTime": 64390, "relativeEndTime": 66018, "length": 1510, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2015-015_01ar.csv", "arrestTime":"Tue Jan 27 15:13:00 EST 2015", "relativeStartTime": 69577, "relativeEndTime": 71145, "length": 1505, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2017-243-01ar.csv", "arrestTime":"Mon Sep 25 00:00:00 EST 2017", "relativeStartTime": 85076, "relativeEndTime": 87665, "length": 1505, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2015-009_01ar.csv", "arrestTime":"Sat Jan 10 00:00:00 EST 2015", "relativeStartTime": 110461, "relativeEndTime": 112201, "length": 1526, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2017-315_04ar.csv", "arrestTime":"Sat Dec 09 00:00:00 EST 2017", "relativeStartTime": 613581, "relativeEndTime": 615157, "length": 1511, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2017-315_04ar.csv", "arrestTime":"Sat Dec 09 00:00:00 EST 2017", "relativeStartTime": 613581, "relativeEndTime": 615157, "length": 1511, "status":"KILLED","fname":"file25"},
	// 		{"filename":"PUH-2013-154_12ar.csv", "arrestTime":"Thu Oct 10 09:20:00 EST 2013", "relativeStartTime":1523044,"relativeEndTime": 1526146, "length": 1507, "status":"KILLED","fname":"file25"}
	// 	];


	var taskStatus = {
	    "SUCCEEDED" : "bar",
	    "FAILED" : "bar-failed",
	    "RUNNING" : "bar-running",
	    "KILLED" : "bar-killed"
	};
	
	//var taskNames = [ "D Job", "P Job", "E Job", "A Job", "N Job" ];
	
	//var taskNames = tasks.map(a => a.filename + '#' + a.arrestTime);
	//var taskNames = tasks.map(a => a.arrestTime);
	//var taskNames = tasks.map(a => a.uuid + '#' + a.arrestTime + '#' + a.filetype);
	var taskNames = tasks.map(a => a.pid + '#' + a.arrestTime + '#' + a.filetype);
	
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

	console.log("mindate: " + minDate);
	console.log("maxdate: " + maxDate);

	var format = "%d";

	//console.log(tasks);

	var gantt = d3.gantt(tasks).taskTypes(taskNames).taskStatus(taskStatus).tickFormat(format);
	gantt(tasks);

};