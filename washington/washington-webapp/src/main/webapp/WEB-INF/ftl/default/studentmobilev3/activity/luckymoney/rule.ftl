<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='兑换预览'
pageCssFile={"luckymoney" : ["public/skin/mobile/student/app/activity/luckymoney/css/skin"]}
>
<div class="luckyMoney-box">
    <img src="<@app.link href="public/skin/mobile/student/app/activity/luckymoney/images/lucky_bg01.jpg" />" alt="">
    <img src="<@app.link href="public/skin/mobile/student/app/activity/luckymoney/images/lucky_bg02.jpg" />" alt="">
    <div class="lmy-main">
        <img src="<@app.link href="public/skin/mobile/student/app/activity/luckymoney/images/lucky_bg03.jpg" />" alt="">
        <div class="lInfo">
            <p class="lTitle pre">活动规则</p>
            <div><p class="num">1.</p><p class="pre">活动期间，续费/开通任意产品就能给小朋友发压岁钱，压岁钱当即存入学生端压岁钱账户。</p></div>
            <div><p class="num">2.</p><p class="pre">压岁钱可用于活动时间段抽奖，兑换头像框和产品优惠券，兑换成功后不可退。</p></div>
            <div><p class="num">3.</p><p class="pre">压岁钱活动时间为2017.01.22-2017.02.12，压岁钱自学积分将在2017.3.1后可开放兑换。</p></div>
        </div>
    </div>
</div>
</@layout.page>