<#import "../../module.ftl" as module>
<@module.page
title="课程管理"
pageJsFile={"siteJs" : "public/script/basic/goods"}
pageJs=["siteJs"]
leftMenu="课程管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" action="/basic/goods/index.vpage" method="get">
        <div class="item">
            <p>课程信息</p>
            <input value="${token!}" name="token" class="v-select" placeholder="请输入课程ID或名称"/>
        </div>
        <div class="item">
            <p>课程状态</p>
            <select name="status" class="v-select">
                <option value="">全部</option>
                <option <#if status?? && status=='PENDING'>selected</#if> value="PENDING">待审核</option>
                <option <#if status?? && status=='ONLINE'>selected</#if> value="ONLINE">在线</option>
                <option <#if status?? && status=='OFFLINE'>selected</#if> value="OFFLINE">离线</option>
            </select>
        </div>
        <div class="item">
            <p>机构信息</p>
            <input value="${shopToken!}" name="shopToken" class="v-select" placeholder="请输入机构ID或名称"/>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>
    <div class="item" style="width:auto; float: right;">
        <p style="color:transparent;">.</p>
        <a class="blue-btn" href="chooseshop.vpage">新增课程</a>
    </div>
</div>
<#if goodsList?? && goodsList?size gt 0>
    <#list goodsList as page>
    <table class="data-table one-page <#if page_index == 0></#if>displayed">
        <thead>
        <tr>
            <#--<th>课程ID</th>-->
            <th>课程名称</th>
            <th>机构名称</th>
            <th>课程价格</th>
            <th>课程状态</th>
            <th style="width:100px;">操作</th>
        </tr>
        </thead>
        <tbody>
            <#list page as goods>
            <tr>
                <#--<td>${goods.goodsId!''}</td>-->
                <td>${goods.goodsName!''}</td>
                <td>${goods.shopName!''}</td>
                <td>${goods.price!''}</td>
                <td>${goods.status!''}</td>
                <td style="width: 150px;">
                    <#--都可以编辑，审核通过后才能在线上看到-->
                    <a class="op-btn" href="/basic/goods/detail.vpage?gid=${goods.goodsId!''}&edit=true" style="margin-right:0;">编辑</a>
                    <a class="op-btn" href="/basic/goods/detail.vpage?gid=${goods.goodsId!''}" style="margin-right:0;">查看</a>
                    <#--只有运营人员有操作上下线的权限-->
                    <#if currentUser.isOperator()>
                        <#switch goods.status!>
                            <#case "在线">
                                <a class="op-btn op-status" data-status="OFFLINE" data-gid="${goods.goodsId!}" href="javascript:void(0);" style="margin-right:0;float:right;">下线</a>
                                <#break />
                            <#case "离线">
                            <#case "待审核">
                                <a class="op-btn op-status" data-status="ONLINE" data-gid="${goods.goodsId!}" href="javascript:void(0);" style="margin-right:0;float:right;">上线</a>
                                <#break />
                            <#default>
                                <#break />
                        </#switch>
                    </#if>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
    </#list>
<#else>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <#--<th>课程ID</th>-->
            <th>课程名称</th>
            <th>机构名称</th>
            <th>课程价格</th>
            <th>课程状态</th>
            <th style="width:100px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td colspan="5" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
        </tr>
        </tbody>
    </table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>