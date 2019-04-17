<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='${(activity.title)!"亲子活动"}'
pageJs=["detail"]
pageJsFile={"detail" : "public/script/mobile/mizar/familyactivity/detail"}
pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/familyActivity"]}
bodyClass="bg-grey">

<style>
    .sub-text img{max-width: 100% !important; height: auto !important;}
    .sub-text p{white-space: normal !important;}
    .sub-text .day-path-detail{width: 100% !important;}
</style>

<#include "../function.ftl"/>

<div class="sub-header">
    <#if (activity.banner)?has_content>
        <div class="pic"><img src="${pressImageAutoW((activity.banner),750)!}"></div>
    </#if>

    <div class="describe">
        <div class="text">${(activity.title)!}</div>
    </div>
</div>
<#if (activity.reportDesc)?has_content>
    <div class="sub-recommend" data-bind="click: reportDescBtn.bind($data,'${(activity.url)!}')">
        <div class="info">
            ${(activity.reportDesc)!}
        </div>
    </div>
</#if>

<div class="sub-main">
    <div class="sub-tab" id="activityShowBox">
        <ul>
            <li data-bind="click: subTabBtn.bind($data,$element)" class="active" data-name="activity"><a href="javascript:void(0)">详情介绍</a></li>
            <li data-bind="click: subTabBtn.bind($data,$element)" data-name="expense"><a href="javascript:void(0)">费用说明</a></li>
            <#--<li><a href="javascript:void(0)">用户评价</a></li>-->
        </ul>
    </div>
    <div class="sub-actIntro">
        <div class="sub-text">
            ${(activity.activityDesc)!''}
        </div>
        <#if (activity.address)?has_content>
            <div class="address" data-bind="click: mapBtn">
                <#if (activity.distance)?has_content>
                    <div class="distance">${(activity.distance?string('#.##'))!''}km</div>
                </#if>
                <div class="txt">${(activity.address)!''}</div>
            </div>
        </#if>
    </div>
    <div class="sub-costExplain">
        <div id="kostenindicatieBox" class="sub-title">费用说明</div>
        <div class="sub-text">${(activity.expenseDesc)!''}</div>
    </div>
    <#--<div class="agencyDetails-box">
        <div class="sub-title">用户评价
            <div class="starBg w-right">
                <span class="numRed">4.9</span>
                <a href="javascript:void(0);" class="cliBg"></a>
                <a href="javascript:void(0);" class="cliBg"></a>
                <a href="javascript:void(0);"></a>
                <a href="javascript:void(0);"></a>
                <a href="javascript:void(0);"></a>
            </div>
        </div>
        <div class="aeg-column">
            <dl class="aeg-comment">
                <dt><img src="images/pic01.png" alt=""></dt>
                <dd>
                    <div class="title">李逍遥家长<div class="time">2016年08月17日</div></div>
                    <div class="starBg">
                        <a href="javascript:void(0);" class="cliBg"></a>
                        <a href="javascript:void(0);" class="cliBg"></a>
                        <a href="javascript:void(0);"></a>
                        <a href="javascript:void(0);"></a>
                        <a href="javascript:void(0);"></a>
                        <div class="price">￥1450</div>
                    </div>
                    <div class="pro">
                        <p>邀请您一起观看纯正美语教学 ,英语思维系列公开课太棒了，你也看看吧。邀请您一起观看纯正美</p>
                    </div>
                    <div class="commentImg">
                        <div class="image">
                            <img src="images/pic01.png" alt="">
                        </div>
                        <div class="image">
                            <img src="images/pic01.png" alt="">
                        </div>
                        <div class="image">
                            <img src="images/pic01.png" alt="">
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>-->
    <div class="w-footer">
        <div class="inner fixed">
            <#if paid!false>
                <#if (activity.successUrl)?has_content>
                    <a href="${(activity.successUrl)!'javascript:void(0);'}" class="bespoke-btn">去上课</a>
                </#if>
            <#else>
                <a data-bind="click: applyBtn" href="javascript:void(0);" class="bespoke-btn">立即报名</a>
            </#if>

            <#if (activity.contact)?has_content>
                <a data-bind="click: telBtn" href="tel:${(activity.contact)!}" class="phone"><i class="icon-phone"></i>咨询</a>
            </#if>
        </div>
    </div>
</div>

<script>
    var directlyPay = ${(directlyPay!false)?string};
</script>

</@layout.page>