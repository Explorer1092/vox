/**
 * Created by Administrator on 2016/12/9.
 */
!function(window,ko,stuDayDetail) {
    "use strict";
    var selfPlayer = (function(){
        var $jPlayerContaner;
        function initJplayerElement(){
            $jPlayerContaner = $("#jquery_jplayer_1");
            if($jPlayerContaner.length == 0){
                $jPlayerContaner = $("<div></div>").attr("id","jquery_jplayer_1");
                $jPlayerContaner.appendTo("body");
            }
        }
        function playAudio(audioList,callback){
            if(!$.isArray(audioList) || audioList.length == 0){
                $17.alert('音频数据为空');
                return false;
            }
            initJplayerElement();
            var playIndex = 0;
            $jPlayerContaner.jPlayer("destroy");
            setTimeout(function(){
                $jPlayerContaner.jPlayer({
                    ready: function (event) {
                        playNextAudio(playIndex,audioList,callback);
                    },
                    error : function(event){
                        playIndex++;
                        playIndex = playNextAudio(playIndex,audioList,callback);
                    },
                    ended : function(event){
                        playIndex++;
                        playIndex = playNextAudio(playIndex,audioList,callback);
                    },
                    volume: 0.8,
                    solution: "html,flash",
                    swfPath: "/public/plugin/jPlayer",
                    supplied: "mp3"
                });
            },200);
        }
        function playNextAudio(playIndex,audioArr,callback){
            if(playIndex >= audioArr.length){
                $jPlayerContaner.jPlayer("destroy");
                $.isFunction(callback) && callback();
            }else{
                var url = audioArr[playIndex];
                url && $jPlayerContaner.jPlayer("setMedia", {
                    mp3: url
                }).jPlayer("play");
            }
            return playIndex;
        }
        function stopAudio(){
            $jPlayerContaner && $jPlayerContaner.jPlayer("clearMedia");
        }
        return {
            playAudio : playAudio,
            stopAudio : stopAudio,
            stopAll   : function(){
                $jPlayerContaner && $jPlayerContaner.jPlayer("destroy");
            }
        };
    }());

    var homework = {};
    var studentInfos = {};
    var objectiveConfigTypeRanks = [];
    var student_history = {
        getQuestionsUrl:"",
        getCompleteUrl:"",
        studentId:"",
        studentName:""
    };
    var categoryMap = {};
    var iconLevel = {
        EXCELLENT:"icon-you",//优
        GOOD:"icon-liang",//良
        FAIR:"icon-zhong",//中
        PASS:"icon-cha",//差
        FAIL:"icon-fail",//没通过,
        RIGHT:"icon-success",//对
        WRONG:"icon-error",//错
        UNKNOWN:"icon-unknown",//未知
        REVIEW:"icon-yue"//阅
    };
    var voiceStatus = {
        isfirst : true,
        toPlay  : true,
        isPause : false,
        qid     : 0,
        index   : 0
    };
    var templateMap = {
        categoryMap: ['<div class="h-floatLayer-L"><ul>{navHtml}</ul></div>'].join(""),
        subjectInfo: ['<div class="h-set-homework" qid="{qid}">',
            '<div class="seth-hd">',
            '<p class="fl"><span>{contentType}</span><span class="border-none">{difficulty}</span></p>',
            '</div>',
            '<div id="subject_{qid}" class="seth-mn"></div>{answers}',
            '</div>'].join(""),
        chineseBase:['<div class="h-set-homework" qid="{qid}">',
            '<div class="seth-hd">',
            '<p class="fl"><span>{testMethodName}</span><span class="border-none">{difficulty}</span></p>',
            '</div>',
            '<div id="subject_{qid}" class="seth-mn"></div>{answers}',
            '</div>'].join(""),
        readReciteInfo : ['<div class="h-set-homework" qid="{qid}">',
            '<div class="seth-hd">',
            '<p class="fl"><span>{answerWay}</span><span>{paragraphCName}</span></sp><span class="border-none">{articleName}</span></p>',
            '</div>',
            '<div id="subject_{qid}" class="seth-mn"></div>{answers}',
            '</div>'].join(""),
        subjectInfo_Photo: ['<div class="questionBox">',
            '<ul><li><img src="{picUrl}" allpic="{allpic}"><span class="icon {icon}"></span></li></ul>',
            '</div>'].join(""),
        subjectInfo_Voice: ['<div class="h-voiceBox">',
            '<div class="h-voice-list" style="width:auto;">',
            '<div class="J_voiceplay left" url="{voiceUrl}">',
            '<i class="icon {icon}"></i>',
            '</div>',
            '</div>',
            '</div>'].join("")
    };
    var stuReportDetail = function(){
        this.webLoading = ko.observable(true);
        this.studentId = ko.observable("");
        this.packageId = ko.observable("");
        this.subject = ko.observable("");

        this.href  = function(){
            return '/teacher/vacation/report/studentweekdetail.vpage?packageId='+this.packageId()+'&studentId='+ this.studentId();
        };
        this.initialise();
    };
    stuReportDetail.prototype = {
        constructor : stuReportDetail,
        initialise : function(){
            $17.voxLog({
                module: "m_Ri7qOfBC",
                op    : "page_detail_stu_day_task",
                s0    : stuDayDetail.subject
            });
            this.initExamCore();
            this.initVoice();
            this.initData();

        },
        substitute: function(str, object, regexp){
            return String(str).replace(regexp || (/\\?\{([^{}]+)\}/g), function(match, name){ if (match.charAt(0) == '\\') return match.slice(1); return (object[name] != null) ? object[name] : ''; })
        },
        initData : function(){
            var that = this;
            $.get(answerDetailData.examUrl,function(res){
                //console.log(res);
                if(res.success){
                    homework = {
                        homeworkId  : res.homeworkId,
                        subject     : res.subject,
                        homeworkType: res.homeworkType,
                        studentId   : res.userId,
                        studentName : res.userName,
                        packageId   : res.packageId
                    };
                    objectiveConfigTypeRanks = objectiveConfigTypeRanks.concat((res.objectiveConfigTypeRanks || []));
                    categoryMap = res.objectiveConfigTypes || {};
                    studentInfos = res.questionInfoMapper;
                    student_history = {
                        getQuestionsUrl : res.questionUrl,
                        getCompleteUrl  : res.completedUrl,
                        studentId       : res.userId,
                        studentName     : res.userName
                    };
                    var navHtml="";
                    $.each(objectiveConfigTypeRanks,function(i,type){
                        navHtml += '<li navtype="' + type + '"><a href="javascript:void(0)">' + categoryMap[type] + '</a></li>';
                    });
                    $(".h-homeworkCorrect").append(that.substitute(templateMap.categoryMap,{navHtml:navHtml}));
                    $($(".h-floatLayer-L li")[0]).addClass("active");

                    that.webLoading(false);
                    that.studentId(homework.studentId);
                    that.packageId(homework.packageId);
                    that.subject(homework.subject);
                    that.initDom();
                    that.initEvent();
                }else{
                    $17.alert(res.info || "亲，您没有访问权限哦");
                }
            });
        },
        initDom : function(){
            var that = this;
            var contentHtml="",tpl="", navName = $(".h-floatLayer-L li.active").attr("navtype");
            var typeTemplateMap = {
                BASIC_APP : "BASIC_APP",
                NATURAL_SPELLING : "BASIC_APP",
                READING   : "READING",
                ORAL_PRACTICE : "ORAL_PRACTICE",
                LS_KNOWLEDGE_REVIEW : "LS_KNOWLEDGE_REVIEW",
                NEW_READ_RECITE : "NEW_READ_RECITE",
                DUBBING         : "DUBBING",
                DUBBING_WITH_SCORE : "DUBBING",
                LEVEL_READINGS          : "READING",
                READ_RECITE_WITH_SCORE  : "READ_RECITE_WITH_SCORE"
            };

            if(typeTemplateMap.hasOwnProperty(navName)){
                var node = document.getElementById("tabContentHolder");
                ko.cleanNode(node);
                $("#tabContentHolder").html($("#" + typeTemplateMap[navName]).html());

                var viewModel = {
                    homeworkId : $17.getQuery("homeworkId"),
                    studentId : $17.getQuery("studentId"),
                    tab     : navName,
                    tabName : categoryMap[navName],
                    convertSecondToMin: function(second){
                        second = +second || 0;
                        var h = Math.floor(second/3600);
                        var min = Math.floor((second % 3600)/60);
                        var sec = (second % 3600)%60;
                        var secDesc = "";
                        secDesc += (h > 0 ? h + "小时" : "");
                        secDesc += (min > 0 ? min + "分" : "");
                        secDesc += (sec > 0 ? sec + "秒" : "");
                        return secDesc;
                    },
                    haveAudio: function (voiceUrls) {
                        if (voiceUrls) {
                            return voiceUrls.length > 0;
                        }
                        return false;
                    },
                    playSpecialAudio: function (url) {
                        if (url) {
                            $("#jquery_jplayer_1").jPlayer("setMedia", {
                                mp3: url
                            }).jPlayer("play");
                        }
                    },
                    playNextAudio: function (playIndex, audioArr) {
                        if (playIndex >= audioArr.length - 1) {
                            $(".voicePlayer").removeClass("pause").removeClass("h-playStop");
                            $("#jquery_jplayer_1").jPlayer("destroy");
                        } else {
                            playIndex++;
                            this.playSpecialAudio(audioArr[playIndex]);
                        }
                        return playIndex;
                    },
                    playAudio: function (element, rootObj) {
                        var that = this;
                        if (rootObj.haveAudio(that.voiceUrls || that.userVoiceUrls || that.personalVoiceToParagraph || that.personalVoiceToApp)) {
                            var showFiles = that.voiceUrls || that.userVoiceUrls  || that. personalVoiceToParagraph || that.personalVoiceToApp;;
                            var $voicePlayer = $(element).hasClass("voicePlayer") ? $(element) : $(element).find("i.voicePlayer");
                            if ($voicePlayer.hasClass("pause") || $voicePlayer.hasClass("h-playStop")) {
                                $voicePlayer.removeClass("pause").removeClass("h-playStop");
                                $("#jquery_jplayer_1").jPlayer("clearMedia");
                            } else {
                                var playIndex = 0;
                                $("#jquery_jplayer_1").jPlayer("destroy");
                                setTimeout(function () {
                                    $("#jquery_jplayer_1").jPlayer({
                                        ready: function (event) {
                                            rootObj.playSpecialAudio(showFiles[playIndex]);
                                        },
                                        error: function (event) {
                                            playIndex = rootObj.playNextAudio(playIndex, showFiles);
                                        },
                                        ended: function (event) {
                                            playIndex = rootObj.playNextAudio(playIndex, showFiles);
                                        },
                                        volume: 0.8,
                                        solution: "html,flash",
                                        swfPath: "/public/plugin/jPlayer",
                                        supplied: "mp3"
                                    });
                                }, 200);

                                $(".voicePlayer").removeClass("pause").removeClass("h-playStop");
                                $(element).hasClass("voicePlayer") ? $voicePlayer.addClass("pause") : $voicePlayer.addClass("h-playStop");
                            }
                        } else {
                            $17.info("音频数据为空");
                        }
                    }
                };
                switch (navName){
                    case "ORAL_PRACTICE":
                        $17.extend(viewModel,{
                            result : studentInfos[navName] || {},
                            showDifficulty : function(difficulty){
                                return difficulty==3?"中等":(this.difficulty==4||this.difficulty==5 ? "困难":"容易");
                            },
                            answerIndex : function(index){
                                return " : 第"+(index+1)+"小题";
                            },
                            renderSubject : function(qid){
                                var $subject = $("#subject_" + qid);
                                $subject.empty();
                                $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id","examImg-" + qid).appendTo($subject);

                                var node = document.getElementById("examImg-" + qid);
                                vox.exam.render(node, 'normal', {
                                    ids       : [qid],
                                    imgDomain : answerDetailData.imgDomain,
                                    env       : answerDetailData.env,
                                    domain    : answerDetailData.domain
                                });
                                return "";
                            }
                        });
                        break;
                    case "BASIC_APP":
                    case "NATURAL_SPELLING":
                        $17.extend(viewModel,{
                            result  : studentInfos[navName] || [],
                            rowspanCount : function(lessons){
                                if(!$.isArray(lessons)){
                                    return 1;
                                }
                                var rowCount = 0;
                                for(var m = 0,mLen = lessons.length; m < mLen; m++){
                                    rowCount += (lessons[m].categories ? lessons[m].categories.length : 0);
                                }
                                return rowCount < 1 ? 1 : rowCount;
                            },
                            viewCategoryDetail : function(){
                                var categoryObj = this;
                                //这儿如果你不return true,就不会跳转
                                return true;
                            }
                        });
                        break;
                    case "READING":
                    case "LEVEL_READINGS":
                        $17.extend(viewModel,{
                            defaultReadingImg : answerDetailData.defaultReadingImg,
                            result : studentInfos[navName] || {},
                            imgsrc : function(element,imgUrl){
                                var self = this;
                                element.onerror = function () {
                                    element.onerror = '';
                                    element.src = self.defaultReadingImg;
                                };
                                return imgUrl;
                            },
                            viewPicBookClick : function(){
                                var picBookObj = this;
                                //这儿如果你不return true,就不会跳转
                                return true;
                            },
                            viewReading : function(self){
                                var reading = this;
                                var dataHtml = "";
                                var paramObj,gameUrl;
                                if(self.tab === "LEVEL_READINGS"){
                                    paramObj = {
                                        pictureBookIds  : reading.pictureBookId,
                                        from            : "preview"
                                    };
                                    var domain;
                                    if(answerDetailData.env === "test"){
                                        domain = "//www.test.17zuoye.net/";
                                    }else{
                                        domain = location.protocol + "//" + location.host;
                                    }
                                    gameUrl = domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param(paramObj);
                                    dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                                }else{
                                    paramObj = {
                                        pictureBookId : this.pictureBookId,
                                        fromModule : ""
                                    };
                                    gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
                                    if(this.keywords && this.keywords.length > 0){
                                        dataHtml += "<div class=\"h-homework-pop\"><div class=\"popTitle\">重点词汇：</div><div class=\"popContent\">"+this.keywords.join(" | ")+"</div></div>";
                                    }
                                    dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';
                                }

                                $.prompt(dataHtml, {
                                    title   : "预 览",
                                    buttons : {},
                                    position: { width: 960 },
                                    close   : function(){
                                        if(self.tab === "READING"){
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
                        });
                        break;
                    case "NEW_READ_RECITE":
                        var checkStatus = false,
                            correctIcon = {
                                level01:"阅",
                                level02:"优",
                                level03:"良",
                                level04:"中",
                                level05:"差"
                            };
                        for(var item in studentInfos[navName]){

                            $.each(studentInfos[navName][item],function () {
                                var temp = typeof this.correctionInfo === "function" ? this.correctionInfo() : this.correctionInfo;
                                this.initQuestions = false;
                                this.checkHover = ko.observable(false);
                                this.showDetail = ko.observable(false);
                                this.correctionInfo = ko.observable(temp);
                                this.levelIcon = ko.pureComputed(function() {

                                    for(var name in correctIcon){
                                        if(correctIcon[name] == this.correctionInfo()){
                                            return name;
                                        };
                                    }
                                    return "";
                                },this);
                            });
                        }

                        var result=[{
                            name:"课文朗读",
                            data:studentInfos[navName].readData || []
                        },{
                            name:"课文背诵",
                            data:studentInfos[navName].reciteData || []
                        }];

                        $17.extend(viewModel,{
                            result : result,
                            showDetailBtn : function () {
                                if(!this.initQuestions){
                                    this.initQuestions = true;

                                    $.each(this.paragraphDetaileds,function () {
                                        var qid = this.questionId,
                                            $subject = $("#subject_" + qid);
                                        $subject.empty();
                                        $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id","examImg-" + qid).appendTo($subject);

                                        var node = document.getElementById("examImg-" + qid);
                                        vox.exam.render(node, 'normal', {
                                            ids       : [qid],
                                            imgDomain : answerDetailData.imgDomain,
                                            env       : answerDetailData.env,
                                            domain    : answerDetailData.domain
                                        });
                                    });
                                }
                                this.showDetail(!this.showDetail());
                            },
                            checkQuestion : function (name,correction) {
                                var that = this;
                                if(checkStatus){
                                    $17.alert("点击太频繁啦,请稍后再试～");
                                    return false;
                                };
                                checkStatus = true;
                                $.post("/teacher/new/homework/batchcorrectquestion.vpage",{
                                    data : JSON.stringify({
                                        homeworkId : homework.homeworkId,
                                        type : "NEW_READ_RECITE",
                                        questionId : this.questionBoxId,
                                        isBatch : false,
                                        corrections: [{
                                            userId: homework.studentId,
                                            review: true,
                                            correctType: null,
                                            correction: correction == "REVIEW" ? null : correction,
                                            teacherMark: ""
                                        }]
                                    })
                                },function (res) {
                                    checkStatus = false;
                                    if(res.success){

                                        that.correctionInfo(name);
                                    }else{

                                        $17.alert(res.info || "批改失败，请稍后再试～");
                                    }
                                });

                                $17.voxLog({
                                    module: "m_Odd245xH",
                                    op    : "personal_detail_text_readrecite_CorrectStatus_click"
                                });
                            }
                        });

                        ko.bindingHandlers.singleHover = {
                            init: function(element, valueAccessor){
                                $(element).hover(
                                    function(){
                                        $(element).find(".level-option").show();
                                    },
                                    function(){
                                        $(element).find(".level-option").hide();
                                    }
                                );
                            }
                        };

                        break;
                    case "DUBBING":
                    case "DUBBING_WITH_SCORE":
                        $17.extend(viewModel,{
                            result : studentInfos[navName] || {},
                            playVideoPopup : function(){
                                var dubbingKO = this;
                                var dataHtml = "";
                                var dubbingObj = ko.mapping.toJS(dubbingKO);
                                dataHtml = template("t:PLAY_VIDEO_OF_SINGLE_DUBBING_POPUP",{});
                                var flashWidth = 550,flashHeight = 275;
                                $.prompt(dataHtml,{
                                    title   : "预 览",
                                    position    : { width: 600},
                                    buttons     : {},
                                    focus       : 1,
                                    submit:function(e,v,m,f){},
                                    close   : function(){},
                                    loaded : function(){
                                        $("#dubbingPlayVideoContainer").getFlash({
                                            id       : "HISTORY_DUBBING_PLAY_PREVIEW",
                                            width    : flashWidth,//flash 宽度
                                            height   : flashHeight, //flash 高度
                                            movie    : answerDetailData.flashPlayerUrl,
                                            scale    : 'showall',
                                            flashvars: "file=" + dubbingObj.studentVideoUrl + "&amp;image=" + dubbingObj.coverUrl + "&amp;width=" + flashWidth + "&amp;height=" + flashHeight + "&amp;autostart=true"
                                        });
                                    }
                                });
                            }
                        });
                        break;
                    case "READ_RECITE_WITH_SCORE":
                        var resultArr = [{
                            name        : "课文朗读",
                            lessonList  : studentInfos[navName].readData || []
                        },{
                            name        : "课文背诵",
                            lessonList  : studentInfos[navName].reciteData || []
                        }];
                        $17.extend(viewModel,{
                            result                  : resultArr,
                            showParagraphId         : ko.observable(null),
                            playingBoxId            : ko.observable(null),
                            playingParagrapId       : ko.observable(null),
                            generateParagraphOrders : function(paragraphs){
                                var details = [];
                                for(var m = 0,mLen = paragraphs.length; m < mLen; m++){
                                    details.push(paragraphs[m].paragraphOrder);
                                }
                                if(details.length > 5){
                                    details = details.slice(0,5);
                                    details.push('......');
                                }
                                return details.join('、');
                            },
                            paragraphShowOrHide      : function(self,boxId,index){
                                var paragraph = this;
                                var paragraphId = boxId + '_' + index;
                                var showParagraphFn = self.showParagraphId;
                                if(!paragraph.initQuestions){
                                    paragraph.initQuestions = true;
                                    var $paragraphContainer = $("#c_" + paragraphId);
                                    var elementId = 'rrws_' + paragraphId;
                                    var $element = $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id",elementId);
                                    $paragraphContainer.empty();
                                    $element.appendTo($paragraphContainer);
                                    var paragraphNode = document.getElementById(elementId);
                                    vox.exam.render(paragraphNode, 'normal', {
                                        ids       : [paragraph.questionId],
                                        imgDomain : answerDetailData.imgDomain,
                                        env       : answerDetailData.env,
                                        domain    : answerDetailData.domain,
                                        objectiveConfigType : self.tab
                                    });
                                }
                                showParagraphFn(showParagraphFn() == paragraphId ? null : paragraphId);
                            },
                            playAudio               : function(self,boxId,index){
                                var paragraph = this;
                                var paragraphId = boxId + '_' + index;
                                var playingParagrapIdFn = self.playingParagrapId;
                                if(paragraphId != playingParagrapIdFn()){
                                    playingParagrapIdFn(paragraphId);
                                    selfPlayer.playAudio(paragraph.voices,function(){
                                        playingParagrapIdFn(null);
                                    });
                                }else{
                                    selfPlayer.stopAudio();
                                    playingParagrapIdFn(null);
                                }
                                $17.voxLog({
                                    module  : "m_Odd245xH",
                                    op      : "	ReadReciteScore_PersonalDetail_play_click"
                                });
                            },
                            playLessonAudio            : function(self){
                                var lesson = this;
                                var boxId = lesson.questionBoxId;
                                var playingBoxIdFn = self.playingBoxId;
                                if(boxId != playingBoxIdFn()){
                                    playingBoxIdFn(boxId);
                                    selfPlayer.playAudio(lesson.voices,function(){
                                        playingBoxIdFn(null);
                                    });
                                }else{
                                    selfPlayer.stopAudio();
                                    playingBoxIdFn(null);
                                }
                                $17.voxLog({
                                    module  : "m_Odd245xH",
                                    op      : "	ReadReciteScore_PersonalDetail_play_click"
                                });
                            },
                            initSubscribes          : function(){
                                var self = this;
                                self.playingBoxId.subscribe(function(val){
                                    if(!$17.isBlank(val)){
                                        self.playingParagrapId(null);
                                    }
                                },self);
                                self.playingParagrapId.subscribe(function(val){
                                    if(!$17.isBlank(val)){
                                        self.playingBoxId(null);
                                    }
                                },self);
                            }
                        });
                        viewModel.initSubscribes();
                        break;
                    default:
                }
                ko.applyBindings(viewModel,node);
            }else{
                switch(navName){
                    case "READ_RECITE":
                        tpl = templateMap.readReciteInfo;
                        break;
                    case "BASIC_KNOWLEDGE":
                        tpl = templateMap.chineseBase;
                        break;
                    case "CHINESE_READING":
                    default:
                        tpl = templateMap.subjectInfo;
                }

                $.each(studentInfos[navName],function(){
                    var answers = "";

                    contentHtml += that.substitute(tpl,{
                        qid             : this.qid,
                        testMethodName  : this.testMethodName,
                        contentType     : this.contentType,
                        difficulty      : this.difficulty==3?"中等":(this.difficulty==4||this.difficulty==5 ? "困难":"容易"),
                        answers         : answers,
                        articleName     : this.articleName,
                        paragraphCName  : this.paragraphCName,
                        answerWay       : this.answerWay
                    });
                });
                $(".J_mainContentHolder").html('<div class="w-base-title" style="background-color: #e1f0fc;"><h3 >'+categoryMap[navName]+'</h3></div>'+contentHtml+'</div>');

                $.each(studentInfos[navName],function(){
                    var node = document.getElementById("subject_" + this.qid);
                    vox.exam.render(node, 'teacher_history', {
                        ids             : [this.qid],
                        imgDomain       : answerDetailData.imgDomain,
                        env             : answerDetailData.env,
                        domain          : answerDetailData.domain,
                        getQuestionsUrl : student_history.getQuestionsUrl+navName,
                        getCompleteUrl  : (student_history.getCompleteUrl+navName+"&studentId="+student_history.studentId)
                    });
                });
            }
        },
        initEvent: function(){
            var that = this;
            $(".h-floatLayer-L").on("click","li",function(){
                $(this).addClass("active").siblings().removeClass('active');
                that.initDom();
            });

            $(".J_eventBind").on("click",".J_voiceplay",function(){
                var self = this;
                var qid = $(this).attr("qid"), index = $(this).attr("index");

                if(voiceStatus.index != index || voiceStatus.qid != qid){
                    voiceStatus.toPlay = true;
                    $("#jquery_jplayer_1").jPlayer("stop");
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.pause);
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.ended);
                    $("#jquery_jplayer_1").jPlayer("clearMedia");
                }
                voiceStatus.qid = qid;
                voiceStatus.index = index;
                if (voiceStatus.toPlay) {
                    $("#jquery_jplayer_1").jPlayer("setMedia", {
                        mp3: $(this).attr("url").split("|")[0]
                    });
                    $("#jquery_jplayer_1").bind($.jPlayer.event.play, function () {
                        $(self).addClass("pause");
                        voiceStatus.toPlay=false;
                    });
                    $("#jquery_jplayer_1").bind($.jPlayer.event.pause, function () {
                        $(self).removeClass("pause");
                        voiceStatus.toPlay=true;
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.ended);
                    });
                    $("#jquery_jplayer_1").bind($.jPlayer.event.ended, function () {
                        $(self).removeClass("pause");
                        voiceStatus.toPlay=true;
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.pause);
                    });
                    $("#jquery_jplayer_1").jPlayer("play");
                }else{
                    $("#jquery_jplayer_1").jPlayer("stop");
                }
            });

            $(".J_eventBind").on("click",".questionBox li", function () {
                that.initBigPic(this);
                $("#showBigPic").show();
            });

            $("#showBigPic .J_bigPicItem ").on("click",".rotation",function(){
                var rotate270 = ["@270w_270h_0r","@270w_270h_90r","@270w_270h_180r","@270w_270h_270r"];
                var rotate410 = ["@410w_410h_0r","@410w_410h_90r","@410w_410h_180r","@410w_410h_270r"];
                var new_ratate = (parseInt($(this).attr("rotate"))+1)%4;
                $(this).attr("rotate", new_ratate);
                var imgSrc = $(this).parent("div").find("img").attr("src");
                var newImgSrc = imgSrc.indexOf("@")>-1?(imgSrc.substring(0,imgSrc.indexOf("@"))):imgSrc;
                if($(this).parents("li").height()==270){
                    $(this).parent("div").find("img").attr("src",newImgSrc+rotate270[new_ratate]);
                }else{
                    $(this).parent("div").find("img").attr("src",newImgSrc+rotate410[new_ratate]);
                }
            });

            $("#showBigPic .close").on("click",function() {
                $("#showBigPic").hide();
            });

            $("#showComment").on("click",function(){
                $("#clazzHomeworkReportEventDiv").trigger({
                    type     : "reportStudentInfo.singleComment",
                    userId   : homework.studentId,
                    userName : homework.studentName,
                    subject  : homework.subject,
                    okBtnFn  :function(comment){
                        //评语保存结构
                        var detail = {};
                        detail.comment = comment;
                        detail.homeworkId = $.trim(homework.homeworkId);
                        $17.voxLog({
                            module: "m_Ri7qOfBC",
                            op    : "comment_confirm_click",
                            s0    : stuDayDetail.subject
                        });
                        $.post('/teacher/vacation/report/comment.vpage', detail, function(data){
                            if(data.success){
                                /*评语更新*/
                                $("#clazzHomeworkReportEventDiv").trigger({
                                    type       : "reportSingleComment.editComment",
                                    studentIds : [homework.studentId],
                                    comment    : comment
                                });
                                $.prompt.goToState("commentSuccess");
                            }else{
                                $17.alert("评语发送失败！");
                            }
                        });
                    }
                });
                $17.voxLog({
                    module: "m_Ri7qOfBC",
                    op    : "comment_click",
                    s0    : stuDayDetail.subject
                });
            });

            $("#rewardBeans").on("click",function(){
                $("#clazzHomeworkReportEventDiv").trigger({
                    type    : "reportStudentInfo.singleRewardBeans",
                    userId   : homework.studentId,
                    userName : homework.studentName,
                    subject  : homework.subject,
                    okBtnFn  : function(rewardIntegral){
                        var recordData = {};
                        recordData.homeworkId = $.trim(homework.homeworkId);
                        recordData.rewardIntegral = rewardIntegral;
                        $17.voxLog({
                            module: "m_Ri7qOfBC",
                            op    : "reward_confirm_click",
                            s0    : stuDayDetail.subject
                        });
                        $.get('/teacher/vacation/report/rewardintegral.vpage', recordData, function(data){
                            if(data.success){
                                $17.alert("奖励学豆成功！");
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                });
                $17.voxLog({
                    module: "m_Ri7qOfBC",
                    op    : "reward_click",
                    s0    : stuDayDetail.subject
                });
            });
        },
        initBigPic : function(e){
            var tpl="",allPics = $(e).find("img").attr("allpic").split(",");
            var imgFormat = allPics.length==3?"@270w_270h":"@410w_410h";
            $.each(allPics,function(){
                var src = this + imgFormat;
                tpl += '<li><div class="image"><div class="grouper"><img src="'+src+'" draggable="false"></div><div class="rotation" rotate="0"></div></div></li>'
            });
            if(allPics.length==2){
                $("#showBigPic .flex-viewport").addClass("flex-viewport-twoImg").removeClass("flex-viewport-threeImg");
            }else if(allPics.length==3){
                $("#showBigPic .flex-viewport").addClass("flex-viewport-threeImg").removeClass("flex-viewport-twoImg");
            }else{
                $("#showBigPic .flex-viewport").removeClass("flex-viewport-twoImg").removeClass("flex-viewport-threeImg");
            }
            $(".J_bigPicItem").html(tpl);
        },
        initExamCore : function(){//初始化加载应试
            try{
                vox.exam.create(function(data){
                    if(data.success){
                        //成功
                    }else{
                        $17.tongji('voxExamCreate','create_error',location.pathname);
                    }
                });
            }catch(exception){
                $17.tongji('voxExamCreate','examCoreJs_error',exception.message);
            }
        },
        initVoice: function(){
            $("#jquery_jplayer_1").jPlayer({
                ready: function (event) {
                    $(this).jPlayer("setMedia", {
                        mp3: ""
                    });
                },
                volume: 0.8,
                solution: "html,flash",
                swfPath: "/public/plugin/jPlayer",
                supplied: "mp3"
            });
        }
    };
    //return new stuReportDetail();
    ko.applyBindings(new stuReportDetail(),document.getElementById("mainContent"));
}(window,ko,stuDayDetail);
