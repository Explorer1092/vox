<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业,一起成长"
pageJs=['jquery', 'weui', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/fiveyears/css/skin"]}
>
<div class="videoDetails">
    <div class="head-back">
        <img src="<@app.link href="/public/skin/project/fiveyears/images/trailer-bg.jpg"></@app.link>" width="100%">
    </div>
    <div class="inner-content">
        <div class="vd-header">
            <div class="logo"><a href="javascript:void(0)"></a></div>
        </div>
        <div class="vd-video JS-clickPlayStop">
            <!--视频-->
            <video class="video" id="videoBox" poster="<@app.link href="/public/skin/project/fiveyears/images/trailer-video.png?1.0.1"></@app.link>" width="100%" height="100%" controls preload>
                <source src="http://v.17zuoye.cn/corp/5years.mp4" type="video/mp4">
            </video>
            <#--<img src="/public/skin/project/fiveyears/images/trailer-video.png" width="100%">-->
        </div>
        <div class="vd-info">
            <div class="tips">温馨提示：建议在WiFi环境下播放</div>
            <div class="text">一起作业五岁了！从第一份中小学在线作业，到全球领先的K12在线教育平台，一颗初心，五年坚持，2500万用户的信任。下个五年，继续陪你一起成长。</div>

        </div>
        <div class="vd-btn">
            <a href="javascript:void(0)" class="shareBtn JS-clickShareBtn">点击分享</a>
        </div>
    </div>
</div>
<script>
    signRunScript = function () {
        var userType = ${(currentUser.userType)!'0'};
        var isFromTeacherApp = window.navigator.userAgent.toLowerCase().indexOf("17teacher") > -1;
        var isFromParentApp = window.navigator.userAgent.toLowerCase().indexOf("17parent") > -1;
        var isFromStudentApp = window.navigator.userAgent.toLowerCase().indexOf("17student") > -1;
        var logState = '';
        if (isFromTeacherApp) {
            logState = 'app-teacher';
        } else if (isFromParentApp) {
            logState = 'parent';
        } else if (isFromStudentApp) {
            logState = 'normal';
        } else {
            logState = 'wechat_logs';
        }

        //点击分享
        $(document).on("click", ".JS-clickShareBtn", function () {
            if (window.external && window.external['shareMethod']) {
                window.external.shareMethod(JSON.stringify({
                    title: "一起作业五周年视频：一起成长",
                    content: "用着用着颜值就上去了，这个教学神器我给满分！",
                    url: location.href + '?type=share_' + userType,
                    type: "SHARE",
                    channel: 4
                }));
            } else {
                if (window['external'] && window.external['shareInfo']) {
                    window.external.shareInfo(JSON.stringify({
                        title: "一起作业五周年视频：一起成长",
                        content: "用着用着颜值就上去了，这个教学神器我给满分！",
                        url: location.href + '?type=share_' + userType
                    }));
                } else {
                    $.alert("分享失败!");
                }
            }
            YQ.voxLogs({
                database: logState,
                module: "m_01YVMhVK",
                op: 'o_0UaY9vGx',
                s0: userType
            });
        });

        //点击播放-暂停
        var VideoBox = document.getElementById("videoBox");
        var PlayBtn = $(".JS-playBtn");

        $(document).on("click", ".JS-clickPlayStop", function () {
            if (VideoBox.paused) {
                VideoBox.play();
                PlayBtn.hide();
                recordVideo();
            } else {
                VideoBox.pause();
                PlayBtn.show();
            }

            YQ.voxLogs({
                database: logState,
                module: "m_01YVMhVK",
                op: 'o_LRgWm9eG',
                s0: userType
            });
        });

        window.onbeforeunload = function (event) {
            VideoBox.pause();
        };

        function recordVideo() {
            var isVideoEnd = setInterval(function () {
                if (VideoBox.ended) {
                    PlayBtn.show();
                    clearInterval(isVideoEnd);
                }
            }, 1000);
        }

        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        }

        if (window['external'] && (window.external['shareMethod'] || window.external['shareInfo'])) {
            //方法获取成功
        } else {
            $(".JS-clickShareBtn").hide();
        }

        YQ.voxLogs({
            database: logState,
            module: "m_01YVMhVK",
            op: 'o_fKT6XPcj',
            s0: userType
        });
    }
</script>

</@layout.page>