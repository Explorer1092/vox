<#import "../layout.ftl" as thanksgiving>
<@thanksgiving.page title="UStalk" pageJs="hot">
    <@sugar.capsule css=['swiper3','ustalk','jbox'] />
<style>
    html, body {
        position: relative;
        height: 100%;
    }

    body {
        background: #eee;
        font-family: Helvetica Neue, Helvetica, Arial, sans-serif;
        font-size: 14px;
        color: #000;
        margin: 0;
        padding: 0;
    }

    .swiper-container {
        width: 100%;
        height: 100%;
    }

    .swiper-slide {
        text-align: center;
        font-size: 18px;
        background: #fff;
        /* Center slide text vertically */
        display: -webkit-box;
        display: -ms-flexbox;
        display: -webkit-flex;
        display: flex;
        -webkit-box-pack: center;
        -ms-flex-pack: center;
        -webkit-justify-content: center;
        justify-content: center;
        -webkit-box-align: center;
        -ms-flex-align: center;
        -webkit-align-items: center;
        align-items: center;
    }
    .jBox-container{
        font-size: 28px;
        padding: 20px;
    }
</style>

<div class="swiper-container">
    <div class="swiper-wrapper">
        <div class="swiper-slide">
            <div class="lc-page1">
                <h1 class="logo">UStalk</h1>
                <span class="slogan"></span>

                <div class="title">
                    <img src="<@app.link href="public/images/parent/activity/ustalk/p1-tit.jpg"></@app.link>" alt="">
                </div>
            </div>
        </div>
        <div class="swiper-slide">
            <div class="lc-page2">
                <h1 class="logo">UStalk</h1>
                <div class="title" id="title"></div>
                <canvas class="c1" id="c1" width="100" height="100"></canvas>
            </div>
        </div>
        <div class="swiper-slide">
            <div class="lc-page3" id="page3">
                <h1 class="logo">UStalk</h1>
                <div class="title"></div>
                <div class="tag-row clearfix">
                    <span class="tag first">了解问题，辅导帮助</span>
                </div>
                <div class="tag-row clearfix">
                    <span class="tag second">发现喜好，激发动力</span>
                </div>
                <div class="tag-row clearfix">
                    <span class="tag third">了解性格，历练短板</span>
                </div>
                <div class="tag-row clearfix">
                    <span class="tag fourth">直面问题，配合提升</span>
                </div>
                <a href="javascript: void(0);" class="evaluation" onclick="swiper.slideNext()">领取测评</a>
            </div>
        </div>
        <div class="swiper-slide">
            <div class="lc-page4">
                <header class="header">
                    <h1 class="logo">UStalk</h1>

                    <div class="title"></div>
                </header>
                <section class="select">
                    <span class="border top"></span>
                    <span class="border bottom"></span>

                    <div class="select-cont">
                        <p class="select-tit">选择需要参加测评的孩子</p>
                        <ul class="avatar-box clearfix">
                            <#if students?? && students?size gt 0>
                                <#list students as student>
                                    <li class="avatar-list <#if student_index == 0>on</#if>" data-sid="${student.id!0}" data-sname="${student.name!''}" style="background:url(<@app.avatar href="${student.img!}"></@app.avatar>) no-repeat 0 0; background-size: 100% 100%;">
                                        <i class="horn"></i>
                                        <#--<img href="<@app.avatar href="${student.img!}"></@app.avatar>">-->
                                        <span class="avatar-name">${student.name?has_content?string('${student.name}','${student.id!0}')}</span>
                                    </li>
                                </#list>
                            </#if>
                        </ul>
                    </div>
                </section>
                <section class="vali-box">
                    <p class="vali-tit">请确认孩子家长的手机号</p>

                    <div class="vali-row">
                        <input id="pmobile" type="tel" maxlength="11" class="vali-tel" value="${authenticatedMobile!''}"/>
                    </div>
                <div class="vali-row clearfix">
                <input type="text" id="verifycode" class="vali-num" />
                <#--<input class="num-btn" id="sendverifycode" value="获取验证码">-->
                <a id="sendverifycode" class="num-btn" href="javascript:void (0);"><span>获取验证码</span></a>
                </div>
                </section>
                <input type="button" class="submit-btn btn-disable" value="提交"/>
            </div>
        </div>
    </div>
    <!-- Add Pagination -->
    <div class="swiper-pagination"></div>
</div>

<div class="alert-box" style="display: none;">
    <div class="opabox"></div>
    <div class="cont-box">
        <div class="result-status">
            <i class="result-icon false"></i>
        </div>
        <p class="result-tit">申领成功</p>
        <p class="result-txt">顾问老师将在24小时内为您安排测评，<br>请保持电话畅通</p>
        <button class="close-btn" onclick="this.parentNode.parentNode.style.display = 'none';">确定</button>
    </div>
</div>

<script>
</script>
</@thanksgiving.page>

