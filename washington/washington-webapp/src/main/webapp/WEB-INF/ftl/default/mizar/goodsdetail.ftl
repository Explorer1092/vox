<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="body-backBlue"
title='课程详情'
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/main"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>
<#include "function.ftl"/>
<div class="courseDetails-box">
    <div class="cd-banner" style="background: #f6f6f6; height: 10rem; overflow: hidden;">
        <div class="headerBanner">
            <#if (goods.bannerPhoto)?has_content>
                <ul class="slides" >
                    <#list goods.bannerPhoto as item>
                        <li>
                            <img src="${pressImage(item!'')}" width="100%">
                        </li>
                    </#list>
                </ul>
            </#if>
        </div>

        <div class="cd-head">
            <div class="right">
                ${((goods.originalPrice gt 0)!false)?string("<span class='price'>￥${(goods.originalPrice)!}</span>", '')}
                ${((goods.price gt 0)!false)?string("￥${(goods.price)!}", '')}
            </div>
            ${(goods.goodsName)!'----'}
        </div>
    </div>
    <div class="cd-main">
        <div class="cd-column">
            <div class="module">
                <div class="image"></div>
                <div class="right">
                    <span class="title">类型</span>
                    <span class="type">${(goods.category)!'--'}</span>
                </div>
            </div>
            <div class="module">
                <div class="image"></div>
                <div class="right">
                    <span class="title">年龄</span>
                    <span class="type">${(goods.target)!'--'}</span>
                </div>
            </div>
            <div class="module">
                <div class="image"></div>
                <div class="right">
                    <span class="title">时段</span>
                    <span class="type">${(goods.goodsTime)!'--'}</span>
                </div>
            </div>
            <div class="module">
                <div class="image"></div>
                <div class="right">
                    <span class="title">试听</span>
                    <span class="type">${(goods.audition)!'--'}</span>
                </div>
            </div>
            <div class="module">
                <div class="image"></div>
                <div class="right">
                    <span class="title">课时</span>
                    <span class="type">${(goods.goodsHours)!'--'}</span>
                </div>
            </div>
            <div class="module">
                <div class="image"></div>
                <div class="right">
                    <span class="title">时长</span>
                    <span class="type">${(goods.duration)!'--'}</span>
                </div>
            </div>
        </div>
        <div class="cd-side">
            <div class="cd-title">套餐亮点</div>
            <p class="des">
                ${(goods.title)!''}<br/>
                ${(goods.desc)!''}
            </p>
        </div>
        <#if (goods.welcomeGift)?has_content>
        <div class="cd-arrow js-reservationBtn" style="cursor: pointer;">
            <span class="label" style="line-height: 150%;">预约礼</span>
            <span style="display: inline-block; vertical-align: middle;">${goods.welcomeGift!}</span>
        </div>
        </#if>
    </div>
    <div class="cd-container">
        <div class="titleBar">图文详情</div>
        <div class="cd-content">
            <div class="paragraph" >
                <style>
                    .content-img{ width: 100%; position: relative;}
                    .content-img .mark-logo{ position: absolute; right: 0.1rem; bottom: 0.1rem; background: url(<@app.link href="public/skin/mobile/mizar/images/mark_sign_logo.png"/>) no-repeat; width: 5rem; height: 1.625rem; background-size: 100%; 100%;}
               </style>
                <#if (goods.detail)??>
                    <#list goods.detail as item>
                        <div class="content-img">
                            <img src="${pressImage(item!'')}" width="100%"/>
                            <div class="mark-logo"></div>
                        </div>
                    </#list>
                </#if>
            </div>
        </div>
    </div>
</div>
<#include 'reservation.ftl'/>
</@layout.page>