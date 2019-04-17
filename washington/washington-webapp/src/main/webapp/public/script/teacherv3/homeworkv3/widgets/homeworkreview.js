/**
 * @fileoverview
 * @Depend  constantObj.XXX
 * @demo
 * var preview = new homeworkReview().initialise();
 OR
 preview.initialise();
 *
 **/
// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2016-07-02 08:09:04.423
Date.prototype.Format = function(fmt){
    var o = {
        "M+" : this.getMonth()+1,                //月份
        "d+" : this.getDate(),                    //日
        "h+" : this.getHours(),                  //小时
        "m+" : this.getMinutes(),                //分
        "s+" : this.getSeconds(),                //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S"  : this.getMilliseconds()            //毫秒
    };
    if(/(y+)/.test(fmt))
    fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
};

var homeworkReview = (function(constantObj){
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
    var displayClazzGroupOfTabTypes = ['FALLIBILITY_QUESTION','KNOWLEDGE_REVIEW',"RW_KNOWLEDGE_REVIEW","LS_KNOWLEDGE_REVIEW"];
    var carts = $17.homeworkv3.getCarts();
    var groupList = []; // 只在displayClazzGroupOfTabTypes中包含的类型下的班级组名称列表
    var _initDom = function(){
        var $content = $("#reviewhomework .w-base");
        var newGroupList = [];
        for(var key in constantObj._reviewQuestions){
            var includeTabType = (displayClazzGroupOfTabTypes.indexOf(key) != -1);
            if(!includeTabType && constantObj._reviewQuestions[key].length > 0){
                switch (key){
                    case "EXAM":
                    case "WORD_PRACTICE":
                    case "LISTEN_PRACTICE":
                    case "ORAL_PRACTICE":
                    case "BASIC_KNOWLEDGE":
                    case "CHINESE_READING":
                    case "INTELLIGENCE_EXAM":
                    case "KEY_POINTS":
                    case "INTERESTING_PICTURE":
                    case "INTELLIGENT_TEACHING":
                    case "ORAL_INTELLIGENT_TEACHING":
                        _initEXAM($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "MENTAL":
                    case "MENTAL_ARITHMETIC":
                    case "CALC_INTELLIGENT_TEACHING":
                        _initMENTAL($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "UNIT_QUIZ":
                    case "MID_QUIZ":
                    case "END_QUIZ":

                        _initQuiz($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "PHOTO_OBJECTIVE":
                    case "VOICE_OBJECTIVE":

                        _initPHOTOANDVOICE($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "READ_RECITE":

                        _initREADRECITE($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "BASIC_APP":
                        _initBASICAPP($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "READING":
                        _initREADING($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "LEVEL_READINGS":
                        _initLEVEL_READINGS($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "NEW_READ_RECITE":
                        _initNEWREADRECITE($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "READ_RECITE_WITH_SCORE":
                        _initREADRECITEWITHSCORE($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "NATURAL_SPELLING":
                        _initNATURALSPELLING($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "DUBBING":
                    case "DUBBING_WITH_SCORE":
                        _initDUBBING($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "OCR_MENTAL_ARITHMETIC":
                        _initOcrMentalArithmetic($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "WORD_RECOGNITION_AND_READING":
                        _initWORD_RECOGNITION_AND_READING($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "ORAL_COMMUNICATION":
                        _initORAL_COMMUNICATION($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "WORD_TEACH_AND_PRACTICE":
                        _initWORD_TEACH_AND_PRACTICE($content,key,constantObj._reviewQuestions[key]);
                        break;
                    case "DICTATION":
                        _initDICTATION($content,key,constantObj._reviewQuestions[key]);
                        break;
                    default:
                        break;
                }
            }else if(includeTabType){
                var zIndex,questions = constantObj._reviewQuestions[key];
                for(var t = 0,tLen = questions.length; t < tLen; t++){
                    zIndex = -1;
                    for(var m = 0,mLen = newGroupList.length; m < mLen; m++){
                        if(newGroupList[m].groupId == questions[t].groupId){
                            zIndex = m;
                            break;
                        }
                    }
                    if(zIndex == -1){
                        var newClazz = {
                            groupId : questions[t].groupId,
                            groupName : questions[t].groupName
                        };
                        newGroupList.push(newClazz);
                    }
                }
            }
        }
        groupList = newGroupList;
        redrawClazzTabType(groupList);
    };

    var redrawClazzTabType = function(groupList){
        var $content = $("#reviewhomework .w-base");
        $content.find(".gradeSelect-label").remove();
        for(var t = 0,tLen = displayClazzGroupOfTabTypes.length; t < tLen;t++){
            $content.find("." + displayClazzGroupOfTabTypes[t]).remove();
        }
        //对按班级预览的区域进行重绘
        if(!$.isArray(groupList) || groupList.length == 0){
            return false;
        }
        var focusGroupId,focusGroupName;
        for(var z = 0,zLen = displayClazzGroupOfTabTypes.length; z < zLen;z++){
            if(z == 0){
                focusGroupId = groupList[0].groupId;
                focusGroupName = groupList[0].groupName;
                _initClazzInfo($content,groupList,focusGroupId);
            }
            var tabType = displayClazzGroupOfTabTypes[z];
            var questionArr = constantObj._reviewQuestions[tabType];
            var newQuestions = _filterQuestionsByGroupId(questionArr,focusGroupId);
            if(newQuestions.length > 0){
                switch (tabType){
                    case "LS_KNOWLEDGE_REVIEW":
                        _initBASICAPP($content,tabType,newQuestions,focusGroupName);
                        break;
                    default:
                        _initEXAM($content,tabType,newQuestions,focusGroupName);
                }
            }
        }
    };

    var _initClazzInfo = function($content,groupList,focusGroupId){
        $content.append(template("T:CLAZZ_TYPE_REVIEW",{
            data         : groupList,
            focusGroupId : focusGroupId
        }));
    };
    var _filterQuestionsByGroupId = function(questions,groupId){
        var newQuestions = [];
        if(!$.isArray(questions)){
            return newQuestions;
        }
        for(var m = 0,mLen = questions.length; m < mLen; m++){
            if(questions[m].groupId == groupId){
                newQuestions.push(questions[m]);
            }
        }
        return newQuestions;
    };

    var _initQuiz = function($content,type,data){
        var paperId = data[0].paperId,paperName = data[0].paperName,paperSource = data[0].paperSource,totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);
        $.each(data,function(i){
            if(i != 0 && paperId != this.paperId){
                paperName = "自组试卷-"+ (new Date()).Format("yyyyMMdd");
                paperSource = $uper.userName;
                return false;
            }
        });

        $content.append(template("T:QUIZ_REVIEW",{
            paperName: paperName,
            paperSource: paperSource,
            typeName: "配套试卷",
            type:type,
            totalTime: totalTime,
            data: data
        }));

        var questions = data.slice(0,3);
        _initSubject(questions,type);
    }

    var _initEXAM = function($content,type,data,focusGroupName){
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);
        var typeName = "";
        switch (type){
            case "LISTEN_PRACTICE":
                typeName = "听力练习";
                break;
            case "WORD_PRACTICE":
                typeName = "生字词练习";
                break;
            case "EXAM":
                typeName = "同步习题";
                break;
            case "ORAL_PRACTICE":
                typeName = "口语习题";
                break;
            case "BASIC_KNOWLEDGE":
                typeName = "基础知识";
                break;
            case "CHINESE_READING":
                typeName = "阅读";
                break;
            case "INTELLIGENCE_EXAM":
                typeName = "同步习题";
                break;
            case "KNOWLEDGE_REVIEW":
                typeName = (focusGroupName ? focusGroupName : "") + "查缺补漏";
                break;
            case "FALLIBILITY_QUESTION":
                typeName = (focusGroupName ? focusGroupName : "") + "高频错题";
                break;
            case "KEY_POINTS":
                typeName = "重难点视频专练";
                break;
            case "INTERESTING_PICTURE":
                typeName = "趣味绘本";
                break;
            case "RW_KNOWLEDGE_REVIEW":
                typeName = "读写查缺补漏";
                break;
            case "INTELLIGENT_TEACHING":
                typeName = constantObj.subject === "MATH" ? "课时讲练测" : "重点讲练测";
                break;
            case "ORAL_INTELLIGENT_TEACHING":
                typeName = "口语讲练测";
                break;
            default :
                typeName = "同步习题";
        }

        $content.append(template("T:EXAM_REVIEW",{
            typeName: typeName,
            totalTime: totalTime,
            type:type,
            data: data
        }));

        var questions = data.slice(0,3);
        _initSubject(questions,type);
    }

    var _initREADRECITE = function($content,type,data){
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);

        $content.append(template("T:READ_RECITE_REVIEW",{
            totalTime: totalTime,
            type:type,
            data: data
        }));
        var questions = data.slice(0,3);
        _initSubject(questions,type);
    };

    var  sortObj = function (obj, name) {
        var arr = [];
        for(var i = 0; i < obj.length; i++){

            if(arr.length==0){
                arr.push(obj[i]);
            }else{
                for(var j = 0; j < arr.length; j++){

                    if(j==0 && obj[i][name] < arr[j][name]){

                        arr.splice(j,0,obj[i]);
                    } else if(j+1 == arr.length && obj[i][name] > arr[j][name]){

                        arr.push(obj[i]);
                        break;
                    }else if(obj[i][name] > arr[j][name] && obj[i][name] < arr[j+1][name]) {

                        arr.splice(j+1,0,obj[i]);
                        break;
                    };
                }
            }
        }

        return arr;
    };

    var _initNEWREADRECITE = function($content,type,data){
        var recite = [],
            read   = [];

        $.each(data,function () {

            //this.sortQuestions = sortObj(this.questions,"paragraphNumber");
            this.sortQuestions = sortObj(this.questions,"sectionNumber");
            this.initQuestions = false;
            this.showDetail = ko.observable(false);
            this.selectchaper = ko.observable("");

            var selectchaper = "";
            $.each(this.sortQuestions,function () {
                selectchaper += this.paragraphNumber + ",";
            });

            if(this.questionBoxType == "READ"){
                this.selectchaper("朗读段落：第"+selectchaper.slice(0,-1) + "段");
                read.push(this);
            }else{
                this.selectchaper("背诵段落：第"+selectchaper.slice(0,-1) + "段");
                recite.push(this);
            };
        });

        $content.append(template("T:NEW_READ_RECITE_REVIEW",{}));

        ko.applyBindings({
            selData:[{
                name : "课文朗读",
                data : read
            }, {
                name : "课文背诵",
                data : recite
            }],
            showDetail : function(){

                if(!this.initQuestions){
                    this.initQuestions = true;
                    _initSubject(this.sortQuestions,type);
                }
                this.showDetail(!this.showDetail());
            }
        },document.getElementById('newReadReciteTPL'));

    };

    var _initREADRECITEWITHSCORE = function($content,type,data){
        var recite = [],
            read   = [];

        $.each(data,function () {
            this.sortQuestions = $.extend(true,[],this.questions);
            this.sortQuestions.sort(function(a1,a2){
                var s1 = (+a1["sectionNumber"] || 999);
                var s2 = (+a2["sectionNumber"] || 999);
                if(s1 < s2) return -1;
                if(s1 > s2) return 1;
                
                var p1 = (+a1["paragraphNumber"] || 999);
                var p2 = (+a2["paragraphNumber"] || 999);
                if(p1 < p2) return -1;
                if(p1 > p2) return 1;
                
                return 0;
            });
            this.initQuestions = false;
            this.showDetail = ko.observable(false);
            this.selectchaper = ko.observable("");

            var selectchaper = [];
            $.each(this.sortQuestions,function () {
                selectchaper.push(this.paragraphNumber);
            });
            var selectchaperDesc = "";
            if(selectchaper.length < 5){
                selectchaperDesc = selectchaper.join(',')
            }else{
                selectchaperDesc = selectchaper.slice(0,5).join(',') + '...';
            }
            if(this.questionBoxType == "READ"){
                this.selectchaper("朗读段落：第" + selectchaperDesc + "段" + (selectchaper.length > 5 ? ", 共" + selectchaper.length + "段" : ""));
                read.push(this);
            }else{
                this.selectchaper("背诵段落：第" + selectchaperDesc + "段" + (selectchaper.length > 5 ? ", 共" + selectchaper.length + "段" : ""));
                recite.push(this);
            };
        });

        $content.append(template("T:READ_RECITE_WITH_SCORE_REVIEW",{}));

        ko.applyBindings({
            playingQuestionId : ko.observable(null),
            selData:[{
                name : "课文朗读",
                data : read
            }, {
                name : "课文背诵",
                data : recite
            }],
            showDetail : function(){

                if(!this.initQuestions){
                    this.initQuestions = true;
                    _initSubject(this.sortQuestions,type);
                }
                this.showDetail(!this.showDetail());
            },
            playAudio : function(self){
                var question = this;
                var audio = ko.unwrap(question.listenUrls);
                if($17.isBlank(audio)){
                    return false;
                }
                if(typeof audio === "string"){
                    audio = [audio];
                }
                var questionId = ko.unwrap(question.id);
                var playingQuestionIdFn = self.playingQuestionId;
                if(playingQuestionIdFn() == questionId){
                    selfPlayer.stopAudio();
                    playingQuestionIdFn(null);
                }else{
                    playingQuestionIdFn(questionId);
                    selfPlayer.playAudio(audio,function(){
                        playingQuestionIdFn(null);
                    });

                    $17.voxLog( {
                        module : "m_H1VyyebB",
                        op     : "PreviewHomework_play_click"
                    });

                }
            }
        },document.getElementById('ReadReciteWithScoreTPL'));

    };

    var _initMENTAL = function($content,type,data){
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);
        var questionIds = [];
        $.each(data,function(){
            // this.questionContent = this.questionContent.replace("__$$__","( )");
            questionIds.push(this.questionId);
        });
        var resultMap = {};
        var tipMessage = '<div class="hPreview-main"><div class="hp-title"><h3>口算练习</h3></div>__$$__</div>';
        $.get("/exam/flash/load/newquestion/byids.vpage",{
            data : JSON.stringify({ids: questionIds,containsAnswer:false})
        }).done(function(res){
            if(res.success){
                var result = res.result;
                for(var m = 0,mLen = result.length; m < mLen; m++){
                    resultMap[result[m].id] = result[m];
                }

                $content.append(template("T:MENTAL_REVIEW",{
                    totalTime   : totalTime,
                    type        : type,
                    typeName    : type === "CALC_INTELLIGENT_TEACHING" ? "计算讲练测":"口算练习",
                    data        : data
                }));

                for(var k = 0,kLen = data.length; k < kLen; k++){
                    var questionId = data[k].questionId;
                    var question = resultMap[questionId];
                    if(question){
                        var containerId = "#HR-" + questionId + "-" + k;
                        //config配置里的参数，可以抽象出来作为属性由父组件传进来，注意兼容性和扩展性。
                        var config = {
                            container: containerId, //容器的id，（必须）
                            formulaContainer:'#reviewhomework', //公式渲染容器（必须）
                            questionList: [question], //试题数组，包含完整的试题json结构， （必须）
                            framework: {
                                vue: Vue, //vue框架的外部引用
                                vuex: Vuex //vuex框架的外部引用
                            },
                            showAnalysis: false, //是否展示解析
                            showUserAnswer: false, //是否展示用户答案
                            showRightAnswer: false, //是否展示正确答案
                            startIndex : 0 //从第几题开始
                        };
                        try{
                            Venus.init(config);
                        }catch (e){
                            $17.info(e.message);
                        }

                    }
                }
            }else{
                $content.append(tipMessage.replace("__$$__","加载数据失败"));
            }
        }).fail(function(e){
            $content.append(tipMessage.replace("__$$__","未连接到网络"));
        });
    };

    var _initPHOTOANDVOICE = function($content,type,data){
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);

        $content.append(template("T:PHOTO_VOICE_REVIEW",{
            typeName: type == "PHOTO_OBJECTIVE"?"动手做一做":"概念说一说",
            totalTime: totalTime,
            type:type,
            data: data
        }));
        var questions = data.slice(0,3);
        _initSubject(questions,type);
    };

    var _initBASICAPP = function($content,type,data,groupName){
        var param = [], totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);
        var typeName;
        switch (type){
            case "LS_KNOWLEDGE_REVIEW":
                typeName = "听说查缺补漏";
                break;
            default:
                typeName = "基础练习";
        }
        $.each(data,function(){
            var that = this, isNewLesson = true;
            that.seconds = 0;
            $.each(that.practices[0].questions,function(){
                that.seconds += this.seconds;
            });

            $.each(param,function(){
                if(that.lessonId==this.lessonId){
                    isNewLesson = false;
                    this.items.push(that);
                    return false;
                }
            });
            if(isNewLesson){
                param.push({
                    lessonId:that.lessonId,
                    lessonName:that.lessonName,
                    sentences:that.sentences,
                    groupId : that.groupId,
                    groupName : that.groupName,
                    items:[that]
                });
            }
        });

        $content.append(template("T:BASIC_APP_REVIEW",{
            categoryIconPrefixUrl:constantObj.categoryIconPrefixUrl,
            totalTime: totalTime,
            type:type,
            typeName : typeName,
            data: param
        }));

    };

    var _initNATURALSPELLING = function($content,type,data){
        var param = [], totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);
        var typeName;
        switch (type){
            case "NATURAL_SPELLING":
            default:
                typeName = "自然拼读";
        }

        var ctGroups = [];
        $.each(data,function(){
            var ctGroup = this;

            ctGroup.seconds = 0;
            $.each(ctGroup.practices[0].questions,function(){
                ctGroup.seconds += this.seconds;
            });

            var isNewCategoryGroup = true;
            $.each(ctGroups,function(){
                if(ctGroup.categoryGroupId == this.categoryGroupId){
                    isNewCategoryGroup = false;
                    this.categories.push(ctGroup);
                    return false;
                }
            });

            if(isNewCategoryGroup){
                ctGroups.push({
                    lessonId:ctGroup.lessonId,
                    lessonName:ctGroup.lessonName,
                    categoryGroupId : ctGroup.categoryGroupId,
                    sentences:ctGroup.sentences,
                    categories:[ctGroup],
                    newLine : ctGroup.newLine || false
                });
            }
        });

        $.each(ctGroups,function(){
            var that = this, isNewLesson = true;

            $.each(param,function(){
                if(that.lessonId == this.lessonId){
                    isNewLesson = false;
                    this.categoryGroups.push(that);
                    return false;
                }
            });
            if(isNewLesson){
                param.push({
                    lessonId:that.lessonId,
                    lessonName:that.lessonName,
                    categoryGroups:[that]
                });
            }
        });

        $content.append(template("T:NATURAL_SPELLING_REVIEW",{
            categoryIconPrefixUrl   : constantObj.categoryIconPrefixUrl,
            totalTime               : totalTime,
            type                    : type,
            typeName                : typeName,
            data                    : param
        }));
    };

    var _initREADING = function($content,type,data){
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);

        $content.append(template("T:READING_REVIEW",{
            totalTime: totalTime,
            type:type,
            data: data
        }));
    };
    
    var _initLEVEL_READINGS = function ($content,type,data) {
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);

        $content.append(template("T:LEVEL_READINGS_REVIEW",{
            totalTime: totalTime,
            type:type,
            data: data
        }));
    };

    var _initDUBBING = function ($content, type, data) {
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);

        $content.append(template("T:DUBBING_REVIEW",{
            totalTime: totalTime,
            type:type,
            data: data
        }));
    };

    var _initORAL_COMMUNICATION = function($content, type, data){
        var totalTime = Math.ceil(constantObj._moduleSeconds[type]/60);

        $content.append(template("T:ORAL_COMMNUNICATION_REVIEW",{
            totalTime   : totalTime,
            type        : type,
            data        : data
        }));
    };

    var _initOcrMentalArithmetic = function($content, type, data){
        $content.append(template("T:OCR_MENTAL_REVIEW",{}));

        ko.applyBindings({
            type : type,
            workBook : data[0],
            deleteWorkBook : function(){
                $(".J_UFOInfo p[type='"+type+"']").find("i.J_delete").trigger("click");
            }
        },document.getElementById('OCR_MENTAL_REVIEW_TPL'));
    };

    var _initWORD_RECOGNITION_AND_READING = function($content, type, data){
        $content.append(template("T:WORD_COGNITION_AND_READING_REVIEW",{
            packageList : data,
            type : type
        }));
    };

    var _initWORD_TEACH_AND_PRACTICE = function($content, type, data){
        //按sectionName 分组,结构参考content.vpage接口
        var content = [];
        var sectionMap = {};  //section对象在content中的位置映射关系
        for(var m = 0,mLen = data.length; m < mLen; m++){
            if(sectionMap.hasOwnProperty(data[m].id)){
                content[sectionMap[data[m].id]].stoneData.push(data[m]);
            }else{
                var selectModules = data[m].selectModules;
                var totalSeconds = 0;
                for(var k = 0,kLen = selectModules.length; k < kLen; k++ ) {
                    var moduleObj;
                    var wordsPractice = data[m].wordsPractice;
                    switch (selectModules[k]) {
                        case "CHINESECHARACTERCULTURE":
                            moduleObj = wordsPractice.chineseCharacterCultureMap;
                            break;
                        case "IMAGETEXTRHYME":
                            moduleObj = wordsPractice.imageTextMap;
                            break;
                        case "WORDEXERCISE":
                            moduleObj = wordsPractice.wordExerciseMap;
                            break;
                    }
                    totalSeconds += (moduleObj ? moduleObj.seconds : 0);
                }
                data[m].seconds = totalSeconds;
                content.push({
                    id : data[m].id,
                    sectionName : data[m].sectionName,
                    stoneData : [data[m]]
                });
                sectionMap[data[m].id] = content.length - 1;
            }
        }
        $content.append(template("T:WORD_TEACH_AND_PRACTICE_PREVIEW",{
            sectionList : content,
            type : type
        }));
    };

    var _initDICTATION = function($content, type, data){
        //按lessonId 分组,结构参考content.vpage接口
        var content = [];
        var sectionMap = {};  //lesson对象在content中的位置映射关系
        var seconds = 0; //总时长(秒)
        for(var m = 0,mLen = data.length; m < mLen; m++){
            seconds += data[m].seconds || 0;
            if(sectionMap.hasOwnProperty(data[m].lessonId)){
                content[sectionMap[data[m].lessonId]]["seconds"] += data[m].seconds || 0;
                content[sectionMap[data[m].lessonId]].questions.push(data[m]);
            }else{
                content.push({
                    lessonId    : data[m].lessonId,
                    lessonName  : data[m].lessonName,
                    seconds     : data[m].seconds || 0,
                    questions   : [data[m]]
                });
                sectionMap[data[m].lessonId] = content.length - 1;
            }
        }
        $content.append(template("t:DICTATION_PREVIEW",{
            lessonList : content,
            type : type,
            seconds : seconds,
            title : (constantObj._homeworkContent.practices[type].ocrDictation ? "纸质拍照听写" : "线上键盘听写")
        }));


    };

    var _initSubject = function(data,type){

        $.each(data,function(){
            var qid = this.id || this.questionId;
            var questionElementId = "reviewImg_" + type + '_' + qid;
            var $mathExamImg = $("#review_" + type + '_' + qid);
            $mathExamImg.empty().parents(".h-set-homework").show();
            $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id",questionElementId).appendTo($mathExamImg);
            var node = document.getElementById(questionElementId);
            vox.exam.render(node, 'normal', {
                ids       : [qid],
                imgDomain : constantObj.imgDomain,
                env       : constantObj.env,
                domain    : constantObj.domain,
                objectiveConfigType : type
            });
        });
    };

    var _bind = function(carts){
        $(document).on("click",".J_clazzTabType",function(){
            var $this = $(this);
            if($this.hasClass("active")){
                return false;
            }
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "homework_Pre-view_class_click",
                s0     : constantObj.subject
            });
            var $content = $("#reviewhomework .w-base");
            for(var t = 0,tLen = displayClazzGroupOfTabTypes.length; t < tLen;t++){
                $content.find("." + displayClazzGroupOfTabTypes[t]).remove();
            }

            var focusGroupId = +$this.attr("data-groupId"),
                focusGroupName = $this.text();
            for(var z = 0,zLen = displayClazzGroupOfTabTypes.length; z < zLen;z++){
                var questionArr = constantObj._reviewQuestions[displayClazzGroupOfTabTypes[z]];
                var newQuestions = _filterQuestionsByGroupId(questionArr,focusGroupId);
                if(newQuestions.length > 0){
                    switch (displayClazzGroupOfTabTypes[z]){
                        case "LS_KNOWLEDGE_REVIEW":
                            _initBASICAPP($content,displayClazzGroupOfTabTypes[z],newQuestions,focusGroupName);
                            break;
                        default:
                            _initEXAM($content,displayClazzGroupOfTabTypes[z],newQuestions,focusGroupName);
                    }
                }
            }
            $(this).addClass("active").siblings().removeClass("active");
        });

        $(document).on("click",".J_subjectRemove",function(){
            var $this = $(this);
            var qid = $this.attr("qid"),
                seconds = $this.attr("seconds"),
                type = $this.attr("category");
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "page_preview_deselect_click",
                s0     : constantObj.subject,
                s1     : type,
                s3     : qid
            });

            if(type == "MENTAL" || type == "MENTAL_ARITHMETIC"){
                constantObj._homeworkContent.practices[type].questions = [];
                constantObj._reviewQuestions[type] = [];
                constantObj._moduleSeconds[type] = 0;
                $this.parents(".hPreview-main").remove();

            }else if(["NEW_READ_RECITE","READ_RECITE_WITH_SCORE"].indexOf(type) != -1){
                var qBoxid = $this.attr("qBoxid");

                $.each(constantObj._homeworkContent.practices[type].apps,function (i) {
                    var item = this;
                    if(item.questionBoxId == qBoxid){

                        $.each(item.questions,function(j){
                            if(this.questionId == qid){

                                item.questions.splice(j,1);
                                return false;
                            };
                        });

                        if(item.questions.length == 0){
                            constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        }

                        return false;
                    }
                });

                $.each(constantObj._reviewQuestions[type],function (i) {
                    var item = this;
                    if(item.questionBoxId == qBoxid){

                        $.each(item.questions,function(j){
                            if(this.id == qid){

                                item.questions.splice(j,1);
                                return false;
                            };
                        });

                        $.each(item.sortQuestions,function(j){
                            if(this.id == qid){

                                item.sortQuestions.splice(j,1);
                                return false;
                            };
                        });

                        if(item.questions.length == 0){
                            constantObj._reviewQuestions[type].splice(i,1);
                        }

                        return false;
                    };
                });

                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".readInner").siblings(".readInner").length > 0){
                    var temp = $this.parents(".r-inner").find(".textGray"),
                        selectchaper = temp.html().slice(0,5) + "第";

                    $this.parents(".readInner").siblings(".readInner").each(function () {
                        selectchaper += $(this).attr("pnumber") + ",";
                    });

                    temp.html(selectchaper.slice(0,-1)+"段");

                    $this.parents(".readInner").remove();
                }else{

                    if($this.parents(".r-inner").siblings(".r-inner").length > 0){

                        $this.parents(".r-inner").remove();
                    }else{
                        if($this.parents(".aDetails-section").siblings(".aDetails-section").length > 0){

                            $this.parents(".aDetails-section").remove();
                        }else{
                            $this.parents(".read-aDetails").remove();
                        }
                    }
                }

                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "review_text_readrecite_delete_click"
                });

            }else if(type == "BASIC_APP"){
                var categoryId = $this.parents("li").attr("categoryId"),lessonId = $this.parents("li").attr("lessonId");
                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.categoryId == categoryId && this.lessonId==lessonId){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.categoryId == categoryId && this.lessonId==lessonId){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents("li").siblings("li").length > 0){
                    $this.parents("li").remove();
                }else{
                    if($this.parents(".e-lessonsList").siblings(".e-lessonsList").length > 0){
                        $this.parents(".e-lessonsList").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }
            }else if(type == "NATURAL_SPELLING"){
                var categoryId = $this.parents("li").attr("categoryId"),lessonId = $this.parents("li").attr("lessonId");
                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.categoryId == categoryId && this.lessonId==lessonId){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.categoryId == categoryId && this.lessonId==lessonId){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents("li").siblings("li").length > 0){
                    $this.parents("li").remove();
                }else{
                    var $elList = $this.closest(".el-list");
                    var ctGroupId = $elList.attr("ctgroupid");
                    if($elList.siblings(".el-list").length > 0){
                        $elList.siblings(".el-name[ctgroupid='" + ctGroupId + "']").remove();
                        $elList.remove();
                    }else{
                        if($this.parents(".e-lessonsList").siblings(".e-lessonsList").length > 0){
                            $this.parents(".e-lessonsList").remove();
                        }else{
                            $this.parents(".hPreview-main").remove();
                        }
                    }
                }
            }else if(type == "READING" || type == "LEVEL_READINGS"){
                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.pictureBookId == qid){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.pictureBookId == qid){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".e-pictureList").siblings(".e-pictureList").length > 0){
                    $this.parents(".e-pictureList").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }
            }else if(type === "WORD_RECOGNITION_AND_READING"){
                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.questionBoxId === qid){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.questionBoxId === qid){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".e-pictureList").siblings(".e-pictureList").length > 0){
                    $this.parents(".e-pictureList").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }
            }else if(type == "DUBBING" || type == "DUBBING_WITH_SCORE"){
                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.dubbingId == qid){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.dubbingId == qid){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".e-pictureList-2").siblings(".e-pictureList-2").length > 0){
                    $this.parents(".e-pictureList-2").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }
            }else if(type === "ORAL_COMMUNICATION"){
                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.oralCommunicationId == qid){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.oralCommunicationId == qid){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".e-pictureList-2").siblings(".e-pictureList-2").length > 0){
                    $this.parents(".e-pictureList-2").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }
            }else if(type == "KEY_POINTS"){
                $.each(constantObj._homeworkContent.practices[type].apps,function(i,videoEntity){
                    $.each(videoEntity.questions,function(j,question){
                        if(this.questionId == qid){
                            constantObj._homeworkContent.practices[type].apps[i]["questions"].splice(j,1);
                            return false;
                        }
                    });
                    videoEntity.questions.length == 0 && (constantObj._homeworkContent.practices[type].apps.splice(i,1));
                });

                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.id == qid){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".h-set-homework").siblings(".h-set-homework").length > 0){
                    $this.parents(".h-set-homework").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }

            }else if(type === "WORD_TEACH_AND_PRACTICE"){
                var stoneDataId = $this.attr("stonedataid");

                $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                    if(this.stoneDataId == stoneDataId){
                        constantObj._homeworkContent.practices[type].apps.splice(i,1);
                        return false;
                    }
                });
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.stoneDataId == stoneDataId){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".J_packet").siblings(".J_packet").length > 0){
                    $this.parents(".J_packet").remove();
                }else{
                    if($this.parents(".J_sectionBox").siblings(".J_sectionBox").length > 0){
                        $this.parents(".J_sectionBox").remove();
                    }else{
                        if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                            $this.parents(".hPreview-main").remove();
                        }else{
                            $this.parents(".hPreview-main").remove();
                        }
                    }
                }
            }else if(type === "DICTATION"){
                var lessonId = $this.attr("lessonid");

                var newQuestions = [];
                $.each(constantObj._homeworkContent.practices[type].questions,function(i){
                    if(this.lessonId != lessonId){
                        newQuestions.push(this);
                    }
                });
                constantObj._homeworkContent.practices[type].questions = newQuestions;

                if(constantObj._homeworkContent.practices[type].questions.length === 0){
                    constantObj._homeworkContent.practices[type].ocrDictation = null;
                }

                var newReviewQuestions = [];
                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.lessonId != lessonId){
                        newReviewQuestions.push(this);
                    }
                });
                constantObj._reviewQuestions[type] = newReviewQuestions;
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".lesson-dictation").siblings(".lesson-dictation").length > 0){
                    $this.parents(".lesson-dictation").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }

            }else if(displayClazzGroupOfTabTypes.indexOf(type) != -1){
                var questionGroupId = $this.attr("groupid");
                if(type === "LS_KNOWLEDGE_REVIEW"){
                    var deletingCategoryId = $this.parents("li").attr("categoryId"),deletingLessonId = $this.parents("li").attr("lessonId");
                    $.each(constantObj._homeworkContent.practices[type].apps,function(i){
                        if(this.groupId == questionGroupId && this.categoryId == deletingCategoryId && this.lessonId == deletingLessonId){
                            constantObj._homeworkContent.practices[type].apps.splice(i,1);
                            return false;
                        }
                    });

                    $.each(constantObj._reviewQuestions[type],function(i){
                        if(this.groupId == questionGroupId && this.categoryId == deletingCategoryId && this.lessonId==deletingLessonId){
                            constantObj._reviewQuestions[type].splice(i,1);
                            return false;
                        }
                    });
                    constantObj._moduleSeconds[type] -= seconds;

                    if($this.parents("li").siblings("li").length > 0){
                        $this.parents("li").remove();
                    }else{
                        if($this.parents(".e-lessonsList").siblings(".e-lessonsList").length > 0){
                            $this.parents(".e-lessonsList").remove();
                        }else{
                            $this.parents(".hPreview-main").remove();
                        }
                    }
                }else{
                    $.each(constantObj._homeworkContent.practices[type].questions,function(i){
                        if((this.id == qid || this.questionId==qid) && this.groupId == questionGroupId){
                            constantObj._homeworkContent.practices[type].questions.splice(i,1);
                            return false;
                        }
                    });
                    $.each(constantObj._reviewQuestions[type],function(i){
                        if((this.id == qid || this.questionId==qid) && this.groupId == questionGroupId){
                            constantObj._reviewQuestions[type].splice(i,1);
                            return false;
                        }
                    });
                    constantObj._moduleSeconds[type] -= seconds;

                    if($this.parents(".h-set-homework").siblings(".h-set-homework").length > 0){
                        $this.parents(".h-set-homework").remove();
                    }else{
                        if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                            $this.parents(".hPreview-main").remove();
                        }else{
                            $this.parents(".hPreview-main").remove();
                        }
                    }
                }
            }else{
                $.each(constantObj._homeworkContent.practices[type].questions,function(i){
                    if(this.id == qid || this.questionId==qid){
                        constantObj._homeworkContent.practices[type].questions.splice(i,1);
                        return false;
                    }
                });

                $.each(constantObj._reviewQuestions[type],function(i){
                    if(this.id == qid || this.questionId==qid){
                        constantObj._reviewQuestions[type].splice(i,1);
                        return false;
                    }
                });
                constantObj._moduleSeconds[type] -= seconds;

                if($this.parents(".h-set-homework").siblings(".h-set-homework").length > 0){
                    $this.parents(".h-set-homework").remove();
                }else{
                    if($this.parents(".hPreview-main").siblings(".hPreview-main").length > 0){
                        $this.parents(".hPreview-main").remove();
                    }else{
                        $this.parents(".hPreview-main").remove();
                    }
                }
            }

            if(displayClazzGroupOfTabTypes.indexOf(type) != -1){
                carts
                && typeof carts["recalculate"] === 'function'
                && carts["recalculate"](type,0);
                var questioncnt = 0,groupId,$reviewHomework = $("#reviewhomework");
                for(var t = 0,tLen = displayClazzGroupOfTabTypes.length; t < tLen;t++){
                    questioncnt += $reviewHomework.find("." + displayClazzGroupOfTabTypes[t]).length;
                    if(questioncnt > 0){
                        break;
                    }
                }
                $(".J_clazzTabType").each(function(i,obj){
                    if($(this).hasClass("active")){
                        groupId = (+$(this).attr("data-groupid"));
                    }
                });
                if(questioncnt == 0){
                    var newIndex = -1;
                    $.each(groupList,function(i,group){
                        if(group.groupId == groupId){
                            newIndex = i;
                        }
                    });
                    newIndex != -1 && (groupList.splice(newIndex,1));
                    redrawClazzTabType(groupList);
                }

            }else{
                if(type === "MENTAL" || type == "MENTAL_ARITHMETIC"){
                    $(".J_UFOInfo p[type='"+type+"']").find("i.J_delete").trigger("click");
                }else{
                    var data_count = 0;
                    $("#assignTotalTime").html(reSetUFO());
                    $(".J_totalTime-"+type).html(Math.ceil(constantObj._moduleSeconds[type]/60));
                    if(["NEW_READ_RECITE","READ_RECITE_WITH_SCORE"].indexOf(type) != -1){
                        $.each(constantObj._reviewQuestions[type],function () {
                            data_count += this.questions.length;
                        });
                    }else{
                        data_count = constantObj._reviewQuestions[type].length;
                    }

                    $(".J_UFOInfo p[type='"+type+"'] .count").attr("data-count",data_count).html(data_count);
                }
            }

            if($("#reviewhomework").find(".hPreview-main").length == 0 && type !== "MENTAL" && type != "MENTAL_ARITHMETIC"){
                //口算作业类型删除因为是全部删除，上面find("i.J_delete").trigger("click")已经触发了预览的click了
                $("#previewBtn").trigger("click");
            }
        });

        $(document).on("mouseover mouseout",".J_basicMask",function(){
            $(this).find(".lessons-mask").toggle();
        }).on("click",".J_basicMask",function(){
            var $this = $(this).parents("li");
            var categoryId = $this.attr("categoryId"),lessonId = $this.attr("lessonId");
            var homeworkType = $this.attr("homeworkType") || "BASIC_APP";
            switch (homeworkType){
                case "NATURAL_SPELLING":
                    var practices;
                    $.each(constantObj._reviewQuestions[homeworkType], function () {
                        if(this.categoryId == categoryId && this.lessonId == lessonId){
                            practices = $.isArray(this.practices) ? this.practices : [];
                        }
                    });
                    var singlePractice = !$.isArray(practices) ? {} : practices[0];
                    var questions = singlePractice.questions || [];
                    if(questions.length <= 0){
                        $17.alert("没有配相应的应试题,暂不能预览");
                        return false;
                    }
                    var qIds = [];
                    for(var t = 0, tLen = questions.length; t < tLen; t++){
                        qIds.push(questions[t].questionId);
                    }
                    var domain = "/";
                    if(constantObj.env === "test"){
                        domain = location.protocol + "//www.test.17zuoye.net/";
                    }else{
                        domain = location.protocol + "//" + location.host;
                    }

                    var urlParams = JSON.stringify({
                        env : constantObj.env,
                        img_domain:constantObj.imgDomain,
                        hw_practice_url:domain + "/flash/loader/newselfstudymobile.vpage?qids=" + qIds.join(",") + "&lessonId=" + lessonId + "&practiceId=" + singlePractice.practiceId,
                        client_name:"pc",
                        client_type:"pc",
                        from : "preview"
                    });

                    gameUrl = domain + "/resources/apps/hwh5/funkyspell/V1_0_0/index.vhtml?__p__=" + encodeURIComponent(urlParams);

                    data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                    $.prompt(data, {
                        title   : "预 览",
                        buttons : {},
                        position: { width: 740 },
                        close   : function(){
                            $('iframe').each(function(){
                                var win = this.contentWindow || this;
                                if(win.destroyHomeworkJavascriptObject){
                                    win.destroyHomeworkJavascriptObject();
                                }
                            });
                        },
                        loaded : function(){
                            var iframe = $("iframe.vox17zuoyeIframe")[0];
                            iframe.contentWindow.addEventListener("closePreviewPopup",function(){
                                $.prompt.close();
                            });
                        }
                    });
                    break;
                case "BASIC_APP":
                    $.each(constantObj._reviewQuestions["BASIC_APP"], function () {
                        if(this.categoryId == categoryId && this.lessonId == lessonId){

                            var practices = this.practices || [];
                            if(practices.length <= 0){
                                $17.alert("没有相应类别应用,暂不能预览");
                                return false;
                            }
                            var questions = practices[0].questions || [];
                            if(questions.length <= 0){
                                $17.alert("没有配相应的应试题,暂不能预览");
                                return false;
                            }
                            var qIds = [];
                            for(var t = 0, tLen = questions.length; t < tLen; t++){
                                qIds.push(questions[t].questionId);
                            }
                            var paramObj = {
                                qids : qIds.join(","),
                                lessonId : lessonId,
                                practiceId : practices[0].practiceId,
                                fromModule : ""
                            };
                            var gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
                            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                            $.prompt(data, {
                                title   : "预 览",
                                buttons : {},
                                position: { width: 740 },
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
                    });
                    break;
                default:
            }


            $17.voxLog({
                module: "homeworkBasicpractice",
                op    : "homework-preview-basicpractice"
            });
        });

        $(document).on("click",".J_deleteAll-quiz",function(){
            var $this = $(this);
            var type = $this.attr("category");

            constantObj._homeworkContent.practices[type].questions = [];
            constantObj._reviewQuestions[type] = [];
            constantObj._moduleSeconds[type] = 0;

            $("#assignTotalTime").html(reSetUFO());
            $(".J_UFOInfo p[type='"+type+"'] .count").attr("data-count",0).html(0);
            $(this).parents(".hPreview-main").remove();

            if($("#reviewhomework").find(".hPreview-main").length == 0){
                $("#previewBtn").trigger("click");
            }
        });

        $(document).on("click",".J_showMoreQuestions",function(){
            var questions = [];
            var tabType = $(this).attr("data-tabtype");
            $(this).siblings(".h-set-homework:hidden").each(function(i){
                if(i < 3){
                    var qid = $(this).find(".seth-mn").attr("id");
                    questions.push({
                        questionId :qid.replace("review_" + tabType + "_","")
                    });
                }
            });
            if($(this).siblings(".h-set-homework:hidden").length <= 3){
                $(this).hide();
            }

            _initSubject(questions,tabType);
        });

        $(document).on("click",".J_readingPreview",function(){
            var paramObj = {
                pictureBookId : $(this).parents("li").attr("qid"),
                fromModule : ""
            };
            var gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';

            $.prompt(data, {
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
        });
        $(document).on("click",".J_levelReadingsPreview",function(){
            var dataHtml = "";
            var paramObj = {
                pictureBookIds : $(this).parents("li").attr("qid"),
                from : "preview"
            };
            var domain = "/";
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
                }
            });
        });


        $(document).on("click",".J_DubbingPreview",function(){
            var dubbingObj;
            var $this = $(this);
            var type = $this.attr("data-type");
            var qid = $this.attr("qid");
            $.each(constantObj._reviewQuestions[type],function(i){
                if(this.dubbingId == qid){
                    dubbingObj = constantObj._reviewQuestions[type][i];
                    return false;
                }
            });
            if(!dubbingObj){
                $17.alert("暂不支持预览");
                return false;
            }
            $17.homeworkv3.viewDubbingDetail({
                bookId : dubbingObj.bookId,
                unitId : dubbingObj.unitId,
                dubbingId : dubbingObj.dubbingId
            });
        });

        $(document).on("click",".single-dictation-question",function(e){
            var $this = $(this);
            var qid = $this.attr("qid"),
                seconds = $this.attr("seconds"),
                type = $this.attr("category");
            $.each(constantObj._homeworkContent.practices[type].questions,function(i){
                if(this.questionId == qid){
                    constantObj._homeworkContent.practices[type].questions.splice(i,1);
                    return false;
                }
            });
            if(constantObj._homeworkContent.practices[type].questions.length === 0){
                constantObj._homeworkContent.practices[type].ocrDictation = null;
            }
            $.each(constantObj._reviewQuestions[type],function(i){
                if(this.id == qid){
                    constantObj._reviewQuestions[type].splice(i,1);
                    return false;
                }
            });
            constantObj._moduleSeconds[type] -= seconds;

            if($this.siblings(".single-dictation-question").length > 0){
                $this.remove();
            }else{
                if($this.parents(".lesson-dictation").siblings(".lesson-dictation").length > 0){
                    $this.parents(".lesson-dictation").remove();
                }else{
                    $this.parents(".hPreview-main").remove();
                }
            }

            $("#assignTotalTime").html(reSetUFO());
            $(".J_totalTime-"+type).html(Math.ceil(constantObj._moduleSeconds[type]/60));
            var data_count = constantObj._reviewQuestions[type].length;
            $(".J_UFOInfo p[type='"+type+"'] .count").attr("data-count",data_count).html(data_count);
            if($("#reviewhomework").find(".hPreview-main").length === 0){
                $("#previewBtn").trigger("click");
            }

        });

        $(document).on("click",".dictation-delete-all",function(){
            var $this = $(this);
            var seconds = $this.attr("seconds"),type = $this.attr("category");

            constantObj._homeworkContent.practices[type].questions = [];
            constantObj._reviewQuestions[type] = [];
            constantObj._moduleSeconds[type] -= seconds;

            if($this.parents(".single-dictation-question").siblings(".single-dictation-question").length > 0){
                $this.parents(".single-dictation-question").remove();
            }else{
                if($this.parents(".lesson-dictation").siblings(".lesson-dictation").length > 0){
                    $this.parents(".lesson-dictation").remove();
                }else{
                    $this.parents(".hPreview-main").remove();
                }
            }
            $("#assignTotalTime").html(reSetUFO());
            $(".J_totalTime-"+type).html(Math.ceil(constantObj._moduleSeconds[type]/60));
            var data_count = constantObj._reviewQuestions[type].length;

            $(".J_UFOInfo p[type='"+type+"'] .count").attr("data-count",data_count).html(data_count);
            if($("#reviewhomework").find(".hPreview-main").length === 0){
                $("#previewBtn").trigger("click");
            }
        });
    }(carts);

    var reSetUFO = function(){
        var totalTime = 0;
        for(var z in constantObj._moduleSeconds){
            if(constantObj._moduleSeconds.hasOwnProperty(z)){
                totalTime += constantObj._moduleSeconds[z];
            }
        }
        return Math.ceil(totalTime/60);
    }


    var homeworkReviewFun = function(){
        this.removeHomeworkModules = function(type){

            $(".hPreview-main."+type).remove();
            if(displayClazzGroupOfTabTypes.indexOf(type) != -1){
                var newGroupList = [];
                $.each(displayClazzGroupOfTabTypes,function(i,key){
                    var zIndex,questions = constantObj._reviewQuestions[key];
                    for(var t = 0,tLen = questions.length; t < tLen; t++){
                        zIndex = -1;
                        for(var m = 0,mLen = newGroupList.length; m < mLen; m++){
                            if(newGroupList[m].groupId == questions[t].groupId){
                                zIndex = m;
                                break;
                            }
                        }
                        if(zIndex == -1){
                            var newClazz = {
                                groupId : questions[t].groupId,
                                groupName : questions[t].groupName
                            };
                            newGroupList.push(newClazz);
                        }
                    }
                });
                groupList = newGroupList;
                if(groupList.length > 0){
                    redrawClazzTabType(groupList);
                }else{
                    //班级筛选项删除
                    $("div.gradeFilter").remove();
                }
            }

            if($("#reviewhomework").find(".hPreview-main").length == 0){
                $("#previewBtn").trigger("click");
            }
        }
    }

    homeworkReviewFun.prototype = {
        constructor : homeworkReviewFun,

        initialise : function(carts){
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_preview_load",
                s0 : constantObj.subject
            });
            _initDom();

            $("#reviewhomework").show();
        },

        hide : function(){
            $("#reviewhomework").hide();
            $("#reviewhomework").find(".gradeSelect-label").remove();
            $("#reviewhomework").find(".hPreview-main").remove();
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_preview_return_adjustment_click",
                s0 : constantObj.subject
            });
        }

    };
    return homeworkReviewFun;

})(constantObj);