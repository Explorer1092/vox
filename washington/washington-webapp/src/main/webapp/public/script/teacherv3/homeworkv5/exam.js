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

    var Exam = function(){
        var self = this;
        self.tabType         = "";
        self.examLoading     = ko.observable(true); //正在加载应试
        self.patterns        = ko.observableArray([]);
        self.words           = ko.observableArray([]);
        self.wordCart        = ko.observableArray([]);
        self.wordAllChecked  = ko.pureComputed(function(){
            return self.words().length == self.wordCart().length;
        });
        self.categories      = ko.observableArray([]);
        self.categoryCart    = ko.observableArray([]);
        self.categoryAllChecked = ko.pureComputed(function(){
            return self.categories().length == self.categoryCart().length;
        });
        self.sectionIds      = [];
        self.packageList     = ko.observableArray([]);
        self.packageQuestionsMap = {};
        self.examQuestions   = [];   //更多题目中全部的应试题
        self.questionList    = [];   //筛选后符合条件的应试题集
        self.currentPage     = ko.observable(1);
        self.userInputPage   = ko.observable(null);
        self.focusExamList   = ko.observableArray([]);
        self.focusExamMap = {};
        self.focusExamList.subscribe(self.setExamChecked,self);
        self.focusPackageIndex = ko.observable(0); //当前焦点题包在packageList的下标
        self.focusPackage = ko.pureComputed(function(){
            return self.packageList()[self.focusPackageIndex()];
        });

        self.totalPage = ko.observable(0);
        self.patternIsCheckedAll = ko.pureComputed(function(){
            var _patterns = self.patterns();
            var _checked = true;
            for(var z = 0,zLen = _patterns.length; z < zLen; z++){
                _checked = _checked && _patterns[z].checked();
            }
            return _checked;
        });
        self.difficulties           = ko.observableArray([]);
        self.difficultyIsCheckedAll = ko.pureComputed(function(){
            var _difficulties = self.difficulties();
            var _checked = true;
            for(var z = 0,zLen = _difficulties.length; z < zLen; z++){
                _checked = _checked && _difficulties[z].checked();
            }
            return _checked;
        });
        self.testMethods     = ko.observableArray([]);
        self.testMethodIsCheckedAll  = ko.pureComputed(function(){
            var _testMethods = self.testMethods(),_checked = true;
            for(var z = 0,zLen = _testMethods.length; z < zLen; z++){
                _checked = _checked && _testMethods[z].checked();
            }
            return _checked;
        });
        self.assignIsCheckedAll = ko.pureComputed(function(){
            var _assigns = self.assigns();
            var _checked = true;
            for(var z = 0,zLen = _assigns.length; z < zLen; z++){
                _checked = _checked && _assigns[z].checked();
            }
            return _checked;
        });
        self.clazzGroupIdsStr = null; //专为打点而生
    };
    Exam.prototype = {
        constructor       : Exam,
        param             : {},
        assigns           : ko.observableArray([]),
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
        run               : function(obj){
            var self = this,paramData = {
                bookId   : self.param.bookId,
                unitId   : self.param.unitId,
                sections : self.sectionIds.toString(),
                type     : self.tabType,
                subject  : constantObj.subject,
                objectiveConfigId : self.param.objectiveConfigId
            };
            obj = $.isPlainObject(obj) ? obj : {};
            self.clazzGroupIdsStr = obj.clazzGroupIdsStr;
            self.examLoading(true);
            $.get("/teacher/new/homework/objective/content.vpage", paramData, function(data){
                if(data.success){
                    var _boxQuestionMap = self._getBoxQuestionMap(),
                        _content = data.content || [],
                        _packages = [],
                        moreQuestions = [],
                        charWordList = [];
                    for(var i = 0,iLen = _content.length; i < iLen; i++){
                        if(_content[i].type == "package"){
                            var _realPackages = _content[i].packages || [];
                            for(var z = 0, zLen = _realPackages.length; z < zLen; z++){
                                var _packageObj = {},
                                    totalSec = _realPackages[z].seconds || 0,
                                    _boxId = _realPackages[z].id;
                                _packageObj["id"] = _boxId;
                                _packageObj["name"] = _realPackages[z].name;
                                _packageObj["selCount"] = _boxQuestionMap[_realPackages[z].id] ? _boxQuestionMap[_realPackages[z].id].length : 0;
                                _packageObj["flag"] = "package";
                                _packageObj["totalCount"] = _realPackages[z].questions.length || 0;
                                //题目是否全部使用过
                                _packageObj["teacherUsed"] = _realPackages[z].showAssigned || false;
                                _packageObj["totalMin"] = Math.ceil(totalSec/60);
                                _packageObj["usageName"] = _realPackages[z].usageName || '';
                                _packageObj['usageColor'] = _realPackages[z].usageColor || '';
                                _packageObj['charWordListTitle'] = '生字和词语：';
                                if($.isArray(_realPackages[z].charList) && _realPackages[z].charList.length > 0){
                                    _packageObj['charWordListTitle'] = '生字：';
                                }else if($.isArray(_realPackages[z].wordList) && _realPackages[z].wordList.length > 0){
                                    _packageObj['charWordListTitle'] = '词语：';
                                }
                                _packageObj['charWordList'] = [].concat(_realPackages[z].charList || [],_realPackages[z].wordList || []);
                                _packages.push(_packageObj);
                                self.packageQuestionsMap[_boxId] = _realPackages[z].questions || [];
                            }
                        }else if(_content[i].type == "question"){
                            moreQuestions = _content[i].questions || [];
                            charWordList = [].concat(_content[i].charList || [], _content[i].wordList || []);

                            var _patterns = _content[i].questionTypes || [];
                            for(var s = 0,sLen = _patterns.length; s < sLen; s++){
                                _patterns[s]["checked"] = false;
                            }
                            _patterns.length > 0 && (self.patterns(ko.mapping.fromJS(_patterns)()));

                            var _words = _content[i].words || [];
                            if(_words.length > 0){
                                self.words(ko.mapping.fromJS(_words)());
                            }

                            var _categories = _content[i].tags || [];
                            if(_categories.length > 0){
                                self.categories(ko.mapping.fromJS(_categories)());
                            }
                            var testMethods = _content[i].testMethods || [];
                            for(var t = 0,tLen = testMethods.length; t < tLen; t++){
                                testMethods[t]["checked"] = false;
                            }
                            testMethods.length > 0 && (self.testMethods(ko.mapping.fromJS(testMethods)()));

                        }
                    }
                    if(moreQuestions.length > 0) {
                        self.packageQuestionsMap["-1"] = moreQuestions;
                        _packages.push({
                            id: "-1",  //虚拟题包ID
                            name: "更多题目",
                            checked: false,
                            selCount: _boxQuestionMap["-1"] ? _boxQuestionMap["-1"].length : 0,
                            totalCount: 0,
                            assignTimes: 0,
                            totalMin: 0,
                            teacherUsed: false,
                            usageName: '',
                            usageColor: '',
                            flag: "more_question",
                            charWordListTitle : '生字和词语：',
                            charWordList : [].concat(charWordList)
                        });
                    }
                    var _packagesKo = ko.mapping.fromJS(_packages)();
                    self.packageList(_packagesKo);
                    if(_packages.length > 0){
                        var _focusBoxId = _packages[self.focusPackageIndex()].id;
                        self._resetExamList(self.packageQuestionsMap[_focusBoxId]);
                    }
                }else{
                    self.packageList([]);
                    self.questionList = [];
                    $17.voxLog( {
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
            self.currentPage(startPage);
            var _startIndex = (startPage - 1) * 5;
            var _subExamQuestions = _questions.slice(_startIndex, _startIndex + 5);

            self.questionList = _questions;
            self._fillFocusExamList(_subExamQuestions,function(){
                self.totalPage(Math.ceil(_questions.length/5));
                self.userInputPage(null);
            });
        },
        _fillFocusExamList: function(questions,callback){
            var self = this;
            var questionIds = [];
            for(var m = 0; m < questions.length; m++){
                questionIds.push(questions[m].id);
            }

            $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                self.focusExamMap = result.success ? result.questionMap : {};
                self.focusExamList(ko.mapping.fromJS(questions)());
                $.isFunction(callback) && callback();
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
        viewPackage : function(self,index){
            var that = this,
                _focusBoxId = that.id(),
                _focusPackageIndex = self.focusPackageIndex();
            index = (+index || 0);
            if(_focusPackageIndex == index){
                //同一包的点击
                return false;
            }
            self.focusPackageIndex(index);
            self._resetExamList(self.packageQuestionsMap[_focusBoxId]);

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : _focusBoxId === "-1" ? "page_assign_tongbu_practice_more_click" : "page_assign_tongbu_practice_package_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : _focusBoxId === "-1" ? null : _focusBoxId
            });
        },
        addOrRemovePackage  : function(){
            var self = this;
            var that = self.focusPackage();
            var _selCount = that.selCount();
            var _totalCount = that.totalCount();
            var _packageId = that.id();
            var _questions = self.packageQuestionsMap[_packageId] || [];
            var logOp;
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

                logOp = "page_assign_tongbu_package_deselectAll_click";
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
                        if(existsQuestionFlag == -1 && self._addExam(_packageId,_questions[z]).success){
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
                logOp = "page_assign_tongbu_practice_package_selectAll_click"
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op : logOp,
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : _packageId
            });
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
        _addExam    : function(currentBoxId,question){
            var self = this;
            //内部方法，约定currentBoxId,question合法的,currentBoxId,question的合法性放在外面判断
            var _questionId = question.id;
            var _similarIds = question.similarQuestionIds || [];
            var _questionObj = {
                questionId    : _questionId,
                seconds       : question.seconds,
                submitWay     : question.submitWay,
                questionBoxId : currentBoxId,
                similarQuestionId : _similarIds.length > 0 ? _similarIds[0] : null,
                book              : question.book
            };
            constantObj._homeworkContent.practices[self.tabType].questions.push(_questionObj);
            constantObj._reviewQuestions[self.tabType].push(question);
            return {
                success : true,
                info    : "添加成功"
            };
        },
        addExam     : function(self,element){
            var that = this;
            var _currentBoxId = null;
            if(self.focusPackage() && self.focusPackage().id){
                _currentBoxId = self.focusPackage().id();
            }

            var _question = ko.mapping.toJS(that);
            var existsQuestionFlag =  self._isExistsByQuestionId(_question.id);
            if(existsQuestionFlag == -1 && self._addExam(_currentBoxId,_question).success){
                that.checked(true);
                var cnt = self.packageList()[self.focusPackageIndex()].selCount() + 1;
                self.packageList()[self.focusPackageIndex()].selCount(cnt);

                var _questionsInCart = constantObj._homeworkContent.practices[self.tabType].questions || [];
                self.updateUfoExam(that.seconds(),_questionsInCart.length);

                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + self.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });

                $17.voxLog({
                    module: "Newhomework_assign_" + $uper.subject.key,
                    op : self.tabType + "_choose_btn"
                });
            }else if(existsQuestionFlag != -1){
                $17.alert("该题与已选题目重复");
            }

            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_tongbu_practice_package_select_click",
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : _currentBoxId,
                s3 : _question.id
            });
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
        removeExam : function(self){
            var that = this;
            var _questionId = that.id();
            that.checked(false);
            var _boxId = self.focusPackage().id();
            var _question = ko.mapping.toJS(that);
            if(self._removeExam(_boxId,_question)){

                var cnt = self.packageList()[self.focusPackageIndex()].selCount();
                self.packageList()[self.focusPackageIndex()].selCount(cnt > 0 ? cnt - 1 : 0);
                var _questionsInCart = constantObj._homeworkContent.practices[self.tabType].questions || [];
                self.updateUfoExam(0 - that.seconds(),_questionsInCart.length);
                $17.voxLog({
                    module: "Newhomework_assign_" + $uper.subject.key,
                    op : self.tabType + "_cancel_btn"
                });
            }else{
                $17.info("这道题不在小车中");
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_tongbu_practice_package_deselect_click",
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : _boxId,
                s3 : _questionId
            });
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
        isTestMethodChecked : function(testMethod){
            var self = this;
            return testMethod && self.testMethodCart.indexOf(testMethod) != -1;
        },
        testMethodAllClick : function(){
            var self = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            self.examLoading(true);
            var _checkedAll = self.testMethodIsCheckedAll(),_testMethods = self.testMethods();
            for(var z = 0,zLen = _testMethods.length; z < zLen; z++){
                _testMethods[z].checked(!_checkedAll);
            }
            self.examFilter();
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "assignhomework_tongbu_moreques_TestSelectAll_click",
                s0 : constantObj.subject,
                s1 : "",
                s2 : self.tabType
            });
            self.examLoading(false);
        },
        testMethod_click  : function(self){
            var that = this;  //single testMethod
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            self.examLoading(true);
            that.checked(!that.checked());
            self.examFilter();
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "assignhomework_tongbu_moreques_TestSelect_click",
                s0 : constantObj.subject,
                s1 : that.name(),
                s2 : self.tabType
            });
            self.examLoading(false);
        },
        isWordChecked : function(word){
            var self = this;
            return word && self.wordCart.indexOf(word) != -1;
        },
        wordAllClick : function(){
            var self = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            self.examLoading(true);
            if(self.wordAllChecked()){
                self.wordCart([]);
            }else{
                var _words = self.words();
                var _wordCart = [];
                for(var z = 0,zLen = _words.length; z < zLen; z++){
                    _wordCart.push(_words[z]);
                }
                self.wordCart(_wordCart);
            }
            self.examFilter();
            self.examLoading(false);
        },
        word_click  : function(self,word){
            var that = this;  //single word
            if(word){
                if(self.examLoading()){
                    $17.alert("处理中,请稍候");
                    return false;
                }
                self.examLoading(true);
                if(!self.isWordChecked(word)){
                    self.wordCart.push(word);
                }else{
                    var _index = self.wordCart.indexOf(word);
                    self.wordCart.splice(_index,1);
                }
                self.examFilter();

                $17.voxLog({
                    module: "m_H1VyyebB",
                    op : "page_assign_unfamiliar_word_wordSift_click",
                    s0 : constantObj.subject,
                    s1 : self.tabType,
                    s2 : word
                });
                self.examLoading(false);
            }
        },
        isCategoryChecked : function(category){
            var self = this;
            return category && self.categoryCart.indexOf(category) != -1;
        },
        categoryAllClick : function(){
            var self = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            self.examLoading(true);
            if(self.categoryAllChecked()){
                self.categoryCart([]);
            }else{
                var _categories = self.categories();
                var _categoryCart = [];
                for(var z = 0,zLen = _categories.length; z < zLen; z++){
                    _categoryCart.push(_categories[z]);
                }
                self.categoryCart(_categoryCart);
            }
            self.examFilter();
            self.examLoading(false);
        },
        category_click : function(self,category){
            var that = this;  //single category
            if(category){
                if(self.examLoading()){
                    $17.alert("处理中,请稍候");
                    return false;
                }
                self.examLoading(true);
                if(!self.isCategoryChecked(category)){
                    self.categoryCart.push(category);
                }else{
                    var _index = self.categoryCart.indexOf(category);
                    self.categoryCart.splice(_index,1);
                }
                self.examFilter();
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op : "page_assign_unfamiliar_word_typeSift_click",
                    s0 : constantObj.subject,
                    s1 : self.tabType,
                    s2 : category
                });
                self.examLoading(false);
            }
        },
        goSpecifiedPage : function(){
            var self = this; //Exam
            var pageNo = self.userInputPage();
            if(/\D/g.test(pageNo)){
                self.userInputPage(null);
            }else{
                self.page_click(self,pageNo);
            }
        },
        page_click : function(self,pageNo){
            pageNo = +pageNo || 0;
            if(pageNo < 1 || pageNo > self.totalPage() || pageNo == self.currentPage()){
                return false;
            }
            self.currentPage(pageNo);
            var _startIndex = (pageNo - 1) * 5;
            self._fillFocusExamList(self.questionList.slice(_startIndex,_startIndex + 5));
        },
        pattern_click    : function(self){
            var that = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_tongbu_practice_typeSift_click",
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : that.id()
            });
            self.examLoading(true);
            that.checked(!that.checked());
            self.examFilter();
            self.examLoading(false);
        },
        patternAllClick  : function(){
            var self = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            self.examLoading(true);
            var _checkedAll = self.patternIsCheckedAll();
            var _patterns = self.patterns();
            for(var z = 0,zLen = _patterns.length; z < zLen; z++){
                _patterns[z].checked(!_checkedAll);
            }
            self.examFilter();
            self.examLoading(false);
        },
        difficulty_click : function(self){
            var that = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }

            self.examLoading(true);
            that.checked(!that.checked());
            self.examFilter();
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_tongbu_practice_difficultySift_click",
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : that.value()
            });
            self.examLoading(false);
        },
        difficultyAllClick : function(){
            var self = this;
            if(self.examLoading()){
                $17.alert("处理中,请稍候");
                return false;
            }
            self.examLoading(true);
            var _checkedAll = self.difficultyIsCheckedAll();
            var _difficulties = self.difficulties();
            for(var z = 0,zLen = _difficulties.length; z < zLen; z++){
                _difficulties[z].checked(!_checkedAll);
            }
            self.examFilter();
            self.examLoading(false);
        },
        assign_click     : function(self){
            var that = this;
            that.checked(!that.checked());
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_tongbu_practice_usedSift_click",
                s0 : constantObj.subject,
                s1 : self.tabType,
                s2 : that.name()
            });
            self.examFilter();
        },
        assignAllClick : function(){
            var self = this;
            var _checkedAll = self.assignIsCheckedAll();
            var assigns = self.assigns();
            for(var k = 0,kLen = assigns.length; k < kLen; k++){
                assigns[k].checked(!_checkedAll);
            }
            self.examFilter();
        },
        examFilter : function(){
            var self = this;
            self.examLoading(true);
            var patternIds = [];
            var diffculties = [];
            var assignKeys = [];
            var wordArr = self.wordCart();
            var categoryArr = self.categoryCart();
            var _patterns = self.patterns();
            for(var z = 0, zLen = _patterns.length; z < zLen; z++){
                if(_patterns[z].checked()){
                    patternIds.push(_patterns[z].id());
                }
            }
            var _difficulties = self.difficulties();
            for(var k = 0,kLen = _difficulties.length; k < kLen; k++){
                if(_difficulties[k].checked()){
                    diffculties = $.merge(diffculties,_difficulties[k].value());
                }
            }
            var _assigns = self.assigns();
            for(var s = 0,sLen = _assigns.length; s < sLen; s++){
                if(_assigns[s].checked()){
                    assignKeys.push(_assigns[s].key());
                }
            }

            var checkedTestMethodIds = [],testMethods = self.testMethods();
            for(var j = 0, jLen = testMethods.length; j < jLen; j++){
                if(testMethods[j].checked()){
                    checkedTestMethodIds.push(testMethods[j].id());
                }
            }

            var _examQuestions = self.examQuestions;

            var filterQuestions = [];
            for(var t = 0,tLen = _examQuestions.length; t < tLen; t++){
                var _question = _examQuestions[t];
                if(patternIds.length > 0 && $.inArray(_question.questionTypeId,patternIds) == -1){
                    continue;
                }
                if(diffculties.length > 0 && $.inArray(_question.difficulty,diffculties) == -1){
                    continue;
                }
                var assign = (_question.teacherAssignTimes > 0) ? 1 : 0;
                if(assignKeys.length > 0 && $.inArray(assign,assignKeys) == -1){
                    continue;
                }

                if(wordArr.length > 0){
                    var questionsWords = _question.words || [];
                    if(questionsWords.length == 0){
                        continue;
                    }
                    var flag = false;
                    for(var m = 0,mLen = questionsWords.length; m < mLen; m++){
                        flag = flag || $.inArray(questionsWords[m],wordArr) != -1;
                        if(flag){
                            break;
                        }
                    }
                    if(!flag){
                        continue;
                    }
                }

                if(categoryArr.length > 0){
                    var qstCategories = _question.tags || [];
                    if(qstCategories.length == 0){
                        continue;
                    }
                    var categoryFlag = false;
                    for(var sm = 0,smLen = qstCategories.length; sm < smLen; sm++){
                        categoryFlag = categoryFlag || $.inArray(qstCategories[sm],categoryArr) != -1;
                        if(categoryFlag){
                            break;
                        }
                    }
                    if(!categoryFlag){
                        continue;
                    }
                }
                if(checkedTestMethodIds.length > 0 && $.inArray(_question.testMethodId,checkedTestMethodIds) == -1){
                    continue;
                }

                filterQuestions.push(_question);
            }
            self.currentPage(1);
            self.totalPage(Math.ceil(filterQuestions.length/5));
            self.questionList = filterQuestions;
            self._fillFocusExamList(filterQuestions.slice(0,5));

            self.examLoading(false);
        },
        initialise        : function(option){
            var self = this;
            option = option || {};
            self.param = option;
            self.tabType = option.tabType; //必传字段
            var _sectionIds = [];
            $.each(option.sections,function(i,section){
                _sectionIds.push(section.sectionId);
            });
            self.sectionIds  = _sectionIds;
            var _difficulties = [{name : "容易",value:[1,2],checked:false},
                {name : "中等",value:[3],checked:false},
                {name : "困难",value:[4,5],checked:false}];
            self.difficulties(ko.mapping.fromJS(_difficulties)());
            self.wordCart([]);
            self.categoryCart([]);

            var assigns = [{key : 1, name:"已布置", checked : false},{key : 0,name : "未布置",checked : false}];
            self.assigns(ko.mapping.fromJS(assigns)());

            //换单元，页码初始化,初始化题包
            self.currentPage(1);
            self.focusPackageIndex(0);

            self.carts = option.carts || null;

            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length == 0){
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
            var gameUrl = "/teacher/new/homework/viewquestion.vpage?" + $.param({qids:_questionId});
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

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "page_assign_tongbu_question_package_answerKey_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : _currentBoxId,
                s3    : _questionId
            });
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getExam          : function(){
            return new Exam();
        },
        getWord_practice : function(){
            return new Exam();
        },
        getChinese_reading : function(){
            return new Exam();
        },
        getInteresting_picture :function(){
            return new Exam();
        }
    });
}($17,ko));