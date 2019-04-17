/**
 * 数学计算讲练测,不支持单题选入
 * Created by dell on 2017/11/27.
 */
(function($17,ko) {
    "use strict";

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

    var CalcIntelligentTeaching = function(){
        var self = this;
        self.tabType         = "";
        self.examLoading     = ko.observable(true); //正在加载应试
        self.sectionIds      = [];
        self.loadExamInitialize = false;
        self.packageList     = ko.observableArray([]);
        self.focusPackageIndex = ko.observable(0); //当前焦点题包在packageList的下标
        self.focusPackage = ko.pureComputed(function(){
            return self.packageList()[self.focusPackageIndex()];
        });

        self.packageKnowledgePointsMap = {};  //题包与题包下知识点的映射关系

        self.displayPointQuestions   = ko.observableArray([]);

        //存放H5返回的句柄
        self.subject = constantObj.subject;
        self.param  = {};
    };
    CalcIntelligentTeaching.prototype = {
        constructor       : CalcIntelligentTeaching,
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
            self.examLoading(true);
            $.get("/teacher/new/homework/objective/content.vpage", paramData).done(function(data){
                if(data.success){
                    var _boxQuestionMap = self._getBoxQuestionMap(),
                        _content = data.content || [],
                        _packages = [];
                    var _realPackages = _content || [];
                    for(var z = 0, zLen = _realPackages.length; z < zLen; z++){
                        var _packageObj = {},
                            _boxId = _realPackages[z].pakId,
                            totalSec = _realPackages[z].questionSeconds || 0;
                        _packageObj["id"] = _boxId;
                        _packageObj["name"] = _realPackages[z].pakName;
                        _packageObj["selCount"] = _boxQuestionMap[_boxId] ? _boxQuestionMap[_boxId].length : 0;
                        _packageObj["totalCount"] = _realPackages[z].questionCount || 0;
                        _packageObj["totalMin"] = Math.ceil(totalSec/60);
                        _packages.push(_packageObj);
                        self.packageKnowledgePointsMap[_boxId] = _realPackages[z].sectionQuestions || [];
                    }
                    self.packageList(ko.mapping.fromJS(_packages)());
                    if(_packages.length > 0){
                        self._forwardSpecialPackage.call(self.focusPackage(),self,self.focusPackageIndex());
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
            }).fail(function(e){
                $17.voxLog( {
                    module : "API_REQUEST_ERROR",
                    op     : "API_STATE_ERROR",
                    s0     : "/teacher/new/homework/content.vpage",
                    s1     : $.toJSON(e.message),
                    s2     : $.toJSON(paramData),
                    s3     : $uper.env
                });
            });
        },
        _getBoxQuestionMap : function(){
            var self = this;
            var _exams = constantObj._homeworkContent.practices[self.tabType].questions || [];
            var _packageDetail = {};
            for(var z = 0,zLen = _exams.length; z < zLen; z++){
                var _questionBoxId = _exams[z].questionBoxId;
                if($17.isBlank(_questionBoxId)){
                    break;
                }
                if(!$.isArray(_packageDetail[_questionBoxId])){
                    _packageDetail[_questionBoxId] = [];
                }
                _packageDetail[_questionBoxId].push(_exams[z].questionId);
            }
            return _packageDetail;
        },
        _forwardSpecialPackage : function(self,index){
            var that = this,
                _focusBoxId = that.id(); //this -> package obj
            self.focusPackageIndex(index);

            var kpList = self.packageKnowledgePointsMap[_focusBoxId];
            var questionIds = [];
            for(var t = 0,tLen = kpList.length; t < tLen; t++){
                var questionList = kpList[t].questions || [];
                for(var k = 0,kLen = questionList.length; k < kLen; k++){
                    questionIds.push(questionList[k]["questionId"]);
                }
                questionIds = questionIds.concat(kpList[t].postQuestions || []);
            }

            QuestionDB.addQuestions(questionIds,function(){
                self.displayPointQuestions(self.packageKnowledgePointsMap[_focusBoxId]);
            });
        },
        viewPackage : function(self,index){
            var that = this,
                _focusPackageIndex = self.focusPackageIndex();
            if(_focusPackageIndex === index){
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
            self._forwardSpecialPackage.call(that,self,index);
        },
        addOrRemovePackage  : function(){
            var self = this,
                that = self.focusPackage(),
                _selCount = that.selCount(),
                _totalCount = that.totalCount(),
                _packageId = that.id();
            var kpList = self.packageKnowledgePointsMap[_packageId];
            if(_selCount >= _totalCount){
                //取消勾选
                var removeSeconds = 0;

                for(var t = 0,tLen = kpList.length; t < tLen; t++){
                    var obj = self._removeQuestinsByPoint(kpList[t]);
                    removeSeconds += obj.seconds;
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
                for(var z = 0,zLen = kpList.length; z < zLen; z++){
                    var objTemp = self._addQuestionsByPoint(kpList[z],_packageId);
                    addSeconds += objTemp.seconds;
                    cnt += objTemp.count;
                }
                cnt = self.packageList()[self.focusPackageIndex()].selCount() + cnt;
                self.packageList()[self.focusPackageIndex()].selCount(cnt);
                //更新UFO_EXAM
                self.updateUfoExam(addSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);
                /*if(beenSelected > 0){
                    $17.alert("有" + beenSelected + "道题与已选题目重复");
                }*/
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "assignhomework_tongbu_PackageDetail_selectAll_click",
                    s0    : constantObj.subject,
                    s1    : that.name(),
                    s2    :  _packageId
                });
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
        _addQuestionsByPoint : function(point,questionBoxId){
            var questions = point["questions"] || [];
            var obj = {
                count : 0,
                seconds : 0
            };
            if(!$.isArray(questions) || questions.length === 0){
                return obj;
            }
            var self = this;
            var param = self.param || {};

            var _newQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
            obj.count = questions.length;
            $.each(questions,function(){
                obj.seconds += (this.seconds || 0);
                _newQuestions.push({
                    book             : point.book,
                    questionId       : this.questionId,
                    seconds          : this.seconds,
                    knowledgePointId : this.knowledgePoint,
                    objectiveId      : param.objectiveTabType,
                    questionBoxId    : questionBoxId
                });
            });

            var _reviewQuestions = constantObj._reviewQuestions[self.tabType];
            _reviewQuestions = _reviewQuestions.concat(questions);
            constantObj._reviewQuestions[self.tabType] = _reviewQuestions;
            return obj
        },
        _removeQuestinsByPoint : function(point){
            //删除某知识点下的题
            var self = this;
            var removeQuestions = point["questions"] || [];
            var deleteQuestionIds = [];
            for(var k = 0,kLen = removeQuestions.length; k < kLen; k++){
                deleteQuestionIds.push(removeQuestions[k].questionId);
            }

            QuestionDB.deleteQuestions(deleteQuestionIds);

            var _reviewQuestions = constantObj._reviewQuestions[self.tabType];
            var _newReviewQuestions = [];
            for(var j = 0,jLen = _reviewQuestions.length; j < jLen; j++){
                if(deleteQuestionIds.indexOf(_reviewQuestions[j].questionId) === -1){
                    _newReviewQuestions.push(_reviewQuestions[j]);
                }
            }
            constantObj._reviewQuestions[self.tabType] = _newReviewQuestions;

            var _newQuestions = [];
            var removeQuestionCount = 0,removeQuestionSeconds = 0;
            var assignQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
            for(var m = 0,mLen = assignQuestions.length; m < mLen; m++){
                if(deleteQuestionIds.indexOf(assignQuestions[m].questionId) === -1){
                    _newQuestions.push(assignQuestions[m]);
                }else{
                    removeQuestionCount += 1;
                    removeQuestionSeconds += assignQuestions[m].seconds;
                }
            }
            constantObj._homeworkContent.practices[self.tabType].questions = _newQuestions;
            return {
                count : removeQuestionCount,
                seconds : removeQuestionSeconds
            }
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
        renderVueQuestion : function(questionId,index){
            var self = this;
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
                    formulaContainer:'#tabContent', //公式渲染容器（必须）
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
                    $17.log(e.message,e.stack);
                }
            }
            return "";
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
            self.focusPackageIndex(0);

            self.carts = option.carts || null;
            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length === 0){
                $ufoexam.empty().html(template("t:UFO_EXAM",{tabTypeName : option.tabTypeName,count : 0}));
            }
        },
        viewCourse : function(self){
            var question = this;
            switch (self.tabType) {
                case "INTELLIGENT_TEACHING":
                case "CALC_INTELLIGENT_TEACHING":
                    var gameUrl = "/teacher/new/homework/previewteachingcourse.vpage?" + $.param({
                        courseId : question["courseId"]
                    });
                    var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>';
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
        getCalc_intelligent_teaching : function(){
            return new CalcIntelligentTeaching();
        }
    });
}($17,ko));
