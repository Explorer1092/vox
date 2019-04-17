<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='付费数据' page_num=1>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 付费数据查询</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a class="btn btn-round" href="javascript:window.history.back();">
                    <i class="icon-chevron-left"></i>
                </a>&nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="paymentsummary.vpage" method="post" class="form-horizontal">
                <fieldset>
                    <table>
                        <tr>
                            <td width="180px">开始日期&nbsp;<input type="text" class="input-small" id="startDate" name="startDate" value="${startDate!}"></td>
                            <td width="180px">结束日期&nbsp;<input type="text" class="input-small" id="endDate" name="endDate" value="${endDate!}"></td>
                            <td width="180px">区域&nbsp;<input type="text" class="input-small" id="regionNames" readonly="true" value="${regionNames!}" style="cursor: pointer">
                                <input type="hidden" name="regionIds" id="regionIds" value="${regionIds!}"/></td>
                            <td width="120px"><input type="checkbox" name="productSplit" <#if productSplit>checked="true"</#if> value="1" >显示产品详细</td>
                            <td width="120px"><input type="checkbox" name="dailySplit" <#if dailySplit>checked="true"</#if> value="1">显示每天详细</td>
                            <td><button type="submit" id="search_btn" class="btn btn-success">查询</button></td>
                        </tr>
                    </table>
                </fieldset>
            </form>
            <br/>
            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">地区</th>
                        <th class="sorting" style="width: 180px;">日期</th>
                        <th class="sorting" style="width: 100px;">产品</th>
                        <th class="sorting" style="width: 100px;">订单数</th>
                        <th class="sorting" style="width: 100px;" title="按日进行了排重,没有按照时间区间进行排重">购买人数<i class="icon-question-sign"></i></th>
                        <th class="sorting" style="width: 100px;">订单总金额</th>
                        <th class="sorting" style="width: 100px;">实物卡金额</th>
                        <th class="sorting" style="width: 100px;">退款金额</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if paymentData??>
                            <#list paymentData.groupSummaryData?keys as groupId>
                            <tr class="odd">
                                <td class="center  sorting_1" rowspan="${paymentData.groupSummaryData[groupId].deepSize}">
                                    <#if paymentData.groupSummaryData[groupId].groupType == 'region'>
                                        <a href="paymentsummary.vpage?startDate=${startDate}&endDate=${endDate}&region=${paymentData.groupSummaryData[groupId].groupId}&dailySplit=${dailySplit?string('1', '0')}&productSplit=${productSplit?string('1','0')}">
                                            ${paymentData.groupSummaryData[groupId].groupName!}
                                        </a>
                                    <#elseif paymentData.groupSummaryData[groupId].groupType == 'school'>
                                        <a href="paymentsummary.vpage?startDate=${startDate}&endDate=${endDate}&school=${paymentData.groupSummaryData[groupId].groupId}&dailySplit=${dailySplit?string('1', '0')}&productSplit=${productSplit?string('1','0')}">
                                            ${paymentData.groupSummaryData[groupId].groupName!}
                                        </a>
                                    <#else>
                                        ${paymentData.groupSummaryData[groupId].groupName!}
                                    </#if>
                                </td>

                                <#assign dailyFlag = false />
                                <#list paymentData.groupSummaryData[groupId].dailySummaryData?keys as date>
                                    <#if dailyFlag></tr><tr></#if>
                                    <#assign dailyFlag = true />
                                    <td class="center  sorting_1" rowspan="${paymentData.groupSummaryData[groupId].dailySummaryData[date].deepSize}">
                                        ${paymentData.groupSummaryData[groupId].dailySummaryData[date].dateRange}

                                    </td>
                                    <#assign productFlag = false />
                                    <#list paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData?keys as productName>
                                        <#if productFlag></tr><tr></#if>
                                        <#assign productFlag = true />
                                        <td class="center  sorting_1">
                                            ${paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData[productName].productName}
                                        </td>

                                        <td class="center  sorting_1">
                                            ${paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData[productName].orderCount}
                                        </td>

                                        <td class="center  sorting_1">
                                            ${paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData[productName].orderUserNum}
                                        </td>

                                        <td class="center  sorting_1">
                                            ${paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData[productName].orderAmount}
                                        </td>

                                        <td class="center  sorting_1">
                                            ${paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData[productName].cardpayAmount}
                                        </td>

                                        <td class="center  sorting_1">
                                            ${paymentData.groupSummaryData[groupId].dailySummaryData[date].productSummaryData[productName].refundAmount}
                                        </td>
                                    </#list>

                                </#list>

                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="region_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择查询区域</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <div id="regiontree" class="controls" style="width: 280px;height: 400px"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="region_select_btn" type="button" class="btn btn-primary">确定</button>
                </div>
            </form>
        </div>
    </div>
</div>


<script type="text/javascript">
    $(function(){

        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $('#regionNames').on('click',function(){
            var $regiontree = $("#regiontree");
            $regiontree.fancytree('destroy');

            $regiontree.fancytree({
                source: {
                    url: "/common/region/loadregion.vpage",
                    cache:false
                },
                checkbox: true,
                selectMode: 2
            });

            $('#region_select_dialog').modal('show');
        });

        $('#region_select_btn').on('click',function(){
            var regionTree = $("#regiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if(regionNodes == null || regionNodes == "undefined") {
                $('#regionIds').val('');
                $('#regionNames').val('');
                return;
            }

            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function(node){
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });

            $('#regionIds').val(selectRegionIdList.join(','));
            $('#regionNames').val(selectRegionNameList.join(','));

            $('#region_select_dialog').modal('hide');

        });

        $('#search_btn').on('click',function(){
            var startDate = $('#startDate').val();
            var endDate = $('#endDate').val();
            var regionIds = $('#regionIds').val();

            if (startDate == '') {
                alert("请选择开始日期!");
                return false;
            }

            if (endDate == '') {
                alert("请选择结束日期!");
                return false;
            }

            if (regionIds == '') {
                alert("请选择要查询的区域!");
                return false;
            }
            return true;
        });

    });
</script>
</@layout_default.page>
