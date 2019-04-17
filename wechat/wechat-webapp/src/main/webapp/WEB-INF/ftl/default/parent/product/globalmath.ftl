<#import "../layout.ftl" as product>
<@product.page title='世界趣味数学挑战赛' pageJs="globalmath">
<@sugar.capsule css=['product','jbox'] />
<form action="/parent/product/order.vpage" method="post">
    <div class="pro-detail">
        <div class="pd-main">
            <div class="banner">
                <img src="/public/images/parent/activity/globalmath/globalmathpaybanner24.png"/>
            </div>
            <div class="intro pdm">
                <h4>产品介绍：</h4>
                <p>
                    <span id='afenti_clazz_tip'></span>五种思维方式，培养思维能力！与全世界最强大脑同台竞技！
                </p>
            </div>
            <div class="child pdm">
                <h4>选择孩子：</h4>
                <ul id="child_list_box" data-selected_student="${sid!}">
                    <#if infos?? && infos?size gt 0>
                        <#list infos as info>
                            <#if info.products?? && info.products?size gt 0>
                                <li data-student_id="${info.uid!0}" data-products='${json_encode(info.products)}' data-buyIds='${json_encode(info.buyIds)}'>
                                    <img src="<@app.avatar href="${info.img!}"/>" /><i></i>
                                    <p class="name">${info.name!''}</p>
                                </li>
                            </#if>
                        </#list>
                    </#if>
                </ul>
            </div>
            <div class="pdm" style="border: none; padding: 30px 25px 300px 25px;">
                <h4>选择难度：</h4>

                <ul id="cycle_list_box" class="skuItem">
                    <li data-pid="122"><table cellpadding="0" cellspacing="0"><tr><td class="wtxt">初露头角篇 二段</td></tr></table></li>
                    <li data-pid="132"><table cellpadding="0" cellspacing="0"><tr><td class="wtxt">技压群芳篇 二段</td></tr></table></li>
                    <li data-pid="142"><table cellpadding="0" cellspacing="0"><tr><td class="wtxt">独步天下篇 二段</td></tr></table></li>
                </ul>

            </div>

        </div>
        <div style="height: 100px;width: 100%;"></div>
        <div class="pro-footer">
            <div class="pf-l">需支付金额：<span class="price_box">0元</span></div>
            <div id="buy_but" class="pf-r" style="cursor: pointer;"><a href="javascript:void(0);">确认并支付</a></div>
            <input type="hidden" value="" name="sid" id="array-student"/>
            <input type="hidden" value="" name="productId" id="array-product"/>
        </div>
    </div>
</form>
<script>
var mathType = "${mathType}";
</script>
</@product.page>