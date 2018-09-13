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
    }

    var groups = {
        "data": []
    };

    var groupTable = $('#groupTable').DataTable({
        ajax: {
            "url": "/analysis/group/" + $("#id").val()
        },
        data: groups.data,
        columnDefs: [{
            "targets": [0, 1],
            "visible": false,
            "searchable": false
        }],
        paging: false,
        columns: [{
            data: 'id'
        }, {
            data: 'queryId'
        }, {
            data: 'label'
        }, {
            data: 'downsample'
        }, {
            data: 'aggregation'
        }, {
            data: 'columns',
            render: function(data) {
                var cols = JSON.parse(data);
                return "<th><b>" + cols.type + "</b></br>" +
                    "<i>" + cols.electrodes.join(', ') + "</i></br>" +
                    cols.columns.join(', ') + "</th>";
            }
        }, {
            "width": "15%",
            data: 'id',
            render: function(data) {
                return "<th><button class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#edit-group-modal\" data-id=\"" + data + "\"><i class=\"zmdi zmdi-edit\"></i> Edit</button> " +
                    "<button class=\"btn btn-danger btn-sm\" data-toggle=\"modal\" data-target=\"#delete-group-modal\" data-id=\"" + data + "\"><i class=\"zmdi zmdi-close\"></i> Delete</a></th>";
            }
        }]
    });

    $("#uploadPatientList").change(function() {
        var fd = new FormData();
        fd.append("plist", document.getElementById('uploadPatientList').files[0]);
        $.ajax({
            type: "POST",
            url: "/api/export/patient_list/" + query.downsample.id,
            data: fd,
            contentType: false,
            cache: false,
            processData: false,
            success: function(result) {
                if (result.code === 1) {
                    $("#upload-plist-modal-body").text(result.msg + ": Update patient list successful.");
                    $("#upload-plist-modal-hdr").text("Success");
                } else {
                    $("#upload-plist-modal-body").text("Failed to update patient list. " + result.msg);
                    $("#upload-plist-modal-hdr").text("Error");
                }
                $("#upload-plist-modal").modal();
            },
            error: function(result) {
                $("#upload-plist-modal-body").text("Failed to update patient list. " + result.msg);
                $("#upload-plist-modal-hdr").text("Error");
                $("#upload-plist-modal").modal();
            }
        });
    });

    $("#delPlistModalButton").click(function() {
        $.ajax({
            type: "DELETE",
            url: "/api/export/patient_list/" + query.downsample.id,
            cache: false,
            success: function(result) {
                if (result.code === 1) {
                    $("#upload-plist-modal-body").text("Delete patient list successful");
                    $("#upload-plist-modal-hdr").text("Success");
                } else {
                    $("#upload-plist-modal-body").text("Failed to delete patient list. " + result.msg);
                    $("#upload-plist-modal-hdr").text("Error");
                }
                $("#upload-plist-modal").modal();
            },
            error: function(result) {
                $("#upload-plist-modal-body").text("Failed to delete patient list. " + result.msg);
                $("#upload-plist-modal-hdr").text("Error");
                $("#upload-plist-modal").modal();
            }
        });
    });

    $("#saveButton").click(function() {
        //        if ($('#parameter-form')[0].checkValidity()) {
        var form = {
            "id": $("#id").val(),
            "alias": $("#alias").val(),
            "period": $("#period").val() * $("#period_unit").val(),
            "origin": $("#origin").val() * $("#origin_unit").val(),
            "duration": $("#duration").val() * $("#duration_unit").val(),
            "minEveryBinThershold": $("#every_bin").val(),
            "minTotalBinThreshold": $("#total_bin").val(),
            "downsampleFirst": $('#downsample_first label.active input').val() == "true" ? true : false
        };
        console.log(form)
        ////            $.ajax({
        ////                'url': "/analysis/query",
        ////                'type': 'put',
        ////                'data': JSON.stringify(form),
        ////                'contentType': "application/json",
        ////                'dataType': 'json',
        ////                'success': function(data) {
        ////                    window.location.href = '/analysis/edit/' + $("#id").val();
        ////                },
        ////                'error': function() {}
        ////            });
        ////            return false;
        //        } else {
        //            console.log("invalid form");
        //            return true;
        //        }
    });

    $("#deleteButton").click(function() {
        var form = {
            "id": $("#id").val(),
        };
        $.ajax({
            'url': "/analysis/query",
            'type': 'delete',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                window.location.href = '/analysis/builder';
            },
            'error': function() {}
        });
    });

    $("#measure").change(function() {
        console.log($("#measure").val());
        if ($("#measure").val() == null) {
            return;
        }
        var form = [$("#measure").val()];
        $.ajax({
            'url': "/api/export/electrode",
            'type': 'post',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                console.log(data);
                var $electrode = $('#electrode');
                var $predefined = $('#predefined');

                // $electrode.attr("size", data.length + 1);
                // $electrode.removeAttr('multiple');
                $electrode.empty();
                $predefined.empty();
                $('#column').empty();
                // $('#column').attr("size", 1);
                $electrode.append('<option value="" disabled>Single Electrodes</option>');
                $predefined.append('<option value="" disabled>Predefined Sets</option>');

                for (var i = 0; i < data.electrodes.length; i++) {
                    var html = '<option value="' + data.electrodes[i].sid + '">' + data.electrodes[i].electrode + '</option>';
                    $electrode.append(html);
                }

                for (var i = 0; i < data.predefined.length; i++) {
                    var html = '<option value="' + data.predefined[i] + '">' + data.predefined[i] + '</option>';
                    $predefined.append(html);
                }
            },
            'error': function() {}
        });
    });

    $("#electrode").change(function() {
        console.log($("#electrode").val());
        var form = {
            "measure": [$("#measure").val()],
            "electrode": $("#electrode").val()
        };
        $("#predefined").val([]);
        $.ajax({
            'url': "/api/export/column",
            'type': 'post',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                var $column = $('#column');
                // $column.attr("size", data.length);
                $column.empty();
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i].column + '">' + data[i].representation + '</option>');
                }
                $column.change();
            },
            'error': function() {}
        });
    });

    $("#predefined").change(function() {
        console.log($("#predefined").val());
        var form = {
            "measure": [$("#measure").val()],
            "electrode": [$("#predefined").val()]
        };
        $("#electrode").val([]);
        $.ajax({
            'url': "/api/export/column",
            'type': 'post',
            'data': JSON.stringify(form),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                var $column = $('#column');
                // $column.attr("size", data.length);
                $column.empty();
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i].column + '">' + data[i].representation + '</option>');
                }
                $column.change();
            },
            'error': function() {}
        });
    });

    $("#method").select2({
        width: "100%",
        dropdownCssClass: "custom-dropdown"
    });

    $("#aggregation").select2({
        width: "100%",
        dropdownCssClass: "custom-dropdown"
    });

    var map = {};
    var eList = [];
    var cList = [];

    $("#addButton").click(function() {
        if ($("#column").val().length == 0) {
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", 'Please select at least one option from each\n category to form a valid column group.');
            return;
        }
        map = {};
        eList = [];
        cList = [];
        map.type = $("#measure").val();

        var form = $("#column").val();
        var $columnsInGroup = $('#columnsInGroup');
        var set = new Set();
        $("#columnsInGroup option").each(function() {
            set.add($(this).val());
        });

        $("#column :selected").each(function(i, sel) {
            cList.push($(sel).text());
        });
        map.columns = cList;

        if ($("#predefined").val() != null) {
            eList.push($("#predefined").val());
            map.electrodes = eList;
            form.forEach(function(e) {
                var electrode = $("#predefined").val();
                var start = electrode.split(' ')[2];
                var end = electrode.split(' ')[4];
                console.log(start + ' ' + end);
                for (i = parseInt(start.substring(1)); i <= parseInt(end.substring(1)); i++) {
                    console.log(i);
                    set.add('I' + i + e);
                }
            });
        } else {
            $("#electrode :selected").each(function(i, sel) {
                eList.push($(sel).text());
            });
            map.electrodes = eList;
            form.forEach(function(e) {
                var electrode = $("#electrode").val();
                electrode.forEach(function(element) {
                    console.log(element);
                    set.add(element + e);
                });
            });
        }

        $columnsInGroup.empty();
        $columnsInGroup.append('<option value="' + map.type + '">' + map.type + '</option>');
        map.electrodes.forEach(function(e) {
            $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
        });
        map.columns.forEach(function(e) {
            $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
        });

        console.log("map = ");
        console.log(map);
    });

    $("#clearButton").click(function() {
        var form = $("#columnsInGroup").val();
        var $columnsInGroup = $('#columnsInGroup');
        var set = new Object();
        $("#columnsInGroup option").each(function() {
            set[$(this).val()] = true;
        });
        console.log(set);
        form.forEach(function(e) {
            delete set[e];
        });
        console.log(set);
        $columnsInGroup.empty();
        var html = ""
        for (var e in set) {
            html += '<option value="' + e + '">' + e + '</option>';
        }
        $columnsInGroup.append(html);
    });

    $("#deleteGroupButton").click(function() {
        var id = $(this).attr('data-id');
        $.ajax({
            'url': "/analysis/group",
            'type': 'delete',
            'data': JSON.stringify(id),
            'contentType': "application/json",
            'dataType': 'json',
            'success': function(data) {
                $("#delete-group-complete-modal").modal();
            },
            'error': function() {}
        });
    });

    $("#add-success-modal").on('hide.bs.modal', function(event) {
        groupTable.ajax.reload();
    });

    $("#delete-group-complete-modal").on('hide.bs.modal', function(event) {
        groupTable.ajax.reload();
    });

    $('#delete-group-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        var id = button.data('id');
        var modal = $(this);
        console.log(id);
        $("#deleteGroupButton").attr('data-id', id);
    });

    var requestMethod;
    var groupId;

    $('#edit-group-modal').on('show.bs.modal', function(event) {
        $('#edit-group-modal').unbind('hidden');
        var button = $(event.relatedTarget);
        var id = button.data('id');
        var modal = $(this);
        if (id == 'none') {
            modal.find('.modal-title').text('Create New Aggregation Group');
            $('#addGroupButton').val('Create');

            $('#columnsInGroup').empty();
            $('#label').val('');
            $('#method').val('');
            $('#method').trigger('change');
            $('#aggregation').val('');
            $('#aggregation').trigger('change');
            requestMethod = 'post';
        } else {
            modal.find('.modal-title').text('Edit Aggregation Group ( Group ID : ' + id + ' )');
            $('#addGroupButton').val('Update');
            $.ajax({
                'url': "/analysis/group/group/" + id,
                'type': 'get',
                'success': function(data) {
                    $('#label').val(data.data.label);
                    $('#method').val(data.data.downsample);
                    $('#method').trigger('change');
                    $('#aggregation').val(data.data.aggregation);
                    $('#aggregation').trigger('change');

                    var columns = JSON.parse(data.data.columns);
                    map = columns;
                    var $columnsInGroup = $('#columnsInGroup');
                    $columnsInGroup.empty();
                    $columnsInGroup.append('<option value="' + columns.type + '">' + columns.type + '</option>');

                    columns.electrodes.forEach(function(e) {
                        $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
                    });
                    columns.columns.forEach(function(e) {
                        $columnsInGroup.append('<option value="' + e + '">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
                    });
                    requestMethod = 'put';
                    groupId = id;
                },
                'error': function() {}
            });
        }
        console.log(map);
        $('#measure').val('');
        $('#measure').trigger('change');
        $('#predefined').empty();
        $('#electrode').empty();
        $('#column').empty();
    });

    $("#addGroupButton").click(function() {
        if ($('#aggregation-form')[0].checkValidity()) {
            if (map.type == null) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", 'Please add at least one column to the final aggregation group list.');
                return false;
            }
            console.log(map);
            var form = {
                "id": groupId,
                "label": $("#label").val(),
                "queryId": $("#id").val(),
                "downsample": $("#method").val(),
                "aggregation": $("#aggregation").val(),
                "columns": JSON.stringify(map)
            };
            $.ajax({
                'url': "/analysis/group",
                'type': requestMethod,
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    $("#edit-group-modal").modal('hide');
                    if (requestMethod == 'post') {
                        $("#add-success-modal").find('.modal-body').text('Successfully added aggreagation group.');
                    } else {
                        $("#add-success-modal").find('.modal-body').text('Successfully updated aggreagation group.');
                    }
                    $('#edit-group-modal').on('hidden.bs.modal', function(event) {
                        $("#add-success-modal").modal();
                    });
                },
                'error': function() {}
            });
            return false;
        } else {
            console.log("invalid form");
            return true;
        }
    });

});