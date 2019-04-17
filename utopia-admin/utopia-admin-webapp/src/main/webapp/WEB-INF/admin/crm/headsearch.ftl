<#macro headSearch>
<div class="head-search">
    <style>
        .head-search form{ display: inline-block; *display: inline; *zoom:1;}
        .head-search input{ width: 170px; margin: 0; vertical-align: middle;}
    </style>
    <form action="${requestContext.webAppContextPath}/crm/user/userhomepage.vpage" method="post" target="_blank">
        用户ID：<input name="userId" type="text"/>
        <button type="submit" class="btn">搜 索</button>
    </form>
    <form action="${requestContext.webAppContextPath}/crm/school/schoolhomepage.vpage" method="get" target="_blank">
        学校ID：<input name="schoolId" type="text"/>
        <button type="submit" class="btn">搜 索</button>
    </form>
    <#--跟客服核对过，此搜索框几乎不用，干掉-->
    <#--<form action="${requestContext.webAppContextPath}/crm/clazz/clazzinfo.vpage" method="post">-->
        <#--班级ID：<input name="clazzId"/>-->
        <#--<input type="submit" class="btn" value="搜索"  />-->
    <#--</form>-->
    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <form action="${requestContext.webAppContextPath}/crm/user/wechatuserhomepage.vpage" method="post" target="_blank">
        OpenID：<input name="openId" type="text"/>
        <button type="submit" class="btn">搜 索</button>
    </form>
    </#if>
</div>
</#macro>