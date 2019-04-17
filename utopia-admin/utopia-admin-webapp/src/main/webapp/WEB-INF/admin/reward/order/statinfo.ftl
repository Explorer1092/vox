<#-- @ftlvariable name="adminDictGroupNameList" type="java.util.List<java.lang.String>" -->
<#import "../../layout_default.ftl" as layout_default>

<@layout_default.page page_title='Web manage' page_num=12>

<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">

<style>

    li, label {
        font-size: 16px;
    }

    .ui-datepicker-calendar {
        display: none;
    }

    input {
        margin-bottom: 0px;
    }

</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>订单统计信息</legend>
        </fieldset>
        <fieldset style="font-size: 12px;">
            奖品ID： <input id="productId" class="input-small" type="text" placeholder="输入奖品ID">
            开始月份： <input id="startDate" class="input-medium" type="text" value="${beginDate!''}">
            结束月份： <input id="endDate" class="input-medium" type="text" placeholder="例：201407">
            <button id="submitSelected" class="btn btn-info">查询统计信息</button>
            <button id="inAuditExport" class="btn btn-success">导出</button>
            <button id="selectCount" class="btn btn-info">查询下单人数</button>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <div id="order_stat_chip"></div>
        </fieldset>
    </div>

    <br><br><br>

    <div>
        <fieldset>
            <legend>发货单生成后统计信息</legend>
        </fieldset>
        <fieldset style="font-size: 12px;">
            月份：<input id="month" class="input-medium" type="text" value="${beginDate!''}" title="查询结果是选择月份前一个月的发货单">
            <button id="submitQuery" class="btn btn-info">查询</button>
            <button id="export" class="btn btn-success">导出</button>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <div id="complete_order_stat_chip"></div>
        </fieldset>
    </div>

    <br><br><br>

    <div>
        <fieldset>
            <legend>订单统计信息(按日查询)</legend>
        </fieldset>
        <fieldset style="font-size: 12px;">
            奖品ID： <input id="productIds" class="input-xlarge" type="text" placeholder="输入奖品ID，以逗号分隔，最多五个">
            开始日期： <input id="startDay" class="input-small" type="text" readonly>
            结束日期： <input id="endDay" class="input-small" type="text" readonly>
            订单状态:
            <select id="status" name="status">
                <option value="">全部</option>
                <#if statusList?has_content>
                    <#list statusList as s>
                        <option value="${s.name()!}">${s.getDesc()!}</option>
                    </#list>
                </#if>
            </select>
            <button id="query-order-btn" class="btn btn-info">查询</button>
            <button id="export-order-btn" class="btn btn-success">导出</button>
        </fieldset>
    </div>

    <div>
        <fieldset>
            <div id="order_stat_1_chip"></div>
        </fieldset>
    </div>

    <br><br><br>

    <div>
        <fieldset>
            <legend>公益活动统计信息</legend>
        </fieldset>
        <fieldset style="font-size: 12px;">
            活动ID: <input id="activity-id" class="input-small" type="text" >
            日期：<input id="activity-start-date" class="input-small" type="text" readonly>
            <button id="query-activity-btn" class="btn btn-info">查询</button>
        </fieldset>
    </div>

    <div>
        <fieldset>
            <div id="activity_stat_chip"></div>
        </fieldset>
    </div>

    <div>
        <fieldset>
            <legend>流量包状态查询</legend>
        </fieldset>
        <fieldset style="font-size: 12px;">
            老师ID: <input id="flowpack-id" class="input-small" type="text"  />
            <button id="query-flowpack-btn" class="btn btn-info" >查询</button>
            <button id="query-flowpack-undone-btn" class="btn btn-success" >查询未响应订单</button>
        </fieldset>
    </div>

    <div>
        <fieldset>
            <div id="flowpack_chip"></div>
        </fieldset>
    </div>

    <script>
        $(function () {
            $("#startDate,#month,#endDate").datepicker({
                dateFormat: 'yymm',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                changeMonth: true,
                changeYear: true,
                onClose: function (dateText, inst) {
                    var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                    var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                    $(this).datepicker('setDate', new Date(year, month, 1));
                },
                beforeShow: function () {
                    $(".ui-datepicker-calendar").hide();
                }
            });

            $("#startDay").datetimepicker({
                language: "cn",
                format: "yyyy-mm-dd",
                autoclose: true,
                minView: 2,
                endDate: new Date()
            }).on("changeDate",function(e){
                var endTime = addDay(e.date,30);
                var startTime = addDay(e.date,-1);
                $("#endDay").datetimepicker("setStartDate",startTime);
                $("#endDay").datetimepicker("setEndDate",endTime);
            });

            $("#endDay").datetimepicker({
                language: "cn",
                format: "yyyy-mm-dd",
                autoclose: true,
                minView: 2
            }).on("changeDate",function(e){
                var startTime = addDay(e.date,-30);
                var endTime = addDay(e.date,-1);
                $("#startDay").datetimepicker("setStartDate",startTime);
                $("#startDay").datetimepicker("setEndDate",endTime);
            });

            function addDay(dd,dadd){
                //可以加上错误处理
                var a = new Date(dd)
                a = a.valueOf()
                a = a + dadd * 24 * 60 * 60 * 1000
                a = new Date(a)
                return a;
            }

            $('#submitSelected').on('click', function () {
                var beginDate = $('#startDate').val();
                var endDate = $('#endDate').val();

                if (beginDate.length == 0) {
                    alert("请选择开始月份");
                    return;
                }

                if (endDate.length == 0) {
                    alert("请选择结束月份");
                    return;
                }

                $('#order_stat_chip').load('getstatinfo.vpage',
                        {productId: $('#productId').val(), beginDate: beginDate, endDate: endDate}
                );
            });

            $("#query-order-btn").on('click', function () {
                var startDay = $('#startDay').val();
                var endDay = $('#endDay').val();

//                if (!startDay || startDay.trim() == '') {
//                    alert("请选择开始日期");
//                    return;
//                }
//
//                if (!endDay || endDay.trim() == '') {
//                    alert("请选择开始结束");
//                    return;
//                }

                var productIds = $('#productIds').val();
                if (!productIds || productIds.trim() == '') {
                    alert("奖品id不能为空");
                    return;
                }

                var products = productIds.split(",");
                if (products && products.length > 5) {
                    alert("奖品id最多填写5个，不能超过!")
                    return;
                }

                var orderStatus = $("#status").val();

                $('#order_stat_1_chip').load('getstatinfo1.vpage',
                        {productIds: $('#productIds').val(), beginDate: startDay, endDate: endDay,orderStatus:orderStatus}
                );
            });

            $('#selectCount').on('click', function () {

                var beginDate = $('#startDate').val();
                var endDate = $('#endDate').val();
                if (beginDate.length == 0) {
                    alert("请选择开始日期");
                    return;
                }
                if (endDate.length == 0) {
                    alert("请选择结束日期");
                    return;
                }

                $(this).attr({"disabled":"disabled"});
                var $this = $(this);

                $.ajax({
                    type: "post",
                    url: "getusercount.vpage",
                    data: {
                        productId: $('#productId').val(),
                        beginDate: beginDate,
                        endDate: endDate
                    },
                    success: function (data) {
                        alert(data.info);
                        $this.removeAttr("disabled");
                    }
                });
            });

            $('#inAuditExport').on('click', function () {
                var beginDate = $('#startDate').val();
                var endDate = $('#endDate').val();
                if (beginDate.length == 0) {
                    alert("请选择开始日期");
                    return;
                }
                if (endDate.length == 0) {
                    alert("请选择结束日期");
                    return;
                }
                location.href = "downloadstatinfo.vpage?productId=" + $('#productId').val() + "&beginDate=" + beginDate + "&endDate=" + endDate;
            });

            $("#export-order-btn").on('click', function () {
                var startDay = $('#startDay').val();
                var endDay = $('#endDay').val();

                if (!startDay || startDay.trim() == '') {
                    alert("请选择开始日期");
                    return;
                }

                if (!endDay || endDay.trim() == '') {
                    alert("请选择开始结束");
                    return;
                }

                var productIds = $('#productIds').val();
                if (!productIds || productIds.trim() == '') {
                    alert("奖品id不能为空");
                    return;
                }

                var products = productIds.split(",");
                if (products && products.length > 5) {
                    alert("奖品id最多填写5个，不能超过!");
                    return;
                }

                for (var i=0; i<products.length; ++i) {
                    if (!$.isNumeric(products[i])){
                        alert("无效的奖品ID：" + products[i]);
                        return;
                    }
                }

                var orderStatus = $("#status").val();

                location.href = "downloadstatinfo.vpage?productIds="
                        + $('#productIds').val() +
                        "&beginDate=" + startDay +
                        "&endDate=" + endDay +
                        "&orderStatus=" + orderStatus +
                        "&queryMode=1";

            });

            $('#submitQuery').on('click', function () {
                var month = $('#month').val();
                if (month.length == 0) {
                    alert("请选择开始月份");
                    return;
                }
                $('#complete_order_stat_chip').load('getcompleteorderstatinfo.vpage', {month: month}
                );
            });

            $('#export').on('click', function () {
                var month = $('#month').val();
                if (month.length == 0) {
                    alert("请选择开始月份");
                    return;
                }
                location.href = "downloadcompleteorderstatinfo.vpage?month=" + month;
            });

            $("#activity-start-date").datetimepicker({
                language: "cn",
                format: "yyyy-mm-dd",
                autoclose: true,
                minView: 2
            });

            $("#query-activity-btn").on('click',function(){
                var activityId = $("#activity-id").val();
                if(!activityId || activityId.length == 0){
                    alert("活动id不能为空!");
                    return;
                }

                var queryDate = $("#activity-start-date").val();
                if(!queryDate || queryDate.length == 0){
                    alert("请选择查询日期!");
                    return;
                }

                $("#activity_stat_chip").load("getactivitystatinfo.vpage",
                        {activityId:activityId,startDate:queryDate});
            });

            $('#query-flowpack-btn').on('click', function () {
                var teacherId = $('#flowpack-id').val();
                if (!teacherId || teacherId.length == 0) {
                    alert("老师ID不能为空!");
                    return;
                }
                $('#flowpack_chip').load("getflowpackhistory.vpage", {teacherId:teacherId});

            });

            $('#query-flowpack-undone-btn').on('click', function () {
                $('#flowpack_chip').load("getundoneflowpack.vpage");

            });
        });
    </script>
</@layout_default.page>