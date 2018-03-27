$(document).ready(function() {
    var queries = {
        "data" : []
    };

    var table = $('#queryTable').DataTable({
        ajax : {
            "url" : "/analysis/query"
        },
        data : queries.data,
        columnDefs : [ {
            "targets" : [ 0 ],
            "visible" : false,
            "searchable" : false
        } ],
        columns : [ {
            data : 'id'
        }, {
            data : 'alias'
        }, {
            data : null,
            render : function(data) {
                return secondsToStr(data.period);
            }
        }, {
            data : null,
            render : function(data) {
                return secondsToStr(data.origin);
            }
        }, {
            data : null,
            render : function(data) {
                return secondsToStr(data.duration);
            }
        }, {
            data : null,
            render : function(data) {
                return localeDateString(data.createTime)
            }
        }, {
            data : null,
            render : function(data) {
                return localeDateString(data.updateTime)
            }
        } ],
        order : [ [ 5, 'desc' ] ],
    });

    var groups = {
        "data" : []
    };

    var groupTable = $('#groupTable').DataTable({
        ajax : {
            "url" : "/analysis/group/" + $("#id").val()
        },
        data : groups.data,
        columnDefs : [ {
            "targets" : [ 0 ],
            "visible" : false,
            "searchable" : false
        }, {
            "targets" : [ 1 ],
            "visible" : false,
            "searchable" : false
        } ],
        columns : [ {
            data : 'group.id'
        }, {
            data : 'group.queryId'
        }, {
            data : 'group.downsample',
        }, {
            data : 'group.aggregation',
        }, {
            data : 'columns',
        }, {
            data : 'group.id',
            render : function(data) {
                return "<th><a class=\"btn btn-outline-danger btn-sm\" href=\"/analysis/group/delete/" + data + "\" role=\"button\"><i class=\"zmdi zmdi-close\"></i></a></th>";
            }
        } ]
    });

    function secondsToStr(seconds) {
        function numberEnding(number) {
            return (number > 1) ? 's' : '';
        }
        var temp = Math.floor(seconds);
        var years = Math.floor(temp / 31536000);
        if (years) {
            return years + ' year' + numberEnding(years);
        }
        var days = Math.floor((temp %= 31536000) / 86400);
        if (days) {
            return days + ' day' + numberEnding(days);
        }
        var hours = Math.floor((temp %= 86400) / 3600);
        if (hours) {
            return hours + ' hour' + numberEnding(hours);
        }
        var minutes = Math.floor((temp %= 3600) / 60);
        if (minutes) {
            return minutes + ' minute' + numberEnding(minutes);
        }
        var seconds = temp % 60;
        if (seconds) {
            return seconds + ' second' + numberEnding(seconds);
        }
        return 'N/A';
    }

    function localeDateString(date) {
        return new Date(date).toLocaleString();
    }

    $('#queryTable tbody').on('mouseover', 'tr', function() {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function() {
        $(this).removeAttr('style');
        ;
    });

    $('#queryTable tbody').on('click', 'tr', function() {
        window.location.href = '/analysis/edit/' + table.row($(this)).data().id;
    });

    $("#saveButton").click(function() {
        var form = {
            "id" : $("#id").val(),
            "alias" : $("#alias").val(),
            "period" : $("#period").val() * $("#period_unit").val(),
            "origin" : $("#origin").val() * $("#origin_unit").val(),
            "duration" : $("#duration").val() * $("#duration_unit").val()
        };
        $.ajax({
            'url' : "/analysis/query",
            'type' : 'put',
            'data' : JSON.stringify(form),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                window.location.href = '/analysis/edit/' + $("#id").val();
            },
            'error' : function() {
            }
        });
    });

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
                $column.attr("size", data.length)
                $column.empty();
                // $column.append('<option disabled="disabled"
                // selected="selected" value="">Select Columns</option>');
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i] + '">' + data[i] + '</option>');
                }
                $column.change();
            },
            'error' : function() {
            }
        });
    });
});