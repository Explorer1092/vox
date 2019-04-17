<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-f4"
title="线上机构"
pageJs=["shopdetailonline", "mizarComment"]
pageJsFile={"shopdetailonline" : "public/script/mobile/mizar/shopdetailonline", "mizarComment" : "public/script/mobile/mizar/mizarComment"}
pageCssFile={"shopdetailonline" : ["public/skin/mobile/mizar/css/skin"]}
>
    <#include "function.ftl"/>
    <#assign isVip = (shop.isVip)!true/> <#--VIP机构-->
    <#assign page_hasVip = true/>
<div class="onlineClass">
    <div class="aeg-top <#if (isVip)!false>dif-details</#if>">
        <dl>
            <dt>
                <a href="/mizar/shoppics.vpage?shopId=${(shop.shopId)!}" data-logs="{op:'shop_album_click'}">
                    <img src="${pressImageAutoW((shop.firstPic)!'', 200)}">
                    <#if (shop.picCount)?has_content><span class="tips">${(shop.picCount)!0}</span></#if>
                </a>
            </dt>
            <dd>
                <div class="head">${(shop.name)!'分店'}</div>
                <div class="starBg">
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 1)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 2)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 3)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 4)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 5)!false>class="cliBg"</#if>></a>
                    <#if (shop.ratingCount)?has_content>
                        <#if shop.ratingCount != 0>
                            <span>${shop.ratingCount}条</span>
                        </#if>
                    </#if>
                </div>
                <div class="tip">
                    <#if (isVip)>
                    <#--${(shop.Vip)!'<i class="auth-icon"></i><span>认证在线机构</span>'}-->
                        <i class="auth-icon"></i><span>认证在线机构</span>
                    </#if>
                </div>
            </dd>
        </dl>
    </div>
    <div class="onCourse-tab">
        <ul>
            <li class="active tabchecks"><a href="javascript:void(0)">首页</a></li>
            <li class="tabchecks"><a href="javascript:void(0)">全部课程</a></li>
            <li class="tabchecks"><a href="javascript:void(0)">家长点评</a></li>
        </ul>
    </div>
</div>
<div>
<#--首页-->
    <style>
        .courseDetails-box .cd-container .cd-content .paragraph img {
            max-width: 100%;
        }
    </style>
    <div class="courseDetails-box agencyDetails-box tabbox">
        <div class="cd-container">
            <div class="titleBar">机构介绍</div>
            <#if (shop.introduction)?has_content>
                <div class="cd-content hideBox"><#--hideBox超出一定高度隐藏，展开更多是移除类hideBox便可显示全部内容-->
                    <div class="paragraph">
                    ${((shop.introduction)?string)!''}

                    </div>
                </div>
            <#else>
                <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无数据~</div>
            </#if>
            <div class="showMore">展开更多</div>
        </div>
        <#if (shop.faculty)?has_content>
            <div class="aeg-container">
                <div class="titleBar">
                    师资和认证
                </div>
                <div class="banner vipBannerBox" style="margin: 0 0.5rem 0 1rem;">
                    <#if (shop.faculty)?has_content>
                        <ul class="bannerImg slides" style="overflow: hidden;">
                            <#list shop.faculty as item>
                                <li style="width: 110px; height: auto;">
                                    <a href="javascript:void(0);"><img src="${pressImageAutoW(item.photo!'', 200)}"
                                                                       style="max-width: 100%;"></a>
                                    <p class="name">${item.name!'--'}</p>
                                    <#if item.experience?has_content><p class="describe red">${item.experience!''}
                                        年教龄</p></#if>
                                    <#if item.description?has_content><p class="describe">${item.description!''}</p></#if>
                                </li>
                            </#list>
                        </ul>
                    <#else>
                        <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无数据~</div>
                    </#if>
                </div>
            </div>
        </#if>
    <#--课程推荐-->
        <#if (shop.recommendGoodsList?size gt 0)!false>
            <div class="titleBar">推荐课程</div>
            <div class="onCourse-main aeg-Main">

                <#list shop.recommendGoodsList as item>
                <#--<#if (item_index == 0)!false><#assign shopWelcomeGift = (item.welcomeGift)!'免费预约，了解课程详情'/></#if>-->
                    <#if (item.redirectUrl)?has_content>
                    <a href="${item.redirectUrl}" data-logs="{op:'shop_goods_click', s1:'${item.goodsId!}'}">
                    <#else>
                    <a href="/mizar/goodsdetail.vpage?goodsId=${item.goodsId!}&shopId=${(shop.shopId)!}"
                       data-logs="{op:'shop_goods_click', s1:'${item.goodsId!}'}">
                    </#if>
                    <div class="onCourse-list">
                        <div class="picInfo"><img src="${pressImageAutoW(item.goodsPic!'', 200)}"></div>
                        <div class="textInfo">
                            <div class="name">${item.goodsName!'Name'}</div>
                            <div class="details">
                                <#if (item.goodsTag?size gt 0)!false>
                                    <span class="type"><#list item.goodsTag as tag>${tag}&nbsp;</#list></span>
                                </#if>
                                <span class="price">${((item.goodsPrice gt 0)!false)?string("￥${(item.goodsPrice)!}", '')}</span>
                            </div>
                        </div>
                    </div>
                </a>
                </#list>
            </div>
        </#if>
    </div>
<#--全部课程-->

    <#include 'curriculum.ftl'/>
<#--点评-->
    <#include 'mizarcomment.ftl'/>
</div>

</@layout.page>