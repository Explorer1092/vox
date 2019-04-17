<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的工作台' page_num=1>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-star-empty"></i> 市场费用审核</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 50px;">大区</th>
                            <th class="sorting" style="width: 80px;">城市</th>
                            <th class="sorting" style="width: 100px;">用户</th>
                            <th class="sorting" style="width: 100px;">区域</th>
                            <th class="sorting" style="width: 150px;">费用内容</th>
                            <th class="sorting" style="width: 50px;">金额</th>
                            <th class="sorting" style="width: 50px;">明细</th>

                        </tr>
                    </thead>

                    <tbody>
                    <#if marketFeeData??>
                    <#list marketFeeData?keys as greatRegionName>
                        <tr class="odd">
                            <td class="center sorting_1" rowspan="${marketFeeData[greatRegionName].dataSize!1}">
                                ${greatRegionName!''}
                            </td>

                            <#list marketFeeData[greatRegionName].partRegionIncomeData?keys as partRegionName>
                                <#if partRegionName_index gt 0>
                                    </tr>
                                    <tr class="odd">
                                </#if>
                                <td class="center sorting_1" rowspan="${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].dataSize!1}">
                                    ${partRegionName!''}
                                </td>

                                <#list marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData?keys as userName>
                                    <#if userName_index gt 0>
                                        </tr>
                                        <tr class="odd">
                                    </#if>
                                    <td class="center sorting_1" rowspan="${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].dataSize!}">
                                        ${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].userName}
                                    </td>

                                    <#list marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].userRegionIncomeData?keys as userRegionName>
                                        <#if userRegionName_index gt 0>
                                            </tr>
                                            <tr class="odd">
                                        </#if>
                                        <td class="center sorting_1" rowspan="${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].userRegionIncomeData[userRegionName].dataSize!}">
                                            ${userRegionName!}
                                        </td>

                                        <#list marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].userRegionIncomeData[userRegionName].incomeList as incomeData>
                                            <#if incomeData_index gt 0>
                                                </tr>
                                                <tr class="odd">
                                            </#if>
                                            <td class="center sorting_1">
                                                ${incomeData.source!}
                                            </td>
                                            <td class="center sorting_1">
                                                ${incomeData.income!}
                                            </td>
                                            <td class="center sorting_1">
                                                ${incomeData.extInfo!}
                                            </td>
                                        </#list>
                                    </#list>
                                    </tr>
                                    <tr>
                                        <td class="center sorting_1" colspan="2">
                                        ${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].userName}费用小计
                                        </td>
                                        <td class="center sorting_1" colspan="2">
                                        ${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].userIncomeData[userName].userIncome!}
                                        </td>
                                    </tr>
                                </#list>
                                </tr>
                                <tr>
                                    <td class="center sorting_1" colspan="3">
                                    ${partRegionName!''}费用合计
                                    </td>
                                    <td class="center sorting_1" colspan="2">
                                        ${marketFeeData[greatRegionName].partRegionIncomeData[partRegionName].totalIncome!}
                                    </td>
                                </tr>
                                </#list>
                            <tr>
                            <td class="center sorting_1" colspan="4">
                            ${greatRegionName!''}费用合计
                            </td>
                            <td class="center sorting_1" colspan="2">
                            ${marketFeeData[greatRegionName].totalIncome!}
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

<script type="text/javascript">
    $(function(){
        $("a[id^='finance_confirm_']").live('click',function(){
            if(!confirm("确定要审核通过这些条目?")){
                return false;
            }

            user_id = $(this).attr("id").substring("finance_confirm_".length);
            salary_month = user_id.substring(0, 6);
            user_id = user_id.substring(7);

            $.post('financeconfirm.vpage',{
                month: parseInt(salary_month),
                user: parseInt(user_id)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });

        $("a[id^='market_confirm_']").live('click',function(){
            if(!confirm("确定要审核通过这些条目?")){
                return false;
            }

            user_id = $(this).attr("id").substring("market_confirm_".length);
            salary_month = user_id.substring(0, 6);
            user_id = user_id.substring(7);

            $.post('marketconfirm.vpage',{
                month: parseInt(salary_month),
                user: parseInt(user_id)
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
