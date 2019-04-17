<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='班长管理详情' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<style>
    .form-horizontal .control-label {
        float: left;
        width: 206px;
        padding-top: 5px;
        text-align: right;
    }

    .control-group {
        margin-top: 10px;
        margin-bottom: 80px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>班长管理详情</legend>
    </fieldset>
<#--<div class="span12">-->
    <div class="control-group">
        <label class="control-label" for="productName">家长ID：</label>
        <label for="title">
            <input type="text" value="${recruitInfo.parentId!''}"
                   name="parentId" id="parentId" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">家长姓名：</label>
        <label for="title">
            <input type="text" value="${recruitInfo.parentName!''}"
                   name="parentName" id="parentName" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">级别：</label>
        <label for="title">
            <select id="level" name="level">
                <option value=1 <#if monitorRecord??&&monitorRecord.level??&&monitorRecord.level==1>selected</#if>>班长
                </option>
                <option value=2 <#if monitorRecord??&&monitorRecord.level??&&monitorRecord.level==2>selected</#if>>
                    初级辅导员
                </option>
                <option value=3 <#if monitorRecord??&&monitorRecord.level??&&monitorRecord.level==3>selected</#if>>
                    中级辅导员
                </option>
                <option value=4 <#if monitorRecord??&&monitorRecord.level??&&monitorRecord.level==4>selected</#if>>
                    高级辅导员
                </option>
                <option value=5 <#if monitorRecord??&&monitorRecord.level??&&monitorRecord.level==5>selected</#if>>
                    资深辅导员
                </option>
                <option value=6 <#if monitorRecord??&&monitorRecord.level??&&monitorRecord.level==6>selected</#if>>
                    荣誉辅导员
                </option>
            </select>
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">分配微信群：</label>
        <label for="title">
            <div class="row-fluid">
                <div class="span12">
                    <div class="well">
                        <table class="table table-hover table-striped table-bordered">
                            <thead>
                            <tr>
                                <th>课程ID</th>
                                <th>课程名称</th>
                                <th>期数</th>
                                <th>适用年级</th>
                                <th>开课时间</th>
                                <th>结课时间</th>
                                <th>当前学习群号</th>
                                <th>当前班级区</th>
                                <th>管理的微信群</th>
                                <th>备注</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                        <#if lessonList?? && lessonList?size gt 0>
                            <#list lessonList as  lesson>
                            <tr>
                                <td>${lesson.lessonId!''}</td>
                                <td>${lesson.title!''}</td>
                                <td>${lesson.phase!''}</td>
                                <td>${lesson.getSuitableGradeText()!''}</td>
                                <td>${lesson.openDate!''}</td>
                                <td>${lesson.closeDate!''}</td>
                            <#--<td>${currentGroupMap["${lessonGroupMap["${lesson.lessonId!''}"]!''}"].wechatGroupName!''}</td>-->
                                <td>${currentGroupMap[lessonGroupMap["${lesson.lessonId!''}"]].wechatGroupName!''}</td>
                                <td><#if groupAreaMap??&&groupAreaMap?size gt 0><#if groupAreaMap[lessonGroupMap["${lesson.lessonId!''}"]]??>${groupAreaMap[lessonGroupMap["${lesson.lessonId!''}"]].groupAreaName!''}</#if></#if></td>
                                <td id="${lesson.lessonId!''}"> <#if groupListMap["${lesson.lessonId!''}"]?? && groupListMap["${lesson.lessonId!''}"]?size gt 0>
                                <#list groupListMap["${lesson.lessonId!''}"] as  group>
                                    <p id="${group.id!''}" class="kol_p"> ${group.wechatGroupName!''}</p>
                                </#list>
                                <#else>
                                </#if></td>
                            <#--<td class="select_group_ids"-->
                            <#--style="display: none"-->
                            <#--data-lesson_id="${lesson.lessonId!''}"> <#if groupListMap["${lesson.lessonId!''}"]?? && groupListMap["${lesson.lessonId!''}"]?size gt 0>-->
                            <#--<#list groupListMap["${lesson.lessonId!''}"] as  group>-->
                            <#--${group.id!''},-->
                            <#--</#list>-->
                            <#--<#else>-->
                            <#--</#if></td>-->
                                <td><#if (recruitInfo.lessonId!'')==lesson.getLessonId()?c>招募</#if></td>
                                <td>
                                    <a class="btn btn-primary" name="checkInfo"
                                       data-lesson_id="${lesson.lessonId!''}"
                                       data-parent_id="${recruitInfo.parentId!''}">
                                        分配
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                            </tbody>
                        </table>

                        <ul class="message_page_list">
                        </ul>
                    </div>
                </div>
            </div>
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">休整开始时间：</label>
        <label for="title">
            <input type="text" value="${monitorRecord.restStartTime!''}"
                   name="restStart" id="restStart"
                   style="width: 20%" class="input form_datetime" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">休整结束时间：</label>
        <label for="title">
            <input type="text" value="${monitorRecord.restStopTime!''}"
                   name="restEnd" id="restEnd"
                   style="width: 20%" class="input form_datetime" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <div class="controls">
            <input type="button" id="saveInfo" value="确定" class="btn btn-large btn-success">
            <input type="button" id="history" value="历史班级管理记录" class="btn btn-large btn-warning"
                   data-parent_id="${recruitInfo.parentId!''}">
            <input type="button" id="changeRecord" value="修改记录" class="btn btn-large btn-primary"
                   data-parent_id="${recruitInfo.parentId!''}">
        </div>
    </div>
</div>

<div id="pass_mon_dialog" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button id="button_close_id" type="button" class="close" data-dismiss="modal" aria-hidden="true">×
                </button>
                <h3 class="modal-title">分配</h3>
            </div>

            <div class="modal-body">
                <dl class="dl-horizontal">
                    <input type="hidden" id="parentId" name="parentId"/>
                    <input type="hidden" id="lessonId" name="lessonId"/>
                    <ul class="inline">
                        <li>
                            <dt>家长姓名</dt>
                            <dd><input type="text" id="editParentName" readonly/></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>课程名称</dt>
                            <dd><input type="text" id="editLessonName" readonly/>
                                <input type="text" id="editLessonId"
                                       readonly style="display: none"/>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>期数</dt>
                            <dd><input type="text" id="editPhase" readonly/></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>微信群</dt>
                            <dd>
                                <input type="text" id="wechatName" name="wechatName" value=""/>
                                <button id="check_wechat_name" class="btn btn-primary">添加</button>
                            </dd>
                        </li>
                    </ul>
                    <div id="change_table">
                        <table class="table table-hover table-striped table-bordered change_table">
                            <thead>
                            <tr>
                                <th>班级Id</th>
                                <th>群名称</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                    </div>

                </dl>
            </div>

            <div class="modal-footer">
                <button id="save_pass_submit" class="btn btn-primary">修 改</button>
                <button id="close_id" class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>
    </div>
</div>
    <script type="text/javascript">
        $(function () {
            var status =${recruitInfo.status?number};
            var level =${monitorRecord.level?number};
            var currentGroupIds = [];
            <#if monitorRecord.groupIds??&&monitorRecord.groupIds?size gt 0>
                <#list monitorRecord.groupIds as groupId>
                    currentGroupIds.push("${groupId!''}");
                </#list>
            </#if>
            if (status === 4) {
                $("#level").attr("disabled", true);
                $("a[name='checkInfo']").css("display", "none");
                $("#restStart").attr("disabled", true);
                $("#restEnd").attr("disabled", true);
            }
            if (level === 1) {
                $("#restStart").attr("disabled", true);
                $("#restEnd").attr("disabled", true);
            }
            if (status === 5) {
                $("#restStart").attr("disabled", true);
            }
            //日期插件
            $(".form_datetime").datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss',//显示格式
                todayHighlight: 1,//今天高亮
                minView: "hour",//设置显示
                startDate: new Date(),
                startView: 2,
                forceParse: 0,
                showMeridian: 0,
                minuteStep: 5,
                autoclose: 1//选择后自动关闭
            });
            $("a[name='checkInfo']").on('click', function () {
                var lessonId = $(this).data('lesson_id');
                var parentId = $(this).data('parent_id');
                $.ajax({
                    url: 'changeWechatGroupStatus.vpage',
                    type: 'GET',
                    data: {lessonId: lessonId, parentId: parentId},
                    async: false,
                    success: function (data) {
                        if (data.success) {
                            console.log(data);
                            $("#editParentName").val(data.recruitInfo.parentName);
                            $("#editLessonName").val(data.lesson.title);
                            $("#editLessonId").val(data.lesson._id);
                            $("#editPhase").val(data.lesson.phase);
                            $("#wechatName").val("");
                            $(".change_table").find("tbody").empty();
                            var groupIds = [];
                            $("#" + lessonId).find("p.kol_p").each(function () {
                                var groupId = $(this).attr("id");
                                var groupName = $(this).html();
                                groupIds.push(groupId);
                                var rowTem = '<tr class="group_tr">'
                                        + '<td class="id_td">' + groupId + '</td>'
                                        + '<td class="name_td">' + groupName + '</td>'
                                        + '<td><a href="#" name="del" >删除</a></td>'
                                        + '</tr>';
                                $(".change_table").find("tbody").append(rowTem);
                            });
                            data.studyGroups.forEach(function (val, index) {
                                if ($.inArray(val.id, groupIds) >= 0) {
                                    return true;
                                }
                                var rowTem = '<tr class="group_tr">'
                                        + '<td class="id_td">' + val.id + '</td>'
                                        + '<td class="name_td">' + val.wechatGroupName + '</td>'
                                        + '<td><a href="#" name="del" >删除</a></td>'
                                        + '</tr>';
                                $(".change_table").find("tbody").append(rowTem);
                            });
                            $("#pass_mon_dialog").modal('show');
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
            $("#save_pass_submit").on('click', function () {
                var lessonId = $("#editLessonId").val();
                if (!lessonId) {
                    alert("未找到lessonId");
                    return;
                }
                var groupIds = {};
                $(".group_tr").each(function () {
                    groupIds[$(this).find('.id_td').html()] = $(this).find('.name_td').html();
                });
                var currentGroupIds = [];
                $("#" + lessonId).find(".kol_p").each(function () {
                    currentGroupIds.push($(this).attr("id"));
                });
                if (Object.keys(groupIds).length === currentGroupIds.length) {
                    return;
                }
                if (Object.keys(groupIds).length > currentGroupIds.length) {
                    $.each(groupIds, function (key, value) {
                        if ($.inArray(key, currentGroupIds) >= 0) {
                            return true;
                        }
                        var p_content = '<p class="kol_p" id="' + key + '">' + value + '</p>';
                        $("#" + lessonId).append(p_content);
                    });
                }
                if (Object.keys(groupIds).length < currentGroupIds.length) {
                    $.each(currentGroupIds, function (value) {
                        if ($.inArray(currentGroupIds[value], Object.keys(groupIds)) >= 0) {
                            return true;
                        }
                        $("#" + lessonId).find('#' + currentGroupIds[value]).remove();
                    });
                }
                $("#pass_mon_dialog").modal("hide");
            });

            $("#check_wechat_name").on('click', function () {
                var lessonId = $("#editLessonId").val();
                var wechatGroupName = $("#wechatName").val();
                $.ajax({
                    url: 'check_wechat_name.vpage',
                    type: 'GET',
                    data: {lessonId: lessonId, wechatName: wechatGroupName},
                    async: false,
                    success: function (data) {
                        if (data.success) {
                            var rowTem = '<tr class="group_tr">'
                                    + '<td class="id_td">' + data.group.id + '</td>'
                                    + '<td class="name_td">' + data.group.wechatGroupName + '</td>'
                                    + '<td><a href="#" name="del" >删除</a></td>'
                                    + '</tr>';
                            $(".change_table").find("tbody").append(rowTem);
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
            $(".change_table").on('click', 'a[name="del"]', function () {
                if (!confirm("确认删除?")) {
                    return;
                }
                $(this).parent().parent().remove();
            });
            $("#saveInfo").on('click', function () {
                var parentId = $("#parentId").val();
                var level = $("#level").val();
                var groupIds = [];
                var lessonIds = [];
                $("p.kol_p").each(function () {
                    var groupId = $(this).attr("id");
                    groupIds.push(groupId);
                    var lessonId = $(this).parent().attr("id");
                    if ($.inArray(lessonId, lessonIds) >= 0) {
                        return true;
                    }
                    lessonIds.push(lessonId);
                });
                var restStart = $("#restStart").val();
                var restEnd = $("#restEnd").val();
                if ((!restStart && restEnd) || (restStart && !restEnd)) {
                    alert("不能单独设置休整时间");
                    return;
                }
                if (!level) {
                    alert("级别不能为空");
                    return;
                }
                if (level === '1') {
                    if (lessonIds.length > 1) {
                        alert("班长只能分配一个课程下的班级");
                        return;
                    }
                    if (restStart || restEnd) {
                        alert("班长不能设置休整");
                        return;
                    }
                }
                if (status === 5 && groupIds.length > currentGroupIds.length) {
                    restStart = "";
                    restEnd = "";
                }
                $.ajax({
                    url: 'savebigmonitor.vpage',
                    type: 'POST',
                    data: {
                        parentId: parentId,
                        level: level,
                        groupIds: JSON.stringify(groupIds),
                        restStartTime: restStart,
                        restEndTime: restEnd
                    },
                    success: function (data) {
                        if (data.success) {
                            alert("保存成功！");
                            window.location.reload();
                        } else {
                            alert(data.info);
                            console.log("data error");
                        }
                    }
                });
            });

            $("#changeRecord").on('click', function () {
                var parentId = $(this).data("parent_id");
                if (!parentId) {
                    alert("家长ID不能为空");
                }
                var url = "/opmanager/studyTogether/getStatusRecords.vpage?parentId=" + parentId;
                window.open(url);
            });
            $("#history").on('click', function () {
                var parentId = $(this).data("parent_id");
                if (!parentId) {
                    alert("家长ID不能为空");
                }
                var url = "/opmanager/studyTogether/getHistoryManageGroupRecords.vpage?parentId=" + parentId;
                window.open(url);
            })
        });
    </script>
</@layout_default.page>