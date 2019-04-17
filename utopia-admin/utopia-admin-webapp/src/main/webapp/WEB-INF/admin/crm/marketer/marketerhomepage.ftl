<#-- @ftlvariable name="recordTypeList" type="java.util.List<com.voxlearning.alps.annotation.common.KeyValuePair>" -->
<#-- @ftlvariable name="marketerInfo" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default/>
<#import "marketerquery.ftl" as marketerQuery/>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <@marketerQuery.queryPage/><br/>
    <legend>市场人员主页</legend>

    <ul class="inline">
        <li>
            <button id="resetPassword_btn" class="btn">重置密码</button>
        </li>
        <li>
            <button id="changeName_btn" class="btn">修改姓名</button>
        </li>
    </ul>

    <#--用户信息-->
    <div>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th style="width: 150px;">创建时间</th>
                <th>用户ID</th>
                <th>用户姓名</th>
            </tr>
            <tr>
                <td>${(marketerInfo.createTime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                <td>${marketerInfo.marketerId!}</td>
                <td id="marketerName_td">${marketerInfo.marketerName!}</td>
            </tr>
        </table>
    </div>

    <#--用户备注-->
    <div>
        <fieldset><legend>用户备注</legend></fieldset>
        <ul class="inline">
            <li>
                <button id="addRecord_btn" class="btn">新增备注</button>
            </li>
            <li>
                <button id="hideRecord_btn" class="btn">隐藏备注</button>
            </li>
        </ul>
        <table id="customerServiceRecord_table" class="table table-hover table-striped table-bordered">
            <tr id="recordFirstRow_tr" >
                <th>用户ID</th>
                <th>添加人</th>
                <th>创建时间</th>
                <th>问题描述</th>
                <th>所做操作</th>
                <th>类型</th>
            </tr>
            <#list marketerInfo.customerServiceRecordList as record >
                <tr>
                    <td>${record.userId!""}</td>
                    <td>${record.adminUserName!""}</td>
                    <td>${record.createDatetime!""}</td>
                    <td style="width: 150px;">${record.questionDesc!""}</td>
                    <td style="width: 150px;">${record.operation!""}</td>
                    <td>${record.recordType!""}</td>
                </tr>
            </#list>
        </table>
        <br/>
        <br/>
    </div>

    <#--dialog 增加用户备注-->
    <div id="addRecord_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>用户进线记录</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <dt>用户ID</dt>
                <dd>${marketerInfo.marketerId}</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>记录类型</dt>
                <dd>
                    <label><select name="recordType" id="recordType_select" class="multiple">
                        <#if recordTypeList?has_content>
                            <#list recordTypeList as recordType>
                                <option value='${recordType.key}'>${recordType.value}</option>
                            </#list>
                        </#if>
                    </select></label>
                </dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>问题描述</dt>
                <dd><label><textarea id="addRecordDesc_textarea" name="questionDesc" cols="35" rows="3"></textarea></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>所做操作</dt>
                <dd><label><textarea id="addRecordOperation_textarea" name="operation" cols="35" rows="3"></textarea></label></dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="addRecord_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <#--dialog 更改用户姓名-->
    <div id="changeUsername_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>更改用户姓名</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <dt>用户ID</dt>
                <dd>${marketerInfo.marketerId}</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>记录类型</dt>
                <dd>市场人员操作</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>新的姓名</dt>
                <dd><label><input id="newUsername_input" type="text" placeholder="名字中只能使用汉字"/></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>问题描述</dt>
                <dd><label><textarea id="changeUsernameDesc_textarea" cols="35" rows="5"></textarea></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>所做操作</dt>
                <dd>更改用户名字。</dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="changeUsername_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <#--dialog 重置密码-->
    <div id="resetPassword_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>重置密码</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <dt>用户ID</dt>
                <dd>${marketerInfo.marketerId}</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>记录类型</dt>
                <dd>市场人员操作</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>新的密码</dt>
                <dd><label><input id="newPassword_input" type="text" value="123456"/></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>问题描述</dt>
                <dd><label><textarea id="passwordDesc_textarea" cols="35" rows="2"></textarea></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>所做操作</dt>
                <dd>重置用户密码。</dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="resetPassword_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

</div>
<script>
    $(function() {

        function appendNewRecord(data){
            var record = "<tr>" +
                    "<td>" + data.customerServiceRecord.userId + "</td>" +
                    "<td>" + data.customerServiceRecord.adminUserName + "</td>" +
                    "<td>" + data.createTime + "</td> " +
                    "<td>" + data.customerServiceRecord.questionDesc + "</td> " +
                    "<td>" + data.customerServiceRecord.operation + "</td> " +
                    "<td>" + data.customerServiceRecord.recordType + "</td>  " +
                    "</tr> ";
            $("#recordFirstRow_tr").after(record);
        }

        $('#addRecord_btn').click(function() {
            $('#recordType_select').val(7);
            $('#addRecordDesc_textarea').val('');
            $('#addRecordOperation_textarea').val('');
            $('#addRecord_dialog').modal('show');
        });

        $('#addRecord_dialog_btn').click(function() {
            var queryUrl = "../user/addcustomerrecord.vpage";
            var postData = {
                userId : ${marketerInfo.marketerId},
                recordType : $("#recordType_select").val(),
                questionDesc : $("#addRecordDesc_textarea").val(),
                operation : $("#addRecordOperation_textarea").val()
            };
            $.post(queryUrl, postData, function(data) {
                if(data.success) {
                    appendNewRecord(data);
                    $("#addRecord_dialog").modal("hide");
                } else {
                    alert(data.info);
                }
            });
        });

        $('#hideRecord_btn').click(function() {
            $("#customerServiceRecord_table").toggle();
            var $this = $(this);
            switch($this.html()){
                case "隐藏备注":
                    $this.html("显示备注");
                    break;
                case "显示备注":
                    $this.html("隐藏备注");
                    break;
            }
        });

        $('#resetPassword_btn').on('click', function() {
            $("#newPassword_input").val("123456");
            $('#passwordDesc_textarea').val('');
            $("#resetPassword_dialog").modal("show");
        });

        $("#resetPassword_dialog_btn").click(function(){
            var queryUrl = '../user/resetpassword.vpage';
            var postData = {
                userId : ${marketerInfo.marketerId},
                password : $("#newPassword_input").val(),
                passwordDesc : $('#passwordDesc_textarea').val()
            };

            $.post(queryUrl, postData, function(data) {
                if(data.success){
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        $('#changeName_btn').click(function() {
            $('#newUsername_input').val('');
            $('#changeUsernameDesc_textarea').val('');
            $("#changeUsername_dialog").modal("show");
        });

        $("#changeUsername_dialog_btn").on("click", function(){
            var queryUrl = "../user/updateusername.vpage";
            var postData = {
                userId : ${marketerInfo.marketerId},
                userName : $("#newUsername_input").val(),
                nameDesc : $('#changeUsernameDesc_textarea').val()
            };
            $.post(queryUrl, postData, function(data) {
                alert(data.info);
                if(data.success){
                    appendNewRecord(data);
                    $("#marketerName_td").html(data.userName);
                    $("#changeUsername_dialog").modal("hide");
                }
            });
        });

    });
</script>
</@layout_default.page>