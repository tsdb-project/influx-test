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

    /*
    * get database information
    * */
    var databaseData = null;
    
//    function getDatabases(){
//        $.ajax({
//            'url': "/aggregation/getUsefulDBs?" + "periodVal=" + $("#period").val() + "andperiodUnit=" + $("#period_unit").val() + "andoriginVal=" + $("#origin").val() + "andoriginUnit=" + $("#origin_unit").val() + "anddurationVal=" + $("#duration").val() + "anddurationUnit=" + $("#duration_unit").val(),
//            'type': 'get',
//            'contentType':"application/json",
//            'dataType':"json",
//            'async': false,
//            'success': function(data) {
//                databaseData = data.data;
//            },
//            'error': function() {}
//        });
//        console.log(databaseData);
//    }
//    getDatabases();

    var groups = {
        "data": []
    };
    var patientList = null;

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
                return "<th><button class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#edit-group-modal\" data-id=\"" +
                    data + "\"><i class=\"zmdi zmdi-edit\"></i> Edit</button> " +
                    "<button class=\"btn btn-danger btn-sm\" data-toggle=\"modal\" data-target=\"#delete-group-modal\" data-id=\"" +
                    data + "\"><i class=\"zmdi zmdi-close\"></i>Delete</a></th>";
            }
        }],

    });

    $("#uploadPatientList").change(function() {
        var formData = new FormData();
        formData.append("plist", document.getElementById('uploadPatientList').files[0]);
        $.ajax({
            type: "POST",
            url: "/api/export/patient_list/",
            data: formData,
            contentType: false,
            cache: false,
            processData: false,
            success: function(result) {
                if (result.code == 1) {
                    notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut",
                        'Successfully uploaded patient list.');
                    patientList = result.data;
                    console.log(patientList);
                } else {
                    notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                        'Failed to upload patient list.');
                }
            },
            error: function(result) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Failed to upload patient list.');
            }
        });
    });

    $("#clear-patient-list").click(function() {
        notify("top", "center", null, "info", "animated bounceIn", "animated fadeOut", 'Patient list cleared.');
        $("#uploadPatientList").val('');
        patientList = null;
    });

    $("#export-modal").on('hidden.bs.modal', function (e) {
    	$("#uploadPatientList").val('');
        patientList = null;
    });
    
    var downsampleMethods = [];
    
    
    //suggest usable time levels
    //whether dsMethod counts: ML models use "stds" in high time-levels, so it can wait...
    //not considered now 
    function usableTimeLevel(period, origin, duration) {
        var suggest = {
            "1": true,
            "60": false,
            "300": false,
            "600": false,
            "900": false,
            "1800": false,
            "3600": false,  
        };
        var levelsInSec = [60, 300, 600, 900, 1800, 3600];
        for (level of levelsInSec) {
            if ((origin == null || (origin != null && origin % level == 0)) && period % level == 0) {
                suggest[level] = true;
            }
        }
        return suggest;
    }

    var usableAggDB = [{
            // id: null,
            // version: null,
            // createTime: null,
            // status: null,
            // total: null,
            // finished: null,
            // artype: null,
            // timeCost: null,
            // nday: 0,
            // comment: null,
            // dbSize: null,
            // pidList: null
        }];

    var usableTable = $('#usableAggdbTable').DataTable({
        data : usableAggDB,
        columns: [ {
            data : 'id'
        }, {
            data : null,
            render : function(data) {
                console.log('ar: ' + data);
                return data.artype ? "AR" : "NOAR";
            }
        }, {
            data : 'timeCost'
        }, {
            data : null,
            render: function(data) {
                return data.pidList == null ? "ALL" : "SOME";
            }
        }, {
            data : 'dbSize'
        }, {
            data : 'version'
        }, {
            data : null,
            render : function(data, type, row, meta) {
                console.log(meta);
                return '<button class="btn btn-info btn-sm" data-toggle="modal" data-target="#comment-modal" data-row="' + meta.row + '"><i class="zmdi zmdi-edit"></i>Check comment</button>'
            }
        }]
    });

    //auto refresh useable Databases table
    $('#refresh').click(function() {
        console.log("click");
        var period = $("#period").val() * $("#period_unit").val();
        var origin = $("#origin").val() * $("#origin_unit").val();
        var duration = $("#duration").val() * $("#duration_unit").val();
        var dsMethod = $('#method').val();
        console.log(patientList);
        console.log(usableTimeLevel(period, origin, duration));
        var para = {
            // "dsmethod": dsMethod, //not important
            "patientList": patientList,
            "ar": $('#ar label.active input').val() == "true"
        }
        $.ajax({
            url: "/aggregation/getUsableDBs",
            type: 'post',
            data: JSON.stringify(para),
            contentType: "application/json",
            dataType: 'json',
            success: function (data) {
                usableAggDB = data.data;
                console.log(usableAggDB);
                usableTable.clear().rows.add(usableAggDB).draw();
            }
        });

    });

    



    $('#comment-modal').on('show.bs.modal', function(event) {
        var button = $(event.relatedTarget);
        console.log(button);
        var id = button.data('row');
        console.log(id);
        $.ajax({
            'url': "/aggregation/getDBs",
            'type': 'get',
            'success': function(data) {
                var agg_dbs = data.data;
                var agg_db = agg_dbs[id];
                $("#agg_db_id").val(agg_db.id);
                $("#agg_db_comment").val(agg_db.comment);
            },
            'error': function() {}
        });
        
    });
    
    $("#edit_comment").click(function () {
        console.log("edit button");
        
        var aggregationdb = {
            "id":$("#agg_db_id").val(),
            "comment":$("#agg_db_comment").val()
        };
        $.ajax({
            'url' : "/aggregation/setComment",
            'type' : 'put',
            'data' : JSON.stringify(aggregationdb),
            'contentType' : "application/json",
            'dataType' : 'json',
            'success' : function(data) {
                notify("top", "center", null, "success", "animated fadeIn", "animated fadeOut", "edit comment success");
            },
            'error' : function() {
            }
        });
    });




    $("#export").click(function (e) {
    	var dbList = document.getElementById("databases");
    	var l = document.getElementById("databases").length;
    	for(var i = 0; i < l; i++){
    		dbList.remove(0);
    	}
//      	$("#uploadPatientList").val('');
//        patientList = null;
    	if ($('#parameter-form')[0].checkValidity()){
            var period = $("#period").val() * $("#period_unit").val();
            var origin = $("#origin").val() * $("#origin_unit").val();
            var duration = $("#duration").val() * $("#duration_unit").val();
            var dsMethod = $('#method').val();
            $.ajax({
            	'url': "/aggregation/getUsefulDBs?" + "period=" + period + "&origin=" + origin + "&duration=" + duration + 
            		"&method=" + dsMethod
            		// "&min=" + exportMethod.Min +
            		// "&mean=" + exportMethod.Mean +
            		// "&median=" + exportMethod.Median +
            		// "&std=" + exportMethod.Std +
            		// "&fq=" + exportMethod.FQ +
            		// "&tq=" + exportMethod.TQ +
                    // "&sum=" + exportMethod.Sum
                    ,
            	'type': 'get',
            	'contentType':"application/json",
            	'dataType':"json",
            	'async': false,
            	'success': function(data) {
            		databaseData = data.data;
            	},
            	'error': function() {}
            });
//        console.log(databaseData);
            for(var i = 0; i < databaseData.length; i++){
            	var data = databaseData[i];
            	var option = document.createElement("option");
            	option.text = data.dbName;
//      	  	console.log(data);
            	dbList.add(option);
            }
    	}
    });
    
    $("#cancelButton").click(function(e){
    	var dbList = document.getElementById("databases");
    	var l = document.getElementById("databases").length;
    	for(var i = 0; i < l; i++){
    		dbList.remove(0);
    	}
    })


    $("#submitJobButton").click(function () {
        $("#submitJobButton").attr('disabled', 'disabled');
        var form = {
            "queryId": $("#id").val(),
            "patientList": patientList,
            "layout": $('#layout label.active input').val() == "true",
            "ar": $('#ar label.active input').val() == "true",
            "dbType": $('#selectdb label.active input').val(),
            "username":$('#user_name').html(),
            "fromDb": $('#databases').val(),

        };
        $.ajax({
            url: "/api/export/export/",
            type: 'post',
            data: JSON.stringify(form),
            contentType: "application/json",
            dataType: 'json',
            success: function () {
                window.location.href = '/analysis/job';
            },
            error: function () {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Failed to submit this job, please try again.');
            }
        });
    });

    $("#saveButton").click(function() {
        if ($('#parameter-form')[0].checkValidity()) {
            var form = {
                "id": $("#id").val(),
                "alias": $("#alias").val(),
                "period": $("#period").val() * $("#period_unit").val(),
                "origin": $("#origin").val() * $("#origin_unit").val(),
                "duration": $("#duration").val() * $("#duration_unit").val(),
                "downsampleFirst": $('#downsample_first label.active input').val() == "true" ? true : false
            };
            if ($("#minBinRowUnit").val() == '%') {
                form.minBinRow = form.period * $("#min_bin_row").val() / 100;
            } else {
                form.minBinRow = $("#min_bin_row").val() * $("#minBinRowUnit").val();
            }
            if ($("#minBinUnit").val() == '%') {
                form.minBin = form.duration / form.period * $("#min_bin").val() / 100;
            } else {
                form.minBin = $("#min_bin").val();
            }
            console.log(form);
            if (form.minBinRow > form.period) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Minimal row for a valid bin exceeded bin size.');
                $("#min_bin_row").addClass('is-invalid');
                return false;
            }
            if ($("#minBinUnit").val() == '%' && (form.minBin < 0 || form.minBin > 100)) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", 'Invalid percentage entered.');
                $("#min_bin").addClass('is-invalid');
                return false;
            }
            if ($("#minBinUnit").val() == '1' && form.duration != 0 && form.minBin > (form.duration / form.period)) {
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Minimal bin number for a valid patient exceeded max bin count.');
                $("#min_bin").addClass('is-invalid');
                return false;
            }
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
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut", 'Invalid form.');
            return false;
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

                $electrode.empty();
                $predefined.empty();
                $('#column').empty();
                $electrode.append('<option value="" disabled>Single Electrodes</option>');
                $predefined.append('<option value="" disabled>Predefined Sets</option>');

                for (var i = 0; i < data.electrodes.length; i++) {
                    var html = '<option value="' + data.electrodes[i].sid + '">' + data.electrodes[i].electrode +
                        '</option>';
                    $electrode.append(html);
                }

                for (var i = 0; i < data.predefined.length; i++) {
                    var html = '<option value="' + data.predefined[i].value + '">' + data.predefined[i].key + '</option>';
                    $predefined.append(html);
                }
                var predefinedSize = data.predefined.length > 0 ? data.predefined.length + 1 : 2;
                $predefined.attr('size', predefinedSize);
                $electrode.attr('size', 13 - predefinedSize);
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
            notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                'Please select at least one option from each\n category to form a valid column group.');
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
            eList.push($("#predefined :selected").text());
            map.electrodes = eList;
            if ($("#predefined").val().startsWith('* ')) {
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
                set.add($("#predefined").val());
            } 
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
        console.log(id);
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
                        $columnsInGroup.append('<option value="' + e +
                            '">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp' + e + '</option>');
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
                notify("top", "center", null, "danger", "animated bounceIn", "animated fadeOut",
                    'Please add at least one column to the final aggregation group list.');
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

    var exportMethod = {
            Max:false,
            Min:false,
            Mean:false,
            Median:false,
            Std:false,
            FQ:false,
            TQ:false,
            Sum:false
        };
    var exportFinalMethod = null;
    
    var maxBtn = $('#MaxBtn');
    var minBtn = $('#MinBtn');
    var meanBtn = $('#MeanBtn');
    var medianBtn = $('#MedianBtn');
    var stdBtn = $('#StdBtn');
    var fqBtn = $('#FQBtn');
    var tqBtn = $('#TQBtn');
    var sumBtn = $('#SumBtn');
    
    $('#param-modal').on('show.bs.modal', function(event) {

        if(exportMethod.Max){
            maxBtn.removeClass("btn-light");
            maxBtn.addClass("btn-primary");
        }else{
            maxBtn.addClass("btn-light");
            maxBtn.removeClass("btn-primary");
        }

        if(exportMethod.Min){
            minBtn.removeClass("btn-light");
            minBtn.addClass("btn-primary");
        }else{
            minBtn.addClass("btn-light");
            minBtn.removeClass("btn-primary");
        }

        if(exportMethod.Mean){
            meanBtn.removeClass("btn-light");
            meanBtn.addClass("btn-primary");
        }else{
            meanBtn.addClass("btn-light");
            meanBtn.removeClass("btn-primary");
        }

        if(exportMethod.Median){
            medianBtn.removeClass("btn-light");
            medianBtn.addClass("btn-primary");
        }else{
            medianBtn.addClass("btn-light");
            medianBtn.removeClass("btn-primary");
        }

        if(exportMethod.Std){
            stdBtn.removeClass("btn-light");
            stdBtn.addClass("btn-primary");
        }else{
            stdBtn.addClass("btn-light");
            stdBtn.removeClass("btn-primary");
        }

        if(exportMethod.FQ){
            fqBtn.removeClass("btn-light");
            fqBtn.addClass("btn-primary");
        }else{
            fqBtn.addClass("btn-light");
            fqBtn.removeClass("btn-primary");
        }

        if(exportMethod.TQ){
            tqBtn.removeClass("btn-light");
            tqBtn.addClass("btn-primary");
        }else{
            tqBtn.addClass("btn-light");
            tqBtn.removeClass("btn-primary");
        }

        if(exportMethod.Sum){
            sumBtn.removeClass("btn-light");
            sumBtn.addClass("btn-primary");
        }else{
            sumBtn.addClass("btn-light");
            sumBtn.removeClass("btn-primary");
        }
    });

    
    maxBtn.click(function () {
        if(! exportMethod.Max){
        	exportMethod.Max = true;
            maxBtn.removeClass("btn-light");
            maxBtn.addClass("btn-primary");
        }else{
        	exportMethod.Max = false;
            maxBtn.addClass("btn-light");
            maxBtn.removeClass("btn-primary");
        }
    });

    minBtn.click(function () {
        if(! exportMethod.Min){
        	exportMethod.Min = true;
            minBtn.removeClass("btn-light");
            minBtn.addClass("btn-primary");
        }else{
        	exportMethod.Min = false;
            minBtn.addClass("btn-light");
            minBtn.removeClass("btn-primary");
        }
    });

    meanBtn.click(function () {
        if(! exportMethod.Mean){
        	exportMethod.Mean = true;
            meanBtn.removeClass("btn-light");
            meanBtn.addClass("btn-primary");
        }else{
        	exportMethod.Mean = false;
            meanBtn.addClass("btn-light");
            meanBtn.removeClass("btn-primary");
        }
    });

    medianBtn.click(function () {
        if(! exportMethod.Median){
        	exportMethod.Median = true;
            medianBtn.removeClass("btn-light");
            medianBtn.addClass("btn-primary");
        }else{
        	exportMethod.Median = false;
            medianBtn.addClass("btn-light");
            medianBtn.removeClass("btn-primary");
        }
    });

    stdBtn.click(function () {
        if(! exportMethod.Std){
        	exportMethod.Std = true;
            stdBtn.removeClass("btn-light");
            stdBtn.addClass("btn-primary");
        }else{
        	exportMethod.Std = false;
            stdBtn.addClass("btn-light");
            stdBtn.removeClass("btn-primary");
        }
    });

    fqBtn.click(function () {
        if(! exportMethod.FQ){
        	exportMethod.FQ = true;
            fqBtn.removeClass("btn-light");
            fqBtn.addClass("btn-primary");
        }else{
        	exportMethod.FQ = false;
            fqBtn.addClass("btn-light");
            fqBtn.removeClass("btn-primary");
        }
    });

    tqBtn.click(function () {
        if(! exportMethod.TQ){
        	exportMethod.TQ = true;
            tqBtn.removeClass("btn-light");
            tqBtn.addClass("btn-primary");
        }else{
        	exportMethod.TQ = false;
            tqBtn.addClass("btn-light");
            tqBtn.removeClass("btn-primary");
        }
    });

    sumBtn.click(function () {
        if(! exportMethod.Sum){
        	exportMethod.Sum = true;
            sumBtn.removeClass("btn-light");
            sumBtn.addClass("btn-primary");
        }else{
        	exportMethod.Sum = false;
            sumBtn.addClass("btn-light");
            sumBtn.removeClass("btn-primary");
        }
    });
    
    $('#saveExportMethod').click(function () {
        notify("top", "center", null, "success", "animated bounceIn", "animated fadeOut", 'aggregation methods Saved.');
        aggFinalMethod = aggMethod;
    });
});