/**
 * Created by free 2016/5/3
 */
define(["$17", "logger", "knockout", "jbox"], function ($17, logger, ko) {
    "use strict";
    logger.log({
        app: "teacher",
        module: 'recommend_book',
        op: 'index_page_pv'
    });

    function RecommendModel(regionCode, subject, cdnBase) {
        this.regionCode = ko.observable(regionCode);
        this.subject = ko.observable(subject);
        this.cdnBase = ko.observable(cdnBase);
        this.currentDivNo = ko.observable(1);
        this.clazzBooks = ko.observableArray();
        this.filterString = ko.observable("");
        this.originalBooks = ko.observableArray();
        this.selectedBook = ko.observable();
        this.previewImageUrl = ko.observable("");
        this.uploadBookName = ko.observable("");
        this.notdone = ko.observable(true);
        this.deleteImage = function () {
            this.previewImageUrl("");
            // 清楚之前选择的内容
            $("input[name=upfile]").val("").trigger("click");
        };
        this.calcBookCoverUrl = function (base) {
            base = base.replace("/math/", "/math_new/");
            var upload = "";
            if (base.search('/upload/') != 0) {
                upload = "/upload";
            }
            return "//" + this.cdnBase() + upload + base;
        };
        this.imageUploaded = ko.computed(function () {
            return this.previewImageUrl().length > 0;
        }, this);
        this.canUpload = ko.computed(function () {
            return this.imageUploaded() || this.uploadBookName();
        }, this);
        this.uploadBook = function () {
            var bookName = this.uploadBookName();
            var cover = this.previewImageUrl();
            if ($17.isBlank(bookName) && $17.isBlank(cover)) {
                $17.jqmHintBox("请填入教材名称或者选择封面图片");
                return false;
            }
            $.post("uploadbook.vpage", {
                bookName: bookName,
                cover: cover,
                clazzId: this.clazz().clazzId
            }, function (data) {
                if (data.success) {
                    this.notdone(false);
                    //$17.jqmHintBox("<span style='font-size: 48px'>提交成功，感谢您的参与！</span>");
                } else {
                    $17.jqmHintBox("<span style='font-size: 48px'>提交失败，请重试！</span>");
                }
            }.bind(this));
        };
        this.uploadImage = function (file) {
            $17.weuiLoadingShow();
            if (file == undefined) {
                return false;
            }
            if (!file.name.match(/\.(jpg|jpeg|png|gif|bmp|tif|fif|fiff)$/)) {
                $17.jqmHintBox("请选择图片文件上传");
                return false;
            }
            var formData = new FormData();
            formData.append('upfile', file);
            $.ajax({
                url: 'uploadcover.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    $17.weuiLoadingHide();
                    if (data.success) {
                        this.previewImageUrl(data.url);
                    } else {
                        $17.jqmHintBox("上传失败");
                    }
                }.bind(this)
            });
            return false;
        }.bind(this);
        this.showDivChoose = function () {
            this.currentDivNo(1);
        };
        this.showDivUpload = function () {
            this.currentDivNo(2);
        };
        this.selectBook = function (data, event) {
            $(".tb-bookBox li").removeClass("active");
            $(event.currentTarget).addClass("active");
            this.selectedBook(data);
        }.bind(this);
        this.chooseBook = function () {
            // 提交选中教材到后台
            var bookId = this.selectedBook().id;
            var bookCname = this.selectedBook().cname;
            var bookEname = this.selectedBook().ename;
            var press = this.selectedBook().press;
            $.post("choosebook.vpage", {
                bookId: bookId,
                bookCname: bookCname,
                bookEname: bookEname,
                press: press,
                clazzId: this.clazz().clazzId
            }, function (data) {
                if (data.success) {
                    this.notdone(false);
                    //$17.jqmHintBox("<span style='font-size: 48px'>提交成功，感谢您的参与！</span>");
                } else {
                    $17.jqmHintBox("<span style='font-size: 48px'>提交失败，请重试！</span>");
                }
            }.bind(this));
        };
        this.clazz = ko.computed(function () {
            var clazz = this.clazzBooks()[0] || {};
            return clazz;
        }, this);
        this.books = ko.computed(function () {
            var filteredBooks = [];
            var filterString = this.filterString();
            if (filterString) {
                var re = new RegExp(filterString, 'i');
                return ko.utils.arrayFilter(this.originalBooks(), function (item) {
                    return !!item.cname.match(re);
                });
            } else {
                return this.originalBooks();
            }
            return filteredBooks;
        }, this);
        this.showNotFound = ko.computed(function () {
            return this.books().length < 0;
        }, this);
        this.loadClazzRecommendBook = function () {
            $17.weuiLoadingShow();
            $.post("loadbooks.vpage", function (data) {
                ko.utils.arrayPushAll(this.clazzBooks, data.clazzBooks);
                this.sortBook();
            }.bind(this));
        }.bind(this);
        this.sortBook = function () {
            $.post("sortbook.api?code=" + this.regionCode() + "&level=" + this.clazz().clazzLevel, function (data) {
                $17.weuiLoadingHide();
                var recommendBookId = this.clazz().recommendBookId;
                var recommendBookIndex = -1;
                for (var i = 0; i < data.total; i++) {
                    var book = data.rows[i];
                    if (book.id == recommendBookId) {
                        recommendBookIndex = i;
                    } else {
                        this.originalBooks.push(book);
                    }
                }
                // 推荐的教材排第一个
                if (recommendBookIndex > 0) {
                    this.originalBooks.unshift(data.rows[recommendBookIndex]);
                }
                //this.selectedBook(data.rows[recommendBookIndex]);
            }.bind(this));
        }.bind(this);
    }

    var recommendModel = new RecommendModel(rootRegionCode, subject, cdnBase);
    recommendModel.loadClazzRecommendBook();
    ko.applyBindings(recommendModel);
});