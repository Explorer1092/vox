<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="用户青苗参与情况" page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<style>

</style>

<span class="span9" style="font-size: 14px">

    <#include '../../userinfotitle.ftl' />

    <form class="form-horizontal" action="/equator/sapling/activity/history.vpage" method="post">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <ul class="inline selectbtn">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus" placeholder="输入学生ID" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            <input type="submit" value="查询" class="btn btn-primary"/>
            <#if studentName??>
                ${studentName}｜
                <#if rich>施助者<#else>受助者</#if>
            </#if>
        </ul>
        <ul class="inline">
        <#if bgnDate?? && endDate??>
            当前种子周期：${bgnDate} ~ ${endDate}
        <#else>
            当前不存在种子周期
        </#if>
        </ul>
    </form>

    <div>
        <#if studentPresentInfo ?? >
        <#if studentPresentInfo.presentSendCount?has_content>
            <span style="color: #00a30c;font-weight: bold">累计帮助：${studentPresentInfo.presentSendCount}人</span>
        </#if>
        </#if>
    <table class="table table-bordered table-condensed" id="mainTable">
        <tr>
            <th>时间</th>
            <th>产品</th>
        </tr>

        <#if studentId??>
        <tbody id="tbody">
        <#if studentPresentInfo ?? >
        <#if studentPresentInfo.presentTakeDetailList?has_content>
            <#list studentPresentInfo.presentTakeDetailList?sort_by(["date"])?reverse as presentDetail>
            <tr <#--<#if (presentDetail.date gte bgnDate) && (endDate gte presentDetail.date)>style="background: #ecf0ff;"</#if>-->>
                <td>${presentDetail.date}</td>
                <td>${presentDetail.appKey}</td>
            </tr>
            </#list>
        </#if>
        </#if>
        </tbody>
        </#if>
    </table>
    </div>

</span>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script>
    $(function () {
    });
</script>
</@layout_default.page>