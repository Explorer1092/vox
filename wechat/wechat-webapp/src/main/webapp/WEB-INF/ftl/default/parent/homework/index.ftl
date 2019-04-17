<#import "../layout.ftl" as homeworkIndex>
<@homeworkIndex.page title="最新作业" pageJs="homework">
    <@sugar.capsule css=['jbox'] />
    <#include "../userpopup.ftl">
    <#include "../subject.ftl">
    <div data-bind="visible: $root.isGraduate()" style="display: none; text-align: center;">暂不支持小学毕业账号</div>

    <div data-bind="visible: !$root.isGraduate(), template:{name : 'homeworkListBox', data : $data}"></div>

    <script id='homeworkListBox' type='text/html'>
        <div class="content" id="eng_panel">
            <h2 data-bind="visible: !$root.hasEnglishTeacher() && $root.focusTab() == 'english' && $root.hasMathTeacher(), text:'还没有英语老师，请向老师申请加入'" class="title_info_box title_info_green_box"></h2>
            <h2 data-bind="visible: !$root.hasMathTeacher() && $root.focusTab() == 'math' && $root.hasEnglishTeacher(), text: '还没有数学老师，请向老师申请加入'" class="title_info_box title_info_green_box"></h2>
            <h2 data-bind="visible: !$root.hasEnglishTeacher() && !$root.hasMathTeacher(), text: '还没有加入班级，请向老师申请加入'" class="title_info_box title_info_green_box"></h2>

            <h2 data-bind="visible: ($data.getHomework().homeworks)" class="title_info_box title_info_green_box">
                <span class="row_right mr_30">未完成<strong class="text_red" data-bind="text: $data.getHomework().unfinishedCount || 0"></strong>次</span>
                <!-- ko if: $data.getHomework().todayHomeworkCount > 0 -->
                    <span data-bind="text: '今日有'+$data.getHomework().todayHomeworkCount+'次新作业' "></span>
                <!-- /ko -->
                <!-- ko ifnot: $data.getHomework().todayHomeworkCount > 0 -->
                <span data-bind="text: '今日没有新作业' "></span>
                <!-- /ko -->
            </h2>

            <div class="homework_box">
                <ul class="list" data-bind="foreach: {data : $data.getHomework().homeworks, as : 'hks'}">
                    <li>
                        <p class="date"><b data-bind="text: hks.createDate" style="font-weight: normal;"></b><span data-bind="text: hks.createTime"></span></p>
                        <div class="line">

                            <!-- ko if: $index() == 0 -->
                                <i class="icon icon_new">new</i>
                            <!-- /ko -->

                            <!-- ko if: $index() == 1 -->
                            <span class="col-line col-line-gray"></span>
                            <i class="icon icon_dine"></i>
                            <!-- /ko -->

                            <!-- ko if: $index() > 1 -->
                            <span class="col-line"></span>
                            <i class="icon icon_dine"></i>
                            <!-- /ko -->
                        </div>
                        <div class="state">
                            <span class="subject-icon" data-bind="visible: hks.subjective" title="主观作业"></span>
                            <#-- 根据作业类型分类 -->

                            <!-- ko if: !hks.workbook && !hks.subjective -->
                                <!-- ko if: hks.homework.finished && hks.homework.checked -->
                                    <a href="javascript:void (0);" data-bind="attr:{href : '/parent/homework/detail.vpage?sid='+hks.homework.studentId+'&hid='+hks.homework.homeworkLocation.homeworkId+'&ht='+hks.homework.homeworkLocation.homeworkType+'&wc='+hks.homework.wrongCount+'&prize='+hks.homework.prize}">
                                        <span class="back">
                                            <!-- ko if: !hks.homework.wrongCount -->
                                                <!-- ko if: hks.quiz -->
                                                    测验
                                                <!-- /ko -->
                                                已完成
                                                <span data-bind="text: (hks.homework.averageScore > 0)?'hks.homework.averageScore':''"></span><br />
                                                <span class="text_blue">查看详情<i class="icon icon_10"></i></span><br />
                                            <!-- /ko -->

                                            <!-- ko if: hks.homework.wrongCount -->
                                                <span style="color: #666;">出现<span class="text_red" data-bind="text: hks.homework.wrongCount"></span>道错题 <i class="send-btn"></i></span>
                                                <span class="r-btn-blue" style="color: #FFFFFF;border-radius:5px; padding: 0 20px;">查 看</span><br />
                                            <!-- /ko -->

                                            <!-- ko if: hks.homework.prize > 0 -->
                                                <span style="color: #666666;">获得了老师设置的<span data-bind="text: hks.homework.prize"></span>学豆</span>
                                            <!-- /ko -->
                                        </span>

                                    </a>

                                <!-- /ko -->
                                <!-- ko if: hks.homework.finished && !hks.homework.checked -->
                                    <a data-bind="click: $root.homeworkUnCheck" href="javascript:void (0);">
                                        <span class="back">
                                            <!-- ko if: !hks.homework.wrongCount -->
                                                <!-- ko if: hks.quiz -->
                                                    测验
                                                <!-- /ko -->
                                                已完成
                                                <span data-bind="text: (hks.homework.averageScore > 0)?'hks.homework.averageScore':''"></span><br />
                                                <span class="text_blue">查看详情<i class="icon icon_10"></i></span><br />
                                            <!-- /ko -->

                                            <!-- ko if: hks.homework.wrongCount -->
                                                <span style="color: #666;">出现<span class="text_red" data-bind="text: hks.homework.wrongCount"></span>道错题 <i class="send-btn"></i></span>
                                                <span class="r-btn-blue" style="color: #FFFFFF;border-radius:5px; padding: 0 20px;">查 看</span><br />
                                            <!-- /ko -->

                                            <!-- ko if: hks.homework.prize > 0 -->
                                                <span style="color: #666666;">获得了老师设置的<span data-bind="text: hks.homework.prize"></span>学豆</span>
                                            <!-- /ko -->
                                        </span>
                                    </a>

                                <!-- /ko -->
                                <!-- ko if: !hks.homework.finished -->
                                    <a data-bind="click: $root.showFinishStudentCount" href="javascript:void (0);">
                                        <span class="back text_red">
                                            <!-- ko if: hks.isFuture -->
                                                请--做作业
                                            <!-- /ko -->

                                            <!-- ko ifnot: hks.isFuture -->
                                                <!-- ko if: hks.quiz -->
                                                    测验
                                                <!-- /ko -->
                                                未完成
                                                <!-- ko if: hks.quiz -->
                                                    （无法补做）
                                                <!-- /ko -->

                                                <!-- ko if: hks.homework.prizeExist -->
                                                    <div style="color: #666;">
                                                        ●本次<!-- ko if: hks.quiz -->测验<!-- /ko --><!-- ko ifnot: hks.quiz -->作业<!-- /ko -->老师设置了学豆奖励
                                                    </div>
                                                <!-- /ko -->

                                                <!-- ko if: !hks.quiz && $index() == 0 && hks.homework.certificated -->
                                                    <div style="color: #666;">●孩子完成新作业后即可给老师送花</div>
                                                <!-- /ko -->
                                            <!-- /ko -->
                                        </span>
                                    </a>

                                <!-- /ko -->
                            <!-- /ko -->

                            <#--教辅作业-->
                            <!-- ko if: hks.workbook -->
                                <!-- ko if: hks.homework.finished && hks.homework.checked -->
                                    <a href="javascript:void (0);">
                                        <span class="back">
                                            教辅作业已完成
                                        </span>
                                    </a>
                                <!-- /ko -->
                                <!-- ko if: hks.homework.finished && !hks.homework.checked -->
                                    <a data-bind="click: $root.homeworkUnCheck" href="javascript:void (0);">
                                        <span class="back">
                                            教辅作业已完成
                                        </span>
                                    </a>
                                <!-- /ko -->

                                <!-- ko ifnot: hks.homework.finished -->
                                    <a data-bind="click: $root.showFinishStudentCount" href="javascript:void (0);">
                                       <span class="back text_red">
                                            <!-- ko if: hks.isFuture -->
                                                请--做作业
                                            <!-- /ko -->

                                            <!-- ko ifnot: hks.isFuture -->
                                                教辅作业未完成 （无法补做）
                                            <!-- /ko -->
                                        </span>
                                    </a>
                                <!-- /ko -->

                            <!-- /ko -->

                            <#--主观作业-->
                            <!-- ko if: hks.subjective -->
                                <!-- ko if: hks.homework.finished && hks.homework.checked -->
                                    <a href="javascript:void (0);">
                                        <span class="back">
                                            主观作业已完成
                                        </span>
                                    </a>
                                <!-- /ko -->
                                <!-- ko if: hks.homework.finished && !hks.homework.checked -->
                                    <a data-bind="click: $root.homeworkUnCheck" href="javascript:void (0);">
                                        <span class="back">
                                            主观作业已完成
                                        </span>
                                    </a>
                                <!-- /ko -->

                                <!-- ko ifnot: hks.homework.finished -->
                                    <a data-bind="click: $root.showFinishStudentCount.bind($data)" href="javascript:void (0);">
                                        <span class="back text_red">
                                            <!-- ko if: hks.isFuture -->
                                                请--做作业
                                            <!-- /ko -->

                                            <!-- ko ifnot: hks.isFuture -->
                                                未完成
                                                <!-- ko if: $index() == 0 && hks.homework.certificated -->
                                                    <div style="color: #666;">孩子完成新作业后即可给老师送花</div>
                                                <!-- /ko -->
                                            <!-- /ko -->
                                        </span>
                                    </a>
                                <!-- /ko -->
                            <!-- /ko -->

                            <span class="circle" data-bind="visible: $index() == 0"></span>
                            <!-- ko if: !hks.workbook && hks.homework.certificated -->
                                <div class="f-send-flower-box">
                                    <span data-bind="text: '已有'+hks.homework.flowerCount+'位家长送花'"></span>
                                    <!--ko if:!hks.homework.sentFlag && $root.focusTab() == 'english'-->
                                        <!--ko if: hks.state == 'giveFlower'-->
                                            <i data-bind="click: $root.sendFlowerBtn.bind($data,$data,false)" class="send-btn giveFlowerBut"></i>
                                        <!--/ko-->
                                        <!--ko if: hks.state == 'notgiveFlower'-->
                                         <i data-bind="click: $root.sendFlowerBtn.bind($data,true)" class="send-btn giveFlowerBut notGiveFlower"></i>
                                        <!--/ko-->
                                    <!--/ko-->
                                    <!--ko if:!hks.homework.sentFlag && $root.focusTab() == 'math'-->
                                    <!--ko if: hks.state == 'giveFlower'-->
                                    <i data-bind="click: $root.sendFlowerBtn.bind($data,$data,false)" class="send-btn giveFlowerBut"></i>
                                    <!--/ko-->
                                    <!--ko if: hks.state == 'notgiveFlower'-->
                                    <i data-bind="click: $root.sendFlowerBtn.bind($data,true)" class="send-btn giveFlowerBut notGiveFlower"></i>
                                    <!--/ko-->
                                    <!--/ko-->

                                    <!-- ko if: hks.state == 'finish'  -->
                                        <i class="send-finish"></i>
                                    <!-- /ko -->
                                </div>
                            <!-- /ko -->
                        </div>
                        <div class="clear"></div>
                    </li>
                </ul>
            </div>
        </div>
    </script>
    <div style=" height: 125px;"></div>
    <#include "../menu.ftl">
</@homeworkIndex.page>
