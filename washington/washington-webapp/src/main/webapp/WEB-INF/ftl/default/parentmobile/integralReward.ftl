<#import './layout.ftl' as layout>

<@layout.page className='Index' pageJs='index' title="学豆奖励">

    <#escape x as x?html>

        <#assign topType = "topTitle">
        <#assign topTitle = "学豆奖励">
        <#include "./top.ftl" >

        <#if result.success>
            <#assign otherParents = result.rewardRank![] integral = result.integralPrize!0 scoreLt60 = integral == 0 studentName=result.studentName!"">

            <#noescape>${buildAutoTrackTag("hwbean|getbean_open")}</#noescape>
            <#if scoreLt60>
                <div class="parentApp-rewardBeansHD parentApp-rewardBeansHD-null">
                    <div class="main">${studentName}本次得分低于60，请下次加油！</div>
                </div>
                <#noescape>${buildAutoTrackTag("hwbean|getnobean_open")}</#noescape>
            <#else>
                <div class="parentApp-rewardBeansHD">
                    <div class="main">你为${studentName}领取了<span>${integral}</span>学豆！</div>
                </div>
            </#if>

            <div class="parentApp-rewardBeansList">
                <#if otherParents?size == 0 >
                    <div class="null">本次作业还没有最高奖励记录～</div>
                <#else>
                    <div class="head">
                        <div class="main">
                            <div class="text">本次作业最高奖励</div>
                        </div>
                    </div>
                    <ul class="list">
                        <#list otherParents as parent>
                            <li>
                                <div class="text">${parent.student!""}家长</div>
                                <div class="beans">+${parent.integral!0}</div>
                            </li>
                        </#list>
                    </ul>
                </#if>
            </div>
        <#else>
            <#noescape>${buildAutoTrackTag("hwbean|getbean_error")}</#noescape>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>

    </#escape>

</@layout.page>
