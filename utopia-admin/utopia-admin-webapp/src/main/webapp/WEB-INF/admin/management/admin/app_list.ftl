<#include "../index.ftl" />

<div class="container">
    <div class="row-fluid">
        <div class="span12 well">
            <legend>业务系统列表： <#if (appListForWrite?length > 0) ><a href="app_new.vpage" class="btn btn-primary pull-right">添加新的业务系统app</a></#if></legend>
            <table class="table table-striped table-bordered">
                <tr>
                    <td></td>
                    <td>业务系统标识</td>
                    <td>业务系统名称</td>
                    <#if showAdmin!>
                        <td>KEY</td>
                        <td>URL</td>
                    </#if>
                    <td>管理员</td>
                </tr>
                <#if appList?has_content>
                    <#list appList as appItem>
                    <tr>
                        <td>${appItem_index+1}</td>
                        <td>${appItem.appName!}</td>
                        <td>${appItem.appDescription!}</td>
                        <#if showAdmin!>
                            <td title="${appItem.appKey!}">鼠标悬停显示</td>
                            <td>${appItem.callBackUrl!}</td>
                        </#if>
                        <td>
                            ${masterList[appItem.appName]!}<br>
                            <#if appListForWrite?contains(appItem.appName)!>
                            <a href="app_admin.vpage?name=${appItem.appName!}" class="btn btn-small pull-right">管理</a>
                            <#if showAdmin!><a href="app_edit.vpage?name=${appItem.appName!}" class="btn btn-small pull-right">修改</a></#if>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>
</div>
