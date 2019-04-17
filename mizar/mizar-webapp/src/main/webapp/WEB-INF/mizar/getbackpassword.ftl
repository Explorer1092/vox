<#import "layout/webview.layout.ftl" as layout/>
<@layout.page
title="找回密码"
pageCssFile={"mizar" : ["public/skin/css/skin"]}
pageJsFile={"siteJs" : "public/script/auth"}
pageJs=["siteJs"]
>
    <div class="topBar">
        <div class="inner clearfix">
            <a href="/index.vpage" class="logo">一起作业</a>
            <span class="tag">开放平台</span>
        </div>
    </div>
    <div style="width:1000px;margin:0 auto 20px; min-height: 76%;">
        <#--<div class="info-text">-->
            <#--总校长、校区校长可通过“忘记密码”功能找回密码，校区老师如需找回密码请联系总校长或校区校长！-->
        <#--</div>-->
        <div class="title-text">
            找回密码
        </div>
        <div class="reset-form">
            <div class="form-title">
                通过已绑定手机找回密码
                <div class="general-error-tip"></div>
            </div>
            <form id="reset-form" action="/auth/resetPassword.vpage" method="post">
                <div class="item clearfix">
                    <label for="tel">手机号</label>
                    <input id="tel" name="mobile" class="reset-input" data-empty-tip="请输入手机号" placeholder="请输入绑定手机号" />
                    <span class="error-tip"></span>
                </div>
                <div class="item clearfix">
                    <label for="code">验证码</label>
                    <input id="code" name="captchaCode" class="reset-input" data-empty-tip="请输入验证码"  placeholder="4位数字验证码" style="width:110px;" />
                    <a href="javascript:void(0)" id="send-code" class="blue-btn send-code" style="float:left;width:80px;padding:0;margin-left:8px;">获取验证码</a>
                    <span class="send-success">验证码成功发送！</span>
                    <span class="error-tip"></span>
                </div>
                <div class="item clearfix">
                    <label for="new-password">新密码</label>
                    <input id="new-password" type="password" name="password" data-empty-tip="请输入新的密码" class="reset-input" placeholder="请输入新密码" />
                    <span class="error-tip"></span>
                </div>
                <div class="item clearfix">
                    <label for="repeat-password">确认新密码</label>
                    <input id="repeat-password" type="password" name="passwordConfirm" data-empty-tip="请再次输入新的密码" class="reset-input" placeholder="请再次输入新密码" />
                    <span class="error-tip" ></span>
                </div>
                <div class="submit-wrapper clearfix">
                    <a id="js-reset" data-op-type="getback" class="submit-btn save-btn submit-reset" href="javascript:void(0)">提交</a>
                </div>
            </form>
        </div>
    </div>
    <div class="footBar">
        <div class="inner">
            <div class="copyright">Copyright &copy; 2011-${.now?string('yyyy')} 17ZUOYE Corporation. All Rights Reserved.</div>
        </div>
    </div>
</@layout.page>