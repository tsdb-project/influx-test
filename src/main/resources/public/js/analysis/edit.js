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
});