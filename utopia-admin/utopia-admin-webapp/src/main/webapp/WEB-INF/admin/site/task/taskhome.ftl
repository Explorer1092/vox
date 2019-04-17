<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<#--<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>-->
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>系统任务二次确认<span style="color: red">友情提示：请在任务真正执行前及时确认<span></legend>
        </fieldset>
        <ul class="inline">
            <li>
                选择确认的任务：<select name="taskName" id="taskName" style="width: 300px;">
                <#if taskList?? >
                    <#list taskList?keys as key>
                        <option value="${key!''}">${taskList[key]!''}</option>
                    </#list>
                </#if>
            </select>
            </li>
            <li>
                <button id="selectTable" type="button" class="btn btn-primary">查询待执行数据</button>
                <button id="submitBut" type="button" class="btn btn-warnning">确认执行</button>
            </li>
        </ul>
    </div>
    <div id="data_table">
    </div>
</div>

<script>
    $(function() {
        $('#selectTable').on('click', function() {
            $('#data_table').load('gettaskdata.vpage',
                    {taskName : $('#taskName').val()}
            );
        });

        $('#submitBut').on('click', function() {
           if(confirm("确认数据无误，奖励按时发放？")){
               $.post("confirmdata.vpage", {taskName : $('#taskName').val()}, function(data){
                   alert(data.info);
                   location.reload();
               });
           }
        });

    });

</script>
</@layout_default.page>