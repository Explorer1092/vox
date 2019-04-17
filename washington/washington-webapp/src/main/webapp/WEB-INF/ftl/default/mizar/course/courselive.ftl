<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="${(periodName)!''}"
pageJs=["detail"]
pageJsFile={"detail" : "public/script/mobile/mizar/microdetail"}
<#--pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/liveLesson"]}-->
bodyClass="bg-grey">

<style>
    html,body{margin:0; padding:0; width:100%; height:100%}
    .warp-drag{ position: fixed; background-color: rgba(86, 172, 245, 0.8) ; bottom: 3.7rem; right: 0.1rem; z-index: 10; border-radius: .4rem; font-family: "微软雅黑","Microsoft YaHei",Arial,Helvetica,sans-serif; font-size: 0.8rem; cursor: pointer;}
    .warp-drag a{ display: block; padding: 0.5rem 0.8rem; color: #fff; text-decoration: none; max-width: 5rem; min-width: 3rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-align: center;}
</style>
</head>
<body>
<#if text?has_content>
    <div class="warp-drag" id="DragMain">
        <a href="${link!'#'}">${text!''}</a>
    </div>
</#if>
<script type="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<iframe src="${liveUrl!''}" style="width: 100%; height: 100%;" frameborder="0" scrolling="auto"></iframe>
<script>
    var initMode = "CourseLiveMode";
    var periodId = "${(periodId)!0}"
</script>
</@layout.page>