$(document).ready(function() {
    var files = {
        "data" : []
    };

    var table = $('#filesTable').DataTable({
        ajax : {
            "url" : "/data/searchfile",
            "contentType" : "application/json",
            "type" : "POST",
            "data" : function() {
                var form = {
                    'dir' : $("#directory").val(),
                }
                return JSON.stringify(form);
            }
        },
        data : files.data,
        columns : [ {
            data : 'name'
        }, {
            data : 'size',
            orderable : false
        } ],
        order : [ [ 0, 'asc' ] ],
        paging : false,
        "sDom": '<"top">rt<"bottom"ilp><"clear">'
    });

    $("#searchButton").click(function() {
        table.ajax.reload();
    });

});