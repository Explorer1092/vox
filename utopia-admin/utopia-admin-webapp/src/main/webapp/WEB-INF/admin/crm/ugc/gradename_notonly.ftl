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
            年级分布&nbsp; &nbsp;
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
                    <a href="/crm/ugc/gradename_notonly.vpage?trigger=0">答案不唯一</a>
                </th>
                <th <#if triggerType == 3>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/gradename_notonly.vpage?trigger=3">答案变更</a>
                </th>
                <th <#if triggerType == 4>style="background-color: #e6ebed"</#if>>
                    <a href="/crm/ugc/gradename_notonly.vpage?trigger=4">与系统年级不符</a>
                </th>
            </tr>
        </table>
    </div>

    <#setting number_format="0.##">
    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <table class="table table-bordered">
        <tr>
            <th>学校ID</th>
            <th>学校全称</th>
            <th>系统年级分布</th>
            <th>参与学生人数</th>
            <#if triggerType?? && (triggerType == 3 || triggerType == 4)>
                <th>最新UGC答案</th>
                <th>答案占比</th>
            </#if>
            <th>历史UGC答案</th>
            <#if triggerType?? && triggerType == 0 >
                <th>人工生成答案</th>
                <th>操作</th>
            </#if>

        </tr>
        <tbody>
            <#if ugcGradeList?has_content>
                <#list ugcGradeList.content as ugcgradelist>
                <tr>
                    <td>${ugcgradelist.schoolId!}</td>
                    <td>
                        <a href="/crm/school/schoolhomepage.vpage?schoolId=${ugcgradelist.schoolId!}" target="_blank">${(ugcgradelist.schoolName)!}</a>
                    </td>
                    <td>${ugcgradelist.gradeName!}</td>

                    <td>${ugcgradelist.validStudentCount!}</td>
                    <#if triggerType?? && (triggerType == 3 || triggerType == 4)>
                        <#assign ugcgradeNames = ugcgradelist.ugcGradeNames!>
                        <td>${((ugcgradeNames=='NULL')?string('',ugcgradeNames))!}</td>
                        <td>${((ugcgradelist.ugcAnswerPercent)!0) * 100}%</td>
                    </#if>
                    <#assign historyUgcGradeName = (ugcgradelist.historyUgcGradeName!'NULL')>
                        <td>${((historyUgcGradeName=='NULL')?string('',historyUgcGradeName))}</td>


                    <#if triggerType?? && triggerType == 0>
                        <#assign ugcgradeNames = ugcgradelist.ugcGradeNames!>
                        <td>${((ugcgradeNames=='NULL')?string('',ugcgradeNames))!}</td>
                        <td>
                            <a target = "_blank" href="/crm/ugc/gradename_detail.vpage?schoolId=${ugcgradelist.schoolId!}&PAGE=${page!}">查看</a>
                        </td>
                    </#if>
                </tr>
                </#list>
            </#if>
        </tbody>
    </table>

    <form id="iform" action="/crm/ugc/gradename_notonly.vpage" method="get">
        <input type="hidden" name="trigger" value="${triggerType!}"/>
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="10" type="hidden"/>
    </form>

    <#assign pager=ugcGradeList!>
    <#assign everySize = 10000 >
    <#include "../pager_foot.ftl">

    <br>
    <legend>汇总数据导出</legend>
    <div id="exportGradeData"></div>

    <br>
    <legend>明细数据导出</legend>
    <div id="exportGradeDetailData"></div>
</div>

<script>
    $(function () {
        $.get("ugcGradeCount.vpage", {}, function (data) {
            var ugcSchoolCount = data.gradeCount;
            var page = ugcSchoolCount /${everySize};
            var href = "";
            for (var i = 0; i < page; i++) {
                var str = '<a href = "exportUgcGradeData.vpage?exportUgcGradePage=' + i + '&everySize=${everySize}">第' + (i + 1) + '页</a>&nbsp;&nbsp;|';
                href = href + str;
            }
            $("#exportGradeData").append(href);
        });

        $.get("ugcGradeDetailCount.vpage", {}, function (data) {
            var ugcSchoolDetailCount = data.gradeDetailCount;
            var page = ugcSchoolDetailCount /${everySize};
            var href = "";
            for (var i = 0; i < page; i++) {
                var str = '<a href = "exportUgcGradeDetailData.vpage?exportgradeDetailPage=' + i + '&everySize=${everySize}">第' + (i + 1) + '页</a>&nbsp;&nbsp;|';
                href = href + str;
            }
            $("#exportGradeDetailData").append(href);
        });
    });
</script>
</@layout_default.page>