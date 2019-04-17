<div data-bind="template:'T:Detailsreport'" id="detailsreport"></div>
<script type="text/html" id="T:Detailsreport">

    <div id="hideAction" class="rp-box" style="padding: 300px 0;text-align: center;display: none">
        <p style="padding: 0 50px;" id="hideAction_p"></p>
    </div>
    <div id="showAction" class="rp-box"
         data-bind="foreach:{data : $root.ajaxData,as:'m'}">
        <!--【头部】-->
        <div class="top">
            <div class="stu-name">
                <span data-bind="text:m.levelEvaluate.stuName"></span>
            </div>
            <div class="stu-evl" data-bind="foreach:{data : m.levelEvaluate.evaluateList,as:'elist'}">
                <span data-bind="text:elist"></span>
            </div>
            <div class="level">
                <div class="people"></div>
                <div class="pillars">
                    <div class="son-pillars"
                         data-bind="attr:{'style':'width:'+ ((m.levelEvaluate.level+1)/ m.levelEvaluate.rankingLevels.length*100)+'%'}"
                    ></div>
                </div>
                <ul class="clazz" data-bind="foreach:{data : m.levelEvaluate.rankingLevels,as:'rankingLevels'}">
                    <li data-bind="attr:{'style':'width:'+ (1/ m.levelEvaluate.rankingLevels.length*100-2)+'%'}">
                        <div class="people" data-bind="visible: m.levelEvaluate.level == $index()"></div>
                        <p  data-bind="text:rankingLevels"></p>
                    </li>
                </ul>
            </div>
        </div>
        <!--【进步情况】-->
        <div class="progress" data-bind="if:m.progress != 'null'">
            <div class="title" data-bind="text:m.progress.title"></div>
            <div class="progress-box">
                <div class="explain">
                    <p data-bind="text:m.progress.proContent">
                    </p>
                    <div class="mark">
                        <img src="/public/skin/newexamv3/images/stureport/ico_yinhao_zuo.png"/>
                        <img src="/public/skin/newexamv3/images/stureport/ico_yinhao_you.png"/>
                    </div>
                </div>
                <div class="pillar-box">
                    <ul class="pillar-ul" data-bind="foreach:{data : [1,2,3,4,5,6,7,8,9],as:'pillar'}">
                        <#--#FF8F2A;-->
                        <li data-bind="attr:{'style':pillar ==  m.progress.proIndex.proIndexStu?'background-color:#FF8F2A':''}">
                            <div class="top-num"     data-bind="if:pillar == m.progress.proIndex.proIndexClass">
                                <span data-bind="text: '班级平均:'+pillar"></span>
                                <i class=""></i>
                            </div>
                            <div data-bind="if:pillar == m.progress.proIndex.proIndexClass">
                                <div class="middle-line"></div>
                            </div>
                            <div class="bottom-num"  data-bind="if:pillar ==  m.progress.proIndex.proIndexStu">
                                <span data-bind="text: '我的等级:'+pillar"></span>
                            </div>
                        </li>


                    </ul>
                </div>
            </div>
        </div>
        <!--【具体表现】-->
        <div class="latitude" data-bind="visible:m.latitude != 'null'">
            <div class="title" data-bind="text:m.latitude.title"></div>
            <div class="latitude-box" data-bind="foreach:{data : m.latitude.latitudeList,as:'latitudeList'}">
                <div class="module module1" data-bind="if:latitudeList.moduleType == 'content'">
                    <div class="item-title">
                        <i></i>
                        <div class="item-name" data-bind="text:latitudeList.moduleName"></div>
                        <div class="star-box">你点亮的星星</div>
                        <div class="i"></div>
                    </div>
                    <div data-bind="foreach:{data: latitudeList.moduleContent,as:'moduleContent'}">
                        <div class="item" >
                            <div class="item-name" data-bind="text:moduleContent.name"></div>
                            <ul  data-bind="attr:{'class': $root.boxStar(moduleContent.value)}">
                                <li></li>
                                <li></li>
                                <li></li>
                                <li></li>
                                <li></li>
                            </ul>
                            <div class="i"></div>
                        </div>
                    </div>
                </div>
                <div class="module module2" data-bind="if:latitudeList.moduleType == 'skills'">
                    <div class="item-title">
                        <i></i>
                        <div class="item-name" data-bind="text:latitudeList.moduleName"></div>
                        <div class="star-box">你点亮的星星</div>
                        <div class="i"></div>
                    </div>
                    <div data-bind="foreach:{data: latitudeList.moduleContent,as:'moduleContent'}">
                        <div class="item" >
                            <div class="item-name" data-bind="text:moduleContent.name"></div>
                            <ul  data-bind="attr:{'class': $root.boxStar(moduleContent.value)}">
                                <li></li>
                                <li></li>
                                <li></li>
                                <li></li>
                                <li></li>
                            </ul>
                            <div class="i"></div>
                        </div>
                    </div>
                </div>
                <div class="module module3" data-bind="if:latitudeList.moduleType == 'abilities'">
                    <div class="item-title">
                        <i></i>
                        <div class="item-name" data-bind="text:latitudeList.moduleName"></div>
                        <div class="star-box">你点亮的星星</div>
                        <div class="i"></div>
                    </div>
                    <div data-bind="foreach:{data: latitudeList.moduleContent,as:'moduleContent'}">
                        <div class="item" >
                            <div class="item-name" data-bind="text:moduleContent.name"></div>
                            <ul  data-bind="attr:{'class': $root.boxStar(moduleContent.value)}">
                                <li></li>
                                <li></li>
                                <li></li>
                                <li></li>
                                <li></li>
                            </ul>
                            <div class="i"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    <#--【题目出错情况】-->
        <div class="errors1" data-bind="visible:m.errorsSituation.errorsList != 'null'">
            <div class="title" data-bind="text:m.errorsSituation.title"></div>
            <div class="errors1-box">
                <ul data-bind="foreach:{data : m.errorsSituation.errorsList,as:'errorsList'}">
                <#--//疑问doubt  努力strive  想一想think  粗心careless-->
                    <li>
                        <img src="/public/skin/newexamv3/images/stureport/img_chucuo_4.png" class="img"
                             data-bind="visible: errorsList.category == 'doubt'"/>
                        <img src="/public/skin/newexamv3/images/stureport/img_chucuo_3.png" class="img"
                             data-bind="visible: errorsList.category == 'strive'"/>
                        <img src="/public/skin/newexamv3/images/stureport/img_chucuo_2.png" class="img"
                             data-bind="visible: errorsList.category == 'think'"/>
                        <img src="/public/skin/newexamv3/images/stureport/img_chucuo_1.png" class="img"
                             data-bind="visible: errorsList.category == 'careless'"/>
                        <div class="promptbox" data-bind="text: errorsList.clues"></div>
                        <div class="questionList">
                            <span data-bind="text: errorsList.questionTypes"></span>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    <#--【题目出错情况】-->
        <div class="errors2" data-bind="visible:m.errorsSituation.errorsList == 'null'">
            <div class="title" data-bind="text:m.errorsSituation.title"></div>
            <div class="errors2-box">
                <img src="/public/skin/newexamv3/images/stureport/img_kaixin.png"/>
                <p>本次考试你没有错题耶，再接再厉~</p>
            </div>
        </div>
    <#--【建议】-->
        <div class="advice"  data-bind="text:m.advice"></div>
    </div>
</script>

