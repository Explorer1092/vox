<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["ko"] css=["homeworkv3.homework","vacation.winterhomework"] />

<div class="h-homeworkCorrect">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a data-bind="attr:{href:'/teacher/vacation/report/list.vpage?subject='+$root.subject()}">假期作业列表</a>&gt;<a data-bind="attr:{href:'/teacher/vacation/report/clazzreport.vpage?packageId='+$root.packageId()}"><span data-bind="text:clazzName()"></span>练习报告</a>&gt;<span data-bind="text:studentName()"></span><span>任务列表</span>
    </h4>
</div>
<div id="id="mainContent"">
<!--ko if:$root.weekPlans && $root.weekPlans().length > 0-->
    <!--ko foreach:{ data: $root.weekPlans(), as:'item'}-->
    <div class="w-base" style="position: relative; zoom: 1;  z-index: 5;">

        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3><span data-bind="text:item.title"></span><span class="win-titleBar" data-bind="text:item.scope"></span></h3>
        </div>
        <!--ko foreach:{data:item.dayPlans,as:'itemChild'}-->
        <div class="win-column">
            <div class="cRight">
                <a href="javascript:void(0);" class="cBtn fontGray" data-bind="visible:itemChild.vacationHomeworkStudentDetail.finish,text:itemChild.vacationHomeworkStudentDetail.repair ? '补做完成' : '已完成'">&nbsp;</a>
                <a href="javascript:void(0);" class="cBtn fontOrange" data-bind="visible:!itemChild.vacationHomeworkStudentDetail.finish">未完成</a>
                <a href="javascript:void(0);" class="cBtn fontBlue" data-bind="visible:itemChild.vacationHomeworkStudentDetail.finish, click:$root.viewDetail.bind($data,$element,$root),attr:{'data-num':itemChild.vacationHomeworkStudentDetail.homeworkId}">查看详情</a>
            </div>
            <div class="cDays"><span data-bind="text:itemChild.dayRankStr">Day1</span></div>
            <div class="cSide" data-bind="css:{'sideTwo':itemChild.vacationHomeworkStudentDetail.finish == false ? 'sideTwo':''}">
                <p class="font-black" data-bind="text:itemChild.name">基础巩固练一练</p>
                <p class="font-black" data-bind="text:itemChild.desc">同步练习+基础练习</p>
                <!--ko if:itemChild.vacationHomeworkStudentDetail.finish -->
                <p class="font-gray"  data-bind="visible:itemChild.vacationHomeworkStudentDetail.finish">分数
                    <span data-bind="text:itemChild.vacationHomeworkStudentDetail.score"></span>，
                    完成时间：<span data-bind="text:itemChild.vacationHomeworkStudentDetail.finishAt"></span>，
                    用时<span data-bind="text:itemChild.vacationHomeworkStudentDetail.duration"></span>分钟</p>
                <!--/ko-->
            </div>
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
<!--/ko-->
</div>


<script type="text/javascript">
    var stuWeekDetail = {
        subject : "${subject!}"
    };
    $(function(){
        LeftMenu.focus("${subject!"ENGLISH"}_vacationhistory");
    });
</script>
<@sugar.capsule js=["vacationhistory.studentweekdetail"] />
</@shell.page>