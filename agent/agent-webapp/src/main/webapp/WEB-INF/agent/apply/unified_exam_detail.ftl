<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='统考申请' page_num=3>
<style>
    body{
        text-shadow:none;
    }
    .radio input[type="radio"], .checkbox input[type="checkbox"]{
        float: left;
        margin-left: -7px;
    }
    .form-horizontal .controls{
        padding-top: 5px;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <#assign data = applyData.apply>
        <div class="box-header well">
            <h2><i class="icon-th"></i>统考申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if data.subject == "201" || data.subject == "202" || data.subject == "203">
            <div class="pull-right">
                <a href="javascript:;" class="btn btn-success js-edit">修改考试相关控制</a>
                <a href="javascript:;" class="btn btn-success js-download">下载人工分成绩</a>
            </div>
            </#if>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <#if data.entryStatus?? && data.entryStatus == "PUBLISHONLINE">
                        <div class="control-group">
                            <label class="control-label">使用试卷ID</label>
                            <div class="controls">
                                <#if data.fetchPaperId()?? && data.fetchPaperId()?size gt 0>
                                    <#list data.fetchPaperId() as paperId>
                                ${paperId!""}
                                </#list>
                                </#if>
                            </div>
                        </div>
                    </#if>
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
                    </#if>
                    <#if data.testPaperId?? && (data.testPaperId!"")!="">
                        <div class="control-group" id="useOldTestPaper_div">
                            <div style="display: inline-block;">
                                <label class="control-label">使用试卷ID</label>
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
                        <div style="display: inline-block;">
                            <label class="control-label">学生获取试卷方式</label>
                            <div class="controls">
                                <#if data.distribution??>
                                    <#if data.distribution == 0>
                                        轮流获取
                                    <#else>
                                        完全随机获取
                                    </#if>
                                </#if>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <div style="display: inline-block;">
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
                                <label class="control-label">教材</label>
                                <div class="controls">
                                    ${data.bookCatalogName}
                                </div>
                            </div>
                        </#if>
                    </div>
                    <#if data.fetchTestScene()??>
                        <div class="control-group">
                            <label class="control-label">考试场景</label>
                            <div class="controls">
                            ${data.fetchTestScene().desc}
                            </div>
                        </div>
                    </#if>
                    <div class="control-group">
                        <label class="control-label">考试类型</label>
                        <div class="controls">
                            <#if data.fetchTestPaperType()?has_content>
                                <#list data.fetchTestPaperType() as testPaperType>
                                    <#if testPaperType == "ORALLANGUAGE">
                                        口语考试（试卷只有口语题）
                                    <#elseif testPaperType == "COMPREHENSIVEEXAMINATION">
                                        口语+非口语综合考试
                                    <#elseif testPaperType == "COMPREHENSIVEEXAMINATION">
                                        非口语考试
                                    <#elseif testPaperType == "NORMAL">
                                        普通考试 &nbsp
                                    <#elseif testPaperType == "ORAL">
                                        口语考试 &nbsp
                                    <#elseif testPaperType == "LISTENING">
                                        听力考试 &nbsp
                                    </#if>
                                </#list>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">年级</label>
                        <div class="controls">
                            <#if data.fetchGradeLevel()?has_content>
                                <#list data.fetchGradeLevel() as level>
                                    <#if level == 1>
                                        一年级 &nbsp
                                    <#elseif level == 2>
                                        二年级 &nbsp
                                    <#elseif level == 3>
                                        三年级 &nbsp
                                    <#elseif level == 4>
                                        四年级 &nbsp
                                    <#elseif level == 5>
                                        五年级 &nbsp
                                    <#elseif level == 6>
                                        六年级 &nbsp
                                    <#elseif level == 7>
                                        七年级 &nbsp
                                    <#elseif level == 8>
                                        八年级 &nbsp
                                    <#elseif level == 9>
                                        九年级 &nbsp
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
                            ${data.cityName}  ${data.regionName!}
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
                    <#if data.fetchTestGradeType()??>
                        <div class="control-group">
                            <label class="control-label">成绩分制</label>
                            <div class="controls">
                            ${data.fetchTestGradeType().desc}
                            </div>
                        </div>
                    </#if>
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
                  <#--  <div class="control-group">
                        <label class="control-label">通知邮箱</label>
                        <div class="controls">
                            ${data.sendEmail!}
                        </div>
                    </div>-->
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
                                    <#if applyData.processResultList?? && (applyData.processResultList?size > 0)>
                                        <#list applyData.processResultList as process>
                                            <tr>
                                                <th class="sorting" style="width: 60px;"><#if process.processDate?has_content>${process.processDate?string("yyyy-MM-dd")}</#if></th>
                                                <th class="sorting" style="width: 60px;">${process.accountName!}</th>
                                                <th class="sorting" style="width: 60px;">${process.result!}</th>
                                                <th class="sorting" style="width: 60px;">${process.processNotes!}</th>
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
<script>
    <#if data.ranks?? && data.ranks != "">
        var ranks = ${data.ranks!""};
        $('#levelContainer').html(template("ranksContainer",{res:ranks}));
    </#if>
    var entryStatus = '${data.entryStatus!""}';
    var applyId = '${data.id!""}';
    $('.js-edit').on('click',function () {
        if(entryStatus !== 'ONLINE'){
            layer.alert('试卷录入完毕后，才能修改相关控制');
        }else{
            window.location.href = 'update_exam_control_page.vpage?applyId=' + applyId;
        }
    });
    var tikuDomain = '${tikuDomain!""}';
    $('.js-download').on('click',function () {
        window.location.href = tikuDomain + '/service/downloadExamResult?applyId=' + applyId + '&type=2';
    });
</script>
</@layout_default.page>
