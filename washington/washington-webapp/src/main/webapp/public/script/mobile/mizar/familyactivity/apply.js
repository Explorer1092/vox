/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/10/28
 */

define(['jquery', 'knockout', 'komapping', "weui", 'voxLogs'], function ($, ko, komapping) {
    var FamilyActivityModule = function () {
        var self = this;
        self.productTypeDetail = ko.observableArray([]);
        self.productTypeIndex = 0;
        for (var t in itemMap) {
            self.productTypeDetail.push({key: t, checked: !self.productTypeIndex});
            self.productTypeIndex += 1;
            for (var i = 0; i < itemMap[t].length; i++) {
                itemMap[t][i].checked = !i;
            }
        }

        self.getQuery = function (item) {
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };

        //获取商品价格
        self.getPrice = function () {
            var price = 0;
            ko.utils.arrayForEach(self.secondList(), function (second) {
                if (second.checked()) {
                    price = second.price();
                    self.remains(second.remains());
                }
            });
            return price;
        };

        self.secondList = ko.observableArray([]);//出行时间
        self.remains = ko.observable(0);// 剩余数量

        self.productTypeDetail(komapping.fromJS(self.productTypeDetail())());

        //默认第一个产品类型被选择
        ko.utils.arrayForEach(self.productTypeDetail(), function (detail) {
            if (detail.checked()) {
                for (var t in itemMap) {
                    if (t == detail.key()) {
                        self.secondList(komapping.fromJS(itemMap[t])());
                    }
                }
            }
        });

        //产品类型选择
        self.productTypeBtn = function (that) {
            ko.utils.arrayForEach(self.productTypeDetail(), function (detail) {
                detail.checked(false);
            });
            that.checked(true);
            self.secondList([]);

            for (var t in itemMap) {
                if (t == that.key()) {
                    self.secondList(komapping.fromJS(itemMap[t])());
                }
            }
        };

        self.secondTypeBtn = function (that) {
            ko.utils.arrayForEach(self.secondList(), function (second) {
                second.checked(false);
            });
            that.checked(true);
        };

        self.submitBtn = function () {
            $.showLoading();
            setTimeout(function () {
                var item = '';
                ko.utils.arrayForEach(self.secondList(), function (second) {
                    if (second.checked()) {
                        item = second.itemId();
                    }
                });
                var param = {
                    actId: self.getQuery('actId'),
                    dp: false,
                    item: item
                };
                location.href = '/mizar/familyactivity/pay.vpage?' + $.param(param);
            }, 200);

            YQ.voxLogs({
                database: 'parent',
                module: 'm_BqyGPVoT',
                op: "o_hjkh9IX1",
                s0: self.getQuery('actId'),
                s1: self.getPrice()
            });
        }
    };

    ko.applyBindings(new FamilyActivityModule());
});