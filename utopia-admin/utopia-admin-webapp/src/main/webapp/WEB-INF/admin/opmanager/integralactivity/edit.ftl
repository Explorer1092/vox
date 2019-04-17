<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='积分活动明细' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
    <legend style="font-weight: 700;">
        积分流通活动&nbsp;&nbsp;&nbsp;&nbsp;
        <#if actEditable??>
            <#if finished??>
                <#if !finished>
                    <#if actEditable>
                        <input type="button" id="saveActBtn" class="btn btn-primary" value="保  存"/>
                    <#else>
                        <input type="button" id="editActBtn" class="btn btn-primary" value="编  辑"/>
                    </#if>
                </#if>
            </#if>
        </#if>
        <input type="button" id="return" onclick="window.location.href='activitypage.vpage'" class="btn btn-primary" value="返  回" />
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="upload_form" name="upload_form" class="form-horizontal" method="post" action="saveactivity.vpage" enctype="multipart/form-data">
                <fieldset>
                    <#if error??>
                        <div class="alert alert-error">
                            <button type="button" class="close" data-dismiss="alert">×</button>
                            <strong>出错啦！ ${error!}</strong>
                        </div>
                    </#if>
                    <div class="control-group" style="display:none">
                        <label class="control-label">活动ID</label>
                        <div class="controls">
                            <label class="control-label" id="activityId" name="activityId" style="text-align: left">
                            <#if activity??>${activity.id!}</#if>
                            </label>

                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动名称</label>
                        <div class="controls">
                            <input type="text" id="activityName" name="activityName" <#if activity??> value="${activity.activityName!''}"</#if> <#if !actEditable> disabled</#if>/>
                            <span style="color: red">(必填)</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动创建人</label>
                        <div class="controls">
                            <label class="control-label" id="creatorName" name="creatorName" style="text-align: left">
                            <#if activity??>${activity.creatorName!''}<#else>${(requestContext.getCurrentAdminUser().realName)!}</#if>
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动归属部门</label>
                        <div class="controls">
                            <select id="department" name="department" <#if activity??>disabled</#if>>
                                <#if departmentList??>
                                    <#list departmentList as dept>
                                        <option <#if activity?? && dept.key == activity.department.type> selected="selected" </#if> value="${dept.key!0}" > ${dept.value!}</option>
                                    </#list>
                                </#if>
                                <#--<option <#if activity??><#if activity.department.type==11>selected="selected" </#if></#if> value=11>产品部-平台</option>-->
                                <#--<option <#if activity??><#if activity.department.type==12>selected="selected" </#if></#if> value=12>产品部-小学业务</option>-->
                                <#--<option <#if activity??><#if activity.department.type==13>selected="selected" </#if></#if> value=13>产品部-中学业务</option>-->
                                <#--<option <#if activity??><#if activity.department.type==21>selected="selected" </#if></#if> value=21>客服部</option>-->
                                <#--<option <#if activity??><#if activity.department.type==31>selected="selected" </#if></#if> value=31>市场部</option>-->
                            </select>
                            <span style="color: red">(必填)</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动开始时间</label>
                        <div class="controls">
                            <input type="text" id="actStartDate" name="actStartDate" <#if activity??> value="${activity.startDate!''}"</#if><#if !actEditable> disabled</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动结束时间</label>
                        <div class="controls">
                            <input type="text" id="actEndDate" name="actEndDate" <#if activity??> value="${activity.endDate!''}"</#if><#if !actEditable> disabled</#if>/>
                        </div>
                    </div>

                    <input type="hidden" id="status" name="status" <#if activity??>value="${activity.status!''}"</#if> />
                    <input type="hidden" id="id" name="id" <#if activity??>value="${activity.id!''}"</#if> />
                </fieldset>
            </form>
        </div>
    </div>
    <div>
        <ul class="inline" style="margin-top: 2em">
            <legend style="font-weight: 700;">积分活动规则</legend>
            <#if ruleEditable??>
                <ul class="inline">
                    <li>
                    <#if ruleEditable>
                    <button class="btn btn-primary" onclick="addNewActivityRule()">新增积分活动规则</button>
                    </#if>
                    </li>
                </ul>
            </#if>
            <br>
            <label style="display:none" id="selectRule">0</label>
            <label style="display:none" id="ruleCount">0</label>
            <table id="customer_service_record" class="table table-hover table-striped table-bordered">
                <tbody>
                <tr id="comment_title">
                    <th>积分规则ID</th>
                    <th style="display: none; ">isLongTerm</th>
                    <th>规则开始时间</th>
                    <th>规则结束时间</th>
                    <th style="display: none; ">CreditValue</th>
                    <th <#--style="display: none; "-->>积分类型</th>
                    <th style="display: none; ">UserValue</th>
                    <th>规则生效对象</th>
                    <th style="display: none; ">PaymentValue</th>
                    <th>积分收支类型</th>
                    <th style="display: none; ">UsageValue</th>
                    <th>积分用途类型</th>
                    <th>场景描述</th>
                    <#if finished??><#if !finished><th>操作</th></#if></#if>
                </tr>
                </tbody>
                <#if rules??>
                <#list rules as rule >
                    <tr>
                        <td>${rule.ruleId!}</td>
                        <td style="display: none; ">false</td>
                        <td>${rule.ruleStartDate!"长期有效"}</td>
                        <td>${rule.ruleEndDate!"长期有效"}</td>
                            <td <#--style="display: none; "-->>${rule.integralType!}</td>
                            <td style="display: none; ">${rule.integralType%10000!}</td>
                        <td style="display: none; ">${rule.userType.type!}</td>
                        <td>${rule.userType.description!""}</td>
                        <td style="display: none; ">${rule.paymentType.type!}</td>
                        <td>${rule.paymentType.description!""}</td>
                        <td style="display: none; ">${rule.usageType.type!}</td>
                        <td>${rule.usageType.description!""}</td>
                        <td>${rule.description!""}</td>
                    <#--<#if finished??><#if !finished>-->
                        <td>
                            <#--<#if ruleEditable>-->
                                <button class="btn btn-primary" onclick="editRule(this)">编 辑</button>
                                <button class="btn btn-danger" onclick="delCurrentRule(this)">删 除</button>
                            <#--</#if>-->
                        </td>
                    <#--</#if></#if>-->
                    </tr>
                </#list>
                </#if>
            </table>
    </div>
</div>
<div id="addRule-dialog" class="modal fade hide" style="width: 40%; left: 40%;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>积分活动规则信息</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt></dt>
                    <dd><input type="checkbox" id="permanent" onclick="setDateEditable()" style="text-align: right">此条积分活动规则长期生效</input> </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>规则开始时间</dt>
                    <dd>
                        <input type="text" id="ruleStartDate" name="ruleStartDate" />
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>规则结束时间</dt>
                    <dd>
                        <input type="text" id="ruleEndDate" name="ruleEndDate"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline" id="need-not-to-know">
                <li>
                    <dt>积分类型</dt>
                    <dd>
                        <input type="number" id="integralType" name="integralType" max="999999" min="000000"/>
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>生效对象</dt>
                    <dd>
                        <select name="userType" id="userType" class="multiple">
                            <option value=1>教师</option>
                            <option value=2>家长</option>
                            <option value=3>学生</option>
                        </select>
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>积分收支类型</dt>
                    <dd>
                        <select name="paymentType" id="paymentType" class="multiple">
                            <option value=1>用户获得积分</option>
                            <option value=2>用户支出积分</option>
                        </select>
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>积分用途类型</dt>
                    <dd>
                        <select name="usageType" id="usageType" class="multiple">
                            <option value=1>功能</option>
                            <option value=2>场景</option>
                            <option value=3>其他</option>
                        </select>
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>场景描述</dt>
                    <dd>
                        <textarea id="description" name="description" cols="50" rows="10"></textarea>
                        <span style="color: red">(200字以内)</span>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="saveRuleBtn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

</div>


<script type="text/javascript">
    $(function () {
        $("#actStartDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#actEndDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#ruleStartDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#ruleEndDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#saveActBtn").on("click", function () {
            var actMapper = {
                activityName: $("#activityName").val(),
                creatorName: $("#creatorName").html().trim(),
                department: $("#department").val(),
                actStartDate: $("#actStartDate").val(),
                actEndDate: $("#actEndDate").val(),
                disabled: $("#disabled").val()
                <#if activity??>, activityId: '${(activity.id)!''}'</#if>
            };
            if (actMapper.activityName == undefined || actMapper.activityName.trim() == '') {
                alert("请输入活动名称");
                return false;
            }
            if (actMapper.department == undefined || actMapper.department.trim() == '') {
                alert("请选择活动归属部门");
                return false;
            }
            $.ajax({
                type: "post",
                url: "saveactivity.vpage",
                data: actMapper,

                success: function (data) {
                    $("#record_success").val(data.success);
                    if (data.success) {
                        window.location.href='activityinfo.vpage?id='+data.activityId+'&edit=false';
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#editActBtn").on("click", function () {
            window.location.href='activityinfo.vpage?id=<#if activity??>${activity.id!}</#if>&edit=true';
        });

        $("#saveRuleBtn").on("click", function () {
            if (confirm("是否确认保存活动规则？")) {
                var postUrl = "saverule.vpage";
                var activityId = $("#activityId").html().trim();
                var selectRule = $("#selectRule").val();
                $.ajax({
                    type: "post",
                    url: postUrl,
                    data: {
                        selectRule: selectRule,
                        activityId: activityId,
                        ruleStartDate: $("#ruleStartDate").val(),
                        ruleEndDate: $("#ruleEndDate").val(),
                        department: $("#department").val(),
                        integralType: $("#integralType").val(),
                        userType: $("#userType").val(),
                        paymentType: $("#paymentType").val(),
                        usageType: $("#usageType").val(),
                        description: $("#description").val()
                    },

                    success: function (data) {
                        $("#record_success").val(data.success);
                        if (data.success) {
                            $("#addRule-dialog").modal("hide");
                            window.location.href = 'activityinfo.vpage?id=<#if activity??>${activity.id!}</#if>&edit=false';
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });

    });

    function setDateEditable() {
        var tempEnd = $("#ruleEndDate");
        if($("#permanent:checked").val()) {
            tempEnd.val("");
            tempEnd.attr("disabled", true);
        } else {
            tempEnd.attr("disabled", false);
        }
    }

    function addNewActivityRule() {
        $("#selectRule").val("0");
        $("#need-not-to-know").attr("style", "display: none;");
        $("#ruleStartDate").val("");
        $("#ruleEndDate").val("");
        $("#integralType").val("");
        $("#description").val("");
        $("#addRule-dialog").modal("show");
    }

    function editRule(obj){
        var selectedRule=obj;
        while(selectedRule.tagName!="TR"){
            selectedRule=selectedRule.parentNode;
        }
        $("#selectRule").val(selectedRule.children[0].textContent);
        $("#ruleStartDate").val(selectedRule.children[2].textContent);
        var endDate = selectedRule.children[3].textContent;
        if(endDate == '长期有效') {
            $("#permanent").attr("checked", true);
            $("#ruleEndDate").val("");
            $("#ruleEndDate").attr("disabled", true);
        } else {
            $("#ruleEndDate").val(selectedRule.children[3].textContent);
        }
        $("#need-not-to-know").attr("style", "");
        $("#integralType").val(selectedRule.children[4].textContent);
        $("#userType").val(selectedRule.children[6].textContent);
        $("#paymentType").val(selectedRule.children[8].textContent);
        $("#usageType").val(selectedRule.children[10].textContent);
        $("#description").val(selectedRule.children[12].textContent);
        $("#addRule-dialog").modal("show");
    }

    function delCurrentRule(obj){
        var selectedRule = obj;
        while (selectedRule.tagName != "TR") {
            selectedRule = selectedRule.parentNode;
        }
        var ruleId = $($(selectedRule).children()[0]).html().trim();
        if (confirm("确定删除当前规则？(ID:" + ruleId +")")) {
            $.ajax({
                type: "post",
                url: "delrule.vpage",
                data: {
                    ruleId: ruleId
                },
                success: function (data) {
                    if (data.success) {
                        selectedRule.parentNode.removeChild(selectedRule);
                    } else {
                        alert("积分活动规则删除失败");
                    }
                }
            });
        }
    }

    function appPostJson(url, data, callback, error, dataType) {
        dataType = dataType || "json";
        return $.ajax({
            type: 'post',
            url: url,
            data: JSON.stringify(data),
            success: callback,
            error: error,
            dataType: dataType,
            contentType: 'application/json;charset=UTF-8'
        });

    }


</script>
</@layout_default.page>