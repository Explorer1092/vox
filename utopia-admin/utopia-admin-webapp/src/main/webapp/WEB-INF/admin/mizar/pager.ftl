<#macro pager>
<ul class="pager">
<#if hasPrev>
    <li><a href="javascript:void(0);" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
<#else>
    <li class="disabled"><a href="javascript:void(0);">&lt;</a></li>
</#if>
    <li><a href="javascript:void(0);" onclick="pagePost(1)">首页</a></li>
    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
    <li><a href="javascript:void(0);" onclick="pagePost(${totalPage})">末页</a></li>
<#if hasNext>
    <li><a href="javascript:void(0);" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
<#else>
    <li class="disabled"><a href="javascript:void(0);">&gt;</a></li>
</#if>
</ul>
<script>
    function pagePost(pageNumber) {
        $("#page").val(pageNumber);
        $("#query_frm").submit();
    }
</script>
</#macro>