/**
 * @author xinqiang.wang
 * @description   "布置作业"
 * @createDate 2016/1/25
 *
 */

define(["$17", "knockout", "komapping", 'examCore_new', "picker", 'logger','pickerDate', 'pickerTime', 'jbox'], function ($17, ko, komapping, examCore, datetimepicker, logger) {

    homeworkConstant._homeworkContent = {//保存布置作业
        desc: "",
        startTime: homeworkConstant.startDate,
        startDateTime: homeworkConstant.startDate+ ' 00:00:00',
        clazzIds: homeworkConstant.clazzIds,
        homeworkType: 'Normal',
        endTime: "",
        remark: "",
        duration: 0,
        practices: {
            BASIC_APP: {
                name: "基础练习",
                apps: []
            },
            EXAM: {
                name: "同步习题",
                questions: []
            },
            MENTAL: {
                name: "口算",
                questions: []
            },
            READING: {
                name: "绘本",
                apps: []
            },
            WORD_PRACTICE: {
                name: "生字词练习",
                questions: []
            },
            READ_RECITE: {
                name: "课文读背题",
                questions: []
            },
            UNIT_QUIZ: {
                name: "测验",
                questions: []
            },
            PHOTO_OBJECTIVE: {
                name: "动手做一做",
                questions: []
            },
            VOICE_OBJECTIVE: {
                name: "概念说一说",
                questions: []
            },
            ORAL_PRACTICE:{
                name: "口语习题",
                questions:[]
            },
            KNOWLEDGE_REVIEW:{
                name:"查缺补漏",
                questions:[]
            },
            FALLIBILITY_QUESTION:{
                name:"高频错题",
                questions:[]
            },
            INTELLIGENCE_EXAM:{
                name:"同步习题（新）",
                questions:[]
            }
        },
        books: {
            BASIC_APP: [],
            EXAM: [],
            MENTAL: [],
            READING: [],
            WORD_PRACTICE: [],
            READ_RECITE: [],
            UNIT_QUIZ: [],
            MID_QUIZ: [],
            END_QUIZ: [],
            PHOTO_OBJECTIVE: [],
            VOICE_OBJECTIVE: [],
            ORAL_PRACTICE: [],
            INTELLIGENCE_EXAM:[],
            FALLIBILITY_QUESTION:[],
            KNOWLEDGE_REVIEW:[]
        }
    };

    var subject = $17.getQuery("subject");

    var HomeworkModel = function () {
        var self = this;
        self.homeworkEnum = {
            BASIC_APP: 'BASIC_APP',
            EXAM: 'EXAM',
            MENTAL: 'MENTAL',
            UNIT_QUIZ: 'UNIT_QUIZ',
            PHOTO_OBJECTIVE: 'PHOTO_OBJECTIVE',
            VOICE_OBJECTIVE: 'VOICE_OBJECTIVE',
            READING: "READING",
            LISTEN_PRACTICE: "LISTEN_PRACTICE",
            ORAL_PRACTICE:"ORAL_PRACTICE",
            INTELLIGENCE_EXAM:"INTELLIGENCE_EXAM",
            FALLIBILITY_QUESTION:"FALLIBILITY_QUESTION",
            KNOWLEDGE_REVIEW:"KNOWLEDGE_REVIEW"
        };
        self.homeworkIcon = {
            BASIC_APP: 'icon-tblx',
            BASIC_KNOWLEDGE : 'icon-tbxt',
            EXAM: 'icon-tbxt',
            CHINESE_READING : 'icon-tbxt',
            KEY_POINTS: 'icon-tbxt',
            MENTAL: 'icon-kslx',
            UNIT_QUIZ: 'icon-cs',
            END_QUIZ: 'icon-cs',
            PHOTO_OBJECTIVE: 'icon-dszyz',
            VOICE_OBJECTIVE: 'icon-gnsys',
            WORD_PRACTICE: 'icon-scb',
            READ_RECITE: 'icon-kwdb',
            READING: 'icon-hbyd',
            ORAL_PRACTICE :'icon-oral',
            INTELLIGENCE_EXAM:'icon-tbxt',
            KNOWLEDGE_REVIEW:'icon-cqbl',
            FALLIBILITY_QUESTION:'icon-gpcs',
            INTERESTING_PICTURE : 'icon-hbyd'
        };

        /*自定义下拉模板*/
        self.koTemplateName = ko.observable(''); // ko template
        self.koTemplateClose = function () {
            self.koTemplateName('');
        };
        self.changeQuestionTypeBox = ko.observable(false); //展示切换题目类型

        self.ajaxLoading = ko.observable(false);
        self.packageDetail = ko.observableArray([]); //作业列表详情
        self.showHomeworkPackageList = ko.observable(true); //是否显示作业列表
        self.showHomeworkPackageDetailBox = ko.observable(false); //是否显示作业详情列表
        self.viewHomeworkDetail = ko.observableArray([]);
        self.viewHomeworkSelectedDetail = ko.observableArray([]); //根据作业类型选择展示内容
        self.viewHomeworkFirstTabName = ko.observable('');
        self.viewHomeworkSelectedTabName = ko.observable('');
        self.showViewHomeworkBox = ko.observable(false);//是否显示预览作业页
        self.showViewHomeworkSelectBox = ko.observable(false);//预览作业页-选择框
        self.showConfirmBox = ko.observable(false);//是否显示二次确认页
        self.showViewPackageBox = ko.observable(false);//是否显示题包预览
        self.viewPackageName = ko.observable();
        self.showHomeworkFinishBtn = ko.observable(false);
        self.showHomeworkEndDateTime = ko.observable(0);
        self.homeworkIntegral = ko.observable(0);
        self.homeworkIntegralMaxCount = ko.observable(0);
        self.selectedClazzLength = homeworkConstant.clazzIds.split(',').length;
        self.divScrollTop = ko.observable(0);
        self.voxExamRenderObj = '';//examCoreJs 外部接口

        self.selectedHomeworkType = ko.observable("");//选择的作业类型
        self.selectedHomeworkName = ko.observable("");//选择的作业名称
        self.selectedHomeworkTypeId = ko.observable("");//选择的作业类型ID
        self.selectedHomeworkTypeArray = []; //记录已加载过的作业类型
        self.generalError = ko.observable(false); //通用报错
        self.subject = ko.observable(subject);

        self.homeworkTimeList = ko.mapping.fromJS([{name: "今天内", value: 'today', checked: true}, {
            name: "明天内",
            value: 'tomorrow',
            checked: false
        }, {name: "后天内", value: 'afterTomorrow', checked: false}, {name: "自定义", value: 'other', checked: false}]);

        self.displayQuestionContent = function (_questionContent) {//口算题目内容过滤
            return _questionContent ? _questionContent.replace("__$$__", "(  )") : "";
        };

        /*打点*/
        self.sendLog = function () {
            var logMap = {
                app: "teacher",
                module: 'm_xYdJ7vAV',
                s0: subject
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);

        };

        /*作业列表图标*/
        self.icon = function (type) {
            //没有图标统一使用[icon-tbxt]样式
            var icon = 'icon-tbxt';
            for (var i in self.homeworkIcon) {
                if (i == type) {
                    icon = self.homeworkIcon[i];
                }
            }
            return icon;
        };

        /*获取题目类型名称*/
        self.getPackageNameByType = function (packageType) {
            var packageName = '';
            ko.utils.arrayForEach(self.packageDetail(), function (pt) {
                if (pt.type() == packageType) {
                    packageName = pt.name();
                }
            });
            return packageName;
        };

        /*动态显示题包导航位置*/
        self.graceShowTopMenu = function () {
            var examPackageTopMenuBox = $('#examPackageTopMenuBox:visible,#quizPackageTopMenuBox:visible,#oralPracticePackageTopMenuBox:visible');
            var liWidth = examPackageTopMenuBox.find('ul li.active').innerWidth();
            var index = examPackageTopMenuBox.find('ul li.active').data('index');
            examPackageTopMenuBox.scrollLeft(liWidth * index - 50);
        };

        /*exam*/
        self.packageList = ko.observableArray([]);
        self.examQuestions = [];
        self.examAllQuestionList = [];
        self.questionList = ko.observableArray([]);
        self.examPackageList = ko.observableArray([]);
        self.examDetail = ko.observableArray([]);
        self.examCart = ko.observableArray([]);
        self.examCartCount = ko.pureComputed(function () {
            return self.examCart().length;
        });
        self.examPackageQuestionObj = ko.observableArray([]);//预览题包
        self.examCurrentPage = ko.observable(1);
        self.examPageSize = 5;
        self.examLoading = ko.observable(true);
        self.examDuration = ko.observable(0);// 应试题用时 单元：秒

        self.examPackageCurrentPage = ko.observable(1);
        self.examPackagePageSize = 5;
        self.examCurrentPackageId = ko.observable(-1); //当前选择题包的ID

        self.examKnowledgePointsDetail = ko.observableArray([]);
        self.examPatternsSearchDetail = ko.observableArray([{id: -1, name: '全部', checked: true}]);//题型
        self.examDifficultySearchDetail = ko.mapping.fromJS([{name: "全部", value: [], checked: true}, {
            name: "容易",
            value: [1, 2],
            checked: false
        }, {name: "中等", value: [3], checked: false}, {name: "困难", value: [4, 5], checked: false}]);//难度
        self.examArrangeSearchDetail = ko.mapping.fromJS([{key: -1, name: "全部", checked: false}, {
            key: 1,
            name: "已布置",
            checked: false
        }, {key: 0, name: "未布置", checked: false}]);//安照布置次数查找
        self.examCurrentPatternsSearchVal = ko.observable();
        self.examCurrentDifficultySearchVal = ko.observable();
        self.examCurrentArrangeSearchVal = ko.observable();
        self.examCurrentPackageDetail = ko.observableArray([]);

        self.examKnowledgePointsListBox = ko.observable(false);
        self.examFocusPointId = ko.observable(0);

        /*应试包是否被全选*/
        self.examPackageIsSelectAll = function () {
            var totalLength = self.examCurrentPackageDetail().length;
            var selectedTotal = 0;
            ko.utils.arrayForEach(self.examCurrentPackageDetail(), function (exam) {
                if (exam.checked()) {
                    ++selectedTotal
                }
            });
            return totalLength == selectedTotal
        };

        /*mental*/
        self.mentalDetail = ko.observableArray([]);
        self.mentalCartCount = ko.observable(0);
        self.mentalConvenient = 5;//题目变化基数
        self.mentalMaxQuestionsNum = 20;//题目数最大值
        self.mentalQuestionsDetail = {};
        self.mentalSelectedKpId = [];
        self.mentalDuration = ko.observable(0);//单元：秒

        /*quiz*/
        self.quizDetail = ko.observableArray([]);
        self.quizPackageList = ko.observableArray([]);
        self.showQuizViewBox = ko.observable(false);
        self.quizViewAllDetail = [];
        self.quizViewCurrentPage = ko.observable(1);
        self.quizViewTotalPage = ko.observable(0);
        self.quizViewLoading = ko.observable(false);
        self.quizViewPageSize = 5;
        self.quizViewDetail = ko.observableArray([]);
        self.quizDuration = ko.observable(0);//单元：秒
        self.quizCurrentPackageId = ko.observable(-1); //当前选择题包的ID
        self.quizCurrentPackageDetail = ko.observableArray([]);
        self.quizPackageCurrentPage = ko.observable(1);
        self.quizPackagePageSize = 5;
        self.quizCart = ko.observableArray([]);
        /*获取已选测验中的题目总数*/
        self.quizQuestionsCount = function () {
            return self.quizCart().length;
        };

        /*PHOTO_OBJECTIVE*/
        self.photoObjectiveDetail = ko.observableArray([]);
        self.photoObjectiveDuration = ko.observable(0);//单元：秒
        self.photoObjectiveCart = ko.observableArray([]);
        self.photoObjectiveCartCount = ko.pureComputed(function () {
            return self.photoObjectiveCart().length;
        });

        /*VOICE_OBJECTIVE*/
        self.voiceObjectiveDetail = ko.observableArray([]);
        self.voiceObjectiveDuration = ko.observable(0);//单元：秒
        self.voiceObjectiveCart = ko.observableArray([]);
        self.voiceObjectiveCartCount = ko.pureComputed(function () {
            return self.voiceObjectiveCart().length;
        });

        /*WORD_PRACTICE*/
        self.wordPracticeQuestions = [];
        self.wordPracticeAllQuestionList = [];
        self.wordTags = ko.observableArray([]);
        self.wordPracticeLoading = ko.observable(true);
        self.wordPracticeCurrentPage = ko.observable(1);
        self.wordPracticePageSize = 5;
        self.wordPracticeQuestionList = ko.observableArray([]);
        self.wordPracticeCart = ko.observableArray([]);
        self.wordPracticeCartCount = ko.pureComputed(function () {
            return self.wordPracticeCart().length;
        });
        self.wordPracticeWordSearchDetail = ko.observableArray([]);//按生词
        self.wordPracticeCategoriesSearchDetail = ko.observableArray([]);//按类别
        self.wordPracticeDuration = ko.observable(0);//用时 单元：秒
        self.wordSearchByWordName = ko.observable("全部");
        self.wordSearchByCategoriesName = ko.observable("全部");
        self.wordSearchByArrangeVal = ko.observable();
        self.wordArrangeSearchDetail = ko.mapping.fromJS([{key: -1, name: "全部", checked: false}, {
            key: 1,
            name: "已布置",
            checked: false
        }, {key: 0, name: "未布置", checked: false}]);//安照布置次数查找
        self.wordCurrentPracticeWordSearchVal = ko.observable();
        self.wordCurrentPracticeCategoriesSearchVal = ko.observable();

        /*READ_RECITE*/
        self.readReciteQuestions = [];
        self.readReciteAllQuestions = [];
        self.readReciteExamLoading = ko.observable(true);
        self.readReciteDetail = ko.observableArray([]);
        self.readReciteDuration = ko.observable(0);//单元：秒
        self.readReciteCart = ko.observableArray([]);
        self.readReciteCartCount = ko.pureComputed(function () {
            return self.readReciteCart().length;
        });
        self.readRecitePageSize = 5;
        self.readReciteCurrentPage = ko.observable(1);

        /*BASIC_APP*/
        self.basicAppDetail = ko.observableArray([]);
        self.basicAppCardCount = function () {
            var count = 0, second = 0;
            ko.utils.arrayForEach(self.basicAppDetail(), function (detail) {
                ko.utils.arrayForEach(detail.categories(), function (cate) {
                    if (cate.checked()) {
                        ++count;
                        ko.utils.arrayForEach(cate.practices(), function (practices, index) {
                            if (index == 0) {
                                ko.utils.arrayForEach(practices.questions(), function (questions) {
                                    second += questions.seconds();
                                });
                            }

                        });
                    }
                });
            });
            return {questionCount: count, seconds: second};
        };

        /*READING*/
        self.readingCard = ko.observableArray([]);
        self.readingCardCount = function () {
            return self.readingCard().length;
        };
        self.readingDuration = ko.observable(0);

        /*ORAL_PRACTICE*/
        self.oralPracticeCart = ko.observableArray([]);
        self.oralPracticeCartCount = ko.pureComputed(function () {
            return self.oralPracticeCart().length;
        });
        self.oralPracticeDuration = ko.observable(0);// 应试题用时 单元：秒


        /*去选题*/
        self.topicsBtn = function () {
            var that = this;
            self.showHomeworkPackageList(false);
            self.showHomeworkPackageDetailBox(true);
            self.selectedHomeworkType(that.type());
            self.selectedHomeworkName(that.name());
            self.selectedHomeworkTypeId(that.typeId());
            self.sendLog({
                op: "page_typelist_choose_title_click",
                s1: self.selectedHomeworkType()
            });

            if(["KNOWLEDGE_REVIEW","FALLIBILITY_QUESTION","INTELLIGENCE_EXAM"].indexOf(self.selectedHomeworkType()) != -1){
                return false;
            }else{
                /*通用报错*/
                if(!self.homeworkEnum.hasOwnProperty(self.selectedHomeworkType()) || (self.selectedHomeworkType() == 'UNIT_QUIZ' && subject == 'CHINESE')){
                    self.generalError(true);
                    return false;
                }else{
                    // self.generalError(false);
                    self.getHomeworkContent();
                    $17.backToTop();
                    self.gotoTop();
                }
            }
        };

        /*顶部导航选择作业类型*/
        self.homeworkTypeClick = function () {
            var that = this;
            self.selectedHomeworkType(that.type());
            self.selectedHomeworkName(that.name());
            self.selectedHomeworkTypeId(that.typeId());
            self.changeQuestionTypeBox(false);

            self.sendLog({
                op: "page_switchtype_type_click",
                s1: that.type()
            });

            if(["KNOWLEDGE_REVIEW","FALLIBILITY_QUESTION","INTELLIGENCE_EXAM"].indexOf(self.selectedHomeworkType()) != -1){
                self.generalError(false);// 引导页与不支持作业类型切换
                return false;
            }else{
                /*通用报错*/
                if(!self.homeworkEnum.hasOwnProperty(self.selectedHomeworkType()) || (self.selectedHomeworkType() == 'UNIT_QUIZ' && subject == 'CHINESE')){
                    self.generalError(true);
                    return false;
                }else{
                    self.generalError(false);
                    self.getHomeworkContent();
                    $17.backToTop();
                    self.gotoTop();

                    if (self._readingCloseAllSearchBox) {
                        self._readingCloseAllSearchBox();
                    }
                }
            }
        };

        /*切换题目类型*/
        self.changeQuestionTypeBtn = function () {
            self.changeQuestionTypeBox(true);

            self.sendLog({
                op: "page_select_title_switchtype_click"
            });
        };

        /*获取当前作业中的类型*/
        self.getPackage = function () {
            $17.weuiLoadingShow();
            self.ajaxLoading(true);
            $.post("/teacher/homework/packagetype.vpage", {
                bookId: homeworkConstant.bookId,
                unitId: homeworkConstant.unitId,
                sections: homeworkConstant.sections,
                subject: subject
            }, function (data) {
                if (data.success) {
                    if (data.homeworkType.length > 0) {
                        for (var i = 0, ht = data.homeworkType; i < ht.length; i++) {
                            ht[i].count = 0;
                            ht[i].show = true;
                            ht[i].unit = '道';
                            switch (ht[i].type) {
                                case self.homeworkEnum.BASIC_APP:
                                    ht[i].unit = "个练习";
                                    break;

                                case  self.homeworkEnum.READING:
                                    ht[i].unit = "本";
                                    break;

                                case self.homeworkEnum.LISTEN_PRACTICE:
                                    ht[i].show = false;
                                    break;
                            }

                        }
                        self.packageDetail(ko.mapping.fromJS(data.homeworkType)());
                    } else {
                        $17.msgTip("作业列表为空");
                    }
                } else {
                    $17.msgTip("作业列表获取失败");
                }
                $17.weuiLoadingHide();
                self.ajaxLoading(false);
                $("#loadingBox").hide();
            });
        }();

        //根据作业类型获取作业详情
        self.getHomeworkContent = function () {
            if ($.inArray(self.selectedHomeworkType(), self.selectedHomeworkTypeArray) == -1) {
                self.selectedHomeworkTypeArray.push(self.selectedHomeworkType());
            } else {
                return;
            }
            self.ajaxLoading(true);
            $17.weuiLoadingShow();
            $.post("/teacher/homework/content.vpage", {
                bookId: homeworkConstant.bookId,
                unitId: homeworkConstant.unitId,
                sections: homeworkConstant.sections,
                type: self.selectedHomeworkTypeId(),
                subject: subject
            }, function (data) {
                if (data.success) {
                    var _content = data.content || [], hasQuestions = false;
                    if (self.selectedHomeworkType() == self.homeworkEnum.EXAM) {
                        for (var ii = 0; ii < _content.length; ii++) {
                            if (_content[ii].questions && _content[ii].questions.length > 0) {
                                hasQuestions = true;
                            }
                        }
                    }

                    /*oral_practice*/
                    if(self.selectedHomeworkType() == self.homeworkEnum.ORAL_PRACTICE){
                        for (var t = 0, tLen = _content.length; t < tLen; t++) {
                            if (_content[t].type == "package") {
                                var _oralPracticePackages = _content[t].packages || [];
                                for (var j = 0, jLen = _oralPracticePackages.length; j < jLen; j++) {
                                    _oralPracticePackages[j]["checked"] = !j;
                                    _oralPracticePackages[j]['selCount'] = 0;
                                    _oralPracticePackages[j]["packageUsed"] = true;
                                    for (var tj = 0, tjArr = _oralPracticePackages[j].questions; tj < tjArr.length; tj++) {
                                        tjArr[tj]["checked"] = false;
                                        tjArr[tj]["upImage"] = tjArr[tj].upImage || false;
                                        if (tjArr[tj]["teacherAssignTimes"] == 0) {
                                            _oralPracticePackages[j]["packageUsed"] = false;
                                        }
                                    }
                                }

                                self.oralPracticePackageList(ko.mapping.fromJS(_oralPracticePackages)());

                                //set first click
                                ko.utils.arrayForEach(self.oralPracticePackageList(), function (exam) {
                                    if (exam.checked()) {
                                        self.oralPracticePackageBoxSelected(exam);
                                    }
                                });
                            }
                        }
                    }else{
                        /*exam*/
                        for (var i = 0, iLen = _content.length; i < iLen; i++) {
                            if (_content[i].type == "package") {
                                var _packages = _content[i].packages || [];
                                for (var z = 0, zLen = _packages.length; z < zLen; z++) {
                                    _packages[z]["checked"] = !z;
                                    _packages[z]['selCount'] = 0;
                                    _packages[z]["packageUsed"] = true;
                                    for (var pq = 0, pqd = _packages[z].questions; pq < pqd.length; pq++) {
                                        pqd[pq]["checked"] = false;
                                        pqd[pq]["upImage"] = pqd[pq].upImage || false;
                                        if (pqd[pq]["teacherAssignTimes"] == 0) {
                                            _packages[z]["packageUsed"] = false;
                                        }
                                    }
                                }

                                //set more question
                                if (hasQuestions) {
                                    _packages.push({
                                        id: -1,
                                        name: "更多题目",
                                        checked: !!_packages.length,
                                        packageUsed: false,
                                        selCount: 0
                                    });
                                }

                                self.packageList(ko.mapping.fromJS(_packages)());

                                //set first click
                                ko.utils.arrayForEach(self.packageList(), function (exam) {
                                    if (exam.checked()) {
                                        self.packageBoxSelected(exam);
                                    }
                                });


                            } else if (_content[i].type == "question") {
                                if (self.selectedHomeworkType() == self.homeworkEnum.EXAM) {
                                    var _questions = _content[i].questions || [];
                                    for (var k = 0, kLen = _questions.length; k < kLen; k++) {
                                        _questions[k]["checked"] = false;
                                        _questions[k]["upImage"] = _questions[k].upImage || false;
                                    }
                                    self.examQuestions = self.examAllQuestionList = _questions;
                                    self.getExamQuestions(1);
                                    var _patterns = _content[i].questionTypes || [];
                                    var _points = _content[i].knowledgePoints || [];

                                    for (var s = 0, sLen = _patterns.length; s < sLen; s++) {
                                        _patterns[s]["checked"] = false;
                                        self.examPatternsSearchDetail.push(_patterns[s]);
                                    }

                                    for (var p = 0, pLen = _points.length; p < pLen; p++) {
                                        _points[p]["isActive"] = true;
                                        for (var kp = 0; kp < _points[p].knowledgePoints.length; kp++) {
                                            _points[p].knowledgePoints[kp]["checked"] = false;
                                        }
                                        self.examKnowledgePointsDetail.push(_points[p]);
                                    }
                                    self.examKnowledgePointsDetail(ko.mapping.fromJS(_points)());

                                    if (_questions.length > 0) {
                                        $("#questionsSearchBox").show();
                                    }

                                } else if (self.selectedHomeworkType() == self.homeworkEnum.WORD_PRACTICE) {
                                    var _wq = _content[i].questions || [];
                                    for (var w = 0, wLen = _wq.length; w < wLen; w++) {
                                        _wq[w]["checked"] = false;
                                    }
                                    if (_wq.length > 0) {
                                        self.wordPracticeQuestions = self.wordPracticeAllQuestionList = _wq;
                                        self.getWordPracticeQuestions(1);

                                        var _word = _content[i].words || [];
                                        _word.unshift("全部");
                                        if (_word.length > 0) {
                                            self.wordPracticeWordSearchDetail(ko.mapping.fromJS(_word)());
                                        }
                                    }

                                    var _categories = _content[i].tags || [];
                                    if (_categories.length > 0) {
                                        _categories.unshift("全部");
                                        self.wordPracticeCategoriesSearchDetail(ko.mapping.fromJS(_categories)());
                                    }
                                    self.wordPracticeCurrentPage(1);
                                }
                            }
                        }
                    }

                    /*mental*/
                    if (self.selectedHomeworkType() == self.homeworkEnum.MENTAL) {
                        for (var m = 0, mLen = _content.length; m < mLen; m++) {
                            var questionCount = 0;
                            questionCount = _content[m].question_count - (_content[m].question_count % self.mentalConvenient);
                            _content[m].count = 0;//题目数默认为0
                            _content[m].maxCount = questionCount; //questionCount最大值

                        }
                        self.mentalDetail(ko.mapping.fromJS(_content)());
                    }

                    /*quiz*/
                    if (self.selectedHomeworkType() == self.homeworkEnum.UNIT_QUIZ) {
                        for (var q = 0; q < _content.length; q++) {
                            _content[q]['checked'] = !q;
                            _content[q]['selCount'] = 0;
                            _content[q]["packageUsed"] = true;
                            for (var qpq = 0, qpc = _content[q].questions; qpq < qpc.length; qpq++) {
                                qpc[qpq]["checked"] = false;
                                qpc[qpq]["upImage"] = qpc[qpq].upImage || false;
                                if (qpc[qpq]["teacherAssignTimes"] == 0) {
                                    _content[q]["packageUsed"] = false;
                                }
                            }
                        }
                        self.quizPackageList(ko.mapping.fromJS(_content)());
                        //set first click
                        ko.utils.arrayForEach(self.quizPackageList(), function (exam) {
                            if (exam.checked()) {
                                self.quizPackageBoxSelected(exam);
                            }
                        });
                    }

                    /*PHOTO_OBJECTIVE*/
                    for (var p = 0; p < _content.length; p++) {
                        _content[p]['checked'] = false;
                    }
                    if (self.selectedHomeworkType() == self.homeworkEnum.PHOTO_OBJECTIVE) {
                        self.photoObjectiveDetail(ko.mapping.fromJS(_content)());
                    } else if (self.selectedHomeworkType() == self.homeworkEnum.VOICE_OBJECTIVE) {
                        self.voiceObjectiveDetail(ko.mapping.fromJS(_content)());
                    }

                    /*READ_RECITE*/
                    if (self.selectedHomeworkType() == self.homeworkEnum.READ_RECITE) {
                        for (var r = 0; r < _content.length; r++) {
                            _content[r]['checked'] = false;
                        }
                        //self.readReciteDetail(ko.mapping.fromJS(_content)());
                        self.readReciteQuestions = self.readReciteAllQuestions = _content;
                        self.getReadReciteQuestions(1);
                    }

                    /*BASIC_APP*/
                    if (self.selectedHomeworkType() == self.homeworkEnum.BASIC_APP) {
                        self.basicAppDetail(ko.mapping.fromJS(_content)());
                    }

                    /*READING*/
                    if (self.selectedHomeworkType() == self.homeworkEnum.READING) {
                        for (var rd = 0; rd < _content.length; rd++) {
                            if (_content[rd].type == 'weeklyRecommend' || _content[rd].type == "synchronous") {
                                for (var rdr = 0; rdr < _content[rd].readingList.length; rdr++) {
                                    _content[rd].readingList[rdr].checked = false;
                                }
                                if (_content[rd].type == "weeklyRecommend") {
                                    self.readingWeeklyDetail(ko.mapping.fromJS(_content[rd].readingList)());
                                    self.readingRecommendResult(_content[rd].recommendResult);
                                } else {
                                    self.readingSynchronousDetail(ko.mapping.fromJS(_content[rd].readingList)());
                                }
                            } else if (_content[rd].type = 'search') {

                                for (var sc = 0; sc < _content[rd].clazzLevels.length; sc++) {
                                    _content[rd].clazzLevels[sc].checked = false
                                }
                                for (var st = 0; st < _content[rd].topics.length; st++) {
                                    _content[rd].topics[st].checked = false;
                                }

                                for (var ss = 0; ss < _content[rd].series.length; ss++) {
                                    _content[rd].series[ss].checked = false;
                                }
                                _content[rd].clazzLevels.unshift({name: '全部', clazzLevel: -1, checked: true});
                                _content[rd].topics.unshift({topicName: '全部', topicId: -1, checked: true});
                                _content[rd].series.unshift({seriesName: '全部', seriesId: -1, checked: true});
                                self.readingSearchByClazzDetail(ko.mapping.fromJS(_content[rd].clazzLevels)());
                                self.readingSearchByTopicDetail(ko.mapping.fromJS(_content[rd].topics)());
                                self.readingSearchBySeriesDetail(ko.mapping.fromJS(_content[rd].series)());
                                self.readingSearch();
                            }
                        }
                    }

                } else {
                    $17.msgTip(data.info);
                }
                $17.weuiLoadingHide();
                self.ajaxLoading(false);
            }).fail(function () {
                $17.msgTip("作业加载失败");
                $17.weuiLoadingHide();
                self.ajaxLoading(false);
            });

            self.gotoTop();
        };

        self.voxExamRender = function (obj) {
            self.voxExamRenderObj = vox.exam.render(obj.node, obj.type, {
                ids: [obj.ids],
                getQuestionByIdsUrl: '/teacher/homework/query/questions.vpage',
                imgDomain: homeworkConstant.imgDomain,
                env: homeworkConstant.env,
                domain: homeworkConstant.domain,
                showExplain: obj.showExplain ? true : false,
                showAudio: ($17.isBlank(obj.showAudio) || obj.showAudio)
            });
        };

        /*-----------------------------exam start------------------------------*/
        //加载应试
        self.loadExamImg = function (examId, index) {
            var $mathExamImg = $("#mathExamImg" + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "examImg-" + index).appendTo($mathExamImg);
            var node = document.getElementById("examImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: examId,
                showExplain: false,
                showAudio: false
            });
        };

        self.loadExamPackageSingleImg = function (examId, index) {
            var $mathExamImg = $("#mathExamPackageSingleImg" + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "examPackageImg-" + index).appendTo($mathExamImg);
            var node = document.getElementById("examPackageImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: examId,
                showExplain: false,
                showAudio: false
            });
        };

        //应试筛选
        self.examFilter = function () {
            var self = this;
            var patternIds = [];
            var diffculties = [];
            var assignKeys = [];
            var _patterns = self.examPatternsSearchDetail();
            for (var z = 0, zLen = _patterns.length; z < zLen; z++) {
                if (_patterns[z].checked) {
                    patternIds.push(_patterns[z].id);
                }
            }
            var _difficulties = self.examDifficultySearchDetail();
            for (var k = 0, kLen = _difficulties.length; k < kLen; k++) {
                if (_difficulties[k].checked()) {
                    diffculties = $.merge(diffculties, _difficulties[k].value());
                }
            }
            var _assigns = self.examArrangeSearchDetail();
            for (var s = 0, sLen = _assigns.length; s < sLen; s++) {
                if (_assigns[s].checked()) {
                    assignKeys.push(_assigns[s].key());
                }
            }
            var _examQuestions = self.examAllQuestionList;
            var _focusPointId = self.examFocusPointId();
            var filterQuestions = [];

            //筛选仅可单选  题型选择全部时
            if (patternIds[0] == -1) {
                patternIds = [];
                for (var i = 0, iLen = _patterns.length; i < iLen; i++) {
                    if (_patterns[i].id != -1) {
                        patternIds.push(_patterns[i].id);
                    }
                }
            }
            if (assignKeys[0] == -1) {
                assignKeys = [];
                for (var a = 0, aLen = _assigns.length; a < aLen; a++) {
                    if (_patterns[a].key != -1) {
                        patternIds.push(_patterns[a].key);
                    }
                }
            }

            for (var t = 0, tLen = _examQuestions.length; t < tLen; t++) {
                var _question = _examQuestions[t];
                //知识点
                var questionPoints = _question.knowledgePoints || [];
                if (_focusPointId && (questionPoints.length == 0 || questionPoints.indexOf(_focusPointId) == -1)) {
                    continue;
                }

                if ((patternIds.length > 0 && $.inArray(_question.questionTypeId, patternIds) == -1)) {
                    continue;
                }
                if (diffculties.length > 0 && $.inArray(_question.difficulty, diffculties) == -1) {
                    continue;
                }
                var assign = (_question.teacherAssignTimes > 0) ? 1 : 0;
                if (assignKeys.length > 0 && $.inArray(assign, assignKeys) == -1) {
                    continue;
                }
                filterQuestions.push(_question);
            }

            //初始化
            self.questionList([]);
            self.examCurrentPage(1);

            setTimeout(function () {
                if (filterQuestions.length == 0) {
                    //默认全部
                    self.examQuestions = [];
                } else {
                    self.examQuestions = filterQuestions;
                }
                self.getExamQuestions(1);
                self.examLoading(true);
                $('#scrollListBox').scrollTop($('#hkTabcontent').offset().top);
                $('#questionsListBox').animate({scrollTop: '0px'}, 0);
            }, 200);
        };

        /*英语应试set book*/
        self._unitSetBooks = function (unitId, questionId, operate, $this) {
            if ($17.isBlank(operate) || (operate != 'add' && operate != 'remove')) {
                return false;
            }
            if ($17.isBlank(unitId) || $17.isBlank(questionId)) {
                return false;
            }
            var _bookExams = homeworkConstant._homeworkContent.books.EXAM;
            var unitIndex = -1;
            for (var k = 0, kLen = _bookExams.length; k < kLen; k++) {
                if (_bookExams[k].unitId == unitId) {
                    unitIndex = k;
                    break;
                }
            }
            if (unitIndex != -1) {
                var _includeQuestions = homeworkConstant._homeworkContent.books.EXAM[unitIndex].includeQuestions;
                if (operate == 'add') {
                    homeworkConstant._homeworkContent.books.EXAM[unitIndex].includeQuestions.push(questionId);
                } else {
                    var _qIndex = $.inArray(questionId, _includeQuestions);
                    if (_qIndex != -1) {
                        _includeQuestions.splice(_qIndex, 1);
                    }
                    if (_includeQuestions.length == 0) {
                        homeworkConstant._homeworkContent.books.EXAM.splice(unitIndex, 1);
                    }
                }
            } else {
                if (operate == 'add') {
                    var _bookObj = {
                        bookId: $this.book.bookId() || null,
                        unitId: $this.book.unitId() || null,
                        includeQuestions: []
                    };
                    _bookObj.includeQuestions.push(questionId);
                    homeworkConstant._homeworkContent.books.EXAM.push(_bookObj);
                }
            }
            return true;

        };

        //选入应试
        self.addExam = function () {
            var that = this;
            var _questionId = that.id();
            if ($.inArray(_questionId, self.examCart()) == -1) {
                that.checked(true);
                self.examCart.push(_questionId);
                var _submitWay = ko.mapping.toJS(that.submitWay());
                var _similarIds = that.similarQuestionIds() || [];
                var _questionObj = {
                    questionId: _questionId,
                    seconds: that.seconds(),
                    submitWay: _submitWay,
                    questionBoxId: self.examCurrentPackageId(),
                    similarQuestionId: _similarIds.length > 0 ? _similarIds[0] : null


                };
                homeworkConstant._homeworkContent.practices.EXAM.questions.push(_questionObj);
                if (subject == 'ENGLISH') {
                    self._unitSetBooks(that.book.unitId(), _questionId, "add", that);
                } else {
                    self.examSectionIntoBooks(that.book.sectionId(), _questionId, "add", that)
                }

                self.showQuestionsTotalCount();
                self.examDuration(self.examDuration() + that.seconds());

                //set select count
                ko.utils.arrayForEach(self.packageList(), function (exam) {
                    if (exam.id() == self.examCurrentPackageId()) {
                        exam.selCount(exam.selCount() + 1);
                    }
                });
            } else {
                $17.jqmHintBox("该题与已选题目重复");
            }

            if (self.examCurrentPackageId() == -1) {
                self.sendLog({
                    op: "page_select_title_tongbu_more_select_click",
                    s1: self.homeworkEnum.EXAM,
                    s2: _questionId
                });
            } else {
                self.sendLog({
                    op: "page_select_title_tongbu_package_select_click",
                    s1: self.homeworkEnum.EXAM,
                    s2: self.examCurrentPackageId(),
                    s3: _questionId
                });
            }
        };

        //移除应试
        self.removeExam = function () {
            var that = this;
            var _questionId = that.id();
            that.checked(false);
            var _questionIndex = self.examCart.indexOf(_questionId);
            self.examCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.EXAM.questions.splice(_questionIndex, 1);
            if (subject == 'ENGLISH') {
                self._unitSetBooks(that.book.unitId(), _questionId, "remove", that);
            } else {
                self.examSectionIntoBooks(that.book.sectionId(), _questionId, "remove", that);
            }

            self.showQuestionsTotalCount();
            self.examDuration(self.examDuration() - that.seconds());

            //set select count
            ko.utils.arrayForEach(self.packageList(), function (exam) {
                if (exam.id() == self.examCurrentPackageId()) {
                    exam.selCount(exam.selCount() - 1);
                }
            });

            if (self.examCurrentPackageId() == -1) {
                self.sendLog({
                    op: "page_select_title_tongbu_more_deselect_click",
                    s1: self.homeworkEnum.EXAM,
                    s2: _questionId
                });
            } else {
                self.sendLog({
                    op: "page_select_title_tongbu_package_deselect_click",
                    s1: self.homeworkEnum.EXAM,
                    s2: self.examCurrentPackageId(),
                    s3: _questionId
                });
            }
        };

        //题包预览
        self.viewPackage = function (name) {
            var that = this;
            self.showHomeworkPackageDetailBox(false);
            self.showViewPackageBox(true);
            self.examPackageQuestionObj([]);
            self.viewPackageName(name);
            if (name == 'package') {
                var ids = [];
                for (var i = 0, il = that.questions(); i < il.length; i++) {
                    ids.push(il[i].questionId());
                }
                var contents = {EXAM: ids};
                $.post("/teacher/homework/report/preview.vpage", {content: JSON.stringify(contents)}, function (data) {
                    if (data.success) {
                        self.examPackageQuestionObj(data.content)
                    }
                });
            } else {
                //拼装成同package的结构
                var obj = {};
                obj.questions = [{
                    questionId: that.id(),
                    difficultyName: that.difficultyName(),
                    questionType: that.questionType(),
                    upImage: that.upImage()
                }];
                self.examPackageQuestionObj(ko.mapping.toJS([obj]));
            }

            self.sendLog({
                op: "page_select_title_tongbu_package_title_click",
                s1: self.homeworkEnum.EXAM,
                s2: self.examCurrentPackageId(),
                s3: that.id()
            });
        };

        //题包预览  展示
        self.loadExamPackageImg = function (examId, index) {
            var $quizImg = $("#mathExamPackageImg" + index);
            $quizImg.empty();
            $("<div></div>").attr("id", "qpImg-" + index).appendTo($quizImg);
            var node = document.getElementById("qpImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'teacher_preview',
                ids: examId,
                showExplain: true
            });
        };

        /*题包返回*/
        self.viewPackageBackBtn = function () {
            self.showViewPackageBox(false);
            self.showHomeworkPackageDetailBox(true);
            if (self.viewPackageName() == 'package') {
                $17.backToTop();
            }
            try {
                self.voxExamRenderObj.dispose();
            } catch (e) {
                logger.log({
                    app: "teacher",
                    module: 'voxExamRender',
                    op: 'dispose_error'
                });
            }
        };

        /*展示全部题包*/
        self.examPackageAllBtn = function () {
            self.koTemplateName("examPackageAllBox");
        };

        /*分页获取应试*/
        self.getExamQuestions = function (page) {
            var groupExam = self.examQuestions.slice((page - 1) * self.examPageSize, page * self.examPageSize);
            ko.utils.arrayForEach(groupExam, function (exam) {
                if ($.inArray(exam.id, self.examCart()) != -1) {
                    exam.checked = true;
                }
                self.questionList.push(ko.mapping.fromJS(exam));
            });
        };

        self.getExamPackageDetails = function (page) {
            var groupExam = self.examCurrentPackageDetail.slice((page - 1) * self.examPackagePageSize, page * self.examPackagePageSize);
            ko.utils.arrayForEach(groupExam, function (exam) {
                if ($.inArray(exam.id, self.examCart()) != -1) {
                    exam.checked = true;
                }
                self.examPackageList.push(ko.mapping.fromJS(exam));
            });
        };

        //滚动加载应试题目
        self.examScrolled = function (data, event) {
            var elem = event.target;
            if (self.selectedHomeworkType() == self.homeworkEnum.EXAM) {
                var hkVisible = $("#hkTabcontent").is(':visible');
                if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200) && self.examLoading()) {
                    if (hkVisible) {
                        self.examCurrentPage(self.examCurrentPage() + 1);
                        self.getExamQuestions(self.examCurrentPage());
                    } else {
                        self.examPackageCurrentPage(self.examPackageCurrentPage() + 1);
                        self.getExamPackageDetails(self.examPackageCurrentPage());
                    }

                }
            }
            if (self.selectedHomeworkType() == self.homeworkEnum.WORD_PRACTICE) {
                if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200) && self.wordPracticeLoading()) {
                    self.wordPracticeCurrentPage(self.wordPracticeCurrentPage() + 1);
                    self.getWordPracticeQuestions(self.wordPracticeCurrentPage());
                }
            }
            if (self.selectedHomeworkType() == self.homeworkEnum.READ_RECITE) {
                if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200) && self.readReciteExamLoading()) {
                    self.readReciteCurrentPage(self.readReciteCurrentPage() + 1);
                    self.getReadReciteQuestions(self.readReciteCurrentPage());
                }
            }

            if (self.selectedHomeworkType() == self.homeworkEnum.UNIT_QUIZ) {
                if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200)) {
                    self.quizPackageCurrentPage(self.quizPackageCurrentPage() + 1);
                    self.getQuizPackageDetails(self.quizPackageCurrentPage());
                }
            }

            if (self.selectedHomeworkType() == self.homeworkEnum.READING) {
                if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200) && !self.readingLoading() && self.readingPageNum() <= self.redingTotalPageNum() - 1) {
                    self.readingPageNum(self.readingPageNum() + 1);
                    self.readingSearch();
                }
            }

            if (self.selectedHomeworkType() == self.homeworkEnum.ORAL_PRACTICE) {
                if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200) && self.examLoading()) {
                    self.oralPracticeExamPackageCurrentPage(self.oralPracticeExamPackageCurrentPage() + 1);
                    self.getOralPracticePackageDetails(self.oralPracticeExamPackageCurrentPage());
                }
            }
        };

        /*全部题目 条件筛选*/
        /*按题型*/
        self.examCurrentPatternsSearch = function () {
            var that = self.examCurrentPatternsSearchVal();
            for (var s = 0, sLen = self.examPatternsSearchDetail(); s < sLen.length; s++) {
                sLen[s].checked = false;
            }
            that.checked = true;
            self.examFilter();
            self.examLoading(false);
            self.examSearchLogger();
        };


        /*按难度*/
        self.examCurrentDifficultySearch = function () {
            var that = self.examCurrentDifficultySearchVal();
            for (var s = 0, sLen = self.examDifficultySearchDetail(); s < sLen.length; s++) {
                sLen[s].checked(false);
            }
            that.checked(true);
            self.examFilter();
            self.examLoading(false);
            self.examSearchLogger();
        };

        /*按布置*/
        self.examCurrentArrangeSearch = function () {
            var that = self.examCurrentArrangeSearchVal();
            for (var s = 0, sLen = self.examArrangeSearchDetail(); s < sLen.length; s++) {
                sLen[s].checked(false);
            }
            that.checked(true);
            self.examFilter();
            self.examLoading(false);
            self.examSearchLogger();
        };

        self.examSearchLogger = function () {
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_assign_home_' + subject,
                op: 'EXAM_topic_screening_click'
            });
        };

        //保存已选题目的book
        self.examSectionIntoBooks = function (sectionId, questionId, operate, $this) {
            var homeworkType = self.selectedHomeworkType();
            if ($17.isBlank(operate) || (operate != 'add' && operate != 'remove')) {
                return false;
            }
            if ($17.isBlank(sectionId) || $17.isBlank(questionId)) {
                return false;
            }
            var _bookExams = homeworkConstant._homeworkContent.books[homeworkType];
            var sectionIndex = -1;
            for (var k = 0, kLen = _bookExams.length; k < kLen; k++) {
                if (_bookExams[k].sectionId == sectionId) {
                    sectionIndex = k;
                    break;
                }
            }
            if (sectionIndex != -1) {
                var _includeQuestions = homeworkConstant._homeworkContent.books[homeworkType][sectionIndex].includeQuestions;
                if (operate == 'add') {
                    homeworkConstant._homeworkContent.books[homeworkType][sectionIndex].includeQuestions.push(questionId);
                } else {
                    var _qIndex = $.inArray(questionId, _includeQuestions);
                    if (_qIndex != -1) {
                        _includeQuestions.splice(_qIndex, 1);
                    }
                    if (_includeQuestions.length == 0) {
                        homeworkConstant._homeworkContent.books[homeworkType].splice(sectionIndex, 1);
                    }
                }
            } else {
                if (operate == 'add') {
                    var _bookObj = {
                        bookId: $this.book.bookId() || null,
                        unitId: $this.book.unitId() || null,
                        includeQuestions: []
                    };
                    if (homeworkConstant._homeworkContent.books[homeworkType].length == 0) {
                        _bookObj.sectionId = $this.book.sectionId() || null;
                        _bookObj.includeQuestions = [questionId];

                        homeworkConstant._homeworkContent.books[homeworkType].push(_bookObj);
                    } else {
                        for (var i = 0, book = homeworkConstant._homeworkContent.books[homeworkType]; i < book.length; i++) {
                            if (book[i].sectionId == sectionId) {
                                _bookObj.sectionId = book[i].sectionId || null;
                            } else {
                                _bookObj.sectionId = sectionId || null;
                            }
                        }
                        if (_bookObj) {
                            _bookObj.includeQuestions.push(questionId);
                            homeworkConstant._homeworkContent.books[homeworkType].push(_bookObj);
                        }
                    }
                }
            }
        };

        //题包选择
        self.packageBoxSelected = function (that) {
            var pid = that.id();
            self.examCurrentPackageId(pid);

            for (var i = 0, dl = self.packageList(); i < dl.length; i++) {
                dl[i].checked(false);
            }
            that.checked(true);
            var examPackageBox = $("#examPackageBox");
            var hkTabcontent = $("#hkTabcontent");
            examPackageBox.show();
            hkTabcontent.hide();


            self.koTemplateName('');
            self.graceShowTopMenu();

            if (pid == -1) {
                examPackageBox.hide();
                hkTabcontent.show();
                setTimeout(function () {
                    $('#scrollListBox').animate({scrollTop: '1px'}, 0);
                }, 500);

                self.sendLog({
                    op: "page_select_title_tongbu_more_click",
                    s1: self.homeworkEnum.EXAM
                });

                return false;
            }
            $17.loadingStart();
            ko.utils.arrayForEach(self.packageList(), function (exam) {
                if (pid == exam.id()) {
                    self.examCurrentPackageDetail(exam.questions());
                    $17.loadingEnd();
                }
            });

            $('#scrollListBox').animate({scrollTop: '0px'}, 10);

            //set examPackageList init
            self.examPackageList([]);
            self.examPackageCurrentPage(1);
            self.getExamPackageDetails(1);

            self.sendLog({
                op: "page_select_title_tongbu_package_click",
                s1: self.homeworkEnum.EXAM,
                s2: pid
            });
        };

        /*题包总时间*/
        self.examPackageTotalMin = function () {
            var time = 0;
            ko.utils.arrayForEach(self.examCurrentPackageDetail(), function (exam) {
                time += exam.seconds();
            });
            return Math.ceil(time / 60);
        };

        /*题包-全选*/
        self.examPackageSelectAllBtn = function () {
            var addSeconds = 0;
            if (self.examCurrentPackageDetail().length > 0) {
                ko.utils.arrayForEach(self.examCurrentPackageDetail(), function (exam) {
                    var _qid = exam.id();
                    if ($.inArray(_qid, self.examCart()) == -1) {
                        //没有，就添加进去
                        self.examCart.push(_qid);
                        var _similarIds = exam.similarQuestionIds() || [];
                        var _questionObj = {
                            questionId: _qid,
                            questionBoxId: self.examCurrentPackageId(),
                            seconds: exam.seconds(),
                            submitWay: exam.submitWay(),
                            similarQuestionId: _similarIds.length > 0 ? _similarIds[0] : null

                        };
                        homeworkConstant._homeworkContent.practices.EXAM.questions.push(_questionObj);
                        if (subject == 'ENGLISH') {
                            self._unitSetBooks(exam.book.unitId(), _qid, "add", exam);
                        } else {
                            self.examSectionIntoBooks(exam.book.sectionId(), _qid, "add", exam);
                        }

                        addSeconds += exam.seconds();
                        exam.checked(true);
                    } else {
                        //$17.jqmHintBox("题目已被选入");
                    }
                });
            }
            self.examDuration(self.examDuration() + addSeconds);
            self.showQuestionsTotalCount();
            //set select count
            ko.utils.arrayForEach(self.packageList(), function (exam) {
                if (exam.id() == self.examCurrentPackageId()) {
                    var selectedTotal = 0;
                    var total = self.examCurrentPackageDetail().length;
                    ko.utils.arrayForEach(self.examCurrentPackageDetail(), function (exam) {
                        if (exam.checked()) {
                            ++selectedTotal
                        }
                    });
                    exam.selCount(selectedTotal);
                    if (selectedTotal != total) {
                        $17.jqmHintBox("有" + (total - selectedTotal) + "道题与已选题目重复");
                    }
                }
            });

            self.sendLog({
                op: "page_select_title_tongbu_package_selectAl",
                s1: self.homeworkEnum.EXAM,
                s2: self.examCurrentPackageId()
            });
        };

        /*题包-反选*/
        self.examPackageClearAllBtn = function () {
            var removeSeconds = 0;
            if (self.examCurrentPackageDetail().length > 0) {
                ko.utils.arrayForEach(self.examCurrentPackageDetail(), function (exam) {
                    var _qid = exam.id(),_questionIndex = self.examCart.indexOf(_qid);
                    self.examCart.splice(_questionIndex, 1);
                    homeworkConstant._homeworkContent.practices.EXAM.questions.splice(_questionIndex, 1);
                    removeSeconds += exam.seconds();
                    if (subject == 'ENGLISH') {
                        self._unitSetBooks(exam.book.unitId(), _qid, "remove", exam);
                    } else {
                        self.examSectionIntoBooks(exam.book.sectionId(), _qid, "remove", exam);
                    }

                    exam.checked(false);
                });
            }
            self.examDuration(self.examDuration() - removeSeconds);
            self.showQuestionsTotalCount();
            //set select count
            ko.utils.arrayForEach(self.packageList(), function (exam) {
                if (exam.id() == self.examCurrentPackageId()) {
                    exam.selCount(0);
                }
            });

            self.sendLog({
                op: "page_select_title_tongbu_package_deselect",
                s1: self.homeworkEnum.EXAM,
                s2: self.examCurrentPackageId()
            });
        };

        /*知识点展开or收起*/
        self.examPkIsActiveClick = function (that) {
            that.isActive(!that.isActive());
        };

        /*切换知识点*/
        self.examChangeKp = function () {
            var that = this;
            ko.utils.arrayForEach(self.examKnowledgePointsDetail(), function (pkd) {
                ko.utils.arrayForEach(pkd.knowledgePoints(), function (pk) {
                    pk.checked(false);
                });
            });
            that.checked(true);
            self.examKnowledgePointsListBox(false);
            var kpId = that.kpId();
            self.examFocusPointId(kpId);
            self.examFilter();
            self.examLoading(false);
            self.sendLog({
                op: "float_knowledgePoint_confirm_click",
                s1: self.homeworkEnum.EXAM,
                s2: kpId
            });
        };

        self.examKnowledgePointsBtn = function () {
            self.examKnowledgePointsListBox(true);
            self.sendLog({
                op: "page_select_title_tongbu_more_knowledgePointSift_click",
                s1: self.homeworkEnum.EXAM
            });
        };
        /*-----------------------------exam end------------------------------*/

        /*-----------------------------口算 start ------------------------------*/
        /*查询口算题*/
        self.getMentalQuestions = function ($this) {
            var questionCount = 0,kpId = $this.kp_id(),chosenQuestions = [];
            //计算questionCount最大值
            //questionCount = $this.question_count() - ($this.question_count() % self.mentalConvenient);
            if (self.mentalQuestionsDetail.hasOwnProperty(kpId)) {
                var specialQuestions = self.mentalQuestionsDetail[kpId];
                for(var m = 0,mLen = specialQuestions.length; m < mLen; m++){
                    chosenQuestions.push(specialQuestions[m].questionId);
                }
            }

            $.post("/teacher/homework/mental/questions.vpage", {
                knowledgePoint : kpId,
                contentTypeId  : $this.content_type_id(),
                questionIds    : chosenQuestions.length > 0 ? chosenQuestions.join(",") : null,
                questionCount  : self.mentalConvenient
            }, function (data) {
                if (data.success) {
                    ($.inArray(kpId, self.mentalSelectedKpId) != -1) && self.mentalSelectedKpId.push($this.kp_id());
                    self.mentalQuestionsDetail[kpId] = (self.mentalQuestionsDetail[kpId] || []).concat(data.questions);
                    self.mentalSetQuestion($this);
                }
            });
        };

        /*根据选择题数，记录保存结果*/
        var mental = {}, mentalSecondsObj = {}, _idsList = [];
        self.mentalSetQuestion = function ($this) {
            var kpId = $this.kp_id();
            var mentalList = [], seconds = 0, totalSeconds = 0;
            mental[kpId] = [];//清空kpId对应的question
            homeworkConstant._homeworkContent.practices.MENTAL.questions = [];
            for (var i = 0, mLen = self.mentalQuestionsDetail[kpId]; i < $this.count(); i++) {
                var _mq = {
                    questionId: mLen[i].questionId,
                    seconds: mLen[i].seconds,
                    knowledgePointId: kpId,
                    sectionId: $this.book.sectionId()
                };
                mentalList.push(_mq);
                seconds += mLen[i].seconds;
            }
            mentalSecondsObj[kpId] = seconds;
            mental[kpId] = [].concat(mentalList);
            //计算所选题目的总时长
            for (var m in mentalSecondsObj) {
                totalSeconds += mentalSecondsObj[m];
            }
            self.mentalDuration(totalSeconds);

            //写入保存结果中
            var mentalQuestionsLength = 0;
            /*if ($.inArray($this.book.sectionId(), _idsList) == -1) {
             _idsList.push($this.book.sectionId());
             }*/
            var _mentalList = [];
            for (var j in mental) {
                if (!mental.hasOwnProperty(j)) {
                    continue;
                }
                for (var k = 0; k < mental[j].length; k++) {
                    homeworkConstant._homeworkContent.practices.MENTAL.questions.push(mental[j][k]);

                    //set book
                    var sectionIndex = -1;
                    for (var n = 0, nLen = _mentalList.length; n < nLen; n++) {
                        if (_mentalList[n].sectionId == mental[j][k].sectionId) {
                            sectionIndex = n;
                            break;
                        }
                    }
                    if (sectionIndex == -1) {
                        var _newBookObj = {
                            bookId: $this.book.bookId() || null,
                            unitId: $this.book.unitId() || null,
                            lessonId: mental[j][k].lessonId || null,
                            sectionId: mental[j][k].sectionId || null,
                            includeQuestions: [mental[j][k].questionId]
                        };
                        _mentalList.push(_newBookObj);
                    } else {
                        if ($.inArray(mental[j][k].questionId, _mentalList[sectionIndex].includeQuestions) == -1) {

                            _mentalList[sectionIndex].includeQuestions.push(mental[j][k].questionId);
                        }
                    }
                }
                mentalQuestionsLength += mental[j].length;
            }
            homeworkConstant._homeworkContent.books.MENTAL = _mentalList;
            self.mentalCartCount(mentalQuestionsLength);
        };

        self.minusMentalClick = function () {
            var that = this;
            if (that.count() > 0) {
                var ct = that.count() - self.mentalConvenient
                    ,kpId = that.kp_id();
                that.count(ct);
                self.mentalQuestionsDetail[kpId] = self.mentalQuestionsDetail[kpId].slice(0,ct);
                self.mentalSetQuestion(that);
                self.sendLog({
                    op: "page_select_title_MentalArithmetic_deselect_click",
                    s1: self.homeworkEnum.MENTAL,
                    s2: that.id()
                });
            }
        };

        self.plusMentalClick = function () {
            var that = this;
            if (that.count() < that.maxCount()) {
                that.count(that.count() + self.mentalConvenient);
                self.getMentalQuestions(that);

                self.sendLog({
                    op: "page_select_title_MentalArithmetic_select_click",
                    s1: self.homeworkEnum.MENTAL,
                    s2: that.id()
                });

            }
        };
        /*-----------------------------口算 end------------------------------*/

        /*-----------------------------测验 start -----------------------------*/
        self.quizUpdateChecked = function (_questionId, bool) {
            ko.utils.arrayForEach(self.quizCurrentPackageDetail(), function (exam) {
                if (exam.id() == _questionId) {
                    exam.checked(bool);
                }
            });
        };

        /*选入测验*/
        self.addQuiz = function () {
            var that = this;
            var _questionId = that.id();
            if ($.inArray(_questionId, self.quizCart()) == -1) {
                that.checked(true);
                self.quizCart.push(_questionId);
                var _submitWay = ko.mapping.toJS(that.submitWay());
                var _questionObj = {
                    questionId: _questionId,
                    seconds: that.seconds(),
                    submitWay: _submitWay,
                    paperId: self.quizCurrentPackageId()

                };
                homeworkConstant._homeworkContent.practices.UNIT_QUIZ.questions.push(_questionObj);
                self.quizSetBookInfo('add', that);
                self.showQuestionsTotalCount();
                self.quizDuration(self.quizDuration() + that.seconds());

                //set select count
                ko.utils.arrayForEach(self.quizPackageList(), function (exam) {
                    if (exam.id() == self.quizCurrentPackageId()) {
                        exam.selCount(exam.selCount() + 1);
                    }
                });

                self.quizUpdateChecked(_questionId, true);

            } else {
                $17.jqmHintBox("该题与已选题目重复");
            }

            self.sendLog({
                op: "page_select_title_exam_examName_select_click",
                s1: self.homeworkEnum.UNIT_QUIZ,
                s2: self.quizCurrentPackageId(),
                s3: _questionId
            });

        };

        /*移除测验*/
        self.removeQuiz = function () {
            var that = this;
            var _questionId = that.id();
            that.checked(false);
            var _questionIndex = self.quizCart.indexOf(_questionId);
            self.quizCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.UNIT_QUIZ.questions.splice(_questionIndex, 1);
            self.quizSetBookInfo('remove', that);
            self.showQuestionsTotalCount();
            self.quizDuration(self.quizDuration() - that.seconds());

            //set select count
            ko.utils.arrayForEach(self.quizPackageList(), function (exam) {
                if (exam.id() == self.quizCurrentPackageId()) {
                    exam.selCount(exam.selCount() - 1);
                }
            });

            self.quizUpdateChecked(_questionId, false);

            self.sendLog({
                op: "page_select_title_exam_examName_deselect_click",
                s1: self.homeworkEnum.UNIT_QUIZ,
                s2: self.quizCurrentPackageId(),
                s3: _questionId
            });

        };

        /*测验预览展示*/
        self.loadQuizImg = function (examId, index) {
            var $quizImg = $("#quizImg" + index);
            $quizImg.empty();
            $("<div></div>").attr("id", "qImg-" + index).appendTo($quizImg);
            var node = document.getElementById("qImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'teacher_preview',
                ids: examId,
                showExplain: true
            });
        };


        /*quiz set book*/
        self.quizSetBookInfo = function (operate, $this) {
            var book = {
                bookId: homeworkConstant.bookId,
                unitId: homeworkConstant.unitId,
                includeQuestions: []
            };

            var homeworkType = self.selectedHomeworkType();
            if (operate == 'add') {
                if (homeworkConstant._homeworkContent.books[homeworkType].length == 0) {
                    book.includeQuestions.push($this.id());
                    homeworkConstant._homeworkContent.books[homeworkType].push(book);
                } else {
                    homeworkConstant._homeworkContent.books[homeworkType][0].includeQuestions.push($this.id());
                }
            } else {
                var _includeQuestions = homeworkConstant._homeworkContent.books[homeworkType][0].includeQuestions;
                var _qIndex = $.inArray($this.id(), _includeQuestions);
                if (_qIndex != -1) {
                    _includeQuestions.splice(_qIndex, 1);
                }
                if (_includeQuestions.length == 0) {
                    homeworkConstant._homeworkContent.books[homeworkType].splice(0, 1);
                }
            }
        };

        self.quizPackageAllBtn = function () {
            self.koTemplateName('quizPackageAllBox');
        };

        /*quiz题包选择*/
        self.quizPackageBoxSelected = function (that) {
            var pid = that.id();
            self.quizCurrentPackageId(pid);

            for (var i = 0, dl = self.quizPackageList(); i < dl.length; i++) {
                dl[i].checked(false);
            }
            that.checked(true);
            self.koTemplateName('');

            self.graceShowTopMenu();
            $17.loadingStart();
            ko.utils.arrayForEach(self.quizPackageList(), function (exam) {
                if (pid == exam.id()) {
                    self.quizCurrentPackageDetail(exam.questions());
                    $17.loadingEnd();
                }
            });
            $('#scrollListBox').animate({scrollTop: '0px'}, 10);

            //set examPackageList init
            self.quizDetail([]);
            self.quizPackageCurrentPage(1);
            self.getQuizPackageDetails(1);

            self.sendLog({
                op: "page_select_title_exam_examName_click",
                s1: self.homeworkEnum.UNIT_QUIZ,
                s2: pid
            });

        };

        /*quiz题包总时间*/
        self.quizPackageTotalMin = function () {
            var time = 0;
            ko.utils.arrayForEach(self.quizCurrentPackageDetail(), function (exam) {
                time += exam.seconds();
            });
            return Math.ceil(time / 60);
        };


        /*quiz包是否被全选*/
        self.quizPackageIsSelectAll = function () {
            var totalLength = self.quizCurrentPackageDetail().length;
            var selectedTotal = 0;
            ko.utils.arrayForEach(self.quizCurrentPackageDetail(), function (exam) {
                if (exam.checked()) {
                    ++selectedTotal
                }
            });
            return totalLength == selectedTotal
        };

        /*quiz 分页加载数据*/
        self.getQuizPackageDetails = function (page) {
            var groupExam = self.quizCurrentPackageDetail.slice((page - 1) * self.quizPackagePageSize, page * self.quizPackagePageSize);
            ko.utils.arrayForEach(groupExam, function (exam) {
                if ($.inArray(exam.id, self.quizCart()) != -1) {
                    exam.checked = true;
                }
                self.quizDetail.push(ko.mapping.fromJS(exam));
            });
        };

        /*quiz 渲染题目*/
        self.loadQuizPackageSingleImg = function (examId, index) {
            var $mathExamImg = $("#quizPackageSingleImg" + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "quizPackageImg-" + index).appendTo($mathExamImg);
            var node = document.getElementById("quizPackageImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: examId,
                showExplain: false,
                showAudio: false
            });
        };


        /*题包-全选*/
        self.quizPackageSelectAllBtn = function () {
            var addSeconds = 0;
            if (self.quizCurrentPackageDetail().length > 0) {
                ko.utils.arrayForEach(self.quizCurrentPackageDetail(), function (exam) {
                    var _qid = exam.id();
                    if ($.inArray(_qid, self.quizCart()) == -1) {
                        //没有，就添加进去
                        self.quizCart.push(_qid);
                        var _questionObj = {
                            questionId: _qid,
                            paperId: self.quizCurrentPackageId(),
                            seconds: exam.seconds(),
                            submitWay: exam.submitWay()
                        };
                        homeworkConstant._homeworkContent.practices.UNIT_QUIZ.questions.push(_questionObj);
                        self.quizSetBookInfo('add', exam);
                        addSeconds += exam.seconds();
                        exam.checked(true);
                    } else {
                        //$17.jqmHintBox("题目已被选入");
                    }
                });
            }
            self.quizDuration(self.quizDuration() + addSeconds);
            self.showQuestionsTotalCount();
            //set select count
            ko.utils.arrayForEach(self.quizPackageList(), function (exam) {
                if (exam.id() == self.quizCurrentPackageId()) {
                    var selectedTotal = 0;
                    var total = self.quizCurrentPackageDetail().length;
                    ko.utils.arrayForEach(self.quizCurrentPackageDetail(), function (exam) {
                        if (exam.checked()) {
                            ++selectedTotal
                        }
                    });
                    exam.selCount(selectedTotal);
                    if (selectedTotal != total) {
                        $17.jqmHintBox("有" + (total - selectedTotal) + "道题与已选题目重复");
                    }
                }
            });

            self.sendLog({
                op: "page_select_title_exam_selectall_click",
                s1: self.homeworkEnum.UNIT_QUIZ,
                s2: self.quizCurrentPackageId()
            });
        };

        /*题包-反选*/
        self.quizPackageClearAllBtn = function () {
            var removeSeconds = 0;
            if (self.quizCurrentPackageDetail().length > 0) {
                ko.utils.arrayForEach(self.quizCurrentPackageDetail(), function (exam) {
                    var _qid = exam.id();
                    self.quizCart.splice(_qid, 1);
                    homeworkConstant._homeworkContent.practices.UNIT_QUIZ.questions.splice(_qid, 1);
                    removeSeconds += exam.seconds();
                    self.quizSetBookInfo('remove', exam);
                    exam.checked(false);
                });
            }
            self.quizDuration(self.quizDuration() - removeSeconds);
            self.showQuestionsTotalCount();
            //set select count
            ko.utils.arrayForEach(self.quizPackageList(), function (exam) {
                if (exam.id() == self.quizCurrentPackageId()) {
                    exam.selCount(0);
                }
            });

            self.sendLog({
                op: "page_select_title_exam_deselectall_click",
                s1: self.homeworkEnum.UNIT_QUIZ,
                s2: self.quizCurrentPackageId()
            });


        };

        /*-----------------------------测验 end------------------------------*/

        /*-----------------------------PHOTO_OBJECTIVE start------------------------------*/
        /*动手做一做-选入*/
        self.addPhotoObjective = function () {
            var that = this;
            that.checked(true);
            var _questionId = that.questionId();
            self.photoObjectiveCart.push(_questionId);
            var _submitWay = ko.mapping.toJS(that.submitWay());
            var _questionObj = {
                questionId: _questionId,
                seconds: that.seconds(),
                submitWay: _submitWay
            };
            homeworkConstant._homeworkContent.practices.PHOTO_OBJECTIVE.questions.push(_questionObj);
            self.showQuestionsTotalCount();
            self.photoObjectiveDuration(self.photoObjectiveDuration() + that.seconds());
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "add", that);
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_assign_home_' + subject,
                op: 'PHOTO_OBJECTIVE_select_btn'
            });
        };

        /*动手做一做-移除*/
        self.removePhotoObjective = function () {
            var that = this;
            var _questionId = that.questionId();
            that.checked(false);
            var _questionIndex = self.photoObjectiveCart.indexOf(_questionId);
            self.photoObjectiveCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.PHOTO_OBJECTIVE.questions.splice(_questionIndex, 1);
            self.showQuestionsTotalCount();
            self.photoObjectiveDuration(self.photoObjectiveDuration() - that.seconds());
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "remove", that);
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_assign_home_' + subject,
                op: 'PHOTO_OBJECTIVE_cancel_btn'
            });
        };

        /*动手做一做-展示*/
        self.loadPhotoObjectiveImg = function (examId, index) {
            var $photoObjectiveImg = $("#photoObjectiveImg" + index);
            $photoObjectiveImg.empty();
            $("<div></div>").attr("id", "poImg-" + index).appendTo($photoObjectiveImg);
            var node = document.getElementById("poImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'teacher_preview',
                ids: examId
            });
        };


        /*-----------------------------PHOTO_OBJECTIVE end------------------------------*/

        /*-----------------------------VOICE_OBJECTIVE start------------------------------*/
        /*概念说一说-选入*/
        self.addVoiceObjective = function () {
            var that = this;
            that.checked(true);
            var _questionId = that.questionId();
            self.voiceObjectiveCart.push(_questionId);
            var _submitWay = ko.mapping.toJS(that.submitWay());
            var _questionObj = {
                questionId: _questionId,
                seconds: that.seconds(),
                submitWay: _submitWay
            };
            homeworkConstant._homeworkContent.practices.VOICE_OBJECTIVE.questions.push(_questionObj);
            self.showQuestionsTotalCount();
            self.voiceObjectiveDuration(self.voiceObjectiveDuration() + that.seconds());
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "add", that);
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_assign_home_' + subject,
                op: 'VOICE_OBJECTIVE_select_btn'
            });
        };

        /*概念说一说-移除*/
        self.removeVoiceObjective = function () {
            var that = this;
            var _questionId = that.questionId();
            that.checked(false);
            var _questionIndex = self.voiceObjectiveCart.indexOf(_questionId);
            self.voiceObjectiveCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.VOICE_OBJECTIVE.questions.splice(_questionIndex, 1);
            self.showQuestionsTotalCount();
            self.voiceObjectiveDuration(self.voiceObjectiveDuration() - that.seconds());
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "remove", that);
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_assign_home_' + subject,
                op: 'VOICE_OBJECTIVE_cancel_btn'
            });
        };

        /*概念说一说-展示*/
        self.loadVoiceObjectiveImg = function (examId, index) {
            var $voiceObjectiveImg = $("#voiceObjectiveImg" + index);
            $voiceObjectiveImg.empty();
            $("<div></div>").attr("id", "voImg-" + index).appendTo($voiceObjectiveImg);
            var node = document.getElementById("voImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'teacher_preview',
                ids: examId
            });
        };


        /*-----------------------------VOICE_OBJECTIVE end------------------------------*/


        /*-----------------------------生字词练习 start------------------------------*/
        /*分页获取应试*/
        self.getWordPracticeQuestions = function (page) {
            var groupExam = self.wordPracticeQuestions.slice((page - 1) * self.wordPracticePageSize, page * self.wordPracticePageSize);
            ko.utils.arrayForEach(groupExam, function (exam) {
                if ($.inArray(exam.id, self.wordPracticeCart()) != -1) {
                    exam.checked = true;
                }
                self.wordPracticeQuestionList.push(ko.mapping.fromJS(exam));
            });
        };

        //题包预览  展示
        self.loadWordPracticeImg = function (examId, index) {
            var $quizImg = $("#wordPracticeImg" + index);
            $quizImg.empty();
            $("<div></div>").attr("id", "wordImg-" + index).appendTo($quizImg);
            var node = document.getElementById("wordImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: examId
            });
        };

        //选入生字词练习
        self.addWordPractice = function () {
            var that = this;
            var _questionId = that.id();
            if ($.inArray(_questionId, self.wordPracticeCart()) == -1) {
                that.checked(true);
                self.wordPracticeCart.push(_questionId);
                var _submitWay = ko.mapping.toJS(that.submitWay());
                var _questionObj = {
                    questionId: _questionId,
                    seconds: that.seconds(),
                    submitWay: _submitWay
                };
                homeworkConstant._homeworkContent.practices.WORD_PRACTICE.questions.push(_questionObj);
                self.examSectionIntoBooks(that.book.sectionId(), _questionId, "add", that);
                self.showQuestionsTotalCount();
                self.wordPracticeDuration(self.wordPracticeDuration() + that.seconds());

            }

            self.sendLog({
                op: "page_select_title_unfamiliar_word_select_click",
                s1: self.homeworkEnum.WORD_PRACTICE,
                s2: _questionId
            });

        };
        //移除生字词练习
        self.removeWordPractice = function () {
            var that = this;
            var _questionId = that.id();
            that.checked(false);
            var _questionIndex = self.wordPracticeCart.indexOf(_questionId);
            self.wordPracticeCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.WORD_PRACTICE.questions.splice(_questionIndex, 1);
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "remove", that);
            self.showQuestionsTotalCount();
            self.wordPracticeDuration(self.wordPracticeDuration() - that.seconds());
            self.sendLog({
                op: "page_select_title_unfamiliar_word_deselect_click",
                s1: self.homeworkEnum.WORD_PRACTICE,
                s2: _questionId
            });
        };

        //根据类型筛选
        self.wordPracticeFilter = function () {
            var _wordQuestions = self.wordPracticeAllQuestionList, _wordSearch = [];
            var word = self.wordSearchByWordName();
            var categories = self.wordSearchByCategoriesName();
            for (var w = 0; w < _wordQuestions.length; w++) {
                if (word != '全部' && categories == '全部') {
                    if ($.inArray(word, _wordQuestions[w].words) != -1) {
                        _wordSearch.push(_wordQuestions[w]);
                    }
                }

                if (word != '全部' && categories != '全部') {
                    if ($.inArray(word, _wordQuestions[w].words) != -1 && $.inArray(categories, _wordQuestions[w].tags) != -1) {
                        _wordSearch.push(_wordQuestions[w]);
                    }
                }

                if (word == '全部' && categories != '全部') {
                    if ($.inArray(categories, _wordQuestions[w].tags) != -1) {
                        _wordSearch.push(_wordQuestions[w]);
                    }
                }
                if (word == '全部' && categories == '全部') {
                    _wordSearch.push(_wordQuestions[w]);
                }
            }

            var key = self.wordSearchByArrangeVal().key(), __list = [];
            if (key != -1) {
                for (var i = 0; i < _wordSearch.length; i++) {
                    if (key == _wordSearch[i].teacherAssignTimes) {
                        __list.push(_wordSearch[i]);
                    }
                }
                _wordSearch = __list;
            }

            //初始化
            $('#wordQuestionsListBox').animate({scrollTop: '0px'}, 0);
            self.wordPracticeQuestionList([]);
            self.wordPracticeCurrentPage(1);
            self.wordPracticeQuestions = _wordSearch;
            self.getWordPracticeQuestions(1);
            self.gotoTop();
        };

        //生字词练习-筛选
        self.wordPracticeWordSearch = function (searchName) {
            if (searchName == 'categories') {//按分类
                self.wordSearchByCategoriesName(self.wordCurrentPracticeCategoriesSearchVal());
                self.sendLog({
                    op: "page_select_title_unfamiliar_word_typeSift_click",
                    s1: self.homeworkEnum.WORD_PRACTICE,
                    s2: self.wordSearchByCategoriesName()
                });
            } else if (searchName == 'practice') {//按生字
                self.wordSearchByWordName(self.wordCurrentPracticeWordSearchVal());
                self.sendLog({
                    op: "page_select_title_unfamiliar_word_wordSift_click",
                    s1: self.homeworkEnum.WORD_PRACTICE,
                    s2: self.wordSearchByWordName()
                });
            } else if (searchName == 'arrange') {//按布置
                var that = self.wordSearchByArrangeVal();
                for (var s = 0, sLen = self.wordArrangeSearchDetail(); s < sLen.length; s++) {
                    sLen[s].checked = false;
                }
                that.checked = true;

                self.sendLog({
                    op: "page_select_title_unfamiliar_word_usedSift_click",
                    s1: self.homeworkEnum.WORD_PRACTICE,
                    s2: that.name()
                });
            }
            self.wordPracticeFilter();
            self.examSearchLogger();
        };

        /*-----------------------------生字词练习 end------------------------------*/

        /*-----------------------------课文背读练习 start------------------------------*/
        var answerWayBox = $("#answerWayBox"), readReciteSelectAllBtn = $("#readReciteSelectAllBtn");
        //答题方式
        answerWayBox.find('span').on('click', function () {
            var $this = $(this);
            var way = $this.data('way');
            if (self.readReciteCartCount() > 0) {
                var myModal = new jBox('Confirm', {
                    content: '所选段落的答题方式都会改变，确认要更换吗？',
                    confirmButton: '确定',
                    cancelButton: "取消",
                    confirm: function () {
                        $this.addClass("active").siblings().removeClass("active");
                        var read = homeworkConstant._homeworkContent.practices.READ_RECITE.questions;
                        for (var i = 0; i < read.length; i++) {
                            read[i].answerWay = [[way]];
                        }
                    }
                });
                myModal.open();
            } else {
                $this.addClass("active").siblings().removeClass("active");
            }

            var readMap = {'1000': "READ", '1001': "RECITE"};
            if (way == 1000) {
                self.sendLog({
                    op: "page_select_title_text_readrecite_read_click",
                    s1: self.homeworkEnum.READ_RECITE,
                    s2: "朗读"
                });
            } else {
                self.sendLog({
                    op: "page_select_title_text_readrecite_recite_click",
                    s1: self.homeworkEnum.READ_RECITE,
                    s2: "背诵"
                });
            }
        });

        self._addReadRecite = function (that) {
            that.checked(true);
            var _questionId = that.questionId();

            if ($.inArray(_questionId, self.readReciteCart()) == -1) {
                self.readReciteCart.push(_questionId);
            }
            var _submitWay = answerWayBox.find('span.active').data('way');
            var _questionObj = {
                questionId: _questionId,
                seconds: that.seconds(),
                submitWay: that.submitWay(),
                answerWay: [[_submitWay]],
                sentenceIds: that.sentenceIds()
            };

            homeworkConstant._homeworkContent.practices.READ_RECITE.questions.push(_questionObj);
            self.showQuestionsTotalCount();
            self.readReciteDuration(self.readReciteDuration() + that.seconds());
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "add", that);
        };

        self._removeReadRecite = function (that) {
            var _questionId = that.questionId();
            that.checked(false);
            var _questionIndex = self.readReciteCart.indexOf(_questionId);
            self.readReciteCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.READ_RECITE.questions.splice(_questionIndex, 1);
            self.showQuestionsTotalCount();
            self.readReciteDuration(self.readReciteDuration() - that.seconds());
            self.examSectionIntoBooks(that.book.sectionId(), _questionId, "remove", that);
        };

        self._readReciteSelectAll = function () {
            if (self.readReciteCartCount() == self.readReciteDetail().length) {
                readReciteSelectAllBtn.addClass("active");
            } else {
                readReciteSelectAllBtn.removeClass('active');
            }
        };

        //选入
        self.addReadRecite = function () {
            var that = this;
            self._addReadRecite(that);
            self._readReciteSelectAll();
            self.sendLog({
                op: "page_select_title_text_readrecite_paragraph_select",
                s1: self.homeworkEnum.READ_RECITE,
                s2: answerWayBox.find('span.active').text(),
                s3: that.questionId()
            });
        };

        //移除
        self.removeReadRecite = function () {
            var that = this;
            self._removeReadRecite(that);
            self._readReciteSelectAll();
            self.sendLog({
                op: "page_select_title_text_readrecite_paragraph_desele",
                s1: self.homeworkEnum.READ_RECITE,
                s2: answerWayBox.find('span.active').text(),
                s3: that.questionId()
            });
        };

        //全选or反选
        readReciteSelectAllBtn.on('click', function () {
            var $this = $(this);
            $this.toggleClass('active');
            self.readReciteCart([]);
            homeworkConstant._homeworkContent.practices.READ_RECITE.questions = [];
            var selectAll = !!$this.hasClass('active');

            ko.utils.arrayForEach(self.readReciteDetail(), function (read) {
                read.checked(selectAll);
            });

            for (var r = 0, rc = self.readReciteAllQuestions; r < rc.length; r++) {
                rc[r]['checked'] = selectAll;
                var _questionId = rc[r].questionId;
                if (selectAll) {
                    if ($.inArray(_questionId, self.readReciteCart()) == -1) {
                        self.readReciteCart.push(_questionId);
                    }
                    var _submitWay = answerWayBox.find('span.active').data('way');
                    var _questionObj = {
                        questionId: _questionId,
                        seconds: rc[r].seconds,
                        submitWay: rc[r].submitWay,
                        answerWay: [[_submitWay]],
                        sentenceIds: rc[r].sentenceIds
                    };
                    homeworkConstant._homeworkContent.practices.READ_RECITE.questions.push(_questionObj);
                    self.showQuestionsTotalCount();
                    self.readReciteDuration(self.readReciteDuration() + rc[r].seconds);
                    self.examSectionIntoBooks(rc[r].book.sectionId, _questionId, "add", ko.mapping.fromJS(rc[r]));
                } else {
                    self.readReciteCart([]);
                    homeworkConstant._homeworkContent.practices.READ_RECITE.questions = [];
                    self.showQuestionsTotalCount();
                    self.readReciteDuration(0);
                    self.examSectionIntoBooks(rc[r].book.sectionId, _questionId, "remove", ko.mapping.fromJS(rc[r]));
                }
            }

            if (selectAll) {
                self.sendLog({
                    op: "page_select_title_text_readrecite_selectAll_click",
                    s1: self.homeworkEnum.READ_RECITE,
                    s2: answerWayBox.find('span.active').text()
                });
            } else {
                self.sendLog({
                    op: "page_select_title_text_readrecite_deselectAll_click",
                    s1: self.homeworkEnum.READ_RECITE,
                    s2: answerWayBox.find('span.active').text()
                });
            }
        });

        self.loadReadReciteImg = function (examId, index) {
            var $quizImg = $("#readReciteImg" + index);
            $quizImg.empty();
            $("<div></div>").attr("id", "rdImg-" + index).appendTo($quizImg);
            var node = document.getElementById("rdImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: examId
            });
        };

        /*分页获取应试*/
        self.getReadReciteQuestions = function (page) {
            self.readReciteExamLoading(false);
            var groupExam = self.readReciteQuestions.slice((page - 1) * self.readRecitePageSize, page * self.readRecitePageSize);
            ko.utils.arrayForEach(groupExam, function (read) {
                if ($.inArray(read.id, self.readReciteCart()) != -1) {
                    read.checked = true;
                }
                self.readReciteDetail.push(ko.mapping.fromJS(read));
                self.readReciteExamLoading(true);
            });
        };

        /*-----------------------------课文背读练习 end------------------------------*/

        //设置作业列表页面，已选择的题目
        self.setSelectHomeworkCount = function () {
            var examCount = self.examCartCount();
            var _package = self.packageDetail();
            for (var i = 0; i < _package.length; i++) {
                switch (_package[i].type()) {
                    case self.homeworkEnum.EXAM:
                        _package[i].count(examCount);
                        break;

                    case self.homeworkEnum.MENTAL:
                        _package[i].count(self.mentalCartCount());
                        break;

                    case self.homeworkEnum.UNIT_QUIZ:
                        _package[i].count(self.quizQuestionsCount());
                        break;

                    case self.homeworkEnum.PHOTO_OBJECTIVE:
                        _package[i].count(self.photoObjectiveCartCount());
                        break;

                    case self.homeworkEnum.VOICE_OBJECTIVE:
                        _package[i].count(self.voiceObjectiveCartCount());
                        break;
                    case self.homeworkEnum.WORD_PRACTICE:
                        _package[i].count(self.wordPracticeCartCount());
                        break;
                    case self.homeworkEnum.READ_RECITE:
                        _package[i].count(self.readReciteCartCount());
                        break;
                    case self.homeworkEnum.BASIC_APP:
                        _package[i].count(self.basicAppCardCount().questionCount);
                        break;
                    case self.homeworkEnum.READING:
                        _package[i].count(self.readingCardCount());
                        break;
                    case self.homeworkEnum.ORAL_PRACTICE:
                        _package[i].count(self.oralPracticeCartCount());
                        break;
                }
            }
        };

        /*作业编辑*/
        self.homeworkEditClick = function () {
            self.showHomeworkFinishBtn(true);

            self.sendLog({
                op: "page_typelist_edit_click"
            });
        };

        /*作业编辑完成*/
        self.homeworkFinishClick = function () {
            self.showHomeworkFinishBtn(false);

            self.sendLog({
                op: "page_typelist_finish_click"
            });
        };

        /*作业清空*/
        self.clearAllBtn = function () {
            var that = this;
            var type = that.type();
            switch (type) {
                case self.homeworkEnum.EXAM:
                    //清空精题包
                    var _packageList = self.packageList();
                    ko.utils.arrayForEach(_packageList, function (packageList) {
                        packageList.selCount(0);
                        if (packageList.id() != -1) {
                            ko.utils.arrayForEach(packageList.questions(), function (questions) {
                                questions.checked(false);
                            });
                        }
                    });
                    //清空全部题目
                    var _questionList = self.questionList();
                    for (var q = 0, qLen = _questionList.length; q < qLen; q++) {
                        _questionList[q].checked(false);
                    }
                    self.examCart([]);
                    self.examDuration(0);
                    break;

                case self.homeworkEnum.MENTAL:
                    var _pointList = self.mentalDetail();
                    for (var m = 0, mLen = _pointList.length; m < mLen; m++) {
                        _pointList[m].count(0);
                    }
                    //清除口算题
                    self.mentalCartCount(0);
                    self.mentalDuration(0);
                    self.mentalQuestionsDetail = {};
                    mental = {};
                    mentalSecondsObj = {};
                    break;

                case self.homeworkEnum.UNIT_QUIZ:
                    ko.utils.arrayForEach(self.quizPackageList(), function (packageList) {
                        packageList.selCount(0);
                        ko.utils.arrayForEach(packageList.questions(), function (questions) {
                            questions.checked(false);
                        });
                    });

                    self.quizCart([]);
                    self.quizDuration(0);
                    break;
                case self.homeworkEnum.PHOTO_OBJECTIVE:
                    self.photoObjectiveCart([]);
                    var _photoObjective = self.photoObjectiveDetail();
                    for (var pho = 0, phoLen = _photoObjective.length; pho < phoLen; pho++) {
                        _photoObjective[pho].checked(false);
                    }
                    self.photoObjectiveDuration(0);
                    break;

                case self.homeworkEnum.VOICE_OBJECTIVE:
                    self.voiceObjectiveCart([]);
                    var _voiceObjective = self.voiceObjectiveDetail();
                    for (var vo = 0, voLen = _voiceObjective.length; vo < voLen; vo++) {
                        _voiceObjective[vo].checked(false);
                    }
                    self.voiceObjectiveDuration(0);
                    break;

                case self.homeworkEnum.WORD_PRACTICE:
                    self.wordPracticeCart([]);
                    var _wordPractice = self.wordPracticeQuestionList();
                    for (var wo = 0, woLen = _wordPractice.length; wo < woLen; wo++) {
                        _wordPractice[wo].checked(false);
                    }
                    self.wordPracticeDuration(0);
                    break;
                case self.homeworkEnum.READ_RECITE:
                    self.readReciteCart([]);
                    var _readRecite = self.readReciteDetail();
                    for (var rd = 0, rdLen = _readRecite.length; rd < rdLen; rd++) {
                        _readRecite[rd].checked(false);
                    }
                    self.readReciteDuration(0);
                    self._readReciteSelectAll();
                    break;
                case self.homeworkEnum.BASIC_APP:
                    ko.utils.arrayForEach(self.basicAppDetail(), function (detail) {
                        ko.utils.arrayForEach(detail.categories(), function (cate) {
                            cate.checked(false);
                        });
                    });
                    break;
                case self.homeworkEnum.READING:
                    ko.utils.arrayForEach(self.readingWeeklyDetail(), function (weekly) {
                        weekly.checked(false);
                    });
                    ko.utils.arrayForEach(self.readingSynchronousDetail(), function (weekly) {
                        weekly.checked(false);
                    });
                    ko.utils.arrayForEach(self.readingAllDetail(), function (weekly) {
                        weekly.checked(false);
                    });
                    self.readingCard([]);
                    self.readingDuration(0);
                    break;
                case self.homeworkEnum.ORAL_PRACTICE:
                    //清空精题包
                    var _oralPracticePackageList = self.oralPracticePackageList();
                    ko.utils.arrayForEach(_oralPracticePackageList, function (packageList) {
                        packageList.selCount(0);
                        if (packageList.id() != -1) {
                            ko.utils.arrayForEach(packageList.questions(), function (questions) {
                                questions.checked(false);
                            });
                        }
                    });
                    self.oralPracticeCart([]);
                    self.oralPracticeDuration(0);
                    break;
            }
            homeworkConstant._homeworkContent.practices[type].questions = [];
            homeworkConstant._homeworkContent.practices[type].apps = [];
            homeworkConstant._homeworkContent.books[type] = [];
            self.setSelectHomeworkCount();
            self.showQuestionsTotalCount();

            self.sendLog({
                op: "page_typelist_empty_click",
                s1: type
            });
        };

        /*展示已选择的题数*/
        self.showQuestionsTotalCount = function () {
            return self.examCartCount() +
                self.mentalCartCount() +
                self.quizQuestionsCount() +
                self.photoObjectiveCartCount() +
                self.voiceObjectiveCartCount() +
                self.wordPracticeCartCount() +
                self.basicAppCardCount().questionCount +
                self.readingCardCount() +
                self.readReciteCartCount() +
                self.oralPracticeCartCount();
        };

        /*展示已选择的题目总时间*/
        self.showQuestionsTotalDuration = function () {
            return self.examDuration() +
                self.mentalDuration() +
                self.quizDuration() +
                self.basicAppCardCount().seconds +
                self.photoObjectiveDuration() +
                self.voiceObjectiveDuration() +
                self.wordPracticeDuration() +
                self.readingDuration() +
                self.readReciteDuration() +
                self.oralPracticeDuration();
        };

        /*选好了 去布置*/
        self.selectFinishedBtn = function () {
            self.showHomeworkPackageDetailBox(false);
            self.showHomeworkPackageList(true);
            self.setSelectHomeworkCount();
            $17.backToTop();

            self.sendLog({
                op: "page_select_title_gotoassign_click"
            });
        };

        self._gotoConfirm = function () {
            $.post('/teacher/homework/maxic.vpage', {
                clazzIds: homeworkConstant.clazzIds,
                subject: subject
            }, function (data) {
                if (data.success) {
                    self.homeworkIntegral(data.dc);
                    self.homeworkIntegralMaxCount(data.mc);
                }
            });
            self.showHomeworkPackageList(false);
            self.showConfirmBox(true);

            self.sendLog({
                op: "page_typelist_assignHomework_click"
            });
        };

        /*二次确认页展示*/
        self.gotoConfirm = function () {
            var _includeTabFlag = false; //是否包含白名单中的一项
            var _questionCount = self.showQuestionsTotalCount();
            var _practices = homeworkConstant._homeworkContent.practices;
            if (subject == "ENGLISH") {
                var _whiteTabList = ["BASIC_APP", "READING", "PHOTO_OBJECTIVE", "VOICE_OBJECTIVE"];  //只要包含其中一项，则不检查题量多少
                for (var tab in _practices) {
                    if (_practices.hasOwnProperty(tab)) {
                        var contents = (_practices[tab].questions || _practices[tab].apps) || [];
                        if (contents.length > 0) {
                            _includeTabFlag = _includeTabFlag || (_whiteTabList.indexOf(tab) != -1);
                        }
                    }
                }
            } else {
                for (var t in _practices) {
                    if (_practices.hasOwnProperty(t)) {
                        var _contents = (_practices[t].questions || _practices[t].papers) || [];
                        if (_contents.length > 0) {
                            if (t == "PHOTO_OBJECTIVE" || t == "VOICE_OBJECTIVE") {
                                _includeTabFlag = true;
                            }
                        }
                    }
                }
            }

            if (!_includeTabFlag && _questionCount < 3) {
                var confirm = new jBox('Confirm', {
                    content: '您本次作业题量过少，继续布置将不会获得园丁豆！',
                    confirmButton: '调整题目',
                    cancelButton: '继续布置',
                    closeOnClick: false,
                    minWidth: 500,
                    maxWidth: 600,
                    closeButton: false,
                    confirm: function () {
                    },
                    cancel: function () {
                        self._gotoConfirm();
                    }
                });
                confirm.open();
            } else {
                self._gotoConfirm();
            }

        };

        /*预览作业*/
        self.viewHomeworkBtn = function () {

            var _books = homeworkConstant._homeworkContent.books;
            var contents = {}, isFirstTab = true;
            for (var tab in _books) {
                if (_books.hasOwnProperty(tab)) {
                    var _ids = [];
                    for (var z = 0, zLen = _books[tab].length; z < zLen; z++) {
                        var tempIds = _books[tab][z].includeQuestions || _books[tab][z].includePapers || _books[tab][z].categories || _books[tab][z].includePictureBooks;
                        if (tab == self.homeworkEnum.BASIC_APP) {
                            for (var i = 0; i < tempIds.length; i++) {
                                tempIds[i] = tempIds[i].replace(/:/g, '|');
                            }
                        }
                        if (tempIds && tempIds.length > 0) {
                            _ids = _ids.concat(tempIds);
                        }
                    }
                    if (_ids.length > 0) {
                        contents[tab] = _ids;
                    }
                }
            }

            if ($.isEmptyObject(contents)) {
                $17.msgTip("请选择内容");
                return false
            }

            //获取已选择的第一个作业类型
            for (var j in contents) {
                if (isFirstTab) {
                    self.viewHomeworkFirstTabName(j);
                }
                isFirstTab = false;
            }

            $17.weuiLoadingShow();
            $.post('/teacher/homework/report/preview.vpage', {
                content: JSON.stringify(contents),
                bookId: homeworkConstant.bookId
            }, function (data) {
                if (data.success) {
                    self.viewHomeworkDetail(data.content);
                    self.showHomeworkPackageList(false);
                    self.showViewHomeworkBox(true);
                    self.showViewHomeworkClick(self.viewHomeworkFirstTabName());

                } else {
                    $17.msgTip("预览数据加载失败");
                }
                $17.weuiLoadingHide();
            });

            self.sendLog({
                op: "page_typelist_preview_click"
            });
        };

        /*预览作业-返回*/
        self.viewHomeworkBackBtn = function () {
            self.showHomeworkPackageList(true);
            self.showViewHomeworkBox(false);
        };

        /*切换作业类型-弹框*/
        self.showViewHomeworkTitleClick = function () {
            self.showViewHomeworkSelectBox(!self.showViewHomeworkSelectBox());
        };

        /*切换作业类型-选择*/
        self.showViewHomeworkClick = function (name) {
            for (var i = 0, dl = self.viewHomeworkDetail(); i < dl.length; i++) {
                if (name == dl[i].type) {
                    self.viewHomeworkSelectedDetail(dl[i]);
                }
            }
            self.showViewHomeworkSelectBox(false);
            self.viewHomeworkSelectedTabName(name);

            self.sendLog({
                op: "page_previewHomework_changetype_click",
                s1: name
            });
        };

        self.viewHomeworkExamImg = function (id, index) {
            var $quizImg = $("#viewHomeworkExamImg" + index);
            $quizImg.empty();
            $("<div></div>").attr("id", "qImgs-" + index).appendTo($quizImg);
            var node = document.getElementById("qImgs-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: id
            });
        };

        /*学豆奖励页面展示*/
        self.gotoIntegral = function () {
            //一期暂无奖励
            self.homeworkSubmit();
        };

        $('#scrollListBox').scroll(function () {
            var $this = $(this);
            self.divScrollTop($this.scrollTop());
            if (self.selectedHomeworkType() == self.homeworkEnum.WORD_PRACTICE) {
                $('#w_questionsSearchBox').addClass('fixTop-4');
            } else if (self.selectedHomeworkType() == self.homeworkEnum.READING) {
                var readingAll_box = $('#readingAll_box');
                var pdf = readingAll_box.find('._pft');
                if (readingAll_box.length && readingAll_box.offset().top < 0) {
                    pdf.addClass('pb-fixTop').css({'top': '1rem'});
                    readingAll_box.find('.empty-hbFixed').show();
                } else {
                    if (pdf.hasClass('pb-fixTop') && !self.readingSearchLock()) {
                        pdf.removeClass('pb-fixTop');
                        readingAll_box.find('.empty-hbFixed').hide();
                    }
                }
            }else if(self.selectedHomeworkType() == self.homeworkEnum.ORAL_PRACTICE){

            } else {
                if ($('#hkTabcontent').offset().top - $('#topMenuListBox').height() <= 0) {
                    $('#questionsSearchBox').addClass('fixTop-4');
                } else {
                    $('#questionsSearchBox').removeClass('fixTop-4');
                }
            }
            if ($this.scrollTop() == 0) {
                $('#w_questionsSearchBox').removeClass('fixTop-4');
                $17.backToTop();
            }

        });

        self.gotoTop = function () {
            $('#scrollListBox').scrollTop(0);
        };

        /*返回作业列表页*/
        self.gotoHomeworkListBox = function () {
            self.showHomeworkPackageList(true);
            self.showConfirmBox(false);

            self.sendLog({
                op: 'page_assignHomework_back_click'
            });
        };

        /*测验预览返回*/
        self.viewQuizBackBtn = function () {
            self.showQuizViewBox(false);
            self.showHomeworkPackageDetailBox(true);
            $17.backToTop();
        };

        self.getWeekName = function (weekDay) {
            return ["(星期日)", "(星期一)", "(星期二)", "(星期三)", "(星期四)", "(星期五)", "(星期六)"][weekDay];
        };

        self.showWeek = function () {
            var _endDateTime = self.showHomeworkEndDateTime() + ' 23:59:59';
            var timeArr = _endDateTime.split(/:|-|\s/g);
            var endDate = new Date(timeArr[0], timeArr[1] - 1, timeArr[2], timeArr[3], timeArr[4], timeArr[5]);
            return self.getWeekName(endDate.getDay());
        };

        /*作业时间设置*/
        self.setTimeClick = function (data, event) {
            var that = this;
            for (var t = 0, time = self.homeworkTimeList(); t < time.length; t++) {
                time[t].checked(false);
            }
            that.checked(true);
            self.setHomeworkEndDateTime(that.value(), event);
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_assign_home_' + subject,
                op: 'assign_confirm_select_time_click'
            });
        };

        self.setHomeworkEndDateTime = function (value, event) {
            var endDateTime = $('#endDateTime');
            var _startDateTime = homeworkConstant._homeworkContent.startDateTime;
            var startTimeArr = _startDateTime.split(/:|-|\s/g);
            var refDate = new Date(startTimeArr[0], startTimeArr[1] - 1, startTimeArr[2], startTimeArr[3], startTimeArr[4], startTimeArr[5]);
            var defaultTime = " 23:59:59";
            switch (value) {
                case "today":
                    self.showHomeworkEndDateTime($17.DateUtils("%Y-%M-%d", 0, "d", refDate) + defaultTime);
                    break;
                case "tomorrow":
                    self.showHomeworkEndDateTime($17.DateUtils("%Y-%M-%d", 1, "d", refDate) + defaultTime);
                    break;
                case "afterTomorrow":
                    self.showHomeworkEndDateTime($17.DateUtils("%Y-%M-%d", 2, "d", refDate) + defaultTime);
                    break;
                case "other":
                    var minDate = homeworkConstant._homeworkContent.startTime;
                    var minTime = +homeworkConstant.currentHour + 1;
                    var selectDate = homeworkConstant._homeworkContent.startTime;//默认为当天
                    //如果当前时间>=23点时，最小日期加一天
                    if (homeworkConstant.currentHour >= 23) {
                        minDate = $17.DateUtils("%Y-%M-%d", 1, "d", refDate);
                        minTime = 0;
                    }
                    var datepicker = $('#doHomeworkDate').pickadate({
                        format: 'yyyy-mm-dd',
                        firstDay: 1,
                        editable: true,
                        min: minDate,
                        onClose: function () {
                            selectDate = picker.get();
                            //是否选择的是当天
                            if (selectDate != minDate) {
                                minTime = 0;
                            } else {
                                minTime = +homeworkConstant.currentHour + 1;
                            }
                            timepicker.set('min', [minTime, 0]);
                        },
                        onSet: function (item) {
                            if ('select' in item) {
                                setTimeout(timepicker.open, 0);
                            }
                        }
                    });

                    var timepicker = $('#doHomeworkTime').pickatime({
                        interval: 60,
                        format: 'HH:i',
                        formatLabel: 'HH:i',
                        min: [minTime, 0],
                        clear: false,
                        onRender: function() {
                            $('<div style="text-align: center;">选择布置作业时段</div>').prependTo( this.$root.find('.picker__box') );
                        },
                        onOpen: function () {
                            this.set('value', endDateTime.val());
                        },
                        onSet: function (item) {
                            if ('select' in item) setTimeout(function () {
                                var dateTime = picker.get() + ' ' + timepicker.get()+":00";
                                endDateTime.val(dateTime);
                                self.showHomeworkEndDateTime(dateTime);
                            }, 0);
                        }
                    }).pickatime('picker');
                    var picker = datepicker.pickadate('picker');
                    if (picker.get('open')) {
                        picker.close();
                    } else {
                        picker.stop().start();
                        picker.open();
                    }
                    event.stopPropagation();
            }
            endDateTime.val(self.showHomeworkEndDateTime());
        };

        /*初始化日期*/
        /*超过17点后，默认为第二天*/
        if (homeworkConstant.currentHour >= 17) {
            self.setHomeworkEndDateTime("tomorrow");
            for (var t = 0, time = self.homeworkTimeList(); t < time.length; t++) {
                time[t].checked(false);
                if (time[t].value() == 'tomorrow') {
                    time[t].checked(true);
                }
            }
        } else {
            self.setHomeworkEndDateTime("today");
        }

        /*奖励学豆*/
        self.minusIntegralClick = function () {
            if (self.homeworkIntegral() > 0) {
                self.homeworkIntegral(self.homeworkIntegral() - 1);
            }
        };

        self.plusIntegralClick = function () {
            if (self.homeworkIntegral() != self.homeworkIntegralMaxCount()) {
                self.homeworkIntegral(self.homeworkIntegral() + 1);
            }
        };

        /*保存作业*/
        self.homeworkSubmit = function () {
            homeworkConstant._homeworkContent.subject = subject;
            homeworkConstant._homeworkContent.prize = self.homeworkIntegral();
            homeworkConstant._homeworkContent.startTime = homeworkConstant._homeworkContent.startDateTime;
            homeworkConstant._homeworkContent.endTime = self.showHomeworkEndDateTime();
            homeworkConstant._homeworkContent.duration = self.showQuestionsTotalDuration(); //秒
            $17.weuiLoadingShow();
            $.ajax({
                type: 'post',
                url: '/teacher/homework/assign.vpage',
                data: JSON.stringify(homeworkConstant._homeworkContent),
                success: function (data) {
                    if (data.success) {
                        $17.msgTip("布置成功了",function(){
                            if(subject === 'ENGLISH'){
                                var param = {
                                    from : "SET_HOMEWORK",
                                    subject : subject,
                                    homeworkIds : data.homeworkIds.join(",")
                                };
                                location.href = "/teacher/homework/offlinehomework/index.vpage?" + $.param(param);
                            }else{
                                window.location.href = "/teacher/homework/report/history.vpage";
                            }
                        });
                        self.sendLog({
                            op: "page_assignHomework_confirmcontent_click",
                            s1: self.homeworkIntegral(),
                            s2: homeworkConstant._homeworkContent.endTime,
                            s3: "SUCCESS"
                        });
                    } else {
                        $17.msgTip(data.info);
                        self.sendLog({
                            op: "page_assignHomework_confirmcontent_click",
                            s1: self.homeworkIntegral(),
                            s2: homeworkConstant._homeworkContent.endTime,
                            s3: "FAILURE",
                            s4: data.info || ''
                        });
                    }
                    $17.weuiLoadingHide();
                },
                error: function () {
                    $17.weuiLoadingHide();
                    logger.log({
                        app: "teacher",
                        module: 'assign',
                        op: 'assign_confirm_' + subject.toLocaleLowerCase() + '_fail_button'
                    });
                },
                dataType: 'json',
                contentType: 'application/json;charset=UTF-8'
            });
        };
    };
    /*exam初始化*/
    window.ko = ko;
    try {
        vox.exam.create(function (data) {
            if (!data.success) {
                logger.log({
                    app: "teacher",
                    module: 'homework',
                    op: 'vox_exam_create_fail'
                });
            } else {

            }
        }, false, {env: homeworkConstant.env});
    } catch (exception) {
        logger.log({
            app: "teacher",
            module: 'homework',
            op: 'vox_exam_create_exception'
        });
    }

    var homeworkModule = new HomeworkModel();

    if (subject == "ENGLISH") {
        /*basic_app*/
        require(['basicapp', 'reading','oralpractice'], function (basicapp, reading,oralpractice) {
            basicapp.call(homeworkModule);
            reading.call(homeworkModule);
            oralpractice.call(homeworkModule);
        });
    }
    ko.applyBindings(homeworkModule);

});
