<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='任务对话管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    .btn{
        margin-bottom: 10px;
    }
</style>
<div id="main_container" class="span9">
    <legend>
        小程序二维码管理
    </legend>
    <div >
        <a class="btn btn-primary" href="/chips/ailesson/task/addform.vpage">添加</a>
        <a class="btn btn-primary" href="/chips/ailesson/data/export.vpage?type=miniprogram&page=${current! 0}">导出</a>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>内容</td>
                        <td>创建时间</td>
                        <td>小程序码</td>
                        <td>操作</td>
                    </tr>
                    <#if result?? >
                        <#list result as r >
                            <tr>
                                <td>${r.content!}</td>
                                <td>${r.createDate!}</td>
                                <td><img src="${r.image!}" width="120" height="120"/></td>
                                <td>
                                    <a href="javascript:void(0);" name="remove" remove="${r.id!}">删除</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
            <ul class="pager">
                <li><a href="#" onclick="pagePost(${current-1})" title="Pre">上一页</a></li>
                <li><a href="#" onclick="pagePost(${current+1})" title="Next">下一页</a></
                <li>当前第 ${current! 0} 页 |</li>
                <li>共${totalPage! 0} 页</li>
                <input name="totalPage" type="hidden" id="totalPage" value="${totalPage! 0}"/>
            </ul>
        </div>
    </div>
</div>
<form id="frm" name="frm">
    <input name="page" type="hidden" id="page"/>

</form>

<script type="text/javascript">
    $(function(){

        $("a[name=remove]").click(function () {
            if (!confirm("确定删除数据吗？")) {
                return;
            }
            var id = $(this).attr("remove");

            $.post('${requestContext.webAppContextPath}/chips/ailesson/task/delete.vpage', {id: id}, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });
    });
    function pagePost(pageNumber) {
        var totalPage = $("#totalPage").val();
        if (pageNumber != null && pageNumber > totalPage) {
            pageNumber = totalPage;
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        $("#page").val(pageNumber);
        $("#frm").submit();
    }
</script>
</@layout_default.page>