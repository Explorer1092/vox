<#macro page title="一起作业" dpi="">
<!DOCTYPE html>
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
<html>
<head>
    <title>${title!}</title>
    <meta charset="utf-8">
    <meta name="viewport" content="target-densitydpi=device-dpi,width=640px, user-scalable=no" />
    <@app.script href="public/skin/mobile/pc/js/fullScreenDpi${dpi!}.js" />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <#nested>
    <@sugar.site_traffic_analyzer_end />
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
</#macro>
