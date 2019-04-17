<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='任务对话管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    .btn{
        margin-bottom: 10px;
    }
</style>
<div id="main_container" class="span9">
    <legend>
        对话剧本管理
    </legend>
    <div >
        <a class="btn btn-primary" href="/chips/ailesson/play/adddrama.vpage">添加-对话剧本</a>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>课程Id</td>
                        <td>对话标题</td>
                        <td>更新日期</td>
                        <td>操作</td>
                    </tr>
                    <#if result?? >
                        <#list result as r >
                            <tr>
                                <td>${r.id!}</td>
                                <td>${r.title!}</td>
                                <td>${r.updateTime!}</td>
                                <td>
                                    <a href="/chips/ailesson/play/adddrama.vpage?id=${r.id!}" name="edit" data-id="${r.id!}">编辑</a>
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
            $.post('${requestContext.webAppContextPath}/chips/ailesson/play/delete.vpage', {id: id}, function (res) {
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