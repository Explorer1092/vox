<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='审核管理' page_num=11>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 已处理任务</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr role="row">
                        <th class="sorting" style="width: 50px;">申请日期</th>
                        <th class="sorting" style="width: 50px;">申请人</th>
                        <th class="sorting" style="width: 80px;">申请类型</th>
                        <th class="sorting" style="width: 220px;">简介</th>
                        <th class="sorting" style="width: 50px;">审核日期</th>
                        <th class="sorting" style="width: 50px;">审核结果</th>
                        <th class="sorting" style="width: 220px;">审核意见</th>
                        <th class="sorting" style="width: 80px;">操作</th>
                    </tr>
                    </thead>

                    <tbody>
                        <#if dataList?has_content>
                            <#list dataList as item>
                                    <tr class="odd">
                                        <#if item.workFlowRecord?has_content>
                                            <td class="center  sorting_1">${item.workFlowRecord.createDatetime?string("yyyy-MM-dd")}</td>
                                            <td class="center  sorting_1">${item.workFlowRecord.creatorName!}</td>
                                            <td class="center  sorting_1">${item.workFlowRecord.workFlowType.desc!}</td>
                                            <td class="center  sorting_1">${item.workFlowRecord.taskContent!}</td>
                                        <#else>
                                            <td class="center  sorting_1"></td>
                                            <td class="center  sorting_1"></td>
                                            <td class="center  sorting_1"></td>
                                            <td class="center  sorting_1"></td>
                                        </#if>
                                        <#if item.processHistory?has_content>
                                            <td class="center  sorting_1">${item.processHistory.createDatetime?string("yyyy-MM-dd")}</td>
                                            <td class="center  sorting_1">${item.processHistory.result.desc!}</td>
                                            <td class="center  sorting_1">${item.processHistory.processNotes!}</td>
                                        <#else>
                                            <td class="center  sorting_1"></td>
                                            <td class="center  sorting_1"></td>
                                            <td class="center  sorting_1"></td>
                                        </#if>
                                        <td class="center  sorting_1"><#if item.workFlowRecord?has_content><a href="/apply/view/apply_datail.vpage?applyType=${item.workFlowRecord.workFlowType}&workflowId=${item.workFlowRecord.id}"> 查看详情</a></#if></td>
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
        $('#show_hide').hide();
    });
</script>
</@layout_default.page>
