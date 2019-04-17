<!doctype html>
<html>
<head>
    <title>练习</title>
    <@sugar.capsule js=["jquery"] css=[] />
</head>
<body style="padding:0;margin:0;background-color:white;">
<div class="">
    ${filename!}
</div>
<script type="text/javascript">
    $(function(){
        var photoShowBox = $(parent.window.document) || $(top.window.document) || $("<div/>");//查找父子iframe
        <#if filename?? && filename?has_content>
            photoShowBox.find(".v-photoShowBox-${missionId!}").show().find(".photo").html('<img src="<@app.avatar href='${filename!}'/>"/>');
        <#else>
            //失败
            photoShowBox.find(".v-photoShowBox-${missionId!}").show().find(".photo").html('${error!}');
        </#if>
    });
</script>