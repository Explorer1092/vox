<script type="text/html" id="T:LoginTemplateMain">
    <#--登录-->
    <div class="loginPop-box loginBg">
        <div class="loginPop-close JS-clear-btn"></div>
        <div class="lop-inner">
            <div class="loginPop-logo"></div>
            <h1>登录</h1>
            <#if (userinfo??)>
                <div class="lop-content">
                    <div class="c-text" style="font-size: 16px;">
                        ${userinfo!""?html} 您成功的登录了
                    </div>
                </div>
                <div class="loginPop-footer">
                    <a href="/index.vpage" class="login-btn">进入首页</a>
                    <div class="qq-btn">
                        <a href="/ucenter/logout.vpage?_=1" class="info-btn">退出</a>
                    </div>
                </div>
            <#else>
                <form id="FormSubmitInit" class="JS-formSubmit" method="post" action="/j_spring_security_check">
                    <input name="returnURL" type="hidden" value='${returnURL!""?html}'>
                    <div class="lop-content">
                        <div class="c-text">
                            <span class="login-icon icon-5"></span>
                            <input type="text" value="${lastAttemptUserName!''?html}" name="j_username" tabIndex="1"
                                   class="JS-inputEvent" id="index_login_username" placeholder="手机号/学号"/>
                            <div class="errorTips">请输入学号/手机号</div>
                        </div>
                        <div class="c-text">
                            <span class="login-icon icon-4"></span>
                            <input type="password" id="index_login_password" name="j_password" class="JS-inputEvent"
                                   tabIndex="2" placeholder="输入密码"/>
                            <span class="pwd-icon icon-8 JS-pwd"></span>
                            <div class="errorTips"></div>
                        </div>
                        <div class="c-text">
                            <a href="javascript:void(0);" class="info active JS-rememberMe-btn">
                                <input name="_spring_security_remember_me" value="on" type="checkbox" checked="checked"
                                       style="display: none;"/>
                                <span class="login-icon icon-6"></span>记住我
                            </a>
                            <a href="/ucenter/resetnavigation.vpage?ref=fixed" target="_blank" class="info">忘记密码</a><span
                                class="line"> | </span><a href="javascript:void(0);" class="info JS-register-main">立即注册</a>
                        </div>
                    </div>
                    <div class="loginPop-footer">
                        <input type="submit" value="登录" class="login-btn" id="_a_loginForm" name="_a_loginForm" tabIndex="3"/>
                        <#--<div class="qq-btn">
                            <a href="/qq/authorizecode.vpage" target="_blank" class="info-btn">用QQ登录</a>
                        </div>-->
                    </div>
                </form>
            </#if>
        </div>
    </div>
</script>