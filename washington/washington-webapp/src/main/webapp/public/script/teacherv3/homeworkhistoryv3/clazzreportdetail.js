!function() {
    "use strict";

    var selfPlayer = (function(){
        var $jPlayerContaner;
        var $element;
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
                url = $17.utils.hardCodeUrl(url);
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


    ko.bindingHandlers.singleBasicContentHover = {
        init: function(element, valueAccessor){
            $(element).hover(
                function(){
                    $(element).addClass("hover");
                },
                function(){
                    $(element).removeClass("hover");
                }
            );
        }
    };
    var buttonGroup;
    //存放H5返回的句柄
    var questionHandles = {};
    var homework = {
        homeworkId : null,
        homeworkType: null,
        subject : null
    };
    var subjectInfos = {}, categoryMap = {}, objectiveConfigTypeRanks = [];
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
    var updateResultList = {
        homeworkId: "",
        type: "",
        questionId: "",
        isBatch: false,
        corrections: []
    };
    var voiceStatus = {
        isfirst      : true,
        toPlay       : true,
        isPause      : false,
        curStudentId : 0,
        qid          : 0,
        index        : 0
    };
    var templateMap = {
        categoryMap: ['<div class="h-floatLayer-L"><ul>{navHtml}</ul>',
                        '<a href="javascript:void(0)" class="correct-btn"><span>一键批改</span><div class="correct-flyaer"></div></a>',
                        '</div>'].join(""),
        subjectInfo: ['<div class="h-set-homework examTopicBox" navtype="{navType}" qid="{qid}">',
                        '<div class="seth-hd">',
                            '<p class="fl"><span>{contentType}</span><span class="border-none">{difficulty}</span></p>',
                        '</div>',
                        '<div class="seth-mn iconWrapper"><div id="subject_{qid}"></div>{rate}<div class="testPaper-info"><div class="linkGroup"><a class="view_exam_answer" style="display: none;" href="javascript:void(0);">查看答案与解析</a></div></div></div>',
                        '<div class="t-error-info w-table" style="margin-top: 1px;">',
                            '<table>',
                                '<thead><tr><td style="width: 190px;">答案</td><td>对应同学</td></tr></thead>',
                                '<tbody>{answers}</tbody>',
                            '</table>',
                         '</div>',
                    '</div>'].join(""),
        chineseBase:['<div class="h-set-homework examTopicBox" navtype="{navType}" qid="{qid}">',
            '<div class="seth-hd">',
            '<p class="fl"><span>{testMethodName}</span><span class="border-none">{difficulty}</span></p>',
            '</div>',
            '<div class="seth-mn iconWrapper"><div id="subject_{qid}"></div>{rate}<div class="testPaper-info"><div class="linkGroup"><a class="view_exam_answer" style="display: none;" href="javascript:void(0);">查看答案与解析</a></div></div></div>',
            '<div class="t-error-info w-table" style="margin-top: 1px;">',
            '<table>',
            '<thead><tr><td style="width: 190px;">答案</td><td>对应同学</td></tr></thead>',
            '<tbody>{answers}</tbody>',
            '</table>',
            '</div>',
            '</div>'].join(""),
        subjectInfo_PHOTO_VOICE:['<div class="h-set-homework" qid="{qid}">',
                                    '<div class="seth-hd">',
                                    '<p class="fl"><span>{contentType}</span><span class="border-none">{difficulty}</span></p>',
                                    '</div>',
                                    '<div class="seth-mn iconWrapper line"><div id="subject_{qid}"></div></div>',
                                    '{answers}',
                                    '</div>'].join(""),
        subjectInfo_READ_RECITE:['<div class="h-set-homework" qid="{qid}">',
                                    '<div class="seth-hd">',
                                    '<p class="fl"><span>{answerWay}</span><span>{paragraphCName}</span><span class="border-none">{articleName}</span></p>',
                                    '</div>',
                                    '<div class="seth-mn iconWrapper line"><div id="subject_{qid}"></div></div>',
                                    '{answers}',
                                    '</div>'].join(""),
        answers_nopic : '<tr class="{odd}"><td class="{color}">{answer}</td><td>{userName}</td></tr>',
        answers_pic : ['<tr class="{odd}">',
                            '<td>{answer}</td>',
                            '<td><div class="questionBox">',
                                '<ul>{picbox}</ul>',
                            '</div></td>',
                        '</tr>'].join(""),
        answer_voice : ['<div class="h-voice-list" uid={uid}>',
                            '<div class="J_voiceplay left" url="{url}" uid={uid}><i class="icon {icon}"></i></div>',
                            '<div class="right">',
                            '<p class="name">{userName}</p>',
                            '<a href="javascript:void(0)" class="J_updateScore btn {yueicon}" type="REVIEW">阅</a>',
                            '<div class="J_dafen" style="display:inline-block;"><a href="javascript:void(0)" class="btn btn-blue">打分</a>',
                            '<ul class="gradeBox hidden">',
                                '<li class="J_updateScore" type="EXCELLENT">优</li>',
                                '<li class="J_updateScore" type="GOOD">良</li>',
                                '<li class="J_updateScore" type="FAIR">中</li>',
                                '<li class="J_updateScore" type="PASS">差</li>',
                            '</ul></div>',
                            '</div>',
                        '</div>'].join("")
    };

    var reportdetail = function(){
        this.initExamCore();
        this.initialise();
    };
    reportdetail.prototype = {
        constructor : reportdetail,
        initialise : function(){
            this.initData();
            this.initVoice();
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
        },
        substitute: function(str, object, regexp){
            return String(str).replace(regexp || (/\\?\{([^{}]+)\}/g), function(match, name){ if (match.charAt(0) == '\\') return match.slice(1); return (object[name] != null) ? object[name] : ''; })
        },
        initData : function(){
            var that = this;
            $.get(answerDetailData.examUrl,function(res){
                homework = {
                    homeworkId : res.homeworkId,
                    homeworkType: res.homeworkType,
                    subject : res.subject
                };
                $17.voxLog({
                    module : "m_Odd245xH",
                    op     : "hometype_all_details_load",
                    s0     : res.subject,
                    s1     : res.homeworkType,
                    s2     : res.homeworkId,
                    s3     : that.getQuery("tabtype")
                });
                if(res.success){
                    objectiveConfigTypeRanks = objectiveConfigTypeRanks.concat((res.objectiveConfigTypeRanks || []));
                    categoryMap = res.objectiveConfigTypes || {};
                    subjectInfos = res.questionInfoMapper;
                    var navHtml="",category="";
                    $.each(objectiveConfigTypeRanks,function(i,type){
                        navHtml += '<li navtype="'+type+'"><a href="javascript:void(0)">'+categoryMap[type]+'</a></li>';
                        category += type+",";
                    });
                    $(".h-homeworkCorrect").append(that.substitute(templateMap.categoryMap,{navHtml:navHtml}));

                    var navtype = that.getQuery("tabtype") || "";
                    if(navtype){
                        $(".h-floatLayer-L li[navtype='"+navtype.toUpperCase()+"']").addClass("active");
                    }else{
                        $($(".h-floatLayer-L li")[0]).addClass("active");
                    }
                    var _includeSubjective = res.includeSubjective || false;
                    if(!_includeSubjective){
                        $(".h-floatLayer-L").find(".correct-btn").hide();
                    }
                    that.initDom();
                    that.initEvent();
                    //上一个下一个按钮
                    var ButtonGroup = function(obj){
                        var self = this,_typeRanks = obj.objectiveConfigTypeRanks || [],foucsNavType = obj.foucsNavType || "";
                        self.categoryMap = obj.categoryMap || {};
                        self.objectiveConfigTypeRanks = ko.observableArray(_typeRanks);
                        self.focusIndex = ko.observable(_typeRanks.length > 0 ? _typeRanks.indexOf(foucsNavType) : -1);
                        self.newNextIndex = function(nextIndex){
                            var typeRanksLen = self.objectiveConfigTypeRanks().length;
                            if(nextIndex < 0){
                                nextIndex = typeRanksLen - 1;
                            }
                            if(nextIndex > typeRanksLen - 1){
                                nextIndex = 0;
                            }
                            return nextIndex;
                        };
                        self.getTypeName = function(nextIndex){
                            return self.categoryMap[self.objectiveConfigTypeRanks()[self.newNextIndex(nextIndex)]];
                        };
                        self.prevOrNextClick = function(newIndex){
                            var newFocusIndex = self.newNextIndex(newIndex),navType = self.objectiveConfigTypeRanks()[newFocusIndex];
                            self.focusIndex(newFocusIndex);
                            $(".h-floatLayer-L li[navtype='" + navType.toUpperCase() + "']").trigger("click");
                        };
                        self.setFocusType = function(navType){
                            self.focusIndex(_typeRanks.length > 0 ? _typeRanks.indexOf(navType) : -1);
                        };
                    };
                    buttonGroup = new ButtonGroup({
                        objectiveConfigTypeRanks : objectiveConfigTypeRanks,
                        categoryMap              : categoryMap,
                        foucsNavType             : navtype
                    });
                    ko.applyBindings(buttonGroup,document.getElementById("homeworkTypeExchange"));
                }else{
                    $17.alert(res.info || "您没有访问权限哦~");
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : answerDetailData.examUrl,
                        s1     : $.toJSON(res),
                        s3     : $uper.env
                    });
                }
            });
        },
        initEvent: function(){
            var that = this;
            $(".h-floatLayer-L li").on("click",function(){
                var $this = $(this);
                $(this).addClass('active').siblings().removeClass('active');
                window.scroll(0, 0);
                that.initDom();
                //alert($this.attr("navtype"));
                buttonGroup.setFocusType($this.attr("navtype"));
            });

            $(".h-floatLayer-L .correct-btn").on("click",function(){
                that.sendlog("Newhomework_passport_"+$uper.subject.key,"homework_answer_page_a_key_correct_btn");
                that.updateScore({
                    homeworkId:that.getQuery("homeworkId"),
                    isBatch:true
                },function(){
                    $17.alert("已把所有未批改作业批改为已阅，请及时查看");
                    window.location.reload();
                });
            });

            $(".J_eventBind").on("click",".questionBox li", function () {
                $(this).addClass("currentBigPic");

                if($(".h-floatLayer-L li[class='active']").attr("navtype")=="EXAM"){
                    $(".J_checkSubjectRegion").html(['<span><a href="javascript:void(0);" class="btn-read" type="REVIEW"></a></span>',
                        '<span><a href="javascript:void(0);" class="btn-right" type="RIGHT"></a></span>',
                        '<span><a href="javascript:void(0);" class="btn-error" type="WRONG"></a></span>'].join(" "));
                }else{
                    $(".J_checkSubjectRegion").html(['<span><a href="javascript:void(0);" class="btn-read" type="REVIEW"></a></span>',
                                                    '<span><a href="javascript:void(0);" class="btn-excellent" type="EXCELLENT"></a></span>',
                                                    '<span><a href="javascript:void(0);" class="btn-good" type="GOOD"></a></span>',
                                                    '<span><a href="javascript:void(0);" class="btn-mid" type="FAIR"></a></span>',
                                                    '<span><a href="javascript:void(0);" class="btn-differ" type="PASS"></a></span>'].join(" "));
                }
                that.initBigPic(this);
                $("#showBigPic").show();
            });

            $("#showBigPic .flex-direction-nav").on("click","a",function(){
                var contain = $(".J_mainContentHolder li[class='currentBigPic']").parent("ul");
                var index = contain.find("li").index($(".J_mainContentHolder li[class='currentBigPic']"));
                if($(this).hasClass("flex-next")){
                    if(contain.find("li").length==(index+1)){
                        return false;
                    }
                    contain.find("li").eq(index+1).addClass("currentBigPic").siblings().removeClass("currentBigPic");
                    that.initBigPic(contain.find("li").eq(index+1));
                }else{
                    if(index == 0){
                        return false;
                    }
                    contain.find("li").eq(index-1).addClass("currentBigPic").siblings().removeClass("currentBigPic");
                    that.initBigPic(contain.find("li").eq(index-1));
                }
            });

            $("#showBigPic .close").on("click",function(){
                $(".J_mainContentHolder li").removeClass("currentBigPic");
                if(updateResultList.corrections.length==0){
                    $("#showBigPic").hide();
                    return;
                }else{
                    updateResultList.homeworkId = that.getQuery("homeworkId");
                    updateResultList.type = $(".h-floatLayer-L li[class='active']").attr("navtype");
                    updateResultList.questionId = $($("#showBigPic .J_bigPicItem li")[0]).attr("qid");
                    updateResultList.isBatch = false;
                }
                $("#showBigPic").hide();
                that.updateScore(updateResultList,function(res,item,qid){
                    updateResultList.corrections = [];
                    if(res.success && res.questioninfos){
                        $.each(res.questioninfos,function(){
                            $(".h-set-homework[qid='"+qid+"']").find("li[uid='"+this.userId+"'] .icon").attr("class","icon "+(iconLevel[this.correction] || "icon-yue"));
                        });
                    }
                },this,$($("#showBigPic .J_bigPicItem li")[0]).attr("qid"));
            });

            $("#showBigPic .J_checkSubjectRegion").on("click","a",function(){
                var that = this,isNew = true;
                if($(".J_bigPicItem img").attr("src").indexOf("upflie-img")>-1){
                    return ;
                }
                $(that).parent("span").addClass("active").siblings().removeClass("active");
                $.each(updateResultList.corrections,function(){
                    if(this.userId==$($("#showBigPic .J_bigPicItem li")[0]).attr("uid")){
                        isNew = false;
                        this.correction = $(that).attr("type")=="REVIEW"?"":$(that).attr("type");
                    }
                });
                if(isNew){
                    updateResultList.corrections.push({
                        userId: $($("#showBigPic .J_bigPicItem li")[0]).attr("uid"),
                        review: true,
                        correctType: null,
                        correction: $(that).attr("type")=="REVIEW"? null : $(that).attr("type"),
                        teacherMark: ""
                    });
                }
            });

            $("#showBigPic .J_bigPicItem ").on("click",".rotation",function(){
                var rotate270 = ["@270w_270h_0r","@270w_270h_90r","@270w_270h_180r","@270w_270h_270r"];
                var rotate410 = ["@410w_410h_0r","@410w_410h_90r","@410w_410h_180r","@410w_410h_270r"];
                var new_ratate = (parseInt($(this).attr("rotate"))+1)%4;
                $(this).attr("rotate", new_ratate);
                var imgSrc = $(this).parent("div").find("img").attr("src");
                if(imgSrc.indexOf("upflie-img")>-1){
                    return;
                }
                var newImgSrc = imgSrc.indexOf("@")>-1?(imgSrc.substring(0,imgSrc.indexOf("@"))):imgSrc;
                if($(this).parents("li").height()==270){
                    $(this).parent("div").find("img").attr("src",newImgSrc+rotate270[new_ratate]);
                }else{
                    $(this).parent("div").find("img").attr("src",newImgSrc+rotate410[new_ratate]);
                }
            });

            $(".J_eventBind").on("mouseover mouseout",".J_dafen",function(){
                var self = this;
                var defaultIcon = $($(this).parents(".h-voice-list")[0]).find(".icon").attr("class");
                $.each(iconLevel,function(i){
                    if(defaultIcon.indexOf(this)>-1){
                        $(self).find(".J_updateScore[type='"+i+"']").addClass("active").siblings().removeClass('active');
                    }
                });
                $(this).find(".gradeBox").toggle();
            }).on("click",".J_voiceplay",function(){
                var self = this;
                var uid = $(this).attr("uid"), qid = $(this).attr("qid"), index = $(this).attr("index");

                if(voiceStatus.qid != qid || voiceStatus.curStudentId != uid || voiceStatus.index != index){
                    voiceStatus.toPlay = true;
                    $("#jquery_jplayer_1").jPlayer("stop");
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.play);
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.pause);
                    $("#jquery_jplayer_1").unbind($.jPlayer.event.ended);
                    $("#jquery_jplayer_1").jPlayer("clearMedia");
                }
                voiceStatus.qid = qid;
                voiceStatus.curStudentId = uid;
                voiceStatus.index = index;
                if (voiceStatus.toPlay) {
                    var mp3Url = $(this).attr("url").split("|")[0];
                    mp3Url = $17.utils.hardCodeUrl(mp3Url);
                    $("#jquery_jplayer_1").jPlayer("setMedia", {
                        mp3: mp3Url
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
            }).on("click",".J_updateScore",function(){
                if($(this).hasClass("btn-disabled")){
                    return;
                }
                if($(this).attr("type")=="REVIEW"){
                    $(this).removeClass("btn-yellow").addClass("btn-disabled");
                    $(".J_updateScore ").removeClass("active");
                }else{
                    $(this).parents(".J_dafen").siblings(".J_updateScore").removeClass("btn-disabled").addClass("btn-yellow");
                    $(this).addClass("active").siblings().removeClass('active');
                }
                var navType = $(".h-floatLayer-L li[class='active']").attr("navtype");
                var qid = $($(this).parents(".h-set-homework")[0]).attr("qid");
                var review = true;
                var userId = $($(this).parents(".h-voice-list")[0]).attr("uid");
                var correction = $(this).attr("type")=="REVIEW"? null : $(this).attr("type");
                var param = {
                    homeworkId : that.getQuery("homeworkId"),
                    type : navType,
                    questionId : qid,
                    isBatch : false,
                    corrections: [{
                        userId: userId,
                        review: review,
                        correctType: null,
                        correction: correction,
                        teacherMark: ""
                    }]
                };
                that.updateScore(param,function(res,item){
                    updateResultList.corrections = [];
                    if(res.success){
                        $($(item).parents(".h-voice-list")[0]).find("i").attr("class","icon "+iconLevel[$(item).attr("type")]);
                        that._updateSubjectInfos(navType,qid,[userId],review,correction);

                    }
                },this);
            });
            $(".J_eventBind").on("mouseover",".J_voiceplay",function(){
                $(this).addClass("play");
            }).on("mouseout",".J_voiceplay",function(){
                $(this).removeClass("play");
            });


            $(".J_eventBind").on("mouseover","div.examTopicBox",function(){
                var $this = $(this),tempNavType = $this.attr("navtype");
                if(["EXAM","INTELLIGENCE_EXAM","UNIT_QUIZ"].indexOf(tempNavType) !== -1){
                    $this.addClass("current");
                    $this.find("a.view_exam_answer").show();
                }
            }).on("mouseout","div.examTopicBox", function(){
                var $this = $(this);
                $this.removeClass("current");
                $this.find("a.view_exam_answer").hide();
            });

            $(".J_eventBind").on("click","a.view_exam_answer",function(){
                var $this = $(this),_qid = $this.closest("div.examTopicBox").attr("qid");
                if(questionHandles.hasOwnProperty(_qid)){
                    questionHandles[_qid].showAnalysis(_qid);
                }else{
                    $17.alert("该题目没有解析");
                }
            });

            $(".J_eventBind").on("click","li.mentalArithmeticTab",function(){
                var elementId = $(this).attr("data-ref");
                var $element = $("#" + elementId);
                var contentHtml="",TPL = templateMap.subjectInfo, rate="",tabTypeUrl="",navName = $(".h-floatLayer-L li[class='active']").attr("navtype");
                if(elementId === "viewMentalQuestion" && !$element.hasClass("data-loaded")){
                    $.each(subjectInfos[navName].questionDetails,function(){
                        var answers = "";
                        if($.isArray(this.errorAnswerList) && this.errorAnswerList.length > 0){
                            if(this.showType==0){
                                $.each(this.errorAnswerList,function(i,item){
                                    var userName="",odd=i%2==0?"odd":"",color=(item.answer == "答案正确")?"txt-green":"";
                                    for(var i=0;i<item.users.length;i++){
                                        userName += item.users[i].userName + "，";
                                    }
                                    answers += that.substitute(templateMap.answers_nopic,{odd:odd,answer:(item.answer || "未作答"),color:color,userName:userName.substring(0,userName.length-1)});
                                });
                            }else if(this.showType==1){
                                $.each(this.errorAnswerList,function(i,item){
                                    var picbox="",odd=i%2==0?"odd":"",color=(item.answer == "答案正确")?"txt-green":"";
                                    answers += '<div class="questionBox"><ul>';
                                    for(var i=0;i<item.users.length;i++){
                                        var icon = iconLevel[item.users[i].correction]?iconLevel[item.users[i].correction]:(item.users[i].review?"icon-yue":"");
                                        var showpic = answerDetailData.defaultPicUrl,hidepic = answerDetailData.defaultPicUrl;
                                        if(item.users[i].showPics.length>0){
                                            showpic = item.users[i].showPics[0];
                                            hidepic = item.users[i].showPics.join(",");
                                        }
                                        picbox += '<li uid="'+item.users[i].userId+'"><p class="name">'+item.users[i].userName+'</p><img allpic="'+hidepic+'" src="'+showpic+'"><span class="icon '+icon+'"></span></li>';
                                    }
                                    if(navName=="PHOTO_OBJECTIVE"){
                                        answers += picbox;
                                    }else{
                                        answers += that.substitute(templateMap.answers_pic,{odd:odd,answer:(item.answer || "未作答"),color:color,picbox:picbox});
                                    }
                                    answers += "</ul></div>";
                                });
                            }else if(this.showType==2){
                                $.each(this.errorAnswerList,function(){
                                    answers += '<div class="h-voiceBox">';
                                    for(var i=0;i<this.users.length;i++){
                                        var uid = this.users[i].userId;
                                        var icon = iconLevel[this.users[i].correction]?iconLevel[this.users[i].correction]:(this.users[i].review?"icon-yue":"");
                                        var yueicon =  icon=="icon-yue"?"btn-disabled":"btn-yellow";
                                        var url = this.users[i].showPics.join("|");
                                        answers += that.substitute(templateMap.answer_voice,{uid:uid,url:url,icon:icon,userName:this.users[i].userName,yueicon:yueicon});
                                    }
                                    answers += "</div>";
                                });
                            }
                        }

                        if(this.rate==0){
                            rate = ['<div class="icon-correct-b icon-b">',
                                '<div class="inner">',
                                '<div class="text">全部正确</div>',
                                '<div class="item"><span class="icon-correct-s"></span></div>',
                                '</div>',
                                '</div>'].join("");
                        }else if( !$17.isBlank(this.rate) ){
                            rate = ['<div class="icon-error-b icon-b">',
                                '<div class="inner">',
                                '<div class="text">失分率</div>',
                                '<div class="item">'+this.rate+'%</div>',
                                '</div>',
                                '</div>'].join("");
                        }

                        contentHtml += that.substitute(TPL,{
                            navType : navName,
                            qid:this.qid,
                            testMethodName:this.testMethodName,
                            contentType:this.contentType,
                            difficulty:this.difficulty==3?"中等":(this.difficulty==4||this.difficulty==5 ? "困难":"容易"),
                            rate:rate,
                            answers:answers,
                            articleName : this.articleName,
                            paragraphCName : this.paragraphCName,
                            answerWay : this.answerWay
                        });
                    });
                    $element.html(contentHtml);

                    $.each(subjectInfos[navName].questionDetails,function(){
                        var node = document.getElementById("subject_" + this.qid);
                        var obj = vox.exam.render(node, 'normal', {
                            ids       : [this.qid],
                            imgDomain : answerDetailData.imgDomain,
                            env       : answerDetailData.env,
                            domain    : answerDetailData.domain
                        });
                        questionHandles[this.qid] = obj;
                    });

                    $element.addClass("data-loaded");
                }
                $(this).addClass("active").siblings().removeClass("active");
                $element.show().siblings().hide();
            });

        },
        _updateSubjectInfos : function (type,qid,userIds,review,correction,correct_desc) {
            if(!qid || !$.isArray(userIds) || userIds.length == 0){
                return false;
            }
            var _questionArr = subjectInfos[type] || [];
            if(_questionArr.length > 0){
                $.each(_questionArr,function (index,question) {
                    if(question.qid === qid && question.errorAnswerList && question.errorAnswerList.length > 0){
                        $.each(question.errorAnswerList,function(eIndex,answerObj){
                            $.each(answerObj.users,function(userIndex,user){
                                if(userIds.indexOf(user.userId.toString()) != -1){
                                    if(review){
                                        user.review = review;
                                    }
                                    if(correction){
                                        user.correction = correction;
                                    }
                                    if(correct_desc){
                                        user.correct_desc = correct_desc;
                                    }
                                }
                            });
                        });
                    }
                });
            }
            return true;
        },
        initDom : function(){
            var that = this;
            var contentHtml="",TPL = "", rate="",tabTypeUrl="",navName = $(".h-floatLayer-L li[class='active']").attr("navtype");
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
                ORAL_COMMUNICATION      : "ORAL_COMMUNICATION",
                WORD_TEACH_AND_PRACTICE  : "WORD_TEACH_AND_PRACTICE"
            };
            if(typeTemplateMap.hasOwnProperty(navName)){
                var node = document.getElementById("tabContentHolder");
                ko.cleanNode(node);
                $("#tabContentHolder").html($("#" + typeTemplateMap[navName]).html());
                var viewModel = {};
                var viewModel_voice = {
                    haveAudio       : function(voiceUrls){
                        return this.getAudios(voiceUrls).length > 0;
                    },
                    getAudios       : function(voiceUrls){
                        if($.isArray(voiceUrls)){
                            return voiceUrls;
                        }else if(typeof voiceUrls === "string"){
                            return voiceUrls.split("|").length > 0;
                        }else{
                            return [];
                        }
                    },
                    playAudio : function(element,rootObj){
                        var that = this;
                        var showFiles = rootObj.getAudios(ko.mapping.toJS(that.voiceUrls || that.userVoiceUrls || that.showPics));
                        if(showFiles.length > 0){
                            var $voicePlayer = $(element).hasClass("voicePlayer") ? $(element) : $(element).find("i.voicePlayer");
                            if($voicePlayer.hasClass("pause") || $voicePlayer.hasClass("h-playStop")){
                                if(navName == "NEW_READ_RECITE"){
                                    $voicePlayer.html("播放");
                                }
                                $voicePlayer.removeClass("pause").removeClass("h-playStop");
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
                                if(navName == "NEW_READ_RECITE"){
                                    $voicePlayer.html("停止");
                                }
                                $(".voicePlayer").removeClass("pause").removeClass("h-playStop");
                                $(element).hasClass("voicePlayer") ? $voicePlayer.addClass("pause") : $voicePlayer.addClass("h-playStop");

                                $17.voxLog({
                                    module: "m_Odd245xH",
                                    op    : "question_detail_text_readrecite_play_click"
                                });
                            }
                        }else{
                            $17.info("音频数据为空");
                        }
                    },
                    playNextAudio    : function(playIndex,audioArr){
                        if(playIndex >= audioArr.length - 1){
                            this.stopAudio();
                        }else{
                            playIndex++;
                            this.playSpecialAudio(audioArr[playIndex]);
                        }
                        return playIndex;
                    },
                    stopAudio        : function(){
                        var navName = $(".h-floatLayer-L li[class='active']").attr("navtype");
                        $(".voicePlayer").removeClass("h-playStop").removeClass("pause");
                        $("#jquery_jplayer_1").jPlayer("destroy");

                        if(navName == "NEW_READ_RECITE"){
                            $(".voicePlayer").html("播放");
                        }
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
                    case "ORAL_PRACTICE":
                    case "ORAL_INTELLIGENT_TEACHING":
                        $17.extend(viewModel,viewModel_voice);
                        $17.extend(viewModel,{
                            result       : subjectInfos[navName] || [],
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
                    case "LS_KNOWLEDGE_REVIEW":
                        var BasicApp = function (obj){
                            var self = this;
                            self.homeworkId     = obj.homeworkId;
                            self.tab            = obj.tab;
                            self.tabName        = obj.tabName;
                            self.resultList     = ko.observableArray(obj.resultList || []);
                            self.displayCount   = 6;
                            self.moveCount      = 1;
                            self.initPosition   = 0;
                            self.startPosition  = ko.observable(self.initPosition);
                            self.focusPosition  = ko.observable(self.initPosition);
                            self.displayList    = ko.pureComputed(function(){
                                var self = this;
                                var _tabs = self.resultList();
                                var _startPos = self.startPosition();
                                var _newTabs = [];
                                if(_startPos != null){
                                    if(_tabs.length <= self.moveCount){
                                        _newTabs = _tabs;
                                    }else{
                                        if(_tabs.length - _startPos >= self.displayCount){
                                            _newTabs = _tabs.slice(_startPos,_startPos + self.displayCount);
                                        }else{
                                            _newTabs = _tabs.slice(_startPos);
                                        }
                                    }
                                }
                                return _newTabs;
                            },self);
                            self.focusBasic = ko.pureComputed(function(){
                                return self.resultList()[self.focusPosition()];
                            });
                            self.leftEnabled  = ko.pureComputed(function(){
                                //左箭头是否可用
                                return self.startPosition() > 0;
                            });
                            self.rightEnabled = ko.pureComputed(function(){
                                //右箭头是否可用
                                return (self.startPosition() + self.moveCount) < self.resultList().length;
                            });
                            self.tags = [{
                                key     : "student",
                                name    : "学生得分情况"
                            },{
                                key     : "content",
                                name    : "内容得分情况"
                            }];
                            self.focusTags = ko.observableArray([]);
                            self.focusTagKey = ko.observable("");
                            self.personalStatistics = ko.observableArray([]);
                            self.contentStatistics = ko.observableArray([]);
                        };
                        BasicApp.prototype = {
                            constructor          : BasicApp,
                            arrowClick           : function(directionOfArrow){
                                var self = this;
                                var _startPos = self.startPosition();
                                if(directionOfArrow == "arrowLeft" && self.leftEnabled()){
                                    self.startPosition(_startPos - self.moveCount);
                                }else if(directionOfArrow == "arrowRigth" && self.rightEnabled()){
                                    self.startPosition(_startPos + self.moveCount);
                                }
                            },
                            basicClick           : function(self,index){
                                var basic = this;
                                if(basic.unitId === self.focusBasic().unitId && basic.lessonId === self.focusBasic().lessonId && basic.categoryId === self.focusBasic().categoryId){
                                    return false;
                                }
                                self.stopAudio();
                                self.resetFocusTag("",[],[]);
                                var _newIndex = index + self.startPosition();
                                self.focusPosition(_newIndex);
                                self.setFocusData(_newIndex);
                            },
                            setFocusData         : function(basicIndex){
                                var self = this;
                                var basic = self.resultList()[basicIndex];
                                switch (self.tab){
                                    case "NATURAL_SPELLING":
                                        var focusTag,newTag = [];
                                        if($.isArray(basic.personalStatistics) && basic.personalStatistics.length > 0){
                                            focusTag = "student";
                                        }else if($.isArray(basic.contentStatistics) && basic.contentStatistics.length > 0){
                                            focusTag = "content";
                                        }
                                        $.each(self.tags,function(i,obj){
                                            (obj.key === focusTag) && (newTag.push(obj));
                                        });
                                        self.focusTags(newTag);
                                        self.setFocusTag(basicIndex,focusTag);
                                        break;
                                    default:
                                        if(basic.isNeedRecord){
                                            self.focusTags(self.tags);
                                            self.setFocusTag(basicIndex,self.focusTags()[0].key);
                                        }else{
                                            self.focusTags(self.tags.slice(1));
                                            self.setFocusTag(basicIndex,self.focusTags()[0].key);
                                        }
                                }
                            },
                            setFocusTag     : function(basicIndex,key){
                                var self = this;
                                var basic = self.resultList()[basicIndex];
                                switch(key){
                                    case "student":
                                        self.resetFocusTag(key,basic.personalStatistics || [],[]);
                                        break;
                                    case "content":
                                        self.contentStatistics([]);
                                        var contentArr = basic.contentStatistics || [];
                                        if(contentArr.length > 0){
                                            for(var m = 0,mLen = contentArr.length; m < mLen; m++){
                                                //默认隐藏学生答题结果
                                                contentArr[m]["show"] = (m == 0);
                                            }
                                        }
                                        self.resetFocusTag(key,[],ko.mapping.fromJS(contentArr)());
                                        break;
                                    default:
                                        self.resetFocusTag("",[],[]);
                                }
                            },
                            resetFocusTag   : function(key,personalStatistics,contentStatistics){
                                var self = this;
                                self.focusTagKey(key);
                                self.personalStatistics(personalStatistics);
                                self.contentStatistics(contentStatistics);
                            },
                            tagClick        : function(self){
                                var tagObj = this;
                                self.stopAudio();
                                self.setFocusTag(self.focusPosition(),tagObj.key);
                            },
                            contentClick    : function(){
                                var contentObj = this;
                                contentObj.show(!contentObj.show());
                            },
                            getSentenceText : function(sentenceList){
                                var sentenceText = "";
                                if(!sentenceList){
                                    return sentenceText;
                                }
                                var _sentenceList = ko.mapping.toJS(sentenceList);
                                if(!$.isArray(_sentenceList)){
                                    return sentenceText;
                                }
                                for(var t = 0,tLen = _sentenceList.length; t < tLen; t++){
                                    sentenceText += _sentenceList[t].sentenceContent;
                                }
                                return sentenceText;
                            },
                            init                 : function(){
                                var self = this;
                                var resultList = self.resultList();
                                if(resultList.length > 0){
                                    self.setFocusData(self.focusPosition());
                                }
                            },
                            joinPropertyValue : function(arr,property){
                                if(!$.inArray(arr)){
                                    return ""
                                }
                                if(typeof property !== 'string'){
                                    return $.parseJSON(arr);
                                }
                                arr = ko.mapping.toJS(arr);
                                for(var m = 0,propertyValues = [],mLen = arr.length; m < mLen; m++){
                                    propertyValues.push(arr[m][property]);
                                }
                                return propertyValues.join(",");
                            }
                        };
                        viewModel = new BasicApp({
                            homeworkId   : that.getQuery("homeworkId"),
                            tab          : navName,
                            tabName      : categoryMap[navName],
                            resultList   : subjectInfos[navName] || []
                        });
                        $17.extend(viewModel,viewModel_voice);
                        viewModel.init();
                        break;
                    case "READING":
                    case "LEVEL_READINGS":
                        var Reading = function(obj){
                            var self = this;
                            for(var pr in viewModel){
                                if(viewModel.hasOwnProperty(pr)){
                                    self[pr] = viewModel[pr];
                                }
                            }
                            self.tab               = obj.tab || "";
                            self.tabName           = obj.tabName || "";
                            self.defaultReadingImg = obj.defaultReadingImg || "";
                            self.pictureBookList = ko.observableArray(subjectInfos[navName].pictureBookInfo || []);
                            self.bookStudentsMap = subjectInfos[navName].studentsInfo || {};
                            self.focusStudents   = ko.observableArray(null);
                            self.startPackagePosition = ko.observable(0);
                            self.displayPackageCnt = 4;  //可见区域绘本的个数
                            self.movePackageCnt    = 3;  //一次移动绘本的个数（左右箭头点击）
                            self.focusPackageIndex = ko.observable(0);
                            self.currentPackageList = ko.pureComputed(function(){
                                var self = this;
                                var _tabs = self.pictureBookList();
                                var _startPos = self.startPackagePosition();
                                var _newTabs = [];
                                if(_startPos != null){
                                    if(_tabs.length <= self.movePackageCnt){
                                        _newTabs = _tabs;
                                    }else{
                                        if(_tabs.length - _startPos >= self.displayPackageCnt){
                                            _newTabs = _tabs.slice(_startPos,_startPos + self.displayPackageCnt);
                                        }else{
                                            _newTabs = _tabs.slice(_startPos);
                                        }
                                    }
                                }
                                return _newTabs;
                            },self);
                            self.focusPackage = ko.pureComputed(function(){
                                return self.pictureBookList()[self.focusPackageIndex()];
                            });
                            self.leftEnabled  = ko.pureComputed(function(){
                                //左箭头是否可用
                                // console.info(self.startPackagePosition() > 0);
                                return self.startPackagePosition() > 0;
                            });
                            self.rightEnabled = ko.pureComputed(function(){
                                //右箭头是否可用
                                console.info((self.startPackagePosition() + self.movePackageCnt) < self.pictureBookList().length);
                                return (self.startPackagePosition() + self.movePackageCnt) < self.pictureBookList().length;
                            });

                        };
                        Reading.prototype = {
                            constructor          : Reading,
                            imgsrc               : function(element,imgUrl){
                                var self = this;
                                element.onerror = function () {
                                    element.onerror = '';
                                    element.src = self.defaultReadingImg;
                                };
                                return imgUrl;
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
                            },
                            arrowClick           : function(directionOfArrow){
                                var self = this;
                                var _startPos = self.startPackagePosition();
                                if(directionOfArrow == "arrowLeft" && self.leftEnabled()){
                                    self.startPackagePosition(_startPos - self.movePackageCnt);
                                }else if(directionOfArrow == "arrowRigth" && self.rightEnabled()){
                                    self.startPackagePosition(_startPos + self.movePackageCnt);
                                }
                            },
                            setFocusData         : function(bookIndex){
                                var self = this;
                                var _bookId = self.pictureBookList()[bookIndex].pictureBookId;
                                self.focusStudents([]); //防止ko不更新
                                self.focusStudents(self.bookStudentsMap[_bookId] || []);
                            },
                            bookClick            : function (self,index) {
                                var book = this;
                                if(book.pictureBookId === self.focusPackage().pictureBookId){
                                    return false;
                                }
                                var _newIndex = index + self.startPackagePosition();
                                self.focusPackageIndex(_newIndex);
                                self.setFocusData(_newIndex);
                            },
                            init                 : function(){
                                var self = this;
                                var picBookList = self.pictureBookList();
                                if(picBookList.length > 0){
                                    self.setFocusData(self.focusPackageIndex());
                                }
                            },
                            viewReading : function(tab){
                                var dataHtml,paramObj,gameUrl;
                                if(tab === "LEVEL_READINGS"){
                                    paramObj = {
                                        pictureBookIds  : this.focusPackage().pictureBookId,
                                        from            : "preview"
                                    };
                                    var domain = "/";
                                    if(answerDetailData.env === "test"){
                                        domain = "//www.test.17zuoye.net/";
                                    }else{
                                        domain = location.protocol + "//" + location.host;
                                    }
                                    gameUrl = domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param(paramObj);
                                    dataHtml = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                                    $.prompt(dataHtml, {
                                        title   : "预 览",
                                        buttons : {},
                                        position: { width: 960 },
                                        close   : function(){
                                        }
                                    });
                                }else{
                                    paramObj = {
                                        pictureBookId : this.focusPackage().pictureBookId,
                                        fromModule : ""
                                    };
                                    gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
                                    dataHtml = "";
                                    if(this.focusPackage().keywords && this.focusPackage().keywords.length > 0){
                                        dataHtml += "<div class=\"h-homework-pop\"><div class=\"popTitle\">重点词汇：</div><div class=\"popContent\">"+this.focusPackage().keywords.join(" | ")+"</div></div>";
                                    }
                                    dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                                    $.prompt(dataHtml, {
                                        title   : "预 览",
                                        buttons : {},
                                        position: { width: 960 },
                                        close   : function(){
                                            $('iframe').each(function(){
                                                var win = this.contentWindow || this;
                                                if(win.destroyHomeworkJavascriptObject){
                                                    win.destroyHomeworkJavascriptObject();
                                                }
                                            });
                                        }
                                    });
                                }
                            },
                            playDubbingVideo : function(self){
                                var student  = this;
                                var dataHtml = "";
                                var paramObj = {
                                    dubbingId  : student.dubbingId,
                                    from       : "preview"
                                };

                                if(!student.dubbingId){
                                    return false;
                                }

                                var domain;
                                if(answerDetailData.env === "test"){
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
                        };
                        viewModel = new Reading({
                            defaultReadingImg   : answerDetailData.defaultReadingImg,
                            tab                 : navName,
                            tabName             : categoryMap[navName]
                        });
                        viewModel.init();
                        break;
                    case "NEW_READ_RECITE":
                        $17.extend(viewModel,viewModel_voice);
                        var checkStatus = false,
                            correctIcon = {
                            level01:"阅",
                            level02:"优",
                            level03:"良",
                            level04:"中",
                            level05:"差"
                        };
                        for(var item in subjectInfos[navName]){
                            $.each(subjectInfos[navName][item],function () {

                                this.initQuestions = false;
                                this.showDetail = ko.observable(false);
                                $.each(this.users,function () {

                                    var temp = typeof this.correct_des === "function" ? this.correct_des() : this.correct_des;
                                    this.correct_des = ko.observable(temp);
                                    this.checkHover = ko.observable(false);
                                    this.levelIcon = ko.pureComputed(function() {

                                        for(var name in correctIcon){
                                            if(correctIcon[name] == this.correct_des()){
                                                return name;
                                            };
                                        };
                                        return "";
                                    },this);
                                });
                            });
                        }

                        var result=[{
                            name:"课文朗读",
                            data:subjectInfos[navName].readData || []
                        },{
                            name:"课文背诵",
                            data:subjectInfos[navName].reciteData || []
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
                            checkQuestion : function (name,correction,questionBoxId) {

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
                                        questionId : questionBoxId,
                                        isBatch : false,
                                        corrections: [{
                                            userId: that.userId,
                                            review: true,
                                            correctType: null,
                                            correction: correction == "REVIEW" ? null : correction,
                                            teacherMark: ""
                                        }]
                                    })
                                },function (res) {
                                    checkStatus = false;
                                    if(res.success){

                                        that.correct_des(name);
                                    }else{

                                        $17.alert(res.info || "批改失败，请稍后再试～");
                                    }
                                });

                                $17.voxLog({
                                    module: "m_Odd245xH",
                                    op    : "question_detail_text_readrecite_correct_click"
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
                        var Dubbing = function(obj){
                            var self = this;
                            for(var pr in viewModel){
                                if(viewModel.hasOwnProperty(pr)){
                                    self[pr] = viewModel[pr];
                                }
                            }
                            self.tab               = obj.tab || "";
                            self.tabName           = obj.tabName || "";
                            self.defaultDubbingImg = obj.defaultDubbingImg || "";
                            self.dubbingList = ko.observableArray(subjectInfos[navName].dubbingInfo || []);
                            self.dubbingStudentsMap = subjectInfos[navName].studentInfo || {};
                            self.focusStudents   = ko.observableArray(null);
                            self.startPackagePosition = ko.observable(0);
                            self.displayPackageCnt = 4;  //可见区域绘本的个数
                            self.movePackageCnt    = 3;  //一次移动绘本的个数（左右箭头点击）
                            self.focusPackageIndex = ko.observable(0);
                            self.currentPackageList = ko.pureComputed(function(){
                                var self = this;
                                var _tabs = self.dubbingList();
                                var _startPos = self.startPackagePosition();
                                var _newTabs = [];
                                if(_startPos != null){
                                    if(_tabs.length <= self.movePackageCnt){
                                        _newTabs = _tabs;
                                    }else{
                                        if(_tabs.length - _startPos >= self.displayPackageCnt){
                                            _newTabs = _tabs.slice(_startPos,_startPos + self.displayPackageCnt);
                                        }else{
                                            _newTabs = _tabs.slice(_startPos);
                                        }
                                    }
                                }
                                return _newTabs;
                            },self);
                            self.focusPackage = ko.pureComputed(function(){
                                return self.dubbingList()[self.focusPackageIndex()];
                            });
                            self.leftEnabled  = ko.pureComputed(function(){
                                //左箭头是否可用
                                // console.info(self.startPackagePosition() > 0);
                                return self.startPackagePosition() > 0;
                            });
                            self.rightEnabled = ko.pureComputed(function(){
                                //右箭头是否可用
                                console.info((self.startPackagePosition() + self.movePackageCnt) < self.dubbingList().length);
                                return (self.startPackagePosition() + self.movePackageCnt) < self.dubbingList().length;
                            });

                        };
                        Dubbing.prototype = {
                            constructor          : Dubbing,
                            imgsrc               : function(element,imgUrl){
                                var self = this;
                                element.onerror = function () {
                                    element.onerror = '';
                                    element.src = self.defaultDubbingImg;
                                };
                                return imgUrl;
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
                            },
                            arrowClick           : function(directionOfArrow){
                                var self = this;
                                var _startPos = self.startPackagePosition();
                                if(directionOfArrow == "arrowLeft" && self.leftEnabled()){
                                    self.startPackagePosition(_startPos - self.movePackageCnt);
                                }else if(directionOfArrow == "arrowRigth" && self.rightEnabled()){
                                    self.startPackagePosition(_startPos + self.movePackageCnt);
                                }
                            },
                            setFocusData         : function(bookIndex){
                                var self = this;
                                var _dubbingId = self.dubbingList()[bookIndex].dubbingId;
                                self.focusStudents([]); //防止ko不更新
                                self.focusStudents(self.dubbingStudentsMap[_dubbingId] || []);
                            },
                            dubbingClick            : function (self,index) {
                                var book = this;
                                if(book.dubbingId === self.focusPackage().dubbingId){
                                    return false;
                                }
                                var _newIndex = index + self.startPackagePosition();
                                self.focusPackageIndex(_newIndex);
                                self.setFocusData(_newIndex);
                            },
                            init                 : function(){
                                var self = this;
                                var picBookList = self.dubbingList();
                                if(picBookList.length > 0){
                                    self.setFocusData(self.focusPackageIndex());
                                }
                            },
                            viewDubbing : function(coverUrl,videoUrl,from){
                                var dataHtml = "";
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
                                            flashvars: "file=" + videoUrl + "&amp;image=" + coverUrl + "&amp;width=" + flashWidth + "&amp;height=" + flashHeight + "&amp;autostart=true"
                                        });
                                    }
                                });

                                var logOp;
                                if(from === "student_dubbing_video"){
                                    logOp = "o_oigO8YvD";
                                }else if(from === "dubbing_video"){
                                    logOp = "o_mXflMs6D";
                                }
                                $17.voxLog({
                                    module : "m_Odd245xH",
                                    op     : logOp
                                });
                            }
                        };
                        viewModel = new Dubbing({
                            tab                 : navName,
                            tabName             : categoryMap[navName],
                            defaultDubbingImg : answerDetailData.defaultDubbingImg
                        });
                        viewModel.init();
                        break;
                    case "READ_RECITE_WITH_SCORE":
                        var resultArr = [{
                            name        : "课文朗读",
                            lessonList  : subjectInfos[navName].readData || []
                        },{
                            name        : "课文背诵",
                            lessonList  : subjectInfos[navName].reciteData || []
                        }];
                        $17.extend(viewModel,{
                            result                  : resultArr,
                            playingUserId           : ko.observable(null),
                            forwardStudentDetail    : function(self){
                                //跳到学生详情
                                var userObj = this;
                                var paramObj = {
                                    homeworkId : homework.homeworkId,
                                    studentId  : userObj.userId,
                                    tabType    : navName
                                };
                                $17.voxLog({
                                    module  : "m_Odd245xH",
                                    op      : "ReadReciteScore_ClazzDetail_PersonalDetail_click"
                                });
                                setTimeout(function(){
                                    location.href = "/teacher/new/homework/report/studentreportdetail.vpage?" + $.param(paramObj);
                                },200);
                            },
                            playAudio               : function(self,lesson){
                                var user = this;
                                var uniqueKey = [lesson.questionBoxId,user.userId].join("_");
                                var playingUserIdFn = self.playingUserId;
                                if(uniqueKey != playingUserIdFn()){
                                    playingUserIdFn(uniqueKey);
                                    selfPlayer.playAudio(user.voices,function(){
                                        playingUserIdFn(null);
                                    });
                                }else{
                                    selfPlayer.stopAudio();
                                    playingUserIdFn(null);
                                }
                                $17.voxLog({
                                    module  : "m_Odd245xH",
                                    op      : "	ReadReciteScore_ClazzDetail_play_click"
                                });
                            }
                        });
                        break;
                    case "WORD_RECOGNITION_AND_READING":
                        $17.extend(viewModel,{
                            resultList              : subjectInfos[navName] || [],
                            playingUserId           : ko.observable(null),
                            forwardStudentDetail    : function(self){
                                //跳到学生详情
                                var userObj = this;
                                var paramObj = {
                                    homeworkId : homework.homeworkId,
                                    studentId  : userObj.userId,
                                    tabType    : navName
                                };
                                $17.voxLog({
                                    module  : "m_Odd245xH",
                                    op      : "ReadReciteScore_ClazzDetail_PersonalDetail_click"
                                });
                                setTimeout(function(){
                                    location.href = "/teacher/new/homework/report/studentreportdetail.vpage?" + $.param(paramObj);
                                },200);
                            },
                            playAudio               : function(self,lesson){
                                var user = this;
                                var uniqueKey = [lesson.questionBoxId,user.userId].join("_");
                                var playingUserIdFn = self.playingUserId;
                                if(uniqueKey !== playingUserIdFn()){
                                    playingUserIdFn(uniqueKey);
                                    selfPlayer.playAudio(user.voices,function(){
                                        playingUserIdFn(null);
                                    });
                                }else{
                                    selfPlayer.stopAudio();
                                    playingUserIdFn(null);
                                }
                                $17.voxLog({
                                    module  : "m_Odd245xH",
                                    op      : "	ReadReciteScore_ClazzDetail_play_click"
                                });
                            }
                        });
                        break;
                    case "ORAL_COMMUNICATION":
                        var OralItem = function(obj){
                            var self = this;
                            for(var pr in viewModel){
                                if(viewModel.hasOwnProperty(pr)){
                                    self[pr] = viewModel[pr];
                                }
                            }
                            self.homeworkId        = obj.homeworkId;
                            self.tab               = obj.tab || "";
                            self.tabName           = obj.tabName || "";
                            self.defaultDubbingImg = obj.defaultDubbingImg || "";
                            self.dubbingList = ko.observableArray(subjectInfos[navName].dubbingInfo || []);
                            self.dubbingStudentsMap = subjectInfos[navName].studentInfo || {};
                            self.focusStudents   = ko.observableArray(null);
                            self.startPackagePosition = ko.observable(0);
                            self.displayPackageCnt = 4;  //可见区域绘本的个数
                            self.movePackageCnt    = 3;  //一次移动绘本的个数（左右箭头点击）
                            self.focusPackageIndex = ko.observable(0);
                            self.currentPackageList = ko.pureComputed(function(){
                                var self = this;
                                var _tabs = self.dubbingList();
                                var _startPos = self.startPackagePosition();
                                var _newTabs = [];
                                if(_startPos != null){
                                    if(_tabs.length <= self.movePackageCnt){
                                        _newTabs = _tabs;
                                    }else{
                                        if(_tabs.length - _startPos >= self.displayPackageCnt){
                                            _newTabs = _tabs.slice(_startPos,_startPos + self.displayPackageCnt);
                                        }else{
                                            _newTabs = _tabs.slice(_startPos);
                                        }
                                    }
                                }
                                return _newTabs;
                            },self);
                            self.focusPackage = ko.pureComputed(function(){
                                return self.dubbingList()[self.focusPackageIndex()];
                            });
                            self.leftEnabled  = ko.pureComputed(function(){
                                //左箭头是否可用
                                // console.info(self.startPackagePosition() > 0);
                                return self.startPackagePosition() > 0;
                            });
                            self.rightEnabled = ko.pureComputed(function(){
                                //右箭头是否可用
                                console.info((self.startPackagePosition() + self.movePackageCnt) < self.dubbingList().length);
                                return (self.startPackagePosition() + self.movePackageCnt) < self.dubbingList().length;
                            });

                        };
                        OralItem.prototype = {
                            constructor          : OralItem,
                            imgsrc               : function(element,imgUrl){
                                var self = this;
                                element.onerror = function () {
                                    element.onerror = '';
                                    element.src = self.defaultDubbingImg;
                                };
                                return imgUrl;
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
                            },
                            arrowClick           : function(directionOfArrow){
                                var self = this;
                                var _startPos = self.startPackagePosition();
                                if(directionOfArrow === "arrowLeft" && self.leftEnabled()){
                                    self.startPackagePosition(_startPos - self.movePackageCnt);
                                }else if(directionOfArrow === "arrowRigth" && self.rightEnabled()){
                                    self.startPackagePosition(_startPos + self.movePackageCnt);
                                }
                            },
                            setFocusData         : function(bookIndex){
                                var self = this;
                                var _stoneId = self.dubbingList()[bookIndex].stoneId;
                                self.focusStudents([]); //防止ko不更新
                                self.focusStudents(self.dubbingStudentsMap[_stoneId] || []);
                            },
                            oralItemClick            : function (self,index) {
                                var book = this;
                                if(book.stoneId === self.focusPackage().stoneId){
                                    return false;
                                }
                                var _newIndex = index + self.startPackagePosition();
                                self.focusPackageIndex(_newIndex);
                                self.setFocusData(_newIndex);
                            },
                            init                 : function(){
                                var self = this;
                                var picBookList = self.dubbingList();
                                if(picBookList.length > 0){
                                    self.setFocusData(self.focusPackageIndex());
                                }
                            },
                            viewOralRecord : function(userId,element){
                                var self = this;
                                var stoneId = self.focusPackage().stoneId;
                                if(!userId){
                                    return "javascript:void(0)"
                                }else{
                                    $(element).attr("target","_blank");
                                    return "/teacher/new/homework/report/singleoralcommunicationpackagedetail.vpage?homeworkId=" + that.getQuery("homeworkId") +"&stoneId=" + stoneId;
                                }
                            }
                        };
                        viewModel = new OralItem({
                            homeworkId          : that.getQuery("homeworkId"),
                            tab                 : navName,
                            tabName             : categoryMap[navName],
                            defaultDubbingImg : answerDetailData.defaultDubbingImg
                        });
                        viewModel.init();
                        break;
                    case "WORD_TEACH_AND_PRACTICE":
                        $17.extend(viewModel,{
                            resultList              : subjectInfos[navName] || [],
                            viewSingleExam          : function(questionId){
                                var questionObj = this;
                                var totalNum = questionObj.totalNum || 0,interventionRightNum = questionObj.interventionRightNum || 0;
                                $17.QuestionDB.getQuestionByIds(questionId,function(result){
                                    var allConversation = {
                                        questionDetail: {
                                            title       : '题目详情',
                                            html        : template("t:PREVIEW_QUESTION", {}),
                                            position    : { width: 770},
                                            buttons     : {},
                                            focus       : 1,
                                            submit:function(e,v,m,f){}
                                        }
                                    };
                                    $.prompt(allConversation,{
                                        loaded : function(){
                                            $.prompt.goToState("questionDetail");
                                            ko.applyBindings({
                                                submitNum   : totalNum,
                                                errorNum    : (totalNum - interventionRightNum),
                                                questionId  : questionId,
                                                focusExamMap : result.success ? result.questionMap : {},
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
                                                }
                                            }, document.getElementById("jqistate_questionDetail"));
                                        },
                                        close : function () {}
                                    });
                                });
                            },
                            viewModuleDetail  : function(stoneId,wordTeachModuleType){
                                //字词训练
                                location.href = "/teacher/new/homework/report/clazzwordteachmoduledetail.vpage?" + $.param({
                                    homeworkId  : homework.homeworkId,
                                    stoneId     : stoneId,
                                    wordTeachModuleType : wordTeachModuleType
                                });
                            }
                        });
                        break;
                    default:
                        break;
                }
                ko.applyBindings(viewModel,node);
            }else if(navName == "MENTAL_ARITHMETIC"){
                var html = template("t:MENTAL_ARITHMETIC",{
                    studentList : subjectInfos[navName].calculationStudents || []
                });
                $(".J_mainContentHolder").html(html);
            }else{
                switch (navName){
                    case "READ_RECITE":
                        TPL = templateMap.subjectInfo_READ_RECITE;
                        break;
                    case "PHOTO_OBJECTIVE":
                    case "VOICE_OBJECTIVE":
                        TPL = templateMap.subjectInfo_PHOTO_VOICE;
                        break;
                    case "BASIC_KNOWLEDGE":
                        TPL = templateMap.chineseBase;
                        break;
                    case "CHINESE_READING":
                    default:
                        TPL = templateMap.subjectInfo;
                }

                $.each(subjectInfos[navName],function(){
                    var answers = "";
                    if($.isArray(this.errorAnswerList) && this.errorAnswerList.length > 0){
                        if(this.showType==0){
                            $.each(this.errorAnswerList,function(i,item){
                                var userName="",odd=i%2==0?"odd":"",color=(item.answer == "答案正确")?"txt-green":"";
                                for(var i=0;i<item.users.length;i++){
                                    userName += item.users[i].userName + "，";
                                }
                                answers += that.substitute(templateMap.answers_nopic,{odd:odd,answer:(item.answer || "未作答"),color:color,userName:userName.substring(0,userName.length-1)});
                            });
                        }else if(this.showType==1){
                            $.each(this.errorAnswerList,function(i,item){
                                var picbox="",odd=i%2==0?"odd":"",color=(item.answer == "答案正确")?"txt-green":"";
                                answers += '<div class="questionBox"><ul>';
                                for(var i=0;i<item.users.length;i++){
                                    var icon = iconLevel[item.users[i].correction]?iconLevel[item.users[i].correction]:(item.users[i].review?"icon-yue":"");
                                    var showpic = answerDetailData.defaultPicUrl,hidepic = answerDetailData.defaultPicUrl;
                                    if(item.users[i].showPics.length>0){
                                        showpic = item.users[i].showPics[0];
                                        hidepic = item.users[i].showPics.join(",");
                                    }
                                    picbox += '<li uid="'+item.users[i].userId+'"><p class="name">'+item.users[i].userName+'</p><img allpic="'+hidepic+'" src="'+showpic+'"><span class="icon '+icon+'"></span></li>';
                                }
                                if(navName=="PHOTO_OBJECTIVE"){
                                    answers += picbox;
                                }else{
                                    answers += that.substitute(templateMap.answers_pic,{odd:odd,answer:(item.answer || "未作答"),color:color,picbox:picbox});
                                }
                                answers += "</ul></div>";
                            });
                        }else if(this.showType==2){
                            $.each(this.errorAnswerList,function(){
                                answers += '<div class="h-voiceBox">';
                                for(var i=0;i<this.users.length;i++){
                                    var uid = this.users[i].userId;
                                    var icon = iconLevel[this.users[i].correction]?iconLevel[this.users[i].correction]:(this.users[i].review?"icon-yue":"");
                                    var yueicon =  icon=="icon-yue"?"btn-disabled":"btn-yellow";
                                    var url = this.users[i].showPics.join("|");
                                    answers += that.substitute(templateMap.answer_voice,{uid:uid,url:url,icon:icon,userName:this.users[i].userName,yueicon:yueicon});
                                }
                                answers += "</div>";
                            });
                        }
                    }

                    if(this.rate==0){
                        rate = ['<div class="icon-correct-b icon-b">',
                            '<div class="inner">',
                            '<div class="text">全部正确</div>',
                            '<div class="item"><span class="icon-correct-s"></span></div>',
                            '</div>',
                            '</div>'].join("");
                    }else if( !$17.isBlank(this.rate) ){
                        rate = ['<div class="icon-error-b icon-b">',
                            '<div class="inner">',
                            '<div class="text">失分率</div>',
                            '<div class="item">'+this.rate+'%</div>',
                            '</div>',
                            '</div>'].join("");
                    }

                    contentHtml += that.substitute(TPL,{
                        navType : navName,
                        qid:this.qid,
                        testMethodName:this.testMethodName,
                        contentType:this.contentType,
                        difficulty:this.difficulty==3?"中等":(this.difficulty==4||this.difficulty==5 ? "困难":"容易"),
                        rate:rate,
                        answers:answers,
                        articleName : this.articleName,
                        paragraphCName : this.paragraphCName,
                        answerWay : this.answerWay
                    });
                });
                $(".J_mainContentHolder").html('<div class="w-base-title" style="background-color: #e1f0fc;"><h3 >'+categoryMap[navName]+'</h3></div>'+contentHtml+'</div>');

                $.each(subjectInfos[navName],function(){
                    var node = document.getElementById("subject_" + this.qid);
                    var obj = vox.exam.render(node, 'normal', {
                        ids       : [this.qid],
                        imgDomain : answerDetailData.imgDomain,
                        env       : answerDetailData.env,
                        domain    : answerDetailData.domain
                    });
                    questionHandles[this.qid] = obj;
                });
            }
        },
        initBigPic : function(e){
            var tpl="",iscorrection = false,allPics = $(e).find("img").attr("allpic").split(",");
            var imgFormat = allPics.length==3?"@270w_270h":"@410w_410h";
            var icon = $(e).find("span").attr("class").split(" ")[1];
            var qid=$($(e).parents(".h-set-homework")[0]).attr("qid"),uid=$(e).attr("uid");
            var contains = $(e).parent("ul").find("li");
            $("#showBigPic .title").html("<span>（"+(contains.index($(e))+1)+"/"+contains.length+"）</span>" + $(e).find("p").html());

            $.each(allPics,function(){
                var src = this.indexOf("upflie-img")>-1 ? this : (this + imgFormat);
                tpl += '<li uid="'+uid+'" qid="'+qid+'"><div class="image"><div class="grouper"><img src="'+src+'" draggable="false"></div><div class="rotation" rotate="0"></div></div></li>'
            });
            if(allPics.length==2){
                $("#showBigPic .flex-viewport").addClass("flex-viewport-twoImg").removeClass("flex-viewport-threeImg");
            }else if(allPics.length==3){
                $("#showBigPic .flex-viewport").addClass("flex-viewport-threeImg").removeClass("flex-viewport-twoImg");
            }else{
                $("#showBigPic .flex-viewport").removeClass("flex-viewport-twoImg").removeClass("flex-viewport-threeImg");
            }

            $(".J_checkSubjectRegion span").removeClass("active");
            $(".J_bigPicItem").html(tpl);

            $.each(updateResultList.corrections,function(){
                if(this.userId==$($("#showBigPic .J_bigPicItem li")[0]).attr("uid")){
                    if(this.correction){
                        $(".J_checkSubjectRegion a[type='"+this.correction+"']").parent("span").addClass("active");
                    }else if(this.review){
                        $(".J_checkSubjectRegion a[type='REVIEW']").parent("span").addClass("active");
                    }
                    iscorrection = true;
                    return ;
                }
            });
            if(icon && !iscorrection){
                $.each(iconLevel,function(i,item){
                    if(item==icon){
                        $(".J_checkSubjectRegion a[type='"+i+"']").parent("span").addClass("active");
                        return ;
                    }
                });
            }
        },
        initExamCore : function(){//初始化加载应试
            try{
                vox.exam.create(function(data){
                    if(data.success){
                        //成功
                    }else{
                        $17.voxLog({
                            module: 'vox_exam_create',
                            op:'create_error'
                        });
                        $17.tongji('voxExamCreate','create_error',location.pathname);
                    }
                },false,{
                    imgDomain : answerDetailData.imgDomain,
                    env       : answerDetailData.env,
                    domain    : answerDetailData.domain
                });
            }catch(exception){
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: exception.message,
                    userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
                });
                $17.tongji('voxExamCreate','examCoreJs_error',exception.message);
            }
        },
        getQuery : function(param) {
            var value = location.search.match(new RegExp('[\?\&]' + param + '=([^\&]*)(\&?)', 'i'));
            return value ? value[1] : value;
        },
        updateScore : function(data,fn,item,qid){
            $("body").append('<div class="J_viewPicture-box-mask-text"><div class="t-viewPicture-box-mask"></div><div class="t-viewPicture-box-mask-text">判题保存中，请稍等...</div></div>');
            $.post("/teacher/new/homework/batchcorrectquestion.vpage",{data:JSON.stringify(data)},function(res){
                $(".t-viewPicture-box-mask-text").html(res.info);
                setTimeout(function(){
                    if(typeof fn === "function"){
                        fn(res,item,qid);
                        $(".J_viewPicture-box-mask-text").remove();
                    }else{
                        window.location.reload();
                    }
                },1500);
            });
        },
        sendlog : function(module,op){
            $17.voxLog({
                module : module,
                op : op
            });
        }
};

    return new reportdetail();
}();
