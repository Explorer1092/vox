/*-----------课程管理相关-----------*/
define(["jquery", "prompt", "datetimepicker", "paginator", "jqform", "template"], function ($) {


    /*查询*/
    $("#js-filter").on("click", function () {
        $('#page').val(0)
        $("#filter-form").submit();
    });

    /** 专题类型选择事件**/
    $(document).ready(function() {
        $('input[type=radio][name=type]').change(function() {
            if (this.value == 'to_detail') {
                $('#special_goods_div').show();//商品列表
                $('#special_detailImg_div').show();//专题头图
                $('#url').attr("readonly","readonly");
                $('#urlDiv').hide();//url后台生成,不需要输入
                $('#url').removeClass("require");
            }
            else if (this.value == 'outer_url') {
                $('#special_goods_div').hide();//商品列表
                $('#special_detailImg_div').hide();//专题头图
                $('#urlDiv').show();
                $('#url').val("https://");
                $('#url').removeAttr("readonly");
                $('#url').addClass("require");
            }
        });
    });
    /*分页插件*/
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: parseInt(paginator.attr("data-totalPage")),
            visiblePages: 10,
            currentPage: parseInt(paginator.attr("data-startPage")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',

            onPageChange: function (pageIndex, opType) {
                if(opType=='change'){
                    $('#page').val(pageIndex)
                    $("#filter-form").submit();
                }
            }
        });
    }

    var startTime = $('#startTime'),
        endTime   = $('#endTime'),
        endStart   = $('#endStart'),
        endEnd   = $('#endEnd');
    /*时间控件*/
    startTime.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd hh:ii:ss',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        minView:0,
        changeMonth: false,
        changeYear: false,
        autoclose:true
    });
    endTime.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd hh:ii:ss',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        autoclose:true
    });

    endStart.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd hh:ii:ss',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        autoclose:true
    });
    endEnd.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd hh:ii:ss',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        autoclose:true
    });
    /*上传照片*/
    var fileInput = $("#upload-image");
    var uploadImgFrom = '';
    $(".upload-image").on("click", function () {
        uploadImgFrom = $(this);
        fileInput.click();
    });
    fileInput.on("change", function () {
        $(".upload-image").addClass("count-down").html("上传中…");
        if($(uploadImgFrom).attr("refId") && $(uploadImgFrom).attr("refId")=='cover-img'){
            $(this).parent().attr("action","/groupon/topic/uploadcheckphoto.vpage?position="+$("#position").val());
        }
        $(this).parent().ajaxSubmit(function (res) {
            if (res.success) {
                $(".upload-image").removeClass("count-down").html("上传照片");
                fileInput.val("");
               var targetId= $(uploadImgFrom).attr("refId");
                $('#'+targetId).val(res.imgUrl);
                uploadImgFrom.closest('.input-control').siblings(".image-preview").html('<div class="image"><img src="' + res.imgUrl + '" /><div class="del-btn">删除</div></div>');
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

    /*------------------------新建课程------------------------------*/
    var requireInputs = $(".require");
    var gid = $("#goods-id").val();
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

    function isEmptyInput() {
        var requireInputs = $(".require");
        var isTrue = false;
        requireInputs.each(function(){
            if($(this).val() == ''){
                $(this).addClass("error").val("请填写"+$(this).attr("data-title"));
                isTrue = true;
            }
        });
        return isTrue;
    }

    $("#save-btn").on("click", function () {
        if (isEmptyInput()) {
            return false;
        }

        $("#detail-form").ajaxSubmit(function (res) {
            if (res.success) {
                $.prompt("<div style='text-align:center;'>保存成功</div>", {
                    title: "操作提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        location.href = '/groupon/topic/detail.vpage?tid='+res.id;
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

    $(".op-status").on("click", function () {
        var $this = $(this);
        var data = {
            tid: $this.attr("data-tid"),
            status: $this.attr("data-status")
        };
        $.post("/groupon/topic/changestatus.vpage", data, function (res) {
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
    });

    $(document).on("focus", ".require.error", function () {
        $(this).removeClass('error').val('');
    });

    $(".groupon-btn").on('click', function() {
        $.prompt("<div style='text-align:center;'>输入逗号分割的商品ID<br /><div class='input-control' style='margin:20px auto 0;width:300px;'><textarea class='new-mark' id='content' style='resize: none;width: 300px;' placeholder='AAA1,BBB2,CCC3'></textarea></div></div>", {
            title: "添加专题商品",
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    var content = $("#content");

                    if(content.val() == ''){
                        return false;
                    }
                    var $table = $('#goodsList');
                    var $input = $('#goods-input');
                    $.post("/groupon/topic/searchgoods.vpage", {goods: content.val()}, function (res) {
                        if(res.success){
                            //成功
                            var dataList = res.goodsList;
                            for (var i=0; i<dataList.length; ++i) {
                                var gid = dataList[i].id;
                                var str = "<tr id='row_"+gid+"'><td style='width:140px;'>"+gid+
                                    "</td><td>"+dataList[i].title+"</td><td style='width:70px;'>" +
                                    "<a data-gid='"+gid+"' class='op-btn del-goods' href='javascript:void(0)' style='float: none; display: inline-block;'>删除</a></td></tr>";
                                $table.append(str);
                                $input.append("<input id='input_'"+gid+" name='grouponGoodsIdList' type='hidden' value='"+gid+"'>");
                            }
                        }else{
                            $.prompt((res.info || "输入错误"), {
                                title: "提示",
                                buttons: {"知道了": false}
                            });
                        }
                    });
                }
            }
        });
    });

    $(document).on('click', '.del-goods', function() {
        var gid = $(this).data().gid;
        $('#row_'+gid).remove();
        $('#input_'+gid).remove();
    });
});