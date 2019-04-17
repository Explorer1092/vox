<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="UTF-8">
    <title>SearchTest</title>
</head>
<body>
<div>
    <form id="iform" action="/devtest/jxtnewssearchtest.vpage" method="get">
        关键字：<input id="keyWord" name="keyWord" type="text" placeholder="输入关键字"/>
        <input id="submit" type="submit" value="搜索"/>
    </form>
    <div>
    <#if testResult?? && testResult?size gt 0>
        <#list testResult as item>
            <a href="http://www.test.17zuoye.net/view/mobile/parent/information/detail?newsId=${item}">${item}</a></br>
        </#list>
    </#if>
    <#if stagingResult?? && stagingResult?size gt 0>
        <#list stagingResult as item>
            <a href="http://www.17zuoye.com/view/mobile/parent/information/detail?newsId=${item}">${item}</a></br>
        </#list>
    </#if>
    </div>
</div>
<script type="text/javascript">
</script>
</body>
</html>