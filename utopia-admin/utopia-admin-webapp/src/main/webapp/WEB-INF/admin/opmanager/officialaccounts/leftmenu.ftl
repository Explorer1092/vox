<div class="span2">
    <div class="well sidebar-nav" style="background-color: #fff;">
        <ul class="nav nav-list">
            <li class="nav-header">配置</li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/officialaccounts/accountdetail.vpage?accountId=${accountId}">基本信息</a></li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/officialaccounts/toollist.vpage?accountId=${accountId}">工具栏管理</a></li>
            <#if accounts?? && accounts.accountsKey?string == 'dianduji'><li> <a href="${requestContext.webAppContextPath}/opmanager/officialaccounts/savedatemessageforjob.vpage?accountId=${accounts.id}">运营内容</a></li></#if>

            <li class="nav-header">管理</li>
            <li><a href="">素材管理</a></li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/officialaccounts/articlelist.vpage?accountId=${accountId}">文章发布管理</a></li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/officialaccounts/usermanagement.vpage?accountId=${accountId}">用户管理</a></li>

            <li class="nav-header">统计</li>
            <li><a href="">概要</a></li>

        </ul>
    </div>
</div>
