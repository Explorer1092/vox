<#import "layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业_开放平台"
pageCssFile={"mizar" : ["public/skin/css/skin"]}
pageJsFile={"siteJs" : "public/script/auth"}
pageJs=["siteJs"]
>
<#assign baseInfo={
"headerText":"一起作业",
"tagText":"开放平台",
"loginDialogTitle":"合作账号登录",
"copyRight":"Copyright © 2011-${.now?string('yyyy')} 17ZUOYE Corporation. All Rights Reserved."
}/>
<style>
    body{
        background:#8addf1;
        min-width: 100%;
    }
    @media screen and (max-width: 1080px) {
        .page-login{
            -webkit-overflow-scrolling: touch;
            margin-bottom: 6rem;
        }
        .page-login .module{
            margin: 2rem auto 6rem;
        }
        .footBar.big .copyright {
            padding: 15px 0;
        }
    }



</style>
<div class="topBar white">
    <div class="inner clearfix">
        <a href="javascript:;" class="logo">${baseInfo['headerText']!}</a>
        <span class="tag">${baseInfo['tagText']!}</span>
    </div>
</div>
<div class="page-login">
    <!--[if lte IE 9]>
        <div class="prom">
            <a id="js-close" href="javascript:;" class="close"></a>
            <span class="text">请您使用谷歌（Chrome）、火狐（Firefox）或ie9.0以上版本浏览器访问本站</span>
        </div>
    <![endif]-->

    <div class="module">
        <div class="head">${baseInfo['loginDialogTitle']!}</div>
        <form id="login-form" method="post" action="/auth/login.vpage">
            <div class="main">
                <ul class="list">
                    <li id="error-tip" style="color:#ff4d4d;text-align: center;height:21px;"></li>
                    <li>
                        <div class="input username"><input name="username" class="require-login" type="text" placeholder="请输入账号或手机号"></div>
                        <div class="error">请输入账号</div>
                    </li>
                    <li>
                        <div class="input password"><input name="password" class="require-login" type="password" placeholder="请输入密码"></div>
                        <div class="error">请输入密码</div>
                    </li>
                    <li>
                        <div class="bar clearfix">
                            <a href="/auth/getBackPass.vpage">忘记密码</a>
                            <label class="checkbox remember-password">
                                <input checked type="checkbox" />记住密码
                            </label>
                        </div>
                    </li>
                </ul>
                <button id="js-login" class="button" type="button">登&nbsp;&nbsp;录</button>
            </div>
        </form>
    </div>
</div>
<div class="footBar big">
    <div class="inner">
        <div class="copyright">${baseInfo['copyRight']}</div>
    </div>
</div>
</@layout.page>