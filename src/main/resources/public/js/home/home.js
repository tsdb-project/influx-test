$(document).ready(function() {
    $.ajax({
        "url" : "/versionControl/getLastVersion",
        "type" : "GET",
        'contentType' : "application/json",
        'dataType' : 'json',
        'success' : function(data) {
            total = data.data;
            var time = gettime(total.totalLength);
            var date = getDate(total.createDate);
            document.getElementById("csv").innerText=total.csvFileNum;
            document.getElementById("length").innerText=time;
            document.getElementById("patient").innerText=total.patientsWithCsv;
            document.getElementById("size").innerText=total.dbSize;
            document.getElementById("allpatients").innerText=total.patientNum;
        },
        'error' : function() {
        }
    });

    function gettime(data) {
        var total = data;
        var nd = parseInt(total/(60*60));
        return nd;
    }

    function getDate (data) {
        var Date = data.substring(0,10);
        return Date;
    }
});