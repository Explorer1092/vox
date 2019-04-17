<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起时光与老师穿越时空30年_一起作业"
pageJs=['jquery', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/teadayvideo/css/teaDay-pc"]}
fastClickFlag=false
>
<div class="videoDetails" style="width: 720px; margin: 0 auto;">
    <div class="head-back">
        <img src="<@app.link href="public/skin/project/teadayvideo/images/d-top.png"/>" width="100%">
    </div>
    <div class="inner-content">
        <div class="vd-header">
            <div class="logo"><a href="/"></a></div>
        </div>
        <div class="vd-video JS-clickPlayStop" style="overflow: visible;">
            <#--play button-->
            <embed width="632" height="374"
                   flashvars="file=http://v.17zuoye.cn/corp/teachersDay_720.mp4&amp;image=http://cdn.17zuoye.com/static/project/teacherday/teacherday-video-img1.jpg&amp;width=632&amp;height=375"
                   allowfullscreen="true" quality="high" name="single"
                   src="http://trustee.oss.17zuoye.com/thirdparty/flvplayer/flvplayer.swf"
                   type="application/x-shockwave-flash"></embed>
        </div>

        <div class="vd-info">
            <div class="tips"><#--温馨提示：建议在WiFi环境下播放-->&nbsp;</div>
            <div class="text" style="font-size: 20px">你是否还记得与老师在一起的时光？你有没有想过，30年后，老师会变成什么模样？</div>
            <div class="text" style="font-size: 20px">2016教师节前夕，一起作业重磅推出大型公益策划“一起时光”，为一位小学教师实施了一次极其特别的时光之旅。让30年后的她出现在今天的课堂上，给现在的学生们讲一堂课。面对这突如其来的巨大变化，学生们的反应超乎我们想象.....</div>
        </div>
    </div>
    <div class="foot-back">
        <img src="<@app.link href="public/skin/project/teadayvideo/images/d-footer.png"/>" width="100%"/>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function () {
        $(document).on("click", ".JS-clickPlayStop", function(){
            YQ.voxLogs({
                module: "m_wsOhBCV9",
                op : 'o_ROxLdawn',
                s0 : 'pc'
            });
        });

        YQ.voxLogs({
            module: "m_wsOhBCV9",
            op : 'o_fmtcflvx',
            s0 : 'pc'
        });
    }
</script>
</@layout.page>