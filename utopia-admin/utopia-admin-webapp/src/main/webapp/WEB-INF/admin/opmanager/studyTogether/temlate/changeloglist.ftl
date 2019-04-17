<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>课程内容模板管理/操作日志</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="change_log_list.vpage">
        <input type="hidden" id="pageNum" name="page" value="${page!'1'}"/>
        <input type="hidden" id="template_id" name="template_id" value="${template_id!''}"/>
        <input type="hidden" id="change_log_type" name="change_log_type" value="${change_log_type!''}"/>
    </form>
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
                        <#if change_log_list?? && change_log_list?size gt 0>
                            <#list change_log_list as  log>
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
                                <td colspan="7" style="text-align: center">暂无数据</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list">
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${total_page!1},
            current: ${page!1},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });
    });
</script>
</@layout_default.page>