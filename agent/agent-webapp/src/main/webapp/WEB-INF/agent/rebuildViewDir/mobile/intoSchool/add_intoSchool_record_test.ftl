<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#if recordId??>
    <#assign header = "查看进校记录">
<#else>
    <#assign header = "填写进校记录">
</#if>
<#assign register_stu_have = register_stu?? && register_stu?has_content >
<#assign auth_stu_have = auth_stu?? && auth_stu?has_content >
<#assign single_act_stu_have = single_act_stu?? && single_act_stu?has_content >
<#assign double_act_stu_have = double_act_stu??&& double_act_stu?has_content  >
<@layout.page title="${header!}" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['school']/>
<div>
    <div class="btn-stroke" id="lcationBtn">位置</div>
    <br><br>


    <div class="btn-stroke" id="imageBtn">拍照</div>
</div>
<script>
    var vox = vox || {};
    vox.task = vox.task || {};
    var AT = new agentTool();


    //获取位置回调
    vox.task.setLocation = function(res){
        AT.alert("位置回调："+JSON.parse(res));
    };

    //拍照回调
    vox.task.setImageToHtml = function(result){
        AT.alert("拍照回调："+JSON.parse(result));
    };


    $(document).on("click","#lcationBtn",function(){
        window.webkit.messageHandlers.getLocation.postMessage("");
    });
    $(document).on("click","#imageBtn",function(){
        var carData =  {
            uploadUrlPath:"mobile/file/upload.vpage",
            NeedAlbum:false,
            NeedCamera:true,
            uploadPara: {}
        };

        window.webkit.messageHandlers.getImageByHtml.postMessage(JSON.stringify(carData));
    });


</script>
</@layout.page>