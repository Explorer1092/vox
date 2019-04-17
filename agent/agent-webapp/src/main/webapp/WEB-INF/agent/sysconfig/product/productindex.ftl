<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='商品设置' page_num=14>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 商品设置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
            <div class="pull-right">
                <a class="btn btn-success" href="addproduct.vpage">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
                &nbsp;
                <a href="javascript:void(0);" class="btn btn-primary" id="sure_up">批量上架</a>
                &nbsp;
                <a href="javascript:void(0);" class="btn btn-primary" id="sure_down">批量下架</a>
                &nbsp;
                <a href="javascript:void(0);" class="btn btn-primary" id="sure_manage">批量管理库存</a>
                &nbsp;
            </div>
            </#if>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>

                    <tr>
                        <th class="unSorting" style="width: 30px;"><input type="checkbox" class="all-select">全选</th>
                        <th class="sorting" style="width: 100px;">商品名称</th>
                        <#--<th class="sorting" style="width: 100px;">商品类型</th>-->
                        <th class="sorting" style="width: 100px;">商品说明</th>
                        <th class="sorting" style="width: 70px;">图片</th>
                        <th class="sorting" style="width: 40px;">价格</th>
                        <th class="sorting" style="width: 70px;">库存</th>
                        <th class="sorting" style="width: 30px;">上架</th>
                        <th class="sorting" style="width: 30px;">小学可见</th>
                        <th class="sorting" style="width: 30px;">中学可见</th>
<#--                        <th class="sorting" style="width: 70px;">截止有效期</th>
                        <th class="sorting" style="width: 80px;" >最后编辑人</th>-->
                        <th class="sorting" style="width: 145px;">操作</th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if products??>
                            <#list products as product>
                                <tr class="odd">
                                    <td class="center sorting_1"><input type="checkbox" class="product-apply-item"
                                                                        value="${product.id!''}" data-type="${product.status!1}">
                                    </td>
                                    <td class="center  sorting_1">${product.productName!}</td>
                                    <td class="center  sorting_1">${product.productDesc!}</td>
                                    <td class="center  sorting_1">
                                        <#if product.productImg1! != ''>
                                            <a href="${product.productImg1!}" target="_blank"><img src="${product.productImg1!}" width="65px"/></a><br/>
                                        </#if>
                                        <#if product.productImg2! != ''>
                                            <a href="${product.productImg2!}" target="_blank"><img src="${product.productImg2!}" width="65px"/></a><br/>
                                        </#if>
                                        <#if product.productImg3! != ''>
                                            <a href="${product.productImg3!}" target="_blank"><img src="${product.productImg3!}" width="65px"/></a><br/>
                                        </#if>
                                        <#if product.productImg4! != ''>
                                            <a href="${product.productImg4!}" target="_blank"><img src="${product.productImg4!}" width="65px"/></a>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">${product.price?string(",##0.##")}</td>
                                    <td class="center  sorting_1">${product.inventoryQuantity!0}</td>
                                    <td class="center  sorting_1"><#if product.status?? && product.status==2>是<#else>否</#if></td>
                                    <td class="center  sorting_1"><#if product.primarySchoolVisible?? && product.primarySchoolVisible==true>是<#else>否</#if></td>
                                    <td class="center  sorting_1"><#if product.juniorSchoolVisible?? && product.juniorSchoolVisible==true>是<#else>否</#if></td>
                                    <td class="center ">
                                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                        <a class="btn btn-primary" href="addproduct.vpage?id=${product.id!}">
                                            <i class="icon-edit icon-white"></i>
                                            编辑
                                        </a>
                                        &nbsp;
                                        <a id="line_status_${product.id!}_${product.status!}" class="btn btn-primary" href="javascript:void(0);" >
                                            <i class="icon-edit icon-white"></i>
                                            <#if product.status?? && product.status==2>下架
                                            <#else>上架
                                            </#if>
                                        </a>
                                        &nbsp;
                                        <a id="delete_product_${product.id!}" class="btn btn-danger" href="javascript:void(0);">
                                            <i class="icon-trash icon-white"></i>
                                            删除
                                        </a>
                                        &nbsp;
                                        <a id="product_records_${product.id!}" class="btn btn-primary" href="product_records.vpage?id=${product.id!}">
                                            查库存
                                        </a>
                                        </#if>
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

<#--批量管理库存模板-->
<script type="text/html" id="manageContainer">
    <table class="table table-striped table-bordered bootstrap-datatable datatable">
        <thead>
            <tr>
                <th class="unSorting" style="width: 180px;">商品名称</th>
                <th class="unSorting" style="width: 60px;">当前库存</th>
                <th class="unSorting" style="width: 80px;">操作</th>
                <th class="unSorting" style="width: 80px;">增加/减少数量</th>
                <th class="unSorting" style="width: 250px;">变更原因</th>
            </tr>
        </thead>
        <%for(var i=0;i < res.dataList.length;i++){%>
        <%var data = res.dataList[i] %>
        <tr class="manage_list">
            <td><%=data.productName%> <input type="hidden" value="<%=data.id%>" class="id"></td>
            <td><%=data.inventoryQuantity%></td>
            <td>
                <select class="inventoryOpt" style="width: 100px;">
                    <option value="1" selected>增加库存</option>
                    <option value="-1">减少库存</option>
                </select>
            </td>
            <td class=""><input type="number" class="quantity" style="width: 50px;" onkeyup="value=value.replace(/[^\d]/g,'')"></td>
            <td class=""><input type="text" maxlength="20" class="quantityChangeDesc" style="width: 200px;"></td>
        </tr>
        <%}%>
    </table>
    <p style="color: #f00">提示：增加/减少数量只能填写正整数，变更原因最多20字</p>
</script>
<#--批量管理库存弹出框-->
<div id="show_manage_dialog" class="modal fade hide" style="width: 800px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">批量管理库存</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body manage_body" style="max-height: 500px; height: auto; overflow-y: scroll; width: auto">

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="save_manage_btn" type="button" class="btn btn-primary">确认</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $("#sure_up").bind("click", function () {
        updateStatus(2);
    });
    $("#sure_down").bind("click", function () {
        updateStatus(1)
    });
    function updateStatus(status) {
        var up = 0;
        var down = 0;
        var data = [];
        var postData = {
            pid: data,
            status: status
        };
        $(".product-apply-item").each(function (index, element) {
            if (status === 2 && $(element).attr("checked") && $(element).attr("data-type") === "1") {
                data.push($(element).val());
                down++;
            }
            if (status === 1 && $(element).attr("checked") && $(element).attr("data-type") === "2") {
                data.push($(element).val());
                up++;
            }
        });
        if (data.length === 0) {
            layer.alert("没有符合状态的产品");
            return;
        }
        if(status === 1){//下架
            sureApply(JSON.stringify(postData));
        }else{//上架
            $.get('before_batch_online_product.vpage',{productIdList:JSON.stringify(data)},function (res) {
                if(res.success){
                    var data = res.dataMap;
                    layer.confirm('将为您上架'+ data.haveInventoryNum +'件有货商品，'+ data.haveNoInventoryNum +'件无货商品”', {
                        btn: ['上架有货商品','全部上架','取消'] //按钮
                    }, function(){
                        postData.onlineFlag = 'onlineHaveInventory';
                        sureApply(JSON.stringify(postData));
                    }, function(){
                        postData.onlineFlag = 'onlineAll';
                        sureApply(JSON.stringify(postData));
                    },function () {

                    });
                }
            });
        }
    }

    function sureApply(data) {
        $.ajax({
            type: 'post',
            url: "update_status_all.vpage",
            dataType: 'json',
            contentType: 'application/json;charset=UTF-8',
            data: data,
            success: success,
            error: function () {
                layer.alert("批量更改失败");
            }
        })
    }
    function success(res) {
        if (res.success) {
            layer.alert(  "失败:" + (res.failed) +
                    "\r\n成功:" + (res.successItem) +
                    "\r\n合计:" + (res.summary),function () {
                window.location.reload();
            });
        } else {
            layer.alert(res.info);
        }
    }

    $(".product-apply-item").click(function () {
        var allSelect = true;
        $(".product-apply-item").each(function (index, element) {
            if (!$(element).attr("checked")) {
                allSelect = false;
            }
        });
        if (allSelect) {
            $(".all-select").attr("checked", allSelect);
            $(".all-select").parent("span").addClass("checked");
        } else {
            $(".all-select").attr("checked", false);
            $(".all-select").parent("span").removeClass("checked");
        }
    });

    $(".all-select").click(function () {
        var allSelect = $(this).attr("checked");
        $(".product-apply-item").each(function (index, element) {
            if (allSelect) {
                $(element).attr("checked", allSelect);
                $(element).parent("span").addClass("checked");
            } else {
                $(element).attr("checked", false);
                $(element).parent("span").removeClass("checked");
            }
        });
    });

    $(function(){
        $("a[id^='delete_product_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_product_".length);

            layer.confirm("确定要删除此条记录?",{
                btn: ['确认','取消'] //按钮
            },function () {
                $.post('delproduct.vpage',{
                    id:id
                },function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        $(window.location).attr('href', 'index.vpage');
                    }
                });
            });
        });

        $("a[id^='line_status_']").live('click',function(){
            var idAndStatus = $(this).attr("id").substring("line_status_".length).split("_");
            var id = idAndStatus[0];
            var status = idAndStatus[1];
            if(!status){
                status = 1;
            }
            if(status == 1){
                status = 2;
            }else {
                status = 1;
            }
            $.post('update_status.vpage',{
                id:id,
                status: status
            },function(data){
                if(!data.success){
                    layer.alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });
    });

    //批量管理库存
    $('#sure_manage').on('click',function () {
        var productIdList = [];
        $(".product-apply-item").each(function (index, element) {
            if ($(element).attr("checked")) {
                productIdList.push($(element).val());
            }
        });
        if(productIdList.length == 0){
            layer.alert('请选择商品');
            return false;
        }

        $.get('product_list_by_ids.vpage',{productIdList:JSON.stringify(productIdList)},function (res) {
            if(res.success){
                $(".manage_body").html(template("manageContainer",{res:res}));
                $('#show_manage_dialog').modal('show');
            }else {
             layer.alert('获取产品失败');
            }
        });

    });


    $('#save_manage_btn').on('click',function () {
        var productInventoryChangeInfoList = [];
        var bol = true;
        $(".manage_list input").each(function (index, element) {
            if ($(element).val().trim()==='') {
                layer.alert('请填写完整的信息！');
                bol = false;
                return false;
            }
        });
        if(bol){
            $(".manage_list").each(function (index, element) {
                productInventoryChangeInfoList.push({
                    id:$(element).find('.id').val(),
                    inventoryOpt:$(element).find('.inventoryOpt').val(),
                    quantity:$(element).find('.quantity').val(),
                    quantityChangeDesc:$(element).find('.quantityChangeDesc').val()
                });
            });
            $.post('batch_update_product_inventory.vpage',
                    {productInventoryChangeInfoList:JSON.stringify(productInventoryChangeInfoList)},
            function (res) {
                if(res.success){
                    layer.alert(res.successInfo,function () {
                        window.location.href = '/sysconfig/product/index.vpage';
                    });
                }else {
                    layer.alert(res.errorInfoList.toString());
                }
            })
        }
    });
</script>
</@layout_default.page>
