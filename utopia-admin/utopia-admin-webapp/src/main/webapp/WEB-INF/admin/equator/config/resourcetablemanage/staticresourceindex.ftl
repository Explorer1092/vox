<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="资源表管理-图片等静态资源管理" page_num=24>

<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9">
    <h3>静态资源上传和打包</h3>
    <#include "inner_header.ftl"/>
    <#include "inner_static_header.ftl"/>
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>错误信息：${error!}</strong>
            </div>
        </#if>

        <#if successInfo??>
            <div class="alert alert-success" sta>
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>成功信息：${successInfo!}</strong>
            </div>
        </#if>
    </div>

    <br/>

    <h4>上传静态资源</h4>
    <form class="form-horizontal" action="/equator/config/resourcetablemanage/uploadmultistaticresource.vpage"
          method="post" id="fileResourceForm" enctype="multipart/form-data">
        <div class="control-group">
            <label class="col-sm-2 control-label">一级和二级分类</label>
            <div class="controls">
                <select id="categoryCombination" name="categoryCombination" style="width: 400px">
                    <if categoryList??>
                         <#list categoryList as oneCategory>
                             <option value="${oneCategory["value"]}"
                                     <#if currentCategoryCombination?? && currentCategoryCombination==oneCategory["value"]>selected</#if>>
                                 ${oneCategory["firstCategory"]?default("")}${oneCategory["secondCategory"]?default("")}
                                 SR静态资源
                             </option>
                         </#list>
                    </if>
                </select>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">文件类型</label>
            <div class="controls">
                <select id="fileType" name="fileType" style="width: 400px">
                    <option value="IMAGE">IMAGE</option>
                    <option value="FILE">FILE</option>
                    <option value="AUDIO">AUDIO</option>
                </select>
            </div>
        </div>

        <div class="control-group">
            <label class="col-sm-2 control-label">批量选择文件</label>
            <div class="controls">
                <input type="file" name="files" id="files" multiple="multiple" title="批量选择"/>
            </div>
        </div>


        <div class="control-group">
            <div class="controls">
                <button name="upload_button" type="submit" class="btn btn-primary">上传并保存
                </button>
            </div>
        </div>
    </form>

    <br/><br/>
    <hr style="border-bottom-color: #0e90d2"/>
    <h4>打包资源</h4>

    <form class="form-horizontal" action="/equator/config/resourcetablemanage/packstaticfileresource.vpage"
          method="post" id="fileResourcePackForm" enctype="multipart/form-data">

        <div class="control-group">
            <label class="col-sm-2 control-label">一级和二级分类</label>

            <div class="controls">
                <select id="categoryCombinationForPack" name="categoryCombinationForPack" style="width: 400px">
                    <if categoryList??>
                         <#list categoryList as oneCategory>
                             <option value="${oneCategory["value"]}">
                                 ${oneCategory["firstCategory"]?default("")}${oneCategory["secondCategory"]?default("")}
                                 SR静态资源
                             </option>
                         </#list>
                    </if>
                </select>
                &nbsp; &nbsp;
                <button name="pack_static_resource_buttion" type="submit" class="btn btn-primary">打包
                </button>
                <br/>
                <br/>
                <strong>打包后资源唯一标识的格式是：（"一级分类" + "二级分类" + "SR"）</strong><br>
            </div>
        </div>

    </form>


</div>


</@layout_default.page>