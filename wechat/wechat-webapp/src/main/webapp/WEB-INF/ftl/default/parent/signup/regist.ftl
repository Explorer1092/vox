<#import "../layout.ftl" as login>
<@login.page title='注册' pageJs="parentRegist">
<style>
    html, body{background-color: #55abff;}
</style>
<div class="main body_background">
    <h1 class="logo"></h1>
    <div class="form_main">
        <h2>免费注册 &nbsp;&nbsp;绑定孩子</h2>
        <ul class="fm_box">
            <li>
                <input id="children_id" type="tel" value="" placeholder="请输入孩子帐号"/>
            </li>
            <li>
                <input id="children_password" type="password" value="" placeholder="请输入密码"/>
                <input id="woid" type="hidden" value="${woid!}"/>
            </li>
        </ul>
        <div class="password_log">
            <span id="messageTip" style="display: none;"></span>
        </div>
        <div class="submit_box">
            <a id="next_but" href="javascript:void(0);" class="btn_mark btn_mark_block">下一步</a>
        </div>
    </div>
    <!--表单 end//-->
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'pv_regbymobile'
            })
        })
    }
</script>
</@login.page>