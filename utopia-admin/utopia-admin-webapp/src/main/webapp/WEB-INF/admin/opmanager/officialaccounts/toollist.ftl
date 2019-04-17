<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="工具栏列表" page_num=16>
<div id="main_container" class="span9">
    <legend>
        <strong>工具栏列表</strong>
        <#if isTopAuditor||isSeniorAuditor>
            <a href="addtool.vpage?accountId=${accountId!0}" type="button" class="btn btn-info" style="float: right">添加工具栏</a>
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>创建时间</th>
                        <th>工具栏名称</th>
                        <th>工具栏URL</th>
                        <th>是否拼接SID</th>
                        <th>删除</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if toolList??>
                            <#if (toolList?size>0)>
                                <#list toolList as tool >
                                <tr>
                                    <td>${tool.createDatetime!}</td>
                                    <td>${tool.toolName!}</td>
                                    <td>${tool.toolUrl!}</td>
                                    <td>${tool.bindSid?string('是', "否")}</td>
                                    <td>
                                        <#if isTopAuditor||isSeniorAuditor>
                                        <a class="revoke" href="javascript:void(0);" data-id="${tool.id!}">删除</a>
                                        </#if>
                                    </td>
                                </tr>
                                </#list>
                            <#else>
                            <tr>
                                <td colspan="5">暂时没有配置工具栏哦~</td>
                            </tr>
                            </#if>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<style>
    .table td , .table th{
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
<script type="text/javascript">
    $(function(){
        $(".revoke").on('click',function(){
            var toolName = $(this).parent().siblings().eq(1).html();
            if(!confirm('确认删除工具栏"'+toolName+'"？')){
                return;
            }

            var $this=$(this),
                    data={
                        toolId  : $this.attr("data-id")
                    };
            $.post("deletetool.vpage",data,function(res){
                if(res.success){location.reload();}
            });
        });
    });
</script>
</@layout_default.page>