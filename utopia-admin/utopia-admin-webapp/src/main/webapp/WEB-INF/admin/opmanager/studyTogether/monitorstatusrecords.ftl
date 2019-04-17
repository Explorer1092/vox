<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div class="span9">
    <fieldset>
        <legend>班长、辅导员状态和等级日志</legend>
    </fieldset>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>操作时间</th>
                        <th>状态</th>
                        <th>等级</th>
                        <th>休整开始</th>
                        <th>休整结束</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if returnList?? && returnList?size gt 0>
                            <#list returnList as  log>
                            <tr>
                                <td>${log.recordTime!''}</td>
                                <td>${log.status!''}</td>
                                <td>${log.level!''}</td>
                                <td>${log.restStart!''}</td>
                                <td>${log.restEnd!''}</td>
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