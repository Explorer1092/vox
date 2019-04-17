<#import "../../module.ftl" as module>
<@module.page
title="商品分类管理"
pageJsFile={"siteJs" : "public/script/basic/grouponcategory"}
pageJs=["siteJs"]
leftMenu="商品分类管理"
>
<div class="op-wrapper clearfix">
    <a class="blue-btn" href="/groupon/category/detail.vpage">新增分类</a>
</div>
<table class="data-table displayed" style="margin-bottom:50px;">
    <thead>
    <tr>
        <th>ID</th>
        <th>名称</th>
        <th>标识</th>
        <th>是否禁用</th>
        <th>排序值</th>
        <th>创建时间</th>
        <th>修改时间</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
    <#if categoryList?size gt 0>
        <#list categoryList as category>
        <tr>
            <td>${category.id!''}</td>
            <td>${category.categoryName!''}</td>
            <td>${category.categoryCode!''}</td>
            <td><#if category.disabled??>${category.disabled?string("是","否")}</#if></td>
            <td>${category.orderIndex!''}</td>
            <td><#if category.createAt??>${category.createAt?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
            <td><#if category.updateAt??>${category.updateAt?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
            <td>
                <a class="op-btn" href="/groupon/category/detail.vpage?id=${category.id!''}" style="margin-right:0;">编辑</a>

            </td>
            </td>
        </tr>
        </#list>
    <#else>
        <tr>
            <td colspan="5" style="text-align: center">暂时还没有分类哦~</td>
        </tr>
    </#if>
    </tbody>
</table>
</@module.page>