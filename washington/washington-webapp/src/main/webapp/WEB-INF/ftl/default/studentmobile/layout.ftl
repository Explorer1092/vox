<#macro page dpi=".595" title="">
<!DOCTYPE html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />
    <meta name="MobileOptimized" content="320" />
    <meta name="Iphone-content" content="320" />
    <meta name="apple-mobile-web-app-capable" content="no">
    <meta name='apple-touch-fullscreen' content='no'>
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no">
    <meta name="format-detection" content="address=no">
    <title>${title!}</title>
    <@sugar.capsule js=['zepto','studentapp','template'] css=['studentapp'] />
    <@app.script href="public/skin/mobile/pc/js/fullScreenDpi${dpi!'.595'}.js" />
</head>

<body>
    <#nested />

    <script type="text/javascript">
        //用户登录失效处理
        function loginInvalid(data){
            if(data.errorCode == 900){
                location.href = '/studentMobile/center/logininvalid.vpage';
            }
        }

        $(function(){
            $(document).on('click','#promptAlertCloseBut',function(){
                $(this).closest('div.layer').hide();
            });

            //适配640以下的分辨率
           /* var screenWidth = window.screen.width;
            if(screenWidth == 540){
                $("html").addClass("availWidth-540");
            }
            if(screenWidth == 480){
                $("html").addClass("availWidth-480");
            }*/
        });
    </script>
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
</#macro>
