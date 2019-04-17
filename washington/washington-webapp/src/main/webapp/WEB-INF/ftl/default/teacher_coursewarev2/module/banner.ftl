<#--banner-->
<div class="bannerBox" id="bannerContent">
    <!-- 轮播图片 -->
    <div class="swiper-container bannerSwiper">
        <div class="swiper-wrapper">
            <div class="swiper-slide scrollImg">
                <img class="rollImg" src="<@app.link href="public/skin/teacher_coursewarev2/images/pc_banner08_1.jpg"/>" alt="">
                <div class="innerBox innerBox02">
                    <div class="bannerLeft" style="display: none;">
                        <div class="btnBox">
                            <div class="bannerBtn joinBtn bannerJoinGame"></div>
                            <div class="bannerBtn downloadBtn needtrack downloadTemplate" data_op="o_GYtcE08gC0"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="swiper-slide scrollImg">
                <img class="rollImg" src="<@app.link href="public/skin/teacher_coursewarev2/images/pc_banner01_1.png"/>" alt="">
                <div class="innerBox innerBox02">
                    <div class="bannerLeft">
                        <div class="btnBox">
                            <div class="bannerBtn joinBtn bannerJoinGame" style="display: none;"></div>
                            <div class="bannerBtn downloadBtn needtrack downloadTemplate" data_op="o_GYtcE08gC0"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="swiper-slide scrollImg">
                <img class="rollImg" src="<@app.link href="public/skin/teacher_coursewarev2/images/pc_banner06_2.jpg"/>" alt="">
                <div class="innerBox innerBox02">
                    <div class="bannerLeft" style="display: none;">
                        <div class="btnBox">
                            <div class="bannerBtn joinBtn bannerJoinGame"></div>
                            <div class="bannerBtn downloadBtn needtrack downloadTemplate" data_op="o_GYtcE08gC0"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="swiper-pagination"></div>
    </div>
    <!-- 右侧滚动作品消息 -->
    <div class="innerBox innerBox03">
        <div class="bannerRight clearfix" data-bind="visible: newCoursewareList().length" style="display: none">
            <div class="swiper-container bannerCourseSwiper">
                <ul class="listBox swiper-wrapper">
                    <!-- ko foreach: newCoursewareList -->
                    <li class="swiper-slide swiper-no-swiping" style="cursor: default">
                        <div class="listInfo">
                            <div class="infoPic">
                                <img alt="封面图" data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}">
                            </div>
                            <div class="infoDetail">
                                <div class="infoTop">
                                    <span class="name" data-bind="text: teacherName"></span>
                                    <span class="time" data-bind="text: createDate"></span>
                                </div>
                                <div class="school" data-bind="text: schoolName"></div>
                            </div>
                        </div>
                        <div class="listTitle" data-bind="text: '上传了' + title"></div>
                    </li>
                    <!-- /ko -->
                </ul>
            </div>
        </div>
    </div>
</div>