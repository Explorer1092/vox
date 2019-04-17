/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/9/12
 */
define(['jquery', 'knockout', "knockoutscroll", "weui","$17"], function ($, ko) {
    var RemarkModel = function () {
        var self = this;
        self.remarkList = ko.observableArray([]);
        self.pageSize = ko.observable(20);
        self.pageNum = ko.observable(1);
        self.totalPage = ko.observable(0);

        self.getRemarkList = function () {
            if (self.remarkList().length == 0) {
                $.showLoading();
            }
            $.post('/mizar/loadratingpage.vpage', {
                shopId: $17.getQuery('shopId'),
                pageNum: self.pageNum(),
                pageSize: self.pageSize()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    ko.utils.arrayForEach(data.rows, function (rows) {
                        if ( $17.isBlank(rows.avatar) ) {
                            rows.avatar = mizarMap.avatar;
                        }
                        self.remarkList.push(rows);
                    });
                    self.totalPage(data.totalPage);

                    //评论部分有复用,暂时这样区分
                    if(location.pathname.indexOf("ratinglist") != -1){
                        $(".js-commentDiv").show();
                    }
                } else {
                    $.alert(data.info);
                }

            }).fail(function () {
                $.hideLoading();
            });
        };

        /*滚屏加载数据*/
        self.scrolled = function () {
            if (self.pageNum() < self.totalPage()) {
                self.pageNum(self.pageNum() + 1);
                self.getRemarkList();
            }
        };

        /*初始化*/
        self.getRemarkList();

    };

    ko.applyBindings(new RemarkModel());

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    //点击评价
    $(".footer .inner").on('click', function () {
        location.href = '/mizar/remark/remark.vpage?_from=shopdetail&shopId=' + getQueryString('shopId') + "&shopName=" + baseData.shopName;
    });
});

