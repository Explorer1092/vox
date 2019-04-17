<#import "../module.ftl" as module>
<@module.page
title="预约数据"
pageJsFile={"siteJs" : "public/script/data/reserve"}
pageJs=["siteJs"]
leftMenu = "预约数据"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" action="/data/reserve/index.vpage" method="get">
        <div class="item" style="width:120px;">
            <p>机构信息</p>
            <input value="${shopToken!}" name="shopToken" class="v-select" placeholder="机构名称/ID"/>
        </div>
        <#--<div class="item" style="width:120px;">-->
            <#--<p>客户称呼</p>-->
            <#--<input value="${studentName!}" name="studentName" class="v-select" />-->
        <#--</div>-->
        <div class="item" style="width:120px;">
            <p>联系电话</p>
            <input value="${mobile!}" name="mobile" class="v-select" />
        </div>
        <div class="item" style="width:120px;">
            <p>客户状态</p>
            <select name="status" class="v-select">
                <option value="">全部</option>
                <option <#if status == "New">selected</#if> value="New">未接触</option>
                <option <#if status == "Attach">selected</#if> value="Attach">已联系</option>
                <option <#if status == "Access">selected</#if> value="Access">到店</option>
                <option <#if status == "Success">selected</#if> value="Success">付费</option>
            </select>
        </div>
        <div class="item" style="width:120px;">
            <p>开始时间</p>
            <input autocomplete="off" value="${start!}" name="start" id="startDate" class="v-select" />
        </div>
        <div class="item" style="width:120px;">
            <p>结束时间</p>
            <input autocomplete="off" value="${end!}" name="end" id="endDate" class="v-select" />
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>

</div>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="download-frm" action="/data/reserve/downloadexcel.vpage" method="post">
        <div class="item" style="margin:0 5px;line-height:30px;width: 100px;">导出时间区间:</div>
        <div class="item" style="width:180px;">
            <input autocomplete="off" id="startTime" name="start" class="v-select" placeholder="开始时间"/>
        </div>
        <div class="item" style="margin:0 5px;line-height:30px;width: 30px;">至</div>
        <div class="item" style="width:180px;">
            <input autocomplete="off" id="endTime" name="end" class="v-select" placeholder="结束时间"/>
        </div>
    </form>
    <div class="item" style="width:auto; ">
        <a class="blue-btn" id="download-btn" style="float:left;width: 85px;" href="javascript:void(0)">导出Excel</a>
    </div>
    <#--<div class="item" style="width:auto;margin-right:0;">-->
        <#--<a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>-->
    <#--</div>-->
</div>
<#if reservePage?? && reservePage?size gt 0>
    <#list reservePage as page>
    <table class="data-table one-page <#if page_index == 0></#if>displayed">
        <thead>
        <tr>
            <th style="width:138px;">预约时间</th>
            <th>学生名</th>
            <th>年级</th>
            <th>家长</th>
            <th>联系电话</th>
            <th>预约门店</th>
            <th style="width:80px;">客户状态</th>
            <th style="width:70px;">备注</th>
        </tr>
        </thead>
        <tbody>
        <#list page as item>
            <#switch item.status!>
                <#case "未接触">
                    <#assign preVal = "New" postVal = "Attach" postHtml = "已联系" />
                    <#break />
                <#case "已联系">
                    <#assign preVal = "Attach" postVal = "Access" postHtml = "到店" />
                    <#break />
                <#case "到店">
                    <#assign preVal = "Access" postVal = "Success" postHtml = "付费" />
                    <#break />
            </#switch>
            <tr>
                <td>${item.reserveTime!?string("yyyy-MM-dd hh:mm:ss")}</td>
                <td><div class="inner" style="width:40px;">${item.studentName!}</div></td>
                <td>${item.clazzLevel!}</td>
                <td><div class="inner" style="width:40px;">${item.callName!}</div></td>
                <td>${item.mobile!}</td>
                <td><div class="inner" style="width:100px;">${item.shopName!}</div></td>
                <td class="status-cell" data-value="${preVal!}">${item.status!}</td>
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
        <th style="width:138px;">预约时间</th>
        <th>学生名</th>
        <th>年级</th>
        <th>家长</th>
        <th>联系电话</th>
        <th>预约门店</th>
        <th style="width:80px;">客户状态</th>
        <th style="width:70px;">备注</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td colspan="8" style="text-align: center;">请输入机构信息查询预约信息哦~</td>
    </tr>
    </tbody>
</table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>