<#import "../layout.ftl" as winterCamp>
<@winterCamp.page title="2016成长冬令营" pageJs="reservation">
    <@sugar.capsule css=['wintercamp','jbox'] />
    <style>
        html,body{ width: 100%; height: 100%;}
    </style>
    <#function reservationShow shopId>
        <#if shopId == 11>
            <#return "三亚游学冬令营">
        <#elseif shopId == 12>
            <#return "北京科技冬令营">
        <#elseif shopId == 13>
            <#return "长白山亲子冬令营">
        </#if>
    </#function>
<div id="reservation_box" class="wc-wrap wc-bgYellow">
    <div class="wc-signBox">
        <div class="header">
            <div class="left">
                <div class="title">${reservationShow(shopId)}</div>
                <#if shopId == 11>
                    <img src="/public/images/parent/wintercamp/payup-topDetail-sy.jpg">
                <#elseif shopId == 12>
                    <img src="/public/images/parent/wintercamp/payup-topDetail-bj.jpg">
                <#elseif shopId == 13>
                    <img src="/public/images/parent/wintercamp/payup-topDetail-cbs.jpg">
                </#if>
            </div>
            <div class="right">
                <div class="info">
                    <h1>您选择的：${reservationShow(shopId)}</h1>
                    <p>冬令营教育顾问将主动联系您，为您提供专业产品咨询服务</p>
                </div>
            </div>
        </div>
        <div class="main">
            <div class="yx-form">
                <div class="left">
                    <#-- mobile 是明文，不太好。。。。建议改成加*-->
                    <input id="mobile" value="${mobile!}" placeholder="请输入手机号码(必填)" type="tel" maxlength="11"/>
                    <input id="verifyCode" placeholder="填写验证码" type="text"/>
                    <input id="email" placeholder="请填写电子邮箱(发送协议资料用)" type="text"/>
                </div>
                <div class="right">
                    <a id="verifyCodeBtn" href="javascript:void(0)" class="wc-btnGreen disabled">获取验证码</a>
                </div>
            </div>
            <a id="reservationBtn" data-shop_id="${shopId}" data-cid="${cid!}" href="javascript:void(0)" class="wc-btnRed-well">支付1元 ,预约报名</a>
            <p style="text-align: center;">温馨提示：1元预约不可退款</p>
        </div>
    </div>
</div>
</@winterCamp.page>