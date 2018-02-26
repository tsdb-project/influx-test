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
                                                    'dir' : $("#directory")
                                                            .val(),
                                                }
                                                directory = $("#directory")
                                                        .val();
                                                return JSON.stringify(form);
                                            }
                                        },
                                        data : files.data,
                                        columns : [
                                                {
                                                    data : 'name'
                                                },
                                                {
                                                    data : 'size',
                                                    orderable : false
                                                },
                                                {
                                                    data : null,
                                                    render : function(data,
                                                            type, full, meta) {
                                                        var name = data.directory
                                                                + data.name;
                                                        var checkbox = "<th><label class=\"custom-control custom-checkbox\">"
                                                                + "<input type=\"checkbox\" class=\"custom-control-input file-checkbox\" value=\""
                                                                + name
                                                                + "\" name=\"files\"><span class=\"custom-control-indicator\"></span></label></th>";
                                                        return checkbox;
                                                    }
                                                } ],
                                        order : [ [ 0, 'asc' ] ],
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

                    $("#importButton").click(
                            function() {
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

                                $("#progressCard").slideDown();
                                var update = setInterval(function() {

                                    $.ajax({
                                        'url' : "/api/data/progress",
                                        'success' : function(data) {
                                            var percent = data.progress;
                                            var name = data.file;
                                            $("#fileProgress").attr("style",
                                                    "width: " + percent + "%");
                                            $("#fileProgress").attr(
                                                    "aria-valuenow",
                                                    "" + percent);
                                            $("#fileName").html(name);
                                            $("#filePercent").html(
                                                    percent + "%");

                                            var totalPercent = data.total;
                                            $("#totalProgress").attr(
                                                    "style",
                                                    "width: " + totalPercent
                                                            + "%");
                                            $("#totalProgress").attr(
                                                    "aria-valuenow",
                                                    "" + totalPercent);
                                            $("#totalPercent").html(
                                                    totalPercent + "%");

                                            if (data.finished == true) {
                                                clearInterval(update);
                                                $("#importButton").removeAttr(
                                                        'disabled');
                                                alert("Data import finished.");
                                            }
                                        },
                                        'error' : function() {
                                            clearInterval(update);
                                            $("#importButton").removeAttr(
                                                    'disabled');
                                        }
                                    });

                                }, 2000);

                            });
                });