<#import "../module.ftl" as module>
<@module.page
title="机构分类管理"
pageJsFile={"siteJs" : "public/script/config/category"}
pageJs=["siteJs"]
leftMenu = "机构分类管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="index-form" action="/config/category/index.vpage" method="get">
        <div class="item">
            <p>一级分类名称</p>
            <select class="v-select" name="category">
                <option value="">--全部--</option>
                <#list firstCategory as fc>
                    <option <#if category?? && fc == category>selected</#if> value="${fc!}">${fc!}</option>
                </#list>
            </select>
            <#--<input value="${category!}" name="category" class="v-select" />-->
        </div>
        <div class="item" style="width:auto;margin-right:10px;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="index-filter" style="float:right;" href="javascript:void(0)">搜索</a>
        </div>
        <div class="item" style="width:auto;margin-right:0;float: right;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="add-category" style="float:right;" href="javascript:void(0)">新增</a>
        </div>
    </form>
</div>
<#if categoryList?? && categoryList?size gt 0>
    <#list categoryList as page>
        <table class="data-table one-page <#if page_index == 0>displayed</#if>">
            <thead>
            <tr>
                <th>一级分类名称</th>
                <th>二级分类名称</th>
                <th style="width:110px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <#if page?? && page?size gt 0>
                <#list page as category>
                <tr>
                    <td id="first_${category.id!}">${(category.firstCategory)!}</td>
                    <td id="second_${category.id!}">${(category.secondCategory)!}</td>
                    <td>
                        <a class="op-btn edit-category" href="javascript:void(0);" data-cid="${category.id!}">编辑</a> &nbsp;&nbsp;
                        <a class="op-btn del-category" href="javascript:void(0);" data-cid="${category.id!}">删除</a>
                    </td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
    </#list>
<#else>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th>一级分类名称</th>
            <th>二级分类名称</th>
            <th style="width:110px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td colspan="3" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
        </tr>
        </tbody>
    </table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
<script id="T:CATEGORY_MODEL" type="text/html">
    <div id="uploaderDialog">
        <input type="hidden" id="category_id" value="<%=cat.id%>">
        <div style="float: left;">
            <div class="input-control">
                <label><span class="red-mark">*</span>一级分类：</label>
                <input type="text" id="category_1" style="width: 180px;" data-title="一级分类" class="item" value="<%=cat.first%>"/>
            </div>
        </div>
        <div style="float: right;">
            <div class="input-control">
                <label><span class="red-mark">*</span>二级分类：</label>
                <input type="text" id="category_2" style="width: 180px;" data-title="二级分类" class="item" value="<%=cat.second%>"/>
            </div>
        </div>
    </div>
</script>
</@module.page>