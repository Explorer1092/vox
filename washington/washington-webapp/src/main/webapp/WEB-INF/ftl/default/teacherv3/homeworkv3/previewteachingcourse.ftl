<!doctype html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>预览</title>
    <@sugar.capsule js=["jquery", "core", "alert", "jquery.flashswf","vue","vuex"] css=["plugin.alert", "student.widget"] />
    <#if (ProductDevelopment.isDevEnv())!false>
        <script type="text/javascript" src="//www.test.17zuoye.net/resources/apps/hwh5/homework-apps/light-interaction-apps/v0.1.7/dubhe.js"></script>
        <link rel="stylesheet" type="text/css" href="//www.test.17zuoye.net/resources/apps/hwh5/homework-apps/light-interaction-apps/v0.1.7/dubhe.css"/>
    <#else>
        <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/homework-apps/light-interaction-apps/v0.1.7/dubhe.js" />" ></script>
        <link rel="stylesheet" type="text/css" href="<@app.link href="/resources/apps/hwh5/homework-apps/light-interaction-apps/v0.1.7/dubhe.css" />" />
    </#if>
    <@sugar.site_traffic_analyzer_begin />
</head>
<body style="padding:0;margin:0;background-color:white;">
<div id="loadImage">
    <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;width: auto;" />
</div>
<div id="container">

</div>
<script type="text/javascript">
    $(function(){
        function initLightInteraction(pages,theme){
            theme = theme || {};
            var config = {
                container: "#container",//容器的id(必须)
                pages: pages,//数组，需要展示的完整数据结构(必须)
                cdnName: "${imgDomain!}", //当前cdn的名字,用于回答正确(错误)音频的播放（必须）
                autoPlay: true,//是否一进入页面播放音频 true/false
                framework:{
                    Vue : Vue,
                    Vuex : Vuex
                },
                type : "light",
                backgroundImage : theme.backgroundImage,
                onCloseSure : function(){
                    window.parent && (window.parent.postMessage("closePopup","${requestContext.webAppBaseUrl}"));
                },
                onFinish: function () {
                    window.parent && (window.parent.postMessage("closePopup","${requestContext.webAppBaseUrl}"));
                }
            };
            try{
                Dubhe.init(config);
            }catch (e) {
                $17.alert(e.message || "Dubhe初始化失败，请关闭重试");
            }
        }

        var $loadImage = $("#loadImage");
        $.get("/exam/flash/light/interaction/v2/course.vpage",{
            data: JSON.stringify({
                ids : ["${courseId}"]
            })
        }).done(function(res){
            if(res.success){
                var courseInfoArr = res.courseInfo || [];
                if(courseInfoArr.length > 0 && courseInfoArr[0].pages){
                    initLightInteraction(courseInfoArr[0].pages,courseInfoArr[0]);
                    $loadImage.hide();
                }else{
                    $17.alert("获取课程[${courseId}]为空，请反馈给客服");
                }
            }else{
                $17.alert(res.info || "获取数据失败");
            }
        }).fail(function(){
            $17.alert("网络失败，请退出页面重试");
        });
    });
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>
