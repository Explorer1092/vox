<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加绩效指标' page_num=6>
<div class="row-fluid">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 编辑绩效指标</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
            <div class="pull-right">
                <a id="saveKpiDefBtn" class="btn btn-primary" href="#">
                    <i class="icon-ok icon-white"></i>
                    保存
                </a>&nbsp;
            </div>
            </#if>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">绩效指标名称</label>
                        <div class="controls">
                            <input id="kpiName" class="input-xlarge focused" type="text" value="<#if kpiDef??>${kpiDef.kpiName!}</#if>"
                                    <#if !requestContext.getCurrentUser().isCountryManager() && !requestContext.getCurrentUser().isAdmin()> readonly="true" </#if>
                                    >
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">绩效指标说明</label>
                        <div class="controls">
                            <textarea id="kpiDesc" style="width: 270px;" rows="4" <#if !requestContext.getCurrentUser().isCountryManager() && !requestContext.getCurrentUser().isAdmin()> readonly="true" </#if> ><#if kpiDef??>${kpiDef.kpiDesc!}</#if></textarea>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="selectError1">适用对象角色</label>
                        <div class="controls">
                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                            <select id="kpiRole" name="kpiRole" style="width: 280px">
                                <#list allAgentRoleMap?keys as item>
                                    <#if item != "1" && item != "2">
                                    <option value="${item!}" <#if kpiDef?? && kpiDef.kpiRole?? && (kpiDef.kpiRole?string == item)>selected</#if>>
                                    ${allAgentRoleMap[item?string].roleName!}
                                    </option>
                                    </#if>
                                </#list>
                            </select>
                        <#else>
                            <input id="kpiRole" class="input-xlarge focused" type="text" readonly
                                   value="<#if kpiDef?? && kpiDef.kpiRole??> ${allAgentRoleMap[kpiDef.kpiRole?string].roleName!}</#if>">
                        </#if>
                        </div>
                    </div>

                    <#if requestContext.getCurrentUser().isAdmin()>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">绩效指标CODE</label>
                        <div class="controls">
                            <input id="kpiCode" class="input-xlarge focused" type="text" value="<#if kpiDef??>${kpiDef.kpiCode!}</#if>">
                        </div>
                    </div>
                    <#else>
                        <input type="hidden" id="kpiCode" class="input-xlarge focused" value="<#if kpiDef??>${kpiDef.kpiCode!}</#if>">
                    </#if>
            </form>
        </div>
    </div>
    <div class="row-fluid" style="display: none" id="kpiDetailPanel">
    <div class="box span6">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 绩效指标考核标准</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
            <div class="pull-right">
                <a id="addKpiAssessmentBtn" class="btn btn-success" href="#">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>&nbsp;
            </div>
            </#if>
        </div>
        <div class="box-content">
            <table class="table table-striped table-bordered bootstrap-datatable dataTable">
                <thead>
                <tr>
                    <th class="sorting">完成度（%）</th>
                    <th class="sorting">现金奖励 </th>
                    <th class="sorting">点数奖励 </th>
                    <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                    <th class="sorting">操作 </th>
                    </#if>
                </tr>
                </thead>

                <tbody>
                    <#if kpiDef??>
                    <#if kpiDef.kpiAssessmentList??>
                        <#list kpiDef.kpiAssessmentList as kpiAssessment>
                        <tr class="odd">
                            <td class="center  sorting_1">${kpiAssessment.baseline!} 以下 </td>
                            <td class="center  sorting_1">${kpiAssessment.cashReward!}</td>
                            <td class="center  sorting_1">${kpiAssessment.pointReward!}</td>
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                            <td class="center ">
                                <a id="edit_kpi_assessment_${kpiAssessment.id!}" class="btn btn-info" href="#">
                                    <i class="icon-edit icon-white"></i>
                                    编辑
                                </a>
                                <a id="delete_kpi_assessment_${kpiAssessment.id!}" class="btn btn-danger" href="#">
                                    <i class="icon-trash icon-white"></i>
                                    删除
                                </a>
                            </td>
                            </#if>
                        </tr>
                        </#list>
                    </#if>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
    <div class="box span6">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 绩效指标考核期间</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
            <div class="pull-right">
                <a id="addKpiEvalBtn" class="btn btn-success" href="#">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>&nbsp;
            </div>
            </#if>
        </div>
        <div class="box-content">
            <table class="table table-striped table-bordered bootstrap-datatable dataTable">
                <thead>
                <tr>
                    <th class="sorting">考核开始日期</th>
                    <th class="sorting">考核结束日期</th>
                    <th class="sorting">考核结算日期</th>
                    <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                    <th class="sorting">操作 </th>
                    </#if>
                </tr>
                </thead>

                <tbody>
                    <#if kpiDef??>
                        <#if kpiDef.kpiEvalList??>
                            <#list kpiDef.kpiEvalList as kpiEval>
                            <tr class="odd">
                                <td class="center  sorting_1">${kpiEval.evalDurationFrom?string("yyyy-MM-dd")}</td>
                                <td class="center  sorting_1">${kpiEval.evalDurationTo?string("yyyy-MM-dd")}</td>
                                <td class="center  sorting_1">${kpiEval.evalDate?string("yyyy-MM-dd")}</td>
                                <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                <td class="center ">
                                    <a id="edit_kpi_eval_${kpiEval.id!}" class="btn btn-info" href="#">
                                        <i class="icon-edit icon-white"></i>
                                        编辑
                                    </a>
                                    <a id="delete_kpi_eval_${kpiEval.id!}" class="btn btn-danger" href="#">
                                        <i class="icon-trash icon-white"></i>
                                        删除
                                    </a>
                                </td>
                                </#if>
                            </tr>
                            </#list>
                        </#if>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div id="kpiAssessmentDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">添加绩效考核基准</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">完成度</label>
                        <div class="controls">
                            <input id="modalBaseline" class="input-xlarge focused" type="text">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">现金奖励</label>
                        <div class="controls">
                            <input id="modalCashReward" class="input-xlarge focused" type="text">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">点数奖励</label>
                        <div class="controls">
                            <input id="modalPointReward" class="input-xlarge focused" type="text">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="saveKpiAssessmentBtn" type="button" class="btn btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="kpiEvalDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">添加绩效考核期间</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">考核开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-xlarge" id="modalEvalDurationFrom">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">考核结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-xlarge" id="modalEvalDurationTo">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">考核结算时间</label>
                        <div class="controls">
                            <input type="text" class="input-xlarge" id="modalEvalDate">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="saveKpiEvalBtn" type="button" class="btn btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>

<input type="hidden" id="kpiId" value="${kpiId!}">
<input type="hidden" id="kpiAssessmentId" value="0">
<input type="hidden" id="kpiEvalId" value="0">
<script type="text/javascript">

$(function(){
    $("#modalEvalDurationFrom").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : new Date(),
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){}
    });
    $("#modalEvalDurationTo").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : new Date(),
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){}
    });

    $("#modalEvalDate").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : new Date(),
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){}
    });

    var kpiId = parseInt($('#kpiId').val());
    if (kpiId > 0) {
        $('#kpiDetailPanel').show();
    }

    $('#saveKpiDefBtn').live('click',function(){
        saveKpiDef();
    });

    $('#addKpiAssessmentBtn').live('click',function(){
        $('#kpiAssessmentId').val('0');
        $('#modalBaseline').val("");
        $('#modalCashReward').val("");
        $('#modalPointReward').val("");
        $('#kpiAssessmentDialog').modal('show');
    });

    $('#saveKpiAssessmentBtn').live('click',function(){
        saveKpiAssessment();
    });

    $("a[id^='edit_kpi_assessment_']").live('click',function(){
        var assessmentId = $(this).attr("id").substring("edit_kpi_assessment_".length);
        editKpiAssessment(assessmentId);
    });
    $("a[id^='delete_kpi_assessment_']").live('click',function(){
        var kpiId = $('#kpiId').val();
        var assessmentId = $(this).attr("id").substring("delete_kpi_assessment_".length);
        deleteKpiAssessment(kpiId, assessmentId);
    });

    $('#addKpiEvalBtn').live('click',function(){
        $('#kpiEvalId').val('0');
        $('#modalEvalDurationFrom').val("");
        $('#modalEvalDurationTo').val("");
        $('#modalEvalDate').val("");
        $('#kpiEvalDialog').modal('show');
    });

    $('#saveKpiEvalBtn').live('click',function(){
        saveKpiEval();
    });

    $("a[id^='edit_kpi_eval_']").live('click',function(){
        var evalId = $(this).attr("id").substring("edit_kpi_eval_".length);
        editKpiEval(evalId);
    });
    $("a[id^='delete_kpi_eval_']").live('click',function(){
        var kpiId = $('#kpiId').val();
        var evalId = $(this).attr("id").substring("delete_kpi_eval_".length);
        deleteKpiEval(kpiId, evalId);
    });


});

function saveKpiDef() {
    var kpiId = $('#kpiId').val();
    var kpiName = $('#kpiName').val();
    var kpiDesc = $('#kpiDesc').val();
    var kpiRole = $('#kpiRole').find('option:selected').val();
    var kpiCode = $('#kpiCode').val();

    if (kpiName.trim() == '') {
        alert("请输入绩效指标名称!");
        return false;
    }

    $.post('savekpidef.vpage',{
        kpiId: kpiId,
        kpiName: kpiName,
        kpiDesc: kpiDesc,
        kpiRole: kpiRole,
        kpiCode:kpiCode
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            $(window.location).attr('href', 'addkpi.vpage?kpiId=' + data.kpiId);
        }
    });
}

function saveKpiAssessment() {
    var kpiId = $('#kpiId').val();
    var kpiAssessmentId = $('#kpiAssessmentId').val();
    var baseline = $('#modalBaseline').val().trim();
    var cashReward = $('#modalCashReward').val().trim();;
    var pointReward = $('#modalPointReward').val().trim();;

    if (baseline == '' || !$.isNumeric(baseline)) {
        alert("完成度必须输入并且为数字类型!");
        return false;
    }

    if (cashReward == '' || !$.isNumeric(cashReward)) {
        alert("现金奖励必须输入并且为数字类型!");
        return false;
    }

    if (pointReward == '' || !$.isNumeric(pointReward)) {
        alert("现金奖励必须输入并且为数字类型!");
        return false;
    }

    $.post('addassessment.vpage',{
        kpiId: kpiId,
        assessmentId: kpiAssessmentId,
        baseline: baseline,
        cashReward: cashReward,
        pointReward: pointReward
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            window.location.reload();
            $('#kpiAssessmentDialog').modal('hide');
        }
    });
}

function editKpiAssessment(assessmentId) {
    $.post('getassessment.vpage',{
        assessmentId: assessmentId
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            $('#kpiAssessmentId').val(assessmentId);
            $('#modalBaseline').val(data.kpiAssessment.baseline);
            $('#modalCashReward').val(data.kpiAssessment.cashReward);
            $('#modalPointReward').val(data.kpiAssessment.pointReward);
            $('#kpiAssessmentDialog').modal('show');
        }
    });
}

function deleteKpiAssessment(kpiId, assessmentId) {
    if(!confirm("确定要删除此绩效指标考核指标吗?")){
        return false;
    }

    $.post('deleteassessment.vpage',{
        kpiId: kpiId,
        assessmentId: assessmentId
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            window.location.reload();
        }
    });
}

function saveKpiEval() {
    var kpiId = $('#kpiId').val();
    var kpiEvalId = $('#kpiEvalId').val();
    var evalDurationFrom = $('#modalEvalDurationFrom').val();
    var evalDurationTo = $('#modalEvalDurationTo').val();
    var evalDate = $('#modalEvalDate').val();

    if (evalDurationFrom == '') {
        alert("请输入考核开始日期!");
        return false;
    }

    if (evalDurationTo == '') {
        alert("请输入考核结束日期!");
        return false;
    }

    if (evalDate == '') {
        alert("请输入考核结算时间!");
        return false;
    }

    if (evalDurationFrom > evalDurationTo) {
        alert("考核开始日期不能晚于考核结束日期!");
        return false;
    }

    if (evalDurationTo > evalDate) {
        alert("考核结算日期必须晚于考核结束日期!");
        return false;
    }

    $.post('addeval.vpage',{
        kpiId: kpiId,
        evalId: kpiEvalId,
        evalDate: evalDate,
        evalDurationFrom: evalDurationFrom,
        evalDurationTo: evalDurationTo
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            window.location.reload();
            $('#kpiEvalDialog').modal('hide');
        }
    });
}

function editKpiEval(evalId) {
    $.post('geteval.vpage',{
        evalId: evalId
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            $('#kpiEvalId').val(evalId);
            $('#modalEvalDurationFrom').val(data.kpiEval.evalDurationFromString);
            $('#modalEvalDurationTo').val(data.kpiEval.evalDurationToString);
            $('#modalEvalDate').val(data.kpiEval.evalDateString);
            $('#kpiEvalDialog').modal('show');
        }
    });
}

function deleteKpiEval(kpiId, evalId) {
    if(!confirm("确定要删除此绩效指标考核期间吗?")){
        return false;
    }

    $.post('deleteeval.vpage',{
        kpiId: kpiId,
        evalId: evalId
    },function(data){
        if(!data.success){
            alert(data.info);
        }else{
            window.location.reload();
        }
    });
}

</script>

</@layout_default.page>