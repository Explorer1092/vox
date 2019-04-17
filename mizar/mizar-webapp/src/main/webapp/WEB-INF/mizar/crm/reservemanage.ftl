<#import "../module.ftl" as module>
<@module.page
title="预约管理"
pageJsFile={"siteJs" : "public/script/crm/reservemanage"}
pageJs=["siteJs"]
leftMenu = "预约信息"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" action="/crm/reserve/index.vpage" method="get">
        <div class="item">
            <p>客户称呼</p>
            <input value="${studentName!}" name="studentName" class="v-select" />
        </div>
        <div class="item">
            <p>联系电话</p>
            <input value="${mobile!}" name="mobile" class="v-select" />
        </div>
        <div  class="item">
            <p>预约门店</p>
            <select name="selectedShop" class="v-select">
                <option value="all">全部门店</option>
                <#if shopList?? && (shopList?size gt 0)>
                    <#list shopList as shop>
                        <option <#if shop.shopId == selectedShop>selected</#if> value="${shop.shopId!}">${shop.shopName!}</option>
                    </#list>
                </#if>
            </select>
        </div>
        <div class="item">
            <p>客户状态</p>
            <select name="status" class="v-select">
                <option value="">全部</option>
                <option <#if status == "New">selected</#if> value="Payment">已支付</option>
                <option <#if status == "Attach">selected</#if> value="Attach">已联系</option>
                <option <#if status == "Access">selected</#if> value="Access">已到课</option>
            </select>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>
</div>
<#if currentPageNumber?has_content>
    <#assign pageNumber = currentPageNumber?number />
<#else>
    <#assign pageNumber = 0 />
</#if>

<#if reservePage?? && reservePage?size gt 0>
    <#list reservePage as page>
    <table class="data-table one-page <#if page_index == pageNumber>displayed</#if>">
        <thead>
        <tr>
            <th>
                <input class="checked-all" type="checkbox" />
            </th>
            <th style="width:138px;">预约时间</th>
            <th>学生名</th>
            <th>年龄</th>
            <th>年级</th>
            <th>学校</th>
            <th>校区</th>
            <th>家长</th>
            <th>联系电话</th>
            <th>预约门店</th>
            <th style="width:80px;">客户状态</th>
            <th style="width:110px;">操作变更</th>
            <th style="width:70px;">备注</th>
        </tr>
        </thead>
        <tbody>
        <#list page as item>
            <#switch item.status!>
                <#case "已支付">
                    <#assign preVal = "Payment" postVal = "Attach" postHtml = "已联系" />
                    <#break />
                <#case "已联系">
                    <#assign preVal = "Attach" postVal = "Access" postHtml = "已到课" />
                    <#break />
            </#switch>
            <tr>
                <td><input data-rid="${item.recordId!}" type="checkbox" /></td>
                <td>${item.reserveTime!}</td>
                <td><div class="inner" style="width:40px;">${item.studentName!}</div></td>
                <td><div class="inner">${item.age!}</div></td>
                <td>${item.clazzLevel!}</td>
                <td>${item.school!}</td>
                <td>${item.schoolArea!}</td>
                <td><div class="inner" style="width:40px;">${item.callName!}</div></td>
                <td>${item.mobile!}</td>
                <td><div class="inner" style="width:100px;">${item.shopName!}</div></td>
                <td class="status-cell" data-value="${preVal!}">${item.status!}</td>
                <td class="change-status-cell">
                    <#if item.status != "已到课">
                        <a class="op-btn change-status" data-rid="${item.recordId!}" data-value="${postVal!}" href="javascript:void(0);">${postHtml!}</a>
                    </#if>
                    <a class="op-btn change-remark" data-rid="${item.recordId!}" href="javascript:void(0);" style="margin-right:0;float:right;">备注</a>
                </td>
                <td><div class="inner" style="width:60px;">${item.notes!}</div></td>
            </tr>
        </#list>
        </tbody>
    </table>
    </#list>
<#else>
<table class="data-table one-page displayed">
    <thead>
    <tr>
        <th>
            <input class="checked-all" type="checkbox" />
        </th>
        <th style="width:138px;">预约时间</th>
        <th>学生名</th>
        <th>年级</th>
        <th>家长</th>
        <th>联系电话</th>
        <th>预约门店</th>
        <th style="width:80px;">客户状态</th>
        <th style="width:110px;">操作变更</th>
        <th style="width:70px;">备注</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td colspan="10" style="text-align: center;">暂时还没有预约信息哦~</td>
    </tr>
    </tbody>
</table>
</#if>
<div class="data-table-foot clearfix">
    <span>将选中客户</span>
    <select id="status-change-type" style="width:160px;" class="v-select">
        <option value="Payment-Attach">已支付变为已联系</option>
        <option value="Attach-Access">已联系变为已到课</option>
    </select>
        <span>
            <a class="blue-btn change-status-batch" href="javascript:void(0)">确定</a>
        </span>
</div>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>