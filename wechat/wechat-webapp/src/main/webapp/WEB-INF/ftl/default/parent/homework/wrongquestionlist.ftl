<#import "../layout.ftl" as homeworkIndex>
<@homeworkIndex.page title="错题本" pageJs="wrongQuestionList">
    <@sugar.capsule css=['jbox','wronglist'] />
    <#include "../userpopup.ftl">
    <#include "../subject.ftl">

    <div data-bind="visible: $root.isGraduate()" style="display: none; text-align: center;">暂不支持小学毕业账号</div>

    <div data-bind="template:{name : 'wrongQuestionListBox', data : $data, if: !$root.isGraduate()}"></div>
    <script id='wrongQuestionListBox' type='text/html'>
        <h2 data-bind="visible: !$root.hasEnglishTeacher() && $root.focusTab() == 'english' && $root.hasMathTeacher(), text:'还没有英语老师，请向老师申请加入'" class="title_info_box title_info_green_box"></h2>
        <h2 data-bind="visible: !$root.hasMathTeacher() && $root.focusTab() == 'math' && $root.hasEnglishTeacher(), text: '还没有数学老师，请向老师申请加入'" class="title_info_box title_info_green_box"></h2>
        <h2 data-bind="visible: !$root.hasEnglishTeacher() && !$root.hasMathTeacher(), text: '还没有加入班级，请向老师申请加入'" class="title_info_box title_info_green_box"></h2>

        <!-- ko if: ($data.getHomework() && $data.getHomework().length > 0 ) -->
        <div data-bind="visible: $root.available() && $root.focusTab() == 'english'"
             style="position: absolute; right: 10px; top: 13px;">
                <a data-bind="click: $root.doAgainBtn.bind($data,'${(isVip?string)!false}');" href="javascript:void (0);" style=" color: white;">错题重做</a>
            </div>

            <div class="wrong-topic">
                <ul class="wt-list" data-bind="foreach : {data : $data.getHomework(), as : 'hks'}">
                    <li>
                        <div class="list-inner">
                            <h3><i></i><span data-bind="text: hks.date"></span></h3>
                            <ul class="list-info">

                                <!--ko foreach:{data : hks.homeworkMapList,as:'hlist'}-->
                                <!-- ko if: hlist && hlist.ids.length > 0 -->
                                <form onclick="submit()" action="/parent/homework/errordetail.vpage" method="post" id="">
                                    <li>
                                        <span class="type">作业错题</span>
                                        <span class="num">共 <span data-bind="text: hlist.ids.length"></span> 题</span>
                                        <i class="arrow-icon"></i>
                                    </li>
                                    <input type="hidden" name="wrongList" value="" data-bind="attr : {value : hlist.ids.toString()}"/>
                                    <input type="hidden" name="sid" data-bind="attr: {value : $root.currentStudentId()}"/>
                                    <input type="hidden" name="ht" data-bind="attr: {value : hlist.homeworkType}"/>
                                    <input type="hidden" name="hid" data-bind="attr: {value : hlist.homeworkId}"/>
                                </form>
                                <!-- /ko -->
                                <!-- /ko -->


                                <!--ko foreach:{data : hks.quizMapLis,as:' qlist'}-->
                                <!-- ko if: qlist && qlist.ids.length > 0 -->
                                    <form onclick="submit()" action="/parent/homework/errordetail.vpage" method="post" id="">
                                        <li>
                                            <span class="type">测验错题</span>
                                            <span class="num">共<span data-bind="text: qlist.ids.length"></span>题</span>
                                            <i class="arrow-icon"></i>
                                        </li>
                                        <input type="hidden" name="wrongList" value="" data-bind="attr : {value : qlist.ids.toString()}"/>
                                        <input type="hidden" name="sid" data-bind="attr: {value : $root.currentStudentId()}"/>
                                        <input type="hidden" name="ht" data-bind="attr: {value : qlist.homeworkType}"/>
                                        <input type="hidden" name="hid" data-bind="attr: {value : qlist.homeworkId}"/>
                                    </form>
                                <!-- /ko -->
                                <!-- /ko -->
                            </ul>
                        </div>
                    </li>
                </ul>
            </div>
        <!-- /ko -->

        <!-- ko if: $data.getHomework() && $data.getHomework().length == 0 &&   !$root.isGraduate() -->
            <div class="null-content">暂无错题记录</div>
        <!-- /ko -->
    </script>
    <div style=" height: 125px;"></div>
    <#include "../menu.ftl">
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'error_point',
                op: 'error_point_pv'
            })
        })
    }
</script>
</@homeworkIndex.page>