<#import "eduadminlayout.ftl" as module>
<@module.page
title="班级管理"
pageCssFile={"" : [""]}
pageJs=["app","jquery"]
pageJsFile={"app" : "public/script/specialteacher/app"}
>
<div class="class-content mt-20 columnRight">
    <div class="claManagement-content">
        <div class="management-column">
            <select class="sel" data-bind="
                 options: adjustGrades,
                 optionsText: 'gradeName',
                 value:adjustGrade,
                 optionsCaption: '选择年级',
                 visible:isAdjustClassPage(),
                 event:{change:changeSelectAdjustGrade}">
            </select>
            <div class="search-btn fr">
                <input type="text" placeholder="学生姓名／学号／填涂号" data-bind="value:searchStudentKey,event:{keyup:searchStuKeyEvent}"><a href="javascript:void(0);" class="search_btn" data-bind="click:searchStudent">搜索</a>
            </div>
        </div>

        <div class="operate-box" data-bind="visible: !isShowMerge()" style="display: none;">
            <#--新增班群-->
            <div class="add-btn mt-20 fr" data-bind="visible:((isGradeCard() && gradeCardListData().administrativeClass.length === 0) || (isClassCard() && clazzCardListData().groups.length === 0))">
                <a href="javascript:void(0);" class="add_btn" data-bind="click:addClazzGroup"><i></i> 新增老师带班</a>
                <a href="javascript:void(0);" class="icon-greenAsk global-ques">
                    <div class="text addClassGroupTip">1，班群是在校内班级的基础上建立的群体；<br>2，正常情况下，一般一个班级中只有一个班群；<br>3，如果出现一个班级中，有多个班群，请核对各科老师及学生信息是否正确，可使用合并班群功能。合并后，各科老师共享同一个班群及学生名单；<br>4，如出现异常可联系客服进行处理。<br></div>
                </a>
            </div>
            <#--新增校内班级（点击调整班级时显示）-->
            <div class="add-btn mt-20 fr" data-bind="visible:isAdjustClassPage()">
                <a href="javascript:void(0);" class="add_btn" data-bind="click:createSchoolClass"><i></i> 新建校内班级</a><a href="javascript:void(0);" class="icon-answer"></a>
            </div>
            <#--合并班群（非调整班级）-->
            <#--visible判断条件：(点击年级时列表>1） 或 （点击班级时，行政班列表>1）-->
            <div class="add-btn merge-btn mt-20 fr" data-bind="visible:((isGradeCard() && gradeCardListData().administrativeClass.length > 1) || (isClassCard() && clazzCardListData().groups.length > 1))">
                <a href="javascript:void(0);" class="add_btn" data-bind="click:mergeGroup"><i></i> 合并班群</a>
            </div>
        </div>

        <#-- 合并-->
        <div class="merge-box" data-bind="visible: isShowMerge()" style="display: none;">
            <div class="add-btn merge-btn mt-20 fr">
                <a href="javascript:void(0);" class="add_btn merge_cancel" data-bind="click:cancelMerge">取消</a>
                <a href="javascript:void(0);" class="icon-greenAsk global-ques">
                    <div class="text addClassGroupTip">选择要合并的班群，点击合并按钮（至少选择两个班群）</div>
                </a>
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
</@module.page>