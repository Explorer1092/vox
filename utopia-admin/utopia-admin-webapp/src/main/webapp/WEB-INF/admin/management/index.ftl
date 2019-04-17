<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="MANAGEMENT" page_num=6>

<div class="navbar" >
    <div class="navbar-inner">
        <div class="nav-collapse">
            <div class="tab-pane active" id="tab1">
        <ul class="nav nav-pills">
            <li class=""><a href="${requestContext.webAppContextPath}/management/user/list.vpage">用户</a></li>
            <li class=""><a href="${requestContext.webAppContextPath}/management/group/list.vpage">权限组</a></li>
            <li class=""><a href="${requestContext.webAppContextPath}/management/path/path_list.vpage">路径&角色</a></li>
            <li class=""><a href="${requestContext.webAppContextPath}/management/log/list.vpage">日志</a></li>
            <li class="divider-vertical"></li>
            <li><a href="${requestContext.webAppContextPath}/management/api/">API</a></li>
            <#if showAdmin?? && showAdmin>
            <#--<li><a href="${requestContext.webAppContextPath}/management/log/admin.vpage">管理日志</a></li>-->
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">系统设置 <b class="caret"></b></a>
                <ul class="dropdown-menu">
                    <li><a href="${requestContext.webAppContextPath}/management/admin/department_list.vpage">部门</a></li>
                    <li><a href="${requestContext.webAppContextPath}/management/admin/group_list.vpage">权限组</a></li>
                    <li><a href="${requestContext.webAppContextPath}/management/admin/app_list.vpage">业务系统</a></li>
                </ul>
            </li>
            </#if>
            <#--fixme:和系统设置有啥区别？-->
            <#--<li class="dropdown">-->
                <#--<a href="#" class="dropdown-toggle" data-toggle="dropdown">查看 <b class="caret"></b></a>-->
                <#--<ul class="dropdown-menu">-->
                    <#--&lt;#&ndash;<li><a href="${requestContext.webAppContextPath}/management/admin/showgroup.vpage">查看人员的权限组</a></li>-->
                    <#--<li><a href="${requestContext.webAppContextPath}/management/admin/user_list.vpage">查看权限组的成员</a></li>&ndash;&gt;-->
                    <#--<li><a href="${requestContext.webAppContextPath}/management/admin/department_list.vpage">查看管理员-部门</a></li>-->
                    <#--<li><a href="${requestContext.webAppContextPath}/management/admin/app_list.vpage"">查看管理员-业务系统</a></li>-->
                    <#--<li><a href="${requestContext.webAppContextPath}/management/admin/group_list.vpage"">查看管理员-权限组</a></li>-->
                <#--</ul>-->
            <#--</li>-->
        </ul>
        </div>
    </div>
</div>
</@layout_default.page>
