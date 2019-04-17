<!DOCTYPE html>
<html>
	<head>
		<title></title>
        <#include "../nuwa/meta.ftl" />
		<style>
			body{ padding:0; margin:0;overflow: hidden; }
			html {
				height: 100%;
			}
			body {
				height: 100%;
			}
			#FaceUpload {
				height: 100%;
			}
		</style>
        <@sugar.capsule js=["jquery", "core"] />
	</head>
	<body>
		<#if userId?exists && userId?has_content>
		<object id="FaceUpload" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=10,2,0"  width="100%" height="100%">
			<param name="movie" value="<@flash.plugin name="FaceUpload"/>" />
			<param name="quality" value="high" />
			<param name="wmode" value="opaque" />
			<param name="menu" value="false" />
			<param name="allowScriptAccess" value ="always" /> 
			<param name="allowFullScreen" value="true" />
			<param name="name" value="FaceUpload" />
			<param name="flashvars" value="uid=${userId}&face=<@app.avatar href="${face!}"/>&subject=${subject!"头像"}&resourceroot=/resources/apps/face/&upload=/uploadfile/avatar.vpage&cancel=avatar_cancel&callback=avatar_callback" />
			<!--[if !IE]>-->
			<object type="application/x-shockwave-flash" data="<@flash.plugin name="FaceUpload"/>" width="100%" height="100%">
				<!--<![endif]-->
				<param name="name" value="FaceUpload" />
				<param name="quality" value="high" />
				<param name="wmode" value="opaque" />
				<param name="menu" value="false" />
				<param name="allowScriptAccess" value ="always" /> 
				<param value="true" name="allowFullScreen"/>
				<param name="flashvars" value="uid=${userId}&face=<@app.avatar href="${face!}"/>&subject=${subject!"头像"}&resourceroot=/resources/apps/face/&upload=/uploadfile/avatar.vpage&cancel=avatar_cancel&callback=avatar_callback"/>
			</object>
			<!--<![endif]-->
		</object>

        <script type="text/javascript">
			var avatar_cancel = function(){
				App.call("${avatar_cancel!}");
			};
			var avatar_callback = function ( data ){
				App.call("${avatar_callback!}", data);
			};
		</script>
		<#else>
		<h1>无法操作！</h1>
		</#if>
	</body>
</html>