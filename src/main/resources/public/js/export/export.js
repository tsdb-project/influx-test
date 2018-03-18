$(document).ready(function() {
    $("#measure").change(function() {
        console.log($("#measure").val());
        var form = [ $("#measure").val() ];
        $.ajax({
            'url' : "/api/export/electrode",
            'type' : 'post',
            'data' : JSON.stringify(form),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                console.log(data);
                var $electrode = $('#electrode');
                $electrode.empty();
                $('#column').empty();
                $electrode.append('<option disabled="disabled" selected="selected" value="">Select Electrodes</option>');
                for (var i = 0; i < data.length; i++) {
                    var html = '<option value="' + data[i] + '">' + data[i] + '</option>';
                    $electrode.append(html);
                }
                // $electrode.change();
            },
            'error' : function() {
            }
        });
    });

    $("#electrode").change(function() {
        console.log($("#electrode").val());
        var form = {
            "measure" : $("#measure").val(),
            "electrode" : [ $("#electrode").val() ]
        };
        $.ajax({
            'url' : "/api/export/column",
            'type' : 'post',
            'data' : JSON.stringify(form),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                var $column = $('#column');
                $column.empty();
                $column.append('<option disabled="disabled" selected="selected" value="">Select Columns</option>');
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i] + '">' + data[i] + '</option>');
                }
                $column.change();
            },
            'error' : function() {
            }
        });
    });

    $("#exportButton").click(function() {
        var form = {
            "measure" : $("#measure").val(),
            "electrode" : $("#electrode").val(),
            "column" : $("#column").val(),
            "method" : $("#method").val(),
            "interval" : $("#time_interval").val(),
            "time" : $("#hours").val(),
            "ar" : $("#ar").val(),
            "gender" : $("#gender").val(),
            "ageLower" : $("#ageLower").val(),
            "ageUpper" : $("#ageUpper").val(),
        };
        $.ajax({
            'url' : "/api/export/export",
            'type' : 'post',
            'data' : JSON.stringify(form),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
            },
            'error' : function() {
            }
        });
    });

    //
    // var patients = {
    // "data" : []
    // };
    //
    // var table = $('#patient-table').DataTable({
    // ajax : {
    // "url" : "/query/differ/query",
    // "contentType" : "application/json",
    // "type" : "POST",
    // "data" : function() {
    // var meta = {
    // 'gender' : $("#gender").val(),
    // 'ageUpper' : $("#ageUpper").val(),
    // 'ageLower' : $("#ageLower").val(),
    // 'ar' : $("#ar").val()
    // }
    // var form = {
    // 'meta' : meta,
    // 'columnA' : $("#columnA").val(),
    // 'columnB' : $("#columnB").val(),
    // 'threshold' : $("#threshold").val(),
    // 'count' : $("#count").val(),
    // }
    // return JSON.stringify(form);
    // }
    // },
    // data : patients.data,
    // columns : [ {
    // data : 'interestPatient.pid'
    // }, {
    // data : 'interestPatient.age'
    // }, {
    // data : 'interestPatient.gender'
    // }, {
    // data : 'occurTimes'
    // } ],
    // });
    //
    // var timespantable = $('#timespan-table').DataTable({
    // data : timespan.data,
    // columns : [ {
    // title : "Start",
    // data : "start"
    // }, {
    // title : "End",
    // data : "end"
    // } ]
    // });
    //
    // $('#patient-table tbody').on('click', 'tr', function() {
    // table.$('tr.selected').removeAttr('style');
    // table.$('tr.selected').removeClass('selected');
    // $(this).addClass('selected');
    // $(this).attr('disabled', '');
    // $(this).attr("style", "background-color:#ffffdd");
    // var timespan = {
    // "data" : table.row($(this)).data().occurTime
    // };
    // timespantable.clear().draw();
    // timespantable.rows.add(timespan.data); // Add
    // // new
    // // data
    // timespantable.columns.adjust().draw();
    // console.log(timespan);
    // $("#inputPatient").val(table.row($(this)).data().interestPatient.pid);
    // $("#timespanCount").val($("#count").val());
    // });
    //
    // $('#timespan-table tbody').on('click', 'tr', function() {
    // timespantable.$('tr.selected').removeAttr('style');
    // timespantable.$('tr.selected').removeClass('selected');
    // $(this).addClass('selected');
    // $(this).attr('disabled', '');
    // $(this).attr("style", "background-color:#ffffdd");
    //
    // $("#inputTimeStart").val(timespantable.row($(this)).data().start);
    // $("#inputTimeEnd").val(timespantable.row($(this)).data().end);
    //
    // var form = {
    // 'tableName' : $("#inputPatient").val(),
    // 'columnNames' : [ $("#inputColumn").val(), $("#compareColumn").val() ]
    // };
    //
    // var params = {
    // 'var-table' : $("#inputPatient").val(),
    // 'var-column' : $("#inputColumn").val(),
    // 'var-compare' : $("#compareColumn").val(),
    // 'from' : Date.parse($("#inputTimeStart").val()) -
    // $("#timespanCount").val() * 2000,
    // 'to' : Date.parse($("#inputTimeEnd").val()) + $("#timespanCount").val() *
    // 2000,
    // 'var-start' : Date.parse($("#inputTimeStart").val()) * 1000000,
    // 'var-end' : Date.parse($("#inputTimeEnd").val()) * 1000000
    // };
    //
    // var openUrl = "http://localhost:3000/dashboard/db/hourly-means?" +
    // jQuery.param(params);
    // window.open(openUrl);
    //
    // $.ajax({
    // 'url' : "/query/raw",
    // 'type' : 'post',
    // 'data' : JSON.stringify(form),
    // 'contentType' : "application/json",
    // 'dataType' : 'json',
    // 'success' : function(data) {
    // plotData = data.raw;
    //
    // plot = $.plot($("#Plot"), plotData, lineChartOptions);
    //
    // overview = $.plot("#Overview", plotData, lineChartOptions3);
    //
    // $("#PlotCard").slideDown();
    // },
    // 'error' : function() {
    // plotData = [ [ [ 0, 0 ], [ 1.65, 0.334 ], [ 2.2, 0.933 ], [ 3, 0.45 ] ]
    // ];
    // plot = $.plot($("#Plot"), plotData, lineChartOptions);
    //
    // overview = $.plot("#Overview", plotData, lineChartOptions3);
    //
    // $("#PlotCard").slideDown();
    // }
    // });
    // });
    //
    // $("#filterButton").click(function() {
    // table.ajax.reload();
    // var timespan = {
    // "data" : []
    // };
    // timespantable.clear().draw();
    // timespantable.rows.add(timespan.data); // Add new data
    // timespantable.columns.adjust().draw();
    // $("#PlotCard").slideUp();
    // $("#inputColumn").val($("#columnA").val());
    // $("#compareColumn").val($("#columnB").val());
    // });

});