<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <legend><a href="../user/userhomepage.vpage?userId=${teacher.id!""}">${teacher.profile.realname!}</a>老师的唤醒历史</legend>
    </div>
    <div>
        <legend>查询结果：</legend>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>被唤醒人</th>
                <th> 是否成功</th>
                <th> 唤醒类型</th>
                <th> 唤醒时间</th>
            </tr>
            <#if datas?has_content>
                <#list datas as data>
                    <tr>
                        <td><a href="../user/userhomepage.vpage?userId=${data.inviteeId!""}">${data.realname!}</a>(${data.inviteeId!""})</td>
                        <td>${data.over?string('是','否')!}</td>
                        <td>${data.activationType!""}</td>
                        <td>
                            ${data.createTime?string('yyyy-MM-dd HH:mm:ss')}
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>