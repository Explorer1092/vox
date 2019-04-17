<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
    <style>
        .table_soll {
            overflow-y: hidden;
            overflow-x: auto;
        }

        .table_soll table td, .table_soll table th {
            white-space: nowrap;
        }

        .basic_info {
            margin-left: 2em;
        }

        .txt {
            margin-left: .5em;
            font-weight: 800
        }

        .button_label {
            with: 7em;
            height: 3em;
            margin-top: 1em
        }

        .info_td {
            width: 7em;
        }

        .info_td_txt {
            width: 13em;
            font-weight: 600
        }

        .date {
            width: 6em;
        }

        .ui-dialog-titlebar-close {
            display: none;
        }
    </style>
</head>

<body style="margin-left: 2em; background: none;">
<div style=";margin-top: 2em">
    <form id="iform" action="/crm/task/user_task.vpage" method="post">
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
        </ul>

        <ul class="inline">
            <li>
                <button type="submit">查询</button>
            </li>
            <li>
                <input type="button" value="重置" onclick="formReset()"/>
            </li>
        </ul>

        <input id="userId" name="userId" value="${userId!}" type="hidden"/>
        <input id="userType" name="userType" value="${userType!}" type="hidden"/>
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="10" type="hidden"/>
        <input id="ORDER" name="ORDER" value="DESC" type="hidden"/>
        <input id="SORT" name="SORT" value="createTime" type="hidden"/>
    </form>

<#setting datetime_format="yyyy-MM-dd"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>创建时间</th>
                <th>任务分类</th>
                <th>任务内容</th>
                <th>任务状态</th>
                <th>创建人</th>
                <th>执行人</th>
                <th>截止时间</th>
                <th>操作</th>
            </tr>
            <tbody>
            <#if tasks?has_content>
                <#list tasks.content as task>
                    <#assign content=task.content!"">
                    <#if (content?length) gt 15>
                        <#assign tip=content?substring(0, 15) + "...">
                        <#assign content="<a onclick='moreDetail(this)' style='cursor: help' detail='${content}'>${tip}</a>">
                    </#if>
                <tr>
                    <td>${task.createTime!}</td>
                    <td>${(task.type.name())!}</td>
                    <td>${content!}</td>
                    <td <#if task.status?? && (task.status == "FINISHED")>style="color: green"</#if>>${task.niceStatus!}</td>
                    <td>${task.creatorName!}</td>
                    <td>${task.executorName!}</td>
                    <td>${task.endTime!}</td>
                    <td>
                        <#assign modifiable=false>
                        <#if task.niceStatus?? && (task.niceStatus == "新建" || task.niceStatus == "待跟进")>
                            <#assign modifiable=true>
                        </#if>
                        <#assign executable=false>
                        <#if (adminUser.adminUserName)?? && adminUser.adminUserName == task.executor>
                            <#assign executable=true>
                        </#if>
                        <#if modifiable == true && executable == true>
                            <input type="button" value="记录" onclick="task.addRecord('${task.id!}', '${task.userId!}')"/>
                        </#if>
                        <#if executable == true>
                            <input type="button" value="转发" onclick="task.forward('${task.id!}')"/>
                        </#if>
                        <input type="button" value="详情" onclick="task.show('${task.id!}')"/>
                        <#if modifiable == true && executable == true>
                            <input type="button" value="标记完成" onclick="task.finish('${task.id!}')"/>
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
</div>

<script type="text/javascript">
    $(function () {
        dater.render();
    });

    function formReset() {
        $("#creator").val("");
        $("#executor").val("");
        $("#status").val("");
    }
</script>
</body>