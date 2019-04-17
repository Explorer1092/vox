/**
 * @author free
 * @description 我的课件
 * @createDate 2018/3/20
 */

define(['jquery', 'knockout','impromptu'], function ($, ko) {
    var CourseModel = function () {
        var self = this;
        self._coursewares = ko.observableArray([]); //未处理的原生数据
        self.coursewares = ko.observableArray([]);  //当前展示数据
        self.currentState = ko.observable("ALL");
        self.showMoreBtn = ko.observable(false);
        self.currentPage = ko.observable(0);
        self.perPageItem = ko.observable(10);

        self.fetchList = function () {
            $.ajax({
                url:'/courseware/contest/myworks/list.vpage',
                type:'GET',
                success:function (res) {
                    if(res.success){
                        self._coursewares(self.parseData(res.coursewares));
                        self.coursewares(self._coursewares());
                    }else{
                        $.prompt(res.info ? res.info : '获取课件出错');
                    }
                },
                error:function () {
                    $.prompt('net:网络出错');
                }
            });
        };

        self.fetchList();

        //TODO 处理后端数据
        self.parseData = function (coursewares) {
            var _coursewares = coursewares || [];
            // 1.已发布状态 显示评分相关
            // 2.未提交状态 操作按钮
            // 3.超过10条 显示更多
            var clength = coursewares.length;
            for(var i=0;i<clength;i++){
                var state = _coursewares[i].status;
                _coursewares[i]["opFlag"] = false;
                _coursewares[i]["countFlag"] = false;
                switch (state){
                    case "DRAFT":
                        _coursewares[i]["opFlag"] = true;
                        break;
                    case "PUBLISHED":
                        _coursewares[i]["countFlag"] = true;
                        break;
                }
            }

            if(clength >self.perPageItem()){
                self.showMoreBtn(true);
                self.currentPage(1);
            }

            console.log(_coursewares);

            return _coursewares;
        };

        self.switchFetch = function (data) {
            self.currentState(data);
            if(data != 'ALL'){
                self.coursewares(self.parseData(self.filterCourseList(data)));
            }else{
                self.coursewares(self._coursewares());
            }
        };

        self.filterCourseList = function (type) {
            var course_wares = self._coursewares();
            var list = [];
            for(var i=0;i<course_wares.length;i++){
                var state = course_wares[i].status;
                if(state == type) {
                    list.push(course_wares[i]);
                }
            }
            return list;
        };

        self.edit = function (data) {
            location.href = "/courseware/contest/upload_course.vpage?step=2&&cid=" + data.id;
        };

        self.del = function (data) {
            $.prompt('<p style="text-align: center;">确定要删除该课件吗？</p>',{
                title: "提示",
                focus: 1,
                buttons: { "取消": false, "确定": true },
                submit: function(e, v){
                    if(v){
                        $.ajax({
                            url:'/courseware/contest/myworks/delete.vpage',
                            type:'POST',
                            data:{id:data.id},
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

        self.loadMore = function () {

        };

        self.uploadPage = function () {
            $.ajax({
                url:'/courseware/contest/myworks/create.vpage',
                type:'POST'
            }).done(function (res) {
                if(res.success){
                    location.href = "/courseware/contest/upload_course.vpage?cid=" + res.id;
                }else{
                    $.prompt(res.info ? res.info : '创建课件出错');
                }
            }).fail(function (e) {
                $.prompt('net:网络出错');
            });

        };

        self.showDetail = function (data) {
            location.href = "/courseware/contest/course_detail.vpage?cid=" + data.id;
        }

    };

    ko.applyBindings(new CourseModel());
});