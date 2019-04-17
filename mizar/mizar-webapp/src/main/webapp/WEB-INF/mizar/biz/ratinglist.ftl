<#import "../module.ftl" as module>
<@module.page
title="评论管理"
pageJsFile={"siteJs" : "public/script/biz/ratingmanage"}
pageJs=["siteJs"]
leftMenu = "用户评论"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" action="/biz/rating/index.vpage" method="get">
        <div class="item">
            <p>机构信息</p>
            <input value="${shopToken!}" name="shopToken" class="v-select" placeholder="机构名称/ID"/>
        </div>
        <div class="item">
            <p>内容关键字</p>
            <input value="${content!}" name="content" class="v-select" />
        </div>
        <div  class="item">
            <p>评论星级</p>
            <select name="ratingStar" class="v-select">
                <option value="">不限</option>
                <option <#if ratingStar?? && ratingStar == 1>selected</#if> value="1">★</option>
                <option <#if ratingStar?? && ratingStar == 2>selected</#if> value="2">★★</option>
                <option <#if ratingStar?? && ratingStar == 3>selected</#if> value="3">★★★</option>
                <option <#if ratingStar?? && ratingStar == 4>selected</#if> value="4">★★★★</option>
                <option <#if ratingStar?? && ratingStar == 5>selected</#if> value="5">★★★★★</option>
            </select>
        </div>
        <div class="item">
            <p>评论状态</p>
            <select name="status" class="v-select">
                <option value="ALL">全部</option>
                <option <#if status?? && status == "PENDING">selected</#if> value="PENDING">待审核</option>
                <option <#if status?? && status == "ONLINE">selected</#if> value="ONLINE">在线</option>
                <option <#if status?? && status == "OFFLINE">selected</#if> value="OFFLINE">离线</option>
                <option <#if status?? && status == "DELETED">selected</#if> value="DELETED">已删除</option>
            </select>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>
</div>
<#if ratingPage?? && ratingPage?size gt 0>
    <#list ratingPage as page>
    <table class="data-table one-page <#if page_index == 0></#if>displayed">
        <thead>
        <tr>
            <th style="width:100px;">评论时间</th>
            <th>客户称呼</th>
            <th>联系电话</th>
            <th>预约门店</th>
            <th>评论星级</th>
            <th>评论内容</th>
            <th>评论图片</th>
            <th style="width:80px;">评论状态</th>
            <th style="width:111px;">操作变更</th>
        </tr>
        </thead>
        <tbody>
        <#list page as item>
            <#switch item.status!>
                <#case "PENDING">
                    <#assign displayHtml = "待审核" />
                    <#break />
                <#case "ONLINE">
                    <#assign displayHtml = "在线" />
                    <#break />
                <#case "OFFLINE">
                    <#assign displayHtml = "离线" />
                    <#break />
                <#case "DELETED">
                    <#assign displayHtml = "已删除" />
                    <#break />
                <#default>
                    <#assign displayHtml = item.status />
                    <#break />
            </#switch>
            <tr>
                <td>${item.ratingTime?string("yyyy-MM-dd hh:mm")}</td>
                <td><div class="inner" style="width: 80px">${item.userName!}</div></td>
                <td>${item.mobile!}</td>
                <td>${item.shopName!}</td>
                <td>${item.rating!}</td>
                <td><div class="inner show-detail" style="width: 100px;">${item.content!}</div></td>
                <td><#if item.photoList?has_content><a class="op-btn show-pic" href="javascript:void(0);" data-pic="${item.photoList!}">点击查看</a><#else>无评论图片</#if></td>
                <td>${displayHtml!}</td>
                <td class="change-status-cell">
                    <#if item.status == "PENDING">
                        <a class="op-btn change-online" data-rid="${item.rid!}"  href="javascript:void(0);">上线</a>
                        <a class="op-btn change-delete" data-rid="${item.rid!}"  href="javascript:void(0);">删除</a>
                    </#if>
                    <#if item.status == "OFFLINE">
                        <a class="op-btn change-online" data-rid="${item.rid!}"  href="javascript:void(0);">上线</a>
                        <a class="op-btn change-delete" data-rid="${item.rid!}"  href="javascript:void(0);">删除</a>
                    </#if>
                    <#if item.status == "ONLINE">
                        <a class="op-btn change-offline" data-rid="${item.rid!}"  href="javascript:void(0);">下线</a>
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
            <th style="width:138px;">评论时间</th>
            <th>客户称呼</th>
            <th>联系电话</th>
            <th>预约门店</th>
            <th>评论星级</th>
            <th>评论内容</th>
            <th>评论图片</th>
            <th style="width:80px;">评论状态</th>
            <th style="width:110px;">操作变更</th>
        </tr>
        </thead>
        <tbody>
            <tr>
                <td colspan="9" style="text-align: center;">查询不到评论信息~</td>
            </tr>
        </tbody>
    </table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>