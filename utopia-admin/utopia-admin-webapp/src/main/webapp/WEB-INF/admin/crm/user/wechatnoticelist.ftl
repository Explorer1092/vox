<#-- @ftlvariable name="intervalDay" type="java.lang.Integer" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userRecordInfoList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">

    <#assign timeRegion=1>
    <#if history>
        <#assign timeRegion=7>
    </#if>

    <fieldset>
        <legend>用户<a href="userhomepage.vpage?userId=${user.id!}">${user.profile.realname!}</a>微信消息历史(最近${timeRegion}天)</legend>
    </fieldset>

    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>openID</th>
            <th style="width: 50%">消息内容</th>
            <th>时间</th>
            <th>状态</th>
            <th>类型</th>
            <th>错误码</th>
            <#if !history>
            <th>操作</th>
            </#if>
        </tr>
        <#if notices?? && notices?has_content>
            <#list notices as notice>
                <tr>
                    <td>${notice.openId!}</td>
                    <td>${notice.message!}</td>
                    <td>${notice.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${notice.stateDesc!}</td>
                    <td>${notice.typeDesc!}</td>
                    <td>${notice.errorCode!'无'}</td>
                    <#if !history>
                    <td><input id="send_btn_${notice.id!}" type="button" value="发送"/></td>
                    </#if>
                </tr>
            </#list>
        </#if>
    </table>
</div>

<script type="text/javascript">
    $(function () {
        $("input[id^='send_btn_']").bind('click', function () {
            var id = $(this).attr("id").substring("send_btn_".length);
            if (!id) {
                alert("参数有误！");
                reurn;
            }
            var data = {
                id: id
            };
            $.post('/crm/user/sendwechatnotice.vpage', data, function (data) {
                if (data > 0) {
                    alert("发送完成！");
                } else {
                    alert("发送失败！");
                }
                window.location.reload();
            });
        });
    });
</script>
</@layout_default.page>
