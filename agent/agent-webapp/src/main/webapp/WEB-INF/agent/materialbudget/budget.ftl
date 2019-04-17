<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='费用管理' page_num=14>
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
                <a href="budget.vpage?type=city" class="btn ">城市费用</a>
                <a href="javascript:;" class="btn btn-primary">物料费用</a>

            </div>
            <div style="clear:both;overflow:hidden">
                <ul class="inline" style="float:left;text-decoration: none;display: block;line-height:30px;height: 30px;">
                    <li style="display: inline-block;">
                        <label for="regionCode" style="text-align:left">部门：
                                <input id="groupName" type="text">
                            共有 <span class="itemLength">0</span>条
                        </label>
                    </li>
                    <li style=" display: inline-block;">
                        <input type="button" id="search" style="margin-top:-10px" value="查询"/>
                        <input type="button" id="export" style="margin:-10px 0 0 10px;" value="导出"/>
                    </li>
                </ul>
                <@apptag.pageElement elementCode="3c00ba515b4940bb">
                <div class="pull-right">
                    <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                          action="/materialbudget/budget/ importMaterialCost.vpage" data-ajax="false"
                          class="form-horizontal">
                        <div class="control-group">
                            <div>
                                <input type="radio" name="templateType" value="1" style="margin-left: 0" checked>物料费用
                                <input type="radio" name="templateType" value="2" style="margin-left: 0" >物料余额
                            </div>
                            <div class="controls" style="margin-left: 0;">
                                <input id="sourceFile" name="sourceFile" type="file">
                                <a href="javascript:;" onclick="isSave()" class="btn btn-primary">导入预算</a>
                                <a href="javascript:;" class="btn btn-primary downBtn">下载导入模版</a>
                            </div>
                        </div>
                    </form>
                </div>
                </@apptag.pageElement>
            </div>
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table  id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">学期</th>
                        <th class="sorting" style="width: 90px;">部门</th>
                        <th class="sorting" style="width: 60px;">部门物料预算</th>
                        <th class="sorting" style="width: 100px;">部门物料余额</th>
                        <th class="sorting" style="width: 100px;">部门状态</th>
                        <th class="sorting" style="width: 100px;">业务类型</th>
                        <th class="sorting" style="width: 100px;">部门失效时间</th>
                        <th class="sorting" style="width: 50px;">操作</th>
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
<script type="text/javascript">
    template.helper('Date', Date);
    $('#groupName').autocomplete({
        delay :600,
        source:function(request,response){
            if(!request.term||request.term.trim()==''){
                return;
            }
            $.get("search_group.vpage",{groupKey: request.term},function(result){
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
    //下载导入模板
    $('.downBtn').on('click',function () {
       window.location.href='download_import_material_cost_template.vpage?templateType=' + $('input[name="templateType"]:checked').val();
    });
    function isSave() {
        $("div.alert-info").hide();
        $("div.alert-error").hide();
        var sourceFile = $("#sourceFile").val();
        if (blankString(sourceFile)) {
            layer.alert("请上传excel！");
            return;
        }
        var fileParts = sourceFile.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt != "xls" && fileExt != "xlsx") {
            layer.alert("请上传正确格式的excel！");
            return;
        }

        var formElement = document.getElementById("importSchoolDict");
        var postData = new FormData(formElement);
        postData.templateType = $('input[name="templateType"]:checked').val();
        $("#loadingDiv").show();
        $.ajax({
            url: "importMaterialCost.vpage",
            type: "POST",
            data: postData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function (res) {
                $("#loadingDiv").hide();
                if (res.success) {
                    var info = '';
                    if($('input[name="templateType"]:checked').val() == 1){
                        info = res.dataMap.groupMaterialCostNum +"部门预算";
                    }else{
                        info = res.dataMap.userMaterialCostNum + "个人员余额";
                    }
                    layer.alert("上传成功，本次上传共计更新"+ info,function () {
                        window.location.reload();
                    });
                } else {
                    var error = res.errorInfoList;
                    setInfo(error, "alert-error", "error-panel");
                }
            },
            error: function (e) {
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
        window.location.href = "export_material_cost.vpage?groupName="+$("#groupName").val();
    });
    $(document).on("click","#search",function () {
        var dataObj = {
            groupName:$("#groupName").val()
        };
        $.get("group_material_cost_list.vpage",dataObj,function (res) {
            if(res.success){
                $(".itemLength").html(res.dataList.length);
                var dataTableList = [];
                for(var i = 0;i< res.dataList.length;i++) {
                    var item = res.dataList[i];
                    var operator = '<span class="btn btn-primary changeBudget" style="cursor:pointer;" data-budget="'+ item.budget +'" data-id="' + item.id + '">修改预算</span>'
                    +'<span class="btn btn-primary material_change_record" style="cursor:pointer;margin-left: 5px;" data-term="'+ item.schoolTerm +'" data-id="'+ item.groupId +'">查明细</span>';
                    var arr = [item.schoolTerm, item.groupName, item.budget, item.balance,item.groupStatus,item.serviceType,item.groupDisableTime, operator];
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
    $("#search").click();
    var changeGroupId = 0;
    var url = "";
    $(document).on("click",".changeBudget",function () {
        $("#changeReason textarea").val('');
        $("#changeSum input").val('');
        $("#editDepInfo_dialog").modal('show');
        $("#changeBudget option").eq(0).html("增加预算");
        $("#changeBudget option").eq(1).html("减少预算");
        var budget = $(this).data("budget");
        $('.changeTitle').html('修改物料费用预算');
        $('#changeMoney').html(budget);
        changeGroupId = $(this).data("id");
        url = "change_group_budget.vpage";
    });
    var changeAjaxFn = function (url,data) {
        $.post(url,data,function (res) {
            if(res.success){
                layer.alert("修改成功");
                $("#editDepInfo_dialog").modal('hide');
                $("#search").click();
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
        var data = {
            id:changeGroupId,
            modifyType:$("#changeBudget option:selected").data("info"),
            modifyReason:$("#changeReason textarea").val(),
            modifyCount:$("#changeSum input").val()
        };
        changeAjaxFn(url,data);
    });
    $(document).on("click",".material_change_record",function () {
        var groupId = $(this).data("id");
        var schoolTerm = $(this).data("term");
        window.location.href = 'user_material_cost_list.vpage?groupId='+groupId+'&schoolTerm='+encodeURIComponent(schoolTerm);
    });

</script>
</@layout_default.page>
