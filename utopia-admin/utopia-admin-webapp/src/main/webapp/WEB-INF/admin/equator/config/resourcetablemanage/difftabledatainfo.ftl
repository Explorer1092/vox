<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="资源表管理-数据对比" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9" style="font-size: 14px">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
    </div>
    <div>
        <#if successInfo??>
            <div class="alert">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${successInfo!}</strong>
            </div>
        </#if>
    </div>

    <#if resourceTableDigest??>
        <h3>数据对比  ${resourceTableDigest.tableExcelName?default("")} (${resourceTableDigest.tableName?default("")})</h3>
    </#if>
    <#include "inner_header.ftl"/>


    <#if changeDataList?? && changeDataList?size gt 0>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><h4>修改数据列表</h4></li>
        </ul>
        <ul class="inline">
        <#list changeDataList as changeData>
            <table class="table table-bordered">
                <tr class="data_header">
                    <th style="width: 60px"></th>
                    <#list  changeData["keys"] as key>
                        <th>${key?default("")}</th>
                    </#list>
                </tr>
                <tr class="data_line">
                    <td>测试环境数据</td>
                    <#list  changeData["testCol"] as value>
                        <td>${value?default("")}</td>
                    </#list>
                </tr>
                <tr class="data_line">
                    <td>线上环境数据</td>
                    <#list  changeData["onlineCol"] as value>
                        <td>${value?default("")}</td>
                    </#list>
                </tr>
            </table>
        </#list>
        </ul>
    </#if>


    <#if exceptionData?exists >
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><h4 style="color: red;">线上环境有新增数据,违背常规流程,请注意</h4></li>
        </ul>
        <div class="panel-body data_area">
            <p>${exceptionData?default("")}</p>
        </div>
    </#if>

    <#if newData?exists >
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><h4>测试环境新增数据</h4></li>
        </ul>
        <div class="panel-body data_area">
            <p>${newData?default("")}</p>
        </div>
    </#if>

</div>

<script type="text/javascript">
</script>
</@layout_default.page>