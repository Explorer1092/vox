<#--同步习题-->
<script id="t:EXAM" type="text/html">
    <!--ko if:$root.packageList && packageList().length > 0-->
    <!--ko if:$root.packageList && packageList().length > 1 && !examLoading()-->
    <div class="h-topicPackage">
        <div class="topicBox">
            <ul>
                <!--ko foreach:{data:packageList(),as:'package'}-->
                <li data-bind="css:{'active':$root.focusPackage() && package.id() == $root.focusPackage().id(),'more':package.id() == '-1'},click:$root.viewPackage.bind($data,$root,$index())"><!--active状态-->
                    <p data-bind="attr:{title:package.name()}">
                        <span data-bind="style:{'color':package.usageName() != '' ? package.usageColor() : '#4e5656'},text: package.usageName() != '' ? '[' + package.usageName() + ']' : ''"></span><!--ko text:package.name(),--><!--/ko--></p>
                    <span data-bind="css:{'state':package.selCount() > 0 || package.teacherUsed(),'state-active' : $root.focusPackage() && package.id() == $root.focusPackage().id() && package.selCount() <= 0 && !package.teacherUsed()},text:package.selCount() > 99 ? '99+' : (package.selCount() > 0 ? package.selCount() : package.teacherUsed() ? '用过' : '')"></span>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <div class="line"></div>
    </div>
    <!--/ko-->
    <div class="newWord-keyword" style="display: none;" data-bind="if:$root.focusPackage() && $root.tabType == 'BASIC_KNOWLEDGE' && $root.focusPackage().charWordList().length > 0,visible:$root.focusPackage() && $root.tabType == 'BASIC_KNOWLEDGE' && $root.focusPackage().charWordList().length > 0">
        <div class="label" data-bind="text:$root.focusPackage().charWordListTitle">生字或词语：</div>
        <div class="box" data-bind="foreach:{data : $root.focusPackage().charWordList,as: 'charWord'}">
            <span data-bind="text:charWord">&nbsp;</span>
        </div>
    </div>
    <!--ko if: $data.focusPackage && $data.focusPackage() && $data.focusPackage().flag() == 'more_question' && $data.knowledgePoints && $data.knowledgePoints() != null && $data.knowledgePoints().length > 0-->
    <div class="e-knowledgePoint">
        <div class="tips">提示：选择对应知识点后再选择题目，能提高布置作业效率哦！</div>
        <div class="kpBox kpBox-show">
            <!--ko foreach:{data:$data.knowledgePoints(),as:'kpCategory'}-->
            <div class="kpList">
                <div class="left"><span class="name" data-bind="text:kpCategory.kpType() + '：'"></span></div>
                <div class="right">
                    <!--ko foreach:{data:kpCategory.knowledgePoints(),as:'point'}-->
                    <span class="label" data-bind="css:{'active':point.kpId() == $root.focusPointId()},text:point.kpName,click:$root.point_click.bind($data,$element,$root,kpCategory.kpType())"></span>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
    </div>
    <!--/ko-->
    <div class="h-tab-box" data-bind="visible:$data.focusPackage && $data.focusPackage() && $data.focusPackage().flag() == 'more_question'">
        <div class="t-homework-form t-tab-box">
            <dl class="dl-cell01" data-bind="if:$root.testMethods && testMethods().length > 0,visible:$root.testMethods && testMethods().length > 0">
                <dt>考法：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p data-bind="css:{'w-checkbox-current' : $root.testMethodIsCheckedAll()},click:testMethodAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:testMethods(),as:'testMethod'}-->
                            <p data-bind="css:{'w-checkbox-current':testMethod.checked},click:$root.testMethod_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:testMethod.name,attr:{title:testMethod.name}">&nbsp;</span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell01" data-bind="if:words().length > 0,visible:words().length > 0">
                <dt>生字：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p data-bind="css:{'w-checkbox-current' : $root.wordAllChecked},click:wordAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:words(),as:'word'}-->
                            <p data-bind="css:{'w-checkbox-current':$root.isWordChecked(word)},click:$root.word_click.bind($data,$root,word)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:word"></span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell01" data-bind="if:categories().length > 0,visible:categories().length > 0">
                <dt>类别：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p data-bind="css:{'w-checkbox-current' : $root.categoryAllChecked},click:categoryAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:categories(),as:'category'}-->
                            <p data-bind="css:{'w-checkbox-current':$root.isCategoryChecked(category)},click:$root.category_click.bind($data,$root,category)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:category"></span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell01" data-bind="if:patterns().length > 0,visible:patterns().length > 0">
                <dt>题型：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p data-bind="css:{'w-checkbox-current' : patternIsCheckedAll()},click:patternAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:patterns(),as:'pattern'}-->
                            <p data-bind="css:{'w-checkbox-current':pattern.checked},click:$root.pattern_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:pattern.name(),attr:{title:pattern.name()}"></span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell02" data-bind="if:(tabType == 'EXAM' || tabType == 'BASIC_KNOWLEDGE') && difficulties().length > 0,visible:difficulties().length > 0">
                <dt>难度：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down">
                            <p data-bind="css:{'w-checkbox-current' : difficultyIsCheckedAll()},click:difficultyAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:difficulties(),as:'difficulty'}-->
                            <p data-bind="css:{'w-checkbox-current':difficulty.checked},click:$root.difficulty_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:difficulty.name">容易</span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell03" data-bind="if:(tabType != 'CHINESE_READING') && assigns().length > 0,visible:(tabType != 'CHINESE_READING') && assigns().length > 0">
                <dt>布置：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down">
                            <p data-bind="css:{'w-checkbox-current' : assignIsCheckedAll()},click:assignAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:assigns(),as:'assign'}-->
                            <p data-bind="css:{'w-checkbox-current':assign.checked},click:$root.assign_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:assign.name()"></span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>

    <!--ko if:focusExamList().length > 0 && !examLoading()-->
    <!--ko if:$root.focusPackage().id() != "-1"-->
    <div class="h-topicPackage-hd">
        <div class="title" data-bind="text:$root.focusPackage().name()">&nbsp;</div>
        <div class="allCheck" data-bind="css:{'checked' : $root.focusPackage().selCount() >= $root.focusPackage().totalCount()},click:$root.addOrRemovePackage"><!--全选被选中时添加类checked-->
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:'全选' + $root.focusPackage().totalCount() + '道题'">&nbsp;</span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.focusPackage().totalMin() + '分钟'">&nbsp;</i></span>
        </div>
    </div>
    <!--/ko-->
    <!--ko foreach:{data:focusExamList(),as:'question'}-->
    <div class="h-set-homework examTopicBox" data-bind="singleExamHover:question.checked">
        <div class="seth-hd">
            <div class="seth-type" data-bind="if:question.readingType,visible:question.readingType"><p class="sub-type" data-bind="text:question.readingType()"></p></div>
            <p class="fl">
                <span style="display: none;" data-bind="visible:question.testMethodName,text:question.testMethodName ? question.testMethodName() : ''"></span>
                <span data-bind="visible:$root.tabType != 'BASIC_KNOWLEDGE',text:question.questionType"></span>
                <span data-bind="text:question.difficultyName"></span>
                <span data-bind="if:question.upImage,visible:question.upImage">支持上传解答过程</span>
                <!--ko if:question.assignTimes && question.assignTimes() > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes() == 0)-->
                <span class="noBorder" data-bind="text:'共被使用' + question.assignTimes() + '次'"></span>
                <!--/ko-->
            </p>
            <p class="fr">
                <!--ko if:question.teacherAssignTimes && question.teacherAssignTimes() > 0-->
                <span class="txtYellow" data-bind="style:{marginRight:(question.readingType ? '20px' : '0px')}">布置过</span>
                <!--/ko-->
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <div class="box" data-bind="attr:{id : 'mathExamImg' + $index()}"></div>
                    <div data-bind="text:$root.loadExamImg(question.id(),$index())"></div>
                </div>
                <div class="linkGroup">
                    <a href="javascript:void(0)" style="display: none;" class="viewExamAnswer" data-bind="click:$root.viewExamAnswer.bind($data,$root,$index())">查看答案解析</a>
                    <a href="javascript:void(0)" style="display: none;" class="feedback" data-bind="click:$root.feedback.bind($data,$root)">反馈</a>
                </div>
                <div class="btnGroup">
                    <!--ko if:question.similarQuestionIds && question.similarQuestionIds().length > 0-->
                    <a href="javascript:void(0)" data-bind="click:$root.viewSimilarQuestion.bind($data,$root)">做错需订正（查看类题）</a>
                    <!--/ko-->
                    <a href="javascript:void(0)" class="btn" data-bind="ifnot:question.checked(),visible:!question.checked(), click:$root.addExam.bind($data,$root,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                    <a href="javascript:void(0)" class="btn cancel" data-bind="if:question.checked(),visible:question.checked(),click:$root.removeExam.bind($data,$root)"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
    <div class="system_message_page_list homework_page_list" style="width: 100%; background: #edf5fa; padding:15px 0; text-align: center;">

        <a data-bind="css:{'disable' : currentPage() <= 1,'enable' : currentPage() > 1},click:page_click.bind($data,$root,currentPage() - 1)" href="javascript:void(0);" v="prev"><span>上一页</span></a>

        <!--ko if:totalPage() <= 7-->
        <!--ko foreach:ko.utils.range(1,totalPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)" href="javascript:void(0);">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:totalPage() > 7 && currentPage() <= 4-->
        <!--ko foreach:ko.utils.range(1,6)-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <span class="points">...</span>
        <a data-bind="click:page_click.bind($data,$root,totalPage())">
            <span data-bind="text:totalPage()"></span>
        </a>
        <!--/ko-->

        <!--ko if:totalPage() > 7 && currentPage() > 4-->
        <a data-bind="click:page_click.bind($data,$root,1)"><span>1</span></a>
        <span class="points">...</span>

        <!--ko if:(totalPage() - currentPage()) <= 3-->
        <!--ko foreach:ko.utils.range(totalPage() - 5,totalPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:(totalPage() - currentPage()) > 3-->
        <!--ko foreach:ko.utils.range(currentPage() - 2,currentPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->

        <!--ko foreach:ko.utils.range(currentPage() + 1,currentPage() + 2)-->
        <a data-bind="click:$parent.page_click.bind($data,$root,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->
        <span class="points">...</span>
        <a data-bind="click:page_click.bind($data,$root,totalPage())"><span data-bind="text:totalPage()"></span></a>
        <!--/ko-->
        <!--/ko-->

        <a data-bind="css:{'disable' : totalPage() <= 1 || currentPage() >= totalPage(), 'enable' : totalPage() > 1 && currentPage() < totalPage()},click:page_click.bind($data,$root,currentPage() + 1)" href="javascript:void(0);" v="next"><span>下一页</span></a>
        <div class="pageGo">
            <input value="" type="text" data-bind="textInput:userInputPage" /><span class="goBtn" data-bind="click:goSpecifiedPage">GO</span>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->

    <!--ko if:(!$root.packageList() || $root.packageList().length == 0 || focusExamList().length == 0) && !examLoading()-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>没有找到题目</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:examLoading,visible:examLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
</script>

<script id="t:UFO_EXAM" type="text/html">
    <span class="name"><%=tabTypeName%></span>
    <span class="count" data-count="<%=count%>">0</span>
    <span class="icon"><i class="J_delete h-set-icon-delete h-set-icon-deleteGrey"></i></span>
</script>

<script id="t:EXAM_DETAIL" type="text/html">
    <div class="ste-dialogTbxt">
        <div class="tbxtList">
            <div class="tbxtHeader">
                <div class="title"><%= title%></div>
                <div class="pull-down allCheck-btn">
                    <span class="txt-left">预计<i><%= time%>分钟</i>完成</span>
                    <p class="J_selectAll check-btn <% if(checkAll){%> w-checkbox-current <%}%>"> <!--全选被选中时添加类w-checkbox-current-->
                        <span class="w-checkbox"></span>
                        <span class="w-icon-md">全选<%= questions.length%>道题</span>
                    </p>
                </div>
            </div>
            <% for(var i=0;i<questions.length;i++) {%>
            <div class="h-set-homework tbxtInner <% if(questions[i].isChecked){%> current<%}%>">
                <div class="seth-hd">
                    <p class="fl">
                        <span><%= questions[i].questionType%></span>
                        <span><%= questions[i].difficultyName%></span>
                        <% if(questions[i].assignTimes > 0) { %>
                        <span class="noBorder">共被使用<%= questions[i].assignTimes%>次</span>
                        <% } %>
                    </p>
                    <% if(questions[i].teacherAssignTimes > 0) { %>
                    <p class="fr"><span>您已布置<%= questions[i].teacherAssignTimes%>次</span></p>
                    <% } %>
                </div>
                <div qid="<%= questions[i].questionId%>">
                   <div id="subject_<%= questions[i].questionId%>">题目加载...</div>
                    <div class="btnGroup">
                        <% if(!questions[i].isChecked) { %>
                        <a href="javascript:void(0)" class="J_addOrRemove btn" qid="<%= questions[i].questionId%>"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                        <%}else{%>
                        <a href="javascript:void(0)" class="J_addOrRemove btn cancel" qid="<%= questions[i].questionId%>"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                        <% } %>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
    </div>
</script>

<script type="text/html" id="T:SIMILAR_QUESTIONS">
    <div class="topicReview-dialog" id="viewSimilarQuestions" style="margin-top: -20px;">
        <div class="tips">学生做错原题后，需要做一道类题进行巩固。</div>
        <div class="h-set-homework">
            <div class="seth-mn" style="height: 300px; overflow-y: auto;">
                <div id="mathExamImgSource"></div>
                <div data-bind="text:$root.loadExamImg()"></div>
            </div>
        </div>
        <div class="btns-box">
            <a href="javascript:void(0);" class="w-btn w-btn-small" style="display: none;" data-bind="visible:questionIds.length > 1,click:$root.nextQuestion">换一道题目</a>
        </div>
    </div>
</script>

<script id="t:INTELLIGENCE_EXAM" type="text/html">
    <!--ko if:$root.examLoading && !examLoading() && $root.packageList && packageList().length > 0-->
    <div class="h-topicPackage">
        <div class="s-topicBox">
            <ul>
                <!--ko foreach:{data:packageList(),as:'package'}-->
                <li data-bind="css:{'active':$root.focusPackage() && package.id() == $root.focusPackage().id(),'special':package.id() == '-1','last-li':($index() % 4 == 3)},click:$root.viewPackage.bind($data,$root,$index())">
                    <span data-bind="attr:{title:package.name()}"><!--ko text:package.name()--><!--/ko--></span>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <div class="line"></div>
    </div>
    <div class="customOption-box" style="display: none;" data-bind="fadeVisible: $root.displayAdvancedOptions,if:$root.displayAdvancedOptions() && $root.smartSearchItems != null">
        <div class="t-homework-form t-tab-box">
            <dl class="dl-cell01">
                <dt>场景：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <!--ko foreach:{data : $root.smartSearchItems.packageAlgoTypes, as :'algoType'}-->
                            <p data-bind="css:{'w-radio-current':algoType.type() == $root.smartSearchItems.focusAlgoType()},click:$root.smartSearchItems.algoTypeClick.bind($root.smartSearchItems,algoType.type())">
                                <span class="w-radio"></span>
                                <span class="w-icon-md" data-bind="text:algoType.name()">&nbsp;</span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell02" style="display: none;" data-bind="visible:$root.subject == 'MATH'">
                <dt>难度：</dt>
                <dd>
                    <div class="starSelect w-select" data-bind="pullDownHover:true">
                        <div class="current">
                            <span class="starBox">
                                <i class="h-starIcon" data-bind="css:{light : $root.smartSearchItems.focusDifficult() >= 1}"></i>
                                <i class="h-starIcon" data-bind="css:{light : $root.smartSearchItems.focusDifficult() >= 2}"></i>
                                <i class="h-starIcon" data-bind="css:{light : $root.smartSearchItems.focusDifficult() >= 3}"></i>
                                <i class="h-starIcon" data-bind="css:{light : $root.smartSearchItems.focusDifficult() >= 4}"></i>
                                <i class="h-starIcon" data-bind="css:{light : $root.smartSearchItems.focusDifficult() >= 5}"></i></span>
                            <span class="w-icon w-icon-arrow"></span>
                        </div>
                        <ul class="starBoxSelect" data-bind="foreach:{data : $root.smartSearchItems.difficults(), as:'difficult'}">
                            <li data-bind="click:$root.smartSearchItems.starBoxClick.bind($data,$root.smartSearchItems),clickBubble: false">
                                <span class="starBox">
                                    <i class="h-starIcon" data-bind="css:{light : difficult >= 1}"></i>
                                    <i class="h-starIcon" data-bind="css:{light : difficult >= 2}"></i>
                                    <i class="h-starIcon" data-bind="css:{light : difficult >= 3}"></i>
                                    <i class="h-starIcon" data-bind="css:{light : difficult >= 4}"></i>
                                    <i class="h-starIcon" data-bind="css:{light : difficult >= 5}"></i>
                                </span>
                            </li>
                        </ul>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell03">
                <dt>题量：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <span class="w-addSub-int">
                        <a class="w-btn w-btn-mini" href="javascript:void(0);" data-bind="css:{'w-btn-disabled' : $root.smartSearchItems.minusBtnDisabled},click:$root.smartSearchItems.minusQuestionCnt.bind($root.smartSearchItems)">-</a>
                        <input class="w-int" type="text" readonly="readonly" data-bind="value: $root.smartSearchItems.questionCnt">
                        <a class="w-btn w-btn-mini" href="javascript:void(0);" data-bind="css:{'w-btn-disabled' : $root.smartSearchItems.addBtnDisabled},click:$root.smartSearchItems.addQuestionCnt.bind($root.smartSearchItems)">+</a>
                    </span>
                </dd>
            </dl>
            <dl class="dl-cell04" data-bind="if:$root.smartSearchItems.knowledgePoints().length > 0,visible:$root.smartSearchItems.knowledgePoints().length > 0">
                <dt>知识点：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p class="w-checkbox-current" data-bind="css:{'w-checkbox-current' : $root.smartSearchItems.knowledgePointsAllChecked()},click:$root.smartSearchItems.knowledgePointAllCheckedClick.bind($root.smartSearchItems)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data: $root.smartSearchItems.knowledgePoints,as:'point'}-->
                            <p data-bind="css:{'w-checkbox-current':point.checked()},click:$root.smartSearchItems.knowledgePointClick.bind($data,$root.smartSearchItems)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:point.name(),attr:{title:point.name()}">&nbsp;</span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell01" data-bind="if:$root.smartSearchItems.patterns().length > 0,visible:$root.smartSearchItems.patterns().length > 0">
                <dt>题型：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p data-bind="css:{'w-checkbox-current' : $root.smartSearchItems.patternIsCheckedAll()},click:$root.smartSearchItems.patternAllClick.bind($root.smartSearchItems)" class="w-checkbox-current">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:$root.smartSearchItems.patterns(),as:'pattern'}-->
                            <p data-bind="css:{'w-checkbox-current':pattern.checked},click:$root.smartSearchItems.pattern_click.bind($data,$root.smartSearchItems)" class="w-checkbox-current">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:pattern.name(),attr:{title:pattern.name()}">&nbsp;</span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="btnBox"><a href="javascript:void(0)" class="w-btn w-btn-well" data-bind="click:$root.smartSearchItems.goExam.bind($root.smartSearchItems)">开始组题</a></div>
    </div>

    <!--ko if: $data.focusPackage && $data.focusPackage() && $data.focusPackage().flag() == 'more_question' && $data.knowledgePoints && $data.knowledgePoints() != null && $data.knowledgePoints().length > 0-->
    <div class="e-knowledgePoint">
        <div class="tips">提示：选择对应知识点后再选择题目，能提高布置作业效率哦！</div>
        <div class="kpBox kpBox-show">
            <!--ko foreach:{data:$data.knowledgePoints(),as:'kpCategory'}-->
            <div class="kpList">
                <div class="left"><span class="name" data-bind="text:kpCategory.kpType() + '：'"></span></div>
                <div class="right">
                    <!--ko foreach:{data:kpCategory.knowledgePoints(),as:'point'}-->
                    <span class="label" data-bind="css:{'active':point.kpId() == $root.focusPointId()},text:point.kpName,click:$root.point_click.bind($data,$element,$root,kpCategory.kpType())"></span>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
    </div>
    <!--/ko-->

    <div class="h-tab-box" data-bind="visible:$data.focusPackage && $data.focusPackage() && $data.focusPackage().flag() == 'more_question'">
        <div class="t-homework-form t-tab-box">
            <dl class="dl-cell01" data-bind="if:patterns().length > 0,visible:patterns().length > 0">
                <dt>题型：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p data-bind="css:{'w-checkbox-current' : patternIsCheckedAll()},click:patternAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:patterns(),as:'pattern'}-->
                            <p data-bind="css:{'w-checkbox-current':pattern.checked},click:$root.pattern_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:pattern.name(),attr:{title:pattern.name()}"></span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell02" data-bind="if:difficulties().length > 0,visible:difficulties().length > 0">
                <dt>难度：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down">
                            <p data-bind="css:{'w-checkbox-current' : difficultyIsCheckedAll()},click:difficultyAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:difficulties(),as:'difficulty'}-->
                            <p data-bind="css:{'w-checkbox-current':difficulty.checked},click:$root.difficulty_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:difficulty.name">容易</span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="dl-cell03" data-bind="if:assigns().length > 0,visible:assigns().length > 0">
                <dt>布置：</dt>
                <dd style="overflow: hidden; *zoom:1;">
                    <div class="t-homeworkClass-list">
                        <div class="pull-down">
                            <p data-bind="css:{'w-checkbox-current' : assignIsCheckedAll()},click:assignAllClick">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md">全部</span>
                            </p>
                            <!--ko foreach:{data:assigns(),as:'assign'}-->
                            <p data-bind="css:{'w-checkbox-current':assign.checked},click:$root.assign_click.bind($data,$root)">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" data-bind="text:assign.name()"></span>
                            </p>
                            <!--/ko-->
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>

    <!--ko if:focusExamList().length > 0 && !examLoading()-->
    <!--ko if:$root.focusPackage().flag() == "package" || $root.focusPackage().flag() == "smart_exam_questions"-->
    <div class="new-topicPackage-hd">
        <div class="l-topic-inner">
            <div class="title">
                <div data-bind="text:$root.focusPackage().name()">&nbsp;</div>
                <div class="star-box">
                    <!--ko if: $root.subject == 'MATH'-->
                    题包难度
                    <i class="h-starIcon" data-bind="css:{'light' : $root.focusPackage().difficulty() >= 1}"></i>
                    <i class="h-starIcon" data-bind="css:{'light' : $root.focusPackage().difficulty() >= 2}"></i>
                    <i class="h-starIcon" data-bind="css:{'light' : $root.focusPackage().difficulty() >= 3}"></i>
                    <i class="h-starIcon" data-bind="css:{'light' : $root.focusPackage().difficulty() >= 4}"></i>
                    <i class="h-starIcon" data-bind="css:{'light' : $root.focusPackage().difficulty() >= 5}"></i>
                    <!--/ko-->
                    <a href="javascript:void(0)" class="linkBlue" data-bind="click:$root.forwardSmartSearch">更换条件</a>
                </div>
            </div>
        </div>
        <div class="allCheck" data-bind="css:{'checked' : $root.focusPackage().selCount() >= $root.focusPackage().totalCount()},click:$root.addOrRemovePackage"><!--全选被选中时添加类checked-->
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:'全选' + $root.focusPackage().totalCount() + '道题'">&nbsp;</span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.focusPackage().totalMin() + '分钟'">&nbsp;</i></span>
        </div>
    </div>
    <!--/ko-->
    <!--ko foreach:{data:focusExamList(),as:'question'}-->
    <div class="h-set-homework examTopicBox" data-bind="singleExamHover:question.checked">
        <div class="seth-hd">
            <p class="fl">
                <span data-bind="text:question.questionType"></span>
                <span data-bind="text:question.difficultyName"></span>
                <span data-bind="if:question.upImage,visible:question.upImage">支持上传解答过程</span>
                <!--ko if:question.assignTimes && question.assignTimes() > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes() == 0)-->
                <span class="noBorder" data-bind="text:'被使用' + question.assignTimes() + '次'"></span>
                <!--/ko-->
                <!--ko if:question.questionTypeId && question.questionTypeId() == 1010013-->
                <span class="noBorder" style="color:red;" data-bind="text:'支持手写'"></span>
                <!--/ko-->
            </p>
            <p class="fr">
                <!--ko if:question.teacherAssignTimes && question.teacherAssignTimes() > 0-->
                <span class="txtYellow">布置过</span>
                <!--/ko-->
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <div class="box" data-bind="attr:{id : 'mathExamImg' + $index()}"></div>
                    <div data-bind="text:$root.loadExamImg(question.id(),$index())"></div>
                </div>
                <div class="linkGroup">
                    <a href="javascript:void(0)" style="display: none;" class="viewExamAnswer" data-bind="click:$root.viewExamAnswer.bind($data,$root,$index())">查看答案解析</a>
                    <a href="javascript:void(0)" style="display: none;" class="feedback" data-bind="click:$root.feedback.bind($data,$root)">反馈</a>
                </div>
                <div class="btnGroup">
                    <!--ko if:question.similarQuestionIds && question.similarQuestionIds().length > 0-->
                    <a href="javascript:void(0)" data-bind="click:$root.viewSimilarQuestion.bind($data,$root)">做错需订正（查看类题）</a>
                    <!--/ko-->
                    <a href="javascript:void(0)" class="btn" data-bind="ifnot:question.checked(),visible:!question.checked(), click:$root.addExam.bind($data,$root,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                    <a href="javascript:void(0)" class="btn cancel" data-bind="if:question.checked(),visible:question.checked(),click:$root.removeExam.bind($data,$root)"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
    <div class="system_message_page_list homework_page_list" style="width: 100%; background: #edf5fa; padding:15px 0; text-align: center;">

        <a data-bind="css:{'disable' : currentPage() <= 1,'enable' : currentPage() > 1},click:page_click.bind($data,$root,currentPage() - 1)" href="javascript:void(0);" v="prev"><span>上一页</span></a>

        <!--ko if:totalPage() <= 7-->
        <!--ko foreach:ko.utils.range(1,totalPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)" href="javascript:void(0);">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:totalPage() > 7 && currentPage() <= 4-->
        <!--ko foreach:ko.utils.range(1,6)-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <span class="points">...</span>
        <a data-bind="click:page_click.bind($data,$root,totalPage())">
            <span data-bind="text:totalPage()"></span>
        </a>
        <!--/ko-->

        <!--ko if:totalPage() > 7 && currentPage() > 4-->
        <a data-bind="click:page_click.bind($data,$root,1)"><span>1</span></a>
        <span class="points">...</span>

        <!--ko if:(totalPage() - currentPage()) <= 3-->
        <!--ko foreach:ko.utils.range(totalPage() - 5,totalPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:(totalPage() - currentPage()) > 3-->
        <!--ko foreach:ko.utils.range(currentPage() - 2,currentPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$root,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->

        <!--ko foreach:ko.utils.range(currentPage() + 1,currentPage() + 2)-->
        <a data-bind="click:$parent.page_click.bind($data,$root,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->
        <span class="points">...</span>
        <a data-bind="click:page_click.bind($data,$root,totalPage())"><span data-bind="text:totalPage()"></span></a>
        <!--/ko-->
        <!--/ko-->

        <a data-bind="css:{'disable' : totalPage() <= 1 || currentPage() >= totalPage(), 'enable' : totalPage() > 1 && currentPage() < totalPage()},click:page_click.bind($data,$root,currentPage() + 1)" href="javascript:void(0);" v="next"><span>下一页</span></a>
        <div class="pageGo">
            <input value="" type="text" data-bind="textInput:userInputPage" /><span class="goBtn" data-bind="click:goSpecifiedPage">GO</span>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->

    <!--ko if:!examLoading() && (!$root.focusPackage() || $root.focusPackage().flag() != 'smart_exam_questions') && (!$root.packageList() || $root.packageList().length == 0 || focusExamList().length == 0)-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>没有找到题目</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <!--ko if:!smartSearchLoading() && $root.smartSearchResponseInfo() && $root.focusPackage && $root.focusPackage() && $root.focusPackage().flag() == 'smart_exam_questions' -->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p data-bind="text:$root.smartSearchResponseInfo">没有找到题目</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:examLoading,visible:examLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:smartSearchLoading,visible:smartSearchLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
</script>

<script id="T:EXAM_LIST_TEMPLATE" title="作业类型下展示应试题" type="text/html">
    <div class="h-set-homework examTopicBox" data-bind="singleExamHover:question.checked">
        <div class="seth-hd">
            <p class="fl">
                <span data-bind="text:question.questionType"></span>
                <span data-bind="text:question.difficultyName"></span>
                <span data-bind="if:question.upImage,visible:question.upImage">支持上传解答过程</span>
                <!--ko if:question.assignTimes() > 0-->
                <span class="noBorder" data-bind="text:'共被使用' + question.assignTimes() + '次'"></span>
                <!--/ko-->
            </p>
            <p class="fr">
                <!--ko if:question.lossRate && question.lossRate() > 0-->
                <span class="txtYellow" data-bind="text:'班级失分率' + question.lossRate() + '%'"></span>
                <!--/ko-->
                <!--ko if:question.teacherAssignTimes() > 0-->
                <span class="txtYellow" data-bind="text:'您已布置' + question.teacherAssignTimes() + '次'"></span>
                <!--/ko-->
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <div class="box" data-bind="attr:{id : 'mathExamImg' + $index()}"></div>
                    <div data-bind="text:$root.loadExamImg(question.id(),$index())"></div>
                </div>
                <div class="linkGroup">
                    <a href="javascript:void(0)" style="display: none;" class="viewExamAnswer" data-bind="click:$root.viewExamAnswer.bind($data,$root,$index())">查看答案解析</a>
                    <a href="javascript:void(0)" style="display: none;" class="feedback" data-bind="click:$root.feedback.bind($data,$root)">反馈</a>
                </div>
                <div class="btnGroup">
                    <!--ko if:question.similarQuestionIds && question.similarQuestionIds().length > 0-->
                    <a href="javascript:void(0)" data-bind="click:$root.viewSimilarQuestion.bind($data,$root)">做错需订正（查看类题）</a>
                    <!--/ko-->
                    <a href="javascript:void(0)" class="btn" data-bind="ifnot:question.checked(),visible:!question.checked(), click:$root.addExam.bind($data,$root,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                    <a href="javascript:void(0)" class="btn cancel" data-bind="if:question.checked(),visible:question.checked(),click:$root.removeExam.bind($data,$root)"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                </div>
            </div>
        </div>
    </div>
</script>


<script id="t:FALLIBILITY_QUESTION" title="高频错题" type="text/html">
    <div class="new-topicPackage">
        <div class="new-tpTips" style="display: none;" data-bind="visible:$root.tabType == 'FALLIBILITY_QUESTION'">根据各班级每周作业情况，提取共性错题，每周五更新。布置仅对指定班级有效。</div>
        <!--ko if: !$root.examLoading() && $root.packageBoxes().length > 0-->
        <div class="new-tpListBox" style="max-height: 340px; overflow: auto;">
            <!--ko foreach:{data:$root.packageBoxes,as:'box'}-->
            <div class="new-tpTime" data-bind="text:box.sectionTitle">12月23日~12月29日（本周）</div>
            <ul data-bind="foreach:{data : box.packageList,as:'package'}">
                <li class="new-tpList" data-bind="css:{'active' : $root.focusPackage() && package.id == $root.focusPackage().id},click:$root.viewPackage.bind($data,$root)">
                    <p class="name" data-bind="text:package.name">&nbsp;</p>
                    <p class="percent" data-bind="text:package.description1">&nbsp;</p>
                    <p class="time">共<!--ko text:package.questionNum--><!--/ko-->题，预计用时<!--ko text:package.totalMin--><!--/ko-->分钟</p>
                </li>
            </ul>
            <!--/ko-->
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:$root.packagePage,if:$root.packagePage != null}"></div>
        <!--/ko-->
    </div>
    <!--ko if: !$root.examLoading() && $root.packageBoxes().length > 0-->
    <div class="finR-kTopic" data-bind="css:{'show':$root.showMorePoints()},if:$root.focusPackagePoints().length > 0,visible:$root.focusPackagePoints().length > 0">
        <span>已选知识点 (点击划去)：</span>
        <!--ko foreach:{data : $root.focusPackagePoints(), as:'point'}-->
        <label class="span-del" data-bind="css:{'span-del' : point.disabled()},attr:{title:point.name()},click:$root.updatePointState.bind($data,$root)"><!--ko text:point.name()--><!--/ko--><i class="del-line"></i></label>
        <!--/ko-->
        <span class="t-arrow" data-bind="click:$root.showOrHidePoints"></span>
    </div>

    <div class="h-topicPackage-hd" data-bind="if:$root.focusPackage() != null && $root.focusExamList().length > 0,visible:$root.focusPackage() != null && $root.focusExamList().length > 0">
        <div class="title"></div>
        <div class="allCheck" data-bind="css:{'checked' : $root.currentSelCnt() > 0 && $root.currentSelCnt() >= $root.currentTotalCnt()},click:$root.addOrRemovePackage"><!--全选被选中时添加类checked-->
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:'全选' + $root.currentTotalCnt() + '道题'">&nbsp;</span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.currentTotalMin() + '分钟'">&nbsp;</i></span>
        </div>
    </div>
    <!--ko if:$root.focusExamList && $root.focusExamList().length > 0-->
    <!--ko template:{name:'T:EXAM_LIST_TEMPLATE',foreach:focusExamList(), as:'question'}--><!--/ko-->
    <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:$root.examPage,if:$root.examPage != null}"></div>
    <!--/ko-->
    <!--/ko-->

    <!--ko if:(!$root.packageBoxes() || $root.packageBoxes().length == 0 || focusExamList().length == 0) && !examLoading()-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p style="display:none;" data-bind="visible:$root.tabType == 'FALLIBILITY_QUESTION'">暂无高频错题</p>
                    <p style="display:none;" data-bind="visible:['FALLIBILITY_QUESTION'].indexOf($root.tabType) == -1">没有找到题目</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:examLoading,visible:examLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
</script>

