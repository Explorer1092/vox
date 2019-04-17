<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title=" 领取奖励"
pageJs=["jquery", "voxLogs"]
pageCssFile={"css" : ["public/skin/mobile/student/app/downloadparentapp/css/skin"]}
>
<div class="header">
    <img src="<@app.link href="public/skin/mobile/student/app/downloadparentapp/images/bg01.jpg"/>">
    <img src="<@app.link href="public/skin/mobile/student/app/downloadparentapp/images/bg02.jpg"/>">
</div>

<div class="main">
    <div class="title"><span class="num num01"></span>下载并打开家长通 App</div>
    <div class="btnBox">
        <a href="javascript:void(0)" class="btn doClickOpenParent" data-module="m_ERIgMdYP" data-op="o_1zmGRhnX">点击打开</a>
    </div>
    <div class="title"><span class="num num02"></span>老师检查作业后，在“作业报告”中领取</div>
    <div class="guidePic">
        <img src="<@app.link href="public/skin/mobile/student/app/downloadparentapp/images/guide-pic01.png"/>">
        <img src="<@app.link href="public/skin/mobile/student/app/downloadparentapp/images/guide-pic02.png"/>">
        <div class="explain">* 需按时完成作业并达到及格线</div>
    </div>
</div>


<script type="text/javascript">
    signRunScript = function(){
        $(document).on("click", ".doClickOpenParent",function(){
            var $self = $(this);

            if(window['external'] && ('openparent' in window.external)){
                window.external.openparent("");
            }

            YQ.voxLogs({
                module : $self.data('module') || "",
                op : $self.data('op') || ""
            });
        });

        YQ.voxLogs({
            module : "m_ERIgMdYP",
            op : "o_NhUzx5ip"
        });
    };
</script>
</@layout.page>