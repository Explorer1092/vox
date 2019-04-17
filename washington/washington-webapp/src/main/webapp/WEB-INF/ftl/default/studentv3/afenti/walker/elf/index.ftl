<!doctype html>
<html>
<head>
<#include "../../../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
<@sugar.check_the_resources />
<@sugar.capsule js=["jquery", "jquery.flashswf"] css=[] />
    <style>
        html, body {
            padding: 0;
            margin: 0;
        }
    </style>
<@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div id="movie">
    <div id="install_flash_player_box" style="margin:20px;">
            <span id="install_download_tip"
                  style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                您未安装Flash Player插件，请 <a href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
                <br/><br/>
                <span>
                    如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                </span>
            </span>
    </div>
</div>
<#include "../../../../flash/prepareflashloadercdntypes.ftl"/>
<script type="text/javascript">

    //返回首页
    function returnbackaft(){
        setTimeout(function(){
            location.href = '/student/index.vpage';
        }, 200);
    }

    //跳转到天空竞技场排行榜页面
    function forwardAfentiArenaRank(){
        window.open('/student/afenti/arena/afentiarenarank.vpage');
    }

    //换教材按钮
    function changeBook(){
        $.get('/student/book/afenti.vpage', function(data){
            $("#afentiChangeBookBox").html(data);
        });
    }

    //根据阿分题-flash的高度 调整页面的高度
    function updateHeight(height){
        $("#movie").closest('dd').css({ height: height + "px" });
        $("#movie").find('object').css({ height: height + "px" });
    }

    $(function(){
        var walker = 'WalkerELF';
        //设置建议反馈类型
        //            feedBackInner.practiceName = AfentiSchool;

        p = {};

        p.flashUrl = '<@app.link_shared href="/resources/apps/flashv1/walkerelf/WalkerElf.swf"/>';
        p.domain = '${requestContext.getWebAppFullUrl('/')}';
        p.imgDomain = '<@app.link_shared href=""/>';
        p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
        p.flashId = walker;

        /** 加载Flash */
        $('#movie').getFlash({
            id       : walker,
            width    : '900',
            height   : '600',
            movie    : '<@flash.plugin name="Preloader"/>',
            flashvars: p
        });
    });
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>
