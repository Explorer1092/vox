<#import "../../layout/mobile.layout.ftl" as temp>
<#assign defaultDpi = ".595" currentTypeCount = 2/>
<#if (currentUser.userType == currentTypeCount)!false>
    <#assign defaultDpi = ""/>
</#if>
<@temp.page title="期末复习" dpi=defaultDpi>
    <@app.css href="public/skin/project/finalreview/app/css/finaldetails.css"/>
    <@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
    <#if (currentUser.userType == currentTypeCount)!false>
        <#include "parentapp.ftl"/>
    <#else>
        <#include "studentapp.ftl"/>
    </#if>
    <script type="text/javascript">
        $(function(){
            $(".js-clickVoxLog").on("click",function(){
                var $thisOP = $(this).data("op");
                if( $17.isBlank($thisOP) ){
                    return false;
                }
                $17.voxLog({
                    module: "final_homework",
                    op: $thisOP
                },"student");
            });

            <#if (currentUser.userType == currentTypeCount)!false>
                $17.voxLog({
                    module : "final_homework",
                    op : "activity_load_parentApp"
                },"student");
            <#else>
                $17.voxLog({
                    module : "final_homework",
                    op : "activity_load_studentApp"
                },"student");
            </#if>
        })
    </script>
</@temp.page>

