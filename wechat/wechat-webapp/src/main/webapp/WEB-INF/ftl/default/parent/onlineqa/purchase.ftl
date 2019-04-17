<#import "../layout.ftl" as purchase>
<@purchase.page title="购买" pageJs="onlineqaPurchase">
    <@sugar.capsule css=['onlineqa'] />
    <div class="buy-interface-wrap m-wrap">
        <div class="buy-interface-main">
            <div class="select-text">
                <h2>在线获得老师真人解答</h2>
                <p>[该功能由第三方公司提供并收费，请自愿使用，</p>
                <p>一起作业的自有功能与服务免费]</p>
            </div>

            <div class="select-type buy-interface-info">
                <h4>选择类型</h4>
                <p>
                    <span class="select-type select-times selected-btn">包次数</span><span class="select-type select-period">包时间</span>
                </p>
                <div style="clear: both;"></div>
            </div>


            <div class="select-period buy-interface-info" style="display: none">
                <h4>选择周期</h4>
                <p  <#if productType!="AiFuDao">class="select-period-s"</#if>>
                    <#list products.period as product>
                        <span class="select-product" data-productid="${product.productId}" data-price="${product.price}" >
                            ${ product.name }
                            <#if productType=="AiFuDao">
                                <#if product.name=="包周">
                                    <i style="font-style: normal; display: block; font-size: 14px; color: #6d6d6d;">每月最多12次</i>
                                <#elseif product.name=="包月">
                                    <i style="font-style: normal; display: block; font-size: 14px; color: #6d6d6d;">每月最多45次</i>
                                </#if>
                            </#if>
                        </span>
                    </#list>
                </>
                <div style="clear: both;"></div>
            </div>

            <div class="select-times buy-interface-info">
                <h4>选择次数</h4>
                <p>
                    <#list products.times as product>
                        <span class="select-product" data-productid="${ product.productId }" data-price="${ product.price }">${ product.name }</span>
                    </#list>
                </p>
                <div style="clear: both;"></div>
            </div>

        </div>
        <div class="select-price">
            <span>价格：￥<span id="selected-product-price"></span></span>
            <input type="hidden" id="selected-product-id">
            <a href="javascript:void(0)" class="buy-btn buy-text buy-pos" id="order">立即购买</a>
        </div>
    </div>
    <script>
        var productType=${json_encode(productType)};
    </script>
</@purchase.page>