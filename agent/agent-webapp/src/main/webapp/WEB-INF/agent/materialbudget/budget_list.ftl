<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='费用余额明细' page_num=14>
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
            <h2><i class="icon-th"></i> 费用余额明细</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <table class="table table-striped table-bordered bootstrap-datatable">
                <thead>
                    <tr>
                        <th>学期</th>
                        <th>部门名称</th>
                        <th>部门物料预算</th>
                        <th>部门物料余额</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>${dataMap.schoolTerm!''}</td>
                        <td>${dataMap.groupName!''}</td>
                        <td>${dataMap.groupBudget!''}</td>
                        <td>${dataMap.groupBalance!''}</td>
                    </tr>
                </tbody>
            </table>
            <table class="table table-striped table-bordered bootstrap-datatable">
                <thead>
                <tr>
                    <th>专员</th>
                    <th>物料余额</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                    <#list dataMap.dataList as list>
                        <tr>
                            <td>${list.userName!''}</td>
                            <td>${list.userBalance!''}</td>
                            <td>
                                <#if list.userId??>
                                    <a href="javascript:;" class="btn btn-primary changeBalance" data-balance="${list.userBalance!''}" data-userid="${list.userId!''}">改余额</a>
                                </#if>
                                <#if list.id??>
                                    <a href="javascript:;" class="btn btn-primary changeBalance" data-balance="${list.userBalance!''}"  data-id="${list.id!''}">改余额</a>
                                    <a href="javascript:;" class="btn btn-primary material_change_record" data-id="${list.id!''}">查明细</a>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
<div id="editDepInfo_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title changeTitle">修改物料费用余额</h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span3">
                            <label for="">物料余额:</label>
                        </div>
                        <div id="changeMoney" class="span8"></div>
                    </div>
                </div>
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span3">
                            <label for="">操作:</label>
                        </div>
                        <div class="span8">
                            <select name="" id="changeBudget">
                                <option value="" data-info="1">增加余额</option>
                                <option value="" data-info="2">减少余额</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span3">
                            <label for="">金额</label>
                        </div>
                        <div id="changeSum" class="span8">
                            <input type="number">
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span3">
                            <label for="">调整原因</label>
                        </div>
                        <div id="changeReason" class="span8">
                            <textarea name="" id="" cols="30" rows="5"></textarea>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="editDepSubmitBtn" type="button" class="btn btn-primary" data-id="">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="apply_history02" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>余额变动记录</h3>
    </div>
    <div class="modal-body02" style="max-height: 500px;overflow-y: scroll;"></div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
    </div>
</div>

<script type="text/html" id="alertBox02">
    <div id="evaluate_table">
        <table class="table table-striped table-bordered bootstrap-datatable">
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
                <td class="center sorting_1"><%=item.createTime%></td>
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
    function getParam(name) {
        return location.href.match(new RegExp('[?#&]' + name + '=([^?#&]+)', 'i')) ? RegExp.$1 : '';
    }
    $(function () {
        var schoolTerm = decodeURIComponent(getParam('schoolTerm'));
        var id = '';
        var userId = '';
        $(document).on("click",".changeBalance",function () {
            var balance = $(this).data("balance");
            id = $(this).data("id");
            userId = $(this).data("userid");
            $("#changeReason textarea").val('');
            $("#changeSum input").val('');
            $('#changeMoney').html(balance);
            $("#editDepInfo_dialog").modal('show');
            url = "change_user_balance.vpage";
        });

        var changeAjaxFn = function (url,data) {
            $.post(url,data,function (res) {
                if(res.success){
                    layer.alert("修改成功",function () {
                        window.location.reload();
                    });
                    $("#editDepInfo_dialog").modal('hide');
                }else{
                    layer.alert(res.info);
                }
            })
        };
        $(document).on("click","#editDepSubmitBtn",function () {
            if($("#changeReason textarea").val() == ""){
                layer.alert("修改原因不能为空");
                return false;
            }
            if($("#changeSum input").val() == ""){
                layer.alert("调整金额不能为空");
                return false;
            }
            if(!(/^([1-9][0-9]*(\.[0-9]{1,2})?|0\.[0-9]{1,2})$/.test($("#changeSum input").val()))){
                layer.alert("请填写正确的金额(大于0，小数点后两位)");
                return false;
            }
            var data = {
                modifyType:$("#changeBudget option:selected").data("info"),
                modifyReason:$("#changeReason textarea").val(),
                modifyCount:$("#changeSum input").val()
            };
            if(id){
                data.id = id;
            }
            if(userId){
                data.userId = userId;
                data.schoolTerm = schoolTerm;
            }
            changeAjaxFn(url,data);
        });

        $(document).on("click",".material_change_record",function () {
            var budgetId = $(this).data("id");
            $.get("material_change_record.vpage?budgetId="+budgetId,function(res){
                if(res.success){
                    for(var i =0;i< res.budgetChangeRecords.length;i++){
                        res.budgetChangeRecords[i].createTime = new Date(res.budgetChangeRecords[i].createTime).Format("yyyyMMdd");
                    }
                    $(".modal-body02").html(template("alertBox02",{res:res.budgetChangeRecords}));
                    $("#apply_history02").modal("show");
                }
            })
        });
    });
</script>
</@layout_default.page>
