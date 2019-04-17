<#include "../index.ftl" />

<div class="container">
    <div class="breadcrumb">${pageMassage!}</div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>业务系统标识</td>
                        <td>业务系统名称</td>
                    </tr>
                    <tr>
                        <td>${appInfo.appName!}</td>
                        <td>${appInfo.appDescription!}</td>
                    </tr>
                </table>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>管理员ID</td>
                        <td>管理</td>
                    </tr>
                    <#if masterList??>
                    <#list masterList as masterInfo>
                    <tr>
                        <td>${masterInfo_index+1}</td>
                        <td>${masterInfo.userName!}</td>
                        <td class="form-inline">
                            <#if rightDelete>
                            <label class="checkbox">
                                <input type="checkbox" data-action="rightRead" value="${masterInfo.id!''}" class="role" <#if masterInfo.rightRead! >checked</#if>> 读
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" data-action="rightWrite" value="${masterInfo.id!''}" class="role" <#if masterInfo.rightWrite! >checked</#if>> 写
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" data-action="rightDelete" value="${masterInfo.id!''}" class="role" <#if masterInfo.rightDelete!>checked</#if> > 删除
                            </label>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                    </#if>
                </table>

                <form class="form-horizontal" id="form1" name="form1" method="post" action="" >
                    <input type="text" class="input-small" placeholder="用户名" name="userName" value="${userName!}">
                    <input type="hidden"  name="appName" value="${appInfo.appName!}">
                    <button type="submit" class="btn">添加新管理员</button>
                </form>

            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $('.role').click( function () {
        var status = $(this).is(':checked');
        if (status) {
            $.post('app_admin_edit.vpage'
                    , {'do': 'add', 'action': $(this).data('action'), adminMasterId: $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('app_admin_edit.vpage'
                    , {'do': 'del', 'action': $(this).data('action'), adminMasterId: $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
</script>
