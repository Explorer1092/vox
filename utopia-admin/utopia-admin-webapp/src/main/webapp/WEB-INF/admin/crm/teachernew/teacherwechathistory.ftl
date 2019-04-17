<#import "../../layout_default.ftl" as layout_default>
<#import "./teacherinfoheader.ftl" as info_header/>
<@layout_default.page page_title="CRM" >
<style>
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div style="margin-left: 2em">
    <#--<@info_header.teacherHead/>-->
    <legend>微信绑定</legend>
        <table class="table table-hover table-striped table-bordered" style="width: 1000px;">
            <tr>
                <th>教师ID</th>
                <th>OPEN_ID</th>
                <th>绑定时间</th>
                <th>更新时间</th>
                <th>绑定方式</th>
                <th>是否有效</th>
                <th>操作</th>
            </tr>
            <#if wechats?has_content>
                <#list wechats as wechat>
                    <tr>
                        <td>
                        ${wechat.USER_ID}
                        </td>
                        <td>${wechat.OPEN_ID}</td>
                        <td>${wechat.CREATE_DATETIME}</td>
                        <td>${wechat.UPDATE_DATETIME}</td>
                        <td>${wechat.SOURCE}</td>
                        <td>
                            <#if wechat.DISABLED>
                                无效
                            <#else >
                                有效
                            </#if>
                        </td>
                        <td>
                            <#if !wechat.DISABLED>
                                <a href="javascript:void(0);" class="btn btn-info sendwxnotice" data-id="${wechat.USER_ID}" style="width: 90px">发送微信消息</a>
                            </#if>
                        </td>
                    </tr>
                </#list>
            <#else ><td >暂无历史信息</td>
            </#if>
        </table>
    </div>

</@layout_default.page>