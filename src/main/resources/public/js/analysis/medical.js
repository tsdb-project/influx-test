$(document).ready(function() {

    $("#medicalButton").click(function() {

        if ($('#parameter-form')[0].checkValidity()) {
            var form = {
                "alias": $("#alias").val(),
                "medicine":$("#medicine").val(),
                "period": $("#period").val() * $("#period_unit").val(),
                "beforeMedicine": $("#beforemedicine").val() * $("#beforemedicine_unit").val(),
                "afterMedicine": $("#aftermedicine").val() * $("#aftermedicine_unit").val()
            };
            $.ajax({
                'url': "/analysis/medicalquery",
                'type': 'post',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    window.location.href = '/analysis/medicaledit/' + data.data.id;
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
    
    $('#medicine').autocomplete({
    serviceUrl: 'analysis/allMedicine',
    onSelect: function (suggestion) {
        alert('You selected: ' + suggestion.value + ', ' + suggestion.data);
    }
}); 
});