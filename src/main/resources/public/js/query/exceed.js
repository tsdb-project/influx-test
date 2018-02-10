var timespan = {
    "data" : [ {
        "start" : "2017-10-26 16:30:20",
        "end" : "2017-10-26 16:30:29",
        "length" : "10 seconds"
    }, {
        "start" : "2017-10-26 16:23:20",
        "end" : "2017-10-26 16:23:32",
        "length" : "13 seconds"
    }, {
        "start" : "2017-10-26 16:44:20",
        "end" : "2017-10-26 16:23:29",
        "length" : "10 seconds"
    } ]
};

$(document).ready(function() {
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
            data : 'problemPid'
        }, {
            data : 'problemPid'
        }, {
            data : 'problemPid'
        }, {
            data : 'occurTime'
        } ],
    });

    $('#patient-table tbody').on('click', 'tr', function() {
        $(this).addClass('selected');
        var time
    });

    var timespantable = $('#timespan-table').DataTable({
        data : timespan.data,
        columns : [ {
            data : 'start'
        }, {
            data : 'end'
        }, {
            data : 'length'
        } ],
    });

    var records = {
        "data" : []
    };

    var recordstable = $('#records-table').DataTable({
        data : records.data,
        columns : [ {
            data : 'time'
        }, {
            data : 'I10_1'
        }, {
            data : 'I10_2'
        }, {
            data : 'I10_3'
        }, {
            data : 'I10_4'
        }, {
            data : 'I10_5'
        }, {
            data : 'I10_6'
        }, {
            data : 'I10_7'
        }, {
            data : 'I10_8'
        } ],
    });

    $("#filterButton").click(function() {
        table.ajax.reload();
        // var form = {
        // 'column' : $("#column").val(),
        // 'threshold' : $("#threshold").val(),
        // 'count' : $("#count").val(),
        // }
        //
        // $.ajax({
        // 'url' : "/query/exceed/query",
        // 'type' : 'post',
        // 'data' : JSON.stringify(form),
        // 'contentType' : "application/json",
        // 'dataType' : 'json',
        // 'success' : function(data) {
        // patients =
        // {"data":[{"patient":{"id":"000000001","firstName":"Xxxxxxxxx","lastName":"
        // Xxxxxx","age":24,"birthDate":"01/01/1994"},"occurence":1},{"patient":{"id":"000000002","firstName":"Xxxxxxxxx","lastName":"
        // Xxxxxx","age":24,"birthDate":"01/01/1994"},"occurence":1}]};
        // table.ajax.reload();
        // // table = $('#patient-table').DataTable({
        // // data : data.data
        // // });
        // },
        // 'error' : function() {
        // alert("error");
        // }
        // });
    });

});