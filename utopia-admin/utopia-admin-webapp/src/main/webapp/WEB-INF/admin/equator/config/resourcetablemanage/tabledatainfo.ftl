<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="资源表管理-数据列表" page_num=24>
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

    <#if resourceTableDigest?? >
    <h3>${resourceTableDigest.tableExcelName?default("")} (${resourceTableDigest.tableName?default("")})</h3>
    </#if>
    <#include "inner_header.ftl"/>

    <#if filedKeyDescList?exists>
        <div class="control-group">
            <label class="col-sm-2 control-label">字段列表:</label>

            <div class="controls">
                <#list filedKeyDescList as fieldKeyDesc>
                    <input name="managerKey" type="checkbox" checked="checked"
                           value="${fieldKeyDesc_index}"/> ${fieldKeyDesc}
                </#list>
            </div>
        </div>
    </#if>

    <#if excelDataListList??>
    <ul class="inline">
        <table class="table table-bordered">
            <#list excelDataListList as excelDataList>
                <#if excelDataList_index == 0>
                   <tr>
                    <#list excelDataList as excelData>
                        <th name="sel_${excelData_index}">${excelData?default("")}</th>
                    </#list>
                   </tr>
                <#elseif excelDataList_index == 1>
                    <tr>
                    <#list excelDataList as excelData>
                        <th name="sel_${excelData_index}">${excelData?default("")}</th>
                    </#list>
                    </tr>
                <#else>
                    <tbody id="tbody">
                    <#if excelDataList??>
                    <#list excelDataList as excelData>
                    <td name="sel_${excelData_index}">${excelData?default("")}</td>
                    </#list>
                    </#if>
                    </tbody>
                </#if>
            </#list>
        </table>
    </ul>
    </#if>

</div>

<script type="text/javascript">
    $(function () {
        $('input:checkbox').click(function () {
            var index = $(this).val();
            var nodeChecked = $(this).is(":checked");
            $("[name=sel_" + index + "]").each(function () {
                if (nodeChecked) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        });
        $(".sel_detail").each(function () {
            $(this).popover()
        })
    });
    $('input:checkbox').each(function () {
        var index = $(this).val();
        var nodeChecked = $(this).is(":checked");
        $("[name=sel_" + index + "]").each(function () {
            if (nodeChecked === false) {
                $(this).hide();
            }
        });
    });

</script>
</@layout_default.page>