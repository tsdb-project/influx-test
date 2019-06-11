$(document).ready(function() {
    var $createUserForm = $('#create-user-form');
    $createUserForm.on('submit', function(ev){
        ev.preventDefault();
        if ($createUserForm[0].checkValidity()) {
            var form = {
                "username" : $("#c_username").val(),
                "firstname" : $("#c_firstname").val(),
                "lastname" : $("#c_lastname").val(),
                "email":$("#c_email").val(),
                "enabled" : true,
                "role" : $('input[name=c_role]:checked', '#create-user-form').val()
            };
            $.ajax({
                'url' : "/user/user",
                'type' : 'put',
                'data' : JSON.stringify(form),
                'contentType' : "application/json",
                'dataType' : 'json',
                'success' : function(data) {
                    table.ajax.reload();
                    $("#create-user-modal").modal('hide');
                },
                'error' : function() {
                }
            });
        } else {
            $createUserForm.find(':submit').click();
            console.log("invalid form");
        }
    });
});