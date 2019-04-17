<#import "../layout.ftl" as layout>
<@layout.page title="APP下载">
    <@sugar.capsule css=['chipsAll','chipsShareVideo'] />

<div class="unloadWrap">
    <div class="unloadMain">
        <p class="logoName">一起学</p>
        <p>下载一起学App进入薯条英语学习</p>
        <p class="introBtn">
            <span><a style="color: #4786F9;" href="/chips/center/downloadappintro.vpage">点我查看如何操作</a></span>
        </p>
        <#--<a href="http://wechat.17zuoye.com/j/jzt"><div class="unloadBtn">立即下载</div></a>-->
        <div class="unloadBtn" id="downloadBtn">立即下载</div>
    </div>
</div>

<!-- 弹窗 -->
<div class="sharePopup downloadapp" id="sharePopup" style="display: none;">
    <div class="shareInner"></div>
</div>

<script type="text/javascript">
    var u = navigator.userAgent;
    var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Linux') > -1; //g
    var isIOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端

    if(!isWeiXin()){
        if (isAndroid) {
            window.downloadBtn.onclick = function(){
                // window.location.href = 'a17parent://platform.open.api/parent_main?from=h5&type=nativePage&val=friesenglish';
                var iframe = document.createElement("iframe");
                iframe.style.cssText='display:none;width=0;height=0';
                document.body.appendChild(iframe);
                iframe.src = "a17parent://platform.open.api/parent_main?from=h5&type=nativePage&val=friesenglish";
                if(!isWeiXin()){
                    setTimeout(function(){
                        window.location.href = "http://wechat.17zuoye.com/j/jzt"
                    },1000)
                }
            }
        }
        if (isIOS) {
            window.downloadBtn.onclick = function(){
                // window.location.href = 'a17parent://jzt?yq_from=h5&yq_type=nativePage&yq_val=friesenglish';
                var iframe = document.createElement("iframe");
                iframe.style.cssText='display:none;width=0;height=0';
                document.body.appendChild(iframe);
                iframe.src = "a17parent://jzt?yq_from=h5&yq_type=nativePage&yq_val=friesenglish";
                if(!isWeiXin()){
                    setTimeout(function(){
                        window.location.href = "http://wechat.17zuoye.com/j/jzt"
                    },1000)
                }
            }
        }
    }else{
        window.downloadBtn.onclick = function(){
            window.sharePopup.style.display = "block"
        }
    }

    window.sharePopup.onclick = function(){
        window.sharePopup.style.display = "none"
    }

    function isWeiXin(){
        //window.navigator.userAgent属性包含了浏览器类型、版本、操作系统类型、浏览器引擎类型等信息，这个属性可以用来判断浏览器类型
        var ua = window.navigator.userAgent.toLowerCase();
        //通过正则表达式匹配ua中是否含有MicroMessenger字符串
        if(ua.match(/MicroMessenger/i) == 'micromessenger'){
            return true;
        }else{
            return false;
        }
    }

    function pageLog(){
        require(['logger'], function(logger) {
            // app下载页_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'downloadapp_load'
            })
        })
    }

</script>
</@layout.page>

<#--</@chipsIndex.page>-->
