/**
 * @description "口语习题"
 */

define(["$17", "logger", 'jbox'], function ($17, logger, jbox) {
    function OralPracticeModel() {
        var self = this;
        var subject = $17.getQuery("subject");
        self.oralPracticePackageList = ko.observableArray([]);
        self.oralPracticeExamPackageList = ko.observableArray([]);  //每个题包中的题的详情
        self.oralPracticePackageQuestionObj = ko.observableArray([]);//预览题包

        self.oralPracticeExamPackageCurrentPage = ko.observable(1);
        self.oralPracticePackagePageSize = 5;
        self.oralPracticeCurrentPackageId = ko.observable(-1); //当前选择题包的ID

        self.oralPracticeCurrentPackageDetail = ko.observableArray([]);

        /*应试包是否被全选*/
        self.oralPracticePackageIsSelectAll = function () {
            var totalLength = self.oralPracticeCurrentPackageDetail().length;
            var selectedTotal = 0;
            ko.utils.arrayForEach(self.oralPracticeCurrentPackageDetail(), function (exam) {
                if (exam.checked()) {
                    ++selectedTotal
                }
            });
            return totalLength == selectedTotal
        };

        self.loadOralPracticeSingleExam = function (examId, index) {
            var $mathExamImg = $("#oralPracticePackageSingleImg" + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "oralPackageImg-" + index).appendTo($mathExamImg);
            var node = document.getElementById("oralPackageImg-" + index);
            self.voxExamRender({
                node: node,
                type: 'normal',
                ids: examId,
                showExplain: false,
                showAudio: false
            });
        };

        /*英语应试set book*/
        self._unitSetBooksOralPractice = function (unitId, questionId, operate, $this) {
            if ($17.isBlank(operate) || (operate != 'add' && operate != 'remove')) {
                return false;
            }
            if ($17.isBlank(unitId) || $17.isBlank(questionId)) {
                return false;
            }
            var _bookExams = homeworkConstant._homeworkContent.books.ORAL_PRACTICE;
            var unitIndex = -1;
            for (var k = 0, kLen = _bookExams.length; k < kLen; k++) {
                if (_bookExams[k].unitId == unitId) {
                    unitIndex = k;
                    break;
                }
            }
            if (unitIndex != -1) {
                var _includeQuestions = homeworkConstant._homeworkContent.books.ORAL_PRACTICE[unitIndex].includeQuestions;
                if (operate == 'add') {
                    homeworkConstant._homeworkContent.books.ORAL_PRACTICE[unitIndex].includeQuestions.push(questionId);
                } else {
                    var _qIndex = $.inArray(questionId, _includeQuestions);
                    if (_qIndex != -1) {
                        _includeQuestions.splice(_qIndex, 1);
                    }
                    if (_includeQuestions.length == 0) {
                        homeworkConstant._homeworkContent.books.ORAL_PRACTICE.splice(unitIndex, 1);
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
                    homeworkConstant._homeworkContent.books.ORAL_PRACTICE.push(_bookObj);
                }
            }
            return true;

        };

        //选入应试
        self.addOralPracticeExam = function () {
            var that = this;
            var _questionId = that.id();
            if ($.inArray(_questionId, self.oralPracticeCart()) == -1) {
                that.checked(true);
                self.oralPracticeCart.push(_questionId);
                var _submitWay = ko.mapping.toJS(that.submitWay());
                var _similarIds = that.similarQuestionIds() || [];
                var _questionObj = {
                    questionId: _questionId,
                    seconds: that.seconds(),
                    submitWay: _submitWay,
                    questionBoxId: self.oralPracticeCurrentPackageId(),
                    similarQuestionId: _similarIds.length > 0 ? _similarIds[0] : null


                };
                homeworkConstant._homeworkContent.practices.ORAL_PRACTICE.questions.push(_questionObj);
                if (subject == 'ENGLISH') {
                    self._unitSetBooksOralPractice(that.book.unitId(), _questionId, "add", that);
                } else {
                    self.oralPracticeSectionIntoBooks(that.book.sectionId(), _questionId, "add", that)
                }

                self.showQuestionsTotalCount();
                self.oralPracticeDuration(self.oralPracticeDuration() + that.seconds());

                //set select count
                ko.utils.arrayForEach(self.oralPracticePackageList(), function (exam) {
                    if (exam.id() == self.oralPracticeCurrentPackageId()) {
                        exam.selCount(exam.selCount() + 1);
                    }
                });
            } else {
                $17.jqmHintBox("该题与已选题目重复");
            }

            if (self.oralPracticeCurrentPackageId() == -1) {
                self.sendLog({
                    op: "page_select_title_tongbu_more_select_click",
                    s1: self.homeworkEnum.ORAL_PRACTICE,
                    s2: _questionId
                });
            } else {
                self.sendLog({
                    op: "page_select_title_tongbu_package_select_click",
                    s1: self.homeworkEnum.ORAL_PRACTICE,
                    s2: self.oralPracticeCurrentPackageId(),
                    s3: _questionId
                });
            }
        };

        //移除应试
        self.removeOralPracticeExam = function () {
            var that = this;
            var _questionId = that.id();
            that.checked(false);
            var _questionIndex = self.oralPracticeCart.indexOf(_questionId);
            self.oralPracticeCart.splice(_questionIndex, 1);
            homeworkConstant._homeworkContent.practices.ORAL_PRACTICE.questions.splice(_questionIndex, 1);
            if (subject == 'ENGLISH') {
                self._unitSetBooksOralPractice(that.book.unitId(), _questionId, "remove", that);
            } else {
                self.oralPracticeSectionIntoBooks(that.book.sectionId(), _questionId, "remove", that);
            }

            self.showQuestionsTotalCount();
            self.oralPracticeDuration(self.oralPracticeDuration() - that.seconds());

            //set select count
            ko.utils.arrayForEach(self.oralPracticePackageList(), function (exam) {
                if (exam.id() == self.oralPracticeCurrentPackageId()) {
                    exam.selCount(exam.selCount() - 1);
                }
            });

            if (self.oralPracticeCurrentPackageId() == -1) {
                self.sendLog({
                    op: "page_select_title_tongbu_more_deselect_click",
                    s1: self.homeworkEnum.ORAL_PRACTICE,
                    s2: _questionId
                });
            } else {
                self.sendLog({
                    op: "page_select_title_tongbu_package_deselect_click",
                    s1: self.homeworkEnum.ORAL_PRACTICE,
                    s2: self.oralPracticeCurrentPackageId(),
                    s3: _questionId
                });
            }
        };

        /*展示全部题包*/
        self.oralPracticePackageAllBtn = function () {
            self.koTemplateName("oralPracticePackageAllBox");
        };

        self.getOralPracticePackageDetails = function (page) {
            var groupExam = self.oralPracticeCurrentPackageDetail.slice((page - 1) * self.oralPracticePackagePageSize, page * self.oralPracticePackagePageSize);
            ko.utils.arrayForEach(groupExam, function (exam) {
                if ($.inArray(exam.id, self.oralPracticeCart()) != -1) {
                    exam.checked = true;
                }
                self.oralPracticeExamPackageList.push(ko.mapping.fromJS(exam));
            });
        };

        //保存已选题目的book
        self.oralPracticeSectionIntoBooks = function (sectionId, questionId, operate, $this) {
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
        self.oralPracticePackageBoxSelected = function (that) {
            var pid = that.id();
            self.oralPracticeCurrentPackageId(pid);

            for (var i = 0, dl = self.oralPracticePackageList(); i < dl.length; i++) {
                dl[i].checked(false);
            }
            that.checked(true);
            var $oralPracticePackageBox = $("#oralPracticePackageBox");
            // var $hkTabcontent = $("#oralPracticeHkTabcontent");
            $oralPracticePackageBox.show();
            // $hkTabcontent.hide();


            self.koTemplateName('');
            self.graceShowTopMenu();

            $17.loadingStart();
            ko.utils.arrayForEach(self.oralPracticePackageList(), function (exam) {
                if (pid == exam.id()) {
                    self.oralPracticeCurrentPackageDetail(exam.questions());
                    $17.loadingEnd();
                }
            });

            $('#scrollListBox').animate({scrollTop: '0px'}, 10);

            //set oralPracticePackageList init
            self.oralPracticeExamPackageList([]);
            self.oralPracticeExamPackageCurrentPage(1);
            self.getOralPracticePackageDetails(1);

            self.sendLog({
                op: "page_select_title_tongbu_package_click",
                s1: self.homeworkEnum.ORAL_PRACTICE,
                s2: pid
            });
        };

        /*题包总时间*/
        self.oralPracticePackageTotalMin = function () {
            var time = 0;
            ko.utils.arrayForEach(self.oralPracticeCurrentPackageDetail(), function (exam) {
                time += exam.seconds();
            });
            return Math.ceil(time / 60);
        };

        /*题包-全选*/
        self.oralPracticePackageSelectAllBtn = function () {
            var addSeconds = 0;
            if (self.oralPracticeCurrentPackageDetail().length > 0) {
                ko.utils.arrayForEach(self.oralPracticeCurrentPackageDetail(), function (exam) {
                    var _qid = exam.id();
                    if ($.inArray(_qid, self.oralPracticeCart()) == -1) {
                        //没有，就添加进去
                        self.oralPracticeCart.push(_qid);
                        var _similarIds = exam.similarQuestionIds() || [];
                        var _questionObj = {
                            questionId: _qid,
                            questionBoxId: self.oralPracticeCurrentPackageId(),
                            seconds: exam.seconds(),
                            submitWay: exam.submitWay(),
                            similarQuestionId: _similarIds.length > 0 ? _similarIds[0] : null

                        };
                        homeworkConstant._homeworkContent.practices.ORAL_PRACTICE.questions.push(_questionObj);
                        if (subject == 'ENGLISH') {
                            self._unitSetBooksOralPractice(exam.book.unitId(), _qid, "add", exam);
                        } else {
                            self.oralPracticeSectionIntoBooks(exam.book.sectionId(), _qid, "add", exam);
                        }

                        addSeconds += exam.seconds();
                        exam.checked(true);
                    } else {
                        //$17.jqmHintBox("题目已被选入");
                    }
                });
            }
            self.oralPracticeDuration(self.oralPracticeDuration() + addSeconds);
            self.showQuestionsTotalCount();
            //set select count
            ko.utils.arrayForEach(self.oralPracticePackageList(), function (exam) {
                if (exam.id() == self.oralPracticeCurrentPackageId()) {
                    var selectedTotal = 0;
                    var total = self.oralPracticeCurrentPackageDetail().length;
                    ko.utils.arrayForEach(self.oralPracticeCurrentPackageDetail(), function (exam) {
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
                s1: self.homeworkEnum.ORAL_PRACTICE,
                s2: self.oralPracticeCurrentPackageId()
            });
        };

        /*题包-反选*/
        self.oralPracticePackageClearAllBtn = function () {
            var removeSeconds = 0;
            if (self.oralPracticeCurrentPackageDetail().length > 0) {
                ko.utils.arrayForEach(self.oralPracticeCurrentPackageDetail(), function (exam) {
                    var _qid = exam.id(),_questionIndex = self.oralPracticeCart.indexOf(_qid);
                    self.oralPracticeCart.splice(_questionIndex, 1);
                    homeworkConstant._homeworkContent.practices.ORAL_PRACTICE.questions.splice(_questionIndex, 1);
                    removeSeconds += exam.seconds();
                    if (subject == 'ENGLISH') {
                        self._unitSetBooksOralPractice(exam.book.unitId(), _qid, "remove", exam);
                    } else {
                        self.oralPracticeSectionIntoBooks(exam.book.sectionId(), _qid, "remove", exam);
                    }

                    exam.checked(false);
                });
            }
            self.oralPracticeDuration(self.oralPracticeDuration() - removeSeconds);
            self.showQuestionsTotalCount();
            //set select count
            ko.utils.arrayForEach(self.oralPracticePackageList(), function (exam) {
                if (exam.id() == self.oralPracticeCurrentPackageId()) {
                    exam.selCount(0);
                }
            });

            self.sendLog({
                op: "page_select_title_tongbu_package_deselect",
                s1: self.homeworkEnum.ORAL_PRACTICE,
                s2: self.oralPracticeCurrentPackageId()
            });
        };

    }

    return OralPracticeModel;
});