<#import "module.ftl" as temp />
<@temp.page level="统考报告">
<@sugar.capsule js=["echarts-3.4.0","ko","jplayer","plugin.newexamv3"]/>
<div class="link-page">
    <a class="itm-page" href="/teacher/index.vpage">首页 <i></i></a>
    <a class="itm-page" href="/teacher/newexam/report/index.vpage?subject=${subject!}">考试报告 <i></i></a>
    <a class="current-page" href="javascript:void(0);">详情报告</a>
</div>
<div class="mk-main" id="newexamv2Root">
    <div class="h-mk-answer" style="display: none;" data-bind="if:!$root.hsLoading(),visible:!$root.hsLoading()">
        <div class="info-test">
            <p class="info-clazz"><!--ko text:$root.result().newExamName--><!--/ko--><i></i><!--ko text:$root.result().clazzName--><!--/ko--></p>
            <p class="info-tips" style="display: none;" data-bind="visible:$root.result().hasOral">(<!--ko text:$root.result().correctStartTime--><!--/ko-->至<!--ko text:$root.result().correctStopTime--><!--/ko-->可修改口语题成绩)</p>
        </div>
        <div class="md-swicth">
            <ul class="box-swicth" data-bind="foreach:{data : $root.tabList,as:'tab'}">
                <li class="active" data-bind="css:{'active' : $index() == $root.focusTabIndex()},text:tab.name,click:$root.tabClick.bind($data,$index(),$root)">&nbsp;</li>
            </ul>
        </div>
        <!-- 主体内容 -->
        <div id="tabContent">

        </div>
    </div>
</div>
<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<#include "template/viewpaper.ftl">
<#include "template/viewstudents.ftl">
<#include "template/analyzepaper.ftl">
<#include "template/ztfxreport.ftl">

<script type="text/javascript">
    var constantObj = {
        clazzId     : "${clazzId}",
        newExamId   : "${newExamId!}",
        subject     : "${subject!}",
        imgDomain   : "${imgDomain!}",
        env : <@ftlmacro.getCurrentProductDevelopment />
    };
</script>
    <@sugar.capsule js=["newexamv3.history"]/>
</@temp.page>