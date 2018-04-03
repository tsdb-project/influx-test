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
                order : [ [ 0, 'asc' ] ],
            });

            $("#refreshButton").click(function() {
                table.ajax.reload();
            });

            $.ajax({
                'url' : "/api/data/progress",
                'success' : function(data) {
                    var progressHtml = "";
                    for (i = 0; i < data.progress.length; i++) {
                        var progress = (data.progress[i].progress * 100).toFixed(2);
                        progressHtml += "<div class=\"progress\"><div class=\"progress-bar\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                                + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.progress[i].file_name + ' - ' + progress + "%</small><br><br>";
                    }

                    var totalPercent = (data.total * 100).toFixed(2);
                    $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                    $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                    $("#totalPercent").html(totalPercent + "%");

                    $("#fileProgress").html(progressHtml);

                    if (totalPercent >= 99.9) {
                        clearInterval(update);
                        $("#running").hide();
                        $("#finished").show();
                    } else {
                        $("#running").show();
                        $("#finished").hide();
                    }
                },
                'error' : function() {
                    clearInterval(update);
                }
            });

            var update = setInterval(function() {
                $.ajax({
                    'url' : "/api/data/progress",
                    'success' : function(data) {
                        var progressHtml = "";
                        for (i = 0; i < data.progress.length; i++) {
                            var progress = (data.progress[i].progress * 100).toFixed(2);
                            progressHtml += "<div class=\"progress\"><div class=\"progress-bar\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                                    + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.progress[i].file_name + ' - ' + progress + "%</small><br><br>";
                        }

                        var totalPercent = (data.total * 100).toFixed(2);
                        $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                        $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                        $("#totalPercent").html(totalPercent + "%");

                        $("#fileProgress").html(progressHtml);

                        if (totalPercent >= 99.9) {
                            clearInterval(update);
                            $("#running").hide();
                            $("#finished").show();
                        } else {
                            $("#running").show();
                            $("#finished").hide();
                        }
                    },
                    'error' : function() {
                        clearInterval(update);
                    }
                });
            }, 2000);

        });