<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="资源表管理-分类管理" page_num=24>
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


    <h3>分类管理</h3>
    <#include "inner_header.ftl"/>

    <button type="submit" id="btn_first_category_save" name="btn_first_category_save" class="btn btn-primary">新增一级分类
    </button>


    <br/>
    <hr style="border-bottom-color:gold;"/>
    <ul class="inline">
        <table class="table table-bordered">
            <tr>
                <th style="width:200px">id</th>
                <th style="width:300px">一级分类</th>
                <th>二级分类</th>
                <th style="width:140px">更新时间</th>
                <th style="width:200px">操作</th>
            </tr>
            <tbody id="tbody">
                <#if resourceTableCategoryList ?? >
                    <#list resourceTableCategoryList as aCategory >
                    <tr>
                        <td>${aCategory.id?default("")}</td>
                        <td>${aCategory.firstCategory?default("")}</td>
                        <td>
                            <#if aCategory.secondCategory?? && aCategory.secondCategory?has_content>
                                <#list aCategory.secondCategory as aSecondCategory>
                                    ${aSecondCategory?default("")} &nbsp; &nbsp;
                                    <#if aSecondCategory_index %2==1>
                                        <br/>
                                    </#if>
                                </#list>
                            </#if>
                        </td>
                        <td>${(aCategory.updateTime)?string('yyyy-MM-dd HH:mm:ss')!''}</td>
                        <td>
                            <button class="btn btn-primary addsecondcategory"
                                    data-id="${(aCategory.id)?default("")}"
                                    data-firstCategory="${(aCategory.firstCategory)?default("")}"
                            >新增二级分类
                            </button>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </ul>
</div>


<div id="addsecondcategory_dialog" class="modal hide fade" style="width:700px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新增二级分类</h3>
    </div>
    <div class="modal-body" style="max-height:600px">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>一级分类</dt>
                    <dd>
                        <input id="editFirstCategory" type="text" value="" placeholder="字符串由英文组成"/>
                        <strong>字符串由英文组成</strong>
                    </dd>

                </li>
            </ul>
            <ul class="inline secondCategoryUL">
                <li>
                    <dt>二级分类</dt>
                    <dd>
                        <input id="editSecondCategory" type="text" value="" placeholder="字符串由英文组成"/>
                        <strong>字符串由英文组成</strong>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="addsecondcategory_dialog_btn" data-status="" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<script>
    $(function () {
        $("#btn_first_category_save").on("click", function () {
            $("#editFirstCategory").val("").attr("readonly", false);
            $("#editSecondCategory").val("");
            $(".secondCategoryUL").hide();
            $("#addsecondcategory_dialog_btn").attr("data-status", "addFirstCategory");
            $("#addsecondcategory_dialog").modal("show");
        });


        $(".addsecondcategory").on("click", function () {
            $("#editFirstCategory").val($(this).attr("data-firstCategory")).attr("readonly", true);
            $("#editSecondCategory").val("");
            $(".secondCategoryUL").show();
            $("#addsecondcategory_dialog_btn").attr("data-status", "addSecondCategory");
            $("#addsecondcategory_dialog").modal("show");
        });

        $("#addsecondcategory_dialog_btn").on("click", function () {
            var firstCategory = $("#editFirstCategory").val();
            var secondCategory = $("#editSecondCategory").val();

            var dataStatus = $("#addsecondcategory_dialog_btn").attr("data-status");
            if (dataStatus === "addFirstCategory") {
                if (isBlank(firstCategory)) {
                    alert("一级分类 不能为空，必填");
                    return;
                }
                $.post('/equator/config/resourcetablemanage/addfirstcategory.vpage', {
                    firstCategory: firstCategory
                }, function (data) {
                    if (data.success) {
                        alert("配置成功，服务数分钟后生效");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            } else if (dataStatus === "addSecondCategory") {
                if (isBlank(firstCategory) || isBlank(secondCategory)) {
                    alert("一级分类 二级分类不能为空，必填");
                    return;
                }
                $.post('/equator/config/resourcetablemanage/addsecondcategory.vpage', {
                    firstCategory: firstCategory,
                    secondCategory: secondCategory
                }, function (data) {
                    if (data.success) {
                        alert("配置成功，服务数分钟后生效");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            } else {
                alert("data-status 有误")
            }

        });
    });

    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }
</script>

</@layout_default.page>