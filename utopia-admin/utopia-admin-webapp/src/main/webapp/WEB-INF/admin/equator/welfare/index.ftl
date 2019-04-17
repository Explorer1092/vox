<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="用户福利券" page_num=24>
<style>
</style>
<span class="span9" style="font-size: 14px">

    <#include '../userinfotitle.ftl' />

    <div id="legend" class="">
        <form class="form-horizontal" action="/equator/userwelfare/index.vpage" method="get" id="userwelfareQueryForm">
            <ul class="inline">
                学生ID：<input type="text" id="studentId" name="studentId" value="${(studentId)!}"/>
                <input type="button" id="submit_query" value="查询" class="btn btn-primary"/>
            </ul>
        </form>
    </div>

    <#if errMsg??>
        <div class="control-group">
            <label class="col-sm-2 control-label"></label>
            <div class="controls">
                ${errMsg}
            </div>
        </div>
    </#if>

    <div class="table_soll">
        <table class="table table-bordered">
            <tr>
                <th>福利券id</th>
                <th>用户id</th>
                <th>有效开始时间</th>
                <th>有效截止时间</th>
                <th>是否使用</th>
                <th>使用日期</th>
                <th>福利券类型</th>
                <th>来源类型</th>
                <th>相关产品类型</th>
                <th>操作</th>
            </tr>
            <tbody id="tbody">
                <#if userWelfareList ?? >
                    <#list userWelfareList as userWelfare>
                    <tr>
                        <td>${userWelfare["id"]?default("")}</td>
                        <td>${userWelfare["userId"]?default("")}</td>
                        <td>${userWelfare["validStartDate"]?default("")}</td>
                        <td>${userWelfare["validEndDate"]?default("")}</td>
                        <td>${userWelfare["used"]?c}</td>
                        <td>${userWelfare["usedDate"]?default("")}</td>
                        <td>${userWelfare["type"]?default("")}</td>
                        <td>${userWelfare["refererType"]?default("")}</td>
                        <td>${(userWelfare.extInfo.productType)!''}</td>
                        <td><a href="javascript:void(0)" onclick="removeWelfare('${userWelfare.id}','${userWelfare.userId}')" class="btn btn-danger btn-small">删除</a></td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</span>

<div id="removeWelfareDialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>删除福利</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd><input name="userId" id="userId" readonly="true"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>福利ID</dt>
                    <dd><input name="welfareId" id="welfareId" readonly="true"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>确定删除福利吗？</li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="removeWelfareBtn" class="btn btn-primary">删 除</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<script>
    $(function () {
        $("#submit_query").on("click", function () {
            $("#userwelfareQueryForm").submit();
        });

        $("#removeWelfareBtn").on("click", function () {
            const studentId = $("#userId").val();
            const welfareId = $("#welfareId").val();

            $.post('/equator/userwelfare/remove.vpage', {
                studentId: studentId,
                welfareId: welfareId
            }, function (data) {
                if (data.success) {
                    alert("删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });

    function removeWelfare(id, userId) {
        $("#welfareId").val(id);
        $("#userId").val(userId);
        $("#removeWelfareDialog").modal("show");
    }
</script>
</@layout_default.page>