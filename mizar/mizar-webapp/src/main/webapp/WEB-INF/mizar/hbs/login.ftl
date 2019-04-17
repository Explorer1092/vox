<#import "./hbslayout.ftl" as layout/>
<@layout.page
title="华罗庚金杯数学竞赛"
pageCssFile={"hbs" : ["/public/skin/css/hbs/skin"]}
pageJsFile={"hbs" : "/public/script/hbs/index"}
pageJs=["hbs"]
>
<h4>华罗庚金杯少年数学邀请赛获奖情况查询</h4>
<div class="inquiryInfo">
    <div class="innerBox">
        <div class="infoBox">
            <div class="titleBox">
                <p>届数</p>
                <p class="bg-color">用户名</p>
                <p>密码</p>
                <p class="bg-color">验证码</p>
            </div>
            <#--<form method="POST" action="/hbs/score/login.vpage" id="loginForm">-->
            <div class="valueBox">
                <div class="inputBox">
                    <p>第22届</p>
                </div>
                <div class="inputBox bg-color">
                    <input type="text" value="${userName!}" data-bind="value:userName" maxlength="30" name="username" placeholder="请输入用户名或手机号"/>
                </div>
                <div class="inputBox">
                    <input type="password" value="" data-bind="value:psw" maxlength="30" name="password" placeholder="请输入密码"/>
                </div>
                <div class="inputBox bg-none">
                    <div class="inpChild bg-color">
                        <input type="text" value="" data-bind="value:viaCode" maxlength="4" name="verifyCode" placeholder="请输入验证码"/>
                    </div>
                    <a class="testCode bg-color" href="javascript:;" data-bind="click:refreshCaptcha">
                        <img data-bind="attr:{src:captcha}" alt="" style="vertical-align: middle;">
                    </a>
                </div>
                <input type="hidden" value="${captchaToken!}" name="captchaToken">
            </div>
            <#--</form>-->
        </div>
        <div class="confirmBox">
            <a class="btn" href="javascript:;" data-bind="click:submitBtn">确认</a>
            <a class="retrieve" href="msm.vpage">忘记密码？</a>
        </div>
        <div class="explainBox">
            <p>登录说明</p>
            <div class="textBox">
                <span>1、</span>
                <div class="txt-box">
                    报名参加考试的学生，初次登录可以使用报名的证件号＋初始密码123456登录，通过手机验证之后，
                    登录个人中心查询决赛获奖情况，之后再次登录用户名变更为验证成功的手机号。如果已经登录过，
                    则使用手机号为用户名进行登录。
                </div>
            </div>
            <div class="textBox">
                <span>2、</span>
                <div class="txt-box">
                    注明：如有任何问题，请联系客服QQ：2089922565</br>工作时间：10:00-19:00(周一到周五)
                </div>
            </div>
        </div>
        <div class="explainBox">
            <p>帮助中心</p>
            <div class="textBox">
                <span>1、</span>
                <div class="txt-box">
                    只能查询奖项。
                </div>
            </div>
            <div class="textBox">
                <span>2、</span>
                <div class="txt-box">
                    评奖按比例，具体请咨询当地参赛单位。
                </div>
            </div>
            <div class="textBox">
                <span>3、</span>
                <div class="txt-box">
                    如查询不到，请联系当地参赛单位。
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    var token = "${captchaToken!}",
        errorMsg = "${errorMsg!}";
</script>
<style>
    div.jqi .jqibuttons button {
        background-color: #5690d8;
        border: 1px solid #5690d8;
    }
</style>
</@layout.page>