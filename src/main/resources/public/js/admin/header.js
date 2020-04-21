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
                $("#first_name").html(data.data.firstName);
            },
            'error': function () {
            }
        });
    }else{
        $("#username").html( 'anonymous user' );
        $("#first_name").html("please login as an authorize user");
    }
});