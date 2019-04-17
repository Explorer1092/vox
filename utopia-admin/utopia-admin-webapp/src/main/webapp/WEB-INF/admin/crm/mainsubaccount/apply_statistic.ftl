<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="包班制申请记录" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div class="span11">
    <legend>
        <a href="apply_list.vpage">包班制申请记录</a>&nbsp;&nbsp;
        包班制申请记录统计
    </legend>
    <form id="frm" action="/crm/main_sub_account/download_statistic.vpage" method="post">
        日期: <input id="start" name="start" type="text" class="input-small" value="<#if start??>${start}</#if>"/>&nbsp;~&nbsp;
        <input id="end" name="end" type="text" class="input-small" value="<#if end??>${end}</#if>"/>
        <a id="query_btn" class="btn btn-primary" href="javascript:void(0);">查 询</a>
        <a id="download_btn" class="btn btn-success" href="javascript:void(0);">导出Excel</a>
    </form>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>省</th>
                <th>市</th>
                <th>区</th>
                <th>申请总数量</th>
                <th>申请通过数</th>
                <th>英语→数学</th>
                <th>英语→语文</th>
                <th>语文→数学</th>
                <th>语文→数学</th>
                <th>数学→英语</th>
                <th>数学→语文</th>
            </tr>
            <tbody>
                <#if recordList??>
                    <#list recordList as record>
                    <tr>
                        <td>${record.provName!}</td>
                        <td>${record.cityName!}</td>
                        <td>${record.countyName!}</td>
                        <td>${record.applyCnt!0}</td>
                        <td>${record.successCnt!0}</td>
                        <td>${record.eng2mat!0}</td>
                        <td>${record.eng2chn!0}</td>
                        <td>${record.chn2mat!0}</td>
                        <td>${record.chn2eng!0}</td>
                        <td>${record.mat2chn!0}</td>
                        <td>${record.mat2eng!0}</td>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="11" style="text-align: center; color:red"><strong>没有数据</strong></td>
                    </tr>
                </#if>
            </tbody>
        </table>
    </div>

    <script type="text/javascript">
        $(function () {
            $('#start').datepicker({
                maxDate: 0,
                dateFormat: "yy-mm-dd",
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeYear: false,
                onSelect: function (dateText) {
                    $('#end').datepicker("option", "minDate", dateText);
                }
            });
            $('#end').datepicker({
                maxDate: 0,
                dateFormat: 'yy-mm-dd',
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeYear: false,
                onSelect: function (dateText) {
                    $('#start').datepicker("option", "maxDate", dateText);
                }
            });
        });

        $('#query_btn').on('click', function() {
            var start = $('#start').val();
            var end = $('#end').val();
            if (start == '' || end == '') {
                alert("请选择日期区间");
                return false;
            }
            window.location.href = "/crm/main_sub_account/apply_statistic.vpage?start="+start+"&end="+end;
        });

        $('#download_btn').on('click', function() {
            var start = $('#start').val();
            var end = $('#end').val();
            if (start == '' || end == '') {
                alert("请选择日期区间");
                return false;
            }
           $('#frm').submit();
        });

    </script>
</@layout_default.page>