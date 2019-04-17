<#macro recordList customerServiceRecordList userId defaultType=0>
    <legend>用户备注</legend>
    <ul class="inline">
        <li>
            <button class="btn" onclick="addCustomerServiceRecord()">新增备注</button>
        </li>
        <li>
            <button id="hide_record_btn" class="btn" onclick="hideCustomerServiceRecord()">隐藏备注</button>
        </li>
    </ul>
    <table id="customer_service_record" class="table table-hover table-striped table-bordered">
        <tr id="comment_title">
            <th>用户ID</th>
            <th>添加人</th>
            <th>创建时间</th>
            <th style="width=150px;">操作内容</th>
            <th style="width=150px;">备注</th>
            <th style="width=100px;">类型</th>
        </tr>
    <#list customerServiceRecordList as record >
        <tr>
            <td style="width: 6em;">${record.userId!""}</td>
            <td style="width: 6em;">${record.operatorId!""}</td>
            <td style="width: 10em;">${record.createTime!""}</td>
            <td style="width: 10em;">${record.operationContent!""}</td>
            <td style="width: 20em;">${record.comments!""}</td>
            <td style="width: 6em;">${record.operationType!""}</td>
        </tr>
    </#list>
    </table>
    <br/><br/>

    <div id="record_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>用户进线记录</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${userId!}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>
                            <select name="recordType" id="recordType" class="multiple">
                                <#if recordTypeList?has_content>
                                    <#list recordTypeList as recordType>
                                        <option value='${recordType.key}'>${recordType.value}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="questionDesc" name="questionDesc" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd><textarea id="operation" name="operation" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="create_record_dialog" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <script>
        function addCustomerServiceRecord(){
            $("#questionDesc").val('');
            $("#operation").val('');
            $('#recordType').val('${defaultType}');
            $("#record_dialog").modal("show");
        }

        function hideCustomerServiceRecord(){
            $("#customer_service_record").toggle(function(){
                var $target = $("#hide_record_btn");
                switch($target.html()){
                    case "隐藏备注":
                        $target.html("显示备注");
                        break;
                    case "显示备注":
                        $target.html("隐藏备注");
                        break;
                }
            });
        }

        function appendNewRecord(data){
            var record = "<tr>" +
                    "<td>" + data.customerServiceRecord.userId + "</td>" +
                    "<td>" + data.customerServiceRecord.operatorId + "</td>" +
                    "<td>" + data.createTime + "</td> " +
                    "<td>" + data.customerServiceRecord.operationContent + "</td> " +
                    "<td>" + data.customerServiceRecord.comments + "</td> " +
                    "<td>" + data.customerServiceRecord.operationType + "</td>  " +
                    "</tr> ";

            $("#comment_title").after(record);
        }

        $(function() {
            $("#create_record_dialog").on("click", function(){
                var queryUrl = "../user/addcustomerrecord.vpage";
                $.ajax({
                    type: "post",
                    url: queryUrl,
                    data: {
                        userId : ${userId},
                        recordType : $("#recordType").val(),
                        questionDesc : $("#questionDesc").val(),
                        operation : $("#operation").val()
                    },
                    success: function (data){
                        if (data.success){
                            appendNewRecord(data);
                        }else{
                            alert("增加日志失败。");
                        }
                        $("#record_dialog").modal("hide");
                    }
                });
            });

        });
    </script>
</#macro>