<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='城市等级维护' page_num=6>
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
            <h2><i class="icon-th"></i> 城市等级维护</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content ">
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right" style="margin-bottom:15px">
                    <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                          action="/sysconfig/citylevel/import_data.vpage" data-ajax="false"
                          class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">上传excel</label>
                            <div class="controls">
                                <input id="sourceFile" name="sourceExcelFile" type="file">
                                <a href="javascript:;" onclick="isSave()" class="btn btn-primary">批量导入</a>
                                &nbsp;&nbsp;&nbsp;&nbsp;
                                <a href="export_data.vpage" class="btn btn-primary">下载导入模版</a>
                                <a href="export_data.vpage?includeLevelData=true" class="btn btn-primary">导出</a>
                            </div>
                        </div>
                    </form>
                </div>
            </#if>
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
                <table id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">城市名称</th>
                        <th class="sorting" style="width: 90px;">行政代码</th>
                        <th class="sorting" style="width: 60px;">级别（S/A/B/C/空）</th>
                        <th class="sorting" style="width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if cityList?? && cityList?size gt 0>
                            <#list cityList as list>
                                <tr class="odd">
                                    <td class="center">${list.cityName!""}</td>
                                    <td class="center">${list.cityCode!""}</td>
                                    <td class="center">${list.level!""}</td>
                                    <td class="center changeCityInfo" data-code="${list.cityCode!""}" data-name="${list.cityName!""}" style="color: #00a0e9;cursor: pointer;">编辑</td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
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

<script id="cityInfo" type="text/html">
    <%if(res){%>
        <div class="control-group">
            <div class="row-fluid">
                <div class="span3">
                    <label for="">城市:</label>
                </div>
                <div id="changeCityName" class="span8"><%=res.cityName%></div>
            </div>
        </div>
        <div class="control-group">
            <div class="row-fluid">
                <div class="span3">
                    <label for="">行政代码:</label>
                </div>
                <div id="changeCityCode" class="span8"><%=res.cityCode%></div>
            </div>
        </div>
        <div class="control-group">
            <div class="row-fluid">
                <div class="span3">
                    <label for="">操作:</label>
                </div>
                <div class="span8">
                    <select name="" id="changeCityLevel">
                        <option value=""></option>
                        <%for (var j in res.levelList){%>
                            <option value="" data-info="<%=j%>" <%if(j == res.level){%>selected<%}%> >
                                <%=res.levelList[j]%>
                            </option>
                        <%}%>
                    </select>
                </div>
            </div>
        </div>
    <%}%>
</script>
<script type="text/javascript">
    template.helper('Date', Date);
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

        $.ajax({
            url: "import_data.vpage",
            type: "POST",
            data: postData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function (res) {
                $("#loadingDiv").hide();
                if (res.success) {
                    alert("上传成功");
                    window.location.reload();
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
    var changeGroupId = 0;
    var url = "";
    $(document).on("click",".changeCityInfo",function () {
        $.get("detail.vpage?cityCode="+$(this).data("code"),function (res) {
            if(res.success){
                $('.modal-body').html(template("cityInfo",{res:res}));
            }
        });
        $("#editDepInfo_dialog").modal('show');
        changeGroupId = $(this).data("id");
        url = "changeBudget.vpage";
    });
    $(document).on("click",".changeBalance",function () {
        $("#changeReason textarea").val('');
        $("#changeSum input").val('');
        $("#editDepInfo_dialog").modal('show');
        var balance = $(this).data("balance");
        $('.changeTitle').html('修改物料费用余额');
        $('#changeMoney').html(balance);
        changeGroupId = $(this).data("id");
        url = "changeBalance.vpage";
    });
    var changeAjaxFn = function (url,data) {
        $.post(url,data,function (res) {
            if(res.success){
                alert("修改成功");
                $("#editDepInfo_dialog").modal('hide');
                window.location.reload();
            }else{
                alert(res.info);
            }
        })
    };
    $(document).on("click","#editDepSubmitBtn",function () {
        var data = {
            id:changeGroupId,
            level:$("#changeCityLevel option:selected").data("info"),
            cityCode:$("#changeCityCode").html(),
            cityName:$("#changeCityName").html()
        };
        changeAjaxFn("update.vpage",data);
    });

</script>
</@layout_default.page>
