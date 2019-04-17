<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
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
            <a href="/crm/ugc/schoolname_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">学校名称</a>
            <a href="/crm/ugc/gradename_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">年级分布</a>
            <a href="javascript:void(0)" style="margin: 12px;text-decoration: none;color: #838c9a">英语起始年级</a>
            <a href="/crm/ugc/gradeclassname_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">年级班级情况</a>
            <a href="/crm/ugc/classname_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">班级名称</a>
            <a href="/crm/ugc/studentcount_notonly.vpage" style="margin: 12px;text-decoration: none;color: #838c9a">班级学生数量</a>
            班级老师名单&nbsp; &nbsp;
        </legend>
    </div>

    <div id="tabs1" style="padding: 10px;margin: 10px">
        <table class="table table-bordered" id="triggerType">
            <tr>
                <th <#if triggerType == 0>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/teachername_notonly.vpage?trigger=0">答案不唯一</a>
                </th>
                <th <#if triggerType == 3>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/teachername_notonly.vpage?trigger=3">答案变更</a>
                </th>
                <th <#if triggerType == 4>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/teachername_notonly.vpage?trigger=4">与系统老师不符</a>
                </th>
            </tr>
        </table>
    </div>

    <div id="tabs2" style="padding: 10px">
        <label>
            <#assign subjectTag = (subject!-1)>
            科目：
            <a <#if subjectTag == -1>style="background-color: #e6ebed"</#if> href="/crm/ugc/teachername_notonly.vpage?trigger=${triggerType!}">不限</a>
            <a <#if subjectTag == 1>style="background-color: #e6ebed"</#if> href="/crm/ugc/teachername_notonly.vpage?trigger=${triggerType!}&subjectTrigger=1">英语</a>
            <a <#if subjectTag == 2>style="background-color: #e6ebed"</#if> href="/crm/ugc/teachername_notonly.vpage?trigger=${triggerType!}&subjectTrigger=2">数学</a>
            <a <#if subjectTag == 3>style="background-color: #e6ebed"</#if> href="/crm/ugc/teachername_notonly.vpage?trigger=${triggerType!}&subjectTrigger=3">语文</a>

        </label>
    </div>

    <#setting number_format="0.##">
    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <table class="table table-bordered">
        <tr>
            <th>学校ID</th>
            <th>学校全称</th>
            <th>系统班级名称</th>
            <th>班级Id</th>
            <th>科目</th>
            <th>系统老师名</th>
            <th>参与学生人数</th>
            <#if triggerType?? && (triggerType == 3 || triggerType == 4)>
                <th>最新UGC答案</th>
                <th>答案占比</th>
            </#if>
            <th>历史UGC答案</th>
            <#if triggerType?? && triggerType == 0 >
                <th>人工答案</th>
                <th>操作</th>
            </#if>

        </tr>
        <tbody>
            <#if ugcTeacherList?has_content>
                <#list ugcTeacherList.content as ugcteacherlist>
                <tr>
                    <td>${ugcteacherlist.schoolId!}</td>
                    <td>
                        <a href="/crm/school/schoolhomepage.vpage?schoolId=${ugcteacherlist.schoolId!}" target="_blank">${(ugcteacherlist.schoolName)!}</a>
                    </td>
                    <td>${ugcteacherlist.className!}</td>
                    <td>${ugcteacherlist.clazzId!}</td>
                    <td>${ugcteacherlist.subject!}</td>
                    <td>${ugcteacherlist.sysTeacherName!}</td>
                    <td>${ugcteacherlist.validStudentCount!}</td>
                    <#if triggerType?? && (triggerType == 3 || triggerType == 4)>
                        <#assign ugcteacherName = ugcteacherlist.ugcTeacherName!"">
                        <td>${((ugcteacherName=='NULL')?string('',ugcteacherName))!}</td>
                        <td>${((ugcteacherlist.ugcAnswerPercent)!0) * 100}%</td>
                    </#if>
                    <#assign historyugcteachername = ugcteacherlist.historyUgcTeacherName!"">
                    <td>${((historyugcteachername=='NULL')?string('',historyugcteachername))}</td>


                    <#if triggerType?? && triggerType == 0>
                        <#assign ugcteacherName = ugcteacherlist.ugcTeacherName!"">
                        <td>${((ugcteacherName=='NULL')?string('',ugcteacherName))!}</td>
                        <td>
                            <a target = "_blank" href="/crm/ugc/teachername_detail.vpage?clazzId=${ugcteacherlist.clazzId!}&schoolId=${ugcteacherlist.schoolId!}&subject=${ugcteacherlist.subject}&PAGE=${page!}">查看</a>
                        </td>
                    </#if>
                </tr>
                </#list>
            </#if>
        </tbody>
    </table>

    <form id="iform" action="/crm/ugc/teachername_notonly.vpage" method="get">
        <input type="hidden" name="trigger" value="${triggerType!}"/>
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="10" type="hidden"/>
    </form>

    <#assign pager=ugcTeacherList!>
    <#assign everySize = 10000 >
    <#include "../pager_foot.ftl">

    <br>
    <legend>班级老师汇总数据导出</legend>
    <div id="exportTeacherData"></div>

    <br>
    <legend>班级老师详细数据导出</legend>
    <div id="exportTeacherDetailData"></div>
</div>

<script>
    $(function () {
        $.get("ugcTeacherCount.vpage", {}, function (data) {
            var ugcTeacherCount = data.teacherCount;
            var page = ugcTeacherCount /${everySize};
            var href = "";
            for (var i = 0; i < page; i++) {
                var str = '<a href = "exportUgcTeacherData.vpage?exportUgcTeacherPage=' + i + '&everySize=${everySize}">第' + (i + 1) + '页</a>&nbsp;&nbsp;|';
                href = href + str;
            }
            $("#exportTeacherData").append(href);
        });

        $.get("ugcTeacherDetailCount.vpage", {}, function (data) {
            var ugcTeacherDetailCount = data.teacherDetailCount;
            var page = ugcTeacherDetailCount /${everySize};
            var href = "";
            for (var i = 0; i < page; i++) {
                var str = '<a href = "exportUgcTeacherDetailData.vpage?exportTeacherDetailPage=' + i + '&everySize=${everySize}">第' + (i + 1) + '页</a>&nbsp;&nbsp;|';
                href = href + str;
            }
            $("#exportTeacherDetailData").append(href);
        });
    });
</script>
</@layout_default.page>