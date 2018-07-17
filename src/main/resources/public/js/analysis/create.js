$(document).ready(function() {

    $("#createButton").click(function() {

        if ($('#parameter-form')[0].checkValidity()) {
            var form = {
                "alias": $("#alias").val(),
                "period": $("#period").val() * $("#period_unit").val(),
                "origin": $("#origin").val() * $("#origin_unit").val(),
                "duration": $("#duration").val() * $("#duration_unit").val()
            };
            $.ajax({
                'url': "/analysis/query",
                'type': 'post',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    window.location.href = '/analysis/edit/' + data.data.id;
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