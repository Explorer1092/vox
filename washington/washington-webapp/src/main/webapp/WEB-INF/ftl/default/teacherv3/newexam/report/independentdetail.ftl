<#import "../../../tts/module.ftl" as temp />
<@temp.page level="专项测试报告">
    <@sugar.capsule js=["ko","jplayer","newexam"] css=["homeworkv3.homework"]/>
<div class="m-main" id="reportList">
    <div class="h-homeworkCorrect">
        <div class="w-base" style="padding-bottom:5px; margin-top:10px">
            <div class="w-base-title">
                <h3 data-bind="text:schoolName"></h3>
                <div class="w-base-ext">
                    <span class="w-bast-ctn" data-bind="text:clazzName"></span>
                </div>
                <div class="w-base-right w-base-switch">
                    <ul>
                        <li class="tab" data-bind="css:{'active' : focusTab() == 'paper'},click:$root.changeTab.bind($data,'paper')">
                            <a  href="javascript:void(0);">
                                <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                查看试卷
                            </a>
                        </li>
                        <li class="tab" data-bind="css:{'active' : focusTab() == 'student'},click:$root.changeTab.bind($data,'student')">
                            <a href="javascript:void(0);">
                                <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                查看学生
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
            <div style="height: 200px; background-color: white; width: 98%;" data-bind="visible:prLoading()">
                <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
            </div>

            <div class="t-del-homework" style="display: none;" data-bind="if:!prLoading(),visible:!prLoading()">
                <h2 data-bind="text:title() + ($root.fullScore && $root.fullScore() > 0 ? '（共' + $root.fullScore() + '分）' : '')"></h2>
                <p><span data-bind="text:clazzName()"></span>  参与人数：<span data-bind="text:joinCount()"></span>  完成人数：<span data-bind="text:submitCount()"></span></p>
            </div>

        <#--试卷-->
            <div style="display: none;" data-bind="if:focusTab() == 'paper',visible:focusTab() == 'paper' && !prLoading()" class="hc-main">
                <!--ko if:paperReports() != null && paperReports().length > 0-->
                <!--ko foreach:{data:paperReports(),as:'part'}-->
                <div class="w-base-title" style="background-color: #e1f0fc;"><h3 data-bind="text:part.partName"></h3></div>
                <!--ko foreach:{data:part.questionInfo(),as:'question'}-->
                <div class="h-set-homework">
                    <div class="seth-hd">
                        <p class="fl">
                            <span data-bind="text:question.contentType()"></span>
                            <span class="border-none" data-bind="text:'本小题：' + question.standardScore() +'分'"></span>
                        </p>
                    </div>
                    <div class="seth-mn iconWrapper line">
                        <div class="box" data-bind="attr:{id : 'newExamImg' + $parentContext.$index() + '-' + $index()}">题目内容</div>
                        <div data-bind="text:$root.loadExamImg($parentContext.$index(),$index(),question.qid())"></div>
                        <div class="icon-error-b icon-b">
                            <div class="inner">
                                <div class="text">平均分</div>
                                <div class="item" data-bind="text:question.avgScore()"></div>
                            </div>
                        </div>
                    </div>
                    <div data-bind='template: { name: $root.displayMode, data:question }'></div>
                </div>
                <!--/ko-->
                <!--/ko-->
                <!--/ko-->
            </div>

            <div style="display: none;" data-bind="if:focusTab() == 'student',visible:focusTab() == 'student' && !prLoading()" class="w-table t-del-homework">
                <table>
                    <thead>
                    <tr>
                        <td style="width: 90px;">姓名</td>
                        <td style="width: 60px;">是否完成</td>
                        <td style="width: 60px;">是否交卷</td>
                        <td style="width: 40px;">成绩</td>
                        <td style="width: 77px;">答题时长</td>
                        <!--ko if:partNames() != null && partNames().length > 0-->
                        <!--ko foreach:partNames-->
                        <td data-bind="text:$data"></td>
                        <!--/ko-->
                        <!--/ko-->
                        <td style="width: 80px;">查看详细</td>
                    </tr>
                    </thead>
                    <tbody>
                    <!--ko if:studentList() != null && studentList().length > 0-->
                    <!--ko foreach:{data:studentList(),as:'student'}-->
                    <tr data-bind="css:{'odd':$index()%2 == 0}">
                        <td data-bind="text:student.userName"></td>
                        <td data-bind="text:student.finish ? '已完成':'未完成'"></td>
                        <td data-bind="text:student.submit ? '已交卷':'未交卷'"></td>
                        <td data-bind="text:student.score"></td>
                        <td data-bind="text:student.durationMilliseconds"></td>
                        <!--ko if:student.partScores != null && student.partScores.length > 0-->
                        <!--ko foreach:student.partScores-->
                        <td data-bind="text:$data"></td>
                        <!--/ko-->
                        <!--/ko-->
                        <td>
                            <!--ko if:student.score >= 0-->
                            <a target="_blank" data-bind="attr:{href:'/teacher/newexam/report/independent/studentanswer.vpage?userId=' + student
                            .userId + '&newexamId=' + $root.newExamId}">查看</a>
                            <!--/ko-->
                        </td>
                    </tr>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko ifnot:studentList() != null && studentList().length > 0-->
                    <tr>
                        <td data-bind="attr:{colspan:$root.partNames().length + 4}">没有学生完成本次测试</td>
                    </tr>
                    <!--/ko-->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="showBigPic" style="display: none;" data-bind="if:users() && users().length > 0 && focusUser() != null,visible:users() && users().length > 0 && focusUser() != null">
    <!--ko if:users() && users().length > 0 && focusUser() != null-->
    <div class="t-viewPicture-box-mask"></div>
    <div class="t-viewPicture-box">
        <div class="flex-viewport" data-bind="css:{'flex-viewport-twoImg' : focusUser().showFiles() && focusUser().showFiles().length == 2,'flex-viewport-threeImg':focusUser().showFiles() && focusUser().showFiles().length == 3}">
            <div class="head">
                <div class="title"><span data-bind="text:'（' + (focusIndex() + 1) +  '/' + users().length + '）'"></span><span data-bind="text:focusUser().userName()">学生名字</span></div>
                <div class="close" data-bind="click:close">×</div>
            </div>
            <div style="position: relative;">
                <ul class="list">
                    <!--ko if:focusUser().showFiles() != null && focusUser().showFiles().length > 0-->
                    <!--ko foreach:{data:focusUser().showFiles(),as:'file'}-->
                    <li>
                        <div class="image">
                            <div class="grouper">
                                <img draggable="false" data-bind="attr:{src:$root.loadImgUrl($data,file,$root.focusUser().showFiles().length)}">
                            </div>
                            <div class="rotation" rotate="0" data-bind="click:$root.rotationImg.bind($data,$index(),$element,$root)"></div>
                        </div>
                    </li>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko ifnot:focusUser().showFiles() != null && focusUser().showFiles().length > 0-->
                    <li>
                        <div class="image">
                            <div class="grouper">
                                <img draggable="false" src="<@app.link href='public/skin/teacherv3/images/homework/upflie-img.png'/>">
                            </div>
                            <div class="rotation" style="display: none;"></div>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
            <ul class="flex-direction-nav">
                <li>
                    <a class="flex-prev" data-bind="css:{'flex-disabled' : focusIndex() <= 0},click:prevOrNext.bind($data,-1)" href="javascript:void(0);"></a>
                </li>
                <li>
                    <a class="flex-next" data-bind="css:{'flex-disabled' : focusIndex() >= (users().length - 1)},click:prevOrNext.bind($data,1)" href="javascript:void(0);"></a>
                </li>
            </ul>
            <div class="J_checkSubjectRegion column"></div>
        </div>
    </div>
    <!--/ko-->
</div>

<div id="jquery_jplayer_1" class="jp-jplayer"></div>
    <@sugar.capsule js=["newexamindependent.detail"]/>
<script id="T:OnlyUserTextAnswer" type="text/html">
    <div class="t-error-info w-table" data-tip="只有答案的">
        <table>
            <thead>
            <tr>
                <td style="width: 190px;">答案</td>
                <td>对应同学</td>
            </tr>
            </thead>
            <tbody>
            <!--ko foreach:{data:question.errorAnswerList(),as:'qanswer'}-->
            <tr data-bind="css:{'odd':$index()%2 == 0}">
                <td data-bind="text:qanswer.answer(),css:{'txt-green':qanswer.answer() == '答案正确'}"></td>
                <td data-bind="text:$root.concatUserNames(ko.mapping.toJS(qanswer.users))"></td>
            </tr>
            <!--/ko-->
            </tbody>
        </table>
    </div>
</script>
<script id="T:UserAnswerWithPic" type="text/html">
    <div class="t-error-info w-table" data-tip="有答案并有图片的题">
        <table>
            <thead>
            <tr>
                <td style="width: 190px;">答案</td>
                <td>对应同学</td>
            </tr>
            </thead>
            <tbody>
            <!--ko foreach:{data:question.errorAnswerList(),as:'qanswer'}-->
            <tr data-bind="css:{'odd':$index()%2 == 0,'txt-green':qanswer.answer() == '答案正确'}">
                <td data-bind="text:qanswer.answer()"></td>
                <td>
                    <div class="questionBox">
                        <ul>
                            <!--ko foreach:{data:qanswer.users,as:'user'}-->
                            <li data-bind="click:$root.previewImg.bind($data,$index(),question.qid(),$parent,$root)">
                                <p class="name" data-bind="text:user.userName()"></p>
                                <!--ko if:user.showFiles() != null && user.showFiles().length > 0-->
                                <img data-bind="attr:{src:user.showFiles()[0]}" />
                                <!--/ko-->
                                <!--ko ifnot:user.showFiles() != null && user.showFiles().length > 0-->
                                <img src="<@app.link href='public/skin/teacherv3/images/homework/upflie-img.png'/>" />
                                <!--/ko-->
                                <span class="icon "></span>
                            </li>
                            <!--/ko-->
                        </ul>
                    </div>
                </td>
            </tr>
            <!--/ko-->
            </tbody>
        </table>
    </div>
</script>
<script id="T:UserSubjectiveAnswerWithPic" type="text/html">
    <div class="t-error-info w-table" data-tip="需判分答案为图片的题">
        <table>
            <tbody>
            <!--ko foreach:{data:question.errorAnswerList(),as:'qanswer'}-->
            <tr class="odd">
                <td>
                    <div class="questionBox">
                        <ul>
                            <!--ko foreach:{data:qanswer.users(),as:'user'}-->
                            <li data-bind="click:$root.previewImg.bind($data,$index(),question.qid(),$parent)">
                                <p class="name" data-bind="text:user.userName()"></p>
                                <div class="show-jobGrade">
                                    <!--ko if:user.showFiles() != null && user.showFiles().length > 0-->
                                    <img data-bind="attr:{src:user.showFiles()[0]}">
                                    <!--/ko-->
                                    <!--ko ifnot:user.showFiles() != null && user.showFiles().length > 0-->
                                    <img src="<@app.link href='public/skin/teacherv3/images/homework/upflie-img.png'/>">
                                    <!--/ko-->
                                    <p class="enterGrade" style="display: none;">得分：<input type="text" class="ipt" data-bind="attr:{'data-score':user.score()},textInput:user.score()"></p>
                                    <p class="enterGrade" style="color: #16d381; display: none;">修改成功</p>
                                </div>
                            </li>
                            <!--/ko-->
                        </ul>
                    </div>
                </td>
            </tr>
            <!--/ko-->
            </tbody>
        </table>
    </div>
</script>
<script id="T:OldUserSubjectiveAnswerWithAudio" type="text/html">
    <!--ko foreach:{data:question.errorAnswerList(),as:'qanswer'}-->
    <div class="h-voiceBox">
        <!--ko foreach:{data:qanswer.users(),as:'user'}-->
        <div class="h-voice-list">
            <div class="left voicePlayer" data-bind="singleAudioHover:'play',click:$root.playAudio.bind($data,$element,question.qid(),$root)">
                <i class="icon"></i>
            </div>
            <div class="right">
                <p class="name" data-bind="text:user.userName()">黑三剁</p>
                <!--ko if:$root.examNeedCorrect() && $root.allowCorrect()-->
                <p class="enterGrade">得分：
                    <input type="text" class="ipt" data-bind="attr:{'data-score':user.score()},value:user.score(),event:{blur:$root.correctEvent.bind($data,$element,question.qid(),question.standardScore(),$parents[1],$root,0)}">
                </p>
                <!--/ko-->
                <!--ko ifnot:$root.examNeedCorrect() && $root.allowCorrect()-->
                <p class="enterGrade" data-bind="text:'得分：' + user.score()"></p>
                <!--/ko-->
                <p class="resultInfo"></p>
            </div>
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
</script>
<script id="T:NewSubjectiveUserAnswerWithAudio" type="text/html">
    <!--ko foreach:{data:question.newOralAnswerList(),as:'subQanswer'}-->
    <div class="speakWorks" data-tip="新口语有多个小题的答案">
        <div class="speak-header" style="border-top:0" data-bind="style:{borderTop : $index() == 0 ? '0' : '1px solid #dfdfdf'},text:question.newOralAnswerList().length > 1 ? '学生答案（第' + ($index() + 1) + '题)' : '学生答案'">学生答案</div>
        <div class="h-voiceBox" data-bind="foreach:{data:subQanswer,as: 'user'}">
            <div class="h-voice-list">
                <div class="left voicePlayer" data-bind="singleAudioHover:'play',click:$root.playAudio.bind($data,$element,question.qid(),$root)">
                    <i class="icon"></i>
                </div>
                <div class="right">
                    <p class="name" data-bind="text:user.userName()">黑三剁</p>
                    <!--ko if:$root.examNeedCorrect() && $root.allowCorrect()-->
                    <p class="enterGrade">得分：
                        <input type="text" class="ipt" data-bind="attr:{'data-score':user.score()},value:user.score(),event:{blur:$root.correctEvent.bind($data,$element,question.qid(),question.subStandardScore(),$parents[2],$root,$parentContext.$index())}">
                    </p>
                    <!--/ko-->
                    <!--ko ifnot:$root.examNeedCorrect() && $root.allowCorrect()-->
                    <p class="enterGrade" data-bind="text:'得分：' + user.score()"></p>
                    <!--/ko-->
                    <p class="resultInfo"></p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
</script>
<script id="T:NotMatchTemplate" type="text/html">
    <div class="speakWorks" data-tip="未匹配到的模板">
        <div class="speak-header" style="border-top:0">学生答案</div>
        <div class="h-voiceBox">
            <p style="text-align: center;padding: 20px 0;">没有符合的模板展示</p>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var newExamId = $17.getQuery("newExamId")
                ,initExamSuccess = false;
        if(!newExamId){
            $17.alert("地址参数信息不全",function(){
                window.location.href = "/teacher/newexam/independent/report.vpage";
            });
        }

        ko.bindingHandlers.singleAudioHover = {
            init: function(element, valueAccessor){
                $(element).hover(
                        function(){
                            $(element).addClass(ko.unwrap(valueAccessor()));
                        },
                        function(){
                            $(element).removeClass(ko.unwrap(valueAccessor()));
                        }
                );
            }
        };

        try{
            vox.exam.initialize({
                renderType:'student_preview',
                domain    : '${requestContext.webAppBaseUrl}/',
                imgDomain : '${imgDomain!}',
                env       : <@ftlmacro.getCurrentProductDevelopment />,
                clientType: "pc",
                clientName: "pc",
                callback  : function(obj){
                    obj.success && (initExamSuccess = true);
                }
            });
        }catch(exception){
            $17.voxLog({
                module: 'tongkao.newexam',
                op: 'examinationJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            });
        }

        var viewPic = $17.newexam.getPreviewPic()
                ,viewModule = $17.newexam.getNewExamReport({
            newExamId       : newExamId,
            initExamSuccess : initExamSuccess,
            viewPic         : viewPic,
            subject         : "${subject!}"
        }).init();
        ko.applyBindings(viewPic,document.getElementById("showBigPic"));
        ko.applyBindings(viewModule,document.getElementById("reportList"));

        <#if ProductDevelopment.isDevEnv()>
            setInterval(function(){
                $("div.jqibox").hide();
            },500);
        </#if>
    });
</script>
</@temp.page>