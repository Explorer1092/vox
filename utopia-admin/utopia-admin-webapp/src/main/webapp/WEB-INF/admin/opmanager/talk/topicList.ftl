<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        话题列表
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" method="get" action="/opmanager/talk/topiclist.vpage">
                <ul class="inline">

                    <li>
                        <label>
                            <input type="text" name="title" value="${title!''}" placeholder="话题名称"/>
                        </label>
                    </li>
                    <li>
                        <label>发布时间&nbsp;
                            <input id="startTime" name="startTime" value="${startTime!''}" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="起始" value="">
                            至
                            <input id="endTime" name="endTime" value="${endTime!''}" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="结束" value="">
                        </label>
                    </li>
                    <li>
                        <button type="submit" id="filter" class="btn btn-primary">查 询</button>
                    </li>

                </ul>
            </form>
        </div>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>话题id</th>
                <th>话题名称</th>
                <th>话题介绍</th>
                <th>类型</th>
                <th>发布人</th>
                <th>发布时间</th>
                <th>报名</th>
                <th>评论</th>
                <th>点赞</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <#if topics?exists>
            <#list topics as topic>
                <tr>
                    <td>${topic.topicId!''}</td>
                    <td>${topic.title!''}</td>
                    <td>${topic.intro!''}</td>
                    <td>${topic.type!''}</td>
                    <td>${topic.publish!''}</td>
                    <td>${topic.time!''}</td>
                    <td>${topic.option!''}</td>
                    <td>${topic.reply!''}</td>
                    <td>${topic.vote!''}</td>
                    <td><a href="topicpublish.vpage?topicId=${topic.topicId}">编辑</a> <a href="#"
                                                                                        data-value="${topic.topicId!''}"
                                                                                        data-action="delete">删除</a></td>
                </tr>
            </#list>
            </#if>
            </tbody>
        </table>
        <ul class="pager" data-index="${pageIndex}">
            <#if pageCount?exists && pageCount gt 0>
            <#list 1 .. pageCount as page>
                <li data-index="${page}"><a href="${query}${page}">${page}</a></li>
            </#list>
            </#if>
        </ul>
    </div>
</div>

<script lang="javascript">
    $(document).ready(function () {
        var index = $(".pager").attr("data-index");
        $(".pager li").each(function () {
            if($(this).attr("data-index") == index){
                $(this).addClass("disabled");
            }
        });

        $(".form_datetime").datetimepicker({
            autoclose: true,
            minuteStep: 5,
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("a[data-action='delete']").click(function () {
            if (confirm("确实要删除么?")) {
                var topicId = $(this).attr("data-value");
                $.post("/opmanager/talk/deletetopic.vpage", {topicId: topicId}, function (data) {
                    if (data.success) {
                        document.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
    })
</script>

</@layout_default.page>