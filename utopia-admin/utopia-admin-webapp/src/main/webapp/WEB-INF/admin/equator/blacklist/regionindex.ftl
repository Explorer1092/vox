<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="地区黑名单管理" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<div id="main_container" class="span9">

    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><a href="/equator/config/blacklist/index.vpage">个人及学校黑名单管理</a></li>
            <li role="presentation" class="active"><a href="/equator/config/blacklist/regionindex.vpage">地区黑名单管理</a>
            </li>
        </ul>
    </div>

    <form class="form-horizontal" action="regionindex.vpage" method="get" id="queryForm">
        黑名单类型:
        <select id="type" name="type">
            <#if typeList??>
                <#list typeList?keys as key>
                    <option value="${key}">${typeList[key]!''}</option>
                </#list>
            </#if>
        </select>
        黑名单模块类型:
        <select id="module" name="module">
            <#if moduleList??>
                <#list moduleList?keys as key>
                    <option value="${key}">${moduleList[key]!''}</option>
                </#list>
            </#if>
        </select>
        <button type="submit" class="btn btn-info">查询</button>

        <button type="button" class="btn btn-info save">保存</button>
    </form>

    <div id="region_dialog" class="span7">
        <div id="regionTree" class="controls" style="width:60%; height: 410px; float: left; display: inline;"></div>
    </div>

</div>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script>
    $(function () {
        $('#regionTree').fancytree({
            source: ${allBuildRegionTrees!},
            checkbox: true,
            selectMode: 3
        });

        $('.save').click(function () {
            if (!confirm("确定要保存设置吗?")) {
                return;
            }

            $(".save").attr("disabled", true);

            let type = $('#type').find('option:selected').val();
            let module = $('#module').find('option:selected').val();
            let regionTree = $("#regionTree").fancytree("getTree");
            let regionNodes = regionTree.getSelectedNodes();

            let regionList = [];
            $.map(regionNodes, function (node) {
                regionList.push(node.key);
            });

            $.post('saveregions.vpage', {
                type: type,
                module: module,
                values: regionList.join(",")
            }, function (data) {
                if (data.success) {
                    alert("更新成功!");
                } else {
                    alert(data.info);
                }
                $(".save").attr("disabled", false);
            })
        });
    });
</script>
</@layout_default.page>