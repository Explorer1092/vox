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

    <label class="col-sm-2 control-label">一级和二级分类</label>
    <form class="form-horizontal" action="/equator/config/resourcetablemanage/staticresourcepicture.vpage"
          method="post" id="fileResourcePictureForm">
        <select id="categoryCombination" name="categoryCombination" style="width: 400px">
            <if categoryList??>
                         <#list categoryList as oneCategory>
                             <option value="${oneCategory["value"]}"
                                     <#if currentCategory?? && oneCategory["value"]== currentCategory["value"]>selected</#if>>
                                 ${oneCategory["firstCategory"]?default("")}${oneCategory["secondCategory"]?default("")}
                                 SR静态资源
                             </option>
                         </#list>
            </if>
        </select>

        <button name="select_button" type="submit" class="btn btn-primary">查询</button>
    </form>



    <#if resourceStaticFileInfoList ?? && resourceStaticFileInfoList?size gt 0 >
        <div class="table_soll">
            <table class="table table-bordered">
                <tr>
                    <th>资源名称</th>
                    <th>url</th>
                    <th>资源类型</th>
                    <th>更新时间</th>
                    <th>操作</th>
                </tr>
                    <#list resourceStaticFileInfoList as resourceStaticFileInfo>
                        <#assign todayUpdate = (resourceStaticFileInfo.updateTime)?string("yyyyMMdd") == .now?string("yyyyMMdd")>
                        <tr title="id标识: ${(resourceStaticFileInfo.id)?default("")}, 文件类型:${(resourceStaticFileInfo.fileType)?default("")}, 创建时间：${(resourceStaticFileInfo.createTime)?default("")}"
                            <#if todayUpdate>style="color: green;"</#if>
                        >
                            <td>${(resourceStaticFileInfo.resourceName)?default("")}</td>
                            <td><a target="_blank" href="${resourceStaticFileInfo.url?default("")}">点击查看</a></td>
                            <td>${(resourceStaticFileInfo.fileType)?default("")}</td>
                            <td>${(resourceStaticFileInfo.updateTime)?default("")}</td>
                            <td>
                                <button class="btn btn-default deleteOneStaticFile"
                                        data-id="${(resourceStaticFileInfo.id)?default("")}"
                                        data-resourceName="${(resourceStaticFileInfo.resourceName)?default("")}"
                                        data-url="${(resourceStaticFileInfo.url)?default("")}"
                                        data-type="${(resourceStaticFileInfo.fileType)?default("")}"
                                >删除
                                </button>
                                <#if todayUpdate><span style="display: inline-block;float: right;" title="今日维护过">※</span></#if>
                            </td>
                        </tr>
                    </#list >
            </table>
        </div>
    </#if>

</div>

<div id="resource_static_file_dialog" class="modal hide fade" style="width:700px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>删除资源</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>id</dt>
                    <dd>
                        <input id="editId" type="text" value="" style="width:400px;"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>资源名</dt>
                    <dd>
                        <input id="editResourceName" type="text" value="" style="width:400px;"/>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>url</dt>
                    <dd>
                        <input id="editUrl" type="text" value="" style="width:400px;"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline" id="imageDisplayZone">
                <li>
                    <dt>图片</dt>
                    <dd>
                        <img id="lookEditImg" src="">
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="resource_static_file_dialog_btn" data-status="insert" class="btn btn-primary">确定删除</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>


<script>
    $(function () {
        /*删除资源弹窗*/
        $(".deleteOneStaticFile").on("click", function () {
            var dataId = $(this).attr("data-id");
            var dataUrl = $(this).attr("data-url");
            var dataResourceName = $(this).attr("data-resourceName");

            $("#editId").val(dataId).attr("readonly", true);
            $("#editResourceName").val(dataResourceName);
            $("#editUrl").val(dataUrl);

            if ($(this).attr('data-type') === 'IMAGE') {
                $("#imageDisplayZone").show();
                $("#lookEditImg").attr("src", dataUrl);
            } else {
                $("#imageDisplayZone").hide();
            }

            $("#resource_static_file_dialog").modal("show");
        });

        $("#resource_static_file_dialog_btn").on("click", function () {
            var id = $("#editId").val();

            if (isBlank(id)) {
                alert("id不能为空");
                return;
            }

            $.post('/equator/config/resourcetablemanage/deleteonestaticfile.vpage', {
                id: id
            }, function (data) {
                if (data.success) {
                    alert("删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        function isBlank(str) {
            return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        }


    });
</script>
</@layout_default.page>