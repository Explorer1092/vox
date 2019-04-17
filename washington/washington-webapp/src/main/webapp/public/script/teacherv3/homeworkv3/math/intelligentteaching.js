/**
 * 数学课时讲练测,不支持单题选入
 * Created by dell on 2017/7/17.
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
    /*
     * 下拉框的显示隐藏
     * */
    ko.bindingHandlers.pullDownHover = {
        init: function(element, valueAccessor){
            $(element).hover(
                function(){
                    $(element).find("ul.starBoxSelect").show();
                },
                function(){
                    $(element).find("ul.starBoxSelect").hide();
                }
            );
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

    var IntelligentTeaching = function(){
        var self = this;
        self.tabType         = "";
        self.examLoading     = ko.observable(true); //正在加载应试
        self.sectionIds      = [];
        self.loadExamInitialize = false;
        self.packageList     = ko.observableArray([]);
        self.packageQuestionsMap = {};
        self.currentPage     = ko.observable(1);
        self.userInputPage   = ko.observable(null);
        self.focusExamList   = ko.observableArray([]);

        self.focusExamList.subscribe(self.setExamChecked,self);
        self.focusPackageIndex = ko.observable(0); //当前焦点题包在packageList的下标
        self.focusPackage = ko.pureComputed(function(){
            return self.packageList()[self.focusPackageIndex()];
        });
        //存放H5返回的句柄
        self.questionHandles = [];
        self.clazzGroupIdsStr = null;
        self.subject = constantObj.subject;
        self.param  = {};
        self.pagination = $17.pagination.initPages({
            pageClickCb : self.page_click.bind(self)
        });
    };
    IntelligentTeaching.prototype = {
        constructor       : IntelligentTeaching,
        param             : {},
        assigns           : ko.observableArray([]),
        run               : function(obj){
            var self = this,paramData = {
                bookId   : self.param.bookId,
                unitId   : self.param.unitId,
                sections : self.sectionIds.toString(),
                type     : self.tabType,
                subject  : constantObj.subject,
                clazzs   : null,
                objectiveConfigId : self.param.objectiveConfigId
            };
            obj = $.isPlainObject(obj) ? obj : {};
            self.clazzGroupIdsStr = obj.clazzGroupIdsStr;
            paramData.clazzs = obj.clazzGroupIdsStr;

            self.examLoading(true);
            $.get("/teacher/new/homework/objective/content.vpage", paramData, function(data){
                if(data.success){
                    var _boxQuestionMap = self._getBoxQuestionMap(),
                        _content = data.content || [],
                        _packages = [];
                    var _realPackages = _content || [];
                    for(var z = 0, zLen = _realPackages.length; z < zLen; z++){
                        var _packageObj = {},
                            _boxId = _realPackages[z].id,
                            totalSec = _realPackages[z].seconds || 0;
                        _packageObj["id"] = _boxId;
                        _packageObj["name"] = _realPackages[z].title;
                        _packageObj["selCount"] = _boxQuestionMap[_realPackages[z].id] ? _boxQuestionMap[_realPackages[z].id].length : 0;
                        _packageObj["flag"] = "package";
                        _packageObj["totalCount"] = _realPackages[z].questions.length || 0;
                        //题目是否全部使用过
                        _packageObj["teacherUsed"] = _realPackages[z].showAssigned || false;
                        _packageObj["totalMin"] = Math.ceil(totalSec/60);
                        _packageObj["usageName"] = _realPackages[z].usageName || '';
                        _packageObj['usageColor'] = _realPackages[z].usageColor || '';
                        _packageObj['difficulty'] = (+_realPackages[z].difficulty || 0);
                        _packageObj['algoType'] = _realPackages[z].algoType || "";
                        _packages.push(_packageObj);
                        self.packageQuestionsMap[_boxId] = _realPackages[z].questions || [];
                    }
                    self.packageList(ko.mapping.fromJS(_packages)());
                    if(_packages.length > 0){
                        self.forwardSpecialPackage.call(self.focusPackage(),self,self.focusPackageIndex());
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
        _resetExamList : function(_questions){
            var self = this;
            if(!$.isArray(_questions)){
                _questions = [];
            }
            for(var k = 0,kLen = _questions.length; k < kLen; k++){
                _questions[k]["checked"] = false;
                _questions[k]["upImage"] = _questions[k].upImage || false;
            }
            self.examQuestions = _questions;
            var startPage = 1;
            var _startIndex = (startPage - 1) * 5;
            var _subExamQuestions = _questions.slice(_startIndex, _startIndex + 5);

            self.questionList = _questions;
            self.focusExamList(ko.mapping.fromJS(_subExamQuestions)());
            self.pagination.setPage(startPage,Math.ceil(_questions.length/5));
            self.userInputPage(null);
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
        _setPackageProperties : function(customPackage,questions){
            var self = this;
            self.packageList()[self.focusPackageIndex()].id(customPackage.id);
            self.packageList()[self.focusPackageIndex()].selCount(customPackage.selCount);
            self.packageList()[self.focusPackageIndex()].totalCount(customPackage.totalCount);
            self.packageList()[self.focusPackageIndex()].totalMin(customPackage.totalMin);
            self.packageList()[self.focusPackageIndex()].difficulty(customPackage.difficulty);
        },
        forwardSpecialPackage : function(self,index){
            var that = this,
                focusPackage = self.focusPackage(),
                _focusBoxId = that.id(); //this -> package obj
            self.focusPackageIndex(index);
            self._resetExamList(self.packageQuestionsMap[_focusBoxId]);
        },
        viewPackage : function(self,index){
            var that = this,
                _focusPackageIndex = self.focusPackageIndex();
            if(_focusPackageIndex == index){
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
        addOrRemovePackage  : function(){
            var self = this,
                that = self.focusPackage(),
                _selCount = that.selCount(),
                _totalCount = that.totalCount(),
                _packageId = that.id(),
                _questions = self.packageQuestionsMap[_packageId] || [];
            var packageName = that.name();
            if(_selCount >= _totalCount){
                //取消勾选
                var removeSeconds = 0;
                if(_questions.length > 0){
                    for(var t = 0,tLen = _questions.length; t < tLen; t++){
                        if(self._removeExam(_packageId,_questions[t])){
                            //有，删除
                            removeSeconds += _questions[t].seconds;
                        }
                    }
                    self.setExamChecked();
                }
                self.packageList()[self.focusPackageIndex()].selCount(0);

                //更新UFO_EXAM
                self.updateUfoExam(0 - removeSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);

                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_tongbu_PackageDetail_DelselectAll_click",
                    s0    : constantObj.subject,
                    s1    : that.name(),
                    s2    : _packageId
                });

            }else{
                //全选
                var addSeconds = 0;
                var cnt = 0;
                var beenSelected = 0; //在其他题包已选过计数,同题包中不会出现重题
                if(_questions.length > 0){
                    var _boxSelQuestionMap = self._getBoxQuestionMap();
                    var _boxSelQuestions = _boxSelQuestionMap[_packageId] || [];
                    for(var z = 0,zLen = _questions.length; z < zLen; z++){
                        var qId = _questions[z].id;
                        var existsQuestionFlag =  self._isExistsByQuestionId(qId);
                        if(existsQuestionFlag == -1 && self._addExam(_packageId,_questions[z],packageName).success){
                            addSeconds +=  _questions[z].seconds;
                            cnt++;
                        }else if(existsQuestionFlag != -1 && _boxSelQuestions.indexOf(qId) == -1){
                            beenSelected++;
                        }
                    }
                    self.setExamChecked();
                }
                cnt = self.packageList()[self.focusPackageIndex()].selCount() + cnt;
                self.packageList()[self.focusPackageIndex()].selCount(cnt);
                //更新UFO_EXAM
                self.updateUfoExam(addSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);
                if(beenSelected > 0){
                    $17.alert("有" + beenSelected + "道题与已选题目重复");
                }
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_tongbu_PackageDetail_selectAll_click",
                    s0    : constantObj.subject,
                    s1    : that.name(),
                    s2    :  _packageId
                });
            }

        },
        setExamChecked : function(){
            //设置当前页面显示的题的选中状态
            var self = this; //Exam
            $17.info("设置当前页中题的状态");
            var _boxQuestionMap = self._getBoxQuestionMap();
            var _selectQuestions = self.focusPackage() ? (_boxQuestionMap[self.focusPackage().id()] || []) : [];

            var _focusExamList = self.focusExamList();
            for(var z = 0,zLen = _focusExamList.length; z < zLen; z++){
                var _questionId = _focusExamList[z].id();

                var _checked = _focusExamList[z].checked();
                if(_selectQuestions.length > 0 && !_checked && _selectQuestions.indexOf(_questionId) != -1){
                    self.focusExamList()[z].checked(true);
                }else if(_selectQuestions.length == 0 && _checked){
                    self.focusExamList()[z].checked(false);
                }
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
                    if(_questions[m].questionId === questionId && _questionIndex == -1){
                        _questionIndex = m;
                    }
                }
            }
            return {
                selectCount : cnt,
                questionIndex : _questionIndex
            };
        },
        _addExam    : function(currentBoxId,question,packageName){
            var self = this,param = self.param || {};
            //内部方法，约定currentBoxId,question合法的,currentBoxId,question的合法性放在外面判断
            var _questionId = question.id,
                ids = [],
                existsQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
            existsQuestions = $.isArray(existsQuestions) ? existsQuestions : [];
            $.each(existsQuestions,function(i,question){
                ids.push(question.questionId);
            });
            if(ids.indexOf(_questionId) == -1){
                var _similarIds = question.similarQuestionIds || [],
                    _questionObj = {
                        questionId          : _questionId,
                        seconds             : question.seconds,
                        submitWay           : question.submitWay,
                        questionBoxId       : currentBoxId,
                        questionBoxName     : packageName,
                        courseId            : question["courseId"],
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
            if(_questionIndex != -1){
                constantObj._homeworkContent.practices[self.tabType].questions.splice(_questionIndex,1);

                $.each(constantObj._reviewQuestions[self.tabType],function(i){
                    if(this.id == _questionId){
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
        page_click : function(pageNo){
            var self = this;
            pageNo = +pageNo || 0;
            if(pageNo < 1){
                return false;
            }
            var _startIndex = (pageNo - 1) * 5;
            self.focusExamList(ko.mapping.fromJS(self.questionList.slice(_startIndex,_startIndex + 5))());
        },
        loadExamImg : function(examId,index,boxId){
            var self = this;
            boxId = boxId || "";
            var prefixBoxId = boxId.slice(0,1);
            boxId = (prefixBoxId === "#" ? boxId : "#" + boxId);
            if(!$17.isBlank(examId) && self.loadExamInitialize){
                var $mathExamImg = $(boxId);
                $mathExamImg.empty();
                var generateExamId = "examImg-" + index + "_" + new Date().getTime();
                $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id",generateExamId).appendTo($mathExamImg);
                var node = document.getElementById(generateExamId);
                var obj = self.renderExam(examId,node);
                if(self.questionHandles){
                    self.questionHandles[index] = obj;
                }
                $17.examExposureLog({
                    subject         : constantObj.subject,
                    homeworkType    : self.tabType,
                    packageId       : self.focusPackage() && self.focusPackage().id ? self.focusPackage().id() : null,
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

            //换单元，页码初始化,初始化题包
            self.currentPage(1);
            self.focusPackageIndex(0);

            self.carts = option.carts || null;
            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length === 0){
                $ufoexam.empty().html(template("t:UFO_EXAM",{tabTypeName : option.tabTypeName,count : 0}));
            }
        },
        feedback : function(self){
            var that = this; //single exam
            var _questionId = that.id();
            var _currentBoxId = null;
            if(self.focusPackage() && self.focusPackage().id){
                _currentBoxId = self.focusPackage().id();
            }
            $.prompt("<div><span class='text_blue'>如果您发现题目出错了，请及时反馈给我们，感谢您的支持！</span><textarea id='feedbackContent' cols='91' rows='8' style='width: 94%' class='int_vox'></textarea><p class='init text_red'></p></div>", {
                title: "错题反馈", focus: 1, buttons: {"取消": false, "提交": true}, submit: function(e, v){
                    if(v){
                        var feedbackContent = $("#feedbackContent")
                            ,paramData = {
                            feedbackType: 4,
                            examId      : that.id(),
                            content     : feedbackContent.val()
                        };
                        if($17.isBlank(feedbackContent.val())){
                            feedbackContent.siblings(".init").html("错题反馈不能为空。");
                            feedbackContent.focus();
                            return false;
                        }
                        $.post("/project/examfeedback.vpage", paramData, function(data){
                            if(data.success){
                                $17.alert("提交成功，感谢您的支持！");
                            }else{
                                $17.voxLog({
                                    module : "API_REQUEST_ERROR",
                                    op     : "API_STATE_ERROR",
                                    s0     : "/project/examfeedback.vpage",
                                    s1     : $.toJSON(data),
                                    s2     : $.toJSON(paramData),
                                    s3     : $uper.env
                                });
                            }
                        });
                        $17.voxLog({
                            module: "m_H1VyyebB",
                            op    : "page_assign_tongbu_feedback_popup_submit_click",
                            s0    : constantObj.subject,
                            s1    : self.tabType,
                            s2    : _currentBoxId,
                            s3    : _questionId
                        });
                    }
                },
                loaded : function(){
                    $17.voxLog({
                        module: "m_H1VyyebB",
                        op    : "page_assign_tongbu_package_feedback_popup_show",
                        s0    : constantObj.subject,
                        s1    : self.tabType,
                        s2    : _currentBoxId,
                        s3    : _questionId
                    });
                }
            });

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_assign_tongbu_package_feedback_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : _currentBoxId,
                s3    : _questionId
            });
        },
        viewExamAnswer : function(self,index){
            var that = this; //single exam
            var _questionId = that.id();
            var _currentBoxId = null;
            if(self.focusPackage() && self.focusPackage().id){
                _currentBoxId = self.focusPackage().id();
            }
            if(self.questionHandles[index]){
                self.questionHandles[index].showAnalysis(that.id());
            }

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_assign_tongbu_question_package_answerKey_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : _currentBoxId,
                s3    : _questionId
            });
        },
        viewSimilarQuestion : function(self){
            var that = this; // single exam
            var allConversation = {
                questionDetail: {
                    title       : '题目详情',
                    html        : template("T:SIMILAR_QUESTIONS", {}),
                    position    : { width: 770},
                    buttons     : {},
                    focus       : 1,
                    submit:function(e,v,m,f){}
                }
            };
            var _similarIds;
            if(that.similarQuestionIds){
                _similarIds = that.similarQuestionIds() || [];
            }else{
                _similarIds = [];
            }

            var _questionId = that.id();
            var _boxId = self.focusPackage().id();
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_similarRecommend_click",
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : _boxId,
                s3 : _questionId
            });

            var previewModel = {
                questionIds : _similarIds,
                focusIndex  : 0,
                getFocusQuestionId : function(){
                    var vm = this;
                    return vm.questionIds[vm.focusIndex];
                },
                loadExamImg : function(){
                    var vm = this;
                    var examId = vm.getFocusQuestionId();
                    if(!$17.isBlank(examId) && self.loadExamInitialize){
                        var $mathExamImg = $("#mathExamImgSource");
                        $mathExamImg.empty();
                        $("<div></div>").attr("id","similarExamImg0").appendTo($mathExamImg);
                        var node = document.getElementById("similarExamImg0");
                        var obj = self.renderExam(examId,node);
                    }else{
                        $("#mathExamImgSource").html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
                    }
                    return "";
                },
                nextQuestion:function(){
                    var vm = this;
                    var newIndex = vm.focusIndex + 1;
                    if(newIndex >= vm.questionIds.length){
                        newIndex = 0;
                    }
                    vm.focusIndex = newIndex;
                    vm.loadExamImg();

                    $17.voxLog({
                        module: "m_H1VyyebB",
                        op : "page_assign_similarRecommend_popup_change_click",
                        s0 : constantObj.subject,
                        s1 : self.tabType,
                        s2 : _boxId,
                        s3 : _questionId
                    });

                },
                init : function(){
                    var vm = this;
                }
            };
            $.prompt(allConversation,{
                loaded : function(){
                    if(that.checked()){
                        var _tempObj = self._getSpecialBoxInfo(_boxId,_questionId);
                        var _questionIndex = _tempObj.questionIndex;
                        if(_questionIndex != -1){
                            var _questions = constantObj._homeworkContent.practices[self.tabType].questions;
                            var similarQuestionId = _questions[_questionIndex]["similarQuestionId"];
                            var similarIndex = previewModel.questionIds.indexOf(similarQuestionId);
                            previewModel.focusIndex = (similarIndex == -1 ? 0 : similarIndex);
                        }
                    }
                    ko.applyBindings(previewModel, document.getElementById("viewSimilarQuestions"));

                    $17.voxLog({
                        module: "m_H1VyyebB",
                        op : "page_assign_similarRecommend_popup_show",
                        s0 : constantObj.subject,
                        s1 : self.tabType,
                        s2 : _boxId,
                        s3 : _questionId
                    });
                },
                close : function () {
                    if(that.checked()){
                        var _tempObj = self._getSpecialBoxInfo(_boxId,_questionId);
                        var _questionIndex = _tempObj.questionIndex;
                        if(_questionIndex != -1){
                            var _questions = constantObj._homeworkContent.practices[self.tabType].questions;
                            _questions[_questionIndex]["similarQuestionId"] = previewModel.getFocusQuestionId();
                        }
                    }else{
                        //什么也不做
                    }
                }
            });
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
        viewCourse : function(self){
            var question = this;
            switch (self.tabType) {
                case "INTELLIGENT_TEACHING":
                    var gameUrl = "/teacher/new/homework/previewteachingcourse.vpage?" + $.param({
                        courseId : question["courseId"]
                    });
                    var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>';
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
                default:
                    break;
            }
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getIntelligent_teaching : function(){
            return new IntelligentTeaching();
        }
    });
}($17,ko));
