<#import "../../module.ftl" as module>
<@module.page
title="新建分类"
pageJsFile={"siteJs" : "public/script/basic/grouponcategory"}
pageJs=["siteJs"]
leftMenu="分类管理"
>
<div class="bread-nav">
    <a class="parent-dir" href="/groupon/category/list.vpage">分类管理</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">${isNew?string("新增","编辑")}分类</a>
</div>
<h3 class="h3-title">
    分类
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span>为必填项</span>
</h3>
<form id="detail-form"   action="${isNew?string("/groupon/category/addcategory.vpage","/groupon/category/updatecategory.vpage")}" method="post">
    <#if isNew?? &&!isNew><input value="${(category.id)!}" name="id" style="display:none;"></#if>
    <div style="float:left;">
        <div class="input-control">
            <label><span class="red-mark">*</span>分类名称：</label>
            <input name="categoryName" data-title="分类名称" class="require item" value="${(category.categoryName)!}" />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>排序值：</label>
            <input name="orderIndex" id="orderIndex" data-title="排序值,大值靠前" class="require item" value="${(category.orderIndex)!}"  />
        </div>
    </div>
    <div>
        <div class="input-control">
            <label><span class="red-mark">*</span>分类标识：</label>
            <input name="categoryCode" id="categoryCode" data-title="分类标识" class="require item" value="${(category.categoryCode)!}"  />
        </div>
        <div class="input-control clearfix">
            <label>是否禁用：</label>
            <label class="checkbox"><input class="require radio" type="radio" value="1" id="disabled" name="disabled" <#if (category.disabled)??><#if category.disabled>checked</#if><#else>checked</#if> >是</label>
            <label class="checkbox"><input class="require radio" type="radio" value="0" id="disabled" name="disabled" <#if (category.disabled)?? && !category.disabled>checked</#if>>否</label>
        </div>
    </div>
    <div class="clearfix submit-box">
        <a id="save-btn" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">保存分类</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/groupon/category/list.vpage">取消</a>
    </div>
</form>
</@module.page>
