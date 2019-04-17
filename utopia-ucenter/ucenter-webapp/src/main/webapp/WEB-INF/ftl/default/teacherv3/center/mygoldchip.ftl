<table>
    <thead>
    <tr>
        <td style="width: 147px;font-size:14px;">获取时间</td>
        <td style="width: 145px;font-size:14px;">获取数量</td>
        <td style="font-size: 14px;">获取来源</td>
    </tr>
    </thead>
    <tbody>
    <#if pagination?? && pagination.getTotalPages() gt 0>
        <#list pagination.getContent() as row>
            <#if row_index % 2 == 0>
            <tr class="odd">
            <#else>
            <tr>
            </#if>
            <td style="font-size: 12px;">${(row.dateYmdString)!""}</td>
            <td style="font-size: 12px;">${row.integral!""}</td>
            <td style="font-size: 12px;">${row.comment!""}</td>
        </tr>
        </#list>
    <#else>
    <tr>
        <td colspan="3" style="text-align:center;font-size:12px;" id="integral_null_box"></td>
    </tr>
    </#if>
    </tbody>
</table>
<#if pagination?? && pagination.getTotalPages() gt 0>
<div class="message_page_list" style="background-color: #f8f8f8; border-top: 1px solid #dfdfdf;"></div>
</#if>

<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${(pagination.getTotalPages())!0},
            current: ${currentPage!},
            autoBackToTop: false,
            jumpCallBack: loadSystemMessage
        });
    });
</script>