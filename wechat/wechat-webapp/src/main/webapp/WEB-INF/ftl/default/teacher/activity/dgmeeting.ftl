<#import "../layout.ftl" as activityMain>
<@activityMain.page title="一起作业历程" pageJs="dgmeeting">
<@sugar.capsule css=['dgmeetingNew','swiper','swiperAnimate'] />
<!--引导start//-->
<div class="course-share-back js-guideContent" style="display: none;">
    <div class="container">
        <div class="btn js-closeGuideDiv">
            <a href="javascript:void(0);"></a>
        </div>
    </div>
</div>
<#if isJustRegistered!false>
<#assign
    sliderConfig = {
        "0" : '<span>${teacherName!""}</span>老师于 <span>二零一六</span>年<span>五</span>月加入</p><p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="2.8s" >刚刚开启一起作业之旅',
        "1" : '<span>告别</span>繁琐的作业批改压力',
        "2" : '引导学生<span>发现</span>学习的兴趣',
        "3" : '更高效的教学成果</p><p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="0.8s" swiper-animate-delay="2.5s">与学生<span>成长</span>的惊喜',
        "4" : '一起作业网<span class="arrow">，</span>一直与您一起成长</p><p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="1.5s">感谢您的<span>信任</span>与<span>支持</span>'
    }
>
<#else>
    <#assign
    sliderConfig = {
    "0" : '<span>${teacherName!""}</span>老师于 <span>${registerDate.year!""}</span>年<span>${registerDate.month!""}</span>月加入</p><p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="2.5s" >距今已有 <span class="num">${useDays}</span> 天',
    "1" : '<span class="num">${basicHomeworkCount!"0"}</span>份作业<span class="num">${quizHomeworkCount!"0"}</span>份试卷',
    "2" : '<span class="num">${clazzCount!"0"}</span>个班级<span class="num">${studentCount!"0"}</span>名学生',
    "3" : '经过努力收获了 <span class="num">${rewardIntegralSum!"0"}</span>个园丁豆',
    "4" : '一起作业网<span class="arrow">，</span>一直与您一起成长</p><p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="1.5s">感谢您的<span>支持</span>与<span>相伴</span>'
    }
    >
</#if>
<!--引导//end-->
<div class="course-box">
    <div class="header">
        <div class="logo"></div>
        <div id="bgmBtn" class="audio">
            <a href="javascript:void(0);"></a>
        </div>
        <div class="hidden">
            <audio id="bgmSource" src="<@app.link href='public/images/teacher/activity/dgmeetingNewBgm.mp3'/>" autoplay="autoplay" loop="loop"></audio>
        </div>
    </div>
    <div class="swiper-container">
        <div class="swiper-wrapper">
            <div class="swiper-slide">
                <div class="section resize">
                    <div class="title">
                        <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="0.5s" src="<@app.link href='public/images/teacher/activity/course-title1.png'/>" />
                    </div><!--加入-->
                    <div class="content">
                        <p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1.5s" swiper-animate-delay="1.5s">
                            ${sliderConfig['0']}
                        </p>
                    </div>
                </div><!--加入-->
                <div class="bg">
                    <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="3.5s" src="<@app.link href='public/images/teacher/activity/courseBg1.png'/>" />
                </div>
                <div class="footer">
                    <a href="javascript:void(0);" class="arrow"></a>
                </div>
            </div>
            <div class="swiper-slide">
                <div class="section resize">
                    <div class="title">
                        <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="0.2s" src="<@app.link href='public/images/teacher/activity/course-title2.png'/>" />
                    </div><!--批改-->
                    <div class="content">
                        <p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1.3s" swiper-animate-delay="1.2s">
                        ${sliderConfig['1']}
                        </p>
                    </div>
                </div><!--批改-->
                <div class="bg">
                    <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="2.5s" src="<@app.link href='public/images/teacher/activity/courseBg2.png'/>" />
                </div>
                <div class="footer">
                    <a href="javascript:void(0);" class="arrow"></a>
                </div>
            </div>
            <div class="swiper-slide">
                <div class="section resize">
                    <div class="title">
                        <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="0.2s" src="<@app.link href='public/images/teacher/activity/course-title3.png'/>" />
                    </div><!--指导-->
                    <div class="content">
                        <p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1.3s" swiper-animate-delay="1.5s">
                        ${sliderConfig['2']}
                        </p>
                    </div>
                </div>
                <div class="bg">
                    <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="2.8s" src="<@app.link href='public/images/teacher/activity/courseBg3.png'/>" />
                </div><!--指导-->
                <div class="footer">
                    <a href="javascript:void(0);" class="arrow"></a>
                </div>
            </div>
            <div class="swiper-slide">
                <div class="section resize">
                    <div class="title">
                        <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="0.2s" src="<@app.link href='public/images/teacher/activity/course-title4.png'/>" />
                    </div><!--收获-->
                    <div class="content">
                        <p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1.3s" swiper-animate-delay="1.2s">
                        ${sliderConfig['3']}
                        </p>
                    </div>
                </div>
                <div class="bg">
                    <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="<#if isJustRegistered!false>3.3s<#else>2.3s</#if>" src="<@app.link href='public/images/teacher/activity/courseBg4.png'/>" />
                </div><!--收获-->
                <div class="footer">
                    <a href="javascript:void(0);" class="arrow"></a>
                </div>
            </div>
            <div class="swiper-slide">
                <div class="section resize">
                    <div class="content">
                        <p class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1.3s" swiper-animate-delay="0.2s">
                        ${sliderConfig['4']}
                        </p>
                    </div>
                </div>
                <div class="bg">
                    <img class="ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="2.5s" src="<@app.link href='public/images/teacher/activity/courseBg5.png'/>" />
                </div><!--成长-->
                <div class="footer ani" swiper-animate-effect="fadeIn" swiper-animate-duration="1s" swiper-animate-delay="3s">
                    <#if isFromShare!false>
                        <a href="http://wx.17zuoye.com/teacher/activity/dgmeeting.vpage" class="share_btn">查看我的历程</a>
                    <#else>
                        <a href="javascript:void(0);" class="share_btn js-shareBtn">分享</a>
                    </#if>
                </div>
            </div>
        </div>
    </div>
<script type="text/javascript">
<#if config_signature?has_content>
var wechatConfig = {};
wechatConfig.signature = "${config_signature}";
wechatConfig.appid = "${appid}";
wechatConfig.timestamp = "${config_timestamp}";
wechatConfig.noncestr = "${config_nonceStr}";
wechatConfig.shareUrl="${shareUrl}";
wechatConfig.title="我的一起作业之旅";
wechatConfig.sharePic="<@app.link href="/public/images/teacher/activity/shareParicon.png"/>";
</#if>
var sliderConfig = {};
<#assign contentArray= [0,1,2,3,4]>
<#list contentArray as index>
sliderConfig["${index}"]= '${sliderConfig["${index}"]}';
</#list>
</script>
</@activityMain.page>