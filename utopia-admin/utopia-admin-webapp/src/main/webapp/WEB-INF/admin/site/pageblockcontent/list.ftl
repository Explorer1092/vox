<#-- @ftlvariable name="pageBlockContentList" type="java.util.List<com.voxlearning.utopia.service.config.api.entity.PageBlockContent>" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='页面内容列表' page_num=4>

<!--/span-->
<div class="span9">
<#--//start-->
    <div style="padding: 0 0 10px;">
        <a href='edit.vpage' class="btn"><i class="icon-plus"></i> 新建</a>
    </div>
    <table class="table table-hover table-striped table-bordered ">
        <thead>
        <tr>
            <th>id</th>
        <#-- <th>createDatetime</th>   -->
        <#--<th>pageName</th>-->
        <#-- <th>blockName</th>-->
            <th>displayOrder</th>
            <th>startDatetime</th>
            <th>endDatetime</th>
            <th>memo</th>
            <td>操作</td>
        </tr>
        </thead>
        <tbody>
            <#list pageBlockContentList![] as pageBlockContent>
            <tr>
                <td>${pageBlockContent.id}</td>
            <#-- <td>${pageBlockContent.createDatetime}</td> -->
            <#--<td>${pageBlockContent.pageName}</td> -->
                <td>${pageBlockContent.blockName}</td>
            <#--<td>${pageBlockContent.displayOrder}</td>-->
                <td>${pageBlockContent.startDatetime}</td>
                <td>${pageBlockContent.endDatetime}</td>
                <td>${pageBlockContent.memo?html}</td>
                <td>
                    <#if pageBlockContent.blockName == "student_mobile_app">
                        <#if isSuperAdmin>
                            <a href='edit.vpage?id=${pageBlockContent.id}' class="btn btn-success"><i
                                    class="icon-edit icon-white"></i> 编辑</a>
                            <a id="delete_${pageBlockContent.id}" href='javascript:void(0);' class="btn btn-inverse"><i
                                    class="icon-remove icon-white"></i> 删除</a>
                        </#if>
                    <#else>
                        <a href='edit.vpage?id=${pageBlockContent.id}' class="btn btn-success"><i
                                class="icon-edit icon-white"></i> 编辑</a>
                        <a id="delete_${pageBlockContent.id}" href='javascript:void(0);' class="btn btn-inverse"><i
                                class="icon-remove icon-white"></i> 删除</a>
                    </#if>
                </td>
            </tr>

            </#list>
        </tbody>
    </table>
<#--end//-->
</div>
<!--/span-->
<script type="text/javascript">
    $(function(){
        $('[id^="delete_"]').on('click', function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            $.post('delete.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
</@layout_default.page>