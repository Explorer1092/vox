/**
 * @author xinqiang.wang
 * @description "口碑结构"
 * @createDate 2016/9/8
 */
define(['jquery', 'knockout', "weui", 'voxLogs'], function ($, ko) {

    var RemarkModel = function () {
        var self = this;

        /*预约*/
        self.subscribeBtn = function (shopId,shopType) {
            setTimeout(function () {
                location.href = '/mizar/shopdetail.vpage?shopId='+shopId;
            }, 200);

            YQ.voxLogs({
                database: 'parent',
                module: 'm_Ug7dW2ob',
                op: "o_au0f9lj0",
                s0: shopId,
                s1: shopType
            });
        };

        //展示更多
        self.showMoreBtn = function () {
            setTimeout(function () {
                location.href = '/mizar/list.vpage';
            }, 200);

            YQ.voxLogs({
                database: 'parent',
                module: 'm_Ug7dW2ob',
                op: "o_4Gw0GTli"
            });
        };

        YQ.voxLogs({
            database: 'parent',
            module: 'm_Ug7dW2ob',
            op: "o_6KXx2HGo"
        });
    };
    ko.applyBindings(new RemarkModel());
});