<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起时光与老师穿越时空30年_一起作业"
pageJs=['jquery', 'weui', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/teadayvideo/css/teaDay"]}
>
<div class="videoHeader JS-downloadBox" data-type="share_2" style="display: none;">
    <a class="loadMore" href="http://wx.17zuoye.com/download/17parentapp?cid=100348" target="_blank">查看更多</a>
    <div class="left">
        <div class="pic" style="border:none; background: none;"><img src="http://cdn-cnc.17zuoye.cn/resources/app/17jzt/res/logo.png" width="100%"></div>
        <div class="text">
            <p class="tag">一起作业家长通</p>
            <p>用心打造高质量家庭教育资讯</p>
        </div>
    </div>
</div>
<div class="videoHeader JS-downloadBox" data-type="share_1" style="display: none;">
    <a class="loadMore" href="http://wx.17zuoye.com/download/17teacherapp?cid=300141" target="_blank">查看更多</a>
    <div class="left">
        <div class="pic" style="border:none; background: none;"><img src="http://cdn-cnc.17zuoye.cn/resources/app/17teacher/res/icon.png" width="100%"></div>
        <div class="text">
            <p class="tag">一起作业老师端</p>
            <p>轻松教学：随时布置、检查作业...</p>
        </div>
    </div>
</div>
<div class="videoHeader JS-downloadBox" data-type="share_3" style="display: none;">
    <a class="loadMore" href="http://wx.17zuoye.com/download/17studentapp?cid=100128" target="_blank">查看更多</a>
    <div class="left">
        <div class="pic" style="border:none; background: none;"><img src="http://cdn-cnc.17zuoye.cn/resources/app/17student/res//logo.png" width="100%"></div>
        <div class="text">
            <p class="tag">一起作业学生端</p>
            <p>海量题库：快乐学习，轻松作业</p>
        </div>
    </div>
</div>

<div class="videoDetails">
    <div class="head-back">
        <img src="<@app.link href="public/skin/project/teadayvideo/images/d-top.png"/>" width="100%">
    </div>
    <div class="inner-content">
        <div class="vd-header">
            <div class="logo"><a href="/"></a></div>
        </div>
        <div class="vd-video">
            <#--play button-->
            <div class="playBtn JS-playBtn JS-clickPlayStop" style="z-index: 6; left: 0; top: 0; width: 100%; height: 100%; margin: 0; background-image: url(http://cdn.17zuoye.com/static/project/teacherday/teacherday-video-img.jpg)"></div>
            <video id="VideoBox" poster="" width="100%" height="100%" controls><#--controls-->
                <#--<source src="http://www.runoob.com/try/demo_source/mov_bbb.mp4" type="video/mp4">-->
                <source src="http://v.17zuoye.cn/corp/teachersDay_720.mp4" type="video/mp4">
            </video>
        </div>

        <div class="vd-info">
            <div class="tips">温馨提示：建议在WiFi环境下播放</div>
            <div class="text">你是否还记得与老师在一起的时光？你有没有想过，30年后，老师会变成什么模样？</div>
            <div class="text">2016教师节前夕，一起作业重磅推出大型公益策划“一起时光”，为一位小学教师实施了一次极其特别的时光之旅。让30年后的她出现在今天的课堂上，给现在的学生们讲一堂课。面对这突如其来的巨大变化，学生们的反应超乎我们想象.....</div>
        </div>
        <div class="vd-btn">
            <a href="javascript:void(0)" class="shareBtn JS-clickShareBtn">点击分享</a>
        </div>
    </div>
    <div class="foot-back">
        <img src="<@app.link href="public/skin/project/teadayvideo/images/d-footer.png"/>" width="100%"/>
    </div>
</div>

<#--如果页面很简单，就可以用这种方式，单页实现JS效果-->
<script type="text/javascript">
    signRunScript = function () {
        var userType = ${(currentUser.userType)!'0'};

        //点击分享
        $(document).on("click", ".JS-clickShareBtn", function(){
            if (window['external'] && window.external['shareMethod']) {
                window.external.shareMethod(JSON.stringify({
                    title   : "一起时光：与老师穿越时空30年",
                    content : "教师节刷爆朋友圈的视频！瞬间戳中泪点……",
                    url     : location.protocol + '//' + location.host + location.pathname + '?type=share_' + userType,
                    type    : "SHARE",
                    channel : 4
                }));
            }else{
                if (window['external'] && window.external['shareInfo']) {
                    window.external.shareInfo(JSON.stringify({
                        title   : "一起时光：与老师穿越时空30年",
                        content : "教师节刷爆朋友圈的视频！瞬间戳中泪点……",
                        url     : location.protocol + '//' + location.host + location.pathname + '?type=share_' + userType
                    }));
                }else{
                    $.alert("分享失败!");
                }
            }

            YQ.voxLogs({
                module: "m_wsOhBCV9",
                op : 'o_rTQbMQ6z',
                s0: userType
            });
        });

        //点击播放-暂停
        var VideoBox = document.getElementById("VideoBox");
        var PlayBtn = $(".JS-playBtn");

        $(document).on("click", ".JS-clickPlayStop", function(){
            if (VideoBox.paused){
                VideoBox.play();
                PlayBtn.hide();
                recordVideo();
            }else{
                VideoBox.pause();
                PlayBtn.show();
            }

            YQ.voxLogs({
                module: "m_wsOhBCV9",
                op : 'o_ROxLdawn',
                s0: userType
            });
        });

        window.onbeforeunload = function(event){
            VideoBox.pause();
        };

        function recordVideo(){
            var isVideoEnd = setInterval(function(){
                if(VideoBox.ended){
                    PlayBtn.show();
                    clearInterval(isVideoEnd);
                }
            }, 1000);
        }

        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }

        if ( window['external'] && (window.external['shareMethod'] || window.external['shareInfo']) ) {
            //方法获取成功
        }else{
            $(".JS-clickShareBtn").hide();
        }

        //老师分享进入
        if(getQueryString('type') && getQueryString('type').indexOf('share') > -1){
            $(".JS-clickShareBtn").hide();
            $(".JS-downloadBox[data-type='"+ getQueryString('type') +"']").show();

            YQ.voxLogs({
                module: "m_wsOhBCV9",
                op : 'o_fmtcflvx',
                s1: getQueryString('type')
            });
            return false;
        }else{
            YQ.voxLogs({
                module: "m_wsOhBCV9",
                op : 'o_fmtcflvx',
                s0: userType
            });
        }
    }
</script>
</@layout.page>