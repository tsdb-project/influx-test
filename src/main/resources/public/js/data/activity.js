$(document).ready(
        function() {

            var files = {
                "data" : []
            };

            var table = $('#activityTable').DataTable({
                ajax : {
                    "url" : "/api/data/activity/list"
                },
                data : files.data,
                columns : [ {
                    data : null,
                    render : function(data) {
                        if (data.finished == false) {
                            return '<i class="zmdi zmdi-settings zmdi-hc-spin"></i>';
                        } else {
                            return '<i class="zmdi zmdi-check"></i>';
                        }
                    },
                    orderable : false
                }, {
                    data : 'batchId',
                    orderable : false
                } ],
                dom : 'tip'
            });

            $('#activityTable tbody').on('click', 'tr', function() {
                $('.t-detail').show();
                $('.t-list').hide();
                var activity = table.row($(this)).data();
                console.log(activity);
                // $("#inputPatient").val(table.row($(this)).data().interestPatient.pid);
                // $("#timespanCount").val($("#count").val());
                $("#batchId").html(activity.batchId);
                $("#startTime").html(activity.startTime);
                if (activity.finished) {
                    $("#endTime").html(activity.endTime);
                } else {
                    $("#endTime").html('N/A');
                }
                $("#elapsedTime").html(activity.elapsedTime);
                $("#size").html(activity.size);
                $("#fileCount").html(activity.fileCount);
                $("#finishedCount").html(activity.finishedCount);
                $("#inProgressCount").html(activity.inProgressCount);
                $("#queuedCount").html(activity.queuedCount);
                $("#failCount").html(activity.failCount);
            });

            $('#backButton').on('click', function() {
                $('.t-detail').hide();
                $('.t-list').show();
            });

            $("#refreshButton").click(function() {
                table.ajax.reload();
            });

            $.ajax({
                'url' : "/api/data/progress",
                'success' : function(data) {
                    var progressHtml = "";
                    for (i = 0; i < data.progress.length; i++) {
                        var progress = (data.progress[i].percent * 100).toFixed(2);
                        var color = data.progress[i].status == "STATUS_FINISHED" ? " bg-success" : "";
                        progressHtml += "<div class=\"progress\"><div class=\"progress-bar" + color + "\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                                + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.progress[i].filename + ' - ' + progress + "%</small><br><br>";
                    }

                    var totalPercent = (data.total * 100).toFixed(2);
                    $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                    $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                    $("#totalPercent").html(totalPercent + "%");

                    $("#fileProgress").html(progressHtml);

                    if (totalPercent == 100.00) {
                        clearInterval(update);
                        if (data.progress.length == 0) {
                            $("#running").hide();
                            $("#finished").show();
                        }
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
                            var progress = (data.progress[i].percent * 100).toFixed(2);
                            var color = data.progress[i].status == "STATUS_FINISHED" ? " bg-success" : "";
                            progressHtml += "<div class=\"progress\"><div class=\"progress-bar" + color + "\" role=\"progressbar\" style=\"width: " + progress + "%\" aria-valuenow=\"" + progress
                                    + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small class=\"card-subtitle\">" + data.progress[i].filename + ' - ' + progress + "%</small><br><br>";
                        }

                        var totalPercent = (data.total * 100).toFixed(2);
                        $("#totalProgress").attr("style", "width: " + totalPercent + "%");
                        $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
                        $("#totalPercent").html(totalPercent + "%");

                        $("#fileProgress").html(progressHtml);

                        if (totalPercent == 100.00) {
                            clearInterval(update);
                            if (data.progress.length == 0) {
                                $("#running").hide();
                                $("#finished").show();
                            }
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