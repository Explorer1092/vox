<!DOCTYPE HTML>
<html>
<head>
    <title>一起作业，奖品中心 - 一起作业 www.17zuoye.com</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<#--37个灰度城市的学生的奖品中心只保留"幸运抽大奖"，所以首页直接重定向到"幸运抽大奖"-->
    <#if (currentUser.userType) == 3 && ((currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu",true))!false)>
        <meta http-equiv="Refresh" content="0; url=/campaign/studentlottery.vpage"/>
    <#else>
        <meta http-equiv="Refresh" content="0; url=/reward/product/exclusive/index.vpage"/>
    </#if>
</head>
<body>
</body>
</html>

