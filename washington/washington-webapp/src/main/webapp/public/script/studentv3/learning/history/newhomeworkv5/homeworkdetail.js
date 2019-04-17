!function() {
    "use strict";

    var selfPlayer = (function(){
        var $jPlayerContaner;
        function initJplayerElement(){
            $jPlayerContaner = $("#jquery_jplayer_1");
            if($jPlayerContaner.length === 0){
                $jPlayerContaner = $("<div></div>").attr("id","jquery_jplayer_1");
                $jPlayerContaner.appendTo("body");
            }
        }
        function playAudio(audioList,callback){
            if(!audioList){
                $17.alert('音频数据为空');
                return false;
            }
            if(typeof audioList === "string") audioList = audioList.split(",");
            if(!$.isArray(audioList) || audioList.length === 0){
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
                    mp3: $17.utils.hardCodeUrl(url)
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
    var QuestionDB = (function(){
        var questionMap = {};   //用来存储每道题的详细信息，即题库数据
        return {
            addQuestions : function(questionIds,callback){
                callback = $.isFunction(callback) ? callback : function(){};
                if(!$.isArray(questionIds) || questionIds.length === 0){
                    callback({
                        success : true,
                        info    : "题目为空"
                    });
                    return false;
                }
                var unLoadQuestionIds = [];
                for(var m = 0,mLen = questionIds.length; m < mLen; m++){
                    !questionMap.hasOwnProperty(questionIds[m]) && unLoadQuestionIds.push(questionIds[m]);
                }
                $.get("/exam/flash/load/newquestion/byids.vpage",{
                    data:JSON.stringify({ids: unLoadQuestionIds,containsAnswer:false})
                }).done(function(res){
                    if(res.success){
                        var result = res.result;
                        for(var t = 0,tLen = result.length; t < tLen; t++){
                            questionMap[result[t].id] = result[t];
                        }
                    }
                    callback({
                        success : res.success
                    });
                }).fail(function(e){
                    callback({
                        success : false,
                        info    : e.message
                    });
                });
            },
            deleteQuestions : function(questionIds){
                if(!$.isArray(questionIds) || questionIds.length === 0){
                    return false;
                }
                for(var m = 0,mLen = questionIds.length; m < mLen; m++){
                    delete questionMap[questionIds[m]];
                }
                return true;
            },
            getQuestionById : function(questionId){
                return questionMap[questionId] || null;
            }
        }
    }());
    var objectiveConfigTypeRanks = [];
    var detailDate = {};
    var categoryMap = {};
    var voiceStatus = {
        isfirst : true,
        toPlay : true,
        isPause : false
    };
    var TPL = {
        mainConent:['<div class="h-answerD-list">',
            '<div id="subject_{qid}" class="hd" style="font-size: 16px;"></div>',
            '<div class="ft">',
            '<ul class="ul-list-2">',
            '<li>标准答案：{standardAnswers}</li>',
            '<li class="lastLi">我的答案：{userAnswers}</li>',
            '</ul>',
            '</div>',
            '</div>'].join(""),
        mainContent_Photo_Voice:['<div class="h-answerD-list">',
            '<div id="subject_{qid}" class="hd" style="font-size: 16px;"></div>',
            '<div class="ft">',
            '<p class="myAnswer-title">我的作答：</p>',
            '<ul class="show-myAnswer">{answer}</ul>',
            '</div>',
            '</div>'].join(""),
        mainContent_READ_RECITE:['<div class="h-answerD-list">',
            '<div class="title"><span>{answerWay}</span><span>{paragraphCName}</span><span>{articleName}</span></div>',
            '<div id="subject_{qid}" class="hd" style="font-size: 16px;"></div>',
            '<div class="ft">',
            '<p class="myAnswer-title">我的作答：</p>',
            '<ul class="show-myAnswer">{answer}</ul>',
            '</div>',
            '</div>'].join("")

    };
    var stuDetail = function(){
        this.initialise();
    };
    stuDetail.prototype = {
        constructor : stuDetail,
        initialise : function(){
            this.initDom();
            this.initVoice();

            $17.voxLog({
                module : "m_iYt273Lm",
                op     : "homework_detail_page_load"
            },"student");
        },
        substitute: function(str, object, regexp){
            return String(str).replace(regexp || (/\\?\{([^{}]+)\}/g), function(match, name){ if (match.charAt(0) == '\\') return match.slice(1); return (object[name] != null) ? object[name] : ''; })
        },
        optimizeAnswer : function(content){
            //对学生答案分数的优化,支持正常显示分数
            if((/\\frac\{.*?\}\{.*?\}/g).test(content)){
                content = content.replace(/\\frac\{.*?\}\{.*?\}/g,function(word){
                    return "$"+word+"$";
                });
            }else if(/\\\w+(\[[^\]]+\])?({[^}]+})*/g.test(content)){
                content = '$' + content + '$';
            }
            return content;
        },
        initDom : function(){
            var that = this,homeworkId = $17.getQuery("homeworkId");
            $.get("/student/learning/history/newhomework/answerdetail.vpage",{
                homeworkId:homeworkId
            },function(res){
                detailDate = res;
                if(res.success){
                    $.extend(true,categoryMap,res.objectiveConfigTypes);
                    objectiveConfigTypeRanks = objectiveConfigTypeRanks.concat((res.objectiveConfigTypeRanks || []));
                    that.navInfo();
                    var navName = $(".J_navigation a[class='active']").attr("navtype");
                    that.mainContent(navName);
                    that.subjectInfo(navName);
                    that.initEvent();
                }else{
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/student/learning/history/newhomework/answerdetail.vpage",
                        s1     : $.toJSON(res),
                        s2     : $.toJSON({homeworkId : homeworkId}),
                        s3     : answerDetailData.env
                    });
                }
            });
        },
        initEvent: function(){
            var that = this;
            $(".J_navigation").on("click","li",function(){
                var navName = $(this).find("a").attr("navtype");
                var subject = $17.getQuery("subject") || "";
                $(".J_navigation a").removeClass("active");
                $(this).find("a").addClass("active");
                that.mainContent(navName);
                that.subjectInfo(navName);
                $17.voxLog({
                    module : "m_9vFa5c0g",
                    op     : "result_tab_load",
                    s0     : subject,
                    s1     : navName
                },"student");
            });
            $(".J_mainConent").on("click",".J_voiceplay",function(){
                var self = this;
                if(voiceStatus.isPause){
                    voiceStatus.isPause = false;
                    $("#jquery_jplayer_1").jPlayer("stop");
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.pause);
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.ended);
                    $("#jquery_jplayer_1").jPlayer("clearMedia");
                    return;
                }else{
                    var mp3Url = $(this).attr("v_src");
                    mp3Url = $17.utils.hardCodeUrl(mp3Url);
                    $("#jquery_jplayer_1").jPlayer("setMedia", {
                        mp3: mp3Url
                    });
                    $("#jquery_jplayer_1").bind($.jPlayer.event.play, function () {
                        $(self).addClass("audio-pause");
                        $(self).removeClass("hover");
                        voiceStatus.isPause = true;
                    });
                    $("#jquery_jplayer_1").bind($.jPlayer.event.pause, function () {
                        $(self).removeClass("audio-pause");
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.ended);
                        voiceStatus.isPause = false;
                    });
                    $("#jquery_jplayer_1").bind($.jPlayer.event.ended, function () {
                        $(self).removeClass("audio-pause");
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                        $("#jquery_jplayer_1").unbind($.jPlayer.event.pause);
                        voiceStatus.isPause = false;
                    });
                    $("#jquery_jplayer_1").jPlayer("play");
                }
            }).on("mouseover",".J_voiceplay",function(){
                var $this = $(this);
                if(!$this.hasClass("audio-pause")){
                    $this.addClass("hover");
                }
            }).on("mouseout",".J_voiceplay",function(){
                $(this).removeClass("hover");
            });
        },
        navInfo: function(){
            var nav = "";
            $.each(objectiveConfigTypeRanks, function(index, type){
                nav += "<li><a href=\"javascript:void(0)\"  navtype=\"" + type + "\">" + categoryMap[type] + "</a></li>";
            });
            $(".J_navigation").html(nav);

            var _navFocus = $17.getQuery("type");
            if(_navFocus && categoryMap.hasOwnProperty(_navFocus)){
                $(".J_navigation a[navtype='" + _navFocus + "']").addClass("active");
            }else{
                $($(".J_navigation a")[0]).addClass("active");
            }
        },
        mainContent: function(navName){
            var typeTemplateMap = {
                BASIC_APP               : "BASIC_APP",
                NATURAL_SPELLING        : "BASIC_APP",
                READING                 : "READING",
                ORAL_PRACTICE           : "ORAL_PRACTICE",
                ORAL_INTELLIGENT_TEACHING : "ORAL_PRACTICE",
                LS_KNOWLEDGE_REVIEW     : "LS_KNOWLEDGE_REVIEW",
                NEW_READ_RECITE         : "NEW_READ_RECITE",
                DUBBING                 : "DUBBING",
                DUBBING_WITH_SCORE      : "DUBBING",
                LEVEL_READINGS          : "READING",
                READ_RECITE_WITH_SCORE  : "READ_RECITE_WITH_SCORE",
                WORD_RECOGNITION_AND_READING : "WORD_RECOGNITION_AND_READING",
                ORAL_COMMUNICATION : "ORAL_COMMUNICATION",
                WORD_TEACH_AND_PRACTICE : "WORD_TEACH_AND_PRACTICE"
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
                    focusExamMap : {},  //存放题目的实体结构
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
                    fetchRemoteQuestion : function(questionIds,callback){
                        var self = this;
                        var focusExamMap = self.focusExamMap;
                        var newQuestionIds = [];
                        for(var z = 0,zLen = questionIds.length; z < zLen; z++){
                            if(!focusExamMap.hasOwnProperty(questionIds[z])){
                                newQuestionIds.push(questionIds[z]);
                            }
                        }
                        if(newQuestionIds.length === 0){
                            $.isFunction(callback) && callback();
                            return false;
                        }
                        $17.QuestionDB.getQuestionByIds(newQuestionIds,function(result){
                            var questionMap = result.success ? result.questionMap : {};
                            self.focusExamMap = $.extend(true,focusExamMap,questionMap);
                            $.isFunction(callback) && callback();
                        });
                    },
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
                    }
                };
                var viewModel_voice = {
                    playAudio       : function(element,rootObj){
                        var that = this,
                            showFiles = that.userVoiceUrls || this.personalVoiceToParagraph || this.personalVoiceToApp,
                            $voicePlayer = $(element),
                            $jplayer = $("#jquery_jplayer_1"),
                            playIndex = 0;

                        if(!showFiles){
                            $17.alert("没有音频");
                            return false;
                        }

                        if($voicePlayer.hasClass("pause")){
                            $voicePlayer.removeClass("pause");
                            $jplayer.jPlayer("clearMedia");
                        }else{
                            $jplayer.jPlayer("destroy");
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
                                    solution: "flash, html",
                                    swfPath: "/public/plugin/jPlayer",
                                    supplied: "mp3"
                                });
                            },200);
                            $(".voicePlayer").removeClass("pause");
                            $voicePlayer.addClass("pause");
                        }

                        $17.voxLog({
                            module : "m_iYt273Lm",
                            op     : "text_readrecite_detail_play_click",
                            s0     : !this.personalVoiceToApp ? "part" : "full"
                        },"student");
                    },
                    playNextAudio    : function(playIndex,audioArr){
                        if(playIndex >= audioArr.length - 1){
                            $(".voicePlayer").removeClass("pause");
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
                    }
                };
                switch (navName){
                    case "BASIC_APP":
                    case "NATURAL_SPELLING":
                    case "LS_KNOWLEDGE_REVIEW":
                        $17.extend(viewModel,{
                            result  : detailDate.questionInfoMapper[navName] || [],
                            rowspanCount : function(lessons){
                                if(!$.isArray(lessons)){
                                    return 1;
                                }
                                var rowCount = 0;
                                for(var m = 0,mLen = lessons.length; m < mLen; m++){
                                    rowCount += (lessons[m].categories ? lessons[m].categories.length : 0);
                                }
                                return rowCount < 1 ? 1 : rowCount;
                            }
                        });
                        break;
                    case "READING":
                    case "LEVEL_READINGS":
                        $17.extend(viewModel,{
                            defaultReadingImg : answerDetailData.defaultReadingImg,
                            result : detailDate.questionInfoMapper[navName] || {},
                            imgsrc : function(element,imgUrl){
                                element.onerror = function () {
                                    element.onerror = '';
                                    element.src = this.defaultReadingImg;
                                };
                                return imgUrl;
                            },
                            viewReading : function(pictureBookId,keywords,self){
                                var dataHtml = "";
                                var paramObj,gameUrl;
                                if(self.tab === "LEVEL_READINGS"){
                                    paramObj = {
                                        pictureBookIds : pictureBookId,
                                        from : "preview"
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
                                        pictureBookId : pictureBookId,
                                        fromModule : ""
                                    };
                                    gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
                                    if(keywords && keywords.length > 0){
                                        dataHtml += "<div class=\"h-homework-pop\"><div class=\"popTitle\">重点词汇：</div><div class=\"popContent\">"+keywords.join(" | ")+"</div></div>";
                                    }
                                    dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';
                                }
                                $.prompt(dataHtml, {
                                    title   : "预 览",
                                    buttons : {},
                                    position: { width: 960 },
                                    close   : function(){
                                        if(self.tab === "READING") {
                                            $('iframe').each(function () {
                                                var win = this.contentWindow || this;
                                                if (win.destroyHomeworkJavascriptObject) {
                                                    win.destroyHomeworkJavascriptObject();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                        break;
                    case "ORAL_PRACTICE":
                    case "ORAL_INTELLIGENT_TEACHING":
                        $17.extend(viewModel,viewModel_voice);
                        $17.extend(viewModel,{
                            questionDetailList : detailDate.questionInfoMapper[navName] || [],
                            questionLoading : ko.observable(true),
                            fetchQuestion : function(){
                                var self = this;
                                var typeResult = self.questionDetailList;
                                var questionIds = [];
                                for(var m = 0,mLen = typeResult.length; m < mLen; m++){
                                    questionIds.push(typeResult[m].qid);
                                }
                                viewModel.fetchRemoteQuestion(questionIds,function(){
                                     self.questionLoading(false);
                                });
                            }
                        });
                        viewModel.fetchQuestion.call(viewModel);
                        break;
                    case "NEW_READ_RECITE":
                        var correctIcon = {
                            level01:"未批改",
                            level02:"阅",
                            level03:"优",
                            level04:"良",
                            level05:"中",
                            level06:"差"
                        };
                        for(var item in detailDate.questionInfoMapper[navName]){
                            $.each(detailDate.questionInfoMapper[navName][item],function () {
                                this.initQuestions = false;
                                this.showDetail = ko.observable(false);
                                this.levelIcon = ko.pureComputed(function() {
                                    for(var name in correctIcon){
                                        if(correctIcon[name] == this.correctionInfo){
                                            return name;
                                        }
                                    }
                                    return "level01";
                                },this);
                            });
                        }
                        var result=[{
                            name:"课文朗读",
                            data:detailDate.questionInfoMapper[navName].readData || []
                        },{
                            name:"课文背诵",
                            data:detailDate.questionInfoMapper[navName].reciteData || []
                        }];
                        $17.extend(viewModel,viewModel_voice);
                        $17.extend(viewModel,{
                            result :  result,
                            showParagraphId  : ko.observable(null),
                            showDetailBtn : function (self,boxId,index) {
                                var paragraphId = boxId + '_' + index;
                                var showParagraphFn = self.showParagraphId;
                                var questionIds = [];
                                $.each(this.paragraphDetaileds,function () {
                                    questionIds.push(this.questionId);
                                });
                                self.fetchRemoteQuestion(questionIds,function(){
                                    showParagraphFn(showParagraphFn() === paragraphId ? null : paragraphId);
                                });
                            }
                        });
                        break;
                    case "DUBBING":
                    case "DUBBING_WITH_SCORE":
                        $17.extend(viewModel,{
                            result : detailDate.questionInfoMapper[navName] || {},
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
                            type        : "READ",
                            lessonList  : detailDate.questionInfoMapper[navName].readData || []
                        },{
                            name        : "课文背诵",
                            type        : "RECITE",
                            lessonList  : detailDate.questionInfoMapper[navName].reciteData || []
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
                                var questionId = paragraph.questionId;
                                var sentencesArr = paragraph.sentences;
                                var wordResultArr = ($.isArray(sentencesArr) && sentencesArr.length > 0 && $.isArray(sentencesArr[0].words)) ? sentencesArr[0].words : [];
                                var questionObj = QuestionDB.getQuestionById(questionId);

                                var callbackFn = function(question,words,voiceEngineType){
                                    words = $.isArray(words) ? words : [];
                                    if(!question || !question.content){
                                        return "";
                                    }
                                    var subContents = question.content.subContents;
                                    if(!Array.isArray(subContents) || subContents.length === 0){
                                        return "";
                                    }
                                    var questionContent = subContents[0].content;
                                    var $questionTextContent = $(questionContent);
                                    var wds = $questionTextContent.find('.paraNotShiGe').children('span');
                                    var submitVoiceEngine = voiceEngineType;
                                    wds  = wds.filter(function(idx){
                                        // 过滤空格和静音
                                        var trim_wds = $(wds[idx]).text() != ' ' && $(wds[idx]).text() != 'sil';
                                        if(submitVoiceEngine === "SingSound"){
                                            //先声过滤DOM中符号
                                            trim_wds = trim_wds && /[0-9\u4e00-\u9fa5]/.test($(wds[idx]).text());
                                        }

                                        return trim_wds;
                                    });
                                    for(var i = 0; i < words.length; i++){
                                        if(((submitVoiceEngine === "Unisound" && words[i].score < 3) ||
                                            (submitVoiceEngine === "SingSound" && words[i].score < 4))
                                            && /[0-9\u4e00-\u9fa5]/.test($(wds[i]).text())){
                                            $(wds[i]).addClass('tRed');
                                        }
                                    }

                                    var elementId = 'rrws_' + paragraphId;
                                    var $element = $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id",elementId);
                                    $questionTextContent.appendTo($element);
                                    var $paragraphContainer = $("#c_" + paragraphId);
                                    $paragraphContainer.empty();
                                    $element.appendTo($paragraphContainer);
                                    showParagraphFn(showParagraphFn() === paragraphId ? null : paragraphId);
                                };
                                //题目不存在，则认为没有渲染过
                                if(!questionObj){
                                    QuestionDB.addQuestions([questionId],function(result){
                                        if(result.success){
                                            questionObj = QuestionDB.getQuestionById(questionId);
                                            callbackFn(questionObj,wordResultArr,paragraph.voiceEngineType);
                                        }else{
                                            $17.alert("获取题目失败");
                                        }
                                    });
                                }else if(!paragraph.initQuestions){ //该段落没有初始化过，则认为没有渲染过
                                    callbackFn(questionObj,wordResultArr,paragraph.voiceEngineType);
                                }else{
                                    showParagraphFn(showParagraphFn() === paragraphId ? null : paragraphId);
                                }
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
                                    module : "m_iYt273Lm",
                                    op     : "ReadReciteScore_PersonalDetail_play_click"
                                },"student");
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
                                    module : "m_iYt273Lm",
                                    op     : "ReadReciteScore_PersonalDetail_play_click"
                                },"student");
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
                    case "WORD_RECOGNITION_AND_READING":
                        $17.extend(viewModel,{
                            result                  : detailDate.questionInfoMapper[navName],
                            showParagraphId         : ko.observable(null),
                            playingBoxId            : ko.observable(null),
                            playingParagrapId       : ko.observable(null),
                            paragraphShowOrHide      : function(self,boxId,index){
                                var question = this;
                                var paragraphId = boxId + '_' + index;
                                var showParagraphFn = self.showParagraphId;
                                var questionId = question.questionId;
                                // if(!self.getQuestion(questionId)){
                                QuestionDB.addQuestions([questionId],function(result){
                                    if(result.success){
                                        showParagraphFn(showParagraphFn() === paragraphId ? null : paragraphId);
                                    }else{
                                        $17.alert("获取题目失败");
                                    }
                                });
                                // }
                                $17.voxLog({
                                    module : "m_9vFa5c0g",
                                    op     : "word_read_result_tab_expand_click"
                                },"student");
                            },
                            playAudio               : function(audioParams){
                                audioParams = audioParams || {};
                                var self = this;
                                var paragraphId = audioParams.id;
                                var playingParagrapIdFn = self.playingParagrapId;
                                if(paragraphId !== playingParagrapIdFn()){
                                    playingParagrapIdFn(paragraphId);
                                    selfPlayer.playAudio(audioParams.audioUrl,function(){
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
                                if(boxId !== playingBoxIdFn()){
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
                            },
                            getQuestion : function(questionId){
                                return QuestionDB.getQuestionById(questionId);
                            }
                        });
                        viewModel.initSubscribes();
                        break;
                    case "ORAL_COMMUNICATION":
                        $17.extend(viewModel,{
                            result : detailDate.questionInfoMapper[navName] || {}
                        });
                        break;
                    case "WORD_TEACH_AND_PRACTICE":
                        $17.extend(viewModel,{
                            resultList : detailDate.questionInfoMapper[navName] || [],
                            completedUrl : detailDate.completedUrl,
                            sectionModuleLoaded : ko.observableArray([]),
                            questionAnswerMap : {},  //题目ID对应的答案
                            focusExamMap : {},  //题目详情
                            getQuestionDifficultyName : function(difficulty){
                                difficulty = difficulty * 1;
                                return difficulty === 3 ? "中等" : (difficulty === 4 || difficulty === 5 ? "困难":"容易")
                            },
                            getQuestion : function(examId,stoneId){
                                var questionObj = this.focusExamMap[examId];
                                if(!questionObj){
                                    return 	[];
                                }
                                var questions = questionObj.questions;
                                if(!$.isArray(questions) || questions.length === 0){
                                    return [];
                                }
                                questions = questions.slice(0,1);
                                var stoneQuestionMap = this.questionAnswerMap[stoneId] || {};
                                var questionAnswer = stoneQuestionMap[examId] || {};
                                var answerResponse = {
                                    userAnswers: questionAnswer["userAnswers"],
                                    subMaster: questionAnswer["subMaster"],
                                    master: questionAnswer["master"],
                                    hwTrajectory : questionAnswer["hwTrajectory"]
                                };
                                //将用户答案与题目数据合并，生成新的题目对象
                                questions[0] = $.extend(true, {}, questions[0], answerResponse);
                                return questions;
                            },
                            _initQuestionEnitiy : function(){
                                var self = this,resultList = this.resultList;
                                var sectionModuleLoaded = [], questionIds = [];
                                for(var t = 0,tLen = resultList.length; t < tLen; t++){
                                    var wordExerciseModuleData = resultList[t].wordExerciseModuleData;
                                    if(!wordExerciseModuleData){break;}
                                    var sectionId = resultList[t].sectionId;
                                    var stoneId = resultList[t].stoneId;
                                    var uniqueKey = [sectionId,stoneId].join("_");
                                    var questions = wordExerciseModuleData.wordExerciseQuestionData || [];
                                    for(var m = 0,mLen = questions.length; m < mLen; m++){
                                        questionIds.push(questions[m].qid);
                                    }
                                    sectionModuleLoaded.push(uniqueKey);
                                }
                                var focusExamMap = this.focusExamMap;
                                $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                                    var tempExamMap = result.success ? result.questionMap : {};
                                    for(var key in tempExamMap){
                                        if(tempExamMap.hasOwnProperty(key) && !focusExamMap.hasOwnProperty(key)){
                                            focusExamMap[key] = tempExamMap[key];
                                        }
                                    }
                                    self.sectionModuleLoaded(sectionModuleLoaded);
                                });
                            },
                            fetchExamAnswer : function(){
                                var self = this;
                                $.get("/student/exam/word/teach/questions/answer.vpage",{
                                    homeworkId : $17.getQuery("homeworkId"),
                                    type : navName
                                }).done(function(res){
                                    self.questionAnswerMap = res.result ? res.result : {};
                                    self._initQuestionEnitiy();
                                }).fail(function(){
                                    self.questionAnswerMap = {};
                                    self._initQuestionEnitiy();
                                });
                            },
                            previewChapter : function(){
                                var studentObj = this;
                                var domain = "/";
                                if(answerDetailData.env === "test"){
                                    domain = "//www.test.17zuoye.net/";
                                }else{
                                    domain = location.protocol + "//" + location.host;
                                }
                                var gameUrl = domain + "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-speak-training/works.vhtml?" + $.param({
                                    __p__ : JSON.stringify({
                                        shareUrl : studentObj.flashvarsUrl
                                    })
                                });
                                var dataHtml = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="375" marginwidth="0" height="640" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                                $.prompt(dataHtml, {
                                    title   : "预 览",
                                    buttons : {},
                                    position: { width: 435 },
                                    submit  : function(){},
                                    close   : function(){}
                                });
                            }
                        });
                        viewModel.fetchExamAnswer();
                        break;
                    default:
                        break;
                }
                ko.applyBindings(viewModel,node);
            }else{
                var that=this,str = '<div class="h-title-2" style="margin-bottom: 15px;"><span class="left-text">作业题目 -- 答题详情</span></div>';
                $.each(detailDate.questionInfoMapper[navName],function(){
                    if(this.fileType[0]=="IMAGE"){
                        var answer = "";
                        $.each(this.fileUrl,function(){
                            answer += '<li style="margin-right: 10px;"><img src="'+this+'@50q"></li>';
                        });
                        str += that.substitute(TPL.mainContent_Photo_Voice,{
                            qid:this.qid,
                            answer:answer
                        });
                    }else if(this.fileType[0]=="AUDIO"){
                        str += that.substitute(navName === "READ_RECITE" ? TPL.mainContent_READ_RECITE : TPL.mainContent_Photo_Voice,{
                            qid:this.qid,
                            answer:'<div class="J_voiceplay audio-play" V_src="'+this.fileUrl[0]+'"></div>',
                            articleName : this.articleName,
                            paragraphCName : this.paragraphCName,
                            answerWay : this.answerWay
                        });
                    }else{
                        str += that.substitute(TPL.mainConent,{
                            qid:this.qid,
                            standardAnswers: that.optimizeAnswer(this.standardAnswers),
                            userAnswers:that.optimizeAnswer(this.userAnswers)
                        });
                    }
                });
                $(".J_mainConent").html(str);
            }
        },
        subjectInfo: function(navName){
            switch (navName){
                case "BASIC_APP":
                case "NATURAL_SPELLING":
                case "READING":
                case "LS_KNOWLEDGE_REVIEW":
                case "NEW_READ_RECITE":
                case "ORAL_PRACTICE":
                case "ORAL_INTELLIGENT_TEACHING":
                case "DUBBING":
                case "DUBBING_WITH_SCORE":
                case "READ_RECITE_WITH_SCORE":
                case "WORD_RECOGNITION_AND_READING":
                case "LEVEL_READINGS":
                    break;
                default:
                    var typeResult = detailDate.questionInfoMapper[navName];
                    var questionIds = [];
                    $.each(typeResult,function(){
                        questionIds.push(this.qid);
                    });
                    //下面已经有答案了。使用normal模式吧
                    var focusExamMap = {};
                    var getQuestion = function(examId){
                        var questionObj = focusExamMap[examId];
                        if(!questionObj){
                            return 	[];
                        }
                        var questions = questionObj.questions;
                        if(!$.isArray(questions) || questions.length === 0){
                            return [];
                        }
                        return questions.slice(0,1);
                    };
                    $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                        focusExamMap = result.success ? result.questionMap : {};
                        $.each(typeResult,function(){
                            var config = {
                                container: "#subject_" + this.qid, //容器的id，（必须）
                                formulaContainer:'#tabContentHolder', //公式渲染容器（必须）
                                questionList: getQuestion(this.qid), //试题数组，包含完整的试题json结构， （必须）
                                framework: {
                                    vue: Vue, //vue框架的外部引用
                                    vuex: Vuex //vuex框架的外部引用
                                },
                                showAnalysis: false, //是否展示解析
                                showUserAnswer: false, //是否展示用户答案
                                showRightAnswer: false, //是否展示正确答案
                                startIndex : 0 //从第几题开始
                            };
                            window["Venus"].init(config);
                        });
                    });
                    break;
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
                solution: "flash, html",
                swfPath: "/public/plugin/jPlayer",
                supplied: "mp3"
            });
        }
    };
    return new stuDetail();
}();
