<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='${title!"亲子活动"}'
pageJs=["pay"]
pageJsFile={"pay" : "public/script/mobile/mizar/familyactivity/pay"}
pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/familyActivity"]}
bodyClass="bg-grey">
<div class="payment-box">
    <div class="pay-content">
        <div class="title">订单金额：<span class="price">￥${price!0}</span></div>
        <div class="info">${title!''}</div>
    </div>
    <div class="pay-list" id="payWayBox">
        <div class="pay-info">支付方式</div>
        <a href="javascript:void(0);" class="alipay active" data-way="zfb"><!--active控制是否支付-->
            <div>支付宝支付</div>
            <div class="side">推荐有支付宝账号的用户使用</div>
        </a>
        <a href="javascript:void(0);" class="wechat" data-way="wx">
            <div>微信支付</div>
            <div class="side">推荐安装微信5.0及以上版本的用户使用</div>
        </a>
    </div>
    <div class="w-footer">
        <div class="inner fixed">
            <div class="btnBox">
                <a id="submitBtn" href="javascript:void(0);" class="w-btn w-btnBig">确认支付</a>
            </div>
        </div>
    </div>
</div>
<script>
    var payMapper = {
        actId: '${actId!0}',
        dp: ${(dp!false)?string},
        itemId: '${itemId!0}',
        price: '${price!0}'
    };
</script>
</@layout.page>