<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的业绩' page_num=1>
<#if kpiMap?has_content>
    <#list kpiMap?keys as key>
        <div class="row-fluid sortable ui-sortable">
            <div class="box span12">
                <div class="box-header well" data-original-title="">
                    <h2><i class="icon-signal"></i> ${key}</h2>

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
                    <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                        <table class="table table-striped table-bordered" id="DataTables_Table_0"
                               aria-describedby="DataTables_Table_0_info">
                            <thead>
                            <tr>
                                <th class="sorting" style="width: 80px;">用户</th>
                                <th class="sorting" style="width: 80px;">用户负责区域</th>
                                <th class="sorting" style="width: 150px;">考核周期</th>
                                <th class="sorting" style="width: 190px;">指标名称</th>
                                <th class="sorting" style="width: 100px;">已完成数量</th>
                                <th class="sorting" style="width: 100px;">目标数量</th>
                                <th class="sorting" style="width: 100px;">实绩完成度 </th>
                                <th class="sorting" style="width: 100px;">计划完成度 </th>
                            </tr>
                            </thead>

                            <tbody role="alert" aria-live="polite" aria-relevant="all">
                                <#list kpiMap[key] as performance>
                                    <tr class="odd">
                                        <td class="center sorting_1">
                                            <#if subUserCntMap[performance.user.id?string]?has_content && subUserCntMap[performance.user.id?string] gt 0>
                                                <a href="myperformance.vpage?userId=${performance.user.id}">
                                                    ${performance.user.realName}
                                                </a>
                                            <#else>
                                                ${performance.user.realName}
                                            </#if>
                                        </td>
                                        <td class="center sorting_1">
                                            ${performance.regionName}<br/>
                                        </td>
                                        <td class="center  sorting_1">${performance.kpiEval.getEvalDurationFromString()}~${performance.kpiEval.getEvalDurationToString()}</td>
                                        <td class="center  sorting_1">${(performance.kpiEval.kpiDef.kpiName)!}</td>
                                        <td class="center  sorting_1">${(performance.kpiResult)!0}</td>
                                        <td class="center  sorting_1">
                                            <#if performance.kpiTarget == -1>
                                                ---
                                            <#else>
                                                ${(performance.kpiTarget)!0}
                                            </#if>
                                        </td>
                                        <td class="center  sorting_1">
                                            <#if performance.kpiTarget == -1>
                                                ---
                                            <#else>
                                                <#if performance?? && performance.kpiTarget?has_content && performance.kpiResult?has_content>
                                                    <#assign completeRate = (performance.kpiResult / performance.kpiTarget * 100) />
                                                    <#assign processRate = completeRate * (performance.kpiEval.dayEvalDurationDiff()/performance.kpiEval.dayEvalDurationFromAndNowDiff())?int />
                                                    <#--进度比例小于70%时，给用户提醒-->
                                                    <#if processRate lt 70>
                                                        <span class="label label-important">${completeRate?string("###0.0")}%</span>
                                                    <#else>
                                                        <span class="label label-success">${completeRate?string("###0.0")}%</span>
                                                    </#if>
                                                </#if>
                                            </#if>
                                        </td>
                                        <td class="center  sorting_1">
                                            <#if performance.kpiTarget == -1>
                                                ---
                                            <#else>
                                                <#if performance?? && performance.kpiTarget?has_content>
                                                    <#assign planFinishRage = performance.kpiEval.dayEvalDurationFromAndNowDiff() / performance.kpiEval.dayEvalDurationDiff() * 100 />
                                                    <span class="label label-info">${planFinishRage?string("###0.0")}%</span>
                                                </#if>
                                            </#if>
                                        </td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </#list>
<#else>
    <div class="row-fluid sortable ui-sortable">
        <div class="box span12">
            <div class="box-header well" data-original-title="">
                <h2><i class="icon-info"></i>业绩情况</h2>

                <div class="box-icon">
                    <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                    <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
                </div>
                <div class="pull-right">
                    &nbsp;
                </div>
            </div>

            <div class="box-content">
                <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                    <table class="table table-striped table-bordered" id="DataTables_Table_0"
                           aria-describedby="DataTables_Table_0_info">
                        <thead>
                        <tr>
                            <th class="sorting" style="width: 80px;">用户</th>
                            <th class="sorting" style="width: 80px;">用户负责区域</th>
                            <th class="sorting" style="width: 150px;">考核周期</th>
                            <th class="sorting" style="width: 190px;">指标名称</th>
                            <th class="sorting" style="width: 100px;">已完成数量</th>
                            <th class="sorting" style="width: 100px;">目标数量</th>
                            <th class="sorting" style="width: 100px;">实绩完成度 </th>
                            <th class="sorting" style="width: 100px;">计划完成度 </th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</#if>
<script type="text/javascript">

</script>
</@layout_default.page>
