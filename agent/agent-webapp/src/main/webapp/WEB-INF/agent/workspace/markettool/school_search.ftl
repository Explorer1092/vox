<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page  page_title='学校查询' page_num=9>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 学校ID查询</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form id="school_search_form" action="region_school.vpage" method="get">
                    <select id="topRegion"></select>
                    <select id="middleRegion"></select>
                    <select name="regionCode" id="bottomRegion"></select>
                    <input name="key" id="key" type="text" size="28" placeholder="学校ID/名称"/>
                    <a id="search_btn" href="#" class="ui-btn ui-btn-inline">查询</a>
                <input name="cityName" id="cityName" type="hidden"/>
                <input name="areaName" id="areaName" type="hidden"/>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    var regionTree;
    $(function () {
        var result = ${result!};
        regionTree = result == undefined ? null : result;

        $("#topRegion").on("change",function () {
            loadMiddleRegion();
        });
        $("#middleRegion").on("change",function () {
            loadBottomRegion();
        });
        $("#search_btn").on("click",function () {
            return doSubmit();
        });

        loadTopRegion();
    });

    function loadTopRegion() {
        if (regionTree) {
            for (var code in regionTree) {
                var item = regionTree[code];
                $("#topRegion").append("<option value='" + code + "'>" + item.name + "</option>");
            }
            $("#topRegion").trigger("change");
        }
    }

    function loadMiddleRegion() {
        $("#middleRegion").empty();
        if (regionTree) {
            var top = $("#topRegion").val();
            if (isValidRegionCode(top)) {
                var middles = regionTree[top].children;
                for (var code in middles) {
                    var item = middles[code];
                    $("#middleRegion").append("<option value='" + code + "'>" + item.name + "</option>");
                }
            }
            $("#middleRegion").trigger("change");
        }
    }

    function loadBottomRegion() {
        $("#bottomRegion").empty();
        if (regionTree) {
            var top = $("#topRegion").val();
            var middle = $("#middleRegion").val();
            if (isValidRegionCode(top) && isValidRegionCode(middle)) {
                var bottoms = regionTree[top].children[middle].children;
                for (var code in bottoms) {
                    var item = bottoms[code];
                    $("#bottomRegion").append("<option value='" + code + "'>" + item.name + "</option>");
                }
            }
        }
    }

    function doSubmit() {
        var top = $("#topRegion").val();
        if (!isValidRegionCode(top)) {
            alert("请选择一级区域!");
            return false;
        }
        var middle = $("#middleRegion").val();
        if (!isValidRegionCode(middle)) {
            alert("请选择二级区域!");
            return false;
        }
        var bottom = $("#bottomRegion").val();
        if (!isValidRegionCode(bottom)) {
            alert("请选择三级区域!");
            return false;
        }
        $("#cityName").val(regionTree[top].children[middle].name);
        $("#areaName").val(regionTree[top].children[middle].children[bottom].name);
        $("#school_search_form").submit();
        return true;
    }

    function isValidRegionCode(code) {
        return code != null && code != undefined;
    }
</script>
</@layout_default.page>