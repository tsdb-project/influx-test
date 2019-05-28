$(document).ready(function () {
    var queries = {
        "data": []
    };

    $.fn.dataTable.moment('M/D/YYYY, h:mm:ss a');
    var table = $('#queryTable').DataTable({
        ajax: {
            "url": "/versionControl/getdata"
        },
        data: queries.data,
        columnDefs: [{
            "targets": [0],
            "visible": false,
            "searchable": false
        }],
        columns: [{
            data: 'id'
        },{
            data:'pid'
        },{
            data:'filename'
        },{
            data:null,
            render:function (data){
                return localeDateString(data.startTime)
            }
        },{
            data:null,
            render:function (data) {
                return localeDateString(data.endTime);
            }
        },{
            data:null,
            render:function (data) {
                return getStatus(data.status);
            }
        },{
            data : 'id',
            render : function(data) {
                return "<th><button class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#confirm-alert-modal\" data-id=\"" +
                    data + "\"><i class=\"zmdi zmdi-edit\"></i>Confirm</button> " +
                    "<button class=\"btn btn-danger btn-sm\" data-toggle=\"modal\" data-target=\"#cancel-alert-modal\" data-id=\"" +
                    data + "\"><i class=\"zmdi zmdi-close\"></i>Cancel</a></th>";
            }
        }],
        order: [[0, 'desc']],
    });

    function getStatus(data){
        if(data==1){
            return "Delete"
        }
        if(data==2){
            return "Import"
        }
    }

    function localeDateString(date) {
        var options = {
            hour12: true,
            timeZone: "America/New_York"
        };
        return new Date(date).toLocaleString('en-US', options);
    }

    $("#ConfirmButton").click(function() {
        var id = $(this).attr('data-id');
        var pass = $("#password").val();
        console.log(id);
        console.log(pass);
        if(pass !=="admi"){
            $("#wrong-password-modal").modal();
            return
        }
        $.ajax({
            'url': "/versionControl/confirm",
            'type': 'post',
            'data': JSON.stringify(id),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                $("#confirm-complete-modal").modal();
                location.reload();
            },
            'error': function() {}
        });
    });

    $("#CancelButton").click(function() {
        var id = $(this).attr('data-id');
        var pass = $("#password2").val();
        console.log(id);
        console.log(pass);
        if(pass !== "admi"){
            $("#wrong-password-modal").modal();
            return
        }

        $.ajax({
            'url': "/versionControl/cancel",
            'type': 'post',
            'data': JSON.stringify(id),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                $("#cancel-complete-modal").modal();
                location.reload();
            },
            'error': function() {}
        });
    });

    $('#cancel-alert-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var id = button.data('id');
        var modal = $(this);
        console.log(id);
        $("#CancelButton").attr('data-id', id);
    });

    $('#confirm-alert-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var id = button.data('id');
        var modal = $(this);
        console.log(id);
        $("#ConfirmButton").attr('data-id', id);
    });


    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
    });
});