<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=4>
<div class="row-fluid sortable ui-sortable">
<div class="box span12">
<div class="box-header well">
    <h2><i class="icon-th"></i> 下属收入</h2>
    <div class="box-icon">
        <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
        <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
    </div>
</div>

<div class="tab-pane" id="memberIncomePanel">
    <div class="box-content">
        <div>
            <table class="table table-striped table-bordered bootstrap-datatable dataTable datatable" id="dt2">
                <thead>
                <tr>
                    <th class="sorting" style="width: 60px;">姓名</th>
                    <th class="sorting" style="width: 140px;">期间</th>
                    <th class="sorting" style="width: 60px;">地区</th>
                    <th class="sorting" style="width: 100px;">项目内容</th>
                    <th class="sorting" style="width: 60px;">金额</th>
                    <th class="sorting" style="width: 300px;">业绩明细</th>
                </tr>
                </thead>
                <tbody>
                    <#if memberIncome??>
                        <#list memberIncome as mIncome>
                            <#list mIncome.userRegionIncomeData?keys as userRegionKey>
                                <#list mIncome.userRegionIncomeData[userRegionKey].incomeList as incomeData>
                                <tr class="odd">
                                    <td class="center">
                                        <a href="index.vpage?memberId=${mIncome.userId}">${mIncome.userName}</a>
                                    </td>
                                    <td class="center">
                                    ${incomeData.startTime?string("yyyy-MM-dd")}
                                        -
                                    ${incomeData.endTime?string("yyyy-MM-dd")}
                                    </td>
                                    <td class="center">
                                    ${userRegionKey}
                                    </td>
                                    <td class="center">
                                    ${incomeData.source}
                                    </td>
                                    <td class="center">
                                    ${incomeData.income?string(",##0.##")}
                                    </td>
                                    <td class="center">
                                    ${incomeData.extInfo!}
                                    </td>
                                </tr>
                                </#list>
                            </#list>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
</div>
</div>
</div>
</div><!--/span-->
</div>

</@layout_default.page>
