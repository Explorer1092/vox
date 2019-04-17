/**
 * 数学课时讲练测,不支持单题选入
 * Created by dell on 2018/11/10.
 */
(function($17,ko) {
    "use strict";

    var IntelligentTeaching = function(obj,termCarts){
        var self = this;
        self.termType        = obj.type;
        self.tabType         = obj.homeworkType;
        self.examLoading     = ko.observable(true); //正在加载应试
        self.sectionIds      = [];
        self.termCarts = termCarts;
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
            var self = this;
            obj = obj || {};
            self.param = $.extend(true,{},obj);
            var paramData = {
                type         : obj.type,
                bookId       : obj.bookId,
                subject      : constantObj.subject,
                clazzGroupIds: obj.clazzGroupId
            };
            obj = $.isPlainObject(obj) ? obj : {};
            self.clazzGroupIdsStr = obj.clazzGroupIdsStr;
            paramData.clazzs = obj.clazzGroupIdsStr;
            self.examLoading(true);
            $.get("/teacher/termreview/content.vpage", paramData, function(data){
                if(data.success){
                    var _boxQuestionMap = self._getBoxQuestionMap(),
                        _packages = [];
                    var _realPackages = data.packages || [];
                    for(var z = 0, zLen = _realPackages.length; z < zLen; z++){
                        var _packageObj = {},
                            _boxId = _realPackages[z].id,
                            totalSec = _realPackages[z].seconds || 0;
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
            var _exams = self.termCarts.getQuestionsByHomeworkType(self.tabType);
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
            var _exams = self.termCarts.getQuestionsByHomeworkType(self.tabType);
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
            var that = this,
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
                existsQuestions = self.termCarts.getQuestionsByHomeworkType(self.tabType);
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
                        courseId            : question["courseId"],
                        similarQuestionId   : _similarIds.length > 0 ? _similarIds[0] : null,
                        book                : question.book || null,
                        objectiveId         : param.objectiveTabType
                    };
                self.termCarts.addQuestion({
                    type       : self.tabType,
                    questions  : [_questionObj]
                });
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


                self.termCarts.removeQuestion({
                    type       : self.tabType,
                    questions : [_questionObj]
                });
                return true;
            }else{
                $17.info("已经移除过这道题了");
            }
            return false;
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
            if(!$17.isBlank(examId)){
                var $mathExamImg = $(boxId);
                $mathExamImg.empty();
                var generateExamId = "examImg-" + index + "_" + new Date().getTime();
                $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id",generateExamId).appendTo($mathExamImg);
                var node = document.getElementById(generateExamId);
                var obj = self.renderExam(examId,node);
                if(self.questionHandles){
                    self.questionHandles[index] = obj;
                }
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

    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getIntelligent_teaching : function(obj,termCarts){
            return new IntelligentTeaching(obj,termCarts );
        }
    });
}($17,ko));
