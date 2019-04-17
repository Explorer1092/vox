<#macro page group="index" title="17zuoye Mobile Marketing System">
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="description" content="17zuoye Mobile Marketing System">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no">
    <link rel="stylesheet" href="/public/css/business/skin.css?1.0.2"/>
    <script src="/public/js/jquery-1.9.1.min.js"></script>
    <script src="/public/js/jquery.cookie.js"></script>
    <script src="/public/js/template.js"></script>
    <script src="/public/js/market-common.js"></script>
</head>
<body>
<div role="main">
    <#nested />
</div>
<div class="mobileCRM-V2-footer" <#if (group?? && group == "login")>style="display: none" </#if>>
    <div class="inner">
        <div><a href="/mobile/performance/index.vpage" class="${((group == "业绩")?string("active", ""))!''}">首页</a></div>
        <div><a href="/mobile/school/index.vpage" class="${((group == "搜索")?string("active", ""))!''}">搜索</a></div>
        <div><a id="footer-task" href="/mobile/task/agent_task_detail_list.vpage" class="${((group == "task")?string("active", ""))!''}">任务</a></div>
        <div><a href="/mobile/work_record/index.vpage" class="${((group == "work_record")?string("active", ""))!''}">工作台</a></div>
    </div>
</div>
<script>

</script>
</body>
</html>
</#macro>
