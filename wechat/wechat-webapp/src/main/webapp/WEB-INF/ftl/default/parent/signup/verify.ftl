<#import "../layout.ftl" as login>
<@login.page title='登录' pageJs="loginVerify">
    <@sugar.capsule css=['jbox'] />
    <style>
        html, body{background-color: #55abff;}
    </style>
    <#function getMaskMobile mobile=''>
        <#if (mobile?length > 7)>
            <#return mobile?substring(0, 3) + '****' + mobile?substring(7)>
        <#else>
            <#return mobile>
        </#if>
    </#function>
    <div class="main body_background">
        <h1 class="logo"></h1>
        <div class="form_main">
            <h2>
                <#if mobile?has_content>
                    ${name!''}的${callName!''}已注册账号，请验证登录！
                <#else>
                    <#if name?has_content>
                        请输入${name!''}的${callName!''}的手机号：
                    </#if>
                </#if>
            </h2>

            <ul class="fm_box">
                <li>
                    <#if mobile?has_content>
                        <input type="text" readonly="readonly" style="background-color: #d1d1d1; width: 100%;" value="${getMaskMobile(mobile)}"/>
                        <input id="mobile" name="mobile" type="hidden" value="${mobile}"/>
                    <#else>
                        <input id="mobile" name="mobile" type="tel" value="" placeholder="请输入您的手机号码" />
                    </#if>
                    <a id="getVerifyCodeBtn" data-cid="" data-requrl="" class="btn_mark btn_orange btn_fly btn_disable" href="javascript:void (0);"><span>获取验证码</span></a>
                </li>
                <li>
                    <input id="code" name="code" type="text" value="" placeholder="请输入收到的短信验证码" />
                </li>
            </ul>

            <div class="submit_box">
                <a id="submitBtn" data-cid="${cid!}" data-source="${source!}" href="javascript:void(0);" class="btn_mark btn_mark_block">
                    <#if mobile?has_content>
                        确定
                    <#else>
                        绑定此孩子
                    </#if>
                </a>
            </div>
        </div>
    </div>
</@login.page>