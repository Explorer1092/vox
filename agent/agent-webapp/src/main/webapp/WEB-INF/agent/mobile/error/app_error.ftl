<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <!--页面窗口自动调整到设备宽度，并禁止用户缩放页面-->
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <!--关闭电话号码识别：-->
    <meta name="format-detection" content="telephone=no" />
    <!--关闭邮箱地址识别：-->
    <meta name="format-detection" content="email=no" />
    <!-- iOS 的 safari 顶端状态条的样式 可选default、black、black-translucent-->
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>学校列表</title>
    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/home/base.css" />
    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/intoSchool/school.css" />
</head>
<body>
<div>
    <div class="head" style="text-align: center">${info!"出错了"}</div>
    <div class="error-page"></div>
    <div class="error-tip">
        错误代码：${code!}
        <#if errorMessage?? && errorMessage?has_content>
        <br><br>${errorMessage!}
        </#if>
    </div>
    <div class="clearfix">
        <#if url?? && url?has_content>
            <a href="${url}" class="btn-stroke fix-width back center">返回</a>
        <#else>
            <a id="js-history_back" href="javascript:void(0)" class="btn-stroke fix-width back center">返回</a>
        </#if>
    </div>
</div>
<script>
    var history_back = document.getElementById("js-history_back");
    history_back.onclick = function () {
        disMissViewCallBack();
    };
    function disMissViewCallBack(){
        //平台、设备和操作系统
        var system ={
            win : false,
            mac : false,
            xll : false
        };
        //检测平台
        var p = navigator.platform;
        system.win = p.indexOf("Win") == 0;
        system.mac = p.indexOf("Mac") == 0;
        system.x11 = (p == "X11") || (p.indexOf("Linux") == 0);
        if(system.win||system.mac||system.xll){
            window.history.back();
        }else{
            do_external('disMissView');
        }
    }
</script>
</body>
</html>