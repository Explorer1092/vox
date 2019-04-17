/**
 * @author xinqiang.wang
 * @description "创建/退出班级"
 * @createDate 2016/8/8
 */

define(["$17", "knockout", "komapping", "weuijs"], function ($17, ko) {
    var ClazzModel = function () {
        var self = this;
        self.isP6 = isP6Clazz; //是否为六年制
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
        self.subject = ko.observable(LoggerProxy.subject);
        self.subjectWeight = {"CHINESE": 0, "MATH": 1, "ENGLISH": 2};
        self.sortSubject = function (a, b) {
            return self.subjectWeight[a.name] - self.subjectWeight[b.name];
        };
        self.subjectList = LoggerProxy.subjectList.sort(self.sortSubject);
        // subjectList按照语数外进行排序
        for (var i = 0; i < self.subjectList.length; i++) {
            self.subjectList[i]['checked'] = self.subjectList[i]['name']==self.subject();
        }
        self.subjectList = ko.mapping.fromJS(self.subjectList);

        var selectedClazzValue = [];
        self.clazz = ko.mapping.fromJS(
            [
                {name: '一年级', value: 1, selectedCount: 0, checked: true, show: true, clazzs: []},
                {name: '二年级', value: 2, selectedCount: 0, checked: false, show: true, clazzs: []},
                {name: '三年级', value: 3, selectedCount: 0, checked: false, show: true, clazzs: []},
                {name: '四年级', value: 4, selectedCount: 0, checked: false, show: true, clazzs: []},
                {name: '五年级', value: 5, selectedCount: 0, checked: false, show: true, clazzs: []},
                {name: '六年级', value: 6, selectedCount: 0, checked: false, show: self.isP6, clazzs: []}
            ]
        );

        self.currentClazzLevel = ko.observable(0);
        /*self.clazzCount = ko.observable(actualTeachClazzCount);*/
        self.chazzsMaxCount = ko.observable(12); //任教班级数量最大值
        self.removeClazzOnly = ko.observable(true);
        self.newClazzs = ko.observableArray([]); //已有老师和学生，点击申请加入
        self.adjustClazzs = ko.observableArray([]);

        self.maxClazzCount = ko.observable(self.subject() == 'ENGLISH' ? 8 : 4); //英语老师班级上限8个不变，数学和语文老师上限都改为4个班

        /*选择学科*/
        self.selectSubjectBtn = function () {
            var that = this;
            ko.utils.arrayForEach(self.subjectList(), function (subject) {
                subject.checked(false);
            });
            that.checked(true);
            self.subject(that.name());
            self.maxClazzCount(self.subject() == 'ENGLISH' ? 8 : 4);
            self._chooseClazz();
            //init
            /*self.clazzCount(0);*/
            selectedClazzValue = [];
            ko.utils.arrayForEach(self.clazz(), function (clazz) {
                clazz.selectedCount(0);
                clazz.clazzs([]);
            });
            ko.utils.arrayForEach(self.clazz(), function (clazz) {
                if (clazz.checked()) {
                    self.checkClazzBtn(clazz);
                }
            });
        };

        /*任教班级数量*/
        /*self.minusClazzClick = function () {
            if (self.clazzCount() > 0) {
                self.clazzCount(self.clazzCount() - 1);
            }
        };

        self.plusClazzClick = function () {
            if (self.clazzCount() != self.chazzsMaxCount()) {
                self.clazzCount(self.clazzCount() + 1);
            }
        };*/

        /*已选班级数*/
        self.setSelectedClazzsCount = function () {
            var sc = 0;
            ko.utils.arrayForEach(self.clazz(), function (clazz) {
                if (self.currentClazzLevel() == clazz.value()) {
                    ko.utils.arrayForEach(clazz.clazzs(), function (_clazzs) {
                        if (_clazzs.checked()) {
                            sc += 1;
                            clazz.selectedCount(sc);
                        }
                    });
                }
            });
            return sc;
        };

        /*获取已选择班级的总数*/
        self.getAllSelectedClazzCount = function () {
            var sc = 0;
            ko.utils.arrayForEach(self.clazz(), function (clazz) {
                ko.utils.arrayForEach(clazz.clazzs(), function (_clazzs) {
                    if (_clazzs.checked()) {
                        sc += 1;
                    }
                });
            });
            return sc;
        };

        self.getCountByClazzValue = function (value) {
            var count = 0;
            ko.utils.arrayForEach(self.clazz(), function (_clazz) {
                if (_clazz.value() == value) {
                    count = _clazz.selectedCount();
                }
            });
            return count;
        };

        self._chooseClazz = function () {
            $.showLoading();
            $.post('chooseclazz.vpage', {subject: self.subject()}, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(self.clazz(), function (clazz, index) {
                        var clazzs = data.levelClazzs;
                        for (var i = 0; i < clazzs.length; i++) {
                            for (var j = 0; j < clazzs[i].length; j++) {
                                clazzs[i][j].isOld = clazzs[i][j].checked;
                            }
                            if (i == index) {
                                clazz.clazzs(ko.mapping.fromJS(clazzs[i])());
                            }

                        }
                    });
                    // set selectedCount

                    ko.utils.arrayForEach(self.clazz(), function (clazz) {
                        var sc = 0;
                        ko.utils.arrayForEach(clazz.clazzs(), function (_clazzs) {
                            if (_clazzs.checked()) {
                                sc += 1;
                                clazz.selectedCount(sc);
                            }
                        });
                    });
                }
            });
        };
        self._chooseClazz();

        /*选择年级*/
        self.checkClazzBtn = function (that) {
            ko.utils.arrayForEach(self.clazz(), function (clazz) {
                clazz.checked(false);
            });
            that.checked(true);
            that.selectedCount(0);
            var cl = that.value();
            self.currentClazzLevel(cl);
            self.setSelectedClazzsCount();
        };

        /*选择班级*/
        self.selectClazzsBtn = function (parent) {
            var that = this;
            var isOld = that.isOld();
            if (isOld) {
                $.modal({
                    title: "提示",
                    text: "不再任教此班级？",
                    buttons: [
                        {
                            text: "不教了",
                            className: "default",
                            onClick: function () {
                                that.isOld(false);
                                that.checked(false);

                            }
                        },
                        {
                            text: "继续教",
                            onClick: function () {
                                that.checked(true);
                            }
                        }
                    ]
                });
            } else {
                self.removeClazzOnly(false);
            }

            that.checked(!that.checked());
            var count = self.getAllSelectedClazzCount();

            if (count > self.maxClazzCount() && that.checked()) {
                that.checked(false);
                self.setSelectedClazzsCount();
                $.toast("最多允许" + self.maxClazzCount() + "个班级", 'text');
                return false;
            }
            parent.selectedCount(0);
            self.setSelectedClazzsCount();
        };

        /*默认选中一年级*/
        ko.utils.arrayForEach(self.clazz(), function (clazz) {
            if (clazz.checked()) {
                self.checkClazzBtn(clazz);
            }
        });

        self.joinClazz = function (datajson) {
            /*datajson.actualTeachClazzCount = self.clazzCount();*/
            $.showLoading();
            $.post('adjustclazzs2.vpage', {datajson: JSON.stringify(datajson), sourceType: 'wechat','subject':self.subject()}, function (data) {
                $.hideLoading();
                if (data.success) {
                    $.toast("操作成功", function () {
                        $.showLoading();
                        if (!self.removeClazzOnly()) {
                            if (datajson.newClazzs && datajson.newClazzs.length > 0) {
                                location.href = LoggerProxy.wechatJavaToPythonUrl + '/teacher/regist/success.vpage?showassign=true';
                            } else {
                                location.href = LoggerProxy.wechatJavaToPythonUrl + '/teacher/regist/success.vpage?showassign=false';
                            }

                        } else {
                            location.href = '/teacher/clazzmanage/list.vpage';
                        }
                    });
                } else {
                    $.alert(data.info);
                }
            });

        };

        /*保存*/
        self.saveBtn = function () {
            var classIds = [];
            ko.utils.arrayForEach(self.clazz(), function (_clazz) {
                var clazzMap = {};
                clazzMap.clazzLevel = _clazz.value();
                clazzMap.clazzs = [];
                ko.utils.arrayForEach(_clazz.clazzs(), function (_clazzs) {
                    if (_clazzs.checked()) {
                        clazzMap.clazzs.push({id: _clazzs.id(), name: _clazzs.name()});
                    }
                });
                if (clazzMap.clazzs.length != 0) {
                    classIds.push(clazzMap);
                }

            });
            $.showLoading();
            $.ajax({
                type: "post",
                url: "findclazzinfo.vpage",
                data: JSON.stringify({clazzIds: classIds}),
                success: function (data) {
                    $.hideLoading();
                    if (data.success) {
                        if (data.newClazzs.length == 0) {
                            var dataJson = {
                                newClazzs: data.newClazzs,
                                adjustClazzs: data.adjustClazzs
                            };
                            self.joinClazz(dataJson);
                        } else {
                            for (var i = 0; i < data.newClazzs.length; i++) {
                                data.newClazzs[i].show = true;
                                for (var j = 0; j < data.newClazzs[i].groups.length; j++) {
                                    data.newClazzs[i].groups[j].checked = !j;
                                }

                            }
                            self.newClazzs(ko.mapping.fromJS(data.newClazzs)());
                            self.adjustClazzs(ko.mapping.fromJS(data.adjustClazzs)());

                            $.modal({
                                title: "",
                                id: "adjusttplBox",
                                text: $("#adjusttpl").html(),
                                buttons: [
                                    {
                                        text: ""

                                    },
                                    {
                                        text: ""
                                    }
                                ]
                            }, function () {
                                ko.applyBindings(self, document.getElementById('adjusttplBox'));
                            });
                        }

                    } else {
                        $.toast(data.info, "text");
                    }
                },
                contentType: 'application/json;charset=UTF-8'
            });
        };


        self.getShowClazzCount = function () {
            var count = 0;
            ko.utils.arrayForEach(self.newClazzs(), function (_newClazzs, i) {
                if (_newClazzs.show()) {
                    count += 1;
                }
            });
            return count;
        };

        /*加入班级*/
        self.joinClazzBtn = function () {
            var that = this;
            var cid = that.clazzId(), index;
            ko.utils.arrayForEach(that.groups(), function (_groups, i) {
                if (_groups.checked()) {
                    index = i;
                }

            });

            ko.utils.arrayForEach(self.newClazzs(), function (_newClazzs) {
                if (_newClazzs.clazzId() == cid) {
                    _newClazzs.show(false);
                    ko.utils.arrayForEach(_newClazzs.groups(), function (_groups, k) {
                        delete _groups.students;
                        if (index != k) {
                            _groups.teachers([]);
                        }
                    });
                }
            });
            if (self.getShowClazzCount() == 0) {
                $.closeModal();
                var dataJson = {
                    newClazzs: ko.mapping.toJS(self.newClazzs()),
                    adjustClazzs: ko.mapping.toJS(self.adjustClazzs())
                };
                self.joinClazz(dataJson);
            }
        };

        /*不加入班级*/
        self.unJoinClazzBtn = function () {
            var that = this;
            var cid = that.clazzId();
            ko.utils.arrayForEach(self.newClazzs(), function (_newClazzs, i) {
                if (_newClazzs.clazzId() == cid) {
                    _newClazzs.show(false);
                    self.adjustClazzs.push(_newClazzs.clazzId);
                    self.newClazzs.splice(i, 1);
                }
            });
            if (self.getShowClazzCount() == 0) {
                $.closeModal();
                var dataJson = {
                    newClazzs: ko.mapping.toJS(self.newClazzs()),
                    adjustClazzs: ko.mapping.toJS(self.adjustClazzs())
                };
                self.joinClazz(dataJson);
            }

        };

        self.selectClazzBtn = function (parent) {
            var that = this;
            ko.utils.arrayForEach(parent.groups(), function (_groups) {
                _groups.checked(false);
            });
            that.checked(true);
        };

    };

    ko.applyBindings(new ClazzModel(), document.getElementById('subjectListBox'));
});