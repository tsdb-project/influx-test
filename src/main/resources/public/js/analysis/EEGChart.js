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

            if ($('#downsample_first label.active input').val() == null) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Please choose downsample first or aggregation first.');
                return false;
            }

            if ($('#ARFile label.active input').val() == null) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Please choose AR or NOAR file.');
                return false;
            }

            if (map.type == null) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Please add at least one column to the final aggregation group list.');
                return false;
            }

            var form = {
                "patientID": $("#patientId").text(),
                "downsampleMethod": $("#method").val(),
                "aggregationMethod": $("#aggregation").val(),
                "downsampleFirst":$('#downsample_first label.active input').val() == "true" ? true : false,
                "period":$("#period").val() * $("#period_unit").val(),
                "minBinRow":$("#min_bin_row").val() * $("#minBinRowUnit").val(),
                "columns": JSON.stringify(map),
                "ar":$('#ARFile label.active input').val() == "true" ? true : false,
                "username":$('#user_name').html()
            };


            var EEGData = [];
            var EEGYAxes = [];
            var EEGdatasets = [];

            $.ajax({
                'url': "/analysis/eegChart",  // modify the URL
                'type': 'POST',
                'async': false,
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(text) {
                    $("#eeg-modal").modal('hide');
                    eegResponse = text.data;
                },
                'error': function() {}
            });


            for (i in eegResponse){
                EEGData.push({
                    x: eegResponse[i][0],
                    y: eegResponse[i][1]
                });
            }

            var allDatasets = []; // for store all points
            var allYAxes = []; // store all yAxis in case there are multiple units

            if ($('#myChart').length > 0) {
                var allGraph = $("#myChart").data('graph');
                for (i in allGraph.data.datasets){
                    if (allGraph.data.datasets[i].label != "EEG"){
                        allDatasets.push(allGraph.data.datasets[i]);
                    }
                }
                allYAxes = allGraph.options.scales.yAxes;
            }




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


            Array.prototype.push.apply(allDatasets,EEGdatasets);
            Array.prototype.push.apply(allYAxes,EEGYAxes);


            if ($('#myChart').length > 0){
                allGraph.data.datasets = allDatasets;
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
                                        var MedName = label.split('[')[0].trim()
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


            //Handle stack representation
            if($("#EEGChart").length > 0){
                $("#EEGChart").data('graph').data.datasets = EEGdatasets;
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


                var eegGraph = new Chart($("#EEGChart"), {
                    type : 'line',
                    data : {
                        datasets : EEGdatasets
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
                            callbacks: {
                                mode: 'nearest',
                                title: function (item,data) {
                                    return 'Time: ' + item[0].xLabel
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
                            yAxes : EEGYAxes
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
                $("#EEGChart").data('graph',eegGraph);
            }

            return false;
        }else{
            console.log("invalid form");
            return true;
        }
    });
});
