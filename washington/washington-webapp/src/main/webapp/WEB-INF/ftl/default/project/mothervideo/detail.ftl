<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业母亲节微电影"
pageJs=['jquery', 'weui', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/teadayvideo/css/teaDay-pc"]}
>
<div class="videoDetails motherDetails">
    <div class="inner-content">
        <div class="vd-header">
            <div class="logo"><a href="/"></a></div>
            <div class="title"><img src="<@app.link href="public/skin/project/teadayvideo/images/detailfont.png"/>"/> </div>
        </div>
        <div class="vd-video">
            <div class="JS-noflash" style="display:none; text-align:center;line-height:483px;color:#999;">您的浏览器没有安装flash播放器!</div>
            <div class="JS-clickPlayStop" style="overflow: visible;display:none;">
                <embed width="856" height="483"
                       flashvars="file=//v.17zuoye.cn/class/mothersday01.mp4&amp;image=<@app.link href="public/skin/project/teadayvideo/images/modetail.jpg"/>&amp;width=856&amp;height=483"
                       allowfullscreen="true" quality="high" name="single"
                       src="//www.17zuoye.com/static/video/flvplayer.swf"
                       type="application/x-shockwave-flash"></embed>
            </div>
        </div>
        <div class="vd-info">
            <div class="text">小时候，妈妈在前方为我们搞定一切。长大了，妈妈成了我们背后最有力的支撑。她几乎了解你的一切，可是，她自己的梦想，你真的了解吗？</div>
            <div class="text">母亲节来临之际，一起作业邀请了4对不同年龄层的母子，基于他们各自的家庭故事，分别与他们进行了一场私密对话，聊了聊他们内心最深处的触动……</div>
            <div class="text">此前，一起作业在平台上发起了关于“妈妈的梦想”调研。最终的调研结果显示，妈妈与孩子之间的关心和了解，处于明显不对等的状态：有70%的妈妈能清楚地描绘出孩子的梦想，却只有33%的孩子知道妈妈的梦想是什么。巨大的数字差距背后，意味着什么？相信只要你与妈妈之间有过这样一次认真的对谈，你会找到自己的答案。</div>
        </div>
    </div>
</div>
<script type="text/javascript">
    signRunScript = function () {
        function getBrowser() {
            var ua = window.navigator.userAgent;
            var isFirefox = ua.indexOf("Firefox") != -1;
            if (isFirefox) {
                var swf = navigator.plugins['Shockwave Flash'];
                if(!swf) {
                    $(".JS-noflash").show();
                    $(".JS-clickPlayStop").hide();
                }else{
                    $(".JS-noflash").hide();
                    $(".JS-clickPlayStop").show();
                }
            }else{
                $(".JS-noflash").hide();
                $(".JS-clickPlayStop").show();
            }
        }
        getBrowser();
        YQ.voxLogs({
            module: "m_W7HF1k2R",
            op : 'o_DSpT8Hua',
            s0 : getQueryString('track')
        });
        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }
    }
</script>
</@layout.page>