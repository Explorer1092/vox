<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="进校前/后数据对比">

<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText" id="pageTitle">进校前/后数据对比</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-title">${(schoolRecordData.schoolName)!''}</div>

<div class="mobileCRM-V2-rankInfo">
    <div class="infoBox infoTab">
        <div class="active" name="action" method="Teacher">
            <#if (schoolRecordData.teacherTotal!=0)!false>
                <div class="boxNum">${schoolRecordData.teacherTotal!0}</div>
            <#else>
                <div class="boxNum">-</div>
            </#if>
            <div class="boxFoot">老师总数</div>
        </div>
        <div name="action" method="Student">
            <#if (schoolRecordData.studentTotal!=0)!false>
                <div class="boxNum boxNum-green">${schoolRecordData.studentTotal!0}</div>
            <#else>
                <div class="boxNum boxNum-green">-</div>
            </#if>

            <div class="boxFoot">学生总数</div>
        </div>
    </div>
</div>

<table class="mobileCRM-V2-table" id="Teacher">
    <#if schoolRecordData??>
        <thead>
        <tr>
            <td>时间</td>
            <td>注册</td>
            <td>注册率</td>
            <td>认证</td>
            <td>认证率</td>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>进校前</td>
            <td class="orange">
                <#if (schoolRecordData.teacherRegCountBefore!=0)!false>
                ${schoolRecordData.teacherRegCountBefore!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherRegRateBefore!=0)!false>
                ${schoolRecordData.teacherRegRateBefore!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="orange">
                <#if (schoolRecordData.teacherAuthCountBefore!=0)!false>
                ${schoolRecordData.teacherAuthCountBefore!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherAuthRateBefore!=0)!false>
                ${schoolRecordData.teacherAuthRateBefore!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        <tr>
            <td>7天后</td>
            <td class="orange">
                <#if (schoolRecordData.teacherRegCount7Days!=0)!false>
                ${schoolRecordData.teacherRegCount7Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherRegRate7Days!=0)!false>
                ${schoolRecordData.teacherRegRate7Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="orange">
                <#if (schoolRecordData.teacherAuthCount7Days!=0)!false>
                ${schoolRecordData.teacherAuthCount7Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherAuthRate7Days!=0)!false>
                ${schoolRecordData.teacherAuthRate7Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        <tr>
            <td>14天后</td>
            <td class="orange">
                <#if (schoolRecordData.teacherRegCount14Days!=0)!false>
                ${schoolRecordData.teacherRegCount14Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherRegRate14Days!=0)!false>
                ${schoolRecordData.teacherRegRate14Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="orange">
                <#if (schoolRecordData.teacherAuthCount14Days!=0)!false>
                ${schoolRecordData.teacherAuthCount14Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherAuthRate14Days!=0)!false>
                ${schoolRecordData.teacherAuthRate14Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        <tr>
            <td>最新</td>
            <td class="orange">
                <#if (schoolRecordData.teacherRegCount!=0)!false>
                ${schoolRecordData.teacherRegCount!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherRegRate!=0)!false>
                ${schoolRecordData.teacherRegRate!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="orange">
                <#if (schoolRecordData.teacherAuthCount!=0)!false>
                ${schoolRecordData.teacherAuthCount!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.teacherAuthRate!=0)!false>
                ${schoolRecordData.teacherAuthRate!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        </tbody>
    </table>

    <table class="mobileCRM-V2-table" id="Student" style="display: none">
        <thead>
        <tr>
            <td>时间</td>
            <td>注册</td>
            <td>注册率</td>
            <td>认证</td>
            <td>认证率</td>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>进校前</td>
            <td class="green">
                <#if (schoolRecordData.studentRegCountBefore!=0)!false>
                ${schoolRecordData.studentRegCountBefore!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentRegRateBefore!=0)!false>
                ${schoolRecordData.studentRegRateBefore!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="green">
                <#if (schoolRecordData.studentAuthCountBefore!=0)!false>
                ${schoolRecordData.studentAuthCountBefore!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentAuthRateBefore!=0)!false>
                ${schoolRecordData.studentAuthRateBefore!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        <tr>
            <td>7天后</td>
            <td class="green">
                <#if (schoolRecordData.studentRegCount7Days!=0)!false>
                ${schoolRecordData.studentRegCount7Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentRegRate7Days!=0)!false>
                ${schoolRecordData.studentRegRate7Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="green">
                <#if (schoolRecordData.studentAuthCount7Days!=0)!false>
                ${schoolRecordData.studentAuthCount7Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentAuthRate7Days!=0)!false>
                ${schoolRecordData.studentAuthRate7Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        <tr>
            <td>14天后</td>
            <td class="green">
                <#if (schoolRecordData.studentRegCount14Days!=0)!false>
                ${schoolRecordData.studentRegCount14Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentRegRate14Days!=0)!false>
                ${schoolRecordData.studentRegRate14Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="green">
                <#if (schoolRecordData.studentAuthCount14Days!=0)!false>
                ${schoolRecordData.studentAuthCount14Days!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentAuthRate14Days!=0)!false>
                ${schoolRecordData.studentAuthRate14Days!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        <tr>
            <td>最新</td>
            <td class="green">
                <#if (schoolRecordData.studentRegCount!=0)!false>
                ${schoolRecordData.studentRegCount!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentRegRate!=0)!false>
                ${schoolRecordData.studentRegRate!'0'}
                <#else>
                    -
                </#if>%
            </td>
            <td class="green">
                <#if (schoolRecordData.studentAuthCount!=0)!false>
                ${schoolRecordData.studentAuthCount!'0'}
                <#else>
                    -
                </#if>
            </td>
            <td class="gray">
                <#if (schoolRecordData.studentAuthRate!=0)!false>
                ${schoolRecordData.studentAuthRate!'0'}
                <#else>
                    -
                </#if>%
            </td>
        </tr>
        </tbody>
    </#if>
</table>

<script type="text/javascript">
    $(function () {
        $("div[name='action']").click(function () {
            var id = $(this).attr("method");
            $("div[name='action']").removeClass("active");
            $(this).addClass("active");
            $(".mobileCRM-V2-table").hide();
            $("#" + id).show();
        });
    });
</script>
</@layout.page>