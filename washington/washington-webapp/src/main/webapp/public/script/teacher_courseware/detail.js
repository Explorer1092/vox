/**
 * Created by will on 2018/3/29.
 */
/**
 * @author free
 * @description 我的课件
 * @createDate 2018/3/20
 */

define(['jquery', 'knockout','impromptu','YQ'], function ($, ko) {
    var CourseModel = function () {
        var self = this;
        self.courseId = ko.observable(YQ.getQuery('cid'));
        self.title = ko.observable("");
        self.bookName = ko.observable("");
        self.date = ko.observable("");
        self.description = ko.observable("");
        self.image = ko.observable("");
        self.unitName = ko.observable("");
        self.statusDesc = ko.observable("");
        self.termDesc = ko.observable("");
        self.classLevelDesc = ko.observable("");
        self.coursewareFileName = ko.observable("");
        self.coverImageUrl = ko.observable("");
        self.opFlag = ko.observable(0);
        self.countFlag = ko.observable(0);

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

        //获取课件详情
        self.getCourseDetail = function () {
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

        self.displayCourseDetail = function (res) {
            self.title(res.title);
            self.bookName(res.bookName);
            self.date(res.date);
            self.description(res.description);
            self.image(res.image);
            self.unitName(res.unitName);
            self.statusDesc(res.statusDesc);
            self.termDesc(self.getTermDesc(res.term));
            self.classLevelDesc(self.getClassDesc(res.clazzLevel));
            self.coursewareFileName(res.coursewareFileName);
            self.coverImageUrl(res.image?res.image:"");

            if(res.coursewareFile){
                $("#perViewListContainer>p").html("课件预览正在生成……");
            }

            if(res.coursewareFileImages && res.coursewareFileImages.length != 0){
                self.lazyLoadImage(res.coursewareFileImages);
            }

            res.status === "DRAFT" ? self.opFlag(1) : self.opFlag(0);
            res.status === "PUBLISHED" ? self.countFlag(1) : self.countFlag(0);

        };

        self.edit = function () {
            location.href = "/courseware/contest/upload_course.vpage?step=2&&cid=" + self.courseId();
        };

        self.del = function () {
            $.prompt('<p style="text-align: center;">确定要删除该课件吗？</p>',{
                title: "提示",
                focus: 1,
                buttons: { "取消": false, "确定": true },
                submit: function(e, v){
                    if(v){
                        $.ajax({
                            url:'/courseware/contest/myworks/delete.vpage',
                            type:'POST',
                            data:{id:self.courseId()},
                            success:function (res) {
                                if(res.success){
                                    window.location.reload();
                                }else{
                                    var alertInfo = res.info ? res.info : '操作出错，请稍后重试';
                                    var alertText = '<p style="text-align: center;">'+alertInfo+'</p>';
                                    $.prompt(alertText);
                                }
                            },
                            error:function () {
                                $.prompt('net:网络出错');
                            }
                        });
                    }
                }
            })
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

    };

    ko.applyBindings(new CourseModel());
});