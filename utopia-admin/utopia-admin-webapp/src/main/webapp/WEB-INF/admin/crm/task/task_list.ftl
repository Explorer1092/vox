<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="任务查询" page_num=3>

<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

<div class="span11">
    <legend>任务查询</legend>

    <form id="iform" action="/crm/task/task_list.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="createTime">
                    创建日期：
                    <input name="createStart" id="createStart" value="${createStart!}" type="text" class="date"/> -
                    <input name="createEnd" id="createEnd" value="${createEnd!}" type="text" class="date"/>
                </label>
            </li>

            <li>
                <label for="endTime">
                    截止日期：
                    <input name="endStart" id="endStart" value="${endStart!}" type="text" class="date"/> -
                    <input name="endEnd" id="endEnd" value="${endEnd!}" type="text" class="date"/>
                </label>
            </li>

            <li>
                <label for="finishTime">
                    完成日期：
                    <input name="finishStart" id="finishStart" value="${finishStart!}" type="text" class="date"/> -
                    <input name="finishEnd" id="finishEnd" value="${finishEnd!}" type="text" class="date"/>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>
                <label for="type">
                    任务类型：
                    <select id="type" name="type">
                        <option value="">全部</option>
                        <#if taskTypes?has_content>
                            <#list taskTypes as taskType>
                                <#if type?? && type.name() == taskType.name()>
                                    <option value="${taskType.name()}" selected="selected">${taskType.name()!}</option>
                                <#else>
                                    <option value="${taskType.name()}">${taskType.name()!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>

            <li>
                <label for="status">
                    任务状态：
                    <select id="status" name="status">
                        <option value="">全部</option>
                        <#if taskStatuses?has_content>
                            <#list taskStatuses as taskStatus>
                                <#if status?? && status.name() == taskStatus.name()>
                                    <option value="${taskStatus.name()}" selected="selected">${taskStatus.value!}</option>
                                <#else>
                                    <option value="${taskStatus.name()}">${taskStatus.value!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>

            <li>
                <label for="userType">
                    任务对象：
                    <select id="userType" name="userType">
                        <option value="">全部</option>
                        <option value="TEACHER" <#if userType?? && userType.name() == "TEACHER">selected="selected"</#if>>教师</option>
                        <option value="STUDENT" <#if userType?? && userType.name() == "STUDENT">selected="selected"</#if>>学生</option>
                    </select>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>
                <label for="creator">
                    创建人：
                    <select id="creator" name="creator">
                        <option value="">全部</option>
                        <#if taskUsers?has_content>
                            <#list taskUsers?keys as user>
                                <#if creator?? && creator == user>
                                    <option value="${user}" selected="selected">${taskUsers[user]!}</option>
                                <#else>
                                    <option value="${user}">${taskUsers[user]!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>

            <li>
                <label for="executor">
                    执行人：
                    <select id="executor" name="executor">
                        <option value="">全部</option>
                        <#if taskUsers?has_content>
                            <#list taskUsers?keys as user>
                                <#if executor?? && executor == user>
                                    <option value="${user}" selected="selected">${taskUsers[user]!}</option>
                                <#else>
                                    <option value="${user}">${taskUsers[user]!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>


            <ul class="inline">
            <li>
                <button type="submit" onclick="checkTaskListForm();">查询</button>
            </li>
            <li>
                <input type="button" value="重置" onclick="formReset()"/>
            </li>
        </ul>

        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="100" type="hidden"/>
        <input id="ORDER" name="ORDER" value="${ORDER!'DESC'}" type="hidden"/>
        <input id="SORT" name="SORT" value="${SORT!'createTime'}" type="hidden"/>
    </form>

    <#setting datetime_format="yyyy-MM-dd"/>
    <div>
        <ul class="inline">

            <li>批量操作</li>
            <li><input id="taskForward" type="button" value="转发" onclick="batchForward()"/></li>
        </ul>
        <table class="table table-bordered">
            <tr>
                <th><input type="checkbox" id="ck-all" name="ck-all" onchange="ckAllClick();"/></th>
                <th onclick="pager.sort(this)" pager-sort="createTime" style="cursor: pointer; color: #0081c2">创建时间</th>
                <th>用户角色</th>
                <th>关联用户</th>
                <th>认证状态</th>
                <th>任务分类</th>
                <th>任务内容</th>
                <th>任务状态</th>
                <th>创建人</th>
                <th>执行人</th>
                <th>申请人</th>
                <th onclick="pager.sort(this)" pager-sort="endTime" style="cursor: pointer; color: #0081c2">截止时间</th>
                <th>操作</th>
            </tr>
            <tbody>
                <#if tasks?has_content>
                    <#list tasks.content as task>
                        <#assign userType=(task.userType.name())!"">
                        <#if userType == "TEACHER">
                            <#assign userLink="/crm/teachernew/teacherdetail.vpage?teacherId=${task.userId!}">
                        <#elseif userType == "STUDENT">
                            <#assign userLink="/crm/student/studenthomepage.vpage?studentId=${task.userId!}">
                        <#else>
                            <#assign userLink="javascript:void(0);">
                        </#if>
                        <#assign content=task.content!"">
                        <#if (content?length) gt 15>
                            <#assign tip=content?substring(0, 15) + "...">
                            <#assign content="<a onclick='moreDetail(this)' style='cursor: help' detail='${content}'>${tip}</a>">
                        </#if>
                    <tr>
                        <#assign executable=false>
                        <#if (adminUser.adminUserName)?? && adminUser.adminUserName == task.executor>
                            <#assign executable=true>
                        </#if>

                        <#assign isSuperManager=false>
                        <#if (adminUser.adminUserName)?? && (adminUser.adminUserName == "jingwei.qiu" || adminUser.adminUserName == "caijuan.gao")>
                            <#assign isSuperManager=true>
                        </#if>

                        <td><input type="checkbox" value="${task.id!}" name="ck-task" <#if executable || isSuperManager >can-forward="true"<#else>can-forward="false"</#if>/></td>
                        <td>${task.createTime!}</td>
                        <td>${(task.userType.description)!}</td>
                        <td><a href="${userLink!}" target="_blank">${task.userName!}</a> (${task.userId!"无用户信息"})</td>
                        <td <#if task.userAuthStatus?? && (task.userAuthStatus == "SUCCESS")>style="color: green"</#if>>${(task.userAuthStatus.description)!}</td>
                        <td>${(task.type.name())!}</td>
                        <td>${content!}</td>
                        <td id="status_${task.id!}" <#if task.status?? && (task.status == "FINISHED")>style="color: green"</#if>>${task.niceStatus!}</td>
                        <td>${task.creatorName!}</td>
                        <td>${task.executorName!}</td>
                        <td>${task.applicantName!}<#if task.applicantMobile??>(${task.applicantMobile})</#if></td>
                        <td>${task.endTime!}</td>
                        <td>
                            <#assign onlyFaward=false>
                            <#if (adminUser.adminUserName)?? && adminUser.adminUserName == 'guestcs'>
                                <#assign onlyFaward=true>
                            </#if>

                            <#assign modifiable=false>
                            <#if task.niceStatus?? && (task.niceStatus == "新建" || task.niceStatus == "待跟进")>
                                <#assign modifiable=true>
                            </#if>
                            <#if modifiable == true && executable == true && onlyFaward == false>
                                <input id="recordBtn_${task.id!}" type="button" value="记录" onclick="task.addRecord('${task.id!}', '${task.userId!}', '${task.type!}')"/>
                            </#if>
                            <#if (executable == true && onlyFaward == true) || isSuperManager>
                                <input id="forwardBtn_${task.id!}" type="button" value="转发" onclick="task.batchForwardDialog('${task.id!}','false')"/>
                            </#if>
                            <#if onlyFaward == false>
                            <input id="detailBtn_${task.id!}" type="button" value="详情" onclick="task.show('${task.id!}')"/>
                            </#if>
                            <#if modifiable == true && executable == true && onlyFaward == false>
                                <input id="finishBtn_${task.id!}" type="button" id="finishTask" value="标记完成" onclick="finishTaskItem('${task.id!}', '${task.type!}')"/>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#assign pager=tasks!>
        <#include "../pager_foot.ftl">
    </div>

    <#include "common/record_new.ftl">
    <#include "common/task_new.ftl">
    <#include "common/task_detail.ftl">
    <#include "common/detail_more.ftl">
    <#include "common/task_batch_forward.ftl">

    <#include "common/dialog_box.ftl">
</div>

<script type="text/javascript">
    $(function () {
        dater.render();
        pager.sortTip();
        $("#PAGE").val(${pager.number!0});
    });

    function checkTaskListForm(){
        $("#PAGE").val("0");
    }
    function formReset() {
        $("#createStart").val("");
        $("#createEnd").val("");
        $("#endStart").val("");
        $("#endEnd").val("");
        $("#finishStart").val("");
        $("#finishEnd").val("");
        $("#creator").val("");
        $("#executor").val("");
        $("#userType").val("");
        $("#type").val("");
        $("#status").val("");
        $("#PAGE").val("0");
        var checkAll = document.getElementById("ck-all");
        checkAll.checked = false;
        ckAllClick();
    }

    function ckAllClick(){

        var checkAll = document.getElementById("ck-all");
        var checked = checkAll.checked == true;
        var checkUser = document.getElementsByName("ck-task");
        for (var i = 0; i < checkUser.length; i++) {
            checkUser[i].checked = checked;
        }
    }

    function batchForward(){
        var ckTaskList = document.getElementsByName("ck-task");
        var taskIds = "";
        var j = 0;
        for(var i = 0; i< ckTaskList.length; i++){
            if(ckTaskList[i].checked == true){
                var canForward = $(ckTaskList[i]).attr("can-forward");
                if(canForward ==  "true"){
                    taskIds += ckTaskList[i].value + ",";
                    j++;
                }
            }
        }
        if(j == 0){
            alert("请选择要转发的任务");
            return;
        }
        if(confirm("确认转发选中的" + j + "条有效任务吗？")){
            task.batchForwardDialog(taskIds, "false");
        }

    }

    function finishTaskItem(taskId, taskType){
        if(taskType == "回访流失老师"|| taskType == "老师转校" || taskType == "老师新建班级" || taskType == "老师手机绑定解绑"){
            var ret = task.isRecordByMyself(taskId, taskType);
            if(ret){
                task.finishAndFollowConfirm(taskId, taskType);
            }else{
                alert("你尚未添加工作记录，暂不能完成该任务， 请填写工作记录");
            }
        }else {
            task.finishAndFollowConfirm(taskId, taskType);
        }
    }

    function updateItemData(data){
        if(data.status == "FINISHED"){
            var statusTd = $("#status_" + data.id);
            statusTd.removeAttr("style");
            statusTd.attr("style", "color: green");
            statusTd.text("已完成");
            $("#recordBtn_" + data.id).remove();
            $("#finishBtn_" + data.id).remove();
        }else if(data.status == "FOLLOWING"){
            var statusTd = $("#status_" + data.id);
            statusTd.removeAttr("style");
            statusTd.text("待跟进");
        }
    }


</script>
</@layout_default.page>