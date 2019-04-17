<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="2016年教学手札"
pageJs=['jquery', 'flexSlider', 'weui', 'template', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/2016report/teacher/css/skin"]}
>

<#if report??>
<style>
    .anr-list li.active{ color: #fff; background-color: #ee6170;}
</style>
<a href="javascript:void(0);" class="play_btn JS-clickAudio" style="cursor: pointer;"></a><!--close_btn关闭音乐-->

<div class="JS-uservoice-banner">
    <ul class="slides">
        <li class="children">
            <!--首页-->
            <div class="annualReport-box">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_01.jpg"/>">
                <div class="anr-info textLeft textTopDif">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_01.png"/>" style="width: 7.175rem;">
                </div>
                <a href="javascript:void(0);" class="start_btn JS-flex-next"></a>
                <div class="anr-time">数据截止2016年12月30日</div>
            </div>
        </li>

        <#if (report.diffToday)?has_content>
        <li class="children">
            <!--相遇-->
            <div class="annualReport-box">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_02_01.jpg"/>" style="height:16rem;">
                <div class="anr-info">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/title_02.png"/>" style="width: 3.925rem;">
                    <div class="anr-title">
                        <p><span class="num">${(report.createDate)?split('-')[0]}</span> 年 <span class="num">${(report.createDate)?split('-')[1]}</span> 月 <span class="num">${(report.createDate)?split('-')[2]}</span> 日</p>
                        <p>我遇到了你</p>
                        <p>一路相伴 <span class="num">${(report.diffToday)!'---'}</span> 天</p>
                    </div>
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_02.png"/>" style="width: 5.55rem;">
                </div>
                <div class="anr-foot">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_02_02.jpg"/>">
                    <a href="javascript:void(0);" class="arrowDown_btn"></a>
                </div>
            </div>
        </li>
        </#if>

        <#if (report.comment)?has_content>
        <li class="children">
            <!--沟通-->
            <div class="annualReport-box">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_03_01.jpg"/>" style="height: 14rem;">
                <div class="anr-info textLeft">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_03.png"/>" style="width: 7.05rem;">
                    <div class="anr-title">
                        <p>我的第一条评语，诞生于</p>
                        <p><span class="num">${(report.commentTime)?split('-')[0]}</span> 年 <span class="num">${(report.commentTime)?split('-')[1]}</span> 月 <span class="num">${((report.commentTime)?split('-')[2])?substring(0, 2)}</span> 日</p>
                    </div>
                </div>
                <div class="anr-column">
                    <div class="colImages"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" style="border-radius: 100%;"/></div>
                    <div class="colTime">${(report.commentTime)!'---'}</div>
                    <div class="colComment">${(report.comment)!'---'}</div>
                </div>
                <div class="anr-foot">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_03_02.jpg"/>">
                    <a href="javascript:void(0);" class="arrowDown_btn"></a>
                </div>
            </div>
        </li>
        </#if>
        <#if (report.teacherBean)?has_content>
        <li class="children">
            <!--鼓励-->
            <div class="annualReport-box">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_04.jpg"/>" style="width: 16rem;">
                <div class="anr-info textDown">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_04.png"/>" style="width: 5.5rem;">
                    <div class="anr-title">
                        <p>这一年，我为孩子们发出了</p>
                        <p><span class="num">${(report.teacherBean)!'---'}</span> 个学豆</p>
                    </div>
                </div>
                <div class="anr-foot">
                    <a href="javascript:void(0);" class="arrowDown_btn"></a>
                </div>
            </div>
        </li>
        </#if>
        <#if (report.chartJson)?? && (report.chartJson)?has_content>
        <li class="children">
            <!--成绩-->
            <div class="annualReport-box bg5">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_05.jpg"/>">
                <div class="anr-info textLeft">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/title_05.png"/>" style="width: 4rem;">
                    <div class="anr-title">
                        <p>本学期，正确率分布情况如下</p>
                    </div>
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_05.png"/>" style="width: 7.825rem;">
                </div>
                <div class="anr-main">
                    <ul class="JS-score">
                        <li class="s-1" style="height:1%;" data-info="≤60%">
                            <div class="peo"><span class="num">0</span>人</div>
                        </li>
                        <li class="s-2" style="height:1%;" data-info="60%-69%">
                            <div class="peo"><span class="num">0</span>人</div>
                        </li>
                        <li class="s-3" style="height:1%;" data-info="70%-79%">
                            <div class="peo"><span class="num">0</span>人</div>
                        </li>
                        <li class="s-4" style="height:1%;" data-info="80%-89%">
                            <div class="peo"><span class="num">0</span>人</div>
                        </li>
                        <li class="s-5" style="height:1%;" data-info="≥90%">
                            <div class="peo"><span class="num">0</span>人</div>
                        </li>
                    </ul>
                    <div class="mfoot">
                        <div class="percent">60%以下</div>
                        <div class="percent">60-69%</div>
                        <div class="percent">70-79%</div>
                        <div class="percent">80-89%</div>
                        <div class="percent">90%以上</div>
                    </div>
                </div>
                <div class="anr-foot">
                    <a href="javascript:void(0);" class="arrowDown_btn"></a>
                </div>
            </div>
        </li>
        </#if>
        <#if (report.saveMins)?has_content>
        <li class="children">
            <!--改变-->
            <div class="annualReport-box">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_06.jpg"/>">
                <div class="anr-info textDown textLeft">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_06.png"/>" style="width: 7.825rem;">
                    <div class="anr-title">
                        <p> 线上作业自动批改，</p>
                        <p>我节省了<span class="num"> ${(report.homeworkNum90)!'---'} </span>份作业的批改时间。</p>
                    </div>
                </div>
                <div class="anr-foot">
                    <a href="javascript:void(0);" class="arrowDown_btn"></a>
                </div>
            </div>
        </li>
        </#if>
        <li class="children">
            <!--成就-->
            <div class="annualReport-box">
                <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_07.jpg"/>">
                <div class="anr-info textLeft">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/title_07.png"/>" style="width: 4rem;">
                    <div class="anr-title">
                        <p>我有 ${(report.studentNum)!0} 名学生完成作业，</p>
                        <p>超过了全国<span class="num"> ${((report.ranking*100)?int)!'---'}% </span>的老师。</p>
                    </div>
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/info_07.png"/>" style="width: 7.825rem;">
                </div>
                <div class="anr-side">
                    <#if (report.achievementName == "潜力之星")!false>
                        <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/arrow_01.jpg"/>"><!--潜力之星-->
                    </#if>
                    <#if (report.achievementName == "魅力达人")!false>
                        <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/arrow_02.jpg"/>"><!--魅力达人-->
                    </#if>
                    <#if (report.achievementName == "时尚名师")!false>
                        <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/arrow_03.jpg"/>"><!--时尚名师-->
                    </#if>
                </div>
                <div class="anr-foot">
                    <a href="javascript:void(0);" class="arrowDown_btn"></a>
                </div>
            </div>
        </li>
        <li class="children" data-type="reward_page">
            <!--感恩-->
            <div id="successContent">
                <div class="annualReport-box">
                    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_08.jpg"/>">
                    <div class="anr-list" style=" width: 100%; margin: 0;">
                        <ul class="JS-selectReceive" style="position: relative;    margin: 0 2.25rem;">
                            <li>孩子们快乐成长</li>
                            <li style="position: absolute; top:2.2rem; right: 0; white-space: nowrap; opacity: .8">学生都能学有所获</li>
                            <li style=" position: absolute;top: 0; right: 0;">成为优秀教师</li>
                            <li style="position: absolute; top: 4.5rem; left: 3rem; white-space: nowrap; opacity: 0.6">打造高效、乐学课堂</li>
                            <li style="position: absolute;  top:2.2rem; left: 0; opacity: .8">做学生的好朋友</li>
                        </ul>
                    </div>
                    <div class="anr-foot" style="bottom: .8rem;">
                        <a href="javascript:void(0);" class="receive_btn JS-receiveReward"></a>
                    </div>
                </div>
            </div>
        </li>
    </ul>
</div>
<script type="text/html" id="T:领取结果">
    <div class="annualReport-box"">
        <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_09.jpg"/>">
        <div class="anr-content"><%=info%></div>
        <div class="anr-foot" style="bottom: .8rem;">
            <%if(!isWeChat){%>
            <#if ((report.studentNum gt 8)!false) && (currentUser.profile.realname)?has_content>
            <a href="javascript:void(0);" class="receive_btn share_btn JS-share"></a>
            </#if>
            <%}%>
            <a href="javascript:void(0);" class="receive_btn see_btn JS-see"></a>
        </div>
    </div>
</script>

<a href="javascript:void(0);" class="arrowDown_btn JS-flex-next" data-type='next' style="display: none;"></a>
<script type="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script type="text/javascript">
    signRunScript = function ($) {
        setModeHeight(".JS-uservoice-banner li.children", 320);

        $(".JS-uservoice-banner").flexslider({
            animation: "slide",
            direction: "vertical",
            slideshow: false,
            slideshowSpeed: 3000,
            directionNav: false,
            animationLoop: false,
            controlNav: false,
            start: function (slider) {
                $('.JS-flex-previous').on("click", function () {
                    slider.flexAnimate(slider.getTarget("previous"), true);
                });

                $('.JS-flex-next').on("click", function () {
                    slider.flexAnimate(slider.getTarget("next"), true);
                });

                $(document).on("click", ".JS-see", function () {
                    slider.flexAnimate(0, true);

                    YQ.voxLogs({
                        module: 'annual_interest_report',
                        op: 'click_to_see_it_again_teacher'
                    });
                });
            },
            after: function(slider){
                if(slider.currentSlide >= slider.last || slider.currentSlide <= 0){
                    $(".JS-flex-next[data-type='next']").hide();
                }else{
                    $(".JS-flex-next[data-type='next']").show();
                }

                if($(slider).find('li.children').eq(slider.currentSlide).attr("data-type") == "reward_page"){
                    YQ.voxLogs({
                        module: 'annual_interest_report',
                        op: 'into_the_gift_screen_teacher'
                    });
                }
            }
        });

        var receiveValue = '';

        $(document).on("click", ".JS-selectReceive li", function(){
            var $self = $(this);

            $self.addClass("active").siblings().removeClass('active');

            receiveValue = $self.text();
        });

        //领取奖励
        $(document).on("click", ".JS-receiveReward", function(){
            var successContent = $("#successContent");

            $.post('/activity/recordwish.vpage', {
                wishContent: receiveValue
            }, function(data){
                if(data.success){
                    $.post('getaward.vpage', {}, function(data){
                        if(data.success){
                            successContent.html( template("T:领取结果", {isWeChat: isWeChat(), info: '恭喜您获得：<#if (currentTeacherDetail.isJuniorTeacher())!false>学豆170个<#else>园丁豆17个</#if>'}) );
                        }else{
                            successContent.html( template("T:领取结果", {isWeChat: isWeChat(), info: data.info}) );
                        }
                    });
                }else{
                    $.alert(data.info);
                }

                YQ.voxLogs({
                    module: 'annual_interest_report',
                    op: 'click_to_receive_gift_teacher'
                });
            });
        });

        function getShortUrl(u, callback){
            var $_shortUrl = "", $_originalUrl = "";

            if($_shortUrl != '' && $_originalUrl == u  && callback){
                callback($_shortUrl);
                return false;
            }

            $_originalUrl = u;
            $.post("/project/crt.vpage", {url : u}, function(data){
                if(data.success){
                    $_shortUrl = u = data.url;
                }

                if (callback){ callback(u); }
            });
        }

        //click share
        var shareLink = encodeURI(location.protocol + '//'+ location.host + '/project/teacherreport/share.vpage?userName=${((currentUser.profile.realname)!'')?replace("老师", "")}&avatarUrl=<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>&ranking=${((report.ranking*100)!0)?int}&aName=${(report.achievementName)!''}&studentNum=${(report.studentNum)!0}');

        getShortUrl(shareLink, function(u){
            shareLink = u;
        });

        $(document).on("click", ".JS-share", function(){
            if (window['external'] && window.external['shareInfo']) {
                window.external.shareInfo(JSON.stringify({
                    title   : "2016年教学手札",
                    content : "${((currentUser.profile.realname)!'')?replace("老师", "")}老师获得一起作业2016年“${(report.achievementName)!''}””荣誉称号",
                    url     : shareLink
                }));
            }else{
                console.info(shareLink);
                $.alert("分享失败!");
            }

            YQ.voxLogs({
                module: 'annual_interest_report',
                op: 'click_to_share_teacher'
            });
        });

        function isWeChat(){
            return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") != -1);
        }

        function setModeHeight(id, h ,n) {
            var _winHeight = $(window).height();
            var _defHeight = h || 700;
            var _defLessCount = n || 0;

            if(_winHeight <= _defHeight){
                $(id).height(_defHeight - _defLessCount);
            }else{
                $(id).height( _winHeight - _defLessCount);
            }
        }

        function playPause(sel){
            var myAudio = document.getElementById('audioBox');

            var notPlay = true;
            if(sel){
                notPlay = false;
            }

            if(myAudio.paused && notPlay){
                myAudio.play();
                $(".JS-clickAudio").removeClass('close_btn');
            }else{
                myAudio.pause();
                $(".JS-clickAudio").addClass('close_btn');
            }
        }


        $(document).on("click", '.JS-clickAudio', function(){
            playPause();
        });

        setTimeout(function(){
            $('.JS-clickAudio').click();
        }, 200);

        window.onbeforeunload = window.onunload = function(event) {
            playPause('stop');
        };

        //图表
        var chartJsonList = {};
        <#if (report.chartJson)?? && (report.chartJson)?has_content>
            chartJsonList = ${(report.chartJson)!'{}'};
        </#if>
        for(var i in chartJsonList.datalist){
            var count = chartJsonList.datalist[i]/chartJsonList.max*100;
            $('.JS-score li[data-info="' + i + '"]').height(count + "%").find('.num').text(chartJsonList.datalist[i]);
        }

        if(typeof YQ.voxLogs == 'function'){
            YQ.voxLogs({
                module: 'annual_interest_report',
                op: 'enter_the_first_screen_teacher'
            });
        }

        if(isWeChat()){
            function onBridgeReady(){
                WeixinJSBridge.call('hideOptionMenu');
            }

            if (typeof WeixinJSBridge == "undefined"){
                if( document.addEventListener ){
                    document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                }else if (document.attachEvent){
                    document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                    document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                }
            }else{
                onBridgeReady();
            }

            wx.config({
                debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                jsApiList: ['onMenuShareTimeline','onMenuShareAppMessage','onMenuShareQQ']
            });

            wx.hideOptionMenu();
            wx.hideMenuItems({
                menuList: ['onMenuShareTimeline','onMenuShareAppMessage','onMenuShareQQ', 'onMenuShareQZone', 'onMenuShareWeibo', 'onMenuShareAppMessage'] // 要隐藏的菜单项，只能隐藏“传播类”和“保护类”按钮，所有menu项见附录3
            });

            var shareConfig = {
                title: "2016年教学手札",
                desc: "${((currentUser.profile.realname)!'')?replace("老师", "")}老师获得一起作业2016年“${(report.achievementName)!''}””荣誉称号",
                link: shareLink,
                imgUrl: "http://cdn-cnc.17zuoye.cn/resources/app/17teacher/res/icon.png",
                success: function(){},
                cancel: function(){}
            };

            wx.ready(function() {
                wx.onMenuShareTimeline(shareConfig);
                wx.onMenuShareAppMessage(shareConfig);
                wx.onMenuShareQQ(shareConfig);
                wx.onMenuShareWeibo(shareConfig);
                wx.onMenuShareQZone(shareConfig);
            });
        }
    }
</script>
<audio controls="controls" loop="loop" id="audioBox" style="display: none;">
    <source src="//cdn.17zuoye.com/static/project/teacher_shine_57s.mp3" type="audio/mpeg">
</audio>
<#else>
<!--空白页-->
<div class="annualReport-box">
    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_10.jpg"/>">
</div>
</#if>
</@layout.page>