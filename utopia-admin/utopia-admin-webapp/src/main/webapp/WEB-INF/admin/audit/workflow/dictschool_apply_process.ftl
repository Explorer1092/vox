<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="审核平台" page_num=21>
<style>
    #evaluate_table tr {
        border-bottom: 1px solid #e0e0e0;
    }
    #evaluate_table th {
        text-align:center;
        width:90px;
    }
    #evaluate_table th:nth-child(6){
        width:290px
    }
</style>
<div class="span9">
    <fieldset><legend>字典表调整申请</legend></fieldset>
    <div class="form-horizontal">
    <#if applyData?has_content && applyData.apply?has_content>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <input type="hidden" id="schoolId" name="schoolId" value="${applyData.apply.schoolId!}"/>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>调整类别</strong></label>
                    <div class="controls">
                        <label class="control-label" id="modifyType" style="text-align: left;"><#if applyData.apply.modifyType == 1>添加学校<#elseif applyData.apply.modifyType == 2>删除学校</#if></label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学校ID</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolIdLabel" style="text-align: left;">${applyData.apply.schoolId!}</label>
                        <button type="button" class="btn btn-default" onclick="showHistoryApply()">学校申请记录</button>
                        <button type="button" class="btn btn-default" onclick="showEvaluateHistory()">学校评分记录</button>
                        <button type="button" class="btn btn-default" onclick="showDepartmentData()">当前部门概况</button>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学校名称</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolName" style="text-align: left;">${applyData.apply.schoolName!}</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>所属地区</strong></label>
                    <div class="controls">
                        <label class="control-label" id="regionName" style="text-align: left;">${applyData.apply.regionName!}</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学校阶段</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolLevel" style="text-align: left;"><#if applyData.apply.schoolLevel == 1>小学<#elseif applyData.apply.schoolLevel == 2>初中<#elseif applyData.apply.schoolLevel == 4>高中<#elseif applyData.apply.schoolLevel == 5>学前</#if></label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>相关信息</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolLevel" style="text-align: left;"><#if applyData.apply.modifyDesc?has_content>${applyData.apply.modifyDesc?replace('\r\n', '<br/>')}</#if></label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学生数量</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolLevel" style="text-align: left;">${schoolSize!}</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>调整原因</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolLevel" style="text-align: left;">${applyData.apply.comment!}</label>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>地理位置</strong></label>
                    <div class="controls">
                        <input id="placeScore" type="number" placeholder ="输入1-5之间的数字"/>*必填</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>教学质量</strong></label>
                    <div class="controls">
                        <input id="teachScore" type="number" placeholder ="输入1-5之间的数字"/>*必填</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>生源水平</strong></label>
                    <div class="controls">
                        <input id="studentScore" type="number" placeholder ="输入1-5之间的数字"/>*必填</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>商业潜力</strong></label>
                    <div class="controls">
                        <input id="commercializeScore" type="number" placeholder ="输入1-5之间的数字"/>*必填</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>评分备注</strong></label>
                    <div class="controls">
                        <textarea id="evaluate_remark" rows="3"></textarea>*必填</span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>审核意见</strong></label>
                    <div class="controls">
                        <textarea id="processNote" name="processNote" rows="3"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"></label>
                    <div class="controls">
                        <#if processList?has_content>
                            <#list processList as item>
                                <button type="button" class="btn btn-default" data-dismiss="modal" onclick="processFunction(${item.type}, ${applyData.apply.workflowId!})">${item.desc}</button>
                            </#list>
                        </#if>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>处理意见区</strong></label>
                    <div class="controls">
                        <table class="table table-striped table-bordered" style="font-size: 14px;">
                            <thead>
                            <tr>
                                <th>审核日期</th>
                                <th>审核人</th>
                                <th>处理结果</th>
                                <th>处理意见</th>
                            </tr>
                            </thead>
                            <#if applyData.processResultList?has_content>
                                <#list applyData.processResultList as processResult>
                                    <tr>
                                        <td><#if processResult.processDate?has_content>${processResult.processDate?string("yyyy-MM-dd")}</#if></td>
                                        <td>${processResult.accountName!}</td>
                                        <td>${processResult.result!}</td>
                                        <td>${processResult.processNotes!}</td>
                                    </tr>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
            </div>
        </#if>
    </div>

    <div id="apply_history" class="modal hide fade" style="position:absolute;width:45%;margin-left:-25%;">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>学校申请记录</h3>
        </div>
        <div class="modal-body" >
            <div id="evaluate_table">
                <table>
                    <tr>
                        <th>申请日期</th>
                        <th>申请人</th>
                        <th>申请类型</th>
                        <th>审核结果</th>
                        <th>调整原因</th>
                        <th>审核情况</th>
                    </tr>
                    <#if historyApplyList?has_content>
                        <#list historyApplyList as item>
                            <tr>
                                <td>${item.createDatetime!}</td>
                                <td>${item.accountName!}</td>
                                <td>${item.modifyType!}</td>
                                <td>${item.status!}</td>
                                <td>${item.comment!}</td>
                                <td>${item.processFlow!}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
        </div>
    </div>

    <div id="evaluate_history" class="modal hide fade" style="position:absolute;width:45%;margin-left:-25%;">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>学校评分历史</h3>
        </div>
        <div class="modal-body" >
            <div id="evaluate_table">
                <table>
                    <tr>
                        <th>评价时间</th>
                        <th>评价人</th>
                        <th>地理位置</th>
                        <th>教学质量</th>
                        <th>生源质量</th>
                        <th>商业潜力</th>
                        <th>评分备注</th>
                    </tr>
                    <#if evaluateHistoryList?has_content>
                        <#list evaluateHistoryList as item>
                            <tr>
                                <td>${item.createTime?string("yyyy-MM-dd")}</td>
                                <td>${item.accountName!}</td>
                                <td>${item.placeScore!}</td>
                                <td>${item.teachScore!}</td>
                                <td>${item.studentScore!}</td>
                                <td>${item.commercializeScore!}</td>
                                <td>${item.remark!}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
        </div>
    </div>

    <div id="departmentData" class="modal hide fade" style="position:absolute;width:45%;margin-left:-25%;">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>当前部门概况</h3>
        </div>
        <div class="modal-body" >
            <div id="evaluate_table">
                <table>
                    <tr>
                        <th>部门</th>
                        <th>市经理</th>
                        <th>学校总数</th>
                        <th>学生总数</th>
                        <th>专员数</th>
                        <th>人均学校数</th>
                        <th>人均学生数</th>
                    </tr>
                    <#if agentDepartmentData?has_content>
                        <tr>
                            <td>${agentDepartmentData.groupName!}</td>
                            <td>${agentDepartmentData.groupManagerName!}</td>
                            <td>${agentDepartmentData.schoolCount!0}</td>
                            <td>${agentDepartmentData.allSchoolStudentCount!0}</td>
                            <td>${agentDepartmentData.dbUserCount!0}</td>
                            <td>${agentDepartmentData.perSchoolCount!0}</td>
                            <td>${agentDepartmentData.perStudentCount!0}</td>
                        </tr>
                    </#if>
                </table>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
        </div>
    </div>

</div>


<script type="text/javascript">

    function showHistoryApply(){
        $("#apply_history").modal('show');
    }

    function showEvaluateHistory(){
        $("#evaluate_history").modal('show');
    }

    function showDepartmentData(){
        $("#departmentData").modal('show');
    }

    function processFunction(processResult, workflowId){
        var checkResult = checkInputData();
        if(checkResult && confirm("确认" + (processResult == 1? "通过":"驳回") + "该请求吗？")){
            var processNote = $("#processNote").val().trim();
            $.post('process.vpage',{
                processResult:processResult,
                workflowId:workflowId,
                processNote:processNote
            },function(data){
                if(data.success){
                    var data = {
                        placeScore:$("#placeScore").val(),
                        teachScore:$("#teachScore").val(),
                        studentScore:$("#studentScore").val(),
                        commercializeScore:$("#commercializeScore").val(),
                        remark:$.trim($("#evaluate_remark").val()),
                        schoolId:$("#schoolId").val()
                    };
                    $.post("/crm/school/add_school_evaluate.vpage",data,function(res){
                        if(!res.success){
                            alert(res.info);
                        }
                    });
                    location.href = "todo_list.vpage"
                }else{
                    alert(data.info)
                }
            });
        }
    }

    function checkInputData(){
        var placeScore = $("#placeScore").val();
        var teachScore = $("#teachScore").val();
        var studentScore = $("#studentScore").val();
        var commercializeScore = $("#commercializeScore").val();
        var evaluate_remark = $.trim($("#evaluate_remark").val());

        var processNote = $("#processNote").val().trim();
        if(!checkScore(placeScore)){
            alert("地理位置评分填写错误");
            return false;
        }
        if(!checkScore(teachScore)){
            alert("教学质量评分填写错误");
            return false;
        }
        if(!checkScore(studentScore)){
            alert("生源水平评分填写错误");
            return false;
        }
        if(!checkScore(commercializeScore)){
            alert("商业化潜力评分填写错误");
            return false;
        }
        if(evaluate_remark == ""){
            alert("请填写评分备注");
            return false;
        }

        if(processNote == ""){
            alert("请填写审核意见");
            return false;
        }
        return true;
    }

    function checkScore(score){
        return score%1 == 0 && score > 0 && score < 6;
    }

</script>
</@layout_default.page>
