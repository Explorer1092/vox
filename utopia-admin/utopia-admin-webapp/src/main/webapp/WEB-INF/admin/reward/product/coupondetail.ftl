<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {font: "arial";}
    .index {color: #0000ff;}
    .index, .item {font-size: 18px; font: "arial";}
    .warn {color: red;}
</style>
<div class="span9">
    <fieldset>
        <legend>虚拟奖品兑换码导入</legend>
        <form method="post" action="importcouponno.vpage">
            <ul class="inline">
                <li>
                    <label>产品：${product.productName}
                        <input type="hidden" name="productId" value="${product.id}" />
                    </label>
                </li>
                <li>
                    <label>兑换券：<textarea  id="couponNo" name="couponNo" placeholder="请输入兑换券号码，从excel直接粘贴，一行一条" /></textarea></label>
                </li>
                <li>
                    <button class="btn btn-primary" type="submit" id="submit_select">导入</button>
                    <a id="exchangeExport" href="downloadexchangedata.vpage?productId=${product.id}" role="button" class="btn btn-inverse">导出已兑换数据（限学生）</a>
                    <a id="usedDataImport"  role="button" class="btn btn-warning">导入已使用数据</a>
                </li>
            </ul>
        </form>
    </fieldset>
    <fieldset>
        <legend>兑换码生成</legend>
        <form method="post" action="generatecouponno.vpage">
            <ul class="inline">
                <li>
                    <label>产品：${product.productName}
                        <input type="hidden" name="productId" value="${product.id}" />
                    </label>
                </li>
                <li>
                    <label>兑换码前缀：<input type="text" name="prefix" placeholder="前缀为两位，大写英文字母" /></label>
                </li>
                <li>
                    <label>数量：<input type="text" name="num" /></label>
                </li>
                <li>
                    <button class="btn btn-primary" type="submit" id="submit_generate">生成</button>
                </li>
            </ul>
        </form>
    </fieldset>
    <div>
        <label>失败条数：</label>
        <table class="table table-bordered">
            <tr>
                <td>${wrongSize!0}</td>
            </tr>
        </table>
        <label>成功条数：</label>
        <table class="table table-bordered">
            <tr>
                <td>${successSize!0}</td>
            </tr>
        </table>
    </div>
    <div>
        <label>当前兑换码数量： ${couponSize!0}</label>
    </div>
</div>
<div id="used_data_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>导入已使用数据</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd><textarea id="userIds" name="userIds" placeholder="请输入要导入的用户ID，从excel直接粘贴，一行一条"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="used_data_button" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script type="text/javascript">

    $(function() {

        $('#usedDataImport').on('click', function () {
            $('#used_data_modal').modal();
        });

        $("#used_data_button").on("click", function () {
            $.ajax({
                type: "post",
                url: "importuseddata.vpage",
                data: {
                    userIds: $("#userIds").val(),
                    productId: ${product.id}
                },
                success: function (data) {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>