<#--口算练习-->
<script id="t:UFO_MENTAL_CART" type="text/html">
    <span class="name"><%=tabTypeName%></span>
    <span class="count" data-count="<%=count%>">0</span>
    <span class="icon"><i class="J_delete h-set-icon-delete h-set-icon-deleteGrey"></i></span>
</script>


<script id="t:MENTAL_ARITHMETIC" type="text/html">
    <div class="h-topicPackage">
        <div class="topicBox">
            <ul data-bind="foreach:{data:$root.packageList,as:'packageObj'}">
                <li data-bind="css:{'active':packageObj.id() == $root.focusPackage().id()},click:$root.packageClick.bind($data,$index(),$root)">
                    <p data-bind="attr:{title:packageObj.name},text:packageObj.name()">&nbsp;</p>
                </li>
            </ul>
        </div>
        <div class="line"></div>
    </div>
    <div class="oralCount" data-bind="if:$root.focusPackage().id() == 'PK_CUSTOM',visible:$root.focusPackage().id() == 'PK_CUSTOM'">
        <div class="oralTime">
            <span class="label">限定时间：</span>
            <div class="timeBox" data-bind="foreach:{data:$root.limitTimes,as:'time'}">
                <p data-bind="css:{'w-radio-current' : time == $root.focusLimitTime()},click:$root.timeClick.bind($data,$root)">
                    <span class="w-radio"></span><span class="checkTxt" data-bind="text:time == 0 ? '不限时' : time + '分钟'">&nbsp;</span>
                </p>
            </div>
            <div class="state" data-bind="click:$root.viewTimeLimitHelp"><i class="questionIcon"></i>限定答题时间学生会得到奖励</div>
        </div>
        <div class="title"><span></span></div>
        <div class="t-choose-Knowledge" >
            <div class="desListBox" data-bind="if:$root.mentalPoints.pointList().length > 0,visible:$root.mentalPoints.pointList().length > 0">
                <!--ko foreach:{data:$root.mentalPoints.pointList,as:'point'}-->
                <div class="describe" data-bind="css:{'odd':$index(0 % 2 == 0}">
                    <div class="left">
                        <span data-bind="css:$root.mentalPoints.getKpTypeName(point.kpType(),'color'),text:$root.mentalPoints.getKpTypeName(point.kpType(),'name')">&nbsp;</span><!--ko text:point.kpName--><!--/ko-->（共<!--ko text:point.questionCount--><!--/ko-->道）<i class="assign" data-bind="visible:point.teacherAssignTimes() > 0">已布置</i>
                    </div>
                    <div class="right">
                        <span class="w-addSub-int">
                            <a class="w-btn w-btn-mini w-btn-disabled" href="javascript:void (0)" data-bind="css:{'w-btn-disabled' : point.assignCount() <= 0},click:$root.mentalPoints.removeMentalQuestion.bind($data,$root.mentalPoints)">-</a>
                            <span class="w-int" data-bind="text:point.assignCount">0</span>
                            <a class="w-btn w-btn-mini" href="javascript:void(0)" data-bind="css:{'w-btn-disabled' : point.assignCount() > 0 && (point.assignCount() >= point.questionCount())},click:$root.mentalPoints.addMentalQuestion.bind($data,$root.mentalPoints)">+</a>
                        </span>
                    </div>
                </div>
                <!--/ko-->
            </div>
            <div class="noZhishid"  style="display: none;" data-bind="visible:$root.mentalPoints.pointList().length == 0">
                <p>当前章节下没有知识点，<br/>你可以切换章节或点击添加知识点</p>
            </div>
            <div class="loadMore" data-bind="click:$root.showPointPopup"><i class="addIcon">+</i>添加更多知识点</div>
        </div>
    </div>
    <div style="margin-top:-10px;padding: 0 20px 20px 20px;color: #d3d3d3;">若布置过程中遇见问题，建议使用移动客户端布置</div>
    <div class="oralCount" style="margin-top: -13px;" data-bind="if:$root.displayPointQuestions().length > 0,visible: $root.displayPointQuestions().length > 0">
        <div class="title" data-bind="if:$root.focusPackage().id() == 'PK_CUSTOM',visible:$root.focusPackage().id() == 'PK_CUSTOM'">
            <span>已选题目预览</span>
        </div>
        <div class="t-choose-Knowledge">
            <div class="oralRecommend" data-bind="if:$root.focusPackage().id() == 'PK_RECOMMEND',visible:$root.focusPackage().id() == 'PK_RECOMMEND'">
                <p>布置口算题</p>
                <#--这个5分钟是产品规定死的,若修改这个时间，请把mentalarithmetic.js中的self.updateTimeLimit(5)也修改一下-->
                <p class="time">限时5分钟，共<!--ko text:$root.questionCount--><!--/ko-->道题</p>
                <a href="javascript:void(0);" class="allBtn" style="cursor: pointer;" data-bind="css:{'cancel':$root.recommendSelected()},click:$root.recommendSelectAll,text:$root.recommendSelected() ? '移除':'选入'">选入 </a>
            </div>
            <!--ko foreach:{data:$root.displayPointQuestions,as:'pointObj'}-->
            <div class="oralType" data-bind="text:pointObj.kpName() + (($root.focusPackage().id() == 'PK_RECOMMEND') ? '(' + pointObj.questionCount() + ')' : '')">&nbsp;</div>
            <table class="t-questionBox" data-bind="style:{'border-bottom' : ($root.focusPackage().id() == 'PK_RECOMMEND' && pointObj.postQuestions && pointObj.postQuestions().length > 0) ? '0px' : '1px solid #dae6ee;'}">
                <tbody data-bind="foreach:ko.utils.range(1,Math.ceil(pointObj.questions().length/3))">
                <tr data-bind="foreach:{data:pointObj.questions.slice($index() * 3, ($index() + 1) * 3),as:'question'}">
                    <td style="background: rgb(255, 255, 255);" data-bind="exchangeHover:$root.focusPackage().id() == 'PK_CUSTOM'">
                        <div class="t-question" data-bind="attr:{'id':question.questionId() + '-' + $index()}">正在加载...</div>
                        <div data-bind="text:$root.renderVueQuestion(question.questionId(),$index())"></div>
                        <div class="exchange" style="display: none;">
                            <a class="delete" href="javascript:void(0);" data-bind="click:$root.delQuestion.bind($data,pointObj,$root)">
                                <span class="trashBox"></span>
                            </a>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
            <!--ko if:$root.focusPackage().id() == 'PK_RECOMMEND' && pointObj.postQuestions && pointObj.postQuestions().length > 0-->
            <div style="border-bottom: 1px solid #dae6ee;">
                <ul class="h-analysis-box2">
                    <li class="postInfo" style="float:none;width: 100%;">
                        <div class="c-title">课程辅导 <i class="tag">辅导做错的学生</i></div>
                        <div class="text" style="cursor: pointer;">
                            <div class="coursePic" data-bind="click:$root.viewCourse">
                                <i class="playBtn" style="cursor: pointer;"></i>
                            </div>
                            <div class="desc" style="cursor: pointer;line-height: normal;" data-bind="text:pointObj.courseName,click:$root.viewCourse.bind($data,$root)">询问和回答名字</div>
                        </div>
                        <i class="tagIcon"></i>
                    </li>
                    <li class="postInfo lastChild" style="float:none;width: 100%;">
                        <div class="c-title">测试巩固题 <i class="tag">测试学生能否最终学会</i></div>
                        <div class="context">
                            <table class="t-questionBox" style="border-bottom:0;">
                                <tbody data-bind="foreach:ko.utils.range(1,Math.ceil(pointObj.postQuestions().length/2))">
                                <tr data-bind="foreach:{data:pointObj.postQuestions().slice($index() * 2, ($index() + 1) * 2),as:'questionId'}">
                                    <td style="background: rgba(255, 255, 255,.8);">
                                        <div class="t-question" data-bind="attr:{id : questionId + '-p-' + $parentContext.$parentContext.$index() + '-' + $index()}">正在加载...</div>
                                        <div data-bind="text:$root.renderVueQuestion(questionId,('p-' + $parentContext.$parentContext.$index() + '-' + $index()))"></div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <i class="tagIcon"></i>
                    </li>
                </ul>
            </div>
            <!--/ko-->
            <div class="w-clear"></div>
            <!--/ko-->
        </div>
    </div>

    <div class="h-set-homework" style="border:0;display: none;" data-bind="if:$root.focusPackage().id() == 'PK_RECOMMEND' && $root.displayPointQuestions().length == 0,visible: $root.focusPackage().id() == 'PK_RECOMMEND' && $root.displayPointQuestions().length == 0">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>哦~没题目，多选几个课时就有了~</p>
                </div>
            </div>
        </div>
    </div>

</script>

<script type="text/html" id="t:TIME_LIMIT_HELP">
    <div class="rewardRules-dialog" style="margin-top:-20px;">
        <div class="rulesTxt" style="height: 278px;">
            <div class="aside">
                <h3 class="title">1、什么是限时？</h3>
                <p>限时指限定学生口算练习的时长。如限时5分钟，代表学生的口算练习需要在5分钟内完成。</p>
            </div>
            <div class="aside">
                <h3 class="title">2、限时口算有什么特点？</h3>
                <p>特点一：教师检查作业后，系统将在学生端展示前五名。</p>
                <p>特点二：系统将帮教师发送更多的奖励，鼓励学生。</p>
            </div>
            <div class="aside">
                <h3 class="title">3、限时口算有什么奖励？</h3>
                <p>系统会给学生两种奖励：过程性奖励和结果性奖励。过程性奖励将根据学生答对题目的数量进行分发，<span style="font-weight: bold;">答对越多给的越多</span>；结果性奖励会给<span style="font-weight: bold;">前五名</span>的同学。</p>
            </div>
            <div class="aside">
                <h3 class="title">4、奖励次数？</h3>
                <p>只有限时练习才能给学生发送奖励，每周前两次布置口算练习可发送奖励。</p>
            </div>
        </div>
        <div class="btn-box" style="display: none;"><a href="javascript:void(0)" class="w-btn w-btn-well">OK</a></div>
    </div>
</script>

<script type="text/html" id="t:ADD_POINTS_POPUP">
    <div class="addPoint-dialog">
        <div class="pointNum">已选择知识点<!--ko text:$root.pointIds().length--><!--/ko-->个</div>
        <div class="list">
            <ul data-bind="foreach:{data:$root.pointTree,as:'unit'}">
                <li class="show" data-bind="css:{'show':$root.unitIds().indexOf(unit.unitId) != -1},click:$root.unitShowOrHide.bind($data,$root)">
                    <div class="text"><!--ko text:unit.unitName--><!--/ko--><em class="w-icon-arrow  w-icon-arrow-lBot"></em></div>
                    <ul class="subList" data-bind="foreach:{data:unit.knowledgePoints,as:'point'}">
                        <li data-bind="css:{'active': $root.pointIds().indexOf(point.kpId) != -1}">
                            <div class="text" data-bind="attr:{title:point.kpName},click:$root.toggleChecked.bind($data,$root),clickBubble:false"><!--ko text:point.kpName--><!--/ko--><span class="radioIcon"></span></div>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="btn-box" style="display: none;"><a href="javascript:void(0)" class="w-btn w-btn-well">确认</a></div>
    </div>
</script>