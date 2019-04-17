<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="用户道具" page_num=24>
<style>
</style>

<span class="span9" style="font-size: 14px">

    <#include '../userinfotitle.ftl' />

    <form class="form-horizontal" action="/equator/newwonderland/material/list.vpage" method="post">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <ul class="inline selectbtn">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus" placeholder="输入学生ID" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            道具类型：<select id="materialType" name="materialType">
                <option value="">请选择</option>
                <#list materialTypes as aMaterialType>
                <option value="${aMaterialType}" <#if aMaterialType==materialType>selected</#if>>${aMaterialType.desc}</option>
                </#list>
            </select>
            <input type="submit" value="查询" class="btn btn-primary"/>
            <#if studentName??>${studentName}</#if>
        </ul>
    </form>

    <div class="table_soll">
        <table class="table table-bordered table-condensed table-striped">
            <tr>
                <th>类型</th>
                <th>名称</th>
                <th>道具id</th>
                <th>数量</th>
                <th>学科</th>
                <th>活动标识</th>
                <th style="text-align: center">操作</th>
            </tr>
            <#if studentId??>
            <tbody>
                <#if materialDB ?? >
                <#list materialDB?keys as materialType>
                <#list materialDB[materialType] as material>
                <tr>
                    <#if material_index == 0><td rowspan="${materialDB[materialType]?size}">${materialType}</td></#if>
                    <td>${material.name}</td>
                    <td>${material.id?default("")}</td>
                    <td id="${'quality_' + materialType_index + '_' + material_index}">${material.quality}</td>
                    <td>${material.subject}</td>
                    <td>${material.activityType}</td>
                    <td style="text-align: center;">
                        <input type="button" class="btn btn-small btn-info" id="${'modifyBtn_' + materialType_index + '_' + material_index}" value="修改道具数量" data-value="${material.id}" data-name="${material.name}" onclick="grantMaterial(this)">
                    </td>
                </tr>
                </#list>
                </#list>
                </#if>
            </tbody>
        </#if>
        </table>
    </div>

</span>

<div id="grant_material_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>调整<#if studentName ? has_content>${studentName}</#if><span id="materialName"></span></h3>
    </div>
    <div class="modal-body">
        <label for="materialQuality">指定修改个数：<input id="materialQuality" type="number" /><span style="color: red;font-size: smaller;">*正数时增加，负数时减少</span></label>
        <input type="hidden" value="" id="materialId">
    </div>
    <div class="modal-footer">
        <button id="grant_material_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button class="btn btn-primary" data-dismiss="modal">取消</button>
    </div>
</div>

<script>

    var modifierId = null;

    function grantMaterial(obj) {
        $('#materialId').val($(obj).attr('data-value'));
        $('#materialName').text($(obj).attr('data-name'));
        $('#materialQuality').val('');

        var btnModifyId = $(obj).attr('id');
        modifierId = '#' + btnModifyId.replace('modifyBtn_', 'quality_');
        $('#grant_material_dialog').modal("show");
    }

    $(function () {
        $('#grant_material_dialog_confirm_btn').click(function () {
            var materialQuality = $('#materialQuality').val();
            var studentId = $('#studentId').val();
            var materialId = $('#materialId').val();
            if (materialQuality && materialQuality !== 0) {
                $.post('/equator/newwonderland/material/grant.vpage', {
                    'studentId': studentId,
                    'materialId': materialId,
                    'materialQuality': materialQuality
                }, function (data) {
                    if (data.success) {
                        $('#grant_material_dialog').modal("hide");
                        $(modifierId).text(data.ctx.ext.total);
                    } else {
                        alert(data.info);
                    }
                });
            } else {
                alert("请输入正确的数量。");
            }
        });
    });
</script>
</@layout_default.page>