$(document).ready(function () {

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

    // $.ajax({
    //     'url': "/user/reset_password/" + id,
    //     'type': 'patch',
    //     'data': JSON.stringify(id),
    //     'contentType': "application/json",
    //     'dataType': 'json',
    //     'success': function(data) {
    //         table.ajax.reload();
    //     },
    //     'error': function() {}
    // });

    var $changePasswordForm = $('#change-password-form');
    $changePasswordForm.on('submit', function (ev) {
        ev.preventDefault();
        if ($changePasswordForm[0].checkValidity() && $("#c_newPassword").val() === $("#c_confirmPassword").val()) {
            $.ajax({
                'url': "/user/change_password/",
                'data': {
                    "id": $("#c_id").val(),
                    "password": $("#c_newPassword").val()
                },
                'type': 'POST',
                'contentType': "application/json",
                'dataType': 'json',
                'success': function (data) {

                },
                'error': function () {
                }
            });
        }else{
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                'Two passwords are not the same please try again');
        }

    });


    var $editUserForm = $('#edit-user-form');
    $editUserForm.on('submit', function (ev) {
        ev.preventDefault();
        if ($editUserForm[0].checkValidity()) {
            var form = {
                "id": $("#e_id").val(),
                "username": $("#e_username").val(),
                "firstName": $("#e_firstname").val(),
                "lastName": $("#e_lastname").val(),
                "email": $("#e_email").val(),
                "role" : $('#e_role').val()
            };

            $.ajax({
                'url': "/user/user",
                'type': 'put',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function (data) {
                    table.ajax.reload();
                    $("#edit-user-modal").modal('hide');
                },
                'error': function () {
                }
            });
        } else {
            $editUserForm.find(':submit').click();
            console.log("invalid form");
        }
    });
});