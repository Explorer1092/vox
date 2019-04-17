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
    <form id="iform" action="/crm/task/user_record.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="createTime">
                    创建时间：
                    <input name="createStart" id="createStart" value="${createStart!}" type="text" class="date"/> -
                    <input name="createEnd" id="createEnd" value="${createEnd!}" type="text" class="date"/>
                </label>
            </li>

            <li>
                <label for="recorder">
                    记录人员：
                    <select id="recorder" name="recorder">
                        <option value="">全部</option>
                    <#if taskUsers?has_content>
                        <#list taskUsers?keys as user>
                            <#if recorder?? && recorder == user>
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
                <label for="contactType">
                    沟通渠道：
                    <select id="contactType" name="contactType">
                        <option value="">全部</option>
                    <#if contactTypes?has_content>
                        <#list contactTypes as contact>
                            <#assign iContact=contact.name()>
                            <#if contactType?? && contactType.name() == iContact>
                                <option value="${iContact!}" selected="selected">${iContact!}</option>
                            <#else>
                                <option value="${iContact!}">${iContact!}</option>
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
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="10" type="hidden"/>
        <input id="ORDER" name="ORDER" value="DESC" type="hidden"/>
        <input id="SORT" name="SORT" value="createTime" type="hidden"/>
    </form>

<#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>记录时间</th>
                <th>一级分类</th>
                <th>二级分类</th>
                <th>三级分类</th>
                <th>记录内容</th>
                <th>沟通渠道</th>
                <th>记录人</th>
            </tr>
            <tbody>
            <#if taskRecords?has_content>
                <#list taskRecords.content as record>
                    <#assign content=record.content!"">
                    <#if (content?length) gt 15>
                        <#assign tip=content?substring(0, 15) + "...">
                        <#assign content="<a onclick='moreDetail(this)' style='cursor: help' detail='${content}'>${tip}</a>">
                    </#if>
                <tr>
                    <td>${record.createTime!}</td>
                    <td>${(record.firstCategory.name())!}</td>
                    <td>${(record.secondCategory.name())!}</td>
                    <td>${(record.thirdCategory.name())!}</td>
                    <td>${content!}</td>
                    <td>${(record.contactType.name())!}</td>
                    <td>${record.recorderName!}</td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>

    <#assign pager=taskRecords!>
    <#include "../pager_foot.ftl">
    </div>

<#include "common/detail_more.ftl">
</div>

<script type="text/javascript">
    $(function () {
        dater.render();
    });

    function formReset() {
        $("#createStart").val("");
        $("#createEnd").val("");
        $("#recorder").val("");
        $("#contactType").val("");
    }
</script>
</body>