<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的工作台' page_num=1>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-star-empty"></i> 市场支持费用审核</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <form action="downloadmarketfee.vpage" method="post">
                    <#if evalDate??>
                        <input type="hidden" name="evalDate" value="${evalDate}"/>
                    </#if>
                    <#--<button type="submit" class="btn btn-success">导出excel</button>-->
                </form>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 50px;">区域</th>
                        <th class="sorting" style="width: 80px;">用户姓名</th>
                        <th class="sorting" style="width: 100px;">考核期间</th>
                        <th class="sorting" style="width: 100px;">地区</th>
                        <th class="sorting" style="width: 150px;">项目内容</th>
                        <th class="sorting" style="width: 50px;">现金奖励</th>
                        <th class="sorting" style="width: 50px;">点数奖励</th>
                        <th class="sorting" style="width: 110px;">业绩明细</th>
                        <th class="sorting" style="width: 100px;">全国总监确认</th>
                        <th class="sorting" style="width: 80px;">财务确认</th>
                        <th class="sorting" style="width: 80px;">操作</th>
                    </tr>
                    </thead>

                    <tbody>
                        <#if marketFeeData??>
                            <#list marketFeeData as provinceData>
                            <tr class="odd">
                                <td class="center sorting_1" rowspan="${provinceData.dataSize!}">
                                ${provinceData.provinceName!}
                                </td>

                                <#list provinceData.userIncomeData?keys as userKey>
                                    <#if userKey_index gt 0>
                                    </tr>
                                    <tr class="odd">
                                    </#if>
                                    <td class="center sorting_1" rowspan="${provinceData.userIncomeData[userKey].dataSize!}">
                                    ${provinceData.userIncomeData[userKey].userName!}
                                    </td>

                                    <#list provinceData.userIncomeData[userKey].durationIncomeData?keys as durationKey>
                                        <#if durationKey_index gt 0>
                                        </tr>
                                        <tr class="odd">
                                        </#if>
                                        <td class="center sorting_1" rowspan="${provinceData.userIncomeData[userKey].durationIncomeData[durationKey].dataSize!}">
                                        ${durationKey}
                                        </td>

                                        <#list provinceData.userIncomeData[userKey].durationIncomeData[durationKey].regionIncomeData?keys as regionKey>
                                            <#if regionKey_index gt 0>
                                            </tr>
                                            <tr class="odd">
                                            </#if>
                                            <td class="center sorting_1" rowspan="${provinceData.userIncomeData[userKey].durationIncomeData[durationKey].regionIncomeData[regionKey].dataSize!}">
                                            ${regionKey}
                                            </td>

                                            <#list provinceData.userIncomeData[userKey].durationIncomeData[durationKey].regionIncomeData[regionKey].incomeList as incomeData>
                                                <#if incomeData_index gt 0>
                                                </tr>
                                                <tr class="odd">
                                                </#if>
                                                <td class="center sorting_1">
                                                ${incomeData.source}
                                                </td>
                                                <td class="center sorting_1">
                                                ${incomeData.cashIncome?string(",##0.##")}
                                                </td>
                                                <td class="center sorting_1">
                                                ${incomeData.pointIncome?string(",##0.##")}
                                                </td>
                                                <td class="center sorting_1">
                                                ${incomeData.extInfo}
                                                </td>
                                                <#if userKey_index = 0 && durationKey_index = 0 && regionKey_index = 0 && incomeData_index = 0 >
                                                    <td class="center sorting_1"  rowspan="${provinceData.dataSize!}" >
                                                    ${provinceData.managerChecked?string("<span class='label label-success'>已确认</span>","未确认")}
                                                    </td>
                                                    <td class="center sorting_1"  rowspan="${provinceData.dataSize!}" >
                                                    ${provinceData.financeChecked?string("<span class='label label-success'>已确认</span>","未确认")}
                                                    </td>
                                                    <td class="center sorting_1"  rowspan="${provinceData.dataSize!}" >
                                                        <#if requestContext.getCurrentUser().isFinance()>
                                                            <#if provinceData.financeChecked>
                                                                已确认
                                                            <#else>
                                                                <a id="finance_confirm_${userKey_index}" class="btn btn-success" href="javascript:void(0);" data="${provinceData.dataIdList!}">
                                                                    <i class="icon-check icon-white"></i>
                                                                    确认
                                                                </a>
                                                            </#if>
                                                        </#if>
                                                        <#if requestContext.getCurrentUser().isCountryManager()>
                                                            <#if provinceData.managerChecked>
                                                                已确认
                                                            <#else>
                                                                <a id="country_confirm_${userKey_index}" class="btn btn-success" href="javascript:void(0);" data="${provinceData.dataIdList!}">
                                                                    <i class="icon-check icon-white"></i>
                                                                    确认
                                                                </a>
                                                            </#if>
                                                        </#if>
                                                    </td>
                                                </#if>
                                            </#list>
                                        </#list>
                                    </#list>
                                </tr>
                                <tr>
                                    <td class="center sorting_1" colspan="3">
                                        费用小计
                                    </td>
                                    <td class="center sorting_1">
                                    ${provinceData.userIncomeData[userKey].totalCashIncome?string(",##0.##")}
                                    </td>
                                    <td class="center sorting_1">
                                    ${provinceData.userIncomeData[userKey].totalPointIncome?string(",##0.##")}
                                    </td>
                                    <td class="center sorting_1"> </td>
                                </tr>
                                </#list>
                            <tr>
                                <td class="center sorting_1" colspan="4">
                                    全省市场费用合计
                                </td>
                                <td class="center sorting_1">
                                ${provinceData.totalCashIncome?string(",##0.##")}
                                </td>
                                <td class="center sorting_1">
                                ${provinceData.totalPointIncome?string(",##0.##")}
                                </td>
                                <td class="center sorting_1"> </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $("a[id^='finance_confirm_']").live('click',function(){

            if(!confirm("确定要审核通过这些条目?")){
                return false;
            }
            $.post('financeconfirm.vpage',{
                ids: $(this).attr('data')
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });

        $("a[id^='country_confirm_']").live('click',function(){

            if(!confirm("确定要审核通过这些条目?")){
                return false;
            }
            $.post('managerconfirm.vpage',{
                ids: $(this).attr('data')
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
</@layout_default.page>
