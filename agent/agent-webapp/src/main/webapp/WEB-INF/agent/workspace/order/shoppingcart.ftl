<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='购物车' page_num=14>
<style>
    .box_span div{height:20px;line-height:20px;}
    input::-webkit-outer-spin-button,
    input::-webkit-inner-spin-button{-webkit-appearance: none !important;margin: 0;}
    input[type="number"]{-moz-appearance:textfield;}
    #province select{width:100px;margin-left:20px}
</style>
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
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper span12">
                <fieldset>
                    <div class="control-group">
                        <ul class="nav nav-tabs" style="margin: 0;">
                            <li class="tab-list1"><a href="index.vpage">商品列表</a></li>
                            <li class="tab-list1"><a href="javascript:void(0)">购物车(<span class="carNum"></span>)</a></li>
                        </ul>
                    </div>
                </fieldset>
                <table class="table table-striped table-bordered bootstrap-datatable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">商品名称</th>
                        <th class="sorting" style="width: 100px;">商品单价</th>
                        <th class="sorting" style="width: 100px;">购买数量</th>
                        <th class="sorting" style="width: 100px;">金额</th>
                        <th class="sorting" style="width: 80px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if myOrder?has_content && myOrder.orderProductList??>
                            <#assign totalAmount = 0 />
                            <#assign cardTotalAmount = 0 />
                            <#list myOrder.orderProductList as orderProduct>
                                <#if productList?has_content>
                                    <#list productList as product>
                                        <#if product.id == orderProduct.productId>
                                            <tr class="odd">
                                                <td class="center  sorting_1">
                                                     <#if product.productType != 1><i class="icon-star"></i></#if>${product.productName!}
                                            </td>
                                            <td class="center  sorting_1 " >
                                                    <input class="proPrice" type="hidden" value="${product.price?string(",##0.##")}">
                                                    ${product.price?string(",##0.##")}
                                            </td>
                                            <td class="center  sorting_1">
                                                <#if product.productType == 1>
                                                    <input data-id="${orderProduct.id!}"
                                                           class="input focused ipt_num order_product_quantity" type="number" oninput="if(value>19999)value=19999"
                                                           value="${orderProduct.productQuantity!0}"
                                                           data-type="${product.productType!}"/>个
                                                <#else>
                                                    <input data-id="${orderProduct.id!}"
                                                           class="input focused ipt_num order_product_quantity" type="number" oninput="if(value>19999)value=19999"
                                                           value="${orderProduct.productQuantity/100}"
                                                           data-type="${product.productType!}"/>组
                                                </#if>
                                            </td>
                                            <td class="center  sorting_1 order_amount">
                                                <#assign amount = product.price * orderProduct.productQuantity />
                                                <#assign totalAmount = totalAmount + amount />
                                                <#assign cardAmount = product.price * orderProduct.productQuantity />
                                                <#assign cardTotalAmount = cardTotalAmount + cardAmount />
                                                <span class="" id="order_product_amount_${orderProduct.id!}">${amount?string("###0.00")}</span>
                                            </td>
                                            <td class="center ">
                                                <a data-id="${orderProduct.id!}" class="btn btn-warning delete_product"
                                                   href="javascript:void(0);">
                                                    <i class="icon-trash icon-white"></i>
                                                    删除
                                                </a>
                                            </td>
                                        </tr>
                                    </#if>
                                </#list>
                            </#if>
                        </#list>
                        <tr class="odd">
                            <td class="center allcash  sorting_1" colspan="3">
                                订单合计金额
                            </td>
                            <td class="center  sorting_1">
                                <span class="allmoney">${totalAmount?string("###0.00")}</span>
                            </td>
                            <td class="center  sorting_1">
                            </td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
            <div>
                <p>备注：</p>
                <textarea id="order_notes" name="order_notes" style="width: 95%;"
                          rows="5" placeholder="如使用市场支持费用，需在此处备注上具体的城市名称，否则无法审批通过"><#if myOrder?has_content>${myOrder.orderNotes!}</#if></textarea>
                <#if oldAgentOrder?? && oldAgentOrder?has_content>
                    <div class="span5 box_span" style="border: 1px dashed #000;padding:15px;height:200px;margin-bottom:10px;margin-left: 0;">
                        <div><span>上次订单的收货信息:</span></div>
                        <div>收货人：<span id="oldConsignee">${oldAgentOrder.consignee!''}</span></div>
                        <div>联系电话：<span id="oldMobile">${oldAgentOrder.mobile!''}</span></div>
                        <div>省市区：
                            <span id="provinceName">${oldAgentOrder.province!''}</span>
                            <span id="cityName">${oldAgentOrder.city!''}</span>
                            <span id="countyName">${oldAgentOrder.county!''}</span>
                        </div>
                        <div>收货地址：<span id="oldAddress">${oldAgentOrder.address!''}</span></div>
                        <div><input type="button" id="userOld" value="直接使用" class="btn btn-primary"/></div>
                    </div>
                </#if>
                <div class="span5" style="border: 1px dashed #000;padding:15px;height:200px">
                    <div><span>本次订单收获信息:</span></div>
                    <div>
                        <span>收货人姓名：</span>
                        <input id="consignee" name="consignee" class="input focused" type="text" value="<#if myOrder?has_content>${myOrder.consignee!}</#if>"/>
                    </div>
                    <div>
                        <span>收货人电话：</span>
                        <input id="mobile" name="mobile" class="input focused" type="text" value="<#if myOrder?has_content>${myOrder.mobile!}</#if>"/>
                    </div>
                    <div>
                      <span>省市区 &nbsp; ：</span>
                      <select id="topRegion" style="width: 80px;"></select>
                      <select id="middleRegion" style="width: 80px;"></select>
                      <select name="regionCode" style="width: 80px;" id="bottomRegion" onchange="modifierOrder()"></select>
                    </div>
                    <div>
                        <span>收货地址 &nbsp; ：</span>
                        <input id="address" name="address" style="width: 300px" class="input focused" type="text" value="<#if myOrder?has_content>${myOrder.address!}</#if>"/>
                    </div>
                </div>
                <div style="clear:both;overflow: hidden;">
                    <span style="float:left;">支付方式</span>
                    <#if paymentMode?has_content>
                        <#list paymentMode as mode>
                            <div style="float:left;margin-left:15px" class="ipt_radio">
                                <input type="radio" name="paymentMode" style="margin-left:4px;" class="payment-mode" value="${mode.payId!0}" <#if mode.selected!false>
                                      checked  </#if>/>
                            ${mode.payDes!''}
                            </div>
                        </#list>
                    </#if>
                </div>
                <div id="cost-city" style="clear:both;overflow:hidden;" hidden>
                    <div style="margin-left:85px;margin-top: 5px;margin-bottom: 5px">城市支持费用由所在城市市经理和专员共有，可使用近6个月内余额，请您合理使用</div>

                    <span style="float:left;">使用城市</span>
                    <div style="width: 80%;float: left;">
                        <#if userRegions??>
                            <#list userRegions as region>
                                <div style="float:left;margin-left:15px" class="ipt_radio">
                                    <input type="radio" name="costRegionCode" style="margin-left:4px;" value="${region.regionCode}" <#if region.selected!false>
                                           checked  </#if>/>
                                ${region.regionName!''}(余额：${region.balance!''}元)
                                </div>
                            </#list>
                        </#if>
                        <div class="cityList" style="clear: left;width: 500px;margin-left: 15px;">
                            <table class="table table-striped table-bordered bootstrap-datatable">
                                <thead>
                                    <tr>
                                        <th class="unSorting" style="width: 30px;">城市</th>
                                        <th class="unSorting" style="width: 30px;">月份</th>
                                        <th class="unSorting" style="width: 60px;">城市支持费用余额</th>
                                        <th class="unSorting" style="width: 30px;">选择</th>
                                    </tr>
                                </thead>
                                <tbody>

                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div id="voucher" class="show_hide3" hidden>
                    <span>上传凭证</span>
                    <input id="payment_voucher" type="file">
                    <div id="payment_voucher_photo">
                        <#if myOrder?has_content && myOrder.paymentVoucher?has_content>
                            <img src='${myOrder.paymentVoucher!''}?x-oss-process=image/resize,w_100,h_100/auto-orient,1'/>
                        </#if>
                    </div>
                    <input id="payment_voucher_value" name="paymentVoucher" type="hidden">
                </div>
            </div>
            <input type="button" class="btn btn-primary apply_submit" value="提交申请"/>
        </div>
    </div>
</div>
<script type="text/html" id="budgetList">
    <%for(var i = 0; i< res.length; i++){%>
    <%var data = res[i]%>
    <tr>
        <td><%=data.cityName%></td>
        <td><%=data.month%></td>
        <td><%=data.balance%></td>
        <td><input type="checkbox" class="budgetListInp" id="<%=data.id%>"></td>
    </tr>
    <%}%>
</script>
<script type="application/javascript">
    $("#userOld").on("click", function () {
        $("#consignee").val($("#oldConsignee").html());
        $("#address").val($("#oldAddress").html());
        $("#mobile").val($("#oldMobile").html());
        var provinceName = $("#provinceName").html();
        var cityName = $("#cityName").html();
        var countyName = $("#countyName").html();
        initArea(provinceName, cityName, countyName);
        modifierOrder();
    });
    
    $('.ipt_num').each(function(){
        $(this).keyup(function(){
            var $multi = 0;
            $(this).parent().next().html('');
            var vall = $(this).val();
            if (!vall){
                vall = 0;
            }
            var replaceIpt = $(this).parent().prev().find('input').val().split(",").join('');
            $multi = (parseFloat(vall) * parseFloat(replaceIpt)).toFixed(2);
            $(this).parent().next().html(parseFloat($multi));
            totl();
        });

    });
    function add(a, b) {
        var c, d, e;
        try {
            c = a.toString().split(".")[1].length;
        } catch (f) {
            c = 0;
        }
        try {
            d = b.toString().split(".")[1].length;
        } catch (f) {
            d = 0;
        }
        return e = Math.pow(10, Math.max(c, d)), (mul(a, e) + mul(b, e)) / e;
    }
    function mul(a, b) {
        var c = 0,
                d = a.toString(),
                e = b.toString();
        try {
            c += d.split(".")[1].length;
        } catch (f) {}
        try {
            c += e.split(".")[1].length;
        } catch (f) {}
        return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
    }
    function totl() {
        var sum = 0;
        $(".order_amount").each(function() {
            sum = add(sum,Number($(this).text()))
        });
        $(".allmoney").html(sum);
    }

    var regionTree;
    function paymentModeControl(value){
        if(value == 2){
            getCity($('input[name="costRegionCode"]:checked').val());
            $('#cost-city').show();
            $('#voucher').hide();
        }else if(value == 3){
            $('#cost-city').hide();
            $('#voucher').show();
        }else if(value == 1){
            $('#cost-city').hide();
            $('#voucher').hide();
        }
    }

    //获取城市近6个月费用余额
    function getCity(costRegionCode){
        $.get('/workspace/purchase/get_latest6_month_city_budget.vpage',{costRegionCode:costRegionCode},function (res) {
            if(res.success){
                $('.cityList tbody').html(template('budgetList',{res:res.dataList||[]}))
            }
        });
    }

    $(function () {
        var regionJson = ${regionTree!};
        regionTree = regionJson == undefined ? null : regionJson;
        paymentModeControl($('[name="paymentMode"]:checked').val());
        $(".payment-mode").on("change", function () {
            paymentModeControl($(this).val());
        });
        $("#topRegion").on("change", loadMiddleRegion);
        $("#middleRegion").on("change", loadBottomRegion);

        $(".apply_submit").live("click", submitOrder);
        $(".delete_product").live('click', function () {
            var orderProductId = $(this).attr("data-id");
            delOrderProduct(orderProductId);
        });
        $(".order_product_quantity").blur(modifierOrder);
        $(".order_product_quantity").keyup(function(event){
            if(event.keyCode ==13){
                modifierOrder();
            }
        });
        $("#order_notes").blur(modifierOrder);
        $("#order_notes").keyup(function (event) {
            if (event.keyCode == 13) {
                modifierOrder();
            }
        });
        $("#consignee").blur(modifierOrder);
        $("#consignee").keyup(function (event) {
            if (event.keyCode == 13) {
                modifierOrder();
            }
        });
        $("#mobile").blur(modifierOrder);
        $("#mobile").keyup(function (event) {
            if (event.keyCode == 13) {
                modifierOrder();
            }
        });
        $("#address").blur(modifierOrder);
        $("#address").keyup(function (event) {
            if (event.keyCode == 13) {
                modifierOrder();
            }
        });
        $("input[type='radio']").on("change", modifierOrder);

        loadTopRegion();

        //城市支持费用 选择不同的城市展示不同的数据
        $("input[name='costRegionCode']").on("change", function () {
            getCity($(this).val());
        });

        var cmbProvince = "<#if myOrder??>${myOrder.province!""}</#if>";
        var cmbCity = "<#if myOrder??>${myOrder.city!""}</#if>";
        var cmbArea = "<#if myOrder??>${myOrder.county!""}</#if>";
        initArea(cmbProvince,cmbCity,cmbArea)
    });
    function modifierOrder(){
        var inputValid = true;
        var productQuantity = [];
        $(".order_product_quantity").each(function () {
            if (!$.isNumeric($(this).val()) || $(this).val() <= 0 || $(this).val().indexOf(".") > 0) {
                inputValid = false;
            }
            var quantity = $(this).val();
            if ($(this).attr("data-type") != 1) {
                quantity = quantity * 100;
            }
            productQuantity.push({"id": $(this).attr("data-id"), "size": quantity});
        });

        var orderInfo = getOrderInfo();

        if (orderInfo.consignee && orderInfo.consignee.length > 50){
            return;
        }
        if (orderInfo.mobile && orderInfo.mobile.length > 15){
            return;
        }
        if (orderInfo.address && orderInfo.address.length > 100){
            return;
        }

        if (orderInfo.orderNotes && orderInfo.orderNotes.length > 1000){
            return;
        }
        $.ajax({
            type: 'post',
            url: "modifierorder.vpage",
            dataType: 'json',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify(orderInfo),
            success: function (res) {
                if (res.success) {
                } else {
                    if (res.code && 401 === res.code) {
                        window.location.reload();
                    } else {
                        layer.alert(res.info);
                    }
                }
            },
            error: function () {
                layer.alert("订单保存失败");
            }
        });
    }

    function delOrderProduct(orderProductId) {

        $.post('delorderproduct.vpage', {
            orderProductId: orderProductId
        }, function (data) {
            if (!data.success) {
                layer.alert(data.info);
            } else {
                window.location.reload();
            }
        });
    }

    function initArea(province, city, county){
        $('#topRegion option').filter(function(){return $(this).text()== province;}).attr("selected",true);
        $("#topRegion").trigger("change");
        $('#middleRegion option').filter(function(){return $(this).text()== city;}).attr("selected",true);
        $("#middleRegion").trigger("change");
        $('#bottomRegion option').filter(function(){return $(this).text()== county;}).attr("selected",true);
    }

    function loadTopRegion() {
        if (regionTree) {
            $("#topRegion").append("<option value='0'></option>");
            for (var code in regionTree) {
                var item = regionTree[code];
                $("#topRegion").append("<option value='" + code + "'>" + item.name + "</option>");
            }
            $("#topRegion").trigger("change");
        }
    }

    function loadMiddleRegion() {
        $("#middleRegion").empty();
        if (regionTree) {
            $("#middleRegion").append("<option value='0'></option>");
            var top = $("#topRegion").val();
            if (isValidRegionCode(top)) {
                if(top != 0){
                    var middles = regionTree[top].children;
                    for (var code in middles) {
                        var item = middles[code];
                        $("#middleRegion").append("<option value='" + code + "'>" + item.name + "</option>");
                    }
                }
            }
            $("#middleRegion").trigger("change");
        }
    }

    function loadBottomRegion() {
        $("#bottomRegion").empty();
        if (regionTree) {
            $("#bottomRegion").append("<option value='0'></option>");
            var top = $("#topRegion").val();
            var middle = $("#middleRegion").val();
            if (isValidRegionCode(top) && isValidRegionCode(middle)) {
                if(middle != 0){
                    var bottoms = regionTree[top].children[middle].children;
                    for (var code in bottoms) {
                        var item = bottoms[code];
                        $("#bottomRegion").append("<option value='" + code + "'>" + item.name + "</option>");
                    }
                }
            }
        }
    }

    function isValidRegionCode(code) {
        return code != null && code != undefined;
    }


    function getOrderInfo() {
        var productQuantity = [];
        $(".order_product_quantity").each(function () {
            var quantity = $(this).val();
            if ($(this).attr("data-type") != 1) {
                quantity = quantity * 100;
            }
            productQuantity.push({"id": $(this).attr("data-id"), "size": quantity});
        });
        var orderNotes = $('#order_notes').val().trim();
        var address = $('#address').val().trim();
        var consignee = $("#consignee").val().trim();
        var mobile = $("#mobile").val().trim();
        var province = $("#topRegion option:selected").text().trim();
        var city = $("#middleRegion option:selected").text().trim();
        var county = $("#bottomRegion option:selected").text().trim();
        var payment_mode = $('[name="paymentMode"]:checked').val();

        var paymentVoucher = $("#payment_voucher_value").val();
        var costRegionCode = $('[name="costRegionCode"]:checked').val();
        var productIdList = [];//提交订单之前接口返回（库存不足商品）
        var materialBudgetIdList = [];//勾选物料余额

        $(".budgetListInp").each(function (index,ele) {
            if($(ele).prop('checked')){
                materialBudgetIdList.push($(ele).attr('id'));
            }
        });

        var orderInfo = {
            "orderNotes": orderNotes,
            "province": province,
            "city": city,
            "county": county,
            "mobile": mobile,
            "consignee": consignee,
            "address": address,
            "paymentMode": payment_mode,
            "costRegionCode": costRegionCode,
            "paymentVoucher": paymentVoucher,
            "productQuantity": productQuantity,
            "productIdList": productIdList,
            "materialBudgetIdList": materialBudgetIdList
        };
        return orderInfo;
    }


    function submitOrder() {
        var detailAddress = $("#address").val().trim();

        if (detailAddress == "") {
            layer.alert("请输入收货地址！");
            return false;
        }
        var consignee = $("#consignee").val().trim();
        var mobile = $("#mobile").val().trim();
        if (consignee == "" || mobile == "") {
            layer.alert("请输入收货人及电话!");
            return false;
        }
        if(mobile.length != 11){
            layer.alert('请检查手机号是否输入正确');
            return false;
        }
        if($("#topRegion").val() == 0 || $("#middleRegion").val() == 0 || $("#bottomRegion").val() == 0){
            layer.alert('请检查省市区');
            return false;
        }

        // 如果Notes没有输入提示警告
        var orderNotes = $('#order_notes').val().trim();
        if (orderNotes == "") {
            if (!confirm("订单附加信息内容没有记入，确定要提交此订单吗?")) {
                return false;
            }
        }

        // 检查商品数量是否合格
        var inputValid = true;
        var productQuantity = [];
        $(".order_product_quantity").each(function () {
            if (!$.isNumeric($(this).val()) || $(this).val() <= 0 || $(this).val().indexOf(".") > 0) {
                inputValid = false;
            }
            var quantity = $(this).val();
            if ($(this).attr("data-type") != 1) {
                quantity = quantity * 100;
            }
            productQuantity.push({"id": $(this).attr("data-id"), "size": quantity});
        });

        if (!inputValid) {
            layer.alert("商品数量必须输入大于0的整数!");
            return false;
        }

        if (productQuantity.length === 0) {
            layer.alert("请加入商品!");
            return false;
        }
        var orderInfo = getOrderInfo();


        if (orderInfo.consignee && orderInfo.consignee.length > 50){
            layer.alert("收货人姓名长度不能超过50");
            return;
        }
        if (orderInfo.mobile && orderInfo.mobile.length > 15){
            layer.alert("收货人电话格式不正确");
            return;
        }
        if (orderInfo.address && orderInfo.address.length > 100){
            layer.alert("收货地址长度不能超过100");
            return;
        }

        if (orderInfo.orderNotes && orderInfo.orderNotes.length > 1000){
            layer.alert("备注长度不能超过1000");
            return;
        }
        if($('input[name="paymentMode"]:checked').val() === '2' && orderInfo.materialBudgetIdList.length === 0){
            layer.alert("请选择城市费用购买月份");
            return;
        }

        layer.confirm('确定要提交订单吗?', {
            btn: ['确认','取消']
        },function () {
            $.get('/workspace/purchase/before_submit_order.vpage',function (res) {
                if(res.success){
                    sureSubmit(orderInfo);
                }else{
                    if(res.inventoryFlag){
                        layer.alert(res.errorInfo);
                    }else{
                        layer.confirm(res.errorInfo, {
                            btn: ['提交订单，仅购买有货商品','返回修改订单']
                        },function () {
                            orderInfo.productIdList = res.productIdList || [];
                            sureSubmit(orderInfo);
                        });
                    }
                }
            })
        });
    }

    function sureSubmit(orderInfo){
        $.ajax({
            type: 'post',
            url: "submitorder.vpage",
            dataType: 'json',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify(orderInfo),
            success: function (res) {
                if (res.success) {
                    layer.alert("提交成功",function () {
                        window.location.href = "/apply/view/list.vpage";
                    });
                } else {
                    layer.alert(res.info);
                }
            },
            error: function () {
                layer.alert("订单保存失败");
            }
        });
    }

    $(document).on("change", "#payment_voucher", function () {
        if ($("#payment_voucher").val() != '') {
            var formData = new FormData();
            var file = $('#payment_voucher')[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            $.ajax({
                url: '/file/upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        $("#payment_voucher_photo").html("");
                        $("#payment_voucher_photo").append("<img src='" + data.fileUrl + "?x-oss-process=image/resize,w_100,h_100/auto-orient,1'/>");
                        $("#payment_voucher_value").val(data.fileUrl);
                        layer.alert("上传成功");
                    } else {
                        layer.alert("上传失败");
                    }
                }
            });
        }
    });
    var carNum = $('.odd').length -1;
    if(carNum >0){
        $('.carNum').html(carNum);
    }else{
        $('.carNum').html('0');
    }

</script>
</@layout_default.page>
