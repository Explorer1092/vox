<#if (shop.brandDesc)?has_content>
    <div class="aeg-list">
        <ul class="list" style="display: none;"></ul>
        <ul class="list">
            <li>
                <a href="/mizar/branddetail.vpage?brandId=${(shop.brandId)!}" data-logs="{op:'shop_brand_page_click'}">
                    品牌介绍
                    <div class="right" style="padding-top:.08rem;;width: 75%; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${(shop.brandDesc)!'无'}</div>
                </a>
            </li>
        </ul>
    </div>
</#if>

<#if (shop.welcomeGift)?has_content>
    <#assign shopWelcomeGift = (shop.welcomeGift)!'免费预约，了解课程详情'/>
    <div class="aeg-list js-reservationBtn">
        <ul class="list" style="display: none;"></ul>
        <ul class="list">
            <li>
                <a href="javascript:void (0);">
                    预约礼
                    <div class="right" style="padding-top:.08rem;;width: 75%; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${shopWelcomeGift}</div>
                </a>
            </li>
        </ul>
    </div>
</#if>

<#--shop.goodsList-->
<#if (shop.goodsList)?has_content>
    <div class="aeg-main">
        <div class="title">
            <#--<div class="right">更多</div>-->
            课程介绍
        </div>
        <#if (shop.goodsList)?has_content>
            <#list shop.goodsList as item>
            <#--<#if (item_index == 0)!false><#assign shopWelcomeGift = (item.welcomeGift)!'免费预约，了解课程详情'/></#if>-->
            <a href="/mizar/goodsdetail.vpage?goodsId=${item.goodsId!}&shopId=${(shop.shopId)!}" data-logs="{op:'shop_goods_click', s1:'${item.goodsId!}'}">
                <dl style="overflow: hidden;">
                    <dt>
                        <img src="${pressImageAutoW(item.goodsPic!'', 200)}" width="100%">
                    </dt>
                    <dd>
                        <div class="head">${item.goodsName!'Name'}</div>
                        <#if (item.goodsTag)??>
                            <div class="info"><#list item.goodsTag as tag>${tag}&nbsp;</#list></div>
                        </#if>
                        <div class="tip">
                            <#--<div class="right"><span class="label">热门课</span></div>-->
                            <div class="left">${((item.goodsPrice gt 0)!false)?string("￥${(item.goodsPrice)!}", '')}</div>
                        </div>
                    </dd>
                </dl>
            </a>
            </#list>
        <#else>
            <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无数据~</div>
        </#if>
    </div>
</#if>

<#--推广-->
<div id="generalizeBox"></div>

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
                        <a href="javascript:void(0);"><img src="${pressImageAutoW(item.photo!'', 200)}" style="max-width: 100%;"></a><p class="name">${item.name!'--'}</p>
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
</#if>

<#if (shop.ratingMapList)?has_content>
    <div class="aeg-column">
        <div class="titleBar">
            <div class="right"><a href="/mizar/ratinglist.vpage?shopId=${(shop.shopId)!}" data-logs="{op:'shop_comment_more_click'}">更多</a></div>
            <div class="left">
                <p>用户评论<#--（${(shop.ratingCount)!0}）--></p>
                <#--<p class="comment"><span class="label">地面很干净</span><span class="label">价格太贵</span></p>-->
            </div>
        </div>
        <#if (shop.ratingMapList)?has_content>
            <#list shop.ratingMapList as item>
                <dl class="aeg-comment">
                    <dt>
                        <#if (item.avatar)?has_content>
                            <img src="${pressImageAutoW(item.avatar!'', 200)}" width="100%">
                        <#else>
                            <img src="<@app.avatar href=''/>" width="100%">
                        </#if>
                    </dt>
                    <dd>
                        <div class="title">${item.userName!'--'}<div class="right">${item.ratingDate!'--'}</div></div>
                        <div class="right">
                        ${((item.cost gt 0)!false)?string("￥${(item.cost)!}", '')}
                        </div>
                        <div class="starBg">
                            <a href="javascript:void(0);" <#if (item.rating gte 1)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (item.rating gte 2)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (item.rating gte 3)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (item.rating gte 4)!false>class="cliBg"</#if>></a>
                            <a href="javascript:void(0);" <#if (item.rating gte 5)!false>class="cliBg"</#if>></a>
                        </div>
                        <div class="pro pro-show">
                            <p>${item.content!'--'}</p>
                        </div>
                        <#if (item.photos)??>
                        <div class="commentImg">
                            <#list item.photos as pt>
                                <div class="image">
                                    <a href="${pressImage(pt!'')}">
                                        <img src="${pressImageAutoW(pt!'', 200)}" width="100%">
                                    </a>
                                </div>
                            </#list>
                        </div>
                        </#if>
                    </dd>
                </dl>
            </#list>
        </#if>
        <#if (shop.ratingCount gt 5)!false>
            <div class="aeg-list listBg">
                <ul class="list">
                    <li>
                        <a href="/mizar/ratinglist.vpage?shopId=${(shop.shopId)!}" data-logs="{op:'shop_comment_more_click'}">
                            查看全部用户点评
                        </a>
                    </li>
                </ul>
            </div>
        </#if>
    </div>
</#if>