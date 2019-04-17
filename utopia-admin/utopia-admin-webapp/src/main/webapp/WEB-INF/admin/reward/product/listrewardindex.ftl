<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>首页推荐管理</legend>
        <ul class="inline">
            <li>
                <button class="btn btn-success" id="submit_add">添加</button>
            </li>
        </ul>
    </fieldset>
    <div>
        <fieldset>
            <div>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th></th>
                        <th>ID</th>
                        <th>产品ID</th>
                        <th>类型</th>
                        <th>图片地址</th>
                        <th>排序</th>
                        <th>操作</th>
                    </tr>
                    <#if indexList?has_content>
                        <#list indexList as index>
                            <tr>
                                <th><input name="indexId" type="checkbox" value="${index.id!}"></th>
                                <td>${index.id!}</td>
                                <td>${index.productId!}</td>
                                <td>${index.indexType!}</td>
                                <td>${index.location!}</td>
                                <td>${index.displayOrder!}</td>
                                <td><a name="deleteIndex" role="button" data-content-id="${index.id!}" class="btn btn-primary">删除</a></td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </fieldset>
    </div>

    <div id="add_index_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>添加首页推荐列表</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>产品ID</dt>
                        <dd><input id="productId" type="text"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>排序</dt>
                        <dd><input id="displayOrder" type="text"/></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="add_index_button" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>

<script>

    $(function () {

        $('#submit_add').on('click', function () {
            $('#add_index_dialog').modal();
        });

        $("#add_index_button").on("click", function () {
            $.ajax({
                type: "post",
                url: "addindexproduct.vpage",
                data: {
                    productId: $("#productId").val(),
                    displayOrder: $("#displayOrder").val()
                },
                success: function (data) {
                    if (data.success) {
                        location.href = "listrewardindex.vpage";
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("a[name='deleteIndex']").on("click", function () {
            var indexId = $(this).attr("data-content-id");
            $.ajax({
                type: "post",
                url: "deleteindexproduct.vpage",
                data: {
                    indexId: indexId
                },
                success: function (data) {
                    if (data.success) {
                        location.href = "listrewardindex.vpage";
                    } else {
                        alert(data.info);
                    }
                }
            });
        });


    });
</script>
</@layout_default.page>