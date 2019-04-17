<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        话题列表
    </legend>
    <div class="row-fluid">
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>话题id</th>
                <th>话题名称</th>
                <th>发布人</th>
                <th>发布时间</th>
                <th>报名人数</th>
                <th>视频人数</th>
                <th>点赞量</th>
                <th>评论量</th>
                <th>评论人数</th>
                <th>引用量</th>
                <th>引用人数</th>
                <th>统计时间</th>
            </tr>
            </thead>
            <tbody>
            <#if topics?exists>
            <#list topics as topic>
                <tr>
                    <td>${topic.topicId!''}</td>
                    <td>${topic.title!''}</td>
                    <td>${topic.publish!''}</td>
                    <td>${topic.time!''}</td>
                    <td>${topic.option!''}</td>
                    <td>${topic.video!''}</td>
                    <td>${topic.vote!''}</td>
                    <td>${topic.reply!''}</td>
                    <td>${topic.replier!''}</td>
                    <td>${topic.quoteReply!''}</td>
                    <td>${topic.quoteReplyUser!''}</td>
                    <td>${topic.statisticsTime!''}</td>
                </tr>
            </#list>
            </#if>
            </tbody>
        </table>
    </div>
</div>
</@layout_default.page>