<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div class="span9">
    <fieldset>
        <legend>班长招募日志</legend>
    </fieldset>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>家长ID</th>
                        <th>操作时间</th>
                        <th>状态</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if recordList?? && recordList?size gt 0>
                            <#list recordList as  log>
                            <tr>
                                <td>${log.parentId!''}</td>
                                <td>${log.createDate!''}</td>
                                <td>
                                <#if log.recruitStatus??>
                                    <#switch log.recruitStatus>
                                        <#case 1>
                                            审核中
                                            <#break>
                                        <#case 2>
                                            审核未通过
                                            <#break>
                                        <#case 3>
                                            审核通过
                                            <#break>
                                        <#case 4>
                                            离职
                                            <#break>
                                        <#case 5>
                                            休整
                                            <#break>
                                    </#switch>
                                </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {
    });

</script>
</@layout_default.page>