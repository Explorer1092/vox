<script id="T:TERM_KEY_POINTS" type="text/html">
    <#--题包-->
    <!--ko if: !$root.loading() && $root.packageList().length > 0-->
    <div class="h-topicPackage sliderHolder" style="margin-bottom: 0" data-bind="attr:{name:'J_'+$root.termType}">
        <!--ko if:$root.packageList().length > 3-->
        <div class="arrow arrow-l" data-bind="attr:{id:'left-'+$root.termType}" style="display: none"><i class="icon"></i></div>
        <div class="arrow arrow-r" data-bind="attr:{id:'right-'+$root.termType}"><i class="icon"></i></div>
        <!--/ko-->
        <div class="topicBox" data-bind="attr:{id:'container-'+$root.termType}" style="padding: 0 18px;height:94px;width:756px;overflow: hidden;">
            <ul data-bind="style:{width: $root.packageList().length*252+'px',left:'0px',marginLeft : '15px',position:'absolute',transition: 'left 1s'}"><!--width为li的个数*252px-->
                <!--ko foreach:{data : $root.packageList(),as:'package'}-->
                <li class="slideItem" data-bind="css:{active: $root.currentPackage() && package.videoId() == $root.currentPackage().videoId()},click:$root.changePackage.bind($data,$element,$root)"><#--题包只要一个字段-->
                    <p data-bind="text:package.videoName()">&nbsp;</p>
                    <span class="state" style="display: none;" data-bind="visible:package.selCount() > 0,text:package.selCount()">0</span>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
    <#--重难点视频解析-->
    <div class="finR-video">
        <div class="frv-left" data-playst="stop" data-bind="attr:{'data-init' : $root.getVideoFlashVars($element)},click:$root.playVideoLog.bind($data,$element)">
            <#--<span class="playBtn"></span>-->
        </div>
        <div class="frv-right">
            <p data-bind="text:$root.currentPackage().videoSummary()">&nbsp;</p>
            <p>解题技巧：</p>
            <p data-bind="foreach:{data:$root.currentPackage().solutionTracks,as:'track'}">
                <!--ko text:$data--><!--/ko--><br>
            </p>
        </div>
    </div>
    <#--题目详情-->
    <div>
        <div class="h-topicPackage-hd">
            <div class="allCheck" data-bind="css:{'checked' : $root.currentPackage().selCount() >= $root.currentPackage().totalCount()},click:$root.checkedAllExam"><!--全选被选中时添加类checked-->
                <p class="check-btn">
                    <span class="w-checkbox"></span>
                    <span class="w-icon-md">全选<!--ko text:$root.currentPackage().totalCount()--><!--/ko-->道题</span>
                </p>
                <span class="txt-left">预计<i><!--ko text:Math.ceil($root.currentPackage().totalSeconds()/60)--><!--/ko-->分钟</i></span>
            </div>
        </div>
        <!--ko foreach:{data:focusExamList(),as:'question'}-->
        <div class="h-set-homework examTopicBox">
            <div class="seth-hd">
                <p class="fl">
                    <span data-bind="text:question.questionType"></span>
                    <span data-bind="text:question.difficultyName"></span>
                    <!--ko if:question.assignTimes && question.assignTimes() > 0-->
                    <span class="noBorder" data-bind="text:'共被使用' + question.assignTimes() + '次'"></span>
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
                        <a href="javascript:void(0)" style="display: none;" class="viewExamAnswer" data-bind="click:$root.viewExamAnswer.bind($data,$root)">查看答案解析</a>
                    </div>
                    <div class="btnGroup">
                        <a href="javascript:void(0)" class="btn" data-bind="ifnot:question.checked(),visible:!question.checked(), click:$root.addOrRemoveExam.bind($data,$root,$element,$index())"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                        <a href="javascript:void(0)" class="btn cancel" data-bind="if:question.checked(),visible:question.checked(),click:$root.addOrRemoveExam.bind($data,$root,$element,$index())"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:$root.termPage,if:$root.termPage != null}">

        </div>
    </div>
    <!--/ko-->

    <!--ko if:!loading() && (!$root.packageList() || $root.packageList().length == 0 || focusExamList().length == 0)-->
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

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:$root.loading(),visible:$root.loading()">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
</script>

<#include "../kopagination.ftl">