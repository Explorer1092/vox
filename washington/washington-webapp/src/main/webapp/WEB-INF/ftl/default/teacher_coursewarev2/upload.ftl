<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "upload", "ossupload"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"upload" : "public/script/teacher_coursewarev2/upload",
"ossupload" : "public/script/teacher_coursewarev2/ossupload"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

    <#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div class="indexBox" id="uploadContent">
        <div class="up_content">
            <div class="activity_box">
                <!--参加活动标题-->
                <div class="a_title">
                    <div class="title_left">参加活动 <span>(上传后的作品不可自行删除，如需修改，可在“个人中心”里进行修改)</span></div>
                    <div class="title_right">
                        <div class="btn" data-bind="click: saveCourse">保 存</div>
                        <div class="btn active" data-bind="
                    css: { 'disabled': !inputCourseName() || isShowUploadPoster() || isShowUploadWord() || isShowUploadCourse() || isShowUploadImages() },
                    click: commitCourse">提 交</div>
                    </div>
                </div>
                <!--活动内容-->
                <ul class="a_detail_list">
                    <!--选择科目-->
                    <li class="clear">
                        <div class="title star">选择科目:</div>
                        <div class="tag_box" data-bind="visible: subjectList().length" style="display: none;">
                            <!-- ko foreach: subjectList -->
                            <span data-bind="
                        text: name,
                        css: {
                            'active': id === $root.choiceSubjectInfo().id,
                         },
                        click: $root.choiceSubject.bind($data)"></span>
                            <!-- /ko -->
                        </div>
                    </li>
                    <!--作品属性-->
                    <li class="clear">
                        <div class="title star">作品属性:</div>
                        <div class="bar_box">
                            <!--年级-->
                            <div class="bar_content" style="display: none;" data-bind="
                        visible: gradeList().length,
                        css: { 'open_active' : isShowGradeSelect()},
                        click: clickGrade">
                                <div class="label_name"><!-- ko text: choiceGradeInfo().name --><!-- /ko --><i class="arrow_down"></i></div>
                                <div class="tag_list" data-bind="visible: isShowGradeSelect()" style="display: none;">
                                    <!-- ko foreach: gradeList -->
                                    <span data-bind="
                                text: name,
                                css: { 'active': id === $root.choiceGradeInfo().id },
                                attr: { title: name },
                                click: $root.choiceGrade.bind($data)"></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!--学期-->
                            <div class="bar_content" style="display: none;" data-bind="
                        visible: termList().length,
                        css: { 'open_active' : isShowTermSelect()},
                        click: clickTerm">
                                <div class="label_name"><!-- ko text: choiceTermInfo().name --><!-- /ko --><i class="arrow_down"></i></div>
                                <div class="tag_list" data-bind="visible: isShowTermSelect()" style="display: none;">
                                    <!-- ko foreach: termList -->
                                    <span data-bind="
                                text: name,
                                css: { 'active': id === $root.choiceTermInfo().id },
                                attr: { title: name },
                                click: $root.choiceTerm.bind($data)"></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!--教材-->
                            <div class="bar_content large" style="display: none;" data-bind="
                        visible: bookList().length,
                        css: { 'open_active' : isShowBookSelect()},
                        click: clickSeries">
                                <div class="label_name"><!-- ko text: choiceBookInfo().name --><!-- /ko --><i class="arrow_down"></i></div>
                                <div class="tag_list" data-bind="visible: isShowBookSelect()" style="display: none;">
                                    <!-- ko foreach: bookList -->
                                    <span data-bind="
                                text: name,
                                css: { 'active': id === $root.choiceBookInfo().id },
                                attr: { title: name },
                                click: $root.choiceBook.bind($data)"></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!--单元-->
                            <div class="bar_content large" style="display: none;" data-bind="
                        visible: unitList().length,
                        css: { 'open_active' : isShowUnitSelect()},
                        click: clickUnit">
                                <div class="label_name"><!-- ko text: choiceUnitInfo().unitName --><!-- /ko --><i class="arrow_down"></i></div>
                                <div class="tag_list" data-bind="visible: isShowUnitSelect()" style="display: none;">
                                    <!-- ko foreach: unitList -->
                                    <span data-bind="
                                text: unitName,
                                css: { 'active': unitId === $root.choiceUnitInfo().unitId },
                                attr: { title: unitName },
                                click: $root.choiceUnit.bind($data)"></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!--教程-->
                            <div class="bar_content large" style="display: none;" data-bind="
                        visible: lessonList().length,
                        css: { 'open_active' : isShowLessonSelect()},
                        click: clickLession">
                                <div class="label_name"><!-- ko text: choiceLessonInfo().lessonRealName --><!-- /ko --><i class="arrow_down"></i></div>
                                <div class="tag_list" data-bind="visible: isShowLessonSelect()" style="display: none;">
                                    <!-- ko foreach: lessonList -->
                                    <span data-bind="
                                text: lessonRealName,
                                css: { 'active': lessonId === $root.choiceLessonInfo().lessonId },
                                attr: { title: lessonRealName },
                                click: $root.choiceLession.bind($data)"></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                        </div>
                    </li>
                    <!--作品标题-->
                    <li class="clear">
                        <div class="title star">作品标题:</div>
                        <div class="title_input"><input type="text" placeholder="输入标题" maxlength="30" data-bind="
                    value: inputCourseName,
                    valueUpdate: 'afterkeydown'"></div>
                    </li>
                    <!--作品简介-->
                    <li class="clear">
                        <div class="title null">作品简介:</div>
                        <div class="title_input word_long"><textarea name="" id="" cols="30" rows="10" maxlength="140" placeholder="请填写对上传作品的简介" class="text_box" data-bind="
                    value: inputCourseDescription,
                    valueUpdate: 'afterkeydown',
                    event: { keyup: inputDescriptionKeyUp }"></textarea>
                            <span class="input-left-num" data-bind="text: inputCourseDescriptionLeft"></span>
                        </div>
                    </li>
                    <!--曾获奖项-->
                    <li class="clear">
                        <div class="title null">曾获最高奖项:  <span class="explane">（请上传作品曾获最高奖项证书或奖杯照片，图片格式png或jpg，图片大小不超过2M）</span></div>
                        <div class="bar_box award_box" data-bind="visible: awardList().length" style="display:none;">
                            <div class="bar_content" data-bind="
                        css: { 'open_active' : isShowAwardSelect()},
                        click: clickAward">
                                <div class="label_name"><!-- ko text: choiceAwardInfo().name --><!-- /ko --><i class="arrow_down"></i></div>
                                <div class="tag_list" data-bind="visible: isShowAwardSelect" style="display: none;">
                                    <!-- ko foreach: awardList -->
                                    <span data-bind="
                                text: name,
                                css: { 'active': id === $root.choiceAwardInfo().id },
                                attr: { title: name },
                                click: $root.choiceAward.bind($data)"></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <div class="title_input" data-bind="visible: $root.choiceAwardInfo().id > 0" style="display: none;"><input type="text" placeholder="请填写获奖荣誉全称" data-bind="
                        value: inputAwardDesc,
                        valueUpdate: 'afterkeydown'"></div>
                        </div>

                        <div data-bind="visible: $root.choiceAwardInfo().id > 0" style="display: none;">
                            <!--上传中-->
                            <div class="title_input img_box" data-bind="visible: !isShowUploadAward()" style="display: none">
                                <div class="upload_imgbox upload_award_imgbox">
                                    <img data-bind="attr: {src: awardImageSrc}" alt="奖状图片">
                                </div>
                                <span class="upload_btn" data-bind="click: choiceAwardImage">重新上传</span>
                            </div>
                            <!--上传前-->
                            <div class="title_input img_box" data-bind="visible: isShowUploadAward()" style="display: none">
                                <div class="upload_before" data-bind="click: choiceAwardImage"></div>
                            </div>
                            <input type="file" id="JS-awardImage" accept="image/jpeg, image/jpg,image/png" style="display: none;">
                        </div>
                    </li>
                    <!--作品封面-->
                    <li class="clear">
                        <div class="title star">作品封面: <span class="explane">( 图片格式：png或者jpg,图片大小不能超过2M，建议图片尺寸为240*180 )</span></div>
                        <!--上传中-->
                        <div class="title_input img_box" data-bind="visible: !isShowUploadPoster()" style="display: none">
                            <div class="upload_imgbox">
                                <img alt="封面图" data-bind="attr: {src: posterSrc}" alt="" width="240" height="180" id="posterImage">
                                <p class="poster_title" data-bind="text: inputCourseName, visible: (needCanvasCreatePoster() && !isIEEnvironment())"></p>
                                <canvas id="posterCanvas" width="240" height="180" style="display: none;"></canvas>
                            </div>
                            <span class="upload_btn needtrack" data_op="o_95TODDfHWV" data-bind="click: choicePoster">重新上传</span>
                        </div>
                        <!--上传前-->
                        <div class="title_input img_box" data-bind="visible: isShowUploadPoster()" style="display: none">
                            <div class="upload_before" data-bind="click: choicePoster"></div>
                        </div>
                        <input type="file" id="JS-poster" accept="image/jpeg, image/jpg,image/png" style="display: none;">
                    </li>
                    <!--作品内容-->
                    <li class="clear">
                        <div class="title star">作品内容: <span class="explane">（提交教案、配套课件PPT以及实施该教学设计的课堂实况照片三种文件）</span></div>
                        <ul class="upload_list">
                            <!--上传教案-->
                            <li class="teach_plan">
                                <div class="teach_left">
                                    <p class="teach_title">上传教案<span>word格式</span></p>
                                    <p class="teach_text">文件格式：word(.doc文件)<br>文件命名：以“《作品标题》教案”命名<br><span style="color: #d0021b;">注：请务必使用本活动指定教案模板，<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;未使用模板的作品会被退回哦！</span></p>
                                    <p class="teach_download needtrack downloadTemplate" data_op="o_W2Q2APfTWc"><i class="icon_download"></i>下载模板</p>
                                </div>
                                <!--上传作品后-->
                                <div class="teach_right" style="display: none" data-bind="visible: !isShowUploadWord()">
                                    <div class="teach_right_con">
                                        <p class="icon_word"></p>
                                        <p class="name" data-bind="text: wordName"></p>
                                    </div>
                                    <!-- 进度条 -->
                                    <p class="progress_bar" data-bind="visible: !isShowReUploadWord()" id="wordProgress" style="display: none">
                                        <span class="bg_bar"> <i class="yellow_bar" style="width: 0;"></i></span><span class="value">0%</span>
                                    </p>
                                    <!-- 重新上传按钮 -->
                                    <p class="upload needtrack" data_op="o_i7xcd65o2U" data-bind="visible: isShowReUploadWord(), click: choiceWord"><span>重新上传</span></p>
                                </div>
                                <!--上传作品前-->
                                <div class="teach_right choice_btn needtrack" data_op="o_2WtHdWLCeB" style="display: none" data-bind="
                                    visible: isShowUploadWord(),
                                    click: choiceWord">选择文件</div>

                                <!--阿里云触发dom-->
                                <div id="wordSelect" style="display: none;">选择文件</div>
                                <div id="wordUpload" style="display: none;">开始上传</div>
                            </li>
                            <!--上传课件-->
                            <li class="teach_plan">
                                <div class="teach_left">
                                    <p class="teach_title">上传课件<span>.ppt格式/zip文件</span></p>
                                    <p class="teach_text">上传说明：如课件中插入音频或视频，请与课件PPT一起打包压缩成ZIP文件。<br>文件类型：PPT或者ZIP文件<br>文件大小：不超过100M<br>文件命名：以“《作品标题》课件”命名</p>
                                </div>
                                <div class="teach_right" style="display:none;" data-bind="visible: !isShowUploadCourse()">
                                    <div class="teach_right_con">
                                        <p class="icon_word icon_ppt" data-bind="
                                    css: {
                                        'icon_ppt': courseType() === 'ppt',
                                        'icon_zip': courseType() === 'zip'
                                     }"></p>
                                        <p class="name" data-bind="text: courseName"></p>
                                    </div>
                                    <p class="progress_bar" id="courseProgress" data-bind="visible: !isShowReUploadCourse()" style="display: none;">
                                        <span class="bg_bar"> <i class="yellow_bar" style="width: 0;"></i></span><span class="value">0%</span>
                                    </p>
                                    <!-- 重新上传按钮 -->
                                    <p class="upload needtrack" data_op="o_Fqs9vztm3J" style="display: none;" data-bind="visible: isShowReUploadCourse(), click: choiceCourse"><span>重新上传</span></p>
                                </div>
                                <!--上传作品前-->
                                <div class="teach_right choice_btn needtrack" data_op="o_a5txyQw0m6" style="display: none" data-bind="
                                    visible: isShowUploadCourse(),
                                    click: choiceCourse">选择文件</div>

                                <!--阿里云触发dom-->
                                <div id="courseSelect" style="display: none;">选择文件</div>
                                <div id="courseUpload" style="display: none;">开始上传</div>
                            </li>
                            <!--上传图片-->
                            <li class="teach_plan teach_img">
                                <div class="teach_left">
                                    <p class="teach_title">上传照片<span>.png/jpg格式</span></p>
                                    <p class="teach_text">照片数量：5张<br>照片格式：png或jpg,图片大小不超过2M.建议图片尺寸为240*180</p>
                                </div>
                                <ul class="teach_right img_list"  data-bind="visible: !isShowUploadImages()" style="display: none">
                                    <!-- ko foreach: imagesSrcList -->
                                    <li class="upload_img_box ">
                                        <img alt="图片" class="upload_img" data-bind="attr: { src: url }">
                                    <#--<div class="shadow"></div>-->
                                        <i class="icon_del needtrack" data_op="o_H3zJkdMZoj" data-bind="click: $root.deleteUploadImage.bind($data, $index())"></i>
                                    </li>
                                    <!-- /ko -->
                                    <li class="upload_img_box addBtn needtrack" data_op="o_IivPNAFzOq" data-bind="
                                visible: imagesSrcList().length < 5,
                                click: choiceImages"></li>
                                </ul>
                                <!--上传作品前-->
                                <div class="teach_right choice_btn needtrack" data_op="o_xM58gmOFDN" style="display: none" data-bind="
                            visible: isShowUploadImages(),
                            click: choiceImages">选择文件</div>
                                <input type="file" id="JS-images" accept="image/jpeg,image/jpg,image/png" multiple="multiple" style="display: none;">
                            </li>
                        </ul>
                    </li>
                    <!--退回原因-->
                    <li class="clear" data-bind="visible: courseStatus() === 'REJECTED'" style="display: none">
                        <div class="title">退回原因:<span class="explane multi-line" data-bind="text: rejectCourseInfo"></span></div>
                    </li>
                </ul>
                <div class="a_btns">
                    <div class="btn"></div>
                    <div class="btn"></div>
                </div>
            </div>
            <div class="foot_btns">
                <span class="left" data-bind="click: saveCourse">保 存</span>
                <span class="right active" data-bind="
            css: { 'disabled': !inputCourseName() || isShowUploadPoster() || isShowUploadWord() || isShowUploadCourse() || isShowUploadImages() },
            click: commitCourse">提 交</span>
            </div>
            <div class="noticeBox">我已同意<a href="javascript:void(0);" class="seeJoinNote">活动报名须知</a></div>
        </div>

        <!-- 上传作品成功弹窗 -->
        <div class="coursePopup" style="display: none;" data-bind="visible: isShowUploadSuccess">
            <div class="popupInner">
                <div class="closeBtn" data-bind="click: function () { $root.isShowUploadSuccess(false); }"></div>
                <div class="topContent">
                    <div class="icon-success"></div>
                    <div class="text">您已成功上传！</div>
                    <div class="small-text">您可以在<a href="javascript:void(0)" data-bind="click: seePersonalCenter">个人中心</a>中查看您的上传记录</div>
                </div>
                <div class="otherContent">
                    <a class="continue createCourse" href="javascript:void(0)" data-bind="click: continueUpload">继续上传</a>
                    <a class="btn02" href="javascript:void(0)" data-bind="click: seePersonalCenter">查看记录</a>
                </div>
            </div>
        </div>
        <!-- 删除图片 -->
        <div class="coursePopup" style="display: none" data-bind="visible: isShowDeleteImageSure">
            <div class="popupInner popupInner-del">
                <div class="closeBtn" data-bind="click: function () { $root.isShowDeleteImageSure(false); }"></div>
                <div class="textBox">
                    <p class="boldTxt">确定要删除这张图片吗？</p>
                </div>
                <div class="otherContent">
                    <a class="continue" href="javascript:void(0)" data-bind="click: function () { $root.isShowDeleteImageSure(false); }">取 消</a>
                    <a class="btn02 sure needtrack" data_op="o_KX9xEcIdKM" href="javascript:void(0)" data-bind="click: sureDeleteImage">确 定</a>
                </div>
            </div>
        </div>
    </div>
    <#include "./module/alert.ftl">
    <#include "./module/footer.ftl">
</div>
<script src="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.js')}"></script>
<script src="${getVersionUrl('public/plugin/plupload-1.2.1/plupload.full.min.js')}"></script>
<script>
    var cdnHeader = "<@app.link href='/'/>";
    var userInfo = {};
</script>
</@layout.page>