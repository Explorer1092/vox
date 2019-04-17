<#import "../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起作业-教务老师"
pageJs=["common", "clazzmanageapp", "jquery"]
pageJsFile={"common": "public/script/specialteacherV2/common", "clazzmanageapp": "public/script/specialteacherV2/clazzmanageapp"}
pageCssFile={"index" : ["/public/skin/specialteacherV2/css/skin"]}>

    <#include "../specialteacherV2/header.ftl">
<div id="page_bd" style="min-height: 680px; display: block;">
<#--左侧导航-->
    <div id="main_nav">
        <ul>
            <li class="nav_user">
                <a href="/specialteacher/center/index.vpage">
                    <img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt="" class="nav_user_favicon">
                </a>
                <div class="nav_user_information">
                    <p class="nav_user_username">${(currentUser.profile.realname)!}</p>
                <#--<a href="//ucenter.17zuoye.com/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage">-->
                <#--<i class="nav_user_no_certified"></i>-->
                <#--</a>-->
                </div>
            </li>
            <li class="line"></li>

        <#--班级管理-->
            <li class="nav_plan JS-clazzManageBox tab-active bg-active color-active">
                <a href="javascript:void(0);"><i class="icon"></i>班级管理<i class="arrow" style="display: none;"></i></a>
            </li>

        <#--老师学生管理-->
            <li class="nav_school_bank JS-teacStuManageBox">
                <a href="javascript:void(0);"><i class="icon"></i>老师学生管理<i class="arrow"></i></a>
            </li>

        <#--个人中心-->
            <li class="nav_my_favorites JS-personCenterBox">
                <a href="javascript:void(0);"><i class="icon "></i>个人中心<i class="arrow"></i></a>
            </li>
        </ul>
    </div>

<#--右侧主体-->
    <div id="page_body" style="min-height: 640px;">
        <div style="position:absolute; z-index: 1000; width: 812px; padding:300px 0;text-align: center;font-size:18px;color:#4d4d4d;background-color: #ffffff;" data-bind="visible: !($root.gradeMenu().hasData)">Loading...</div>
        <div class="claManagement-content" style="display: none;" data-bind="visible: $root.gradeMenu().hasData">
            <div class="management-column">
                <div class="choice-box">
                    <select class="choice-grade" data-bind="
                        options: $root.gradeMenu().menu,
                        optionsText: 'gradeName',
                        value: $root.gradeMenu().gradeInfo,
                        event:{change:$root.changeGrade}">
                    </select>
                    <!-- ko with:$root.gradeMenu().gradeInfo -->
                    <select class="choice-class" data-bind="
                        options: clazzs,
                        optionsText: 'clazzName',
                        value: $root.gradeMenu().clazzInfo,
                        optionsCaption: '选择班级',
                        event:{change:$root.changeClass}">
                    </select>
                    <!-- /ko -->
                </div>

            <#--选择年级：调整班级用(调整班级整体拆分)-->
            <#--<select class="sel" data-bind="-->
            <#--options: adjustGrades,-->
            <#--optionsText: 'gradeName',-->
            <#--value:adjustGrade,-->
            <#--optionsCaption: '选择年级',-->
            <#--visible:isAdjustClassPage(),-->
            <#--event:{change:changeSelectAdjustGrade}">-->
            <#--</select>-->
                <div class="search-btn fr">
                    <input type="text" placeholder="学生姓名／学号／填涂号" data-bind="value:searchStudentKey,event:{keyup:searchStuKeyEvent}"><a href="javascript:void(0);" class="search_btn" data-bind="click:searchStudent">搜索</a>
                </div>
            </div>

            <div class="operate-box" data-bind="visible: !isShowMerge()" style="display: none;">
            <#--帮助icon-->
            <#--visible判断条件：新增老师带班 和 合并班群都不显示时 隐藏-->
                <div class="add-btn merge-btn mt-20 fr iconbox" data-bind="visible: !(!(isGradeCard() || (isClassCard() && clazzCardListData().realGroupLength == 0)) && !((isGradeCard() && $root.isHasSameClassMultiGroup()) || (isClassCard() && clazzCardListData().realGroupLength > 1)))">
                    <a href="javascript:void(0);" class="icon-greenAsk global-ques">
                        <div class="text addClassGroupTip">1，班群是在校内班级的基础上建立的群体；<br>2，正常情况下，一般一个班级中只有一个班群；<br>3，如果出现一个班级中，有多个班群，请核对各科老师及学生信息是否正确，可使用合并班群功能。合并后，各科老师共享同一个班群及学生名单；<br>4，如出现异常可联系客服进行处理。<br></div>
                    </a>
                </div>

            <#--新增班群-->
            <#--visible判断条件：(点击年级时） 或 （点击班级时，行政班列表=1）-->
                <div class="add-btn mt-20 fr" data-bind="visible:(isGradeCard() || (isClassCard() && clazzCardListData().realGroupLength == 0))">
                    <a href="javascript:void(0);" class="add_btn" data-bind="click:addClazzGroup"><i></i> 新增老师带班</a>
                </div>

            <#--新增校内班级（点击调整班级时显示）-->
            <#--<div class="add-btn mt-20 fr" data-bind="visible:isAdjustClassPage()">-->
            <#--<a href="javascript:void(0);" class="add_btn" data-bind="click:createSchoolClass"><i></i> 新建校内班级</a><a href="javascript:void(0);" class="icon-answer"></a>-->
            <#--</div>-->

            <#--合并班群（非调整班级）-->
            <#--visible判断条件：(点击年级时存在大于两个班群的班级） 或 （点击班级时，行政班列表>1）-->
                <div class="add-btn merge-btn mt-20 fr" data-bind="visible:((isGradeCard() && $root.isHasSameClassMultiGroup()) || (isClassCard() && clazzCardListData().realGroupLength > 1))">
                    <a href="javascript:void(0);" class="add_btn" data-bind="click:mergeGroup"><i></i> 合并班群</a>
                </div>

            <#--更新学生学号-->
            <#--visible判断条件：(点击年级时） 或 （点击班级时）-->
                <div class="add-btn updateno-btn mt-20 fr" data-bind="visible:(isGradeCard() || isClassCard())">
                    <a href="javascript:void(0);" class="add_btn" data-bind="click:updateStuNo"><i></i> 更新学生学号</a>
                </div>

            <#--批量生成学科组-->
            <#--visible判断条件：后端返回permission为true 且 (点击年级时） 或 （点击班级时）-->
                <div class="add-btn genelalsubject-btn mt-20 fr" data-bind="visible: isShowBatchGenerateSubjectBtn() && (isGradeCard() || isClassCard())">
                    <a href="javascript:void(0);" class="add_btn" data-bind="click:batchGeneralSubject"><i></i> 批量生成学科组</a>
                </div>
            </div>

        <#-- 合并-->
            <div class="merge-box" data-bind="visible: isShowMerge()" style="display: none;">
                <div class="add-btn merge-btn mt-20 fr iconbox">
                    <a href="javascript:void(0);" class="icon-greenAsk global-ques">
                        <div class="text addClassGroupTip">选择要合并的班群，点击合并按钮（至少选择两个班群）</div>
                    </a>
                </div>
                <div class="add-btn merge-btn mt-20 fr">
                    <a href="javascript:void(0);" class="add_btn merge_cancel" data-bind="click:cancelMerge">取消</a>
                </div>
                <div class="add-btn merge-btn mt-20 fr">
                    <a href="javascript:void(0);" class="add_btn merge_sure disabled JS-mergeBtn" data-bind="click:sureMerge">合并</a>
                </div>
            </div>
            <div style="clear:both;"></div>
        </div>

    <#--年级卡片页-->
        <div data-bind="template: { name: 'gradeCardListTemp', data: gradeCardListData },visible:isGradeCard()"></div>

    <#--班级卡片页-->
        <div data-bind="template: { name: 'classCardListTemp', data: clazzCardListData },visible:isClassCard()"></div>

    <#--班群详情页-->
        <div data-bind="template: { name: 'classGroupDetailTemp', data: classGroupDetailData },visible:isClassGroupDetail()"></div>

    <#--调整班级页-->
        <div data-bind="template: { name: 'adjustClassTemp', data: adjustClassData },visible:isAdjustClassPage()"></div>
    </div>
</div>
    <#include "../specialteacherV2/footer.ftl">
    <#include "../specialteacherV2/clazzmanagetemp.ftl">
</@layout.page>