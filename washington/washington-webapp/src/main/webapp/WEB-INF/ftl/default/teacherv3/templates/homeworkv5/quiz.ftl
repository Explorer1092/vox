<#--单元测验-->
<script id="t:UNIT_QUIZ" type="text/html">
<!--ko if:content().length > 0-->
<div class="h-topicPackage">
    <div class="sliderHolder" name="J_quiz">
        <div id="container-quiz" class="topicBox">
            <ul><!--width为li的个数*206px-->
            <!--ko foreach:content-->
                <li class="slideItem" data-bind="attr:{index:$index(),paperid:id(),title:title()}">
                    <p data-bind="text:title()"></p>
                    <span class="state" style="display: none;">用过</span>
                    <span class="triggle-icon"></span>
                </li>
            <!--/ko-->
            </ul>
        </div>
    </div>
    <div class="line"></div>
</div>
<!--/ko-->
<!--ko if:packageInfo() != null -->
<div id="packageInfo" class="h-topicPackage-hd">
    <div class="title" data-bind="text:packageInfo().title(),title:packageInfo().title()"></div>
    <div class="allCheck" data-bind="css:{checked:packageInfo().isSelAll()},click: $root.selectAll.bind($data,$element)">
        <p class="check-btn">
            <span class="w-checkbox"></span>
            <span class="w-icon-md" data-bind="text:'全选'+packageInfo().totalCount()+'道题'"></span>
        </p>
        <span class="txt-left">预计<i data-bind="text:packageInfo().totalTime()"></i>分钟</span>
    </div>
</div>
<!--/ko-->

<div id="subjectRegion">
    <!--ko foreach:{data : currentPageQuestions,as : 'question'}-->
    <div class="h-set-homework examTopicBox" data-bind="css:{current:question.isSelected()},event:{mouseover:$root.showFeedback.bind($data,$element),mouseout:$root.hideFeedback.bind($data,$element)}">
        <div class="seth-hd">
            <p class="fl">
                <span data-bind="text:question.questionType()"></span>
                <span data-bind="text:question.difficultyName()"></span>
                <span class="noBorder" data-bind="text:question.assignTimes && question.assignTimes() > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes() == 0)?'被使用'+question.assignTimes()+'次':''"></span>
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
                    <ko-venus-question params="questions:$root.getQuestion(question.id()),contentId:'subjective_'+ question.id(),formulaContainer:'tabContent'"></ko-venus-question>
                </div>
                <div class="J_viewAnswer linkGroup" style="display:none;">
                    <a href="javascript:void(0)" class="viewExamAnswer" data-bind="click:$root.viewExamAnswer.bind($data,$root,$index())">查看答案解析</a>
                    <a href="javascript:void(0)" class="feedback" data-bind="click:$root.feedback.bind($data,$root)">反馈</a>
                </div>
                <div class="btnGroup">
                    <!--ko ifnot:question.isSelected()-->
                    <a href="javascript:void(0)" class="btn" data-bind="click: $parent.addOrCancelQuiz.bind($data,$element,$parent) "><i class="h-set-icon h-set-icon-add"></i>选入</a>
                    <!--/ko-->
                    <!--ko if:question.isSelected()-->
                    <a href="javascript:void(0)" class="removeQuizQuestions btn cancel" data-bind="click: $parent.addOrCancelQuiz.bind($data,$element,$parent)"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
</div>

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

<!--ko if:content().length == 0-->
    <div class="J_dataLoading" style="text-align: center;height: 30px;line-height: 30px;font-size: 18px;margin-top: 10px;">数据加载中...</div>
<!--/ko-->
</script>






