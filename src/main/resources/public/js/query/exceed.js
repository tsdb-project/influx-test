var timespan = {
    "data" : []
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
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
            $(this).removeAttr("style");
        } else {
            table.$('tr.selected').removeAttr('style');
            table.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
            $(this).attr("style", "background-color:#eeeeee");
        }
        var timespan = {
            "data" : table.row($(this)).data().occurTime
        };
        timespantable.clear().draw();
        timespantable.rows.add(timespan.data); // Add new data
        timespantable.columns.adjust().draw();
        console.log(timespan);
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
    
    $('#timespan-table tbody').on('click', 'tr', function() {
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
            $(this).removeAttr("style");
        } else {
            timespantable.$('tr.selected').removeAttr('style');
            timespantable.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
            $(this).attr("style", "background-color:#eeeeee");
        }
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