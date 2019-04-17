<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=12>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在上传，请等待……</p>
</div>
<div id="main_container" class="span9">
    <legend>快递单管理</legend>
    <div class="row-fluid">
        <div class="span12">
                快递单ID：  <input name="logisticId" id="logisticId" type="text" value="${logisticId!}"/>
                物流单号：   <input name="logisticNo" id="logisticNo" type="text" value="${logisticNo!}"/>
                是否导回：<select id="isBack" name="isBack">
                            <option value="">全部</option>
                            <option value="true">是</option>
                            <option value="false">否</option>
                        </select>
                生成月份： <input id="month" class="input-medium" type="text" placeholder="201601">
                <br/><br/>
                <a id="selectOrder"  role="button" class="btn btn-primary">查询</a>
                <a id="logisticsExport" href="downloadlogistics.vpage" role="button" class="btn btn-inverse">导出快递单（每月1日上午11点以后导出）</a>
                <br/><br/>
                <form id="importLogistic" method="post" enctype="multipart/form-data"
                      action="importlogisticexcel.vpage" data-ajax="false"
                      class="form-horizontal">
                    <input id="sourceFile" name="sourceFile" type="file">
                    <a id="backImport" href="javascript:iSave();"  role="button" class="btn btn-warning">导回快递单（每次最多2W条）</a>
                </form>
            </div>
        </div>
    <div>
        <fieldset>
            <div id="logistic_list_chip"></div>
        </fieldset>
    </div>

    <div id="errorList"></div>
</div>



    <script type="text/javascript">
        function iSave(){
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

            var formElement = document.getElementById("importLogistic");
            var postData = new FormData(formElement);

            $("#loadingDiv").show();

            $.ajax({
                url: "importlogisticexcel.vpage",
                type: "POST",
                data: postData,
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
                success: function (res) {
                    $("#loadingDiv").hide();
                    if(res.success){
                        if(res.errorList.length > 0){
                            var _html = "<table class='table table-bordered table-striped table-hover'>";
                            for(var i = 0, errorList = res.errorList; i < errorList.length; i++){
                                _html += "<tr><td>"+ errorList[i] +"</td></tr>"
                            }
                            _html += "</table>";
                            $("#errorList").html(_html);
                        }else{
                            $("#errorList").html("<label style='color: red'>导入成功</label>");
                        }
                    }else{
                        alert(data.info);
                    }
                },
                error: function (e) {
                    console.log(e);
                    $("#loadingDiv").hide();
                }
            });
        }

        $(function() {
            $("#month").datepicker({
                dateFormat      : 'yymm',  //日期格式，自己设置
                monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate     : new Date(),
                numberOfMonths  : 1,
                changeMonth: false,
                changeYear: false,
                onSelect : function (selectedDate){}
            });

            $('#selectOrder').on('click', function() {
                $('#logistic_list_chip').load('getlogisticlist.vpage',
                        {   logisticId : $('#logisticId').val(),
                            logisticNo: $("#logisticNo").val(),
                            isBack: $("#isBack").val(),
                            month: $("#month").val()
                        }
                );
            });
        });
    </script>
</@layout_default.page>