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


    $('#register').click(function () {
        $.ajax({
            'url' : "/user/getDBVersion",
            'type' : 'get',
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                for(var i=0;i<data.data.size();i++){
                    $('#versionSelect').append("<option value=\""+data.data[i]+"\">Select"+data.data[i]+"</option>");
                }
            }
        });
    });


    var $createUserForm = $('#create-user-form');
    $createUserForm.on('submit', function(ev){
        ev.preventDefault();

        if ( $("#c_password").val() === $("#c_password2").val()){

            if ($createUserForm[0].checkValidity()){
                var form = {
                    "username" : $("#c_username").val(),
                    "firstName" : $("#c_firstname").val(),
                    "lastName" : $("#c_lastname").val(),
                    "email":$("#c_email").val(),
                    "password":$("#c_password").val(),
                    "enabled" : true,
                    "role" : "ROLE_USER"
                };

                $.ajax({
                    'url' : "/user/user",
                    'type' : 'put',
                    'data' : JSON.stringify(form),
                    'contentType' : "application/json",
                    'dataType' : 'json',
                    'success' : function(data) {
                        if(data.code == 0){
                            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                                'found user with the same user name!');
                        }else{
                            notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
                                'register success!');
                            setTimeout("window.location.href = '/login'",1000);
                        }
                    },
                    'error' : function() {
                        notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                            'register failed!');
                    }
                });
            }else{
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Email invalid, please try again!');
            }
        } else {
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                'Two passwords do not match, please try again!');
        }
    });
});