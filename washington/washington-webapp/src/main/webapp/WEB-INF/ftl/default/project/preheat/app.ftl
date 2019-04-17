<#import "../../layout/mobile.layout.ftl" as temp>
<#assign defaultDpi = ".595" currentTypeCount = 2/>
<#if (currentUser.userType == currentTypeCount)!false>
    <#assign defaultDpi = ""/>
</#if>
<@temp.page title="新品预热大狂欢" dpi=defaultDpi>
    <@app.css href="public/skin/project/preheat/app/css/preheat.css"/>
    <@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
    <#if (currentUser.userType == currentTypeCount)!false>
        <#include "parentapp.ftl"/>
    <#else>
        <#include "studentapp.ftl"/>
    </#if>

    <script type="text/javascript">
        $(function(){
            $(".js-clickVoxLog").on("click", function(){
                var $thisOP = $(this).data("op");

                if( $17.isBlank($thisOP) ){
                    return false;
                }

                $17.voxLog({
                    module : "huodongyure",
                    op : $thisOP
                }, "student");
            });

            <#if (currentUser.userType == currentTypeCount)!false>
                $17.voxLog({
                    module : "huodongyure",
                    op : "parentAppLoad"
                }, "student");
            <#else>
                $17.voxLog({
                    module : "huodongyure",
                    op : "studentAppLoad"
                }, "student");
            </#if>
        });
    </script>
</@temp.page>