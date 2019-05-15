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
        // for (var i = 0; i < $trs.length; i++) {
        //     var $tds = $trs.eq(i).find("td,th");
        //     for (var j = 0; j < $tds.length; j++) {
        //         str += $tds.eq(j).text() + ",";
        //     }
        //     str += "\n";
        // }

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