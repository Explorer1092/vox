<#import "../module.ftl" as com>
<@com.page>
<h4>

    找回密码
</h4>
<ul class="stepInfoBox">
    <li class="sel"><i>1</i><b>输入学号</b></li>
    <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
    <li><s></s><i>3</i><b>重置密码</b></li>
    <li><s></s><i>4</i><b>成功</b></li>
</ul>
<div class="aliCenter">一起作业号： <strong class="fontbig" style="color: #fa7252;">${userInfo.userId!''}</strong> &nbsp;&nbsp;<a href="/ucenter/resetnavigation.vpage" class="w-btn w-btn-light w-btn-mini">换个账号</a> </div>
<div class="aliCenter" style="margin: 40px 0px -30px 0px">请选择找回方式：</div>
<div class="findWayBox">
    <#if userInfo.obscuredMobile != ''>
        <div class="SelectBox">
            <ul>
                <li class="first">
                    <p class="icon"></p>
                    <span>手机找回</span>
                </li>
                <li class="second">
                    <p class="detail"><#if userInfo.obscuredMobile != ''>已绑定手机号码${userInfo.obscuredMobile}<#else>您还未绑定手机</#if></p>
                    <p>输入手机收到的验证码重置密码</p>
                </li>
                <li class="three">
                    <a id="phone" href="javascript:next('phone');" class="w-btn reg_btn reg_btn_well v-forgetStaticLog" data-op="click-forget-phone">
                        免费发送验证码
                    </a>
                </li>
            </ul>
        </div>
    </#if>

    <#if userInfo.obscuredEmail != ''>
        <div class="SelectBox">
            <ul>
                <li class="first" >
                    <p class="icon icon01"></p>
                    <span>邮箱找回</span>
                </li>
                <li class="second">
                    <p class="detail"><#if userInfo.obscuredEmail != ''>已绑定邮箱${userInfo.obscuredEmail}<#else>您还未绑定邮箱</#if></p>
                    <p>通过收到的验证邮件链接重置密码</p>
                </li>
                <li class="three" >
                    <a id="email" href="javascript:next('email');" class="w-btn reg_btn reg_btn_well v-forgetStaticLog" data-op="click-forget-email">发送验证邮件</a>
                </li>
            </ul>
        </div>
    </#if>

    <#--<#if (userInfo.userType == 3)!false>-->
        <#--<div class="SelectBox" style="padding-bottom: 80px;">-->
            <#--<ul>-->
                <#--<li class="first" style="margin-top:45px;">-->
                    <#--<p class="icon icon06"></p>-->
                    <#--<span>家长微信找回</span>-->
                <#--</li>-->
                <#--<li class="second" style="padding-top: 15px;">-->
                    <#--<img src="<@app.link href="public/skin/default/images/password/micro-channel-authentication.png?1.0.1"/>" alt=""/>-->
                <#--</li>-->

            <#--</ul>-->
        <#--</div>-->
    <#--</#if>-->

<#--<#if (userInfo.sqSetted)!false>
    <div class="SelectBox">
        <ul>
            <li class="first">
                <p class="icon icon02"></p>
                <span>密保问题</span>
            </li>
            <li class="second" style="width:300px; text-align:center;">
                <p class="detail"><#if (userInfo.sqSetted)!false>已设置密保<#else>您还未设置密保</#if></p>
                <p>回答你已经设置的密保问题重置密码</p>
            </li>
            <li class="three" style="text-align:left;">
                <a id="securityquestion" href="javascript:next('securityquestion');" class="w-btn reg_btn reg_btn_well v-forgetStaticLog" data-op="click-forget-securityquestion">回答密保问题</a>
            </li>
        </ul>
    </div>
</#if>-->
    <div class="SelectBox">
        <ul>
            <li class="first" style="width:185px; text-align:center;">
                <p class="icon icon04"></p>
                <span>客服找回</span>
            </li>
            <li class="second" style="width:300px; text-align:center;">
                <p class="detail">客服在线时间：8:00-21:00</p>
                <p>提供个人详细信息证实身份后找回密码</p>
            </li>
            <li class="three">
                <a href="javascript:void(0);" class="w-btn reg_btn reg_btn_well v-forgetStaticLog js-clickServerPopup" data-origin="PC-找回密码" data-usertype="${((userInfo.userType == 3)!false)?string("student", "teacher")}" data-op="click-forget-hotline">在线申述</a>
            </li>
        </ul>
    </div>
    <div class="clear"></div>
</div>
<#--<div class="serviceBox">
    <a href="javascript:goBack();" class="w-btn w-btn-small w-btn-green">返回</a>
    <@com.feedbackButton />
</div>-->
<script type="text/javascript">
    $(function() {
        $17.tongji("进入选择方式总数");
        var hotline = $("#hotline");

        var $userType = "${((userInfo.userType == 1)!false)?string("teacher", "student")}";

        hotline.click(function() {
            $17.tongji("选择客服找回人数");
            window.open('${ProductConfig.getMainSiteBaseUrl()}/redirector/onlinecs_new.vpage?type='+$userType+'&question_type=question_account_ps&origin=PC-找回密码','','width:856px;height:519px;');
        });

        hotline.on("click", function(){
            $.post("/ucenter/forgotpwdcallcenter.vpage", {token: $17.getQuery("token")}, function(){});
        });
    });

    function goBack(){
        setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step1', 'token': '${context.token}' }); }, 200);
    }

    function next(type){
        if(type == "phone"){
            $17.tongji("选择手机人数");
        }else{
            $17.tongji("选择邮箱人数");
        }

        setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': "step3_" + type, 'token': '${context.token}' }); }, 200);
    }


</script>
<#include "serverinfo.ftl"/>
</@com.page>