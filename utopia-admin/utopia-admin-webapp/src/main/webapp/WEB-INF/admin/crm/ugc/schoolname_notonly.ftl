<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="UGC数据查询" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<style>
    #triggerType th {
        text-align: center;
        vertical-align: middle;
        font-weight: bold;
        padding-left: auto;
        margin-left: auto;
    }

    #tabs2 a {
        padding: 5px;
        margin: 12px;
    }
</style>

<div class="span9">
    <div id="tabs">
        <legend>
            学校名称 &nbsp; &nbsp;
            <a href="/crm/ugc/gradename_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">年级分布</a>
            <a href="javascript:void(0)" style="margin: 12px;text-decoration: none;color: #838c9a">英语起始年级</a>
            <a href="/crm/ugc/gradeclassname_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">年级班级情况</a>
            <a href="/crm/ugc/classname_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">班级名称</a>
            <a href="/crm/ugc/studentcount_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">班级学生数量</a>
            <a href="/crm/ugc/teachername_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">班级老师名单</a>
        </legend>
    </div>

    <div id="tabs1" style="padding: 10px;margin: 10px">
        <table class="table table-bordered" id="triggerType">
            <tr>
                <th <#if triggerType == 0>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/schoolname_notonly.vpage?trigger=0">答案不唯一</a>
                </th>
                <th <#if triggerType == 3>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/schoolname_notonly.vpage?trigger=3">答案变更</a>
                </th>
                <th <#if triggerType == 4>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/schoolname_notonly.vpage?trigger=4">关键字不匹配</a>
                </th>
                <th <#if triggerType == 100>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/schoolname_notonly.vpage?trigger=100">已下发任务</a>
                </th>
            </tr>
        </table>
    </div>

    <#if triggerType?? && (triggerType == 0 || triggerType == 100)>
        <div id="tabs2" style="padding: 10px">
            <label>
                <#assign iAuth = (checkupStatus!-1)>
                鉴定状态：
                <a <#if iAuth == -1>style="background-color: #d77a00"</#if> href="/crm/ugc/schoolname_notonly.vpage?trigger=${triggerType!}">不限</a>
                <a <#if iAuth == 0>style="background-color: #d77a00"</#if> href="/crm/ugc/schoolname_notonly.vpage?trigger=${triggerType!}&authenticationstate=0">待鉴定</a>
                <a <#if iAuth == 1>style="background-color: #d77a00"</#if> href="/crm/ugc/schoolname_notonly.vpage?trigger=${triggerType!}&authenticationstate=1">已鉴定</a>
            </label>
            <#if triggerType == 100>
                <br>
                <label>
                    <#assign auth = checkupStatus!"">
                    <#if isTaskFinished??>
                        <#assign iFinish = isTaskFinished?string("true", "false")>
                    <#else>
                        <#assign iFinish = "">
                    </#if>
                    完成情况：
                    <a <#if iFinish == "">style="background-color: #d77a00"</#if> href="/crm/ugc/schoolname_notonly.vpage?trigger=${triggerType!}&authenticationstate=${auth!}">不限</a>
                    <a <#if iFinish == "false">style="background-color: #d77a00"</#if> href="/crm/ugc/schoolname_notonly.vpage?trigger=${triggerType!}&authenticationstate=${auth!}&isTaskFinished=false">未完成</a>
                    <a <#if iFinish == "true">style="background-color: #d77a00"</#if> href="/crm/ugc/schoolname_notonly.vpage?trigger=${triggerType!}&authenticationstate=${auth!}&isTaskFinished=true">已完成</a>
                </label>
            </#if>
        </div>
    </#if>

    <#setting number_format="0.##">
    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <table class="table table-bordered">
        <tr>
            <th>学校ID</th>
            <th>学校全称</th>
            <th>学校简称</th>

            <th>有效回答人数</th>
            <#if triggerType?? && (triggerType == 3 || triggerType == 4)>
                <th>最新UGC答案</th>
                <th>答案占比</th>
            </#if>
            <#if triggerType?? && triggerType != 100>
                <th>历史UGC答案</th>
            </#if>
            <#if triggerType?? && triggerType == 100>
                <th>下发时间</th>
                <th>完成情况</th>
            </#if>
            <#if triggerType?? && (triggerType == 0 || triggerType == 100)>
                <th>鉴定状态</th>
                <th>操作</th>
            </#if>
        </tr>
        <tbody>
            <#if ugcSchoolList?has_content>
                <#list ugcSchoolList.content as schoollist>
                <tr>
                    <td>${schoollist.schoolId!}</td>
                    <td>
                        <a href="/crm/school/schoolhomepage.vpage?schoolId=${schoollist.schoolId!}" target="_blank">${(schoollist.schoolName)!}</a>
                    </td>
                    <td>${(schoollist.shortName)!}</td>
                    <td>${schoollist.joinStudentCount!}</td>
                    <#if triggerType?? && (triggerType == 3 || triggerType == 4)>
                        <#assign ugcSchoolName = schoollist.ugcSchoolName!"">
                        <td>${((ugcSchoolName=='NA')?string('',ugcSchoolName))!}</td>
                        <td>${((schoollist.ugcAnswerPercent)!0) * 100}%</td>
                    </#if>
                    <#assign historyUgcSchoolName = schoollist.historyUgcSchoolName!"">
                    <#if triggerType?? && triggerType != 100>
                        <td>${((historyUgcSchoolName=='NA')?string('',historyUgcSchoolName))}</td>
                    </#if>
                    <#if triggerType?? && triggerType == 100>
                        <td>${(schoollist.taskAssignedTime)!}</td>
                        <td>${(schoollist.isTaskFinished?? && schoollist.isTaskFinished)?string("已完成", "未完成")}</td>
                    </#if>
                    <#if triggerType?? && (triggerType == 0 || triggerType == 100)>
                        <td <#if schoollist.authStatus??&&schoollist.authStatus==0>style="background-color: #C6CC85"</#if>>
                        ${((schoollist.authStatus == 1)?string('已认证', '等待认证'))!}
                        </td>
                        <td>
                            <a href="/crm/ugc/schoolname_detail.vpage?schoolId=${schoollist.schoolId!}&PAGE=${page}&trigger=${triggerType!}">查看</a>
                        </td>
                    </#if>
                </tr>
                </#list>
            </#if>
        </tbody>
    </table>

    <form id="iform" action="/crm/ugc/schoolname_notonly.vpage" method="get">
        <input type="hidden" name="trigger" value="${triggerType!}"/>
        <input type="hidden" name="authenticationstate" value="${checkupStatus!}"/>
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="10" type="hidden"/>
    </form>

    <#assign pager=ugcSchoolList!>
    <#assign everySize = 10000 >
    <#include "../pager_foot.ftl">

    <br>
    <legend>汇总数据导出</legend>
    <div id="exportSchoolData"></div>

    <br>
    <legend>学校明细数据导出</legend>
    <div id="exportSchoolDetailData"></div>
</div>

<script>
    $(function () {
        $.get("ugcSchoolCount.vpage", {}, function (data) {
            var ugcSchoolCount = data.schoolCount;
            var page = ugcSchoolCount /${everySize};
            var href = "";
            for (var i = 0; i < page; i++) {
                var str = '<a href = "exportUgcSchoolData.vpage?exportUgcSchoolPage=' + i + '&everySize=${everySize}">第' + (i + 1) + '页</a>&nbsp;&nbsp;|';
                href = href + str;
            }
            $("#exportSchoolData").append(href);
        });

        $.get("ugcSchoolDetailCount.vpage", {}, function (data) {
            var ugcSchoolDetailCount = data.schoolDetailCount;
            var page = ugcSchoolDetailCount /${everySize};
            var href = "";
            for (var i = 0; i < page; i++) {
                var str = '<a href = "exportUgcSchoolDetailData.vpage?exportschoolDetailPage=' + i + '&everySize=${everySize}">第' + (i + 1) + '页</a>&nbsp;&nbsp;|';
                href = href + str;
            }
            $("#exportSchoolDetailData").append(href);
        });
    });
</script>
</@layout_default.page>