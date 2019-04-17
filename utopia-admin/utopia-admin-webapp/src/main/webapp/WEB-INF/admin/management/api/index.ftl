<#include "../index.ftl" />


<!-- start content -->
<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <!--Body content-->
            <div class="hero-unit">
                <ol>
                    <#--<li>
                        内部调用 获得用户对应系统的所有权限页面<br />
                        managementService.apiGetUserAppPath(String userName, String AppName)
                        <br />&nbsp;
                    </li>-->
                    <#if showAdmin?? && showAdmin>
                        <li>
                            外部调用 获得用户对应系统的所有权限页面<br />
                            <a href="${requestContext.webAppContextPath}/management/api/getUserAppPath.vpage?userName=admin&appName=crm&appKey=4d4b701d32a02da77824635f437bf32067361e2d" target="_blank">${requestContext.webAppContextPath}/management/api/getUserAppPath</a><br />
                            appkey = DigestUtils.sha1Hex((USER_NAME + APP_NAME + APP_KEY).getBytes("UTF-8"))<br />&nbsp;
                        </li>
                        <#--<li>
                            内部调用 获得用户访问的页面是否有权限<br />
                            managementService.apiHasUserAppPathRight(String userName, String AppName, String pathName)<br />&nbsp;
                        </li>-->
                        <li>
                            外部调用 获得用户访问的页面是否有权限<br />
                            <a href="${requestContext.webAppContextPath}/management/api/isHasUserAppPathRight.vpage?userName=test&appName=auth&pathName=views/test/alltest&appKey=497d88d92a3611e2ba017cd1c3eeb629" target="_blank">${requestContext.webAppContextPath}/management/api/isHasUserAppPathRight</a><br />
                            appkey = DigestUtils.sha1Hex((USER_NAME + APP_NAME + PATH_NAME + APP_KEY).getBytes("UTF-8"))<br />&nbsp;
                        </li>
                    </#if>
                </ol>
            </div>
        </div>
    </div>
</div>
<!-- end content -->
