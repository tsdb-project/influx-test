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
    * get database information
    * */
    var databaseData = null;

    function getDatabases(){
        $.ajax({
            'url': "/aggregation/getDBs",
            'type': 'get',
            'contentType':"application/json",
            'dataType':"json",
            'async': false,
            'success': function(data) {
                databaseData = data.data;
            },
            'error': function() {}
        });
    }
    getDatabases();

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
            data : 'dbName'
        }, {
            data : null,
            render : function(data) {
                var time = data.aggregateTime;
                var string = "";
                var h = 3600;
                var m = 60;
                if(time % h==0){
                    return time/h+"h";
                }else {
                    if(Math.floor(time / h ) > 0){
                        string += Math.floor(time / h ) + "h"
                    }
                    time %= h;

                }
                if(time % m==0){
                    return time/m+"m";
                }else{
                    if(Math.floor(time / m ) > 0){
                        string += Math.floor(time / m ) + "m"
                    }
                    time %= m;
                }
                string += time + "s";

                return string;
            }
        }, {
            data : null,
            render : function(data) {
                string = "";
                if (data.max){string += "Max "}
                if (data.min){string += "Min "}
                if (data.mean){string += "Mean "}
                if (data.median){string += "Median "}
                if (data.sd){string += "Std "}
                if (data.q1){string += "25%ile "}
                if (data.q3){string += "75%ile "}
                if (data.sum){string += "Sum"}
                return string;
            }
        },{
            data : null,
            render : function(data) {
                return data.artype ? "AR" : "NOAR";
            }
        }, {
            data : null,
            render : function(data) {
                return localeDateString(data.createTime)
            }
        }, {
            data : null,
            render : function(data, type, row, meta) {
                html = '<div class="btn-demo">';
                html += '<button class="btn btn-info btn-sm" data-toggle="modal"  id = "showDetaildBtn" data-target="#DB-details-modal" data-row="' + meta.row + '"><i class="zmdi zmdi-edit"></i> Details</button>'
                html += '</div>';
                return html
            }
        },{
            data : null,
            render : function(data, type, row, meta) {
                html = '<div class="btn-demo">';
                if (data.autoUpdate) {
                    html += '<button class="btn btn-light btn-sm" data-toggle="modal" data-target="#toggle-disable-modal" data-row="' + meta.row + '"><i class="zmdi zmdi-block"></i> Disable</button>'
                } else {
                    html += '<button class="btn btn-light btn-sm" data-toggle="modal" data-target="#toggle-enable-modal" data-row="' + meta.row + '"><i class="zmdi zmdi-arrow-right"></i> Enable</button>'
                }
                html += '</div>';
                return html
            }
        },{
            data : null,
            render : function(data, type, row, meta) {
                html = '<div class="btn-demo">';
                html += '<button class="btn btn-light btn-sm integrityCheckBtn"  data-row="' + meta.row + '"><i class="zmdi zmdi-arrow-right"></i> Check</button>';
                html += '</div>';
                return html
            }
        }
        ]
    });

    /*
    * Enable / disable database auto update
    * */

    // $("#enable-user-button").click(function() {
    //     var id = $(this).attr('data-id');
    //     var enable = true;
    //     $.ajax({
    //         'url': "/aggregation/toggle_enable/" + id,
    //         'type': 'patch',
    //         'data': JSON.stringify(enable),
    //         'contentType': "application/json",
    //         'dataType': 'json',
    //         'success': function(data) {
    //             getDatabases();
    //             table.ajax.reload();
    //         },
    //         'error': function() {}
    //     });
    // });


    // $("#disable-user-button").click(function() {
    //     var id = $(this).attr('data-id');
    //     var enable = false;
    //     $.ajax({
    //         'url': "/aggregation/toggle_enable/" + id,
    //         'type': 'patch',
    //         'data': JSON.stringify(enable),
    //         'contentType': "application/json",
    //         'dataType': 'json',
    //         'success': function(data) {
    //             getDatabases()
    //             table.ajax.reload();
    //         },
    //         'error': function() {}
    //     });
    // });

    /*
    * Integrity check
    * */

    $(".integrityCheckBtn").click(function() {
        var id = $(this).attr('data-row');
        console.log(databaseData[id]);
        $.ajax({
            'url': "/aggregation/checkIntegrity/",
            'type': 'POST',
            'data': JSON.stringify(databaseData[id]),
            'contentType': "application/json",
            dataType: 'json',
            success: function (response) {
                notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
                    'Integrity check complete');
            },
            error: function () {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Integrity check Failed.');
            }
        });
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
            dbName: $("#alias").val(),
            aggregateTime: $('#period').val() * $('#period_unit').val(),
            artype:$('#ar label.active input').val() == "true",
            columns:columnsFinalList == null ? null: columnsFinalList.toString(),
            pidList: patientsFinalList == null ? null: patientsFinalList.toString(),
            max:aggFinalMethod == null ? true : aggFinalMethod.Max,
            min:aggFinalMethod == null ? true : aggFinalMethod.Min,
            mean:aggFinalMethod == null ? true : aggFinalMethod.Mean,
            median:aggFinalMethod == null ? true : aggFinalMethod.Median,
            sd:aggFinalMethod == null ? true : aggFinalMethod.Std,
            q1:aggFinalMethod == null ? true : aggFinalMethod.FQ,
            q3:aggFinalMethod == null ? true : aggFinalMethod.TQ,
            sum:aggFinalMethod == null ? true : aggFinalMethod.Sum,
            fromDb:$("#databases").val(),
            total:1,
            finished:0,
            autoUpdate:true
        };

        $.ajax({
            url: "/aggregation/newDB/",
            type: 'post',
            data: JSON.stringify(newDB),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                if(response.code == 1){
                    notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
                        'Successfully create database setting.');
                }else{
                    notify("top", "center", null, "failed", "animated bounceIn", "animated fadeOut",
                        ' Create database setting failed.');
                }
            },
            error: function () {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Failed to submit this job, please try again.');
            }
        });
    });

    /*
    * Integrity check
    **/
    // $("#integrityCheckButton").click(function () {
    //     $.ajax({
    //         url: "/aggregation/checkIntegrity/",
    //         type: 'get',
    //         contentType: "application/json",
    //         dataType: 'json',
    //         success: function (response) {
    //             notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
    //             'Integrity check complete');
    //         },
    //         error: function () {
    //             notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
    //                 'Integrity check Failed.');
    //         }
    //     });
    // });

    /*
    * Show details modal
    * */


    $('#DB-details-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var id = button.data('row');
        var plist = databaseData[id].patientList;
        var clist = databaseData[id].columns;

        $('#patients').empty();
        if (plist == null){
            $('#patients').append("<option >ALL</option>");
        }else{
            plist.forEach(function (value) {
                $('#patients').append('<option value="' + value + '">&nbsp&nbsp&nbsp&nbsp' + value + '</option>');
            });
        }


        $('#columns').empty();
        if(clist == null){
            $('#columns').append("<option>ALL</option>");
        }else {
            clist.forEach(function (value) {
                $('#columns').append('<option value="' + value + '">&nbsp&nbsp&nbsp&nbsp' + value + '</option>');
            })
        }

    });

    /*
    * update the progress
    * */

    var update = setInterval(function() {
        $.ajax({
            'url' : "/aggregation/process",
            'success' : function(data) {
                if(data.data.length > 0){
                    var progressHtml = "";
                    var finishedOverall = 0;
                    var totalOverall = 0;

                    for (i = 0; i < data.data.length; i++) {
                        var progress = (data.data[i].finished / data.data[i].total * 100).toFixed(2);
                        finishedOverall += data.data[i].finished;
                        totalOverall += data.data[i].total;
                        var color = "";
                        if (data.data[i].status == "success") {
                            color = " bg-success";
                        } else if (data.data[i].status == "failed") {
                            color = " bg-danger";
                        }
                        progressHtml += "<div class=\"progress\"><div class=\"progress-bar" + color + "\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                            + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.data[i].dbName + ' - ' + progress + "%</small><br><br>";
                    }

                    var totalPercent = (finishedOverall / totalOverall * 100).toFixed(2);
                    $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                    $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                    $("#totalPercent").html(totalPercent + "%");

                    $("#databasesProgress").html(progressHtml);

                    if (totalPercent == 100.00) {
                        clearInterval(update);
                        if (data.progress.length == 0) {
                            $("#running").hide();
                            $("#finished").show();
                        }
                    } else {
                        $("#running").show();
                        $("#finished").hide();
                    }
                }else{
                    $("#running").hide();
                    $("#finished").show();
                }
            },
            'error' : function() {
                clearInterval(update);
            }
        });
    }, 5000);

    function localeDateString(date) {
        var options = {
            hour12 : true,
            timeZone : "America/New_York"
        };
        return new Date(date).toLocaleString('en-US', options);
    }

});