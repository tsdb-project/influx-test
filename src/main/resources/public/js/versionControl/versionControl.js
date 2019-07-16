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
        "url" : "/versionControl/getAllVersion",
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
            searchPlaceholder : "Search for version in the table..."
        },
        autoWidth : !1,
        responsive : !0,
        columns: [{
            data: 'versionId'
        },{
            data:'createDate'
        },{
            data:'puhPatients'
        },{
            data:'uabPatients'
        },{
            data:'tbiPatients'
        },{
            data:'csvFileNum'
        },{
            data:null,
            render:function (data) {
                var total = data.totalLength;
                var nd = parseInt(total/(24*60*60));
                total = total%(24*60*60);
                var nh = parseInt(total/(60*60));
                total = total%(60*60);
                var nm = parseInt(total/(60));
                total = total%(60);
                var ns = total;
                return nd+"days,"+nh+"hours,"+nm+"minutes,"+ns+"seconds"
            }
        },{
            data:'patinetsWithCsv'
        },{
            data:'medicationNum'
        },{
            data:'patientIncrease'
        },{
            data:'medicationIncrease'
        },{
            data :'csvIncrease'
        },{
            data:'csvDelete'
        },{
            data:null,
            render : function(data) {
                return '<button class="btn btn-info btn-sm" data-toggle="modal" data-target="#comment-modal" data-id="' + data.versionId + '"><i class="zmdi zmdi-edit"></i>Check comment</button>'
            }
        }],
        paging : true,
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


    $("#PublishButton").click(function(){
        $("#PublishButton").attr('disabled', '');
        $.ajax({
            'url' : "/versionControl/publish",
            'type' : 'GET',
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                if(data.res.code===0) {
                    notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", data.res.msg);
                }
                else {
                    notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "publish success");
                    $.ajax({
                        "url" : "/versionControl/getAllVersion",
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
                }

            },
            'error': function () {
            }
        });

    });


    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
    });

    $('#comment-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var id = button.data('id');
        console.log(id);
        $.ajax({
            'url': "/versionControl/getOneVersion/" + id,
            'type': 'get',
            'success': function(data) {
                var version = data.data;
                $("#version_id").val(version.versionId);
                $("#version_comment").val(version.comment);

            },
            'error': function() {}
        });
    });

    $("#edit_comment").click(function () {
        console.log("edit button");
        var version = {
            "versionId":$("#version_id").val(),
            "comment":$("#version_comment").val()
        };
        $.ajax({
            'url' : "/versionControl/setComment",
            'type' : 'put',
            'data' : JSON.stringify(version),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "edit comment success");
            },
            'error' : function() {
            }
        });
    })
});





















