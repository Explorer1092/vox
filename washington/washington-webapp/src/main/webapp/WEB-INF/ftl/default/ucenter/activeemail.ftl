<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule css=["plugin.register"] />
</head>

<body>
    <div class="main">
        <div class="logo"><a href="/" title="" style="display:block; width: 145px;height: 55px; float: left;margin:42px 0 0;"></a></div>
        <div id="signup_loading_box" class="loginbox" style="text-align: center;">
            <div style="color:#64A61E;width:400px;margin:0 auto;text-align:left;font-size:14px;line-height:1.8;font-weight: bold;">
                <p>亲爱的用户：</p>
                <p>感谢您的使用！您的一起作业邮箱已验证成功！<br/>
                    <#if state?? && state>
                        您现在可以<a href="/" style="color:#0033cc;text-decoration: underline">立即登录</a>
                    </#if>
                </p>
                <p id="countdown" style="margin-top: 40px;text-align:center;font-size:12px;font-weight: normal;color: #333;"></p>
            </div>
        </div>
        <div class="footer">
            <p class="tel">客服电话：<b><@ftlmacro.hotline/></b>
            </p>
            <p class="navs">
                <a href='/help/aboutus.vpage'>关于我们</a><span>•</span>
                <a href='/help/jobs.vpage'>诚聘英才</a><span>•</span>
                <a href='/help/contactus.vpage'>联系我们</a><span>•</span>
                <a href='/help/parentsguidelines.vpage'>家长须知</a><span>•</span>
                <a href='/help/childrenhealthonline.vpage'>儿童健康上网</a><span>•</span>
                <a href='/help/index.vpage'>帮助</a><span>•</span>
                <a href='javascript:;' class="js-commentsButton">我要评论</a><span>•</span>
                <a href='javascript:;' class="js-reportButton">我要举报</a><span>•</span>
                <a href="javascript:void(0);">意见反馈</a>
            </p>
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
        <#include "../common/to_comments_report.ftl" >
        <script type="text/javascript">
            var time = 10;
            function countDown(){
                time--;
                document.getElementById("countdown").innerHTML = ''+time+'秒页面后自动跳转';
                if(time == 0){
                    window.location = "/";
                    return false;
                }
                var settime = setTimeout("countDown()",1000);
            }
            window.onload = countDown();
        </script>
    </div>
</body>
</html>