$(document).ready(function () {

    var CColorSet = ['rgba(255, 48, 48, 1)','rgba(255, 127, 36, 1)','rgba(255, 165, 0, 1)','rgba(178, 34, 34, 1)'];
    var IColorSet = ['rgba(0, 0, 205, 1)','rgba(0, 191, 255, 1)','rgba(0, 139, 69, 1)','rgba(0, 197, 205, 1)'];

    function isEmpty(obj)
    {
        for (var name in obj)
        {
            return false;
        }
        return true;
    };



    dataVisualization = function(response,selecteddrugs) {

        //handle the logic of the single graph

        var allDatasets = []; // for store all points
        var allYAxes = []; // store all yAxis in case there are multiple units

        if ($('#myChart').length > 0) {
            var allGraph = $("#myChart").data('graph');

            for (i in allGraph.data.datasets){
                if (allGraph.data.datasets[i].label == "EEG"){
                    allDatasets.push(allGraph.data.datasets[i]);

                    allYAxes.push({
                        id: 'EEG',
                        position: 'left',
                        scaleLabel: {
                            display: true,
                            fontSize: 14,
                            labelString: 'EEG',
                        },
                        ticks : {
                            min : 0
                        }
                    });
                    break;
                }
            }
        }

        var medInfo = new Map(); // store extra information like route
        if ($('.medPanel').length > 0) { $('.medPanel').remove();}

        for ( d in selecteddrugs){
            var curDrug = drug.get(selecteddrugs[d])
            curDrug.sort(function ( a ,b ) {return a.chartDate > b.chartDate ? 1 : -1;}); // sort based on chartDate

            var datasets = []; // for store points
            var cPoint = [];  // Continuous infusion points
            var iPoint = []; // Intermittent infusion points

            var unit = new Map(); // store units
            var route = new Map(); // store routes

            var yAxes = []; // store yAxis in case there are multiple units


            var MedPanel =
                "    <div class=\" medPanel panel panel-info\" >\n" +
                "      <div class=\"panel-heading\">\n" +
                "        <h4 class=\"panel-title\">\n" +
                "          <a data-toggle=\"collapse\" data-parent=\"showEEG\" href=\"#collapse" + d + "\">" + selecteddrugs[d] +"</a>\n" +
                "        </h4>\n" +
                "      </div>\n" +
                "      <div id=\"collapse" + d +"\" class=\"panel-collapse collapse\">\n" +
                "        <div class=\"panel-body\"><canvas id= \"" + d + "\"  ></canvas></div>\n" +
                "      </div>\n" +
                "    </div>\n";

            $('#medChart').append(MedPanel);

            for (var point in curDrug){
                // skip the redundant points for continuous infusion
                if(cPoint.length>=1 && curDrug[point].dose == cPoint[cPoint.length - 1].y){continue;}

                var time = new moment (curDrug[point].chartDate).format().toString(); // format current data point

                //labels.add(time);
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
                                labelString: selecteddrugs[d] + ' [' + curDrug[point].unit + ']',
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
                                labelString: selecteddrugs[d] + ' [' + curDrug[point].unit + ']',
                            },
                            ticks : {
                                min : 0
                            }
                        })
                    }
                }
            }

            medInfo.set(selecteddrugs[d],route); // store routes for current drug

            if (!isEmpty(cPoint)){  //push continuous infusion points to the dataset
                datasets.push({
                    pointRadius: 2,
                    steppedLine : 'before',
                    yAxisID:selecteddrugs[d] + ' C',
                    label: selecteddrugs[d]+ ' [' + unit.get('Continuous') + ']',
                    borderColor: CColorSet[d % CColorSet.length],
                    borderWidth : 1,
                    fill: false,
                    data: cPoint
                })
            }

            if (!isEmpty(iPoint)){ //push intermittent infusion points to the dataset
                datasets.push({
                    pointRadius: 2,
                    steppedLine : 'before',
                    showLine: false,
                    yAxisID:selecteddrugs[d] + ' I',
                    label:  selecteddrugs[d] + ' [' + unit.get('Intermittent') + ']',
                    borderColor: IColorSet[d % IColorSet.length],
                    borderWidth : 1,
                    data: iPoint
                })
            }

            //setting for chart
            //var timeLabels = Array.from(labels).sort();

            var MedGraph = new Chart($("#" + d ), {
                type : 'line',
                data : {
                    datasets : datasets
                },
                options : {
                    legend:{
                        onClick : function(event, legendItem) {
                            allGraph.resetZoom();

                            var index = legendItem.datasetIndex;
                            var ci = this.chart;
                            var meta = ci.getDatasetMeta(index);
                            meta.hidden = meta.hidden === null? !ci.data.datasets[index].hidden : null;
                            ci.update();
                        }
                    },
                    bezierCurve : true,
                    bezierCurveTension: 1,
                    tooltips: {
                        mode: 'nearest',
                        callbacks: {
                            title: function (item,data) {
                                var label = data.datasets[item[0].datasetIndex].label;
                                var MedName = label.split('[')[0].trim();
                                return 'Time: ' + item[0].xLabel + '\n'+
                                    'Route: '+ medInfo.get(MedName).get(item[0].xLabel);
                            }
                        }
                    },
                    events : [ "mousemove", "touchstart", "touchmove", "touchend", "click"
                    ],
                    scales : {
                        xAxes: [{
                            type: 'time',
                            distribution: 'linear',
                            ticks: {
                                source: 'label'
                            }
                        }],
                        yAxes : yAxes
                    },
                    pan:{
                        enabled:true,
                        mode: 'xy',
                        speed: 1
                    },
                    zoom:{
                        enabled:true,
                        mode: 'x'
                    }
                }
            });

            Array.prototype.push.apply(allDatasets,datasets);
            Array.prototype.push.apply(allYAxes,yAxes);
        }

        $("#saveMedInfo").data('route',medInfo);  // Save medInfo of route


        if($('#myChart').length > 0){
            allGraph.data.datasets = allDatasets;
            allGraph.options.scales.yAxes = allYAxes;
            allGraph.update();
            allGraph.resetZoom();
        }else{
            $('#single_Chart').append('<canvas id="myChart"></canvas>');
            var allGraph = new Chart($("#myChart"), {
                type : 'line',
                data : {
                    datasets : allDatasets
                },
                options : {
                    legend:{
                        onClick : function(event, legendItem) {
                            allGraph.resetZoom();

                            var index = legendItem.datasetIndex;
                            var ci = this.chart;
                            var meta = ci.getDatasetMeta(index);
                            meta.hidden = meta.hidden === null? !ci.data.datasets[index].hidden : null;
                            ci.update();
                        }
                    },
                    bezierCurve : true,
                    bezierCurveTension: 1,
                    tooltips: {
                        mode: 'nearest',
                        callbacks: {
                            title: function (item,data) {
                                var label = data.datasets[item[0].datasetIndex].label
                                if (label == 'EEG'){
                                    return 'Time: ' + item[0].xLabel
                                }else{
                                    var MedName = label.split('[')[0].trim();
                                    return 'Time: ' + item[0].xLabel + '\n'+
                                        'Route: '+ $("#saveMedInfo").data('route').get(MedName).get(item[0].xLabel);
                                }
                            }
                        }
                    },
                    events : [ "mousemove", "touchstart", "touchmove", "touchend", "click"
                    ],
                    scales : {
                        xAxes: [{
                            type: 'time',
                            distribution: 'linear',
                            ticks: {
                                source: 'label'
                            }
                        }],
                        yAxes : allYAxes
                    },
                    pan:{
                        enabled:true,
                        mode: 'xy',
                        speed: 1
                    },
                    zoom:{
                        enabled:true,
                        mode: 'x'
                    }
                }
            });
            $("#myChart").data('graph',allGraph);
        }
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
    });
});