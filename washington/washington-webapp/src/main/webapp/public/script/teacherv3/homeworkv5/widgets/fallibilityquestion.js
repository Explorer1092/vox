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

    var PackageBox = function(obj){
        var self = this;
        self.sectionTitle = obj.sectionTitle;
        self.packageList  = obj.packageList;
        self.packageClickCallback = obj.packageClickCallback || null;
    };

    PackageBox.prototype = {
        constructor : PackageBox,
        packageClick : function(){
            var packageObj = this;
        }
    };

    var FallibilityQuestion = function(){
        var self = this;
        self.tabType         = "";
        self.examLoading     = ko.observable(true); //正在加载应试
        self.packageBoxes    = ko.observableArray([]); //题包盒子
        self.packagePage     = new $17.pagination.initPages({ //初始化null会出现第一次无法加载问题
            currentPage : 1,
            totalPage   : 0,
            pageClickCb : self.packagePageClick.bind(self)
        });                  //包的页码对象
        self.focusPackage    = ko.observable(null);
        self.sectionIds      = [];
        self.loadExamInitialize = false;

        self.questionList    = [];   //筛选后符合条件的应试题集
        self.currentSelCnt = ko.observable(0);  //筛选后符合条件的应试题集选择的题数
        self.currentTotalCnt = ko.observable(0);//筛选后符合条件的应试题集总题数
        self.currentTotalMin = ko.observable(0);//筛选后符合条件的应试题集总时间
        self.focusExamList   = ko.observableArray([]);
        self.focusExamList.subscribe(self.setExamChecked,self);
        self.examPage        = new $17.pagination.initPages({ //初始化null会出现第一次无法加载问题
            currentPage : 1,
            totalPage   : 0,
            pageClickCb : self.page_click.bind(self)
        });
        self.focusExamMap = {};  //用户可见题目的映射
        self.clazzGroupIdsStr = null;

        self.showMorePoints = ko.observable(false);
        self.focusPackagePoints = ko.observableArray([]);
    };
    FallibilityQuestion.prototype = {
        constructor       : FallibilityQuestion,
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
        loadPackageBoxes  : function(page){
            var self = this,paramData = {
                bookId   : self.param.bookId,
                unitId   : self.param.unitId,
                sections : self.sectionIds.toString(),
                type     : self.tabType,
                subject  : constantObj.subject,
                clazzs   : self.clazzGroupIdsStr,
                currentPageNum : page,
                objectiveConfigId : self.param.objectiveConfigId
            };
            self.examLoading(true);
            self._resetPackageOptions();
            $.get("/teacher/new/homework/objective/content.vpage", paramData, function(data){
                if(data.success){
                    var _content = data.content || [],
                        realObj = {
                            packageBoxes : [],
                            focusPackage : null
                        },
                        totalPage;
                    for(var i = 0,iLen = _content.length; i < iLen; i++){
                        if(_content[i].type == "package"){
                            var _packageBoxes = _content[i].packages;totalPage = _content[i].totalPages || 0;
                            switch (self.tabType){
                                case "FALLIBILITY_QUESTION":
                                    realObj = $.extend(true,realObj,self._generateFallibilityQuestionPackages(_packageBoxes));
                                    break;
                                default:
                            }
                        }
                    }
                    self.packageBoxes(realObj.packageBoxes);
                    self.packageClickCallback(realObj.focusPackage);
                    self.packagePage.setPage(page,totalPage);
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
        _generateFallibilityQuestionPackages : function(_packageBoxes){
            var self = this,_realPackageBoxes = [],focusPackage;
            for(var z = 0, zLen = _packageBoxes.length; z < zLen; z++){
                var packageList = _packageBoxes[z].weekWrongQuestionBOList;
                packageList = $.isArray(packageList) ? packageList : [];
                if(packageList.length > 0){
                    var newPackageList = [];
                    for(var t = 0,tLen = packageList.length; t < tLen; t++){
                        //默认取第一个
                        var packageId = packageList[t].id,
                            questionList = packageList[t].questions;
                        questionList = $.isArray(questionList) ? questionList : [];
                        var tempPackageObj = {
                            id              : packageId,
                            name            : packageList[t].name,
                            groupId         : packageList[t].groupId,
                            groupName       : packageList[t].groupName,
                            description1    : '失分率:' + packageList[t].lossRate,
                            questionNum     : packageList[t].questionNum,
                            totalMin        : Math.ceil(packageList[t].seconds/60),
                            questionList    : questionList
                        };
                        newPackageList.push(tempPackageObj);
                        (z == 0 && t == 0 && (focusPackage = tempPackageObj));
                    }
                    _realPackageBoxes.push(new PackageBox({
                        sectionTitle : _packageBoxes[z].timeSpan,
                        packageList  : newPackageList,
                        packageClickCallback : self.packageClickCallback.bind(self)
                    }));
                }
            }
            return {
                packageBoxes : _realPackageBoxes,
                focusPackage : focusPackage
            };
        },
        run               : function(obj){
            var self = this;
            obj = $.isPlainObject(obj) ? obj : {};
            self.clazzGroupIdsStr = obj.clazzGroupIdsStr;
            self.loadPackageBoxes(1);
        },
        packagePageClick : function(pageNo){
            var self = this;
            self.loadPackageBoxes(pageNo);
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "assignhomework_Gaps/WrongQues_PackagePagedown_click",
                s0    : constantObj.subject,
                s1    : self.tabType
            });
        },
        _resetExamList : function(_questions){
            if(!$.isArray(_questions)){
                _questions = [];
            }
            var self = this,
                startPage = 1,
                _startIndex = (startPage - 1) * 5,
                _subExamQuestions = _questions.slice(_startIndex, _startIndex + 5),
                totalSeconds = 0,
                focusPackage    = self.focusPackage(),
                _boxQuestionMap = self._getBoxQuestionMap(),
                _selectQuestions = focusPackage ? (_boxQuestionMap[focusPackage.id] || []) : [],
                _selectCount = 0,
                _questionChecked = false;
            for(var k = 0,kLen = _questions.length; k < kLen; k++){
                _questionChecked = (_selectQuestions.indexOf(_questions[k].id) != -1);
                _questions[k]["checked"] = _questionChecked;
                _questions[k]["upImage"] = _questions[k].upImage || false;
                totalSeconds += (+_questions[k].seconds || 0);
                _questionChecked && (_selectCount++);
            }
            self.questionList = _questions;
            self.currentSelCnt(_selectCount);
            self.currentTotalCnt(self.questionList.length);
            self.currentTotalMin(Math.ceil(totalSeconds/60));
            self._fillFocusExamList(_subExamQuestions,function(){
                self.examPage.setPage(startPage, Math.ceil(_questions.length/5));
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
            var self = this,
                _exams = constantObj._homeworkContent.practices[self.tabType].questions || [],
                _packageDetail = {};
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
        _isExistsByQuestionId : function(questionId,groupId){
            //当前作业类型下是否选过此题
            var self = this,
                _exams = constantObj._homeworkContent.practices[self.tabType].questions || [],
                _index = -1;
            for(var z = 0,zLen = _exams.length; z < zLen; z++){
                if(_exams[z].questionId && _exams[z].questionId === questionId && _exams[z].groupId == groupId){
                    _index = z;
                    break;
                }
            }
            return _index;
        },
        _resetPackageOptions : function(){
            var self = this;
            self.focusPackage(null);
            self.currentSelCnt(0);
            self.currentTotalCnt(0);
            self.currentTotalMin(0);
            self.focusExamList([]);
            self.examPage.setPage(1, 0);
        },
        packageClickCallback : function(packageObj){
            var self = this; // self --> FallibilityQuestion
            if(packageObj){
                self.viewPackage.call(packageObj,self);
            }else{
                self._resetPackageOptions();
                return false;
            }

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "assignhomework_Gaps/WrongQues_Package_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : packageObj != null ? packageObj.id : null
            });
        },
        viewPackage : function(self){
            var that = this,_focusBoxId = that.id;
            if(self.focusPackage() && _focusBoxId == self.focusPackage().id){
                //同一包的点击
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_Gaps/WrongQues_Package_click",
                    s0    : constantObj.subject,
                    s1    : self.tabType,
                    s2    : self.focusPackage() != null ? self.focusPackage().id : null
                });
                return false;
            }
            self.focusPackage(that);
            
            var points = that.knowledgePointList;
            points = $.isArray(points) ? points : [];
            for(var m = 0,mLen = points.length; m < mLen; m++){
                points[m]["disabled"] = false;
            }
            self.focusPackagePoints(ko.mapping.fromJS(points)());
            self.examFilter();
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "assignhomework_Gaps/WrongQues_Package_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : self.focusPackage() != null ? self.focusPackage().id : null
            });
        },
        updatePointState    : function(self){
            var that = this;  // this --> self.focusPackagePoints
            that.disabled(!that.disabled());
            self.examFilter();
        },
        showOrHidePoints    : function(){
            var self = this;
            self.showMorePoints(!self.showMorePoints());
        },
        addOrRemovePackage  : function(){
            var self = this,
                that = self.focusPackage(),
                _selCount = self.currentSelCnt(),
                _totalCount = self.currentTotalCnt(),
                _packageId = that.id,
                _groupId   = that.groupId,
                _questions = that.questionList;
            if(_selCount >= _totalCount){
                //取消勾选
                var removeSeconds = 0,deleteCnt = 0;
                if(_questions.length > 0){
                    for(var t = 0,tLen = _questions.length; t < tLen; t++){
                        if(self._removeExam(_packageId,_questions[t])){
                            deleteCnt++;
                            //有，删除
                            removeSeconds += _questions[t].seconds;
                        }
                    }
                }
                if(deleteCnt > 0){
                    self.currentSelCnt((_selCount - deleteCnt) > 0 ? (_selCount - deleteCnt) : 0);
                    self.setExamChecked();
                }
                //更新UFO_EXAM
                self.updateUfoExam(0 - removeSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_Gaps/WrongQues_PackageDetail_DelselectAll_click",
                    s0    : constantObj.subject,
                    s1    : self.tabType,
                    s2    : _packageId
                });
            }else{
                //全选
                var addSeconds = 0,
                    cnt = 0,
                    beenSelected = 0; //在其他题包已选过计数,同题包中不会出现重题
                if(_questions.length > 0){
                    for(var z = 0,zLen = _questions.length; z < zLen; z++){
                        var qId = _questions[z].id,
                            result = self._addExam(_packageId,_questions[z],_groupId,that.groupName);
                        if(result.success){
                            addSeconds +=  _questions[z].seconds;
                            cnt++;
                        }else if(result.existsQuestionFlag != -1){
                            beenSelected++;
                        }
                    }
                    if(cnt > 0){
                        self.currentSelCnt(_selCount + cnt);
                        self.setExamChecked();
                    }
                }
                //更新UFO_EXAM
                self.updateUfoExam(addSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);
                if(beenSelected > 0){
                    $17.alert("有" + beenSelected + "道题与已选题目重复");
                }

                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_Gaps/WrongQues_PackageDetail_selectAll_click",
                    s0    : constantObj.subject,
                    s1    : self.tabType,
                    s2    : _packageId
                });
            }
        },
        setExamChecked : function(){
            $17.info("设置当前页中题的状态");
            var self = this,
                focusPackage    = self.focusPackage(),
                _boxQuestionMap = self._getBoxQuestionMap(),
                _selectQuestions = focusPackage ? (_boxQuestionMap[focusPackage.id] || []) : [],
                _focusExamList = self.focusExamList();
            for(var z = 0,zLen = _focusExamList.length; z < zLen; z++){
                var _questionId = _focusExamList[z].id(),
                    _checked = _focusExamList[z].checked();
                if(_selectQuestions.length > 0 && !_checked && _selectQuestions.indexOf(_questionId) != -1){
                    self.focusExamList()[z].checked(true);
                }else if(_selectQuestions.length == 0 && _checked){
                    self.focusExamList()[z].checked(false);
                }
            }
        },
        updateUfoExam : function(sec,questionCnt){
            //sec : 表示增加或减少的时间数,questionCnt:表示该类型的选入的总题数
            var self = this;
            constantObj._moduleSeconds[self.tabType] = constantObj._moduleSeconds[self.tabType] + sec;
            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(self.tabType,questionCnt);
        },
        _getSpecialBoxInfo : function(boxId,questionId){
            //返回指定的包ID下选择的题数，指定题ID在指定包中的下标，没有返回-1
            var self = this,
                _questions = constantObj._homeworkContent.practices[self.tabType].questions,
                _questionIndex = -1,
                cnt = 0;
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
        _addExam    : function(currentBoxId,question,_groupId,_groupName){
            var self = this,
                _questionId = question.id, //内部方法，约定currentBoxId,question合法的,currentBoxId,question的合法性放在外面判断
                existsQuestionFlag = self._isExistsByQuestionId(_questionId,_groupId);
            var param = self.param;
            if(existsQuestionFlag == -1){
                var _similarIds = question.similarQuestionIds || [],
                    _questionObj = {
                        questionId          : _questionId,
                        seconds             : question.seconds,
                        submitWay           : question.submitWay,
                        book                : question.book,
                        groupId             : _groupId,
                        groupName           : _groupName,
                        questionBoxId       : currentBoxId,
                        similarQuestionId   : _similarIds.length > 0 ? _similarIds[0] : null,
                        objectiveId         : param.objectiveTabType
                    };
                question["groupId"]   = _groupId;
                question["groupName"] = _groupName;
                constantObj._homeworkContent.practices[self.tabType].questions.push(_questionObj);
                constantObj._reviewQuestions[self.tabType].push(question);
                return {
                    success : true,
                    info    : "添加成功"
                };
            }else{
                return {
                    success : false,
                    info    : "添加失败",
                    existsQuestionFlag : existsQuestionFlag
                };
            }
        },
        addExam     : function(self,element){
            var that = this,
                _question,
                focusPackage = self.focusPackage(),
                _currentBoxId = null,
                result;
            if(focusPackage && focusPackage.id){
                _currentBoxId = focusPackage.id;
            }
            _question = ko.mapping.toJS(that);
            if(self._addExam(_currentBoxId,_question,focusPackage.groupId,focusPackage.groupName).success){
                that.checked(true);
                self.currentSelCnt(self.currentSelCnt() + 1);
                var _questionsInCart = constantObj._homeworkContent.practices[self.tabType].questions || [];
                self.updateUfoExam(that.seconds(),_questionsInCart.length);

                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + self.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
            }else{
                $17.alert("该题与已选题目重复");
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "assignhomework_Gaps/WrongQues_PackageDetail_ques_select_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : _currentBoxId,
                s3    : _question.id,
                s4    : _question.lossRate ? _question.lossRate : null
            });
        },
        _removeExam : function(currentBoxId,question){
            var self = this,
                _questionId = question.id,
                _tempObj = self._getSpecialBoxInfo(currentBoxId,_questionId),
                _questionIndex = _tempObj.questionIndex;
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
            var that = this,
                _questionId = that.id(),
                _boxId = self.focusPackage().id,
                _question = ko.mapping.toJS(that);
            that.checked(false);
            if(self._removeExam(_boxId,_question)){
                self.currentSelCnt(self.currentSelCnt() - 1);
                var _questionsInCart = constantObj._homeworkContent.practices[self.tabType].questions || [];
                self.updateUfoExam(0 - that.seconds(),_questionsInCart.length);
            }else{
                $17.info("这道题不在小车中");
            }
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "assignhomework_Gaps/WrongQues_PackageDetail_ques_Delselect_click",
                s0    : constantObj.subject,
                s1    : self.tabType,
                s2    : _boxId,
                s3    : _questionId,
                s4    : _question.lossRate ? _question.lossRate : null
            });
        },
        clearAll  : function(){
            var self = this,
                _packageList = self.packageList();
            for(var z = 0,zLen = _packageList.length; z < zLen; z++){
                _packageList[z].selCount(0);
            }
            constantObj._homeworkContent.practices[self.tabType].questions = [];
            constantObj._reviewQuestions[self.tabType] = [];
            self.currentSelCnt(0);
            self.setExamChecked();
        },
        page_click : function(pageNo){
            var self = this;
            pageNo = +pageNo || 0;
            var _startIndex = (pageNo - 1) * 5;
            self._fillFocusExamList(self.questionList.slice(_startIndex,_startIndex + 5));
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "assignhomework_Gaps/WrongQues_PackagePagedown_click",
                s0    : constantObj.subject,
                s1    : self.tabType
            });
        },
        examFilter : function(){
            var self = this,
                examQuestions = self.focusPackage().questionList,
                filterQuestions = [],
                selectPointIds = [];
            self.examLoading(true);
            //根据知识点过滤...
            ko.utils.arrayForEach(self.focusPackagePoints(),function(point,i){
                !point.disabled() && (selectPointIds.push(point.id()));
            });
            examQuestions = $.isArray(examQuestions) ? examQuestions : [];
            for(var k = 0,kLen = examQuestions.length; k < kLen; k++){
                var kpId = examQuestions[k].kpId;
                (self.tabType !== 'KNOWLEDGE_REVIEW' || selectPointIds.length === 0 || selectPointIds.indexOf(kpId) !== -1) && (filterQuestions.push(examQuestions[k]));
            }
            self._resetExamList(filterQuestions);
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

            self.loadExamInitialize = option.examInitComplete || false;

            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length == 0){
                $ufoexam.empty().html(template("t:UFO_EXAM",{tabTypeName : option.tabTypeName,count : 0}));
            }

            self.carts = option.carts || null;
        },
        feedback : function(self){
            var that = this; //single exam
            var _questionId = that.id();
            var _currentBoxId = null;
            if(self.focusPackage() && self.focusPackage().id){
                _currentBoxId = self.focusPackage().id;
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
                _currentBoxId = self.focusPackage().id;
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
            var _boxId = self.focusPackage().id;
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
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getFallibility_question : function(){
            //高频错题
            return new FallibilityQuestion();
        }
    });
}($17,ko));
