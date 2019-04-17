<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span12">
    <div>
        <fieldset class="inline">
            <legend>用户<#if user??><a
                    href="../user/userhomepage.vpage?userId=${user.id!}">${user.profile.realname!}</a>(${user.id!}
                )</#if>成就详情
            </legend>
        </fieldset>
        <table id="students" class="table table-hover table-striped table-bordered">
            <tr>
                <th>成就名称</th>
                <th>时间</th>
                <th>等级</th>
            </tr>
            <#if achievements?has_content>
                <#list achievements as achievement>
                    <tr >
                        <td>${achievement.name!""}</td>
                        <td>
                           <#if achievement.ct??>
                             ${achievement.ct?string("yyyy-MM-dd HH:mm:ss")}
                           <#else>
                               未知
                           </#if>

                        </td>
                        <td>${achievement.level!0}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script type="text/javascript">

</script>
</@layout_default.page>