<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='专员绩效分组' page_num=6>
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
            <h2><i class="icon-th"></i> 专员绩效分组</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content ">
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right" style="margin-bottom:15px">
                    <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                          action="/sysconfig/performance_group/download_template.vpage" data-ajax="false"
                          class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">上传excel</label>
                            <div class="controls">
                                <input id="sourceFile" name="sourceExcelFile" type="file">
                                <a href="javascript:;" onclick="isSave()" class="btn btn-primary">批量导入</a>
                                &nbsp;&nbsp;&nbsp;&nbsp;
                                <a href="download_template.vpage" class="btn btn-primary">下载导入模版</a>
                            </div>
                        </div>
                    </form>
                </div>
            </#if>

            <form method="GET" id="school_every_scan_form" class="form-horizontal" style="clear: both;">
                <ul class="row-fluid">
                    <li class="span3" style="width:20%">
                        <div class="control-group">
                            <label class="control-label" style="width:90px">选择日期</label>
                            <div class="controls" style="margin-left:100px">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="schoolDate"
                                       name="month" value="${date!}">
                            </div>
                        </div>
                    </li>
                    <li class="span6" style="width:20%;margin-left:10%">
                        <a href="javascript:void(0);" class="btn btn-primary" id="schoolExportBtn"
                           target="_blank">下载</a>
                    </li>
                    <input type="hidden" value="${schoolType!''}" name="type">
                </ul>
            </form>

            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
                <table id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">月份</th>
                        <th class="sorting" style="width: 90px;">姓名</th>
                        <th class="sorting" style="width: 60px;">部门</th>
                        <th class="sorting" style="width: 100px;">绩效分组</th>
                        <th class="sorting" style="width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if dataList?? && dataList?size gt 0>
                            <#list dataList as list>
                            <tr class="odd">
                                <td class="center">${list.month!""}</td>
                                <td class="center">${list.userName!""}</td>
                                <td class="center">${list.groupName!""}</td>
                                <td class="center">
                                    <#if list.performanceGroupType?has_content>
                                        ${list.performanceGroupType.desc!""}
                                    </#if>
                                </td>
                                <td class="center changeCityInfo" data-month= "${list.month!""}" data-userid="${list.userId!""}" style="color: #00a0e9;cursor: pointer;">编辑</td>
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
                <label for="">月份:</label>
            </div>
            <div id="changeCityName" class="span8"><%=res.performanceGroup.month%></div>
        </div>
    </div>
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">姓名:</label>
            </div>
            <div id="changeCityCode" class="span8"><%=res.performanceGroup.userName%></div>
        </div>
    </div>
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">部门:</label>
            </div>
            <div id="changeCityCode" class="span8"><%=res.performanceGroup.groupName%></div>
        </div>
    </div>
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">绩效分组:</label>
            </div>
            <div class="span8">
                <select name="" id="changeCityLevel">
                    <option value=""></option>
                <%for( var i in res.typeList){%>
                     <option value="" data-info="<%=i%>" <%if(i == res.performanceGroup.performanceGroupType){%>selected<%}%> >
                        <%=res.typeList[i]%>
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
    var getParamFromUrl = function(url,name) {
        var reg = new RegExp("[&?#]" + name + "=([^&?#]+)", "i");
        var r = url.match(reg);
        if (r != null) return unescape(r[1]); return null;
    };
    var getUrlParam = function(name) {
        return this.getParamFromUrl(window.location.href,name);
    };
    $("#schoolDate").val(getUrlParam("month"));
    $("#schoolDate").datepicker({
        dateFormat: 'yy-mm',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        onSelect:gotoDate,
        minDate:new Date("2018-01-01")
    });
    function gotoDate(ev) {
        console.log(ev);
        window.location.href = "index.vpage?month=" + ev.replace("-","");
    }
    /*下载*/
    $(document).on("click", "#schoolExportBtn", function () {
        if ($('#schoolDate').val() != '') {
            $("#school_every_scan_form").attr({
                "action": "export_data.vpage",
                "method": "GET"
            });
            var formElement = document.getElementById("school_every_scan_form");
            formElement.submit();
        } else {
            alert('请选择日期')
        }
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

        $.ajax({
            url: "import_data.vpage",
            type: "POST",
            data: postData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function (res) {
                $("#loadingDiv").hide();
                console.log(res);
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
    var month = 0;
    var userId = 0;
    var url = "";
    $(document).on("click",".changeCityInfo",function () {
        $.get("detail.vpage?month="+$(this).data("month") +"&userId="+$(this).data("userid"),function (res) {
            if(res.success){
                $('.modal-body').html(template("cityInfo",{res:res}));
            }
        });
        $("#editDepInfo_dialog").modal('show');
        month = $(this).data("month");
        userId = $(this).data("userid");
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
            month:month,
            userId:userId,
            type:$("#changeCityLevel option:selected").data("info")
        };
        changeAjaxFn("update.vpage",data);
    });

</script>
</@layout_default.page>
