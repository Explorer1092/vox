<#import "module.ftl" as module>
<@module.page
title="修改密码"
pageJsFile={"siteJs" : "public/script/auth"}
pageJs=["siteJs"]
leftMenu="密码修改"
>
<div class="info-text">
    为保证您的帐号安全，首次登录建议修改密码
</div>
<div class="title-text">
    修改密码
</div>
<div class="reset-form">
    <div class="form-title" style="padding-left:313px;padding-top:20px;">
        <div class="general-error-tip"></div>
    </div>
    <form id="reset-form" action="/auth/resetPassword.vpage" method="post" style="margin-left:300px;">
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
            <a id="js-reset" data-op-type="modify" class="submit-btn save-btn submit-reset" href="javascript:void(0)">提交</a>
        </div>
    </form>
</div>
<div class="layer-module">
    <div class="success">
        <div>恭喜您！密码修改成功，请牢记</div>
    </div>
</div>
</@module.page>