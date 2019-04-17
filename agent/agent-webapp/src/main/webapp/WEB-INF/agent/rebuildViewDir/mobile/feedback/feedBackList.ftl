<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="产品反馈列表" pageJs="feedbackList" footerIndex=4>
<@sugar.capsule css=['feedback']/>
<style>body{background-color:rgb(241,242,245)}</style>
<#--<div class="head fixed-head">-->
    <#--<a class="return" href="/mobile/performance/index.vpage"><i class="return-icon"></i>返回</a>-->
    <#--<span class="return-line"></span>-->
    <#--<span class="h-title">产品反馈列表</span>-->
    <#--<a href="/mobile/feedback/view/feedbackinfo.vpage" class="inner-right "><i class="icon-add"></i></a>-->
<#--</div>-->
<div class="main_box" style="padding-top:.5rem">
<#if feedbackList.isManager!false>
<div class="fBack-section">
    <div class="fBack-list arrowR">
        <label>下属反馈</label>
        <div class="right"><span class="txtRed chooseTeacher">请选择</span></div>
    </div>
</div>
</#if>
<div class="fBack-section">
    <div class="fBack-list height-s">
        <span class="txtGray"><#if feedbackList.isManager!false>${userName!'我'}<#else>我</#if>的反馈：累计${feedbackList.totalFeedbackCount!0}条，本月${feedbackList.tmFeedbackCount!0}条</span>
    </div>
</div>
<#if feedbackList?? && feedbackList.productFeedbackInfos?? && feedbackList.productFeedbackInfos?size gt 0>
<#list feedbackList.productFeedbackInfos as product>
    <a onclick="openSecond('/mobile/feedback/view/feedbackinfo.vpage?feedbackId=${product.id!''}')">
<div class="fBack-section">
    <div class="fBack-message">
        <div class="hd"><#if product.feedbackType?? && product.feedbackType?has_content>${product.feedbackType.desc!''}</#if><#if product.teacherId?has_content>—${product.teacherName!''}（${product.teacherId!''}）</#if><#if product.onlineFlag?? && product.onlineFlag><span class="label red">已上线</span></#if><span class="txtRed">${product.statusDesc!''}</span></div>
        <#--<div class="ft"></div>-->
        <div class="mn" style="word-wrap:break-word;font-size:.6rem;line-height:.9rem">${product.fbContent!''}</div>
        <#if product.onlineDate?? && product.onlineDate?has_content><div class="ft"><span class="time" style="color:#000">预计上线日期: ${product.onlineDate!''}</span></div></#if>
        <div class="aside">
            <#if product.processResultList?? && product.processResultList?size gt 0>
            <ul>
                <#list product.processResultList as process>
                <li><i class="avatar"></i>${process.accountName!''}：${process.processNotes!''}</li>
                </#list>
            </ul>
            </#if>
        </div>
    </div>
</div>
    </a>
</#list>
<#else>
        <div class="fBack-section" style="background-color:rgb(241,242,245)">
            您还未反馈任何建议，马上点击右上角加号创建吧！
        </div>
</#if>
</div>
<script>
    window.onload = function () {
        try {
            reloadCallBack();
        } catch (e) {
            alert(e);
        }
    };

    $(document).on('click','.chooseTeacher',function(){
        location.href = "/mobile/performance/choose_agent.vpage?breakUrl=feedback&needCityManage=1"
    });
</script>
</@layout.page>