$(document).ready(function() {

    var files = {
        "data" : []
    };

    var table = $('#patientTable').DataTable({
        ajax : {
            "url" : "/apis/patients/find"
        },
        data : files.data,
        columns : [ {
            data : 'id'
        }, {
            data : 'age',
        }, {
            data : null,
            render : function(data) {
                if (data.female == '0') {
                    return 'Male';
                } else {
                    return 'Female';
                }
            }
        } ],
        order : [ [ 0, 'asc' ] ],
    });

    $("#refreshButton").click(function() {
        table.ajax.reload();
    });

    // $.ajax({
    // 'url' : "/api/data/progress",
    // 'success' : function(data) {
    // var progressHtml = "";
    // for (i = 0; i < data.progress.length; i++) {
    // var progress = (data.progress[i].percent * 100).toFixed(2);
    // var color = data.progress[i].status == "STATUS_FINISHED" ? " bg-success"
    // : "";
    // progressHtml += "<div class=\"progress\"><div class=\"progress-bar" +
    // color + "\" role=\"progressbar\" style=\"width: " + progress + "%\"
    // aria-valuenow=\"" + progress
    // + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small
    // class=\"card-subtitle\">" + data.progress[i].filename + ' - ' + progress
    // + "%</small><br><br>";
    // }
    //
    // var totalPercent = (data.total * 100).toFixed(2);
    // $("#totalProgress").attr("style", "width: " + totalPercent + "%");
    // $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
    // $("#totalPercent").html(totalPercent + "%");
    //
    // $("#fileProgress").html(progressHtml);
    //
    // if (totalPercent == 100.00) {
    // clearInterval(update);
    // // $("#running").hide();
    // // $("#finished").show();
    // // } else {
    // $("#running").show();
    // $("#finished").hide();
    // }
    // },
    // 'error' : function() {
    // clearInterval(update);
    // }
    // });
    //
    // var update = setInterval(function() {
    // $.ajax({
    // 'url' : "/api/data/progress",
    // 'success' : function(data) {
    // var progressHtml = "";
    // for (i = 0; i < data.progress.length; i++) {
    // var progress = (data.progress[i].percent * 100).toFixed(2);
    // var color = data.progress[i].status == "STATUS_FINISHED" ? " bg-success"
    // : "";
    // progressHtml += "<div class=\"progress\"><div class=\"progress-bar" +
    // color + "\" role=\"progressbar\" style=\"width: " + progress + "%\"
    // aria-valuenow=\"" + progress
    // + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div><small
    // class=\"card-subtitle\">" + data.progress[i].filename + ' - ' + progress
    // + "%</small><br><br>";
    // }
    //
    // var totalPercent = (data.total * 100).toFixed(2);
    // $("#totalProgress").attr("style", "width: " + totalPercent + "%");
    // $("#totalProgress").attr("aria-valuenow", "" + totalPercent);
    // $("#totalPercent").html(totalPercent + "%");
    //
    // $("#fileProgress").html(progressHtml);
    //
    // if (totalPercent == 100.00) {
    // clearInterval(update);
    // // $("#running").hide();
    // // $("#finished").show();
    // // } else {
    // $("#running").show();
    // $("#finished").hide();
    // }
    // },
    // 'error' : function() {
    // clearInterval(update);
    // }
    // });
    // }, 2000);

});