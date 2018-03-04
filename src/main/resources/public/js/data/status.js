$(document).ready(
        function() {

            var files = {
                "data" : []
            };

            var table = $('#patientTable').DataTable({
                ajax : {
                    "url" : "/apis/patients/find"
                },
                data : files.data,
                columns : [ {
                    data : 'pid'
                }, {
                    data : 'age',
                }, {
                    data : 'gender',
                }, {
                    data : 'imported_time',
                } ],
                order : [ [ 3, 'asc' ] ],
            });
            
            $("#refreshButton").click(function() {
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

                            $
                                    .ajax({
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