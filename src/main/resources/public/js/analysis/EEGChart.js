$(document).ready(function () {

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

    $("#measure").change(function() {
        if ($("#measure").val() == null) {
            return;
        }
        var form = [$("#measure").val()];
        $.ajax({
            'url': "/api/export/electrode",
            'type': 'post',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                var $electrode = $('#electrode');
                var $predefined = $('#predefined');

                $electrode.empty();
                $predefined.empty();
                $('#column').empty();
                $electrode.append('<option value="" disabled>Single Electrodes</option>');
                $predefined.append('<option value="" disabled>Predefined Sets</option>');

                for (var i = 0; i < data.electrodes.length; i++) {
                    var html = '<option value="' + data.electrodes[i].sid + '">' + data.electrodes[i].electrode +
                        '</option>';
                    $electrode.append(html);
                }

                for (var i = 0; i < data.predefined.length; i++) {
                    var html = '<option value="' + data.predefined[i].value + '">' + data.predefined[i].key + '</option>';
                    $predefined.append(html);
                }
                var predefinedSize = data.predefined.length > 0 ? data.predefined.length + 1 : 2;
                $predefined.attr('size', predefinedSize);
                $electrode.attr('size', 13 - predefinedSize);
            },
            'error': function() {}
        });
    });

    $("#electrode").change(function() {
        var form = {
            "measure": [$("#measure").val()],
            "electrode": $("#electrode").val()
        };
        $("#predefined").val([]);
        $.ajax({
            'url': "/api/export/column",
            'type': 'post',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                var $column = $('#column');
                $column.empty();
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i].column + '">' + data[i].representation + '</option>');
                }
                $column.change();
            },
            'error': function() {}
        });
    });

    $("#predefined").change(function() {
        var form = {
            "measure": [$("#measure").val()],
            "electrode": [$("#predefined").val()]
        };
        $("#electrode").val([]);
        $.ajax({
            'url': "/api/export/column",
            'type': 'post',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                var $column = $('#column');
                $column.empty();
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i].column + '">' + data[i].representation + '</option>');
                }
                $column.change();
            },
            'error': function() {}
        });
    });

    var map = {};
    var eList = [];
    var cList = [];

    $("#addButton").click(function() {
        if ($("#column").val().length == 0) {
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                'Please select at least one option from each\n category to form a valid column group.');
            return;
        }
        map = {};
        eList = [];
        cList = [];
        map.type = $("#measure").val();

        var form = $("#column").val();
        var $columnsInGroup = $('#columnsInGroup');
        var set = new Set();
        $("#columnsInGroup option").each(function() {
            set.add($(this).val());
        });

        $("#column :selected").each(function(i, sel) {
            cList.push($(sel).text());
        });
        map.columns = cList;

        if ($("#predefined").val() != null) {
            eList.push($("#predefined :selected").text());
            map.electrodes = eList;
            if ($("#predefined").val().startsWith('* ')) {
                form.forEach(function(e) {
                    var electrode = $("#predefined").val();
                    var start = electrode.split(' ')[2];
                    var end = electrode.split(' ')[4];
                    for (i = parseInt(start.substring(1)); i <= parseInt(end.substring(1)); i++) {
                        set.add('I' + i + e);
                    }
                });
            } else {
                set.add($("#predefined").val());
            }
        } else {
            $("#electrode :selected").each(function(i, sel) {
                eList.push($(sel).text());
            });
            map.electrodes = eList;
            form.forEach(function(e) {
                var electrode = $("#electrode").val();
                electrode.forEach(function(element) {
                    set.add(element + e);
                });
            });
        }

        $columnsInGroup.empty();
        $columnsInGroup.append('<option value="' + map.type + '">' + map.type + '</option>');
        map.electrodes.forEach(function(e) {
            $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
        });
        map.columns.forEach(function(e) {
            $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
        });
    });

    $("#method").select2({
        width: "100%",
        dropdownCssClass: "custom-dropdown"
    });

    $("#aggregation").select2({
        width: "100%",
        dropdownCssClass: "custom-dropdown"
    });

    $("#period_unit").select2({
        width: "100%",
        dropdownCssClass: "custom-dropdown"
    });

    $("#minBinRowUnit").select2({
        width: "100%",
        dropdownCssClass: "custom-dropdown"
    });


    $("#ShowEEGButton").click(function() {

        if ($('#aggregation-form')[0].checkValidity()) {

            if (map.type == null) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Please add at least one column to the final aggregation group list.');
                return false;
            }

            $("#eeg-modal").modal('hide');

            var form = {
                "id": $("#patientId").text(),
                "downsample": $("#method").val(),
                "aggregation": $("#aggregation").val(),
                "downsample_first":$('#downsample_first label.active input').val() == "true" ? true : false,
                "period":$("#period").val(),
                "period_unit":$("#period_unit").val(),
                "min_bin_row":$("#min_bin_row").val(),
                "minBinRowUnit":$("#minBinRowUnit").val(),
                "columns": JSON.stringify(map),
                "AR":$('#ARFile label.active input').val() == "true" ? true : false
            };


            // $.ajax({
            //     'url': "/analysis/getPatientMedInfoById/" + $("#patientId").text(),  // modify the URL
            //     'type': requestMethod,
            //     'data': JSON.stringify(form),
            //     'contentType': "application/json",
            //     'dataType': 'json',
            //     'success': function(text) {
            //         $("#eeg-modal").modal('hide');
            //         response = text.data;
            //     },
            //     'error': function() {}
            // });

            console.log(form);
            var allDatasets = []; // for store all points
            var allLabels = []; // store all timestamp for chart
            var allYAxes = []; // store all yAxis in case there are multiple units

            if ($('#myChart').length > 0) {
                var allGraph = $("#myChart").data('graph');
                var medInfo = $("#myChart").data('medInfo');
                for (i in allGraph.data.datasets){
                    if (allGraph.data.datasets[i].label != "EEG"){
                        allDatasets.push(allGraph.data.datasets[i]);
                        for (j in allDatasets.data){
                            allLabels.push(allDatasets.data[j].x);
                        }
                    }
                }
                allYAxes = allGraph.options.scales.yAxes;
            }

            var EEGData = [];
            var EEGlabels = [];
            var EEGYAxes = [];
            var EEGdatasets = [];

            //Test data

            EEGData.push({
                x: "2010-05-27T13:57:00-04:00",
                y: 10
            });  // used for store EEG data
            EEGData.push({
                x: "2010-05-28T15:57:00-04:00",
                y: 20
            });  // used for store EEG data

            EEGlabels.push("2010-05-27T13:57:00-04:00");
            EEGlabels.push("2010-05-28T15:57:00-04:00");
            EEGlabels = EEGlabels.sort();


            EEGdatasets.push({
                pointRadius: 2,
                steppedLine : 'before',
                yAxisID: 'EEG',
                label: 'EEG',
                borderColor: ['rgba(200, 5, 5, 1)'],
                backgroundColor : [ 'rgba(200, 5, 5, 0.2)'],
                borderWidth : 1,
                fill: true,
                data: EEGData
            });

            EEGYAxes.push({
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

            //Test data end

            Array.prototype.push.apply(allDatasets,EEGdatasets);
            Array.prototype.push.apply(allLabels,EEGlabels);
            Array.prototype.push.apply(allYAxes,EEGYAxes);

            if ($('#myChart').length > 0){
                allGraph.data.datasets = allDatasets;
                allGraph.data.labels = allLabels;
                allGraph.update()
            }else{
                $('#single_Chart').append('<canvas id="myChart"></canvas>');
                var allTimeUnit = (new Date(allLabels[allLabels.length-1]) - new Date(allLabels[0]) > 24*60*60*1000) ? 'day':'hour';
                var allGraph = new Chart($("#myChart"), {
                    type : 'line',
                    data : {
                        labels : allLabels,
                        datasets : allDatasets
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
                                    if (label == 'EEG'){
                                        return 'Time: ' + item[0].xLabel
                                    }else{
                                        var MedName = label.split('[')[0].trim()
                                        return 'Time: ' + item[0].xLabel + '\n'+
                                            'Route: '+ $("#saveMedInfo").data('route').get(MedName).get(item[0].xLabel);
                                    }
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
                                    unit: allTimeUnit,
                                }
                            }],
                            yAxes : allYAxes
                        }
                    }
                });
                $("#myChart").data('graph',allGraph);
            }


            //Handle stack representation
            if($("#EEGChart").length > 0){
                $("#EEGChart").data('graph').update();
            }else{
                var EEGPanel =
                    "      <div class=\"panel-heading\">\n" +
                    "        <h4 class=\"panel-title\">\n" +
                    "          <a data-toggle=\"collapse\" data-parent=\"showEEG\" href=\"#collapseEEG\"> EEG Data </a>\n" +
                    "        </h4>\n" +
                    "      </div>\n" +
                    "      <div id=\"collapseEEG\" class=\"panel-collapse collapse\">\n" +
                    "        <div class=\"panel-body\"><canvas id= \"EEGChart\"></canvas></div>\n" +
                    "      </div>\n";

                $('#EEG_Chart_Container').append(EEGPanel);
                $('#EEG_Chart_Container').addClass("panel panel-info");


                var timeUnit = (new Date(EEGlabels[EEGlabels.length-1]) - new Date(EEGlabels[0]) > 24*60*60*1000) ? 'day':'hour';

                var eegGraph = new Chart($("#EEGChart"), {
                    type : 'line',
                    data : {
                        labels : EEGlabels,
                        datasets : EEGdatasets
                    },
                    options : {
                        elements: {
                            line: {
                                steppedLine : 'before'
                            }
                        },
                        tooltips: {
                            callbacks: {
                                title: function (item,data) {
                                    return 'Time: ' + item[0].xLabel
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
                                    unit: timeUnit
                                }
                            }],
                            yAxes : EEGYAxes
                        }
                    }
                });
                $("#EEGChart").data('graph',eegGraph);
            }


            // for (point in response){
            //     time = new moment (response[point].EEGtime).format().toString(); // format current data point
            //     EEGlabels.add(time);
            // }



            //clear the canvas
            // if ($('#eegGraph')) { $('#eegGraph').remove();}
            // $('#EEG_Chart').append('<canvas id="eegGraph"></canvas>');

            //     var myChart = new Chart($("#eegGraph" ), {
            //         type : 'line',
            //         data : {
            //             labels : timeLabels,
            //             datasets : datasets
            //         },
            //         options : {
            //             elements: {
            //                 line: {
            //                     steppedLine : 'before',
            //                 }
            //             },
            //             tooltips: {
            //                 callbacks: {
            //                     title: function (item,data) {
            //                         var label = data.datasets[item[0].datasetIndex].label
            //                         var MedName = label.split('(')[0].trim()
            //                         return 'Time: ' + item[0].xLabel + '\n'+
            //                             'Route: '+ medInfo.get(MedName).get(item[0].xLabel);
            //                     }
            //                 }
            //             },
            //             events : [ "mousemove", "touchstart", "touchmove", "touchend", "click"
            //             ],
            //             onClick : function() {$("#collapseExample").collapse('toggle');},
            //             scales : {
            //                 xAxes: [{
            //                     type: 'time',
            //                     distribution: 'linear',
            //                     ticks: {
            //                         source: 'label'
            //                     },
            //                     time: {
            //                         unit: timeUnit,
            //                     }
            //                 }],
            //                 yAxes : yAxes
            //             }
            //         }
            //     });
            // }
            return false;
        }else{
            console.log("invalid form");
            return true;
        }
    });
});
