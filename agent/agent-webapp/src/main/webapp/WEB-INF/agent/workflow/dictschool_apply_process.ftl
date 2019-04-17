<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=11>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 字典表调整审核</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div class="form-horizontal">
            <#if applyData?has_content && applyData.apply?has_content>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">调整类别</label>
                        <div class="controls">
                            <label class="control-label" id="modifyType" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.modifyType == 1>添加学校<#elseif applyData.apply.modifyType == 2>删除学校<#elseif applyData.apply.modifyType == 3>业务变更</#if></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校ID</label>
                        <div class="controls">
                            <label class="control-label" id="schoolIdLabel" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.schoolId!}</label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校名称</label>
                        <div class="controls">
                            <label class="control-label" id="schoolName" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.schoolName!}</label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">所属地区</label>
                        <div class="controls">
                            <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.regionName!}</label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校阶段</label>
                        <div class="controls">
                            <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.schoolLevel == 1>小学<#elseif applyData.apply.schoolLevel == 2>初中<#elseif applyData.apply.schoolLevel == 4>高中<#elseif applyData.apply.schoolLevel == 5>学前</#if></label>
                            <input type="hidden" id="schoolLevelValue" value="${applyData.apply.schoolLevel}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">相关信息</label>
                        <div class="controls">
                            <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.modifyDesc?has_content>${applyData.apply.modifyDesc?replace('\r\n', '<br/>')}</#if></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">该校历史申请记录</label>
                    </div>
                    <div class="dataTables_wrapper" role="grid">
                        <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                            <thead>
                            <tr>
                                <th class="sorting" style="width: 60px;">申请日期</th>
                                <th class="sorting" style="width: 60px;">申请人</th>
                                <th class="sorting" style="width: 60px;">申请类型</th>
                                <th class="sorting" style="width: 60px;">审核结果</th>
                                <th class="sorting" style="width: 60px;">调整原因</th>
                                <th class="sorting" style="width: 60px;">审核情况</th>
                            </tr>
                            </thead>
                            <tbody>
                                <#if historyApplyList?has_content>
                                    <#list historyApplyList as item>
                                    <tr>
                                        <td>${item.createDatetime!}</td>
                                        <td>${item.accountName!}</td>
                                        <#if item.modifyType == 1>
                                            <td>添加申请</td>
                                        <#else>
                                            <td>删除申请</td>
                                        </#if>
                                        <td>${item.status!}</td>
                                        <td>${item.comment!}</td>
                                        <td>${item.processFlow}</td>
                                    </tr>
                                    </#list>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="control-group">
                        <label class="control-label">调整原因</label>
                        <div class="controls">
                            <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.comment!}</label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">审核意见</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="processNote" rows="5" style="width: 880px;"></textarea>
                        </div>
                    </div>
                    <div class="form-actions" style="background:#fff;border:none">
                        <#if processList?has_content>
                            <#list processList as item>
                                <button type="button" class="btn btn-primary" data-dismiss="modal" style="margin-left:30px" onclick="processFunction(${item.type}, ${applyData.apply.workflowId!})">${item.desc}</button>
                            </#list>
                        </#if>
                    </div>

                    <div class="control-group">
                        <label class="control-label">处理意见</label>
                    </div>

                    <div class="dataTables_wrapper" role="grid">
                        <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                            <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">审核日期</th>
                                    <th class="sorting" style="width: 60px;">审核人</th>
                                    <th class="sorting" style="width: 60px;">处理结果</th>
                                    <th class="sorting" style="width: 60px;">处理意见</th>
                                    <th class="sorting" style="width: 60px;">备注</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#if applyData.processResultList?has_content>
                                    <#list applyData.processResultList as processResult>
                                        <tr>
                                            <td><#if processResult.processDate?has_content>${processResult.processDate?string("yyyy-MM-dd")}</#if></td>
                                            <td>${processResult.accountName!}</td>
                                            <td>${processResult.result!}</td>
                                            <td>${processResult.processNotes!}</td>
                                            <td></td>
                                        </tr>
                                    </#list>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </fieldset>
            </#if>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    function processFunction(processResult, workflowId){
        var processNote = $('#processNote').val();
        if(processNote == ""){
            alert("请填写处理意见！");
            return;
        }
        if(confirm("确认" + (processResult == 1? "通过":"驳回") + "该请求吗？")){
            var schoolLevelValue = $("#schoolLevelValue").val();
            var processUserList = new Array();
            if(processResult == 1 && schoolLevelValue == 5){ // 如果是学前学校， 市经理审核通过后，不进行风控审核，系统自动审核通过
                processUserList.push({"userPlatform":"agent", "account":"system", "accountName":"系统"});
            }
            var processUsers = JSON.stringify(processUserList);
            $.post('process.vpage',{
                processResult:processResult,
                workflowId:workflowId,
                processNote:processNote,
                processUsers:processUsers
            },function(data){
                if(data.success){
                    location.href = "list.vpage"
                }else{
                    alert(data.info)
                }
            });
        }
    }

</script>

</@layout_default.page>
