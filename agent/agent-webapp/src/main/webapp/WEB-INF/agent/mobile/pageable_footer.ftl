<#if pages gt 1>
<div id="pager_footer">
    <#if page gt 0>
        <a href="#" onclick="turnPage(0)">首页</a>
        &nbsp;
        <a href="#" onclick="turnPage(${page - 1})">上一页</a>
    <#else>
        首页 &nbsp;上一页
    </#if>
    &nbsp;
    <#if page lt pages - 1>
        <a href="#" onclick="turnPage(${page + 1})">下一页</a>
        &nbsp;
        <a href="#" onclick="turnPage(${pages - 1})">末页</a>
    <#else>
        下一页&nbsp;末页
    </#if>
</div>

<script type="text/javascript">
    var size = 6;
    function turnPage(page) {
        var pages = ${pages};
        if (page == null || page < 0 || page > (pages - 1)) {
            return false;
        }
        var url = window.location.toString();
        if (url.indexOf("?") == -1) {
            url += "?size=" + size + "&page=" + page;
        } else {
            if (url.indexOf("size=") == -1) {
                url += "&size=" + size;
            }
            if (url.indexOf("page=") == -1) {
                url += "&page=" + page;
            } else {
                url = url.split("page=")[0] + "page=" + page;
            }
        }
        window.location.href = url;
    }
</script>
</#if>