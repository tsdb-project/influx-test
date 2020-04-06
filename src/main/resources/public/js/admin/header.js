$(document).ready(function () {
    var username = $('#user_name').html();

    if(username != 'anonymousUser'){
        $.ajax({
            'url': "/user/getUserByName/"+username,
            'type': 'get',
            'contentType': "application/json",
            'dataType': 'json',
            'success': function (data) {
                $("#username").html(data.data.username);
                $("#user_version").html(data.data.databaseVersion);
            },
            'error': function () {
            }
        });
    }else{
        $("#username").html( username );
    }
});