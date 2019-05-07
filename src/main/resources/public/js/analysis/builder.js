$(document).ready(function () {
    var queries = {
        "data": []
    };

    $.fn.dataTable.moment('M/D/YYYY, h:mm:ss a');
    var table = $('#queryTable').DataTable({
        ajax: {
            "url": "/analysis/query"
        },
        data: queries.data,
        columnDefs: [{
            "targets": [0],
            "visible": false,
            "searchable": false
        }],
        columns: [{
            data: 'id'
        }, {
            data: 'alias'
        }, {
            data: null,
            render: function (data) {
                return secondsToStr(data.period);
            }
        }, {
            data: null,
            render: function (data) {
                return secondsToStr(data.origin);
            }
        }, {
            data: null,
            render: function (data) {
                return secondsToStr(data.duration);
            }
        }, {
            data: null,
            render: function (data) {
                return localeDateString(data.createTime)
            }
        }, {
            data: null,
            render: function (data) {
                return localeDateString(data.updateTime)
            }
        }, {
            data: null,
            render: function (data) {
                return ['~Seconds', '~Minutes', '~Hours', '>12 Hours', 'N/A'][estimateExecTime(data.duration, data.period)]
            }
        }],
        order: [[5, 'desc']],
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

    function estimateExecTime(dur, interval) {
        if (dur === undefined || interval === undefined) return 4;
        if (dur === 0 || interval === 0) return 4;
        var idx1 = dur / interval;
        if (idx1 < 16) return 0;
        else if (idx1 < 24) return 1;
        else if (idx1 < 32) return 2;
        else return 3;
    }

    function localeDateString(date) {
        var options = {
            hour12: true,
            timeZone: "America/New_York"
        };
        return new Date(date).toLocaleString('en-US', options);
    }

    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
        ;
    });

    $('#queryTable tbody').on('click', 'tr', function () {
        window.location.href = '/analysis/edit/' + table.row($(this)).data().id;
    });
});