<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        音频文稿
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" method="get" action="/opmanager/talk/audiolist.vpage">
                <ul class="inline">

                    <li>
                        <label>
                            <input type="text" name="title" value="${title!''}" placeholder="文稿名称"/>
                        </label>
                    </li>
                    <li>
                        <label>所属话题&nbsp;
                            <select name="topicId" id="topicId" data="${topicId!''}">
                                    <option value="">所属话题</option>
                                <#if topics?exists>
                                    <#list topics as topic>
                                        <option value="${topic.topicId!}">${topic.title!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </label>
                    </li>
                    <li>
                        <button type="submit" id="filter" class="btn btn-primary">查  询</button>
                    </li>

                </ul>
            </form>
        </div>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>文稿id</th>
                <th>文稿名称</th>
                <th>创建时间</th>
                <th>发布人</th>
                <th>所属话题</th>
                <th>话题讲解</th>
                <th>展示状态</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <#if list?exists>
            <#list list as audio>
                <tr>
                    <td>${audio.audioId!''}</td>
                    <td>${audio.title!''}</td>
                    <td>${audio.createTime!''}</td>
                    <td>${audio.publisher!''}</td>
                    <td>${audio.topic!''}</td>
                    <td>${audio.publishTime!''}</td>
                    <td></td>
                    <td>
                        <#assign flag = audio.flag!''>
                        <#if flag == "cancel">
                            <a href="#" title="播放前5分钟内或者播放中无法编辑">禁止编辑中</a>
                        <#else>
                            <a href="audiopublish.vpage?audioId=${audio.audioId}">编辑</a> |
                            <a href="#" data-value="${audio.audioId!''}" data-action="delete">删除</a>
                        </#if>
                        </td>
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
                var audioId = $(this).attr("data-value");
                $.post("/opmanager/talk/deleteautio.vpage", {audioId: audioId}, function (data) {
                    if (data.success) {
                        document.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("#topicId").val($("#topicId").attr("data"));
    })
</script>

</@layout_default.page>