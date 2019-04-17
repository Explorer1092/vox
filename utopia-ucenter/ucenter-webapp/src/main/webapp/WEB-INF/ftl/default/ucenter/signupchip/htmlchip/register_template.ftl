<div class="js-scrollMainIdBox" id="register_template" style="display: none;">
    <div class="pr-header">
        <div class="pr-header-inner clearfix">
            <a href="/index.vpage" class="logo"></a>
            <div class="f-service">
                <#--<a href="javascript:void(0);" class="btn" id="message_right_sidebar">反馈意见</a>-->
                <a href="javascript:void(0);" class="btn btn-blue" onclick="window.open('${ProductConfig.getMainSiteBaseUrl()}/redirector/onlinecs_new.vpage?type=student&question_type=question_account_ps&origin=PC-注册','','width=856,height=519');" data-tongji="学生-在线咨询">在线咨询</a>
                <span class="text">咨询时间<br/>8:00-21:00</span>
            </div>
        </div>
    </div>
    <div class="pr-main pr-main-child js-registerTemplateType" data-type="student">
        <div class="hd">请设置你的密码！</div>
        <div class="main-box">
            <div class="main-inner">
                <div id="allSearchClazzItem"></div>
                <ul class="main-list">
                    <li style="background: none;border: none;padding: 0;margin: 0;position: relative;top: -90px;">
                        <input type="text" value="" maxlength="4" name="clazzId" data-label="班级" class="require js-select-clazz-id" style="display: none;"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li style="background: none;border: none;padding: 0;margin: 0;position: relative;top: -20px;">
                        <input type="text" value="" maxlength="4" name="gender" data-label="性别" class="require js-select-gender" style="display: none;"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                </ul>
                <ul class="main-list">
                    <li class="r-username ">
                        <label>请输入真实姓名</label>
                        <input type="text" value="" maxlength="20" name="username" data-label="真实姓名" class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                        <div class="reg-arrowInfo">
                            <span class="sar">◆<span class="ir">◆</span></span>
                            <p>1.注册后账号将发送给老师；</p>
                            <p>2.姓名填写后不能修改。</p>
                        </div>
                    </li>
                    <li class="r-password ">
                        <label>请设置密码</label>
                        <input type="password" value="" maxlength="16" name="password" data-label="密码"  class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li>
                        <label>请再次输入设置的密码</label>
                        <input type="password" value="" maxlength="16" name="verify_password" data-label="确认密码"  class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="" style="display: none;">
                        <label>请输入家长手机号</label>
                        <input type="text" value="" maxlength="11" name="mobile" class="" data-label="手机号"/>
                        <span class="info-text"><a href="javascript:void(0);" class="code-btn" id="studentGetCheckCodeBtn"><span>免费获取验证码</span></a></span>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                        <div class="reg-arrowInfo">
                            <span class="sar">◆<span class="ir">◆</span></span>
                            <p>1.不绑定手机，你兑换的奖品将不能寄送；</p>
                            <p>2.不绑定手机，密码丢失将不能通过手机号找回！</p>
                        </div>
                    </li>
                    <li style="display: none;">
                        <label>请输入手机收到的短信验证码</label>
                        <input type="text" value=""maxlength="6" class="" name="ver_code" data-label="验证码"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                </ul>
                <div class="submit"><a href="javascript:void(0);" class="v-submit-register" data-type="student">完成注册</a></div>
            </div>
        </div>
    </div>
    <#--老师-->
    <div class="pr-main pr-main-teacher js-registerTemplateType" style="display: none;" data-type="teacher">
        <div class="hd">请设置你的密码！</div>
        <div class="main-box">
            <div class="main-inner">
                <ul class="main-list">
                    <li class="r-username ">
                        <label>请输入真实姓名</label>
                        <input type="text" value="" maxlength="20" class="require" name="username" data-role="teacher" data-label="真实姓名"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="r-password ">
                        <label>请设置密码</label>
                        <input type="password" value="" maxlength="16" name="password" class="require" data-label="密码"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="last ">
                        <label>请再次输入设置的密码</label>
                        <input type="password" value="" maxlength="16" name="verify_password" data-label="确认密码"  class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="last r-invite null-bor ">
                        <label>请填写邀请人手机或ID（可不填）</label>
                        <input name="invite" type="text" value="" id="invite_info">
                        <#--<span class="info-text">（可不填）</span>-->
                    </li>
                </ul>
                <div class="submit"><a href="javascript:void(0);" class="v-submit-register" data-type="teacher">下一步</a></div>
            </div>
        </div>
    </div>

    <div class="pr-footer">
        <div class="pr-footer-inner">
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
</div>