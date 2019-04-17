<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='待处理' page_num=11>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 待处理任务</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="javascript:void(0);" class="btn btn-primary" id="batch_approved">批量通过</a>
            </div>
        </div>
        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr role="row">
                        <th class="unSorting" style="width: 10px;"><input type="checkbox" class="all-select">全选</th>
                        <th class="sorting" style="width: 50px;">申请日期</th>
                        <th class="sorting" style="width: 50px;">申请人</th>
                        <th class="sorting" style="width: 80px;">申请类型</th>
                        <th class="sorting" style="width: 220px;">简介</th>
                        <th class="sorting" style="width: 80px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if dataList?has_content>
                            <#list dataList as item>
                                <#if item.workFlowRecord?has_content>
                                    <tr class="odd">
                                        <td class="center sorting_1">
                                            <#if item.workFlowRecord.workFlowType != 'AGENT_DATA_REPORT_APPLY'>
                                                <input type="checkbox" class="product-apply-item"
                                                                               value="${item.workFlowRecord.id!''}">
                                            </#if>
                                        </td>
                                        <td class="center  sorting_1">${item.workFlowRecord.createDatetime?string("yyyy-MM-dd")}</td>
                                        <td class="center  sorting_1">${item.workFlowRecord.creatorName!}</td>
                                        <td class="center  sorting_1">${item.workFlowRecord.workFlowType.desc!}</td>
                                        <td class="center  sorting_1">${item.workFlowRecord.taskContent!}</td>
                                        <td class="center  sorting_1"><a href="proccess_page.vpage?workflowId=${item.workFlowRecord.id!}&applyType=${item.workFlowRecord.workFlowType!}"> 审核</a></td>
                                        <#--<td class="center  sorting_1"><a href="javascript:void(0)">下载文档（七天内）</a></td>-->
                                    </tr>
                                </#if>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>


<#--修改信息弹窗-->
<div id="editDepInfo_dialog" class="modal fade hide" style="width:400px">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">批量通过</h4>
            </div>
            <div class="modal-body">
                <div id="editInfoDialog" class="form-horizontal">
                    <div class="control-group" style="text-align:center">
                            已选择<span id="checkedSize">0</span>条申请
                    </div>
                    <div class="control-group">
                        <label style="text-align:left;">填写处理意见：</label>
                        <textarea id="dealDes" style="width:100%;">通过</textarea>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary">批量通过</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">


    $(".all-select").click(function () {
        var allSelect = $(this).attr("checked");
        $(".product-apply-item").each(function (index, element) {
            if (allSelect) {
                $(element).attr("checked", allSelect);
                $(element).parent("span").addClass("checked");
            } else {
                $(element).attr("checked", false);
                $(element).parent("span").removeClass("checked");
            }
        });
    });


    $(".product-apply-item").click(function () {
        var allSelect = true;
        $(".product-apply-item").each(function (index, element) {
            if (!$(element).attr("checked")) {
                allSelect = false;
            }
        });
        if (allSelect) {
            $(".all-select").attr("checked", allSelect);
            $(".all-select").parent("span").addClass("checked");
        } else {
            $(".all-select").attr("checked", false);
            $(".all-select").parent("span").removeClass("checked");
        }
    });
    $("#batch_approved").click(function () {
        var data = [];
        $(".product-apply-item").each(function (index, element) {
            if ($(element).attr("checked")) {
                data.push($(element).val());
            }
        });
        if (data.length <= 0){
            alert("请选择记录");
            return;
        }
        $("#checkedSize").html(data.length);
        $("#editDepInfo_dialog").modal('show');
    });

    $("#editDepSubmitBtn").click(function () {
        var dataIds = [];
        $(".product-apply-item").each(function (index, element) {
            if ($(element).attr("checked")) {
                dataIds.push($(element).val());
            }
        });
        if (dataIds.length <= 0){
            alert("请选择记录");
            return;
        }

        var processNote = $("#dealDes").val().trim();
        if (processNote.length <= 0){
            alert("请填写处理意见");
            return;
        }

        $.post('batchApproved.vpage',{
            workflowIds:dataIds.join(','),
            processNote:processNote
        },function(data){
            if(data.success){
                var showMessage = "本次操作共"+data.allCount + "条，其中成功"+(data.allCount - data.errorMessages.length) + "条，失败"+data.errorMessages.length +"条";
                if (data.errorMessages.length > 0){
                    showMessage = showMessage +"，失败原因如下：\r\n" +data.errorMessages.join("\r\n");
                }

                alert(showMessage);
                location.href = "list.vpage"
            }else{
                alert(data.info)
            }
        });


        $("#editDepInfo_dialog").modal('hide');
    });


</script>
</@layout_default.page>
