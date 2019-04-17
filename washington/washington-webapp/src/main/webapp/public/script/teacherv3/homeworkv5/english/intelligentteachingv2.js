/**
 * 英语课时讲练测,不支持单题选入
 * Created by dell on 2018/10/08.
 */
(function($17,ko) {
    "use strict";
    ko.bindingHandlers.singleExamHover = {
        init: function(element, valueAccessor){
            $(element).hover(
                function(){
                    $(element).addClass("current");
                    $(element).find("a.feedback").show();
                    $(element).find("a.viewExamAnswer").show();
                },
                function(){
                    var _value = ko.unwrap(valueAccessor());
                    if(!_value){
                        $(element).removeClass("current");
                    }
                    $(element).find("a.feedback").hide();
                    $(element).find("a.viewExamAnswer").hide();
                }
            );
        },
        update:function(element, valueAccessor){
            var _value = ko.unwrap(valueAccessor());
            if(_value){
                $(element).addClass("current");
            }else{
                $(element).removeClass("current");
            }
        }
    };
    // Here's a custom Knockout binding that makes elements shown/hidden via jQuery's fadeIn()/fadeOut() methods
    ko.bindingHandlers.fadeVisible = {
        init: function(element, valueAccessor) {
            // Initially set the element to be instantly visible/hidden depending on the value
            var value = valueAccessor();
            $(element).toggle(ko.unwrap(value)); // Use "unwrapObservable" so we can handle values that may or may not be observable
        },
        update: function(element, valueAccessor) {
            // Whenever the value subsequently changes, slowly fade the element in or out
            var value = valueAccessor();
            ko.unwrap(value) ? $(element).fadeIn() : $(element).fadeOut();
        }
    };

    function IntelligentTeachingSmallPackage(params){
        this.smallPackage = params.smallPackage;
        this.showHeader = params.showHeader;
        this.questions = ko.observableArray([]);
        this.postQuestions = ko.observableArray([]);
        this.focusExamMap = {};
        var questionIds = [];
        var questions = this.smallPackage.questions;
        if(questions && questions().length > 0){
            ko.utils.arrayForEach(questions(),function(question){
                questionIds.push(question.id());
            });
        }
        var postQuestions = this.smallPackage.postQuestions;
        if(postQuestions && postQuestions().length > 0){
            ko.utils.arrayForEach(postQuestions(),function(question){
                questionIds.push(question.id());
            });
        }
        var self = this;
        $17.QuestionDB.getQuestionByIds(questionIds,function(result){
            self.focusExamMap = result.success ? result.questionMap : {};
            self.questions(self.smallPackage.questions());
            self.postQuestions(self.smallPackage.postQuestions());
        });
        this.viewCourseFn = params.viewCourse;
        this.addOrRemoveSmallPackageFn = params.addOrRemoveSmallPackage;

    }
    IntelligentTeachingSmallPackage.prototype = {
        constructor : IntelligentTeachingSmallPackage,
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
        viewCourse : function(){
            // console.info(this);
            this.viewCourseFn(ko.mapping.toJS(this.smallPackage));
        },
        addOrRemoveSmallPackage : function(){
            this.addOrRemoveSmallPackageFn(this.smallPackage);
        }
    };

    //注册一个自定义标签
    ko.components.register('intelligent-teaching-smallpackage', {
        viewModel: IntelligentTeachingSmallPackage,
        template: {element : 't:intelligent-teaching-smallpackage'}
    });


    var IntelligentTeaching = function(){
        var self = this;
        self.tabType         = "";
        self.examLoading     = ko.observable(true); //正在加载应试
        self.sectionIds      = [];
        self.loadExamInitialize = false;
        self.bigPackageList     = ko.observableArray([]);
        self.focusBigPackageIndex = ko.observable(0); //当前焦点题包在packageList的下标
        self.focusBigPackage = ko.pureComputed(function(){
            return self.bigPackageList()[self.focusBigPackageIndex()];
        });
        self.smallPackageList = ko.observableArray([]);
        self.param  = {};
        self.clazzGroupIdsStr = null;
    };
    IntelligentTeaching.prototype = {
        constructor       : IntelligentTeaching,
        assigns           : ko.observableArray([]),
        run               : function(obj){
            obj = $.isPlainObject(obj) ? obj : {};
            var self = this,paramData = {
                bookId   : self.param.bookId,
                unitId   : self.param.unitId,
                sections : self.sectionIds.toString(),
                type     : self.tabType,
                subject  : constantObj.subject,
                clazzs   : obj.clazzGroupIdsStr ? obj.clazzGroupIdsStr : null,
                objectiveConfigId : self.param.objectiveConfigId
            };
            self.clazzGroupIdsStr = paramData.clazzs;
            self.examLoading(true);
            $.get("/teacher/new/homework/objective/content.vpage", paramData, function(data){
                if(data.success){
                    var _boxQuestionMap = self._getBoxQuestionMap(),
                        _content = data.content || [],
                        _packages = [];
                    var _realPackages = _content || [];
                    for(var z = 0, zLen = _realPackages.length; z < zLen; z++){
                        var totalSec = _realPackages[z].seconds || 0;

                        var selectAll = true;
                        var questionCount = 0;
                        //小题包下有一个题目在购物车里，小题包就算选中
                        _realPackages[z].smallPackages.forEach(function(smallPackage,index){
                            smallPackage["checked"] = _boxQuestionMap.hasOwnProperty(smallPackage.id);
                            selectAll = selectAll && smallPackage["checked"];
                            questionCount += smallPackage.questions.length;
                            var questionIds = _boxQuestionMap[smallPackage.id] || [];
                            smallPackage.questions.forEach(function(question,index){
                                question["checked"] = questionIds.indexOf(question.id) !== -1;
                            });
                        });

                        _packages.push($.extend(true,_realPackages[z],{
                            name            : _realPackages[z].title,
                            smallPackages   : _realPackages[z].smallPackages || [],
                            flag            : "package",
                            totalCount      : questionCount,
                            selectAll       : selectAll,
                            totalMin        : Math.ceil(totalSec/60)
                        }));

                    }
                    self.bigPackageList(ko.mapping.fromJS(_packages)());
                    if(_packages.length > 0){
                        self.forwardSpecialPackage.call(self.focusBigPackage(),self,self.focusBigPackageIndex());
                    }
                }else{
                    self.packageList([]);
                    self.questionList = [];
                    data.errorCode !== "200" && $17.voxLog( {
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
                self.examLoading(false);
            });
        },
        _getBoxQuestionMap : function(){
            var self = this;
            var _exams = constantObj._homeworkContent.practices[self.tabType].questions || [];
            var _packageDetail = {};
            for(var z = 0,zLen = _exams.length; z < zLen; z++){
                var _questionBoxId = _exams[z].questionBoxId;
                if(!$17.isBlank(_questionBoxId)){
                    if(!$.isArray(_packageDetail[_questionBoxId])){
                        _packageDetail[_questionBoxId] = [];
                    }
                    _packageDetail[_questionBoxId].push(_exams[z].questionId);
                }
            }
            return _packageDetail;
        },
        _isExistsByQuestionId : function(questionId){
            var self = this;
            var _exams = constantObj._homeworkContent.practices[self.tabType].questions || [];
            var _index = -1;
            for(var z = 0,zLen = _exams.length; z < zLen; z++){
                if(_exams[z].questionId && _exams[z].questionId === questionId){
                    _index = z;
                    break;
                }
            }
            return _index;
        },
        forwardSpecialPackage : function(self,index){
            self.focusBigPackageIndex(index);
            var that = this,
                focusBigPackage = self.focusBigPackage(); //this -> package obj
            self.smallPackageList(focusBigPackage.smallPackages());
        },
        viewPackage : function(self,index){
            var that = this,
                _focusBigPackageIndex = self.focusBigPackageIndex();
            if(_focusBigPackageIndex == index){
                //同一包的点击
                return false;
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "assignhomework_tongbu_Scenes_click",
                s0 : constantObj.subject,
                s1 : that.name(),
                s2 : that.id()
            });
            self.forwardSpecialPackage.call(that,self,index);
        },
        addOrRemoveSmallPackage : function(smallPackage){
            // smallPackage ko 对象
            var self = this,
                that = smallPackage,
                _packageId = that.id(),
                _questions = ko.mapping.toJS(smallPackage.questions()) || [];
            var packageName = that.title();
            var courseId = that.courseId();
            var beenSelected = 0; //在其他题包已选过计数,同题包中不会出现重题
            if(that.checked && that.checked()){
                //取消勾选
                var removeSeconds = 0;
                if(_questions.length > 0){
                    for(var t = 0,tLen = _questions.length; t < tLen; t++){
                        if(self._removeExam(_packageId,_questions[t])){
                            //有，删除
                            removeSeconds += _questions[t].seconds;
                        }
                    }
                }
                self.bigPackageList()[self.focusBigPackageIndex()].selectAll(false);
                //更新UFO_EXAM
                self.updateUfoExam(0 - removeSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);
            }else{
                //全选
                var addSeconds = 0;
                if(_questions.length > 0){
                    var _boxSelQuestionMap = self._getBoxQuestionMap();
                    var _boxSelQuestions = _boxSelQuestionMap[_packageId] || [];
                    for(var z = 0,zLen = _questions.length; z < zLen; z++){
                        var qId = _questions[z].id;
                        var existsQuestionFlag =  self._isExistsByQuestionId(qId);
                        if(existsQuestionFlag === -1 && self._addExam(_packageId,_questions[z],packageName,courseId).success){
                            addSeconds +=  _questions[z].seconds;
                        }else if(existsQuestionFlag !== -1 && _boxSelQuestions.indexOf(qId) === -1){
                            beenSelected++;
                        }
                    }
                }
                var selectAll = true;
                ko.utils.arrayForEach(self.smallPackageList(),function (smallPackage, index) {
                    // console.info(index,smallPackage.id(),smallPackage.checked());
                    (smallPackage.id() !== _packageId) && (selectAll = selectAll && smallPackage.checked());
                });
                self.bigPackageList()[self.focusBigPackageIndex()].selectAll(selectAll);
                //更新UFO_EXAM
                self.updateUfoExam(addSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);

            }
            that.checked(!(that.checked && that.checked()));
            return beenSelected;
        },
        addOrRemovePackage  : function(){
            var self = this;
            var that = self.focusBigPackage();
            var _packageId = that.id(),title = that.name();
            var selectAll = (that.selectAll && that.selectAll());
            if(selectAll){
                //取消全选
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_tongbu_PackageDetail_DelselectAll_click",
                    s0    : constantObj.subject,
                    s1    : title,
                    s2    : _packageId
                });
            }else{
                //全选
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_tongbu_PackageDetail_selectAll_click",
                    s0    : constantObj.subject,
                    s1    : title,
                    s2    :  _packageId
                });
            }
            var beenSelected = 0;
            ko.utils.arrayForEach(self.smallPackageList(),function(smallPackage,index){
                var smallPackageChecked = smallPackage.checked && smallPackage.checked();
                if(selectAll === smallPackageChecked){
                    beenSelected += self.addOrRemoveSmallPackage(smallPackage)
                }else{
                    //当前包下的题目状态与目前大题包状态一致的题
                    var unCheckedQuestions = [];
                    ko.utils.arrayForEach(smallPackage.questions(),function(question){
                        (selectAll === question.checked()) && (unCheckedQuestions.push(question.id()));
                    });
                    if(unCheckedQuestions.length > 0){
                        if(!selectAll){
                            //因为有一个题目选中，小题包就选中，所以做一次反操作
                            beenSelected += self.addOrRemoveSmallPackage(smallPackage)
                        }
                        beenSelected += self.addOrRemoveSmallPackage(smallPackage)
                    }
                }
            });
            if(beenSelected > 0){
                $17.alert("有" + beenSelected + "道题与已选题目重复");
            }
        },
        updateUfoExam : function(sec,questionCnt){
            var self = this;
            constantObj._moduleSeconds[self.tabType] = constantObj._moduleSeconds[self.tabType] + sec;
            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(self.tabType,questionCnt);
        },
        _getSpecialBoxInfo : function(boxId,questionId){
            //返回指定的包ID下选择的题数，指定题ID在指定包中的下标，没有返回-1
            var self = this;
            var _questions = constantObj._homeworkContent.practices[self.tabType].questions;
            var _questionIndex = -1;
            var cnt = 0;
            for(var m = 0,mLen = _questions.length; m < mLen; m++){
                if(_questions[m].questionBoxId === boxId){
                    cnt++;
                    if(_questions[m].questionId === questionId && _questionIndex === -1){
                        _questionIndex = m;
                    }
                }
            }
            return {
                selectCount : cnt,
                questionIndex : _questionIndex
            };
        },
        _addExam    : function(currentBoxId,question,packageName,courseId){
            var self = this,param = self.param || {};
            //内部方法，约定currentBoxId,question合法的,currentBoxId,question的合法性放在外面判断
            var _questionId = question.id,
                ids = [],
                existsQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
            existsQuestions = $.isArray(existsQuestions) ? existsQuestions : [];
            $.each(existsQuestions,function(i,question){
                ids.push(question.questionId);
            });
            if(ids.indexOf(_questionId) === -1){
                var _similarIds = question.similarQuestionIds || [],
                    _questionObj = {
                        questionId          : _questionId,
                        seconds             : question.seconds,
                        submitWay           : question.submitWay,
                        questionBoxId       : currentBoxId,
                        questionBoxName     : packageName,
                        courseId            : courseId,
                        similarQuestionId   : _similarIds.length > 0 ? _similarIds[0] : null,
                        book                : question.book || null,
                        objectiveId         : param.objectiveTabType
                    };
                constantObj._homeworkContent.practices[self.tabType].questions.push(_questionObj);
                constantObj._reviewQuestions[self.tabType].push(question);
                return {
                    success : true,
                    info    : "添加成功"
                };
            }else{
                return {
                    success : false,
                    info    : "添加失败"
                };
            }
        },
        _removeExam : function(currentBoxId,question){
            var self = this;
            var _questionId = question.id;
            var _tempObj = self._getSpecialBoxInfo(currentBoxId,_questionId);
            var _questionIndex = _tempObj.questionIndex;
            if(_questionIndex !== -1){
                constantObj._homeworkContent.practices[self.tabType].questions.splice(_questionIndex,1);

                $.each(constantObj._reviewQuestions[self.tabType],function(i){
                    if(this.id === _questionId){
                        constantObj._reviewQuestions[self.tabType].splice(i,1);
                        return false;
                    }
                });
                return true;
            }else{
                $17.info("已经移除过这道题了");
            }
            return false;
        },
        clearAll  : function(){
            var self = this;
            var _packageList = self.packageList();
            for(var z = 0,zLen = _packageList.length; z < zLen; z++){
                _packageList[z].selCount(0);
            }
            constantObj._homeworkContent.practices[self.tabType].questions = [];
            constantObj._reviewQuestions[self.tabType] = [];
            self.setExamChecked();
        },
        loadExamImg : function(examId,index,boxId){
            var self = this;
            boxId = boxId || "";
            var prefixBoxId = boxId.slice(0,1);
            boxId = (prefixBoxId === "#" ? boxId : "#" + boxId);
            if(!$17.isBlank(examId) && self.loadExamInitialize){
                var $mathExamImg = $(boxId);
                $mathExamImg.empty();
                $('<div class="subjectNum" data-tip="题号"><span>' + (index + 1) + '</span></div>').appendTo($mathExamImg);
                var generateExamId = "examImg-" + index + "_" + new Date().getTime();
                $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id",generateExamId).appendTo($mathExamImg);
                var node = document.getElementById(generateExamId);
                var obj = self.renderExam(examId,node);
                $17.examExposureLog({
                    subject         : constantObj.subject,
                    homeworkType    : self.tabType,
                    packageId       : self.focusBigPackage() && self.focusBigPackage().id ? self.focusBigPackage().id() : null,
                    examId          : examId,
                    clazzGroups     : self.clazzGroupIdsStr
                });
            }else{
                $(boxId).html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
            }
            return "";
        },
        renderExam        : function(examId,node){
            return vox.exam.render(node, 'normal', {
                ids       : [examId],
                imgDomain : constantObj.imgDomain,
                env       : constantObj.env,
                domain    : constantObj.domain
            });
        },
        initialise        : function(option){
            var self = this;
            option = option || {};
            self.param = $.extend(true,{},option);
            self.tabType = option.tabType; //必传字段
            var _sectionIds = [];
            $.each(option.sections,function(i,section){
                _sectionIds.push(section.sectionId);
            });
            self.sectionIds  = _sectionIds;
            self.loadExamInitialize = option.examInitComplete || false;

            //换单元，初始化题包
            self.focusBigPackageIndex(0);

            self.carts = option.carts || null;
            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length === 0){
                $ufoexam.empty().html(template("t:UFO_EXAM",{tabTypeName : option.tabTypeName,count : 0}));
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
                $17.tongji('voxExamCreate','examCoreJs_error',exception.message);
            }
        },
        viewCourse : function(packageInfo){
            var self = this;
            var data;
            switch (self.tabType) {
                case "INTELLIGENT_TEACHING":
                    var gameUrl = "/teacher/new/homework/previewteachingcourse.vpage?" + $.param({
                        courseId : packageInfo["courseId"]
                    });
                    data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>';
                    $.prompt(data, {
                        title   : "预 览（电脑端预览无法播放老师讲课音频，请下载app预览）",
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
                            window.addEventListener("message",function(e){
                                $.prompt.close();
                            });
                        }
                    });
                    break;
                case "ORAL_INTELLIGENT_TEACHING":
                    var videoUrl = packageInfo.videoUrl;
                    var thumbnail = packageInfo["thumbnail"];
                    if(!videoUrl){
                        return false;
                    }
                    $.prompt(template("t:video_js_preview_popup",{
                        videoList : [videoUrl],
                        poster : thumbnail
                    }), {
                        title   : "预 览",
                        buttons : {},
                        position: { width: 700 },
                        close   : function(){}
                    });
                    break;
                default:
                    break;
            }
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getIntelligent_teaching : function(){
            return new IntelligentTeaching();
        },
        getOral_intelligent_teaching : function(){
            return new IntelligentTeaching();
        }
    });
}($17,ko));