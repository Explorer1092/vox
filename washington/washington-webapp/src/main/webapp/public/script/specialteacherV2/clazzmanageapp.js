/**
 * Created by fengwei on 2017/6/26.
 */
define(["jquery","knockout","YQ","knockout-switch-case","impromptu"],function($,ko,YQ){
    var GradeManageModal = function () {
        var self = this;
        $.extend(self, {
            canMergeList: ko.observableArray([]),
            mergeSelectList: ko.observableArray([]),

            gradeMenu: ko.observable({}),
            gradeClassInfo: ko.observable({}),

            adjustGrades:ko.observable([]),
            adjustGrade:ko.observable(),

            //年级，班级，班组分别展示标志
            isGradeCard: ko.observable(1),
            isClassCard: ko.observable(0),
            isClassGroupDetail: ko.observable(0),
            isAdjustClassPage: ko.observable(0),
            text:ko.observable(""),
            isShowMerge: ko.observable(0),

            //年级,班级，班组模板数据
            gradeCardListData: ko.observable({
                administrativeClass: [],
                gradeId:"",
                teachingClass: [],
                realAdministrativeClassLength: 0,
                realTeachingClassLength: 0
            }),
            clazzCardListData: ko.observable({
                groups: [],
                gradeId:"",
                realGroupLength: 0
            }),
            isHasSameClassMultiGroup: ko.observable(false),
            isShowBatchGenerateSubjectBtn: ko.observable(false),
            classGroupDetailData: ko.observable({

                hasDetail: false
            }),
            adjustClassData: ko.observable({
                aClassList:ko.observable([]),
                tClassList:ko.observable([]),
                defaultTemp:ko.observable(1)
            }),

            searchStudentKey:ko.observable(),
            searchStuKeyEvent:function (data, event) {
                if(event.keyCode == 13){
                    self.searchStudent();
                    // self.searchStudent().studentSearchDialogModal.getSearch(self.searchStudentKey());
                }
            },
            addOrAdjustFlag:ko.observable(),
            currentGroupId:ko.observable(),
            currentGrade:ko.observable(),
            currentClassId:ko.observable(),
            isTeachingClazz:ko.observable(0), //默认行政班

            cardDetailData: ko.observable(''), // 点击显示卡片详情的数据

            //获取接口数据
            getAllClazzInfo: function (callback) {
                $.get('/specialteacher/manageclazz.vpage', {userId: 81198}, function (res) {
                    if(res.success){
                        self.gradeClassInfo(res);
                        self.gradeMenu({
                            hasData: true,
                            menu: res.menu,
                            gradeInfo: ko.observable(''),
                            clazzInfo: ko.observable(''),
                            adjustClazz:res.adjustClazz
                        });
                        if(callback && typeof(callback) === "function"){
                            callback()
                        }
                    }else{
                        self.alertDialog({
                            text:res.info?res.info:"好像出错咯，请稍后再试"
                        });
                    }
                });
            },

            // 获取配置
            getEaxmGroupManager: function () {
                $.get('/specialteacher/eaxmGroupManager.vpage', function (res) {
                    if (res.success) {
                        if (res.permission) self.isShowBatchGenerateSubjectBtn(true);
                    }
                });
            },

            // 班级管理选择年级(默认选择年级)
            changeGrade: function () {
                // self.gradeMenu中存放的是整个的数据结构
                // 增加gradeInfo字段表示当前年级数据，切换年级时更改他，班级下拉框通过这个数据进行联动显示
                if (!self.gradeMenu().gradeInfo()) return false;
                // 显示年级数据
                self.toggleGradeMenu(self.gradeMenu().gradeInfo());
            },

            // 班级管理选择班级（默认不选择）
            changeClass: function () {
                // 增加clazzInfo字段表示当前班级数据
                if (!self.gradeMenu().clazzInfo()) { // 选择全部班级，显示年级数据
                    self.toggleGradeMenu(self.gradeMenu().gradeInfo());
                } else { // 显示班级数据
                    self.toggleClassMenu(self.gradeMenu().clazzInfo());
                }
            },

            //点击年级菜单
            toggleGradeMenu: function (data, event) {
                self.isShowMerge(0); // 不显示合并按钮
                self.mergeSelectList([]); // 清空合并list
                self.isTeachingClazz(0);

                self.displayOneTemp(1);
                self.currentGrade(data.gradeId);
                // var _index = index(); //click传值很奇怪 TODO
                // 比对menu.clazzs和administrativeClass、teachingClass，增加空class卡片
                var requestClazzs = data.clazzs;
                var requestAdministrativeClass = data.administrativeClass;
                var requestTeachingClass = data.teachingClass;
                var mergedClazzs = requestAdministrativeClass.concat(requestTeachingClass);
                for (var i = 0, clazzsLength = requestClazzs.length; i < clazzsLength; i++) {
                    var classHasGroup = false; // 当前班级是否在administrativeClass或teachingClass中，true表示当前班级group非空
                    for (var m = 0, mergedClazzsLength = mergedClazzs.length; m < mergedClazzsLength; m++) {
                        if (requestClazzs[i].clazzId === mergedClazzs[m].clazzId) {
                            classHasGroup = true;
                        }
                    }
                    if (!classHasGroup) {
                        if (requestClazzs[i].clazzType === "WALKING") { // 教学班
                            requestTeachingClass.push({
                                clazzId: requestClazzs[i].clazzId,
                                groupName: requestClazzs[i].clazzName, // 此处本应为clazzName，但由于是动态插入的卡片，实际上并非是group，便于循环
                                groupIds: [],
                                groupType: "TEACHING_GROUP",
                                studentNum: -1, // -1表示假数据，前台不显示，不用0的原因是有些时候0需要展示
                                subjectAndTeacher: [],
                                stageType: ''
                            });
                        } else { // 行政班
                            requestAdministrativeClass.push({
                                clazzId: requestClazzs[i].clazzId,
                                groupName: requestClazzs[i].clazzName, // 此处本应为clazzName，但由于是动态插入的卡片，实际上并非是group，便于循环
                                groupIds: [],
                                groupType: "SYSTEM_GROUP",
                                studentNum: -1,
                                subjectAndTeacher: [],
                                artScienceType: 'UNKNOWN'
                            });
                        }
                    }
                }
                self.gradeCardListData({
                    administrativeClass: requestAdministrativeClass,
                    gradeId:data.gradeId,
                    teachingClass: requestTeachingClass,
                    realAdministrativeClassLength: data.administrativeClass.length,
                    realTeachingClassLength: data.teachingClass.length
                });
                self.isHasSameClassMultiGroup(self.findIsHasSameClassMultiGroup(self.gradeCardListData().administrativeClass)); // 选择年级时行政班是否存在一个班级拥有多个班群的情况
                // $(".adjust").removeClass("active");
                //子菜单收起
                // var $thisNode = $(event.currentTarget);
                // $thisNode.next('.js-subMenuItem').find('.js-classMenuItem').removeClass('active');
                // if ($thisNode.hasClass('active')) {
                //     $thisNode.removeClass('active').siblings('a.js-classMenuItem').removeClass('active');
                //     $($('.js-subMenuItem')[_index]).addClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                // } else {
                //     $thisNode.addClass('active').siblings('a.js-classMenuItem').removeClass('active');
                //     $($('.js-subMenuItem')[_index]).removeClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                // }
            },

            // 点击班级菜单
            toggleClassMenu: function (data,event) {
                self.isShowMerge(0);
                self.mergeSelectList([]);

                // var $thisNode = $(event.currentTarget),
                //     id = $thisNode.parent(".js-subMenuItem").attr("gradeId");
                self.displayOneTemp(2);
                self.currentClassId(data.clazzId);

                // 制造一个假数据，用来展示空clazz卡片
                var emptyGroup = [{
                    clazzId: data.clazzId,
                    groupName: data.clazzName, // 此处本应为clazzName，但由于是动态插入的卡片，实际上并非是group，便于循环
                    groupIds: [],
                    groupType: self.gradeMenu().clazzInfo().clazzType === "WALKING" ? "TEACHING_GROUP" : "SYSTEM_GROUP",
                    studentNum: -1,
                    subjectAndTeacher: []
                }];
                self.clazzCardListData({
                    groups: data.groups.length !== 0 ? data.groups : emptyGroup,
                    gradeId: self.gradeMenu().gradeInfo().gradeId,
                    realGroupLength: data.groups.length !== 0 ? data.groups.length : 0
                });
                if(data.groups && data.groups.length !=0){
                    if(data.groups[0].groupType != "TEACHING_GROUP"){
                        self.isTeachingClazz(0);
                    }else{
                        self.isTeachingClazz(1);
                    }
                }
                // $thisNode.addClass('active').siblings('.js-classMenuItem').removeClass('active');
                // $(".adjust").removeClass("active");
            },

            // 找出数据中是否存在一个班级拥有多个班群的情况
            findIsHasSameClassMultiGroup: function (data) {
                var isHas = false;
                if (data.length >= 2) {
                    for (var i = 0; i < data.length; i++) {
                        for (var j = 0; j < data.length; j++) {
                            if (data[i].clazzId === data[j].clazzId && i !== j) {
                                isHas = true;
                                break;
                            }
                        }
                    }
                } else {
                    isHas = false;
                }

                return isHas;
            },

            searchStudent:function () {
                var StudentSearchDialogModal = function () {
                    var _this = this;
                    _this.getSearch = function (key) {
                        var searchUrl = '/specialteacher/searchstudent.vpage';
                        if(key){
                            $.ajax({
                                url:searchUrl,
                                type:'GET',
                                data:{key:key},
                                success:function (res) {
                                    var _searchStudentDialogData = _this.searchStudentDialogData();
                                    _searchStudentDialogData.resultList = res.studentList || [];
                                    _this.searchStudentDialogData(_searchStudentDialogData);
                                },
                                error:function (e) {
                                    console.log(e);
                                }
                            })
                        }else{
                            self.alertDialog({
                                text:"请输入学生姓名／学号／填涂号搜索对应学生"
                            })
                        }
                    };
                    _this.getSearch(self.searchStudentKey());
                    _this.searchStudentDialogData = ko.observable({
                        resultList:ko.observable(),
                        showStudentDetail:function (data) {
                            self.jumpToClassGroupDetailTemp(data);
                            $.prompt.close();
                        }
                    });
                };

                var ssm = new StudentSearchDialogModal();

                var dialogTempHtml = "<div id=\"searchStudentContent\" data-bind=\"template: { name: 'searchStudentDialog', data: searchStudentDialogData }\"></div>";

                if(self.searchStudentKey()){
                    $.prompt(dialogTempHtml,{
                        focus : 1,
                        title: "学生搜索结果",
                        buttons: {},
                        position: {width: 500},
                        loaded:function () {
                            ko.applyBindings(ssm,document.getElementById("searchStudentContent"));
                        },
                        submit : function(e, v){
                            if(v){
                                e.preventDefault();
                            }
                        }
                    });
                }
            },

            getSubjValueByKey : function (key) {
                var _subjects = self.gradeClassInfo().subjects,
                    _value = "";
                for(var i = 0; i<_subjects.length;i++){
                    if(_subjects[i].key == key){
                        _value = _subjects[i].value;
                        break;
                    }
                }
                return _value;
            },

            //班组卡片的编辑班级名称
            editCardClazzName: function (gradeId, data) {
                var editCardClazzNameDialogModal = function () {
                    var _this = this;
                    _this.editCardClazzNameDialogData = ko.observable({
                        newClazzName: ko.observable(''),
                        editCardClazzNameErrorText: ko.observable(''),
                        clazzType: ko.observable(''),
                        artScienceTypeArr: [
                            {
                                artScienceKey: 'ARTSCIENCE',
                                artScienceValue: '不分文理科'
                            }, {
                                artScienceKey: 'ART',
                                artScienceValue: '文科'
                            }, {
                                artScienceKey: 'SCIENCE',
                                artScienceValue: '理科'
                            }
                        ],
                        isShowArtScienceSelect: ko.observable(true),
                        isShowStageSelect: ko.observable(true),
                        stageTypeArr: ['不分层', 'A层', 'B层', 'C层', 'D层', 'E层', 'F层'],
                        artScienceType: ko.observable(), // 选中的文理科值 obj
                        stageType: ko.observable(), // 选中的分层值
                        classNameInputFocus: function () {
                            _this.editCardClazzNameDialogData().editCardClazzNameErrorText('');
                        },
                        changeArtScienceType: function () {
                            _this.editCardClazzNameDialogData().editCardClazzNameErrorText('');
                        },
                        changeStageType: function () {
                            _this.editCardClazzNameDialogData().editCardClazzNameErrorText('');
                        }
                    });
                };
                var editCardClazzNameDialogMode = new editCardClazzNameDialogModal();
                var editCardClazzNameDialogHtml = "<div id=\"editCardClazzNameDialogContent\" data-bind=\"template: { name: 'editCardClazzNameDialog', data: editCardClazzNameDialogData}\"></div>";
                var that = editCardClazzNameDialogMode.editCardClazzNameDialogData();
                var initArtScienceType = null;
                var initStageType = null;
                $.prompt(editCardClazzNameDialogHtml, {
                    title  : '修改班级',
                    focus  : 0,
                    buttons: {'取消': false, '确定': true},
                    loaded:function () {
                        that.newClazzName(self.findClazzName(gradeId, data.clazzId, self.gradeClassInfo().adjustClazz));
                        that.clazzType(data.groupType);

                        // 初始化文理科信息
                        that.isShowArtScienceSelect(self.gradeClassInfo().schoolLevel == 'HIGH' && data.groupType == 'SYSTEM_GROUP' && data.groupIds.length != 0);
                        that.isShowStageSelect(data.groupType == 'TEACHING_GROUP' && data.groupIds.length != 0);
                        if (data.artScienceType == null || data.artScienceType == '' || data.artScienceType == 'UNKNOWN' || data.artScienceType == 'ARTSCIENCE') {
                            that.artScienceType(that.artScienceTypeArr[0]);
                            initArtScienceType = that.artScienceTypeArr[0];
                        } else if (data.artScienceType == 'ART'){
                            that.artScienceType(that.artScienceTypeArr[1]);
                            initArtScienceType = that.artScienceTypeArr[1];
                        } else if (data.artScienceType == 'SCIENCE'){
                            that.artScienceType(that.artScienceTypeArr[2]);
                            initArtScienceType = that.artScienceTypeArr[2];
                        }                        // 初始化分层信息
                        that.stageType(data.stageType);
                        initStageType = data.stageType;

                        ko.applyBindings(editCardClazzNameDialogMode, document.getElementById("editCardClazzNameDialogContent"));
                    },
                    submit : function (e, v) {
                        if (v) {
                            e.preventDefault();
                            // 班级名称 、文理科 、 分层 都未更新
                            if (that.newClazzName() === self.findClazzName(gradeId, data.clazzId, self.gradeClassInfo().adjustClazz)) {
                                if (that.artScienceType() === initArtScienceType && that.stageType() === initStageType) {
                                    that.editCardClazzNameErrorText('您还未进行任何操作哦~');
                                    return false;
                                }
                            }
                            // 班级名称修改后存在
                            if (self.findClazzNameHas(gradeId, that.newClazzName(), self.gradeClassInfo().adjustClazz)
                                && that.newClazzName() !== self.findClazzName(gradeId, data.clazzId, self.gradeClassInfo().adjustClazz)) {
                                that.editCardClazzNameErrorText('该班级名称已存在，请填写新的班级名称');
                                return false;
                            }
                            var submitArtScienceType = that.artScienceType().artScienceKey;
                            var submitStageType = that.stageType;
                            $.ajax({
                                url: '/specialteacher/clazz/adjustclazz.vpage',
                                type: 'POST',
                                data: {
                                    clazzId: data.clazzId,
                                    type: 1,
                                    clazzName: that.newClazzName(),
                                    artScienceType: (data.groupType === 'SYSTEM_GROUP' && data.groupIds.length != 0) ? submitArtScienceType : '',
                                    stageType: (data.groupType === 'TEACHING_GROUP' && data.groupIds.length != 0) ? submitStageType : '',
                                    groupId: (data.groupType === 'TEACHING_GROUP' && data.groupIds.length != 0) ? data.groupIds[0] : ''
                                },
                                success: function (res) {
                                    if (res.success) {
                                        var nowGradeInfo = self.gradeMenu().gradeInfo();
                                        var nowClazzInfo = self.gradeMenu().clazzInfo();
                                        self.adjustCardCallback(nowGradeInfo, nowClazzInfo, '您已成功修改该班级', true);
                                    } else {
                                        that.editCardClazzNameErrorText(res.info || '好像错误喽，请稍后重试！');
                                    }
                                }
                            });
                        }
                    }
                })
            },

            // 根据gradeId找出年级数据
            filterCurrentGradeInfo: function (gradeId, data) {
                var currentGradeInfo = "";
                for (var i = 0, len = data.length; i < len; i++) {
                    if (data[i].gradeId === gradeId) {
                        currentGradeInfo = data[i];
                        break;
                    }
                }
                return currentGradeInfo;
            },

            // 根据clazzId找出班级数据
            filterCurrentClazzInfo: function (clazzId, data) {
                var currentClazzInfo = "";
                for (var i = 0, len = data.length; i < len; i++) {
                    if (data[i].clazzId === clazzId) {
                        currentClazzInfo = data[i];
                        break;
                    }
                }
                return currentClazzInfo;
            },

            // 使用gradeId和clazzId查找clazzName（卡片编辑班级名称使用）
            findClazzName: function (gradeId, clazzId, data) {
                var className = '';
                // 先查aClassList，无则查tClassList(处理原因：后台操作错误产生的脏数据造成影响，正常情况遍历aClassList就可以)
                for (var i = 0, gradeLength = data.length; i < gradeLength; i++) {
                    if (gradeId === data[i].gradeId) {
                        for (var j = 0, clazzLength = data[i].aClassList.length; j < clazzLength; j++) {
                            if (clazzId === data[i].aClassList[j].clazzId) {
                                className = data[i].aClassList[j].clazzName;
                                break;
                            }
                        }
                        break;
                    }
                }

                if (className) {
                    return className;
                } else {
                    for (var i = 0, gradeLength = data.length; i < gradeLength; i++) {
                        if (gradeId === data[i].gradeId) {
                            for (var j = 0, clazzLength = data[i].tClassList.length; j < clazzLength; j++) {
                                if (clazzId === data[i].tClassList[j].clazzId) {
                                    className = data[i].tClassList[j].clazzName;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    return className;
                }
            },

            // 查找新班级名在当前年级下是否存在（修改班级名使用）
            findClazzNameHas: function (gradeId, clazzName, data) {
                var isHasSameClassName = false;
                for (var i = 0, gradeLength = data.length; i < gradeLength; i++) {
                    if (gradeId === data[i].gradeId) {
                        var classList = data[i].aClassList.concat(data[i].tClassList);
                        for (var j = 0, clazzLength = classList.length; j < clazzLength; j++) {
                            if (clazzName === classList[j].clazzName) {
                                isHasSameClassName = true;
                                break;
                            }
                        }
                        break;
                    }
                }
                return isHasSameClassName;
            },

            // gradeId找索引
            findNowGradeIndex: function (gradeId, data) {
                var gradeIndex = 0;
                for (var i = 0, len = data.length; i < len; i++) {
                    if (gradeId === data[i].gradeId) {
                        gradeIndex = i;
                        break;
                    }
                }
                return gradeIndex;
            },

            // clazzId找索引(和上方的findNowGradeIndex其实是一样的，便于形象就不使用一个了)
            findNowClazzIndex: function (clazzId, data) {
                var clazzIndex = 0;
                for (var i = 0, len = data.length; i < len; i++) {
                    if (clazzId === data[i].clazzId) {
                        clazzIndex = i;
                        break;
                    }
                }
                return clazzIndex;
            },

            // 卡片上删除班级（空班级）
            deleteCardClazz: function (gradeId,data) {
                var deleteCardClazzDialogModal = function () {
                    var _this = this;
                    _this.deleteCardClazzDialogData = ko.observable({
                        deleteClazzName: ko.observable(''),
                        deleteCardClazzErrorText: ko.observable('')
                    });
                }
                var deleteCardClazzDailogMode = new deleteCardClazzDialogModal();
                var deleteCardClazzDialogHtml = "<div id=\"deleteCardClazzDialogContent\" data-bind=\"template: { name: 'deleteCardClazzDialog', data: deleteCardClazzDialogData}\"></div>"
                $.prompt(deleteCardClazzDialogHtml, {
                    title: '系统提示',
                    focus: 0,
                    buttons: {"取消": false, "确定": true},
                    loaded: function () {
                        deleteCardClazzDailogMode.deleteCardClazzDialogData().deleteClazzName(self.findClazzName(gradeId, data.clazzId, self.gradeClassInfo().adjustClazz));
                        ko.applyBindings(deleteCardClazzDailogMode, document.getElementById('deleteCardClazzDialogContent'));
                    },
                    submit: function (e, v) {
                        if (v) {
                            e.preventDefault();
                            $.ajax({
                                url: '/specialteacher/clazz/adjustclazz.vpage',
                                type: 'POST',
                                data: {
                                    clazzId: data.clazzId,
                                    type: 2,
                                    clazzName: deleteCardClazzDailogMode.deleteCardClazzDialogData().deleteClazzName()
                                },
                                success: function (res) {
                                    if (res.success) {
                                        var nowGradeInfo = self.gradeMenu().gradeInfo();
                                        self.adjustCardCallback(nowGradeInfo, '', '您已成功删除该班级', true);
                                    } else {
                                        deleteCardClazzDailogMode.deleteCardClazzDialogData().deleteCardClazzErrorText(res.info || '好像错误喽，请稍后重试！');
                                    }
                                }
                            })
                        }
                    }
                });

            },

            // 调整班群卡片回调
            adjustCardCallback: function (nowGradeInfo, nowClazzInfo, content, needSelectChange) {
                // var nowGradeInfo = self.gradeMenu().gradeInfo();
                // var nowClazzInfo = self.gradeMenu().clazzInfo();
                self.getAllClazzInfo(function () {
                    self.alertDialog({
                        text: content
                    });
                    self.gobackNowGradeOrClass(nowGradeInfo.gradeId, nowClazzInfo, self.gradeMenu().menu, needSelectChange)
                });
                $.prompt.close();
            },

            // 重新回到当前年级或班级
            gobackNowGradeOrClass: function (gradeId, clazzInfo, menu, needSelectChange) {
                // 选择当前对应的年级（原本应该修改self.gradeMenu().gradeInfo()来驱动select变化，但二次请求后select自动刷回第一个，改用jquery实现）
                // 参数needSelectChange:防止函数在其他使用的地方调用但不需要change
                if (!clazzInfo) { // 当前未选中班级
                    if (needSelectChange) {
                        var nowGradeIndex = self.findNowGradeIndex(gradeId, menu);
                        $('.choice-grade').find('option').eq(nowGradeIndex).attr("selected", true).change();
                    } else {
                        var nowGradeIndex = self.findNowGradeIndex(gradeId, menu);
                        $('.choice-grade').find('option').eq(nowGradeIndex).attr("selected", true);
                    }
                } else {
                    var nowGradeIndex = self.findNowGradeIndex(gradeId, menu);
                    var nowClazzIndex = self.findNowClazzIndex(clazzInfo.clazzId, menu[nowGradeIndex].clazzs);
                    $('.choice-grade').find('option').eq(nowGradeIndex).attr("selected", true);
                    $('.choice-class').find('option').eq(nowClazzIndex + 1).attr("selected", true);
                }
            },

            //班组卡片的查看详情
            showCardDetail: function (gradeId, data, callback) {
                self.gradeMenu().gradeInfo(self.filterCurrentGradeInfo(gradeId, self.gradeMenu().menu)); // 更新gradeInfo信息（搜索后改变年级）
                self.gradeMenu().clazzInfo(self.filterCurrentClazzInfo(data.clazzId, self.gradeMenu().gradeInfo().clazzs)); // 更新clazzInfo
                // self.gobackNowGradeOrClass(gradeId, data, self.gradeMenu().menu);

                self.cardDetailData(data);
                if(data.groupIds && data.groupIds.length !=0){
                    self.currentGroupId(data.groupIds[0]);
                    data.groupId = data.groupIds[0];
                }else{
                    console.error("groupIds length is zero");
                }
                self.currentGrade(gradeId);
                self.currentClassId(data.clazzId);
                self.displayOneTemp(3);
                // data.subjAndTeacherList = self.renderSubject(data.subjectAndTeacher,data.groupType);
                if(data.groupType && data.groupType == "SYSTEM_GROUP"){ //区分教务老师和行政老师
                    data.subjectArray = self.gradeClassInfo().subjects;
                }else{
                    var _subArray = [];
                    for(var i=0;i<data.subjectAndTeacher.length;i++){
                        _subArray.push({
                            key:data.subjectAndTeacher[i].subject,
                            value:self.getSubjValueByKey(data.subjectAndTeacher[i].subject)
                        })
                    }
                    data.subjectArray = _subArray;
                }

                data.subjAndTeacherList = self.renderSubject(data.subjectAndTeacher,data.groupType,data.subjectArray);

                data.scanNumDigit = self.gradeClassInfo().scanNumDigit;

                self.classGroupDetailData($.extend({
                    groupType: data.groupType,
                    groupTypeName: data.groupType === 'SYSTEM_GROUP' ? '行政班' : '教学班',
                    hasDetail: true,
                    isShow: data.groupType,
                    gradeId: gradeId,
                    classGroupStudents: ko.observableArray([]),
                    isShowStuDateAbnormalTip: ko.observable(false),
                    mergeStudentsNames: ko.observableArray([]),
                    //添加学生
                    addStudentAccount:function (res) {
                        var AddStuDialogModal = function () {
                            var _self = this;
                            _self.addStudentDialogData = ko.observable({
                                classGroupName:res.groupName || '',
                                addByOnline:function () {
                                    $.prompt.goToState('state1');
                                },
                                addByExcel:function () {
                                    $.prompt.goToState('state2');
                                },
                                backState:ko.observable('state1'),
                                backToState:function () {
                                    $.prompt.goToState(_self.addStudentDialogData().backState());
                                }
                            })
                        };

                        var OnlineAddStudentModal = function () {
                            var _self = this;
                            _self.onlineAddStudentDialogData = ko.observable({
                                onlineText:ko.observable(''),
                                errorInfo:ko.observable(''),
                                selectedClazzId:data.clazzId,
                                scanNumDigit:self.gradeClassInfo().scanNumDigit,
                                selectedTeacherId:data.subjectAndTeacher[0].teacherId
                            });
                        };

                        var ExcelAddStudentModal = function () {
                            var _self = this;
                            _self.excelAddStudentDialogData = ko.observable({
                                selectedClazzId:data.clazzId,
                                selectedTeacherId:data.subjectAndTeacher[0].teacherId,
                                stuExcelFile:ko.observable(),
                                stuExcelFileName:ko.observable(),
                                stuExcelTypeError:ko.observable(),
                                scanNumDigit:self.gradeClassInfo().scanNumDigit,
                                uploadExcelToStuAccount:function () { //上传Excel
                                    var ie = !-[1,];
                                    if(ie){
                                        $('#stuExcelFile').trigger('click').trigger('change');
                                    }else{
                                        $('#stuExcelFile').trigger('click');
                                    }
                                },
                                stuExcelFileChange:function () {
                                    var files = document.getElementById('stuExcelFile').files;
                                    if(files.length != 0){
                                        var file = files[0];
                                        _self.excelAddStudentDialogData().stuExcelFile(file);
                                        _self.excelAddStudentDialogData().stuExcelFileName(file.name);
                                    }else{
                                        _self.excelAddStudentDialogData().stuExcelFile("");
                                        _self.excelAddStudentDialogData().stuExcelFileName("");
                                    }
                                }
                            });
                        };

                        var AddStudentSuccessModal = function () {
                            var _self = this;
                            _self.addStudentSuccessData = ko.observable({
                                newSignNum:ko.observable(0),
                                recoverNum:ko.observable(0),
                                updateNum:ko.observable(0)
                            });
                        };

                        var AddStudentRepeatedErrorModal = function () {
                            var _self = this;
                            _self.addStudentRepeatedErrorData = ko.observable({
                                repeatedNames:ko.observable(""),
                                errorInfo:ko.observable("")
                            });
                        };

                        var AddStudentTakeUpErrorModal = function () {
                            var _self = this;
                            _self.addStudentTakeUpErrorData = ko.observable({
                                scanNumDigit:self.gradeClassInfo().scanNumDigit,
                                importStudentNames:ko.observable(""),
                                takeUpSutInfo:ko.observable(""),
                                errorInfo:ko.observable("")
                            });
                        };

                        var AddStudentRestoreModal = function () {
                            var _self = this;
                            _self.addStudentRestoreData = ko.observable({
                                restoreStudentInfo: ko.observable(""),
                                errorInfo:ko.observable("")
                            });
                        };

                        var addStuDialogModal = new AddStuDialogModal();
                        var onlineAddStudentModal = new OnlineAddStudentModal();
                        var excelAddStudentModal = new ExcelAddStudentModal();
                        var addStudentSuccessModal = new AddStudentSuccessModal();
                        var addStudentRepeatedErrorModal = new AddStudentRepeatedErrorModal();
                        var addStudentTakeUpErrorModal = new AddStudentTakeUpErrorModal();
                        var addStudentRestoreModal = new AddStudentRestoreModal();

                        var addStuDialogTempHtml = "<div id=\"addStudentDialog\" data-bind=\"template: { name: 'addStudentDialogTemp', data: addStudentDialogData }\"></div>";
                        var onlineAddStuDialogTempHtml = "<div id=\"onlineAddStudentDialog\" data-bind=\"template: { name: 'onlineAddStudentDialogTemp', data: onlineAddStudentDialogData }\"></div>";
                        var excelAddStudentDialogTempHtml = "<div id=\"excelAddStudentDialog\" data-bind=\"template: { name: 'excelAddStudentDialogTemp', data: excelAddStudentDialogData }\"></div>";
                        var addStudentSuccessTempHtml = "<div id=\"addStudentSuccessDialog\" data-bind=\"template: { name: 'addStudentSuccessTemp', data: addStudentSuccessData }\"></div>";
                        var addStudentRepeatedErrorTempHtml = "<div id=\"addStudentRepeatedErrorDialog\" data-bind=\"template: { name: 'addStudentRepeatedErrorTemp', data: addStudentRepeatedErrorData }\"></div>";
                        var addStudentTakeUpErrorTempHtml = "<div id=\"addStudentTakeUpErrorDialog\" data-bind=\"template: { name: 'addStudentTakeUpErrorTemp', data: addStudentTakeUpErrorData }\"></div>";
                        var addStudentRestoreTempHtml = "<div id=\"addStudentRestoreDialog\" data-bind=\"template: { name: 'addStudentRestoreTemp', data: addStudentRestoreData }\"></div>";

                        //验证在线添加输入
                        var validateOnlineAddData = function (str) {
                            if(str != ''){
                                // TODO 验证
                                // var strArray = str.split("\n");
                                // var repeatList = [];
                                // for(var i = 0;i < strArray.length;i++){
                                //     var rowName = strArray[i].split('\t')[0];
                                //     var rowNumber = strArray[i].split('\t')[1];
                                //     if(rowName){
                                //
                                //     }
                                //     if(rowNumber){
                                //
                                //     }
                                // }
                                return true;
                            }else{
                                onlineAddStudentModal.onlineAddStudentDialogData().errorInfo('请输入需要添加的学生姓名和学生号');
                                return false;
                            }
                        };

                        var onlineAddStudentData = {
                            clazzId:onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId,
                            teacherId:onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId
                        };

                        var excelAddStudentData = new FormData();
                        excelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                        excelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);

                        var subAllRepeatedNames = function (rList,moreFlag) {
                            var repeatedNames = rList.join("、");
                            if(moreFlag) repeatedNames+="等";
                            return repeatedNames;
                        };

                        var subAllTakeUpInfo = function (tList) {
                            var tInfo = "";
                            if (tList && tList.length > 0) {
                                for (var i = 0; i < tList.length; i++) {
                                    tInfo += tList[i].teacherName + " " + tList[i].clazzName + " " + tList[i].studentName + "<br/>"
                                }
                            }
                            return tInfo;
                        };

                        //线上添加验证重复
                        var validateOnlineRepeat = function (text) {
                            onlineAddStudentData.batchContext = text;
                            onlineAddStudentData.checkRepeatedStudent = true;
                            onlineAddStudentData.checkTakeUpStudent = false;
                            onlineAddStudentData.checkDeleteStudent = false;
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                if(result.repeatedStudentList && result.repeatedStudentList.length != 0){ // 重复添加的列表
                                    var repeatedNames = subAllRepeatedNames(result.repeatedStudentList,result.moreStudent);
                                    addStudentRepeatedErrorModal.addStudentRepeatedErrorData().repeatedNames(repeatedNames);
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state4'); // 重复弹窗
                                }else{
                                    validateOnlineTakeUp();
                                }
                            });
                        };

                        //线上添加验证占用
                        var validateOnlineTakeUp = function () {
                            onlineAddStudentData.checkRepeatedStudent = false;
                            onlineAddStudentData.checkTakeUpStudent = true;
                            onlineAddStudentData.checkDeleteStudent = false;
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                if(result.isTakeUp){ //被占用
                                    var importStudentNames = subAllRepeatedNames(result.importNames, result.moreFlag);
                                    var takeUpSutInfo = subAllTakeUpInfo(result.takeUpInfo);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().importStudentNames(importStudentNames);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().takeUpSutInfo(takeUpSutInfo);
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state5'); // 占用弹窗
                                }else{
                                    validateOnlineCanResore();
                                }
                            });
                        };

                        //线上添加验证恢复
                        var validateOnlineCanResore = function () {
                            onlineAddStudentData.checkRepeatedStudent = false;
                            onlineAddStudentData.checkTakeUpStudent = false;
                            onlineAddStudentData.checkDeleteStudent = true;
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                if(result.klxDeleteSameName){ // 可恢复
                                    if (result.deleteSameStudents) {
                                        addStudentRestoreModal.addStudentRestoreData().restoreStudentInfo(exchangeDeleteSameStudentsDate(result.deleteSameStudents));
                                    }
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state6'); // 恢复弹窗
                                }else{
                                    addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                    addStudentSuccessModal.addStudentSuccessData().recoverNum(result.recoverNum || 0);
                                    addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state3'); // 成功弹窗
                                }
                            });
                        };

                        //线上添加 不带验证（最终提交）
                        var onlineAddStuWithoutVal = function (restoreOperate) {
                            onlineAddStudentData.checkRepeatedStudent = false;
                            onlineAddStudentData.checkTakeUpStudent = false;
                            onlineAddStudentData.checkDeleteStudent = false;
                            onlineAddStudentData.recoverStudent = restoreOperate; // 是否恢复操作
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                addStudentSuccessModal.addStudentSuccessData().recoverNum(result.recoverNum || 0);
                                addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                $.prompt.goToState('state3'); // 成功弹窗
                            });
                        };

                        //在线添加提交
                        var submitOnlineAddStu = function (postData,successCallback) {
                            $.ajax({
                                url:"/specialteacher/addstudentsonline.vpage",
                                type:"POST",
                                data:postData,
                                success:function (result) {
                                    if(result.success){
                                        if(successCallback && typeof successCallback === "function"){
                                            successCallback(result);
                                        }
                                    }else{
                                        showAddStudentModalErroInfo(result.info);
                                    }
                                },
                                error:function (e) {
                                    console.log(e);
                                }
                            })
                        };

                        //excel添加提交
                        var submitExcelAddStu = function (postData,successCallback) {
                            $.ajax({
                                url:"/specialteacher/addstudentbyexcel.vpage",
                                type:"POST",
                                processData: false,
                                contentType: false,
                                async: true,
                                timeout: 5 * 60 * 1000,
                                data:postData,
                                success:function (result) {
                                    if(result.success){
                                        if(successCallback && typeof successCallback === "function"){
                                            successCallback(result);
                                        }
                                    }else{
                                        showAddStudentModalErroInfo(result.info);
                                    }
                                },
                                error:function (e) {
                                    console.log(e);
                                }
                            })
                        };

                        // 展示弹窗报错
                        var showAddStudentModalErroInfo = function (errorInfo) {
                            // 根据getCurrentStateName的值来找到当前弹窗并展示报错日志
                            var currentStateName = $.prompt.getCurrentStateName();
                            switch (currentStateName) {
                                case 'state1': // 在线添加弹窗
                                    onlineAddStudentModal.onlineAddStudentDialogData().errorInfo(errorInfo?errorInfo:'好像出问题咯！');
                                    break;
                                case 'state2': // excel添加弹窗
                                    excelAddStudentModal.excelAddStudentDialogData().stuExcelTypeError(errorInfo?errorInfo:'好像出问题咯！');
                                    break;
                                case 'state4': // 重复弹窗
                                    addStudentRepeatedErrorModal.addStudentRepeatedErrorData().errorInfo(errorInfo?errorInfo:'好像出问题咯！');
                                    break;
                                case 'state5': // 占用弹窗
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().errorInfo(errorInfo?errorInfo:'好像出问题咯！');
                                    break;
                                case 'state6': // 恢复弹窗
                                    addStudentRestoreModal.addStudentRestoreData().errorInfo(errorInfo?errorInfo:'好像出问题咯！');
                                    break;
                                default:
                            }
                        };

                        //验证excel添加数据
                        var validateExcelAddData = function () {
                            var file = excelAddStudentModal.excelAddStudentDialogData().stuExcelFile();
                            if(file){
                                var fileName = file.name;
                                if(fileName.length <4 || (fileName.substring(fileName.length - 4) != ".xls" && fileName.substring(fileName.length - 5) != ".xlsx")){
                                    excelAddStudentModal.excelAddStudentDialogData().stuExcelTypeError('仅支持上传excel文档');
                                    return false;
                                }else{
                                    return true;
                                }
                            }else{
                                excelAddStudentModal.excelAddStudentDialogData().stuExcelTypeError('请先选择excel文档再上传');
                                return false;
                            }
                        };

                        //提交excel 验证重复
                        var submitExcelValRepeated = function () {
                            // 重新new FormData()对象，否则在第二次上传新excel时会出现数据重复的情况
                            excelAddStudentData = new FormData();
                            excelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                            excelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);
                            excelAddStudentData.append("adjustExcel",excelAddStudentModal.excelAddStudentDialogData().stuExcelFile());
                            excelAddStudentData.append("checkRepeatedStudent",true);
                            excelAddStudentData.append("checkTakeUpStudent",false);
                            excelAddStudentData.append("checkDeleteStudent",false);
                            submitExcelAddStu(excelAddStudentData,function (result) {
                                if(result.repeatedStudentList && result.repeatedStudentList.length != 0){ // 重复添加的列表
                                    var repeatedNames = subAllRepeatedNames(result.repeatedStudentList,result.moreStudent);
                                    addStudentRepeatedErrorModal.addStudentRepeatedErrorData().repeatedNames(repeatedNames);
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state4'); // 重复弹窗
                                }else{
                                    submitExcelValTakeUp();
                                }
                            });
                        };

                        //提交excel 验证占用
                        var submitExcelValTakeUp = function () {
                            var innerExcelAddStudentData = new FormData(),postData = "";
                            if(excelAddStudentData.set && typeof(excelAddStudentData.set) === "function"){
                                excelAddStudentData.set("checkRepeatedStudent",false);
                                excelAddStudentData.set("checkTakeUpStudent",true);
                                excelAddStudentData.set("checkDeleteStudent",false);
                                postData = excelAddStudentData;
                            }else{ //兼容IE等对FormData 实现不彻底的浏览器
                                innerExcelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                                innerExcelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);
                                innerExcelAddStudentData.append("adjustExcel",excelAddStudentModal.excelAddStudentDialogData().stuExcelFile());
                                innerExcelAddStudentData.append("checkRepeatedStudent",false);
                                innerExcelAddStudentData.append("checkTakeUpStudent",true);
                                innerExcelAddStudentData.append("checkDeleteStudent",false);
                                postData = innerExcelAddStudentData;
                            }
                            submitExcelAddStu(postData,function (result) {
                                if(result.isTakeUp){ //被占用
                                    var importStudentNames = subAllRepeatedNames(result.importNames, result.moreFlag);
                                    var takeUpSutInfo = subAllTakeUpInfo(result.takeUpInfo);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().importStudentNames(importStudentNames);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().takeUpSutInfo(takeUpSutInfo);
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state5'); // 占用弹窗
                                }else{
                                    submitExcelValCanRestore();
                                }
                            })
                        };

                        //提交excel 验证恢复
                        var submitExcelValCanRestore = function () {
                            var innerExcelAddStudentData = new FormData(),postData = "";
                            if(excelAddStudentData.set && typeof(excelAddStudentData.set) === "function"){
                                excelAddStudentData.set("checkRepeatedStudent",false);
                                excelAddStudentData.set("checkTakeUpStudent",false);
                                excelAddStudentData.set("checkDeleteStudent",true);
                                postData = excelAddStudentData;
                            }else{ //兼容IE等对FormData 实现不彻底的浏览器
                                innerExcelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                                innerExcelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);
                                innerExcelAddStudentData.append("adjustExcel",excelAddStudentModal.excelAddStudentDialogData().stuExcelFile());
                                innerExcelAddStudentData.append("checkRepeatedStudent",false);
                                innerExcelAddStudentData.append("checkTakeUpStudent",false);
                                innerExcelAddStudentData.append("checkDeleteStudent",true);
                                postData = innerExcelAddStudentData;
                            }
                            submitExcelAddStu(postData,function (result) {
                                if(result.klxDeleteSameName){ // 可恢复
                                    if (result.deleteSameStudents) {
                                        addStudentRestoreModal.addStudentRestoreData().restoreStudentInfo(exchangeDeleteSameStudentsDate(result.deleteSameStudents));
                                    }
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state6'); // 恢复弹窗
                                }else{
                                    addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                    addStudentSuccessModal.addStudentSuccessData().recoverNum(result.recoverNum || 0);
                                    addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state3'); // 成功弹窗
                                }
                            })
                        };

                        //提交excel 不带验证（最终提交）
                        var submitExcelWithoutVal = function (restoreOperate) {
                            var innerExcelAddStudentData = new FormData(),postData = "";
                            if(excelAddStudentData.set && typeof(excelAddStudentData.set) === "function"){
                                excelAddStudentData.set("checkRepeatedStudent",false);
                                excelAddStudentData.set("checkTakeUpStudent",false);
                                excelAddStudentData.set("checkDeleteStudent",false);
                                excelAddStudentData.set("recoverStudent",restoreOperate); // 是否恢复
                                postData = excelAddStudentData;
                            }else{ //兼容IE等对FormData 实现不彻底的浏览器
                                innerExcelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                                innerExcelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);
                                innerExcelAddStudentData.append("adjustExcel",excelAddStudentModal.excelAddStudentDialogData().stuExcelFile());
                                innerExcelAddStudentData.append("checkRepeatedStudent",false);
                                innerExcelAddStudentData.append("checkTakeUpStudent",false);
                                innerExcelAddStudentData.append("checkDeleteStudent",false);
                                innerExcelAddStudentData.append("recoverStudent",restoreOperate);
                                postData = innerExcelAddStudentData;
                            }
                            submitExcelAddStu(postData,function (result) {
                                addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                addStudentSuccessModal.addStudentSuccessData().recoverNum(result.recoverNum || 0);
                                addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                $.prompt.goToState('state3'); // 成功弹窗
                            });
                        };

                        //通用函数，转换后端返回数据
                        var exchangeDeleteSameStudentsDate = function (data) {
                            var collectArr = [];
                            for (var sameStu in data) {
                                var joinFullStr = data[sameStu].join('、');
                                var joinShortStr = data[sameStu].length > 3 ? (data[sameStu].slice(0, 3).join('、') + '...') : data[sameStu].join('、');
                                collectArr.push("<span>" + data[sameStu].length + "名学生（<span title='" + joinFullStr + "'>" + joinShortStr + "</span>）</span>");

                            }
                            return collectArr.join('<br>');
                        };

                        var addStuStates = {
                            state0: {//选择添加方式
                                html:addStuDialogTempHtml,
                                title:"添加学生账号",
                                position: {width: 500},
                                buttons: { },
                                focus: 1
                            },
                            state1: {//在线添加
                                html:onlineAddStuDialogTempHtml,
                                title:"在线添加学生账号",
                                buttons: { "取消": false, "确定": true },
                                position: {width: 640},
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        var onlineAddText = onlineAddStudentModal.onlineAddStudentDialogData().onlineText();
                                        if(validateOnlineAddData(onlineAddText)){ // 判断不为空
                                            validateOnlineRepeat(onlineAddText);
                                        }
                                    }else{
                                        $.prompt.goToState('state0');
                                    }
                                }
                            },
                            state2: { //excel添加
                                html:excelAddStudentDialogTempHtml,
                                title:"通过excel添加账号",
                                buttons: { "取消": false, "确定": true },
                                position: {width: 600},
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        if(validateExcelAddData()){ // 判断excel格式且不为空
                                            submitExcelValRepeated();
                                        }
                                    }else{
                                        $.prompt.goToState('state0');
                                    }
                                }
                            },
                            state3: { //成功添加
                                html:addStudentSuccessTempHtml,
                                title:"添加学生账号",
                                buttons: { "确定": true },
                                position: {width: 640},
                                focus: 0,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        // var nowGradeInfo = self.gradeMenu().gradeInfo();
                                        // var nowClazzInfo = self.gradeMenu().clazzInfo();
                                        $.prompt.close();
                                        self.getAllClazzInfo(function () {
                                            // self.gobackNowGradeOrClass(nowGradeInfo.gradeId, nowClazzInfo, self.gradeMenu().menu);
                                            self.showCardDetail(gradeId, data)
                                        });
                                    }
                                }
                            },
                            state4: {//重复
                                html:addStudentRepeatedErrorTempHtml,
                                title:"系统提示",
                                buttons: { "取消": false, "确定更新学生信息": true },
                                position: {width: 640},
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        //提示了重复之后，直接验证占用
                                        if(addStuDialogModal.addStudentDialogData().backState() == "state1"){
                                            validateOnlineTakeUp();
                                        }else if(addStuDialogModal.addStudentDialogData().backState() == "state2"){
                                            submitExcelValTakeUp();
                                        }
                                    }else{
                                        addStuDialogModal.addStudentDialogData().backToState();
                                    }
                                }
                            },
                            state5: {//占用
                                html:addStudentTakeUpErrorTempHtml,
                                title:"系统提示",
                                buttons: { "取消": false, "确定使用随机填涂号": true },
                                position: {width: 640},
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        // 提示了占用之后，直接验证恢复
                                        if(addStuDialogModal.addStudentDialogData().backState() == "state1"){
                                            validateOnlineCanResore();
                                        }else if(addStuDialogModal.addStudentDialogData().backState() == "state2"){
                                            submitExcelValCanRestore();
                                        }
                                    }else{
                                        addStuDialogModal.addStudentDialogData().backToState();
                                    }
                                }
                            },
                            state6: {//删除历史中有同名学生，选择恢复
                                html: addStudentRestoreTempHtml,
                                title:"系统提示",
                                buttons: { "不恢复并继续": false, "恢复并继续": true},
                                position: {width: 640},
                                focus: 1,
                                submit: function(e,v,m,f) {
                                    e.preventDefault();
                                    if (v) {
                                        // 提示恢复了之后，直接更新
                                        if(addStuDialogModal.addStudentDialogData().backState() == "state1"){
                                            onlineAddStuWithoutVal(true);
                                        }else if(addStuDialogModal.addStudentDialogData().backState() == "state2"){
                                            submitExcelWithoutVal(true);
                                        }
                                    } else {
                                        if(addStuDialogModal.addStudentDialogData().backState() == "state1"){
                                            onlineAddStuWithoutVal(false);
                                        }else if(addStuDialogModal.addStudentDialogData().backState() == "state2"){
                                            submitExcelWithoutVal(false);
                                        }
                                    }
                                }
                            }
                            // 真实顺序：0选择添加方式、1在线添加/2excel添加、4是否存在重复、5是否存在占用、6是否恢复学生、3最终写入上传成功
                        };

                        $.prompt(addStuStates);

                        self.fakePromptLoaded(function () {
                            // 一次完成弹窗内的所有modal绑定
                            ko.applyBindings(addStuDialogModal,document.getElementById("addStudentDialog"));
                            ko.applyBindings(onlineAddStudentModal,document.getElementById("onlineAddStudentDialog"));
                            ko.applyBindings(excelAddStudentModal,document.getElementById("excelAddStudentDialog"));
                            ko.applyBindings(addStudentSuccessModal,document.getElementById("addStudentSuccessDialog"));
                            ko.applyBindings(addStudentRepeatedErrorModal,document.getElementById("addStudentRepeatedErrorDialog"));
                            ko.applyBindings(addStudentTakeUpErrorModal,document.getElementById("addStudentTakeUpErrorDialog"));
                            ko.applyBindings(addStudentRestoreModal,document.getElementById("addStudentRestoreDialog"));
                        });
                    }
                }, data));

                // 获取班组详情数据
                self.getGroupDetailData();

                if(callback && typeof(callback) === "function"){
                    callback();
                }
            },

            //渲染学科
            renderSubject:function (subjectAndTeacherList,groupType,subjectArray) {
                var subjAndTeacherList = [];
                for (var i = 0; i < subjectArray.length; i++) {
                    var _inlineData = {
                        subject:subjectArray[i].key,
                        subjectValue:subjectArray[i].value,
                        teacherList:[]
                    };

                    for (var j = 0; j < subjectAndTeacherList.length; j++) {
                        if(subjectAndTeacherList[j].subject == subjectArray[i].key){
                            _inlineData.hasTeacher = true;
                            _inlineData.teacherList.push({
                                teacherId:subjectAndTeacherList[j].teacherId,
                                teacherName:subjectAndTeacherList[j].teacherName,
                                subject:subjectArray[i].key,
                                subjectValue:subjectArray[i].value
                            });
                            break;
                        }else{
                            _inlineData.hasTeacher = false;
                        }
                    }
                    subjAndTeacherList.push(_inlineData);
                }
                return subjAndTeacherList;
            },

            // 传入对应的模板名称显示标志 1.年级 2.班级 3.班组 4.调整班级
            displayOneTemp: function (index) {
                self.isGradeCard(0);
                self.isClassCard(0);
                self.isClassGroupDetail(0);
                self.isAdjustClassPage(0);
                switch (index) {
                    case 1:
                        self.isGradeCard(1);
                        break;
                    case 2:
                        self.isClassCard(1);
                        break;
                    case 3:
                        self.isClassGroupDetail(1);
                        break;
                    case 4:
                        self.isAdjustClassPage(1);
                        break;
                }
            },

            //获取所有group
            getAllGroups:function () {
                var allData = self.gradeClassInfo();
                var menus = allData.menu;
                var _groups = [];
                if(menus && menus.length != 0){
                    for(var i=0;i<menus.length;i++){
                        var clazzs = menus[i].clazzs;
                        var gradeId = menus[i].gradeId;
                        if(clazzs && clazzs.length != 0){
                            for(var j=0;j<clazzs.length;j++){
                                var groups = clazzs[j].groups;
                                if(groups && groups.length != 0){
                                    for(var k=0;k<groups.length;k++){
                                        _groups.push({
                                            gradeId:gradeId,
                                            group:groups[k]
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
                return _groups;
            },

            getGroupsByGroupId:function (groupObj,gid) {
                var classGroups = [];
                for(var i=0;i<groupObj.length;i++){
                    var group = groupObj[i].group;
                    var gradeId = groupObj[i].gradeId;
                    var groupIds = group.groupIds;
                    for(var j=0;j<groupIds.length;j++){
                        if(groupIds[j] == gid){
                            classGroups.push({
                                gradeId:gradeId,
                                group:group
                            });
                        }
                    }
                }
                return classGroups;
            },

            //跳转到详情
            jumpToClassGroupDetailTemp:function (data) {
                var groupsObj = self.getAllGroups();
                var classGroups = self.getGroupsByGroupId(groupsObj,data.groupId);

                if(classGroups != 0){
                    var classGroup = classGroups[0];

                    self.showCardDetail(classGroup.gradeId, classGroup.group, function () {
                        var selectStudentLoop = setInterval(function () {
                            var trNodes = $('tr[datasum="'+data.studentName+'"]');
                            if(trNodes.length != 0 ){
                                $(trNodes[0]).addClass("active");
                                clearInterval(selectStudentLoop);
                                selectStudentLoop = null;
                            }
                        },100);
                    });
                }else{
                    console.error('cannot match a classGroup by data.groupId : '+data.groupId);
                }
            },

            //重刷详情页
            reFreshClassGroupDetail:function (gid) {
                var groupsObj = self.getAllGroups();
                var classGroups = self.getGroupsByGroupId(groupsObj,gid);
                if(classGroups != 0) {
                    var classGroup = classGroups[0];
                    self.showCardDetail(classGroup.gradeId, classGroup.group);
                }else{
                    console.error('cannot match a classGroup by groupId : '+gid);
                }
            },

            // 新建班群或者调整老师
            addClazzGroupOrAdjustTeacher : function (data) {
                if(data){
                    // 由于行政班和教学的data数据格式不一致，此处根据groupType的不同获取data中的对应数据
                    var adjustSubject = data.groupType ? data.subjAndTeacherList[0].subject : data.subject;
                    var adjustTid = data.groupType ? data.subjAndTeacherList[0].teacherList[0].teacherId : data.teacherId;
                }

                var ClassGroupDialogModal = function () {
                    var _this = this;
                    _this.dialogType = ko.observable();
                    _this.dialogTitle = ko.observable();
                    //获取老师数据
                    _this.getTeacherData = function (key,subject) {
                        $.ajax({
                            url:"/specialteacher/searchteacher.vpage",
                            type:"GET",
                            data:{
                                key:key,
                                subject:subject
                            },
                            success:function (res) {
                                if(res.success){
                                    if(res.teacherList){
                                        // var _classGroupDialogData = _this.classGroupDialogData();
                                        // _classGroupDialogData.newTeacherList = res.teacherList || [];
                                        // _this.classGroupDialogData(_classGroupDialogData);
                                        _this.classGroupDialogData().newTeacherList(res.teacherList);
                                        if(self.addOrAdjustFlag() && self.addOrAdjustFlag() != "add"){
                                            disableSelItems();
                                        }
                                    }
                                }
                            },
                            error:function (e) {
                                console.log(e);
                            }
                        })
                    };

                    //获取调整老师数据
                    _this.getAdjustTeacherData = function (subject) {
                        var adjustTeacherUrl = "/specialteacher/adjustteacherpre.vpage";
                        $.ajax({
                            url:adjustTeacherUrl,
                            type:"GET",
                            // async: false,
                            data:{
                                subject:subject
                            },
                            success:function (res) {
                                if(res.success){
                                    if(res.teacherList){
                                        // var _classGroupDialogData = _this.classGroupDialogData();
                                        // _classGroupDialogData.newTeacherList = res.teacherList || [];
                                        // _this.classGroupDialogData(_classGroupDialogData);
                                        _this.classGroupDialogData().newTeacherList(res.teacherList);
                                        if(self.addOrAdjustFlag() && self.addOrAdjustFlag() != "add"){
                                            disableSelItems();
                                        }
                                    }
                                }
                            },
                            error:function (e) {
                                console.log(e);
                            }
                        });
                    };

                    if(self.addOrAdjustFlag() && self.addOrAdjustFlag() == "add"){
                        _this.dialogType("add");
                        _this.dialogTitle("新建班群");
                    }else{
                        _this.dialogType("adjust");
                        _this.dialogTitle("调整老师");
                    }
                    _this.getAdjustTeacherData(adjustSubject);

                    //搜索老师
                    _this.searchTeacher = function () {
                        _this.getTeacherData(_this.classGroupDialogData().searchTeacherName(),_this.classGroupDialogData().subject());
                    };
                    _this.searchTeacherEvent = function (data, event) {
                        _this.classGroupDialogData().errorInfo(''); // 清除报错信息
                        if(event.keyCode == 13) _this.searchTeacher();
                    };

                    //切换调整老师
                    _this.switchAdjustTeacher = function () {
                        _this.getAdjustTeacherData(_this.classGroupDialogData().subject());
                    };

                    var gradeMenu = self.gradeMenu().menu,
                        GradeArray = [];
                    for(var i=0;i<gradeMenu.length;i++){
                        var clazzArray = [];
                        var classGroupArray = [];
                        for(var j=0;j<gradeMenu[i].clazzs.length;j++){
                            var clazzType = "";
                            for(var k=0;k<gradeMenu[i].clazzs[j].groups.length;k++){
                                classGroupArray.push({
                                    groupName:gradeMenu[i].clazzs[j].groups[k].groupName,
                                    groupId:gradeMenu[i].clazzs[j].groups[k].groupIds[0],
                                    clazzId:gradeMenu[i].clazzs[j].clazzId
                                });
                                clazzType = gradeMenu[i].clazzs[j].groups[k].groupType;
                            }
                            // 老条件：过滤教学班
                            // if(clazzType != "TEACHING_GROUP" && gradeMenu[i].clazzs[j].clazzType != 'WALKING'){ //过滤教学班
                            //     clazzArray.push({
                            //         clazzName:gradeMenu[i].clazzs[j].clazzName,
                            //         clazzId:gradeMenu[i].clazzs[j].clazzId
                            //     });
                            // }
                            // 新条件：支持支持筛选空group教学班级（wiki:36799557）
                            if ((gradeMenu[i].clazzs[j].clazzType != 'WALKING') || (gradeMenu[i].clazzs[j].clazzType == 'WALKING' &&  gradeMenu[i].clazzs[j].groups.length == 0)) {
                                clazzArray.push({
                                    clazzName:gradeMenu[i].clazzs[j].clazzName,
                                    clazzId:gradeMenu[i].clazzs[j].clazzId
                                });
                            }
                        }
                        GradeArray.push({
                            clazzs:clazzArray,
                            classGroups:classGroupArray,
                            gradeId: gradeMenu[i].gradeId,
                            gradeName: gradeMenu[i].gradeName
                        });
                    }

                    _this.classGroupDialogData = ko.observable({
                        newTeacherList: ko.observableArray([]),
                        dialogType:_this.dialogType(), // add、adjust（add:新建班群、adjust:添加老师或修改老师）
                        dialogTypeDetail: ko.observable(), // add、adjust、update（add:新建班群、adjust添加老师、update修改老师，同self.addOrAdjustFlag()）
                        Grades:GradeArray,
                        subjects:ko.observable(self.gradeClassInfo().subjects),
                        grade:ko.observable(),
                        clazz:ko.observable(),
                        clazzGroup:ko.observable(),
                        subject:ko.observable(),
                        subjectName:ko.observable(),
                        // choosedTeacher:ko.observable(),
                        starthasTeacherId:ko.observable(), // 最初老师id
                        temporaryTeacher:ko.observable(''), // 新选择的老师信息
                        searchTeacherName:ko.observable(),
                        errorInfo:ko.observable(''),
                        chooseTeacher:function (data,event) {
                            // _this.classGroupDialogData().choosedTeacher(data.teacherId);
                            _this.classGroupDialogData().temporaryTeacher(data); // 存储当前选中的data
                            var $thisNode = $(event.currentTarget);
                            $thisNode.parent('li').addClass("active").siblings('li').removeClass('active');
                        },
                        changeGrade:function () {
                            if(!_this.classGroupDialogData().grade()){
                                _this.classGroupDialogData().clazz("");
                            }
                        },
                        changeSubject:function () {
                            if(_this.dialogType() == "add"){
                                _this.searchTeacher();
                            }else{
                                _this.switchAdjustTeacher();
                            }
                        },
                        deleteTeacher: function () {
                            // _this.classGroupDialogData().choosedTeacher('');
                            _this.classGroupDialogData().temporaryTeacher('');
                            $('.clazzList').find('li').removeClass('active');
                        }
                    });
                };

                var classGroupDialogModal = new ClassGroupDialogModal();
                var dialogTempHtml = "<div id=\"adjustTeacherDialogContent\" data-bind=\"template: { name: 'adjustTeacherDialog', data: classGroupDialogData }\"></div>";
                var operateGroupOrTeacherState = {
                    state0: {
                        html: dialogTempHtml,
                        title: classGroupDialogModal.dialogTitle(),
                        buttons: { "确定": true },
                        position: {width: 640},
                        submit: function(e,v,m,f){
                            e.preventDefault();
                            if ($('.JS-inputSearchTeacher').is(':focus')) return ;
                            // var newTeacherId = classGroupDialogModal.classGroupDialogData().choosedTeacher();
                            var selectedClazz = classGroupDialogModal.classGroupDialogData().clazz();
                            var newClazzId = "";
                            var newClazzGroup = classGroupDialogModal.classGroupDialogData().clazzGroup();
                            var starthasTeacherId = classGroupDialogModal.classGroupDialogData().starthasTeacherId(); // 最初的老师id
                            var temporaryTeacherInfo = classGroupDialogModal.classGroupDialogData().temporaryTeacher(); // 新选择的老师信息
                            if (temporaryTeacherInfo.teacherId) { // 当前存在老师
                                if (!starthasTeacherId || (temporaryTeacherInfo.teacherId !== starthasTeacherId)) { // 之前无 或者 之前有且与当前的不相等
                                    // 根据类别不同走不同的请求
                                    if(self.addOrAdjustFlag() && self.addOrAdjustFlag() == "add"){ // 新增班群类别
                                        if(selectedClazz){
                                            newClazzId = selectedClazz.clazzId;
                                        }
                                        if(!newClazzId){
                                            classGroupDialogModal.classGroupDialogData().errorInfo("请选择班级");
                                            return false;
                                        }
                                        // 提交新建班群接口
                                        submitNewGroup(newClazzId,temporaryTeacherInfo.teacherId,function (res) {
                                            var nowGradeInfo = self.gradeMenu().gradeInfo();
                                            var nowClazzInfo = self.gradeMenu().clazzInfo();
                                            self.getAllClazzInfo(function () { //新增会返回新的groupId
                                                // self.gobackNowGradeOrClass(nowGradeInfo.gradeId, nowClazzInfo, self.gradeMenu().menu);
                                                if(res.groupId){
                                                    self.reFreshClassGroupDetail(res.groupId);
                                                }
                                            });
                                            $.prompt.close();
                                        });
                                    }else{ // 修改老师(update)和添加老师(adjust)
                                        if(!newClazzGroup){
                                            classGroupDialogModal.classGroupDialogData().errorInfo("请选择班群");
                                            return false;
                                        }
                                        var needToChangeGroupFlag = false,selectedClazzId = newClazzGroup.clazzId,selectedGroupId = newClazzGroup.groupId;
                                        if(selectedClazzId != self.currentClassId()){
                                            needToChangeGroupFlag = true;
                                        }
                                        // 提交调整老师接口
                                        submitAdjustTeacher(selectedClazzId,temporaryTeacherInfo.teacherId,function () {
                                            var nowGradeInfo = self.gradeMenu().gradeInfo();
                                            var nowClazzInfo = self.gradeMenu().clazzInfo();
                                            self.getAllClazzInfo(function () { //调整沿用老的groupId
                                                // self.gobackNowGradeOrClass(nowGradeInfo.gradeId, nowClazzInfo, self.gradeMenu().menu);
                                                setTimeout(function () { //延迟获取共享groups信息，防止操作未完成前被读取
                                                    self.alertDialog({
                                                        text:"调整成功"
                                                    });
                                                    if(!needToChangeGroupFlag){
                                                        if(self.currentGroupId()){
                                                            self.reFreshClassGroupDetail(self.currentGroupId());
                                                        }
                                                    }else{
                                                        self.reFreshClassGroupDetail(selectedGroupId);
                                                    }
                                                },10);
                                            });
                                            $.prompt.close();
                                        });
                                    }
                                } else { // 之前有 且 未改变
                                    classGroupDialogModal.classGroupDialogData().errorInfo("您没有进行任何修改，请进行相关操作后再点击确定");
                                }
                            } else { // 当前不存在
                                if (starthasTeacherId) { // 之前有
                                    var groupTrueTeacher = 0;
                                    for (var k = 0, len = self.classGroupDetailData().subjAndTeacherList.length; k < len; k++) {
                                        if (self.classGroupDetailData().subjAndTeacherList[k].hasTeacher) groupTrueTeacher += 1;
                                    }
                                    if (groupTrueTeacher === 1) { // 当前班群内只有一个老师，不可删除
                                        classGroupDialogModal.classGroupDialogData().errorInfo("班群中至少需要一个老师");
                                    } else if (groupTrueTeacher > 1) { // 多个老师，可删除
                                        $.prompt.goToState('state1');
                                    }
                                } else { // 之前无
                                    classGroupDialogModal.classGroupDialogData().errorInfo("您没有进行任何修改，请进行相关操作后再点击确定");
                                }
                            }
                        }
                    },
                    state1: {
                        html: "<div><p>您确定删除当前老师 <span class='JS-submitDeleteTeacherName'></span> 吗？</p><p style=\"color: #ff0000; margin-top: 10px;\" class=\"JS-submitDeleteTeacherError\"></p></div>",
                        title: "删除老师",
                        buttons: { "取消": false, "确定": true },
                        focus: 1,
                        position: {width: 520},
                        submit: function(e,v,m,f){
                            e.preventDefault();
                            if (v) {
                                submitDeleteTeacher(self.classGroupDetailData().clazzId, classGroupDialogModal.classGroupDialogData().starthasTeacherId(), function (delGroupId) {
                                    // var nowGradeInfo = self.gradeMenu().gradeInfo();
                                    // var nowClazzInfo = self.gradeMenu().clazzInfo();
                                    self.getAllClazzInfo(function () {
                                        // self.gobackNowGradeOrClass(nowGradeInfo.gradeId, nowClazzInfo, self.gradeMenu().menu);
                                        setTimeout(function () {
                                            self.alertDialog({ text:"删除成功" });
                                            var reloadGroupIds = self.classGroupDetailData().groupIds;
                                            reloadGroupIds.splice(reloadGroupIds.indexOf(delGroupId), 1);
                                            self.reFreshClassGroupDetail(reloadGroupIds[0]); // 参数未非删除的groupId的其他groupId
                                        }, 10);
                                    });
                                });
                                $.prompt.close();
                            } else {
                                $.prompt.goToState('state0');
                            }
                        }
                    }
                };
                $.prompt(operateGroupOrTeacherState);
                // init start, 原先这里并非是使用多态弹窗，此处init原本在一个弹窗的loaded方法里面
                // 后因改需求将单个弹窗改成多态弹窗，原loaded方法体中的放置在下方
                self.fakePromptLoaded(function () {
                    ko.applyBindings(classGroupDialogModal, document.getElementById("adjustTeacherDialogContent"));
                    classGroupDialogModal.classGroupDialogData().dialogTypeDetail(self.addOrAdjustFlag());
                    if (self.addOrAdjustFlag() && self.addOrAdjustFlag() == "add") { // 新增班群类别
                        if (self.currentGrade() && self.isGradeCard()) {//年级默认值
                            for (var i in classGroupDialogModal.classGroupDialogData().Grades) {
                                if (classGroupDialogModal.classGroupDialogData().Grades[i].gradeId == self.currentGrade()) {
                                    classGroupDialogModal.classGroupDialogData().grade(classGroupDialogModal.classGroupDialogData().Grades[i]);
                                    break;
                                }
                            }
                        }
                        if (self.currentClassId() && self.isClassCard()) {//班级卡默认值
                            for (var i in classGroupDialogModal.classGroupDialogData().Grades) {
                                if (classGroupDialogModal.classGroupDialogData().Grades[i].gradeId == self.currentGrade()) {
                                    classGroupDialogModal.classGroupDialogData().grade(classGroupDialogModal.classGroupDialogData().Grades[i]);
                                    var innerClazz = classGroupDialogModal.classGroupDialogData().Grades[i].clazzs;
                                    for (var j in innerClazz) {
                                        if (innerClazz[j].clazzId == self.currentClassId()) {
                                            classGroupDialogModal.classGroupDialogData().clazz(innerClazz[j]);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else { // 修改老师(update)和添加老师(adjust)
                        classGroupDialogModal.classGroupDialogData().subject(adjustSubject);
                        adjustSubject = self.classGroupDetailData().groupType === 'SYSTEM_GROUP' ? adjustSubject : self.classGroupDetailData().subjectAndTeacher[0].subject;
                        var subjectArray = self.classGroupDetailData().subjectArray;
                        var subjectTeacherArray = self.classGroupDetailData().subjectAndTeacher;
                        for (var m = 0, len = subjectArray.length; m < len; m++) {
                            if (subjectArray[m].key === adjustSubject) classGroupDialogModal.classGroupDialogData().subjectName(subjectArray[m].value);
                        }
                        // 初始化老师信息
                        for (var n = 0, len2 = subjectTeacherArray.length; n < len2; n++) {
                            if (subjectTeacherArray[n].subject === adjustSubject && subjectTeacherArray[n].teacherId) {
                                classGroupDialogModal.classGroupDialogData().starthasTeacherId(subjectTeacherArray[n].teacherId);
                                classGroupDialogModal.classGroupDialogData().temporaryTeacher(subjectTeacherArray[n]);
                                $('.JS-submitDeleteTeacherName').text(subjectTeacherArray[n].teacherName);
                            }
                        }
                        for (var i in classGroupDialogModal.classGroupDialogData().Grades) {
                            if (classGroupDialogModal.classGroupDialogData().Grades[i].gradeId == self.currentGrade()) {
                                classGroupDialogModal.classGroupDialogData().grade(classGroupDialogModal.classGroupDialogData().Grades[i]);

                                var innerClassGroups = classGroupDialogModal.classGroupDialogData().Grades[i].classGroups;
                                for (var j in innerClassGroups) {
                                    if (innerClassGroups[j].groupId == self.currentGroupId()) {
                                        classGroupDialogModal.classGroupDialogData().clazzGroup(innerClassGroups[j]);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        disableSelItems();
                    }
                });
                // init finished

                //提交新建班群
                var submitNewGroup = function (cid,tid,callBack) {
                    $.ajax({
                        url:"/specialteacher/addnewgroup.vpage",
                        type:"POST",
                        data:{
                            teacherId:tid,
                            clazzId:cid
                        },
                        success:function (res) {
                            if(res.success){
                                if(callBack && typeof(callBack) === "function"){
                                    callBack(res);
                                }
                            }else{
                                classGroupDialogModal.classGroupDialogData().errorInfo(res.info?res.info:'好像出错咯，请稍后重试');
                            }
                        },
                        error:function (e) {
                            console.log(e);
                        }
                    });
                };

                //提交调整老师
                var submitAdjustTeacher = function (cid,tid, callBack) {
                    // 修改老师
                    var oldTeacher=adjustTid;
                    var adjustTeacherUrl = "/specialteacher/adjustteacher.vpage";
                    if(self.addOrAdjustFlag() == "adjust"){ // 新增老师
                        oldTeacher = "";
                        adjustTeacherUrl = "/specialteacher/addteacher.vpage";
                    }

                    var postData = {
                        oldTeacherId:oldTeacher,
                        newTeacherId:tid,
                        groupId:self.currentGroupId(),
                        clazzId:cid
                        // type:adjustType //1.更改分组老师，2.新建共享分组并添加老师
                    };

                    $.ajax({
                        url:adjustTeacherUrl,
                        type:"POST",
                        data:postData,
                        success:function (res) {
                            if(res.success){
                                if(callBack && typeof(callBack) === "function"){
                                    callBack();
                                }
                            }else{
                                classGroupDialogModal.classGroupDialogData().errorInfo(res.info?res.info:'好像出错咯，请稍后重试');
                            }
                        },
                        error:function (e) {
                            console.log(e);
                        }
                    });
                };

                // 提交删除老师
                var submitDeleteTeacher = function (cid, tid, callBack) {
                    var postData = {
                        clazzId: cid,
                        groupId: self.currentGroupId(),
                        teacherId:tid
                    };
                    $.ajax({
                        url: "/specialteacher/delteacher.vpage",
                        type: "POST",
                        data: postData,
                        success: function (res) {
                            if(res.success){
                                if(callBack && typeof(callBack) === "function"){
                                    callBack(res.delGroupId);
                                }
                            } else {
                                $('.JS-submitDeleteTeacherError').text(res.info?res.info:'好像出错咯，请稍后重试');
                            }
                        }
                    })
                }

                var disableSelItems = function () {
                    //禁用调整选项
                    $(".js-gradeSelItem").attr("disabled","disabled");
                    $(".js-clazzSelItem").attr("disabled","disabled");
                    $(".js-subjectSelItem").attr("disabled","disabled");
                };
            },
            //新建班群
            addClazzGroup : function () {
                self.addOrAdjustFlag("add");
                self.addClazzGroupOrAdjustTeacher();
            },
            //合并班群
            mergeGroup: function () {
                // 教学班置为不可操作状态
                $('.JS-mergeOperateBox2').children('div').addClass('disabled');
                if (self.isGradeCard()) { // 点击的是年级导航
                    self.canMergeList(self.gradeCardListData().administrativeClass);
                } else if (self.isClassCard()) { // 点击的是班级导航
                    self.canMergeList(self.clazzCardListData().groups);
                }
                // 遍历self.canMergeList()，增加字段groupId
                for (var i = 0, len = self.canMergeList().length; i < len; i++) {
                    self.canMergeList()[i].groupId = self.canMergeList()[i].groupIds[0];
                }
                self.isShowMerge(1);
                if (self.canMergeList().length < 2) {
                    // $('.JS-mergeBtn').addClass('disabled');
                } else {
                    // $('.JS-mergeBtn').removeClass('disabled');
                    self.updateGroupDetail(-1, '')
                }
            },
            // 更新学生学号
            updateStuNo: function () {
                // 更新学生学号弹窗1模板
                var updateStuNoDialogModal = function () {
                    var _this = this;
                    _this.updateStuNoDialogData = ko.observable({
                        downLoadExcel: function (id) {
                            var downloadIframe = "<iframe style='display:none;' src='/specialteacher/gettemplate.vpage?template=" + id + "'/>";
                            $("body").append(downloadIframe);
                        },
                        uploadExcelFlag: false,
                        checkUploadComplete: function () {
                            $(".JS-fileupload[name='adjustExcel']").change(function () {
                                if (_this.updateStuNoDialogData().uploadExcelFlag) return;
                                _this.updateStuNoDialogData().uploadExcelFlag = true;

                                var uploadExcelName = $(this).val();
                                if (uploadExcelName.substring(uploadExcelName.length - 4) != ".xls" && uploadExcelName.substring(uploadExcelName.length - 5) != ".xlsx") {
                                    $.prompt.goToState('state2', false, function () {
                                        updateStuNoErrorDialogMode.updateStuNoErrorDialogData().updateStuNoErrorTypeOneText("文档格式异常，只支持.xls或.xlsx格式");
                                    });
                                    return ;
                                }

                                var formData = new FormData();
                                var file = $(".JS-fileupload[name='adjustExcel']")[0].files[0];
                                formData.append('adjustExcel', file);
                                // 第一步ajax接口check
                                $.ajax({
                                    url: '/specialteacher/admin/checkupdatestudentnum.vpage',
                                    type: 'POST',
                                    data: formData,
                                    processData: false,
                                    contentType: false,
                                    async: true,
                                    timeout: 10 * 1000,
                                    success: function (res) {
                                        _this.updateStuNoDialogData().uploadExcelFlag = false;
                                        if (res.success) {
                                            $('#updateStuNoForm')[0].submit();// 第二步，form表单submit
                                            if (res.result === 2) { // 全部上传成功
                                                $.prompt.goToState('state4');
                                            } else {
                                                $.prompt.goToState('state3');
                                            }
                                        } else {
                                            $.prompt.goToState('state2', false, function () {
                                                updateStuNoErrorDialogMode.updateStuNoErrorDialogData().updateStuNoErrorTypeOneText(res.info);
                                            });
                                        }
                                    },
                                    error: function () {
                                        _this.updateStuNoDialogData().uploadExcelFlag = false;
                                        $.prompt('出错了，稍后重试！', {
                                            title: '系统提示',
                                            focus: 0,
                                            buttons: {"知道了": true}
                                        });
                                    }
                                });
                            });
                        }
                    });
                };
                // 更新学生学号弹窗2-不执行文档报错（单独抽出成模板是因为尝试在上传失败处理中设置HTML不生效，和绑定模板有关，故而另设置一个模板）
                var updateStuNoErrorDialogModal = function () {
                    var _this = this;
                    _this.updateStuNoErrorDialogData = ko.observable({
                        updateStuNoErrorTypeOneText: ko.observable('')
                    });
                };

                var updateStuNoDialogMode = new updateStuNoDialogModal();
                var updateStuNoErrorDialogMode = new updateStuNoErrorDialogModal();
                var updateStuNoDialogHtml = "<div id=\"updateStuNoDialogContent\" data-bind=\"template: { name: 'updateStuNoDialog', data: updateStuNoDialogData}\" ></div>";
                var updateStuNoErrorDialogHtml = "<div id=\"updateStuNoErrorDialogContent\" data-bind=\"template: {name: 'updateStuNoErrorDialog', data: updateStuNoErrorDialogData}\"></div>";
                var updateStuNoState = {
                    // state0留作后期选择excel或在线添加使用
                    state1: {
                        html: updateStuNoDialogHtml,
                        title: '更新学生学号',
                        focus: 0,
                        buttons: {"上传": true},
                        position: {width: 550},
                        submit: function (e, v) {
                            e.preventDefault();
                            var obj = $(".JS-fileupload[name='adjustExcel']");
                            obj.val(''); // 每次点击上传都清空当前值
                            var ie = !-[1,];
                            if(ie){
                                $(obj).trigger('click').trigger('change');
                            }else{
                                $(obj).trigger('click');
                            }
                            updateStuNoDialogMode.updateStuNoDialogData().checkUploadComplete(); // 检测选中后自动上传
                        }
                    },
                    state2: {
                        html: updateStuNoErrorDialogHtml,
                        title: '系统提示',
                        focus: 1,
                        buttons: {"取消": false, "重试": true},
                        submit: function (e, v) {
                            e.preventDefault();
                            if (v) {
                                $.prompt.goToState('state1');
                            } else {
                                $.prompt.close();
                            }
                        }
                    },
                    state3: {
                        html: '部分上传完成，错误信息请查看结果附件。',
                        title: '系统提示',
                        focus: 0,
                        buttons: {"确定": true}
                    },
                    state4: {
                        html: '全部上传完成，请查看结果附件。',
                        title: '系统提示',
                        focus: 0,
                        buttons: {"确定": true}
                    }
                };
                $.prompt(updateStuNoState);
                self.fakePromptLoaded(function () {
                    ko.applyBindings(updateStuNoDialogMode, document.getElementById('updateStuNoDialogContent'));
                    ko.applyBindings(updateStuNoErrorDialogMode, document.getElementById('updateStuNoErrorDialogContent'));
                });
            },
            // 批量生成学科组
            batchGeneralSubject: function () {
                // 更新学生学号弹窗1模板
                var batchGenerateSubjectDialogModal = function () {
                    var _this = this;
                    _this.batchGenerateSubjectDialogData = ko.observable({
                        batchGenerateSubjectList: ko.observableArray([]),
                        choiceBatchGenerateSubjectList: ko.observableArray([]), // 选中的学科
                        choiceBatchGenerateErrorText: ko.observable(''),
                        choiceBatchGenerateSubject: function (data, event) {
                            var $this = $(event.currentTarget);
                            if ($this.is(':checked')) {
                                _this.batchGenerateSubjectDialogData().choiceBatchGenerateSubjectList().push(data.key); // 收集学科
                            } else {
                                _this.batchGenerateSubjectDialogData().choiceBatchGenerateSubjectList().splice(_this.batchGenerateSubjectDialogData().choiceBatchGenerateSubjectList().indexOf(data.key)); // 去除勾选
                            }
                        },
                        requestFlag: ko.observable(false)
                    });
                };
                var batchGenerateSubjectDialogMode = new batchGenerateSubjectDialogModal();
                var batchGenerateSubjectDialogHtml = "<div id=\"batchGenerateSubjectDialogContent\" data-bind=\"template: { name: 'batchGenerateSubjectDialog', data: batchGenerateSubjectDialogData}\" ></div>";
                $.prompt(batchGenerateSubjectDialogHtml, {
                    title: '选择学科',
                    focus: 1,
                    buttons: { "取消": false, "确定": true },
                    loaded: function () {
                        batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().batchGenerateSubjectList(self.gradeClassInfo().subjects); // 去当前年级下的subjectList
                        ko.applyBindings(batchGenerateSubjectDialogMode, document.getElementById('batchGenerateSubjectDialogContent'));
                    },
                    submit: function (e, v) {
                        if(v){
                            e.preventDefault();
                            if (!batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().choiceBatchGenerateSubjectList().length) {
                                batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().choiceBatchGenerateErrorText('您还未勾选学科');
                                return ;
                            }
                            if (batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().requestFlag()) return ;
                            batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().requestFlag(true);
                            $.ajax({
                                url: '/specialteacher/createExamGroup.vpage',
                                type: 'POST',
                                data: {
                                    gradeId: self.gradeMenu().gradeInfo().gradeId,
                                    subjects: JSON.stringify(batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().choiceBatchGenerateSubjectList())
                                },
                                success: function (res) {
                                    if (res.success) {
                                        var nowGradeInfo = self.gradeMenu().gradeInfo();
                                        self.adjustCardCallback(nowGradeInfo, '', '您已成功生成学科组', true);
                                    } else {
                                        batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().choiceBatchGenerateErrorText(res.info);
                                    }
                                },
                                complete: function () {
                                    batchGenerateSubjectDialogMode.batchGenerateSubjectDialogData().requestFlag(false); // 重置flag
                                }
                            })
                        }
                    }
                })
            },
            selectMergeGroup: function (data, event) {
                if (!self.isShowMerge()) return false;
                // 正确选中的班群扔进self.mergeSelectList()数组中
                var $thisNode = $(event.currentTarget);
                var groupLists = self.canMergeList();
                var mergeSelectLists = self.mergeSelectList();
                if ($thisNode.children('div').hasClass('disabled') || $thisNode.children('div').hasClass('error')) { // 不可选择或出错
                    return false;
                }
                if ($thisNode.children('div').hasClass('selected')) { // 已选中，去除
                    $thisNode.children('div').removeClass('selected');
                    // 遍历找到已选择的删除它
                    for (var i = 0, len = mergeSelectLists.length; i < len; i++) {
                        if (groupLists[$thisNode.index()].groupId === mergeSelectLists[i].groupId) {
                            mergeSelectLists.splice(i, 1);
                            break;
                        }
                    }
                    self.updateGroupDetail($thisNode.index(), 'remove');
                } else { // 当前未选中，选择
                    if (mergeSelectLists.length === 0) { // 第一次选择
                        $thisNode.children('div').removeClass('hover').addClass('selected');
                        mergeSelectLists.push(groupLists[$thisNode.index()]); // 存储成功选择的
                    } else { // 非第一次
                        var selectGroupSubject = groupLists[$thisNode.index()].subjectAndTeacher;
                        for (var j = 0, len1 = selectGroupSubject.length; j < len1; j++) {
                            for (var m = 0, len2 = mergeSelectLists.length; m < len2; m++) {
                                for (var n = 0, len3 = mergeSelectLists[m].subjectAndTeacher.length; n < len3; n++) {
                                    if (selectGroupSubject[j].subject === mergeSelectLists[m].subjectAndTeacher[n].subject) {
                                        $thisNode.children('div').removeClass('hover').addClass('error');
                                        setTimeout(function () {
                                            $thisNode.children('div').removeClass('error');
                                        }, 2000);
                                        break;
                                    }
                                }
                            }
                        }
                        if (!$thisNode.children('div').hasClass('error')) {
                            $thisNode.children('div').removeClass('hover').addClass('selected');
                            mergeSelectLists.push(groupLists[$thisNode.index()]); // 存储成功选择的
                        }
                    }
                    self.updateGroupDetail($thisNode.index(), 'add');
                }
                self.checkoutBtn();
            },
            checkoutBtn: function () {
                if (self.mergeSelectList().length >= 2) {
                    $('.JS-mergeBtn').removeClass('disabled');
                } else {
                    $('.JS-mergeBtn').addClass('disabled');
                }
            },
            // 更新状态
            updateGroupDetail: function (index, type) {
                // 根据当前的选中状态，决定其余的是否可点击，参数index表示当前点击的是第几个，参数type表示当前点击的是添加还是删除
                var $mergeOperateBox = $('.JS-mergeOperateBox');
                $mergeOperateBox.on('mouseover', function () {
                    if (!$(this).children('div').hasClass('selected') &&
                        !$(this).children('div').hasClass('error') &&
                        !$(this).children('div').hasClass('disabled') &&
                        self.isShowMerge()) {
                        $(this).children('div').addClass('hover').parent().siblings('li').children('div').removeClass('hover');
                    }
                }).on('mouseout', function () {
                    $(this).children('div').removeClass('hover');
                });

                if (index === -1) return ;

                var groupLists = self.canMergeList();
                var mergeSelectLists = self.mergeSelectList();
                var nowSelectGroup = groupLists[index];
                if (type === 'add') {
                    for (var m = 0, len = groupLists.length; m < len; m++) {
                        if (groupLists[m].clazzId !== nowSelectGroup.clazzId) {
                            $mergeOperateBox.eq(m).children('div').addClass('disabled');
                        }
                    }
                } else if (type === 'remove') {
                    var hasSameClazzId = false;
                    for (var m = 0, len = groupLists.length; m < len; m++) {
                        for (var n = 0, len2 = mergeSelectLists.length; n < len2; n++) {
                            if (groupLists[m].clazzId === mergeSelectLists[n].clazzId) hasSameClazzId = true;
                        }
                        if (!hasSameClazzId) $mergeOperateBox.eq(m).children('div').removeClass('disabled');
                    }
                } else {
                    return ;
                }

            },
            // 点击合并按钮
            sureMerge: function () {
                if ($('.JS-mergeBtn').hasClass('disabled')) return ;
                var mergeGroupString = '';
                var mergeGroupNameString = '';
                var mergeSelectLists = self.mergeSelectList();
                for (var i = 0, len = mergeSelectLists.length; i < len; i++) {
                    mergeGroupString += ',' + mergeSelectLists[i].groupIds.join(',');
                    mergeGroupNameString += '、' + mergeSelectLists[i].groupName;
                }
                mergeGroupString = mergeGroupString.substring(1);
                mergeGroupNameString = mergeGroupNameString.substring(1);
                var mergeState = {
                    state0: {
                        html: "<div><p>您已选择" + mergeGroupNameString + "，合并后将无法恢复，确认合并吗？</p><p style=\"color: #ff0000; margin-top: 10px;\" class=\"JS-error\"></p></div>",
                        title: "合并班群",
                        buttons: { "取消": false, "确定": true },
                        focus: 1,
                        submit: function(e,v,m,f){
                            e.preventDefault();
                            if (v) {
                                var data = {groupIds: mergeGroupString};
                                $.post('/specialteacher/mergegroups.vpage', data, function (res) {
                                    if (res.success) {
                                        $.prompt.goToState('state1');
                                        $('.jqiclose').hide();
                                    } else {
                                        $('.JS-error').html(res.info);
                                    }
                                });
                            } else {
                                $.prompt.close();
                            }
                        }
                    },
                    state1: {
                        html: "合并成功！",
                        title: "合并班群",
                        buttons: { "确定": true },
                        submit: function (e,v,m,f) {
                            e.preventDefault();
                            if (v) {
                                window.location.reload();
                            }
                        }
                    }
                };
                $.prompt(mergeState);
            },
            // 点击取消按钮
            cancelMerge: function () {
                self.isShowMerge(0);
                $('.JS-mergeBtn').addClass('disabled');
                self.mergeSelectList([]);
                $('.JS-mergeOperateBox').children('div').removeClass('hover selected disabled error'); // 复位合并选中的状态
                $('.JS-mergeOperateBox2').children('div').removeClass('disabled');
            },
            //班群详情添加老师
            addSubjectTeacher: function (data) {
                self.addOrAdjustFlag("adjust");
                self.addClazzGroupOrAdjustTeacher(data);
            },

            //班群详情编辑老师
            editSubjectTeacher: function (data) {
                self.addOrAdjustFlag("update");
                self.addClazzGroupOrAdjustTeacher(data);
            },

            //调整班级菜单
            adjustClassMenu: function (data,event) {
                self.isShowMerge(0);
                self.displayOneTemp(4);
                var adjustClazz = self.gradeClassInfo().adjustClazz;
                self.adjustGrades(adjustClazz);

                // var $thisNode = $(event.currentTarget);
                // if ($thisNode.hasClass('active')) {
                //     $thisNode.removeClass('active').siblings('a.js-classMenuItem').removeClass('active');
                //     $('.js-subMenuItem').addClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                // } else {
                //     $thisNode.addClass('active').siblings('a.js-classMenuItem').removeClass('active');
                //     $('.js-subMenuItem').removeClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                // }
            },

            // 调整班级选择班级
            // changeSelectAdjustGrade: function () {
            //     var selectedGrade = self.adjustGrade();
            //     var _adjustClassData = self.adjustClassData();
            //     if(selectedGrade){
            //         _adjustClassData.defaultTemp = 0;
            //         _adjustClassData.aClassList = selectedGrade.aClassList;
            //         _adjustClassData.tClassList = selectedGrade.tClassList;
            //     }else{
            //         _adjustClassData.defaultTemp = 1;
            //     }
            //     self.adjustClassData(_adjustClassData);
            // },

            //创建校内班级
            createSchoolClass:function (type, data) {
                var CreateSchoolClassModal = function () {
                    var _self = this;
                    _self.createSchoolClassData = ko.observable({
                        classTypes:[
                            {name:"行政班",type:'1'},
                            {name:"教学班",type:'3'}
                        ],
                        classGrades:self.gradeClassInfo().adjustClazz,
                        classSubjects:self.gradeClassInfo().subjects,
                        classType:ko.observable(),
                        classGrade:ko.observable(),
                        classSubject:ko.observable(),
                        className:ko.observable(),
                        showTeachingSubject:ko.observable(0),
                        showTeachingTeacher:ko.observable(0),
                        showStageType: ko.observable(0),
                        stageTypeArr: ['不分层', 'A层', 'B层', 'C层', 'D层', 'E层', 'F层'],
                        stageTypeValue: ko.observable('不分层'),
                        hasTeacherList:ko.observable(0),
                        walkingTeacher:ko.observable(),
                        walkingTeachers:ko.observable(),
                        errorInfo:ko.observable(""),
                        changeClassType:function () {
                            if(_self.createSchoolClassData().classType() && _self.createSchoolClassData().classType().type == "3"){
                                _self.createSchoolClassData().showTeachingSubject(1);
                                _self.createSchoolClassData().showTeachingTeacher(1);
                                _self.createSchoolClassData().showStageType(1);
                            }else{
                                _self.createSchoolClassData().showTeachingSubject(0);
                                _self.createSchoolClassData().showTeachingTeacher(0);
                                _self.createSchoolClassData().showStageType(0);
                            }
                        },
                        changeClassSubject:function () {
                            if(_self.createSchoolClassData().classSubject()){
                                _self.createSchoolClassData().getWalkingTeachers(_self.createSchoolClassData().classSubject().key);
                            }else{
                                _self.createSchoolClassData().getWalkingTeachers();
                            }
                        },
                        getWalkingTeachers:function (subject) { //获取任课老师
                            $.ajax({
                                url:"/specialteacher/adjustteacherpre.vpage",
                                type:"GET",
                                data:{
                                    subject:subject
                                },
                                success:function (res) {
                                    if(res.success){
                                        // if(res.teacherList && res.teacherList.length != 0){
                                        _self.createSchoolClassData().walkingTeachers(res.teacherList);
                                        // }
                                    }
                                    _self.createSchoolClassData().hasTeacherList(1);
                                },
                                error:function (e) {
                                    console.log(e);
                                }
                            })
                        },
                        createSchoolClassSubmit:function () {
                            var postData = {
                                clazzType:_self.createSchoolClassData().classType()?_self.createSchoolClassData().classType().type:"",
                                gradeId: _self.createSchoolClassData().classGrade()?_self.createSchoolClassData().classGrade().gradeId:"",
                                clazzName:_self.createSchoolClassData().className(),
                                subject:_self.createSchoolClassData().classSubject()?_self.createSchoolClassData().classSubject().key:"",
                                walkingTeacherId:_self.createSchoolClassData().walkingTeacher()?_self.createSchoolClassData().walkingTeacher().teacherId:"",
                                stageType: _self.createSchoolClassData().stageTypeValue()
                            };
                            if(validateCreateData(postData)){
                                $.ajax({
                                    url:"/specialteacher/addnewclazz.vpage",
                                    type:"post",
                                    data:postData,
                                    success:function (res) {
                                        if (res.success){
                                            var nowGradeInfo = self.gradeMenu().gradeInfo();
                                            self.adjustCardCallback(nowGradeInfo, '', '您已成功添加新班级', true);
                                            // self.getAllClazzInfo(function () {
                                            //     $('.adjust').click();
                                            // });
                                        }else{
                                            createSchoolClassModal.createSchoolClassData().errorInfo(res.info?res.info:"好像出错咯，请稍后再试");
                                        }
                                    },
                                    error:function (e) {
                                        console.log(e);
                                    }
                                })
                            }
                        }
                    });
                };

                // 校验新增班级
                var validateCreateData = function (postdata) {
                    if(!postdata.clazzType){
                        createSchoolClassModal.createSchoolClassData().errorInfo("请选择班级类型");
                        return false;
                    }
                    if(!postdata.gradeId){
                        createSchoolClassModal.createSchoolClassData().errorInfo("请选择班级年级");
                        return false;
                    }
                    if(!postdata.clazzName){
                        createSchoolClassModal.createSchoolClassData().errorInfo("请填写班级名称");
                        return false;
                    }
                    if(postdata.clazzType == 3 && !postdata.subject){
                        createSchoolClassModal.createSchoolClassData().errorInfo("请选择走课学科");
                        return false;
                    }
                    if(postdata.clazzType == 3 && !postdata.walkingTeacherId){
                        if (createSchoolClassModal.createSchoolClassData().walkingTeachers().length == 0) {
                            createSchoolClassModal.createSchoolClassData().errorInfo("当前学科下暂时没有老师");
                        } else {
                            createSchoolClassModal.createSchoolClassData().errorInfo("请选择任课老师");
                        }
                        return false;
                    }
                    return true;
                };

                var createSchoolClassModal = new CreateSchoolClassModal();
                var createSchoolClassTempHtml = "<div id=\"createSchoolClassDialog\" data-bind=\"template: { name: 'createSchoolClassTemp', data: createSchoolClassData }\"></div>";
                $.prompt(createSchoolClassTempHtml, {
                    title: '新增班级',
                    focus: 1,
                    buttons: { "取消": false, "确定": true },
                    loaded: function () {
                        createSchoolClassModal.createSchoolClassData().getWalkingTeachers(createSchoolClassModal.createSchoolClassData().classSubject());
                        ko.applyBindings(createSchoolClassModal,document.getElementById("createSchoolClassDialog"));
                        // 根据你参数type来展示对应类型
                        if (type == '1') { // 行政班班
                            // 班级类型默认选中
                            createSchoolClassModal.createSchoolClassData().classType(createSchoolClassModal.createSchoolClassData().classTypes[0]);
                            createSchoolClassModal.createSchoolClassData().changeClassType();
                            createSchoolClassModal.createSchoolClassData().showStageType(0);
                        } else if (type == '3') { // 教学班
                            createSchoolClassModal.createSchoolClassData().classType(createSchoolClassModal.createSchoolClassData().classTypes[1]);
                            createSchoolClassModal.createSchoolClassData().changeClassType();
                            createSchoolClassModal.createSchoolClassData().showStageType(1);
                        }
                        // 班级年级默认选中
                        for (var i = 0, len = createSchoolClassModal.createSchoolClassData().classGrades.length; i < len; i++) {
                            if (createSchoolClassModal.createSchoolClassData().classGrades[i].gradeId === self.gradeMenu().gradeInfo().gradeId) {
                                createSchoolClassModal.createSchoolClassData().classGrade(createSchoolClassModal.createSchoolClassData().classGrades[i]);
                            }
                        }
                    },
                    submit:function(e,v,m,f){
                        if(v){
                            e.preventDefault();
                            createSchoolClassModal.createSchoolClassData().createSchoolClassSubmit();
                        }
                    }
                });
            },

            getGroupDetailData: function () {
                $.ajax({
                    url: '/specialteacher/groupdetail.vpage',
                    type: 'POST',
                    data:{groupIds: JSON.stringify(self.classGroupDetailData().groupIds)},
                    success: function (res) {
                        var cgs = res.students || [];
                        cgs.sort(function (a, b) {
                            return a.studentName.localeCompare(b.studentName, 'zh-Hans-CN');
                        });
                        self.classGroupDetailData().classGroupStudents(cgs);
                        self.classGroupDetailData().isShowStuDateAbnormalTip(res.errorFlag||false); // 控制显示异常数据提醒
                        self.classGroupDetailData().mergeStudentsNames(res.mergeStudentsNames||[]); // 控制显示可恢复数据提醒
                    },
                    error: function (e) {
                        console.log(e);
                    }
                });
            },
            changeCgs: function (data,event) {
                var $element = $(event.currentTarget);
                var tr = $element.parents("tr");
                if (tr.hasClass("active")){
                    tr.removeClass("active");
                    return ;
                }
                tr.addClass("active");
            },
            changeActive:function () {
                var changeList = [];
                var changeId = $(".clazzDetails-box").find("tr.active");
                $(changeId).each(function(){
                    changeList.push({
                        userid:$(this).attr("dataid"),
                        a17id:$(this).attr("a17id"),
                        name:$(this).find(".name").text()
                    });
                });
                return changeList;
            },
            // 恢复异常数据
            restoreDetailStuDate: function () {
                $.ajax({
                    url: '/specialteacher/syncgroupstudents.vpage',
                    type: 'POST',
                    data: {
                        groupIds: JSON.stringify(self.classGroupDetailData().groupIds)
                    },
                    success: function (res) {
                        if (res.success) {
                            self.getGroupDetailData(); // 重刷详情
                            $.prompt('学生信息已恢复', {
                                title: '系统提示',
                                focus: 0,
                                buttons: {'知道了': true}
                            });
                        } else {
                            $.prompt(res.info, {
                                title: '系统提示',
                                focus: 0,
                                buttons: {'知道了': true}
                            })
                        }
                    }
                });
            },
            // 合并同姓名学生账号
            mergeDetailStuDate: function () {
                var mergeStudentsNames = self.classGroupDetailData().mergeStudentsNames();
                var mergeDetailStuHtml = "<p>姓名相同，其中一个有一起ID+手机，另一个有校内学号+阅卷机填涂号。此类学生班内共有<span style='color: #599d41;'> " + mergeStudentsNames.length + " </span>人（" + mergeStudentsNames.join('，') + "），合并后会变为姓名+校内学号+一起ID+手机+阅卷机填涂号。<br><br>是否确认合并？</p>";
                $.prompt(mergeDetailStuHtml, {
                    title: '合并学生',
                    focus: 1,
                    buttons: {'取消': false, '合并': true},
                    submit: function (e, v) {
                        e.preventDefault();
                        if (v) {
                            if (!mergeStudentsNames.length) return ;
                            $.ajax({
                                url: '/specialteacher/mergesamenamestudent.vpage',
                                type: 'POST',
                                data: {
                                    groupIds: JSON.stringify(self.classGroupDetailData().groupIds)
                                },
                                success: function (res) {
                                    if (res.success) {
                                        self.getGroupDetailData(); // 重刷详情
                                        $.prompt('学生合并成功！', {
                                            title: '系统提示',
                                            focus: 0,
                                            buttons: {'知道了': true}
                                        });
                                    } else {
                                        $.prompt(res.info, {
                                            title: '系统提示',
                                            focus: 0,
                                            buttons: {'知道了': true}
                                        })
                                    }
                                }
                            });
                        } else {
                            $.prompt.close();
                        }
                    }
                });



            },
            // 按学生姓名排序
            orderStudentName:function () {
                var cgs = self.classGroupDetailData().classGroupStudents();
                var $stuNameIconNode = $('.JS-studentNameSeq');
                var $stuNoIconNode = $('.JS-studentNoSeq');
                $stuNoIconNode.removeClass('order reverse');
                if ($stuNameIconNode.hasClass('order')) {
                    $stuNameIconNode.removeClass('order').addClass('reverse');
                    cgs.sort(function (a, b) {
                        return b.studentName.localeCompare(a.studentName, 'zh-Hans-CN');
                    });
                } else {
                    $stuNameIconNode.removeClass('reverse').addClass('order');
                    cgs.sort(function (a, b) {
                        return a.studentName.localeCompare(b.studentName, 'zh-Hans-CN');
                    });
                }
                self.classGroupDetailData().classGroupStudents(cgs);
            },
            // 按校内学号
            orderStudentNo:function () {
                var cgs = self.classGroupDetailData().classGroupStudents();
                var $stuNameIconNode = $('.JS-studentNameSeq');
                var $stuNoiconNode = $('.JS-studentNoSeq');
                $stuNameIconNode.removeClass('order reverse');
                if ($stuNoiconNode.hasClass('order')) {
                    $stuNoiconNode.removeClass('order').addClass('reverse');
                    cgs.sort(function (a, b) {
                        return b.studentNum - a.studentNum
                    });
                } else {
                    $stuNoiconNode.removeClass('reverse').addClass('order');
                    cgs.sort(function (a, b) {
                        return a.studentNum - b.studentNum
                    });
                }
                self.classGroupDetailData().classGroupStudents(cgs);
            },
            // 转班和复制到教学班操作
            changeCgsBtn:function (index) {
                var text = "",title = "",url = "",result = "",tipText,data = {};
                if(index == 1){
                    text = "您还没有选择要转班的学生哦！";
                    title = "转班";
                    url = "/specialteacher/changeclazz.vpage";
                    result = "转班成功！";
                    tipText = "转移";
                }else{
                    text = "您还没有选择要复制到教学班的学生哦！";
                    title = "复制到教学班";
                    url = "/specialteacher/copytoteachingclazz.vpage";
                    result = "复制学生到教学班已完成！";
                    tipText = "复制";
                }
                if (self.changeActive().length == 0){
                    self.alertDialog({
                        text: text
                    });
                }else{
                    var ChangeClazzModal = function () {
                        var _this = this;
                        var gradeMenu = self.gradeMenu().menu,
                            GradeArray = [],
                            gradeNum = $(".gradeid").attr("gradeId"),
                            gradeName = "";
                        for(var i=0;i<gradeMenu.length;i++){
                            if (gradeMenu[i].gradeId == gradeNum){
                                if(index == 1){
                                    GradeArray = gradeMenu[i].administrativeClass;
                                }else{
                                    GradeArray = gradeMenu[i].teachingClass;
                                }
                                gradeName = gradeMenu[i].gradeName;
                                break;
                            }
                        }
                        _this.changeClazzData = ko.observable({
                            text: ko.observable(tipText),
                            changeList: ko.observableArray(self.changeActive()),
                            clazzs:GradeArray,
                            gradeName:gradeName,
                            changeDelete: function (data,event) {
                                var $element = $(event.currentTarget);
                                $element.parents("li").remove();
                            },
                            errorInfo:ko.observable()
                        });
                    };
                    var changeClazzMode = new ChangeClazzModal();
                    var changeClazzHtml = "<div id=\"changeClazzContent\" data-bind=\"template: { name: 'changeClazz', data: changeClazzData }\"></div>";
                    $.prompt(changeClazzHtml, {
                        focus: 1,
                        title: title,
                        buttons: {"取消": false, "确定": true},
                        position: {width: 700},
                        loaded: function () {
                            ko.applyBindings(changeClazzMode, document.getElementById("changeClazzContent"));
                        },
                        submit: function (e, v) {
                            if (v) {
                                e.preventDefault();
                                var changeEnd = [],change17Id = [];
                                var changeId = $(".js-clazzList").find("li");
                                $(changeId).each(function(){
                                    if ($(this).attr("dataid") == null){
                                        change17Id.push($(this).attr("a17id"));
                                    }else{
                                        changeEnd.push($(this).attr("dataid"));
                                    }
                                });
                                var changeStr = changeEnd.join(","),change17Str = change17Id.join(","),gid = $(".js-choption option:selected").val();
                                if (changeStr == "" && change17Str == ""){
                                    self.alertDialog({
                                        text:text
                                    });
                                    return ;
                                }
                                if (gid == ""){
                                    self.alertDialog({
                                        text:"暂无教学班群，请更换年级或先去创建教学班群！"
                                    });
                                    return ;
                                }

                                if(index == 1){
                                    data = {
                                        srcGroupId: self.classGroupDetailData().groupId,
                                        targetGroupId: gid,
                                        klxUserIds: changeStr,
                                        userIds:change17Str
                                    };
                                }else{
                                    data = {
                                        groupId: gid,
                                        klxUserIds: changeStr,
                                        userIds:change17Str
                                    }
                                }
                                $.ajax({
                                    url: url,
                                    type: 'POST',
                                    data: data,
                                    success: function (res) {
                                        var changeArr = [];
                                        var changeId = $(".js-clazzList").find("li");
                                        $(changeId).each(function(){
                                            var changeObj = {
                                                klxUserId:$(this).attr("dataid"),
                                                a17id:$(this).attr("a17id"),
                                                studentName:$(this).attr("name")
                                            };
                                            changeArr.push(changeObj);
                                        });

                                        if (res.success){
                                            if(index == 1){

                                            }else{
                                                if(res.teachingClassExistStudents && res.teachingClassExistStudents.length != 0){
                                                    var tces = res.teachingClassExistStudents;
                                                    var repeatedNames = [];
                                                    for(var i=0;i<tces.length;i++){
                                                        for(var j=0;j<changeArr.length;j++){
                                                            if(changeArr[j].klxUserId == tces[i] || changeArr[j].a17id == tces[i]){
                                                                repeatedNames.push(changeArr[j].studentName);
                                                            }
                                                        }
                                                    }
                                                    result += "<br><p style='text-align: center;'>以下学生账号在教学班已存在，无需复制："+repeatedNames.join("、")+"</p>";
                                                }
                                            }
                                            $.prompt(result, {
                                                title: "系统提示",
                                                buttons: {"知道了": true},
                                                submit: function () {
                                                    setTimeout(function () {
                                                        // self.getGroupDetailData();
                                                        // self.getAllClazzInfo();

                                                        var nowGradeInfo = self.gradeMenu().gradeInfo();
                                                        // var nowClazzInfo = self.gradeMenu().clazzInfo();
                                                        self.getAllClazzInfo(function () {
                                                            // self.gobackNowGradeOrClass(nowGradeInfo.gradeId, nowClazzInfo, self.gradeMenu().menu);
                                                            self.showCardDetail(nowGradeInfo.gradeId, self.cardDetailData())
                                                        });
                                                    }, 200);
                                                }
                                            });
                                        }else{
                                            if(index == 1){
                                                if(res.dupKlxUserIds && res.dupKlxUserIds.length !=0){
                                                    var dupKlxUserIds = res.dupKlxUserIds;
                                                    var repeatedNames = [],repeatedStr = "";
                                                    for(var i=0;i<dupKlxUserIds.length;i++){
                                                        for(var j=0;j<changeArr.length;j++){
                                                            if(changeArr[j].klxUserId == dupKlxUserIds[i] || changeArr[j].a17id == dupKlxUserIds[i]){
                                                                repeatedNames.push(changeArr[j].studentName);
                                                            }
                                                        }
                                                    }
                                                    for (var i=0;i<repeatedNames.length;i++){
                                                        $(changeId).each(function(){
                                                            if ($(this).text() == repeatedNames[i]){
                                                                $(this).find(".mode").addClass("redbg");
                                                            }
                                                        });
                                                    }
                                                    if (repeatedNames.length >= 3){
                                                        var repeatedThree = [];
                                                        for (var i=0;i<3;i++){
                                                            repeatedThree.push(repeatedNames[i]);
                                                        }
                                                        repeatedStr = repeatedThree.join("、");
                                                        repeatedStr = repeatedStr + "" + "...";
                                                    }else{
                                                        repeatedStr = repeatedNames.join("、");
                                                    }
                                                    changeClazzMode.changeClazzData().errorInfo("学生（"+repeatedStr+"）在新班级有重名学生，请核对信息或修改姓名");
                                                }else{
                                                    changeClazzMode.changeClazzData().errorInfo(res.info?res.info:"好像出错咯，请稍后再试");
                                                }
                                            }else{
                                                if(res.dupKlxUserNotMatchId && res.dupKlxUserNotMatchId.length !=0){
                                                    var dupKlxUserNotMatchId = res.dupKlxUserNotMatchId;
                                                    var repeatedNames = [],repeatedStr = "";
                                                    for (var i=0;i<dupKlxUserNotMatchId.length;i++){
                                                        for (var j=0;j<changeArr.length;j++){
                                                            if (changeArr[j].klxUserId == dupKlxUserNotMatchId[i] || changeArr[j].a17id == dupKlxUserNotMatchId[i]){
                                                                repeatedNames.push(changeArr[j].studentName);
                                                            }
                                                        }
                                                    }

                                                    for (var i=0;i<repeatedNames.length;i++){
                                                        $(changeId).each(function(){
                                                            if ($(this).text() == repeatedNames[i]){
                                                                $(this).find(".mode").addClass("redbg");
                                                            }
                                                        });
                                                    }

                                                    if (repeatedNames.length >= 3){
                                                        var repeatedThree = [];
                                                        for (var i=0;i<3;i++){
                                                            repeatedThree.push(repeatedNames[i]);
                                                        }
                                                        repeatedStr = repeatedThree.join("、");
                                                        repeatedStr = repeatedStr + "" + "...";
                                                    }else{
                                                        repeatedStr = repeatedNames.join("、");
                                                    }
                                                    changeClazzMode.changeClazzData().errorInfo("学生（"+repeatedStr+"）在教学班中有重名学生，请核对信息或修改姓名");
                                                }else{
                                                    changeClazzMode.changeClazzData().errorInfo(res.info?res.info:"好像出错咯，请稍后再试");
                                                }
                                            }
                                        }
                                    },
                                    error: function (e) {
                                        self.alertDialog({
                                            text:"好像出错咯，请稍后再试"
                                        });
                                    }
                                });
                            }
                        }
                    });

                }
            },
            //编辑学生
            editCgs: function (data, event) {
                var _data = data;
                var $thisNode = $(event.currentTarget);
                var EditStudentInfoModal = function () {
                    var _this = this;
                    _this.editStudentInfoData = ko.observable({
                        studentName:ko.observable(_data.studentName),
                        studentNum:ko.observable(_data.studentNum),
                        scanNum:ko.observable(_data.scanNum),
                        errorText:ko.observable(),
                        isTransientStu: ko.observable($thisNode.attr('data-ismarked')),
                        checkTransient: function () {
                            // 是否是借读生
                            _this.editStudentInfoData().isTransientStu(!(_this.editStudentInfoData().isTransientStu()));
                        }
                    });
                };
                var editStudentInfoMode = new EditStudentInfoModal();
                var editStudentInfoHtml = "<div id=\"editStudentInfoContent\" data-bind=\"template: { name: 'editStudentInfo', data: editStudentInfoData }\"></div>";
                $.prompt(editStudentInfoHtml, {
                    focus : 1,
                    title: "更新" + data.studentName + "的资料",
                    buttons: {"取消": false,"确定": true },
                    loaded:function () {
                        ko.applyBindings(editStudentInfoMode,document.getElementById("editStudentInfoContent"));
                    },
                    submit : function(e, v){
                        e.preventDefault();
                        if(v){
                            var studentName = $(".student-name").val();
                            var studentNumber = $(".student-number").val();
                            var studentScanNumber = $(".student-scan-number").val();
                            if (YQ.isBlank(studentName) || YQ.isBlank(studentNumber) || YQ.isBlank(studentScanNumber)) {
                                editStudentInfoMode.editStudentInfoData().errorText("您有未输入的信息");
                            } else if (!YQ.isChinaString(studentName)) {//学生姓名是否为纯汉字，不符合则提示——请输入正确的学生姓名
                                editStudentInfoMode.editStudentInfoData().errorText("请输入正确的学生姓名");
                            } else if (studentName.length > 16) {//姓名是否<=16个字符，不符合则提示——填写的学生名过长
                                editStudentInfoMode.editStudentInfoData().errorText("填写的学生名过长");
                            } else if (!YQ.isNumber(studentNumber)) {//校内学号是否为纯数字，不符合则提示——请输入纯数字学号
                                editStudentInfoMode.editStudentInfoData().errorText("请输入纯数字学号");
                            } else if (studentNumber.length > 14) {//校内学号是否<=14个数字，不符合则提示——填写的校内学号过长
                                editStudentInfoMode.editStudentInfoData().errorText("填写的校内学号过长");
                            } else if (!YQ.isNumber(studentScanNumber)) {
                                editStudentInfoMode.editStudentInfoData().errorText("请输入纯数字阅卷机号");
                            } else {
                                var data = {
                                    clazzId: self.classGroupDetailData().clazzId,
                                    a17id: _data.a17id || '',
                                    klxUserId: _data.klxUserId,
                                    studentName: studentName,
                                    studentNum: studentNumber,
                                    scanNum: studentScanNumber,
                                    isMarked: editStudentInfoMode.editStudentInfoData().isTransientStu()
                                };
                                $.ajax({
                                    url: '/specialteacher/editstudentinfo.vpage',
                                    type: 'POST',
                                    data: data,
                                    success: function (res) {
                                        if (res.success){
                                            $.prompt("修改成功", {
                                                title: "系统提示",
                                                buttons: {"知道了": true},
                                                submit: function () {
                                                    setTimeout(function () {
                                                        // location.reload()
                                                        self.getGroupDetailData();
                                                    }, 200);
                                                }
                                            });
                                        }else{
                                            self.alertDialog({
                                                text:res.info?res.info:"好像出错咯，请稍后再试"
                                            });
                                        }
                                    },
                                    error: function (e) {
                                        self.alertDialog({
                                            text:"好像出错咯，请稍后再试"
                                        });
                                    }
                                });
                            }
                        }else{
                            $.prompt.close();
                        }
                    }
                });
            },
            // 修改密码
            changePassword: function (clazzId, a17id, password, confirmPassword) {
                var data = {
                    clazzId         : clazzId,
                    a17id          : a17id,
                    password        : password,
                    confirmPassword : confirmPassword
                };
                $.ajax({
                    url: '/specialteacher/resetstudentpassword.vpage',
                    type: 'POST',
                    data: data,
                    success: function (data) {
                        if (data.success) {
                            $.prompt("修改成功", {
                                title: "系统提示",
                                buttons: {"知道了": true},
                                submit: function () {
                                    setTimeout(function () {
                                        self.getGroupDetailData();
                                    }, 200);
                                }
                            });
                        } else {
                            $.prompt(data.info, {
                                title: "系统提示",
                                buttons: {"知道了": true}
                            });
                        }
                    }
                });

            },
            // 重置密码
            resetCgs: function (data) {
                // 重置密码
                if (!YQ.isBlank(data.studentMobile) && data.studentMobile) {
                    $.prompt("<p style='line-height:30px;'>正在帮<span class='red'>" + data.studentName + "</span>同学重置密码，请取得学生或家长同意后操作。<br/>密码将发送至学生家长手机：<span class='red'>" + data.studentMobile + "</span></p>", {
                        title   : "系统提示",
                        focus   : 1,
                        buttons : { "取消": false, "确定": true },
                        position: {width: 700},
                        submit  : function(e, v){
                            if(v){
                                e.preventDefault();
                                self.changePassword(self.classGroupDetailData().clazzId, data.a17id,"","");
                            }
                        }
                    });
                } else {
                    var resetPasswordModal = function () {
                        var _this = this;
                        _this.resetPasswordData = ko.observable({
                            errorText:ko.observable()
                        });
                    };
                    var resetPasswordMode = new resetPasswordModal();
                    var resetPasswordHtml = "<div id=\"resetPasswordContent\" data-bind=\"template: { name: 'resetPassword',data:resetPasswordData}\"></div>";
                    $.prompt(resetPasswordHtml, {
                        title   : "重置学生的登录密码",
                        focus   : 1,
                        buttons : { "取消": false, "确定": true },
                        loaded  : function () {
                            ko.applyBindings(resetPasswordMode, document.getElementById("resetPasswordContent"));
                        },
                        submit  : function(e, v){
                            if(v){
                                e.preventDefault();
                                if(YQ.isBlank($(".v-password").val()) || YQ.isBlank($(".v-confirmPassword").val())){
                                    resetPasswordMode.resetPasswordData().errorText("您有未输入的信息");
                                }else if($(".v-password").val() != $(".v-confirmPassword").val()){
                                    $(".v-password, .v-confirmPassword").val("");
                                    resetPasswordMode.resetPasswordData().errorText("您输入的密码不一致");
                                }else{
                                    self.changePassword(self.classGroupDetailData().clazzId, data.a17id,$(".v-password").val(),$(".v-confirmPassword").val() );
                                }
                            }
                        }
                    });
                }
            },
            // 删除学生
            delCgs: function (data) {
                $.prompt("删除学生将会导致学生的扫描记录、学情数据丢失。<br>你确定要删除该学生吗？<br><br>注：如学生填涂号更新，可单个编辑或通过【添加学生账号】批量更新；如学生需要换班或加入新班级，可通过【校内打散换班】功能实现。", {
                    title   : "系统提示",
                    focus   : 1,
                    buttons : { "取消": false, "确定": true },
                    submit  : function(e, v){
                        if(v){
                            var postData = {
                                clazzId: self.classGroupDetailData().clazzId,
                                groupId: self.classGroupDetailData().groupId,
                                a17id: data.a17id || "",
                                klxUserId: data.klxUserId
                            };
                            $.post("/specialteacher/deletestudent.vpage", postData, function(data){
                                if(data.success){
                                    setTimeout(function(){
                                        self.getGroupDetailData();
                                    }, 200);
                                } else {
                                    self.alertDialog({
                                        text:res.info?res.info:"删除学生失败！"
                                    });
                                }
                            });
                        }
                    }
                });
            },
            // 删除班群
            delGroup: function (data) {
                var deleteGroupDialogModal = function () {
                    var _this = this;
                    _this.deleteGroupDialogData = ko.observable({
                        delGroupCause: ko.observableArray(['错误、重复班群', '非我校老师']),
                        delGroupValue: ko.observable(''),
                        delGroupErrorText: ko.observable(''),
                        delGroupErrorText2: ko.observable('123'),
                        selectDeleteGroupCause: function () {
                            _this.deleteGroupDialogData().delGroupErrorText('');
                        }
                    });
                };
                var deleteGroupDialogMode = new deleteGroupDialogModal();
                var deleteGroupDialogHtml = "<div id=\"deleteGroupDialogContent\" data-bind=\"template: { name: 'deleteGroupDialog', data: deleteGroupDialogData}\"></div>";
                var deleteGroupStates = {
                    state0: {
                        html: deleteGroupDialogHtml,
                        title: '删除班群',
                        buttons: { "取消": false, "删除": true },
                        focus: 1,
                        submit: function (e, v) {
                            e.preventDefault(); // 阻止按钮默认事件
                            if (v) {
                                if (!deleteGroupDialogMode.deleteGroupDialogData().delGroupValue()) {
                                    deleteGroupDialogMode.deleteGroupDialogData().delGroupErrorText('请选择删除原因');
                                    return false;
                                }
                                $.prompt.goToState('state1');
                            } else {
                                $.prompt.close();
                            }
                        }
                    },
                    state1: {
                        html: "<p>删除后学生填涂号将被清空、考试数据将丢失，<br>如果班内有英语老师名下学生，英语作业记录也将被删除！</p>",
                        title: '删除班群',
                        buttons: {"取消": false, "确认删除": true},
                        focus: 0,
                        submit: function (e, v) {
                            e.preventDefault(); // 阻止按钮默认事件
                            if (v) {
                                $.ajax({
                                    url: '/specialteacher/clazz/deletegroup.vpage',
                                    type: 'POST',
                                    data: {groupId: data.groupId, desc: deleteGroupDialogMode.deleteGroupDialogData().delGroupValue()},
                                    success: function (data) {
                                        if (data.success) {
                                            var nowGradeInfo = self.gradeMenu().gradeInfo();
                                            self.adjustCardCallback(nowGradeInfo, '', '您已成功删除该班群', true);
                                        } else {
                                            $.prompt(data.info, {
                                                title: '系统提示',
                                                buttons: {'确定': false}
                                            })
                                        }
                                    }
                                });
                            } else {
                                $.prompt.goToState('state0');
                            }
                        }
                    }
                };
                $.prompt(deleteGroupStates);
                self.fakePromptLoaded(function () {
                    ko.applyBindings(deleteGroupDialogMode, document.getElementById("deleteGroupDialogContent"));
                });
            },
            alertDialog:function (option) {
                var title = option.title?option.title:"提示",
                    type = type ? option.type : "sure",
                    button  = option.button || {},
                    position = option.position?option.position:{};
                if(type == "sure") {
                    button = {"确认":true};
                }else if(type == "confirm"){
                    button = {"取消": false,"确认":true};
                }

                if(button){
                    button = button;
                }

                $.prompt(option.text?option.text:"", {
                    title: title,
                    buttons: button,
                    position:position,
                    submit:function(e,v){
                        if(v){
                            if(typeof(option.callback) == "function"){
                                option.callback();
                            }
                        }
                    }
                });
            },

            fakePromptLoaded:function (callBack) {
                var eventLoop = setInterval(function () {
                    var dialogNodes = $(".jqibox");
                    if(dialogNodes.length != 0 ){
                        clearInterval(eventLoop);
                        eventLoop = null;
                        if(callBack && typeof(callBack) === "function"){
                            callBack();
                        }
                    }
                },100);
            }
        });
        self.gradeMenu({
            hasData:false,
            menu:[],
            gradeInfo:'',
            clazzInfo:''
        });
        self.getAllClazzInfo();
        self.getEaxmGroupManager();
    };

    var gModal = new GradeManageModal();
    ko.applyBindings(gModal,document.getElementById("page_bd"));

    // var oldClassName = "";
    // $(document).on("click",".js-editClassNameBtn",function () { // TODO 批量绑定并改变附近元素的方式确实不适合MVVM来实现
    //     $(this).parent("div").hide().siblings(".js-editClassNameItem").show();
    //     var cname = $(this).data('cname');
    //     oldClassName = cname;
    //     $(this).parent("div").siblings(".js-editClassNameItem").find('input').val(cname);
    //     $(this).parents("li").addClass("active");
    // }).on("click",".js-editClassNameSureBtn",function () {
    //     var cid = $(this).data('cid');
    //     var cName = $(this).siblings('input').val().trim();
    //
    //     if(!cName){
    //         gModal.alertDialog({
    //             text:"请填写班级名称"
    //         });
    //         return false;
    //     }else{
    //         if(oldClassName === cName){
    //             gModal.alertDialog({
    //                 text:"新老名称一致，请填写新的班级名称"
    //             });
    //             return false;
    //         }
    //         var classNameNodes = $(this).parents("ul.clazzList").find("span.js-classNameItem");
    //         var hasSameFlag = false;
    //         for(var i=0;i<classNameNodes.length;i++){
    //             if($(classNameNodes[i]).text().trim() == cName){
    //                 hasSameFlag = true;
    //                 break;
    //             }
    //         }
    //         if(hasSameFlag){
    //             gModal.alertDialog({
    //                 text:"新修改的班级名称已存在，请填写新的班级名称"
    //             });
    //             return false;
    //         }
    //     }
    //
    //     $.ajax({
    //         url:"/specialteacher/clazz/adjustclazz.vpage",
    //         type:"post",
    //         data:{
    //             clazzId:cid,
    //             type:1,
    //             clazzName:cName
    //         },
    //         success:function (res) {
    //             if(res.success){
    //                 gModal.alertDialog({
    //                     text:"更名成功,请选择到对应年级查看",
    //                     callback:function () {
    //
    //                     }
    //                 });
    //                 gModal.getAllClazzInfo(function () {
    //                     $('.adjust').click(); //点击调整班级
    //                 });
    //             }else{
    //                 gModal.alertDialog({
    //                     text:res.info?res.info:"好像出错咯，请稍后重试"
    //                 })
    //             }
    //         },
    //         error:function (e) {
    //             console.log(e);
    //         }
    //     });
    // }).on("click",".js-removeClassItemBtn",function () {
    //     var cid = $(this).data('cid');
    //     var cName = $(this).data('cname');
    //     $.prompt("确定要删除"+cName+"吗？",{
    //         title:"系统提示",
    //         position:{width:300},
    //         buttons: {'取消':false,'确定':true},
    //         submit:function(e,v){
    //             if(v){
    //                 $.ajax({
    //                     url:"/specialteacher/clazz/adjustclazz.vpage",
    //                     type:"post",
    //                     data:{
    //                         clazzId:cid,
    //                         type:2,
    //                         clazzName:cName
    //                     },
    //                     success:function (res) {
    //                         if(res.success){
    //                             gModal.alertDialog({
    //                                 text:"删除成功,请选择到对应年级查看",
    //                                 callback:function () {
    //                                 }
    //                             });
    //                             gModal.getAllClazzInfo(function () {
    //                                 $('.adjust').click(); //点击调整班级
    //                             });
    //                         }else{
    //                             gModal.alertDialog({
    //                                 text:res.info?res.info:"好像出错咯，请稍后重试"
    //                             })
    //                         }
    //                     },
    //                     error:function (e) {
    //                         console.log(e);
    //                     }
    //                 });
    //             }else{
    //                 $.prompt.close();
    //             }
    //         }
    //
    //     })
    // });

    // 初始化请求
    // var initMenu = function (callBack) {
    //     var menuEventLoop = setInterval(function () {
    //         var dialogNodes = $("a.js-classMenuItem");
    //         if(dialogNodes.length != 0 ){
    //             $($(".js-classMenuItem")[0]).click();
    //             clearInterval(menuEventLoop);
    //             menuEventLoop = null;
    //             if(callBack && typeof(callBack) === "function"){
    //                 callBack();
    //             }
    //         }
    //     },100);
    // };
    // initMenu();
});