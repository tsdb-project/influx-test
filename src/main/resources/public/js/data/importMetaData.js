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

    $("#addFile").click(function () {
        if($("#file").val() == ""){
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "No file is selected!");
        }else{
            if($("#fileType").val() == null){
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", "Pleae select file type!");
            }else{

                var formData = new FormData();
                formData.append("file", document.getElementById('metadata').files[0]);

                console.log(formData['file']);

                // $("#addFile").attr('disabled', '');
                // $("#result").show();
                // $("#uploadResult").html(
                //     "<div class='alert alert-success form-group'> <span>" + $("#fileType").val()+ ": " + $("#file").val().substring(12) + " upload successful !</span> </div>"
                // )
            }
        }
    });


// new for import patients
    $("#submitFiles").click(function () {

        var regex = RegExp('PCASDatabase_DATA_[LABELS]?[0-9]{4}[-_][0-9]{2}[-_][0-9]{2}[-_][0-9]{4}.csv','g');
        // var pathParts = $('#file').val().split('\\');
        var pathParts = $('#directory').val().split('\\');

        if(regex.test(pathParts[pathParts.length-1])){
            $("#submitFiles").attr('disabled', '');

            $.ajax({
                'url' : "/api/data/importPatients",
                'type' : 'GET',
                'data' : {
                    dir: $('#directory').val()
                },
                'contentType' : "application/json",
                'dataType' : 'json',
                'success' : function(data) {
                    if(data.msg=="fail"){
                        window.alert("Successfully imported "+data.num+" patients, line" +data.num+1 +"is wrong");
                    }else {
                        window.alert("Successfully imported "+data.num+" patients, all done");
                    }

                },
                'error' : function() {
                    notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                        'Patient metadata import failed! ');
                }
            });

        }else {
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                'This may not be the correct file to import');
        }

    });


    $("#timeDrift").click(function () {
        $.ajax({
            'url' : "/api/data/timeDrift",
            'type' : 'GET',
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
                    'successful ! ');
            },
            'error' : function() {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'failed! ');
            }
        });
    })

});