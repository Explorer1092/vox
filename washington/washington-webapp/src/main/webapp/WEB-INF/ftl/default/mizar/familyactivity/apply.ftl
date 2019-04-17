<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='${title!"亲子活动"}'
pageJs=["apply"]
pageJsFile={"apply" : "public/script/mobile/mizar/familyactivity/apply"}
pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/familyActivity"]}
bodyClass="bg-grey">

<div class="purchase-box">
    <div class="pur-list">
        <div class="pur-title">${title!""}</div>
        <dl class="pur-content" data-bind="visible: productTypeDetail" style="display: none;">
            <dt>产品类型</dt>
            <dd>
                <div class="pur-cycle clearfix">
                    <!-- ko foreach : {data : productTypeDetail, as : '_value'} -->
                    <a href="javascript:;" class="box" data-bind="css:{'active': _value.checked},click: $root.productTypeBtn"><div><!--ko text: _value.key--><!--/ko--></div></a>
                    <!--/ko-->
                </div>
            </dd>
        </dl>
        <dl class="pur-content" data-bind="visible: productTypeDetail" style="display: none;">
            <dt>时间</dt>
            <dd>
                <div class="pur-cycle clearfix">
                    <!-- ko foreach : {data : secondList, as : '_se'} -->
                    <a href="javascript:;" class="box" data-bind="css: {'active': _se.checked}, click: $root.secondTypeBtn"><div><!--ko text: _se.itemName--><!--/ko--></div></a>
                    <!--/ko-->
                </div>
            </dd>
        </dl>
        <dl class="pur-content" data-bind="visible: productTypeDetail" style="display: none;">
            <dt>剩余数量： <!--ko text: remains || 0--><!--/ko--></dt>
        </dl>
    </div>
    <div class="w-footer" data-bind="visible: productTypeDetail" style="display: none;">
        <div class="inner fixed clearfix">
            <div class="btnBox">
                <a data-bind="click: submitBtn, visible: remains() != 0" style="display: none"  href="javascript:;" class="w-btn">确定</a>
                <a data-bind="visible: remains() == 0" style="display: none" href="javascript:;" class="w-btn w-btn-disabled">确定</a>
                <div class="money">
                    <span>金额：</span>
                    <span class="redFont"><em>￥</em><!--ko text: getPrice()--><!--/ko--></span>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    var itemMap = ${(json_encode(itemMap))!{}};
</script>
</@layout.page>