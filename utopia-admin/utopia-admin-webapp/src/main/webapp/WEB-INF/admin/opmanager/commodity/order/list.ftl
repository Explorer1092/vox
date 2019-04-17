<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <fieldset>
        <legend><font color="#00bfff">学习币商城</font>/订单管理</legend>
    </fieldset>
    <form id="order-query" class="form-horizontal" method="get"
          action="${requestContext.webAppContextPath}/opmanager/commodity/order/list.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>收件人电话&nbsp;
                    <input type="text" name="phone" id="phone" value="${phone!''}"/>
                </label>
            </li>
            <li>
                <label>学生ID&nbsp;
                    <input type="text" name="studentId" id="studentId" value="${studentId!''}"/>
                </label>
            </li>
            <li>
                <label>订单ID&nbsp;
                    <input type="text" name="orderId" id="orderId" value="${orderId!''}"/>
                </label>
            </li>
            <li>
                <label>订单状态&nbsp
                    <select name="orderStatus" id="orderStatus">
                        <option value="">全部</option>
                    <#if orderStatusMap?has_content>
                        <#list orderStatusMap ? keys as key>
                            <option value="${key}"
                                    <#if orderStatus?? && orderStatus == key>selected="selected"</#if>>${orderStatusMap[key]}</option>
                        </#list>
                    </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>商品分类&nbsp;
                    <select name="category" id="category">
                        <option value="">全部</option>
                    <#if categoryMap?has_content>
                        <#list categoryMap ? keys as key>
                            <option value="${key}"
                                    <#if category?? && category == key>selected="selected"</#if>>${categoryMap[key]}</option>
                        </#list>
                    </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>运单号&nbsp;
                    <input type="text" name="logisticsCode" id="logisticsCode" value="${logisticsCode!''}"/>
                </label>
            </li>
            <li>
                <label> 寄送方式&nbsp;
                    <select name="sendWay">
                        <option value="">全部</option>
                    <#if sendWayMap?has_content>
                        <#list sendWayMap ? keys as key>
                            <option value="${key}"
                                    <#if sendWay?? && sendWay == key>selected="selected"</#if>>${sendWayMap[key]}</option>
                        </#list>
                    </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>寄送状态&nbsp;
                    <select name="sendStatus">
                        <option value="">全部</option>
                    <#if sendStatusMap?has_content>
                        <#list sendStatusMap ? keys as key>
                            <option value="${key}"
                                    <#if sendStatus?? && sendStatus == key>selected="selected"</#if>>${sendStatusMap[key]}</option>
                        </#list>
                    </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>开始时间&nbsp;
                    <input id="startDate" value="${startDateStr!''}" name="startDate" class="input-medium" type="text" autocomplete="off" placeholder="2018-06-15 12:00">
                </label>
            </li>
            <li>
                <label> 结束时间&nbsp;
                    <input id="endDate" value="${endDateStr!''}" name="endDate" class="input-medium" type="text" autocomplete="off" placeholder="2018-06-15 12:00">
                    <span style="color: red">（查询未选择时间情况下，默认展示近30天订单）</span>
                </label>
            </li>
            <li>
                <label>
                    <button type="button" class="btn btn-primary" id="clearQuery">清除</button>
                </label>
            </li>
            <li>
                <label>
                    <button type="button" class="btn btn-primary" id="orderQuery">查询</button>
                </label>
            </li>
        </ul>
    </form>
    <div style="height: 40px">
        <button class="btn btn-primary" id="batchImport">批量导入运单</button>
        <button class="btn btn-primary" id="batchOutput">批量导出</button>
        <a href="batchList.vpage" class="btn btn-primary" id="batchProcess">批量操作进度</a>
    </div>

    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><input type="checkbox" id="batchSelect"></th>
                        <th>订单ID</th>
                        <th>学生ID</th>
                        <th>收件人电话</th>
                        <th>寄送状态</th>
                        <th>寄送方式</th>
                        <th>运单号</th>
                        <th>学习币数量</th>
                        <th>商品分类</th>
                        <th>订单状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if orderList ? has_content>
                            <#list orderList as order>
                                <tr>
                                    <td><input type="checkbox" name="orderCheck" data-id="${order.id!''}" data-category="${order.commodityCategory!''}"></td>
                                    <td>${order.id!''}</td>
                                    <td>${order.studentId!''}</td>
                                    <td class="phone">
                                        <button class="btn btn-primary" name="queryPhone"
                                                data-encode_phone="${order.phone!''}">查看
                                    </td>
                                    <td>${order.sendStatus!''}</td>
                                    <td>${order.sendWay!''}</td>
                                    <td>${order.logisticsCode!'--'}</td>
                                    <td>${order.coin!0}</td>
                                    <td>${order.commodityCategory!''}</td>
                                    <td>${order.orderStatus!''}</td>
                                    <td>
                                        <a href="javascript:void(0)" data-id="${order.id!''}"
                                           name="addLogistics"><#if order.logisticsCode?? && order.logisticsCode != ''>编辑<#else >添加</#if>运单</a>|
                                        <#if order.orderStatus?? && order.orderStatus == '已付币' && order.categoryLevel?? && order.categoryLevel == 1>
                                            <a href="javascript:void(0)" data-id="${order.id!''}" name="returnCoin">申请退币</a>|
                                        </#if>
                                        <a href="detail.vpage?orderId=${order.id!''}">详情</a>|
                                        <a href="logList.vpage?id=${order.id!''}">日志</a>|
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                    <li><a href="#" onclick="pagePost(${totalPage!})" title="Pre">尾页</a></li>
                    <li>&nbsp;跳转至&nbsp;<input type="text" id="jumpPage" style="width: 30px;" maxlength="3">&nbsp;页</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div id="add_logistics_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>添加运单信息</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height:360px;">
        <div style="width: 80%;margin:0 auto;">
            <form id="add-logistics-frm">
                <table>
                    <tr>
                        <td align="right">订单ID：</td>
                        <td><input type="text" id="aOrderId" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td align="right">收件人联系方式：</td>
                        <td><input type="text" id="aPhone" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td align="right">商品名称</td>
                        <td><input type="text" id="aCommodityName" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td align="right"><span style="color: red;font-size: 20px;">*</span>寄送状态</td>
                        <td>
                            <select id="aSendStatus">
                            <#if sendStatusMap?has_content>
                                <#list sendStatusMap ? keys as key>
                                    <option value="${key}">${sendStatusMap[key]}</option>
                                </#list>
                            </#if>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right"><span style="color: red;font-size: 20px;">*</span>寄送方式</td>
                        <td>
                            <select id="aSendWay">
                             <#if sendWayMap?has_content>
                                <#list sendWayMap ? keys as key>
                                    <option value="${key}">${sendWayMap[key]}</option>
                                </#list>
                             </#if>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right"><span style="color: red;font-size: 20px;">*</span>运单号：</td>
                        <td><input type="text" id="aLogisticCode"></td>
                    </tr>
                    <tr>
                        <td align="right">备注</td>
                        <td><textarea id="aRemark"></textarea></td>
                    </tr>
                </table>
            </form>
            <div>
                <div style="float: left">
                    <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
                </div>
                <div style="float: right">
                    <button id="saveOrder" class="btn btn-primary">保 存</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="return_coin_modal" class="modal hide fade" style="width: 360px">
    <div class="modal-body" style="overflow: auto;height:120px;">
        <div style="width: 80%;margin:0 auto;">
            <div style="height: 30px;">
                <span type="text" id="cOrderId" hidden="hidden"></span>
                <span>点击确定，即可退币成功</span>
            </div>
            <div style="height: 30px;">
                <span>原兑币数自动退回用户学习账户</span>
            </div>
            <div style="height: 30px;">
                <span style="color: red">注：操作之后不可恢复，请慎重操作</span>
            </div>
            <div>
                <div style="float: left">
                    <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
                </div>
                <div style="float: right">
                    <button id="applyCoin" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="batch_import_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量导入运单</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height: 240px;">
        <form id="batch-import-frm" enctype="multipart/form-data" action="" method="post">
            <div style="height: 40px;">
                <input type="file" name="order_file" id="order_file" value="选择文档"/>
            </div>
            <div style="height: 40px" id="templateDiv">
                <span style="color: red" id="templateFile">导入excel文档，无表头</span>
                <span id="link"></span>
            </div>
            <div style="height: 40px;">
                <span style="color: red">注：导入后，请在“导入进度”内查看结果</span>
            </div>
        </form>
        <div style="height: 40px;">
            <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
            <button id="batchImportConfirm" class="btn btn-primary">确 定</button>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#order-query").submit();
    }

    $("#orderQuery").on('click', function () {
        $("#pageNum").val(1);
        $("#order-query").submit();
    });

    $(function () {
        $("#clearQuery").on('click', function () {
            location.href="/opmanager/commodity/order/list.vpage";
        });

        $("#jumpPage").on('blur', function () {
            pagePost($("#jumpPage").val());
        });

        $("#startDate").datetimepicker({
            autoclose: true,
            minuteStep: 5,
            format: 'yyyy-mm-dd hh:ii'
        });
        $("#endDate").datetimepicker({
            autoclose: true,
            minuteStep: 5,
            format: 'yyyy-mm-dd hh:ii'
        });

        $("button[name='queryPhone']").on('click', function () {
            var obj = $(this).parent();
            $.get("getPhone.vpage", {encode_phone: $(this).data("encode_phone")}, function (data) {
                if (data.success) {
                    obj.html(data.phone);
                }
            });
        });

        //编辑订单 start
        function mapForm(func) {
            var frm = $("form#add-logistics-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f);
            });
        }

        $("a[name='addLogistics']").on('click', function () {
            var $this = $(this);
            var orderId = $this.data("id");
            $.get("getOrder.vpage", {orderId: orderId}, function (data) {
                if (data.success) {
                    $("#add_logistics_modal").modal('show');
                    var categoryLevel = data.categoryLevel;
                    if (categoryLevel == 1) {
                        $("#aLogisticCode").attr("disabled", false);
                        $("#aSendWay").attr("disabled", false);
                    } else {
                        $("#aLogisticCode").attr("disabled", true);
                        $("#aSendWay").attr("disabled", true);
                    }
                    var order = data.order;
                    mapForm(function (f) {
                        f.val(order[f.attr("id")]);
                    });
                }
            });
        });
        $("#saveOrder").on('click', function () {
            var postData = {
                id: $("#aOrderId").val(),
                sendStatus: $("#aSendStatus option:selected").val(),
                sendWay: $("#aSendWay option:selected").val(),
                logisticsCode: $("#aLogisticCode").val(),
                remark: $("#aRemark").val()
            };

            if (postData.remark != undefined && postData.remark.length > 200) {
                alert("备注过长");
                return;
            }
            $.post("saveOrder.vpage", postData, function (data) {
                if (data.success) {
                    $("#add_logistics_modal").modal('hide');
                    alert("保存订单信息成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
        //end
        //申请退币 start
        $("a[name='returnCoin']").on("click", function () {
            $("#cOrderId").text($(this).data("id"));
            $("#return_coin_modal").modal('show');
        });
        $("#applyCoin").on('click', function () {
            $.post("returnCoin.vpage", {orderId: $("#cOrderId").text()}, function (data) {
                if (data.success) {
                    $("#return_coin_modal").modal('hide');
                    alert("退币成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
        //end

        //批量导入运单 start
        $("#batchImport").on('click', function () {
            $("#link").html("");
            $.get("templateExcel.vpage", {}, function (data) {
                if (data.success) {
                    $("#link").html("<a href='" + data.templateUrl + "' id='fileUrl'>下载模板</a>");
                }
            });
            $("#batch_import_modal").modal('show');
        });
        $("#batchImportConfirm").on('click', function () {
            var frm = $("form#batch-import-frm");
            frm.submit();
        });
        $("form#batch-import-frm").on('submit', function (e) {
            e.preventDefault();
            var formData = new FormData($("#batch-import-frm")[0]);
            $.ajax({
                url: "batchImport.vpage",
                type: "POST",
                data: formData,
                processData: false,
                contentType: false,
                async: false,
                success: function (data) {
                    if (data.success) {
                        alert("批量导入成功");
                    } else {
                        alert(data.info);
                    }
                    window.location.reload();
                }
            });
        });
        //end

        //批量导出 start
        $("#batchSelect").on('click', function () {
            if ($(this).is(":checked")) {
                $("input[name='orderCheck']").each(function () {
                    $(this).prop("checked", true);
                });
            } else {
                $("input[name='orderCheck']").each(function () {
                    $(this).prop("checked", false);
                });
            }
        });
        $("#batchOutput").on('click', function () {
            var orderIds = [];
            var categories = [];
            $("input[name='orderCheck']:checked").each(function () {
                orderIds.push($(this).data("id"));
                var categoryName = $(this).data("category");
                if ($.inArray(categoryName, categories) == -1) {
                    categories.push(categoryName);
                }
            });

            if (categories.length > 1) {
                alert("请选择分类相同的订单");
                return;
            }

            if (orderIds.length == 0) {
                var category = $("#category").val();
                var startDate = $("#startDate").val();
                var endDate = $("#endDate").val();
                if (category == undefined ||category == '') {
                    alert("商品分类必填");
                    return;
                }
                location.href = " /opmanager/commodity/order/batchOutput.vpage?category=" + category + "&startDate=" + startDate + "&endDate=" + endDate;
            } else {
                location.href = " /opmanager/commodity/order/batchOutput.vpage?orderIds=" + orderIds.toString();
            }
        });
        //end
    });
</script>
</@layout_default.page>