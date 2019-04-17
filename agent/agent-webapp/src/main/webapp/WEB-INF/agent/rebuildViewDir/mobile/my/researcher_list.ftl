<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择拜访人员" pageJs="" footerIndex=4>
    <@sugar.capsule css=['new_base','school']/>
<div class="s-list">
    <#if researchersList?? && researchersList?size gt 0>
        <#list researchersList as rl>
            <div class="item js-item" data-sid="${rl.id!0}">
                ${rl.name!0}
            </div>
        </#list>
    </#if>
</div>
<script>
    var backUrl = getUrlParam('backUrl') || "";
    $(document).on("ready",function(e){
        //点击单条数据
        $(document).on("click",".js-item",function(e){
            var sid = $(this).data("sid");
            var sidName = $(this).html().trim();
            if(backUrl == "addMeeting"){
                store.set("meetingInstructorId",sid);
                store.set("meetingInstructorName",sidName);
                setTimeout(disMissViewCallBack(),100);
            }else{
            $.post("save_researchers.vpage",{researchersId:sid},function(res){
                if(res.success){
                    disMissViewCallBack();
                }else{
                    AT.alert(res.info);
                }
            });
            }
        });
    });
</script>
</@layout.page>