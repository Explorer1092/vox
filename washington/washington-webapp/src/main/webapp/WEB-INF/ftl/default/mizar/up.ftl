<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='开学专题'
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/up"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/listskin"]}
>
<div class="agencyHome-box">
    <div class="ah-header">

        <div class="ah-top" id="headerBannerDefault" style="background: none;">
            <img src="<@app.link href="public/skin/mobile/mizar/images/startSchoolBanner.jpg"/>" width="100%"/>
        </div>

        <div class="age-tab">
            <ul>
                <li data-type="englishSubjectList"><a href="javascript:void(0);">外语</a></li>
                <li class="active"><a href="javascript:void(0);">才艺</a></li>
                <li><a href="javascript:void(0);">玩乐</a></li>
            </ul>
        </div>
    </div>
    <div class="ah-main">
        <div class="ad-top" style="display: none;">
            <div class="titleBar">外语</div>
            <#if (englishList?size gt 0)!false>
                <#list englishList as shop>
                <dl class="js-shopItem" data-sid="${(shop.id)!}" data-index="0" style="cursor: pointer;">
                    <dt>
                        <a href="/mizar/shoppics.vpage?shopId=${(shop.id)!}">
                            <img src="${(shop.photo)!}" alt="">
                        </a>
                    </dt>
                    <dd>
                        <a href="/mizar/shopdetail.vpage?shopId=${(shop.id)!}" ><div class="head">${(shop.name)!''}</div></a>
                        <div class="starBg">
                            <a href="javascript:void(0);" <#if (shop.ratingStar gte 1)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (shop.ratingStar gte 2)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (shop.ratingStar gte 3)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (shop.ratingStar gte 4)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (shop.ratingStar gte 5)!false>class="cliBg"</#if>></a>
                            <span>${(shop.ratingCount)!0}条</span>
                            <#--<div class="price">5.6km</div>-->
                        </div>
                        <div class="ordered-btn">
                            <a href="/mizar/shopdetail.vpage?shopId=${(shop.id)!}" class="w-orderedBtn">点击预约</a>
                        </div>
                        <div class="tip">
                            ${(shop.tradeArea)!}
                        </div>
                    </dd>
                </dl>
                </#list>
            <#else>
                <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">暂无外语机构</div>
            </#if>
        </div>
        <div class="ad-top" style="display: block;">
            <div class="titleBar">
                <#--<div class="right">更多</div>-->
                才艺
            </div>
            <#if (cyList?size gt 0)!false>
                <#list cyList as shop>
                    <dl class="js-shopItem" data-sid="${(shop.id)!}" data-index="1" style="cursor: pointer;">
                        <dt>
                            <a href="/mizar/shoppics.vpage?shopId=${(shop.id)!}">
                                <img src="${(shop.photo)!}" alt="">
                            </a>
                        </dt>
                        <dd>
                            <a href="/mizar/shopdetail.vpage?shopId=${(shop.id)!}" ><div class="head">${(shop.name)!''}</div></a>
                            <div class="starBg">
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 1)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 2)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 3)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 4)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 5)!false>class="cliBg"</#if>></a>
                                <span>${(shop.ratingCount)!0}条</span>
                            <#--<div class="price">5.6km</div>-->
                            </div>
                            <div class="ordered-btn">
                                <a href="/mizar/shopdetail.vpage?shopId=${(shop.id)!}" class="w-orderedBtn">点击预约</a>
                            </div>
                            <div class="tip">
                            ${(shop.tradeArea)!}
                            </div>
                        </dd>
                    </dl>
                </#list>
            <#else>
                <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">暂无才艺机构</div>
            </#if>
        </div>
        <div class="ad-top" style="display: none;">
            <div class="titleBar">
                <#--<div class="right">更多</div>-->
                玩乐
            </div>
            <#if (wlList?size gt 0)!false>
                <#list wlList as shop>
                    <dl class="js-shopItem" data-sid="${(shop.id)!}" data-index="2" style="cursor: pointer;">
                        <dt>
                            <a href="/mizar/shoppics.vpage?shopId=${(shop.id)!}">
                                <img src="${(shop.photo)!}" alt="">
                            </a>
                        </dt>
                        <dd>
                            <a href="/mizar/shopdetail.vpage?shopId=${(shop.id)!}" ><div class="head">${(shop.name)!''}</div></a>
                            <div class="starBg">
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 1)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 2)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 3)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 4)!false>class="cliBg"</#if>></a>
                                <a href="javascript:void(0);" <#if (shop.ratingStar gte 5)!false>class="cliBg"</#if>></a>
                                <span>${(shop.ratingCount)!0}条</span>
                            <#--<div class="price">5.6km</div>-->
                            </div>
                            <div class="ordered-btn">
                                <a href="/mizar/shopdetail.vpage?shopId=${(shop.id)!}" class="w-orderedBtn">点击预约</a>
                            </div>
                            <div class="tip">
                            ${(shop.tradeArea)!}
                            </div>
                        </dd>
                    </dl>
                </#list>
            <#else>
                <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">暂无玩乐机构</div>
            </#if>
        </div>
    </div>
</div>
</@layout.page>