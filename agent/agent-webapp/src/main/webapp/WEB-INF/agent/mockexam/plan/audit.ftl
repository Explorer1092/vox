<#--此功能移植自agent 统考申请-->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='测评详情' page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/fileuploader/SimpleAjaxUploader.min.js"></script>
<style>
    body{
        text-shadow:none;
    }
    select.s_time{
        width: 60px;;
    }
    .radio input[type="radio"], .checkbox input[type="checkbox"]{
        float: left;
        margin-left: -7px;
    }
    .form-horizontal .controls{
        padding-top: 5px;
    }
    .apply_input_time{
        width: 80px;
    }
    .show{
        display: none;}
    .achievement td{margin:0 6px 5px 6px;}
</style>
    <#macro forOption start=0 end=0 defaultVal=0>
        <#list start..end as index>
        <option value="<#if index lt 10>0</#if>${index}" <#if defaultVal == index>selected</#if>><#if index lt 10>0</#if>${index}</option>
        </#list>
    </#macro>
<div class="row-fluid">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i>测评基本信息</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#assign messages = requestContext.getAlertMessageManager().getMessages() />
        <#list messages as msg>
            <#if msg.content?? >
                <div class="alert alert-${(msg.category)!''}">${(msg.content)!''}</div>
            </#if>
        </#list>
        <script type="text/javascript">
            ${requestContext.getAlertMessageManager().clearMessages()};
        </script>
        <#if plan?has_content>
            <div class="box-content">
                <div class="form-horizontal">
                    <input type="hidden" id="targetSchoolId" name="targetSchoolId" />
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">测评名称</label>
                            <div class="controls">
                            ${plan.name!''}
                            </div>
                        </div>
                        <div class="control-group">
                            <div  style="display: inline-block;">
                                <label class="control-label">测评学科</label>
                                <div class="controls">
                                    <#if subject?has_content && subject?size gt 0>
                                    <#list subject as i>
                                        <#if (plan.subject!'') == (i.key!'')>${i.value}</#if>
                                    </#list>
                                </#if>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评类型</label>
                            <div class="controls">
                                <#list type as i>
                                    <#if plan.type?? && plan.type?split(",")?size gt 0>
                                    <#list plan.type?split(",") as _type>
                                        <#if _type == (i.key!'')>${i.value}</#if>
                                    </#list>
                                </#if>
                                </#list>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评年级</label>
                            <div class="controls">
                                <div id="junior_div">
                                    <#if grade?has_content && grade?size gt 0>
                                        <#list grade as i>
                                        <#if (plan.grade!'') == (i.key!'')>${i.value}</#if>
                                    </#list>
                                    </#if>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评形式</label>
                            <div class="controls">
                                <#if form?has_content && form?size gt 0>
                                    <#list form as i>
                                    <#if (plan.form!'') == (i.key!'')>${i.value}</#if>
                                </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <div id="teaching_material_div" style="display: inline-block;">
                                <div style="display: inline-block;">
                                    <label class="control-label">使用教材</label>
                                    <div class="controls">
                                    ${plan.bookName!''}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">试卷</label>
                            <div class="controls">
                                <#if paperType?has_content && form?size gt 0>
                                    <#list paperType as i>
                                    <#if plan.paperType?? && plan.paperType == (i.key!'')>${i.value}</#if>
                                </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="flieContainer" <#if plan.paperType?? &&  plan.paperType== "OLD"> style="display: none;" </#if> >
                            <div class="control-group" id="sourceExcelFile_div">
                                <label class="control-label">上传试卷</label>
                                <#if plan.paperDocUrls?has_content>
                                    <#list plan.paperDocUrls?split(",") as address>
                                        <div class="controls">
                                            <a id = "paperDocUrls_a" class="paperDocUrls_a" href="${address!}">点击下载查看详情</a>
                                        </div>
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group" id= "useOldTestPaper_div" <#if plan.paperType?? &&  plan.paperType== "NEW"> style="display: none;" </#if> >
                            <div style="display: inline-block;">
                                <label class="control-label">试卷ID</label>
                                <div class="controls">
                                    <input type="text" id="testPaperId" name ="testPaperId" value="${plan.paperId!}" placeholder="多个试卷Id按照逗号分隔" disabled="disabled">
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">获取试卷方式</label>
                            <div class="controls">
                                <#if distributeType?has_content && distributeType?size gt 0>
                                    <#list distributeType as i>
                                    <#if plan.distributeType?? && plan.distributeType == (i.key!'')>${i.value}</#if>
                                </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评场景</label>
                            <div class="controls">
                                <#if scene?has_content && scene?size gt 0>
                                    <#list scene as i>
                                    <#if plan.scene?? && plan.scene == (i.key!'')>${i.value}</#if>
                                </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评级别</label>
                            <div class="controls">
                                <#if regionLevel?has_content && regionLevel?size gt 0>
                                    <#list regionLevel as i>
                                    <#if (plan.regionLevel!'') == (i.key!"")>${i.value}</#if>
                                </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group" >
                            <label class="control-label">已选地区</label>
                            <div class="controls" id="codeInfo"></div>
                        </div>
                    <#--<#if plan.regionLevel == "school">-->
                    <#--<div id="schoolinfo_div" >-->
                    <#--<table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">-->
                    <#--<tbody id="schoolinfo_tbody">-->
                    <#--<#list schoolList as school>-->
                    <#--<tr class='schoolinfo' data-id="${school.schoolId}">-->
                    <#--<td class="js-schoolIds">${school.schoolId}</td>-->
                    <#--<td >${school.schoolName}</td>-->
                    <#--</tr>-->
                    <#--</#list>-->
                    <#--</tbody>-->
                    <#--</table>-->
                    <#--</div>-->
                    <#--</#if>-->
                        <#if plan.schoolIds??>
                            <div id="unifiedExamSchool_div" class="control-group">
                                <label class="control-label">测评学校</label>
                                <div class="controls">
                                    <table class="table table-bordered table-striped" style="width: 50%;margin-left: 1%">
                                        <thead>
                                        <tr>
                                            <td>学校ID</td>
                                            <td>学校名称</td>
                                        </tr>
                                        </thead>
                                        <tbody id="schoolinfo_tbody">
                                            <#list plan.schoolIds?split(",") as item>
                                            <tr>
                                                <td>${item!'--'}</td>
                                                <#list plan.schoolNames?split(",") as z>
                                                    <#if (z_index!0) == (item_index!0)>
                                                        <td>${z!'--'}</td>
                                                    </#if>
                                                </#list>
                                            </tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </#if>
                        <#if (plan.status!'') == 'EXAM_PUBLISHED' || (plan.status!'') == 'EXAM_OFFLINE' || (plan.status!'') == 'PAPER_READY'>
                            <div class="control-group">
                                <label class="control-label">考试ID</label>
                                <div class="controls">
                                ${plan.examId!''}
                                </div>
                            </div>
                        </#if>
                        <div class="control-group" disabled="true">
                            <div style="display: inline-block">
                                <label class="control-label">考试开始时间:</label>
                                <div class="controls">
                                ${plan.startTime?string('yyyy-MM-dd HH:mm:ss')}
                                </div>
                            </div>
                            <div style="display: inline-block">
                                <label class="control-label"> 考试截止时间：</label>
                                <div class="controls">
                                ${plan.endTime?string('yyyy-MM-dd HH:mm:ss')}
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">是否允许教师查看成绩</label>
                            <div class="controls">
                                <#if (plan.allowTeacherQuery!'') == 'Y'>是</#if>
                                <#if (plan.allowTeacherQuery!'') == 'N'>否</#if>
                            </div>
                        </div>
                        <#if (plan.subject!'') == 'ENGLISH'>
                            <#if (plan.allowTeacherQuery!'') == 'Y'>
                                <div class="control-group">
                                    <label class="control-label">是否允许教师修改系统成绩</label>
                                    <div class="controls">
                                        <#if (plan.allowTeacherModify!'') == 'Y'>是</#if>
                                        <#if (plan.allowTeacherModify!'') == 'N'>否</#if>
                                    </div>
                                </div>
                            </#if>
                        </#if>
                        <div class="control-group">
                            <label class="control-label">老师判分截止时间</label>
                            <div class="controls">
                            ${plan.teacherMarkDeadline?string('yyyy-MM-dd HH:mm:ss')}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">是否允许学生查看成绩</label>
                            <div class="controls">
                                <#if (plan.allowStudentQuery!'') == 'Y'>是</#if>
                                <#if (plan.allowStudentQuery!'') == 'N'>否</#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩发布时间</label>
                            <div class="controls">
                            ${plan.scorePublishTime?string('yyyy-MM-dd HH:mm:ss')}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">允许交卷时间</label>
                            <div class="controls">
                            ${plan.finishExamTime!0} 分钟后
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">答题时长</label>
                            <div class="controls">
                            ${plan.examTotalTime!0} 分钟
                            </div>
                        </div>
                        <#if (plan.subject!'') == 'ENGLISH'>
                            <div class="control-group">
                                <label class="control-label">口语算分逻辑</label>
                                <div class="controls">
                                    <#if spokenScoreType?has_content && spokenScoreType?size gt 0>
                                    <#list spokenScoreType as i>
                                        <#if (plan.spokenScoreType!'') == i.key>${i.value}</#if>
                                    </#list>
                                </#if>
                                </div>
                            </div>
                        </#if>
                        <div id = "spokenAnswerTimes_div" class="control-group" <#if plan.type??><#list plan.type?split(",") as item><#if item == "ORAL"><#else>style="display:none;" </#if></#list></#if>>
                            <label class="control-label">口语可答题次数</label>
                            <div class="controls">
                                <#if spokenTimes?has_content && spokenTimes?size gt 0>
                                <#list spokenTimes as i>
                                    <#if (plan.spokenTimes!'') == i.key>${i.value}</#if>
                                </#list>
                            </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩类型</label>
                            <div class="controls">
                                <#if scoreRuleType?has_content && scoreRuleType?size gt 0>
                                <#list scoreRuleType as i>
                                    <#if (plan.scoreRuleType!'') == i.key>${i.value}</#if>
                                </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">试卷总分</label>
                            <div class="controls">
                            ${plan.totalScore!}分
                            </div>
                        </div>
                        <div id="achievement" class="control-group grading">
                            <label class="control-label">等第设置</label>
                            <div class="controls">
                                <#if plan.scoreRuleType??>
                                    <table id="levelContainer" class="achievement">
                                        <table class="achievement">
                                            <#if plan.scoreRule?? &&  plan.scoreRule?size gt 0>
                                                <#list plan.scoreRule as list>
                                                    <tr class="achievementGrading">
                                                        <td>等第名称</td>
                                                        <td><input class="achievement_name" type="text" style="width:50px" value="${list.rankName!''}" disabled="disabled"></td>
                                                        <td><input class="initialScore" type="number" style="width:50px" value="${list.bottom!''}" disabled="disabled"> %</td>
                                                        <td><=分数区间<<#if list_index == 0>=</#if></td>
                                                        <td><input class="endResults" type="number" style="width:50px" value="${list.top!''}" disabled="disabled"> %</td>
                                                    </tr>
                                                </#list>
                                            </#if>
                                        </table>
                                    </table>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">上传批文/凭证</label>
                            <div class="controls">
                                <table class="file_table">
                                    <#if plan.attachmentFiles?has_content>
                                        <#list plan.attachmentFiles as file>
                                            <tr class="file_tr">
                                                <td><a href="${file.fileUrl!}" download="${file.fileName!'附件'}">${file.fileName!}</a></td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">备注</label>
                            <div class="controls">
                            ${plan.comment!}
                            </div>
                        </div>
                        <#if plan.logs?has_content>
                            <div class="control-group operationLogs">
                                <label class="control-label">操作记录</label>
                                <div class="controls">
                                    <div class="dataTables_wrapper" role="grid">
                                        <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 80%">
                                            <thead>
                                            <tr>
                                                <th class="sorting" style="width: 60px;">操作时间</th>
                                                <th class="sorting" style="width: 60px;">操作人</th>
                                                <th class="sorting" style="width: 60px;">描述</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                                <#if plan.logs?has_content && plan.logs?size gt 0>
                                                    <#list plan.logs as logs>
                                                    <tr>
                                                        <td><#if logs.date??>${logs.date?string("yyyy-MM-dd hh:mm:ss")!}</#if></td>
                                                        <td>${logs.operatorName!}</td>
                                                        <td>${logs.desc!}</td>
                                                    </tr>
                                                    </#list>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="control-label">审核</label>
                            <div class="controls">
                                <input class="option" name="option" type="radio" value="APPROVE" checked>通过
                                <input class="option" name="option" type="radio" value="REJECT">驳回
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">审核备注</label>
                            <div class="controls">
                                <textarea name="note" id="note" cols="30" rows="10"></textarea>
                            </div>
                        </div>
                    </fieldset>
                </div>
            </div>
        <#else>
        </#if>
        <div class="form-actions" style="text-align: center">
            <button id="submitBtn" type="button" class="btn btn-primary need_auditing" style=" margin-right: 20px">提交</button>
            <button id="cancelBtn" type="button" class="btn btn-primary" style=" margin-left: 20px">取消</button>
        </div>
    </div>
</div>
<script type="text/html" id="choicedCode">
    <%for(var i = 0;i< list.length;i++){%>
    <div style="float: left;padding:2px 5px;margin-left:15px;border:1px solid #000;cursor:pointer" class="choicedCode" data-info="<%=list[i]%>">
        <%=obj[list[i]]%>
    </div>
    <%}%>
</script>
<script type="text/html" id="books">
    <%for(var i = 0;i< res.length;i++){%>
    <option name="bookCatalogId" value = "<%=res[i].id%>"><%=res[i].name%></option>
    <%}%>
</script>
<script type="text/javascript">
    var codeList = "${plan.regionCodes!''}".split(",");
    var nameList = "${plan.regionNames!''}".split(",");
    var codeObj = {};
    console.log(codeList);
    if(codeList.length != 0){
        codeList.forEach(function (v,t) {
            codeObj[v] = nameList[t]
        });
        $('#codeInfo').html(template("choicedCode",{obj:codeObj,list:codeList}));
    }
    $(document).on("click","#submitBtn",function () {
            var _id = getUrlParam('id');
            var option = $("input[name='option']:checked").val();
            var note = $("#note").val();
            $.ajax({
                url: '/mockexam/plan/audit.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({id:_id,option:option,note:note}),
                async: false,
                success:function (res) {
                    if(res.success){
                        alert('审核成功');
                        window.history.back();
                    }else{
                        alert(res.info)
                    }
                }
            });
    });
    $(document).on("click","#cancelBtn",function () {
        window.history.back();
    });
</script>
</@layout_default.page>
