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
                    <!--ko foreach:{data:themeForSubs(),as:'theme'}-->
                    <div class="subject-type">
                        <h5 class="sub-title" data-bind="text:$17.Arabia_To_SimplifiedChinese($index()+1)+'、'+theme.desc"></h5>
                        <div data-bind="foreach:{data:theme.questionList,as:'qid'}">
                            <div data-bind="attr:{id:'question_'+qid+$index()}">题目加载中...</div>
                            <div style="display: none;" data-bind="text:$root.loadQuestionContent(qid,$index())"></div>
                        </div>
                    </div>
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
    <a class="teach-gotop" href="javascript:;"></a>

</script>
<#--口语题-->
<script id="tmpl1" type="text/html">
    <div class="answeBox">
        <p class="answe-itm-2"><span>分值：<%=standardScore%>分</span><span>平均分：<%=averScore%>分</span><span>得分率：<%=rate%>%</span></p>
        <p class="answe-itm ">学生答案：
            <% if (kouYuAnswer&&kouYuAnswer.length>4) { %>
            <a class="r-txt btnShow" data-type="1" href="javascript:void (0);">显示全部</a>
            <% }else if(kouYuAnswer.length == 0) { %>
            无
            <% } %>
        </p>
        <% if (kouYuAnswer&&kouYuAnswer.length>0) { %>
        <div class="stud-record-2 answerContent">
            <% for (var i = 0; i < kouYuAnswer.length; i ++) { %>
            <% var student =kouYuAnswer[i]; %>
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
                        <input type="hidden" class="subIndex" value="<%=(subIndex - 1)%>" />
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
        <% if (kouYuReferenceAnswers&&kouYuReferenceAnswers.length>0) { %>
        <div class="answe-refer">
            <i>参考答案：</i>
            <% for (var i = 0; i < kouYuReferenceAnswers.length; i ++) { %>
            <span style="float:left">（<%=i+1 %>）</span><p><%==kouYuReferenceAnswers[i].answer%></p>
            <% } %>
        </div>
        <% }%>

    </div>
</script>
<#--选择题-->
<script id="tmpl2" type="text/html">
    <div class="answeBox">
        <p class="answe-itm">答案：<i class="correct"><%=standardAnswer%></i></p>
        <p class="answe-itm-2"><span>分值：<%=standardScore%>分</span><span>平均分：<%=averScore%>分</span><span>得分率：<%=rate%>%</span></p>

        <div class="answe-all " >
            <p class="text-1">
                <span>作答：正确<i class="correct"><%=rightNum%></i>人；  错误<i class="wrong"><%=wrongNum%></i>人</span>
                <a class="r-txt btnShow" href="javascript:void (0);" data-type="2">展开详情</a>
            </p>
            <div class="opt-all answerContent" style="display: none;">
                <% for (var i = 0; i < xuanZeAnswer.length; i ++) { %>
                <% var xzanswer =xuanZeAnswer[i]; %>
                <p class="stud-all">
                    <span class="<%=xzanswer.answer==standardAnswer?'correct':'wrong'%>"><%=xzanswer.answer=="未答"?xzanswer.answer:"选"+xzanswer.answer%>：</span>
                    <% for (var j = 0; j < xzanswer.students.length; j ++) { %>
                    <% var student =xzanswer.students[j]; %>
                    <i><%=student.userName%></i>
                    <% if(j < xzanswer.students.length-1){ %>
                    、
                    <% } %>
                    <% } %>
                </p>
                <% } %>
            </div>
        </div>
        <p class="answe-itm">解析：<%==analysis.length==0?"无":analysis %></p>
    </div>
</script>
<#--填空题-->
<script id="tmpl3" type="text/html">
    <div class="answeBox">
        <p class="answe-itm">答案：<i class="correct"><%=standardAnswer%></i></p>
        <p class="answe-itm-2"><span>分值：<%=standardScore%>分</span><span>平均分：<%=averScore%>分</span><span>得分率：<%=rate%>%</span></p>
        <div class="answe-all ">
            <p class="text-1">
                <span>作答：正确<i class="correct"><%=rightNum%></i>人；  错误<i class="wrong"><%=wrongNum%></i>人</span>
                <a class="r-txt btnShow" href="javascript:void (0);" data-type="3">展开详情</a>
            </p>
        </div>
        <div class="answerContent" style="display: none;">
            <div class="stud-all "><span class="s-tit">正确：</span>
                <% if(tianKongRightStudents.length>0 ){ %>
                <% for (var i = 0; i < tianKongRightStudents.length; i ++) { %>
                <% var student =tianKongRightStudents[i]; %>
                <div class="itm">
                    <a class="itm-name" href="javascript:;"><%=student.userName%>

                    </a>
                    <% if(i < tianKongRightStudents.length-1){ %>
                    、
                    <% } %>
                    <span class="layer-answer">
                                    <em></em>学生答案：<br>
                                    <% for (var j = 0; j < student.answerList.length; j ++) { %>
                                        <% var sanswer =student.answerList[j]; %>
                                        <i class="<%=sanswer.grasp?'':'wrong'%>">
                                            <%=sanswer.answer%>
                                            <% if(j < student.answerList.length-1){ %>
                                            ;
                                            <% } %>
                                        </i>
                                    <% } %>
                            </span>
                </div>
                <% } %>
                <% } %>
            </div>


            <div class="stud-all"><span class="s-tit">错误：</span>
                <% if(tianKongWrongStudents.length>0 ){ %>
                <% for (var i = 0; i < tianKongWrongStudents.length; i ++) { %>
                <% var student =tianKongWrongStudents[i]; %>
                <div class="itm">
                    <a class="itm-name" href="javascript:;"><%=student.userName%>
                    </a>
                    <span class="layer-answer">
                            <em></em>学生答案：<br>
                            <% for (var j = 0; j < student.answerList.length; j ++) { %>
                                <% var sanswer =student.answerList[j]; %>
                                <i class="<%=sanswer.grasp?'':'wrong'%>">
                                    <%=sanswer.answer%>
                                    <% if(j < student.answerList.length-1){ %>
                                    ;
                                    <% } %>
                                </i>

                            <% } %>
                        </span>
                    <% if(i < tianKongWrongStudents.length-1){ %>
                    、
                    <% } %>
                </div>
                <% } %>
                <% } %>
            </div>
        </div>
        <p class="answe-itm">解析：<%==analysis.length==0?"无":analysis %> </p>
    </div>
</script>
