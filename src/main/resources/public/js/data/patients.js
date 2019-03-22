$(document).ready(function() {
    var a = function() {
        $("#collapseExample").collapse('toggle');
    }
    var ctx = $("#myChart");
    
    var myChart = new Chart(ctx, {
        type : 'line',
        data : {
            labels : [ '2010-09-03 03:10:00', '2010-09-03 04:00:00', '2010-09-03 04:00:00', '2010-09-03 04:30:00', '2010-09-03 05:00:00', '2010-09-03 05:30:00', '2010-09-03 06:00:00'
            ],
            datasets : [ {
                steppedLine : 'before',
                pointRadius : 1,
                label: 'NoAR Files',
                backgroundColor : [ 'rgba(192, 135, 3, 0.2)'
                    ],
                    borderColor : [ 'rgba(192, 135, 3, 1)'
                    ],
                fill: true,
                data: [{
                    x: '2010-09-03 03:45:00',
                    y: 0
                }, {
                    x: '2010-09-03 03:45:00',
                    y: 2
                }, {
                    x: '2010-09-03 04:13:00',
                    y: 0
                }, {
                    x: '2010-09-03 04:22:00',
                    y: 2
                }, {
                    x: '2010-09-03 04:44:00',
                    y: 0
                }, {
                    x: '2010-09-03 04:55:00',
                    y: 2
                }, {
                    x: '2010-09-03 05:44:00',
                    y: 0
                }],
            },{
                steppedLine : 'before',
                pointRadius : 1,
                label: 'AR Files',
                backgroundColor : [ 'rgba(50, 3, 135, 0.2)'
                    ],
                    borderColor : [ 'rgba(50, 3, 135, 1)'
                    ],
                fill: true,
                data: [{
                    x: '2010-09-03 03:45:00',
                    y: 0
                }, {
                    x: '2010-09-03 03:45:00',
                    y: 1
                }, {
                    x: '2010-09-03 04:13:00',
                    y: 0
                }, {
                    x: '2010-09-03 04:26:00',
                    y: 1
                }, {
                    x: '2010-09-03 04:44:00',
                    y: 0
                }],
            }, {
                steppedLine : 'before',
                label : 'PROPOFOL',
                data : [ 24, 24, 30, 27, 24, 24, 24],
                backgroundColor : [ 'rgba(50, 199, 135, 0.2)'
                ],
                borderColor : [ 'rgba(50, 199, 135, 1)'
                ],
                borderWidth : 3,
                fill : true
            },
            {
                showLine: false,
                label : 'ASPIRING',
                data : [ 81, 81],
                backgroundColor : [ 'rgba(200, 5, 5, 0.2)'
                ],
                borderColor : [ 'rgba(200, 5, 5, 1)'
                ],
                borderWidth : 3,
                fill : true
            }
            ]
        },
        options : {
            elements: {
                line: {
                    steppedLine : 'before',
                }
            },
            events : [ "mousemove", "mouseout", "touchstart", "touchmove", "touchend", "click"
            ],
            onClick : a,
            scales : {
                xAxes: [{
                    type: 'time',
//                    distribution: 'series',
                    ticks: {
                        source: 'label'
                    },
                    time: {
                        unit: 'hour',
                        displayFormats: {
                            minute: 'h:mm a'
                        }
                    }
                }],
                yAxes : [ {
                    ticks : {
                        min : 0
                    }
                }
                ]
            }
        }
    });
    
    var files = {
        "data": []
    };

    var table = $('#patientTable').DataTable({
        ajax: {
            "url": "/apis/patients/find",
            "type": "POST"
        },
        data: files.data,
        columns: [{
            data: 'id'
        }, {
            data: 'age',
        }, {
            data: null,
            render: function(data) {
                if (data.female == '0') {
                    return 'Male';
                } else {
                    return 'Female';
                }
            }
        }],
        order: [
            [0, 'asc']
        ],
    });

    console.log(columns);

    $("#refreshButton").click(function() {
        table.ajax.reload();
    });
    
    var columnData = $.map(columns, function (obj) {
        obj.text = obj.text || obj.field; // replace name with the property
                                            // used for the text
        obj.id = obj.id || obj.field;
        return obj;
    });

    console.log(columnData);

    
    $(".field").select2({
        width: '100%',
        data : columnData
    });

    $(".operator").select2({
        width: '100%'
    });
    
    var wrapper = $("#filterForm"); // Fields wrapper
    var add_button = $("#addFilter"); // Add button ID

    var x = 1; // initlal text box count
    $(add_button).click(function(e) { // on add input button click
        e.preventDefault();
        var html = '<div class="row"><div class="col-sm-3 col-md-3"><select class="init-select2 field" data-placeholder="Filter Field" id="field[]" required><option disabled="disabled" selected="selected" value="">Filter Field</option></select></div><div class="col-sm-2 col-md-2"><select class="init-select2 operator" data-placeholder="Filter Method" id="operator[]" required><option value="=">=</option><option value="!=">&ne;</option><option value=">">&gt;</option><option value=">=">&ge;</option><option value="<">&lt;</option><option value="<=">&le;</option></select></div><div class="col-sm-2 col-md-2"><div class="input-group mb-3"><input type="text" class="form-control" id="value[]" placeholder="Input value" required></div></div><div class="col-sm-1 col-md-1" style="margin-top:6px"><a href="#" class="remove_field btn btn-sm btn-outline-danger">remove</a></div></div>';
        $(wrapper).append(html);

        $(".field").select2({
            width: '100%',
            data : columnData
        });

        $(".operator").select2({
            width: '100%'
        });
    });
    $(wrapper).on("click", ".remove_field", function(e) { // user click on
                                                            // remove text
        e.preventDefault();
        $(this).parent('div').parent('div').remove();
    });

});