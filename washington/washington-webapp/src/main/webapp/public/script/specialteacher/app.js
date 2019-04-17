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
                teachingClass: []
            }),
            clazzCardListData: ko.observable({
                groups: [],
                gradeId:""
            }),
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

            //获取接口数据
            getAllClazzInfo: function (callback) {
                $.get('manageclazz.vpage', function (res) {
                    if(res.success){
                        self.gradeClassInfo(res);
                        self.gradeMenu({hasData: true, menu: res.menu,adjustClazz:res.adjustClazz});
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

            //点击年级菜单
            toggleGradeMenu: function (index, data, event) {
                self.isShowMerge(0);
                self.mergeSelectList([]);
                self.isTeachingClazz(0);

                self.displayOneTemp(1);
                self.currentGrade(data.gradeId);
                var _index = index(); //click传值很奇怪 TODO
                self.gradeCardListData({
                    administrativeClass: data.administrativeClass,
                    gradeId:data.gradeId,
                    teachingClass: data.teachingClass
                });

                $(".adjust").removeClass("active");
                //子菜单收起
                var $thisNode = $(event.currentTarget);
                $thisNode.next('.js-subMenuItem').find('.js-classMenuItem').removeClass('active');
                if ($thisNode.hasClass('active')) {
                    $thisNode.removeClass('active').siblings('a.js-classMenuItem').removeClass('active');
                    $($('.js-subMenuItem')[_index]).addClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                } else {
                    $thisNode.addClass('active').siblings('a.js-classMenuItem').removeClass('active');
                    $($('.js-subMenuItem')[_index]).removeClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                }
            },

            // 点击班级菜单
            toggleClassMenu: function (data,event) {
                self.isShowMerge(0);
                self.mergeSelectList([]);

                var $thisNode = $(event.currentTarget),
                    id = $thisNode.parent(".js-subMenuItem").attr("gradeId");
                self.displayOneTemp(2);
                self.currentClassId(data.clazzId);
                self.clazzCardListData({
                    groups: data.groups,
                    gradeId: id
                });
                if(data.groups && data.groups.length !=0){
                    if(data.groups[0].groupType != "TEACHING_GROUP"){
                        self.isTeachingClazz(0);
                    }else{
                        self.isTeachingClazz(1);
                    }
                }
                $thisNode.addClass('active').siblings('.js-classMenuItem').removeClass('active');
                $(".adjust").removeClass("active");
            },

            searchStudent:function () {
                var StudentSearchDialogModal = function () {
                   var _this = this;
                   _this.getSearch = function (key) {
                       var searchUrl = 'searchstudent.vpage';
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

            //班组卡片的查看详情
            showCardDetail: function (gradeId,data,callback) {
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
                    classGroupStudents: [],
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
                                updateNum:ko.observable(0)
                            });
                        };

                        var AddStudentRepeatedErrorModal = function () {
                            var _self = this;
                            _self.addStudentRepeatedErrorData = ko.observable({
                                repeatedNames:ko.observable("")
                            });
                        };

                        var AddStudentTakeUpErrorModal = function () {
                            var _self = this;
                            _self.addStudentTakeUpErrorData = ko.observable({
                                scanNumDigit:self.gradeClassInfo().scanNumDigit,
                                importStudentNames:ko.observable(""),
                                takeUpSutInfo:ko.observable("")
                            });
                        };


                        var addStuDialogModal = new AddStuDialogModal();
                        var onlineAddStudentModal = new OnlineAddStudentModal();
                        var excelAddStudentModal = new ExcelAddStudentModal();
                        var addStudentSuccessModal = new AddStudentSuccessModal();
                        var addStudentRepeatedErrorModal = new AddStudentRepeatedErrorModal();
                        var addStudentTakeUpErrorModal = new AddStudentTakeUpErrorModal();

                        var addStuDialogTempHtml = "<div id=\"addStudentDialog\" data-bind=\"template: { name: 'addStudentDialogTemp', data: addStudentDialogData }\"></div>";
                        var onlineAddStuDialogTempHtml = "<div id=\"onlineAddStudentDialog\" data-bind=\"template: { name: 'onlineAddStudentDialogTemp', data: onlineAddStudentDialogData }\"></div>";
                        var excelAddStudentDialogTempHtml = "<div id=\"excelAddStudentDialog\" data-bind=\"template: { name: 'excelAddStudentDialogTemp', data: excelAddStudentDialogData }\"></div>";
                        var addStudentSuccessTempHtml = "<div id=\"addStudentSuccessDialog\" data-bind=\"template: { name: 'addStudentSuccessTemp', data: addStudentSuccessData }\"></div>";
                        var addStudentRepeatedErrorTempHtml = "<div id=\"addStudentRepeatedErrorDialog\" data-bind=\"template: { name: 'addStudentRepeatedErrorTemp', data: addStudentRepeatedErrorData }\"></div>";
                        var addStudentTakeUpErrorTempHtml = "<div id=\"addStudentTakeUpErrorDialog\" data-bind=\"template: { name: 'addStudentTakeUpErrorTemp', data: addStudentTakeUpErrorData }\"></div>";

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
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                if(result.repeatedStudentList && result.repeatedStudentList.length != 0){ // 重复添加的列表
                                    var repeatedNames = subAllRepeatedNames(result.repeatedStudentList,result.moreStudent);
                                    addStudentRepeatedErrorModal.addStudentRepeatedErrorData().repeatedNames(repeatedNames);
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state4');
                                }else{
                                    validateOnlineTakeUp();
                                }
                            });
                        };

                        //线上添加验证占用
                        var validateOnlineTakeUp = function () {
                            onlineAddStudentData.checkRepeatedStudent = false;
                            onlineAddStudentData.checkTakeUpStudent = true;
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                if(result.isTakeUp){ //被占用
                                    var importStudentNames = subAllRepeatedNames(result.importNames, result.moreFlag);
                                    var takeUpSutInfo = subAllTakeUpInfo(result.takeUpInfo);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().importStudentNames(importStudentNames);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().takeUpSutInfo(takeUpSutInfo);
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state5');
                                }else{
                                    addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                    addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                    addStuDialogModal.addStudentDialogData().backState('state1');
                                    $.prompt.goToState('state3');
                                }
                            });
                        };

                        //线上添加不带验证
                        var onlineAddStuWithoutVal = function () {
                            onlineAddStudentData.checkRepeatedStudent = false;
                            onlineAddStudentData.checkTakeUpStudent = false;
                            submitOnlineAddStu(onlineAddStudentData,function (result) {
                                addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                $.prompt.goToState('state3');
                            });
                        };

                        //在线添加提交
                        var submitOnlineAddStu = function (postData,successCallback) {
                            $.ajax({
                                url:"addstudentsonline.vpage",
                                type:"POST",
                                data:postData,
                                success:function (result) {
                                    if(result.success){
                                        if(successCallback && typeof successCallback === "function"){
                                            successCallback(result);
                                        }
                                    }else{
                                       onlineAddStudentModal.onlineAddStudentDialogData().errorInfo(result.info?result.info:'好像出问题咯！');
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
                                url:"addstudentbyexcel.vpage",
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
                                        excelAddStudentModal.excelAddStudentDialogData().stuExcelTypeError(result.info?result.info:'好像出问题咯！');
                                    }
                                },
                                error:function (e) {
                                    console.log(e);
                                }
                            })
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
                            submitExcelAddStu(excelAddStudentData,function (result) {
                                if(result.repeatedStudentList && result.repeatedStudentList.length != 0){ // 重复添加的列表
                                    var repeatedNames = subAllRepeatedNames(result.repeatedStudentList,result.moreStudent);
                                    addStudentRepeatedErrorModal.addStudentRepeatedErrorData().repeatedNames(repeatedNames);
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state4');
                                }else{
                                    submitExcelValTakeUp();
                                }
                            });
                        };

                        var submitExcelValTakeUp = function () {
                            var innerExcelAddStudentData = new FormData(),postData = "";
                            if(excelAddStudentData.set && typeof(excelAddStudentData.set) === "function"){
                                excelAddStudentData.set("checkRepeatedStudent",false);
                                excelAddStudentData.set("checkTakeUpStudent",true);
                                postData = excelAddStudentData;
                            }else{ //兼容IE等对FormData 实现不彻底的浏览器
                                innerExcelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                                innerExcelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);
                                innerExcelAddStudentData.append("adjustExcel",excelAddStudentModal.excelAddStudentDialogData().stuExcelFile());
                                innerExcelAddStudentData.append("checkRepeatedStudent",false);
                                innerExcelAddStudentData.append("checkTakeUpStudent",true);
                                postData = innerExcelAddStudentData;
                            }
                            submitExcelAddStu(postData,function (result) {
                                if(result.isTakeUp){ //被占用
                                    var importStudentNames = subAllRepeatedNames(result.importNames, result.moreFlag);
                                    var takeUpSutInfo = subAllTakeUpInfo(result.takeUpInfo);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().importStudentNames(importStudentNames);
                                    addStudentTakeUpErrorModal.addStudentTakeUpErrorData().takeUpSutInfo(takeUpSutInfo);
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state5');
                                }else{
                                    addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                    addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                    addStuDialogModal.addStudentDialogData().backState('state2');
                                    $.prompt.goToState('state3');
                                }
                            })
                        };

                        var submitExcelWithoutVal = function () {
                            var innerExcelAddStudentData = new FormData(),postData = "";
                            if(excelAddStudentData.set && typeof(excelAddStudentData.set) === "function"){
                                excelAddStudentData.set("checkRepeatedStudent",false);
                                excelAddStudentData.set("checkTakeUpStudent",false);
                                postData = excelAddStudentData;
                            }else{ //兼容IE等对FormData 实现不彻底的浏览器
                                innerExcelAddStudentData.append("clazzId",onlineAddStudentModal.onlineAddStudentDialogData().selectedClazzId);
                                innerExcelAddStudentData.append("teacherId",onlineAddStudentModal.onlineAddStudentDialogData().selectedTeacherId);
                                innerExcelAddStudentData.append("adjustExcel",excelAddStudentModal.excelAddStudentDialogData().stuExcelFile());
                                innerExcelAddStudentData.append("checkRepeatedStudent",false);
                                innerExcelAddStudentData.append("checkTakeUpStudent",false);
                                postData = innerExcelAddStudentData;
                            }
                            submitExcelAddStu(postData,function (result) {
                                addStudentSuccessModal.addStudentSuccessData().newSignNum(result.newSignNum || 0);
                                addStudentSuccessModal.addStudentSuccessData().updateNum(result.updateNum || 0);
                                $.prompt.goToState('state3');
                            });
                        };

                        var addStuStates = {
                            state0: {
                                html:addStuDialogTempHtml,
                                title:"添加学生账号",
                                buttons: { },
                                focus: 1,
                                submit:function(e,v,m,f){

                                }
                            },
                            state1: {
                                html:onlineAddStuDialogTempHtml,
                                title:"在线添加学生账号",
                                buttons: { "取消": false, "确定": true },
                                position: {width: 640},
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        var onlineAddText = onlineAddStudentModal.onlineAddStudentDialogData().onlineText();
                                        if(validateOnlineAddData(onlineAddText)){
                                            validateOnlineRepeat(onlineAddText);
                                        }
                                    }else{
                                        $.prompt.goToState('state0');
                                    }
                                }
                            },
                            state2: {
                                html:excelAddStudentDialogTempHtml,
                                title:"通过excel添加账号",
                                buttons: { "取消": false, "确定": true },
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        if(validateExcelAddData()){
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
                                focus: 0,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        $.prompt.close();
                                        self.getGroupDetailData();
                                        self.getAllClazzInfo();
                                    }
                                }
                            },
                            state4: {//重复
                                html:addStudentRepeatedErrorTempHtml,
                                title:"系统提示",
                                buttons: { "取消": false, "确定更新学生信息": true },
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
                                focus: 1,
                                submit:function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        // 提示了占用之后，直接更新
                                        if(addStuDialogModal.addStudentDialogData().backState() == "state1"){
                                            onlineAddStuWithoutVal();
                                        }else if(addStuDialogModal.addStudentDialogData().backState() == "state2"){
                                            submitExcelWithoutVal();
                                        }
                                    }else{
                                        addStuDialogModal.addStudentDialogData().backToState();
                                    }
                                }
                            }
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
                    self.showCardDetail(classGroup.gradeId,classGroup.group,function () {
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
                    self.showCardDetail(classGroup.gradeId,classGroup.group);
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
                            url:"searchteacher.vpage",
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
                        var adjustTeacherUrl = "adjustteacherpre.vpage";
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
                            if(clazzType != "TEACHING_GROUP"){ //过滤教学班
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
                                            self.getAllClazzInfo(function () { //新增会返回新的groupId
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
                                            self.getAllClazzInfo(function () { //调整沿用老的groupId
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
                                    self.getAllClazzInfo(function () {
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
                            if (subjectTeacherArray[n].subject === adjustSubject) {
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
                        url:"addnewgroup.vpage",
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
                var submitAdjustTeacher = function (cid,tid,callBack) {
                    // 修改老师
                    var oldTeacher=adjustTid;
                    var adjustTeacherUrl = "adjustteacher.vpage";
                    if(self.addOrAdjustFlag() == "adjust"){ // 新增老师
                        oldTeacher = "";
                        adjustTeacherUrl = "addteacher.vpage";
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
                        url: "delteacher.vpage",
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

                var $thisNode = $(event.currentTarget);
                if ($thisNode.hasClass('active')) {
                    $thisNode.removeClass('active').siblings('a.js-classMenuItem').removeClass('active');
                    $('.js-subMenuItem').addClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                } else {
                    $thisNode.addClass('active').siblings('a.js-classMenuItem').removeClass('active');
                    $('.js-subMenuItem').removeClass('hidden').siblings('.js-subMenuItem').addClass('hidden');
                }
            },

            changeSelectAdjustGrade: function () {
                var selectedGrade = self.adjustGrade();
                var _adjustClassData = self.adjustClassData();
                if(selectedGrade){
                    _adjustClassData.defaultTemp = 0;
                    _adjustClassData.aClassList = selectedGrade.aClassList;
                    _adjustClassData.tClassList = selectedGrade.tClassList;
                }else{
                    _adjustClassData.defaultTemp = 1;
                }
                self.adjustClassData(_adjustClassData);
            },

            //创建校内班级
            createSchoolClass:function () {
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
                        hasTeacherList:ko.observable(0),
                        walkingTeacher:ko.observable(),
                        walkingTeachers:ko.observable(),
                        errorInfo:ko.observable(""),
                        changeClassType:function () {
                            if(_self.createSchoolClassData().classType() && _self.createSchoolClassData().classType().type == "3"){
                                _self.createSchoolClassData().showTeachingSubject(1);
                                _self.createSchoolClassData().showTeachingTeacher(1);
                            }else{
                                _self.createSchoolClassData().showTeachingSubject(0);
                                _self.createSchoolClassData().showTeachingTeacher(0);
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
                                url:"adjustteacherpre.vpage",
                                type:"GET",
                                data:{
                                    subject:subject
                                },
                                success:function (res) {
                                    if(res.success){
                                        if(res.teacherList && res.teacherList.length != 0){
                                            _self.createSchoolClassData().walkingTeachers(res.teacherList);
                                        }
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
                                walkingTeacherId:_self.createSchoolClassData().walkingTeacher()?_self.createSchoolClassData().walkingTeacher().teacherId:""
                            };
                            if(validateCreateData(postData)){
                                $.ajax({
                                    url:"addnewclazz.vpage",
                                    type:"post",
                                    data:postData,
                                    success:function (res) {
                                        if (res.success){
                                            $.prompt.goToState('state1');
                                            self.getAllClazzInfo(function () {
                                                $('.adjust').click();
                                            });
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
                        createSchoolClassModal.createSchoolClassData().errorInfo("请选择任课老师");
                        return false;
                    }
                    return true;
                };

                var createSchoolClassModal = new CreateSchoolClassModal();
                var createSchoolClassTempHtml = "<div id=\"createSchoolClassDialog\" data-bind=\"template: { name: 'createSchoolClassTemp', data: createSchoolClassData }\"></div>";
                var createSchoolClassStates = {
                    state0: {
                        html:createSchoolClassTempHtml,
                        title:"新增班级",
                        buttons: { "取消": false, "确定": true },
                        focus: 1,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            if(v){
                                createSchoolClassModal.createSchoolClassData().createSchoolClassSubmit();
                            }else{
                                $.prompt.close();
                            }
                        }
                    },
                    state1: {
                        html:"<p style='text-align: center;'>新增班级成功,请选择到对应年级查看</p>",
                        title:"系统提示",
                        buttons: { "确定": true },
                        position: {width: 400},
                        focus: 0,
                        submit:function(e,v,m,f){
                            e.preventDefault();
                            if(v){
                                $.prompt.close();
                            }
                        }
                    }
                };

                $.prompt(createSchoolClassStates);

                self.fakePromptLoaded(function () {
                    createSchoolClassModal.createSchoolClassData().getWalkingTeachers(createSchoolClassModal.createSchoolClassData().classSubject());
                    // 一次完成弹窗内的所有modal绑定
                    ko.applyBindings(createSchoolClassModal,document.getElementById("createSchoolClassDialog"));
                });
            },

            getGroupDetailData: function () {
                $.ajax({
                    url: 'groupdetail.vpage',
                    type: 'POST',
                    data:{groupIds: JSON.stringify(self.classGroupDetailData().groupIds)},
                    success: function (res) {
                        var cgd = self.classGroupDetailData();
                        cgd.classGroupStudents = res.students || [];
                        self.classGroupDetailData(cgd);
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
            // 转班和复制到教学班操作
            changeCgsBtn:function (index) {
                var text = "",title = "",url = "",result = "",tipText,data = {};
                if(index == 1){
                    text = "您还没有选择要转班的学生哦！";
                    title = "转班";
                    url = "changeclazz.vpage";
                    result = "转班成功！";
                    tipText = "转移";
                }else{
                    text = "您还没有选择要复制到教学班的学生哦！";
                    title = "复制到教学班";
                    url = "copytoteachingclazz.vpage";
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
                                                        self.getGroupDetailData();
                                                        self.getAllClazzInfo();
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
                                            text:res.info?res.info:"好像出错咯，请稍后再试"
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
                    position: {width: 700},
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
                            } else if (studentName.length > 16) {//姓名是否<=12个字符，不符合则提示——填写的学生名过长
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
                                    url: 'editstudentinfo.vpage',
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
                                            text:res.info?res.info:"好像出错咯，请稍后再试"
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
                    url: 'resetstudentpassword.vpage',
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
            // 充值密码
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
                        title   : "重置学生一起作业的登录密码",
                        focus   : 1,
                        buttons : { "取消": false, "确定": true },
                        position: {width: 700},
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
                $.prompt("你确定要删除" + (data.studentName || "此") + "学生吗？", {
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
                            $.post("deletestudent.vpage", postData, function(data){
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
                                            $.prompt.goToState('state2');
                                            $('.jqiclose').hide();
                                        } else {
                                            $('.jqiclose').hide();
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
                    },
                    state2: {
                        html: "您已成功删除该班群",
                        title: "删除班群",
                        buttons: {"确定":true},
                        submit: function (e, v) {
                            e.preventDefault();
                            if (v) {
                                $.prompt.close();
                                window.location.reload();
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
        self.gradeMenu({hasData:true,menu:[]});
        self.getAllClazzInfo();
    };

    var gModal = new GradeManageModal();
    ko.applyBindings(gModal,document.getElementById("koApp"));

    var oldClassName = "";
    $(document).on("click",".js-editClassNameBtn",function () { // TODO 批量绑定并改变附近元素的方式确实不适合MVVM来实现
        $(this).parent("div").hide().siblings(".js-editClassNameItem").show();
        var cname = $(this).data('cname');
        oldClassName = cname;
        $(this).parent("div").siblings(".js-editClassNameItem").find('input').val(cname);
        $(this).parents("li").addClass("active");
    }).on("click",".js-editClassNameSureBtn",function () {
        var cid = $(this).data('cid');
        var cName = $(this).siblings('input').val().trim();

        if(!cName){
            gModal.alertDialog({
                text:"请填写班级名称"
            });
            return false;
        }else{
            if(oldClassName === cName){
                gModal.alertDialog({
                    text:"新老名称一致，请填写新的班级名称"
                });
                return false;
            }
            var classNameNodes = $(this).parents("ul.clazzList").find("span.js-classNameItem");
            var hasSameFlag = false;
            for(var i=0;i<classNameNodes.length;i++){
                if($(classNameNodes[i]).text().trim() == cName){
                    hasSameFlag = true;
                    break;
                }
            }
            if(hasSameFlag){
                gModal.alertDialog({
                    text:"新修改的班级名称已存在，请填写新的班级名称"
                });
                return false;
            }
        }

        $.ajax({
            url:"/specialteacher/clazz/adjustclazz.vpage",
            type:"post",
            data:{
                clazzId:cid,
                type:1,
                clazzName:cName
            },
            success:function (res) {
                if(res.success){
                    gModal.alertDialog({
                        text:"更名成功,请选择到对应年级查看",
                        callback:function () {

                        }
                    });
                    gModal.getAllClazzInfo(function () {
                        $('.adjust').click(); //点击调整班级
                    });
                }else{
                    gModal.alertDialog({
                        text:res.info?res.info:"好像出错咯，请稍后重试"
                    })
                }
            },
            error:function (e) {
                console.log(e);
            }
        });
    }).on("click",".js-removeClassItemBtn",function () {
        var cid = $(this).data('cid');
        var cName = $(this).data('cname');
        $.prompt("确定要删除"+cName+"吗？",{
            title:"系统提示",
            position:{width:300},
            buttons: {'取消':false,'确定':true},
            submit:function(e,v){
                if(v){
                    $.ajax({
                        url:"/specialteacher/clazz/adjustclazz.vpage",
                        type:"post",
                        data:{
                            clazzId:cid,
                            type:2,
                            clazzName:cName
                        },
                        success:function (res) {
                            if(res.success){
                                gModal.alertDialog({
                                    text:"删除成功,请选择到对应年级查看",
                                    callback:function () {
                                    }
                                });
                                gModal.getAllClazzInfo(function () {
                                    $('.adjust').click(); //点击调整班级
                                });
                            }else{
                                gModal.alertDialog({
                                    text:res.info?res.info:"好像出错咯，请稍后重试"
                                })
                            }
                        },
                        error:function (e) {
                            console.log(e);
                        }
                    });
                }else{
                    $.prompt.close();
                }
            }

        })
    });

    var initMenu = function (callBack) {
        var menuEventLoop = setInterval(function () {
            var dialogNodes = $("a.js-classMenuItem");
            if(dialogNodes.length != 0 ){
                $($(".js-classMenuItem")[0]).click();
                clearInterval(menuEventLoop);
                menuEventLoop = null;
                if(callBack && typeof(callBack) === "function"){
                    callBack();
                }
            }
        },100);
    };

    initMenu();
});