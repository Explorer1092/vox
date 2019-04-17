<#import "./hbslayout.ftl" as layout/>
<@layout.page
title="华罗庚金杯数学竞赛"
pageCssFile={"hbs" : ["/public/skin/css/hbs/skin"]}
pageJsFile={"hbs" : "/public/script/hbs/msm"}
pageJs=["hbs"]
>
<h4>华罗庚金杯少年数学邀请赛获奖情况查询</h4>
<div class="inquiryInfo">
    <div class="innerBox">
        <div class="infoBox">
            <div class="titleBox">
                <p>手机号码</p>
                <p class="bg-color">设置密码</p>
                <p>确认密码</p>
                <p class="bg-color">验证码</p>
                <p>短信验证码</p>
            </div>
            <div class="valueBox">
                <div class="inputBox">
                    <#if phoneNumber?? && phoneNumber?has_content>
                        <p id="phoneNumber">${phoneNumber!"--"}</p>
                    <#else>
                        <input type="text" value="" data-bind="value:mobile" maxlength="11" placeholder="请输入手机号"/>
                    </#if>
                </div>
                <div class="inputBox bg-color">
                    <input type="password" value="" data-bind="value:psw" maxlength="20" placeholder="请输入6到20位以内的密码"/>
                </div>
                <div class="inputBox">
                    <input type="password" value="" data-bind="value:psw2" maxlength="20" placeholder="请输入6到20位以内的确认密码"/>
                </div>
                <div class="inputBox bg-none">
                    <div class="inpChild bg-color">
                        <input type="text" value="" data-bind="value:viaCode" maxlength="4" placeholder="请输入验证码"/>
                    </div>
                    <a class="testCode bg-color" href="javascript:;" data-bind="click:refreshCaptcha">
                        <img src="" alt="" data-bind="attr:{src:captcha}" style="vertical-align: middle;">
                    </a>
                    <input type="hidden" value="${captchaToken!}" name="captchaToken">
                </div>
                <div class="inputBox bg-none">
                    <div class="inpChild ">
                        <input type="text" value="" data-bind="value:phoneViaCode" minlength="6" maxlength="20" placeholder="请输入短信验证码"/>
                    </div>
                    <a class="testCode" href="javascript:;" data-bind="text:btnText,click:sendMsm" id="getCodeBtn">获取短信</a>
                </div>
            </div>
        </div>
        <div class="confirmBox">
            <a class="btn" href="javascript:;" data-bind="click:submitBtn">确认</a>
        </div>
    </div>
</div>
<script>
    var token = "${captchaToken!}",
            error = "${errorMsg!}",
            hasPhone =<#if phoneNumber?? && phoneNumber?has_content>true<#else>false</#if>;
</script>
<style>
    div.jqi .jqibuttons button {
        background-color: #5690d8;
        border: 1px solid #5690d8;
    }
</style>
</@layout.page>