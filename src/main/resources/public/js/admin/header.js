$(document).ready(function () {
    console.log("header code");
    var username = $('#user_name').html();
    console.log(username);
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
});