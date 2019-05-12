$(document).ready(function() {
    var files;
    $.fn.dataTable.moment("MM/DD/YYYY HH:mm:ss");
    $.fn.dataTable.moment("MM/DD/YYYY HH:mm:ss");
    
    var table = $("#csv-file-table").DataTable({
        ajax : {
            "url" : "/apis/patient/" + patientId,
            "type" : "GET",
            "dataSrc" : function(json) {
                files = json.data;
                return json.data;
            }
        },
        columns : [
                {
                    data : "filename"
                }, {
                    data : "startTime",
                }, {
                    data : "endTime"
                }, {
                    data : "length"
                }, {
                    data : "density"
                }, {
                    data : null,
                    render : function(data, type, row, meta) {
                        return "<th><button role=\"button\" class=\"btn btn-danger btn-sm\" data-row=\"" + meta.row + "\">DELETE</button><th>"
                    }
                }
        ],
        columnDefs : [
            {
                targets : [
                        1, 2
                ],
                render : $.fn.dataTable.render.moment("YYYY-MM-DDTHH:mm:ss", "MM/DD/YYYY HH:mm:ss")
            }
        ],
        paging : false
    });
    
    $('#csv-file-table').on('click', 'button', function(event) {
        var row = event.target.dataset.row;
        $.ajax({
            'url' : "/apis/file",
            'type' : 'DELETE',
            'data' : JSON.stringify(files[row]),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                console.log(data);
                table.ajax.reload();
            },
            'error' : function() {
            }
        });
        
    });
    
    $("#refreshButton").click(function() {
        console.log(files)
        table.ajax.reload();
    });
});
