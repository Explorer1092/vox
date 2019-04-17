<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的工作台' page_num=1>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>市场支持费用查询</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div class="span2">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">结算日期</th>
                    </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>
                                <a href="index2015.vpage?month=9">2015年09月</a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="index2015.vpage?month=10">2015年10月</a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="index2015.vpage?month=11">2015年11月</a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="index2015.vpage?month=12">2015年12月</a>
                            </td>
                        </tr>
                        <#--<#if kpiEvalDateList??>-->
                            <#--<#list kpiEvalDateList as kpiEvalDate>-->
                            <#--<tr class="odd">-->
                                <#--<td class="center sorting_1">-->
                                    <#--<a href="index2015.vpage?evalDate=${kpiEvalDate}">${kpiEvalDate}</a>-->
                                <#--</td>-->
                            <#--</#list>-->
                        <#--</#if>-->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

</@layout_default.page>
