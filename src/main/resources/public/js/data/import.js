$(document)
        .ready(
                function() {
                    
                    var directory;
                    
                    var files = {
                        "data" : []
                    };
                    
                    var table = $('#filesTable')
                            .DataTable(
                                    {
                                        ajax : {
                                            "url" : "/data/searchfile",
                                            "contentType" : "application/json",
                                            "type" : "POST",
                                            "data" : function() {
                                                var form = {
                                                    'dir' : $("#directory").val(),
                                                }
                                                directory = $("#directory").val();
                                                return JSON.stringify(form);
                                            }
                                        },
                                        data : files.data,
                                        language : {
                                            searchPlaceholder : "Search for files in the table..."
                                        },
                                        columns : [
                                                {
                                                    data : 'name'
                                                },
                                                {
                                                    data : {
                                                        display : 'size',
                                                        sort : 'bytes'
                                                    },
                                                    type : 'num'
                                                },
                                                {
                                                    data : null,
                                                    render : function(data, type, full, meta) {
                                                        var name = data.directory + data.name;
                                                        var checkbox = "<th><div class=\"custom-control custom-checkbox\">"
                                                                + "<input type=\"checkbox\" class=\"custom-control-input file-checkbox\" value=\"" + name
                                                                + "\" name=\"files\"><label class=\"custom-control-label\" for=\"customCheck1\"></label></div></th>";
                                                        return checkbox;
                                                    }
                                                }
                                        ],
                                        paging : false,
                                        autoWidth: !1,
                                        responsive: !0,
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
                    })
                    
                    
                    $('#selectAllFiles').click(function() {
                        if ($('#selectAllFiles').is(':checked')) {
                            $('.custom-control-input').prop("checked", true);
                        } else {
                            $('.custom-control-input').prop("checked", false);
                        }
                    });
                    
                    $("#searchButton").click(function() {
                        table.ajax.reload();
                    });
                    
                    $("#importButton").click(function() {
                        $("#importButton").attr('disabled', '');
                        
                        var data = {
                            'files' : []
                        };
                        $.each($('.file-checkbox:checked'), function() {
                            data['files'].push($(this).val());
                        });
                        
                        var files = data;
                        
                        $.ajax({
                            'url' : "/api/data/import",
                            'type' : 'post',
                            'data' : JSON.stringify(files),
                            'contentType' : "application/json",
                            'dataType' : 'json',
                            'success' : function(data) {
                            },
                            'error' : function() {
                            }
                        });
                        
                    });
                    
                    // new for validate csv
                    $("#analyzeButton").click(function() {
                        $("#analyzeButton").attr('disabled', '');
                        
                        var data = {
                            'files' : []
                        };
                        $.each($('.file-checkbox:checked'), function() {
                            data['files'].push($(this).val());
                        });
                        
                        var files = data;
                        
                        $.ajax({
                            'url' : "/api/data/validate",
                            'type' : 'post',
                            'data' : JSON.stringify(files),
                            'contentType' : "application/json",
                            'dataType' : 'json',
                            'success' : function(data) {
                                window.alert("analyze finished");
                            },
                            'error' : function() {
                            }
                        });
                        
                    });
                });
