
<div class="onCourse-main tabbox" style="display: none ">
    <#if (shop.goodsList?size gt 0)!false>
        <#list shop.goodsList as item>
        <#--<#if (item_index == 0)!false><#assign shopWelcomeGift = (item.welcomeGift)!'免费预约，了解课程详情'/></#if>-->
            <#if (item.redirectUrl)?has_content>
            <a href="${item.redirectUrl}" data-logs="{op:'shop_goods_click', s1:'${item.goodsId!}'}">
            <#else>
            <a href="/mizar/goodsdetail.vpage?goodsId=${item.goodsId!}&shopId=${(shop.shopId)!}" data-logs="{op:'shop_goods_click', s1:'${item.goodsId!}'}">
            </#if>
                <div class="onCourse-list">
                   <div class="picInfo"><img src="${pressImageAutoW(item.goodsPic!'', 200)}"></div>
                   <div class="textInfo">
                      <div class="name">${item.goodsName!'Name'}</div>
                      <div class="details">
                         <#if (item.goodsTag)??>
                             <span class="type"><#list item.goodsTag as tag>${tag}&nbsp;</#list></span>
                         <#else><span class="type"></span>
                         </#if>
                         <span class="price">${((item.goodsPrice gt 0)!false)?string("￥${(item.goodsPrice)!}", '')}</span>
                      </div>
                   </div>
                </div>
            </a>
        </#list>
    <#else>
        <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无课程~</div>
    </#if>
        <div class="ah-btn">
            <a href="javascript:void(0);" class="refresh-btn js-refreshBtn" style="margin-top:.5rem; ">查看更多</a>
        </div>

    </div>


