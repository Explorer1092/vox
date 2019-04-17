<#import "../../module.ftl" as module>
<@module.page
title="选择课程生效门店"
pageJsFile={"siteJs" : "public/script/basic/goods"}
pageJs=["siteJs"]
leftMenu="课程管理"
>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/goods/index.vpage">课程列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">选择生效门店</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">新增课程</a>
</div>
<div class="op-wrapper clearfix">
    <span class="title-h1">选择课程生效门店</span>
    <a class="blue-btn goods-step2" href="javascript:void(0)">下一步</a>
</div>
<div id="selected-shop" class="op-wrapper clearfix" <#if (!shopList??)!false>style="display: none;" </#if>>
    <#if shopList?? && shopList?size gt 0>
        <#list shopList as shop>
            <div id="sid-${shop.shopId!}" class='shop-label' data-sid="${shop.shopId!}">
                <label>${shop.shopName!}</label><span><a class='cancel-shop' href='javascript:void(0);'>&nbsp;&times;</a></span>
            </div>
        </#list>
    </#if>
</div>
<div class="op-wrapper orders-wrapper clearfix">
    <div style="padding: inherit;">
        <div class="item">
            <p>机构信息</p>
            <input id="shopToken" class="v-select" placeholder="请输入机构ID或名称"/>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="search-shop" href="javascript:void(0)">搜索</a>
        </div>
    </div>
</div>
<table class="data-table one-page displayed">
    <thead>
    <tr>
        <th style="width:50px; text-align: center;">选择</th>
        <th style="width: 120px;">机构ID</th>
        <th>机构名称</th>
        <th style="width:50px; text-align: center;">选择</th>
        <th style="width: 120px;">机构ID</th>
        <th>机构名称</th>
    </tr>
    </thead>
    <tbody id="shopList">
    <tr><td colspan="6" style="text-align: center">该查询条件下没有数据</td></tr>
    </tbody>
</table>
<script id="T:ShopChooser" type="text/html">
    <%var item = data.shopList%>
    <%if(data.success && item.length){%>
    <%for(var i = 0; i < item.length; i++){%>
    <%if(i % 2 == 0){%><tr><%}%>
        <td style="text-align: center;">
            <input class="select-shop" type="checkbox" <%if(item[i].selected) {%>checked<%}%> />
        </td>
        <td><%=item[i].shopId%></td>
        <td><%=item[i].shopName%></td>
    <%if(i % 2 !=0 || i == item.length-1){%></tr><%}%>
    <%}%>
    <%}else{%>
    <tr><td colspan="6" style="text-align: center"><%if(data.success){%>该查询条件下没有数据<%}else{%><%=data.info%><%}%></td></tr>
    <%}%>
</script>
</@module.page>