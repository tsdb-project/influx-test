$(document).ready(function () {
    var queries = {
        "data": []
    };

    $.fn.dataTable.moment('M/D/YYYY, h:mm:ss a');
    var table = $('#queryTable').DataTable({
        ajax: {
            "url": "/milestone/getallMilestones"
        },
        data: queries.data,
        columnDefs: [{
            "targets": [0],
            "visible": false,
            "searchable": false
        }],
        columns: [{
            data: 'id'
        }, {
            data: null,
            render: function (data) {
                return localeDateString(data.date)
            }
        },{
            data:'numpatients'
        },{
            data:'numcsv'
        }],
        order: [[1, 'desc']],
    });

    function localeDateString(date) {
        var options = {
            hour12: true,
            timeZone: "America/New_York"
        };
        return new Date(date).toLocaleString('en-US', options);
    }

    $("#unlock").click(function () {
        var pass = $("#password2").val();
        if(pass !="admi"){
            window.alert("wrong password");
            return
        }
        var nowDate = new Date().getTime();
        var date = new Date(nowDate);
        Y = date.getFullYear() + '-';
        M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
        D = date.getDate() + ' ';
        h = date.getHours() + ':';
        if(h.length===2){
            h = '0'+h;
        }
        m = date.getMinutes() + ':';
        if(m.length===2){
            m = '0'+m;
        }
        s = date.getSeconds().toString();
        if(s.length===1){
            s = '0'+s;
        }
        console.log(Y+M+D+h+m+s);
        var form={
            'publishtime':Y+M+D+h+m+s
        };
        console.log(form);
        console.log(JSON.stringify(form));
        $.ajax({
            'url':"/milestone/unlock",
            'type':"POST",
            'data':JSON.stringify(form),
            'dataType':'json',
            'contentType':'application/json',
            'success':function (text) {
                if(text.msg=="success"){
                    window.alert("Database has been unlocked");
                }else {
                    window.alert("already unlocked");
                }

            }
        });
    });

    $("#publish").click(function () {
        var pass = $("#password").val();
        if(pass !="admi"){
            window.alert("wrong password");
            return
        }
        var nowDate = new Date().getTime();
        var date = new Date(nowDate);
        Y = date.getFullYear() + '-';
        M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
        D = date.getDate() + ' ';
        h = date.getHours() + ':';
        if(h.length===2){
            h = '0'+h;
        }
        m = date.getMinutes() + ':';
        if(m.length===2){
            m = '0'+m;
        }
        s = date.getSeconds().toString();
        if(s.length===1){
            s = '0'+s;
        }
        console.log(Y+M+D+h+m+s);
        var form={
            'publishtime':Y+M+D+h+m+s
        };
        console.log(form);
        console.log(JSON.stringify(form));
        $.ajax({
            'url':"/milestone/publish",
            'type':"POST",
            'data':JSON.stringify(form),
            'dataType':'json',
            'contentType':'application/json',
            'success':function (text) {
                if(text.msg=="success"){
                    window.alert("Database has been locked");
                    location.reload();
                }else{
                    window.alert("no update");
                }

            }
        });
    });

    $('#queryTable tbody').on('mouseover', 'tr', function () {
        $(this).attr("style", "background-color:#ffffdd");
    });

    $('#queryTable tbody').on('mouseout', 'tr', function () {
        $(this).removeAttr('style');
    });
});