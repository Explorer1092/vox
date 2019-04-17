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
            <form id="school_search_form" action="/workspace/markettool/region_school.vpage" method="get" data-ajax="false">
                    <select id="topRegion"></select>
                    <select id="middleRegion"></select>
                    <select name="regionCode" id="bottomRegion"></select>
                    <input name="key" id="key" type="text" size="28" placeholder="学校ID/名称"/>
                    <a id="search_btn" href="#" class="ui-btn ui-btn-inline">查询</a>
                <input name="cityName" id="cityName" type="hidden"/>
                <input name="areaName" id="areaName" type="hidden"/>
            </form>
            <h5>${cityName!} >> ${areaName!}</h5>
            <#include "pageable_header.ftl">
            <#if (total > 0)>
                <ul data-role="listview" data-inset="true">
                    <#list result.content as e>
                        <li data-role="list-divider">${(e.school.cname)!}<span style="color: red;">(ID:${(e.school.id)!})</span></li>
                        <li>
                            <p>
                                <strong>${(e.type.description)!}</strong>&nbsp;&nbsp;教师数量：
                                <#if e.teacherCount gt 0>${e.teacherCount}
                                <#else>0
                                </#if>
                            </p>

                            <#if e.ambassadors?has_content>
                                <p>校园大使：</p>

                                <p>
                                    <#list e.ambassadors as a>
                                        <a href="teacher_info.vpage?teacherId=${(a.id)!}"
                                           data-ajax="false">${(a.profile.realname)!}</a>&nbsp;
                                    </#list>
                                </p>
                            <#else><p>无校园大使</p>
                            </#if>
                        </li>
                    </#list>
                </ul>
                <#include "pageable_footer.ftl">
            </#if>
        </div>
    </div>
</div>
<script type="text/javascript">
    var regionTree;
    $(function () {
        var result = ${regionTree!};
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