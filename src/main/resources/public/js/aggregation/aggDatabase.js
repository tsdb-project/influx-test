$(document).ready(function() {
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

    /*
    * initialize the data
    * */

    var patientList = [];
    var patientsFinalList = null;

    var aggMethod = {
        Max:false,
        Min:false,
        Mean:false,
        Median:false,
        Std:false,
        FQ:false,
        TQ:false,
        Sum:false
    };
    var aggFinalMethod = null
    ;

    /*
    *  select columns modal
    * */
    // $("#measure").change(function() {
    //     if ($("#measure").val() == null) {
    //         return;
    //     }
    //     var form = [$("#measure").val()];
    //     $.ajax({
    //         'url': "/api/export/electrode",
    //         'type': 'post',
    //         'data': JSON.stringify(form),
    //         'contentType': "application/json",
    //         'dataType': 'json',
    //         'success': function(data) {
    //             var $electrode = $('#electrode');
    //             var $predefined = $('#predefined');
    //
    //             $electrode.empty();
    //             $predefined.empty();
    //             $('#column').empty();
    //             $electrode.append('<option value="" disabled>Single Electrodes</option>');
    //             $predefined.append('<option value="" disabled>Predefined Sets</option>');
    //
    //             for (var i = 0; i < data.electrodes.length; i++) {
    //                 var html = '<option value="' + data.electrodes[i].sid + '">' + data.electrodes[i].electrode +
    //                     '</option>';
    //                 $electrode.append(html);
    //             }
    //
    //             for (var i = 0; i < data.predefined.length; i++) {
    //                 var html = '<option value="' + data.predefined[i].value + '">' + data.predefined[i].key + '</option>';
    //                 $predefined.append(html);
    //             }
    //             var predefinedSize = data.predefined.length > 0 ? data.predefined.length + 1 : 2;
    //             $predefined.attr('size', predefinedSize);
    //             $electrode.attr('size', 13 - predefinedSize);
    //         },
    //         'error': function() {}
    //     });
    // });
    //
    // $("#electrode").change(function() {
    //     var form = {
    //         "measure": [$("#measure").val()],
    //         "electrode": $("#electrode").val()
    //     };
    //     $("#predefined").val([]);
    //     $.ajax({
    //         'url': "/api/export/column",
    //         'type': 'post',
    //         'data': JSON.stringify(form),
    //         'contentType': "application/json",
    //         'dataType': 'json',
    //         'success': function(data) {
    //             var $column = $('#column');
    //             $column.empty();
    //             for (var i = 0; i < data.length; i++) {
    //                 $column.append('<option value="' + data[i].column + '">' + data[i].representation + '</option>');
    //             }
    //             $column.change();
    //         },
    //         'error': function() {}
    //     });
    // });
    //
    // $("#predefined").change(function() {
    //     var form = {
    //         "measure": [$("#measure").val()],
    //         "electrode": [$("#predefined").val()]
    //     };
    //     $("#electrode").val([]);
    //     $.ajax({
    //         'url': "/api/export/column",
    //         'type': 'post',
    //         'data': JSON.stringify(form),
    //         'contentType': "application/json",
    //         'dataType': 'json',
    //         'success': function(data) {
    //             var $column = $('#column');
    //             $column.empty();
    //             for (var i = 0; i < data.length; i++) {
    //                 $column.append('<option value="' + data[i].column + '">' + data[i].representation + '</option>');
    //             }
    //             $column.change();
    //         },
    //         'error': function() {}
    //     });
    // });
    //
    // var map = {};
    // var eList = [];
    // var cList = [];
    //
    // $("#addButton").click(function() {
    //     if ($("#column").val().length == 0) {
    //         notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
    //             'Please select at least one option from each\n category to form a valid column group.');
    //         return;
    //     }
    //     map = {};
    //     eList = [];
    //     cList = [];
    //     map.type = $("#measure").val();
    //
    //     var form = $("#column").val();
    //     var $columnsInGroup = $('#columnsInGroup');
    //     var set = new Set();
    //     $("#columnsInGroup option").each(function() {
    //         set.add($(this).val());
    //     });
    //
    //     $("#column :selected").each(function(i, sel) {
    //         cList.push($(sel).text());
    //     });
    //     map.columns = cList;
    //
    //     if ($("#predefined").val() != null) {
    //         eList.push($("#predefined :selected").text());
    //         map.electrodes = eList;
    //         if ($("#predefined").val().startsWith('* ')) {
    //             form.forEach(function(e) {
    //                 var electrode = $("#predefined").val();
    //                 var start = electrode.split(' ')[2];
    //                 var end = electrode.split(' ')[4];
    //                 for (i = parseInt(start.substring(1)); i <= parseInt(end.substring(1)); i++) {
    //                     set.add('I' + i + e);
    //                 }
    //             });
    //         } else {
    //             set.add($("#predefined").val());
    //         }
    //     } else {
    //         $("#electrode :selected").each(function(i, sel) {
    //             eList.push($(sel).text());
    //         });
    //         map.electrodes = eList;
    //         form.forEach(function(e) {
    //             var electrode = $("#electrode").val();
    //             electrode.forEach(function(element) {
    //                 set.add(element + e);
    //             });
    //         });
    //     }
    //
    //     $columnsInGroup.empty();
    //     $columnsInGroup.append('<option value="' + map.type + '">' + map.type + '</option>');
    //     map.electrodes.forEach(function(e) {
    //         $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
    //     });
    //     map.columns.forEach(function(e) {
    //         $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
    //     });
    // });

    var columnsList = [];
    var columnsFinalList = null;
    var columnsFinal = $('#columnsInGroup');

    $("#addButton").click(function() {
        columnsList = [];

        if ($("#measure").val().length == 0) {
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                'Please select at least one option from each\n category to form a valid column group.');
            return;
        }

        $("#measure :selected").each(function(i, sel) {
            columnsList.push($(sel).text());
        });

        columnsFinal.empty();
        columnsList.forEach(function(e) {
            columnsFinal.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
        });
    });

    $("#saveColumns").click(function() {
        notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut", 'column list Saved.');
      columnsFinalList = columnsList;
    });


    /*
    * test data
    * */

    var databaseData = [{
        id:1,
        DBname: "test",
        aggregationLevel: "1m",
        ar:true,
        columnList:["I1_1"],
        patientList:["PUH-2019-015"],
        origin: 0,
        duration: 0,
        max:aggMethod.Max,
        min:aggMethod.Min,
        mean:aggMethod.Mean,
        median:true,
        std:true,
        fq:aggMethod.FQ,
        tq:aggMethod.TQ,
        sum:aggMethod.Sum,
        lastUpdate:Date.now(),
        autoUpdate:true
    }];

    /*
    * aggregation methods
    * */

    var maxBtn = $('#MaxBtn');
    var minBtn = $('#MinBtn');
    var meanBtn = $('#MeanBtn');
    var medianBtn = $('#MedianBtn');
    var stdBtn = $('#StdBtn');
    var fqBtn = $('#FQBtn');
    var tqBtn = $('#TQBtn');
    var sumBtn = $('#SumBtn');

    $('#agg-method-modal').on('show.bs.modal', function(event) {

        if(aggMethod.Max){
            maxBtn.removeClass("btn-light");
            maxBtn.addClass("btn-primary");
        }else{
            maxBtn.addClass("btn-light");
            maxBtn.removeClass("btn-primary");
        }

        if(aggMethod.Min){
            minBtn.removeClass("btn-light");
            minBtn.addClass("btn-primary");
        }else{
            minBtn.addClass("btn-light");
            minBtn.removeClass("btn-primary");
        }

        if(aggMethod.Mean){
            meanBtn.removeClass("btn-light");
            meanBtn.addClass("btn-primary");
        }else{
            meanBtn.addClass("btn-light");
            meanBtn.removeClass("btn-primary");
        }

        if(aggMethod.Median){
            medianBtn.removeClass("btn-light");
            medianBtn.addClass("btn-primary");
        }else{
            medianBtn.addClass("btn-light");
            medianBtn.removeClass("btn-primary");
        }

        if(aggMethod.Std){
            stdBtn.removeClass("btn-light");
            stdBtn.addClass("btn-primary");
        }else{
            stdBtn.addClass("btn-light");
            stdBtn.removeClass("btn-primary");
        }

        if(aggMethod.FQ){
            fqBtn.removeClass("btn-light");
            fqBtn.addClass("btn-primary");
        }else{
            fqBtn.addClass("btn-light");
            fqBtn.removeClass("btn-primary");
        }

        if(aggMethod.TQ){
            tqBtn.removeClass("btn-light");
            tqBtn.addClass("btn-primary");
        }else{
            tqBtn.addClass("btn-light");
            tqBtn.removeClass("btn-primary");
        }

        if(aggMethod.Sum){
            sumBtn.removeClass("btn-light");
            sumBtn.addClass("btn-primary");
        }else{
            sumBtn.addClass("btn-light");
            sumBtn.removeClass("btn-primary");
        }
    });

    maxBtn.click(function () {
        if(! aggMethod.Max){
            aggMethod.Max = true;
            maxBtn.removeClass("btn-light");
            maxBtn.addClass("btn-primary");
        }else{
            aggMethod.Max = false;
            maxBtn.addClass("btn-light");
            maxBtn.removeClass("btn-primary");
        }
    });

    minBtn.click(function () {
        if(! aggMethod.Min){
            aggMethod.Min = true;
            minBtn.removeClass("btn-light");
            minBtn.addClass("btn-primary");
        }else{
            aggMethod.Min = false;
            minBtn.addClass("btn-light");
            minBtn.removeClass("btn-primary");
        }
    });

    meanBtn.click(function () {
        if(! aggMethod.Mean){
            aggMethod.Mean = true;
            meanBtn.removeClass("btn-light");
            meanBtn.addClass("btn-primary");
        }else{
            aggMethod.Mean = false;
            meanBtn.addClass("btn-light");
            meanBtn.removeClass("btn-primary");
        }
    });

    medianBtn.click(function () {
        if(! aggMethod.Median){
            aggMethod.Median = true;
            medianBtn.removeClass("btn-light");
            medianBtn.addClass("btn-primary");
        }else{
            aggMethod.Median = false;
            medianBtn.addClass("btn-light");
            medianBtn.removeClass("btn-primary");
        }
    });

    stdBtn.click(function () {
        if(! aggMethod.Std){
            aggMethod.Std = true;
            stdBtn.removeClass("btn-light");
            stdBtn.addClass("btn-primary");
        }else{
            aggMethod.Std = false;
            stdBtn.addClass("btn-light");
            stdBtn.removeClass("btn-primary");
        }
    });

    fqBtn.click(function () {
        if(! aggMethod.FQ){
            aggMethod.FQ = true;
            fqBtn.removeClass("btn-light");
            fqBtn.addClass("btn-primary");
        }else{
            aggMethod.FQ = false;
            fqBtn.addClass("btn-light");
            fqBtn.removeClass("btn-primary");
        }
    });

    tqBtn.click(function () {
        if(! aggMethod.TQ){
            aggMethod.TQ = true;
            tqBtn.removeClass("btn-light");
            tqBtn.addClass("btn-primary");
        }else{
            aggMethod.TQ = false;
            tqBtn.addClass("btn-light");
            tqBtn.removeClass("btn-primary");
        }
    });

    sumBtn.click(function () {
        if(! aggMethod.Sum){
            aggMethod.Sum = true;
            sumBtn.removeClass("btn-light");
            sumBtn.addClass("btn-primary");
        }else{
            aggMethod.Sum = false;
            sumBtn.addClass("btn-light");
            sumBtn.removeClass("btn-primary");
        }
    });

    $('#saveAggMethod').click(function () {
        notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut", 'aggregation methods Saved.');
        aggFinalMethod = aggMethod;
    });

    /*
    * get database information
    * */

    function getDatabases(){
        // $.ajax({
        //     'url': "/aggregation/getDBs",
        //     'type': 'get',
        //     'success': function(data) {
        //         databaseData = data.data
        //     },
        //     'error': function() {}
        // });
    }

    $.fn.dataTable.moment('M/D/YYYY, hh:mm:ss a');
    var table = $('#databaseTable').DataTable({
        data : databaseData,
        columnDefs : [ {
            "targets" : [ 0
            ],
            "searchable" : false
        }
        ],
        columns : [ {
            data : 'id'
        }, {
            data : 'DBname'
        }, {
            data : 'aggregationLevel'
        }, {
            data : null,
            render : function(data) {
                string = "";
                if (data.max){string += "Max "}
                if (data.min){string += "Min "}
                if (data.mean){string += "Mean "}
                if (data.median){string += "Median "}
                if (data.std){string += "Std "}
                if (data.fq){string += "25%ile "}
                if (data.tq){string += "75%ile "}
                if (data.sum){string += "Sum"}
                return string;
            }
        },{
            data : null,
            render : function(data) {
                return data.ar ? "AR" : "NOAR";
            }
        }, {
            data : null,
            render : function(data) {
                return data.columnList.length;
            }
        }, {
            data : null,
            render : function(data) {
                return data.patientList.length;
            }
        }, {
            data : null,
            render : function(data) {
                return localeDateString(data.lastUpdate)
            }
        }, {
            data : null,
            render : function(data) {
                html = '<div class="btn-demo">';
                html += '<button class="btn btn-info btn-sm" data-toggle="modal" data-target="#DB-details-modal" data-id="' + data.id + '"><i class="zmdi zmdi-edit"></i> Details</button>'
                html += '</div>';
                return html
            }
        },{
            data : null,
            render : function(data) {
                html = '<div class="btn-demo">';
                if (data.autoUpdate) {
                    html += '<button class="btn btn-light btn-sm" data-toggle="modal" data-target="#toggle-disable-modal" data-id="' + data.id + '"><i class="zmdi zmdi-block"></i> Disable</button>'
                } else {
                    html += '<button class="btn btn-light btn-sm" data-toggle="modal" data-target="#toggle-enable-modal" data-id="' + data.id + '"><i class="zmdi zmdi-arrow-right"></i> Enable</button>'
                }
                html += '</div>';
                return html
            }
        }
        ]
    });

    /*
    * Enable / disable database auto update
    * */

    $("#enable-user-button").click(function() {
        var id = $(this).attr('data-id');
        var enable = true;
        // $.ajax({
        //     'url': "/aggregation/toggle_enable/" + id,
        //     'type': 'patch',
        //     'data': JSON.stringify(enable),
        //     'contentType': "application/json",
        //     'dataType': 'json',
        //     'success': function(data) {
        //         getDatabases()
        //         table.ajax.reload();
        //     },
        //     'error': function() {}
        // });
    });


    $("#disable-user-button").click(function() {
        var id = $(this).attr('data-id');
        var enable = false;
        // $.ajax({
        //     'url': "/aggregation/toggle_enable/" + id,
        //     'type': 'patch',
        //     'data': JSON.stringify(enable),
        //     'contentType': "application/json",
        //     'dataType': 'json',
        //     'success': function(data) {
        //         getDatabases()
        //         table.ajax.reload();
        //     },
        //     'error': function() {}
        // });
    });

    /*
    *  update patient modal
    * */

    $("#uploadPatientList").change(function() {
        patientList = null;
        var formData = new FormData();
        formData.append("plist", document.getElementById('uploadPatientList').files[0]);
        $.ajax({
            type: "POST",
            url: "/api/export/patient_list/",
            data: formData,
            contentType: false,
            cache: false,
            processData: false,
            success: function(result) {
                if (result.code == 1) {
                    notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
                        'Successfully uploaded patient list.');
                    patientList = result.data;
                    console.log(patientList);
                } else {
                    notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                        'Failed to upload patient list.');
                }
            },
            error: function(result) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Failed to upload patient list.');
            }
        });
    });

    $("#clear-patient-list").click(function() {
        notify("top", "center", null, "info", "animated bounceIn", "animated fadeOut", 'Patient list cleared.');
        $("#uploadPatientList").val('');
        patientList = null;
    });

    $("#savePatientList").click(function() {
        notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut", 'Patient list Saved.');
        patientsFinalList = patientList;
    });

    $("#upload-patient-modal").on('hidden.bs.modal', function (e) {
        $("#uploadPatientList").val('');
    });

    /*
    * add new database
    * */

    $("#createButton").click(function () {
        $("#createButton").attr('disabled', 'disabled');
        var newDB = {
            DBname: $("#alias").val(),
            aggregationLevel: $('#period').val() * $('#period_unit').val(),
            ar:$('#ar label.active input').val() == "true",
            columnList:columnsFinalList == null ? "ALL": columnsFinalList,
            patientList: patientsFinalList== null ? "ALL" : patientsFinalList,
            origin: $("#origin").val() * $("#origin_unit").val(),
            duration: $("#duration").val() * $("#duration_unit").val(),
            Max:aggFinalMethod == null ? true : aggFinalMethod.Max,
            Min:aggFinalMethod == null ? true : aggFinalMethod.Min,
            Mean:aggFinalMethod == null ? true : aggFinalMethod.Mean,
            Median:aggFinalMethod == null ? true : aggFinalMethod.Median,
            Std:aggFinalMethod == null ? true : aggFinalMethod.Std,
            FQ:aggFinalMethod == null ? true : aggFinalMethod.FQ,
            TQ:aggFinalMethod == null ? true : aggFinalMethod.TQ,
            Sum:aggFinalMethod == null ? true : aggFinalMethod.Sum,
            lastUpdate:Date.now(),
            autoUpdate:true
        };

        console.log(newDB);
        // $.ajax({
        //     url: "/aggregation/createDB/",
        //     type: 'post',
        //     data: JSON.stringify(form),
        //     contentType: "application/json",
        //     dataType: 'json',
        //     success: function () {
        //         getDatabases();
        //         table.ajax.reload();
        //         notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
        //             'Successfully create database setting.');
        //     },
        //     error: function () {
        //         notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
        //             'Failed to submit this job, please try again.');
        //     }
        // });
    });

    /*
    * Show details modal
    * */

    $('#DB-details-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var id = button.data('id')-1;
        var plist = databaseData[id].patientList;
        var clist = databaseData[id].columnList;

        $('#patients').empty();
        plist.forEach(function (value) {
            $('#patients').append('<option value="' + value + '">&nbsp&nbsp&nbsp&nbsp' + value + '</option>');
        });

        $('#columns').empty();
        clist.forEach(function (value) {
            $('#columns').append('<option value="' + value + '">&nbsp&nbsp&nbsp&nbsp' + value + '</option>');
        })

    });

    /*
    * update the progress
    * */
    $("#finished").show();

    // var update = setInterval(function() {
    //     $.ajax({
    //         'url' : "/aggregation/progress",
    //         'success' : function(data) {
    //             var progressHtml = "";
    //             for (i = 0; i < data.progress.length; i++) {
    //                 var progress = (data.progress[i].percent * 100).toFixed(2);
    //                 var color = "";
    //                 if (data.progress[i].status == "STATUS_FINISHED") {
    //                     color = " bg-success";
    //                 } else if (data.progress[i].status == "STATUS_FAIL") {
    //                     color = " bg-danger";
    //                 }
    //                 progressHtml += "<div class=\"progress\"><div class=\"progress-bar" + color + "\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
    //                     + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.progress[i].filename + ' - ' + progress + "%</small><br><br>";
    //             }
    //
    //             var totalPercent = (data.total * 100).toFixed(2);
    //             $("#totalProgress").attr("style", "width: " + totalPercent + "%");
    //             $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
    //             $("#totalPercent").html(totalPercent + "%");
    //
    //             $("#databasesProgress").html(progressHtml);
    //
    //             if (totalPercent == 100.00) {
    //                 clearInterval(update);
    //                 if (data.progress.length == 0) {
    //                     $("#running").hide();
    //                     $("#finished").show();
    //                 }
    //             } else {
    //                 $("#running").show();
    //                 $("#finished").hide();
    //             }
    //         },
    //         'error' : function() {
    //             clearInterval(update);
    //         }
    //     });
    // }, 2000);

    function localeDateString(date) {
        var options = {
            hour12 : true,
            timeZone : "America/New_York"
        };
        return new Date(date).toLocaleString('en-US', options);
    }

});