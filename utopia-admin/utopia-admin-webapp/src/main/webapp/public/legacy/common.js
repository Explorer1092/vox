/**
 * Common utilities for crm task & task record; eg. number, string, array, dater .
 * @author Jia HuanYin
 * @since 2015/10/19
 */

function validNumber(value) {
    return !isNaN(value) && parseInt(value) > 0;
}

function nullString(value) {
    return value == undefined || value == null;
}

function emptyString(value) {
    return nullString(value) || value === "";
}

function blankString(value) {
    return $.trim(value) === "";
}

function emptyArray(source) {
    return source == null || source.length < 1;
}

function stringArray(source) {
    if (emptyArray(source)) {
        return "";
    }
    var values = "";
    for (var i in source) {
        values += source[i] + ",";
    }
    return values;
}

function stringToArray(source, separator) {
    if (blankString(source)) {
        return [];
    }
    var arr = source.split(separator);
    var ret = [];
    for (var i in arr) {
        if(!blankString(arr[i])){
            ret.push(arr[i])
        }
    }
    return ret;
}

function closeDialog(id) {
    $("#" + id).dialog("close");
}

var dater = {};
dater.render = function () {
    $(".date").datepicker({
        dateFormat: "yy-mm-dd",
        monthNames: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
        monthNamesShort: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: true,
        changeYear: true
    });
};

dater.parse = function (format, value) {
    if (blankString(format) || blankString(value)) {
        return null;
    }
    try {
        return $.datepicker.parseDate(format, value);
    } catch (e) {
    }
    return null;
};

var region = {};
region.render = function (provinceCode, cityCode, countyCode) {
    if (validNumber(provinceCode)) {
        $("#provinceCode").val(provinceCode);
    }
    region.cities(provinceCode, cityCode, countyCode);
};

region.provinces = function (provinceCode, cityCode, countyCode) {
    $("#provinceCode").empty().append("<option value=''>全部</option>");
    var provinces = provinceRegions();
    for (var i in provinces) {
        var province = provinces[i];
        $("#provinceCode").append("<option value='" + province.provinceCode + "'>" + province.provinceName + "</option>");
    }
    if (validNumber(provinceCode)) {
        $("#provinceCode").val(provinceCode);
    }
    region.cities(provinceCode, cityCode, countyCode);
};
function provinceRegions() {
    var provinces = null;
    $.ajax({
        url: "/crm/task/province_regions.vpage",
        type: "POST",
        async: false,
        success: function (data) {
            if (!data) {
                alert("区域信息获取失败！");
            } else {
                provinces = data;
            }
        }
    });
    return provinces;
}

region.cities = function (provinceCode, cityCode, countyCode) {
    $("#cityCode").empty().append("<option value=''>全部</option>");
    if (validNumber(provinceCode)) {
        var cities = childRegions(provinceCode);
        for (var i in cities) {
            var city = cities[i];
            $("#cityCode").append("<option value='" + city.code + "'>" + city.name + "</option>");
        }
        if (validNumber(cityCode)) {
            $("#cityCode").val(cityCode);
        }
    }
    region.counties($("#cityCode").val(), countyCode);
};

region.counties = function (cityCode, countyCode) {
    $("#countyCode").empty().append("<option value=''>全部</option>");
    if (validNumber(cityCode)) {
        var counties = childRegions(cityCode);
        for (var i in counties) {
            var county = counties[i];
            $("#countyCode").append("<option value='" + county.code + "'>" + county.name + "</option>");
        }
        if (validNumber(countyCode)) {
            $("#countyCode").val(countyCode);
        }
    }
};
function childRegions(regionCode) {
    var children = null;
    $.ajax({
        url: "/crm/task/child_regions.vpage",
        type: "POST",
        async: false,
        data: {
            "regionCode": regionCode
        },
        success: function (data) {
            if (!data) {
                alert("区域信息获取失败！");
            } else {
                children = data;
            }
        }
    });
    return children;
}

var task = {"ACTION": {"NEW": "TASK_NEW", "FORWARD": "TASK_FORWARD", "RECORD": "RECORD_NEW"}};
task.show = function (taskId) {
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        var task = taskDetail(taskId);
        if (task == null) {
            alert("记录获取失败！");
        } else {
            fillTask(task);
            taskStubs(task.taskStubs);
            iviewTask(task);
            $("#task-detail").dialog({
                height: "auto",
                width: "700",
                autoOpen: true,
                title: "任务详情"
            });
        }
    }
};
function taskDetail(taskId) {
    var task = null;
    $.ajax({
        url: "/crm/task/task_detail.vpage",
        type: "POST",
        async: false,
        data: {
            "taskId": taskId
        },
        success: function (data) {
            task = data;
        }
    });
    return task;
}
function fillTask(task) {
    $("#task-detail").attr("task-id", task.id);
    $("#task-detail").attr("user-id", task.userId);
    $("#task-detail").attr("task-type", task.type);
    $("#task-show-creator").text(task.creatorName);
    $("#task-show-executor").text(task.executorName);
    $("#task-edit-executor").text(task.executorName);
    $("#task-show-type").text(task.type);
    $("#task-edit-type").text(task.type);
    $("#task-show-status").text(task.niceStatus);
    $("#task-edit-status").val(task.status);
    $("#task-show-createTime").text(task.niceCreateTime);
    $("#task-show-endTime").text(task.niceEndTime);
    $("#task-edit-endTime").val(task.niceEndTime);
    $("#task-show-user").text(task.userName);
    $("#task-show-user").attr("href", userUrl(task));
    $("#task-show-userId").text(task.userId);
    $("#task-show-title").text(task.title);
    $("#task-edit-title").text(task.title);
    $("#task-show-content").text(task.content);
    $("#task-edit-content").val(task.content);
}
function userUrl(task) {
    if (task == null) {
        return "javascript:void(0);";
    }
    var userType = task.userType;
    var userId = task.userId;
    if (blankString(userType) || blankString(userId)) {
        return "javascript:void(0);";
    }
    if (userType === "TEACHER") {
        return "/crm/teachernew/teacherdetail.vpage?teacherId=" + userId;
    } else if (userType === "STUDENT") {
        return "/crm/student/studenthomepage.vpage?studentId=" + userId;
    }
    return "javascript:void(0);";
}
function taskStubs(taskStubs) {
    $("#task-stubs").empty();
    $("#stubs-show").hide();
    $("#stubs-more").hide().empty();
    if (emptyArray(taskStubs)) {
        return;
    }
    var MAX = 10;
    for (var i in taskStubs) {
        var stub = taskStubs[i];
        var actionTime = stub.niceActionTime == null ? "" : stub.niceActionTime;
        var title = stub.title == null ? "" : stub.title;
        var content = stub.content;
        if (content != null && content.length > 45) {
            var tip = content.substr(0, 45) + "...";
            content = "<a onclick='moreDetail(this)' style='cursor: help' detail='" + content + "'>" + tip + "</a>"
        }
        var li = "<li>" + actionTime + " " + title + " " + content + "</li>";
        if (i < MAX) {
            $("#task-stubs").append(li);
        } else {
            $("#stubs-more").append(li);
        }
    }
    if (taskStubs.length > MAX) {
        $("#stubs-show").show();
    }
}
function iviewTask(task) {
    $("#task-edit").hide();
    $("#task-show").show();
    if (task.status === "FINISHED") {
        $("#task-show-status").css("color", "green");
    } else {
        $("#task-show-status").css("color", "");
    }
    var taskStatus = task.niceStatus;
    var adminUser = $("#task-detail").attr("admin-user");
    if ((taskStatus === "新建" || taskStatus === "待跟进") && adminUser === task.executor) {
        $("#edit-task").show();
        $("#new-record").show();
    } else {
        $("#edit-task").hide();
        $("#new-record").hide();
    }
}

task.edit = function () {
    $("#task-show").hide();
    $("#task-edit").show();
    $("#task-detail").dialog({
        height: "auto",
        width: "auto",
        autoOpen: true,
        title: "任务更改"
    });
};

task.update = function () {
    var taskId = $("#task-detail").attr("task-id");
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        if (updateTask(taskId)) {
            if (confirm("任务更新成功，是否刷新页面查看最新记录状态？")) {
                $("#iform").submit();
            }
            $("#task-detail").dialog("close");
        }
    }
};
function updateTask(taskId) {
    var endTime = $("#task-edit-endTime").val();
    if (blankString(endTime)) {
        alert("请选择截止时间！");
        return false;
    }
    var content = $("#task-edit-content").val();
    if (blankString(content)) {
        alert("请填写任务内容！");
        return false;
    }
    var status = $("#task-edit-status").val();
    if (blankString(status)) {
        alert("请选择任务状态！");
        return false;
    }
    var success = false;
    $.ajax({
        url: "/crm/task/update_task.vpage",
        type: "POST",
        async: false,
        data: {
            "taskId": taskId,
            "endTime": endTime,
            "content": content,
            "status": status
        },
        success: function (data) {
            if (!data) {
                alert("任务更新失败！");
            } else {
                success = true;
            }
        }
    });
    return success;
}

task.finish = function (taskId) {
    if (blankString(taskId)) {
        alert("无效的任务ID！");
        return;
    }
    $.ajax({
        url: "/crm/task/finish_task.vpage",
        type: "POST",
        data: {
            "taskId": taskId
        },
        success: function (data) {
            if (data) {
                alert("操作成功！");
                updateItemData(data);
            } else {
                alert("操作失败！");
            }
            closeDialog('dialog-box');
        }
    });
};

task.isRecordByMyself = function(taskId, taskType){
    var ret = false;
    if(taskType == "回访流失老师" || taskType == "老师转校" || taskType == "老师新建班级" || taskType == "老师手机绑定解绑"){
        $.ajax({
            url: "/crm/task/allready_record_by_myself.vpage",
            type: "POST",
            async: false,
            data: {
                "taskId": taskId
            },
            success: function (data) {
                ret = data;
            }
        });
    }
    return ret;
};

task.finishAndFollowConfirm = function(taskId, taskType){
    if (blankString(taskId)) {
        alert("无效的任务ID！");
        return;
    }
    $("#dialog-box-confirm-text").text("确认完成该任务吗？");
    if(taskType == "回访流失老师" ) {
        var extendHtml = '<td colspan="2" style="text-align: left"><input type="checkbox" id="dialog-box-agent-follow" >需要市场跟进</input></td>';
        $("#dialog-box-extend-row").html(extendHtml);
    }else{
        $("#dialog-box-extend-row").html("");
    }
    $("#dialog-box-confirm-button").unbind();
    $("#dialog-box-confirm-button").bind("click",function(){task.finishAndFollow(taskId, taskType)});
    $("#dialog-box").dialog({
        height: "auto",
        width: "400px",
        autoOpen: true
    });
};

task.finishAndFollow = function(taskId, taskType){
    if(taskType == "回访流失老师" || taskType == "老师转校" || taskType == "老师新建班级" || taskType == "老师手机绑定解绑"){
        var checkBox = document.getElementById("dialog-box-agent-follow");
        var needFollow = false;
        if(checkBox){
            needFollow = checkBox.checked == true;
        }
        $.ajax({
            url: "/crm/task/finish_task.vpage",
            type: "POST",
            data: {
                "taskId": taskId
            },
            success: function (data) {
                if (data) {
                    var ret = taskForward(taskId, taskType, needFollow);
                    if(ret){
                        alert("操作成功");
                    }
                    //$("#iform").submit();
                    updateItemData(data);
                } else {
                    alert("操作失败！");
                }
                closeDialog('dialog-box');
            }
        });
    }else{
        task.finish(taskId);
    }
};

//将任务转发到其他平台
function taskForward(taskId, taskType, needFollow){
    var ret = false;
    if(taskType == "回访流失老师"  || taskType == "老师转校" || taskType == "老师新建班级" || taskType == "老师手机绑定解绑"){
        $.ajax({
            url: "/crm/task/task_forward_to_agent.vpage",
            type: "POST",
            async: false,
            data: {
                "taskId": taskId,
                "needFollow" : needFollow
            },
            success: function (data) {
                if (!data) {
                    alert("任务流转失败");
                    ret = data
                }
            }
        });
    }
    return ret;
};

task.follow = function (taskId) {
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        $.ajax({
            url: "/crm/task/follow_task.vpage",
            type: "POST",
            data: {
                "taskId": taskId
            },
            success: function (data) {
                if (data) {
                    //if (confirm("操作成功，是否刷新页面查看最新记录状态？")) {
                    //    $("#iform").submit();
                    //}
                    alert("操作成功");
                    updateItemData(data);
                } else {
                    alert("操作失败！");
                }
            }
        });
    }
};

task.new = function (userId) {
    if (!validNumber(userId)) {
        alert("无效的用户ID！");
    } else {
        newTask("新建任务", [userId], task.ACTION.NEW);
    }
};

task.batchNew = function (userIds) {
    if (emptyArray(userIds)) {
        alert("无效的用户ID！");
    } else {
        newTask("新建任务", userIds, task.ACTION.NEW);
    }
};

task.forward = function (taskId) {
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        $.ajax({
            url: "/crm/task/task_snapshot.vpage",
            type: "POST",
            data: {
                "taskId": taskId
            },
            success: function (data) {
                if (!data) {
                    alert("操作失败！");
                } else {
                    var userId = data.userId;
                    if (!validNumber(userId)) {
                        alert("无效的用户ID！");
                    } else {
                        $("#task-new-type").val(data.type);
                        $("#task-new-endTime").val(data.niceEndTime);
                        $("#task-new-title").val(data.title);
                        $("#task-new-content").val(data.content);
                        newTask("任务转发", [userId], task.ACTION.FORWARD, taskId);
                    }
                }
            }
        });
    }
};
function newTask(title, userIds, action, source, content) {
    $("#task-new").attr("users", stringArray(userIds));
    $("#task-new").attr("action", action);
    $("#task-new").attr("source", source);
    $("#task-new-content").val(content);
    $("#task-new").dialog({
        height: "auto",
        width: "auto",
        autoOpen: true,
        title: title
    });
};

task.batchForwardDialog = function(taskIds, createFlg){
    var taskIdArr = stringToArray(taskIds, ",");
    if(taskIdArr.length == 0){
        alert("无效的任务ID！");
    }
    $("#task-batch-forward").attr("action", task.ACTION.FORWARD);
    $("#task-batch-forward").attr("source", stringArray(taskIdArr));
    $("#task-batch-forward").attr("create_flag", createFlg);
    $("#task-batch-forward-count").html(taskIdArr.length);
    $("#task-batch-forward").dialog({
        height: "auto",
        width: "auto",
        autoOpen: true
    });
};

task.save = function () {
    var action = $("#task-new").attr("action");
    var source = $("#task-new").attr("source");
    if (blankString(action)) {
        alert("无效的参数[action]！");
    } else {
        if (saveTask(action, source)) {
            if (confirm("任务创建成功，是否刷新页面查看最新记录状态？")) {
                $("#iform").submit();
            }
            $("#task-new").dialog("close");
        }
    }
};
function saveTask(action, source) {
    var executor = $("#task-new-executor").val();
    if (blankString(executor)) {
        alert("请选择执行人！");
        return false;
    }
    var type = $("#task-new-type").val();
    if (blankString(type)) {
        alert("请选择任务分类！");
        return false;
    }
    var endTime = $("#task-new-endTime").val();
    if (blankString(endTime)) {
        alert("请选择截止时间！");
        return false;
    }
    var end = dater.parse("yy-mm-dd", endTime);
    if (end == null) {
        alert("任务截止时间有误！");
        return false;
    }
    var today = dater.parse("yy-mm-dd", $.datepicker.formatDate("yy-mm-dd", new Date()));
    if (end.getTime() < today.getTime()) {
        alert("任务截止时间不能早于今天！");
        return false;
    }
    var content = $("#task-new-content").val();
    if (blankString(content)) {
        alert("请填写任务内容！");
        return false;
    }
    var users = $("#task-new").attr("users");
    if (blankString(users)) {
        alert("无效的用户ID！");
        return false;
    }
    var title = $("#task-new-title").val();
    var success = false;
    $.ajax({
        url: "/crm/task/add_task.vpage",
        type: "POST",
        async: false,
        data: {
            "executor": executor,
            "type": type,
            "endTime": endTime,
            "title": title,
            "content": content,
            "userIds": users,
            "ACTION": action,
            "SOURCE": source
        },
        success: function (data) {
            if (!data) {
                alert("任务创建失败！");
            } else {
                success = true;
            }
        }
    });
    return success;
}

task.addRecord = function (taskId, userId , taskType) {
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else if (!validNumber(userId)) {
        alert("无效的用户ID！");
    } else {
        $(".record-new-record").hide();
        $(".record-new-task").show();
        newRecord(userId, taskId, taskType);
    }
};

task.newRecord = function () {
    var taskId = $("#task-detail").attr("task-id");
    var userId = $("#task-detail").attr("user-id");
    var taskType = $("#task-detail").attr("task-type");
    task.addRecord(taskId, userId, taskType);
};

var auth = {};
auth.get = function (teacherId) {
    $.ajax({
        url: "/crm/teacher/bigdataauth.vpage",
        type: "GET",
        async: false,
        data: {"teacherId": teacherId},
        success: function (data) {
            if (data.success) {
                alert("已通过大数据认证");
            } else {
                alert("未通过大数据认证");
            }
        }
    });
}
var record = {};
record.updateTask = function () {
    var taskId = $("#record-new").attr("taskId");
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        var record = saveRecord(taskId);
        if (record != null) {
            $("#record-new").dialog("close");
            task.show(taskId);
        }
    }
};

record.finishTask = function () {
    var taskId = $("#record-new").attr("taskId");
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        var record = saveRecord(taskId);
        if (record != null) {
            $("#record-new").dialog("close");
            var taskType = $("#record-new").attr("task-type");
            finishAndFollowConfirmDialog(taskId,taskType);
        }
    }
};

function finishAndFollowConfirmDialog(taskId,taskType){
    if (blankString(taskId)) {
        alert("无效的任务ID！");
        return;
    }
    $("#dialog-box-confirm-text").text("工作记录已提交，确认完成该任务吗？");
    if(taskType == "回访流失老师" ) {
        var extendHtml = '<td colspan="2" style="text-align: left"><input type="checkbox" id="dialog-box-agent-follow" >需要市场跟进</input></td>';
        $("#dialog-box-extend-row").html(extendHtml);
    }else{
        $("#dialog-box-extend-row").html("");
    }
    $("#dialog-box-confirm-button").unbind();
    $("#dialog-box-confirm-button").bind("click",function(){task.finishAndFollow(taskId, taskType)});
    $("#dialog-box").dialog({
        height: "auto",
        width: "400px",
        autoOpen: true
    });
};


record.followTask = function () {
    var taskId = $("#record-new").attr("taskId");
    if (blankString(taskId)) {
        alert("无效的任务ID！");
    } else {
        var record = saveRecord(taskId);
        if (record != null) {
            $("#record-new").dialog("close");
            task.follow(taskId);
        }
    }
};

record.newTask = function () {
    var userId = $("#record-new").attr("userId");
    if (!validNumber(userId)) {
        alert("无效的用户ID！");
    } else {
        var record = saveRecord("");
        if (record != null) {
            $("#record-new").dialog("close");
            newTask("新建任务", [userId], task.ACTION.RECORD, record.id, record.content);
        }
    }
};

record.new = function (userId) {
    if (!validNumber(userId)) {
        alert("无效的用户ID！");
    } else {
        $(".record-new-task").hide();
        $(".record-new-record").show();
        newRecord(userId, "", "");
    }
};
function newRecord(userId, taskId, taskType) {
    $("#record-new").attr("userId", userId);
    $("#record-new").attr("taskId", taskId);
    $("#record-new").attr("task-type", taskType);
    initRecordNewData();
    $("#record-new").dialog({
        height: "auto",
        width: "auto",
        autoOpen: true
    });
}

record.save = function () {
    var userId = $("#record-new").attr("userId");
    if (!validNumber(userId)) {
        alert("无效的用户ID！");
    } else {
        var record = saveRecord("");
        if (record != null) {
            if (confirm("工作记录创建成功，是否刷新页面查看最新记录状态？")) {
                $("#iform").submit();
            }
            $("#record-new").dialog("close");
        }
    }
};
function saveRecord(taskId) {
    var userId = $("#record-new").attr("userId");
    if (!validNumber(userId)) {
        alert("无效的用户ID！");
        return null;
    }
    var contactType = $("#record-new-contact").val();
    if (blankString(contactType)) {
        alert("请选择沟通渠道！");
        return null;
    }
    var thirdCategory = $("#record-new-thirdCategory").val();
    if (blankString(thirdCategory)) {
        alert("请选择记录分类！");
        return null;
    }
    var content = $("#record-new-content").val();
    if (blankString(content)) {
        alert("请填写记录内容！");
        return null;
    }
    var redmineAssigned = $("#record-new-redmineAssigned").val();
    var record = null;
    $.ajax({
        url: "/crm/task/add_record.vpage",
        type: "POST",
        async: false,
        data: {
            "taskId": taskId,
            "userId": userId,
            "recordCategory": thirdCategory,
            "contactType": contactType,
            "content": content,
            "redmineAssigned": redmineAssigned
        },
        success: function (data) {
            if (!data) {
                alert("工作记录创建失败！");
            } else {
                record = data;
            }
        }
    });
    return record;
}

record.renderCategories = function (appendAll) {
    firstCategories(appendAll);
};
function firstCategories(appendAll) {
    $(".firstCategory").empty();
    if (appendAll === true) {
        $(".firstCategory").append("<option value=''>全部</option>");
    }
    if (CATEGORIES != null) {
        for (var first in CATEGORIES) {
            $(".firstCategory").append("<option value='" + first + "'>" + first + "</option>");
        }
    }
    var category = $(".firstCategory").attr("category");
    if (!blankString(category)) {
        $(".firstCategory").val(category);
    }
    secondCategories(appendAll);
}
function secondCategories(appendAll) {
    $(".secondCategory").empty();
    if (appendAll === true) {
        $(".secondCategory").append("<option value=''>全部</option>");
    }
    var first = $(".firstCategory").val();
    if (CATEGORIES != null && !blankString(first)) {
        var seconds = CATEGORIES[first];
        for (var second in seconds) {
            $(".secondCategory").append("<option value='" + second + "'>" + second + "</option>");
        }
    }
    var category = $(".secondCategory").attr("category");
    if (!blankString(category)) {
        $(".secondCategory").val(category);
    }
    thirdCategories(appendAll);
}
function thirdCategories(appendAll) {
    $(".thirdCategory").empty();
    if (appendAll === true) {
        $(".thirdCategory").append("<option value=''>全部</option>");
    }
    var first = $(".firstCategory").val();
    var second = $(".secondCategory").val();
    if (CATEGORIES != null && !blankString(first) && !blankString(second)) {
        var thirds = CATEGORIES[first][second];
        for (var i in thirds) {
            var third = thirds[i];
            $(".thirdCategory").append("<option value='" + third + "'>" + third + "</option>");
        }
    }
    var category = $(".thirdCategory").attr("category");
    if (!blankString(category)) {
        $(".thirdCategory").val(category);
    }
}

var clue = {};
clue.new = function(targetId, targetType) {
    if (!validNumber(targetId)) {
        alert("无效的用户ID！");
    } else {
        newClue([targetId], targetType);
    }
};

clue.batchNew = function(targetIds, targetType) {
    if (emptyArray(targetIds)) {
        alert("无效的用户ID！");
    } else {
        newClue(targetIds, targetType);
    }
};

function newClue(targetIds, targetType) {
    var clueNew = $("#clue-new");
    clueNew.attr("targetIds", stringArray(targetIds));
    clueNew.attr("targetType", targetType);
    clueNew.dialog({
        height: "auto",
        width: "auto",
        autoOpen: true,
        title: "新建线索"
    });
}

clue.save = function () {
    var clueNew = $("#clue-new");
    var targetIds = clueNew.attr("targetIds");
    var targetType = clueNew.attr("targetType");
    if (blankString(targetIds)) {
        alert("无效的参数[targetIds]！");
    }else if(blankString(targetType)){
        alert("无效的参数[targetType]！");
    } else {
        saveClue(targetIds, targetType);
    }
};

clue.showSaveResult = function (data) {
    if (nullString(data)) {
        alert("无效的任务ID！");
        return;
    }

    if(data.success){
        $("#alert-box-confirm-text").text("创建成功！");
    }else{
        $("#alert-box-confirm-text").text("创建失败");
    }
    $("#alert-box-extend-row").html("");

    if(!emptyArray(data.invalidStatusTeacherMessages) || !emptyArray(data.existTeacherMessages) || !emptyArray(data.noAgentTeacherMessages)){
        var extendHtml = '<td colspan="2" style="text-align: left">';
        extendHtml += '<div>';
        extendHtml += '<span>以下老师提交线索失败：</span><br/>';
        extendHtml += '<table width="100%" height="100%">';
        if(!emptyArray(data.invalidStatusTeacherMessages)){
            for (var index in data.invalidStatusTeacherMessages) {
                extendHtml += '<tr><td colspan="2" style="text-align: left"><span>' + data.invalidStatusTeacherMessages[index] + '</span></td></tr>';
            }
        }
        if(!emptyArray(data.existTeacherMessages)){
            for (var index in data.existTeacherMessages) {
                extendHtml += '<tr><td colspan="2" style="text-align: left"><span>' + data.existTeacherMessages[index] + '</span></td></tr>';
            }
        }
        if(!emptyArray(data.noAgentTeacherMessages)){
            for (var index in data.noAgentTeacherMessages) {
                extendHtml += '<tr><td colspan="2" style="text-align: left"><span>' + data.noAgentTeacherMessages[index] + '</span></td></tr>';
            }
        }
        extendHtml += '</table>';
        extendHtml += '</div>';
        extendHtml += '</td>';
        $("#alert-box-extend-row").html(extendHtml);
    }

    $("#alert-box-confirm-button").unbind();
    $("#alert-box-confirm-button").bind("click",function(){
        closeDialog('alert-box');
    });
    $("#alert-box").dialog({
        height: "auto",
        width: "400px",
        autoOpen: true
    });

};

saveClue = function(targetIds, targetType) {
    var clueType = $("#clue-new-type").val();
    if (blankString(clueType)) {
        alert("请选择线索类型");
        return false;
    }

    $.ajax({
        url: "/crm/clue/add_clue.vpage",
        type: "POST",
        async: false,
        data: {
            "targetType": targetType,
            "targetIds": targetIds,
            "clueType": clueType
        },
        success: function (data) {
            $("#clue-new").dialog("close");
            clue.showSaveResult(data);
        }
    });

}

