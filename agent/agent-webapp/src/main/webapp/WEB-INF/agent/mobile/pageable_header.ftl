<#assign total = (result.totalElements)!0>
<#assign page = (result.number)!0>
<#assign pages = (result.totalPages)!0>
<div id="pager_header">
    <span>共 <strong>${total!0}</strong> 条记录; 当前第 <strong>${page+1}</strong> 页, 共 <strong>${pages}</strong> 页</span>
</div>