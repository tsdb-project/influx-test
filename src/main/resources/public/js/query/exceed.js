var timespan = {
    "data" : []
};

$(document).ready(function() {

    // helper for returning the weekends in a period

    function weekendAreas(axes) {

        var markings = [], d = new Date(axes.xaxis.min);

        // go to the first Saturday

        d.setUTCDate(d.getUTCDate() - ((d.getUTCDay() + 1) % 7))
        d.setUTCSeconds(0);
        d.setUTCMinutes(0);
        d.setUTCHours(0);

        var i = d.getTime();

        // when we don't set yaxis, the rectangle automatically extends to
        // infinity upwards and downwards

        do {
            markings.push({
                xaxis : {
                    from : i,
                    to : i + 2 * 24 * 60 * 60 * 1000
                }
            });
            i += 7 * 24 * 60 * 60 * 1000;
        } while (i < axes.xaxis.max);

        return markings;
    }

    // Chart Options
    var lineChartOptions2 = {
        xaxis : {
            mode : "time",
            tickLength : 5
        },
        selection : {
            mode : "x"
        },
        grid : {
            markings : weekendAreas
        }
    };

    var lineChartOptions = {
        series : {
            lines : {
                show : true,
                barWidth : 0.05,
                fill : 0
            }
        },
        crosshair : {
            mode : "x"
        },
        shadowSize : 0.1,
        grid : {
            // borderWidth : 1,
            // borderColor : '#edf9fc',
            show : true,
            hoverable : true,
            clickable : true,
            markings : weekendAreas
        },
        yaxis : {
        // tickColor : '#edf9fc',
        // tickDecimals : 0,
        // font : {
        // lineHeight : 13,
        // style : 'normal',
        // color : '#9f9f9f',
        // },
        // shadowSize : 0
        },
        xaxis : {
            mode : "time",
            tickLength : 5,
            tickColor : '#fff',
            tickDecimals : 0,
            font : {
                lineHeight : 13,
                style : 'normal',
                color : '#9f9f9f'
            },
            shadowSize : 0,
        },
        legend : {
            container : '#PlotLegend',
            backgroundOpacity : 0.5,
            noColumns : 0,
            backgroundColor : '#fff',
            lineWidth : 0,
            labelBoxBorderColor : '#fff'
        },
        selection : {
            mode : "x"
        }
    };

    var lineChartOptions3 = {
        series : {
            lines : {
                show : true,
                lineWidth : 1
            },
            shadowSize : 0
        },
        xaxis : {
            ticks : [],
            mode : "time"
        },
        yaxis : {
            ticks : [],
            min : 0,
            autoscaleMargin : 0.1
        },
        selection : {
            mode : "x"
        }
    };

    var plotData = [];

    var plot = $.plot($("#Plot"), plotData, lineChartOptions);

    var overview = $.plot("#Overview", plotData, lineChartOptions3);

    // now connect the two

    $("#Plot").bind("plotselected", function(event, ranges) {

        // do the zooming
        $.each(plot.getXAxes(), function(_, axis) {
            var opts = axis.options;
            opts.min = ranges.xaxis.from;
            opts.max = ranges.xaxis.to;
        });
        plot.setupGrid();
        plot.draw();
        plot.clearSelection();

        // don't fire event on the overview to prevent eternal loop

        overview.setSelection(ranges, true);
    });

    $("#Overview").bind("plotselected", function(event, ranges) {
        plot.setSelection(ranges);
    });

    var patients = {
        "data" : []
    };

    var table = $('#patient-table').DataTable({
        ajax : {
            "url" : "/query/exceed/query",
            "contentType" : "application/json",
            "type" : "POST",
            "data" : function() {
                var form = {
                    'column' : $("#column").val(),
                    'threshold' : $("#threshold").val(),
                    'count' : $("#count").val(),
                }
                return JSON.stringify(form);
            }
        },
        data : patients.data,
        columns : [ {
            data : 'interestPatient.pid'
        }, {
            data : 'interestPatient.age'
        }, {
            data : 'interestPatient.gender'
        }, {
            data : 'occurTimes'
        } ],
    });

    var timespantable = $('#timespan-table').DataTable({
        data : timespan.data,
        columns : [ {
            title : "Start",
            data : "start"
        }, {
            title : "End",
            data : "end"
        } ]
    });

    $('#patient-table tbody').on('click', 'tr', function() {
        table.$('tr.selected').removeAttr('style');
        table.$('tr.selected').removeClass('selected');
        $(this).addClass('selected');
        $(this).attr('disabled', '');
        $(this).attr("style", "background-color:#ffffdd");
        var timespan = {
            "data" : table.row($(this)).data().occurTime
        };
        timespantable.clear().draw();
        timespantable.rows.add(timespan.data); // Add new data
        timespantable.columns.adjust().draw();
        console.log(timespan);
        $("#inputPatient").val(table.row($(this)).data().interestPatient.pid);
        $("#timespanCount").val($("#count").val());
    });

    $('#timespan-table tbody').on('click', 'tr', function() {
        timespantable.$('tr.selected').removeAttr('style');
        timespantable.$('tr.selected').removeClass('selected');
        $(this).addClass('selected');
        $(this).attr('disabled', '');
        $(this).attr("style", "background-color:#ffffdd");

        $("#inputTimeStart").val(timespantable.row($(this)).data().start);
        $("#inputTimeEnd").val(timespantable.row($(this)).data().end);

        var form = {
            'tableName' : $("#inputPatient").val(),
            'columnNames' : [ $("#inputColumn").val() ]
        };

        var params = {
            'var-table' : $("#inputPatient").val(),
            'var-column' : $("#inputColumn").val(),
            'var-compare' : $("#inputColumn").val(),
            'from' : Date.parse($("#inputTimeStart").val()) - $("#timespanCount").val() * 2000,
            'to' : Date.parse($("#inputTimeEnd").val()) + $("#timespanCount").val() * 2000,
            'var-start' : Date.parse($("#inputTimeStart").val()) * 1000000,
            'var-end' : Date.parse($("#inputTimeEnd").val()) * 1000000
        };

        var openUrl = "http://localhost:3000/dashboard/db/templating?" + jQuery.param(params);
        window.open(openUrl);

        $.ajax({
            'url' : "/query/raw",
            'type' : 'post',
            'data' : JSON.stringify(form),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                plotData = data.raw;

                plot = $.plot($("#Plot"), plotData, lineChartOptions);

                overview = $.plot("#Overview", plotData, lineChartOptions3);

                $("#PlotCard").slideDown();
            },
            'error' : function() {
                plotData = [ [ [ 0, 0 ], [ 1.65, 0.334 ], [ 2.2, 0.933 ], [ 3, 0.45 ] ] ];
                plot = $.plot($("#Plot"), plotData, lineChartOptions);

                overview = $.plot("#Overview", plotData, lineChartOptions3);

                $("#PlotCard").slideDown();
            }
        });

    });

    $("#filterButton").click(function() {
        table.ajax.reload();
        var timespan = {
            "data" : []
        };
        timespantable.clear().draw();
        timespantable.rows.add(timespan.data); // Add new data
        timespantable.columns.adjust().draw();
        $("#PlotCard").slideUp();
        $("#inputColumn").val($("#column").val());
    });

});