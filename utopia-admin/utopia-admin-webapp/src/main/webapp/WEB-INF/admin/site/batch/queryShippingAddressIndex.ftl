<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
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
    <@h.head/>
    <fieldset>
        <legend>查询订单物流信息</legend>
        <div>
            <form id="s_form" action="${requestContext.webAppContextPath}/site/orderShippAddress/loadOrderShippAddress.vpage"
                  method="post" class="form-horizontal">
                <ul class="inline">
                    <li>
                        活动来源：<input id="activitySource" name="activitySource" value="${activitySource!''}" type="text"></input>
                    </li>
                    <li>
                        用户Id：<input id="userId" name="userId" value="${userId!''}" type="text"></input>
                    </li>
                    <li>
                        订单Id：<input id="orderId" name="orderId" value="${orderId!''}" type="text"></input>
                    </li>
                    <li>
                        收件人姓名：<input id="receiverName" name="receiverName" value="${receiverName!''}" type="text"></input>
                    </li>
                    <li>
                        物流编号：<input id="logisticsNum" name="logisticsNum"  value="${logisticsNum!''}" type="text"></input>
                    </li>
                    <li>
                        <button id="editBut" type="submit" class="btn btn-primary">查询</button>
                        <button class="btn" id="exportBtn">导出</button>
                    </li>
                </ul>
            </form>
        </div>
        <div>
            查询结果：
            <table class="table table-bordered">
                <tr>
                    <th>用户ID</th>
                    <th>订单ID</th>
                    <th>收件人</th>
                    <th>收件人电话</th>
                    <th>收件人详细地址</th>
                    <th>邮编</th>
                    <th>省编号</th>
                    <th>省名称</th>
                    <th>城市编号</th>
                    <th>城市名称</th>
                    <th>区县编号</th>
                    <th>区县名称</th>
                    <th>物流公司</th>
                    <th>物流编号</th>
                    <th>活动来源</th>
                </tr>

                <#if orderShippingAddresses??>
                        <#list orderShippingAddresses as address>
                        <tr>
                            <td>${address.userId!}</td>
                            <td>${address.orderId!}</td>
                            <td>${address.receiverName!''} </td>
                            <td>${address.receiverPhone!''}</a></td>
                            <td>${address.detailAddress!''}</td>
                            <td>${address.postCode!''}</td>
                            <td>${address.provinceCode!''}</td>
                            <td>${address.provinceName!''}</td>
                            <td>${address.cityCode!''}</td>
                            <td>${address.cityName!''}</td>
                            <td>${address.countyCode!''}</td>
                            <td>${address.countyName!''}</td>
                            <td>${address.logisticsCompany!''}</td>
                            <td>${address.logisticsNum!''}</td>
                            <td>${address.activitySource!''}</td>
                            <td></td>
                        </tr>
                        </#list>
                </#if>
            </table>
        </div>
    </fieldset>
</div>

<script type="text/javascript">
    $(function(){
        $('#exportBtn').on('click', function () {
            if ($("#exportBtn").attr("disabled")) {
                return;
            }
            $("#exportBtn").attr("disabled", true).text("信息导出中，请稍等...");
            var data = {
                activitySource: $("#activitySource").val(),
                userId: $("#userId").val(),
                orderId: $("#orderId").val(),
                receiverName: $("#externalTrade").val(),
                logisticsNum: $("#logisticsNum").val(),
            };
            $.ajax({
                url: '/site/orderShippAddress/createOrderShippAddressXls.vpage',
                type: 'POST',
                data: data,
                timeout: 0,
                success: function (res) {
                    if (res.success) {
                        alert('导出成功');
                        //下载报告
                        var filePath = res.filePath;
                        var requestUrl = "/site/batch/downReport.vpage?filePath=" + filePath + "&fileName=订单物流信息.xlsx";
                        var downloadIframe = "<iframe style='display:none;' src=" + requestUrl + "/>";
                        $("body").append(downloadIframe);
                    } else {
                        alert(res.info);
                    }
                    $("#exportBtn").attr("disabled", false).text("导出");
                }
            })
        });

    });
</script>

</@layout_default.page>