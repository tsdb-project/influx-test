$(document).ready(function () {
    var queries = {
        "data": []
    };

    $.fn.dataTable.moment('M/D/YYYY, h:mm:ss a');
    var table = $('#queryTable').DataTable({
        ajax: {
            "url": "/analysis/getWrongPatients"
        },
        data: queries.data,
        columnDefs: [{
            "targets": [0],
            "visible": true,
            "searchable": true
        }],
        columns: [{
            data: 'pid'
        }, {
            data:null,
            render:function (data){
                return booleanToStr(data.isoverlap);
            }
        },{
            data:'ar_miss'
        },{
            data:'noar_miss'
        },{
            data:null,
            render:function (data) {
                return booleanToStr(data.wrongname);
            }
        }],
        order: [[0, 'desc']],
    });

    var total =[];
    $.ajax({
        "url" : "/analysis/getWrongPatientsNum",
        "type" : "GET",
        'contentType' : "application/json",
        'dataType' : 'json',
        'success' : function(data) {
            total = data.data;
            document.getElementById("overlap").innerText=total.overlap;
            document.getElementById("missAr").innerText=total.missAr;
            document.getElementById("missNoar").innerText=total.missNoar;
            document.getElementById("wrongName").innerText=total.wrongName;
        },
        'error' : function() {
        }
    });


    function booleanToStr(flag){
        return flag ? 'T':' ';
    }

    $("#export").click(function () {
        $.ajax({ type: "GET",
            url: "/analysis/getWrongPatients",
            async: false,
            success : function(text)
            {
                response = text.data;
            }
        });
        console.log(response);
        var str ="pid;isoverlap;ar_miss;noar_miss;wrongname";
        str+="\n";
        for(var i=0; i<response.length;i++){
            str+=response[i].pid+";";
            if(response[i].isoverlap){
                str+="T;";
            }else {
                str+=";";
            }
            str+=response[i].ar_miss+";";
            str+=response[i].noar_miss+";";
            if(response[i].wrongname){
                str+="T;";
            }else {
                str+=";";
            }
            str+="\n";
        }

        var aaaa = "data:text/csv;charset=utf-8,\ufeff" + str;
        var link = document.createElement("a");
        link.setAttribute("href", aaaa);
        var filename = "wrongpatinets";
        link.setAttribute("download", filename + ".csv");
        link.click();
    });

    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
    });
});