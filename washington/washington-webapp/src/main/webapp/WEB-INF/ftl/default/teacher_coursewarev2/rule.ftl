<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "rule", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"rule" : "public/script/teacher_coursewarev2/rule",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

<#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div class="indexBox" id="ruleContent">
        <div class="rulesBox">
            <!-- 赛程安排 -->
            <div class="courseSection courseSection-match">
                <div class="secInner">
                    <div class="secBox">
                        <div class="secTitle title05"></div>
                        <div class="secContent">
                            <div class="matchPic"></div>
                            <div class="matchBtn" style="display:none;">
                                <ul>
                                    <li>
                                        <a href="javascript:void(0)"><span>参加活动</span><i></i></a>
                                    </li>
                                    <li>
                                        <a href="javascript:void(0)"><span>为喜欢的作品打榜</span><i></i></a>
                                    </li>
                                    <li>
                                        <a href="javascript:void(0)"><span>为优秀作品投票</span><i></i></a>
                                    </li>
                                    <li>
                                        <a href="javascript:void(0)"><span>查看获奖作品</span><i></i></a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 参与方式 -->
            <div class="courseSection courseSection-yellow">
                <div class="secInner">
                    <div class="secBox secBox02">
                        <div class="secTitle title02"></div>
                        <div class="secContent">
                            <div class="methodPart">
                                <p class="title">一、活动对象</p>
                                <p class="info">公立、民办学校、培训机构小学英语、语文、数学教师</p>
                            </div>
                            <div class="methodPart">
                                <p class="title">二、参与方式</p>
                                <p class="info">登陆www.17zuoye.com 注册后，即可免费报名参与并上传作品</p>
                            </div>
                            <div class="methodPart">
                                <p class="title">三、作品维度</p>
                                <p class="info">应从以下四个维度反映教师的教学特色及能力：</p>
                                <p class="info">1.课堂目标：基于课程标准，指向核心素养</p>
                                <p class="info">2.学生活动：学生为中心，体现学生自主探究与合作学习</p>
                                <p class="info">3.信息技术支持：信息技术与课堂目标与学习环节的无缝融合</p>
                                <p class="info">4.教师能力：教师驾驭信息技术能力、驾驭课堂内容能力和组织学生能力</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 作品要求 -->
            <div class="courseSection courseSection-award">
                <div class="secInner">
                    <div class="secBox secBox-products">
                        <div class="secTitle title07"></div>
                        <div class="secContent">
                            <div class="awardExplain">
                                <div class="e-title">作品要求</div>
                                <div class="e-list">1.参与老师从现行小学语数英教材或国外原版教材中自选内容形成教案和课件，每件作品应为一个完整的教学课时内容。</div>
                                <div class="e-list">2.教学课件统一使用PPT格式。教学课件应反映教学目标和主要教学内容，可插入教学音频、视频。</div>
                                <div class="e-list">3.教案设计应反映教学思想、课程设计思路和教学特色。文件格式：word(.doc文件)。需使用本活动统一教案模板（<a class="needtrack downloadTemplate" data_op="o_7nGCR1Pp9a" href="javascript:void(0)" style="color: #2d0aff;">下载模板</a>）。</div>
                                <div class="e-list">4.所上传作品须是真实施教过，并提交5张课堂实况照片，每张大小不超过2M，应包括课件播放中的大屏幕、板书、学生自主学习、师生交流等场景。请确保照片真实、清晰，该照片将作为教案和课件的真实性评价依据。</div>
                                <div class="e-list">5.尊重原创，杜绝抄袭。课件和教案引用部分不超过30%，超过30%的作品视为抄袭，直接取消比赛资格。</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "./module/alert.ftl">
    <#include "./module/footer.ftl">
</div>
<script src="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.js')}"></script>
<script>
    var cdnHeader = "<@app.link href='/'/>";
    var userInfo = {};
    var awardTeachers = null; // 获奖老师名单
</script>
</@layout.page>