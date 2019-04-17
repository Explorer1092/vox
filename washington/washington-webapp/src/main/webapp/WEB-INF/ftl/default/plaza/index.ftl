<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
fastClickFlag=false
pageJs=['jquery', 'YQ', 'flexSlider']
pageCssFile={"init" : ["public/skin/default/v5/css/pc-home"]}
>
<script type="text/javascript">
    (function(){
        if(getQueryString('ref') != "back"){
            var browser = {
                versions: function(){
                    var u = navigator.userAgent;
                    var isMobile = {//移动终端浏览器版本信息
                        mobile : !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
                        android: u.indexOf('Android') > -1, //android终端或者uc浏览器
                        isIOS : u.indexOf('iPhone') > -1 //是否为iPhone或者iPad
                    };

                    //移动端访问页面
                    if(isMobile.android || (isMobile.isIOS && isMobile.mobile)){
                        location.href = "/project/mobilegoin/index.vpage";
                    }
                }()
            };
        }

        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }
    }());
</script>
<div class="zy-header">
    <div class="zy-inner">
        <div class="zy-nav">
            <a href="/" class="logo"></a>
            <div class="navList">
                <a href="/index.vpage" class="menu active" title="首页">首页</a>
                <a href="/help/concept.vpage" class="menu" title="产品概念">产品概念</a>
                <a href="/help/uservoice.vpage" class="menu" title="各界声音">各界声音</a>
                <a href="/help/news/index.vpage" class="menu" title="新闻中心">新闻中心</a>
                <a href="/help/aboutus.vpage" class="menu" title="关于我们">关于我们</a>
                <a href="/help/jobs.vpage" class="menu" title="加入我们">加入我们</a>
            </div>
        </div>
        <div class="rightIn">
            <a href="/help/downloadApp.vpage?refrerer=pc" target="_blank" class="load"><i class="phone-icon"></i>APP下载</a>
        </div>
    </div>
</div>

<div class="zy-homeContainer" style="height: 100%;">
    <div class="JS-indexSwitch-main">
        <ul class="zy-homeBox slides">
            <li class="homeItem homeItem02">
                <div class="innerBox">
                    <div class="info">
                        <div class="title01" style="font-size: 54px">让学习成为美好体验</div>
                        <div class="loginBtn clearfix">
                            <div class="zy-header" style="position: static; display: inline-block; width: auto; float: right; padding-left: 20px;">
                                <div class="rightIn" style="display: inline-block; float: none; margin: 0;padding: 0;">
                                    <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc" target="_blank" class="load" style="padding: 0; border: none; float: none; width: auto; font-size: 16px; color: #000;"><i class="phone-icon"></i>APP下载</a>
                                </div>
                            </div>
                            <#if !(userinfo??)>
                                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=register" class="JS-register-main">注册</a>
                            </#if>
                            <#--<a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=login" class="active JS-login-main">登录</a>-->
                            <#--这里会导致本地开发有问题，通过后端链接走sso流程跳转ucenter-->
                            <a href="/tempLogin.vpage?ref=login" class="active JS-login-main">登录</a>
                        </div>
                    </div>
                </div>
            </li>

            <li class="homeItem homeItem03" >
                <div class="innerBox">
                    <div class="info">
                        <div style="padding-left: 20px;">
                            <div class="title02">一起作业新形象</div>
                            <div class="text">
                                <div>新学年，新开始: 一起作业更换了别具寓意的新logo以及视觉形象，我们将带你进入一段充满惊喜的学习旅程……</div>
                                <div class="more"><a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/news/newscontent_27.vpage" target="_blank">查看更多</a></div>
                            </div>
                        </div>
                        <div class="loginBtn clearfix">
                            <div class="zy-header" style="position: static; display: inline-block; width: auto; float: right; padding-left: 20px;">
                                <div class="rightIn" style="display: inline-block; float: none; margin: 0;padding: 0;">
                                    <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc" target="_blank" class="load" style="padding: 0; border: none; float: none; width: auto; font-size: 16px; color: #000;"><i class="phone-icon"></i>APP下载</a>
                                </div>
                            </div>
                            <#if !(userinfo??)>
                                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=register" class="JS-register-main">注册</a>
                            </#if>
                            <#--<a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=login" class="active JS-login-main">登录</a>-->
                            <#--这里会导致本地开发有问题，通过后端链接走sso流程跳转ucenter-->
                            <a href="/tempLogin.vpage?ref=login" class="active JS-login-main">登录</a>
                        </div>
                    </div>
                </div>
            </li>

            <li class="homeItem homeItem01" style="width: 100%">
                <div class="innerBox">
                    <div class="info">
                        <div class="title01" style="font-size: 54px">让学习成为美好体验</div>
                        <div class="loginBtn clearfix">
                            <div class="zy-header" style="position: static; display: inline-block; width: auto; float: right; padding-left: 20px;">
                                <div class="rightIn" style="display: inline-block; float: none; margin: 0;padding: 0;">
                                    <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc" target="_blank" class="load" style="padding: 0; border: none; float: none; width: auto; font-size: 16px; color: #000;"><i class="phone-icon"></i>APP下载</a>
                                </div>
                            </div>
                            <#if !(userinfo??)>
                                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=register" class="JS-register-main">注册</a>
                            </#if>
                            <#--<a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=login" class="active JS-login-main">登录</a>-->
                            <#--这里会导致本地开发有问题，通过后端链接走sso流程跳转ucenter-->
                            <a href="/tempLogin.vpage?ref=login" class="active JS-login-main">登录</a>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
    </div>
    <ul class="zy-scrollNav JS-indexSwitch-mode">
        <li style="left: -16px;"><a>1</a></li>
        <li><a>2</a></li>
        <li class="flex-active" style="right: -16px;"><a class="">3</a></li>
    </ul>
</div>

<div class="zy-homeFooter JS-indexPageBox">
    <div class="innerBox">
        <div class="left">
            <#--<div class="il-ppxd">
                <span class="il-md">兄弟品牌：</span>
                <a href="http://www.ustalk.com" target="_blank"><i class="zy-icon icon-ustalk"></i></a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc&count=2" target="_blank"><i class="zy-icon icon-parent"></i><span>家长通</span></a>
                &lt;#&ndash;<a href="http://kuailexue.com" target="_blank"><i class="zy-icon icon-kuailexue"></i><span>快乐学</span></a>&ndash;&gt;
            </div>-->
            <p class="text">${(layout.getWebInfo("copyright"))!}</p>
            <p class="text">${(layout.getWebInfo("icp"))!}</p>
        </div>
        <div class="right">
            <div class="cell">
                <a href="javascript:;" class="tag" style="cursor: default;">使用须知</a>
                <a href="${mainSiteBaseUrl}/help/serviceagreement.vpage" target="_blank"><i class="zy-icon icon-agree1"></i><span>用户协议</span></a>
                <a href="${mainSiteBaseUrl}/help/privacyprotection.vpage" target="_blank"><i class="zy-icon icon-agree2"></i><span>隐私保护</span></a>
            </div>
            <div class="cell">
                <a href="javascript:;" style="cursor: default;" class="tag">联系我们</a>
                <a href="/help/kf/index.vpage" target="_blank"><i
                        class="zy-icon icon-bzfk"></i><span>帮助反馈</span></a>
                <a href="javascript:;"><i class="zy-icon icon-tel"></i><span>${(layout.getWebInfo("tel"))!}</span></a>
            </div>
            <div class="cell">
                <a href="javascript:;" style="cursor: default;" class="tag">关注我们</a>
                <a href="http://weibo.com/yiqizuoye" target="_blank"><i class="zy-icon icon-xlwb"></i><span>新浪微博</span></a>
                <a href="javascript:;">
                    <i class="zy-icon icon-gfwx"></i><span>官方微信</span>
                    <div class="codeImg"></div>
                </a>
            </div>
        </div>
    </div>
</div>

<#--圣诞节-->
<div class="hallowmas-flayer JS-splashScreenImg" style="display: none; background:#e03033 url(<@app.link href='public/skin/default/v5/images/newyearBg.png'/>) center center">
    <div class="countBox">
        <div class="count" style="color: #ffc2c3; border-color: #ffc2c3 ; right: 50px; bottom: 100px;"><span class="JS-splashTime">3</span>秒</div>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function($, YQ){
        //登录首页
        $(".JS-indexSwitch-main").flexslider({
            animation: "slide",
            slideshow: true,
            slideshowSpeed: 5000,
            //startAt: 1,
            directionNav: false,
            animationLoop: true,
            manualControls: ".JS-indexSwitch-mode li",
            touch: true //是否支持触屏滑动
        });

        indexSwitch();

        $(window).resize(function () {
            indexSwitch();
        });

        function indexSwitch() {
            var _winHeight = $(window).height();

            if(_winHeight <= 600){
                $(".JS-indexSwitch-main li").height(600);
            }else{
                $(".JS-indexSwitch-main li").height( _winHeight );
            }
        }

        <#if (((.now gte "2016-12-24 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss")) && (.now lte "2017-01-03 24:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))) || ftlmacro.devTestSwitch)!false>
            if(!YQ.getCookie("newyear")){
                YQ.setCookie("newyear", '1', 1);

                $(".JS-splashScreenImg").show();
                var splashCount = 3;
                var timer = setInterval(function(){
                    splashCount--;

                    if(splashCount == 0){
                        $(".JS-splashScreenImg").hide();
                        clearInterval(timer);
                        return false;
                    }

                    $(".JS-splashTime").text(splashCount);
                },1000);
            }
        </#if>
    }
</script>
</@layout.page>