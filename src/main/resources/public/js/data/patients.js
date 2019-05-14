$(document).ready(
        function() {
            var table = $('#patientTable').DataTable({
                ajax : {
                    "url" : "/apis/patients/find",
                    "type" : "POST"
                },
                data : data.data,
                language : {
                    searchPlaceholder : "Search for patients in the table..."
                },
                columns : [
                        {
                            data : 'id'
                        }, {
                            data : 'age',
                        }, {
                            data : null,
                            render : function(data) {
                                if (data.female == '0') {
                                    return 'Male';
                                } else if (data.female == '1') {
                                    return 'Female';
                                } else {
                                    return '';
                                }
                            }
                        }, {
                            data : null,
                            render : function(data) {
                                return "<th><button role=\"button\" class=\"btn btn-primary btn-sm\" data-id=\"" + data.id + "\">MANAGE</button><th>"
                            }
                        }
                ],
                order : [
                    [
                            0, 'asc'
                    ]
                ],
            });
            
            var files = []
            var fileTable = $("#csv-file-table").DataTable({
                data : files,
                language : {
                    searchPlaceholder : "Search for files in the table..."
                },
                autoWidth : !1,
                responsive : !0,
                columns : [
                        {
                            data : "csvFile.filename",
                        }, {
                            data : "csvFile.startTime",
                        }, {
                            data : "csvFile.endTime"
                        }, {
                            data : "csvFile.length"
                        }, {
                            data : "csvFile.density"
                        }, {
                            data : null,
                            render : function(data, type, row, meta) {
                                if (data.counterpart.length == 1) {
                                    return "Valid"
                                }
                                if (data.counterpart.length == 0) {
                                    return "NONE"
                                }
                                var counterpartHtml = "";
                                data.counterpart.forEach(function(counterpart) {
                                    counterpartHtml += counterpart.filename + "<br>"
                                });
                                return counterpartHtml.substr(0, counterpartHtml.length - 4);
                            }
                        }, {
                            data : "gap"
                        }, {
                            data : null,
                            render : function(data, type, row, meta) {
                                return "<button role=\"button\" class=\"btn btn-danger btn-sm\" data-row=\"" + meta.row + "\">DELETE</button>"
                            }
                        }
                ],
                order : [
                    [
                            1, 'asc'
                    ]
                ],
                columnDefs : [
                        {
                            targets : [
                                    1, 2
                            ],
                            render : $.fn.dataTable.render.moment("YYYY-MM-DDTHH:mm:ss", "MM/DD/YYYY HH:mm:ss")
                        }, {
                            targets : 4,
                            createdCell : function(td, cellData, rowData, row, col) {
                                if (cellData > 1 || cellData < 0.8) {
                                    var alpha = 1 - cellData > 0 ? 1 - cellData : 1
                                    var color = 'rgba(255, 107, 104, ' + alpha + ')'
                                    $(td).css('background-color', color)
                                }
                            }
                        }, {
                            targets : 5,
                            createdCell : function(td, cellData, rowData, row, col) {
                                if (cellData.counterpart.length != 1) {
                                    var color = 'rgba(255, 107, 104, 0.5)'
                                    $(td).css('background-color', color)
                                }
                            }
                        }, {
                            targets : 6,
                            createdCell : function(td, cellData, rowData, row, col) {
                                if (cellData.startsWith("-") || parseInt(cellData) > 4) {
                                    var color = 'rgba(255, 107, 104, 0.5)'
                                    $(td).css('background-color', color)
                                }
                            }
                        }
                ],
                paging : false
            });
            
            table.on('click', 'button', function(event) {
                $("#card-patient-id").val(event.target.dataset.id);
                $("#card-patient-id").html(event.target.dataset.id)
                $.ajax({
                    "url" : "/apis/patient/files",
                    "type" : "GET",
                    'data' : {
                        pid : $("#card-patient-id").val()
                    },
                    'contentType' : "application/json",
                    'dataType' : 'json',
                    'success' : function(data) {
                        files = data.data
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
                // $.ajax({
                // 'url' : "/apis/file",
                // 'type' : 'DELETE',
                // 'data' : JSON.stringify(files[row]),
                // 'contentType' : "application/json",
                // 'dataType' : 'json',
                // 'success' : function(data) {
                // console.log(data);
                // table.ajax.reload();
                // },
                // 'error' : function() {
                // }
                // });
                
            });
            
            $("#refreshButton").click(function() {
                table.ajax.reload();
            });
            
            var columnData = $.map(columns, function(obj) {
                obj.text = obj.text || obj.field;
                obj.id = obj.id || obj.field;
                return obj;
            });
            
            console.log(columnData);
            
            $(".field").select2({
                width : '100%',
                data : columnData
            });
            
            $(".operator").select2({
                width : '100%'
            });
            
            var wrapper = $("#filterForm"); // Fields wrapper
            var add_button = $("#addFilter"); // Add button ID
            
            var x = 1; // initlal text box count
            $(add_button).click(
                    function(e) {
                        e.preventDefault();
                        var html = '<div class="row"><div class="col-sm-3 col-md-3"><select class="init-select2 field" '
                                + 'data-placeholder="Filter Field" id="field[]" required>'
                                + '<option disabled="disabled" selected="selected" value="">Filter Field</option></select>'
                                + '</div><div class="col-sm-2 col-md-2"><select class="init-select2 operator" '
                                + 'data-placeholder="Filter Method" id="operator[]" required><option value="=">=</option>'
                                + '<option value="!=">&ne;</option><option value=">">&gt;</option><option value=">=">&ge;'
                                + '</option><option value="<">&lt;</option><option value="<=">&le;</option></select></div>'
                                + '<div class="col-sm-2 col-md-2"><div class="input-group mb-3"><input type="text" '
                                + 'class="form-control" id="value[]" placeholder="Input value" required></div></div>'
                                + '<div class="col-sm-1 col-md-1" style="margin-top:6px">'
                                + '<a href="#" class="remove_field btn btn-sm btn-outline-danger">remove</a></div></div>';
                        $(wrapper).append(html);
                        
                        $(".field").select2({
                            width : '100%',
                            data : columnData
                        });
                        
                        $(".operator").select2({
                            width : '100%'
                        });
                    });
            
            $(wrapper).on("click", ".remove_field", function(e) {
                e.preventDefault();
                $(this).parent('div').parent('div').remove();
            });
            
        });
