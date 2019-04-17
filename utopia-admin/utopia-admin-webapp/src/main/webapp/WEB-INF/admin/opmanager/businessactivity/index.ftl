<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="导流运营管理平台" page_num=9>
<#import "../../common/pager.ftl" as pager />
<div id="main_container" class="span9">
    <legend>
        <strong>导流管理</strong>
        <a id="add_advertiser_btn" href="activitydetail.vpage" type="button" class="btn btn-info" style="float: right">添加商品</a>
    </legend>
    <form id="query_frm" class="form-horizontal" method="get" action="${requestContext.webAppContextPath}/opmanager/businessactivity/index.vpage" >
        <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>类型：&nbsp;
                    <select id="type" name="type">
                        <option value="">类型</option>
                        <#list types as activityType>
                            <option <#if activityType==type>selected</#if> value="${activityType!''}">
                                <#switch activityType>
                                    <#case "Pay">
                                        支付
                                        <#break>
                                    <#case "Reserve">
                                        报名
                                        <#break>
                                    <#case "joinGroup">
                                        加群
                                        <#break>
                                    <#case "Subscribe">
                                        预约
                                        <#break>
                                    <#default>
                                </#switch>
                            </option>
                        </#list>
                    </select>
                </label>
            </li>
            <li>
                <button type="submit" id="filter" class="btn btn-primary">查  询</button>
            </li>
        </ul>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="50px">ID</th>
                        <th>生成后的链接</th>
                        <th>文章标题</th>
                        <th width="90px">商品类型</th>

                        <th>上线状态</th>
                        <th>生成时间</th>
                        <th width="90px">查看订单</th>
                        <th>修改</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if businessActivityPage?? && businessActivityPage.content?? >
                            <#list businessActivityPage.content as activity >
                            <tr>
                                <td>${activity.id!}</td>
                                <td>${activity.activityUrl!}</td>
                                <td>${activity.title!}</td>
                                <td data-type="${activity.activityType!}">
                                    <#switch activity.activityType>
                                        <#case "Pay">
                                            支付
                                            <#break>
                                        <#case "Reserve">
                                            报名
                                            <#break>
                                        <#case "joinGroup">
                                            加群
                                            <#break>
                                        <#case "Subscribe">
                                            预约
                                            <#break>
                                        <#default>
                                    </#switch>
                                </td>
                                <td>
                                    <#switch (activity.status)>
                                        <#case "Online">
                                            上线
                                            <#break>
                                        <#case "Offline">
                                            下线
                                            <#break>
                                        <#default>
                                           状态异常
                                    </#switch>
                                </td>
                                <td>${activity.createDatetime}</td>
                                <td>
                                    <a href="checkHistory.vpage?aid=${activity.id!}&type=${activity.activityType!''}">
                                        <#switch activity.activityType>
                                            <#case "Pay">
                                                查看支付
                                                <#break>
                                            <#case "Reserve">
                                                查看报名
                                                <#break>
                                            <#case "Subscribe">
                                                查看预约
                                                <#break>
                                            <#default>
                                        </#switch>
                                    </a>
                                </td>
                                <td>
                                    <a href="activitydetail.vpage?aid=${activity.id}">编辑</a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<style>
    .table td , .table th{
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
</@layout_default.page>