/**
 * @author xinqiang.wang
 * @description "亲子活动首页"
 * @createDate 2016/10/27
 */

define(['jquery', 'knockout', "weui", 'voxLogs', 'flexSlider'], function ($, ko) {

    var FamilyActivityModule = function () {
        var self = this;

        self.contentDetail = ko.observableArray([]);

        self.imagesDetail = ko.observableArray([]);
        self.text = ko.observable('');

        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(0);
        self.totalPage = ko.observable(0);

        self.ajaxIsLoading = ko.observable(false);
        self.ajaxLoadFinished = ko.observable(false);

        self.longitude = ko.observable(116.494384);
        self.latitude = ko.observable(40.00582);

        self.getContentDetail = function () {
            if (self.contentDetail().length == 0) {
                $.showLoading();
            }
            self.ajaxIsLoading(true);
            $.post('/mizar/familyactivity/more.vpage', {
                pageNum: self.pageNum(),
                pageSize: self.pageSize()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(data.activityList, function (rows) {
                        if (rows.image && rows.image.indexOf('oss-image.17zuoye.com') !== -1) {
                            rows.image = rows.image + "@310h_1o";
                        }
                        rows.cover = rows.cover && rows.cover != '' ? rows.cover : '';
                        rows.desc = rows.desc == '' ? [] : rows.desc.split(',');

                        self.contentDetail.push(rows);
                    });
                    self.totalPage(data.totalPage);
                } else {
                    $.alert(data.info);
                }

                self.ajaxIsLoading(false);
                self.ajaxLoadFinished(true);
            }).fail(function () {
                $.hideLoading();
                self.ajaxIsLoading(false);
                $.toast('数据异常', 'weui_toast_forbidden');
            });
        };

        self.getContentDetail();

        //图片广告位
        self.getImageAdp = function () {
            $.post('/be/newinfo.vpage', {p: 221002}, function (data) {
                if (data.success) {

                    ko.utils.arrayForEach(data.data, function (value) {
                        value.img = data.imgDoMain + 'gridfs/' + value.img;
                    });
                    self.imagesDetail(data.data);
                    setTimeout(function () {
                        //顶部导航广告。
                        $("#bannerContentBox").flexslider({
                            animation: "fade",
                            directionNav: false,
                            pauseOnAction: true,
                            after: function (slider) {
                                if (!slider.playing) {
                                    slider.play();
                                }
                            },
                            slideshowSpeed: 4000,
                            animationSpeed: 400
                        });
                    },0);
                }
            });
        };
        self.getImageAdp();

        //广告点击
        self.topImgBtn = function (index) {
            var that = this;
            if(that.hasUrl){
                $.showLoading();
                setTimeout(function () {
                    location.href = '/be/london.vpage?aid='+that.id+"&index="+index;
                }, 200);
            }
        };

        //文案广告位
        self.getTextAdp = function () {
            $.post('/be/newinfo.vpage', {p: 221003}, function (data) {
                console.info(data);
                if (data.success) {
                    self.text(data.data);
                }
            });
        };

        self.getTextAdp();


        //活动点击
        self.actBtn = function () {
            var that = this;
            $.showLoading();
            setTimeout(function () {
                var param = {
                    actId : that.actId,
                    lat: self.latitude(),
                    lng: self.longitude()
                };
                location.href = '/mizar/familyactivity/detail.vpage?'+ $.param(param)+"&_from=list";
            }, 200);

        };

        //获取经纬度
        var vox = vox || {};
        vox.task = vox.task || {};
        vox.task.setLocation = function (res) {
            var resJson = JSON.parse(res);
            var errorCode = parseInt(resJson.errorCode || 0);

            if (errorCode > 0) {
                onError({
                    code: errorCode
                })
            } else {
                onSuccess({
                    coords: {
                        longitude: resJson.longitude,
                        latitude: resJson.latitude,
                        address: resJson.address
                    }
                });
            }
        };

        function getLocation() {
            var options = {
                enableHighAccuracy: true,
                maximumAge: 1000
            };

            if (window['external'] && ('getLocation' in window.external)) {
                window.external.getLocation();
            } else {
                if (navigator['geolocation'] && navigator.geolocation['getCurrentPosition']) {
                    navigator.geolocation.getCurrentPosition(onSuccess, onError, options);
                } else {
                    //alert("位置反解析失败,重新定位");
                }
            }
        }

        //成功时
        function onSuccess(position) {
            self.latitude(position.coords.latitude);
            self.longitude(position.coords.longitude);
        }

        //失败时
        function onError(error) {
            switch (error.code) {
                case 1:
                    //alert("位置服务被拒绝,重新定位");
                    break;
                case 2:
                    //alert("暂时获取不到位置信息,重新定位");
                    break;
                case 3:
                    //alert("获取信息超时,重新定位");
                    break;
                case 4:
                    //alert("未知错误,重新定位");
                    break;
                default:
            }

        }

        $(window).scroll(function () {
            if (($(document).height() - 200 <= $(window).height() + $(window).scrollTop())) {
                if (self.pageNum() < self.totalPage() - 1 && !self.ajaxIsLoading()) {
                    self.pageNum(self.pageNum() + 1);
                    self.getContentDetail();
                }
            }
        });

        YQ.voxLogs({
            database: 'parent',
            module: 'm_BqyGPVoT',
            op : "o_MfjzvRjH"
        });
    };

    ko.applyBindings(new FamilyActivityModule());
});