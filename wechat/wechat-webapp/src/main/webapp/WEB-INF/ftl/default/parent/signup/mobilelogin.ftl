<#import "../layout.ftl" as login>
<@login.page title='登录' pageJs="mobileLogin">
<@sugar.capsule css=['jbox'] />
<style>
    html, body{background-color: #55abff;}
</style>

<div class="main body_background">
    <h1 class="logo"></h1>
    <div class="form_main">
        <h2 class="js-notice">更换后，原号码${mobile!""}不能作为登录使用</h2>
        <ul class="fm_box">
            <li>
                <input id="mobile" name="mobile" type="text" value="" placeholder="请输入您的手机号码" />
                <a id="getVerifyCodeBtn" data-cid="${cid!0}" data-requrl="/signup/parent/sendlogincode.vpage" class="btn_mark btn_orange btn_fly btn_disable" href="javascript:void (0);"><span>获取验证码</span></a>
            </li>
            <li>
                <input id="code" name="code" type="text" value="" placeholder="6位短信验证码" />
                <input id="source" name="source" type="hidden" value="${source!""}"/>
            </li>
        </ul>

        <div class="submit_box">
            <a id="bindingSubmitBtn" href="javascript:void(0);" class="btn_mark btn_mark_block">绑定</a>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'pv_loginbymobile'
            })
        })
    }
</script>
</@login.page>