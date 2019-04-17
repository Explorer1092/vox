<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='视频管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    .btn{
        margin-bottom: 10px;
    }
</style>
<div id="main_container" class="span9">
    <legend>
        视频管理
    </legend>
    <div >
        <a class="btn btn-primary" href="/chips/ai/video/addform.vpage">添加-视频</a>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>视频标签</td>
                        <td>视频Id</td>
                        <td>主标题</td>
                        <td>视频url</td>
                        <td>更新日期</td>
                        <td>操作</td>
                    </tr>
                    <#if result?? >
                        <#list result as r >
                            <tr>
                                <td><span class="label label-info">
                                    <#if r.type == "HOT_VIDEO">热门
                                    <#elseif r.type == "ACTIVITY_VIDEO">精选活动
                                    <#elseif r.type == "FUNNY_VIDEO">搞笑视频
                                    <#else>今日精讲
                                    </#if>
                                    </span></td>
                                <td>${r.id!}</td>
                                <td>${r.title!}</td>
                                <td>${r.videoUrl!}</td>
                                <td>${r.updateTime!}</td>
                                <td>
                                    <a href="/chips/ai/video/addform.vpage?id=${r.id!}" name="edit" data-id="${r.id!}">编辑</a>
                                    <a href="javascript:void(0);" name="remove" remove="${r.id!}">删除</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){

        $("a[name=remove]").click(function () {
            if (!confirm("确定删除数据吗？")) {
                return;
            }
            var id = $(this).attr("remove");
            debugger;
            $.post('${requestContext.webAppContextPath}/chips/ai/video/delete.vpage', {id: id}, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

    });
</script>
</@layout_default.page>