<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
pageJs=['init']
pageJsFile={"init" : "public/script/paymentmobile/finance"}
pageCssFile={"init" : ["public/skin/paymentmobile/css/finance"]}
>
<div class="header-title">
    <div class="p-inner">
        <a href="javascript:void(0);" class="back" data-bind="click: return_previous"></a>
        <div>订单支付</div>
    </div>
</div>
<div class="finance-box">
    <#if isBindParent?? && isBindParent>
        <div class="finance-top">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance1.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance2.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance3.jpg"/>">
        </div>
        <div class="finance-ba">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance4-v2.jpg"/>" class="banner-title">
            <div class="finance-banner">
                <div class="liveL-banner" id="financeContentBox">
                    <ul class="slides">
                        <li>
                            <img src="<@app.link href="public/skin/paymentmobile/images/finance-img02.jpg"/>" alt="">
                            <p>2. 制定计划：开通了学习产品，就要坚持练习。这样成绩才会提高哦。</p>
                        </li>
                        <li>
                            <img src="<@app.link href="public/skin/paymentmobile/images/finance-img03.jpg"/>" alt="">
                            <p>3. 合理消费：需要什么买什么，不乱花钱，做勤俭节约的好孩子！</p>
                        </li>
                        <li>
                            <img src="<@app.link href="public/skin/paymentmobile/images/finance-img01.jpg"/>" alt="">
                            <p>1. 正确沟通：有礼貌的和爸爸妈妈讲清楚为什么要买，用了会有什么好处。不要哭闹哦。</p>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    <#else>
        <!--未绑定家长-->
        <div class="finance-top">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance1.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance5.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance6.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance7.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance8.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance9.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance10.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance11.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance12.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance13.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance14.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance15.jpg"/>">
            <img src="<@app.link href="public/skin/paymentmobile/images/finance16.jpg"/>">
        </div>
    </#if>
    <div class="finance-code">
        <img src="<@app.link href="public/skin/paymentmobile/images/parent-code.png"/>">
    </div>
    <p class="finance-tip">请扫描二维码下载家长通<br>（该功能需家长通升级到2.5.0版本才能使用）</p>
    <div class="finance-download" data-bind="click: download_parent"></div>
    <p class="finance-tip2">孩子购买的产品需前往家长通代付，同时可查看孩子作业报告</p>
</div>
<script type="text/javascript">
    var isBindParent = "${(isBindParent!false)?string}" == 'true' ? true : false;
    var orderId = "${(orderId)!0}";
</script>
</@layout.page>