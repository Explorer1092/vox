<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起教育科技母亲节微电影"
pageJs=['jquery', 'weui', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/teadayvideo/css/teaDay"]}
>

<div class="videoDetails">
    <div class="head-back">
        <img src="<@app.link href="public/skin/project/teadayvideo/images/motherdaybg.jpg"/>" width="100%">
    </div>
    <div class="inner-content">
        <div class="vd-header">
            <div class="logo"><a href="/"></a></div>
        </div>
        <div class="vd-video" style="margin-top:40%; border-radius:.3rem;">
        <#--play button-->
            <div class="playBtn JS-playBtn JS-clickPlayStop" style="z-index: 6; left: 0; top: 0; width: 100%; height: 100%; margin: 0; background-image: url(<@app.link href="public/skin/project/teadayvideo/images/motherday.jpg"/>)"></div>
            <video id="VideoBox" poster="" width="100%" height="100%" controls>
                <source src="http://v.17zuoye.cn/class/mothersday01.mp4" type="video/mp4">
            </video>
        </div>

        <div class="vd-info">
            <div class="tips">温馨提示：建议在WiFi环境下播放</div>
            <div class="text">小时候，妈妈在前方为我们搞定一切。长大了，妈妈成了我们背后最有力的支撑。她几乎了解你的一切，可是，她自己的梦想，你真的了解吗？</div>
            <div class="text">母亲节来临之际，一起教育科技邀请了4对不同年龄层的母子，基于他们各自的家庭故事，分别与他们进行了一场私密对话，聊了聊他们内心最深处的触动……</div>
            <div class="text">此前，一起教育科技在平台上发起了关于“妈妈的梦想”调研。最终的调研结果显示，妈妈与孩子之间的关心和了解，处于明显不对等的状态：有70%的妈妈能清楚地描绘出孩子的梦想，却只有33%的孩子知道妈妈的梦想是什么。巨大的数字差距背后，意味着什么？相信只要你与妈妈之间有过这样一次认真的对谈，你会找到自己的答案。</div>
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
        var track = getQueryString("track");
        YQ.voxLogs({
            module: "m_ndgjXjPA",
            op : 'o_ngrugoCX',
            s0 : track
        });
        var userType = ${(currentUser.userType)!'0'};

        //点击分享
        $(document).on("click", ".JS-clickShareBtn", function(){
            if (window['external'] && window.external['shareMethod']) {
                window.external.shareMethod(JSON.stringify({
                    title   : "《摔跤吧！爸爸》之中国版：梦想吧！妈妈",
                    content : "一起教育科技母亲节微电影",
                    url     : location.protocol + '//' + location.host + location.pathname + '?type=share_' + userType,
                    type    : "SHARE",
                    channel : 4,
                    moudle : "motherday"
                }));
            }else{
                if (window['external'] && window.external['shareInfo']) {
                    window.external.shareInfo(JSON.stringify({
                        title   : "《摔跤吧！爸爸》之中国版：梦想吧！妈妈",
                        content : "一起教育科技母亲节微电影",
                        url     : location.protocol + '//' + location.host + location.pathname + '?type=share_' + userType,
                        moudle : "motherday"
                    }));
                }else{
                    $.alert("分享失败!");
                }
            }

            YQ.voxLogs({
                module: "m_ndgjXjPA",
                op : 'o_wqeN9WAw',
                s0 : track
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
                module: "m_ndgjXjPA",
                op : 'o_0wBhjqGW',
                s0 : track
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
    }
</script>
</@layout.page>