<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学生奖品">
    <@sugar.capsule css=['rewardLogistics']/>
<style>
    .nav.tab-head.c-flex.c-flex-5{
        display: none;
    }
</style>
<div class="crmList-box resources-box">
    <div>
        <#if rewardLogistics?? && (rewardLogistics?size > 0 )>
            <#list rewardLogistics as rl>
                <div class="reward-box reward-gap">
                    <div class="particular-title"><span class="data">发货月份：${rl.month!''}</span></div>
                    <div class="particular-body">
                        <div class="left"><a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${rl.receiverId!''}">收货老师：<p>${rl.receiverName!'暂未导入数据'}</p></a></div>
                        <#if rl.rewardStatus??>
                            <span style="float:right;margin-right:.5rem">
                                <#if rl.rewardStatus == -1>
                                    <i class="reward_expired"></i>
                                <#elseif rl.rewardStatus == 0>
                                    <i class="reward_to_get"></i>
                                <#elseif rl.rewardStatus == 1>
                                    <i class="reward_recevied"></i>
                                </#if>
                            </span>
                        </#if>
                    </div>
                    <div class="particular-body" style="padding:0rem 1rem 0.65rem 0;">
                        <div class="left">快递单号：<#if rl.logisticNo?has_content>${rl.logisticNo!''}（${rl.companyName!''}）<#else>暂未导入</#if></div>
                    </div>
                </div>
            </#list>
        <#else>
            <div class ="nonInfo">暂无兑换记录</div>
            <#--<div style="background:#fff;text-align:center;margin-top:2rem;width:100%;height:3rem;padding-top:2rem" > 暂无兑换记录 </div>-->
        </#if>
    </div>
</div>
</@layout.page>