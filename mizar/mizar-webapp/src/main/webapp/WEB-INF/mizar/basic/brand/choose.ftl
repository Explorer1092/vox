<#--例：layout 1 -->
<#import "../../layout/webview.layout.ftl" as layout/>
<#import "../../common/pager.ftl" as pager />
<@layout.page
title="品牌列表"
pageJsFile={"siteJs" : "public/script/basic/brand", "commonJs" :"public/script/common/common"}
pageCssFile={"mizar" : ["/public/skin/css/skin"]}
pageJs=["siteJs"]
>
<div class="op-wrapper orders-wrapper clearfix"  style="background: #fff;">
    <form id="filter-form" class="form-table" action="/basic/brand/choose.vpage" method="get">
        <input type="hidden" name="pageIndex" id="pageIndex" value="${pageIndex!1}">
        <#--<table cellspacing="0" cellpadding="0">-->
            <#--<tr>-->
                <#--<td>品牌名称:-->
                    <#--<input type="text" class="form-control" name="brandName" size="10" value="<#if Request.brand??>${Request["brand"]}</#if>" placeholder="请输入品牌名称"></td>-->
                <#--<td>-->
                    <#--<label>状态：</label>-->
                    <#--<select name="postFree">-->
                        <#--<option value="">全部</option>-->
                        <#--<option value="1" <#if paramPostFree?? && "1"=="${paramPostFree}">selected</#if> >是</option>-->
                        <#--<option value="0" <#if paramPostFree?? && "0"=="${paramPostFree}">selected</#if>>否</option>-->
                    <#--</select>-->
                <#--</td>-->
                <#--<td>-->
                    <#--<a class="blue-btn" id="query-btn" style="float:left;" href="javascript:void(0)">搜索</a>-->
                <#--</td>-->
            <#--</tr>-->
        <#--</table>-->
        <div class="item" style="margin-right: 40px;">
            <p>品牌ID</p>
            <input value="<#if Request.bid??>${Request["bid"]}</#if>" name="brandId" class="v-select" placeholder="请输入品牌ID" style="width: 180px;"/>
        </div>
        <div class="item" style="margin-right: 40px;">
            <p>品牌名称</p>
            <input value="<#if Request.brand??>${Request["brand"]}</#if>" name="brandName" class="v-select" placeholder="请输入品牌名称" style="width: 180px;"/>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="query-btn" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>
</div>

<table class="data-table one-page displayed">
    <thead>
    <tr>
        <th width="30%">ID</th>
        <th>名称</th>
        <#--<th>操作</th>-->
    </tr>
    </thead>
    <tbody>
        <#if brandList?? && brandList?has_content>
            <#list brandList as brand>
            <tr name="recordRow" brandName="${brand.brandName!''}" brandId="${brand.id!''}">
                <td>${brand.id!''}</td>
                <td>${brand.brandName!''}</td>
                <#--<td>操作</td>-->
            </tr>
            </#list>
        <#else>
        <tr><td colspan="2" style="text-align: center;"><strong>没有数据</strong></td></tr>
        </#if>
    </tbody>
</table>
<div id="paginator" pageIndex="${(pageIndex!1)}" <#--title="后台从0开始,分页插件从第1页开始"-->  class="paginator clearfix" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@layout.page>


