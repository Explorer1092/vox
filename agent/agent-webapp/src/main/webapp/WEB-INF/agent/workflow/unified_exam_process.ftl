<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='统考申请' page_num=3>
<style>
    body{
        text-shadow:none;
    }
    .form-horizontal .controls{
        padding-top: 5px;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i>统考申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#assign data = applyData.apply>
        <div class="box-content">
            <form class="form-horizontal">
                <input type="hidden" id="targetSchoolId" name="targetSchoolId" />
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">考试名称</label>
                        <div class="controls">
                        ${data.unifiedExamName}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">是否重复使用已录入</label>
                        <div class="controls">
                            <#if data.testPaperSourceType ==  "ANCIENT"> 是
                            <#else >
                                否
                            </#if>
                        </div>
                    </div>
                    <#if data.testPaperSourceType ==  "NEWLYADDED">
                        <div class="control-group" id="sourceExcelFile_div">
                            <label class="control-label">上传试卷</label>
                            <div class="controls">
                                <#if data.fetchPaperAddress()?? && data.fetchPaperAddress()?size gt 0>
                                    <#list data.fetchPaperAddress() as fetch>
                                        <a href="${fetch!""}">点击查看试卷</a>
                                    </#list>
                                </#if>

                            </div>
                        </div>
                    <#else >
                        <div class="control-group" id= "useOldTestPaper_div">
                            <div style="display: inline-block;">
                                <label class="control-label">使用试卷学校ID</label>
                                <div class="controls">
                                    <#if data.fetchPaperId()?? && data.fetchPaperId()?size gt 0>
                                    <#list data.fetchPaperId() as paperId>
                                    ${paperId!""}
                                    </#list>
                                </#if>
                                </div>
                            </div>
                        </div>
                    </#if>
                    <div class="control-group">
                        <label class="control-label">学生获取试卷方式</label>
                        <div class="controls">
                            <#if data.distribution??>
                                <#if data.distribution == 0>轮流获取<#elseif data.distribution == 1>随机获取</#if>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <div style="display: inline-block;" >
                            <label class="control-label">学科</label>
                            <div class="controls">
                                <#if data.subject == "101">
                                    小学语文
                                <#elseif data.subject == "103">
                                    小学英语
                                <#elseif data.subject == "102">
                                    小学数学
                                <#elseif data.subject == "203">
                                    初中英语
                                <#elseif data.subject == "201">
                                    初中语文
                                <#elseif data.subject == "202">
                                    初中数学
                                </#if>
                            </div>
                        </div>
                        <#if data.testPaperSourceType == "NEWLYADDED" && data.bookCatalogName??>
                            <div style="display: inline-block;" >
                                <label class="control-label">学科</label>
                                <div class="controls">
                                ${data.bookCatalogName}
                                </div>
                            </div>
                        </#if>
                    </div>
                    <div class="control-group">
                        <label class="control-label">考试场景</label>
                        <div class="controls">
                        <#if data.testScene??><#if data.testScene == 0>在线考试<#elseif data.testScene == 1>集中考试</#if>
                        </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">考试类型</label>
                        <div class="controls">
                            <#if data.fetchTestPaperType()?? && data.fetchTestPaperType()?size gt 0>
                                <#list data.fetchTestPaperType() as testPaperType>
                                    <#if testPaperType == "ORAL">
                                        口语考试
                                    <#elseif testPaperType == "LISTENING">
                                        听力考试
                                    <#elseif testPaperType == "NORMAL">
                                        普通题型考试
                                    </#if>
                                </#list>
                            </#if>

                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">年级</label>
                        <div class="controls">
                            <#if data.fetchGradeLevel()??>
                                <#list data.fetchGradeLevel() as grade>
                                    <#if grade == 1>
                                        一年级
                                    <#elseif grade == 2>
                                        二年级
                                    <#elseif grade == 3>
                                        三年级
                                    <#elseif grade == 4>
                                        四年级
                                    <#elseif grade == 5>
                                        五年级
                                    <#elseif grade == 6>
                                        六年级
                                    <#elseif grade == 7>
                                        七年级
                                    <#elseif grade == 8>
                                        八年级
                                    <#else>
                                        九年级
                                    </#if>
                                </#list>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">级别</label>
                        <div class="controls">
                            <#if data.regionLeve == "city">
                               ${data.cityName}
                            <#elseif data.regionLeve == "country">
                                区级
                            <#else >
                                校级
                            </#if>
                        </div>
                    </div>
                    <#if data.regionLeve == "country">
                        <div id="regionName_div" class="control-group">
                            <label class="control-label">区域</label>
                        <div class="controls">
                        ${data.cityName}   ${data.regionName!}
                        </div>
                        </div>
                    </#if>
                    <#if data.regionLeve == "school">
                        <div id="unifiedExamSchool_div" class="control-group">
                            <label class="control-label">学校</label>
                        </div>
                        <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                            <thead>
                            <tr>
                                <th>学校id</th>
                                <th>学校名称</th>
                                <th>中/小学</th>
                            </tr>
                            </thead>

                            <tbody>
                                <#list schoolList as school>
                                <tr>
                                    <td class="js-schoolIds">${school.schoolId}</td>
                                    <td >${school.schoolName}</td>
                                    <td >${school.schoolLevel.description}</td>
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>
                    <div class="control-group" disabled="true">
                        <div style="display: inline-block">
                            <label class="control-label">考试开始时间:</label>
                            <div class="controls">
                            ${data.unifiedExamBeginTime}
                            </div>
                        </div>
                        <div style="display: inline-block">
                            <label class="control-label"> 考试截止时间：</label>
                            <div class="controls">
                            ${data.unifiedExamEndTime}
                            </div>
                        </div>

                    </div>
                    <div class="control-group">
                        <label class="control-label">老师批改截止时间</label>
                        <div class="controls">
                        ${data.correctingTestPaper}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">成绩发布时间</label>
                        <div class="controls">
                        ${data.achievementReleaseTime}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">考试开始后</label>
                        <div class="controls">
                        ${data.minSubmittedTestPaper}分钟
                        </div>
                    </div>
                    <#if data.testPaperType == "ORAL">
                        <div class="control-group">
                            <label class="control-label">口语可答题次数</label>
                            <div class="controls">
                                <#if data.oralLanguageFrequency == 1>
                                    一次
                                <#elseif data.oralLanguageFrequency == 2>
                                    两次
                                <#elseif data.oralLanguageFrequency == 3>
                                    三次
                                <#else >
                                    无限次
                                </#if>
                            </div>
                        </div>
                    </#if>

                    <div class="control-group">
                        <label class="control-label">答题时长</label>
                        <div class="controls">
                        ${data.maxSubmittedTestPaper}分钟
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">成绩分制</label>
                        <div class="controls">
                            <#if data.gradeType??><#if data.gradeType == 0>分数制<#elseif data.gradeType == 1>等第制</#if></#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷总分</label>
                        <div class="controls">
                        ${data.score!}分
                        </div>
                    </div>
                    <#if data.gradeType??>
                        <#if data.gradeType == 1>
                            <div class="control-group">
                                <label class="control-label">等第设置</label>
                                <div class="controls" id="levelContainer">
                                </div>
                            </div>
                        </#if>
                    </#if>
                    <div class="control-group">
                        <label class="control-label">状态</label>
                        <div class="controls">
                            ${data.entryStatus.desc}
                        </div>
                    </div>
                    <div>
                        <div class="control-group">
                            <label class="control-label">审核记录</label>
                        </div>
                        <div class="dataTables_wrapper" role="grid">
                            <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">审核日期</th>
                                    <th class="sorting" style="width: 60px;">审核人</th>
                                    <th class="sorting" style="width: 60px;">处理结果</th>
                                    <th class="sorting" style="width: 60px;">处理意见</th>
                                    <th class="sorting" style="width: 120px;">备注</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#assign processResultList = applyData.processResultList>
                                    <#if processResultList?? && (processResultList?size > 0)>
                                        <#list processResultList as process>
                                        <tr>
                                            <th class="sorting" style="width: 60px;">${process.processDate}</th>
                                            <th class="sorting" style="width: 60px;">${process.accountName}</th>
                                            <th class="sorting" style="width: 60px;">${process.result}</th>
                                            <th class="sorting" style="width: 60px;">${process.processNotes}</th>
                                            <th class="sorting" style="width: 120px;"></th>
                                        </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div >
                    </div>
                </fieldset>
            </form>
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
</div>
<script type="text/html" id="ranksContainer">
    <table class="achievement">
        <%for(var i=0;i< res.length;i++){%>
            <tr class="trd show">
                <td>等第名称</td>
                <td><%=res[i].rankName%></td>
                <td><%=res[i].bottom%>%</td>
                <td><=分数区间<</td>
                <td><%=res[i].top%>%</td>
            </tr>
        <%}%>
    </table>
</script>
<script type="text/javascript">

    function processFunction(processResult, workflowId){
        var processNote = $('#processNote').val();
        if(processNote == ""){
            alert("请填写处理意见！");
            return;
        }
        console.log(workflowId);
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
    $("#processNote").on("click",function(){
        $(this).focus();
    });
    <#if data.ranks?? && data.ranks != "">
    var ranks = ${data.ranks!""};
    $('#levelContainer').html(template("ranksContainer",{res:ranks}))
    </#if>
</script>
</@layout_default.page>
