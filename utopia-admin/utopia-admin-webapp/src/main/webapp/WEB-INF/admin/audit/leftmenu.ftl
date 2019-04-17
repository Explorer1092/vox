<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<div class="span2">
    <div class="well sidebar-nav" style="background-color: #fff;">
        <li data-toggle="collapse" data-target="#article" class="nav-header">申请管理</li>
        <div id="article">
            <ul class="nav nav-list">
                <li><a href="${requestContext.webAppContextPath}/audit/apply/apply.vpage">创建申请</a></li>
                <li><a href="${requestContext.webAppContextPath}/audit/apply/list.vpage">我的申请</a></li>
            </ul>
        </div>

        <li data-toggle="collapse" data-target="#push" class="nav-header">审核管理</li>
        <div id="push">
            <ul class="nav nav-list">
                <li><a href="${requestContext.webAppContextPath}/audit/workflow/todo_list.vpage">待处理</a></li>
                <li><a href="${requestContext.webAppContextPath}/audit/workflow/done_list.vpage">已处理</a></li>
            </ul>
        </div>
    </div>
</div>
