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
                                    data : 'size',
                                    orderable : false
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

                // $("#progressCard").slideDown();
                // var update = setInterval(function() {
                // var url = "/api/data/progress";
                // if (data.files.length != 0) {
                // url += "?file=" + data.files[0];
                // }
                // $.ajax({
                // 'url' : url,
                // 'success' : function(data) {
                // var progressHtml = "";
                // for (i = 0; i < data.filename.length; i++) {
                // var progress = (data.progress[i] * 100).toFixed(2);
                // progressHtml += "<div class=\"progress\"><div
                // class=\"progress-bar\" role=\"progressbar\" style=\"width: "
                // + progress + "%\" aria-valuenow=\"" + progress
                // + "\" aria-valuemin=\"0\"
                // aria-valuemax=\"100\"></div></div><small
                // class=\"card-subtitle\">" + data.filename[i]
                // + "</small><small class=\"card-subtitle\">" + progress +
                // "%</small><br><br>";
                // }
                //
                // var totalPercent = data.total;
                // $("#totalProgress").attr("style", "width: " + totalPercent +
                // "%");
                // $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                // $("#totalPercent").html(totalPercent + "%");
                //
                // $("#fileProgress").html(progressHtml);
                //
                // if (data.total >= 100) {
                // clearInterval(update);
                // $("#importButton").removeAttr('disabled');
                // alert("Data import finished.");
                // }
                // },
                // 'error' : function() {
                // clearInterval(update);
                // $("#importButton").removeAttr('disabled');
                // }
                // });
                //
                // }, 2000);

            });
        });