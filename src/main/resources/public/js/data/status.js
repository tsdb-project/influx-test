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
                        var progress = (data.progress[i] * 100).toFixed(2);
                        progressHtml += "<div class=\"progress\"><div class=\"progress-bar\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                                + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.filename[i] + "</small><small class=\"card-subtitle\">" + progress
                                + "%</small><br><br>";
                    }

                    var totalPercent = data.total;
                    $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                    $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                    $("#totalPercent").html(totalPercent + "%");

                    $("#fileProgress").html(progressHtml);

                    if (data.total >= 99.9) {
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
                            var progress = (data.progress[i] * 100).toFixed(2);
                            progressHtml += "<div class=\"progress\"><div class=\"progress-bar\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                                    + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.filename[i] + "</small><small class=\"card-subtitle\">&nbsp&nbsp"
                                    + progress + "%</small><br><br>";
                        }

                        var totalPercent = data.total;
                        $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                        $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                        $("#totalPercent").html(totalPercent + "%");

                        $("#fileProgress").html(progressHtml);

                        if (data.total >= 99.9) {
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