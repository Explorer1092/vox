<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span12">
    <div>
        <fieldset class="inline">
            <legend>用户<#if user??><a
                    href="../user/userhomepage.vpage?userId=${user.id!}">${user.profile.realname!}</a>(${user.id!}
                )</#if>成长值详情
            </legend>
        </fieldset>
        <br/>
        <ul class="inline">
            <li>
                成长值：${growth!}
            </li>
            <li>
                成长等级：${growthLevel!}
            </li>
        </ul>
        <table id="students" class="table table-hover table-striped table-bordered">
            <tr>
                <th> 时间</th>
                <th> 数量</th>
                <th> 操作类型</th>
            </tr>
            <#if logs?has_content>
                <#list logs as log>
                    <tr >
                        <td>${log.actionTime!""}</td>
                        <td>${log.delta!0}</td>
                        <td>${log.type}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script type="text/javascript">

</script>
</@layout_default.page>