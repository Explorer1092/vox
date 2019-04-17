<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>家长，师生家长互动作业平台 - 一起作业  www.17zuoye.com</title>
    <#include "../../nuwa/meta.ftl" />
    <style>
        body{ padding:0; margin:0;overflow: hidden; }
        html {
            height: 100%;
        }
        body {
            height: 100%;
        }
        #FlashID {
            height: 100%;
        }
    </style>
    <@sugar.capsule js=["jquery"] />
    <script type="text/javascript">
		var change_title = function( title ){
			document.title = title;
		};
		var Show_Share_Box = function(){
			$CKE.over();
		};
	</script>
</head>
<body>
	<#assign file="ExpForParents"/>
	<object id="${file}" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=10,2,0"  width="100%" height="100%">
	    <param name="movie" value="<@flash.plugin name="${file}"/>" />
	    <param name="quality" value="high" />
	    <param name="wmode" value="opaque" />
	    <param name="menu" value="false" />
	    <param name="allowScriptAccess" value ="always" />
	    <param name="allowFullScreen" value="true" />
	    <param name="name" value="${file}" />
	    <param name="flashvars" value="domain=/&imgDomain=/&flashId=${file}" />
	    <!--[if !IE]>-->
	    <object type="application/x-shockwave-flash" data="<@flash.plugin name="${file}"/>" width="100%" height="100%">
	        <!--<![endif]-->
	        <param name="name" value="${file}" />
	        <param name="quality" value="high" />
	        <param name="wmode" value="opaque" />
	        <param name="menu" value="false" />
	        <param name="allowScriptAccess" value ="always" />
	        <param value="true" name="allowFullScreen"/>
	        <param name="flashvars" value="domain=/&imgDomain=/&flashId=${file}" />
	    </object>
	    <!--<![endif]-->
	</object>
	<!-- JiaThis Button BEGIN -->
	<script type="text/javascript">var jiathis_config = {data_track_clickback:true, marginTop:$(window).height() - 249};</script>
	<script type="text/javascript" src="http://v2.jiathis.com/code_mini/jiathis_r.js?move=0&amp;btn=r1.gif&amp;uid=1613716" charset="utf-8"></script>
	<!-- JiaThis Button END -->
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>