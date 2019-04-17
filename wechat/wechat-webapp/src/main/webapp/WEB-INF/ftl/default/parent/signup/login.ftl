<#import "../layout.ftl" as login>
<@login.page title='登录' pageJs="login">
<style>
    html, body{background-color: #55abff;}
</style>

<div class="main body_background">
    <h1 class="logo"></h1>
    <div class="form_main">
        <h2>请输入账号，完成绑定</h2>
        <ul class="fm_box">
            <li>
                <input id="token" name="token" type="tel" value="" placeholder="孩子学号/绑定手机号"/>
            </li>
            <li>
                <input id="pwd" name="pwd" type="password" value="" placeholder="请输入密码"/>
                <input type="hidden" id="woid" value="${woid!}"/>
                <input type="hidden" id="source" value="${source!}"/>
            </li>
        </ul>
        <div class="password_log">
            <span id="messageTip" style="display: none;"></span>
        </div>
        <div class="submit_box">
            <a id="btn_submit" href="javascript:void(0);" class="btn_mark btn_mark_block">登录</a>
        </div>
        <div class="btn_verifyParents js-parentMobileLogin">
            <a id="mobileLoginBtn" href="javascript:void(0);" class="btn_mark" style="display: block;border: 1px solid #fff;background-color: #55abff;">验证家长手机登录</a>
        </div>
        <#if ref?has_content && ref?string == "microCourse"> <#--微课堂-->
            <p style="color: #FFF;line-height: 1.3rem;padding: .3rem;font-size: 25px;margin-top: 25px;">
                提示：为了及时收到开课提醒和适配教材，请使用注册家长通时绑定的手机号登录，再使用微课堂。
            </p>
        </#if>
    </div>
</div>
</@login.page>