<#import "../serviceV2Module.ftl" as layout/>
<@layout.page
bodyClass='bg-02'
title="一起小学下载、一起中学下载"
keywords="一起作业,一起作业网,一起作业学生端APP下载,一起作业老师端APP下载"
description="一起作业网是一个学生、老师和家长三方互动的作业平台；下载一起作业网学生端，学生可以在线做英语口语作业，英使用英语点读机功能，体验争当小学霸的快乐学习；下载一起作业家长通，家长可以定期查看孩子的学习进度及教育资讯报告。下载一起作业APP，让学习成为一种美好体验。"
currentMenu="APP下载"
pageJs=['init']
pageJsFile={'init': 'public/script/project/help'}
>
<div style="height: 30px;"></div>
<div class="downloadPage-box">
    <div class="downloadPage-tab">
        <ul>
            <li class="JS-downloadType" data-type="teacher">
                <div class="dlp-image"><span class="icon-arrow"></span><img
                        src="<@app.link href="public/skin/default/v5/images/icon-teacher-v2.png"/>"></div>
                <div class="dlp-name">老师APP</div>
            </li>
            <li class="JS-downloadType" data-type="student">
                <div class="dlp-image"><span class="icon-arrow"></span><img
                        src="<@app.link href="public/skin/default/v5/images/stuApp-icon.png"/>"></div>
                <div class="dlp-name">学生APP</div>
            </li>
            <li class="JS-downloadType" data-type="parent">
                <div class="dlp-image"><span class="icon-arrow"></span><img
                        src="<@app.link href="public/skin/default/v5/images/icon-parents.png"/>"></div>
                <div class="dlp-name">一起学</div>
            </li>
        </ul>
    </div>
    <div class="downloadPage-main JS-animation">
        <ul class="slides">
            <li style="display: none;">

                <!--教师APP-->
                <div class="carousel-inner JS-slides-banner" data-type="0">
                    <div class="item active">
                        <div class="car-left">
                            <div class="viewport-inner JS-currentImg">
                                <ul style="width: 980px; height: 439px;">
                                    <li style="float: left;">
                                    <#--<img src="<@app.link href="public/skin/default/v5/images/teacher-0-v3.jpg"/>">-->
                                        <img src="<@app.link href="public/skin/default/v5/images/teacher-1.jpg"/>">
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/tea-2.png"/>">
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/tea-3.png"/>">
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/tea-mobile-04.png"/>">
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="car-right">
                            <h1>一起小学老师端</h1>
                            <div class="info JS-textInfo">
                                <p data-text="0">您的高效智能教学助手：布置、检查作业、详实学情分析，尽在掌握。</p>
                                <p data-text="1" style="display: none;">作业报告：强大的作业类型报告，学生掌握情况一目了然</p>
                                <p data-text="2" style="display: none;">听说读写：全面覆盖听说读写，帮学生甩掉死记硬背</p>
                                <p data-text="3" style="display: none;">轻松教学：随时布置、检查作业，自动批改， 轻松高效</p>
                            </div>
                            <div class="content">
                                <div class="c-left">
                                    <img src="<@app.link href="public/skin/default/v5/images/code-teacher-v1.6.png"/>"
                                         width="147">
                                </div>
                                <div class="c-right">
                                    <div class="btn"><a href="${pcDownUrl.teacher[0]}" target="_blank"
                                                        class="and-btn"><i class="icon-and"></i>安卓下载</a></div>
                                    <div class="btn"><a href="${pcDownUrl.teacher[1]}" target="_blank"
                                                        class="ios-btn"><i class="icon-ios"></i>iOS下载</a></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </li>
            <li style="display: none;">
                <!--学生APP-->
                <div class="carousel-inner JS-slides-banner" data-type="1">
                    <div class="item active">
                        <div class="car-left">
                            <div class="viewport-inner JS-currentImg">
                                <ul style="width: 980px; height: 439px;">
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/student-1-v2.jpg"/>"/>
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/stu-mobile-02.png"/>"/>
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/stu-3.png"/>"/>
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/stu-4.png"/>"/>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="car-right">
                            <h1>一起小学学生端</h1>
                            <div class="info JS-textInfo">
                                <p data-text="0">陪孩子一起成长的好伙伴。写作业、听课文、背单词，快乐学习。</p>
                                <p data-text="1" style="display: none;">跟读打分：语音智能评分，提高成绩更快</p>
                                <p data-text="2" style="display: none;">课本同步：与课本紧密结合，学习更高效</p>
                                <p data-text="3" style="display: none;">成绩提分：记录学习点滴，随时查看作业历史提分更快</p>
                            </div>
                            <div class="content">
                                <div class="c-left">
                                    <img style="width:147px"
                                         src="<@app.link href="public/skin/default/v5/images/code-student-v2.9.png"/>">
                                </div>
                                <div class="c-right">
                                    <div class="btn"><a href="${pcDownUrl.student[0]}" target="_blank"
                                                        class="and-btn"><i class="icon-and"></i>安卓下载</a></div>
                                    <div class="btn"><a href="${pcDownUrl.student[1]}" target="_blank"
                                                        class="ios-btn"><i class="icon-ios"></i>iOS下载</a></div>
                                <#--<div class="btn"><a href="javascript:;" style="cursor: default;" class="ios-btn"><i class="icon-ios"></i>敬请期待</a></div>-->
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </li>
            <li style="display: none;">
                <!--家长通-->
                <div class="carousel-inner JS-slides-banner" data-type="2">
                    <div class="item active">
                        <div class="car-left">
                            <div class="viewport-inner  JS-currentImg">
                                <ul style="width: 980px; height: 439px;">
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/parent-1_v2.png"/>">
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/parent-2_v2.png"/>">
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/parent-3_v2.png"/>">
                                    </li>
                                    <li style="float: left;">
                                        <img src="<@app.link href="public/skin/default/v5/images/parent-4_v2.png"/>">
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="car-right">
                            <h1>一起学</h1>
                            <div class="info JS-textInfo">
                                <p data-text="0">手机上的家庭教育助手。作业报告、点读机、错题本、教育资讯，尽在一起学！</p>
                                <p data-text="1" style="display: none;">作业报告：作业及错题提醒，辅导孩子学习更精准</p>
                                <p data-text="2" style="display: none;">海量学习资源：轻课、绘本、趣味音视频，海量内容帮你学</p>
                                <p data-text="3" style="display: none;">点读机：手机秒变课本点读机，小学英语、语文全覆盖</p>
                            </div>
                            <div class="content">
                                <div class="c-left">
                                    <img src="<@app.link href="public/skin/default/v5/images/code-parent-v2.0.png"/>"
                                         style="width:147px;">
                                </div>
                                <div class="c-right">
                                    <div class="btn"><a href="${pcDownUrl.parent[0]}" target="_blank" class="and-btn"><i
                                            class="icon-and"></i>安卓下载</a></div>
                                    <div class="btn"><a href="${pcDownUrl.parent[1]}" target="_blank" class="ios-btn"><i
                                            class="icon-ios"></i>iOS下载</a></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
        <!--焦点-->
        <ul class="zy-scrollNav JS-carousel" style="margin-left: -80px;">
            <li style="left: -16px;" class=""><a class="">1</a></li>
            <li style="margin-right: 20px;"><a class="">2</a></li>
            <li><a class="">3</a></li>
            <li class="" style="right: -16px;"><a class="">4</a></li>
        </ul>

        <a class="carousel-control prev JS-flex-prev" href="javascript:;" style="z-index: 2;"></a>
        <a class="carousel-control next JS-flex-next" href="javascript:;" style="z-index: 2;"></a>
    </div>
</div>

</@layout.page>