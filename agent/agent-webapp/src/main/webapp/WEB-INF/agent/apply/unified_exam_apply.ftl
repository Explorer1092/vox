<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='统考申请' page_num=3>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
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
            <h2><i class="icon-th"></i>统考申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="download_example.vpage?type=2" class="btn btn-success">申请须知</a>
                <a href="download_example.vpage?type=1" class="btn btn-primary">下载统考样卷</a>
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
        <#if applyDetail?? >
            <#assign apply = applyDetail.apply>
            <#assign processResultList = applyDetail.processResultList>
            <div class="box-content">
                <div class="form-horizontal">
                    <input type="hidden" id="targetSchoolId" name="targetSchoolId" />
                    <fieldset>
                        <input id="id" value="${apply.id}" type ="hidden">
                        <input id="workflowId" value="${apply.workflowId}" type ="hidden">
                        <div class="control-group">
                            <label class="control-label">考试名称</label>
                            <div class="controls">
                                <input type="text" id="unifiedExamName" name ="unifiedExamName" placeholder="考试名称" value="${apply.unifiedExamName}">（需包含学校、城市、区域、年级等信息）
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">是否重复使用已录入</label>
                            <div class="controls">
                                <select id = "testPaperSourceType" >
                                    <option name="testPaperSourceType" value = "ANCIENT"    <#if apply.testPaperSourceType == "ANCIENT">selected</#if>>是</option>
                                    <option name="testPaperSourceType" value = "NEWLYADDED" <#if apply.testPaperSourceType == "NEWLYADDED">selected</#if>>否</option>
                                </select>
                            </div>
                        </div>
                        <div class="flieContainer">
                            <div class="control-group" id="sourceExcelFile_div">
                                <label class="control-label">上传试卷</label>
                                <#if apply.testPaperAddress?has_content>
                                    <#list apply.testPaperAddress?split(",") as address>
                                        <div class="controls">
                                            <button id="upload_file_but" type="button" class="btn btn-primary upload_file_but">上传文件</button>
                                            <input id="testPaperAddress" class="testPaperAddress" type="hidden" value="${address!}">
                                            <a  id = "testPaperAddress_a" class="testPaperAddress_a" href="${address!}">点击下载查看详情</a>
                                            <button type="button" class="btn btn-primary btnFile do_add_upload">添加</button>
                                            <#if address_index = 0>
                                                <button type="button" class="btn btn-primary btnFile do_remove_upload">删除</button>
                                            </#if>
                                        </div>
                                    </#list>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group" id= "useOldTestPaper_div" <#if apply.testPaperSourceType == "NEWLYADDED"> style="display: none;" </#if> >
                            <div style="display: inline-block;">
                                <label class="control-label">以往试卷ID</label>
                                <div class="controls">
                                    <input type="text" id="testPaperId" name ="testPaperId" value="${apply.testPaperId!}" placeholder="多个试卷Id按照逗号分隔">
                                </div>
                            </div>
                        </div>
                        <div class="control-group" id= "useOldTestPaper_div" style="display: none;">
                            <div style="display: inline-block;">
                                <label class="control-label">使用试卷学校ID</label>
                                <div class="controls">
                                    <input type="text" id="testPaperId" name ="testPaperId"  value = "${apply.testPaperId!}">
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">学生获取试卷方式</label>
                            <div class="controls">
                                <input type="radio"  name ="distribution" value="1" <#if apply.distribution?? && apply.distribution == 1>checked</#if>> 完全随机获取
                                <input type="radio"  name ="distribution" value="0" <#if apply.distribution?? && apply.distribution == 0>checked</#if> >轮流获取
                            </div>
                        </div>
                        <div class="control-group">
                            <div  style="display: inline-block;">
                                <label class="control-label">学科</label>
                                <div class="controls">
                                    <select id = "subject" >
                                        <#--<option name="subject" value = "101"  <#if apply.subject == "101">selected</#if> >小学语文</option>-->
                                        <#--<option name="subject" value = "102"  <#if apply.subject == "102">selected</#if> >小学数学</option>-->
                                        <#--<option name="subject" value = "103"  <#if apply.subject == "103">selected</#if> >小学英语</option>-->
                                    <#--<option name="subject" value = "201"  <#if apply.subject == "201">selected</#if> >初中语文</option>-->
                                    <#--<option name="subject" value = "202"  <#if apply.subject == "202">selected</#if> >初中数学</option>-->
                                        <option name="subject" value = "203"  <#if apply.subject == "203">selected</#if> >初中英语</option>
                                    </select>
                                </div>
                            </div>
                            <div id="teaching_material_div" <#if apply.testPaperSourceType == "NEWLYADDED">style="display: inline-block;" <#else > style="display: none;" </#if> >
                                <div style="display: inline-block;">
                                    <label class="control-label">使用教材</label>
                                    <div class="controls">
                                        <select id = "bookCatalogId" >
                                            <#list bookProfiles as bp>
                                                <option name="bookCatalogId" value = "${bp.id}" <#if apply.bookCatalogId?? && apply.bookCatalogId == bp.id> selected </#if>  >${bp.name}</option>
                                            </#list>
                                            <option name="bookCatalogId" id="bookCatalogDefault"></option>
                                        </select>
                                    </div>
                                </div>
                                <div style="display: inline-block;">
                                    <input id="bookCatalogId-select" placeholder="输入教材名称快速定位">
                                </div>
                            </div>
                        </div>
                        <div class="control-group" style="display: none;">
                            <label class="control-label">考试场景</label>
                            <div class="controls">
                                <input type="radio"  name ="testScene" value="0" <#if apply.testScene?? && apply.testScene == 0>checked</#if>> 在线考试
                                <input type="radio"  name ="testScene" value="1" <#if apply.testScene?? && apply.testScene == 1>checked</#if>>集中考试 <span style="color:red">（在机房集中考试，需要现场部署考试机，官网上没有考试卡片，需要在机房里集中考试）</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">考试类型</label>
                            <div class="controls">
                                <input type="checkbox"  name ="testPaperType" value="ORAL" <#if apply.testPaperType??><#list apply.testPaperType?split(",") as type><#if type == "ORAL">checked</#if></#list></#if> > 口语考试
                                <input type="checkbox"  name ="testPaperType" value="LISTENING" <#if apply.testPaperType??><#list apply.testPaperType?split(",") as type><#if type == "LISTENING">checked</#if></#list></#if>> 听力考试
                                <input type="checkbox"  name ="testPaperType" value="NORMAL" <#if apply.testPaperType??><#list apply.testPaperType?split(",") as type><#if type == "NORMAL">checked</#if></#list></#if>> 普通题型考试
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">年级</label>
                            <div class="controls">
                                <div id="middle_div">
                                    <input type="radio"  name ="gradeLevel" value="6"  <#if apply.fetchGradeLevel()??> <#list apply.fetchGradeLevel() as level><#if level == 6><#if apply?? && apply.subject?? && apply.subject == "203">checked</#if></#if></#list></#if>> 六年级
                                    <input type="radio"  name ="gradeLevel" value="7"  <#if apply.fetchGradeLevel()??> <#list apply.fetchGradeLevel() as level><#if level == 7>checked</#if></#list></#if>> 七年级
                                    <input type="radio"  name ="gradeLevel" value="8"  <#if apply.fetchGradeLevel()??> <#list apply.fetchGradeLevel() as level><#if level == 8>checked</#if></#list></#if>> 八年级
                                    <input type="radio"  name ="gradeLevel" value="9"  <#if apply.fetchGradeLevel()??> <#list apply.fetchGradeLevel() as level><#if level == 9>checked</#if></#list></#if>> 九年级
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">级别</label>
                            <div class="controls">
                                <input type="radio"  name ="regionLeve" value="city" <#if apply.regionLeve == "city">checked</#if>> 市级
                                <input type="radio"  name ="regionLeve" value="country" <#if apply.regionLeve == "country">checked</#if>> 区级
                                <input type="radio"  name ="regionLeve" value="school" <#if apply.regionLeve == "school">checked</#if>> 校级
                            </div>
                        </div>
                        <div id="city_div" class="control-group" >
                            <label class="control-label">市</label>
                            <div class="controls">
                                <select id ="cityCode">
                                    <#if cityInfoList??>
                                        <#list cityInfoList as cityInfo>
                                            <option name="cityName" value = "${cityInfo.cityCode}" data-groupId="${cityInfo.groupId}"  data-provinceCode="${cityInfo.provinceCode}" data-provinceName="${cityInfo.provinceName}" <#if cityCodeDefault?? && cityCodeDefault == cityInfo.cityCode>selected</#if> >${cityInfo.cityName}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div id="regionName_div" class="control-group"  <#if  apply.regionLeve != "country"> style="display: none;" </#if>>
                            <label class="control-label">区域</label>
                            <div class="controls">
                                <select id ="regionName">
                                    <#assign regionCode = apply.regionCode>
                                    <#if regionList??>
                                        <#list regionList as region>
                                            <option name="regionName" value = "${region.countyCode}" data-name="${region.countyName}" >${region.countyName}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div id="unifiedExamSchool_div" class="control-group" <#if apply.regionLeve != "school"> style="display: none;" </#if> >
                            <label class="control-label">学校</label>
                            <div class="controls">
                                <input id="addSchoolBtn" class ="addSchoolBtn" type="button" value="添加" data-type ="batch">
                                <input id ="agentUserId" value="${agentUserId}" type="hidden" >
                            </div>
                        </div>
                        <#if apply.regionLeve == "school">
                            <div id="schoolinfo_div" >
                                <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                                    <tbody id="schoolinfo_tbody">
                                        <#list schoolList as school>
                                        <tr class='schoolinfo' data-id="${school.schoolId}">
                                            <td class="js-schoolIds">${school.schoolId}</td>
                                            <td >${school.schoolName}</td>
                                            <td ><a onclick='deleteScooolInfo("+id+")' data-id="${school.schoolId}" href="javascript:void(0)">删除</a></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </#if>
                        <div id="schoolinfo_div" style="display: none;">
                            <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                                <tbody id="schoolinfo_tbody">
                                </tbody>
                            </table>
                        </div>
                        <div class="control-group" disabled="true">
                            <div style="display: inline-block">
                                <label class="control-label">考试开始时间:</label>
                                <div class="controls">
                                    <input type="text" id="unifiedExamBeginTime" name ="unifiedExamBeginTime" value = "${apply.unifiedExamBeginTime?string('yyyy-MM-dd')}"   class="apply_input_time">
                                    <select class="s_time" name="unifiedExamBeginTimeHour" id="unifiedExamBeginTimeHour">
                                        <#assign beginTime_HH = apply.unifiedExamBeginTime?string("HH")>
                                            <#assign beginTime_mm = apply.unifiedExamBeginTime?string('mm')>
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
                                    <input type="text" id="unifiedExamEndTime" name ="unifiedExamEndTime" value = "${apply.unifiedExamEndTime?string('yyyy-MM-dd')}" class="apply_input_time">
                                    <select class="s_time" name="unifiedExamEndTimeHour" id="unifiedExamEndTimeHour">
                                        <#assign endTime_HH = apply.unifiedExamEndTime?string('HH')>
                                            <#assign beginTime_mm = apply.unifiedExamEndTime?string('mm')>
                                            <@forOption start=0 end=23 defaultVal= endTime_HH?number />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamEndTimeMin" id="unifiedExamEndTimeMin">
                                        <@forOption start=0 end=59 defaultVal= beginTime_mm?number />
                                    </select> 分
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">老师批改截止时间</label>
                            <div class="controls">
                                <input type="text" id="correctingTestPaper" name ="correctingTestPaper" value = "${apply.correctingTestPaper?string('yyyy-MM-dd')}"  class="apply_input_time">
                                <select class="s_time" name="correctingTestPaperHour" id="correctingTestPaperHour">
                                    <#assign testPaper_HH = apply.correctingTestPaper?string('HH')>
                                    <#assign testPaper_mm = apply.correctingTestPaper?string('mm')>
                                    <@forOption start=0 end=23 defaultVal= testPaper_HH?number />
                                </select> 时
                                <select class="s_time" name="correctingTestPaperMin" id="correctingTestPaperMin">
                                    <@forOption start=0 end=59 defaultVal=testPaper_mm?number />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">学生端成绩发布时间</label>
                            <div class="controls">
                                <input type="text" id="achievementReleaseTime" name ="achievementReleaseTime" value = "${apply.achievementReleaseTime?string('yyyy-MM-dd')}" class="apply_input_time">
                                <select class="s_time" name="achievementReleaseTimeHour" id="achievementReleaseTimeHour">
                                    <#assign releaseTime_HH = apply.achievementReleaseTime?string('HH')>
                                    <#assign releaseTime_mm = apply.achievementReleaseTime?string('mm')>
                                    <@forOption start=0 end=23 defaultVal= releaseTime_HH?number />
                                </select> 时
                                <select class="s_time" name="achievementReleaseTimeMin" id="achievementReleaseTimeMin">
                                    <@forOption start=0 end=59 defaultVal= releaseTime_mm?number />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">考试开始后</label>
                            <div class="controls">
                                <input type="text" id="minSubmittedTestPaper" name ="minSubmittedTestPaper" value = "${apply.minSubmittedTestPaper}"> 分钟后方能交卷
                            </div>
                        </div>
                        <div id = "oralLanguageFrequency_div" class="control-group" <#if apply.testPaperType??><#list apply.testPaperType?split(",") as type><#if type == "ORAL"><#else>style="display:none;" </#if></#list></#if>>
                            <label class="control-label">口语可答题次数</label>
                            <div class="controls">
                                <select id ="oralLanguageFrequency">
                                    <option name="oralLanguageFrequency" value="1" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == 1>selected</#if>  >一次</option>
                                    <option name="oralLanguageFrequency" value="2" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == 2>selected</#if> >两次</option>
                                    <option name="oralLanguageFrequency" value="3" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == 3>selected</#if> >三次</option>
                                    <option name="oralLanguageFrequency" value="-1" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == -1>selected</#if> >无限次</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">答题时长</label>
                            <div class="controls">
                                <input type="text" id="maxSubmittedTestPaper" name ="maxSubmittedTestPaper" value = "${apply.maxSubmittedTestPaper}"> 分钟
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩分制</label>
                            <div class="controls">
                                <input type="radio"  name ="grading" value="0" <#if apply.gradeType?? && apply.gradeType == 0>checked</#if>> 分数制
                                <input type="radio"  name ="grading" value="1" <#if apply.gradeType?? && apply.gradeType == 1>checked</#if> > 等第制
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">试卷总分</label>
                            <div class="controls">
                                <input type="text" id="score" name ="score" value="${apply.score!}"> 分
                            </div>
                        </div>
                        <div id="achievement" class="control-group grading">
                            <label class="control-label">等第设置</label>
                            <div class="controls">
                                <#if apply.ranks??>
                                    <table id="levelContainer" class="achievement">
                                    </table>
                                <#else>
                                <table class="achievement">
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="优" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="85" style="width:50px"> %</td>
                                        <td><=分数区间<=</td>
                                        <td><input class="endResults" type="number" value="100" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="良" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="75" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="85" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="合格" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="60" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="75" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="待合格" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="0" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="60" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="trd show">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                </#if>
                            </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">状态</label>
                            <div class="controls">
                                待录入
                                <input id="entryStatus" value="PENDINGENTRY" type="hidden">
                            </div>
                        </div>
                        <div class="form-actions" style="text-align: center">
                            <button id="submitBtn" type="button" class="btn btn-primary need_auditing" style=" margin-right: 20px">重新提交申请</button>
                            <button id="cancelBtn" type="button" class="btn btn-primary" style=" margin-left: 20px">取消</button>
                        </div>
                        <div>
                            <div class="control-group">
                                <label class="control-label">审核记录</label>
                            </div>
                            <div class="dataTables_wrapper" role="grid">
                                <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                    <thead>
                                    <tr>
                                        <th class="sorting" style="width: 60px;">申请日期</th>
                                        <th class="sorting" style="width: 60px;">申请人</th>
                                        <th class="sorting" style="width: 60px;">备注</th>
                                        <th class="sorting" style="width: 60px;">审核结果</th>
                                    </tr>
                                    </thead>
                                    <#if processResultList??  && (processResultList?size > 0)>
                                        <#list processResultList as process>
                                            <tr>
                                                <th class="sorting" style="width: 60px;">${process.processDate!}</th>
                                                <th class="sorting" style="width: 60px;">${process.accountName!}</th>
                                                <th class="sorting" style="width: 60px;">${process.processNotes!}</th>
                                                <th class="sorting" style="width: 60px;">${process.result!}</th>
                                            </tr>
                                        </#list>
                                    </#if>
                                </table>
                            </div >
                        </div>
                    </fieldset>
                </div>
            </div>
        <#else>
            <div class="box-content">
                <div class="form-horizontal">
                    <input type="hidden" id="targetSchoolId" name="targetSchoolId" />
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">考试名称</label>
                            <div class="controls">
                                <input type="text" id="unifiedExamName" name ="unifiedExamName"  >（需包含学校、城市、区域、年级等信息）
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">是否重复使用已录入</label>
                            <div class="controls">
                                <select id = "testPaperSourceType" >
                                    <option name="testPaperSourceType" value = "ANCIENT" >是</option>
                                    <option name="testPaperSourceType" value = "NEWLYADDED" selected>否</option>
                                </select>
                            </div>
                        </div>
                        <div class="flieContainer">
                            <div class="control-group" id="sourceExcelFile_div">
                                <label class="control-label">上传试卷</label>
                                <div class="controls">
                                    <button id="upload_file_but" type="button" class="btn btn-primary upload_file_but">上传文件</button>
                                    <input id="testPaperAddress" class="testPaperAddress" type="hidden" value="${testPaperAddress!}">
                                    <a  id = "testPaperAddress_a" class="testPaperAddress_a" href="${testPaperAddress!}" style="display: none;">点击下载查看详情</a>
                                <#--<span class="action" style="-moz-user-select: none;">(只能上传一套试卷的word文档,文件总大小不能超过10M)</span>-->
                                    <button type="button" class="btn btn-primary btnFile do_add_upload">添加</button>
                                    <button type="button" class="btn btn-primary btnFile do_remove_upload">删除</button>
                                </div>
                            </div>
                        </div>
                        <div class="control-group" id= "useOldTestPaper_div" style="display: none;">
                            <div style="display: inline-block;">
                                <label class="control-label">以往试卷ID</label>
                                <div class="controls">
                                    <input type="text" id="testPaperId" name ="testPaperId" placeholder="多个试卷Id用逗号分隔" style="width:1000px">
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">学生获取试卷方式</label>
                            <div class="controls">
                                <input type="radio"  name ="distribution" value="1" checked> 完全随机获取
                                <input type="radio"  name ="distribution" value="0" >轮流获取
                            </div>
                        </div>
                        <div class="control-group">
                            <div style="display: inline-block">
                                <label class="control-label">学科</label>
                                <div class="controls">
                                    <select id = "subject" >
                                    <#--    <option name="subject" value = "101">小学语文</option>
                                        <option name="subject" value = "102" >小学数学</option>
                                        <option name="subject" value =  "103" selected>小学英语</option>-->
                                    <#--<option name="subject" value = "201">初中语文</option>-->
                                    <#--<option name="subject" value = "202">初中数学</option>-->
                                        <option name="subject" value = "203">初中英语</option>
                                    </select>
                                </div>
                            </div>
                            <div id="teaching_material_div" style="display: inline-block;">
                                <div style="display: inline-block;">
                                    <label class="control-label">使用教材</label>
                                    <div class="controls">
                                        <select id = "bookCatalogId" >
                                            <#list bookProfiles as bp>
                                                <option name="bookCatalogId" value = "${bp.id}" selected  >${bp.name}</option>
                                            </#list>
                                            <option name="bookCatalogId" id="bookCatalogDefault"></option>
                                        </select>
                                    </div>
                                </div>
                                <div style="display: inline-block;">
                                    <input id="bookCatalogId-select" placeholder="输入教材名称快速定位">
                                </div>
                            </div>
                        </div>
                        <div class="control-group" style="display: none;">
                            <label class="control-label">考试场景</label>
                            <div class="controls">
                                <input type="radio"  name ="testScene" value="0" checked> 在线考试
                                <input type="radio"  name ="testScene" value="1" >集中考试 <span style="color:red">（在机房集中考试，需要现场部署考试机，官网上没有考试卡片，需要在机房里集中考试）</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">考试类型</label>
                            <div class="controls">
                                <input type="checkbox"  name ="testPaperType" value="ORAL" checked> 口语考试
                                <input type="checkbox"  name ="testPaperType" value="LISTENING" > 听力考试
                                <input type="checkbox"  name ="testPaperType" value="NORMAL"> 普通题型考试
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">年级</label>
                            <div class="controls">
                                <div id="middle_div">
                                    <input type="radio"  name ="gradeLevel" value="6"> 六年级
                                    <input type="radio"  name ="gradeLevel" value="7"> 七年级
                                    <input type="radio"  name ="gradeLevel" value="8"> 八年级
                                    <input type="radio"  name ="gradeLevel" value="9"> 九年级
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">级别</label>
                            <div class="controls">
                                <input type="radio"  name ="regionLeve" value="city" checked>市级
                                <input type="radio"  name ="regionLeve" value="country"> 区级
                                <input type="radio"  name ="regionLeve" value="school"> 校级
                            </div>
                        </div>
                        <div id="city_div" class="control-group" >
                            <label class="control-label">市</label>
                            <div class="controls">
                                <select id ="cityCode">
                                    <#if cityInfoList??>
                                        <#list cityInfoList as cityInfo>
                                            <option name="cityName" value = "${cityInfo.cityCode}" data-groupId="${cityInfo.groupId}"  data-provinceCode="${cityInfo.provinceCode}" data-provinceName="${cityInfo.provinceName}" <#if cityCodeDefault?? && cityCodeDefault?number == cityInfo.cityCode>selected</#if> >${cityInfo.cityName}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div id="regionName_div" class="control-group" style="display: none">
                            <label class="control-label">区域</label>
                            <div class="controls">
                                <select id ="regionName">
                                    <#if regionList??>
                                        <#list regionList as region>
                                            <option name="regionName" value = "${region.countyCode}" data-name="${region.countyName}">${region.countyName}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div id="unifiedExamSchool_div" class="control-group" style="display: none;">
                            <label class="control-label">学校</label>
                            <div class="controls">
                                <input id="unifiedExamSchoolView" name ="unifiedExamSchoolView" type="hidden" readonly>
                                <input id="unifiedExamSchool" name ="unifiedExamSchool" type="hidden" readonly>
                                <input id="addSchoolBtn" class ="addSchoolBtn" type="button" value="添加" data-type ="batch">
                                <input id ="agentUserId" value="${agentUserId}" type="hidden" >
                            </div>
                        </div>
                        <div id="schoolinfo_div" style="display: none;">
                            <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                                <tbody id="schoolinfo_tbody">
                                </tbody>
                            </table>
                        </div>
                        <div class="control-group">
                            <div style="display: inline-block">
                                <label class="control-label">考试开始时间:</label>
                                <div class="controls">
                                    <input type="text" id="unifiedExamBeginTime" name ="unifiedExamBeginTime" class="apply_input_time">
                                    <select class="s_time" name="unifiedExamBeginTimeHour" id="unifiedExamBeginTimeHour">
                                        <@forOption start=0 end=23 defaultVal=0 />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamBeginTimeMin" id="unifiedExamBeginTimeMin">
                                        <@forOption start=0 end=59 defaultVal=0 />
                                    </select> 分
                                </div>
                            </div>
                            <div style="display: inline-block">
                                <label class="control-label"> 考试截止时间：</label>
                                <div class="controls">
                                    <input type="text" id="unifiedExamEndTime" name ="unifiedExamEndTime" class="apply_input_time">
                                    <select class="s_time" name="unifiedExamEndTimeHour" id="unifiedExamEndTimeHour">
                                        <@forOption start=0 end=23 defaultVal=0 />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamEndTimeMin" id="unifiedExamEndTimeMin">
                                        <@forOption start=0 end=59 defaultVal=0 />
                                    </select> 分
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">老师批改截止时间</label>
                            <div class="controls">
                                <input type="text" id="correctingTestPaper" name ="correctingTestPaper" class="apply_input_time">
                                <select class="s_time" name="correctingTestPaperHour" id="correctingTestPaperHour">
                                    <@forOption start=0 end=23 defaultVal=0 />
                                </select> 时
                                <select class="s_time" name="correctingTestPaperMin" id="correctingTestPaperMin">
                                    <@forOption start=0 end=59 defaultVal=0 />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">学生端成绩发布时间</label>
                            <div class="controls">
                                <input type="text" id="achievementReleaseTime" name ="achievementReleaseTime" class="apply_input_time">
                                <select class="s_time" name="achievementReleaseTimeHour" id="achievementReleaseTimeHour">
                                    <@forOption start=0 end=23 defaultVal=0 />
                                </select> 时
                                <select class="s_time" name="achievementReleaseTimeMin" id="achievementReleaseTimeMin">
                                    <@forOption start=0 end=59 defaultVal=0 />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">考试开始后</label>
                            <div class="controls">
                                <input type="text" id="minSubmittedTestPaper" name ="minSubmittedTestPaper" value="5">分钟后方能交卷
                            </div>
                        </div>
                        <div id="oralLanguageFrequency_div" class="control-group">
                            <label class="control-label">口语可答题次数</label>
                            <div class="controls">
                                <select id ="oralLanguageFrequency">
                                    <option name="oralLanguageFrequency" value="1">一次</option>
                                    <option name="oralLanguageFrequency" value="2">两次</option>
                                    <option name="oralLanguageFrequency" value="3">三次</option>
                                    <option name="oralLanguageFrequency" value="-1">无限次</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">答题时长</label>
                            <div class="controls">
                                <input type="text" id="maxSubmittedTestPaper" name ="maxSubmittedTestPaper" > 分钟
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩分制</label>
                            <div class="controls">
                                <input type="radio"  name ="grading" value="0" checked> 分数制
                                <input type="radio"  name ="grading" value="1"> 等第制
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">试卷总分</label>
                            <div class="controls">
                                <input type="text" id="score" name ="score" > 分
                            </div>
                        </div>
                        <div id="achievement" class="control-group grading">
                            <label class="control-label">等第设置</label>
                            <div class="controls">
                                <table class="achievement">
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="优" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="85" style="width:50px"> %</td>
                                        <td><=分数区间<=</td>
                                        <td><input class="endResults" type="number" value="100" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="良" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="75" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="85" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="合格" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="60" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="75" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="待合格" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="0" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="60" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="trd show">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">状态</label>
                            <div class="controls">
                                待录入
                                <input id="entryStatus" value="PENDINGENTRY" type="hidden">
                            </div>
                        </div>
                        <div class="form-actions" style="text-align: center">
                            <button id="submitBtn" type="button" class="btn btn-primary need_auditing" style=" margin-right: 20px">提交申请</button>
                            <button id="cancelBtn" type="button" class="btn btn-primary" style=" margin-left: 20px">取消</button>
                        </div>
                        <div style="display: none">
                            <div class="control-group">
                                <label class="control-label">审核记录</label>
                            </div>
                            <div class="dataTables_wrapper" role="grid">
                                <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                    <thead>
                                    <tr>
                                        <th class="sorting" style="width: 60px;">申请日期</th>
                                        <th class="sorting" style="width: 60px;">申请人</th>
                                        <th class="sorting" style="width: 60px;">申请类型</th>
                                        <th class="sorting" style="width: 60px;">备注</th>
                                        <th class="sorting" style="width: 60px;">审核结果</th>
                                        <th class="sorting" style="width: 60px;">调整原因</th>
                                        <th class="sorting" style="width: 120px;">审核情况</th>
                                    </tr>
                                    </thead>
                                </table>
                            </div >
                        </div>
                    </fieldset>
                </div>
            </div>
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

<script type="text/javascript">
    $(function(){
        $(document).on("click",".achievement_add",function(){
            if($(".trd").hasClass("show")){
                $(".trd").removeClass("show").addClass("achievementGrading");
            }else{
                var trd = $(".trd")[0];
                var trd_html = trd.outerHTML;
                var lastTr = $(".achievement").find("tr:last");
                lastTr.after(trd_html);
                if($(".achievement tr").length>9){
                    $('.achievement_add').hide();
                }
            }
        });
        //城市联动设置
        $("#cityCode").change(function(){
            var $groupId_val = $('#cityCode>option:selected').attr("data-groupId");
            var $cityCode_val = $('#cityCode>option:selected').val();
            $.get('searchUnifiedExamApplyRegionList.vpage?groupId='+$groupId_val+"&cityCode="+$cityCode_val,function(data){
                if(data.success){
                    var regionList = $(data.regionList);
                    var rlstr = '';
                    for(var rl = 0 ; rl < regionList.length; rl++){
                        rlstr+='<option  name="regionName" value = "'+regionList[rl].countyCode+'" data-name="'+regionList[rl].countyName+'" selected>'+regionList[rl].countyName+'</option>';
                    }
                    $("#regionName").html(rlstr);
                }else{
                    alert(data.info);
                }
            })
        });
        /**
         * 教材快速定位
         * */
        $("#bookCatalogId-select").on('change',function(){
            var $select_val = $.trim($(this).val());
            var  $bookCatalogs = $("option[name='bookCatalogId']");
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
        $("#testPaperSourceType").on("change",function(){
            var $val = $(this).val();
            if($val=="NEWLYADDED"){
                $("#sourceExcelFile_div").show();
                $("#useOldTestPaper_div").hide();
                $("#teaching_material_div").css("display","inline-block");
            }else{
                $("#sourceExcelFile_div").hide();
                $("#useOldTestPaper_div").show();
                $("#teaching_material_div").hide();
            }
        });
        //学科选取事件
        $("#subject").on("change",function(){
            var $subject_val = $(this).val();
            $("input[name=gradeLevel]").each(function () {
                $(this).removeAttr("checked").parent().removeClass("checked");
            });
            if($subject_val != 203 && $subject_val!= 103){
                $("input[name=testPaperType]").each(function () {
                    if($(this).val() == "ORAL" || $(this).val() == "LISTENING"){
                        $(this).attr("disabled",true);
                        $(this).removeAttr("checked").parent().removeClass("checked");
                    }else{
                        $("#oralLanguageFrequency_div").hide();
                        $(this).attr("checked","checked");
                        $(this).parent().addClass("checked");
                    }
                });
            }else{
                $("input[name=testPaperType]").removeAttr("disabled")
            }
            var $testPaperSourceType_val = $("#testPaperSourceType").val();
            if($testPaperSourceType_val == 'NEWLYADDED'){
                $.get('/apply/create/searchBookProfileBySubject.vpage?subject='+$subject_val,function(data){
                    if(data.success){
                        var bookProfiles = $(data.bookProfiles);
                        var bpstr = '';
                        for(var bp = 0 ; bp < bookProfiles.length; bp++){
                            bpstr+='<option name="bookCatalogId" value = "'+bookProfiles[bp].id+'" selected>'+bookProfiles[bp].name+'</option>';
                        }
                        $("#bookCatalogId").html(bpstr);
                    }else{
                        alert(data.info);
                    }
                });
            }
        });
        //区域选择事件
        $("input[name='regionLeve']").on("click",function(){
            var $regionLeve_val = $(this).val();
            if($regionLeve_val== "country"){
                $("#regionName_div").show();
            }else{
                $("#regionName_div").hide();
            }
            if($regionLeve_val == "school"){
                $("#unifiedExamSchool_div").show();
            }else{
                $("#unifiedExamSchool_div").hide();
            }
        });
    });
    //考试类型 联动事件
    $("input[name='testPaperType']").on('click',function(){
        for(var i=0;i< $("input[name='testPaperType']:checked").length;i++){
            if($("input[name='testPaperType']:checked").eq(i).val() == "ORAL"){
                $("#oralLanguageFrequency_div").show();
                break
            }else{
                $("#oralLanguageFrequency_div").hide();
            }
        }
    });


    //日期控件绑定
    $("#unifiedExamBeginTime").datepicker({
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

        }
    });

    $("#unifiedExamEndTime").datepicker({
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
            var $unifiedExamBeginTime_val =  $("#unifiedExamBeginTime").val();
            if($unifiedExamBeginTime_val ){
                var $unifiedExamBeginTime = new Date($unifiedExamBeginTime_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < $unifiedExamBeginTime){
                    alert("考试截止时间不能早于考试开始日期");
                    $("#unifiedExamEndTime").val(null);//头疼的写法  return false; 竟然不管用~~~
                    // return false;
                }
            }
            return true;
        }
    });
    $("#correctingTestPaper").datepicker({
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
            var $unifiedExamEndTime_val =  $("#unifiedExamEndTime").val();
            if($unifiedExamEndTime_val ){
                var $unifiedExamEndTime = new Date($unifiedExamEndTime_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < $unifiedExamEndTime){
                    alert("老师批改试卷时间不能早于考试结束日期");
                    $("#correctingTestPaper").val(null);
                    // return false;
                }
            }
            return true;
        }
    });
    $("#achievementReleaseTime").datepicker({
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
            var $correctingTestPaper_val =  $("#correctingTestPaper").val();
            if($correctingTestPaper_val ){
                var $correctingTestPaper = new Date($correctingTestPaper_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < $correctingTestPaper){
                    alert("成绩发布日期不能早于老师批改试卷时间");
                    $("#achievementReleaseTime").val(null);
                    //return false;
                }
            }
            return true;
        }
    });

    $(document).on('click', '.achievement_remove', function () {
        if($('.achievement_remove').length == 1){
            $(this).parent().addClass("show");
        }else{
            $(this).parent().remove();
        }
        if($('.achievement_add').length < 10){
            $('.achievement_add').show();
        }
    });

    //提交数据 数据校验和数据整合
    $('#submitBtn').live('click',function(){
        // 等第设置
        var _this = $(this);

            if ($("input[name='grading']:checked").val() == "1") {
                var achievement = $('.achievementGrading');
                var len = achievement.length;
                var achievement_mix = 0;
                if((achievement.eq(0).find(".endResults").val() - achievement.eq(len-1).find(".initialScore").val()) != 100){
                    alert("等第设置中成绩区间必须涵盖0-100%");
                    return false;
                }
                for(var i = 0; i < achievement.length; i++){
                    if(achievement.eq(i).find(".achievement_name").val() == "" || achievement.eq(i).find(".achievement_name").val() == achievement.eq(i+1).find(".achievement_name").val()){
                        alert("等第名称不能为空或不能重复");
                        return false;
                    }
                    var achievement_sub = achievement.eq(i).find(".endResults").val() - achievement.eq(i).find(".initialScore").val();
                    achievement_mix += achievement_sub ;
                    if( i+1 < len && parseFloat(achievement.eq(i).find(".initialScore").val() != achievement.eq(i+1).find(".endResults").val())){
                        alert("等第下限不能大于上限");
                        return false;
                    }else{
                        if(i==0){
                            if(  parseFloat(achievement.eq(0).find(".initialScore").val()) > parseFloat(achievement.eq(0).find(".endResults").val()) ){
                                alert("等第下限不能大于上限");
                                return false;
                            }
                        }else{
                            if(  parseFloat(achievement.eq(i).find(".initialScore").val()) >= parseFloat(achievement.eq(i).find(".endResults").val()) ){
                                alert("等第下限不能大于上限");
                                return false;
                            }
                        }
                    }

                }
                if(achievement_mix != 100){
                    alert("等第上限或下限设置错误");
                    return false;
                }
            }
            var tdArr = [];
            for(var i =0;i< $('.achievementGrading').length;i++){
                tdArr.push({"rankName":$('.achievementGrading .achievement_name').eq(i).val(),"bottom":$('.achievementGrading .initialScore').eq(i).val(),"top":$('.achievementGrading .endResults').eq(i).val()})
            }
            var $unifiedExamName_val = $.trim($("#unifiedExamName").val());
            if(!$unifiedExamName_val){
                alert("考试名称不能为空");
                return false;
            }
            //试卷校验
            var $testPaperSourceType_val = $("#testPaperSourceType").val();
            var $testPaperAddress_val = "";
            var arr = [];
            for(var i = 0; i< $(".testPaperAddress").length;i++){
                arr.push($(".testPaperAddress").eq(i).val())
            }
            $testPaperAddress_val = arr.toString();
            var $testPaperId_val = $.trim($("#testPaperId").val());
            if($testPaperSourceType_val == "ANCIENT"){
                if(!$testPaperId_val){
                    alert("请选取已存在试卷的所在学校和试卷名称");
                    return false;
                }
                if($testPaperId_val.indexOf("P") == -1){
                    alert("试卷ID以'P'开头");
                    return false;
                }
                $testPaperAddress_val = "";
            }else if($testPaperSourceType_val == "NEWLYADDED"){
                if(!$testPaperAddress_val){
                    alert("请上传试卷");
                    return false;
                }
                $testPaperId_val = "";
                var $bookCatalogId = $("#bookCatalogId").val();
                if(!$bookCatalogId || $bookCatalogId == ''){
                    alert("请选择有效的教材");
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
            var $subject_val = $("#subject").val()-0;
            var $distribution = $("input[name='distribution']:checked").val() - 0;
            var $testPaperType_val = "";
            var testPaperType = [];
            if($("input[name='testPaperType']:checked").length>0){
                for(var i=0;i<$("input[name='testPaperType']:checked").length;i++){
                    testPaperType.push($("input[name='testPaperType']:checked").eq(i).val())
                }
                $testPaperType_val = testPaperType.toString();
            }
            var $gradeLevel_val = 0;
            if($("input[name='gradeLevel']:checked").length>0){
                for(var i=0;i<$("input[name='gradeLevel']:checked").length;i++){
                    $gradeLevel_val += Math.pow(2,($("input[name='gradeLevel']:checked").eq(i).val()-1))
                }
            }
            if ($gradeLevel_val == 0) {
                alert("请选择考试年级");
                return false;
            }
            if ($distribution != 0 && $distribution != 1) {
                alert("请选择学生获取试卷方式");
                return false;
            }
            //城市信息
            var $cityCode_val = $('#cityCode').val();
            var $cityName_val = $("#cityCode option:selected").text();
            var $regionCode_val = $.trim($("#regionName").val());
            var $regionName_val = $("#regionName").find("option:selected").text();
            var $provinceCode_val = $('#cityCode>option:selected').attr("data-provinceCode");
            var $provinceName_val = $('#cityCode>option:selected').attr("data-provinceName");

            var schoolinfo = $("#schoolinfo_div").find(".schoolinfo");
            var $unifiedExamSchool_val = "";
            if( schoolinfo && schoolinfo.length> 0){
                for(var f = 0; f < schoolinfo.length ;f++){
                    $unifiedExamSchool_val += "," + $( $(schoolinfo)[f]).attr("data-id");
                }
                $unifiedExamSchool_val = $unifiedExamSchool_val.substring(1);
            }

            var $regionLeve_val = $("input[name='regionLeve']:checked").val();
            var $grading_val = $("input[name='grading']:checked").val();
            if($regionLeve_val == "country" && ! $regionCode_val){
                alert("请选择有效的区域");
                return false;
            }
            if($grading_val != 0 && $grading_val != 1){
                alert("请选择成绩分制");
                return false;
            }
            if($regionLeve_val == "school"){
                if(! $unifiedExamSchool_val || $unifiedExamSchool_val == ""){
                    alert("请选择有效的统考学校");
                    return false;
                }
                if(schoolinfo.length >20){
                    alert("校级统考不能超过20所学校");
                    return false;
                }
            }else{
                $unifiedExamSchool_val = "" ;
            }
            var $unifiedExamBeginTime_val = $("#unifiedExamBeginTime").val();
            if(!$unifiedExamBeginTime_val){
                alert("考试开始时间不能为空");
                return false;
            }
            $unifiedExamBeginTime_val =  $unifiedExamBeginTime_val + " " + $("#unifiedExamBeginTimeHour").val() + ":" + $("#unifiedExamBeginTimeMin").val() + ":00";
            $unifiedExamBeginTime_val = new Date($unifiedExamBeginTime_val.replace(/-/g,"/"));
            var limitDate = new Date();
            if($testPaperSourceType_val == "ANCIENT"){
                limitDate.setDate(limitDate.getDate() + 1);
            }else{
                if($subject_val < 201){
                    limitDate.setDate(limitDate.getDate() + 9);
                }else{
                    limitDate.setDate(limitDate.getDate() + 6);
                }
            }
            if(  $unifiedExamBeginTime_val <= limitDate){
                alert("使用之前的试卷，考试开始时间需要提前一天申请,使用新的试卷,考试开始时间则需提前六天(中学)或九天（小学）申请");
                return false;
            }
            var $unifiedExamEndTime_val =$("#unifiedExamEndTime").val();
            if(!$unifiedExamEndTime_val){
                alert("考试结束时间不能为空且不能早于考试开始时间");
                return false;
            }
            $unifiedExamEndTime_val =  $unifiedExamEndTime_val + " " + $("#unifiedExamEndTimeHour").val() + ":" + $("#unifiedExamEndTimeMin").val() + ":00";
            $unifiedExamEndTime_val = new Date( $unifiedExamEndTime_val.replace(/-/g,"/"));
            if($unifiedExamEndTime_val <  $unifiedExamBeginTime_val){
                alert("考试结束时间不能为空且不能早于考试开始时间");
                return false;
            }
            var $correctingTestPaper_val = $("#correctingTestPaper").val();
            if(!$correctingTestPaper_val){
                alert("老师批改试卷时间不能为空且不能早于考试结束时间");
                return false;
            }
            $correctingTestPaper_val = $correctingTestPaper_val + " " + $("#correctingTestPaperHour").val() + ":" + $("#correctingTestPaperMin").val() + ":00";
            $correctingTestPaper_val = new Date( $correctingTestPaper_val.replace(/-/g,"/"));
            if($correctingTestPaper_val <  $unifiedExamEndTime_val){
                alert("老师批改试卷时间不能为空且不能早于考试结束时间");
                return false;
            }
            var $achievementReleaseTime_val = $("#achievementReleaseTime").val()
            if(!$achievementReleaseTime_val){
                alert("学生端成绩发布时间不能为空且不能早于老师批改试卷时间");
                return false;
            }
            $achievementReleaseTime_val = $achievementReleaseTime_val + " " + $("#achievementReleaseTimeHour").val() + ":" + $("#achievementReleaseTimeMin").val() + ":00";
            $achievementReleaseTime_val = new Date($achievementReleaseTime_val.replace(/-/g,"/"));
            if($achievementReleaseTime_val <  $correctingTestPaper_val){
                alert("学生端成绩发布时间不能为空且不能早于老师批改试卷时间");
                return false;
            }
            var  $minSubmittedTestPaper_val =$.trim( $("#minSubmittedTestPaper").val())-0;
            if(!$minSubmittedTestPaper_val || $minSubmittedTestPaper_val <= 1){
                alert("最短答题时长不能为空且需大于一分钟");
                return false;
            }
            var  $maxSubmittedTestPaper_val =$.trim( $("#maxSubmittedTestPaper").val())-0;
            if(!$maxSubmittedTestPaper_val || $maxSubmittedTestPaper_val <  $minSubmittedTestPaper_val){
                alert("答题时长不能为空且不能小于最短答题时长");
                return false;
            }
            var $oralLanguageFrequency_val = $("#oralLanguageFrequency").val();
            var $entryStatus_val = $("#entryStatus").val();

            var $workflowId = $.trim($("#workflowId").val());
            if(!$workflowId || $workflowId == ""){
                $workflowId = null;
            }
            var regular_num = /^[0-9]*[1-9][0-9]*$/g;//正整数
            var $score_val =$.trim($("#score").val());
            if(!$score_val || $score_val == '' || !regular_num.test($score_val)){
                alert("请填写有效的总分数");
                return false;
            }
            var $testScene_val = $("input[name='testScene']:checked").val();
            if($testScene_val != 0 && $testScene_val != 1){
                alert("请选择考试场景");
                return false;
            }
            var unifiedExamApply = new Object();
            unifiedExamApply.id = $id;
            unifiedExamApply.workflowId = $workflowId;
            unifiedExamApply.unifiedExamName = $unifiedExamName_val;
            unifiedExamApply.testPaperSourceType = $testPaperSourceType_val;
            if($testPaperSourceType_val == 'NEWLYADDED'){
                unifiedExamApply.bookCatalogId = $("#bookCatalogId").val();
                unifiedExamApply.bookCatalogName = $("#bookCatalogId option:selected").text();
            }
            unifiedExamApply.testPaperId = $testPaperId_val;
            unifiedExamApply.testPaperAddress = $testPaperAddress_val;
            unifiedExamApply.subject = $subject_val;
            unifiedExamApply.distribution = $distribution;
            unifiedExamApply.testPaperType =$testPaperType_val;
            unifiedExamApply.gradeLevel = $gradeLevel_val;
            //地区
            unifiedExamApply.provinceCode = $provinceCode_val;
            unifiedExamApply.provinceName = $provinceName_val;
            unifiedExamApply.cityCode =$cityCode_val;
            unifiedExamApply.cityName =$cityName_val;
            unifiedExamApply.regionCode = $regionCode_val;
            unifiedExamApply.regionName = $regionName_val;
            //考试时间地点
            unifiedExamApply.unifiedExamSchool = $unifiedExamSchool_val;
            unifiedExamApply.unifiedExamBeginTime = $unifiedExamBeginTime_val;
            unifiedExamApply.unifiedExamEndTime = $unifiedExamEndTime_val;
            unifiedExamApply.correctingTestPaper = $correctingTestPaper_val;
            unifiedExamApply.achievementReleaseTime = $achievementReleaseTime_val;
            unifiedExamApply.minSubmittedTestPaper = $minSubmittedTestPaper_val;
            unifiedExamApply.maxSubmittedTestPaper = $maxSubmittedTestPaper_val;

            if($testPaperType_val != "UNORALLANGUAGE"){
                unifiedExamApply.oralLanguageFrequency = $oralLanguageFrequency_val;
            }
            unifiedExamApply.regionLeve = $regionLeve_val;
            unifiedExamApply.entryStatus = $entryStatus_val;
            unifiedExamApply.gradeType = $grading_val;
            unifiedExamApply.score  = $score_val;
            unifiedExamApply.testScene = $testScene_val;
            //unifiedExamApply.ranks  = tdArr;
            var unifiedExamApplyJson = JSON.stringify(unifiedExamApply);
            var secondJson = JSON.stringify(tdArr);
            if(_this.hasClass('need_auditing')){
                layer.alert('请再次核实申请信息无误！任何信息错误都将导致考试无法进行!',{
                    btn:['前往确认']
                },function () {
                    layer.close(layer.index);
                    _this.removeClass('need_auditing');
                });
            }else {
                $.post('unified_exam_apply.vpage', {
                    unifiedExamApply: unifiedExamApplyJson,
                    ranks: secondJson
                }, function (data) {
                    if (!data.success) {
                        alert(data.info);
                    } else {
                        if (confirm("提交成功") == true) {
                            window.location.href = "/apply/view/list.vpage";
                        }
                    }
                });
            }
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
        var $cityCode_val = $("#cityCode").val();
        if(!schoolIds){
            alert("请输入要查询学校的id");
            return false;
        }
        var agentUserId = $("#agentUserId").val();
        if(schoolIds.length != 0 && valiteSplitByicon(schoolIds)){
            $.get("/apply/create/searchSchoolsByIdAndCityCode.vpage?schoolIds="+schoolIds+"&agentUserId="+agentUserId + "&cityCode="+$cityCode_val,function(res){
                if(res.success){
                    var dataTemp = {},errorList=[];
                    var dataTempt = [];
                    var schoolLevelError = [];
                    if(res.searchResult){
                        dataTemp.schoolData = res.searchResult.dataList;
                        dataTemp.totalNo = res.searchResult.dataList.length || [];
                        errorList = res.searchResult.invaildSchoolIdList || [];
                    }
                    var $subject = $("#subject").val()-0;
                    for(var ff = 0; ff < dataTemp.schoolData.length ; ff++){
                        var datad = new Object();
                        datad =$( dataTemp.schoolData)[ff];
                        if(datad.schoolLevel == "JUNIOR"){
                            datad.schoolLevelName = "小学";
                        }else if(datad.schoolLevel == "MIDDLE"){
                            datad.schoolLevelName = "中学";
                        }else{
                            datad.schoolLevelName = "高中";
                        }
                        if((datad.schoolLevel == "JUNIOR" && $subject < 200) || (datad.schoolLevel == "MIDDLE" && $subject > 200)){
                            dataTempt.push(datad);
                        }else{
                            schoolLevelError.push(datad.schoolId);
                        }
                    }
                    dataTemp.schoolData = dataTempt;
                    renderDepartment('#chooseSchoolTableTemp',dataTemp,"#schoolTable");
                    var tempHtml = '';
                    if(errorList.length != 0){
                        tempHtml = '<p>有'+errorList.length+'所学校为假学校、不属于此部门或者与所选城市不符，无法添加！</p>'+
                                '<p>'+errorList.join(",")+'</p>';
                    }
                    if(schoolLevelError.length != 0){
                        tempHtml +=  '<p>有'+schoolLevelError.length+'所学校级别与考试科目不对应，无法添加！</p>'+
                                '<p>'+schoolLevelError.join(",")+'</p>';
                    }
                    if(tempHtml != ''){
                        $("#alertInfoInDialog").show();
                        $("#alertInfoInDialog").html(tempHtml);
                    }else{
                        $("#alertInfoInDialog").hide();
                    }
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

    $("#add_school_submit_btn").on("click",function(){
        var $schoolIds =  $("#schoolTable").find(".js-schoolIds");
        var schoolId = '';
        var schoolView = '';
        if($schoolIds){
            for(var ii = 0 ; ii < $schoolIds.length ; ii++ ){
                var id = $($schoolIds[ii]).attr("data-id");
                var name = $($schoolIds[ii]).attr("data-name");
                schoolView += "<tr class='schoolinfo' id='schoolInfo_"+ id +"' data-id='"+id+"'><td>"+ id+" </td><td >"+name+"</td><td class='delete-schoolinfo' data-id='"+id+"'><a onclick='deleteScooolInfo("+id+")' data-id='"+id+"' href=' javascript:void(0)'>删除</a></td></tr>"
                schoolId += "," +$($schoolIds[ii]).attr("data-id");
            }
            if(schoolId){
                schoolId = schoolId.substring(1);
            }
            $("#closed_div").hide();
            $("#unifiedExamSchool").val(schoolId);
            $("#schoolinfo_tbody").empty();
            $("#schoolinfo_tbody").append(schoolView);
            $("#schoolinfo_div").show();
            $("#region_select_dialog").modal('hide');
        }else{
            alert("请输入要添加的学校");
            return false;
        }
    });
    //删除学校列表联动
    deleteScooolInfo = function(id){
        $("#schoolInfo_"+id).remove();
        var sl = $(".schoolinfo").length;
        if(!sl || sl == 0){
            $("#schoolinfo_div").hide();
        }
    };

</script>
<script id="chooseSchoolTableTemp" type="text/x-handlebars-template">
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>学校id</th>
            <th>学校名称</th>
            <th>中/小学</th>
        </tr>
        </thead>
        <tbody>
        {{#each schoolData}}
        <tr>
            <td class="js-schoolIds" data-id="{{schoolId}}" data-name ="{{schoolName}}">{{schoolId}}</td>
            <td >{{schoolName}}</td>
            <td > {{schoolLevelName}}</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
    <div class="pull-left" style="padding-top: 10px;">共计 <span style="color: red;">{{#if totalNo}}{{totalNo}}{{else}}0{{/if}}</span> 所学校</div>
</script>
<script type="text/html" id="ranksContainer">
    <table class="achievement">
        <%for(var i=0;i< res.length;i++){%>
        <tr class="achievementGrading">
            <td>等第名称</td>
            <td><input class="achievement_name" type="text" style="width:50px" value="<%=res[i].rankName%>"></td>
            <td><input class="initialScore" type="number" style="width:50px" value="<%=res[i].bottom%>"> %</td>
            <td><=分数区间<=</td>
            <td><input class="endResults" type="number" style="width:50px" value="<%=res[i].top%>"> %</td>
            <td class="achievement_add btn btn-primary">添加</td>
            <%if (i != 0){%>
            <td class="achievement_remove btn btn-primary">删除</td>
            <%}%>
        </tr>
        <%}%>
        <tr class="trd show">
            <td>等第名称</td>
            <td><input class="achievement_name" value="" type="text" style="width:50px"></td>
            <td><input class="initialScore" type="number" value="" style="width:50px"> %</td>
            <td><=分数区间<</td>
            <td><input class="endResults" type="number" value="" style="width:50px"> %</td>
            <td class="achievement_add btn btn-primary">添加</td>
            <td class="achievement_remove btn btn-primary">删除</td>
        </tr>
    </table>
</script>
<!--上传文件JS-->
<script>
        <#if applyDetail?? && applyDetail.apply?? && applyDetail.apply.ranks?? && applyDetail.apply.ranks != "">
        var ranks = ${applyDetail.apply.ranks!""} ;
        $('#levelContainer').html(template("ranksContainer",{res:ranks}));
        </#if>
    $(function() {
        var init_upload = function($dom){
            new ss.SimpleUpload({
                button: $dom, // file upload button
                url: 'upload_testpaper.vpage', // server side handler
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
                            .next().html(filename).attr("href", response.fileUrl).show();   // HTML A
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
