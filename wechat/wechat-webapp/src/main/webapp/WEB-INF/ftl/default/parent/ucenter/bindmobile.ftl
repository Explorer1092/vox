<#import "../layout.ftl" as homeworkIndex>
<@homeworkIndex.page title="绑定手机" pageJs="bindMobile">
    <style>
        html, body{background-color: #55abff;}
    </style>
    <div class="main body_background">
        <h1 class="logo"></h1>
        <div class="form_main">
            <h2>验证手机，随时找回孩子密码</h2>
            <ul class="fm_box">
                <li>
                    <input id="mobile" name="mobile" type="tel" maxlength="11" value="" placeholder="请输入您的手机号" />
                </li>
                <li>
                    <input id="code" name="code" type="tel" value="" placeholder="6位短信验证码" />
                </li>
            </ul>
            <div class="submit_box">
                <a style="width: 100%; margin: 10px 0; padding:10px 0;" id="getVerifyCodeBtn" data-cid="${cid!0}" data-requrl="/parent/ucenter/sendcode.vpage" class="btn_mark btn_orange btn_disable" href="javascript:void (0)"> <strong>获取验证码</strong> </a>
                <a id="bindingSubmitBtn" href="javascript:void(0);" class="btn_mark btn_mark_block">提交</a>
                <input type="hidden" id="back_url" name="back_url" value=""/>
            </div>
        </div>
    </div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'pv_bindparentmobile'
            })
        })
    }
</script>
</@homeworkIndex.page>