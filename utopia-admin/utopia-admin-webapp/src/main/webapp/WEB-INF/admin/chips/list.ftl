<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语字典配置' page_num=26>

<!--/span-->
<div class="span10">
<#--//start-->
    <div style="padding: 0 0 10px;">
        <a href='addIndex.vpage' class="btn"><i class="icon-plus"></i> 新建</a>
    </div>
    <table class="table table-hover table-striped table-bordered ">
        <thead>
        <tr>
            <th>配置项名称</th>
            <th style="width: 700px;">配置值</th>
            <th>描述</th>
            <td>操作</td>
        </tr>
        </thead>
        <tbody>
        <#if configList?? && configList?size gt 0>
            <#list configList as config >
            <tr>
                <td><#if config.name??>${config.name}</#if></td>
                <td><#if config.value??>${config.value}</#if></td>
                <td><#if config.memo??>${config.memo}</#if></td>
                <td>
                <#if config.id??>
                    <a href='editIndex.vpage?id=${config.id}' class="btn btn-success"><i
                            class="icon-edit icon-white"></i> 编辑</a>
                    <a id="delete_${config.id}" href='javascript:void(0);' class="btn btn-inverse"><i
                            class="icon-remove icon-white"></i> 删除</a>
                </#if>
                </td>
            </tr>
            </#list>
        </#if>
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