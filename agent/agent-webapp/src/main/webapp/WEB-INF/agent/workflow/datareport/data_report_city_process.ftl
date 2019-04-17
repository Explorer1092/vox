<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='大数据报告审核' page_num=11> <#--这个也已经改过2次了-->
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 大数据报告审核</h2>
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
                            <label class="control-label">学科</label>
                            <div class="controls">
                                <label class="control-label" id="modifyType" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.subject == 1>小学英语<#elseif applyData.apply.subject == 2>小学数学</#if></label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">区域/学校</label>
                            <div class="controls">
                                <label class="control-label" id="schoolIdLabel" style="text-align: left;margin-left:90px;width:400px">
                                    <#if applyData.apply.reportLevel == 2>${applyData.apply.countyName!}
                                    <#elseif applyData.apply.reportLevel == 1>${applyData.apply.cityName!}/${applyData.apply.countyName!}
                                    <#elseif applyData.apply.reportLevel == 3>${schoolRegion!""} ${applyData.apply.schoolName!}（${applyData.apply.schoolId!}）
                                    </#if>
                                </label>
                            </div>
                        </div>
                        <#if applyData.apply.engStartGrade??>
                            <div class="control-group">
                                <label class="control-label">英语起始年级</label>
                                <div class="controls">
                                    <label class="control-label" id="modifyType" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.engStartGrade == 1>小学一年级<#elseif applyData.apply.engStartGrade == 3>小学三年级</#if></label>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="control-label">时间维度</label>
                            <div class="controls">
                                <label class="control-label" id="schoolName" style="text-align: left;margin-left:90px;width:250px">
                                    <#if applyData.apply.reportType == 1>学期报告
                                    <#elseif applyData.apply.reportType == 2>月度报告
                                    </#if>
                                </label>
                            </div>
                        </div>

                        <#if applyData.apply.reportType == 1>
                            <div class="control-group">
                                <label class="control-label">学期</label>
                                <div class="controls">
                                    <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:250px">
                                        <#if applyData.apply.reportTerm == 1>2016年9-12月
                                        <#elseif applyData.apply.reportTerm == 2>2017年1-6月
                                        <#elseif applyData.apply.reportTerm == 3>2017年7-12月
                                        <#elseif applyData.apply.reportTerm == 4>2018年1-6月
                                        </#if>
                                    </label>
                                </div>
                            </div>
                        <#elseif applyData.apply.reportType == 2>
                            <div class="control-group">
                                <label class="control-label">月份</label>
                                <div class="controls">
                                    <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.reportMonth!}</label>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="control-label">样本校</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.sampleSchoolId!}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${applyData.apply.sampleSchoolName!}</label>
                            </div>
                        </div>

                        <div class="control-group">
                            <#--<label class="control-label"></label>-->
                            <div class="control">
                                <label class="control-label" style="text-align: left;margin-left:90px;width:250px">
                                    申请人历史申请记录：共计${historyApplies?size!0}条
                                </label>
                            </div>
                        </div>
                        <div class="dataTables_wrapper" role="grid">
                            <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">申请日期</th>
                                    <th class="sorting" style="width: 60px;">申请学科</th>
                                    <th class="sorting" style="width: 60px;">区域/学校</th>
                                    <th class="sorting" style="width: 60px;">时间维度</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if historyApplies?has_content>
                                        <#list historyApplies as item>
                                        <tr>
                                            <td>${item.createDatetime?string("yyyy-MM-dd")}</td>
                                            <td><#if item.subject == 1>小学英语<#elseif item.subject ==2>小学数学</#if></td>
                                            <td>
                                                <#if item.reportLevel == 2>${item.countyName!}
                                                <#elseif item.reportLevel == 1>${item.cityName!}/${item.countyName!}
                                                <#elseif item.reportLevel == 3>${item.schoolName!}（${item.schoolId!}）
                                                </#if>
                                            </td>

                                            <td>
                                                <#if item.reportType == 1>学期报告
                                                    <#if item.reportTerm == 1>2016年9-12月
                                                    <#elseif item.reportTerm == 2>2017年1-6月
                                                    <#elseif item.reportTerm == 3>2017年7-12月
                                                    <#elseif item.reportTerm == 4>2018年1-6月
                                                    </#if>
                                                <#elseif item.reportType == 2>月度报告${item.reportMonth}
                                                </#if>
                                            </td>
                                        </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <div class="control-group">
                            <label class="control-label">申请原因</label>
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
                                    <button type="button" class="btn btn-primary" data-dismiss="modal" style="margin-left:180px;padding:8px 20px" onclick="processFunction(${item.type}, ${applyData.apply.workflowId!})">${item.desc}</button>
                                </#list>
                            </#if>
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
        if(processResult !=1 && processResult != 2){
            alert('请选择处理意见');
            return;
        }
        if(confirm("确认" + (processResult == 1? "通过":"驳回") + "该请求吗？")){
            $.post('process.vpage',{
                processResult:processResult,
                workflowId:workflowId,
                processNote:processNote
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
