(function(window,$17,constantObj,undefined){
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

    function LevelsAndBook(obj, resolveCb, rejectCb, vacationCarts){
        var self = this;
        self.resolveCb              = resolveCb || null;
        self.rejectCb               = rejectCb || null;
        self.loading                = ko.observable(true);
        self.showLevel              = ko.observable(0);
        self.showClazzList          = ko.observableArray([]);
        self.batchclazzs            = [[],[],[],[],[],[]];
        self.isAllChecked           = ko.pureComputed(function(){
            return self.showClazzList().length == self.checkedClazzGroupIds().length;
        });
        self.clazzClickCb = null;

        //课本单元
        self.bookId                 = ko.observable(null);
        self.bookName               = ko.observable(null);
        self.defaultTermType        = 1;
        self.termType               = self.defaultTermType;

        //换教材
        self.changeBookModule       = null;
        self.vacationCarts          = vacationCarts || {};
    }
    LevelsAndBook.prototype = {
        constructor : LevelsAndBook,
        run         : function(){
            var self = this,paramData = {
                subject : constantObj.subject
            };
            $.get("/teacher/vacation/vacationclazzlist.vpage", paramData,function(data){
                if(data.success){
                    var _batchclazzs = data.batchclazzs || [];
                    if(_batchclazzs.length > 0){
                        var clazzLevel = -1;
                        for(var i = 0,iLen = _batchclazzs.length; i < iLen; i++){
                            if(_batchclazzs[i].clazzs.length > 0 && _batchclazzs[i].canBeAssigned){
                                var level = +_batchclazzs[i].classLevel;
                                self.batchclazzs[level - 1] = self.batchclazzs[level - 1].concat(_batchclazzs[i].clazzs);
                                if(_batchclazzs[i].canBeAssigned && clazzLevel == -1){
                                    clazzLevel = level;
                                }
                            }
                        }
                        if(clazzLevel > 0){
                            self.levelClick(self,clazzLevel);
                        }else{
                            $17.info("clazzs can't assign");
                            //对当前重置
                            self.loading(false);
                            self.showClazzList([]);
                            typeof self.rejectCb === 'function' && self.rejectCb({
                                errorCode : "NO_CLAZZ"
                            });
                        }
                    }else{
                        $17.info("clazzs can't assign");
                        //对当前重置
                        self.loading(false);
                        self.showLevel(0);
                        self.showClazzList([]);
                        typeof self.rejectCb === 'function' && self.rejectCb({
                            errorCode : "NO_CLAZZ"
                        });
                    }
                }else{
                    //调用 reject 回调,并日志打点
                    typeof self.rejectCb === 'function' && self.rejectCb({
                        errorCode : "NO_CLAZZ"
                    });
                    data.errorCode === "200" && $17.voxLog({
                        module  : "API_REQUEST_ERROR",
                        op      : "API_STATE_ERROR",
                        s0      : "/teacher/termreview/clazzlist.vpage",
                        s1      : $.toJSON(data),
                        s2      : $.toJSON(paramData)
                    });
                }
            });
        },
        chooseOrCancelAll       : function(){
            var self = this;
            var _isAllChecked = self.isAllChecked();
            var _clazzList = self.showClazzList();
            for(var k = 0; k < _clazzList.length; k++){
                self.showClazzList()[k].checked(!_isAllChecked);
            }
            self.setCartsClazzCount(_isAllChecked ? 0 : _clazzList.length);
        },
        singleClazzAddOrCancel  : function(self,index){
            var checked = self.showClazzList()[index].checked();
            self.showClazzList()[index].checked(!checked);

            typeof self.clazzClickCb === "function" && self.clazzClickCb();
            self.setCartsClazzCount(self.checkedClazzGroupIds().length);
        },
        setCartsClazzCount  : function(clazzCount){
            var self = this;
            typeof self.vacationCarts.setClazzCount === 'function'
            && self.vacationCarts.setClazzCount(clazzCount);
        },
        checkedClazzGroupIds    : function(){
            var self = this;
            var checkedClazzGroupIds = [];
            var _clazzList = self.showClazzList();
            for(var z = 0; z < _clazzList.length; z++){
                if(_clazzList[z].checked()){
                    var clazzGroupId = _clazzList[z].classId() + "_" + _clazzList[z].groupId();
                    checkedClazzGroupIds.push(clazzGroupId);
                }
            }
            return checkedClazzGroupIds;
        },
        checkedClazzNames    : function(){
            var self = this;
            var checkedClazzNameList = [];
            var _level = self.showLevel();
            var _clazzList = self.showClazzList();
            for(var z = 0; z < _clazzList.length; z++){
                if(_clazzList[z].checked()){
                    var clazzName = _level + "年级" + _clazzList[z].className();
                    checkedClazzNameList.push(clazzName);
                }
            }
            return checkedClazzNameList;
        },
        getCanBeAssignClazzList : function(level){
            var canBeAssignClazzList = []
                ,_clazzsList = self.batchclazzsMap[level] || [];
            for(var i = 0, iLen = _clazzsList.length; i < iLen; i++){
                if(_clazzsList[i].canBeAssigned){
                    canBeAssignClazzList.push(_clazzsList[i]);
                }
            }
            return canBeAssignClazzList;
        },
        levelClick              : function(self,level){
            level  = (+level) || 0;
            if(level <= 0 || level == self.showLevel()){
                return false;
            }
            var levelFn = function(){
                var _clazzList = []
                    ,_clazzs = self.batchclazzs[level - 1];
                for(var i = 0, iLen = _clazzs.length; i < iLen; i++){
                    if(_clazzs[i].canBeAssigned){
                        _clazzs[i]["checked"] = true;
                        _clazzList.push(_clazzs[i]);
                    }
                }
                self.setCartsClazzCount(_clazzList.length);
                self.showClazzList(ko.mapping.fromJS(_clazzList)());
                self.showLevel(level);
                self.loadBookByClazzIds(self.checkedClazzGroupIds());
            };
            levelFn();
        },
        initBookInfo : function(bookObj){
            var self = this;
            self.bookId(bookObj.bookId);
            self.bookName(bookObj.bookName);
            self.termType = bookObj.termType || self.defaultTermType;
            self.bookId() == null && typeof self.rejectCb === "function" && self.rejectCb({
                success : false,
                info    : "请先在上方选择复习范围哦"
            });
            self.bookId() != null && typeof self.resolveCb === "function" && self.resolveCb({
                success : true,
                info    : ""
            });
        },
        loadBookByClazzIds  : function(clazzGroupIds){
            if(!$.isArray(clazzGroupIds) || clazzGroupIds.length == 0){
                // 调用 reject 回调
                return false;
            }
            var self = this,
                clazzBookUrl = "/teacher/new/homework/clazz/book.vpage",
                paramData = {
                    clazzs  : clazzGroupIds.join(","),
                    subject : constantObj.subject,
                    from    : "vacation"
                };

            $.get(clazzBookUrl,paramData,function(data){
                self.loading(false);
                if(data.success && data.clazzBook){
                    self.initBookInfo(data.clazzBook);
                }else{
                    //重置课本信息
                    self.bookId(null);
                    self.bookName(null);
                    //调用 reject 回调
                    data.errorCode !== "200" && $17.voxLog({
                        module  : "API_REQUEST_ERROR",
                        op      : "API_STATE_ERROR",
                        s0      : clazzBookUrl,
                        s1      : $.toJSON(data),
                        s2      : $.toJSON(paramData)
                    });
                }
            });
        },
        changeBook : function(){
            var self   = this,
                _level = self.showLevel(),
                _term  = self.termType;

            var clazzGroupIds = self.checkedClazzGroupIds();
            var bookPop = function(){
                var changeBookOption = {
                    level           : _level,
                    term            : _term,
                    clazzGroupIds   : clazzGroupIds,
                    bookName        : self.bookName(),
                    subject         : constantObj.subject,
                    isSaveBookInfo  : false
                };
                if(!self.changeBookModule){
                    self.changeBookModule = new ChangeBook()
                }
                self.changeBookModule.init(changeBookOption,function(data){
                    self.initBookInfo(data);
                });
            };
            bookPop();
        },
        getBookId : function(){
            return this.bookId();
        }
    };

    var QuestionDB = (function(){
        var questionMap = {};   //用来存储每道题的详细信息，即题库数据
        return {
            addQuestions : function(questionIds,callback){
                callback = $.isFunction(callback) ? callback : function(){};
                if(!$.isArray(questionIds) || questionIds.length == 0){
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
                if(!$.isArray(questionIds) || questionIds.length == 0){
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


    /**
     * 作业内容中周选项卡列表
     * @constructor
     */
    function WeekContent(obj,vactionCarts){
        var self            = this;
        self.loading        = ko.observable(true);
        self.bookId         = null;
        self.weekList       = ko.observableArray([]);
        self.focusWeekRank  = ko.observable(-1);

        self.weekDayMap = {};
        self.dayPlanList    = ko.observableArray([]);
        self.currentDayPlanIndex = ko.observable(-1);
        self.currentDayPlan = ko.pureComputed(function(){
            var focusDayPlanIndex = self.currentDayPlanIndex(),iLen = self.dayPlanList().length;
            return focusDayPlanIndex >=0 && focusDayPlanIndex < iLen ? self.dayPlanList()[focusDayPlanIndex] : {};
        });
        self.dayPlanDetailCache = {};  //key : bookId_weekRank_dayRank,value : dayPlanDetailList
        self.dayPlanDetailList = ko.observableArray([]);

        self.vactionCarts = vactionCarts || null;

        self.movePackageCnt = 3;
        self.leftEnabled    = ko.pureComputed(function(){
            //左箭头是否可用
            return Math.floor(self.currentDayPlanIndex()/self.movePackageCnt) * self.movePackageCnt > 0;
        });
        self.rightEnabled  = ko.pureComputed(function(){
            //右箭头是否可用
            return self.currentDayPlanIndex() < Math.floor(self.dayPlanList().length/self.movePackageCnt) * self.movePackageCnt;
        });
    }
    WeekContent.prototype = {
        constructor : WeekContent,
        getTemplateNameByHomeworkType : function(homeworkType){
            var templateMap = {
                BASIC_APP           : "T:VACATION_BASIC_APP",
                NATURAL_SPELLING    : "T:VACATION_NATURAL_SPELLING",
                DUBBING             : "T:VACATION_DUBBING",
                DUBBING_WITH_SCORE  : "T:VACATION_DUBBING",
                READING             : "T:VACATION_READING",
                LEVEL_READINGS      : "T:VACATION_READING",
                EXAM                : "T:VACATION_SINGLE_PACKAGE_STYLE",
                INTELLIGENCE_EXAM   : "T:VACATION_SINGLE_PACKAGE_STYLE",
                MENTAL              : "T:VACATION_MENTAL",
                INTERESTING_PICTURE : "T:VACATION_SINGLE_PACKAGE_STYLE",
                BASIC_KNOWLEDGE     : "T:VACATION_SINGLE_PACKAGE_STYLE",
                CHINESE_READING     : "T:VACATION_SINGLE_PACKAGE_STYLE",
                NEW_READ_RECITE     : "T:VACATION_NEW_READ_RECITE",
                MATH_INTERESTING_PICTURE : "T:VACATION_MATH_INTERESTING_PICTURE",
                MENTAL_ARITHMETIC   : "T:VACATION_MENTAL_ARITHMETIC",
                READ_RECITE_WITH_SCORE : "T:VACATION_NEW_READ_RECITE"
            };
            return templateMap[homeworkType] || "T:UNKNOWN_TEMPLATE";
        },
        arrowClick      : function(directionOfArrow){
            var self = this,movePackageCnt = self.movePackageCnt,_startPos = self.currentDayPlanIndex(),newDayPlanIndex;
            if(directionOfArrow === "arrowLeft" && self.leftEnabled()){
                var newStartPos = _startPos - movePackageCnt;
                newStartPos = newStartPos > 0 ? newStartPos : 0;
                newDayPlanIndex = Math.floor(newStartPos/movePackageCnt) * self.movePackageCnt;
            }else if(directionOfArrow === "arrowRight" && self.rightEnabled()){
                newDayPlanIndex = Math.floor((_startPos + movePackageCnt)/movePackageCnt) * self.movePackageCnt;
            }
            self.currentDayPlanIndex(newDayPlanIndex);
            self.dayPlanDetail(self.currentDayPlan().dayRank);
            return false;
        },
        packageClick: function(dayPlanIndex,self){
            if(dayPlanIndex == self.currentDayPlanIndex()){
                return false;
            }
            self.currentDayPlanIndex(dayPlanIndex);
            self.dayPlanDetail(self.currentDayPlan().dayRank);
        },
        run : function(bookId){
            var self = this,
                param = {
                    bookId  : bookId,
                    subject : constantObj.subject
                };
            self.bookId = bookId || null;
            $.post("/teacher/vacation/weektab.vpage",param,function(data){
                if(data.success){
                    var _weekPlans = data.weekPlans || [],
                        _newWeekList = [],
                        weekTaskCount = 0;
                    for(var m = 0,mLen = _weekPlans.length; m < mLen; m++){
                        var dayPlans = _weekPlans[m]["dayPlans"] || [];
                        self.weekDayMap[_weekPlans[m].weekRank] = dayPlans;
                        weekTaskCount += dayPlans.length;
                        _newWeekList.push({
                            weekRank : _weekPlans[m]["weekRank"],
                            title    : _weekPlans[m]["title"],
                            scope    : _weekPlans[m]["scope"]
                        });
                    }
                    self.vactionCarts != null && typeof self.vactionCarts.setTaskCount === 'function'
                    && self.vactionCarts.setTaskCount(weekTaskCount);
                    self.weekList(_newWeekList);
                    if(_newWeekList.length > 0){
                        self.setWeekData(_newWeekList[0].weekRank);
                    }else{
                        self.focusWeekRank(-1);
                        self.dayPlanList([]);
                        self.currentDayPlanIndex(-1);
                        self.dayPlanDetail(null);
                    }

                    self.loading(false);
                    self.initExamCore();
                    if(self.weekList().length > 5){
                        new SimpleSlider({
                            slideName      : "slider",
                            clickLeftId    : "#swipingLeft",
                            clickRightId   : "#swipingRight",
                            slideContainer : "#slideContainer",
                            slideItem      : ".slideItem",
                            itemWidth      : "155",
                            slideCount     : 5,
                            totalCount     : self.weekList().length
                        });
                    }
                }else{
                    data.errorCode !== "200" && $17.voxLog({
                        module  : "API_REQUEST_ERROR",
                        op      : "API_STATE_ERROR",
                        s0      : "/teacher/vacation/weektab.vpage",
                        s1      : $.toJSON(data),
                        s2      : $.toJSON(param)
                    });
                }
            });
        },
        setWeekData : function(weekRank){
            var self = this;
            self.focusWeekRank(weekRank);
            var dayPlanList = self.weekDayMap[weekRank] || [];
            self.dayPlanList(dayPlanList);
            (dayPlanList.length > 0) && self.currentDayPlanIndex(0);
            self.dayPlanDetail(self.currentDayPlan().dayRank);
        },
        weekClick   : function(weekRank,self){
            self.setWeekData(weekRank);
        },
        dayPlanDetail : function(dayRank){
            var self = this;
            dayRank = +dayRank || -1;
            if(dayRank <= 0){
                self.dayPlanDetailList([]);
                return false;
            }
            var param = {
                bookId : self.bookId,
                weekRank : self.focusWeekRank(),
                dayRank  : dayRank
            },cacheKey = [self.bookId,self.focusWeekRank(),dayRank].join("_");
            if(self.dayPlanDetailCache.hasOwnProperty(cacheKey)){
                self.dayPlanDetailList(self.dayPlanDetailCache[cacheKey]);
            }else{
                self.dayPlanDetailList([]);
                $.post("/teacher/vacation/day/planelements.vpage",param,function(data){
                    if(data.success){
                        var dayPlanDetails = data.dayPlanElements || [];
                        var questionsIds = [];
                        for(var m = 0,mLen = dayPlanDetails.length; m < mLen; m++){
                            var dayPlanDetailObj = dayPlanDetails[m];
                            if(dayPlanDetailObj.objectiveConfigType === "MENTAL_ARITHMETIC"){
                                questionsIds = questionsIds.concat(dayPlanDetailObj.questionIds);
                            }
                        }
                        if(questionsIds.length > 0){
                            QuestionDB.addQuestions(dayPlanDetailObj.questionIds,function(){
                                self.dayPlanDetailList(dayPlanDetails);
                            });
                        }else{
                            self.dayPlanDetailList(dayPlanDetails);
                        }
                        self.dayPlanDetailCache[cacheKey] = dayPlanDetails;
                    }else{
                        //调用 reject 回调
                        data.errorCode !== "200" && $17.voxLog({
                            module  : "API_REQUEST_ERROR",
                            op      : "API_STATE_ERROR",
                            s0      : "/teacher/vacation/day/planelements.vpage",
                            s1      : $.toJSON(data),
                            s2      : $.toJSON(param)
                        });
                    }
                });
            }
        },
        displayMode : function(homeworkTypeInfo,bindingContext){
            return bindingContext.$parent.getTemplateNameByHomeworkType(homeworkTypeInfo.objectiveConfigType);
        },
        viewQuestions : function(planDetail){
            var self = this;
            var templateName;
            var objectiveConfigType = planDetail.objectiveConfigType;
            switch (objectiveConfigType){
                case "READ_RECITE_WITH_SCORE":
                    templateName = "t:READ_RECITE_WITH_SCORE_QUESTIONS_PREVIEW";
                    break;
                default:
                    templateName = "t:viewDetailTPL";
            }
            if(!templateName){
                return false;
            }
            $.prompt(template(templateName,{}), {
                title    : "题目预览",
                buttons  : {},
                position : { width: 960},
                close    : function () {
                    $('body').css('overflow', 'auto');
                    //关闭所有播放
                    selfPlayer.stopAll();
                }
            });
            $('body').css('overflow', 'hidden');
            ko.applyBindings({
                playingQuestionId     : ko.observable(null),
                hasVideo  : false,
                showCount : ko.observable(3),
                questions : self.questions || self.reading.questions,
                loadQuestionContent : function (data,index) {
                    var $mathExamImg = $("#subjective_" + data.id + index);
                    $mathExamImg.empty();
                    $("<div></div>").attr("id","examImg_" + data.id + index).appendTo($mathExamImg);
                    var node = document.getElementById("examImg_" + data.id + index);
                    vox.exam.render(node, 'normal', {
                        ids       : [data.id],
                        imgDomain : constantObj.imgDomain,
                        env       : constantObj.env,
                        domain    : constantObj.domain,
                        objectiveConfigType : objectiveConfigType
                    });

                    return null;
                },
                showMoreQuestions : function(){
                    this.showCount(this.showCount()+3);
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
                    }
                }
            }, document.getElementById('jqistate_state0'));
        },
        viewVideo : function(){
            var self = this;

            $.prompt(template("t:viewDetailTPL",{}), {
                title    : "题目预览",
                buttons  : {},
                position : { width: 960},
                close    : function () {
                    $('body').css('overflow', 'auto');
                }
            });
            $('body').css('overflow', 'hidden');
            ko.applyBindings({
                hasVideo  : true,
                videoInfo : {
                    videoSummary   : self.videoSummary,
                    solutionTracks : self.solutionTracks || [],
                    videoUrl       : self.videoUrl,
                    videoConverUrl : self.coverUrl
                },
                questions : self.questions,
                showCount : ko.observable(3),
                getVideoFlashVars : function(embedContainerElem){
                    var videoUrl = this.videoInfo.videoUrl,
                        videoConverUrl = this.videoInfo.videoConverUrl;
                        htmlStr = '<embed width="438" height="300" flashvars="file=' + videoUrl + '&amp;image=' + videoConverUrl + '&amp;width=438&amp;height=300" allowfullscreen="true" quality="high" wmode="opaque" windowlessVideo=1 value="transparent" src="' + constantObj.flashPlayerUrl + '" type="application/x-shockwave-flash">';

                    $(embedContainerElem).html(htmlStr);
                    return "";
                },
                loadQuestionContent : function (data,index) {
                    var $mathExamImg = $("#subjective_" + data.id + index);
                    $mathExamImg.empty();
                    $("<div></div>").attr("id","examImg_" + data.id + index).appendTo($mathExamImg);
                    var node = document.getElementById("examImg_" + data.id + index);
                    vox.exam.render(node, 'normal', {
                        ids       : [data.id],
                        imgDomain : constantObj.imgDomain,
                        env       : constantObj.env,
                        domain    : constantObj.domain
                    });

                    return null;
                },
                showMoreQuestions : function(){
                    this.showCount(this.showCount()+3);
                }
            }, document.getElementById('jqistate_state0'));
        },
        displayQuestionContent : function(data){
            return data ? data.replace(/__\$\$__/g,"(  )") : "";
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
                    }
                },false,{
                    imgDomain : constantObj.imgDomain,
                    env       : constantObj.env,
                    domain    : constantObj.domain
                });
            }catch(exception){
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: exception.message,
                    userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
                });
            }
        },
        covertSentences    : function(sentences,separator){
            if(!$.isArray(sentences)){
                return "";
            }
            separator = separator || " / ";
            return sentences.join(separator);
        },
        getCategroyIconUrl : function(categroyIcon){
            categroyIcon = +categroyIcon || 50000;
            return constantObj.basicIconPrefixUrl + "e-icons-" + categroyIcon + ".png";
        }
    };

    $.extend(true,WeekContent.prototype,{
        //基础练习方法集合BASIC_APP
        categoryPreview: function(lessonId,self){
            var categoryKO = this;
            var practices = categoryKO.practices || [];
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

            return false;
        }
    },{
        //自然拼读方法集合
        spellingPreview : function(){
            var categoryKO = this;
            var previewVideo = categoryKO.previewVideo ? categoryKO.previewVideo : null;
            var previewImage;
            if(categoryKO.previewImages){
                var koImages = ko.mapping.toJS(categoryKO.previewImages);
                previewImage = $.isArray(koImages) && koImages.length > 0 ? koImages[0] : null;
            }
            previewImage = previewImage ? constantObj.imgDomain + previewImage : null;
            if(!$17.isBlank(previewVideo)){
                var flashWidth = 275,flashHeight = 500;

                $.prompt(template("t:video_preview_tip",{}),{
                    title   : "预 览",
                    position    : { width: 315},
                    buttons     : {},
                    focus       : 1,
                    submit:function(e,v,m,f){},
                    close   : function(){},
                    loaded : function(){
                        $("#movie").getFlash({
                            id       : "NATURAL_SPELLING_PREVIEW",
                            width    : 275,//flash 宽度
                            height   : 500, //flash 高度
                            movie    : constantObj.flashPlayerUrl,
                            scale    : 'showall',
                            flashvars: "file=" + previewVideo + "&amp;image=" + previewImage + "&amp;width=" + flashWidth + "&amp;height=" + flashHeight + "&amp;autostart=true"
                        });
                    }
                });
            }else{
                $17.alert("抱歉，此应用暂不支持预览,我们正跑步上线...");
            }
        }
    },{
        //趣味配音DUBBING
        dubbingView : function(){
            var dubbingKO = this;
            var dataHtml = "";
            var dubbingObj = ko.mapping.toJS(dubbingKO);
            dataHtml = template("t:SINGLE_DUBBING_PREVIEW",{
                dubbingObj : dubbingObj
            });
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
                        id       : "DUBBING_PLAY_PREVIEW",
                        width    : flashWidth,//flash 宽度
                        height   : flashHeight, //flash 高度
                        movie    : constantObj.flashPlayerUrl,
                        scale    : 'showall',
                        flashvars: "file=" + dubbingObj.videoUrl + "&amp;image=" + dubbingObj.coverUrl + "&amp;width=" + flashWidth + "&amp;height=" + flashHeight + "&amp;autostart=false"
                    });
                }
            });
        }
    },{
        //阅读绘本
        viewReading : function (pictureBookId,objectiveConfigType) {
            var dataHtml = "";
            var gameUrl;
            switch (objectiveConfigType) {
                case "LEVEL_READINGS":
                    var paramObj = {
                        pictureBookIds : pictureBookId,
                        from : "preview"
                    };
                    var domain = "/";
                    if(constantObj.env === "test"){
                        domain = "//www.test.17zuoye.net/";
                    }else{
                        domain = location.protocol + "//" + location.host;
                    }
                    gameUrl = domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param(paramObj);
                    dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                    $.prompt(dataHtml, {
                        title   : "预 览",
                        buttons : {},
                        position: { width: 960 },
                        close   : function(){
                        }
                    });
                    break;
                default:
                    gameUrl  = "/flash/loader/newselfstudy.vpage?" + $.param({
                        pictureBookId : pictureBookId,
                        fromModule : ""
                    });

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
                    break;
            }
        }
    },{
        //趣味卡
        picturePreview:function (picUrl) {
            var dataHtml = "";
            dataHtml = template("t:PREVIEW_IMAGE",{
                imgUrl : picUrl
            });
            $.prompt(dataHtml, {
                title   : "预 览",
                buttons : {},
                position: { width: 700 },
                close   : function(){}
            });
        }

    },{
        //新口算
        renderVueQuestion : function(questionId,index){
            var containerId = '#' + questionId + '-' + index;
            var $containerId = $(containerId);
            var question = QuestionDB.getQuestionById(questionId);
            if(!question){
                $containerId.html("题目未加载");
                return false;
            }
            if(!$containerId.attr("data-init")){
                $containerId.attr("data-init","true");
                //config配置里的参数，可以抽象出来作为属性由父组件传进来，注意兼容性和扩展性。
                var config = {
                    container: containerId, //容器的id，（必须）
                    questionList: [question], //试题数组，包含完整的试题json结构， （必须）
                    framework: {
                        vue: Vue, //vue框架的外部引用
                        vuex: Vuex //vuex框架的外部引用
                    },
                    commitType: 'local', //结果提交方式：local/remote
                    readonly: true, //是否纯展示（不可答题）
                    answerPosition: 'all', //答案展示的位置，'up'/'down'/'all'/false
                    showAnalysis: false, //是否展示解析, true/false/'wrong'/'right'
                    showUserAnswer: false, //是否展示用户答案, true/false/'wrong'/'right'
                    showRightAnswer: true, //是否展示正确答案, true/false/'wrong'/'right'
                    showCommitBtn: false, //是否展示提交按钮
                    showNextBtn: false, //是否显示下一题按钮
                    showFinishBtn: false, //是否显示完成按钮
                    getQuestionRemote: false, //是否自己去取题
                    expandAnalysis: false, //是否展开解析
                    expandComplex: false, //复合题小题是否在面板中展开显示
                    autoFocusBlank: false, //填空题自动聚焦在第一空，并弹出键盘
                    showNextBlankBtn: false, //数学键盘是否显示下一空按钮
                    startIndex: 0, //从第几题开始展示，默认为0
                    listMode: false, //是否开启列表渲染模式，默认为false
                    lineDrag: false, //连线题是否开启拖动连线，默认关闭,
                    reviewMode: true,
                    multiInstance: true, //是否要初始化多个实例
                    withoutMathJax: true,
                    displayCtrlbar: false,
                    isParseFormula: false,  //是否进行公式转换
                    onQuestionRender: function(){
                        //单题渲染完毕
                        Venus.parseFormula(document.getElementById("tabContent"));
                    }
                };
                try{
                    Venus.init(config);
                }catch (e){
                    $17.log(e.message,e.stack);
                }
            }
            return "";
        }
    });

    /*
    * 购物车
    * */
    function VactionCarts(obj){
        var self = this;
        self.clazzCount = ko.observable(0);
        self.taskCount = ko.observable(0);
        self.assignClickCb = obj.assignClickCb || null;
    }
    VactionCarts.prototype = {
        constructor     : VacationIndex,
        isInteger       : function(clazzCount){
            return !(/\D/g.test(clazzCount));
        },
        setClazzCount   : function(clazzCount){
            var self = this;
            self.isInteger(clazzCount) && self.clazzCount(+clazzCount);
        },
        setTaskCount    : function(taskCount){
            this.isInteger(taskCount) && this.taskCount(taskCount);
        },
        assignClick     : function(){
            var self = this;
            typeof self.assignClickCb === 'function'
            && self.assignClickCb();

            $17.voxLog({
                module : "m_elhqnSjz",
                op     : "arrangement_click",
                s0     : constantObj.subject
            })
        },
        getTaskCount   : function(){
            return this.taskCount();
        }
    };

    /**
     * 布置弹窗
     * @constructor
     */
    var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
    var m = [
        '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
        '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
        '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
        '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
        '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
        '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
    ];
    function VacationConfirm(option,vacationConfirmAssign){
        var self = this;
        self.taskCount      = option.taskCount || 0;
        self.clazzNames     = ko.observableArray(option.clazzNames || []);
        //最早开始时间
        self.earliestStartDateTime = option.earliestStartDateTime || option.startDateTime; //yyyy-MM-dd 00:00:00
        //开始时间
        self.startDateTime  = ko.observable(option.startDateTime);  //yyyy-MM-dd 00:00:00
        self.startDate      = ko.pureComputed(function(){
            return self.startDateTime().substring(0,10);
        });
        self.latestStartDateTime = option.latestStartDateTime;
        var arr = self.splitDateTime(option.startDateTime);
        self.startHourSelect = ko.observableArray(h);
        self.startFocusHour = ko.observable(arr[3]);
        self.startMinSelect = ko.observableArray(m);
        self.startFocusMin  = ko.observable(arr[4]);
        self.startFocusHour.subscribe(function(newValue){
            self.startDateTime(self.startDate() + " " + newValue + ":" + self.startFocusMin() + ":00");

        },self);
        self.startFocusMin.subscribe(function(newValue){
            self.startDateTime(self.startDate() + " " + self.startFocusHour() + ":" + newValue + ":00");
        },self);

        //最早开始时间
        self.earliestEndDateTime = option.earliestEndDateTime || option.endDateTime; //yyyy-MM-dd 00:00:00
        //结束时间
        self.endDateTime  = ko.observable(option.endDateTime);  //yyyy-MM-dd 00:00:00
        self.endDate      = ko.pureComputed(function(){
            return self.endDateTime().substring(0,10);
        });
        self.latestEndDateTime = option.latestEndDateTime;
        var endArr = self.splitDateTime(option.endDateTime);
        self.endHourSelect = ko.observableArray(h);
        self.endFocusHour = ko.observable(endArr[3]);
        self.endMinSelect = ko.observableArray(m);
        self.endFocusMin  = ko.observable(endArr[4]);
        self.endFocusHour.subscribe(function(newValue){
            self.endDateTime(self.endDate() + " " + newValue + ":" + self.endFocusMin() + ":00");

        },self);
        self.endFocusMin.subscribe(function(newValue){
            self.endDateTime(self.endDate() + " " + self.endFocusHour() + ":" + newValue + ":00");
        },self);
        self.vacationConfirmAssign = vacationConfirmAssign || null;

        self.planDays = ko.observableArray([{
            totalDay : 20,
            dayOfWeek : 5
        },{
            totalDay : 25,
            dayOfWeek : 5
        },{
            totalDay : 30,
            dayOfWeek : 5
        }]);
        self.focusPlanDay = ko.observable(20);
    }

    VacationConfirm.prototype = {
        constructor : VacationConfirm,
        splitDateTime     : function(dateTime){
            return dateTime.split(/:|-|\s/g);
        },
        getTimeArray      : function(array, index){
            return $.grep(array, function (val, key) {
                return val >= index;
            });
        },
        saveHomework      : function(element){
            var self     = this,
                $element = $(element),
                _sTime   = new Date(self.startDateTime().replace(/-/g,"/")).getTime(), //ie只识别2017/07/01 08:00:00
                _eTime   = new Date(self.endDateTime().replace(/-/g,"/")).getTime();
            var minStartTime = new Date(self.earliestStartDateTime.replace(/-/g,"/")).getTime();
            if($element.hasClass("w-btn-disabled")){
                return false;
            }
            if(_sTime < minStartTime){
                $17.alert("假期作业最早开始时间为：" + self.earliestStartDateTime);
                return false;
            }

            typeof self.vacationConfirmAssign === 'function' && self.vacationConfirmAssign($element,{
                startTime : _sTime,
                endTime   : _eTime,
                plannedDays : self.focusPlanDay()
            });
            $17.voxLog({
               module : "m_elhqnSjz",
                op    : "confirm_arrangement_click",
                s0    : constantObj.subject
            });
        },
        run : function(){
            var self = this;
            var saveHomeworkPopFn = function(){
                var node = document.getElementById("saveMathDialog");
                ko.cleanNode(node);
                ko.applyBindings(self, node);

                var earliestStartDate = self.earliestStartDateTime.slice(0,10);
                var lastestStartDate = self.latestStartDateTime.slice(0,10);
                $("#startDateInput").datepicker({
                    dateFormat      : 'yy-mm-dd',
                    defaultDate     : self.startDate(),
                    numberOfMonths  : 1,
                    minDate         : earliestStartDate,
                    maxDate         : lastestStartDate,
                    onSelect        : function(selectedDate){
                        var startFocusHour = self.startFocusHour();
                        var startFocusMin = self.startFocusMin();
                        var defaultHour = "00";
                        var defaultMin = "00";
                        var startFocusSec = "00";
                        if(earliestStartDate === selectedDate){
                            var arr = self.splitDateTime(self.earliestStartDateTime);
                            startFocusHour = arr[3];
                            startFocusMin = arr[4];
                            startFocusSec = arr[5];
                            defaultHour = arr[3];
                            defaultMin = arr[4];
                        }else{
                            startFocusHour = "08";
                            startFocusMin = "00";
                            startFocusSec = "00";
                        }
                        var _hourArr = self.getTimeArray(h,defaultHour);
                        var _minArr = self.getTimeArray(m,defaultMin);
                        self.startHourSelect(_hourArr);
                        self.startMinSelect(_minArr);
                        self.startFocusHour(startFocusHour);
                        self.startFocusMin(startFocusMin);
                        self.startDateTime(selectedDate + " " + startFocusHour + ":" + startFocusMin + ":" + startFocusSec);
                    }
                });


                var earliestEndDate = self.earliestEndDateTime.slice(0,10);
                var lastestEndDate = self.latestEndDateTime.slice(0,10);
                $("#endDateInput").datepicker({
                    dateFormat      : 'yy-mm-dd',
                    defaultDate     : self.endDate(),
                    numberOfMonths  : 1,
                    minDate         : earliestEndDate,
                    maxDate         : lastestEndDate,
                    onSelect        : function(selectedDate){
                        var endFocusHour = self.endFocusHour();
                        var endFocusMin = self.endFocusMin();
                        var defaultHour = "00";
                        var defaultMin = "00";
                        var endFocusSec = "00";
                        if(earliestEndDate === selectedDate){
                            var arr = self.splitDateTime(self.earliestEndDateTime);
                            endFocusHour = arr[3];
                            endFocusMin = arr[4];
                            endFocusSec = arr[5];
                            defaultHour = arr[3];
                            defaultMin = arr[4];
                        }else{
                            endFocusHour = "23";
                            endFocusMin = "59";
                            endFocusSec = "00";
                        }
                        var _hourArr = self.getTimeArray(h,defaultHour);
                        var _minArr = self.getTimeArray(m,defaultMin);
                        self.endHourSelect(_hourArr);
                        self.endMinSelect(_minArr);
                        self.endFocusHour(endFocusHour);
                        self.endFocusMin(endFocusMin);
                        self.endDateTime(selectedDate + " " + self.endFocusHour() + ":" + self.endFocusMin() + ":" + endFocusSec);
                    }
                });

                $17.voxLog({
                   module : "m_elhqnSjz",
                    op    : "popup_arrangement_show",
                    s0    : constantObj.subject
                });
            };

            var popState = {
                state1 : {
                    name    : 'loadImage',
                    comment : '布置作业',
                    html    : template("t:LOAD_IMAGE",{}),
                    title   : '布置作业',
                    position: { width : 560},
                    focus   : 1,
                    buttons : {}
                },
                state0 : {
                    name    : 'arrangeHomework',
                    comment : '布置作业',
                    html    : template("t:confirm",{}),
                    title   : '布置作业',
                    position: { width : 560},
                    focus   : 1,
                    buttons : {}
                },
                state2 : {
                    name : 'noTask',
                    comment:'没有可布置的任务',
                    title   : '系统提示',
                    position: { width : 500},
                    focus  : 1,
                    html : "没有可布置的任务",
                    buttons: { "确定": true},
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.close();
                    }
                },
                state3 : {
                    name : 'noClazzs',
                    comment:'没有选择班级',
                    title   : '系统提示',
                    position: { width : 500},
                    focus  : 1,
                    html : "请选择班级",
                    buttons: { "确定": true},
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.close();
                    }
                }
            };

            $.prompt(popState,{
                loaded : function(event){
                    if(!$.isArray(self.clazzNames()) || self.clazzNames().length == 0){
                        $.prompt.goToState('noClazzs');
                    }else if((+self.taskCount || 0) == 0) {
                        $.prompt.goToState('noTask');
                    }else{
                        $.prompt.goToState('arrangeHomework');
                        saveHomeworkPopFn();
                    }
                }
            });
        },
        planDayClick : function(self){
            var planDayObj = this;
            var totalDay = planDayObj.totalDay;
            var focusPlanDayFn = self.focusPlanDay;
            if(totalDay == focusPlanDayFn()){
                return false;
            }
            focusPlanDayFn(totalDay);
        }
    };


    function VacationIndex(){
        var self =  this;
        self.levelsAndBook = null;
        self.weekContent   = null;
        self.vacationCarts = new VactionCarts({
            assignClickCb : self.assignClickCb.bind(self)
        });
        ko.applyBindings(self.vacationCarts,document.getElementById("ufoassign"));
        self.vacationConfirm = null;
    }
    VacationIndex.prototype = {
        constructor : VacationIndex,
        run        : function(){
            var self = this;
            if(!self.levelsAndBook){
                self.levelsAndBook = new LevelsAndBook(undefined,self.levelsAndBookResolve.bind(self),undefined,self.vacationCarts);
                ko.applyBindings(self.levelsAndBook,document.getElementById("levelsAndBook"));
            }
            self.levelsAndBook.run();
        },
        levelsAndBookResolve : function(){
            var self = this;
            if(!self.weekContent){
                self.weekContent = new WeekContent(undefined,self.vacationCarts);
                ko.applyBindings(self.weekContent,document.getElementById("hkTabcontent"));
            }
            self.weekContent.run(self.levelsAndBook.getBookId());
        },
        assignClickCb : function(){
            var self = this;
            self.vacationConfirm = new VacationConfirm({
                taskCount       : self.vacationCarts.getTaskCount(),
                clazzNames      : self.levelsAndBook.checkedClazzNames(),
                earliestStartDateTime : constantObj.earliestStartDateTime,
                startDateTime   : constantObj.defaultStartDateTime,  //yyyy-MM-dd HH:mm:ss
                latestStartDateTime : constantObj.latestStartDateTime,
                earliestEndDateTime : constantObj.earliestEndDateTime,
                endDateTime     : constantObj.defaultEndDateTime,
                latestEndDateTime : constantObj.latestEndDateTime
            },self.vacationConfirmAssign.bind(self));
            self.vacationConfirm.run();
        },
        vacationConfirmAssign : function($element,obj){
            var self = this;
            $element.addClass("w-btn-disabled");
            //确认窗口推荐回调
            var clazzGroupIds = self.levelsAndBook.checkedClazzGroupIds()
                ,bookId = self.levelsAndBook.getBookId()
                ,clazzBookMap = {};
            for(var m = 0,mLen = clazzGroupIds.length; m < mLen; m++){
                clazzBookMap[clazzGroupIds[m]] = {
                    bookId  : bookId,
                    subject : constantObj.subject
                };
            }
            var _homeworkContent = {
                startTime : obj.startTime,
                endTime   : obj.endTime,
                plannedDays : obj.plannedDays,
                clazzBookMap : clazzBookMap
            };
            App.postJSON("/teacher/vacation/assign.vpage", _homeworkContent, function(data){
                if(data.success){
                    var tipText = "假期作业布置成功!";
                    var buttonsObj = {};
                    if(constantObj.adValidaty === "true"){
                        buttonsObj = $.extend(buttonsObj,{ "取消": false, "去抽奖": true });
                    }else{
                        buttonsObj = $.extend(buttonsObj,{ "确定": false });
                    }
                    var popState = {
                        state0 : {
                            name    : 'success_tip',
                            comment :'布置成功提示',
                            title   : '系统提示',
                            focus   : 1,
                            position: { width : 500},
                            html    : tipText,
                            buttons : buttonsObj,
                            submit  : function(e,v,m,f){
                                e.preventDefault();
                                if(v){
                                    location.href = "/teacher/activity/summer/2018/lottery.vpage?track=center";
                                }else{
                                    $.prompt.close(true);
                                }
                            }
                        }
                    };

                    $.prompt(popState,{
                        loaded : function(event){},
                        close : function(){
                            location.href = "/teacher/vacation/report/list.vpage?subject=" + constantObj.subject;
                        }
                    });
                }else{
                    $17.alert(data.info);
                    data.errorCode !== "200" && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/vacation/assign.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(_homeworkContent)
                    });
                }
                $element.removeClass("w-btn-disabled");
            }, function(){
                $17.alert("请求失败，请确认网络情况再重试");
                $element.removeClass("w-btn-disabled");
                $17.voxLog( {
                    module : "API_REQUEST_ERROR",
                    op     : "API_STATE_ERROR",
                    s0     : "/teacher/vacation/assign.vpage",
                    s1     : "请求失败，请确认网络情况再重试",
                    s2     : $.toJSON(_homeworkContent)
                });
            });
        }
    };

    $17.vacation = $17.vacation || {};
    $17.extend($17.vacation, {
        getVacationIndex: function(){
            return new VacationIndex();
        }
    });
}(window,$17,constantObj));