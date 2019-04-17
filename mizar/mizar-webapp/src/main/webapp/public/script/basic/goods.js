/*-----------课程管理相关-----------*/
define(["jquery", "prompt", "datetimepicker", "paginator", "jqform", "template"], function ($) {

    Array.prototype.indexOf = function (val) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == val) return i;
        }
        return -1;
    };

    Array.prototype.contains = function (val) {
        return this.indexOf(val) >= 0;
    };

    Array.prototype.remove = function (val) {
        var index = this.indexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };

    /*********       课程列表相关          *********/
    // 查询
    $("#js-filter").on("click", function () {
        $("#filter-form").submit();
    });

    // 分页插件
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: pages.length,
            visiblePages: 10,
            currentPage: 1,
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (num) {
                pages.eq(num - 1).addClass("displayed").siblings().removeClass("displayed");
                currentPage = num;
            }
        });
    }

    // 变更状态
    $(".op-status").on("click", function () {
        var $this = $(this);
        var data = {
            gid: $this.attr("data-gid"),
            status: $this.attr("data-status")
        };
        $.prompt("<div style='text-align:center;'>是否确认"+$this.html()+"操作</div>", {
            title: "操作提示",
            buttons: {"取消":false, "确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post("/basic/goods/changestatus.vpage", data, function (res) {
                        if (res.success) {
                            $.prompt("<div style='text-align:center;'>状态更新成功</div>", {
                                title: "操作提示",
                                buttons: {"确定": true},
                                focus: 1,
                                submit: function (e, v) {
                                    location.reload();
                                },
                                useiframe: true
                            });
                        } else {
                            $.prompt("<div style='text-align:center;'>" + (res.info || "状态更新失败！") + "</div>", {
                                title: "错误提示",
                                buttons: {"确定": true},
                                focus: 1,
                                useiframe: true
                            });
                        }
                    });
                }
            },
            useiframe: true
        });
    });
    /*********       课程列表结束          *********/

    /*********       课程选择相关          *********/
    // 搜索机构
    $("#search-shop").on("click", function () {
        var shopToken = $("#shopToken").val();
        var shopList = [];
        $(".shop-label").each(function () {
            var $this = $(this);
            shopList.push($this.data().sid);
        });
        $.ajax({
            type: 'GET',
            url: '/basic/goods/searchshop.vpage',
            data: {'shopToken': shopToken, "shopList": shopList.join(",")},
            success: function (data) {
                $("#shopList").html(template("T:ShopChooser", {data: data}));
            }
        });
    });

    // 选择机构
    $(document).on("change", ".select-shop", function () {
        var $this = $(this);
        var $index = $this.parent().index();
        var selected = $this.is(":checked");
        var shopId = $this.parent().siblings().eq($index).html();
        var shopName = $this.parent().siblings().eq($index + 1).html();
        appendShop(shopId, shopName, selected,$this);
    });

    // 取消选择
    $(document).on("click", ".cancel-shop", function () {
        var $this = $(this);
        $this.parent().parent().remove();
        // 取消的时候就不跟下面的表格联动了。。。
    });

    // 新增机构标签
    var appendShop = function (shopId, shopName, selected,obj) {
        var shopList = [];
        var labels = $(".shop-label");
        labels.each(function () {
            var $this = $(this);
            shopList.push($this.data().sid);
        });
        if (!shopList.contains(shopId) && selected) {
            if (shopList.length >= 5) {
                obj.prop("checked", false);
                alert("门店数量不能超过5家");
                return false;
            }
            // 生成标签
            var append = "<div id='sid-" + shopId + "' class='shop-label' data-sid='" + shopId + "'><label>" + shopName + "</label><span><a class='cancel-shop' href='javascript:void(0);'>&nbsp;&times;</a></span></div>";
            $("#selected-shop").append(append).show();
        } else if (shopList.contains(shopId) && !selected) {
            $('#sid-' + shopId).remove();
        }
    };

    // 缓存编辑信息
    $(".goods-step1").on("click", function () {
        var imageNames = [];
        $(".image").each(function () {
            imageNames.push($(this).children("img").first().attr("src"));
        });
        var data = {
            goodsName: $("input[name=goodsName]").val(), // 课程名称
            title: $("input[name=title]").val(), // 课程标题
            desc: $("input[name=desc]").val(), // 课程简介
            goodsHours: $("input[name=goodsHours]").val(), // 课时
            duration: $("input[name=duration]").val(), // 时长
            goodsTime: $("input[name=goodsTime]").val(), // 上课时间
            target: $("input[name=target]").val(), // 年龄段
            category: $("input[name=category]").val(), // 课程分类
            audition: $("select[name=duration]").find("option:selected").val(), // 试听
            price: $("input[name=price]").val(), // 课程现价
            appointGift: $("input[name=appointGift]").val(), // 预约礼
            welcomeGift: $("input[name=welcomeGift]").val(), // 到店礼
            tags: $("input[name=tags]").val(), // 课程标签
            detailImg: imageNames.join(',') // 课程详情图片
        };
        // 先缓存课程信息
        $.post('goodsbuffer.vpage', data, function (res) {
            // 缓存成功之后返回门店选择
            if (res.success) {
                window.location.href = 'chooseshop.vpage';
            } else {
                alert(res.info);
                return false;
            }
        });
    });

    // 缓存门店信息
    $(".goods-step2").on("click", function () {
        var shopList = [];
        $(".shop-label").each(function () {
            var $this = $(this);
            shopList.push($this.data().sid);
        });
        if (shopList.length <= 0) {
            alert("请先选择生效门店");
            return false;
        }
        if (shopList.length > 5) {
            alert("门店数量不能超过5家");
            return false;
        }
        // 先缓存选择的shopId
        $.post('shopselected.vpage', {shopList: shopList.join(",")}, function (res) {
            // 缓存成功之后编辑课程
            if (res.success) {
                window.location.href = 'detail.vpage';
            } else {
                alert(res.info);
                return false;
            }
        });
    });
    /*********       课程选择相关          *********/

    /*********       课程编辑相关          *********/
    // 上传照片
    var fileInput = $("#upload-image");
    var uploadImgFrom = '';
    $(".upload-image").on("click", function () {
        uploadImgFrom = $(this);
        var imgList = uploadImgFrom.closest('.input-control').siblings(".image-preview").find('.image').length;
        if (imgList >= 10) {
            $.prompt("<div style='text-align:center;'>一次最多只能上传10张照片哦~</div>", {
                title: "温馨提示",
                buttons: {"确定": true},
                focus: 1,
                submit: function (e, v) {
                    if (v) {
                        $(".upload-image").removeClass("count-down").html("上传照片");
                    }
                }
            });
        } else {
            fileInput.click();
        }
    });
    fileInput.on("change", function () {
        $(".upload-image").addClass("count-down").html("上传中…");
        $(this).parent().ajaxSubmit(function (res) {
            if (res.success) {
                $(".upload-image").removeClass("count-down").html("上传照片");
                fileInput.val("");
                uploadImgFrom.closest('.input-control').siblings(".image-preview").append('<div class="image"><img src="' + res.imgUrl + '" /><div class="del-btn">删除</div></div>');
            } else {
                $.prompt("<div style='text-align:center;'>" + res.info + "</div>", {
                    title: "错误提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if (v) {
                            $(".upload-image").removeClass("count-down").html("上传照片");
                        }
                    }
                });
            }
        });
    });

    // 删除照片
    $(document).on("click", ".del-btn", function () {
        var $this = $(this);
        var src = $this.prev().attr("src");
        $.prompt("<div style='text-align:center;'>确认删除这张照片？<br /><img style='width:143px;height:109px;margin-top:20px;' src='" + src + "' /></div>", {
            title: "删除照片",
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    $this.parent().remove();
                }
            },
            useiframe: true
        });
    });

    // 校验输入
    var requireInputs = $(".require");
    var gid = $("#goods-id").val();

    function isEmptyInput() {
        var isTrue = false;
        requireInputs.each(function () {
            if ($(this).val() == '') {
                $(this).addClass("error").val("请填写" + $(this).attr("data-title"));
                isTrue = true;
            }
        });
        if ($(".require.error").length > 0) {
            return true;
        }
        return isTrue;
    }

    $(document).on("focus", ".require.error", function () {
        $(this).removeClass('error').val('');
    });

    // 保存课程
    $("#save-btn").on("click", function () {
        if (isEmptyInput()) {
            return false;
        }
        var bannerImageNames = [],detailImageNames = [];
        $(".bannerImg .image").each(function () {
            bannerImageNames.push($(this).children("img").first().attr("src"));
        });

        $(".detailImg .image").each(function () {
            detailImageNames.push($(this).children("img").first().attr("src"));
        });

        var shopList = [];
        $(".shop-label").each(function () {
            var $this = $(this);
            shopList.push($this.data().sid);
        });
        $('#sid').val(shopList.join(','));
        $("#top-img").val($(".topImg .image").children("img").first().attr("src"));
        $("#banner-img").val(bannerImageNames.join(','));
        $("#detail-img").val(detailImageNames.join(','));
        $("#detail-form").ajaxSubmit(function (res) {
            if (res.success) {
                $.prompt("<div style='text-align:center;'>提交成功</div>", {
                    title: "操作提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        location.href = '/basic/goods/index.vpage';
                    }
                });
            } else {
                $.prompt("<div style='text-align:center;'>" + (res.info || "保存失败！") + "</div>", {
                    title: "错误提示",
                    buttons: {"确定": true},
                    focus: 1,
                    useiframe: true
                });
            }
        });
    });

    // 放弃编辑
    $(".abandon-btn").on("click", function () {
        $.prompt("<div style='text-align:center;'>确认放弃编辑？</div>", {
            title: "放弃编辑",
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    $.ajax({
                        type: 'POST',
                        url: '/basic/goods/abandonedit.vpage',
                        success: function (res) {
                            window.location.href = '/basic/goods/index.vpage';
                        },
                        error: function (res) {
                            window.location.href = '/basic/goods/index.vpage';
                        }
                    });
                }
            },
            useiframe: true
        });
    });

    // 批量到课
    $(".access-btn").on("click", function () {
        var $this = $(this);
        $.prompt("<div style='text-align:center;'>确认批量到课？</div>", {
            title: "批量到课",
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    var text = $("#accessMobiles").val();//获取id为ta的textarea的全部内容
                    var mobileArray = text.split("\n");
                    var data={
                        mobiles : mobileArray.join(','),
                        goodsId : $this.attr("data-goodsId")
                    };
                    $.post("/crm/reserve/access_by_mobiles.vpage",data,function(res){
                        if(res.success){
                            window.location.reload();
                        }else{
                            $.prompt("<div style='text-align:center;'>"+(res.info||"变更状态失败！")+"</div>", {
                                title: "错误提示",
                                buttons: { "确定": true },
                                focus : 1,
                                useiframe:true
                            });
                        }
                    });
                }
            },
            useiframe: true
        });
    });

    /*********       课程编辑结束          *********/

});