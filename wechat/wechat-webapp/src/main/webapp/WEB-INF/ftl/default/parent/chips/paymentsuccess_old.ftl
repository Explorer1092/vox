<#import "../layout.ftl" as layout>
<@layout.page title="支付成功" pageJs="chipsPaySuccess">
    <@sugar.capsule css=['chipsSuccess'] />

<div class="paymentWrap">
    <div class="paymentHead"></div>
    <div class="paymentMain">
        <p class="paymentTitle">想了解更多吗，做好准备，上课前请关注<span>[薯条英语]</span>公众号,关注后，你将获得：</p>
        <div class="paymentImg"></div>
        <div class="concernBox">
            <p>关注方式：</p>
            <div class="concernWay">
                <div class="wayTitle">
                    <span class="num">1</span>
                    <span class="detail">微信关注公众号【薯条英语】</span>
                </div>
                <div class="wayImg"></div>
            </div>
            <div class="concernWay">
                <div class="wayTitle">
                    <span class="num">2</span>
                    <span class="detail">点击【个人中心】，用购买账号登录即可</span>
                </div>
                <div class="wayImg wayImg02"></div>
            </div>
        </div>
    </div>
</div>


</@layout.page>

