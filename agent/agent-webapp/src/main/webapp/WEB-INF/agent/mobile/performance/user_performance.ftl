<#import "../layout_new.ftl" as layout>
<@layout.page group="业绩" title="我的业绩">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <#if data == 2>
                <#assign header = "双科认证">
            </#if>
            <div class="headerText">${header!"新增认证"}</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-date">
    <div class="date" id="select-date"><span><em></em>${startDate!''} ~ ${endDate!''}</span></div>
</div>
<ul class="mobileCRM-V2-list">
    <#if regionPerformance?has_content>
        <#assign total = 0>
        <#list regionPerformance as performance>
            <#if data == 2>
                <#assign num = performance.stuSlDsaNum!0>
            <#else>
                <#assign num = performance.addStuAuthNum!0>
            </#if>
            <#assign total = total + num>
            <#if performance.type != "SCHOOL" || num gt 0>
                <li>
                    <#if performance.type != "SCHOOL">
                    <a href="/mobile/performance/user_performance.vpage?startDate=${startDate!''}&endDate=${endDate!''}&regionCode=${performance.key!''}&DATA=${data!0}&DATATYPE=${performance.type!''}" class="link link-ico">
                    <#else>
                    <a href="/mobile/school/school_info.vpage?schoolId=${performance.key!''}" class="link link-ico">
                    </#if>
                    <div class="side-fl" style="width: 60%; line-height: 1.5rem;">${performance.name!'未知'}<#if (performance.type) == "SCHOOL_REGION" || (performance.type) == "REGION_NONE_MAIN_CITY_COUNTY">（指定学校）</#if></div>
                    <div class="side-fr side-orange side-width" style="padding: 0 1.0rem 0 0;">${num!}</div>
                </a>
                </li>
            </#if>
        </#list>
        <li style="color: #7C7C7C">
            <div class="side-fl" style="width: 60%; line-height: 1.5rem;">合计</div>
            <div class="side-fr side-width" style="padding: 0 1.0rem 0 0;">${total!}</div>
        </li>
    </#if>
</ul>
<div class="mobileCRM-V2-layer" style="display:none">
    <div class="dateBox">
        <div class="boxInner">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">起始日期</div>
                        <input type="date" id="start" value="${startDate!''}" placeholder="${startDate!''}" class="textDate">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">结束日期</div>
                        <input type="date" id="end" value="${endDate!''}" placeholder="${endDate!''}" class="textDate">
                    </div>
                </li>
            </ul>
            <div class="boxFoot">
                <div class="side-fl" id="select-cancel">取消</div>
                <div class="side-fr" id="select-ok">确定</div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#select-date").click(function () {
            $(".mobileCRM-V2-layer").show();
        });
        $("#select-cancel").click(function () {
            $(".mobileCRM-V2-layer").hide();
        });
        $("#select-ok").click(function () {
            var startDate = $("#start").val();
            if (!startDate || startDate === "") {
                alert("请输入起始日期");
                return;
            }
            var endDate = $("#end").val();
            if (!endDate || endDate === "") {
                alert("请输入结束日期");
                return;
            }
            if (new Date(startDate) > new Date(endDate)) {
                alert("起始日期大于结束日期，请重新输入");
                return;
            }
            window.location.href = "/mobile/performance/user_performance.vpage?startDate=" + startDate + "&endDate=" + endDate + "&regionCode=${regionCode!''}&DATA=${data!0}&DATATYPE=${dataType!''}";
        });
    });
</script>
</@layout.page>