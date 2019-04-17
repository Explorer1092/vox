<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='大数据报告审核' page_num=11> <#--这个已经改过2次了-->
<div id="loadingDiv" style="display:none ;position: fixed;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在上传，请等待……</p>
</div>
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
                                    <#if applyData.apply.reportLevel == 1>${applyData.apply.cityName!}
                                    <#elseif applyData.apply.reportLevel == 2>${applyData.apply.cityName!}/${applyData.apply.countyName!}
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
                            <label class="control-label" style="text-align: left;margin-left:90px;width:250px">
                                申请人历史申请记录：共计${historyApplies?size!0}条
                            </label>
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
                                                <#if item.reportLevel == 1>${item.cityName!}
                                                <#elseif item.reportLevel == 2>${item.cityName!}/${item.countyName!}
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

                        <#--   前端处理     -->
                        <div class="control-group">
                            <label class="control-label">处理意见</label>
                        </div>
                            <#if processList?has_content>
                                <#list processList as item>
                                <div class="control-group schoolShow">
                                    <label class="control-label"><input class="auditOpinion" type="radio" name="auditOpinion"
                                                                        value="${item.type}"/>${item.desc}</label>
                                    <#if item.type == 1><#--同意的情况-->
                                        <div class="controls fileUnUseSpan firstDocument">
                                            <input id="fileUnUse1" name="fileUnUse" class="fileUnUse" data-content="<#if applyData.apply.reportLevel == 1>${applyData.apply.cityName!}
                                    <#elseif applyData.apply.reportLevel == 2>${applyData.apply.cityName!}/${applyData.apply.countyName!}
                                    <#elseif applyData.apply.reportLevel == 3>${applyData.apply.schoolName!}（${applyData.apply.schoolId!}）
                                    </#if><#if applyData.apply.reportType == 1>学期报告
                                    <#elseif applyData.apply.reportType == 2>月度报告
                                    </#if>"  style="background: #43a1da;color:#fff" type="file" >&nbsp;&nbsp;&nbsp;&nbsp;上传学期/月份报告
                                        </div>
                                        <div class="controls fileUnUseSpan secondDocument" style="margin-top:20px">
                                            <input id="fileUnUse2" name="fileUnUse2" class="fileUnUse" data-content="样本校报告"  style="background: #43a1da;color:#fff" type="file">&nbsp;&nbsp;&nbsp;&nbsp;上传样本校报告
                                        </div>
                                    <#elseif item.type == 2><#--驳回的情况-->
                                        <label class="control-label">
                                            <textarea class="input-xlarge" id="processNote" rows="5" style="width: 880px;margin-left:50px" readonly="readonly"></textarea>
                                        </label>
                                    </#if>
                                </div>

                                </#list>
                            </#if>
                        <div class="form-actions" style="background:#fff;border:none">
                            <button type="button" class="btn btn-primary" data-dismiss="modal" style="margin-left:30px">提交</button>
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
    var processResult;
    var workflowId = ${applyData.apply.workflowId!0};
    $(document).on("change",".fileUnUse",function(){
        var _this = $(this);
        if ($(this).val() != '') {
            var formData = new FormData();
            var file = $(this)[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            var dataUrl;
            if(file.type == 'application/pdf'){
                $("#loadingDiv").show();
                $.ajax({
                    url: '/mobile/file/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var content = _this.data('content');
                            _this.closest('.fileUnUseSpan').append('<span class="dataUrl" style="color:#43a1da;margin-left:20px;cursor: pointer;" data-url="'+data.fileUrl+'">'+/*file.name*/content+'</span>');
                            alert("上传成功");
                            $("#loadingDiv").hide();
                        } else {
                            alert("上传失败");
                            $("#loadingDiv").hide();
                        }
                    }
                });
            }else{
                alert('请选择pdf格式文件');
                return
            }
        }
    });

    $(document).on('change','.auditOpinion',function(){
        if($(this).val() == 1){
            $('#processNote').attr('readOnly','addOnly');
            processResult = 1;
        }else{
            $('#processNote').removeAttr('readOnly','addOnly');
            processResult = 2;
        }
    });
    $(document).on('click','.btn-primary',function(){
        processFunction(processResult, workflowId);
    });
    function processFunction(processResult, workflowId){
        if($("input[name='auditOpinion']:checked").val() == 2) {
            var processNote = $('#processNote').val();
            if (processNote == "") {
                alert("请填写处理意见！");
                return;
            }
        }
        if(processResult !=1 && processResult != 2){
            alert('请选择处理意见');
            return;
        }
        if(confirm("确认" + (processResult == 1? "通过":"驳回") + "该请求吗？")){
            if(processResult == 1){
                $.post("/apply/data_report/add_document.vpage",{
                    workflowId:workflowId,
                    firstDocument:$('.firstDocument .dataUrl').data('url'),
                    secondDocument:$('.secondDocument .dataUrl').data('url')
                },function(data){
                    if(data.success){
                        $.post('process.vpage',{
                            processResult:processResult,
                            workflowId:workflowId,
                            processNote:'同意'
                        },function(data){
                            if(data.success){
                                location.href = "list.vpage";
                            }else{
                                alert(data.info)
                            }
                        });
                    }else{
                        alert(data.info)
                    }
                });
            }else if(processResult == 2){
                $.post('process.vpage',{
                    processResult:processResult,
                    workflowId:workflowId,
                    processNote:processNote
                },function(data){
                    if(data.success){
                        location.href = "list.vpage";
                    }else{
                        alert(data.info)
                    }
                });
            }
        }
    }

</script>

</@layout_default.page>
