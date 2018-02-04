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
            data : 'patient.id'
        }, {
            data : 'patient.firstName'
        }, {
            data : 'patient.lastName'
        }, {
            data : 'patient.age'
        }, {
            data : 'patient.birthDate'
        }, {
            data : 'occurence'
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