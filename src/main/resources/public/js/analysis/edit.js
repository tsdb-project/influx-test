$(document).ready(function() {
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
        paging : false,
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
            "width": "20%",
            data: 'group.id',
            render: function(data) {
                return "<th><a class=\"btn btn-primary btn-sm\" role=\"button\" data-toggle=\"modal\" data-target=\"#edit-group-modal\" data-id=\"" + data + "\"><i class=\"zmdi zmdi-edit\"></i> Edit</a> " +
                       "<a class=\"btn btn-danger btn-sm\" role=\"button\" href=\"/analysis/group/delete/" + data + "\"><i class=\"zmdi zmdi-close\"></i> Delete</a></th>";
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
                $electrode.attr("size", data.length + 1);
                $electrode.removeAttr('multiple');
                $electrode.empty();
                $('#column').empty();
                $('#column').attr("size", 1);
                $electrode.append('<option disabled="disabled" selected="selected" value="">Select Electrodes</option>');
                for (var i = 0; i < data.length; i++) {
                    var html = '<option value="' + data[i] + '">' + data[i] + '</option>';
                    $electrode.append(html);
                }
            },
            'error': function() {}
        });
    });

    $("#addButton").click(function() {
        var form = $("#column").val();
        var $columnsInGroup = $('#columnsInGroup');
        var set = new Set();
        $("#columnsInGroup option").each(function() {
            set.add($(this).val());
        });
        form.forEach(function(e) {
            set.add(e);
        });
        $columnsInGroup.empty();
        set.forEach(function(e) {
            var html = '<option value="' + e + '">' + e + '</option>';
            $columnsInGroup.append(html);
        });
        $columnsInGroup.attr('size', $('#columnsInGroup').children('option').length);
    });

    $("#clearButton").click(function() {
        var form = $("#columnsInGroup").val();
        var $columnsInGroup = $('#columnsInGroup');
        var set = new Set();
        $("#columnsInGroup option").each(function() {
            set.add($(this).val());
        });
        form.forEach(function(e) {
            set.delete(e);
        });
        $columnsInGroup.empty();
        set.forEach(function(e) {
            var html = '<option value="' + e + '">' + e + '</option>';
            $columnsInGroup.append(html);
        });
        var length = $('#columnsInGroup').children('option').length;
        $columnsInGroup.attr('size', length > 0 ? length : 1);
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
                $column.attr("size", data.length)
                $column.empty();
                for (var i = 0; i < data.length; i++) {
                    $column.append('<option value="' + data[i] + '">' + data[i] + '</option>');
                }
                $column.change();
            },
            'error': function() {}
        });
    });

    $("#addGroupButton").click(function() {
        var columns = [];
        $("#columnsInGroup option").each(function() {
            columns.push($(this).val());
        });
        if ($('#aggregation-form')[0].checkValidity()) {
            if (columns.length == 0) {
                $("#modal-empty").modal();
                return false;
            }
            var form = {
                "group": {
                    "label": $("#label").val(),
                    "queryId": $("#id").val(),
                    "downsample": $("#method").val(),
                    "aggregation": $("#aggregation").val()
                },
                "columns": columns
            };
            $.ajax({
                'url': "/analysis/group",
                'type': 'post',
                'data': JSON.stringify(form),
                'contentType': "application/json",
                'dataType': 'json',
                'success': function(data) {
                    groupTable.ajax.reload();
                    $("#add-success-modal").modal();
                },
                'error': function() {}
            });
            return false;
        } else {
            console.log("invalid form");
            return true;
        }
    });
    
    $('#edit-group-modal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget) 
        var id = button.data('id') 
        var modal = $(this)
// modal.find('.modal-body').html(id)
      })

    
});