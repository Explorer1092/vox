<#--此功能移植自agent 统考申请-->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='编辑测评信息' page_num=3>
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
        margin-left: 0;
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
<div id="realSubmit" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body">
                <label id="checkResultInfo"></label>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="realSubmitBtn" type="button" class="btn btn-large btn-primary">继续提交</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">去修改</button>
                </div>
            </div>
        </div>
    </div>
</div>
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
                    <input type="hidden" id="status" name ="status" value="${plan.status!''}">
                    <input type="hidden" id="isAdmin" name="isAdmin" value="<#if isAdmin?? && isAdmin == true>true<#else>false</#if>">
                    <fieldset>
                        <input id="id" value="${plan.id!0}" type ="hidden">
                        <input id="workflowId" value="${plan.id!0}" type ="hidden">
                        <div class="control-group">
                            <label class="control-label">测评名称</label>
                            <div class="controls">
                                <input type="text" id="name" name ="name" placeholder="考试名称" value="${plan.name!''}" <#if editable?? && !editable.name>disabled="disabled"</#if>>
                            </div>
                        </div>
                        <div class="control-group">
                            <div  style="display: inline-block;">
                                <label class="control-label">测评学科</label>
                                <div class="controls">
                                    <#if subject?? && subject?size gt 0>
                                        <#list subject as i>
                                            <input class="subject" type="radio"  name ="subject" value="${i.key}" <#if (plan.subject!'') == (i.key!'')>checked</#if> <#if editable?? && !editable.subject>disabled="disabled"</#if>> ${i.value}
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评类型</label>
                            <div class="controls">
                                <#if type?? && type?size gt 0>
                                    <#list type as i>
                                        <span class="testType">
                                            <input class="type" type="checkbox"  name ="type" value="${i.key}" <#if plan.type??><#list plan.type?split(",") as item><#if item == (i.key!"")>checked</#if></#list></#if> <#if editable?? && !editable.type>disabled="disabled"</#if>> ${i.value}
                                        </span>
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评年级</label>
                            <div class="controls">
                                <div id="junior_div">
                                    <#if grade?? && grade?size gt 0>
                                        <#list grade as i>
                                            <input type="radio"  name ="grade" value="${i.key}" <#if (plan.grade!'') == (i.key!'')>checked</#if> <#if editable?? && !editable.grade>disabled="disabled"</#if>> ${i.value}
                                        </#list>
                                    </#if>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评形式</label>
                            <div class="controls">
                                <#if form?? && form?size gt 0>
                                    <#list form as i>
                                        <input type="radio"  name ="form" value="${i.key}" <#if (plan.form!'') == (i.key!'')>checked</#if> <#if editable?? && !editable.form>disabled="disabled"</#if>> ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <div id="teaching_material_div" style="display: inline-block;">
                                <div style="display: inline-block;">
                                    <label class="control-label">使用教材</label>
                                    <div class="controls">
                                        <select id = "bookCatalogId" >
                                        </select>
                                    </div>
                                </div>
                                <div style="display: inline-block;">
                                    <input id="searchBookName" placeholder="输入教材名称快速定位"  <#if editable?? && !editable.book>disabled="disabled"</#if>>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">试卷</label>
                            <div class="controls">
                                <select id = "paperType" >
                                    <#if paperType?? && paperType?size gt 0>
                                        <#list paperType as i>
                                            <option name ="paperType" value="${i.key}"  <#if plan.paperType?? && plan.paperType == (i.key!'')>selected</#if>  <#if editable?? && !editable.paper>disabled="disabled"</#if>> ${i.value}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="flieContainer" id="sourceExcelFile_div" <#if plan.paperType?? &&  plan.paperType== "OLD"> style="display: none;" </#if> >
                            <div class="control-group">
                                <label class="control-label">上传试卷</label>
                                    <div class="controls" <#if editable?? && !editable.paperDoc> style="display: none;" </#if>>
                                        <button id="upload_file_but" type="button" class="btn btn-primary upload_file_but">上传文件</button>
                                        <input id="paperDocUrls" class="paperDocUrls" type="hidden">
                                        <input id="paperDocUrls" class="paperDocNames" type="hidden">
                                        <a id = "paperDocUrls_a" class="paperDocUrls_a" href="" style="display: none;">点击下载查看详情</a>
                                        <button type="button" class="btn btn-primary btnFile do_add_upload">添加</button>
                                        <button type="button" class="btn btn-primary btnFile do_remove_upload">删除</button>
                                    </div>
                                <#if plan.paperDocUrls?has_content>
                                    <#list plan.paperDocUrls?split(",") as address>
                                        <div class="controls">
                                            <#if editable?? && editable.paperDoc>
                                                <button id="upload_file_but" type="button" class="btn btn-primary upload_file_but">上传文件</button>
                                            </#if>
                                            <input id="paperDocUrls" class="paperDocUrls" type="hidden" value="${address!}">
                                            <input id="paperDocUrls" class="paperDocNames" type="hidden" value="${plan.paperDocNames?split(',')[address_index]!}">
                                            <a  id = "paperDocUrls_a" class="paperDocUrls_a" href="${address!}">${plan.paperDocNames?split(',')[address_index]!}</a>
                                            <#if editable?? && editable.paperDoc>
                                                <button type="button" class="btn btn-primary btnFile do_add_upload">添加</button>
                                                <button type="button" class="btn btn-primary btnFile do_remove_upload">删除</button>
                                            </#if>
                                        </div>
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group" id= "useOldTestPaper_div" <#if plan.paperType?? &&  plan.paperType== "NEW"> style="display: none;" </#if> >
                            <div style="display: inline-block;">
                                <label class="control-label">试卷ID</label>
                                <div class="controls">
                                    <input type="text" id="testPaperId" name ="testPaperId" value="${plan.paperId!}" placeholder="多个试卷Id按照逗号分隔"  <#if editable?? && !editable.paper>disabled="disabled"</#if>>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">获取试卷方式</label>
                            <div class="controls">
                                <#if distributeType?? && distributeType?size gt 0>
                                    <#list distributeType as i>
                                        <input type="radio"  name ="distributeType" value="${i.key}" <#if plan.distributeType?? && plan.distributeType == (i.key!'')>checked</#if>  <#if editable?? && !editable.distributeType>disabled="disabled"</#if>> ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评场景</label>
                            <div class="controls">
                                <#if scene?? && scene?size gt 0>
                                    <#list scene as i>
                                        <input type="radio"  name ="scene" value="${i.key}"  <#if plan.scene?? && plan.scene == (i.key!'')>checked</#if>  <#if editable?? && !editable.scene>disabled="disabled"</#if>> ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div id="patternDiv" class="control-group">
                            <label class="control-label">测评模式</label>
                            <div class="controls">
                                <#list pattern as i>
                                    <input type="radio" id="pattern" name ="pattern" value="${i.key}" <#if plan.pattern?? && plan.pattern == (i.key!'')>checked</#if>> ${i.value}
                                </#list>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">测评级别</label>
                            <div class="controls">
                                <#if regionLevel?? && regionLevel?size gt 0>
                                    <#list regionLevel as i>
                                        <input type="radio"  name ="regionLevel" value="${i.key}" <#if (plan.regionLevel!'') == (i.key!"")>checked</#if> <#if editable?? && !editable.region>disabled="disabled"</#if>> ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div id="province_div" class="control-group" >
                            <label class="control-label">省</label>
                            <div class="controls">
                                <select id ="provinceCode">
                                    <option name="provinceName" value = "" >请选择</option>
                                    <#if provinces?? && provinces?size gt 0>
                                        <#list provinces as provinceInfo>
                                            <option name="provinceName" value = "${provinceInfo.id}"  <#if editable?? && !editable.region>disabled="disabled"</#if>>${provinceInfo.name}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div id="city_div" class="control-group" <#if (plan.regionLevel!'') == 'PROVINCE'>style="display: none"</#if>>
                            <label class="control-label">市</label>
                            <div class="controls">
                                <select id ="cityCode">
                                    <option name="cityName" value = "" >请选择</option>
                                </select>
                            </div>
                        </div>
                        <div id="county_div" class="control-group" <#if (plan.regionLevel!'') != 'COUNTY'>style="display: none"</#if>>
                            <label class="control-label">区域</label>
                            <div class="controls">
                                <select id ="countyCode">
                                    <option name="countyName" value = "" >请选择</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group" >
                            <label class="control-label">已选地区</label>
                            <div class="controls" id="codeInfo"></div>
                        </div>

                        <div <#if editable?? && !editable.region>style="display: none"</#if>>
                            <div id="unifiedExamSchool_div" class="control-group" <#if plan.regionLevel != "SCHOOL">style="display: none"</#if>>
                                <label class="control-label">测评学校</label>
                                <div class="controls">
                                    <input id="unifiedExamSchoolView" name ="unifiedExamSchoolView" type="hidden" readonly>
                                    <input id="unifiedExamSchool" name ="unifiedExamSchool" type="hidden" readonly>
                                    <input id="addSchoolBtn" class ="addSchoolBtn" type="button" value="添加" data-type ="batch">
                                    <input id ="agentUserId" value="${agentUserId!''}" type="hidden" >
                                </div>
                            </div>
                            <div id="schoolinfo_div" >
                                <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                                    <tbody id="schoolinfo_tbody">
                                        <#if plan.regionLevel == "SCHOOL">
                                            <#if plan.schoolIds?has_content>
                                                <#list plan.schoolIds?split(",") as item>
                                                <tr class='schoolinfo' id='schoolInfo_${item!0}' data-id='${item!0}'>
                                                    <td>${item!'--'}</td>
                                                    <#list plan.schoolNames?split(",") as z>
                                                        <#if (z_index!0) == (item_index!0)>
                                                            <td class="oldschoolNames">${z!'--'}</td>
                                                            <#if editable?? && editable.region>
                                                                <td class='delete-schoolinfo' data-id='"+id+"'>
                                                                    <a class ='delete_data' data-name='${z!'--'}' data-id='${item!'--'}' href='javascript:void(0)'>删除</a>
                                                                </td>
                                                            </#if>
                                                        </#if>
                                                    </#list>
                                                </tr>
                                                </#list>
                                            </#if>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="control-group" disabled="true">
                            <div style="display: inline-block">
                                <label class="control-label">考试开始时间:</label>
                                <div class="controls">
                                    <input type="text" id="startTime" name ="startTime" value = "${plan.startTime?string('yyyy-MM-dd')}"   class="apply_input_time">
                                    <select class="s_time" name="unifiedExamBeginTimeHour" id="unifiedExamBeginTimeHour">
                                        <#assign beginTime_HH = plan.startTime?string("HH")>
                                            <#assign beginTime_mm = plan.startTime?string('mm')>
                                            <@forOption start=0 end=23 defaultVal= beginTime_HH?number />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamBeginTimeMin" id="unifiedExamBeginTimeMin">
                                        <@forOption start=0 end=59 defaultVal= beginTime_mm?number />
                                    </select> 分
                                </div>
                            </div>
                            <div style="display: inline-block">
                                <label class="control-label"> 考试截止时间：</label>
                                <div class="controls">
                                    <input type="text" id="endTime" name ="endTime" value = "${plan.endTime?string('yyyy-MM-dd')}" class="apply_input_time">
                                    <select class="s_time" name="unifiedExamEndTimeHour" id="unifiedExamEndTimeHour">
                                        <#assign endTime_HH = plan.endTime?string('HH')>
                                            <#assign beginTime_mm = plan.endTime?string('mm')>
                                            <@forOption start=0 end=23 defaultVal= endTime_HH?number />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamEndTimeMin" id="unifiedExamEndTimeMin">
                                        <@forOption start=0 end=59 defaultVal= beginTime_mm?number />
                                    </select> 分
                                </div>
                            </div>
                        </div>
                        <div class="control-group" id="signUpLimitTime">
                            <div style="display: inline-block">
                                <label class="control-label">报名截止时间:</label>
                                <div class="controls">
                                    <input type="text" id="registrationDeadlineTime" name ="registrationDeadlineTime" class="apply_input_time" <#if plan.registrationDeadlineTime??>value ="${plan.registrationDeadlineTime?string('yyyy-MM-dd')}"</#if>>
                                    <select class="s_time" name="registrationDeadlineHour" id="registrationDeadlineHour">
                                        <#if plan.registrationDeadlineTime??>
                                        <@forOption start=0 end=23 defaultVal=plan.registrationDeadlineTime?string("HH")?number />
                                    <#else>
                                            <@forOption start=0 end=23 defaultVal=0 />
                                        </#if>
                                    </select> 时
                                    <select class="s_time" name="registrationDeadlineMin" id="registrationDeadlineMin">
                                        <#if plan.registrationDeadlineTime??>
                                        <@forOption start=0 end=59 defaultVal= plan.registrationDeadlineTime?string('mm')?number />
                                    <#else>
                                            <@forOption start=0 end=59 defaultVal= 0 />
                                        </#if>
                                    </select> 分
                                </div>
                            </div>
                        </div>

                    <#--<div class="control-group">-->
                            <#--<label class="control-label">教师查看试卷时间</label>-->
                            <#--<div class="controls">-->
                                <#--<input type="text" id="teacherQueryTime" name ="teacherQueryTime" value="${plan.teacherQueryTime?string('yyyy-MM-dd')}" class="apply_input_time">-->
                                <#--<select class="s_time" name="teacherQueryTimeHour" id="teacherQueryTimeHour">-->
                                    <#--<#assign teacherQueryTime_HH = plan.teacherQueryTime?string('HH')>-->
                                            <#--<#assign teacherQueryTime_mm = plan.teacherQueryTime?string('mm')>-->
                                            <#--<@forOption start=0 end=23 defaultVal= teacherQueryTime_HH?number />-->
                                <#--</select> 时-->
                                <#--<select class="s_time" name="teacherQueryTimeMin" id="teacherQueryTimeMin">-->
                                    <#--<@forOption start=0 end=59 defaultVal=teacherQueryTime_mm?number/>-->
                                <#--</select> 分-->
                            <#--</div>-->
                        <#--</div>-->
                        <div class="control-group">
                            <label class="control-label">是否允许教师查看成绩</label>
                            <div class="controls">
                                <input type="radio"  name ="allowTeacherQuery" value="Y" <#if (plan.allowTeacherQuery!'') == 'Y'>checked</#if>> 是
                                <input type="radio"  name ="allowTeacherQuery" value="N" <#if (plan.allowTeacherQuery!'') == 'N'>checked</#if>> 否
                            </div>
                        </div>
                        <div class="control-group allowTeacherModify" style="display: none">
                            <label class="control-label">是否允许教师修改系统成绩</label>
                            <div class="controls">
                                <input type="radio"  name ="allowTeacherModify" value="Y" checked> 是
                                <input type="radio"  name ="allowTeacherModify" value="N"> 否
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">老师判分截止时间</label>
                            <div class="controls">
                                <input type="text" id="teacherMarkDeadline" name ="teacherMarkDeadline" value = "${plan.teacherMarkDeadline?string('yyyy-MM-dd')}"  class="apply_input_time">
                                <select class="s_time" name="correctingTestPaperHour" id="correctingTestPaperHour">
                                    <#assign testPaper_HH = plan.teacherMarkDeadline?string('HH')>
                                    <#assign testPaper_mm = plan.teacherMarkDeadline?string('mm')>
                                    <@forOption start=0 end=23 defaultVal= testPaper_HH?number />
                                </select> 时
                                <select class="s_time" name="correctingTestPaperMin" id="correctingTestPaperMin">
                                    <@forOption start=0 end=59 defaultVal=testPaper_mm?number />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">是否允许学生查看成绩</label>
                            <div class="controls">
                                <input type="radio"  name ="allowStudentQuery" value="Y" <#if (plan.allowStudentQuery!'') == 'Y'>checked</#if>> 是
                                <input type="radio"  name ="allowStudentQuery" value="N" <#if (plan.allowStudentQuery!'') == 'N'>checked</#if>> 否
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩发布时间</label>
                            <div class="controls">
                                <input type="text" readonly="readonly"  id="scorePublishTime" name ="scorePublishTime" value = "${plan.scorePublishTime?string('yyyy-MM-dd')}" class="apply_input_time">
                                <select class="s_time" name="achievementReleaseTimeHour" id="achievementReleaseTimeHour">
                                    <#assign releaseTime_HH = plan.scorePublishTime?string('HH')>
                                    <#assign releaseTime_mm = plan.scorePublishTime?string('mm')>
                                    <@forOption start=0 end=23 defaultVal= releaseTime_HH?number />
                                </select> 时
                                <select class="s_time" name="achievementReleaseTimeMin" id="achievementReleaseTimeMin">
                                    <@forOption start=0 end=59 defaultVal= releaseTime_mm?number />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">允许交卷时间</label>
                            <div class="controls">
                                <input type="number" id="finishExamTime" name ="finishExamTime" value = "${plan.finishExamTime}"> 分钟后
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">试卷总答题时长</label>
                            <div class="controls">
                                <input type="number" id="examTotalTime" name ="examTotalTime" value = "${plan.examTotalTime!1}"> 分钟
                            </div>
                        </div>
                        <div class="control-group" id="spokenScoreType">
                            <label class="control-label">口语算分逻辑</label>
                            <div class="controls">
                                <#if spokenScoreType?has_content && spokenScoreType?size gt 0>
                                    <#list spokenScoreType as i>
                                        <input type="radio"  name ="spokenScoreType" value="${i.key}" <#if (plan.spokenScoreType!'') == i.key>checked</#if> > ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div id = "spokenAnswerTimes_div" class="control-group" <#if plan.type??><#list plan.type?split(",") as item><#if item == "ORAL"><#else>style="display:none;" </#if></#list></#if>>
                            <label class="control-label">口语可答题次数</label>
                            <div class="controls">
                                <#if spokenTimes?has_content && spokenTimes?size gt 0>
                                    <#list spokenTimes as i>
                                        <input type="radio"  name ="spokenAnswerTimes" value="${i.key}" <#if plan.spokenAnswerTimes?? && plan.spokenAnswerTimes == i.key>checked</#if>> ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩类型</label>
                            <div class="controls">
                                <#if scoreRuleType?has_content && scoreRuleType?size gt 0>
                                    <#list scoreRuleType as i>
                                        <input type="radio" name ="scoreRuleType" value="${i.key}" <#if plan.scoreRuleType?? && plan.scoreRuleType == (i.key!'')>checked</#if>> ${i.value}
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div id="achievement" class="control-group grading">
                            <label class="control-label">等第设置</label>
                            <div class="controls">
                                <div class="achievement_add btn btn-primary">添加</div>
                                <table class="achievement">
                                    <#if plan.scoreRule?has_content>
                                        <#list plan.scoreRule as rule>
                                            <tr class="achievementGrading">
                                                <td>等第名称</td>
                                                <td><input class="achievement_name" value="${rule.rankName!}" type="text" style="width:50px"></td>
                                                <td><input class="initialScore" type="number" value="${rule.bottom!}" style="width:50px"> %</td>
                                                <td><=分数区间<<#if rule_index == 0>=</#if></td>
                                                <td><input class="endResults" type="number" value="${rule.top!}" style="width:50px"> %</td>
                                                <td class="achievement_remove btn btn-primary">删除</td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">上传批文/凭证</label>
                            <div class="controls">
                                <div>
                                    <span class="file_add btn btn-primary" <#if plan.attachmentFiles?has_content && (plan.attachmentFiles?size >= 5) > style="display: none"</#if> >添加</span>
                                    <span style="color: #ff0000">(请上传教研员批文，通知截图凭证；可支持上传附件格式Word, pdf, jpg, png, 最多5个)</span>
                                </div>
                                <table class="file_table">
                                    <#if plan.attachmentFiles?has_content>
                                        <#list plan.attachmentFiles as file>
                                            <tr class="file_tr">
                                                <td style="width:200px"><a target="_blank" href="${file.fileUrl!}" download="${file.fileName!'附件'}">${file.fileName!}</a><input type="hidden" class="file_info" fileName="${file.fileName!}" fileUrl="${file.fileUrl!}"></td>
                                                <td class="file_remove btn btn-primary">删除</td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">备注</label>
                            <div class="controls">
                                <textarea id="comment"  class="controls" style="width:80%;margin-left: 0px;" placeholder="" maxlength="100">${plan.comment!}</textarea>
                            </div>
                        </div>
                        <div class="form-actions" style="text-align: center">
                            <button id="submitBtn" type="button" class="btn btn-primary need_auditing" style=" margin-right: 20px">提交</button>
                            <button id="cancelBtn" type="button" class="btn btn-primary" style=" margin-left: 20px">取消</button>
                        </div>
                    </fieldset>
                </div>
            </div>
        <#else>
        </#if>
    </div>
    <div id="region_select_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">选择学校</h4>
                    <input id="selectSchoolType" type="hidden" >
                </div>
                <div class="modal-body">
                    <div class="control-group">
                        <textarea id="selectSids"  class="controls" style="width:80%" placeholder="输入学校id,多个学校以“,”分隔" value="2641"></textarea>
                        <button class="btn btn-large btn-primary" type="button" id="addSchooleBtn">查询</button>
                    </div>
                    <div class="control-group" id="alertInfoInDialog" style="color: red;display: none;">
                    </div>
                    <div class="control-group">
                        <div id="schoolTable"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="pull-left">
                        <button id="add_school_submit_btn" type="button" class="btn btn-large btn-primary">确定</button>
                        <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
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
    <option name="bookCatalogId" value = "<%=res[i].id%>"  <#if editable?? && !editable.book>disabled="disabled"</#if> <%if(res[i].name == "${plan.bookName!''}"){%>selected<%}%>><%=res[i].name%></option>
    <%}%>
</script>
<script type="text/javascript">
    var createDatetime = "${plan.createDatetime?string('yyyy/MM/dd HH:mm:ss')}";
    var codeList = "${plan.regionCodes!''}".split(",");
    var nameList = "${plan.regionNames!''}".split(",");
    var codeObj = {};
    codeList.forEach(function (v,t) {
        codeObj[v] = nameList[t]
    });
    $('#codeInfo').html(template("choicedCode",{obj:codeObj,list:codeList}));
    $(function(){


        $("#spokenAnswerTimes_div").hide();
        $("#spokenScoreType").hide();

        function showTestType(){//显示隐藏测评类型
            if($('input[name=subject]:checked').val() == 'ENGLISH'){
                $('.testType').each(function(){
                    if($(this).find('input').val() == "GENERAL" || $(this).find('input').val() == "SPOKEN" || $(this).find('input').val() == "AUDITION"){
                        $(this).show();
                    }else {
                        $(this).hide();
                        $(this).find('input').prop("checked",false).parent().removeClass("checked");
                    }
                });
            }else if($('input[name=subject]:checked').val() == 'MATH'){
                $('.testType').each(function(){
                    if($(this).find('input').val() == "GENERAL" || $(this).find('input').val() == "CALCULATION"){
                        $(this).show();
                    }else{
                        $(this).hide();
                        $(this).find('input').prop("checked",false).parent().removeClass("checked");
                    }
                });
            }else if($('input[name=subject]:checked').val() == 'CHINESE'){
                $('.testType').each(function(){
                    if($(this).find('input').val() == "GENERAL"){
                        $(this).show();
                    }else {
                        $(this).hide();
                        $(this).find('input').prop("checked",false).parent().removeClass("checked");
                    }
                });
            }
        }

        showTestType();//页面初始化时显示隐藏测评类型
        <#if isAdmin?? && !isAdmin>
            $('#patternDiv').remove();
            $('#signUpLimitTime').remove();
        </#if>

        function patternStatus() {
            var status = $('#status').val();
            if(status == "EXAM_PUBLISHED" || status == 'EXAM_OFFLINE') {
                $('input[name="pattern"]').attr("disabled",true);
                $('input[name="pattern"]').attr("disabled","disabled");
            }
        }

        patternStatus();//如果已上线不可修改测评模式

        $(document).on('click', '#pattern', function () {
            var status = $('#status').val();
            if(status == "EXAM_PUBLISHED" || status == 'EXAM_OFFLINE') {
                alert("已上线的测评，测评模式不能更改！");
                return false;
            }
            var _pattern =  $('input[name="pattern"]:checked').val();
            if(_pattern == 'GENERAL'){
                $('#signUpLimitTime').hide();
            } else {
                $('#signUpLimitTime').show();
            }
        });

        function showSignUpLimitTime(){//显示隐藏考试截止时间
            var isAdmin = $('#isAdmin').val();
            if(isAdmin == 'true') {
                var _pattern =  $('input[name="pattern"]:checked').val();
                if(_pattern == 'GENERAL'){
                    $('#signUpLimitTime').hide();
                } else {
                    $('#signUpLimitTime').show();
                }
            }
        }
        showSignUpLimitTime();//页面初始化时显示隐藏考试截止时间

        function getBooks(subject,name){
            $.ajax({
                url: '/mockexam/refer/books.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({subject:subject,q:name}),
                async: false,
                success: function (data) {
                    if (!data.success) {
                        alert(data.errorDesc);
                    } else {
                        $('#bookCatalogId').html(template('books',{res:data.data}))
                    }
                }
            });
        };
        getBooks($('input[name=subject]:checked').val());
        $('#searchBookName').on("change",function(){
            getBooks($('input[name=subject]:checked').val(),$('#searchBookName').val())
        });
        $(document).on("click",".achievement_add",function(){
            var trs = $(".achievement").find("tr");
            var trd_html = '<tr class="achievementGrading">'
                    + '<td>等第名称</td>'
                    + '<td><input class="achievement_name" value="" type="text" style="width:50px"></td>'
                    + '<td><input class="initialScore" type="number" value="" style="width:50px"> %</td>'
                    + (trs.length == 0 ? '<td><=分数区间<=</td>' : '<td><=分数区间<</td>')
                    + '<td><input class="endResults" type="number" value="" style="width:50px"> %</td>'
                    + '<td class="achievement_remove btn btn-primary">删除</td>'
                    + '</tr>';
            $(".achievement").append(trd_html);
            if($(".achievement tr").length>9){
                $('.achievement_add').hide();
            }
        });

        $(document).on('click', '.achievement_remove', function () {
            $(this).parent().remove();
            if($('.achievement tr').length < 10){
                $('.achievement_add').show();
            }
        });

        $(document).on("click",".file_add",function(){
            var tr_html = '<tr class="file_tr">'
                    + '<td style="width:200px"><input type="file" class="file_upload_btn" accept="application/msword,application/pdf,image/jpeg,image/png,application/vnd.openxmlformats-officedocument.wordprocessingml.document"><input type="hidden" class="file_info"></td>'
                    + '<td class="file_remove btn btn-primary">删除</td>'
                    + '</tr>';
            $(".file_table").append(tr_html);
            if($(".file_table tr").length > 4){
                $('.file_add').hide();
            }
        });

        $(document).on("change", ".file_upload_btn", function () {
            var $this = $(this);
            if ($this.val() != '') {
                var formData = new FormData();
                var file =$this.get(0).files[0];
                var name = file.name;
                formData.append('file', file);
                formData.append('file_size', file.size);
                formData.append('file_type', file.type);
                if(file.type != '' && file.type != 'application/msword' && file.type != 'application/pdf' && file.type != 'image/jpeg' && file.type != 'image/png' && file.type != 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'){
                    if(this.outerHTML){
                        this.outerHTML = this.outerHTML;
                    }else {
                        this.value = "";
                    }
                    alert("不支持该文件格式！");
                    return false;
                }
                $.ajax({
                    url: '/file/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var fileUrl = data.fileUrl;
                            $this.next('.file_info').attr("fileName", name);
                            $this.next('.file_info').attr("fileUrl", fileUrl);
                            $this.parent().append('<a target="_blank" href="' + fileUrl + '" download="' + name + '">' + name + '</a>');
                            $this.remove();
                            layer.alert("上传成功");
                        } else {
                            layer.alert("上传失败");
                        }
                    }
                });
            }
        });

        $(document).on('click', '.file_remove', function () {
            $(this).parent().remove();
            if($('.file_table tr').length < 5){
                $('.file_add').show();
            }
        });

        function initSelect(id, name){
            var rlstr = '<option  name="' + name + '" value = "">请选择</option>';
            $("#" + id).html(rlstr);
        }

        function setRegionList(id, name, regionList){
            var rlstr = '<option  name="' + name + '" value = "">请选择</option>';
            for (var i = 0; i < regionList.length; i++) {
                rlstr += '<option  name="' + name + '" value = "' + regionList[i].id + '">' + regionList[i].name + '</option>';
            }
            $("#" + id).html(rlstr);
        }

        //城市联动设置
        function renderCity(){
            var _provinceCode = $('#provinceCode').val();
            if(_provinceCode != ''){
                var regionLevel = $("input[name='regionLevel']:checked").val()
                if(regionLevel != "PROVINCE"){
                    initSelect("countyCode", "countyName");
                    $.get('/mockexam/refer/region/cities.vpage?provinceCode=' + _provinceCode, function (res) {
                        if (res.success) {
                            var regionList = res.data;
                            setRegionList("cityCode", "cityName", regionList)
                        } else {
                            alert(data.info);
                        }
                    })
                }
            }else {
                initSelect("cityCode", "cityName");
                initSelect("countyCode", "countyName");
            }
        }

        //城市联动设置
        function renderCounty(){
            var _cityCode = $('#cityCode').val();
            if(_cityCode != ''){
                var regionLevel = $("input[name='regionLevel']:checked").val()
                if(regionLevel == "COUNTY"){
                    $.get('/mockexam/refer/region/counties.vpage?cityCode=' + _cityCode, function (res) {
                        if (res.success) {
                            var regionList = res.data;
                            setRegionList("countyCode", "countyName", regionList)
                        } else {
                            alert(data.info);
                        }
                    })
                }
            }else{
                initSelect("countyCode", "countyName");
            }
        }

        function addSelectedRegion(id){
            var _value = $("#" + id).val();
            if(_value != ''){
                var _name = $("#" + id + " option:selected").text();
                if(codeList.indexOf(_value) == -1){
                    codeList.push(_value);
                    nameList.push(_name);
                    codeObj[_value] = _name;
                    $('#codeInfo').html(template("choicedCode",{obj:codeObj,list:codeList}))
                }
            }
        }

        $("#provinceCode").change(function(){
            renderCity();
            if($("input[name='regionLevel']:checked").val() == 'PROVINCE'){
                addSelectedRegion("provinceCode");
            }
        });

        $("#cityCode").change(function(){
            renderCounty();
            var regionLevelValue = $("input[name='regionLevel']:checked").val();
            if(regionLevelValue == 'CITY' || regionLevelValue == 'SCHOOL'){
                addSelectedRegion("cityCode");
            }
        });

        $("#countyCode").change(function(){
            if($("input[name='regionLevel']:checked").val() == 'COUNTY') {
                addSelectedRegion("countyCode");
            }
        });

        <#if editable?? && editable.region>
            $(document).on('click','.choicedCode',function(){
                var _info = $(this).data('info').toString();
                var _name = $(this).html().trim();
                codeList.splice(codeList.indexOf(_info),1);
                nameList.splice(nameList.indexOf(_name),1);
                if(codeList.length == 0){
                    $('#provinceCode option:first').prop("selected", 'selected');
                    renderCity();
                }
                $('#codeInfo').html(template("choicedCode",{obj:codeObj,list:codeList}))
            });
        </#if>
        /**
         * 教材快速定位
         * */
        $("#bookId-select").on('change',function(){
            var $select_val = $.trim($(this).val());
            var  $bookCatalogs = $("option[name='bookId']");
            if(!$select_val){
                for(var bcl= 0; bcl < $bookCatalogs.length; bcl++){
                    $($bookCatalogs[bcl]).show();
                }
            }else{
                var showBookCatalog=[];
                for(var bcl= 0; bcl < $bookCatalogs.length; bcl++){
                    if($($bookCatalogs[bcl]).text().indexOf($select_val) < 0){
                        $($bookCatalogs[bcl]).attr("selected",false);
                        $($bookCatalogs[bcl]).hide();
                    }else{
                        $($bookCatalogs[bcl]).show();
                        $($bookCatalogs[bcl]).attr("selected",false);
                        showBookCatalog.push($($bookCatalogs[bcl]));
                    }

                }
                if(showBookCatalog.length == 0){
                    $("#bookCatalogDefault").attr("selected",true);
                }else{
                    $(showBookCatalog[0]).attr("selected",true);
                }
            }

        })
        //是否使用已录入下拉框事件
        $("#paperType").on("change",function(){
            var $val = $(this).val();
            if($val=="NEW"){
                $("#sourceExcelFile_div").show();
                $("#useOldTestPaper_div").hide();
            }else{
                $("#sourceExcelFile_div").hide();
                $("#useOldTestPaper_div").show();
            }
        });
        //学科选取事件
        $(document).on('change','.subject',function () {
            var _subject = $(this).val();
            $('#searchBookName').val('');
                getBooks(_subject);
            showTestType();
            $("input[name=grade]").each(function () {
                $(this).removeAttr("checked").parent().removeClass("checked");
            });
        });

        //区域选择事件
        $("input[name='regionLevel']").on("change",function(){
            codeList = [];
            nameList = [];
            codeObj = {};
            $('#codeInfo').html(template("choicedCode",{obj:codeObj,list:codeList}));

            $('#schoolinfo_div').hide();
            $("#schoolinfo_tbody").html('');
            $('#provinceCode option:first').prop("selected", 'selected');
            renderCity();

            var _regionLevel = $(this).val();
            if(_regionLevel == 'PROVINCE'){
                $("#city_div").hide();
                $("#county_div").hide();
                $("#unifiedExamSchool_div").hide();
            }else if(_regionLevel== 'CITY'){
                $("#city_div").show();
                $("#county_div").hide();
                $("#unifiedExamSchool_div").hide();
            }else if(_regionLevel== 'COUNTY'){
                $("#city_div").show();
                $("#county_div").show();
                $("#unifiedExamSchool_div").hide();
            }else if(_regionLevel== 'SCHOOL'){
                $("#city_div").show();
                $("#county_div").hide();
                $("#unifiedExamSchool_div").show();
            }
        });

        function changeTestType(){
            if($("input[name='type'][value='SPOKEN']").prop("checked")){
                $("#spokenAnswerTimes_div").show();
                $("#spokenScoreType").show();
            }else {
                $("#spokenAnswerTimes_div").hide();
                $("#spokenScoreType").hide();
            }
        }
        changeTestType();
        //考试类型 联动事件
        $("input[name='type']").on('click',function(){
            changeTestType();
        });
    });

    //日期控件绑定
    $("#startTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var registrationDeadline = $("#registrationDeadlineTime").val();
            if(registrationDeadline != "") {
                registrationDeadline = registrationDeadline + " " + $("#registrationDeadlineHour").val() + ":" + $("#registrationDeadlineMin").val() + ":00";
            }
            var _startTime = $("#startTime").val();
            if(!_startTime){
                alert("考试开始时间不能为空");
                return false;
            }
            _startTime =  _startTime + " " + $("#unifiedExamBeginTimeHour").val() + ":" + $("#unifiedExamBeginTimeMin").val() + ":00";
            var registrationDeadlineTime = new Date(_startTime.replace(/-/g,"/")).getTime() - 1000 * 60 * 60;
            var registrationDeadlineDay = new Date(registrationDeadlineTime).Format("yyyy-MM-dd");
            var registrationDeadlineHour = new Date(registrationDeadlineTime).Format("hh");
            var registrationDeadlineMin = new Date(registrationDeadlineTime).Format("mm");

            if(!registrationDeadline || new Date(registrationDeadline.replace(/-/g,"/")).getTime() >= registrationDeadlineTime){
                $("#registrationDeadlineTime").val(registrationDeadlineDay);
                $("#registrationDeadlineHour").val(registrationDeadlineHour);
                $("#registrationDeadlineMin").val(registrationDeadlineMin);
            }

            var _endTime =  $("#endTime").val();
            if(_endTime ){
                var endTime = new Date(_endTime.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate > endTime){
                    alert("考试开始时间必须晚于考试开始日期");
                    $("#endTime").val(null);//头疼的写法  return false; 竟然不管用~~~
                    // return false;
                }
            }
            return true;
        }
    });

    $('#registrationDeadlineHour').on("change",function () {

        if(!$("#registrationDeadlineTime").val()){
            alert("报名截止日期不能为空");
            return false;
        }

        var _startTime = $("#startTime").val();
        if(!_startTime){
            alert("考试开始时间不能为空");
            return false;
        }
        var startTime = new Date(_startTime.replace(/-/g,"/")).getTime() + $("#unifiedExamBeginTimeHour").val() * 60 * 60 * 1000;
        var $selectedDate = new Date( $("#registrationDeadlineTime").val().replace(/-/g,"/")).getTime() + $("#registrationDeadlineHour").val() * 60 * 60 * 1000;
        if($selectedDate > startTime - 1000 * 60 * 60) {
            alert("报名截止时间必须在考试开始一个小时之前");
            $("#registrationDeadlineHour").val(00);
            return false;
        }
    });

    $('#unifiedExamBeginTimeHour').on("change",function () {
        var registrationDeadline = $("#registrationDeadlineTime").val()+ " " + $("#registrationDeadlineHour").val() + ":" + $("#registrationDeadlineMin").val() + ":00";
        var _startTime = $("#startTime").val();
        if(!_startTime){
            alert("考试开始时间不能为空");
            return false;
        }
        _startTime =  _startTime + " " + $("#unifiedExamBeginTimeHour").val() + ":" + $("#unifiedExamBeginTimeMin").val() + ":00";
        var registrationDeadlineTime = new Date(_startTime.replace(/-/g,"/")).getTime() - 1000 * 60 * 60;
        var registrationDeadlineDay = new Date(registrationDeadlineTime).Format("yyyy-MM-dd");
        var registrationDeadlineHour = new Date(registrationDeadlineTime).Format("hh");
        var registrationDeadlineMin = new Date(registrationDeadlineTime).Format("mm");

        if(!registrationDeadline || new Date(registrationDeadline.replace(/-/g,"/")).getTime() >= registrationDeadlineTime){
            $("#registrationDeadlineTime").val(registrationDeadlineDay);
            $("#registrationDeadlineHour").val(registrationDeadlineHour);
            $("#registrationDeadlineMin").val(registrationDeadlineMin);
        }
    });

    $('#unifiedExamBeginTimeMin').on("change",function () {
        var registrationDeadline = $("#registrationDeadlineTime").val()+ " " + $("#registrationDeadlineHour").val() + ":" + $("#registrationDeadlineMin").val() + ":00";
        var _startTime = $("#startTime").val();
        if(!_startTime){
            alert("考试开始时间不能为空");
            return false;
        }
        _startTime =  _startTime + " " + $("#unifiedExamBeginTimeHour").val() + ":" + $("#unifiedExamBeginTimeMin").val() + ":00";
        var registrationDeadlineTime = new Date(_startTime.replace(/-/g,"/")).getTime() - 1000 * 60 * 60;
        var registrationDeadlineDay = new Date(registrationDeadlineTime).Format("yyyy-MM-dd");
        var registrationDeadlineHour = new Date(registrationDeadlineTime).Format("hh");
        var registrationDeadlineMin = new Date(registrationDeadlineTime).Format("mm");

        if(!registrationDeadline || new Date(registrationDeadline.replace(/-/g,"/")).getTime() > registrationDeadlineTime){
            $("#registrationDeadlineTime").val(registrationDeadlineDay);
            $("#registrationDeadlineHour").val(registrationDeadlineHour);
            $("#registrationDeadlineMin").val(registrationDeadlineMin);
        }
    });

    $("#teacherQueryTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var _endTime =  $("#endTime").val();
            if(_endTime ){
                var endTime = new Date(_endTime.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate > endTime){
                    alert("考试开始时间必须晚于考试开始日期");
                    $("#endTime").val(null);//头疼的写法  return false; 竟然不管用~~~
                    // return false;
                }
            }
            return true;
        }
    });

    $("#endTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var _startTime =  $("#startTime").val();
            if(_startTime ){
                var startTime = new Date(_startTime.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < startTime){
                    alert("考试截止时间必须晚于考试开始日期");
                    $("#endTime").val(null);//头疼的写法  return false; 竟然不管用~~~
                    // return false;
                }
            }
            return true;
        }
    });

    $("#registrationDeadlineTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var _startTime = $("#startTime").val();
            if(!_startTime){
                alert("考试开始时间不能为空");
                return false;
            }

            var startTime = new Date(_startTime.replace(/-/g,"/")).getTime();
            var registrationDeadline = selectedDate + " " + $("#registrationDeadlineHour").val() + ":" + $("#registrationDeadlineMin").val() + ":00";
            var $registrationDeadline = new Date(registrationDeadline.replace(/-/g,"/")).getTime();
            if($registrationDeadline > startTime - 1000 * 60 * 60) {
                alert("报名截止时间必须在考试开始一个小时之前");
                $("#registrationDeadlineTime").val(null);
                $("#registrationDeadlineHour").val(00);
                return false;
            }
            return true;
        }
    });

    $("#teacherMarkDeadline").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var _endTime =  $("#endTime").val();
            if(_endTime ){
                var _endTime = new Date(_endTime.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < _endTime){
                    alert("老师批改试卷时间不能早于考试结束日期");
                    $("#teacherMarkDeadline").val(null);
                    // return false;
                }
            }
            return true;
        }
    });
    $("input[name='allowTeacherQuery']").on("change",function () {
        if($(this).val() == 'Y' && $("input[name='subject']:checked").val() == 'ENGLISH'){
//            $('.allowTeacherModify').show();
        }else if($(this).val() == 'N'){
            $('.allowTeacherModify').hide();
        }
    });
    $("#scorePublishTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:new Date(new Date().getTime() + 24 * 60 * 60 * 1000),
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var _teacherMarkDeadline =  $("#teacherMarkDeadline").val();
            if(_teacherMarkDeadline ){
                var _teacherMarkDeadline = new Date(_teacherMarkDeadline.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < _teacherMarkDeadline){
                    alert("成绩发布日期不能早于老师批改试卷时间");
                    $("#scorePublishTime").val(null);
                    //return false;
                }
            }
            return true;
        }
    });



    // 等第设置， 不管是分数制，还是等第制，都需要填写等第信息，（分数制的情况下，等第信息会在生成成绩报告时使用）
    function checkDengdi(){
        var achievement = $('.achievementGrading');
        if(achievement.length == 0){
            alert("请填写等第信息");
            return false;
        }
        var labelList = [];
        var scoreList = [];
        for(var i = 0; i < achievement.length; i++){
            if(!achievement.eq(i).hasClass('show')){
                var itemName = achievement.eq(i).find(".achievement_name").val();
                if(itemName == ""){
                    alert("等第名称不能为空");
                    return false;
                }else {
                    if(labelList.indexOf(itemName) == -1){
                        labelList.push(itemName)
                    }else {
                        alert("等第名称不能重复");
                        return false;
                    }
                }

                var itemStartValue = achievement.eq(i).find(".initialScore").val();
                var itemEndValue = achievement.eq(i).find(".endResults").val();
                if(itemStartValue == "" || itemEndValue == "" || isNaN(parseFloat(itemStartValue)) || isNaN(parseFloat(itemEndValue))){
                    alert("分数区间必须为有效数字");
                    return false;
                }
                if(parseFloat(itemStartValue) >=  parseFloat(itemEndValue)){
                    alert("等第设置同一分数区间中，“上限值”必须大于“下限值”");
                    return false;
                }
                scoreList.push(parseFloat(itemStartValue));
                scoreList.push(parseFloat(itemEndValue));
            }
        }

        scoreList.sort(function(a, b){
            return a - b;
        });

        if(scoreList[scoreList.length - 1] - scoreList[0] != 100){
            alert("等第设置中成绩区间必须涵盖0-100%");
            return false;
        }

        for(var i = 0; i < (scoreList.length - 1);){
            if(scoreList[i] >=  scoreList[i + 1]){
                alert("等第设置中分数下限不能大于上限");
                return false;
            }
            if(i > 0 && scoreList[i] !=  scoreList[i - 1]){
                alert("等第设置中分数区间不能有间隔/重叠");
                return false;
            }
            i = i + 2;
        }
        return true;
    }

    //提交数据 数据校验和数据整合
    function data_info(url){

        // 等第设置
        if(!checkDengdi()){
            return false;
        }

        var name = $.trim($("#name").val());
        if(!name){
            alert("考试名称不能为空");
            return false;
        }
        //试卷校验
        var _paperType = $("#paperType").val();
        var _paperDocUrls = "";
        var paperDocNames = "";
        var arr = [];
        var arr1 = [];
        if($(".paperDocUrls").length > 1 && $("input[name='subject']:checked").val() == 'MATH' && $(".paperDocUrls").eq(0).val() != ''){
            alert('数学科目只允许上传1个文档');
            return false;
        }
        for(var i = 0; i< $(".paperDocUrls").length;i++){
            arr.push($(".paperDocUrls").eq(i).val());
            arr1.push($(".paperDocNames").eq(i).val())
        }
        _paperDocUrls = arr.toString();
        paperDocNames = arr1.toString();
        var $testPaperId_val = $.trim($("#testPaperId").val());
        if(_paperType == "OLD"){
            if(!$testPaperId_val){
                alert("请选取已存在试卷的所在学校和试卷名称");
                return false;
            }
            if($testPaperId_val.indexOf("P") == -1){
                alert("试卷ID以'P'开头");
                return false;
            }
            _paperDocUrls = "";
            paperDocNames = "";
        }else if(_paperType == "NEW"){
            if(!_paperDocUrls){
                alert("请至少上传一个文件；只支持上传doc，docx格式的文件；单个文件大小不能超过10M");
                return false;
            }
        }else{
            alert("请选择是否重复使用已录入");
            return false;
        }

        //创建对象
        var $id = $.trim($("#id").val());
        if(!$id || id==""){
            $id = null
        }
        //学科
        var _subject = $("input[name='subject']:checked").val();
        var _distributeType = $("input[name='distributeType']:checked").val();
        var _allowTeacherQuery = $("input[name='allowTeacherQuery']:checked").val();
        if(!_allowTeacherQuery){
            alert('请选择是否允许教师查看试卷');
            return false;
        }
        if(_subject == 'ENGLISH'){
            var _spokenScoreType = $("input[name='spokenScoreType']:checked").val();
            if(!_spokenScoreType){
                alert('请选择选择口语算分类型');
                return false;
            }
            var _allowTeacherModify = $("input[name='allowTeacherModify']:checked").val();
            if(_allowTeacherQuery == 'Y' &&!_allowTeacherModify){
                alert('请选择是否允许教师修改成绩');
                return false;
            }
        }

        var _allowStudentQuery = $("input[name='allowStudentQuery']:checked").val();
        if(!_allowStudentQuery){
            alert('是否允许学生查看成绩');
            return false;
        }
        var _form = $("input[name='form']:checked").val();
        if(!_form){
            alert('请选择测评形式');
            return false;
        }
        var _type = "";
        var type = [];
        if($("input[name='type']:checked").length>0){
            for(var i=0;i<$("input[name='type']:checked").length;i++){
                type.push($("input[name='type']:checked").eq(i).val())
            }
            _type = type.toString();
        }
        var _grade = [];
        if($("input[name='grade']:checked").length>0){
            for(var i=0;i<$("input[name='grade']:checked").length;i++){
                _grade.push($("input[name='grade']:checked").eq(i).val())
            }
        }
        if (_grade.length == 0) {
            alert("请选择考试年级");
            return false;
        }
        if (!_distributeType) {
            alert("请选择学生获取试卷方式");
            return false;
        }

        var schoolinfo = $("#schoolinfo_div").find(".schoolinfo");
        var _regionLevel = $("input[name='regionLevel']:checked").val();
        var _scoreType = $("input[name='scoreRuleType']:checked").val();

        if(_scoreType != 'SCORE' && _scoreType != 'GRADE'){
            alert("请选择成绩分制");
            return false;
        }

        //城市信息
        if(codeList.length < 1){
            alert("请选择有效的测评区域");
            return false;
        }

        if(typeof(_regionLevel)=='undefined'){
            alert("请选择测评级别");
            return false;
        }

        if(_regionLevel == "SCHOOL"){
            if(_schoolIds.length == 0){
                alert("请选择有效的统考学校");
                return false;
            }
            if(_schoolIds.length >20){
                alert("校级统考不能超过20所学校");
                return false;
            }
        }

        var _startTime = $("#startTime").val();
        if(!_startTime){
            alert("考试开始时间不能为空");
            return false;
        }
        _startTime =  _startTime + " " + $("#unifiedExamBeginTimeHour").val() + ":" + $("#unifiedExamBeginTimeMin").val() + ":00";
        _startTime = new Date(_startTime.replace(/-/g,"/"));
//        var _teacherQueryTime = $("#teacherQueryTime").val();
//        if(!_teacherQueryTime){
//            alert("考试开始时间不能为空");
//            return false;
//        }
//        _teacherQueryTime =  _teacherQueryTime + " " + $("#teacherQueryTimeHour").val() + ":" + $("#teacherQueryTimeMin").val() + ":00";
//        _teacherQueryTime = new Date(_teacherQueryTime.replace(/-/g,"/"));
        var limitDate = new Date();
        if(_paperType == "OLD"){
            limitDate.setDate(limitDate.getDate() + 1);
        }else{
            limitDate.setDate(limitDate.getDate() + 6);
        }
        <#if (isAdmin?? && !isAdmin) && (plan.status?? && !( plan.status == 'PAPER_READY' || plan.status == 'EXAM_PUBLISHED'))>
            if(  _startTime <= limitDate){
                alert("使用新试卷，提前9天申请考试（不算申请当天和考试当天）；使用已有试卷，提前1天申请考试(不算申请当天和考试当天)");
                return false;
            }
        </#if>
        var _endTime =$("#endTime").val();
        if(!_endTime){
            alert("考试结束时间不能为空且不能早于考试开始时间");
            return false;
        }
        _endTime =  _endTime + " " + $("#unifiedExamEndTimeHour").val() + ":" + $("#unifiedExamEndTimeMin").val() + ":00";
        _endTime = new Date( _endTime.replace(/-/g,"/"));
        if(_endTime < _startTime){
            alert("考试结束时间不能为空且不能早于考试开始时间");
            return false;
        }

        var _teacherMarkDeadline = $("#teacherMarkDeadline").val();
        if(!_teacherMarkDeadline){
            alert("老师判分时间不能为空");
            return false;
        }
        _teacherMarkDeadline = _teacherMarkDeadline + " " + $("#correctingTestPaperHour").val() + ":" + $("#correctingTestPaperMin").val() + ":00";
        _teacherMarkDeadline = new Date( _teacherMarkDeadline.replace(/-/g,"/"));
        if(_teacherMarkDeadline.getTime() <  _endTime.getTime()){
            alert("老师判分时间不能早于考试结束时间");
            return false;
        }
            var _scorePublishTime = $("#scorePublishTime").val();
            if(!_scorePublishTime){
                alert("学生端成绩发布时间不能为空");
                return false;
            }
            _scorePublishTime = _scorePublishTime + " " + $("#achievementReleaseTimeHour").val() + ":" + $("#achievementReleaseTimeMin").val() + ":00";
            _scorePublishTime = new Date(_scorePublishTime.replace(/-/g,"/"));
            if(_scorePublishTime.getTime() <  _teacherMarkDeadline.getTime()){
                alert("成绩发布时间不能早于老师批改试卷时间");
                return false;
            }
        var twoMonthLater = new Date(createDatetime);
        twoMonthLater.setMonth(twoMonthLater.getMonth() + 2);
        if(_scorePublishTime.getTime() > twoMonthLater.getTime()){
            alert("成绩发布时间与创建考试时间的间隔不能超过2个月");
            return false;
        }
        var  _finishExamTime =$.trim( $("#finishExamTime").val());
        var  _examTotalTime =$.trim( $("#examTotalTime").val());

        if(!_finishExamTime){
            alert("允许交卷时间不能为空");
            return false;
        }
        if(!_examTotalTime || _examTotalTime <1){
            alert("试卷总答题时长不能为空且至少为1分钟");
            return false;
        }
        if(+_finishExamTime >=  +_examTotalTime){
            alert("允许交卷时间必须小于试卷总答题时长");
            return false;
        }
        if(_examTotalTime < 10){
            if(!confirm('【试卷总答题时长】小于10分钟，有可能导致部分学生答题时间不够，是否确认提交？')) {
                return false;
            }
        }

        var _bookId = $("#bookCatalogId option:selected").val();
        if (!_bookId) {
            alert("请选择使用教材");
            return false;
        }

        var _spokenAnswerTimes = $("input[name='spokenAnswerTimes']:checked").val();
        var $entryStatus_val = $("#entryStatus").val();

        var $workflowId = $.trim($("#workflowId").val());
        if(!$workflowId || $workflowId == ""){
            $workflowId = null;
        }
        var regular_num = /^[0-9]*[1-9][0-9]*$/g;//正整数
        var _scene = $("input[name='scene']:checked").val();
        if(_scene != 'ONLINE' && _scene != 'FOCUS'){
            alert("请选择考试场景");
            return false;
        }

        var _pattern = $("input[name='pattern']:checked").val();
        var isAdmin = $("#isAdmin").val();
        if(isAdmin == 'true') {
            if(_pattern != 'GENERAL' && _pattern != 'REGISTER'){
                alert("请选择考试模式");
                return false;
            }
        } else {
            _pattern = 'GENERAL';
        }

        var _registrationDeadlineTime = '';
        if(_pattern != 'GENERAL') {
            _registrationDeadlineTime = $("#registrationDeadlineTime").val()+ " " + $("#registrationDeadlineHour").val() + ":" + $("#registrationDeadlineMin").val() + ":00";
            _registrationDeadlineTime = new Date( _registrationDeadlineTime.replace(/-/g,"/"));
        }

        var tdArr = [];
        for(var i =0;i< $('.achievementGrading').length;i++){
            if($('.achievementGrading .achievement_name').eq(i).val() != '' && $('.achievementGrading .initialScore').eq(i).val() != '' && $('.achievementGrading .endResults').eq(i).val() != ''){
                tdArr.push({"rankName":$('.achievementGrading .achievement_name').eq(i).val(),"bottom":$('.achievementGrading .initialScore').eq(i).val(),"top":$('.achievementGrading .endResults').eq(i).val()})
            }
        }
        var unifiedExamApply = new Object();
        unifiedExamApply.scoreRule = tdArr;
        unifiedExamApply.id = $id;
        unifiedExamApply.workflowId = $workflowId;
        unifiedExamApply.name = name;
        unifiedExamApply.paperType = _paperType;
        if(_paperType == 'OLD'){
            unifiedExamApply.bookId = $("#bookId").val();
            unifiedExamApply.bookName = $("#bookId option:selected").text();
        }
        unifiedExamApply.paperId = $testPaperId_val;
        unifiedExamApply.paperDocUrls = _paperDocUrls;
        unifiedExamApply.paperDocNames = paperDocNames;
        unifiedExamApply.subject = _subject;
        unifiedExamApply.distributeType = _distributeType;
        unifiedExamApply.type =_type;
        unifiedExamApply.grade = _grade.toString();
        unifiedExamApply.form = _form;
        //地区
        unifiedExamApply.regionCodes = codeList.toString();
        unifiedExamApply.regionNames = nameList.toString();
        //考试时间地点
        unifiedExamApply.startTime = _startTime;
        if(_regionLevel == 'SCHOOL'){
            unifiedExamApply.schoolIds = _schoolIds.toString();
            unifiedExamApply.schoolNames = _schoolNames.toString();
        }
//        unifiedExamApply.teacherQueryTime = _teacherQueryTime;
        unifiedExamApply.allowTeacherQuery = _allowTeacherQuery;
        if(_allowTeacherQuery == 'Y'){
            unifiedExamApply.allowTeacherModify = _allowTeacherModify;
        }
        unifiedExamApply.allowStudentQuery = _allowStudentQuery;
        unifiedExamApply.endTime = _endTime;
        unifiedExamApply.spokenScoreType = _spokenScoreType;
        unifiedExamApply.teacherMarkDeadline = _teacherMarkDeadline;
        unifiedExamApply.scorePublishTime = _scorePublishTime;
        unifiedExamApply.finishExamTime = _finishExamTime;
        unifiedExamApply.examTotalTime = _examTotalTime;
        unifiedExamApply.spokenAnswerTimes = _spokenAnswerTimes;
        unifiedExamApply.regionLevel = _regionLevel;
//        unifiedExamApply.status = $entryStatus_val;
        unifiedExamApply.scoreRuleType = _scoreType;
        unifiedExamApply.scene = _scene;
        unifiedExamApply.pattern = _pattern;
        unifiedExamApply.bookId = $("#bookCatalogId option:selected").val();
        unifiedExamApply.bookName = $("#bookCatalogId option:selected").text().trim();
        unifiedExamApply.registrationDeadlineTime = _registrationDeadlineTime;
        var attachmentFiles = [];
        var fileInfos = $(".file_info");
        var fileCount = 0;
        for(var i = 0; i<fileInfos.length; i++){
            console.log(fileInfos[i])
            var fileName = $(fileInfos[i]).attr("fileName");
            var fileUrl = $(fileInfos[i]).attr("fileUrl");
            if(fileName && fileUrl){
                var fileObj = {};
                fileObj.fileName = fileName;
                fileObj.fileUrl = fileUrl;
                attachmentFiles.push(fileObj);
                fileCount++;
            }
        }
        if(fileCount == 0 && (_regionLevel == 'CITY' || _regionLevel == 'COUNTY')){
            alert("备注项中请至少上传一个附件");
            return false;
        }
        unifiedExamApply.attachmentFiles = attachmentFiles;
        var comment = $("#comment").val();
        if(comment.length > 100){
            comment = comment.substr(0, 100);
        }
        unifiedExamApply.comment = comment;
        //unifiedExamApply.ranks  = tdArr;
        var unifiedExamApplyJson = JSON.stringify(unifiedExamApply);
//        var secondJson = JSON.stringify(tdArr);
        if (unlock) {
            unlock = false;
            $.ajax({
                url: url,
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: unifiedExamApplyJson,
                async: false,
                success: function (data) {
                    if (!data.success) {
                        if(data.errorCode==672) {
                            unlock = true;
                            if (unlock) {
                                layer.confirm(data.info, {
                                    btn: ['继续提交','去修改'] //按钮
                                }, function(){
                                    if (unlock) {
                                        unlock = false;
                                        $.ajax({
                                            url: "/mockexam/plan/submit.vpage",
                                            type: "POST",
                                            datType: "JSON",
                                            contentType: "application/json",
                                            data: unifiedExamApplyJson,
                                            async: false,
                                            success: function (data) {
                                                $('#realSubmit').modal('hide');
                                                if (!data.success) {
                                                    unlock = true;
                                                    alert(data.info);
                                                } else {
                                                    window.location.href = "/mockexam/plan/forquery.vpage";
                                                }
                                            },
                                            error: function () {
                                                unlock = true;
                                            }
                                        });
                                    }
                                }, function(){
                                    unlock = true;
                                });
                            }
                        } else {
                            layer.alert(data.info);
                        }
                    } else {
                        $.ajax({
                            url: "/mockexam/plan/submit.vpage",
                            type: "POST",
                            datType: "JSON",
                            contentType: "application/json",
                            data: unifiedExamApplyJson,
                            async: false,
                            success: function (data) {
                                if (!data.success) {
                                    unlock = true;
                                    alert(data.info);
                                } else {
                                    if (confirm("提交成功") == true) {
                                        window.location.href = "/mockexam/plan/forquery.vpage";
                                    }
                                }
                            },
                            error: function () {
                                unlock = true;
                            }
                        });
                    }
                }
            });
        }
    }
    var unlock = true;
    $('#submitBtn').live('click',function(){
        data_info('/mockexam/plan/book_check.vpage');
    });
    //添加学校dialog
    $(document).on("click",".addSchoolBtn",function(){
        var uid = $(this).data("uid");
        var gid = $(this).data("gpid");
        $("#selectSids").val("");
        $("#schoolTable").html("");
        $("#selectSchoolType").val($(this).attr("data-type"));
        $("#region_select_dialog").modal('show');
        $("#alertInfoInDialog").hide();
    });
    //获取焦点事件
    $("#selectSids").on("click",function (){
        $(this).focus();
    });
    //添加学校Btn
    $(document).on("click","#addSchooleBtn",function(){
        var schoolIds = $.trim($("#selectSids").val());
        if(!schoolIds){
            alert("请输入要查询学校的id");
            return false;
        }
        var agentUserId = $("#agentUserId").val();
        if(schoolIds.length != 0 && valiteSplitByicon(schoolIds)){
            var _codes = codeList.toString();
            $.get("/mockexam/refer/schools.vpage?schoolIds="+schoolIds + "&cityCodes="+ codeList.toString(),function(res){
                if(res.success){
                    var dataTemp = {},errorList=[];
                    var data = [];
                    var schoolLevelError = "";
                    dataTemp.data = res.data;
                    dataTemp.totalNo = res.data.length || 0;
//                        errorList = res.info || [];
                    var $subject = $("#subject").val();
//                    for(var ff = 0; ff < dataTemp.schoolData.length ; ff++){
//                        var datad = new Object();
//                        datad =$( dataTemp.schoolData)[ff];
//                        if(datad.schoolLevel == "JUNIOR"){
//                            datad.schoolLevelName = "小学";
//                        }else if(datad.schoolLevel == "MIDDLE"){
//                            datad.schoolLevelName = "中学";
//                        }else{
//                            datad.schoolLevelName = "高中";
//                        }
//                        if((datad.schoolLevel == "JUNIOR" && $subject < 200) || (datad.schoolLevel == "MIDDLE" && $subject > 200)){
//                            dataTempt.push(datad);
//                        }else{
//                            schoolLevelError.push(datad.schoolId);
//                        }
//                    }
//                    dataTemp.schoolData = dataTempt;
                    renderDepartment('#chooseSchoolTableTemp',dataTemp,"#schoolTable");
//                    var tempHtml = '';
//                    if(errorList.length != 0){
//                        tempHtml = '<p>有'+errorList.length+'所学校为假学校、不属于此部门或者与所选城市不符，无法添加！</p>'+
//                                '<p>'+errorList.join(",")+'</p>';
//                    }
//                    if(schoolLevelError.length != 0){
//                        tempHtml +=  '<p>有'+schoolLevelError.length+'所学校级别与考试科目不对应，无法添加！</p>'+
//                                '<p>'+schoolLevelError.join(",")+'</p>';
//                    }
//                    if(tempHtml != ''){
//                        $("#alertInfoInDialog").show();
//                        $("#alertInfoInDialog").html(tempHtml);
//                    }else{
//                        $("#alertInfoInDialog").hide();
//                    }
                }else{
                    alert(res.info);
                }
            })
        }else{
            alert("请输入学校ID,并以英文格式逗号分隔");
        }
    });
    //逗号分隔验证
    var valiteSplitByicon = function(str){
        var flag = false;
        var ex = /[0-9]+(,[0-9]+)*/g;
        var strWords = str.replace(/\s/g,"");
        if(strWords == strWords.match(ex)[0]){
            flag = true
        }
        return flag;
    };
    //渲染模板
    var renderDepartment = function(tempSelector,data,container){
        var source   = $(tempSelector).html();
        var template = Handlebars.compile(source);

        $(container).html(template(data));
    };

    var _schoolIds = [];
    var _schoolNames = [];

    for(var k = 0;k < $('.schoolinfo').length;k++){
        if($('.schoolinfo').eq(k).data('id') !=''){
            _schoolIds.push($('.schoolinfo').eq(k).data('id'));
            _schoolNames.push($('.oldschoolNames').eq(k).html().trim());
        }
    }
    $("#add_school_submit_btn").on("click",function(){
        var $schoolIds =  $("#schoolTable").find(".js-schoolIds");
        var schoolId = '';
        var schoolView = '';
        if($schoolIds){
            for(var ii = 0 ; ii < $schoolIds.length ; ii++ ){
                var id = $($schoolIds[ii]).data("id");
                var name = $($schoolIds[ii]).data("name");
                if(_schoolIds.indexOf(id) == -1){
                    _schoolIds.push(id);
                    _schoolNames.push(name);
                    schoolView += "<tr class='schoolinfo' id='schoolInfo_"+ id +"' data-id='"+id+"'><td>"+ id+" </td><td >"+name+"</td><td class='delete-schoolinfo' data-id='"+id+"'><a class ='delete_data' data-name='"+name+"' data-id='"+id+"' href=' javascript:void(0)'>删除</a></td></tr>"
                    schoolId += "," +$($schoolIds[ii]).attr("data-id");
                }
            }
            if(schoolId){
                schoolId = schoolId.substring(1);
            }
            $("#closed_div").hide();
            $("#unifiedExamSchool").val(schoolId);
//            $("#schoolinfo_tbody").empty();
            $("#schoolinfo_tbody").append(schoolView);
            $("#schoolinfo_div").show();
            $("#region_select_dialog").modal('hide');
        }else{
            alert("请输入要添加的学校");
            return false;
        }
    });
    //删除学校列表联动
    $(document).on("click",".delete_data",function () {
        var id = $(this).data('id');
        var name = $(this).data('name');
        _schoolIds.splice(_schoolIds.indexOf(id),1)
        _schoolNames.splice(_schoolNames.indexOf(name + ''),1)
        $("#schoolInfo_"+id).remove();

        var sl = $(".schoolinfo").length;
        if(!sl || sl == 0){
            $("#schoolinfo_div").hide();
        }
    })

</script>
<script id="chooseSchoolTableTemp" type="text/x-handlebars-template">
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>学校id</th>
            <th>学校名称</th>
        </tr>
        </thead>
        <tbody>
        {{#each data}}
        <tr>
            <td class="js-schoolIds" data-id="{{id}}" data-name ="{{name}}">{{id}}</td>
            <td >{{name}}</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
    <div class="pull-left" style="padding-top: 10px;">共计 <span style="color: red;">{{#if totalNo}}{{totalNo}}{{else}}0{{/if}}</span> 所学校</div>
</script>

<!--上传文件JS-->
<script>

    $(function() {
        var init_upload = function($dom){
            new ss.SimpleUpload({
                button: $dom, // file upload button
                url: '/apply/create/upload_testpaper.vpage', // server side handler
                name: 'testPaper', // upload parameter name
                responseType: 'json',
                multipart: true,
                allowedExtensions: ['doc', 'docx'],
                maxSize: 10 * 1024, // kilobytes
                hoverClass: 'ui-state-hover',
                focusClass: 'ui-state-focus',
                disabledClass: 'ui-state-disabled',
                debug: false,
                onExtError: function (filename, extension) {
                    alert("仅支持doc|docx文件格式");

                    return false;
                },
                onSizeError:function (filename, extension) {
                    alert("单个文件大小不能超过10M");
                    return false;
                },
                onSubmit: function (filename, extension) {
                },
                onComplete: function (filename, response) {
                    var $fileresult = $("span[id='fileresult']");

                    if (!response.success) {
                        $fileresult.text(response.info).show();
                        return false;
                    }
                    var html = filename + "上传成功";
                    $fileresult.text(html).show();
                    $dom
                            .next().val(response.fileUrl)   // input hidden
                            .next().val(filename)   // input hidden
                            .next().html(filename.split('.')[0]).attr("href", response.fileUrl).show();   // HTML A
                }
            });
        };

        var init_upload_btn = $('.upload_file_but'),
                upload_block = init_upload_btn.parent(),
                upload_block_p = upload_block.parent(),
                upload_block_html = upload_block[0].outerHTML;
        $(".do_remove_upload").eq(0).hide();
        $(document).on('click', '.do_add_upload', function () {
            init_upload($(upload_block_html).appendTo(upload_block_p).find('.upload_file_but'));
            if($('.do_add_upload').length > 9){
                $('.do_add_upload').hide();
            }
        });
        $(document).on('click', '.do_remove_upload', function () {
            $(this).parent().remove();
            if($('.do_add_upload').length < 10){
                $('.do_add_upload').show();
            }
        });

        init_upload(init_upload_btn);
    });

</script>
</@layout_default.page>
