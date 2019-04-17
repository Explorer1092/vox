<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='近六个月城市支持费用余额' page_num=14>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="alert alert-error" hidden>
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="error-panel"></strong>
        </div>
        <div class="alert alert-info" hidden>
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="info-panel"></strong>
        </div>
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 近六个月城市支持费用余额</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content ">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid" style="margin-top:30px">
                <table  id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">月</th>
                        <th class="sorting" style="width: 90px;">部门</th>
                        <th class="sorting" style="width: 60px;">城市</th>
                        <th class="sorting" style="width: 100px;">城市支持费用预算</th>
                        <th class="sorting" style="width: 60px;">城市支持费用余额</th>
                        <th class="sorting" style="width: 75px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<div id="apply_history02" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>余额变动记录</h3>
    </div>
    <div class="modal-body02" ></div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
    </div>
</div>
<script type="text/html" id="alertBox02">
    <div id="evaluate_table">
        <table>
            <thead>
            <tr>
                <th class="sorting" style="width: 100px;">日期</th>
                <th class="sorting" style="width: 100px;">操作人</th>
                <th class="sorting" style="width: 100px;">变动金额</th>
                <th class="sorting" style="width: 250px;">备注</th>
                <th class="sorting" style="width: 100px;">调整前</th>
                <th class="sorting" style="width: 100px;">调整后</th>
            </tr>
            </thead>
            <tbody>
            <%if(res){%>
            <%for(var i=0;i< res.length;i++){%>
            <%var item = res[i]%>
            <tr class="odd" style="text-align:center">
                <td class="center sorting_1"><%=item.operatorName%></td>
                <td class="center sorting_1"><%=item.operatorName%></td>
                <td class="center sorting_1"><%if(item.afterCash < item.preCash){%>-<%}%><%if(item.afterCash >= item.preCash){%>+<%}%><%=item.quantity%></td>
                <td class="center sorting_1"><%=item.comment%></td>
                <td class="center sorting_1"><%=item.preCash%></td>
                <td class="center sorting_1"><%=item.afterCash%></td>
            </tr>
            <%}%>
            <%}%>
            </tbody>
        </table>
    </div>
</script>
<script type="text/javascript">
    template.helper('Date', Date);
    var groupId = getUrlParam("groupId");
        var dataObj = {
            agentGroupId:groupId
        };
        $.post("departmentDetail.vpage",dataObj,function (res) {
            if(res.success){
                /*$(".itemLength").html(res.budgetList.length);*/
                var dataTableList = [];
                for(var i = 0;i< res.latest6MonthCityBudgetData.dataList.length;i++) {
                    var item = res.latest6MonthCityBudgetData.dataList[i];
                    var operator = '<span class="material_change_record" style="cursor:pointer;color:blue;margin-left: 5px;" data-id="'+ item.id +'">导出明细</span>';
                    var arr = [item.month, item.groupName, item.regionName, item.budget,item.balance,operator];
                    dataTableList.push(arr);
                }
                var reloadDataTable = function () {
                    var table = $('#datatable').dataTable();
                    table.fnClearTable();
                    table.fnAddData(dataTableList); //添加添加新数据
                };
                setTimeout(reloadDataTable(),0); //重绘
            }else{
                $(".itemLength").html(0);
                $(".tbodyContainer").html("")
            }
        });
    $(document).on("click",".material_change_record",function () {
        var budgetId = $(this).data("id");
        $.get("/materialbudget/budget/material_change_record.vpage?budgetId="+budgetId,function(res){
            if(res.success){
                for(var i =0;i< res.budgetChangeRecords.length;i++){
                    res.budgetChangeRecords[i].createTime = new Date(res.budgetChangeRecords[i].createTime).Format("yyyyMMdd");
                }
                $(".modal-body02").html(template("alertBox02",{res:res.budgetChangeRecords}));
                $("#apply_history02").modal("show");
            }
        })
    });

</script>
</@layout_default.page>
