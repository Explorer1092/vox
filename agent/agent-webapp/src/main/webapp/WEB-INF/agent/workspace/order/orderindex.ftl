<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='商品购买' page_num=14>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 可购买商品列表</h2>
            <span style="position:absolute;top:90px;right:150px"><i class="icon-star"></i>可用余额:${userBalance?string("###0.00")}</span>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">
                <fieldset>
                    <div class="control-group span3" style="width:95%;padding-left:5%">
                        <ul class="nav nav-tabs">
                            <li class="tab-list1"><a href="javascript:void(0)">商品列表</a></li>
                            <li class="tab-list1"><a href="shopping_cart.vpage">购物车(${productkind!0})</a></li>
                        </ul>
                    </div>
                </fieldset>
                <table class="table table-striped table-bordered bootstrap-datatable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">商品名称</th>
                        <th class="sorting" style="width: 145px;">商品说明</th>
                        <th class="sorting" style="width: 270px;">商品图片</th>
                        <th class="sorting" style="width: 70px;">原价</th>
                        <th class="sorting" style="width: 70px;">库存</th>
                        <th class="sorting" style="width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if productList??>
                            <#list productList as product>
                            <tr class="odd">
                                <td class="center  sorting_1"><#if product.productType != 1><i
                                        class="icon-star"></i></#if>${product.productName!}</td>
                                <td class="center  sorting_1">${product.productDesc!}</td>
                                <td class="center  sorting_1">
                                    <#if product.productImg1! != ''>
                                        <a href="${product.productImg1!}" target="_blank"><img src="${product.productImg1!}" width="64px"/></a>
                                    </#if>
                                    <#if product.productImg2! != ''>
                                        <a href="${product.productImg2!}" target="_blank"><img src="${product.productImg2!}" width="64px"/></a>
                                    </#if>
                                    <#if product.productImg3! != ''>
                                        <a href="${product.productImg3!}" target="_blank"><img src="${product.productImg3!}" width="64px"/></a>
                                    </#if>
                                    <#if product.productImg4! != ''>
                                        <a href="${product.productImg4!}" target="_blank"><img src="${product.productImg4!}" width="64px"/></a>
                                    </#if>
                                </td>
                                <td class="center  sorting_1">${product.price?string(",##0.##")}</td>
                                <td class="center  sorting_1">${product.inventoryQuantity!}</td>
                                <td class="center ">
                                    <a data-id="${product.id!}" <#if product.inventoryQuantity?? && product.inventoryQuantity != 0><#else>style="background:#ddd"</#if> class="btn btn-success <#if product.inventoryQuantity?? && product.inventoryQuantity != 0>add-order-product<#else></#if>" href="#">
                                        <i class="icon-edit icon-white"></i>
                                        加入订单
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">
    $(function () {
        $(".add-order-product").live("click", function () {
            var productId = $(this).attr("data-id");
            addOrderProduct(productId);
        });
    });
    function addOrderProduct(productId) {
        $.post('addorderproduct.vpage', {
            productId: productId
        }, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                window.location.reload();
            }
        });
    }
</script>
</@layout_default.page>
