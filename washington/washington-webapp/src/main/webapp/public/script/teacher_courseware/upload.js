/**
 * Created by will on 2018/3/21.
 */
define(['jquery', 'knockout','impromptu','YQ'], function ($, ko) {
    var CourseModel = function () {
        var self = this;
        self.courseId = ko.observable(YQ.getQuery('cid'));
        self.currentStep = ko.observable(1);
        self.subject = ko.observable('');
        self.term = ko.observable(1);
        self.clazzLevel = ko.observable(1);
        self.bookName = ko.observable('');
        self.maxFileSize = ko.observable(10 * 1024 * 1024);
        self.bookList = ko.observableArray([]);
        self.currentUnitList = ko.observableArray([]);
        self.coursewareFileImages = ko.observableArray([]);
        self.currentUnit = ko.observable('');
        self.currentBook = ko.observable('');
        self.course_name = ko.observable('');
        self.teachingDesign = ko.observable('');
        self.coursewaresFile = ko.observable();
        self.coverImageUrl = ko.observable("");

        self.showCurrentUnitList = ko.observable(1);
        self.coursewareFileName = ko.observable("");

        self.showLoading = ko.observable(false);

        self.termList = ko.observableArray([
            {value:1,termDesc:"上册"},
            {value:2,termDesc:"下册"}
        ]);

        self.gradeList = ko.observableArray([
            {value:1,classDesc:"一年级"},
            {value:2,classDesc:"二年级"},
            {value:3,classDesc:"三年级"},
            {value:4,classDesc:"四年级"},
            {value:5,classDesc:"五年级"},
            {value:6,classDesc:"六年级"}
        ]);

        self.termDesc = ko.observable('上册');
        self.clazzDesc = ko.observable('一年级');
        self.bookDesc = ko.observable('');
        self.unitDesc = ko.observable('');

        self.currentStep(YQ.getQuery('step') ? YQ.getQuery('step') : 1);
        self.switchStep = function (data) {
            // self.currentStep(data);

        };

        self.switchTerm = function (term) {
            self.term(term.value);
            self.termDesc(term.termDesc);
            self.getTeachingBookInfo();
        };

        self.switchClassLevel = function (cl) {
            self.clazzLevel(cl.value);
            self.clazzDesc(cl.classDesc);
            self.getTeachingBookInfo();
        };

        self.nextStep = function () {
            //TODO watch all value to able btn state
            if(self.currentStep() < 3){
                self.currentStep(parseInt(self.currentStep()) + 1)
            }
        };

        self.switchUnitDisplay = function () {
            self.showCurrentUnitList(!self.showCurrentUnitList());
        };

        //获取课件详情
        self.getCourseDetail = function (callback) {
            if(self.courseId()){
                $.ajax({
                    url:'/courseware/contest/myworks/detail.vpage',
                    type:'GET',
                    data:{
                        id:self.courseId()
                    },
                    success:function (res) {
                        if(res.success){
                            self.displayCourseDetail(res);
                            if(typeof callback === "function"){
                                callback();
                            }
                        }else{
                            $.prompt(res.info ? res.info : '获取老师信息出错');
                        }
                    },
                    error:function () {
                        $.prompt('net:网络出错');
                    }
                })
            }else{
                self.getTeacherInfo();
            }
        };

        self.getCourseDetail();

        // 课件详情绑定到页面模板
        self.displayCourseDetail = function (res) {
            self.term(res.term?res.term:1);
            self.clazzLevel(res.clazzLevel?res.clazzLevel:1);

            self.termDesc(self.getTermDesc(self.term()));
            self.clazzDesc(self.getClassDesc(self.clazzLevel()));
            //TODO 当前教材编辑时候的交互
            self.currentBook(res.bookId?res.bookId:"");
            self.currentUnit(res.unitId?res.unitId:"");

            self.coursewareFileName(res.coursewareFileName?res.coursewareFileName:"");
            self.coverImageUrl(res.image?res.image:"");
            self.course_name(res.title?res.title:"");
            self.teachingDesign(res.description?res.description:"");

            if(res.coursewareFile){
                $("#perViewListContainer>p").html("课件预览正在生成……");
            }

            if(res.coursewareFileImages && res.coursewareFileImages.length != 0){
                self.coursewareFileImages(res.coursewareFileImages);
                self.lazyLoadImage(res.coursewareFileImages);
            }else{
                $("#perViewListContainer").html('<p style="text-align: center;color: #968e8e;margin-top: 120px;">课件预览正在生成……</p>');
            }

            if(res.subject){
                self.subject(res.subject);
                self.getTeachingBookInfo(function () {
                    if(res.bookId){
                        self.bookDesc(res.bookName);
                        self.activeBookItem(self.currentBook());

                        if(res.unitId && res.unitName){
                            self.currentUnit(res.unitId);
                            self.unitDesc(res.unitName);
                        }
                        self.currentUnitList(self.getUnitList(self.currentBook()));
                        self.showCurrentUnitList(0);

                    }
                });
            }else{
                self.getTeacherInfo();
            }
        };

        self.getUnitList = function (bid) {
            var _uList = [];
            var bookList = self.bookList();
            for(var i=0;i<bookList.length;i++){
                if(bookList[i].items && bookList[i].items.length != 0){
                    for(var j=0;j<bookList[i].items.length;j++){
                        if(bookList[i].items[j].id == bid){
                            _uList = bookList[i].items[j].unitList;
                            break;
                        }
                    }
                }
            }

            return _uList;
        };

        //10张懒加载图片
        self.lazyLoadImage = function (list) {
            var perViewListContainer = $("#perViewListContainer");
            // if(list.length > 10){
            //     //TODO 大于10张
            // }else{
                var _temp = "";
                for(var i=0;i<list.length;i++){
                    _temp += '<img src='+list[i]+'>';
                }
                perViewListContainer.html(_temp);
            // }
        };

        self.getTermDesc = function (term) {
            var desc = "";
            if(term){
                for(var i=0;i<self.termList().length;i++){
                    if(term == self.termList()[i].value){
                        desc = self.termList()[i].termDesc;
                    }
                }
            }
            return desc;
        };

        self.getClassDesc = function (clazzLevel) {
            var desc = "";
            if(clazzLevel){
                for(var i=0;i<self.gradeList().length;i++){
                    if(clazzLevel == self.gradeList()[i].value){
                        desc = self.gradeList()[i].classDesc;
                    }
                }
            }
            return desc;
        };

        self.lastStep = function () {
            if(self.currentStep() > 1){
                self.currentStep(parseInt(self.currentStep()) - 1)
            }
        };

        self.getTeacherInfo = function () {
            $.ajax({
                url:'/courseware/contest/teacherinfo.vpage',
                type:'GET',
                success:function (res) {
                    if(res.success){
                        if(res.subjects && res.subjects.length != 0){
                            self.subject(res.subjects[0]);
                            self.getTeachingBookInfo();
                        }
                    }else{
                        $.prompt(res.info ? res.info : '获取老师信息出错');
                    }
                },
                error:function () {
                    $.prompt('net:网络出错');
                }
            })
        };

        self.getTeachingBookInfo = function (callback) {
            $.ajax({
                url:'/courseware/contest/booklist.vpage',
                type:'GET',
                data:{
                    subject:self.subject(),
                    term:self.term(),
                    clazzLevel:self.clazzLevel(),
                    name:self.bookName()
                },
                success:function (res) {
                    if(res.success){
                        var list = [],_list = [],bl = res.books;
                        if(bl.length != 0){
                            var fList = bl;
                            for(var i = 0 ;i<=(fList.length/3);i++){
                                _list = [];

                                fList[3*i+0] && _list.push(fList[3*i+0]);
                                fList[3*i+1] && _list.push(fList[3*i+1]);
                                fList[3*i+2] && _list.push(fList[3*i+2]);

                                list.push({
                                    items:_list
                                })
                            }
                        }
                        self.bookList(list);

                        self.currentUnitList([]);
                        self.showCurrentUnitList(1);
                        self.currentUnit("");
                        self.unitDesc("");

                        if(typeof callback === "function"){
                            callback();
                        }
                    }else{
                        $.prompt(res.info ? res.info : '获取教材出错');
                    }
                },
                error:function () {
                    $.prompt('net:网络出错');
                }
            })
        };

        self.searchTeachingBookInfo = function (data,event) {
            if(event.keyCode ==13){
                self.getTeachingBookInfo();
            }
            return true;
        };

        self.chooseBook = function (data) {
            self.currentBook(data.id);
            self.bookDesc(data.name);
            //TODO active dom
            self.activeBookItem(self.currentBook());

            self.currentUnitList(data.unitList);
            self.showCurrentUnitList(0);
            self.currentUnit("");
            self.unitDesc("");

        };

        self.activeBookItem = function (bid) {
            var nodeList = $(".js-bItem");
            for(var i=0;i<nodeList.length;i++){
                if(bid == $(nodeList[i]).attr("bid")){
                    $(nodeList[i]).addClass("active");
                }else{
                    $(nodeList[i]).removeClass("active");
                }
            }
        };

        self.activeUnitItem = function (uid) {
            var nodeList = $(".js-uItem");
            for(var i=0;i<nodeList.length;i++){
                if(uid == $(nodeList[i]).attr("uid")){
                    $(nodeList[i]).addClass("active");
                }else{
                    $(nodeList[i]).removeClass("active");
                }
            }
        };

        self.chooseUnit = function (data) {

            self.currentUnit(data.unitId);
            self.unitDesc(data.unitName);

        };

        self.chooseCourseFile = function (data,event) {
            var files = event.target.files;
            if(files.length != 0){

                var file = files[0];
                if(file.size <= self.maxFileSize()){
                    if(file.name.toString().indexOf('.doc') != -1 ||
                        file.name.toString().indexOf('docx') != -1 ||
                        file.name.toString().indexOf('ppt') != -1 ||
                        file.name.toString().indexOf('pptx') != -1
                    ){
                        self.coursewaresFile(file);
                        self.uploadCourseFile(function(){
                            self.coursewareFileName(file.name);
                            self.showLoading(false);
                            //update detail name and desc keep before value
                            var name = self.course_name();
                            var desc = self.teachingDesign();
                            self.getCourseDetail(function () {
                                name && self.course_name(name);
                                desc && self.teachingDesign(desc);
                                self.refreshPerView();
                            });
                        });
                        self.showLoading(true);
                    }else{
                        $.prompt('请选择word或者ppt格式附件');
                        self.coursewaresFile(null);
                    }
                }else{
                    $.prompt('附件大小不要超过10M');
                    self.coursewaresFile(null);
                }
            }
        };

        //上传课件
        self.uploadCourseFile = function (callback) {
            if(self.coursewaresFile()){
                var formData = new FormData();
                formData.append('file', self.coursewaresFile());
                formData.append('id',self.courseId());
                $.ajax({
                    url: '/courseware/contest/myworks/uploadfile.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false
                }).done(function(res) {
                    if(res.success){
                        $.prompt("上传成功");
                        if(typeof callback === "function"){
                            callback();
                        }
                    }else{
                        $.prompt(res.info ? res.info : '上传文件出错');
                    }
                }).fail(function(e) {
                    $.prompt('net：网络错误');
                });
            }else{
                $.prompt('请选择需要');
            }
        };

        //第一页信息提交
        self.updateBookInfo = function () {
            if(self.validateFirstStep()){
                $.ajax({
                    url: '/courseware/contest/myworks/updatebook.vpage',
                    type: 'POST',
                    data: {
                        id:self.courseId(),
                        subject:self.subject(),
                        term:self.term(),
                        clazzLevel:self.clazzLevel(),
                        bookId:self.currentBook(),
                        unitId:self.currentUnit()
                    }
                }).done(function(res) {
                    if(res.success){
                        self.nextStep();
                    }else{
                        $.prompt(res.info ? res.info : '更新课件信息出错');
                    }
                }).fail(function(e) {
                    $.prompt('net：网络错误');
                });
            }
        };

        //第二页提交信息
        self.updateContentInfo = function () {
            // TODO 验证提交项
            if(self.validateSecondStep()){
                $.ajax({
                    url: '/courseware/contest/myworks/updatecontent.vpage',
                    type: 'POST',
                    data: {
                        id:self.courseId(),
                        title:self.course_name(),
                        description:self.teachingDesign()
                    }
                }).done(function(res) {
                    if(res.success){
                        self.nextStep();
                    }else{
                        $.prompt(res.info ? res.info : '更新课件信息出错');
                    }
                }).fail(function(e) {
                    $.prompt('net：网络错误');
                });
            }
        };

        //提交审核
        self.submitApply = function () {
            // TODO 验证提交项
            $.prompt('<p style="text-align: left;">确认课件无误并提交审核？提交后将不能编辑或删除课件。</p>' +
                '<p style="text-align: left;">审核中的课件在一周内审核完毕，被退回的课件无法进入展示和评分。</p>' +
                '<p>只保存不提交审核的课件，可编辑后再提交。</p>',{
                title: "提示",
                focus: 1,
                buttons: { "取消": false, "确定": true },
                submit: function(e, v){
                    if(v){
                        $.ajax({
                            url: '/courseware/contest/myworks/commit.vpage',
                            type: 'POST',
                            data: {
                                id:self.courseId()
                            }
                        }).done(function(res) {
                            if(res.success){
                                //TODO 提示
                                location.href = "/courseware/contest/my_course.vpage";
                            }else{
                                $.prompt(res.info ? res.info : '更新课件信息出错');
                            }
                        }).fail(function(e) {
                            $.prompt('net：网络错误');
                        });
                    }
                }
            });
        };

        //校验第一个步骤
        self.validateFirstStep = function () {
            var flag = true;
            if(!self.currentUnit()){
                $.prompt("请选择对应单元");
                flag = false;
            }
            if(!self.currentBook()){
                $.prompt("请选择教材");
                flag = false;
            }

            return flag;
        };

        //校验第二步骤
        self.validateSecondStep = function () {
            var flag = true;
            if(!self.teachingDesign()){
                $.prompt("请填写教学设计");
                flag = false;
            }
            if(!self.coursewaresFile()){
                $.prompt("请上传教学课件");
                flag = false;
            }
            if(!self.course_name()){
                $.prompt("请填写课件名称");
                flag = false;
            }

            return flag;
        };

        window.onbeforeunload=function(){

            if(self.currentStep() == 2){
                self.updateContentInfo();
            }
            if(self.currentStep() == 3){
                self.submitApply();
            }

            return "确定要离开吗";
        };

        //30s 刷新获取课件预览图
        var detailTimer;
        self.refreshPerView = function () {
            detailTimer = setInterval(function () {
                if(self.currentStep() == 3){
                    self.getCourseDetail(function () {
                        if(self.coursewareFileImages().length != 0 ){
                            clearInterval(detailTimer);
                        }
                    });
                }
            },30000);
        };
        self.refreshPerView();

    };

    ko.applyBindings(new CourseModel());
});
