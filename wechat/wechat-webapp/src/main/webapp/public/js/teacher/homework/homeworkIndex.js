/**
 * 作业相关
 */
define(["$17", "knockout", "komapping", 'logger'], function ($17, ko, komapping, logger) {
    var HomeworkModel = function () {
        var self = this;
        self.showLevel = ko.observable("");// 当前已选班级
        self.selectedLevel = ko.observable(0);
        self.selectedClazz = ko.observableArray([]);//已选中的班级
        self.getSubjectValue = function (subjectName) {
            var subjectValue = '';
            switch (subjectName) {
                case "ENGLISH":
                    subjectValue = '英语';
                    break;
                case "MATH":
                    subjectValue = '数学';
                    break;
                case "CHINESE":
                    subjectValue = '语文';
                    break;
            }
            return subjectValue;
        };
        //展示没有布置作业的学科
        var sb = LoggerProxy.subject;
        if(canBeAssignedSubjects.length > 0){
            sb = canBeAssignedSubjects[0].name;
        }
        self.subject = ko.observable(sb);
        self.subjectList = canBeAssignedSubjects;
        for (var i = 0; i < self.subjectList.length; i++) {
            self.subjectList[i]['checked'] = !i;
        }
        self.subjectList = ko.mapping.fromJS(self.subjectList);
        self.hasUnitModules = ko.observable(false);

        self.levelDetail = ko.observableArray([]); //当前可布置做的
        self.batchClazzs = [[], [], [], [], [], []]; //年级

        self.unitsDetail = ko.observableArray([]);
        self.showUnitDetail = ko.observableArray([]);//展示已选择的单元-- 只能布置单个单元

        self.changeBookClazzList = ko.mapping.fromJS([
            {name: "一年级", level: 1, checked: false},
            {name: "二年级", level: 2, checked: false},
            {name: "三年级", level: 3, checked: false},
            {name: "四年级", level: 4, checked: false},
            {name: "五年级", level: 5, checked: false},
            {name: "六年级", level: 6, checked: false}
        ]);
        self.changeBookSelectedClazzLevel = ko.observable(0);
        self.changeBookSelectedClazzTerm = ko.observable(1);
        self.booksDetail = ko.observableArray([]);
        self.showChangeBookSubmit = ko.observable(false);

        /*打点*/
        self.sendLog = function(){
            var logMap = {
                app: "teacher",
                module: 'm_xYdJ7vAV',
                s0: self.subject()
            };
            $17.extend(logMap, arguments[0]);
            logger.log(logMap);

        };

        /*选择学科*/
        self.selectSubjectBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.subjectList(), function (subject) {
                subject.checked(false);
            });
            that.checked(true);
        };

        /*选择学科--提交*/
        self.submitSubjectBtn = function () {
            var subjectName = '';
            ko.utils.arrayForEach(self.subjectList(), function (subject) {
                if (subject.checked()) {
                    subjectName = subject.name();
                }
            });
            self.subject(subjectName);
            self.closeChangeEventBox();
            self.getClazzDetail();
            self.sendLog({
                op: "float_selectSubject_confirm_click"
            });
        };

        self.showClazzIds = function () {
            var cid = [];
            for (var i = 0, clazz = self.showClazzList(); i < clazz.length; i++) {
                if (clazz[i].checked) {
                    cid.push(clazz[i].clazzId + '_' + clazz[i].groupId)
                }
            }
            return cid;
        };

        self.getSelectedUnitId = function () { //获取当前单元ID
            var unitId = 0;
            //根据units中modules有无数据，选择使用units or modules
            ko.utils.arrayForEach(self.showUnitDetail(), function (modules) {
                if (modules.moduleName) {
                    ko.utils.arrayForEach(modules.units(), function (units) {
                        if (units.isDefault()) {
                            unitId = units.id();
                        }
                    })
                }
            });
            if (unitId == 0) {
                for (var i = 0, unit = self.showUnitDetail(); i < unit.length; i++) {
                    if (unit[i].isDefault()) {
                        unitId = unit[i].id();
                    }
                }
            }
            return unitId;
        };

        self.lessonsDetail = ko.observableArray([]);
        self.getSelectedSectionIds = function () { //获取已选择的SectionIds
            var sectionIds = [];
            for (var i = 0, _lesson = ko.mapping.toJS(self.lessonsDetail()); i < _lesson.length; i++) {
                for (var j = 0, _sections = _lesson[i].children; j < _sections.length; j++) {
                    if (_lesson[i].children[j]["checked"]) {
                        sectionIds.push(_lesson[i].children[j].id);
                    }
                }
            }
            if (self.subject() == 'ENGLISH') {
                sectionIds.push(-1);
            }
            return sectionIds;
        };

        self.showClazzList = ko.observableArray([]); //根据年级展示班级
        self.isAllChecked = ko.pureComputed(function () {
            return self.selectedClazz().length == self.getCheckedClazzIds().length;
        });

        /*教材相关*/
        self.defaultBookDetail = ko.observableArray([]);

        /*更换【班级、课本、单元】*/
        self.changeEvent = ko.observable("");//值：clazz、book、unit
        self.changeEventClick = function (name) {
            self.changeEvent(name);
            if (name == 'book') {
                var db = ko.mapping.toJS(self.defaultBookDetail());
                var level = db.level;
                var term = db.term;
                self.loadBookByClazzIds(level, term);
                self.changeBookSelectedClazzLevel(level);
                self.changeBookSelectedClazzTerm(term);

                self.sendLog({
                    op: "page_start_assign_change_book_click"
                });
            } else if (name == 'clazz') {
                self.sendLog({
                    op: "page_start_assign_selectClass_click"
                });
            } else if (name == 'unit') {
                self.sendLog({
                    op: "page_start_assign_unit_click"
                });
            } else if (name == 'subject') {
                ko.utils.arrayForEach(self.subjectList(), function (subject) {
                    subject.checked(false);
                    if (subject.name() == self.subject()) {
                        subject.checked(true)
                    }
                });
                self.sendLog({
                    op: "page_start_assign_selectSubject_click"
                });
            }

        };
        self.closeChangeEventBox = function () {
            self.changeEvent("");
        };

        /*更换教材*/
        self.loadBookByClazzIds = function (level, term) {
            if ($17.isBlank(level) || $17.isBlank(term)) {
                return;
            }
            self.showChangeBookSubmit(false);
            $17.weuiLoadingShow();
            $.post("/teacher/clazz/books.vpage", {level: level, term: term,subject: self.subject()}, function (data) {
                var db = ko.mapping.toJS(self.defaultBookDetail());
                if (data.success) {
                    for (var i = 0; i < data.books.length; i++) {
                        if (db.id == data.books[i].id) {
                            data.books[i].checked = true;
                            self.showChangeBookSubmit(true);
                        } else {
                            data.books[i].checked = false;
                        }
                    }
                    self.booksDetail(ko.mapping.fromJS(data.books)());
                } else {
                    $17.msgTip(data.info);
                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
            });
        };
        /*更改教材--更换年级*/
        self.changeBookClazzClick = function (level) {
            self.changeBookSelectedClazzLevel(level);
            self.loadBookByClazzIds(self.changeBookSelectedClazzLevel(), self.changeBookSelectedClazzTerm());
        };

        /*更改教材--更换上下册*/
        self.changeBookSelectedClazzTermClick = function (term) {
            self.changeBookSelectedClazzTerm(term);
            self.loadBookByClazzIds(self.changeBookSelectedClazzLevel(), self.changeBookSelectedClazzTerm());
        };

        /*选择课本*/
        self.changeBookSelectBook = function () {
            var that = this;
            for (var t = 0, book = self.booksDetail(); t < book.length; t++) {
                book[t].checked(false);
            }
            that.checked(true);
            self.showChangeBookSubmit(true);

            self.sendLog({
                op: "float_change_book_name_click"
            });
        };

        /*提交选择的课本*/
        self.changeBookSubmit = function () {
            var bookId = 0;
            for (var t = 0, book = self.booksDetail(); t < book.length; t++) {
                if (book[t].checked()) {
                    bookId = book[t].id();
                    self.defaultBookDetail(book[t]);
                }
            }
            $17.weuiLoadingShow();
            $.post('/teacher/clazz/book/change.vpage', {
                clazzAndGroupIds: self.showClazzIds().join(","),
                bookId: bookId,
                subject: self.subject()
            }, function (data) {
                if (data.success) {
                    self.closeChangeEventBox();
                    self.getUnitsDetail(self.getCheckedClazzIds().join(","), bookId);
                } else {
                    $17.msgTip("课本更换失败")
                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
            });

            self.sendLog({
                op: "float_change_book_confirm_click"
            });
        };

        /*获取可布置作业的年级*/
        self.getClazzDetail = function () {
            $17.weuiLoadingShow();
            $.post("/teacher/clazz/summary.vpage", {subject: self.subject()}, function (data) {
                var miniLevel = ''; //获取最新年级
                if (self.selectedClazz().length > 0) {
                    self.batchClazzs = [[], [], [], [], [], []];
                    self.selectedClazz([]);
                }
                if (data.success) {
                    for (var i = 0; i < data.summary.length; i++) {
                        if (data.summary[i].assignable) {
                            var level = +data.summary[i].clazzLevel;
                            self.batchClazzs[level - 1] = self.batchClazzs[level - 1].concat(data.summary[i]);
                            if ($17.isBlank(miniLevel) || miniLevel > level) {
                                miniLevel = level;
                                self.showLevel(miniLevel);
                                var _clazzList = [];
                                var _clazzs = self.batchClazzs[level - 1];
                                for (var j = 0, iLen = _clazzs.length; j < iLen; j++) {
                                    if (_clazzs[j].assignable) {
                                        _clazzs[j]["checked"] = true;
                                        _clazzList.push(_clazzs[j]);
                                    }
                                }
                                self.selectedClazz(ko.mapping.fromJS(_clazzList)());
                            }
                        }

                    }
                    if (!$17.isBlank(miniLevel)) {
                        self.levelDetail(ko.mapping.fromJS(self.batchClazzs)());
                        self.levelClick(miniLevel);
                        self.clazzSubmitBtn();
                    }
                    if (canBeAssignedSubjects.length == 0) {
                        $("#clazzBox").hide();
                        $("#noClazzBox").show();
                    }
                } else {
                    $17.msgTip(data.info);
                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
                logger.log({
                    app: "teacher",
                    module: 'WECHAT_Newhomework_assign_home_' + self.subject(),
                    op: 'summaryLoadError'
                });
            });
        };

        /*年级选择*/
        self.levelClick = function (level) {
            level = (+level) || 0;
            if (level <= 0) {
                return false;
            }
            var _clazzList = [];
            var _clazzs = self.batchClazzs[level - 1];
            for (var i = 0, iLen = _clazzs.length; i < iLen; i++) {
                if (_clazzs[i].assignable) {
                    _clazzs[i]["checked"] = true;
                    _clazzList.push(_clazzs[i]);
                }
            }
            self.selectedClazz(ko.mapping.fromJS(_clazzList)());
            self.selectedLevel(level);
        };

        /*班级选择*/
        self.clazzClick = function (index) {
            var checked = self.selectedClazz()[index].checked();
            self.selectedClazz()[index].checked(!checked);
        };

        /*更换班级*/
        self.clazzSubmitBtn = function () {
            self.showLevel(self.selectedLevel());

            self.showClazzList(ko.mapping.toJS(self.selectedClazz()));

            self.getCheckedClazzIds();
            if (self.getCheckedClazzIds().length > 0) {
                //获取教材
                self.getDefaultBooksDetail(self.getCheckedClazzIds());
                self.closeChangeEventBox();
            }
        };

        /*选择单元*/
        self.unitClick = function () {
            var that = this;
            var unitId = 0;
            //根据units中modules有无数据，选择使用units or modules
            ko.utils.arrayForEach(self.unitsDetail(), function (modules) {
                if (modules.moduleName) {
                    ko.utils.arrayForEach(modules.units(), function (units) {
                        unitId = -1;
                        units.isDefault(false);
                    })
                }
            });
            if (unitId == 0) {
                for (var i = 0, unit = self.unitsDetail(); i < unit.length; i++) {
                    self.unitsDetail()[i].isDefault(false);
                }
            }
            that.isDefault(true);

            self.sendLog({
                op:"float_unit_name_click"
            });
        };

        /*获取已选班级的ids*/
        self.getCheckedClazzIds = function () {
            var clazzIds = [];
            var _clazzList = self.selectedClazz();
            for (var i = 0; i < _clazzList.length; i++) {
                if (_clazzList[i].checked()) {
                    clazzIds.push(self.selectedClazz()[i].clazzId());
                }
            }
            return clazzIds;
        };

        /*全选or取消取消*/
        self.chooseOrCancelAll = function () {
            var _isAllChecked = self.isAllChecked();
            var _clazzList = self.selectedClazz();
            for (var i = 0; i < _clazzList.length; i++) {
                self.selectedClazz()[i].checked(!_isAllChecked);
            }
        };

        /*更换单元*/
        self.unitSubmitBtn = function () {
            self.showUnitDetail(self.unitsDetail());
            self.getSelectedUnitId();
            if (self.getSelectedUnitId() != 0) {
                if (self.subject() != 'ENGLISH') {
                    self.getLessonsDetail();
                }
                self.closeChangeEventBox();
            }

            self.sendLog({
                op: "float_unit_confirm_click"
            })
        };

        /*获取默认教材*/
        self.getDefaultBooksDetail = function (clazzIds) {
            $17.weuiLoadingShow();
            $.post("/teacher/clazz/defaultbook.vpage", {
                clazzIds: clazzIds.join(","),
                subject: self.subject()
            }, function (data) {
                if (data.success) {
                    self.defaultBookDetail(data.book);
                    self.getUnitsDetail(clazzIds.join(","), data.book.id);

                } else {
                    $17.weuiLoadingHide();
                    $17.msgTip("课本加载失败");
                    self.defaultBookDetail([]);
                    self.showUnitDetail([]);
                    self.lessonsDetail([]);
                }
            }).fail(function () {
                $17.weuiLoadingHide();
                logger.log({
                    app: "teacher",
                    module: 'WECHAT_Newhomework_assign_home_' + self.subject(),
                    op: 'defaultBookLoadError'
                });
            });
        };

        /*单元*/
        self.getUnitsDetail = function (clazzIds, bookId) {

            self.hasUnitModules(false);
            self.unitsDetail([]);
            self.showUnitDetail([]);

            $17.weuiLoadingShow();
            $.post("/teacher/clazz/book/units.vpage", {
                clazzIds: clazzIds,
                bookId: bookId,
                subject: self.subject()
            }, function (data) {
                if (data.success) {
                    if (data.modules.length > 0) {
                        self.hasUnitModules(true);


                        self.unitsDetail(ko.mapping.fromJS(data.modules)());
                        self.showUnitDetail(ko.mapping.fromJS(data.modules)());

                    } else {
                        self.unitsDetail(ko.mapping.fromJS(data.units)());
                        self.showUnitDetail(ko.mapping.fromJS(data.units)());
                    }

                    //英语到unit
                    if (self.subject() != 'ENGLISH') {
                        self.getLessonsDetail();
                    }

                } else {
                    $17.msgTip("单元加载失败");
                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
                logger.log({
                    app: "teacher",
                    module: 'WECHAT_Newhomework_assign_home_' + LoggerProxy.subject,
                    op: 'unitsLoadError'
                });
            });

        };

        /*课*/
        self.getLessonsDetail = function () {
            $17.weuiLoadingShow();
            var unitId = self.getSelectedUnitId();
            $.post("/teacher/clazz/book/unit/lessons.vpage", {
                unitId: unitId,
                subject: self.subject()
            }, function (data) {
                if (data.success) {
                    if (data.lessons.length == 0) {
                        $17.msgTip("暂无课时");
                    }
                    for (var i = 0, _lesson = data.lessons; i < _lesson.length; i++) {
                        for (var j = 0, _sections = _lesson[i].children; j < _sections.length; j++) {
                            _lesson[i].children[j].checked = _lesson[i].children[j].isDefault || false;
                        }
                    }
                    self.lessonsDetail(ko.mapping.fromJS(data.lessons)());

                } else {
                    $17.msgTip("课时加载失败");
                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
                logger.log({
                    app: "teacher",
                    module: 'WECHAT_Newhomework_assign_home_' + self.subject(),
                    op: 'lessonsLoadError'
                });
            });
        };

        /*section*/
        self.sectionClick = function () {
            var that = this;
            that.checked(!that.checked());
        };

        /*去布置作业*/
        self.submitBtn = function () {
            var clazzId = self.showClazzIds();
            var bookId = self.defaultBookDetail().id;
            var unitId = self.getSelectedUnitId();
            var sectionIds = self.getSelectedSectionIds();
            $17.weuiLoadingShow();
            var param = {
                bookId: bookId,
                unitId: unitId,
                sections: sectionIds.join(','),
                clazzIds: clazzId.join(','),
                subject: self.subject()
            };

            setTimeout(function () {
                location.href = "/teacher/homework/package.vpage?" + $.param(param);
            }, 200);

            self.sendLog({
                op: "page_start_assign_assignHomework_click"
            });
        };

        /*初始化*/
        self.getClazzDetail();

        self.sendLog({
            op: 'page_start_assign_load'
        });
    };


    ko.applyBindings(new HomeworkModel());
});