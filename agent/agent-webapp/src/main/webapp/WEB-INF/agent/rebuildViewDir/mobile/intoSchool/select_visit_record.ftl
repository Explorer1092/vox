<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择下属拜访记录" pageJs="" footerIndex=4 navBar="hidden">
<@sugar.capsule css=['new_base','school']/>
<#--<div class="head fixed-head">-->
    <#--<a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>-->
    <#--<span class="return-line"></span>-->
    <#--<span class="h-title">选择下属拜访记录</span>-->
<#--</div>-->
    <#if visitRecord?? && visitRecord?size gt 0>
        <#list visitRecord as vr>
        <div class="s-record">
            <div class="item">
                <p class="time">
                    ${vr.workTime?string("yyyy-MM-dd")!''}
                </p>
                <div class="info clearfix">
                    <p style="font-size:0.75rem;">${vr.partnerName!""}(${vr.schoolName!''})</p>
                    <p>平台培训｜拜访${vr.visitTeacherList?size!0}位关键人</p>
                    <p>拜访效果及详情：</p>
                    <#if vr.visitTeacherList??>
                        <#if vr.visitTeacherList?size gt 0>
                            <#list vr.visitTeacherList as vt>
                                <p>${vt.teacherName!""}：${vt.visitInfo!''} </p>
                            </#list>
                        </#if>
                    </#if>
                    <#if vr.followingPlan??>
                        <p>待办：${vr.followingPlan!''}</p>
                    </#if>
                    <#if vr.followingTime??>
                        <p>
                            计划下次：${vr.followingTime?string("yyyy年MM月dd日")!''}
                        </p>
                    </#if>
                    <a href="addVisit.vpage?schoolRecordId=${vr.schoolRecordId!0}&schoolId=${vr.schoolId!0}">
                        <div class="item btn-stroke fix-padding the" style="padding-top:0;float:right;width:6.5rem">
                            去填写陪访建议
                            <div class="inner-right" style="color:transparent">.</div>
                        </div>
                    </a>
                </div>
            </div>
        </#list>
</div>
<#else>
<p style="text-align: center;font-size: 18px;margin-top: 50px;">
    没有可选择的拜访记录，快请您的下属去填写吧
</p>
</#if>
</@layout.page>