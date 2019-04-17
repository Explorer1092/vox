<#import "layout_default.ftl" as layout_default>
<#import "crm/headsearch.ftl" as headsearch>
<@layout_default.page page_title='17zuoye Manage System'>


    <@headsearch.headSearch/>
    <#if myNewFeedbackCount?? && myNewFeedbackCount gt 0>
        <h4 class="font-zh">
            <a href="javascript:void(0);" id="feedback">您有${myNewFeedbackCount}条未处理的用户反馈</a>
        </h4>
        <form id="feedbackform" method="post" action="/crm/feedback/feedbackindex.vpage" class="form-horizontal">
            <input type="hidden" name="watcher" value="${watcher}"/>
            <input type="hidden" name="startDate" value="${startDate}"/>
            <input type="hidden" name="feedbackState" value="0"/>
        </form>
    <#else>
        <#if adminIndexPageBlockContent?has_content>
            ${adminIndexPageBlockContent}
        <#else>
            <h1 class="font-zh">Welcome 17zuoye manage system</h1>
            <p></p>
        </#if>
    </#if>
<script type="text/javascript">
    $(function(){
        $('#feedback').on('click',function(){
            $('#feedbackform').submit();
        });
    });
</script>
</@layout_default.page>
