<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的申请' page_num=3>
<style>
    .active{
        background:#eaeaea
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 我的申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="monthstatistics.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <ul class="nav nav-tabs">
                        <#if statusList?has_content>
                            <#list statusList as item>
                                <li class="tab-list1 <#if status?? && status == item>active</#if>"><a
                                        href="list.vpage?status=${item.code!}">${item.desc!}</a></li>
                            </#list>
                        </#if>
                    </ul>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 10%;">申请日期</th>
                        <th class="sorting" style="width: 10%;">申请类型</th>
                        <th class="sorting" style="width: 30%;">简介</th>
                        <th class="sorting" style="width: 20%;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if applyList??>
                            <#list applyList?sort_by("createDatetime")?reverse as apply>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1"><#if apply.applyType?? && apply.applyType == "AGENT_MATERIAL_APPLY" && apply.orderTime??>${apply.orderTime?string("yyyy-MM-dd")}<#else>${apply.createDatetime?string("yyyy-MM-dd")}</#if></td>
                                    <td class="center  sorting_1">${apply.applyType.desc!}</td>
                                    <td class="center  sorting_1">${apply.generateSummary()!}</td>
                                    <td class="center  sorting_1">
                                        <a href="apply_datail.vpage?applyType=${apply.applyType!}&applyId=${apply.id!}">查看详情</a>&nbsp;&nbsp;
                                        <#if apply.canRevoke?? && apply.canRevoke && selectStatus!=4 && selectStatus!=3>
                                            <a href="javascript:void(0)" onclick="revokeApply('${apply.applyType!}','${apply.id!}')">撤销</a>
                                        </#if>
                                        <#if selectStatus==3 && apply.applyType == "AGENT_UNIFIED_EXAM_APPLY" && apply.unifiedExamStatus?? && (apply.unifiedExamStatus == "SO_REJECTED" || apply.unifiedExamStatus == "CR_REJECTED")>
                                            <a href="/apply/create/unified_exam_apply_view.vpage?applyId=${apply.id!}">重新申请</a>
                                        </#if>
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
        if(getQuery('status') == 'PENDING'){
            $('.tab-list1').addClass('active')
        }else if(getQuery('status') == 'APPROVED'){
            $('.tab-list2').addClass('active')
        }else if(getQuery('status') == 'REJECTED'){
            $('.tab-list3').addClass('active')
        }else if(getQuery('status') == 'CANCELED'){
            $('.tab-list4').addClass('active')
        }
    });

    function revokeApply(applyType, applyId){
        if(confirm("是否撤销")) {
            if (applyType == "" || applyId == "") {
                alert("请填写调整原因！");
                return;
            }
            $.post('/apply/create/revoke_apply.vpage', {
                applyType: applyType,
                applyId: applyId
            }, function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    if (!alert("订单撤销成功")) {
                        window.location.reload();
                    }
                }
            });
        }
    }

</script>
</@layout_default.page>
