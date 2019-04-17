<#include "../index.ftl" />
<#import "../../mizar/pager.ftl" as pager />

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>
                    ${pageMessage!}列表：
                    <#if (appListForWrite?length > 0) >
                        <span class="pull-right">
                            <a class="btn btn-primary" href="path_new.vpage">添加新${pageMessage!}</a>
                            <a class="btn btn-primary" href="path_new_batch.vpage">批量添加新路径</a>
                            <a class="btn btn-primary" href="role_new.vpage">添加新角色</a>
                            <a class="btn btn-primary" href="path_new_unsigned.vpage">未配置路径</a>
                            <#--<a class="btn btn-primary" href="/jman/path/role/member/list/">批量关联 角色-权限组</a>-->
                        </span>
                    </#if>
                </legend>

                <form class="form-horizontal" id="query_frm">
                    <select name='appName'>
                        <option value="">All</option>
                        <#list selectAppList as applistItem>
                        <option value="${applistItem.appName!}" <#if applistItem.appName == appName> selected="selected" </#if>>${applistItem.appDescription!}</option>
                        </#list>
                    </select>
                    <input type="text" class="input-large" placeholder="Path" name="pathName" value="${pathName!}">
                    <input type="text" class="input-large" placeholder="名称" name="pathDescription" value="${pathDescription!}">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>

                    <button type="submit" class="btn">查找</button>
                </form>

                <@pager.pager/>
                <table class="table table-striped table-bordered table-hover">
                    <tr>
                        <th></th>
                        <th>所属业务系统</th>
                        <th>路径</th>
                        <th>名称</th>
                        <th>角色(点击关联权限组)</th>
                        <th></th>
                    </tr>
                    <#if pathPage?? && pathPage.content?? >
                    <#list pathPage.content as pathItem>
                    <tr>
                        <td>${pathItem_index+1}</td>
                        <td>${appNames[pathItem.appName]!}</td>
                        <td>${pathItem.pathName!}</td>
                        <td>${pathItem.pathDescription!}</td>
                        <td>
                            <#if pathItem.id??>
                            <#assign pathItemId = "${pathItem.id!}" >
                            <#if pathRoleMap[pathItemId]??>
                                <#list pathRoleMap[pathItemId] as roleItem>
                                    <a href="path_role_group.vpage?pathRoleId=${roleItem.pathRoleId!}">${roleList[roleItem.roleName]!} - ${roleItem.roleName!}</a> <br>
                                </#list>
                            </#if>
                            </#if>
                        </td>
                        <td>
                            <a class="btn btn-mini" href="path_role.vpage?pathId=${pathItem.id}">关联角色</a>
                            <#if appListForWrite?contains(pathItem.appName)>
                                <#--<a href="mod/?dn={{ one.dn }}&t=path" class="btn btn-mini">修改</a>-->
                                <#--<a href="copy/?dn={{ one.dn }}&t=path" class="btn btn-mini">复制</a>-->
                            </#if>
                            <#if appListForDelete?contains(pathItem.appName)>
                                <#--<a href="del/?dn={{ one.dn }}" class="btn btn-mini btn-danger" onclick="javascript:return de_confirm();">删除</a>-->
                            </#if>
                        </td>
                    </tr>
                    </#list>
                    </#if>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function de_confirm(){
        if ( ! confirm("确认删除吗?")){
            return false;
        }
    }
    $("select").change(function () {
        $('form').submit();
    });
</script>
