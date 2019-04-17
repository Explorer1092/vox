<#-- @ftlvariable name="unactivateUserList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="手机绑定人工验证" page_num=3>
<div class="span9">
    <fieldset><legend>手机绑定人工验证</legend></fieldset>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th></th>
            <th>申请时间</th>
            <th>用户</th>
            <th>手机</th>
            <th>学校</th>
            <th>省/市</th>
            <th>操作</th>
        </tr>
        <#if unactivateUserList?has_content>
            <#list unactivateUserList as unactivateUser>
                <tr data-user_id="${unactivateUser.userId!}" data-mobile="${unactivateUser.mobile!}">
                    <td>${unactivateUser_index + 1}</td>
                    <td>${unactivateUser.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td><a href="../user/userhomepage.vpage?userId=${unactivateUser.userId!}">${unactivateUser.userName!}</a>(${unactivateUser.userId!})</td>
                    <td>${unactivateUser.mobile!}</td>
                    <td><a href="../school/schoolhomepage.vpage?schoolId=${unactivateUser.schoolId!}">${unactivateUser.schoolName!}</a>(${unactivateUser.schoolId!})</td>
                    <td>${unactivateUser.regionName!}(${unactivateUser.regionCode!})</td>
                    <td>
                        <a id="activateMobile_${unactivateUser.userId!}" href="javascript:void(0)">绑定</a>
                        <a id="deleteRecord_${unactivateUser.userId!}" href="javascript:void(0)">删除</a>
                    </td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function() {

        $('a[id^="activateMobile_"]').click(function() {
            var $thisTr = $(this).closest('tr');
            var postData = {
                userId : $thisTr.data('user_id'),
                mobile : $thisTr.data('mobile')
            };
            $.post('activateusermobile.vpage', postData, function(data) {
                alert(data.info);
                if(data.success) {
                    $thisTr.remove();
                }
            });
        });

        $('a[id^="deleteRecord_"]').click(function() {
            if(confirm('确认删除记录？')) {
                var $thisTr = $(this).closest('tr');
                var postData = {
                    userId : $thisTr.data('user_id'),
                    mobile : $thisTr.data('mobile')
                };
                $.post('deleterecord.vpage', postData, function(data) {
                    alert(data.info);
                    if(data.success) {
                        $thisTr.remove();
                    }
                });
            }
        });

    });
</script>
</@layout_default.page>