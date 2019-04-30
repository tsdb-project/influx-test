$(document).ready(
        function() {

            var directory;

            var files = {
                "data" : []
            };

            var table = $('#filesTable').DataTable(
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
                                        var checkbox = "<th><div class=\"custom-control custom-checkbox\">" + "<input type=\"checkbox\" class=\"custom-control-input file-checkbox\" value=\"" + name
                                                + "\" name=\"files\"><label class=\"custom-control-label\" for=\"customCheck1\"></label></div></th>";
                                        return checkbox;
                                    }
                                } ],
                        paging : false,
                        "sDom" : '<"top">rt<"bottom"ilp><"clear">'
                    });

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