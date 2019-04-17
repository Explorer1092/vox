<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="background: none;">
<div style="margin-left: 2em">
    <div style="margin-top: 2em">
        <table>
            <tr>
                <td class="info_td">原始邀请人：</td>
                <td class="info_td_txt"><#if teacherSummary??><a target="_blank" href="teacherdetail.vpage?teacherId=${teacherSummary.invitorId!''}"><#if teacherSummary.inviteType?has_content>${teacherSummary.invitorName!''}(${teacherSummary.invitorId!''})</#if></#if></a></td>
                <td class="info_td">邀请时间：</td>
                <td class="info_td_txt"><#if teacherSummary??><#if teacherSummary.inviteType?has_content>${(teacherSummary.inviteTime!)?number_to_datetime}</#if></#if></td>
                <td class="info_td">邀请方式：</td>
                <td class="info_td_txt"><#if teacherSummary??><#if teacherSummary.inviteType?has_content>${teacherSummary.inviteType!''}</#if></#if></td>
            </tr>
            <tr>
                <td class="info_td" colspan="6">
                    邀请后如果没有拿到奖励, 请点击 http://wiki.17zuoye.net/pages/viewpage.action?pageId=45226721
                </td>
            </tr>
        </table>
    </div>
    <div style="margin-top: 2em">
        <ul class="inline">
            <li><span style="font-weight:600">邀请他人</span></li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 老师名称</th>
                <th> 学科</th>
                <th> 邀请时间</th>
                <th> 邀请方式</th>
                <th> 是否成功</th>
                <th> 成功时间</th>
                <th> 原因</th>
            </tr>
            <#if inviteMapList?has_content>
                <#list inviteMapList as inviteMap>
                    <#if inviteMap["userId"]?has_content>
                    <tr>
                        <td><a target="_blank" href="teacherdetail.vpage?teacherId=${inviteMap["userId"]!''}">${inviteMap["userName"]!''}(${inviteMap["userId"]!''})</a></td>
                        <td>${inviteMap["subject"]!''}</td>
                        <td>${inviteMap["createTime"]!''}</td>
                        <td>${inviteMap["inviteType"]!''}</td>
                        <td><#if inviteMap["success"] == true>邀请成功<#else >-</#if></td>
                        <td><#if inviteMap["success"] == true>${inviteMap["updateTime"]!''}<#else >-</#if></td>
                        <td>${inviteMap["reason"]!""}</td>
                    </tr>
                    </#if>
                </#list>
            <#else ><td >暂无历史信息</td>
            </#if>
        </table>
    </div>

    <div style="margin-top: 2em">
        <ul class="inline">
            <li><span style="font-weight:600">帮助记录</span></li>
        </ul>
        <ul class="inline">
            <li>帮助他人</li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 老师名称</th>
                <th> 学科</th>
                <th> 帮助时间</th>
                <th> 帮助事项</th>
                <th> 是否成功</th>
                <th> 成功时间</th>
            </tr>
            <#if helpOtherMapList?has_content>
                <#list helpOtherMapList as helpHistoryMap>
                    <tr>
                        <td><#if helpHistoryMap["userId"]?has_content><a target="_blank" href="teacherdetail.vpage?teacherId=${helpHistoryMap["userId"]!''}">${helpHistoryMap["userName"]!''}(${helpHistoryMap["userId"]!''})</a></#if></td>
                        <td>${helpHistoryMap["subject"]!''}</td>
                        <td>${helpHistoryMap["createTime"]!''}</td>
                        <td>${helpHistoryMap["mentorCategory"]!''}</td>
                        <td><#if helpHistoryMap["success"] == true>达成认证<#else >-</#if></td>
                        <td><#if helpHistoryMap["success"] == true>${helpHistoryMap["updateTime"]!''}<#else >-</#if></td>
                    </tr>
                </#list>
            <#else ><td >暂无历史信息</td>
            </#if>
        </table>

        <ul class="inline">
            <li>寻求帮助</li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 老师名称</th>
                <th> 学科</th>
                <th> 帮助时间</th>
                <th> 帮助事项</th>
                <th> 是否成功</th>
                <th> 成功时间</th>
            </tr>
            <#if helpedMapList?has_content>
                <#list helpedMapList as helpedMap>
                    <tr>
                        <td><#if  helpedMap["userId"]?has_content><a target="_blank" href="teacherdetail.vpage?teacherId=${helpedMap["userId"]!''}">${helpedMap["userName"]!''}(${helpedMap["userId"]!''})</a></#if></td>
                        <td>${helpedMap["subject"]!''}</td>
                        <td>${helpedMap["createTime"]!''}</td>
                        <td>${helpedMap["mentorCategory"]!''}</td>
                        <td><#if helpedMap["success"] == true>达成认证<#else >-</#if></td>
                        <td><#if helpedMap["success"] == true>${helpedMap["updateTime"]!''}<#else >-</#if></td>
                    </tr>
                </#list>
            <#else ><td >暂无历史信息</td>
            </#if>
        </table>

        <ul class="inline">
            <li>关联账号</li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> ID</th>
                <th> 账号类型</th>
                <th> 是否认证</th>
                <th> 注册来源</th>
                <th> 关联日志</th>
                <th> 操作人</th>
            </tr>
        <#if bindData?has_content>
            <#list bindData as bind>
                <tr>
                    <td>${bind.userId!''}</td>
                    <td>${bind.userType!''}</td>
                    <td>${bind.authtication!''}</td>
                    <td>${bind.webSource!''}</td>
                    <td>${bind.log!''}</td>
                    <td>${bind.adminUser!''}</td>
                </tr>
            </#list>
        <#else ><td >暂无关联信息</td>
        </#if>
        </table>
    </div>
</div>
</body>
</html>