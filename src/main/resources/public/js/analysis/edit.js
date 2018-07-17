$(document).ready(function() {
    function notify(from, align, icon, type, animIn, animOut) {
        $.notify({
            icon: icon,
            title: '',
            message: 'Please add at least one column to the final aggregation group list.',
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
            data: 'group.id'
        }, {
            data: 'group.queryId'
        }, {
            data: 'group.label'
        }, {
            data: 'group.downsample'
        }, {
            data: 'group.aggregation'
        }, {
            data: 'columns'
        }, {
            "width": "15%",
            data: 'group.id',
            render: function(data) {
                return "<th><button class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#edit-group-modal\" data-id=\"" + data + "\"><i class=\"zmdi zmdi-edit\"></i> Edit</button> " +
                    "<button class=\"btn btn-danger btn-sm\" data-toggle=\"modal\" data-target=\"#delete-group-modal\" data-id=\"" + data + "\"><i class=\"zmdi zmdi-close\"></i> Delete</a></th>";
            }
        }]
    });

    $("#saveButton").click(function() {
        if ($('#parameter-form')[0].checkValidity()) {
            var form = {
                "id": $("#id").val(),
                "alias": $("#alias").val(),
                "period": $("#period").val() * $("#period_unit").val(),
                "origin": $("#origin").val() * $("#origin_unit").val(),
                "duration": $("#duration").val() * $("#duration_unit").val()
            };
            $.ajax({
                'url': "/analysis/query",
                'type': 'put',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    window.location.href = '/analysis/edit/' + $("#id").val();
                },
                'error': function() {}
            });
            return false;
        } else {
            console.log("invalid form");
            return true;
        }
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
                // $electrode.attr("size", data.length + 1);
                $electrode.removeAttr('multiple');
                $electrode.empty();
                $('#column').empty();
                // $('#column').attr("size", 1);
                for (var i = 0; i < data.length; i++) {
                    var html = '<option value="' + data[i] + '">' + data[i] + '</option>';
                    $electrode.append(html);
                }
            },
            'error': function() {}
        });
    });

    $("#electrode").change(function() {
        console.log($("#electrode").val());
        var form = {
            "measure": [$("#measure").val()],
            "electrode": [$("#electrode").val()]
        };
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
                    $column.append('<option value="' + data[i] + '">' + data[i] + '</option>');
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
    
    $("#addButton").click(function() {
        var form = $("#column").val();
        var $columnsInGroup = $('#columnsInGroup');
        var set = new Set();
        $("#columnsInGroup option").each(function() {
            set.add($(this).val());
        });
        form.forEach(function(e) {
            if (e.startsWith('Ix')) {
                var electrode = $("#electrode").val();
                var start = electrode.split(' ')[2];
                var end = electrode.split(' ')[4];
                console.log(start + ' ' + end);
                for (i = parseInt(start.substring(1)); i <= parseInt(end.substring(1)); i++) {
                    console.log(i);
                    set.add('I' + i + '_' + e.substring(3));
                }
            } else {
                set.add(e);
            }
        });
        $columnsInGroup.empty();
        set.forEach(function(e) {
            var html = '<option value="' + e + '">' + e + '</option>';
            $columnsInGroup.append(html);
        });
        // $columnsInGroup.attr('size',
        // $('#columnsInGroup').children('option').length);
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
        // var length = $('#columnsInGroup').children('option').length;
        // $columnsInGroup.attr('size', length > 0 ? length : 1);
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
        $('#edit-group-modal').unbind('hidden');;
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
                    $('#label').val(data.data.group.label);
                    $('#method').val(data.data.group.downsample);
                    $('#method').trigger('change');
                    $('#aggregation').val(data.data.group.aggregation);
                    $('#aggregation').trigger('change');

                    var columns = data.data.columns.split(', ');
                    var $columnsInGroup = $('#columnsInGroup');
                    var set = new Set();
                    columns.forEach(function(e) {
                        set.add(e);
                    });
                    $columnsInGroup.empty();
                    set.forEach(function(e) {
                        var html = '<option value="' + e + '">' + e + '</option>';
                        $columnsInGroup.append(html);
                    });
                    requestMethod = 'put';
                    groupId = id;
                },
                'error': function() {}
            });
        }
        $('#measure').val('');
        $('#measure').trigger('change');
        $('#electrode').empty();
        $('#column').empty();
    });

    $("#addGroupButton").click(function() {
        var columns = [];
        $("#columnsInGroup option").each(function() {
            columns.push($(this).val());
        });
        columns = columns.join(", ");
        if ($('#aggregation-form')[0].checkValidity()) {
            if (columns.length == 0) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut");
                return false;
            }
            var form = {
                "group": {
                    "id": groupId,
                    "label": $("#label").val(),
                    "queryId": $("#id").val(),
                    "downsample": $("#method").val(),
                    "aggregation": $("#aggregation").val(),
                    "columns": columns
                },
                "columns": columns
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