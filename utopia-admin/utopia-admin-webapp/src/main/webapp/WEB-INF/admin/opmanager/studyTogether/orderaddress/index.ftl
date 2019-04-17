<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">订单-发货单管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>用户 id&nbsp;
                        <input type="text" id="userId" name="userId" value="${userId!''}"/>
                    </label>
                </li>
                <li>
                    <label>订单 id&nbsp;
                        <input type="text" id="orderId" name="orderId" value="${orderId!''}" />
                    </label>
                </li>
                <li>
                    <label>支付开始时间&nbsp;
                        <input type="text" id="startDate" name="startDate" placeholder="开始时间" class="form-control js-postData" value="${startDate!''}" style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd'});" autocomplete="OFF"/>
                    </label>
                </li>
                <li>
                    <label>支付结束时间&nbsp;
                        <input type="text" id="endDate" name="endDate" placeholder="结束时间" class="form-control js-postData" value="${endDate!''}" style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd'});" autocomplete="OFF"/>
                    </label>
                </li>
                <li><button type="button" class="btn btn-primary" id="searchBtn">查询</button></li>
                <li><button type="button" class="btn btn-primary" id="exportBtn">导出发货单</button></li>
            </ul>
        </div>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>家长 id</th>
                        <th>订单 id</th>
                        <th>支付状态</th>
                        <th>支付金额</th>
                        <th>支付时间</th>
                        <th>收货人</th>
                        <th>联系电话</th>
                        <th>省</th>
                        <th>市</th>
                        <th>区</th>
                        <th>详细地址</th>
                        <th>商品 id</th>
                        <th>商品 名称</th>

                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as map>
                            <tr>
                                <td>${map.userId!''}</td>
                                <td>${map.orderId!''}</td>
                                <td>${map.payStatus!''}</td>
                                <td>${map.payAmount!''}</td>
                                <td>
                                    ${map.payDate!''}
                                </td>
                                <td>
                                    ${map.consigneeName!''}
                                </td>
                                <td id='phone_${map.orderId!''}'> <button type="button" class="btn btn-primary" data-orderId="${map.orderId!''}" onclick="qureyPhone('${map.orderId!''}')" id="getPhone:${map.orderId!''}">查看</button></td>
                                <td>
                                    ${map.province!''}
                                </td>
                                <td>${map.city!''}</td>
                                <td>
                                    ${map.county!''}
                                </td>
                                <td>
                                    ${map.detail!''}
                                </td>
                                <td>
                                    ${map.productId!''}
                                </td>
                                <td>
                                    ${map.productName!''}
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="9" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list"></ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate.trim() !== '' && endDate.trim() !== ''){
                if (startDate >= endDate){
                    alert("开始时间 必须 小于结束时间！");
                    return;
                }
            }
            $("#op-query").submit();
        });

        $("#exportBtn").on('click', function () {
            $("#pageNum").val(1);
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate.trim() !== '' && endDate.trim() !== ''){
                if (startDate >= endDate){
                    alert("开始时间 必须 小于结束时间！");
                    return;
                }
            }
            $("#op-query").submit();
        });

        $("#exportBtn").on('click', function () {
            var userId = $("#userId").val();
            var orderId = $("#orderId").val();
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            location.href = "export.vpage?userId=" + userId + "&orderId=" + orderId + "&startDate=" + startDate + "&endDate=" + endDate;
        });


        // $("#getPhone").on('click', function () {
        //     $.get("get_address_phone.vpage", {orderId: $(this).data("orderid")}, function (data) {
        //         if (data.success) {
        //             var phone = data.phone;
        //             $("#phone:".$(this).data("orderId")).html(phone);
        //         } else {
        //             alert(data.info);
        //         }
        //     });
        // })
    });
    function qureyPhone(oid) {
        $.get("get_address_phone.vpage", {orderId: oid}, function (data) {
            if (data.success) {
                var phone = data.phone;
                console.log($("#phone:"+oid));
                $("#phone_"+oid).html(phone);
            } else {
                alert(data.info);
            }
        });
    }
</script>
</@layout_default.page>