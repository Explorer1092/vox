<div class="mhw-confirm" data-bind="visible : $root.showConfirmBox" style="display: none;" >
    <div class="mc-box">
        <div class="title">布置作业</div>
        <ul class="list">
            <li>
                <div class="left fl">布置时间:</div>
                <div class="right txt-overflow">
                    ${.now?string('yyyy年MM月dd日')}(${.now?string("EEEE")})
                </div>
            </li>
            <li>
                <div class="left fl">布置内容:</div>
                <div class="right">
                    <p data-bind="visible : $root.basicAppCardCount().questionCount > 0"><span data-bind="text:$root.getPackageNameByType('BASIC_APP')">基础练习</span>（共<span data-bind="text: $root.basicAppCardCount().questionCount"></span>个练习）</p>
                    <p data-bind="visible : $root.examCartCount() > 0"> <span data-bind="text:$root.getPackageNameByType('EXAM')">同步习题</span>（共<span data-bind="text: $root.examCartCount()"></span>题）</p>
                    <p data-bind="visible : $root.mentalCartCount() > 0"><span data-bind="text:$root.getPackageNameByType('MENTAL')">口算练习</span>（共<span data-bind="text: $root.mentalCartCount()"></span>题）</p>
                    <p data-bind="visible : $root.readingCardCount() > 0"><span data-bind="text:$root.getPackageNameByType('READING')">绘本阅读</span>（共<span data-bind="text: $root.readingCardCount()"></span>本）</p>
                    <p data-bind="visible : $root.wordPracticeCartCount() > 0"><span data-bind="text:$root.getPackageNameByType('WORD_PRACTICE')">生字词练习</span> （共<span data-bind="text: $root.wordPracticeCartCount()"></span>题）</p>
                    <p data-bind="visible : $root.readReciteCartCount() > 0"><span data-bind="text:$root.getPackageNameByType('READ_RECITE')">课文读背题</span>（共<span data-bind="text: $root.readReciteCartCount()"></span>题）</p>
                    <p data-bind="visible : $root.quizQuestionsCount() > 0"><span data-bind="text:$root.getPackageNameByType('UNIT_QUIZ')">历年考题</span>（共<span data-bind="text: $root.quizQuestionsCount()"></span>题）</p>
                    <p data-bind="visible : $root.photoObjectiveCartCount() > 0"><span data-bind="text:$root.getPackageNameByType('PHOTO_OBJECTIVE')">动手做一做</span>（共<span data-bind="text: $root.photoObjectiveCartCount()"></span>题）</p>
                    <p data-bind="visible : $root.voiceObjectiveCartCount() > 0"><span data-bind="text:$root.getPackageNameByType('VOICE_OBJECTIVE')">概念说一说</span>（共<span data-bind="text: $root.voiceObjectiveCartCount()"></span>题）</p>
                    <p data-bind="visible : $root.oralPracticeCartCount() > 0"><span data-bind="text:$root.getPackageNameByType('ORAL_PRACTICE')">口语习题</span>（共<span data-bind="text: $root.oralPracticeCartCount()"></span>题）</p>

                </div>
            </li>
            <li>
                <div class="left fl">预计时间:</div>
                <div class="right">
                    <span data-bind="text: Math.ceil($data.showQuestionsTotalDuration()/60)"></span>分钟
                </div>
                <div class="tips">(学生实际完成用时受设备和网络影响，预计时长仅供参考)</div>
            </li>
            <li>
                <div class="left fl">完成时间:</div>
                <div class="right">
                    <!-- ko foreach: {data : $data.homeworkTimeList(), as : '_setTime'} -->
                        <span class="time" data-bind="text: _setTime.name(), css : {'active' : _setTime.checked()},click: $root.setTimeClick"></span>
                    <!--/ko-->
                    <span style="position: relative; display: inline-block;">
                        <input type="text" placeholder="" disabled="" readonly="" id="endDateTime" class="time" value="">
                        <i style="display: inline-block; height: 2.6rem; width: 9.5rem; position: absolute; left: 0; top:0;"></i>
                    </span>
                    <#--fix 隐藏默认选项-->
                    <div style="position: absolute; top: 0; height: 1px; width: 1px; overflow: hidden;">
                        <input type="hidden" id="doHomeworkDate" disabled="" readonly=""/>
                        <input type="hidden" id="doHomeworkTime" disabled="" readonly=""/>
                    </div>
                    <div id="outlet"></div>
                    <div class="tips">作业提交截止时间为：<br> <span data-bind="text: $data.showHomeworkEndDateTime()"></span><span data-bind="text: $data.showWeek()"></span></div>
                </div>
            </li>
            <li class="rewardBeans">
                <div class="eb-text">
                    <span class="describe">给每个班奖励学豆</span>
                </div>

                <div class="rb-operation">
                    <span class="operation" data-bind="click: $root.minusIntegralClick, css : {'disabled' : $root.homeworkIntegral() == 0}">-</span>
                    <span class="ipt" data-bind="text: $root.homeworkIntegral()">--</span>
                    <span class="operation" data-bind="click: $root.plusIntegralClick,css : {'disabled' : $root.homeworkIntegral() == $root.homeworkIntegralMaxCount()}">+</span><!--disabled为不可点状态-->
                </div>
            </li>
        </ul>
    </div>
    <div class="mhw-btns btns-2">
        <a data-bind="click: $root.gotoHomeworkListBox" class="w-btn w-btn-lightBlue" href="javascript:void(0)">返回</a>
        <a data-bind="click: $root.gotoIntegral" href="javascript:void(0)" class="w-btn">确认</a>
    </div>
</div>
