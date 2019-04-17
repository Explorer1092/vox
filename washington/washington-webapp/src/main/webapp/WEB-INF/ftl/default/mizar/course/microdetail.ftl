<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="${(course.periodName)!''}"
pageJs=["detail"]
pageJsFile={"detail" : "public/script/mobile/mizar/microdetail"}
pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/liveLesson"]}
bodyClass="bg-grey">
    <#--<#include "../function.ftl"/>-->

<div class="sub-header">
    <#if (course.video)?has_content>
        <video id="microVideo" width="100%" height="100%" controls preload="auto"  poster="${(course.img)!''}" style="display: block;">
            <source src="${(course.video)!''}" type="video/mp4">
        </video>
    <#else>
        <#if (course.img)?has_content>
            <img src="${(course.img)!''}" />
        </#if>
    </#if>
</div>
<div class="sub-main">
    <div class="sub-tab" id="activityShowBox">
        <ul>
            <li class="active" data-name="activity"><a href="javascript:void(0);">课程介绍</a></li>
            <#if course.series?has_content>
                <li data-name="series"><a href="javascript:void(0);">系列课程</a></li>
            </#if>
            <#if course.longClassUrl?has_content && course.longClassPhoto?has_content>
                <li data-name="clazz"><a href="javascript:void(0);">配套长期班</a></li>
            </#if>
        </ul>
    </div>
    <div class="sub-actIntro showAll">
        <div class="sub-text">
            <div class="sub-box-text" style="word-break:break-all;word-wrap:break-word;">
                ${(course.intro)!'暂无课程介绍！'}
            </div>
        </div>
        <#--<div class="showBtn actShowBtn" style="display: none;">展开更多</div>-->
    </div>
    <#if course.series?has_content>
        <div class="d-courseList">
            <div class="sub-title" id="seriesShowBox">系列课程</div>
            <#if course.series?size gt 0>
                <ul>
                <#list course.series as seriesList>
                    <#if (seriesList_index) lt 5>
                        <li class="dc-list" data-bind="click: $root.seriesClick.bind($data,'${(seriesList.id)!0}')"><a href="javascript:;">${(seriesList.name)!'--'}</a><span>${(seriesList.desc)!''}</span></li>
                    </#if>
                    <#if (seriesList_index) gt 4>
                        <li class="dc-list gtMore" data-bind="click: $root.seriesClick.bind($data,'${(seriesList.id)!0}')" style="display:none; <#if (seriesList_index) == (course.series?size-1)> border-bottom:0;</#if>"><a href="javascript:;">${(seriesList.name)!'--'}</a><span>${(seriesList.desc)!''}</span></li>
                    </#if>
                </#list>
                </ul>
                <#if course.series?size gt 5>
                    <div class="serShowBtn">展开更多</div>
                </#if>
            </#if>
        </div>
    </#if>
    <#if course.longClassUrl?has_content && course.longClassPhoto?has_content>
    <div id="clazzBox">
        <div class="sub-title">配套长期班</div>
        <div class="corseclazzbox">
            <a href="${(course.longClassUrl)!'javascript:;'}"><img src="${(course.longClassPhoto)!''}" /></a>
        </div>
    </div>
    </#if>
    <div class="sub-footer">
        <div class="footerFixed">
            <#if timeFlag == "BEFORE" || !(pid?has_content && pid?string != "")>
                <#if status==0>
                    <div class="innerBox">
                        <#if course.price == 0>
                            <a href="javascript:;" class="w-btn JS-indexSubmit" data-logs="{database: 'parent', m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'开课前'}" data-type="Subscribe">${(course.btnContent1)!'--'}</a>
                        <#elseif course.price gt 0>
                            <a href="javascript:;" data-bind="click: payHrefClick" data-logs="{database: 'parent', m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'开课前'}" class="w-btn">${(course.btnContent1)!'--'}</a>
                        </#if>
                    </div>
                </#if>

                <#if status == 1 || status == 2 >
                    <div class="mBase-list" data-bind="click: subscribeSucPop">
                        <div class="w-btn disabled"><a href="javascript:;">教室入口</a><span>课前15分钟开放</span></div>
                        <span class="txt"><#if status == 1>购买<#elseif status == 2>预约</#if>成功，上课前您将收到提醒</span>
                    </div>
                    <#--<#if (course.qqTip?has_content && course.qqUrl?has_content)!false>
                        <div class="mBase-list">
                            <a href="${(course.qqUrl)!'javascript:;'}" class="w-btn">立即加群</a>
                            <span class="txt">${(course.qqTip)!'--'}</span>
                        </div>
                    <#else>
                        <div class="mBase-list"><#if status == 1>购买<#elseif status == 2>预约</#if>成功，将于<span class="courseTimes"></span>在此页面开放入口</div>
                    </#if>-->
                </#if>
            <#elseif timeFlag == "ING">
                    <#if course.price gt 0>
                        <#if status==0>
                        <div class="mBase-list">
                            <a href="javascript:;" data-bind="click: payHrefClick" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播中'}" class="w-btn">立即购买</a>
                            <span class="txt">${(course.btnContent2)!'--'}</span>
                        </div>
                        <#elseif status == 1>
                            <div class="innerBox">
                            <#if course.liveUrl?has_content>
                                <a href="${(course.liveUrl)!'#'}" data-bind="" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播中'}" class="w-btn"><i class="tv-icon"></i>正在直播，点击进入直播间</a>
                            <#else>
                                <a href="javascript:;" data-bind="click: ingVideoRoomClick.bind($data,'${(course.periodId)!0}',1)" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播中'}" class="w-btn"><i class="tv-icon"></i>正在直播，点击进入直播间</a>
                            </#if>
                            </div>
                        </#if>
                    <#elseif course.price == 0 >
                        <div class="innerBox">
                            <#if course.liveUrl?has_content>
                                <a href="${(course.liveUrl)!'#'}" data-bind="" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播中'}" class="w-btn"><i class="tv-icon"></i>正在直播，点击进入直播间</a>
                            <#else>
                                <a href="javascript:;" data-bind="click: ingVideoRoomClick.bind($data,'${(course.periodId)!0}',1)" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播中'}" class="w-btn"><i class="tv-icon"></i>正在直播，点击进入直播间</a>
                            </#if>
                        </div>
                    </#if>
            <#elseif timeFlag == "AFTER">
                <#if course.price gt 0>
                    <#if status==0>
                        <div class="mBase-list">
                            <a href="javascript:;" data-bind="click: payHrefClick" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播结束'}" class="w-btn">立即购买</a>
                            <span class="txt">直播已结束，购买课程回放</span>
                        </div>
                    <#elseif status == 1>
                        <div class="mBase-list">
                        <#if waiting>
                            <a href="javascript:void(0);" class="w-btn">即将开放</a>
                            <span class="txt">直播已结束，正在生成回放</span>
                        <#else>
                            <#if course.replayUrl?has_content>
                                <a href="${(course.replayUrl)!'#'}" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播结束'}" class="w-btn">查看回放</a>
                                <span class="txt">直播已结束</span>
                            <#else>
                                <a href="javascript:;" data-bind="click: ingVideoRoomClick.bind($data,'${(course.periodId)!0}',2)" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播结束'}" class="w-btn">查看回放</a>
                                <span class="txt">直播已结束</span>
                            </#if>
                        </#if>
                        </div>
                    </#if>
                <#elseif course.price == 0 >
                    <div class="mBase-list">
                    <#if waiting>
                        <a href="javascript:void(0);" class="w-btn">即将开放</a>
                        <span class="txt">直播已结束，正在生成回放</span>
                    <#else>
                        <#if course.replayUrl?has_content>
                            <a href="${(course.replayUrl)!'#'}" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播结束'}" class="w-btn">查看回放</a>
                            <span class="txt">直播已结束</span>
                        <#else>
                            <a href="javascript:;" data-bind="click: ingVideoRoomClick.bind($data,'${(course.periodId)!0}',2)" data-logs="{database: 'parent',m: 'm_kUjlHnIL', op: 'o_fxpl7mJn',s0:'${(course.periodId)!0}',s1:yq.getQuery('track'),s2:'直播结束'}" class="w-btn">查看回放</a>
                            <span class="txt">直播已结束</span>
                        </#if>
                    </#if>
                    </div>
                </#if>
            </#if>
        </div>
    </div>
    <#if course.qqUrl?has_content!false>
        <div class="course-add">
            <a href="${(course.qqUrl)!'javascript:;'}">课程咨询群</a>
        </div>
    </#if>
    <script>
        var initMode = "MicroDetailMode";
        var microData = {
            id:"${id!0}",
            payAll:"${payAll?c}",
            courseTime:"${(course.startTime)!0}",
            qqUrl: "${(course.qqUrl)!''}",
            timeSure:"${timeFlag}"
        },logged = <#if pid?has_content && pid?string != "">true<#else>false</#if>;
    </script>

</@layout.page>