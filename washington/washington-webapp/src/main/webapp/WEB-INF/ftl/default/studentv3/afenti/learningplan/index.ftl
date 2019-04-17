<#import '../../layout/layout.ftl' as temp>
<@temp.page pageName='assignments' clazzName='m-body-back-learning'>
<div style="height: auto; padding: 10px 0; text-align: center; width: 640px; margin: 0 auto;">
    <#--<#assign url='http://www.test.17zuoye.net/resources/apps/hwh5/afenti/V2_0_0/index.html?subject=MATH&domain=http%3A%2F%2Fwww.test.17zuoye.net&img_domain=http%3A%2F%2Fcdn-static-shared.test.17zuoye.net%2F&server_type=test'/>-->
    <iframe class="vox17zuoyeIframe" id="afentBox" src="${url!}" name="apps_game_homework" scrolling="auto" frameborder="0" style="width: 640px; height: 800px; "></iframe>
    <div style="position: relative;">
        <div style="position: absolute; right: -175px; bottom: 0;">
            <div><img src="<@app.link href="public/skin/project/afentidetailapp/images/afenti-page-code.png"/>" alt="" width="170" height="170"></div>
            <div style="font-size: 14px; line-height: 150%; color: #fff; padding: 10px 0;">在一起作业学生端上使用<br/>阿分题，体验更佳哦～</div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function myBrowser() {
        var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
        //判断是否IE浏览器
        return userAgent.indexOf("compatible") > -1 && (userAgent.indexOf("MSIE 5.5") > -1 || userAgent.indexOf("MSIE 6.0") > -1 || userAgent.indexOf("MSIE 7.0") > -1 || userAgent.indexOf("MSIE 8.0") > -1 || userAgent.indexOf("MSIE 9.0") > -1);

    }
    $(function () {
        if (myBrowser()) {
            $("#afentBox").attr("src", "/project/ie/index.vpage?ref=open");
        }

        $(window).bind("beforeunload", function () {
            return '系统可能不会保存您所做的更改。';
        });
    });
</script>
</@temp.page>
