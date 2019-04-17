<#import "../layout.ftl" as trusteeTwoMain>
<@trusteeTwoMain.page title='公开课报名' pageJs="ocreserve">
<@sugar.capsule css=['jbox','openclass'] />
<div class="train-wrap bg-blue">
    <div class="train03-box">
        <div class="header"></div>
        <div class="main">
            <h2>请确认联系人手机号</h2>
            <#--（以家长ID尾数奇偶进行呈现，奇数维持原SOP，偶数为去除验证码的SOP）-->
            <#assign isOdd = (((currentUserId)!0)%2 != 0) />
            <div class="yx-form">
                <#if isOdd>
                    <div class="left">
                        <input placeholder="请输入手机号码(必填)" type="tel" maxlength="11" id="phoneNo" data-bind="value: phoneNo" value=""/>
                        <input placeholder="填写验证码" type="text" maxlength="6" data-bind="value: validCode"/>
                    </div>
                    <div class="right">
                        <a href="javascript:void(0)" class="btn-green" id="phoneVerCodeBtn" style="font-size: 20px;">获取验证码</a>
                    </div>
                <#else>
                    <div class="left">
                        <input placeholder="请输入手机号码(必填)" type="tel" maxlength="11" id="phoneNo" data-bind="value: phoneNo" value=""/>
                    </div>
                </#if>
            </div>
            <a href="javascript:void(0)" class="btn-red-well" data-bind="click: subBook">支付${reservePrice!1}元 立即报名</a>
            <p class="tips">点击立即预约，即表示您同意<a href="legalnotice.vpage">《法律声明及隐私政策》</a></p>
        </div>
    </div>
</div>
<script>
    var pNo = '${mobile!""}',cid='${cid!""}',pid = '${currentUserId!0}';
    ga('trusteeTracker.send', 'pageview');
</script>
</@trusteeTwoMain.page>