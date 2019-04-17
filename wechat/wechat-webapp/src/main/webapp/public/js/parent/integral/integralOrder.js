/*贡献班级学豆*/
define(['jquery','knockout'], function ($,ko) {
    var orderModel = function () {
        var self = this;
        self.selectedTeacher = ko.observable('');
        self.selectedTeacherId = ko.observable(0);
        self.selectedCount = ko.observable(0);
        self.integalPrice = ko.observable(0);


        self.selectedRule = ko.observable(true);
        self.response = ko.observable(null);

        //选择要赠送的老师
        self.selectSubject = function (tid, subject) {
            self.selectedTeacher(subject);
            self.selectedTeacherId(tid);
        };


        //选择要赠送的学豆数
        self.selectedIntegral = function (count, price) {
            self.selectedCount(count);
            self.integalPrice(price);

        };

        //是否勾选协议
        self.ruleBtn = function (bl) {
            self.selectedRule(bl);
        };

        //立即购买
        self.buyBtn = function () {
            if (self.selectedRule() && self.integalPrice() > 0 && self.selectedTeacherId() != 0 ) {
                document.forms[0].submit();
            }
        };
    };

    ko.applyBindings(new orderModel());
});