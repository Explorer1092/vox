<#import "../../module.ftl" as module>
<#import "../../common/pager.ftl" as pager />
<@module.page
title="品牌列表"
pageJsFile={"siteJs" : "public/script/basic/brand", "commonJs" :"public/script/common/common"}
pageJs=["siteJs"]
leftMenu="品牌管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" class="form-table" action="/basic/brand/index.vpage" method="get">
        <input type="hidden" name="pageIndex" id="pageIndex" value="${pageIndex!1}">
        <#--<table cellspacing="0" cellpadding="0">-->
            <#--<tr>-->
                <#--<td>品牌名称:-->
                    <#--<input type="text" class="form-control" name="brandName" size="10" value="<#if Request.brand??>${Request["brand"]}</#if>" placeholder="请输入品牌名称"></td>-->
            <#--</tr>-->
            <#--<tr>-->
                <#--<td colspan="2">-->
                    <#--<a class="blue-btn" id="query-btn" style="float:left;" href="javascript:void(0)">搜索</a>-->
                    <#--<a class="blue-btn" id="add-btn" style="float:left;margin-left:20px;padding-left: 20px;" href="/basic/brand/add.vpage">新增品牌</a>-->
                <#--</td>-->
            <#--</tr>-->
        <#--</table>-->
        <div class="item">
            <p>品牌名称</p>
            <input value="${brandName!}" name="brandName" class="v-select" placeholder="请输入品牌名称"/>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="query-btn" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
        <#if currentUser.isOperator()>
            <div class="item" style="width:auto; float: right;">
                <p style="color:transparent;">.</p>
                <a class="blue-btn" href="/basic/brand/add.vpage">新增品牌</a>
            </div>
        </#if>
    </form>
</div>

<table class="data-table one-page displayed">
    <thead>
    <tr>
        <th>名称</th>
        <th>品牌LOGO</th>
        <th>品牌规模</th>
        <th>创立时间</th>
        <th>录入时间</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
        <#if brandList?? && brandList?has_content>
            <#list brandList as brand>
            <tr>
                <td>${brand.brandName!''}</td>
                <td><#if brand.brandLogo??><img src="${brand.brandLogo!''}" style="width: 60px;height: 60px"></#if></td>
                <td>${brand.shopScale!''}</td>
                <td><#if brand.establishment??>${brand.establishment!''}</#if></td>
                <td><#if brand.createAt??>${brand.createAt?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
                <td>
                    <#if currentUser.isOperator() && (changeBrandIdList?? && !changeBrandIdList?seq_contains("${brand.id}"))>
                        <a class="op-btn" href="/basic/brand/edit.vpage?id=${brand.id!''}" style="margin-right:0;">编辑</a>
                        &nbsp; &nbsp;
                    </#if>
                    <a class="op-btn  " href="/basic/brand/view.vpage?id=${brand.id!''}" style="margin-right:0;">详情</a>
                </td>
            </tr>
            </#list>
        <#else>
        <tr><td colspan="6" style="text-align: center;"><strong>没有数据</strong></td></tr>
        </#if>
    </tbody>
</table>
<div id="paginator" pageIndex="${(pageIndex!1)}" class="paginator clearfix" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@module.page>

