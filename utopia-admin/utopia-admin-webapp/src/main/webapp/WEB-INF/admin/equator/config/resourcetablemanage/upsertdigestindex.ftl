<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="资源表管理-增加摘要" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9" style="font-size: 14px">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
    </div>
    <div>
        <#if successInfo??>
            <div class="alert">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${successInfo!}</strong>
            </div>
        </#if>
    </div>


    <h3>增加修改摘要</h3>
    <#include "inner_header.ftl"/>

    <form class="form-horizontal" action="/equator/config/resourcetablemanage/upsertresourcetabledigest.vpage"
          method="post"
          id=digestForm"
          enctype="multipart/form-data">
        <div class="control-group" style="display:none">
            <label class="col-sm-2 control-label">id</label>
            <div class="controls">
                <input type="text" name="id" id="id" value="${(resourceTableDigest.id)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">分类</label>
            <div class="controls">
                <select id="category" name="category">
                    <#if categoryList?exists>
                        <#list categoryList as alone>
                            <option value="${alone}"
                                    <#if resourceTableDigest?exists && resourceTableDigest.category?default("") == alone>selected</#if>>${alone}</option>
                        </#list>
                    </#if>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">资源表唯一标识</label>
            <div class="controls">
                <input type="text" name="tableName" id="tableName" value="${(resourceTableDigest.tableName)!}"
                       placeholder="由(字母 数字 _)组成,建议用字母"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<strong>相同分类下标识不能重复,字符串可以由(字母 数字 _)组成,</strong>
                &nbsp;<strong style="color:red">建议用字母</strong>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">excel的名称</label>
            <div class="controls">
                <input type="text" name="tableExcelName" id="tableExcelName"
                       value="${(resourceTableDigest.tableExcelName)!}" placeholder="相同分类下excel的名称不能重复"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<strong>相同分类下excel的名称不能重复</strong>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">资源下载类型</label>
            <div class="controls">
                <select id="resourceType" name="resourceType">
                    <#if resourceTypeList?exists>
                        <#list resourceTypeList as mid>
                            <option value="${mid}"
                                    <#if resourceTableDigest?? && resourceTableDigest.resourceType?default("") == mid>selected</#if>>${mid?default("")}</option>
                        </#list>
                    </#if>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">资源下载Url</label>
            <div class="controls">
                <input type="text" name="url" id="url" value="${(resourceTableDigest.url)!}"/>
                <span class="controls-desc"></span>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">对应实体</label>
            <div class="controls">
                <select id="classTypeName" name="classTypeName">
                    <option value="">无配置</option>
                    <#if configClassList?exists>
                        <#list configClassList as configClass>
                            <option value="${configClass.classTypeName}"
                                    <#if resourceTableDigest?? && resourceTableDigest.classTypeName?default("") == configClass.classTypeName>selected</#if>>${configClass.classSimpleName?default("")}</option>
                        </#list>
                    </#if>
                </select>
                &nbsp;&nbsp;&nbsp;&nbsp;<strong>DATABASE类型数据对应代码层实体</strong>
                <span class="controls-desc"></span>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">表的数据维护人</label>
            <div class="controls">
                <input type="text" name="tableModifier" id="tableModifier"
                       value="${(resourceTableDigest.tableModifier)!}" placeholder="请输入维护表的产品人员名称"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<strong>分为主维护人员，副维护人员，名称用逗号隔开</strong>
                <span class="controls-desc"></span>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">表的上传人</label>
            <div class="controls">
                <input type="text" name="tableUploader" id="tableUploader"
                       value="${(resourceTableDigest.tableUploader)!}" placeholder="请输入上传表的开发人员名称"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<strong>分为主上传人员，副上传人员，名称用逗号隔开</strong>
                <span class="controls-desc"></span>
            </div>
        </div>

        <div class="control-group">
            <div class="controls">
                <button type="submit" id="btn_save" name="btn_save" class="btn btn-primary">插入或更新摘要</button>
            </div>
        </div>
    </form>


</div>


</@layout_default.page>