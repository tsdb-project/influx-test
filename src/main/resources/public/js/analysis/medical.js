$(document).ready(function() {
    function notify(from, align, icon, type, animIn, animOut, msg) {
        $.notify({
            icon: icon,
            title: '',
            message: msg,
            url: ''
        }, {
            element: 'body',
            type: type,
            allow_dismiss: false,
            placement: {
                from: from,
                align: align
            },
            offset: {
                x: 20,
                y: 20
            },
            spacing: 10,
            z_index: 1000000000,
            delay: 1500,
            timer: 750,
            url_target: '_blank',
            mouse_over: false,
            animate: {
                enter: animIn,
                exit: animOut
            },
            template: '<div data-notify="container" class="alert alert-dismissible alert-{0} alert--notify" role="alert">' +
                '<span data-notify="icon"></span> ' +
                '<span data-notify="title">{1}</span> ' +
                '<span data-notify="message">{2}</span>' +
                '<div class="progress" data-notify="progressbar">' +
                '<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
                '</div>' +
                '<a href="{3}" target="{4}" data-notify="url"></a>' +
                '<button type="button" aria-hidden="true" data-notify="dismiss" class="alert--notify__close">Close</button>' +
                '</div>'
        });
    };


    $.ajax({ type: "GET",
        url: "/analysis/getAllMedicine/",
        async: false,
        success : function(text)
        {
            response = text.data;
        }
    });

    $(".field").select2({
        width: '100%',
        data : response
    });

    $("#medicalButton").click(function() {
        if ($('#parameter-form')[0].checkValidity()) {
            var before = $("#beforemedicine").val() * $("#beforemedicine_unit").val();
            var after = $("#aftermedicine").val() * $("#aftermedicine_unit").val();
            if(before >5*3600 || after>5*3600){
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'The range is too large, the system will set the large one as 5 hours');
                before = Math.min(before,5*3600);
                after = Math.min(after,5*3600);
                setTimeout(1000);
            }
            var form = {
                "alias": $("#alias").val(),
                "medicine":$("#medicine").val(),
                "period": $("#period").val() * $("#period_unit").val(),
                "beforeMedicine": before,
                "afterMedicine": after
            };
            $.ajax({
                'url': "/analysis/medicalquery",
                'type': 'post',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    setTimeout(function (){window.location.href = '/analysis/medicaledit/' + data.data.id},1000);
                },
                'error': function() {
                }
            });
            return false;
        } else {
            console.log("invalid form");
            return true;
        }

    });
});