<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="金刚位管理" page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        <a href="${requestContext.webAppContextPath}/site/diamond/index.vpage" style="color: #0C0C0C">副金刚位显示设置</a>&nbsp;&nbsp;
        <a href="index.vpage">分类设置</a>&nbsp;&nbsp;
        <a id="add_advertiser_btn" href="type.vpage" type="button" class="btn btn-info" style="float: right">增加</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="get" action="index.vpage" >
                    中文名称：
                    <input id="chName" name="chName" value="${chName!''}" autocomplete="true"/>
                    &nbsp;&nbsp;
                    英文名称：
                    <input id="enName" name="enName" value="${enName!''}" autocomplete="true"/>
                    &nbsp;&nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>中文分类名称</td>
                        <td>英文分类名称</td>
                        <td>创建时间</td>
                        <td style="width: 175px;">操作</td>
                    </tr>
                    <#if positionTypes?? >
                        <#list positionTypes as pt>
                            <tr>
                            <td>${pt.chName!}</td>
                            <td>${pt.enName!}</td>
                            <td>${pt.createTime!?string('yyyy-MM-dd HH:mm:ss')!}</td>
                            <td>
                        <a class="btn btn-success" href="type.vpage?id=${pt.id!}">编辑</a>
                        <a class="btn btn-danger delete" id="delete_${pt.id!}" href="javascript:void(0);">删除</a>
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
        $('[id^="delete_"]').on('click', function() {
            if(!confirm("确定要删除吗？")) {
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            $.post('del.vpage', {
                id:id
            },function(data) {
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>