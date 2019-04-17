<#import "../layout.ftl" as product>
<@product.page title='趣味数学' pageJs="stem">
<@sugar.capsule css=['product','jbox'] />
<form action="/parent/product/order.vpage" method="post">
    <div class="pro-detail">
        <div class="pd-main">
            <div class="banner">
                <img src="/public/images/parent/product/stem101banner.png"/>
            </div>
            <div class="intro pdm">
                <h4>产品介绍：</h4>
                <p>
                    <span id='afenti_clazz_tip'></span>不止是计算，更是逻辑思维训练与创新能力锻炼的完美融合。只需动动手指，就能引起一场头脑风暴！
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
            <div class="period pdm" style="border: none; padding: 30px 25px 300px 25px;">
                <h4>选择难度：</h4>

                <ul id="cycle_list_box">
                    <li class="stemItem" data-index="12"><p class="stemname">初露头角篇</p></li>
                    <li class="stemItem" data-index="13"><p class="stemname">技压群芳篇</p></li>
                    <li class="stemItem" data-index="14"><p class="stemname">独步天下篇</p></li>
                </ul>
                <ul class="column js-levelList">
                    <#--<li class="sub"></li>-->
                </ul>
            </div>

        </div>
        <div class="pro-footer">
            <div class="pf-l">需支付金额：<span class="price_box">0元</span></div>
            <div id="buy_but" class="pf-r" style="cursor: pointer;"><a href="javascript:void(0);">确认并支付</a></div>
            <input type="hidden" value="" name="sid" id="array-student"/>
            <input type="hidden" value="" name="productId" id="array-product"/>
        </div>
    </div>
</form>
</@product.page>