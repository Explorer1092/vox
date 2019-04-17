<#import "../layout.ftl" as homework>
<@homework.page title="课堂表现" pageJs="smart">
    <@sugar.capsule css=['jbox'] />
    <#include "../userpopup.ftl">

    <div class="main" data-bind="visible: $root.isGraduate()" style="display: none; text-align: center;">
        <div>暂不支持小学毕业账号</div>
    </div>


<div data-bind="template:{name : 'smartListBox', data : $data},visible: !$root.isGraduate()"></div>

<script id='smartListBox' type='text/html'>
    <div class="main">
        <h2 class="title_info_box title_info_green_box">
            <!-- ko if: $data.smartData().length == 0 -->
            老师还没有发奖励，记得告诉老师啊
            <!-- /ko -->

            <!-- ko if: $data.smartData().length != 0 -->
            <!-- ko if: $data.isToday() -->
            今天有新奖励
            <!-- /ko -->

            <!-- ko if: !$data.isToday() -->
            今天还没有被奖励
            <!-- /ko -->
            <!-- /ko -->
        </h2>

        <div class="homework_box">
            <ul class="list" data-bind="foreach: {data : $data.smartData(), as : '_smart'}">

                <li>
                    <a href="javascript:void (0);">
                        <p class="date"><b data-bind="text: _smart.date" style="font-weight: normal;"></b> <span
                                data-bind="text: _smart.time"></span></p>

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

                        <p class="state ">
                                <span class="back">

                                    <!-- ko if: _smart.history.subject == 'ENGLISH' -->
                                        英语课上，
                                    <!-- /ko -->
                                    <!-- ko if: _smart.history.subject == 'MATH' -->
                                        数学课上，
                                    <!-- /ko -->
                                    <!-- ko if: _smart.history.subject == 'CHINESE' -->
                                        语文课上，
                                    <!-- /ko -->

                                    <span data-bind="text: _smart.history.comment"></span>


                                </span>

                            <i class="report_arrow"></i>
                            <span data-bind="visible: $index() == 0" class="circle"></span>
                        </p>
                    </a>

                    <div class="clear"></div>
                </li>
            </ul>
        </div>

    </div>

</script>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'smart',
                op: 'smart_pv_index'
            })
        })
    }
    var nowDate = "${.now?string('yyyy-MM-dd')}";
</script>
</@homework.page>