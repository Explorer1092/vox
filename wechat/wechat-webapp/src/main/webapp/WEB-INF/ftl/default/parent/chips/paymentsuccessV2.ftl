<#import "../layout.ftl" as layout>
<@layout.page title="支付成功" pageJs="chipsPaySuccess">
    <@sugar.capsule css=['chipsSuccess'] />

<style>
    .wxNumber_span {
        -webkit-user-select: all !important;
    }
</style>

<div class="chipsPayWrap">
    <div class="stateBox">
        <div class="stateTitle">购买成功<i></i></div>
        <div class="stateTips"><span>【重要通知】</span>报名当天必须完成以下两步，否则影响上课！</div>
    </div>
    <!-- 添加老师微信 -->
    <div class="teacherMain">
        <div class="addTeacher">1.添加老师微信</div>
        <div class="method">
            <span class="way">方法一：</span>
            <span>保存二维码，在微信中识别二维码添加老师，老师会在2天内通过</span>
        </div>
        <div class="codeBox">
            <#if qrCode?? && qrCode != '' >
                <img class="codeImg" src="${qrCode!''}" alt=""/>
            <#else>
                <img class="codeImg" src="/public/images/parent/chips/teacher.jpg" alt=""/>
            </#if>

        </div>
        <div class="method">
            <span class="way">方法二：</span>
            复制老师微信号，在微信中添加老师
        </div>
        <div class="wxBox">
            <div class="wxNumber">微信号：
                <span class="wxNumber_span">
                    <#if wxCode?? && wxCode != ''>
                        ${wxCode}
                    <#else>
                        shutiao004
                    </#if>
            </span></div>
        </div>
    </div>
    <!-- 微信关注公众号【薯条英语】 -->
    <div class="paymentMain">
        <div class="concernBox">
            <div class="addTeacher">2.微信关注公众号【薯条英语】并登录个人中心</div>
            <p>关注方式：</p>
            <div class="concernWay">
                <div class="wayTitle">
                    <span class="num">1</span>
                    <span class="detail">微信关注公众号【薯条英语】</span>
                </div>
                <div class="wayImg"></div>
            </div>
            <div class="concernWay concernWay02">
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

