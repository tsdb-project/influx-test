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
            data : null,
            render : function(data,type,row,meta) {
                var name = meta.row;
                var checkbox = "<th><div class=\"custom-control custom-checkbox\">"
                    + "<input type=\"checkbox\" class=\"custom-control-input file-checkbox\" value=\"" + name
                    + "\" name=\"files\"><label class=\"custom-control-label\" for=\"customCheck1\"></label></div></th>";
                return checkbox;
            }
        }],
        paging : false,
        dom : '<"dataTables__top"fB>rt<"dataTables__bottom"i><"clear">',
        buttons : [
            {
                extend : "excelHtml5",
                title : "Export Data"
            }, {
                extend : "csvHtml5",
                title : "Export Data"
            }, {
                extend : "print",
                title : "Material Admin"
            }
        ],
        initComplete : function(a, b) {
            $(this)
                .closest(".dataTables_wrapper")
                .find(".dataTables__top")
                .prepend(
                    '<div class="dataTables_buttons hidden-sm-down actions"><span class="actions__item zmdi zmdi-print" data-table-action="print" /><span class="actions__item zmdi zmdi-fullscreen" data-table-action="fullscreen" /><div class="dropdown actions__item"><i data-toggle="dropdown" class="zmdi zmdi-download" /><ul class="dropdown-menu dropdown-menu-right"><a href="" class="dropdown-item" data-table-action="excel">Excel (.xlsx)</a><a href="" class="dropdown-item" data-table-action="csv">CSV (.csv)</a></ul></div></div>')
        }
    });

    function getStatus(data){
        if(data===1){
            return "Delete"
        }
        if(data===2){
            return "Import"
        }
    }

    var import_list =[];
    var delete_list = [];
    var csvFile = [];
    var rownumbers = [];


    $("#ConfirmButton").click(function(){
        $.each($('.file-checkbox:checked'), function() {
            rownumbers.push($(this).val());
            csvFile.push(files[$(this).val()]);
        });

        for(var i=0;i<csvFile.length;i++){
            if(csvFile[i].status===1){
                delete_list.push(csvFile[i]);
            }else {
                import_list.push(csvFile[i]);
            }
        }
        //confirm delete
        console.log(delete_list);
        console.log(import_list);
        console.log(rownumbers);
        if(delete_list.length !==0){
            $.ajax({
                'url' : "/apis/file",
                'type' : 'DELETE',
                'data' : JSON.stringify(delete_list),
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
        if(import_list.length !==0){
            $.ajax({
                'url' : "/versionControl/confirmImport",
                'type' : 'POST',
                'data' : JSON.stringify(import_list),
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

    $(".dataTables_filter input[type=search]").focus(function() {
        $(this).closest(".dataTables_filter").addClass("dataTables_filter--toggled")
    }),
        $(".dataTables_filter input[type=search]").blur(function() {
            $(this).closest(".dataTables_filter").removeClass("dataTables_filter--toggled")
        }),
        $("body").on("click", "[data-table-action]", function(a) {
            a.preventDefault();
            var b = $(this).data("table-action");
            if ("excel" === b && $(this).closest(".dataTables_wrapper").find(".buttons-excel").trigger("click"),
            "csv" === b && $(this).closest(".dataTables_wrapper").find(".buttons-csv").trigger("click"),
            "print" === b && $(this).closest(".dataTables_wrapper").find(".buttons-print").trigger("click"),
            "fullscreen" === b) {
                var c = $(this).closest(".card");
                c.hasClass("card--fullscreen") ? (c.removeClass("card--fullscreen"),
                    $("body").removeClass("data-table-toggled")) : (c.addClass("card--fullscreen"),
                    $("body").addClass("data-table-toggled"))
            }
        });

    $('#selectAllFiles').click(function() {
        if ($('#selectAllFiles').is(':checked')) {
            $('.custom-control-input').prop("checked", true);
        } else {
            $('.custom-control-input').prop("checked", false);
        }
    });

    $("#CancelButton").click(function(){
        $.each($('.file-checkbox:checked'), function() {
            rownumbers.push($(this).val());
            csvFile.push(files[$(this).val()]);
        });

        for(var i=0;i<csvFile.length;i++){
            if(csvFile[i].status===1){
                delete_list.push(csvFile[i]);
            }else {
                import_list.push(csvFile[i]);
            }
        }
        console.log(delete_list);
        console.log(import_list);
        console.log(rownumbers);
        // cancel delete
        if(delete_list.length !==0){
            $.ajax({
                'url': "/versionControl/cancelDelete",
                'type': 'post',
                'data': JSON.stringify(delete_list),
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
        if(import_list.length !==0){
            $.ajax({
                'url' : "/apis/file",
                'type' : 'DELETE',
                'data' : JSON.stringify(import_list),
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