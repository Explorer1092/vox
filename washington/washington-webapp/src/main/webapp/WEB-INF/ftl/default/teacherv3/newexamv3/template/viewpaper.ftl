<script type="text/html" id="T:ViewByPaper">
    <div class="content-main">
        <div class="h-answerBox">
            <div class="h-wrap">
                <!--ko if:newExamPaperInfos.length > 1 -->
                <div class="test-paper">试卷：
                    <!--ko foreach:{data:newExamPaperInfos,as:'paper'}-->
                    <a class="active" href="javascript:void (0);" data-bind="text:paper.paperName,css:{'active':$index()==$root.currentPaperIndex()},click:$root.changePaper.bind($root,$index())"></a>
                    <!--/ko-->
                </div>
                <!--/ko-->
                <div class="total-num">
                    <div class="people-num lf">
                        <p class="p1" data-bind="text:jointNum()+'人'"></p>
                        <p class="p2">参考人数</p>
                    </div>
                    <div class="people-num rg">
                        <p class="p1" data-bind="text:submitNum()+'人'"></p>
                        <p class="p2">交卷人数</p>
                    </div>
                </div>
                <div class="content-title"></div>
                <div class="h-content">
                    <div class="h-content" id="J_paperContent" style="position:relative;"></div>
                    <!--ko if:$root.themeForSubs().length > 0 -->
                    <div style="display: none;" data-bind="text:$root.loadQuestionContent()"></div>
                    <!--/ko-->
                </div>
            </div>
        </div>
        <div class="h-answerCard">
            <div class="cardBox">
                <div class="cardInner">
                    <!--ko foreach:{data : themeForSubs,as:'theme'}-->
                    <div class="cardTitle" data-bind="text:($index()+1)+'.'+theme.desc"></div>
                    <div class="cardNum">
                        <!--ko foreach:{data : theme.subQuestions,as:'question'}-->
                        <a data-bind="text:question.index,click:$root.questionNoClick.bind($root,question.qid,question.index,question.subIndex)" >
                        </a>
                        <!--/ko-->
                    </div>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
    <a class="teach-gotop" href="javascript:void(0);"></a>
</script>

<script id="tmpl_TOP_3" type="text/html">
    <div class="answeBox">
        <p class="answe-itm-2"><span>分值：<%=standardScore%>分</span><span style="display: none;">平均分：<%=averScore%>分</span><span>得分率：<%=rate%>%</span></p>
        <div class="answe-all ">
            <p class="text-1">
                <span>作答：正确<i class="correct"><%=rightCount%></i>人；  错误<i class="wrong"><%=wrongCount%></i>人</span>
                <a class="r-txt btnShow" href="javascript:void (0);" data-type="3">展开详情</a>
            </p>
        </div>
        <div class="answerContent" style="display: none;">
            <%include('t:clazzAnswerTable')%>
        </div>
    </div>
</script>

<#-- 小题为选择题题型-->
<script id="tmpl_CHOICE" type="text/html">
    <div class="answeBox">
        <p class="answe-itm-2"><span>分值：<%=standardScore%>分</span><span style="display: none;">平均分：<%=averScore%>分</span><span>得分率：<%=rate%>%</span></p>
        <div class="answe-all " >
            <p class="text-1">
                <span>作答：正确<i class="correct"><%=rightCount%></i>人；  错误<i class="wrong"><%=wrongCount%></i>人</span>
                <a class="r-txt btnShow" href="javascript:void (0);" data-type="2">展开详情</a>
            </p>
            <div class="opt-all answerContent" style="display: none;">
                    <%include('t:clazzAnswerTable')%>
            </div>
        </div>
    </div>
</script>

<script id="t:clazzAnswerTable" type="text/html">
    <table>
        <tbody>
        <%for(var t = 0; t < answerStudentsList.length; t++){%>
        <% var answerOption = answerStudentsList[t],views = answerOption.views;%>
        <% var students = answerStudentsList[t].students;%>
        <tr>
            <td>
                <i class="<%=(answerOption.master == 'NOT_ANSWER' ? 'lookNone' : (answerOption.master == 'RIGHT' ? 'lookRight' : (answerOption.master == 'WRONG' ? 'lookWrong' : 'lookOther')))%>"></i>
                <span class="lookInfo2">
                    <%for(var m = 0,mLen = views.length; m < mLen; m++){%>
                       <%include("t:fractionCard",{value: views[m],subMaster: answerOption.subMaster[m]})%><%=((m < mLen -1)? ',':'')%>
                    <%}%>
                </span>
            </td>
            <td>
                <span class="lookInfo stud-all">
                    <%if(students && students.length > 0){%>
                        <%if(answerOption.studentDetailShowAnswer){%>
                            <%include("t:studentWithAnswer",{students:students,subContentTypeId:subContentTypeId,content:content})%>
                        <%}else{%>
                            <%include("t:studentNoAnswer",{students:students})%>
                        <%}%>
                    <%}else{%>
                        无
                    <%}%>
                </span>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
</script>

<script id="t:studentWithAnswer" data-tip="带答案的学生" type="text/html">
    <%for(var k = 0,kLen = students.length;k < kLen; k++){%>
    <div class="itm">
        <a class="itm-name" href="javascript:void(0);"><%= students[k].userName%></a>
        <%= ((k < kLen - 1) ? '、':'') %>
        <span class="layer-answer">
            <em></em>学生答案：<br>
            <%if(subContentTypeId == 25){%>
            &nbsp;&nbsp;&nbsp;&nbsp;<%== content%><br>
            <%}%>
            <%for(var j = 0,jLen = students[k].answer.length; j < jLen; j++){%>
                <%include('t:fractionCard',{ value:students[k].answer[j], subMaster : students[k].subMaster[j],subContentTypeId:subContentTypeId})%>
                <%if(subContentTypeId != 25){%> <%=((j < jLen -1) ? ',' : '')%> <%}else{%><br><%}%>
            <%}%>
        </span>
    </div>
    <%}%>
</script>
<script id="t:studentNoAnswer" data-tip="不带答案的学生" type="text/html">
    <%for(var k = 0,kLen = students.length;k < kLen; k++){%>
        <%= students[k].userName%><%= ((k < kLen - 1) ? '、':'') %>
    <%}%>
</script>

<script id="t:fractionCard" data-tip="显示分数的模板" type="text/html">
    <i class="<%=(subMaster ? 'right' : 'wrong')%>"><%if(subContentTypeId == 25){%><i style="color: #fff;white-space: nowrap;"> = </i><%}%><%=subMaster%><%= optimizeAnswer(value)%></i>
</script>

<#--口语题-->
<script id="tmpl_ORAL" type="text/html">
    <div class="answeBox">
        <p class="answe-itm-2"><span>分值：<%=standardScore%>分</span><span style="display: none;">平均分：<%=averScore%>分</span><span>得分率：<%=rate%>%</span></p>
        <p class="answe-itm ">学生答案：
            <% if (students && students.length > 4) { %>
            <a class="r-txt btnShow" data-type="1" href="javascript:void (0);">显示全部</a>
            <% } %>
        </p>
        <% if (students && students.length > 0) { %>
        <div class="stud-record-2 answerContent">
            <% for (var i = 0; i < students.length; i ++) { %>
            <% var student =students[i]; %>
            <div class="stud-answer-itm">
                <div class="teach-record-box">
                    <a class="teach-audio-s1 btnPlay"  data-audio_src="<%=student.voiceUrls.join('|')%>" href="javascript:void (0);"></a>
                </div>
                <div class="stud-info">
                    <p class="name" title="<%=student.userName%>"><%=student.userName%></p>
                    <p class="frac"><i><%=student.score%></i>分
                        <% if (!endCorrect) { %>
                        <a class="btn-modify btnEditScore" href="javascript:void(0);">修改</a>
                        <% } %>
                    </p>
                    <p class="frac-input" style="display: none">
                        <input type="hidden" class="scoreValue" value="<%=student.score%>" />
                        <input type="hidden" class="questionId" value="<%=qid%>" />
                        <input type="hidden" class="userId" value="<%=student.userId%>" />
                        <input type="hidden" class="subIndex" value="<%=(subIndex)%>" />
                        <input type="hidden" class="standardScore" value="<%=standardScore%>" />
                        <input type="hidden" class="index" value="<%=index%>" />
                        <input class="input-box" type="text" value="">
                        <a class="btn-modify btnEditScoreOk" href="javascript:void (0);">完成</a>
                    </p>
                    <span class="err-txt"  style="display: none">修改失败</span>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>
        <% if (kouYuReferenceAnswers && kouYuReferenceAnswers.length > 0) { %>
        <div class="answe-refer">
            <i>参考答案：</i>
            <% for (var i = 0; i < kouYuReferenceAnswers.length; i ++) { %>
            <span style="float:left">（<%=i+1 %>）</span><p><%==kouYuReferenceAnswers[i].answer%></p>
            <% } %>
        </div>
        <% }%>

    </div>
</script>

