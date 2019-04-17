<#import './layout.ftl' as layout>

<@layout.page className='ChildWish bgYellow' pageJs="second" title="孩子心愿">

<#escape x as x?html>

    <#assign topType = "topTitle">
    <#assign topTitle = "孩子心愿">
    <#include "./top.ftl" >

    <div class="pr-box pr-mt">
        <div class="pr-apply">
            <div class="pr-title pr-title-yellow"></div>
            <#if result.success>
                <#assign wishes = result.wishes![] studentName = result.studentName>
                <#if wishes?size == 0>
                    <div class="clearfix" style="line-height: 2rem; text-align: center; color: #999; padding: 3rem 0;">孩子还没许愿，快去创建新目标，<br>给孩子一份惊喜吧！</div>
                <#else>
                    <ul class="pr-contentList">
                        <#list wishes as wish>
                            <li>
                                <#assign isOnGoing = wish.missionState?exists && wish.missionState == "ONGOING">
                                <#assign text = isOnGoing?string(
                                    '申请更新一次目标完成进度,目标：${wish.mission!""}',
                                    '许了一个愿望：${wish.rewards!""}'
                                )>

                                <div class="contentText">
                                    <p>${wish.missionDate!"" }</p>
                                    <p>${studentName!""} ${text!""}</p>
                                </div>
                                <div class="contentFoot">
                                    <#assign wishId = wish.id!"">
                                    <#if isOnGoing>
                                        <#if (wish.finishCount!0)  gte (wish.totalCount!0)>
                                            <a href="javascript:;" class="btn btn-green doUpdateProgress doTrack" data-track_error="wish_step"  data-track="parent|complete_click" data-completed = "1"  data-missionid="${wishId}">完成目标</a>
                                        <#else>
                                            <a href="javascript:;" class="btn btn-green doUpdateProgress doTrack" data-track_error="wish_complete"  data-track="parent|wish_step" data-completed = "0"  data-missionid="${wishId}">进度+1</a>
                                        </#if>
                                    <#else>
                                        <a href="/parentMobile/parentreward/createTask.vpage?step=v2&sid=${sid}&wish=${wish.rewards!''}&missionid=${wishId}" class="btn btn-yellow doTrack" data-track="parent|wish_setgoal">创建目标</a>
                                    </#if>
                                </div>
                            </li>
                        </#list>
                    </ul>
                </#if>
            <#else>
                <p class="hide doAutoTrack" data-track="parent|wish_fail"></p>
                <#assign info = result.info errorCode = result.errorCode>
                <#include "errorTemple/errorBlock.ftl">
            </#if>
        </div>
    </div>
</#escape>

</@layout.page>

