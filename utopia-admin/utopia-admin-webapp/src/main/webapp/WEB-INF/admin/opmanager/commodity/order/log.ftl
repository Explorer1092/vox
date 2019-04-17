<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<div class="span9">
    <fieldset>
        <legend><font color="#00bfff">学习币商城</font>/订单日志</legend>
    </fieldset>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>操作类型</th>
                        <th>操作内容</th>
                        <th>操作时间</th>
                        <th>操作人</th>
                        <th>备注</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if logs?has_content>
                            <#list logs as log>
                                <tr>
                                    <td>${log.opType!''}</td>
                                    <td>
                                        <#if log.opList??>
                                            <#list log.opList as op>
                                                <label>${op}</label>
                                            </#list>
                                        </#if>
                                    </td>
                                    <td>${log.opDate!''}</td>
                                    <td>${log.operator!''}</td>
                                    <td>${log.remark!''}</td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>