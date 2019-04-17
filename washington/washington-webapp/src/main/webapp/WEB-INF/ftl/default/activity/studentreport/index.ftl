<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="2016我的成长记录"
pageJs=['jquery', 'flexSlider', 'weui', 'template', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/2016report/student/css/skin"]}
>

<#if report??>

    <style>
        html, body{background-color: #fac845;}
        .JS-selectReceive li.active{ color: #000;}
    </style>
    <a href="javascript:void(0);" class="play_btn JS-clickAudio" style="cursor: pointer;"></a><!--close_btn关闭音乐-->

    <div class="JS-uservoice-banner">
        <ul class="slides">
            <li class="children">
                <!--首页-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_01.jpg"/>"/>
                </div>
            </li>
            <li class="children">
                <!--相遇-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_02.jpg"/>">
                    <div class="grd-info">
                        <p><span class="num">${((report.createDate)?split('-')[0])!'---'}</span> 年 <span class="num">${((report.createDate)?split('-')[1])!'--'}</span> 月 <span class="num">${((report.createDate)?split('-')[2])!'--'}</span> 日，我注册了一起小学。</p>
                        <p>至今，已在一起小学App学习 <span class="num">${(report.diffToday)!0}</span> 天</p>
                    </div>
                </div>
            </li>
            <#if (report.engFirstHwDate)?? || (report.mathFirstHwDate)??>
            <li class="children">
                <!--初识-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_03.jpg"/>">
                    <div class="grd-info">
                        <#if ((report.engFirstHwDate)?split('-')[0])?has_content>
                        <p><span class="num">${((report.engFirstHwDate)?split('-')[0])!'---'}</span> 年 <span class="num">${((report.engFirstHwDate)?split('-')[1])!'--'}</span> 月 <span class="num">${((report.engFirstHwDate)?split('-')[2])!'--'}</span> 日，我完成了第一份<span class="fontGreen">英语</span>练习</p>
                        </#if>
                        <#if ((report.mathFirstHwDate)?split('-')[0])?has_content>
                        <p><span class="num">${((report.mathFirstHwDate)?split('-')[0])!'---'}</span> 年 <span class="num">${((report.mathFirstHwDate)?split('-')[1])!'--'}</span> 月 <span class="num">${((report.mathFirstHwDate)?split('-')[2])!'--'}</span> 日，我完成了第一份<span class="fontGreen">数学</span>练习</p>
                        </#if>
                    </div>
                </div>
            </li>
            </#if>


            <#if (report.voiceUrl)?has_content>
            <li class="children">
                <!--记录-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_04.jpg"/>">
                    <audio controls="controls" id="voiceUrl" style="display: none;">
                        <#if report.voiceUrl?index_of("http://") gt -1 || report.voiceUrl?index_of("https://") gt -1>
                            <source src="${(report.voiceUrl?trim)!}" type="audio/mpeg">
                        <#else>
                            <source src="http://${(report.voiceUrl?trim)!}" type="audio/mpeg">
                        </#if>
                    </audio>
                    <div class="grd-bubble JS-clickVoiceUrl" style="cursor: pointer;">
                        <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/icon_sound.png"/>" class="pic">
                        <span class="bTime"><#--17′08″--></span>
                    </div>
                    <div class="grd-info fontSize">
                        <p>我的本学期高分语音记录，快来听吧~</p>
                    </div>
                </div>
            </li>
            </#if>
            <#if (report.comment)?has_content && (report.commentTeacherName)?has_content>
            <li class="children">
                <!--鼓励-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_05.jpg"/>">
                    <div class="grd-comment">
                        <div class="gTitle"><div style="width: 8.5rem; height: 6rem; overflow: hidden; text-overflow: ellipsis;">${(report.comment)!'---'}</div></div>
                        <div class="grd-info">
                            <#--<div class="gImage"><img src="" alt=""></div>-->
                            <p><span class="num">${(report.commentTeacherName)!'---'}</span> 老师</p>
                            <p>给了我第一条评语</p>
                        </div>
                    </div>
                </div>
            </li>
            </#if>
            <#if (report.homeworkNum90)?has_content>
            <li class="children">
                <!--成绩-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_06.jpg"/>">
                    <div class="anr-main">
                        <div class="gHead">我的正确率分布情况</div>
                        <ul>
                            <li class="s-1 JS-scoreEnglish" data-info="≤60%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-6 JS-scoreMath" data-info="≤60%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-2 JS-scoreEnglish" data-info="60%-69%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-7 JS-scoreMath" data-info="60%-69%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-3 JS-scoreEnglish" data-info="70%-79%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-8 JS-scoreMath" data-info="70%-79%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-4 JS-scoreEnglish" data-info="80%-89%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-9 JS-scoreMath" data-info="80%-89%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-5 JS-scoreEnglish" data-info="≥90%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                            <li class="s-10 JS-scoreMath" data-info="≥90%" style="height:1%;">
                                <div class="peo"><span class="num">0</span></div>
                            </li>
                        </ul>
                        <div class="mfoot">
                            <div class="percent">60%以下</div>
                            <div class="percent">60-69%</div>
                            <div class="percent">70-79%</div>
                            <div class="percent">80-89%</div>
                            <div class="percent">90%以上</div>
                        </div>
                        <div class="gArrow">
                            <div class="g-1"><i></i>英语正确率</div>
                            <div class="g-2"><i></i>数学正确率</div>
                        </div>
                    </div>
                    <div class="grd-info">
                        <p>本学期，我有 <span class="num">${(report.homeworkNum90)!'---'}</span> 次练习正确率达<span class="fontGreen">90%</span>以上</p>
                    </div>
                </div>
            </li>
            </#if>
            <#if ((report.finishHwNum gte 3)!false) && currentStudentName?has_content>
            <li class="children">
                <!--勤奋-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_07.jpg"/>">
                    <div class="grd-content">${(report.achievementName)!'---'}</div><!--学霸精英--><!--学习达人-->
                    <div class="grd-info">
                        <p>我完成了<span class="num">${(report.finishHwNum)!'---'}</span> 份练习，平均正确率<span class="num">${((report.finishRate!0)*100)?int}</span>%，</p>
                        <p>超过全国<span class="num"><#if (report.ranking gt 0.01)!false>${((report.ranking!)*100)?int}<#else>1</#if></span>％的同年级同学，</p>
                        <p>获得<span class="num">${(report.achievementName)!'---'}</span>称号</p>
                    </div>
                </div>
            </li>
            </#if>
            <#if report.studyBean?has_content>
            <li class="children">
                <!--收获-->
                <div class="growthRecord-box">
                    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_08.jpg"/>">
                    <div class="grd-info">
                        <p>这一年，我收获了<span class="num">${(report.studyBean)!'---'}</span>个学豆</p>
                    </div>
                </div>
            </li>
            </#if>
            <li class="children" data-type="reward_page">
                <!--展望-->
                <div id="successContent" style="height: 100%;">
                    <div class="growthRecord-box">
                        <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_09.jpg"/>">
                        <div class="grd-list">
                            <ul class="JS-selectReceive">
                                <li style="margin-left: 6rem;">养成学习好习惯</li>
                                <li style="margin: -.5rem 0 0 0">成为班级小明星</li>
                                <li style="margin: -1rem 0 0 7rem;">和老师做好朋友</li>
                                <li style="margin: -.5rem 0 0 .8rem;">和父母一起读书</li>
                                <li style="margin:.5rem 0 0 1rem; clear: both;">找到自己的兴趣</li>
                            </ul>
                        </div>
                        <div class="grd-footer">
                            <#if ((currentUser.userType == 3)!false) && ((currentStudentDetail.isJuniorStudent())!false)>
                                <#if ((report.finishHwNum gte 3)!false) && currentStudentName?has_content>
                                <a href="javascript:void(0);" class="receive_btn JS-share" data-type="junior">分享给朋友</a>
                                </#if>
                            <#else>
                                <a href="javascript:void(0);" class="receive_btn JS-receiveReward">领取新年礼物</a>
                            </#if>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
    </div>

    <script type="text/html" id="T:领取结果">
        <div class="growthRecord-box">
            <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_10.jpg"/>">
            <div class="grd-main">
                <ul>
                    <#if (report.achievementName == "学习达人")!false>
                        <li>
                            <div class="mImages">
                                <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/icon_04.png"/>">
                            </div>
                        </li>
                    <#elseif (report.achievementName == "学习精英")!false>
                        <li class="oneImages">
                            <div class="mImages">
                                <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/icon_03.png"/>">
                            </div>
                        </li>
                    <#elseif (report.achievementName == "潜力之星")!false>
                        <li>
                            <div class="mImages">
                                <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/icon_02.png"/>">
                            </div>
                        </li>
                    </#if>
                    <li <#if !((report.achievementName)?has_content)>style="float: none; margin: 0 auto;" </#if>>
                        <div class="mImages mDiffer">
                            <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/icon_01.png"/>">
                            <div class="mHead">
                                <img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>">
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
            <div class="grd-footer">
                <a href="javascript:void(0);" class="receive_btn yellow_btn JS-see">再看一次</a>
                <#if ((report.finishHwNum gte 3)!false) && currentStudentName?has_content>
                <a href="javascript:void(0);" class="receive_btn JS-share">分享给朋友</a>
                </#if>
            </div>
        </div>
    </script>

    <a href="javascript:void(0);" class="arrowDown_btn JS-flex-next" data-type='next'></a>

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
                            op: 'click_to_see_it_again_student'
                        });
                    });
                },
                after: function(slider){
                    if(slider.currentSlide >= slider.last){
                        $(".JS-flex-next[data-type='next']").hide();
                    }else{
                        $(".JS-flex-next[data-type='next']").show();
                    }

                    if($(slider).find('li.children').eq(slider.currentSlide).attr("data-type") == "reward_page"){
                        YQ.voxLogs({
                            module: 'annual_interest_report',
                            op: 'into_the_gift_screen_student'
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
                                successContent.html( template("T:领取结果", {info: '成功领取2017礼包。'}) );
                            }else{
                                successContent.html( template("T:领取结果", {info: data.info}) );
                            }
                        });
                    }else{
                        $.alert(data.info);
                    }

                    YQ.voxLogs({
                        module: 'annual_interest_report',
                        op: 'click_to_receive_gift_student'
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
            var shareLink = encodeURI(location.protocol + '//'+ location.host + '/project/studentreport/share.vpage?userName=${(currentStudentName)!''}&avatarUrl=${currentUserAvatarUrl!''}&ranking=<#if (report.ranking gt 0.01)!false>${((report.ranking!0)*100)?int}<#else>1</#if>&aName=${(report.achievementName)!''}&userType=${(currentUser.userType)!}');

            getShortUrl(shareLink, function(u){
                shareLink = u;
            });

            $(document).on("click", ".JS-share", function(){
                if (window['external'] && window.external['shareInfo']) {
                    window.external.shareInfo(JSON.stringify({
                        title   : "2016年成长记录",
                        content : "${(currentStudentName)!''}同学获得一起教育科技2016年“${(report.achievementName)!''}”荣誉称号",
                        url     : shareLink
                    }));
                }else if(window['external'] && window.external['shareMethod']) {
                    window.external.shareMethod(JSON.stringify({
                        title: "2016年成长记录",
                        content: "${(currentStudentName)!''}同学获得一起教育科技2016年“${(report.achievementName)!''}”荣誉称号",
                        url     : shareLink,
                        type: "SHARE",
                        channel: 4
                    }));
                }else{
                    console.info(shareLink);
                    $.alert("分享失败!");
                }

                if($(this).attr('data-type') == 'junior'){
                    $.post('/activity/recordwish.vpage', {
                        wishContent: receiveValue
                    }, function(data){

                    });
                }

                YQ.voxLogs({
                    module: 'annual_interest_report',
                    op: 'click_to_share_student'
                });
            });

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

            function voiceUrlPlayPause(sel){
                var myAudio = document.getElementById('voiceUrl');

                var notPlay = true;
                if(sel){
                    notPlay = false;
                }

                if(myAudio.paused){
                    myAudio.play();
                }else{
                    myAudio.pause();
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
                voiceUrlPlayPause('stop');
            };

            $(document).on("click", '.JS-clickVoiceUrl', function(){
                voiceUrlPlayPause();
            });

            //图表
            var chartJsonList = {};
            <#if (report.chartJson)?? && (report.chartJson)?has_content>
                chartJsonList = ${(report.chartJson)!'{}'};
            </#if>
            for(var i in chartJsonList.datalist1){
                var count1 = chartJsonList.datalist1[i]/chartJsonList.max*100;
                $('.JS-scoreEnglish[data-info="' + i + '"]').height(count1 + "%").find('.num').text(chartJsonList.datalist1[i]);
            }

            for(var k in chartJsonList.datalist2){
                var count2 = chartJsonList.datalist2[k]/chartJsonList.max*100;
                $('.JS-scoreMath[data-info="' + k + '"]').height(count2 + "%").find('.num').text(chartJsonList.datalist2[k]);
            }

            if(typeof YQ.voxLogs == 'function'){
                YQ.voxLogs({
                    module: 'annual_interest_report',
                    op: 'enter_the_first_screen_student'
                });
            }
        }
    </script>
    <audio controls="controls" loop="loop" id="audioBox" style="display: none;">
        <source src="//cdn.17zuoye.com/static/project/student_summer_23s.mp3" type="audio/mpeg">
    </audio>
<#else>
    <div class="growthRecord-box">
        <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_11.jpg"/>">
    </div>
</#if>
</@layout.page>