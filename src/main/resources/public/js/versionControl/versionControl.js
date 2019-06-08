$(document).ready(function () {
    function notify(from, align, icon, type, animIn, animOut, msg) {
        $.notify({
            icon : icon,
            title : '',
            message : msg,
            url : ''
        }, {
            element : 'body',
            type : type,
            allow_dismiss : false,
            placement : {
                from : from,
                align : align
            },
            offset : {
                x : 20,
                y : 20
            },
            spacing : 10,
            z_index : 1000000000,
            delay : 1500,
            timer : 750,
            url_target : '_blank',
            mouse_over : false,
            animate : {
                enter : animIn,
                exit : animOut
            },
            template : '<div data-notify="container" class="alert alert-dismissible alert-{0} alert--notify" role="alert">'
                + '<span data-notify="icon"></span> ' + '<span data-notify="title">{1}</span> '
                + '<span data-notify="message">{2}</span>' + '<div class="progress" data-notify="progressbar">'
                + '<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" '
                + 'aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div></div>'
                + '<a href="{3}" target="{4}" data-notify="url"></a>'
                + '<button type="button" aria-hidden="true" data-notify="dismiss" class="alert--notify__close">Close</button>' + '</div>'
        });
    }
    var files = [];
    $.ajax({
        "url" : "/versionControl/getdata",
        "type" : "GET",
        'contentType' : "application/json",
        'dataType' : 'json',
        'success' : function(data) {
            files = data.data;
            fileTable.clear();
            fileTable.rows.add(files);
            fileTable.draw();
        },
        'error' : function() {
        }
    });

    var fileTable = $('#queryTable').DataTable({
        data: files,
        language : {
            searchPlaceholder : "Search for files in the table..."
        },
        autoWidth : !1,
        responsive : !0,
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
            data:'startTime',
        },{
            data:'endTime',
        },{
            data:null,
            render:function (data) {
                return getStatus(data.status);
            }
        },{
            data:'comment'
        },{
            data :null,
            render : function(data,type,row,meta) {
                return "<th><button class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#confirm-alert-modal\" data-row=\"" +
                    meta.row + "\">Confirm</button> " +
                    "<button class=\"btn btn-danger btn-sm\" data-toggle=\"modal\" data-target=\"#cancel-alert-modal\" data-row=\"" +
                    meta.row + "\" >Cancel</a></th>";
            }
        }],
        order: [[0, 'desc']],
    });

    function getStatus(data){
        if(data===1){
            return "Delete"
        }
        if(data===2){
            return "Import"
        }
    }

    var csvFile =[];
    fileTable.on('click','button',function (event) {
        var row = event.target.dataset.row;
        csvFile = files[row];
        console.log(row);
        console.log(csvFile.status);
        console.log(csvFile.comment);
    });

    $("#ConfirmButton").click(function(){
        if($("#password").val()!=='admi'){
            notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "wrong password");
            return
        }
        //confirm delete
        if(csvFile.status===1){
            $.ajax({
                'url' : "/apis/file",
                'type' : 'DELETE',
                'data' : JSON.stringify(csvFile),
                'contentType' : "application/json",
                'dataType' : 'json',
                'success' : function(data) {
                    notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Deletion confirmed.");
                    console.log(data);
                    $.ajax({
                        "url" : "/versionControl/getdata",
                        "type" : "GET",
                        'contentType' : "application/json",
                        'dataType' : 'json',
                        'success' : function(data) {
                            files = data.data;
                            fileTable.clear();
                            fileTable.rows.add(files);
                            fileTable.draw();
                        },
                        'error' : function() {
                        }
                    });
                },
                'error': function () {
                    notify("top", "center", null, "failed", "animated fadeIn", "animated fadeOut", "Import confirmed failed.");
                }
            });
        }
        // confirm import data
        if(csvFile.status===2){
            $.ajax({
                'url' : "/versionControl/confirmImport",
                'type' : 'POST',
                'data' : JSON.stringify(csvFile),
                'contentType' : "application/json",
                'dataType' : 'json',
                'success' : function(data) {
                    notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Import confirmed.");
                    console.log(data);
                    $.ajax({
                        "url" : "/versionControl/getdata",
                        "type" : "GET",
                        'contentType' : "application/json",
                        'dataType' : 'json',
                        'success' : function(data) {
                            files = data.data;
                            fileTable.clear();
                            fileTable.rows.add(files);
                            fileTable.draw();
                            $("#csv-file-card").show();
                            $('html, body').animate({
                                scrollTop : ($("#csv-file-table").offset().top)
                            }, 500);
                        },
                        'error' : function() {
                        }
                    });
                },
                'error': function () {
                    notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "Import confirmed failed.");
                }
            });
        }

    });

    $("#CancelButton").click(function(){
        if($("#password2").val()!=='admi'){
            notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "wrong password");
            return
        }
        // cancel delete
        if(csvFile.status===1){
            $.ajax({
                'url': "/versionControl/cancelDelete",
                'type': 'post',
                'data': JSON.stringify(csvFile.id),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Deletion canceled.");
                    console.log(data);
                    $.ajax({
                        "url" : "/versionControl/getdata",
                        "type" : "GET",
                        'contentType' : "application/json",
                        'dataType' : 'json',
                        'success' : function(data) {
                            files = data.data;
                            fileTable.clear();
                            fileTable.rows.add(files);
                            fileTable.draw();
                        },
                        'error' : function() {
                        }
                    });
                },
                'error': function() {
                    notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "Deletion canceled failed.");
                }
            });
        }
        // cancel import
        if(csvFile.status===2){
            $.ajax({
                'url' : "/apis/file",
                'type' : 'DELETE',
                'data' : JSON.stringify(csvFile),
                'contentType' : "application/json",
                'dataType' : 'json',
                'success' : function(data) {
                    notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "Import canceled.");
                    console.log(data);
                    $.ajax({
                        "url" : "/versionControl/getdata",
                        "type" : "GET",
                        'contentType' : "application/json",
                        'dataType' : 'json',
                        'success' : function(data) {
                            files = data.data;
                            fileTable.clear();
                            fileTable.rows.add(files);
                            fileTable.draw();
                        },
                        'error' : function() {
                        }
                    });
                },
                'error': function () {
                    notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "Import canceled failed.");
                }
            });
        }


    });


    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
    });
});