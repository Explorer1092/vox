    <#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='品牌介绍'
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/main"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
bodyClass="bg-f4"
>
<#include "function.ftl"/>
<div class="brandIntroduce-box">
    <div class="bi-top">
        <div class="bi-banner">
            <div style="height: 100%; overflow: hidden;">
                <#if (brand.brandPhoto)??>
                    <ul>
                        <#list brand.brandPhoto as pic>
                            <#if pic_index == 0>
                                <li>
                                    <img src="${pressImage(pic!'')}" style=""/>
                                </li>
                            </#if>
                        </#list>
                    </ul>
                </#if>
            </div>
            <div class="bi-tip"><img src="${pressImageAutoW((brand.brandLogo)!'', 200)}"></div>
        </div>
        <div class="bi-introduce">
            <div>${(brand.introduction)!}</div>
        </div>
    </div>
    <div id="RecentBusinesses"></div>
    <div class="bi-column">
        <ul>
            <#if (brand.establishment)?has_content>
                <li><span class="title">创立时间</span><span class="info">${(brand.establishment)!'0000-00-00'}</span></li>
            </#if>
            <li><span class="title">品牌规模</span><span class="info">${(brand.shopScale)!'---'}</span></li>
        </ul>
    </div>
    <div class="bi-main">
        <div class="titleBar">师资力量</div>
        <div class="bi-content"></div>

        <div class="banner vipBannerBox" style="margin: 0 0.5rem 0 1rem;">
            <#if (brand.faculty)?has_content>
                <ul class="bannerImg slides" >
                    <#list brand.faculty as item>
                        <li style="padding: 0 10px;">
                            <img src="${pressImageAutoW((item.photo)!'', 200)}" width="100%" style="border-radius: 18px;">
                            <p class="name">${item.name!'--'}</p>
                            <#if item.experience?has_content><p class="describe red">${item.experience!''}年教龄</p></#if>
                            <#if item.description?has_content><p class="describe">${item.description!''}</p></#if>
                        </li>
                    </#list>
                </ul>
            <#else>
                <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无数据~</div>
            </#if>
        </div>
    </div>
    <div class="bi-main">
        <div class="titleBar">师资证书</div>
        <div class="bi-content">
            <p class="pro">${(brand.certificationName)!''}</p>
        </div>

        <div class="banner vipBannerBox" style="margin: 0 0.5rem 0 1rem;">
            <#if (brand.certificationPhotos)?has_content>
                <ul class="bannerImg slides" >
                    <#list brand.certificationPhotos as item>
                        <li style="padding: 0 10px;">
                            <img src="${pressImageAutoW(item!'', 200)}" width="100%">
                        </li>
                    </#list>
                </ul>
            <#else>
                <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无数据~</div>
            </#if>
        </div>

    </div>
</div>

<div id="FooterReservation"></div>
<script type="text/html" id="T:最近商户">
    <div class="bi-side">
        <div class="titleBar">最近商户</div>
        <div class="agencyDetails-box">
            <div class="aeg-top bi-module JS-GoToPage" data-url="/mizar/shopdetail.vpage?shopId=<%=shopMap.shopId%>" style="cursor: pointer;">
                <dl>
                    <dd class="mod-content">
                        <div class="head"><%=(shopMap.shopName)%></div>
                        <div class="starBg" style="cursor: default;">
                            <a href="javascript:void(0);" class="<%=(shopMap.ratingStar >= 1 ? 'cliBg': '')%>"></a>
                            <a href="javascript:void(0);" class="<%=(shopMap.ratingStar >= 2 ? 'cliBg': '')%>"></a>
                            <a href="javascript:void(0);" class="<%=(shopMap.ratingStar >= 3 ? 'cliBg': '')%>"></a>
                            <a href="javascript:void(0);" class="<%=(shopMap.ratingStar >= 4 ? 'cliBg': '')%>"></a>
                            <a href="javascript:void(0);" class="<%=(shopMap.ratingStar >= 5 ? 'cliBg': '')%>"></a>
                            <%if(shopMap.ratingCount && shopMap.ratingCount > 0){%><span class="num"><%=(shopMap.ratingCount)%>条</span><%}%>
                            <%
                            var fDistance = shopMap.distance.toFixed(2) + 'km';
                            if(shopMap.distance < 1 && shopMap.distance > 0){
                            fDistance = (shopMap.distance * 1000).toFixed(0) + 'm';
                            }
                            %>
                            <span class="distance"><%=fDistance%></span>
                        </div>
                    </dd>
                </dl>
                <#--<a href="javascript:void(0);" class="icon-arrowRight">11人已预约</a>-->
            </div>
            <div class="aeg-list">
                <ul class="list">
                    <li>
                        <a href="/mizar/shopmap.vpage?shopId=<%=shopMap.shopId%>&type=map" style="padding: .5rem .5rem .5rem 1rem;"><%=(shopMap.address)%></a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:预约试听Foot">
    <div class="footer">
        <div class="inner">
            <a href="/mizar/reserveselect.vpage?shopId=<%=(shopMap.shopId)%>" class="bespoke-btn" style="width: 95%;">预约试听</a>
        </div>
    </div>
</script>

<script type="text/javascript">
    var initMode = "BrandDetail";
    var brandId = "${(brand.id)!0}";
</script>
</@layout.page>