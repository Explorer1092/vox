<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["ko"] css=["homeworkv3.homework","vacation.winterhomework"] />

<div id="mcontent">
    <div class="h-homeworkCorrect">
        <h4 class="link">
            <a href="/">首页</a>&gt;<a data-bind="attr:{href:'/teacher/vacation/report/list.vpage?subject='+$root.subject()}">假期作业列表</a>&gt;<span data-bind="text:clazzName()"></span><span>作业报告</span>
        </h4>
    </div>
    <div id="mainContent" class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3 data-bind="text:clazzName()"></h3>
        </div>
        <div class="win-subTitle">人数统计</div>
        <div class="win-list">
            <ul>
                <li>
                    <div class="lNum"><span data-bind="text:finishedStudentNum()"></span>/<span data-bind="text:totalStudentNum()"></span></div>
                    <div class="lSub">完成人数</div>
                </li>
                <li>
                    <div class="lNum"><span data-bind="text:beginVacationHomeworkNum()"></span>/<span data-bind="text:totalStudentNum()"></span></div>
                    <div class="lSub">开始人数</div>
                </li>
            </ul>
        </div>
        <div class="win-subTitle">学生作业情况</div>
        <div class="win-table">
            <ul>
                <!--ko foreach:{data:$root.vacationHomeworkStudentPanoramas(), as:'item'}-->
                <li>
                    <div class="tNumber" data-bind="text:$index() == $index()?$index()+1:$index()"></div>
                    <div class="tName" data-bind="text:item.studentName || '&nbsp;'"></div>
                    <div class="tName">完成进度：<span data-bind="text:item.finishedHomeworkNum"></span>/<span data-bind="text:item.totalHomeworkNum"></span></div>
                    <div class="tBtn">
                        <a href="javascript:void(0);" class="fontBlue-btn" data-bind="click:$root.viewPersonReport.bind($data,$element,$root)">查看详情</a>
                    </div>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
</div>

<script type="text/javascript">
    var clazReportObj = {
        subject : "${subject!}"
    };
    $(function(){
        LeftMenu.focus("${subject!"ENGLISH"}_vacationhistory");
    });
</script>
<@sugar.capsule js=["vacationhistory.clazzreport"] />
</@shell.page>