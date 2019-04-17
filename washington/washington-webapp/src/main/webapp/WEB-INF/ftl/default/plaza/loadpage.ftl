<#if notLoading == 1 >
<center>正在加载中...</center>
</#if>
<script type="text/javascript">
	var redirectUrl = "${redirectUrl}";

	<#if redirect?exists && redirect?has_content>
    setTimeout(function(){ ${redirect}.location.href = redirectUrl; }, 200);
	<#else>
    setTimeout(function(){ location.href = redirectUrl; }, 200);
	</#if>
</script>