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
    }


    // filtered Patient list
    var filteredPatient = [];

    // Get Json data from csv_file table
    var patientTimelines = [];
    var username = $('#user_name').html();

    $.ajax({
        type: "get",
        url: "/analysis/getPatientTimelinesByVersion/"+username,
        contentType: "application/json",
        dataType: 'json',
        async: false,
        success : function(text)
        {
            patientTimelines = text.data;
        }
    });

    // placeholder for structuredData
    var structuredData = [];

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
        .domain([ "ar","noar"])
        .range(["#aec7e8", "#98df8a"]);

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
            .onSegmentClick(function (s) {
                window.location.href = '/analysis/medInfo/PUH-' + s.label.split('#')[0];
            })
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
                    window.location.href = '/analysis/medInfo/PUH-' + s1.split('#')[0];
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
                            val: response[r].filetype,
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
                        val: response[r].filetype,
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
                    val:  response[r].filetype,
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

});