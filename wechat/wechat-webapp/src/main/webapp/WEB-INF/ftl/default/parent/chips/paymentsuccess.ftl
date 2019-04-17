<#import "../layout.ftl" as layout>
<@layout.page title="支付成功" pageJs="chipsPaySuccess">
    <@sugar.capsule css=['chipsSuccess'] />

<style>
    .wxNumber_span {
        -webkit-user-select: all !important;
    }
</style>

<div class="payment_success" id="payment_success">
    <div class="header-wrapper">
        <img src="/public/images/parent/chips/payment_success/ic_purchase_succee.png" alt="" />
        <div style="font-size: 0.5rem;">购买成功</div>
        <div class="notify-text-wrapper">
            <div class="notify-title">重要通知</div>
            <div class="notify-text">报名当天必须完成以下 3 步，以免影响上课</div>
        </div>
    </div>
    <div class="step-wrapper">
        <div class="step-title">
            <span class="step-name">第一步:</span>
            <span class="step-content">添加老师微信</span>
        </div>
        <div class="first-step-wrapper">
            <img src="${teacherAvatar ! ''}" alt="" />
            <div class="teacher-name">${teacherName ! '薯条'}老师</div>
            <div class="teacher-said">“Hi，我是薯条英语班主任老师，老师会在开课前1天拉入学习群内，在课程期间老师会在学习群内发布班课内容。快来添加老师微信吧！”</div>
            <div style="font-size: 0.7rem;color: #1D1906;">老师微信号<input id="wxCodeInput" value="${wxCode ! ''}" style="outline:none;border: none;"/></div>
            <div class="copy-wechat-code" id="copyWxCode" data-clipboard-target="#wxCodeInput">复制老师微信号</div>
        </div>
    </div>
    <div class="step-wrapper">
        <div class="step-title">
            <span class="step-name">第二步:</span>
            <span class="step-content">【重要】添加老师实名企业微信</span>
        </div>
        <div class="second-step-wrapper">
            <div style="font-size: 0.75rem;color: #4A4A4A;width: 85%;margin: 1.25rem auto;text-align: justify;line-height: 1.8;">
                “为了外教老师可以给孩子们进行一对一视频点评，视频点评内容需要通过企业微信发送（普通微信有发送量限制），所以，还需要
                <span style="font-size: 0.9rem;color: #151515;background: #FFDE3D;font-weight: bold;">添加老师实名认证的企业微信</span>”
            </div>
            <div class="wechat-wrapper">
                <img class="wechat-qrcode" src="${companyQrCode ! ''}" alt="" />
            </div>
        </div>
    </div>
    <div class="step-wrapper">
        <div class="step-title">
            <span class="step-name">第三步:</span>
            <span class="step-content">微信关注公众号【薯条英语】并登陆个人中心</span>
        </div>
        <div class="third-step-wrapper">
            <img style="width: 80%;display: block;margin: 1rem auto;" src="/public/images/parent/chips/payment_success/subscribe.png" alt="" />
        </div>
    </div>
</div>

</@layout.page>