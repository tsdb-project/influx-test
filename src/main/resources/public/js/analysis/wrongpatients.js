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

    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
        ;
    });
});