<#import "../module.ftl" as module>
<@module.page
title="功能权限"
pageJsFile={"siteJs" : "public/script/config/syspath"}
pageJs=["siteJs"]
leftMenu="功能权限"
>
<div class="bread-nav">
    <a class="parent-dir" href="/config/syspath/index.vpage">功能权限</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">${isNew?string("新增","编辑")}功能权限</a>
</div>
<form id="detail-form" action="/config/syspath/add.vpage" method="post">
    <input title="" value="${pathId!}" name="id" style="display:none;">
    <input title="" value="" name="roleGroupIds" style="display:none;">
    <input name="" name="is-new" value="${isNew?string("true","false")}" style="display:none;">
    <div style="float:left;">
        <div class="input-control">
            <label>一级名称：</label>
            <input title="" name="functionName" data-title="一级功能名称" class="require item" value="${(sysPathInfo.appName)!}" />
        </div>
        <div class="input-control">
            <label>二级名称：</label>
            <input title="" name="pathName" data-title="二级功能名称" class="require item" value="${(sysPathInfo.pathName)!}" />
        </div>
        <div class="input-control">
            <label>功能说明：</label>
            <textarea title="" name="desc" data-title="功能说明" class="require" style="resize: none" rows="3">${(sysPathInfo.description)!}</textarea>
        </div>
        <div class="input-control">
            <label>用户角色：</label>
        <#--<div class="checkboxes clearfix" style="overflow: hidden;">
            <#list allRoleMap?keys as item>
                <label class="checkbox"><input class="role-checkbox" data-value="${item!}" type="checkbox"
                    <#if sysPathInfo?? && sysPathInfo.authRoleList??>
                        <#list sysPathInfo.authRoleList as roleItem>
                            <#if item == roleItem.role?string>checked</#if>
                        </#list>
                    </#if>
                />${allRoleMap[item?string].roleName!}</label> <br/>
            </#list>
        </div>-->
            <div class="fancytree-checkbox" id="role-tree" ></div>
        </div>
    </div>


    <div class="clearfix submit-box" style="margin:20px 85px;">
        <a id="save-btn" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/config/syspath/index.vpage">取消</a>
    </div>
</form>
</@module.page>