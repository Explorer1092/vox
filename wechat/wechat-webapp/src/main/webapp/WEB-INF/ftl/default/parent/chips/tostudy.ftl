<#import "../layout.ftl" as layout>
<@layout.page title="今日学习开启" pageJs="">
    <@sugar.capsule css=['chipsAll'] />
<style>
    .beginWrap {
        -webkit-background-size: contain;
        background-size: contain;
    }
    .beginWrap .beginBtn{
        display: none;
    }
    .beginWrap-web .beginBtn{
        display: block;
    }
</style>
<div class="beginWrap">
    <#--<a href="a17parent://jzt?yq_from=h5&yq_type=nativePage&yq_val=friesenglish"><div class="beginBtn" id="beginBtn">立即学习</div></a>-->

    <!-- 在浏览器中打开后添加类名 beginWrap-web -->
    <div class="beginWrap" id="box">
        <div class="beginBtn" id="beginBtn">立即学习</div>
    </div>
</div>

<script type="text/javascript">
    var u = navigator.userAgent;
    var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Linux') > -1; //g
    var isIOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
    if (isAndroid) {
        window.beginBtn.onclick = function(){
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
        window.beginBtn.onclick = function(){
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

    if(!isWeiXin()){
        var className = 'beginWrap-web';
        window.box.classList.add(className)
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

</script>

</@layout.page>



