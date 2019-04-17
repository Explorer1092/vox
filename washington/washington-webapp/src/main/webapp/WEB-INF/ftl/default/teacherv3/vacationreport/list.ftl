<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["ko"] css=["homeworkv3.homework"] />

<div id="vacationHomeworkList" class="h-homeworkList">
    <!--ko if:homeworkList().length > 0 && !hkLoading()-->
    <!--ko foreach:{data:homeworkList(),as:'homework'}-->
    <div class="h-workList-box">
        <div class="hwl-header">
            <span data-bind="text:homework.className() + '&nbsp;&nbsp;' + (homework.subjectName != null ? homework.subjectName() : '') +  '假期作业'"></span>
        </div>
        <div class="hwl-main">
            <table class="hwl-table">
                <tr>
                    <td class="td-cell01">
                        <div class="title">开始时间：<span data-bind="text:homework.startTime()"></span></div>
                        <div class="title">截止时间：<span data-bind="text:homework.endTime()"></span></div>
                    </td>
                    <td class="td-cell02">
                        <p class="txt-green"><span class="font-b" data-bind="text:homework.finishNum()">0</span>/<span data-bind="text:homework.totalNum()">0</span>人</p>
                        <p class="txt-green">完成人数</p>
                    </td>
                    <td class="td-cell02" style="width: 200px;">
                        <p class="txt-green"><span class="font-b" data-bind="text:homework.beginNum()">0</span>/<span data-bind="text:homework.totalNum()">0</span>人</p>
                        <p class="txt-green">开始人数</p>
                    </td>
                    <td class="td-cell03">
                        <#--<a href="javascript:void(0);" data-bind="visible:!homework.ableToDelete || homework.ableToDelete(),click:$root.deleteHomework" class="link">删除</a>-->
                        <a href="javascript:void(0);" data-bind="click:$root.viewReport" class="w-btn w-btn-well">查看详情</a>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->
    <!--ko if:homeworkList().length == 0 && hkLoading()-->
    <div data-bind="template:{name:'t:加载中'}"></div>
    <!--/ko-->
    <div class="h-workList-box" style="display: none;" data-bind="if:homeworkList().length == 0 && !hkLoading(),visible:homeworkList().length == 0 && !hkLoading()">
        <div class="hwl-main">
            <p style="text-align: center;padding: 80px 0;">您还没有布置假期作业哦~ </p>
        </div>
    </div>
</div>

<script type="text/javascript">
    var constantObj = {
        subject        : "${(subject)!}"
    };

    $(function(){
        LeftMenu.focus("${subject!'ENGLISH'}_vacationhistory");
    });
</script>
<@sugar.capsule js=["vacationhistory.list"] />
</@shell.page>