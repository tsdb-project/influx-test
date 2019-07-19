$(document).ready(function() {
    $.ajax({
        "url" : "/versionControl/getLastVersion",
        "type" : "GET",
        'contentType' : "application/json",
        'dataType' : 'json',
        'success' : function(data) {
            total = data.data;
            console.log(total);
            var time = gettime(total.totalLength);
            console.log(total.patientNum);
            document.getElementById("csv").innerText=total.csvFileNum;
            document.getElementById("length").innerText=time;
            document.getElementById("patient").innerText=total.patientsWithCsv;
            document.getElementById("size").innerText=total.dbSize;
        },
        'error' : function() {
        }
    });

    function gettime(data) {
        var total = data;
        var nd = parseInt(total/(24*60*60));
        return nd;
    }
});