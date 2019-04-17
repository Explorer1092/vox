<#import "../layout.ftl" as layout>
<@layout.page title="我的推荐" pageJs="chipsMyRecommend">
    <@sugar.capsule css=['chipsAll'] />

<div class="recommendWrap">
    <div class="recommendMain">
        <div class="recommendTitle">我的推荐奖励</div>
        <div class="recommendBox">
            <div class="recommendInfo">
                <div class="part">
                    <div class="title">已成功推荐好友</div>
                    <div class="number"><span>${userNO ! 0}</span>位</div>
                </div>
                <div class="part">
                    <div class="title">已获得现金券</div>
                    <div class="number"><span>${totalAward ! 0}</span>元</div>
                </div>
                <div></div>
            </div>
            <div class="recommendTxt">奖励将在好友开课后2天发放</div>
        </div>
        <a href="/chips/center/coupon.vpage"><div class="lookBtn">查看优惠券</div></a>
    </div>
</div>

</@layout.page>

<#--</@chipsIndex.page>-->
