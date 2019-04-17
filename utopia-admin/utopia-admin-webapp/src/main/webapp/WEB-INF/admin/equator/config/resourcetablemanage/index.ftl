<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="资源表管理-表的摘要信息" page_num=24>
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


    <h3>资源表摘要信息</h3>
    <#include "inner_header.ftl"/>

    <form action="/equator/config/resourcetablemanage/index.vpage" method="Get" id="categoryForm">
        <ul class="inline">
            <li><label class="col-sm-2 control-label">产品类型</label>
            </li>
            <li>
                <div class="control-group">
                    <div class="controls">
                        <select id="category" name="category">
                            <#if categoryList?exists>
                                <#list categoryList as alone>
                                    <option value="${alone}"
                                            <#if category?default("") == alone>selected</#if>>${alone}</option>
                                </#list>
                            </#if>
                        </select>
                        <span class="controls-desc"></span>
                    </div>
                </div>
            </li>
            <li>
                <input id="btn_app_search" type="submit" class="btn btn-primary" value="查询"/>
            </li>
            <li><span style="color: #cd0a0a">(注意：数据库表增加字段，代码上线之后必须重新刷一下版本)</span></li>
        </ul>
    </form>


    <ul class="inline">
        <table class="table table-bordered">
            <tr>
                <th>唯一标识</th>
                <th>表excel名称</th>
                <th>最新版本</th>
                <th>下载类型</th>
                <th>表的数据维护人</th>
                <th>表的上传人</th>
                <th>更新时间</th>
                <th>操作</th>
            </tr>
            <tbody id="tbody">
                <#if resourceTableDigestList ?? >
                    <#list resourceTableDigestList as digest >
                    <tr>
                        <td>${digest.tableName?default("")}</td>
                        <td>${digest.tableExcelName?default("")}</td>
                        <td>${digest.version?default("")}</td>
                        <td>${digest.resourceType?default("")}</td>
                        <td>${digest.tableModifier?default("")}</td>
                        <td>${digest.tableUploader?default("")}</td>
                        <td>${(digest.updateTime)?string('yyyy-MM-dd HH:mm:ss')!''}</td>
                        <td>
                            <a href="/equator/config/resourcetablemanage/upsertdigestindex.vpage?digestId=${digest.id?default("")}">编辑 </a>
                            <a href="/equator/config/resourcetablemanage/downloadexcel.vpage?resourceType=${digest.resourceType?default("")}&digestId=${digest.id?default("")}">下载 </a>
                            <a href="/equator/config/resourcetablemanage/tabledatainfo.vpage?digestId=${digest.id?default("")}"
                               target="_blank">数据列表</a>
                            <a href="/equator/config/resourcetablemanage/difftabledatainfo.vpage?digestId=${digest.id?default("")}"
                               target="_blank">数据对比</a>
                            <a href="/equator/config/resourcetablemanage/increasetableversion.vpage?digestId=${digest.id?default("")}">刷新版本</a>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </ul>


    <legend>更新表数据</legend>
    <form class="form-horizontal" action="/equator/config/resourcetablemanage/uploadexceldata.vpage" method="post"
          id="uploadexceldataForm"
          enctype="multipart/form-data">
        <div class="control-group" name="ext-info">
            <label class="col-sm-2 control-label">表配置</label>
            <div class="controls">
                <select id="digestIdForTableExcelName" name="digestIdForTableExcelName">
                    <#if resourceTableDigestList?exists>
                        <#list resourceTableDigestList as digest>
                            <#if !(digest.staticResourceChildCategory?? && digest.staticResourceChildCategory?has_content)>
                            <option value="${(digest.id)!}" }>${(digest.tableExcelName)!}</option>
                            </#if>
                        </#list>
                    </#if>
                </select>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">文件</label>
            <div class="controls">
                <input type="file" name="excelFile" id="excelFile"/>
                <span class="controls-desc">解析为json数据，并保存到数据库或者cdn</span>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <button type="submit" id="btn_excel_file_save" name="btn_excel_file_save" class="btn btn-primary">保存
                </button>
            </div>
        </div>
    </form>


</div>

<script>
    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }


    function deleteResourceTableDigest(digestId) {
        if (isBlank(digestId)) {
            alert("参数digestId为空");
            return;
        }


        $.post('/equator/config/resourcetablemanage/fetchresourcetabledigest.vpage', {
            digestId: digestId
        }, function (data) {
            if (data != null && data.success === true && data.digest != null) {
                if (window.confirm("确定要删除" + data.digest.tableExcelName + "吗？")) {
                    $.post('/equator/config/resourcetablemanage/deleteresourcetabledigest.vpage', {
                        digestId: digestId
                    }, function (data) {
                        if (data.success) {
                            alert("删除成功,digestId=" + digestId);
                        } else {
                            alert("删除失败," + data.info);
                        }
                    });
                }
            } else {
                alert(data.info);
            }
        });

    }
</script>

</@layout_default.page>