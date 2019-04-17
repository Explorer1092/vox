<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="添加计划" pageJs="addVisitPlan" footerIndex=4 navBar="hidden">
<@sugar.capsule css=['school']/>
<div class="head fixed-head">
    <a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>
    <span class="return-line"></span>
    <span class="h-title">添加计划</span>
    <a href="javascript:void(0)" class="inner-right js-submitVisPlan">确定</a>
</div>
<div class="flow">
    <div class="item tip">
        必填
    </div>
    <div class="item">
        拜访学校
        <div class="inner-right js-visitSchoolBtn" style="overflow: hidden;">
            <#if plan.schoolName??>${plan.schoolName!''}<#else>请选择</#if>
        </div>
        <input hidden type="text" id="schoolId" name="schoolId" value="${plan.schoolId!''}" class="js-need js-postData" data-einfo="请选择拜访学校"/>
    </div>
    <div id="js-selectDate" class="item" style="position:relative;">
        拜访时间：
        <div class="inner-right select-date js-selectDate" style="color: #50546d;"><#if plan.visitTime?has_content>${plan.visitTime?string("yyyy-MM-dd")!''}<#else><#if startDate??>${startDate?string("yyyy-MM-dd")!}<#else>${.now?string("yyyy-MM-dd")}</#if></#if></div>
        <input id="nextVisitTime" style="position:absolute;width:100%;height:100%;top:0;left:0;border:none;opacity: 0;" class="_calender date js-need js-postData" name="visitTime" type="date" value="<#if plan.visitTime?has_content>${plan.visitTime?string("yyyy-MM-dd")!''}<#else><#if startDate??>${startDate?string("yyyy-MM-dd")!}<#else>${.now?string("yyyy-MM-dd")}</#if></#if>" data-einfo="请选择拜访时间"/>
    </div>

    <div class="item tip">
        选填
    </div>
    <div class="item">
        计划内容
        <textarea rows="5" class="content js-postData" id="content" name="content" value="<#if plan.content??>${plan.content!''}</#if>" placeholder="填写进校时，计划做什么..."><#if plan.content??>${plan.content!''}</#if></textarea>
    </div>
</div>
<script>
    $("#nextVisitTime").width($("#js-selectDate").outerWidth());
    var AT = new agentTool();
    AT.cleanAllCookie();
</script>
</@layout.page>