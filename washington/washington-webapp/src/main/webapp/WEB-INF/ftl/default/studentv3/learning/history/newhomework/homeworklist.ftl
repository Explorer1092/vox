<#import "../../module.ftl" as module>
<@module.learningCenter menuName='record'>
    <@sugar.capsule css=["homeworkhistory.report"] />
<div class="h-historyWork w-fl-right h-historyBox">
    <div class="h-title-2">
        <span class="left-text"></span>
    </div>
    <div class="J_mainContent" style="margin-top:20px"></div>
</div>
<div class="message_page_list" id="sharingPage"></div>
<script type="text/javascript">
    var $homeworkReportList = {
        env : <@ftlmacro.getCurrentProductDevelopment />
    }
</script>
    <@sugar.capsule js=["studentreport.list"] />
</@module.learningCenter>