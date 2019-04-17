<#import '../../../layout/layout.ftl' as temp>
<@temp.page pageName='homeworkreport'>
    <#if currentStudentWebGrayFunction.isAvailable("PCHomework", "UseVenus")>
        <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre"] />
    </#if>
    <@sugar.capsule js=["ko",'homework2nd',"jplayer","util.hardcodeurl","studentreport.readingdetail"] css=["homeworkhistory.report"] />
<div class="t-center-container" id="clazzreadingdetail">
    <div class="breadcrumb" style="padding: 15px 0;">
        <span><a class="w-blue" href="/student/index.vpage">首页</a> &gt;</span>
        <span><a class="w-blue" data-bind="attr:{href:'/student/learning/history/newhomework/homeworkreport.vpage?homeworkId=' + homeworkId + '&subject=ENGLISH'}">作业详情</a> &gt;</span>
        <span>答题详情</span>
    </div>
    <div class="h-historyBox">
        <div class="h-title">
            <span class="left-text">作业练习——答题详情</span>
        </div>
        <div class="pb-details">
            <div class="pb-details-section" data-bind="if:$root.oralQuestions && $root.oralQuestions().length > 0">
                <div class="title">跟读练习</div>
                <div class="pb-content-table">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td class="name">内容</td>
                            <td class="time">播放</td>
                        </tr>
                        </thead>
                        <tbody>
                        <!--ko foreach:{data:oralQuestions,as:'question'}-->
                        <tr class="odd" data-bind="css:{'odd': $index()%2 == 0}">
                            <td class="name"><a href="javascript:void(0)" data-bind="text:question.text">&nbsp;</a></td>
                            <td class="time" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                                <i class="play-icon voicePlayer" data-bind="css:{'play-disabled' : !$root.haveAudio(question.audio)},attr:{title : $root.haveAudio(question.audio) ? '' : '听读模式'}"></i>
                            </td>
                        </tr>
                        <!--/ko-->
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="pb-details-section" id="pb-details-201811081534" data-bind="if:$root.getExercisesQuestions().length > 0,visible:$root.getExercisesQuestions().length > 0">
                <div class="title">阅读理解</div>
                <!--ko foreach:{data:$root.getExercisesQuestions(),as:'qs'}-->
                <div class="h-set-homework">
                    <div class="seth-hd">
                        <p class="fl">
                            <span data-bind="text:qs.questionType">&nbsp;</span>
                            <!--ko if:$root.type == 'READING'-->
                            <span class="border-none" data-bind="text:qs.difficultyName">&nbsp;</span>
                            <!--/ko-->
                        </p>
                    </div>
                    <div class="seth-mn">
                        <!--ko if:$root.useVenus-->
                        <div class="box">
                            <ko-venus-question params="questions:$root.getQuestion(qs.questionId),contentId:'readingQuestionImg_' + $index(),formulaContainer:'pb-details-201811081534'"></ko-venus-question>
                        </div>
                        <!--/ko-->
                        <!--ko ifnot:$root.useVenus-->
                            <div class="box" data-bind="attr:{id:'readingQuestionImg' + $index()}"></div>
                            <div data-bind="text:$root.loadExamImg('readingQuestionImg' + $index(),'rdExamImg',qs.questionId,$index())"></div>
                        <!--/ko-->
                    </div>
                    <div class="h-answerD-list" style="margin-bottom: 0px;">
                        <div class="ft">
                            <ul class="ul-list-2">
                                <li>标准答案：<!--ko if:qs.standardAnswers--><!--ko text:qs.standardAnswers--><!--/ko--><!--/ko--></li>
                                <li class="lastLi">我的答案：<!--ko if:qs.userAnswers--><!--ko text:qs.userAnswers--><!--/ko--><!--/ko--></li>
                            </ul>
                        </div>
                    </div>
                </div>
                <!--/ko-->
            </div>
            <div class="pb-details-section" data-bind="if:$root.dubbingId && $root.dubbingId(),visible:$root.dubbingId && $root.dubbingId()">
                <div class="title">绘本配音</div>
                <div class="pb-content-table new-table">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td class="name">绘本作品</td>
                            <td class="time">得分</td>
                            <td class="time">作品</td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="odd">
                            <td class="name" data-bind="text:$root.readingName()"></td>
                            <td class="time" data-bind="text:$root.dubbingScoreLevel"></td>
                            <td class="time" data-bind="click:$root.playDubbingVideo"><i class="h-playIcon h-playVideo"></i></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<script type="text/javascript">
    var constantObj = {
        detailUrl        : "${detailUrl!}",
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />,
        useVenus         : ${((currentStudentWebGrayFunction.isAvailable("PCHomework", "UseVenus"))!false)?string}
    };
    $(function(){
        //当前页面需要应试信息，只初始化一次
        //初始化加载应试
        try{
            vox.exam.create(function(data){
                if(!data.success){
                    $17.voxLog({
                        module: 'vox_exam_create',
                        op:'create_error'
                    });
                    $17.tongji('voxExamCreate','create_error',location.pathname);
                }
            },false,{
                imgDomain:constantObj.imgDomain,
                domain : constantObj.domain,
                env : constantObj.env
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox_exam_create',
                op: 'examCoreJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            });
        }
        var singleReadDetail = new $17.clazzReport.getSingleReadDetail({
            homeworkId : $17.getQuery("homeworkId"),
            readingId  : $17.getQuery("readingId"),
            detailUrl  : constantObj.detailUrl,
            type       : $17.getQuery("type"),
            useVenus   : constantObj.useVenus
        });
        singleReadDetail.run();
        ko.applyBindings(singleReadDetail,document.getElementById("clazzreadingdetail"));
    });
    (function(){
        var SingleReadDetail = function(obj){
            var self = this;
            self.homeworkId     = obj.homeworkId || null;
            self.readingId      = obj.readingId || null;
            self.detailUrl      = obj.detailUrl || null;
            self.type           = obj.type || null;
            self.useVenus = obj.useVenus || false;
            self.dubbingId = ko.observable(null);
            self.dubbingScoreLevel = ko.observable("");
            self.studentName    = ko.observable("");
            self.readingName    = ko.observable("");
            self.oralQuestions  = ko.observableArray([]);
            self.exercisesInfo  = ko.observable(null);
            self.questionUrl    = null;
            self.completeUrl    = null;
            self.focusExamMap = {};
        };
        SingleReadDetail.prototype = {
            constructor : SingleReadDetail,
            getQuestion : function(examId){
                var self = this;
                var questionObj = self.focusExamMap[examId];
                if(!questionObj){
                    return 	[];
                }
                var questions = questionObj.questions;
                if(!$.isArray(questions) || questions.length === 0){
                    return [];
                }
                return questions.slice(0,1);
            },
            loadExamImg : function(rootEleId,prefixElemId,examId,index){
                var self = this;
                if($17.isBlank(rootEleId) || $17.isBlank(prefixElemId) || $17.isBlank(index)){
                    return "";
                }
                index = index || 0;
                var $mathExamImg = $("#" + rootEleId);
                if(!$17.isBlank(examId)){
                    $mathExamImg.empty();
                    var elemId = prefixElemId + "-" + index;
                    $("<div></div>").attr("id", elemId).appendTo($mathExamImg);
                    var node = document.getElementById(elemId);
                    var obj = vox.exam.render(node, 'teacher_history', {
                        ids       : [examId],
                        getQuestionsUrl : self.questionUrl + "READING&videoId=" + self.readingId,
                        getCompleteUrl  : self.completeUrl + "READING&videoId=" + self.readingId
                    });
                }else{
                    $mathExamImg.html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
                }
                return "";
            },
            haveAudio       : function(audio){
                if(audio){
                    return audio.split("|").length > 0;
                }
                return false;
            },
            playAudio       : function(element,rootObj){
                var that = this; //this -> user object
                if(rootObj.haveAudio(that.audio)){
                    var showFiles = that.audio.split("|") || [];
                    var $voicePlayer = $(element).find(".voicePlayer");
                    if($voicePlayer.hasClass("play-stop")){
                        $voicePlayer.removeClass("play-stop");
                        $("#jquery_jplayer_1").jPlayer("clearMedia");
                    }else{
                        var playIndex = 0;
                        $("#jquery_jplayer_1").jPlayer("destroy");
                        setTimeout(function(){
                            $("#jquery_jplayer_1").jPlayer({
                                ready: function (event) {
                                    rootObj.playSpecialAudio(showFiles[playIndex]);
                                },
                                error : function(event){
                                    playIndex = rootObj.playNextAudio(playIndex,showFiles);
                                },
                                ended : function(event){
                                    playIndex = rootObj.playNextAudio(playIndex,showFiles);
                                },
                                volume: 0.8,
                                solution: "html,flash",
                                swfPath: "/public/plugin/jPlayer",
                                supplied: "mp3"
                            });
                        },200);
                        $(".voicePlayer").removeClass("play-stop");
                        $voicePlayer.addClass("play-stop");
                    }
                }else{
                    $17.info("音频数据为空");
                }
            },
            playNextAudio    : function(playIndex,audioArr){
                if(playIndex >= audioArr.length - 1){
                    $(".voicePlayer").removeClass("play-stop");
                    $(this).jPlayer("destroy");
                }else{
                    playIndex++;
                    this.playSpecialAudio(audioArr[playIndex]);
                }
                return playIndex;
            },
            playSpecialAudio : function(url){
                if(url){
                    $("#jquery_jplayer_1").jPlayer("setMedia", {
                        mp3: $17.utils.hardCodeUrl(url)
                    }).jPlayer("play");
                }
            },
            getExercisesQuestions : function(){
                var self = this;
                var questions = [];
                var _info = self.exercisesInfo();
                if(_info != null
                        && _info.exercisesQuestionInfo != null
                        && _info.exercisesQuestionInfo.length > 0){
                    questions = _info.exercisesQuestionInfo;
                }
                return questions;
            },
            run         : function(){
                var self = this;
                if(self.detailUrl){
                    $.post(self.detailUrl,{
                        homeworkId          : self.homeworkId,
                        readingId           : self.readingId,
                        objectiveConfigType : self.type
                    },function(data){
                        var initData = function(){
                            self.questionUrl = data.questionUrl;
                            self.completeUrl = data.completedUrl;
                            self.studentName(data.studentName || "");
                            self.oralQuestions(data.oralQuestions || []);
                            self.exercisesInfo(data.exercisesInfo || null);
                            self.readingName(data.readingName || "");
                            self.dubbingId(data.dubbingId || null);
                            self.dubbingScoreLevel(data.dubbingScoreLevel || "");
                        };
                        if(data.success){
                            var _info = data.exercisesInfo || null;
                            if(self.useVenus){
                                var questionIds = [];
                                if(_info != null
                                        && _info.exercisesQuestionInfo != null
                                        && _info.exercisesQuestionInfo.length > 0){
                                    var questions = _info.exercisesQuestionInfo;
                                    for(var m = 0,mLen = questions.length; m < mLen; m++){
                                        questionIds.push(questions[m].questionId);
                                    }
                                }
                                $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                                    self.focusExamMap = result.success ? result.questionMap : {};
                                    initData();
                                });
                            }else{
                                initData();
                            }
                        }else{
                            $17.alert(data.info);
                        }
                    });
                }
            },
            playDubbingVideo : function(){
                var reading  = this;
                var dataHtml = "";
                var paramObj = {
                    dubbingId  : reading.dubbingId(),
                    from       : "preview"
                };
                var domain;
                if(constantObj.env === "test"){
                    domain = "//www.test.17zuoye.net/";
                }else{
                    domain = location.protocol + "//" + location.host;
                }
                var gameUrl = domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param(paramObj);
                dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';
                $.prompt(dataHtml, {
                    title   : "预 览",
                    buttons : {},
                    position: { width: 960 },
                    close   : function(){
                        if(reading.type === "READING"){
                            $('iframe').each(function(){
                                var win = this.contentWindow || this;
                                if(win.destroyHomeworkJavascriptObject){
                                    win.destroyHomeworkJavascriptObject();
                                }
                            });
                        }
                    }
                });
            }
        };
        $17.clazzReport = $17.clazzReport || {};
        $17.extend($17.clazzReport, {
            getSingleReadDetail : function(obj){
                return new SingleReadDetail(obj);
            }
        });
    }());
</script>
</@temp.page>