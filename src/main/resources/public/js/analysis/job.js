$(document).ready(function() {
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

    var jobs = {
        "data": []
    };
    var patientList = null;

    $.fn.dataTable.moment('M/DD/YYYY, h:mm:ss a');
    var jobTable = $('#job-table').DataTable({
        ajax: {
            "url": "/api/analysis/job"
        },
        data: jobs.data,
        columnDefs: [{
            "targets": [0],
            "searchable": false
        }],
        order: [
            [0, "desc"]
        ],
        pagingType: "full_numbers",
        pageLength: 15,
        columns: [{
            data: 'id'
        }, {
            data: 'alias'
        }, {
            data: null,
            render: function(data) {
                return localeDateString(data.createTime)
            }
        }, {
            data:null,
            render: function(data){
                return (data.finishedPatient/data.allPatient).toFixed(4)*100+"%";
            }
        },{
            data: null,
            render: function(data) {
                if (data.finished) {
                    return "<th><button role=\"button\" class=\"btn btn-success btn-sm\" data-toggle=\"modal\" data-target=\"#download-modal\" " +
                        "data-type=\"download\" data-id = \"" + data.id + "\"><i class=\"zmdi zmdi-download\"></i> Download</button><th>"
                } else {
                    return "<th><button role=\"button\" class=\"btn btn-info btn-sm\" data-type=\"progress\" data-id=\"" +
                    data.id + "\"><i class=\"zmdi zmdi-settings zmdi-hc-spin\"></i> Check Progress</button><th>"
                }
            }
        }]
    });

    var currentPid;

    $('#job-table').on('click', 'button', function(event) {
        var status = event.target.dataset.type;
        if (status == 'download') {
            currentPid = event.target.dataset.id;
        }else if (status == 'progress' ) {
            alert("current job is still in progress");
        }
    });

    $('#complete-version-button').click(function () {
        window.location = '/download?path=archive/output_' + currentPid + '.zip&id=' + currentPid;
        $('#download-modal').modal('hide');
    });

    $('#split-version-button').click(function () {
        window.location = '/download?path=archive/output_split_' + currentPid + '.zip&id=' + currentPid;
        $('#download-modal').modal('hide');
    });

    setInterval( function () {
        jobTable.ajax.reload();
    }, 15000 );
    
    function localeDateString(date) {
        var options = {
            hour12: true,
            timeZone: "America/New_York"
        };
        return new Date(date).toLocaleString('en-US', options);
    }
});