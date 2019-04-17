<#import "../layout.ftl" as activity>
<@activity.page title="圣诞节" pageJs="christmas">
    <@sugar.capsule css=['jbox','christmas'] />
    <div class="projectChristmas-bg-1">
        <div class="module-1">老师们变身圣诞老人，来奖励完成<br>作业／测验的孩子！</div>
        <div class="module-2">活动时间：12月21日－12月31日</div>
    </div>
    <div class="projectChristmas-bg-2">
        <div class="module-box">
            <div class="module-inner">
                <div class="module-head">活动具体步骤：</div>
                <div class="module-cont">
                    <ul class="list">
                        <li class="teacher">
                            <div class="hd">
                                <div class="portrait"></div>
                                <div class="box">老师需要做</div>
                            </div>
                            <div class="txt">
                                <div class="box">1.布置作业／测验</div>
                                <div class="box">2.将学豆装入圣诞袜</div>
                            </div>
                        </li>
                        <li class="student">
                            <div class="hd">
                                <div class="portrait"></div>
                                <div class="box">学生需要做</div>
                            </div>
                            <div class="txt">
                                <div class="box">3.完成作业／测验</div>
                                <div class="box">4.领取圣诞袜</div>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="module-head" style="margin:10px 0 0;">给老师学生送礼物：</div>
                <div class="module-cont">
                    <ul class="gift">
                        <li>
                            <div class="box">
                                <div class="ico-flower"></div>
                                <div class="txt">已有<span data-bind="text: sendFlowerParentCount"></span>个家长给老师送花。</div>
                                <a data-bind="click: sendFlowersToTeacherBtn,text: (canSendFlower()) ? '给老师送花' : '已送'" href="javascript:void (0);" class="btn"></a>
                            </div>
                        </li>
                        <li>
                            <div class="box">
                                <div class="ico-beans"></div>
                                <div class="txt">已有<span data-bind="text: sendIntegralParentCount"></span>个家长给班级贡献<span data-bind="text: clazzIntegralCount"></span>个学豆。</div>
                                <a data-bind="click: sendChristmasSocksBtn" href="javascript:void(0);" class="btn">补充圣诞袜</a>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <div class="projectChristmas-bg-3">
        <div class="table-head">
            <div class="box">
                <span style="position: relative;">
                    <select name="" id="" data-bind="options: studentList,optionsText : 'name', value : currentStudent"></select>
                    <b style="font-size: 20px; position: absolute; right: 20px; top: 15px; color: #fff">▼</b>
                </span>
                <span class="num-beans" data-bind="text: studentIntegral"></span>
                <span class="num-gift" data-bind="text: studentSocks"></span>
            </div>
        </div>
        <div class="table-main">
            <div class="inner" data-bind="template: {name : 'rewardListBox'}">
                <#--奖励详情-->
            </div>
        </div>
    </div>
    <script type="text/html" id="rewardListBox">
        <table>
            <thead>
            <tr>
                <td style="width: 32%;">学生</td>
                <td style="width: 32%;">获得圣诞袜</td>
                <td>获得学豆</td>
            </tr>
            </thead>
        </table>
        <div style="overflow: hidden; overflow-y: auto; height: 170px; ">
            <table>
                <tbody style="">
                    <!-- ko if : $root.rewardList().length > 0 -->
                        <!-- ko foreach : {data : $root.rewardList, as : '_de'} -->
                        <tr>
                            <td data-bind="text: _de.studentName" style="width: 32%;"></td>
                            <td data-bind="text: _de.sc" style="width: 32%;"></td>
                            <td data-bind="text: _de.ic"></td>
                        </tr>
                        <!-- /ko-->
                    <!-- /ko-->
                    <!-- ko if : $root.rewardList().length == 0 -->
                        <tr>
                            <td colspan="3">暂无数据</td>
                        </tr>
                    <!-- /ko-->
                </tbody>
            </table>
        </div>
    </script>
</@activity.page>