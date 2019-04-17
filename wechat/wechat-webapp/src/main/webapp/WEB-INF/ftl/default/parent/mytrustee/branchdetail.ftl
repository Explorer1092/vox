<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='机构详情' pageJs="branchdetail">
<@sugar.capsule css=['mytrustee'] />
<#--实际数-->
<#function parseNum array>
    <#if array?? && array?size gt 0>
        <#local sum = 0>
        <#list array as ay>
            <#if ay.src != "">
                <#local sum = sum +1>
            </#if>
        </#list>
        <#return sum>
    </#if>
</#function>
<#--单位换算-->
<#function parseMAndKm number>
    <#if number??>
        <#local sum = "">
        <#if number gte 1000>
            <#local sum = (number/1000) + "千米">
        <#else>
            <#local sum = number + "米">
        </#if>
        <#return sum>
    </#if>
</#function>
<div class="mc-wrap">
    <div class="mc-detailBanner">
        <ul>
            <li>
                <img src="${(detail.images[0].src+"@1e_1c_0o_0l_240h_640w_80q")!""}" class="js-imagesBtn"/>
                <p class="name nameMask">${detail.name!""}<#if distance!=0>(距离${schoolName!"——"}${parseMAndKm(distance)})</#if></p>
            </li>
        </ul>
        <div class="page pageTop"><span>${parseNum(detail.images)}张</span></div>
    </div>
    <div class="mc-trusteeDetail">
        <div class="info mcList-box">
            <#if viewCount gt 20>
                <div class="title">基本信息<span class="fl">已有${viewCount}人浏览</span></div>
            </#if>
            <div class="inner">
                <p class="address"><i class="address-icon"></i>${detail.address!""}</p>
                <p class="tel"><a href="tel:${detail.tel!""}"><i class="tel-icon"></i>${detail.tel!""}</a></p>
                <div class="evaluation slideUp"><!--去除或添加slideUp-->
                    <div class="label">
                        <#if detail.tags?? && detail.tags?size gt 0>
                            <#list detail.tags as tag>
                                <span>${tag!""}</span>
                            </#list>
                        </#if>
                    </div>
                    <span class="slide-icon js-moreTagBtn"></span>
                </div>
            </div>
        </div>
        <div class="mtd-intro">
            <div class="header">
                <div style="height: 84px;">
                    <ul class="js-descTab">
                        <li class="active" data-type="groupDesc">机构介绍</li>
                        <li data-type="courseDesc">服务介绍</li>
                    </ul>
                </div>
            </div>
            <div class="main">
                <!--机构介绍-->
                <div class="main-trustee">
                    <div class="trusteeIntro">
                        <h3>托管班简介</h3>
                        <p class="describe">${detail.desc!""}</p>
                        <#if (detail.images)?? && (detail.images)?size gt 0>
                            <#list detail.images as image>
                                <#if image.src != ''>
                                    <div class="ti-section">
                                        <div class="fc-intro">
                                            <img class="js-imgItem" src="${image.src+'@1e_1c_0o_0l_390h_520w_80q'}">
                                            <p class="nameMask">${image.tag!''}</p>
                                        </div>
                                        <p class="describe">${image.desc!''}</p>
                                    </div>
                                </#if>
                            </#list>
                        </#if>
                    </div>
                    <a href="javascript:void(0);" class="footerBtn js-knowClassBtn">去了解课程</a>
                </div>
                <!--服务介绍-->
                <div class="main-major" style="display: none;">
                    <div class="majorIntro">
                        <#if goods?? && goods?size gt 0>
                            <#list goods as good>
                            <div class="mi-section slideUp">
                                <h3><span>${good.name!""}(${good.periodName!"——"})</span><span class="slide-icon js-contentMoreBtn"></span></h3>
                                <p class="mi-intro">
                                    <#if (good.tags)?? && (good.tags)?size gt 0>
                                        <#list good.tags as tag>
                                            <#if tag!="">
                                                <span>${tag}</span>
                                            </#if>
                                        </#list>
                                    </#if>
                                </p>
                                <div class="mi-main">${good.desc!""}</div>
                                <div class="mi-footer">
                                    <div class="left">￥${good.price!0}元<del><#if good.price lt good.orignalPrice>（￥${good.orignalPrice!0}元）</#if></del></div>
                                    <a href="javascript:void(0);" class="right js-buyServiceItem <#if !good.isOrderable>disabled</#if>" data-gid="${good.id}"><#if good.type == "experience">预约体验<#else>购买服务</#if></a>
                                </div>
                            </div>
                            </#list>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#--<script>-->
<#--<#if config_signature?has_content>-->
<#--var wechatConfig = {};-->
<#--wechatConfig.signature = "${config_signature}";-->
<#--wechatConfig.appid = "${appid}";-->
<#--wechatConfig.timestamp = "${config_timestamp}";-->
<#--wechatConfig.noncestr = "${config_nonceStr}";-->
<#--</#if>-->
<#--</script>-->
</@trusteeMain.page>