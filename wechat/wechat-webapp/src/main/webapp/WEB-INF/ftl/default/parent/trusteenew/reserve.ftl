<#import "../layout.ftl" as trusteeTwoMain>
<@trusteeTwoMain.page title='预约报名' pageJs="bookregist">
    <@sugar.capsule css=['jbox','trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active03-box">
        <div class="ab03-hd ab03-hd-two" id="imgeFormdiv"></div>
        <div class="ab03-mn">
            <h2>报名截止日期：${endDate!""}</h2>
            <div class="yx-form">
                <div class="left">
                    <input placeholder="请输入手机号码" type="tel" maxlength="11" id="phoneNo" data-bind="value: phoneNo" value=""/>
                    <input placeholder="请输入验证码" type="text" maxlength="6" data-bind="value: validCode"/>
                </div>
                <div class="right">
                    <a href="javascript:void(0)" class="getcode order_icon" id="phoneVerCodeBtn">获取验证码</a>
                </div>
            </div>
            <a href="javascript:void(0)" class="active-bottom-know order_icon" data-bind="click: subBook">支付${reservePrice!0}元 立即预约</a>
            <p class="tips">点击立即预约，即表示您同意<a href="javascript:void(0)" data-bind="click: showLawDetail">《法律声明及隐私政策》</a></p>
        </div>
    </div>
</div>
<script>
    var pNo = '${mobile!""}',cid='${cid!""}';
    ga('trusteeTracker.send', 'pageview');
</script>
</@trusteeTwoMain.page>