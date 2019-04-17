<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-f4"
title=(shop.name)!"机构详情"
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/main"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>
    <#include "function.ftl"/>
<#--vip还未开放-->
<#--<#assign page_hasVip = (!shop.isVip)!true/>-->
<#assign isVip = (shop.isVip)!true/> <#--VIP机构-->
    <#assign page_hasVip = true/>
<div class="agencyDetails-box">
    <div class="aeg-top <#if (isVip)!false>dif-details</#if>" style="padding: 0;">
        <dl>
        <#if !(isVip)!false> <#--非VIP-->
            <dt>
                <a href="/mizar/shoppics.vpage?shopId=${(shop.shopId)!}" data-logs="{op:'shop_album_click'}">
                    <img src="${pressImageAutoW((shop.firstPic)!'', 200)}">
                    <#if (shop.picCount)?has_content><span class="tips">${(shop.picCount)!0}</span></#if>
                </a>
            </dt>
        </#if>
            <dd>
                <div class="head">${(shop.name)!'分店'}</div>
            <#--<div class="right">￥0</div>-->
                <div class="starBg">
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 1)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 2)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 3)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 4)!false>class="cliBg"</#if>></a>
                    <a href="javascript:void(0);" <#if (shop.ratingStar gte 5)!false>class="cliBg"</#if>></a>
                    <span>${(shop.ratingCount)!0}条</span>
                </div>
                <div class="tip">${(shop.area)!'商圈'}
                    <#if (shop.secondCategory)??><#list shop.secondCategory as tag><span>${tag}&nbsp;</span></#list></#if>
                </div>
            </dd>
        </dl>
        <#if (isVip)!false> <#--VIP头部优化-->
            <div class="aeg-picShow vipHeaderBanner">
                <ul class="slides" style="/*overflow: hidden;*/">
                    <#if (shop.photos)?has_content>
                        <#assign pic = ""/>
                        <#list shop.photos as sp>
                            <#if sp_index lte 6>
                                <li class="pic">
                                    <a href="/mizar/shoppics.vpage?shopId=${(shop.shopId)!}" data-logs="{op:'shop_album_click'}">
                                    <img src="${pressImageAutoW((sp)!'', 200)}" alt="">
                                    <#if sp_index == 6> <#--第7张展示更多-->
                                        <div class="pic-mask">
                                            <i class="camera"></i>
                                            <p>查看更多</p>
                                        </div>
                                    </#if>
                                    </a>
                                </li>
                            </#if>
                        </#list>
                    </#if>
                </ul>
            </div>
        </#if>
    </div>
    <div class="aeg-list">
        <ul class="list">
            <li><a href="/mizar/shopmap.vpage?shopId=${(shop.shopId)!0}&type=map" data-logs="{op:'shop_addr_map_click'}">${(shop.address)!'详情地址'}</a></li>
            <#if (shop.phone)?has_content>
                <li>
                <span class="tel">
                    <#list shop.phone as p>
                        <a href="tel:${(p)!'+86-010-00000000'}" data-logs="{op:'shop_tel_dialed'}">${(p)!'+86-010-00000000'} </a>&nbsp;&nbsp;
                    </#list>
                </span>
                </li>
            </#if>
            <#--<#if (shop.isVip)!false>
            &lt;#&ndash;vip&ndash;&gt;
            <#else>
                <li><a href="javascript:void(0);" id="merchantShowBtn">商家入驻，补全更多师资课程信息</a></li>
            </#if>-->
            <#if shop.sameReserveCount?has_content && shop.sameReserveCount gt 0>
                <li class="listCome"><a href="/mizar/samereserve.vpage?shopId=${(shop.shopId)!}">有${shop.sameReserveCount!0}位同学来过</a></li>
            </#if>
        </ul>
    </div>
    <#if (page_hasVip)!false>
        <#include 'hasvip.ftl'/>
    <#else>
        <#include 'notvip.ftl'/>
    </#if>
</div>
    <#include 'reservation.ftl'/>
</@layout.page>