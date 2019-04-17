<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="新增老师">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText">新增老师</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-date">
    <div class="date" id="select-date"><span><em></em>${startDate!''} ~ ${endDate!''}</span></div>
</div>
<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">区域</div>
            <div class="side-fr side-time side-width">认证</div>
            <div class="side-fr side-time side-width">注册</div>
        </div>
    </li>
    <#if  regionPerformance?has_content>
        <#assign totalTeaAuthNum = 0>
        <#assign totalAddTeaRegNum = 0>
        <#list regionPerformance as performance>
            <#assign addTeaAuthNum = performance.addTeaAuthNum!0>
            <#assign addTeaRegNum = performance.addTeaRegNum!0>
            <#if performance.type == "REGION" || addTeaAuthNum gt 0 || addTeaRegNum gt 0>
                <#assign totalTeaAuthNum = totalTeaAuthNum + addTeaAuthNum>
                <#assign totalAddTeaRegNum = totalAddTeaRegNum + addTeaRegNum>
                <li>
                    <#if performance.type == "REGION">
                    <a href="/mobile/performance/user_region_teacher.vpage?startDate=${startDate!''}&endDate=${endDate!''}&regionCode=${performance.key!''}" class="link link-ico">
                    <#else>
                    <a href="/mobile/performance/school_add_teacher.vpage?startDate=${startDate!''}&endDate=${endDate!''}&schoolId=${performance.key!''}" class="link link-ico">
                    </#if>
                    <div class="side-fl" style="width: 50%; line-height: 1.5rem;">${performance.name!'未知'}</div>
                    <div class="side-fr side-orange side-width">${addTeaAuthNum!}</div>
                    <div class="side-fr side-orange side-width">${addTeaRegNum!}</div>
                </a>
                </li>
            </#if>
        </#list>
        <li>
            <a href="javascript:void(0);" class="link linkNo-ico">
                <div class="side-fl side-total" style="width: 50%; line-height: 1.5rem;">总计</div>
                <div class="side-fr side-total side-width">${totalTeaAuthNum}</div>
                <div class="side-fr side-total side-width">${totalAddTeaRegNum}</div>
            </a>
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
            window.location.href = "/mobile/performance/user_region_teacher.vpage?startDate=" + startDate + "&endDate=" + endDate + "&regionCode=${regionCode!''}";
        });
    });
</script>
</@layout.page>