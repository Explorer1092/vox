/**
 * Created by huihui.li on 2017/7/13.
 */
define(["jquery","knockout","YQ","knockout-switch-case","impromptu"],function($,ko,YQ){

    var teacherStuModal = function () {
        var self = this;
        var menu = ["添加老师账号","为老师建班授课","添加学生账号","校内打散换班","复制教学班学生","标记借读生"];
        $.extend(self, {
            gradeMenu: ko.observable({}),
            Grades:ko.observable([]),
            manageMenu: ko.observable({
                menu: menu
            }),
            codeNumInput:ko.observable(),
            clazzIdInput:ko.observable(),
            printBarShow:ko.observable(false),
            gradeIdIn:ko.observable(),
            clazzsIdIn:ko.observable(),
            mergeStatus: ko.observable(false),
            clazzsIdPrint:ko.observable(),
            printBarGrade:ko.observable(),
            lockUpload:ko.observable(false),
            isTeacherShow:ko.observable(true),

            createClazzShow:ko.observable(false),
            isStudentShow:ko.observable(false),
            changeClazzShow:ko.observable(false),
            copyTeachingShow:ko.observable(false),
            markTransientShow:ko.observable(false),

            importTName:ko.observable("请选择校内老师账号文件"),
            teacherClazzName:ko.observable("请选择为老师建班授课文件"),
            importSName:ko.observable("请选择学生账号文件"),
            changeStudentName:ko.observable("请选择校内打散换班文件"),
            copyStudentName:ko.observable("请选择复制教学班学生文件"),
            markTransientName:ko.observable("请选择标记借读生文件"),

            teacherErrorShow:ko.observable(false),
            teacherErrorText:ko.observable(""),
            creatErrorShow:ko.observable(false),
            creatErrorText:ko.observable(""),
            studentErrorShow:ko.observable(false),
            studentErrorText:ko.observable(""),
            changeStudentErrorShow:ko.observable(false),
            changeStudentErrorText:ko.observable(""),
            copyStudentErrorShow:ko.observable(false),
            copyStudentErrorText:ko.observable(""),
            markTransientErrorShow:ko.observable(false),
            markTransientErrorText:ko.observable(""),

            //获取接口数据
            getAllClazzInfo: function () {
                $.get('/specialteacher/manageclazz.vpage', function (res) {
                    if(res.success){
                        self.gradeMenu({menu: res.menu});
                    }else{
                        self.alertDialog({
                            text:res.info?res.info:"好像出错咯，请稍后再试"
                        });
                    }
                });
                $.get('/specialteacher/batchgeneratebarcodecheck.vpage', function (res) {
                    if(res.success){
                        self.printBarShow(true);
                    }else{
                        self.printBarShow(false);
                    }
                });
            },
            gradeAllList:function () {
                var gradeMenu = self.gradeMenu().menu,
                    GradeArray = [];
                if (typeof gradeMenu === 'undefined') return GradeArray;
                for(var i=0;i<gradeMenu.length;i++){
                    var clazzArray = [];
                    for(var j=0;j<gradeMenu[i].clazzs.length;j++){
                        clazzArray.push({
                            clazzName:gradeMenu[i].clazzs[j].clazzName,
                            clazzId:gradeMenu[i].clazzs[j].clazzId,
                            groups:gradeMenu[i].clazzs[j].groups
                        });
                    }
                    GradeArray.push({
                        clazzs:clazzArray,
                        gradeId: gradeMenu[i].gradeId,
                        gradeName: gradeMenu[i].gradeName
                    });
                }
                return GradeArray;
            },
            downLoadStudentExcel:function () {
                // 此处不知道为什么，上面的函数处理数据时偶尔会报错，增加错误弹窗
                var GradeArray = self.gradeAllList();
                if (GradeArray.length === 0) {
                    self.alertDialog({ text: "出错了，请重试！" });
                    return ;
                }
                //  下载学生名单
                var DownloadStudentModal = function () {
                    var _this = this;
                    _this.downloadStudentData = ko.observable({
                        Grades:GradeArray,
                        grade:ko.observable(),
                        clazz:ko.observable(),
                        choiceDownloadStudentStatus: function (data, event) {
                            // 选择下载表格是否合并
                            var $thisNode = $(event.currentTarget).children('i');
                            if ($thisNode.hasClass('active')) {
                                $thisNode.removeClass('active');
                            } else {
                                $thisNode.addClass('active');
                            }
                        },
                    });
                };
                var downloadStudentMode = new DownloadStudentModal();
                var downloadStudentHtml = "<div id=\"downloadStudentContent\" data-bind=\"template: { name: 'downloadStudentList', data: downloadStudentData }\"></div>";
                $.prompt(downloadStudentHtml, {
                    focus: 1,
                    title: "下载学生名单",
                    buttons: {"取消": false, "确定": true},
                    loaded: function () {
                        ko.applyBindings(downloadStudentMode, document.getElementById("downloadStudentContent"));
                    },
                    submit: function (e, v) {
                        if (v) {
                            e.preventDefault();
                            var newGradeId = downloadStudentMode.downloadStudentData().grade().gradeId || "";
                            var newClazzId = "";

                            if ($(".js-allgrade option:selected").text() == '全部班级'){
                                var gradeMenu = self.gradeMenu().menu,clazzArr,newClazzArr = [];

                                for (var i=0;i<gradeMenu.length;i++){
                                    if (newGradeId == gradeMenu[i].gradeId){
                                        for(var j=0;j<gradeMenu[i].clazzs.length;j++){
                                            if (gradeMenu[i].clazzs.length > 0){
                                                newClazzArr.push(gradeMenu[i].clazzs[j].clazzId);
                                            }else{
                                                self.alertDialog({
                                                    text: "该年级没有班级哦！"
                                                });
                                                return ;
                                            }
                                        }
                                        newClazzId = newClazzArr.join(",");
                                    }
                                }
                            }else{
                               if(downloadStudentMode.downloadStudentData().clazz().groups.length > 0){
                                   newClazzId = '' + downloadStudentMode.downloadStudentData().clazz().clazzId;
                               }else{
                                   self.alertDialog({
                                       text: "该班级没有学生哦！"
                                   });
                                   return ;
                               }
                            }
                            self.gradeIdIn(newGradeId);
                            self.clazzsIdIn(newClazzId);
                            if (typeof $('.JS-downloadInfo').css('display') !== 'undefined' &&
                                $('.JS-downloadInfo').children('i').hasClass('active') ) {
                                self.mergeStatus(true);
                            } else {
                                self.mergeStatus(false);
                            }
                            $("#downloadStudents").submit();
                        }
                    }
                });
            },
            printBarCode:function () {
            //    打印学生条形码
                var printBarModal = function () {
                    var _this = this;
                    var GradeArray = self.gradeAllList();
                    _this.printBarData = ko.observable({
                        Grades:GradeArray,
                        grade:ko.observable(),
                        clazz:ko.observable()
                    });

                };
                var printBarMode = new printBarModal();

                var printBarHtml = "<div id=\"printBarContent\" data-bind=\"template: { name: 'printBarStudent', data: printBarData }\"></div>";
                $.prompt(printBarHtml, {
                    focus: 1,
                    title: "打印学生条形码",
                    buttons: {"取消": false, "预览": true},
                    loaded: function () {
                        ko.applyBindings(printBarMode, document.getElementById("printBarContent"));
                    },
                    submit: function (e, v) {
                        if (v) {
                            e.preventDefault();
                            var newClazzId = '' + printBarMode.printBarData().clazz().clazzId;
                            self.clazzsIdPrint(newClazzId);
                            self.confirmPrintBarCommit();
                        }
                    }
                });
            },
            confirmPrintBarCommit:function () {
                var confirmPrintModal = function () {
                    var _this = this;
                    _this.confirmPrintData = ko.observable({
                        scanNumInput:ko.observable(1),
                        makeSureNum:ko.observable(false),
                        printDataNum:ko.observable(1),
                        clazzNameInput:ko.observable(),
                        gradeNum:ko.observable(0),
                        studentNum:ko.observable(0),
                        noScanNum:ko.observable(0),
                        indexScan:ko.observable(1),
                        printTypeItem:function (index,data,event) {
                            var $element = $(event.currentTarget);
                            $element.addClass("s-select").siblings("div").removeClass("s-select");
                            _this.confirmPrintData().indexScan(index);
                        }
                    });
                };
                var confirmPrintMode = new confirmPrintModal();
                var confirmPrintHtml = "<div id=\"confirmPrintContent\" data-bind=\"template: { name: 'confirmPrintTypeTemp', data: confirmPrintData }\"></div>";
                $.get('/specialteacher/getstudentswithoutscannum.vpage',{clazzIds:self.clazzsIdPrint()}, function (res) {
                    if(res.success){
                        confirmPrintMode.confirmPrintData().studentNum(res.studentTotalNum || 0);
                        confirmPrintMode.confirmPrintData().noScanNum(res.studentWithoutScanNumCount || 0);
                    }
                });
                $.prompt(confirmPrintHtml, {
                    focus: 1,
                    title: "批量生成条形码",
                    buttons: {"取消": false, "确定": true},
                    position: {width: 700},
                    loaded: function () {
                        ko.applyBindings(confirmPrintMode, document.getElementById("confirmPrintContent"));
                    },
                    submit: function (e, v) {
                        if (v) {
                            e.preventDefault();
                            if (confirmPrintMode.confirmPrintData().studentNum() <= confirmPrintMode.confirmPrintData().noScanNum()){
                                self.alertDialog({
                                    text: "选中的班级中，有填涂号的学生数为0，请确认后重新操作！"
                                });
                                return ;
                            }
                            var inputValidateFlag = true;
                            confirmPrintMode.confirmPrintData().makeSureNum(false);
                            var val;
                            if (confirmPrintMode.confirmPrintData().indexScan() == 2){
                                val = $('.js-defaultTotalNum').text();
                            }else {
                                val = $('.js-scanNumInput').val();
                            }

                            if(!YQ.isNumber(val) || (YQ.isNumber(val) && (val < 1 || val > 56))){
                                val = 1;
                                confirmPrintMode.confirmPrintData().scanNumInput(1);
                                confirmPrintMode.confirmPrintData().makeSureNum(true);
                                inputValidateFlag = false;
                            }

                            if(inputValidateFlag){
                                self.codeNumInput(val);
                                self.clazzIdInput(self.clazzsIdPrint());
                            }
                            if (!confirmPrintMode.confirmPrintData().makeSureNum()){
                                $("#printCodeBarSubmit").submit();
                            }
                        }
                    }
                });
            },
            toggleGuide:function (data,event) {
                var $element = $(event.currentTarget),
                    $parent = $element.parents(".class-module");
                if ($parent.hasClass("closeGuide")){
                    $parent.removeClass("closeGuide");
                    $element.find("span").text("收起");
                    return ;
                }
                $parent.addClass("closeGuide");
                $element.find("span").text("展开");
            },
            menuClick:function (index, data, event) {
                var $element = $(event.currentTarget);
                $element.addClass("active").siblings().removeClass("active");
                self.displayOneTemp(index);
            },
            downLoadExcel:function (id) {
                var downloadIframe = "<iframe style='display:none;' src='/specialteacher/gettemplate.vpage?template=" + id + "'/>";
                $("body").append(downloadIframe);
            },
            fileUpload:function (index,data,event) {
                var $element = $(event.currentTarget),
                    obj = $element.parent().find(".v-fileupload");
                switch (index) {
                    case 1:
                        self.teacherErrorShow(false);
                        self.teacherErrorText("");
                        break;
                    case 2:
                        self.creatErrorShow(false);
                        self.creatErrorText("");
                        break;
                    case 3:
                        self.studentErrorShow(false);
                        self.studentErrorText("");
                        break;
                    case 4:
                        self.changeStudentErrorShow(false);
                        self.changeStudentErrorText("");
                        break;
                    case 5:
                        self.copyStudentErrorShow(false);
                        self.copyStudentErrorText("");
                        break;
                    case 6:
                        self.markTransientErrorShow(false);
                        self.markTransientErrorText("");
                        break;
                }
                var ie = !-[1,];
                if(ie){
                    $(obj).trigger('click').trigger('change');
                }else{
                    $(obj).trigger('click');
                }

                $(obj).change(function(){
                    // 截掉前面的路径，只留文件名
                    var fileInput = $(obj).val();
                    fileInput = fileInput.substring(fileInput.lastIndexOf("\\") + 1);
                    switch (index) {
                        case 1:
                            self.importTName(fileInput);
                            break;
                        case 2:
                            self.teacherClazzName(fileInput);
                            break;
                        case 3:
                            self.importSName(fileInput);
                            break;
                        case 4:
                            self.changeStudentName(fileInput);
                            break;
                        case 5:
                            self.copyStudentName(fileInput);
                            break;
                        case 6:
                            self.markTransientName(fileInput);
                            break;
                    }
                    $element.addClass("green");
                });
            },
            fileUploadBtn:function (formObj,index,data,event) {
                self.fileUpLoadNoError(index); // 先清空现有报错

                if (self.lockUpload()){
                    return ;
                }
                self.lockUpload(true);
                var $element = $(event.currentTarget),
                    fileInput = $element.parent().find(".v-fileupload"),
                    fileName = fileInput.val(),
                    fileSubmit = "",
                    fileUrl = "",
                    fileSubmitChange = "";
                if (fileName == ""){
                    self.fileUpLoadIndex(index,"您还没有选择excel文档！");
                    self.lockUpload(false);
                    return ;
                }
                if (fileName.substring(fileName.length - 4) != ".xls" && fileName.substring(fileName.length - 5) != ".xlsx") {
                    self.fileUpLoadIndex(index,"仅支持上传excel文档！");
                    self.lockUpload(false);
                    return ;
                }
                switch (index) {
                    case 1:
                        fileSubmit = 'importTeacher';
                        fileUrl = '/specialteacher/admin/checkimportteacher.vpage';
                        break;
                    case 2:
                        fileSubmit = 'teacherClazz';
                        fileUrl = '/specialteacher/admin/createteacherclazz.vpage';
                        break;
                    case 3:
                        fileSubmit = 'importStudent';
                        fileUrl = '/specialteacher/admin/checkimportstudent.vpage';
                        break;
                    case 4:
                        fileSubmit = 'changeStudentData';
                        fileSubmitChange = 'changeStudentType';
                        fileUrl = '/specialteacher/clazz/checkchangeclassdata.vpage';
                        break;
                    case 5:
                        fileSubmit = 'changeStudentData';
                        fileSubmitChange = 'changeStudentType';
                        fileUrl = '/specialteacher/clazz/checklinkclassdata.vpage';
                        break;
                    case 6:
                        fileSubmit = 'markStudents';
                        fileUrl = '/specialteacher/admin/checkmarkstudents.vpage';
                        break;
                }
                var formData = new FormData();
                var file = fileInput[0].files[0];
                formData.append(fileSubmit, file);
                // if (index == 4 ){
                //     formData.append("changeStudentType", "changeclazz");
                // }else if(index == 5){
                //     formData.append("changeStudentType", "linkclazz");
                // }
                $.ajax({
                    url: fileUrl,
                    type: "POST",
                    data: formData,
                    processData: false,
                    contentType: false,
                    async: true,
                    timeout: 5 * 60 * 1000,
                    success: function (data) {
                        self.lockUpload(false);
                        if (data.success) {
                            if (index != 2){
                                $(formObj).submit();
                            }
                            if (index != 1 && index != 3){
                                setTimeout(function () {
                                    $.prompt("操作成功！请在班级管理页面查看", {
                                        focus: 0,
                                        title: "提示",
                                        buttons: {"知道了": true},
                                        submit: function (e, v) {
                                        }
                                    });
                                },1400)
                            }
                        } else {
                            self.fileUpLoadIndex(index,data.info);
                            return ;
                        }
                    },
                    error:function () {
                        self.lockUpload(false);
                    }
                });
            },
            fileUpLoadIndex:function (_index,content) {
                switch (_index) {
                    case 1:
                        self.teacherErrorShow(true);
                        self.teacherErrorText(content);
                        break;
                    case 2:
                        self.creatErrorShow(true);
                        self.creatErrorText(content);
                        break;
                    case 3:
                        self.studentErrorShow(true);
                        self.studentErrorText(content);
                        break;
                    case 4:
                        self.changeStudentErrorShow(true);
                        self.changeStudentErrorText(content);
                        break;
                    case 5:
                        self.copyStudentErrorShow(true);
                        self.copyStudentErrorText(content);
                        break;
                    case 6:
                        self.markTransientErrorShow(true);
                        self.markTransientErrorText(content);
                        break;
                }
            },
            fileUpLoadNoError:function (_index) {
                switch (_index) {
                    case 1:
                        self.teacherErrorShow(false);
                        self.teacherErrorText('');
                        break;
                    case 2:
                        self.creatErrorShow(false);
                        self.creatErrorText('');
                        break;
                    case 3:
                        self.studentErrorShow(false);
                        self.studentErrorText('');
                        break;
                    case 4:
                        self.changeStudentErrorShow(false);
                        self.changeStudentErrorText('');
                        break;
                    case 5:
                        self.copyStudentErrorShow(false);
                        self.copyStudentErrorText('');
                        break;
                    case 6:
                        self.markTransientErrorShow(false);
                        self.markTransientErrorText('');
                        break;
                }
            },
            alertDialog:function (option) {
                var title = option.title?option.title:"提示",
                    type = type ? option.type : "sure",
                    button  = option.button || {},
                    focus = 0,
                    position = option.position?option.position:{};
                if(type == "sure") {
                    button = {"确认":true};
                    focus = 0;
                }else if(type == "confirm"){
                    button = {"取消": false,"确认":true};
                    focus = 1;
                }

                if(button){
                    button = button;
                }

                $.prompt(option.text?option.text:"", {
                    title: title,
                    buttons: button,
                    position:position,
                    focus:focus,
                    submit:function(e,v){
                        if(v){
                            if(typeof(option.callback) == "function"){
                                option.callback();
                            }
                        }
                    }
                });
            },
            displayOneTemp: function (index) {
                self.isTeacherShow(false);
                self.createClazzShow(false);
                self.isStudentShow(false);
                self.changeClazzShow(false);
                self.copyTeachingShow(false);
                self.markTransientShow(false);
                switch (index) {
                    case 0:
                        self.isTeacherShow(true);
                        break;
                    case 1:
                        self.createClazzShow(true);
                        break;
                    case 2:
                        self.isStudentShow(true);
                        break;
                    case 3:
                        self.changeClazzShow(true);
                        break;
                    case 4:
                        self.copyTeachingShow(true);
                        break;
                    case 5:
                        self.markTransientShow(true);
                        break;
                }
            }
        });
        self.getAllClazzInfo();
        for (var i=1;i<7;i++){
            _fileUpload(i,".v-fileupload"+ "" + i);
        }
        function _fileUpload(index,obj) {
            $(document).on("change",obj,function () {
                switch (index) {
                    case 1:
                        self.teacherErrorShow(false);
                        self.teacherErrorText("");
                        break;
                    case 2:
                        self.creatErrorShow(false);
                        self.creatErrorText("");
                        break;
                    case 3:
                        self.studentErrorShow(false);
                        self.studentErrorText("");
                        break;
                    case 4:
                        self.changeStudentErrorShow(false);
                        self.changeStudentErrorText("");
                        break;
                    case 5:
                        self.copyStudentErrorShow(false);
                        self.copyStudentErrorText("");
                        break;
                    case 6:
                        self.markTransientErrorShow(false);
                        self.markTransientErrorText("");
                        break;
                }
                // 截掉前面的路径，只留文件名
                var fileInput = $(obj).val();
                fileInput = fileInput.substring(fileInput.lastIndexOf("\\") + 1);
                switch (index) {
                    case 1:
                        self.importTName(fileInput);
                        break;
                    case 2:
                        self.teacherClazzName(fileInput);
                        break;
                    case 3:
                        self.importSName(fileInput);
                        break;
                    case 4:
                        self.changeStudentName(fileInput);
                        break;
                    case 5:
                        self.copyStudentName(fileInput);
                        break;
                    case 6:
                        self.markTransientName(fileInput);
                        break;
                }
                $(this).parent().siblings("span").addClass("green");
            });
        }

    };

    var tModal = new teacherStuModal();
    ko.applyBindings(tModal,document.getElementById("stuApp"));
});