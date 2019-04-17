<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" >
    <#include "../work_record/region_tree.ftl">
<div id="regionContiner">
</div>
<script type="text/javascript">
    $(function(){
        var areaTreeObj = ${userRegionTree};
        var htmlContent = template("regionTreeTest", { regionTree : areaTreeObj });
        $("#regionContiner").html(htmlContent);
        window.regionTreeOK=function(name,code){
            window.location.href = "/mobile/school/region_school_list.vpage?countyCode="+code+"&back=region"
        };
        window.regionTreeReturn=function(name,code){
            window.history.back();
        };
    });
</script>
</@layout.page>