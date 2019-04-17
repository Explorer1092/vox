<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='费用管理' page_num=14>
<style>
    body .form-horizontal .control-group{
        display: inline-block;
        width:auto;
    }
</style>
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
            <h2><i class="icon-th"></i> 费用管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content ">
            <div class="box-content">
                <a href="javascript:;" class="btn btn-primary">城市费用</a>
                <a href="budget.vpage?type=material" class="btn">物料费用</a>
            </div>
            <@apptag.pageElement elementCode="3c00ba515b4940bb">
                <div class="pull-right" style="margin-bottom:15px">
                    <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                          action="/sysconfig/schooldic/bulkImportSchoolDictInfo.vpage" data-ajax="false"
                          class="form-horizontal">
                        <div class="control-group">
                            <div>
                                <input type="radio" name="templateType" value="1" style="margin-left: 0" checked>城市费用
                                <input type="radio" name="templateType" style="margin-left: 0" value="2">城市余额
                            </div>
                            <div class="controls" style="margin-left: 0;">
                                <input id="sourceFile" name="sourceFile" type="file">
                                <a href="javascript:;" onclick="isSave()" class="btn btn-primary">导入预算</a>
                                &nbsp;&nbsp;&nbsp;&nbsp;
                                <a href="javascript:;" class="btn btn-primary downBtn">下载导入模版</a>
                            </div>
                        </div>
                    </form>
                </div>
            </@apptag.pageElement>
            <form class="form-horizontal" style="clear:both;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">部门</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="groupName" name="groupName">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">城市</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="cityName" name="groupName">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">起始月：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="beginMonth" name="beginMonth" value="${.now?string("yyyyMM")}">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">截止月：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="endMonth" name="endMonth" value="${.now?string("yyyyMM")}">
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <button type="button" id="search_btn" class="btn btn-success">查询</button>
                            <button type="button" id="export" class="btn btn-success">导出</button>
                        </div>
                    </div>
                </fieldset>
            </form>
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid" style="margin-top:30px">
                <table  id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">月份</th>
                        <th class="sorting" style="width: 90px;">分区</th>
                        <th class="sorting" style="width: 60px;">城市</th>
                        <th class="sorting" style="width: 100px;">城市级别</th>
                        <th class="sorting" style="width: 60px;">市经理</th>
                        <th class="sorting" style="width: 75px;">城市经费预算</th>
                        <th class="sorting" style="width: 145px;">城市经费余额</th>
                        <th class="sorting" style="width: 60px;">部门状态</th>
                        <th class="sorting" style="width: 75px;">业务类型</th>
                        <th class="sorting" style="width: 75px;">部门失效时间</th>
                        <th class="sorting" style="width: 145px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

    </div>
</div>
<div id="editDepInfo_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title changeTitle"></h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span3">
                            <label for="">城市支持费用:</label>
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
                                <option value="" data-info="1">增加预算</option>
                                <option value="" data-info="2">减少预算</option>
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
                            <textarea name="" id="" cols="30" rows="10"></textarea>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary" data-id="">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
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
        <table>
            <thead>
            <tr>
                <th class="sorting" style="width: 100px;">日期</th>
                <th class="sorting" style="width: 100px;">操作人</th>
                <th class="sorting" style="width: 100px;">操作类型</th>
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
                <td class="center sorting_1">
                    <%if(item.recordType == 1){%>修改预算<%}%>
                    <%if(item.recordType == 2){%>修改余额<%}%>
                </td>
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
<script id="showChangeList" type="text/html">
    <%if(res){%>
    <%for(var i=0;i< res.length;i++){%>
    <%var item = res[i]%>
    <tr class="odd">
        <td class="center sorting_1"><%=item.createTime%></td>
        <td class="center sorting_1"><%=item.operatorName%></td>
        <td class="center sorting_1"><%=item.quantity%></td>
        <td class="center sorting_1"><%=item.comment%></td>
        <td class="center sorting_1"><%=item.preCash%></td>
        <td class="center sorting_1"><%=item.afterCash%></td>
    </tr>
    <%}%>
    <%}%>
</script>
<script id="groupList" type="text/html">
    <%if(res){%>
    <%for(var i=0;i< res.length;i++){%>
    <%var item = res[i]%>
    <tr class="odd">
        <td class="center"><%=item.month%></td>
        <td class="center"><%=item.groupName%></td>
        <td class="center"><%=item.regionName%></td>
        <td class="center"><%=item.regionLevel%></td>
        <td class="center"><%=item.cityManager%></td>
        <td class="center"><%=item.budget%></td>
        <td class="center"><%=item.balance%></td>
        <td class="center ">
            <span class="changeBudget" style="cursor:pointer;color:blue" data-budget="<%=item.budget%>" data-id="<%=item.id%>">修改预算</span>
            <span class="changeBalance" style="cursor:pointer;color:blue" data-balance="<%=item.balance%>" data-id="<%=item.id%>">修改余额</span>
            <span class="material_change_record" style="cursor:pointer;color:blue" data-id="<%=item.id%>">变更记录</span>
        </td>
    </tr>
    <%}%>
    <%}%>
</script>
<script type="text/javascript">
    template.helper('Date', Date);
    $('#groupName').autocomplete({
        delay :600,
        source:function(request,response){
            if(!request.term||request.term.trim()==''){
                return;
            }
            $.get("/materialbudget/budget/search_group.vpage",{groupKey: request.term},function(result){
                response( $.map( result.dataList, function( item ) {
                    return {
                        label: item.groupName,
                        value: item.groupName,
                        id: item.id
                    }
                }));
            });
        },
        select: function( event, ui ) {
            $('#groupName').val(ui.item.value);
        }
    });
    $('#cityName').autocomplete({
        delay :600,
        source:function(request,response){
            if(!request.term||request.term.trim()==''){
                return;
            }
            $.get("search_city.vpage",{cityKey: request.term},function(result){
                response( $.map( result.dataList, function( item ) {
                    return {
                        label: item.cityName,
                        value: item.cityName,
                        id: item.id
                    }
                }));
            });
        },
        select: function( event, ui ) {
            $('#cityName').val(ui.item.value);
        }
    });
    $(function () {
        $("#beginMonth,#endMonth").datepicker({
            dateFormat      : 'yymm',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false
        });
    });
    function isSave() {
        $("div.alert-info").hide();
        $("div.alert-error").hide();
        var sourceFile = $("#sourceFile").val();
        if (blankString(sourceFile)) {
            alert("请上传excel！");
            return;
        }
        var fileParts = sourceFile.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt != "xls" && fileExt != "xlsx") {
            alert("请上传正确格式的excel！");
            return;
        }

        var formElement = document.getElementById("importSchoolDict");
        var postData = new FormData(formElement);

        $("#loadingDiv").show();
        postData.templateType = $('input[name="templateType"]:checked').val();
        var tip_string = '城市费用';
        if(postData.templateType == 2){
            tip_string = '城市余额';
        }
        $.ajax({
            url: "importCityBudget.vpage",
            type: "POST",
            data: postData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function (res) {
                $("#loadingDiv").hide();
                if (res.success) {
                    layer.alert("上传成功，本次上传共计新增"+ res.insertCount +"条"+tip_string+"，更新"+res.updateCount + "条"+tip_string,function () {
                        window.location.reload();
                    });
                } else {
                    var error = res.errorList;
                    setInfo(error, "alert-error", "error-panel");
                }
            },
            error: function (e) {
                console.log(e);
                $("#loadingDiv").hide();
            }
        });
    }
    function setInfo(info, classEle, idEle) {
        resInfo = getInfo(info);
        if (resInfo) {
            $("div." + classEle).show();
            $("#" + idEle).html(resInfo);
        }
    }

    function getInfoNoBr(info) {
        if (info) {
            var res = "";
            info.forEach(function (e) {
                res += (e + ",");
            });
            return res;
        }
        return false;
    }

    function getInfo(info) {
        if (info) {
            var res = "";
            info.forEach(function (e) {
                res += (e + "<br/>");
            });
            return res;
        }
        return false;
    }
    $(document).on("click","#export",function () {
        window.location.href = "exportCityBudget.vpage?groupName="+$("#groupName").val()+"&cityName="+$("#cityName").val()+"&beginMonth="+$("#beginMonth").val()+"&endMonth="+$("#endMonth").val();
    });
    $(document).on("click","#search_btn",function () {
        var dataObj = {
            groupName:$("#groupName").val(),
            cityName:$("#cityName").val(),
            beginMonth:$("#beginMonth").val(),
            endMonth:$("#endMonth").val()
        };
        $.get("city.vpage",dataObj,function (res) {
            if(res.success){
                /*$(".itemLength").html(res.budgetList.length);*/
                var dataTableList = [];
                for(var i = 0;i< res.budgetList.length;i++) {
                    var item = res.budgetList[i];
                    var operator = '<span class="changeBudget" style="cursor:pointer;color:blue" data-budget="'+ item.budget +'" data-id="' + item.id + '">修改预算</span>'
                            +'<span class="changeBalance" style="cursor:pointer;color:blue;margin-left: 5px;" data-balance="'+ item.balance +'" data-id="'+ item.id+'">修改余额</span>'
                            +'<span class="material_change_record" style="cursor:pointer;color:blue;margin-left: 5px;" data-id="'+ item.id +'">变更记录</span>';
                    var arr = [item.month, item.groupName, item.regionName, item.regionLevel,item.cityManager, item.budget, item.balance,item.groupStatus,item.serviceType,item.groupDisableTime, operator];
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
        })
    });
    $("#search_btn").click();
    var changeGroupId = 0;
    var url = "";
    $(document).on("click",".changeBudget",function () {
        $("#changeReason textarea").val('');
        $("#changeSum input").val('');
        $("#editDepInfo_dialog").modal('show');
        $("#changeBudget option").eq(0).html("增加预算");
        $("#changeBudget option").eq(1).html("减少预算");
        var budget = $(this).data("budget");
        $('.changeTitle').html('修改城市费用预算');
        $('#changeMoney').html(budget);
        changeGroupId = $(this).data("id");
        url = "changeBudget.vpage";
    });
    $(document).on("click",".changeBalance",function () {
        $("#changeReason textarea").val('');
        $("#changeSum input").val('');
        $("#editDepInfo_dialog").modal('show');
        $("#changeBudget option").eq(0).html("增加余额");
        $("#changeBudget option").eq(1).html("减少余额");
        var balance = $(this).data("balance");
        $('.changeTitle').html('修改城市费用余额');
        $('#changeMoney').html(balance);
        changeGroupId = $(this).data("id");
        url = "changeBalance.vpage";
    });
    var changeAjaxFn = function (url,data) {
        $.post(url,data,function (res) {
            if(res.success){
                alert("修改成功");
                $("#editDepInfo_dialog").modal('hide');
                $("#search_btn").click();
            }else{
                alert(res.info);
            }
        })
    };
    $(document).on("click","#editDepSubmitBtn",function () {
        if($("#changeReason textarea").val() == ""){
            alert("修改原因不能为空");
            return false;
        }
        if($("#changeSum input").val() == ""){
            alert("调整金额不能为空");
            return false;
        }
        var data = {
            id:changeGroupId,
            modifyType:$("#changeBudget option:selected").data("info"),
            modifyReason:$("#changeReason textarea").val(),
            modifyCount:$("#changeSum input").val()
        };
        changeAjaxFn(url,data);
    });
    $(document).on("click",".material_change_record",function () {
        var budgetId = $(this).data("id");
        $.get("material_change_record.vpage?budgetId="+budgetId,function(res){
            if(res.success){
                for(var i =0;i< res.budgetChangeRecords.length;i++){
                    res.budgetChangeRecords[i].createTime = new Date(res.budgetChangeRecords[i].createTime).Format("yyyy-MM-dd hh:mm:ss");
                }
                $(".modal-body02").html(template("alertBox02",{res:res.budgetChangeRecords}));
                $("#apply_history02").modal("show");
            }
        })
    });
    //下载导入模板
    $('.downBtn').on('click',function () {
        window.location.href='download_import_city_budget_template.vpage?templateType=' + $('input[name="templateType"]:checked').val();
    });
</script>
</@layout_default.page>
