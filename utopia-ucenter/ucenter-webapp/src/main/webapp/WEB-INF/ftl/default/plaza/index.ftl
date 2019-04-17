<#import "module.ftl" as layout>
<@layout.page>
<#--<div class="zy-homeContainer JS-indexPageBox" style="height: 100%;">
    <div class="JS-indexSwitch-main">
        <ul class="zy-homeBox slides">
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
                                <a href="javascript:;" class="JS-register-main">注册</a>
                            </#if>
                            <a href="javascript:;" class="active JS-login-main">登录</a>
                        </div>
                    </div>
                </div>
            </li>
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
                            <a href="javascript:;" class="JS-register-main">注册</a>
                            </#if>
                            <a href="javascript:;" class="active JS-login-main">登录</a>
                        </div>
                    </div>
                </div>
            </li>

        </ul>
    </div>
    <ul class="zy-scrollNav JS-indexSwitch-mode">
        <#if (.now lt "2017-05-15 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
            <li style="left: -12px;"><a>1</a></li>
            <li style="right: 0;"><a>2</a></li>
            <li id="motherLi" style="left: 15px;"><a>3</a></li>
        <#else>
            <li style="left: -16px;"><a>1</a></li>
            <li style="right: -16px;"><a>2</a></li>
        </#if>
        &lt;#&ndash;<li class="flex-active" style="right: -16px;"><a class="">3</a></li>&ndash;&gt;
    </ul>
</div>-->

<div class="homepage-wrap minWidth JS-indexPageBox">
    <div class="education-dream-works">
        <!-- animate_wrap-2 低分辨率适配版 animate_box 动画类 static_box 静态类 -->
        <div class="animate_wrap animate_box" id="animateBox1">
            <div class="btn_box ">
                <a class="signIn JS-login-main" href="javascript:;">登录</a>
                <#if !(userinfo??)>
                <a class="register JS-register-main" href="javascript:;">注册</a>
                </#if>
            </div>
            <div class="fixed company_name textFedeIn">
                <p>一起教育科技</p>
                <p>智能教育“梦想工厂”</p>
            </div>
            <!-- 手机底板 movePlateTop -->
            <#--D:\17zuoyeGitWork\vox-17zuoye\utopia-ucenter\ucenter-webapp\src\main\webapp\public\skin\default\v5\images-->
            <#--<img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-011.png'/>">-->

            <div class="fixed phone-box-1 movePlateBtm"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-011.png'/>" alt=""></div>
            <div class="fixed model_1 shadowSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-06.png'/>" alt=""></div>
            <div class="fixed model_2 movePlateTop"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-03.png'/>" alt=""></div>
            <div class="fixed model_3 floorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-04.png'/>" alt=""></div>
            <div class="fixed model_4 bfloorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-07.png'/>" alt=""></div>
            <div class="fixed model_5 bookSlideOut">
                <img class=" " src="<@app.link href='public/skin/default/v5/images/animatePic/bg-10.png'/>" alt="">
                <div class="ladder stairSwing"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-09.png'/>" alt=""></div>
                <div class="board_1 ladderFadeIn_1"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-15.png'/>" alt=""></div>
                <div class="board_2 ladderFadeIn_2"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-16.png'/>" alt=""></div>
                <div class="board_3 ladderFadeIn_3"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-16.png'/>" alt=""></div>
                <div class="people_1 peopleSlideR"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-17.png'/>" alt=""></div>
                <div class="screen_1 phoneFadeIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-18.png'/>" alt=""></div>
            </div>
            <div class="fixed model_6 rearFloorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-05.png'/>" alt=""></div>
            <div class="fixed model_8 coluSlideUp">
                <img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-08.png'/>" alt="">
                <div class="aperture_box">
                    <img class="aperture-1 apertSlideUp_1" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-29.png'/>" alt="">
                    <img class="aperture-2 apertSlideUp_2" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-29.png'/>" alt="">
                </div>
            </div>
            <div class="fixed model_9  rabbitFadeIn "><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-19.png'/>" alt=""></div>
            <!-- sfloorSlideUp -->
            <div class="fixed model_10 sfloorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-14.png'/>" alt=""></div>
            <div class="fixed model_11">
                <img class="beamFadeIn-2" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-13.png'/>" alt="">
                <div class="chassis_box tableFadeIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-26.png'/>" alt=""></div>
                <div class="beam_box beamFadeIn-1"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-12.png'/>" alt=""></div>
                <div class="phone_box phoneSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-11.png'/>" alt=""></div>
            </div>
            <!-- streetFedeIn_1 -->
            <div class="fixed model_12 streetFedeIn_1"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-24.png'/>" alt=""></div>
            <div class="fixed model_13 streetLightSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-20.png'/>" alt=""></div>
            <div class="fixed model_14 streetFedeIn_2"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-23.png'/>" alt=""></div>
            <div class="fixed model_15 peopleFedaIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-22.png'/>" alt=""></div>
            <div class="fixed model_16 airshipFloat">
                <img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-25.png'/>" alt="">
                <div class="beam_2 lightFadeIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-27.png'/>" alt=""></div>
            </div>
            <div class="fixed model_17 shadowSlideUp_2"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-28.png'/>" alt=""></div>
        </div>
        <!-- animate_wrap-2 低分辨率适配版 animate_box 动画类 static_box 静态类 -->
        <div class="animate_wrap-2 animate_box" id="animateBox2">
            <div class="btn_box ">
                <a class="signIn JS-login-main" href="javascript:;">登录</a>
                <#if !(userinfo??)>
                    <a class="register JS-register-main" href="javascript:;">注册</a>
                </#if>
            </div>
            <div class="fixed company_name textFedeIn">
                <p>一起教育科技</p>
                <p>智能教育“梦想工厂”</p>
            </div>
            <!-- 手机底板 s-movePlateBtm -->
            <div class="fixed phone-box-1 s-movePlateBtm"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-011.png'/>" alt=""></div>
            <div class="fixed model_1 s-shadowSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-06.png'/>" alt=""></div>
            <div class="fixed model_2 s-movePlateTop"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-03.png'/>" alt=""></div>
            <div class="fixed model_3 s-floorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-04.png'/>" alt=""></div>
            <div class="fixed model_4 s-bfloorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-07.png'/>" alt=""></div>
            <div class="fixed model_5 s-bookSlideOut">
                <img class="" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-10.png'/>" alt="">
                <div class="ladder s-stairSwing"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-09.png'/>" alt=""></div>
                <div class="board_1 s-ladderFadeIn_1"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-15.png'/>" alt=""></div>
                <div class="board_2 s-ladderFadeIn_2"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-16.png'/>" alt=""></div>
                <div class="board_3 s-ladderFadeIn_3"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-16.png'/>" alt=""></div>
                <div class="people_1 s-peopleSlideR"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-17.png'/>" alt=""></div>
                <div class="screen_1 s-phoneFadeIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-18.png'/>" alt=""></div>
            </div>
            <div class="fixed model_6 s-rearFloorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-05.png'/>" alt=""></div>
            <!--<div class="fixed model_7"><img src="images/animatePic/bg-05.png" alt=""></div>-->
            <div class="fixed model_8 s-coluSlideUp">
                <img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-08.png'/>" alt="">
                <div class="aperture_box">
                    <img class="aperture-1 s-apertSlideUp_1" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-29.png'/>" alt="">
                    <img class="aperture-2 s-apertSlideUp_2" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-29.png'/>" alt="">
                </div>
            </div>
            <div class="fixed model_9  s-rabbitFadeIn "><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-19.png'/>" alt=""></div>
            <!-- sfloorSlideUp -->
            <div class="fixed model_10 s-sfloorSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-14.png'/>" alt=""></div>
            <div class="fixed model_11">
                <img class="s-beamFadeIn-2" src="<@app.link href='public/skin/default/v5/images/animatePic/bg-13.png'/>" alt="">
                <div class="chassis_box s-tableFadeIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-26.png'/>" alt=""></div>
                <div class="beam_box s-beamFadeIn-1"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-12.png'/>" alt=""></div>
                <div class="phone_box s-phoneSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-11.png'/>" alt=""></div>
            </div>
            <!-- streetFedeIn_1 -->
            <div class="fixed model_12 s-streetFedeIn_1"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-24.png'/>" alt=""></div>
            <div class="fixed model_13 s-streetLightSlideUp"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-20.png'/>" alt=""></div>
            <div class="fixed model_14 s-streetFedeIn_2"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-23.png'/>" alt=""></div>
            <div class="fixed model_15 s-peopleFedaIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-22.png'/>" alt=""></div>
            <div class="fixed model_16 s-airshipFloat">
                <img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-25.png'/>" alt="">
                <div class="beam_2 s-lightFadeIn"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-27.png'/>" alt=""></div>
            </div>
            <div class="fixed model_17 s-shadowSlideUp_2"><img src="<@app.link href='public/skin/default/v5/images/animatePic/bg-28.png'/>" alt=""></div>
        </div>
    </div>
    <div class="part-wrapper">
        <div class="text-box" id="textBox">
            <h2 class=" ">关于我们</h2>
            <div class="describe">
                <!--<p>About Us</p>-->
                <!--<p>17 Education & Technology Group Inc.</p>-->
                <i class="line-blue"></i>
            </div>
            <p class="text">科技中有温度，数据里有梦想。七年来，一起人怀着“让学习成为美好体验”的使命，</p>
            <p class="text">为K12阶段学生打造学校、家庭、社会教育场景下的智能教育平台，助力更美好的教育。</p>
        </div>
        <ul class="about-us-list">
            <li class="item zuoye-17">
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/primaryschool.vpage"">
                    <div class=" pic-1"><img src="<@app.link href='public/skin/default/v5/images/pic_3-1.png'/>" alt=""></div>
                    <p class="md-title">一起小学/一起中学</p>
                    <p class="line"></p>
                    <div class="describe-box">
                        <p>大数据驱动学校教育平台</p>
                    </div>
                    <div class="know-more"><span >了解更多 &gt;</span></div>
                </a>
            </li>
            <li class="item study-17">
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/togetherlearn.vpage">
                    <div class=" pic-2"><img src="<@app.link href='public/skin/default/v5/images/pic_2_1.png'/>" alt=""></div>
                    <p class="md-title">一起学</p>
                    <p class="line"></p>
                    <div class="describe-box">
                        <p>个性化学习为目标的家庭教育平台</p>
                    </div>
                    <div class="know-more"><span >了解更多 &gt;</span></div>
                </a>
            </li>
            <li class="item publicw-17">
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/publicwelfare.vpage">
                    <div class="pic-3"><img src="<@app.link href='public/skin/default/v5/images/pic_3.png'/>" alt=""></div>
                    <p class="md-title">一起公益</p>
                    <p class="line"></p>
                    <div class="describe-box">
                        <p>链接公益力量的社会教育平台</p>
                    </div>
                    <div class="know-more"><span >了解更多 &gt;</span></div>
                </a>
            </li>
        </ul>
        <div class="text-box" style="display: none">
            <h2>我们的故事</h2>
            <div class="describe">
                <!--<p>Globle Leading K12 Smart Education Paltform</p>-->
                <i class="line-red"></i>
            </div>
            <p class="text">通过Socrates智能学习系统，一起教育科技链接教育与科技，为青少年加速培养可持续性的竞争力和学习力，平等享受高质量的教育内容。</p>
        </div style=>
        <div class=" video-pic" style="display: none;">
            <span class="play-btn"></span>
            <img src="<@app.link href='public/skin/default/v5/images/video.png'/>" alt="">
        </div>
        <div class="text-box">
            <h2>加入我们</h2>
            <div class="describe">
                <!--<p>Join Us</p>-->
                <i class="line-yellow"></i>
            </div>
            <p class="text">加入一起教育科技，完成我们共同的使命——“让学习成为美好体验”</p>
            <p class="text">加入一起教育科技，共同改变中国乃至世界教育！</p>
        </div>
        <ul class="join-us-list">
            <li class="item">
                <div class="pic"><img class="pic1" src="<@app.link href='public/skin/default/v5/images/join-us-1.png'/>" alt=""></div>
                <div class="zhaopin"><a class="btn" href="https://app.mokahr.com/apply/17zuoye">社会招聘</a></div>
            </li>
            <li class="item">
                <div class="pic pic2"><img src="<@app.link href='public/skin/default/v5/images/join-us-2.png'/>" alt=""></div>
                <div class="zhaopin"><a class="btn" href="http://app.mokahr.com/campus_apply/17zuoye">实习生招聘</a></div>
            </li>
            <li class="item">
                <div class="pic "><img class="pic3" src="<@app.link href='public/skin/default/v5/images/join-us-3.png'/>" alt=""></div>
                <div class="zhaopin"><a class="btn" href="https://app.mokahr.com/campus_apply/17zuoye">校内招聘</a></div>
            </li>
        </ul>

    </div>
</div>


<#--RenderMain-->
<div id="RenderMain" class="JS-indexPageBox" style="position: relative;z-index: 30"></div>
<#if ((((.now gte "2017-11-23 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss")) && (.now lte "2017-11-23 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss"))) || ftlmacro.devTestSwitch)!false) >
    <#assign activityName = 'thanks'/>
<#--闪屏-->
<#--<div class="hallowmas-flayer" id="JS-splashScreenImg" style="display: none; background: url(<@app.link href='public/skin/default/v5/images/'+ activityName +'-home-back.jpg'/>) center center">
    <div class="countBox">
        <div class="count" style="color: #fff; border-color: #fff; opacity: 0.7; right: 0px; bottom: 100px;"><span class="JS-splashTime" id="JS-splashTime">3</span>秒</div>
    </div>
</div>-->

<script type="text/javascript">
var html = document.querySelector('html'),
    body = document.querySelector('body');

    if(!$17.getCookieWithDefault("${activityName!'attivi'}")){
        var eBox = document.getElementById("JS-splashScreenImg");
        // eBox.style.display = "block";
        $17.setCookieOneDay("${activityName!'attivi'}", 1);
        /*eBox.onclick = function(){
            eBox.style.display="none";
            html.style.overflow = 'initial';
            body.style.overflow = 'initial';
        };*/
        var timer = setInterval(function(){
            var num;
            var eTime = document.getElementById("JS-splashTime");
            num = parseInt(eTime.innerText);
            if(num>0){
                num--;
                eTime.innerText = num;
            }else{
                // eBox.style.display="none";
                html.style.overflow = 'initial';
                body.style.overflow = 'initial';
                clearInterval(timer);
            }
        },1000);
        /* 禁止滾動 */
        html.style.overflow = 'hidden';
        body.style.overflow = 'hidden';
    }

</script>
</#if>

<#--登录模板-->
<#include "login_main.ftl"/>
<#include "forgetPopup.ftl"/>

<#--注册模块-->
<#if !(userinfo??)>
    <#include "register_main.ftl"/>
    <#include "register_next.ftl"/>
</#if>

<#--神算子来源用户弹窗提示-->
<#include "ssztip.ftl" />
</@layout.page>