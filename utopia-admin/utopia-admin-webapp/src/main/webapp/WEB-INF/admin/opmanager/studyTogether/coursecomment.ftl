<div class="row-fluid">
    <div class="span12">
        <div class="well">
            <table class="table table-hover table-striped table-bordered">
                <thead>
                <tr>
                    <th>操作内容</th>
                    <th>操作时间</th>
                    <th>操作人</th>
                </tr>
                </thead>
                <tbody>
                        <#if logList?? && logList?size gt 0>
                            <#list logList as log>
                                <tr>
                                    <td>
                                        <#if log.logList?? && log.logList?size gt 0>
                                            <#list log.logList as  log_detail>
                                                <#escape log_detail as log_detail?html>
                                                    <p>${log_detail!""}</p>
                                                </#escape>
                                            </#list>
                                        </#if>
                                    </td>
                                    <td>${log.createTime!''}</td>
                                    <td>${log.adminUserName!''}</td>
                                </tr>
                            </#list>
                        <#else >
                            <tr>
                                <td colspan="3" style="text-align: center">暂无数据</td>
                            </tr>
                        </#if>
                </tbody>
            </table>
            <ul class="message_page_list"></ul>
        </div>
    </div>
</div>