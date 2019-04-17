define(['jquery', 'knockout', 'userpopup', '$17', 'menu'], function ($, ko, userpopup, $17) {
    var UnitReport, UnitReportVM, fetch, requestUrl, viewModel;
    requestUrl = '/parent/homework/report/unitreport.vpage';
    fetch = function (sid, subject) {
        var request;
        viewModel.unitReportList.removeAll();
        request = {
            sid: viewModel.sId,
            subject: viewModel.focusTab().toUpperCase()
        };
        return $17.ajax({
            url: requestUrl,
            showLoading: true,
            data: request,
            success: function (data) {
                var urs;
                if (data.success) {
                    urs = data.urs;
                    if(data.isGraduate){
                        $('#isGraduateBox').show();
                        $("#unitReportChip").hide();
                    }else{
                        $("#unitReportChip").show();
                    }
                    return viewModel.unitReportList(urs);
                }
            },
            error: function (data) {
            }
        });
    };
    UnitReport = (function () {
        function UnitReport(options) {
            this.title = options.title;
        }

        return UnitReport;

    })();
    UnitReportVM = (function () {
        function UnitReportVM() {
            this.unitReportList = ko.observableArray([]);
            this.focusTab = ko.observable('english');
            this.sId = 0;
        }

        UnitReportVM.prototype.changeTab = function (subject) {
            this.focusTab(subject);
            if (subject === 'english') {
                return fetch();
            } else {
                return this.unitReportList.removeAll();
            }
        };

        UnitReportVM.prototype.toDetail = function (data) {
            var root;
            root = this;
            return (function (_this) {
                return function () {
                    $17.loadingStart();
                    $17.tongji('parent-单元报告', '点击查看按钮');
                    return setTimeout(function () {
                        location.href = '/parent/homework/report/unitreportdetail.vpage?sid=' + root.sId + '&unitId=' + data.unitId + '&subject=' + root.focusTab().toUpperCase();
                    }, 200);
                };
            })(this);
        };

        return UnitReportVM;

    })();
    viewModel = new UnitReportVM();
    /*菜单组件，后完善 */
    $('#loading').hide();
    $('#unitReport').show();
    ko.applyBindings(viewModel, document.getElementById('unitReport'));
    userpopup.selectStudent("unitReport");
    return {
        loadMessageById: function (sId) {
            viewModel.sId = sId;
            return fetch();
        }
    };
});
