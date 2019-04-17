/**
 * @author xinqiang.wang
 * @description "写点评"
 * @createDate 2016/9/9
 */
define(['jquery', 'knockout', 'komapping', "weui", "$17", 'voxLogs'], function ($, ko, komapping) {
    var RemarkModel = function () {
        var self = this;
        self.shopId = $17.getQuery('shopId');

        self.activityId = $17.getQuery('activityId');
        self.starList = komapping.fromJS([
            {index: 0, checked: false},
            {index: 1, checked: false},
            {index: 2, checked: false},
            {index: 3, checked: false},
            {index: 4, checked: false}]);

        self.remarkContent = ko.observable('');
        self.maxImgCount = ko.observable(6);

        self.uploadImgList = ko.observableArray([]);

        self.fromShopDetail = $17.getQuery('_from') || '';
        self.coursePrice = ko.observable('');
        self.textareaPlaceholder = (self.fromShopDetail != '') ? '请写下对机构的评价，您的经验对其他家长很重要，建议30字以上。' : '为选择的机构打分和评价，被选入优质点评可为孩子赢得100学豆奖励';


        /*星级选择*/
        self.starBtn = function (index) {
            ko.utils.arrayForEach(self.starList(), function (list, i) {
                if (i + 1 <= index) {
                    list.checked(true);
                } else {
                    list.checked(false);
                }
            });
        };

        self.uploadImg = function () {
            if (self.uploadImgList().length >= 5) {
                $.alert("最多5张。");
                return false;
            }
            var $this = $('.fileUpBtn');
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                    $.alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('file', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
                if (fileSize >= 5) {
                    $.alert("图片过大，重新选择。");
                    return false;
                }
                $.showLoading();
                $.ajax({
                    url: '/mizar/uploadphoto.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            self.uploadImgList.push(data.fileName);
                        } else {
                            $.alert("上传失败");
                        }
                        $.hideLoading();
                    }
                });
            }
        };

        /*删除图片*/
        self.deleteImgBtn = function (index) {
            self.uploadImgList.splice(index, 1);
        };

        /*数据提交*/
        self.submitBtn = function () {
            var star = 0;
            ko.utils.arrayForEach(self.starList(), function (list) {
                if (list.checked()) {
                    star += 1;
                }
            });

            if (star == 0) {
                $.alert("请选择总体评价");
                return false;
            }

            if ($17.isBlank(self.remarkContent())) {
                $.alert("请填写评价内容");
                return false;
            }

            if (self.remarkContent().length < 15 && self.fromShopDetail == 'shopdetail') {
                $.alert("评论至少15字，您的建议对大家很重要哦");
                return false;
            }

            if (!$17.isBlank(self.coursePrice()) && !$17.isNumber(self.coursePrice())) {
                $.alert("必须是数字");
                return false;
            }

            $.showLoading();
            $.post('/mizar/rating.vpage', {
                shopId: self.shopId,
                ratingStar: star,
                ratingContent: self.remarkContent(),
                photo: self.uploadImgList().join(',') || '',
                activityId: self.activityId,
                cost: self.coursePrice() || 0
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    $.toast("点评成功", function () {
                        if (self.fromShopDetail == 'shopdetail') {
                            location.href = '/mizar/shopdetail.vpage?shopId=' + self.shopId;
                        } else {
                            location.href = '/mizar/remark/detail.vpage?shopId=' + self.shopId;
                        }
                    });

                } else {
                    $.alert(data.info);
                }
            }).fail(function () {
                $.toast("数据异常", 'forbidden');
                $.hideLoading();
            });
        };
        YQ.voxLogs({
            database: 'parent',
            module: 'm_Ug7dW2ob',
            op: "o_dTmCc1tJ",
            s0: self.shopId
        });
    };
    ko.applyBindings(new RemarkModel());

});