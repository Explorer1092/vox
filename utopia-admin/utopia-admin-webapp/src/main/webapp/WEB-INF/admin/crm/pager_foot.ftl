<#assign page = (pager.number)!0>
<#assign pages = (pager.totalPages)!0>

<ul class="inline">
<#if (page > 0)>
    <li>
        <a href="#" onclick="turnPage(0)">首页</a>
    </li>
    <li>
        <a href="#" onclick="turnPage(${page - 1})">上一页</a>
    </li>
<#else>
    <li>
        首页
    </li>
    <li>
        上一页
    </li>
</#if>
<#if (page < pages - 1)>
    <li>
        <a href="#" onclick="turnPage(${page + 1})">下一页</a>
    </li>
    <li>
        <a href="#" onclick="turnPage(${pages - 1})">末页</a>
    </li>
<#else>
    <li>
        下一页
    </li>
    <li>
        末页
    </li>
</#if>
    <li>
        当前第 <strong>${page + 1}</strong> 页
    </li>
    <li>
        共 <strong>${pages}</strong> 页
    </li>
    <li>
        共 <strong>${(pager.totalElements)!0}</strong> 条记录
    </li>
</ul>

<script type="text/javascript">
    function turnPage(page) {
        var pages = ${pages};
        if (page == null || page < 0 || page > (pages - 1)) {
            return false;
        }
        $("#PAGE").val(page);
        $("#iform").submit();
    }

    var pager = {"ORDER": {"ASC": "ASC", "DESC": "DESC"}};
    pager.sort = function (node) {
        var sort = $(node).attr("pager-sort");
        if (blankString(sort)) {
            return false;
        }
        var order = $("#ORDER").val();
        if (order === pager.ORDER.ASC) {
            $("#ORDER").val(pager.ORDER.DESC);
        } else {
            $("#ORDER").val(pager.ORDER.ASC);
        }
        $("#SORT").val(sort);
        $("#iform").submit();
    };

    pager.sortTip = function () {
        var sort = $("#SORT").val();
        if (blankString(sort)) {
            return false;
        }
        var order = $("#ORDER").val();
        var tip = order === pager.ORDER.ASC ? "升序" : "降序";
        $("th[pager-sort='" + sort + "']").append(" (<span class='order-tip' style='font-size: small'>" + tip + "</span>)");
    };
</script>