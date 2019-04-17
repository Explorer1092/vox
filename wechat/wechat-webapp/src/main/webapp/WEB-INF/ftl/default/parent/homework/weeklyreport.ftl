<#import "../layout.ftl" as homework>
<@homework.page title="每周报告" pageJs="weeklyReport">
    <@sugar.capsule css=['jbox'] />
    <#include "../userpopup.ftl">
    <#include "../subject.ftl">
    <div data-bind="visible: $root.isGraduate()" style="display: none; text-align: center;">暂不支持小学毕业账号</div>

    <div data-bind="template:{name : 'weeklyReportBox', data : $data, if: !$root.isGraduate()}"></div>
    <script id='weeklyReportBox' type='text/html'>
        <div class="main" >
            <div data-bind="visible: $data.getHomework().updateTime">
                <div style="text-align: right;">
                    <span class="text_blue" style="font-size:22px;padding: 0 35px;" data-bind="text: ($data.getHomework().updateTime)+'更新'"></span>
                </div>

                <h4 data-bind="visible: $root.homeworkTotalCount == 0 ,text: $data.getHomework().teacher+'老师没留作业，快去提醒老师。'"></h4>


                <!-- ko if:$root.homeworkTotalCount > 0 && $root.curWeekHomeworkCount == 0 -->
                    <!-- 老师本周没布置过作业 -->
                    <h4 data-bind="text: $data.getHomework().teacher+'老师本周没有新作业，快去提醒老师。'"></h4>
                    <div class="homework_history">
                        <ul class="list">
                            <li>
                                <h3>★ 作业完成情况</h3>
                                <div class="content">
                                    <p>老师总共布置了<span class="text_red" data-bind="text: $root.homeworkTotalCount"></span>次作业</p>
                                    <p>
                                        <span data-bind="text: $data.getHomework().first().studentName"></span>完成了
                                        <span class="text_red" data-bind="text: $root.completeHomeworkCount"></span>次
                                    </p>
                                    <!-- if: $data.getHomework().first && $data.getHomework().first.studentId != $root.studentId -->
                                        <p>
                                            班级第一名<span data-bind="text: $data.getHomework().first.studentName"></span>完成了
                                            <span class="text_red" data-bind="text: $data.getHomework().first.completeHomeworkCount"></span>次
                                        </p>
                                    <!-- /ko -->
                                </div>
                            </li>
                        </ul>
                    </div>
                <!-- /ko -->


                <!-- ko if:$root.curWeekHomeworkCount > 0 && $root.curWeekCompleteCount == 0 -->
                    <!--  老师本周布置了作业，学生没做 -->
                    <h2 class="title_info_box" data-bind="text: '老师上周留了'+$root.curWeekHomeworkCount+'次作业，您的孩子未完成'"></h2>
                    <div class="homework_history">
                        <ul class="list">
                            <li>
                                <h3>★ 作业完成情况</h3>
                                <div class="content">
                                    <p>老师总共布置了<span class="text_red" data-bind="text: $root.homeworkTotalCount"></span>次作业</p>
                                    <p>您的孩子完成了<span class="text_red" data-bind="text: $root.completeHomeworkCount"></span>次</p>
                                    <!-- if: $data.getHomework().first && $data.getHomework().first.studentId != $root.studentId -->
                                        <p>
                                            班级第一名<span data-bind="text: $data.getHomework().first.studentName"></span>完成了
                                            <span class="text_red" data-bind="text: $data.getHomework().first.completeHomeworkCount"></span>次
                                        </p>
                                    <!-- /ko -->
                                </div>
                            </li>
                        </ul>
                    </div>
                <!-- /ko -->

                <!-- ko if:$root.curWeekHomeworkCount <= 0 || $root.curWeekCompleteCount != 0 || $root.homeworkTotalCount <= 0  -->

                    <!-- ko if: $data.getHomework().curWeekAvgScore > $data.getHomework().lastWeekAvgScore -->
                        <!-- 学生进步了 -->
                        <h2 class="title_info_box">
                            您的孩子上周英语作业进步了
                            <!-- ko if: (($data.getHomework().first) && ($data.getHomework().first.studentId == $root.studentId)) -->
                            并且是第1名，
                            <!-- /ko -->
                            请继续保持。
                        </h2>

                    <!-- /ko -->

                    <!-- ko if: $data.getHomework().curWeekAvgScore < $data.getHomework().lastWeekAvgScore -->
                        <!-- 学生退步了 -->
                        <h2 class="title_info_box">您的孩子上周没有明显进步</h2>

                    <!-- /ko -->

                    <!-- ko if: $data.getHomework().curWeekAvgScore == $data.getHomework().lastWeekAvgScore -->
                        <!-- 成绩稳定 -->
                        <h2 class="title_info_box">您的孩子近期英语作业稳定，请继续保持。</h2>
                    <!-- /ko -->


                <!-- /ko -->

                <!-- ko if: $data.getHomework().first && $data.getHomework().first.studentId != $root.studentId -->
                    <!--  孩子不是第一名 -->
                    <div class="homework_history">
                        <ul class="list">
                            <li>
                                <h3>★ 词汇量</h3>
                                <div class="content">
                                    <p>
                                        教学要求掌握<span data-bind="text: $data.getHomework().needLearnWordsCount"></span>个，班平均掌握
                                        <span data-bind="text: ('avglearnwords' in $data.getHomework() ? $data.getHomework().avglearnwords : 0 )"></span>个
                                    </p>

                                    <!-- ko if: 'learnWordsCount' in $data.getHomework() -->
                                        <p>
                                            您的孩子掌握
                                            <span class="text_red" data-bind="text: $data.getHomework().learnWordsCount"></span>个
                                        </p>
                                    <!-- /ko -->

                                    <!-- ko if: (!('learnWordsCount' in $data.getHomework()) ? 0 : $data.getHomework().learnWordsCount) <= ($data.getHomework().first && 'learnwords' in $data.getHomework().first ? $data.getHomework().first.learnwords : 0) -->
                                        <p>
                                            班级第一名(<span data-bind="text: $data.getHomework().first.studentName"></span>)掌握
                                            <span class="text_red" data-bind="text: ($data.getHomework().first && 'learnwords' in $data.getHomework().first ? $data.getHomework().first.learnwords : 0)"></span>个
                                        </p>

                                    <!-- /ko -->

                                </div>
                            </li>

                            <!-- ko if: $data.getHomework().clazzlevel > 3 && ('listenrate' in $data.getHomework() || 'avglistenrate' in $data.getHomework()) -->
                                <li>
                                    <h3>★ 听力题</h3>
                                    <div class="content">
                                        <!-- ko if: 'listenrate' in $data.getHomework() -->

                                            <!-- ko if: $data.getHomework().listenrate <= (('listenrate' in $data.getHomework().first) ? $data.getHomework().first.listenrate : 0) -->
                                                <p>
                                                    班级第一名（ <span data-bind="text: $data.getHomework().first.studentName"></span> ）正确率
                                                    <span  class="text_red" data-bind="text : (('listenrate' in $data.getHomework().first) ? $data.getHomework().first.listenrate : 0) +'%'"></span>
                                                </p>
                                            <!-- /ko -->
                                        <!-- /ko -->

                                        <!-- ko if: 'avglistenrate' in $data.getHomework() -->
                                            <p>班平均正确率<span class="text_red" data-bind="text : $data.getHomework().avglistenrate+'%'"></span></p>
                                        <!-- /ko -->

                                        <!-- ko if: 'listenrate' in $data.getHomework() -->
                                            <p>
                                                您的孩子正确率
                                                <span class="text_red" data-bind="text : $data.getHomework().listenrate+'%'"></span>
                                            </p>
                                        <!-- /ko -->
                                    </div>
                                </li>
                            <!-- /ko -->
                        </ul>
                    </div>
                <!-- /ko -->

                <!-- ko if: $data.getHomework().first && ($data.getHomework().first.studentId == $root.studentId) -->
                    <!-- 孩子是第一名 -->
                    <div class="homework_history">
                        <ul class="list">
                            <li>
                                <div class="content">
                                    <!-- ko if: 'learnWordRank' in $data.getHomework() && $data.getHomework().learnWordRank > 0  -->
                                        <p>词汇掌握量在班内排第<span class="text_red" data-bind="text: $data.getHomework().learnWordRank"></span>名</p>

                                    <!-- /ko -->

                                    <!-- ko if: 'listenRateRank' in $data.getHomework() && $data.getHomework().listenRateRank > 0 -->
                                        <p>听力正确率在班内排第<span class="text_red" data-bind="$data.getHomework().listenRateRank"></span>名</p>
                                    <!-- /ko -->
                                </div>
                            </li>
                        </ul>
                    </div>
                <!-- /ko -->
            </div>

            <!-- ko if: $root.focusTab() == 'math'  -->
                <div class="waiting_box"><span class="wb"></span><p>正在开发中,敬请期待...</p></div>
            <!-- /ko -->

            <h2 data-bind="visible: !$data.getHomework().updateTime && $root.focusTab() == 'english'" class="title_info_box title_info_green_box">未查询到作业周报</h2>

            <div data-bind="visible: $data.shareKey() && $data.getHomework().updateTime" class="foot_btn_box">
                <a href="javascript:void ();" data-bind="attr: {href : '/parent/homework/sharereport.vpage?share_key='+$data.shareKey()}" class="btn_mark btn_mark_block" style="color: #F9F9F9">晒宝贝成绩单<i class="icon icon_6"></i></a>
            </div>
        </div>
    </script>
    <div style=" height: 125px;"></div>
    <#include "../menu.ftl">
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'weekly_report',
                op: 'weekly_report_pv'
            })
        })
    }
</script>
</@homework.page>