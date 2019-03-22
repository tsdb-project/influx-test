$(document).ready(function () {

    var rgbaSet = ['rgba(192, 135, 3, 1)','rgba(50, 3, 135, 1)','rgba(50, 199, 135, 1)','rgba(200, 5, 5, 1)'];

    function isEmpty(obj)
    {
        for (var name in obj)
        {
            return false;
        }
        return true;
    };

    dataVisualization = function(response,selecteddrugs) {

        //clear the canvas
        $('#myChart').remove();
        $('.chart-container').append('<canvas id="myChart"></canvas>');

        var datasets = []; // for store points
        var labels = new Set(); // store timestamp for chart
        var medInfo = new Map(); // store extra information like route
        var yAxes = []; // store yAxis in case there are multiple units

        for ( d in selecteddrugs){
            var curDrug = drug.get(selecteddrugs[d])
            curDrug.sort(function ( a ,b ) {return a.chartDate > b.chartDate ? 1 : -1;}); // sort based on chartDate
            var cPoint = [];  // Continuous infusion points
            var iPoint = []; // Intermittent infusion points
            var unit = new Map(); // store units
            var route = new Map(); // store routes


            for (var point in curDrug){
                // skip the redundant points for continuous infusion
                if(cPoint.length>=1 && curDrug[point].dose == cPoint[cPoint.length - 1].y){continue;}

                time = new moment (curDrug[point].chartDate).format().toString(); // format current data point

                labels.add(time);
                route.set(time,curDrug[point].route); // store routes used for current time point

                if(curDrug[point].type == "Continuous"){

                    cPoint.push({
                            x: time,
                            y: curDrug[point].dose
                        });

                    if (!unit.has('Continuous')){
                        unit.set( 'Continuous' ,curDrug[point].unit); // set unit for continuous infusion
                        yAxes.push({
                            id: selecteddrugs[d] + ' C',
                            position: !unit.has('Intermittent')? 'left' : 'right',
                            scaleLabel: {
                                display: true,
                                fontSize: 14,
                                labelString: selecteddrugs[d] + ' (' + curDrug[point].unit + ')',
                            },
                            ticks : {
                                min : 0
                            }
                        })
                    }

                }else{
                    iPoint.push({
                        x: time,
                        y: curDrug[point].dose
                    });

                    if (!unit.has('Intermittent')){
                        unit.set( 'Intermittent' ,curDrug[point].unit);// set unit for Intermittent infusion
                        yAxes.push({
                            id: selecteddrugs[d] + ' I',
                            position: !unit.has('Continuous')? 'left' : 'right',
                            scaleLabel: {
                                display: true,
                                fontSize: 14,
                                labelString: selecteddrugs[d] + ' (' + curDrug[point].unit + ')',
                            },
                            ticks : {
                                min : 0
                            }
                        })
                    }
                }
            }

            medInfo.set(selecteddrugs[d],route); // store routes for current drug
            var color;

            if (!isEmpty(cPoint)){  //push continuous infusion points to the dataset
                datasets.push({
                    pointRadius: 1,
                    steppedLine : 'before',
                    yAxisID:selecteddrugs[d] + ' C',
                    label: selecteddrugs[d] + ' (' + unit.get('Continuous') + ')',
                    borderColor: rgbaSet[d % rgbaSet.length],
                    borderWidth : 3,
                    fill: false,
                    data: cPoint
                })
            }

            if (!isEmpty(iPoint)){ //push intermittent infusion points to the dataset
                datasets.push({
                    pointRadius: 1,
                    steppedLine : 'before',
                    showLine: false,
                    yAxisID:selecteddrugs[d] + ' I',
                    label: selecteddrugs[d] + ' (' + unit.get('Intermittent') + ')',
                    borderColor: rgbaSet[d % rgbaSet.length],
                    borderWidth : 3,
                    data: iPoint
                })
            }
        }

        //setting for chart
        var timeLabels = Array.from(labels).sort();
        var timeUnit = (new Date(timeLabels[timeLabels.length-1]) - new Date(timeLabels[0]) > 24*60*60*1000) ? 'day':'hour';

        var myChart = new Chart($("#myChart"), {
            type : 'line',
            data : {
                labels : timeLabels,
                datasets : datasets
            },
            options : {
                elements: {
                    line: {
                        steppedLine : 'before',
                    }
                },
                tooltips: {
                    callbacks: {
                        title: function (item,data) {
                            var label = data.datasets[item[0].datasetIndex].label
                            var MedName = label.split('(')[0].trim()
                            return 'Time: ' + item[0].xLabel + '\n'+
                                    'Route: '+ medInfo.get(MedName).get(item[0].xLabel);
                        }
                    }
                },
                events : [ "mousemove", "touchstart", "touchmove", "touchend", "click"
                ],
                onClick : function() {$("#collapseExample").collapse('toggle');},
                scales : {
                    xAxes: [{
                        type: 'time',
                        distribution: 'linear',
                        ticks: {
                            source: 'label'
                        },
                        time: {
                            unit: timeUnit,
                        }
                    }],
                    yAxes : yAxes
                }
            }
        });
    };

    $.ajax({ type: "GET",
        url: "/analysis/getPatientMedInfoById/" + $("#patientId").text(),
        async: false,
        success : function(text)
        {
            response = text.data;
        }
    });

    var drug = new Map();
    var type;

    for (r in response){

        // logic to find out whether the drug is continuous infusion or not.
        type = ((response[r].doseUnit).indexOf("/") > -1) ? 'Continuous':'Intermittent';

        if(drug.has(response[r].drugName)){
            data = drug.get(response[r].drugName)

            // skip the redundant points for continuous infusion
            if (data[0].type == 'Continuous' && response[r].infusedVol != 0){continue;}

            data.push({
                    chartDate: response[r].chartDate,
                    dose: response[r].dose,
                    unit:response[r].doseUnit,
                    type: type,
                    route: response[r].route
            });
            drug.set(response[r].drugName ,data);
        }
        else{
            drug.set(response[r].drugName ,[{
                chartDate: response[r].chartDate,
                dose: response[r].dose,
                unit:response[r].doseUnit,
                type:type,
                route: response[r].route
            }])
        }
    }


    var columnData = Array.from(drug.keys());

    $(".field").select2({
        width: '100%',
        data : columnData
    });

    var wrapper = $("#filterForm"); // Fields wrapper
    var add_button = $("#addDrug"); // Add button ID
    var go_button = $("#queryDrug"); // Go button ID

    var x = 1; // initlal text box count
    $(add_button).click(function(e) { // on add input button click
        e.preventDefault();
        var html = '                                <div class="row">\n' +
            '                                    <div class="col-sm-3 col-md-3">\n' +
            '                                        <select class="init-select2 field" data-placeholder="Drug">\n' +
            '                                            <option disabled="disabled" selected="selected" value="">Drugs</option>\n' +
            '                                        </select>\n' +
            '                                    </div>\n' +
            '                                    <div class="col-sm-1 col-md-1" style="margin-top: 6px;">\n' +
            '                                        <a href="#" class="remove_field btn btn-sm btn-outline-danger">remove</a>\n' +
            '                                    </div>\n' +
            '                                </div>';
        $(wrapper).append(html);

        $(".field").select2({
            width: '100%',
            data : columnData
        });

    });
    $(wrapper).on("click", ".remove_field", function(e) { // user click on
        // remove text
        e.preventDefault();
        $(this).parent('div').parent('div').remove();
    });

    $(go_button).click(function(e) {
        var drugs = $(".field option:selected" );
        var selectedDrugs = new Set();
        for (i in drugs) {
            if(drugs[i].value){
                selectedDrugs.add(drugs[i].value);
            }
        }
        dataVisualization(response,Array.from(selectedDrugs));
    })
    // var table = $('#medInfoTable').DataTable({
    //     ajax: {
    //         "url": "/analysis/getPatientMedInfoById/" + $("#patientId").text()
    //     },
    //     data: Patient.data,
    //     columnDefs: [{
    //         "targets": [0],
    //         "visible": false,
    //         "searchable": false
    //     }],
    //     columns: [{
    //         data: 'id'
    //     },{
    //         data: 'infusedVol'
    //     },{
    //         data: null,
    //         render: function (data) {
    //             return new Date(data.chartDate);
    //         }
    //     }, {
    //         data: 'drugName'
    //     }, {
    //         data: 'dose'
    //     },{
    //         data: 'doseUnit'
    //     },{
    //         data: 'rate'
    //     }, {
    //         data: 'rateUnit'
    //     },{
    //         data: 'orderedAs'
    //     },{
    //         data: 'route'
    //     }, {
    //         data: 'status'
    //     }, {
    //         data: 'site'
    //     }, {
    //         data: 'infusedVolUnit'
    //     }, {
    //         data: 'infuseInd'
    //     }, {
    //         data: 'ivFlag'
    //     }, {
    //         data: 'bolusFlag'
    //     }, {
    //         data: 'tdripInd'
    //     },{
    //         data: null,
    //         render: function(data) {
    //             return "<th><button class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#edit-group-modal\" data-id=\"" +
    //                 data + "\"> Export</button> " + "</th>";
    //         }
    //     }],
    //     order: [[2, 'desc']],
    // });

    // $('#medInfoTable tbody').on('mouseover', 'tr', function () {
    //     $(this).attr("style", "background-color:#ffffdd");
    // });
    //
    // $('#medInfoTable tbody').on('mouseout', 'tr', function () {
    //     $(this).removeAttr('style');
    // });

});