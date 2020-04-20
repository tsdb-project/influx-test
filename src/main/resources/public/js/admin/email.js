$(document).ready(function() {
    //notify function
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

    $("#send").click(function() {

        if ($('#parameter-form')[0].checkValidity()) {
            var form = {
                "emailAddress": $("#email").val(),
                "content": $("#content").val()
            };
            $.ajax({
                'url': "/user/sendEmail",
                'type': 'post',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    if(data.code==1){
                        notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "We have received your message");
                    }
                    else {
                        notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "Something went wrong, please try again");
                    }
                },
                'error': function() {
                }
            });
            return false;
        } else {
            console.log("invalid form");
            notify("top", "center", null, "danger", "animated fadeIn", "animated fadeOut", "Something went wrong, please try again");
            return true;
        }

    });
});