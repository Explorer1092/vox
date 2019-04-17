<#import "../layout.ftl" as layout>
<@layout.page title="登录" pageJs="chipsLogin">
    <@sugar.capsule css=['chips'] />
<div class="signWrap">
    <div class="signMain">
        <p class="signName">薯条英语</p>
        <div class="signBox">
            <div class="signInput">
                <input type="number" data-cid="${cid!''}" class="signPhone" placeholder="账号/手机号">
            </div>
            <div class="signInput">
                <div class="smallInput">
                    <input type="tel" style="box-sizing: border-box;" maxlength="4" class="signPassword" placeholder="填写验证码">
                </div>
                <div class="getNum active" id="get_code">获取验证码</div>
            </div>
        </div>
        <input type="hidden" value="${returnUrl!}" id = "returnUrl">
        <div class="signBtn active">登录</div>
    </div>
</div>


</@layout.page>

<#--</@chipsIndex.page>-->
