<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["ko"] css=["homework.pagination","new_teacher.goal"] />

<div id="independentReport" data-bind="if:!webLoading(),visible:!webLoading()" style="display:none;position: relative; zoom: 1;  z-index: 5;">
    <div class="w-base" >
        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3>线上测试</h3>
            <div class="w-base-right w-base-switch">
                <ul>
                    <li class="tab">
                        <a href="/teacher/newexam/independent/index.vpage?subject=${subject!}">
                            <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                            布置专项测试
                        </a>
                    </li>
                    <li class="tab active">
                        <a href="javascript:void(0);">
                            <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                            测试报告
                        </a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="specialTest-clazz">
            <div class="label">班级：</div>
            <div class="clazzOption">
                <!--ko foreach:$root.clazzList-->
                <p data-bind="css:{'w-radio-current' : $root.currentGroupId() == $data.groupId},click:$root.changeClazz.bind($data,$root)">
                    <span class="w-radio"></span>
                    <span class="w-icon-md" data-bind="text:$data.clazzName"></span>
                </p>
                <!--/ko-->
            </div>
        </div>
    </div>
    <div class="w-base" style="padding-top: 18px;">
        <!--ko if: $root.examList && $root.examList().length > 0-->
        <div class="tE-tableList">
            <table cellpadding="0" cellspacing="0">
                <thead>
                    <tr>
                        <td width="200">测试名称</td><td width="120">测试时间</td><td width=120">班级</td><td>参与学生</td><td>完成人数</td><td width="120">操作</td>
                    </tr>
                </thead>
                <tbody>
                    <!--ko foreach: $root.examList-->
                    <tr class="odd" data-bind="css : {'odd': $index()%2==0}">
                        <td><span class="testName" data-bind="text:$data.newExamName,attr:{title:$data.newExamName}"></span></td>
                        <td><span class="timeRow" data-bind="text:$data.startAt + '至' + $data.stopAt"></span></td>
                        <td><span class="clazzName" data-bind="text:$data.clazzName"></span></td>
                        <td><span data-bind="text:$data.joinUserCount"></span></td>
                        <td><span data-bind="text:$data.submitUserCount"></span></td>
                        <td><sapn class="marR-20 textBlue hand" data-bind="click:$root.detailReview">详细报告</sapn><span class="hand" data-bind="click:$root.deleteExam">删除</span></td>
                    </tr>
                    <!--/ko-->
                </tbody>
            </table>
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:$root.termPage,if:$root.termPage != null}"></div>
        <!--/ko-->
        <!--ko if: $root.examList && $root.examList().length == 0-->
        <div class="w-noContent">
            <i class="emptyTips-icon"></i>
            <p class="tipsTxt txtBlue">您还没有布置过模考试卷哦～</p>
            <p class="tipsTxt">快去布置吧～</p>
        </div>
        <!--/ko-->
    </div>
</div>


<#include "../templates/kopagination.ftl">

<script type="text/javascript">
    var constantObj = {
        subject         : "${subject!}",
        imgDomain       : '${imgDomain!''}',
        domain          : '${requestContext.webAppBaseUrl}/',
        env             : <@ftlmacro.getCurrentProductDevelopment />
    };
    $(function(){

        LeftMenu.focus("${subject!}_newexam");
    });
</script>
    <@sugar.capsule js=["homeworkv3.newexamreport"] />
</@shell.page>