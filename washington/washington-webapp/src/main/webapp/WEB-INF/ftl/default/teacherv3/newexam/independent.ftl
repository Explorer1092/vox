<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["ko","datepicker"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework"] />

<div id="arrangenewexam" class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
    <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
        <h3>线上测试</h3>
        <div class="w-base-right w-base-switch">
            <ul>
                <li class="tab active">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        布置专项测试
                    </a>
                </li>
                <li class="tab">
                    <a href="/teacher/newexam/independent/report.vpage?subject=${subject!}">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        测试报告
                    </a>
                </li>
            </ul>
        </div>
    </div>
    <div class="w-base-container">
        <div id="level_and_clazzs" class="t-homework-form" style="overflow: visible;">
            <dl>
                <dt>班级</dt>
                <dd style="*zoom:1; position: relative;">
                    <div data-bind="template:{name : 't:年级和班级'}" class="w-border-list t-homeworkClass-list"></div>
                </dd>
            </dl>
            <div style="display: none;" data-bind="if:showClazzList().length == 0,visible:showClazzList().length == 0,css:{'w-noData-box' : showClazzList().length == 0}">
                <!--ko if:$root.hasStudents() != null && $root.hasStudents()-->
                您需要检查班级上一次作业，才可以布置新作业，去<a href="/teacher/new/homework/report/list.vpage?subject=${subject!}" class="w-blue">检查作业</a>
                <!--/ko-->
                <!--ko ifnot:$root.hasStudents() != null && $root.hasStudents()-->
                您班级的学生数为0，需添加后才可以布置新作业，去<a class="w-blue" href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage">添加学生</a>
                <!--/ko-->
            </div>
        </div>
        <div id="paperModule" style="display: none;" data-bind="if:!webLoading(),visible:!webLoading()">
            <div class="t-homework-form" style="overflow: visible;">
                <dl style="overflow: visible; z-index: 12;">
                    <dt>教材</dt>
                    <dd  style=" position: relative;">
                        <div class="text">
                            <span data-bind="text:$root.bookName()"></span>
                            <a class="w-blue" href="javascript:void(0);" style="margin-left: 50px;" data-bind="click:$root.changeBook">
                                更换教材<span class="w-icon-public w-icon-switch w-icon-switchBlue" style="margin-right: -5px; margin-left: 10px; *margin: 3px 0 0 10px;"></span>
                            </a>
                        </div>
                    </dd>
                </dl>
            </div>
            <!--ko if: $root.paperModuleList().length > 0-->
            <div class="specialTest" style="border:0px;">
                <!--ko foreach: $root.paperModuleList-->
                <div class="specialTest-section">
                    <div class="sTest-title">
                        <p class="tit01" data-bind="text:$data.moduleName + '（每份试卷' + $data.score + '分）'"></p>
                        <p class="tit02" data-bind="text:$data.description"></p>
                    </div>
                    <div class="sTest-main">
                        <!--ko foreach:$data.paperList-->
                        <div class="sTest-list">
                            <span class="preBtn" data-bind="click:$root.paperDetail.bind($data,$root)">预览</span>
                            <div class="info">
                                <p class="name"><span class="maxWidth" data-bind="text:$data.paperName,attr:{title:$data.paperName}"></span><span data-bind="text:'（共'+$data.questionCount+'题）'"></span></p>
                                <p class="time" data-bind="text:'作答时长：'+$data.durationMinutes+'分钟'"></p>
                            </div>
                        </div>
                        <!--/ko-->
                    </div>
                </div>
                <!--/ko-->
            </div>
            <!--/ko-->
            <!--ko if: $root.paperModuleList().length == 0-->
            <div class="h-set-homework current">
                <div class="seth-mn">
                    <div class="testPaper-info">
                        <div class="inner" style="padding: 15px 10px; text-align: center;">
                            <p>所选教材暂无试卷</p>
                        </div>
                    </div>
                </div>
            </div>
            <!--/ko-->
        </div>
    </div>
</div>

<#include "../templates/homeworkv3/levelandclazzs.ftl">
<#include "../templates/homeworkv3/changebook.ftl">
<#include "../templates/homeworkv3/newexamreview.ftl">
<#include "../templates/homeworkv3/newexamconfirm.ftl">

<script type="text/javascript">
    var constantObj = {
        term            : ${term!1},
        subject         : "${subject!}",
        currentDateTime : "${currentDateTime}",
        endDate         : "${endDate!}",
        endTime         : "${endTime!}",
        batchclazzs     : ${batchclazzs![]},
        imgDomain       : '${imgDomain!''}',
        domain          : '${requestContext.webAppBaseUrl}/',
        env             : <@ftlmacro.getCurrentProductDevelopment />
    };
    $(function(){

        LeftMenu.focus("${subject!}_newexam");
    });
</script>
    <@sugar.capsule js=["homework2nd","homeworkv3.newexam"] />
</@shell.page>