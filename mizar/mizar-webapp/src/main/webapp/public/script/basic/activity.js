/*-----------课程管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform","template"],function($){

    /*********       活动列表相关          *********/
    // 分页
    var paginator = $('#paginator');
    if(paginator.length>0){
        paginator.jqPaginator({
            totalPages:parseInt(paginator.attr("totalPage")),
            visiblePages: 10,
            currentPage: parseInt(paginator.attr("pageIndex")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex,opType) {
                if(opType=='change'){
                    $('#pageIndex').val(pageIndex);
                    $('#filter-form').submit();
                }
            }
        });
    }

    // 变更状态
    $(".op-status").on("click",function(){
        var $this = $(this);
        var data = {
            gid : $this.attr("data-gid"),
            status : $this.attr("data-status")
        };
        $.post("/basic/activity/changestatus.vpage",data,function(res){
            if(res.success){
                $.prompt("<div style='text-align:center;'>状态更新成功</div>", {
                    title: "操作提示",
                    buttons: { "确定": true },
                    focus : 1,
                    submit: function (e, v) {
                        if (v) {
                            location.reload();
                        }
                    },
                    useiframe:true
                });
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"状态更新失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    /*********       活动列表结束          *********/

    /*********       活动详情相关          *********/
    // 上传照片
    var fileInput = $("#upload-image");
    var uploadImgFrom = '';
    $(".upload-image").on("click",function(){
        uploadImgFrom = $(this);
        fileInput.click();
    });
    fileInput.on("change",function(){
        $(".upload-image").addClass("count-down").html("上传中…");
        $(this).parent().ajaxSubmit(function(res){
            if(res.success){
                $(".upload-image").removeClass("count-down").html("上传照片");
                fileInput.val("");
                uploadImgFrom.closest('.input-control').siblings(".image-preview").html('<div class="image"><img src="' + res.imgUrl + '" /><div class="del-btn">删除</div></div>');
            }else{
                $.prompt("<div style='text-align:center;'>"+res.info+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    submit: function( e,v ){
                        if ( v ) {
                            $(".upload-image").removeClass("count-down").html("上传照片");
                        }
                    },
                    useiframe:true
                });
            }
        });
    });

    // 删除照片
    var gid = $("#goods-id").val();
    $(document).on("click",".del-btn",function(){
        var $this = $(this);
        var src = $this.prev().attr("src");
        $.prompt("<div style='text-align:center;'>确认删除这张照片？<br /><img style='width:143px;height:109px;margin-top:20px;' src='"+src+"' /></div>", {
            title: "删除照片",
            buttons: { "取消": false, "确定": true },
            submit: function( e,v ){
                if ( v ) {
                    $this.parent().remove();
                }
            },
            useiframe:true
        });
    });

   // 校验输入
    var requireInputs = $(".require");
    function isEmptyInput(){
        var isTrue = false;
        requireInputs.each(function(){
            if($(this).val() == ''){
                $(this).addClass("error").val("请填写"+$(this).attr("data-title"));
                isTrue = true;
            }
        });
        if ($(".require.error").length > 0) {
            return true;
        }
        return isTrue;
    }

    $(document).on("focus",".require.error",function(){
        $(this).removeClass('error').val('');
    });

    // 添加产品类型
    $('.add-category').on('click', function() {
        $('#itemList').append(template("T:category", {}));
    });

    // 删除产品类型
    $(document).on("click", ".del-line", function() {
        $(this).parent().parent().remove();
    });

    // 保存活动
    $("#save-btn").on("click",function(){
        if(isEmptyInput()){
            return false;
        }
        var bannerImageNames = [],detailImageNames = [];
        $(".bannerImg .image").each(function () {
            bannerImageNames.push($(this).children("img").first().attr("src"));
        });

        $(".detailImg .image").each(function () {
            detailImageNames.push($(this).children("img").first().attr("src"));
        });

        $("#banner-img").val(bannerImageNames.join(','));
        $("#detail-img").val(detailImageNames.join(','));

        //处理新增类型
        var productTypeList = [];
        $('#itemList').find('tr').each(function () {
            var that = $(this);
            var map = {
                itemId : that.find('.data-itemId').val(),
                categoryName: that.find('.data-categoryName').val(),
                itemName: that.find('.data-itemName').val(),
                price: that.find('.data-price').val(),
                inventory: that.find('.data-inventory').val(),
                remains: that.find('.data-inventory').val()
            };
            productTypeList.push(map);
        });
        $("#productType").val(JSON.stringify(productTypeList));

        $("#detail-form").ajaxSubmit(function(res){
            if(res.success){
                $.prompt("<div style='text-align:center;'>保存成功，请等待管理员审核</div>", {
                    title: "操作提示",
                    buttons: { "确定": true },
                    focus : 1,
                    submit: function (e, v) {
                        if (v) {
                            location.href = "/basic/activity/index.vpage";
                        }
                    },
                    useiframe:true
                });
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"保存失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    /*********       活动详情相关          *********/

    /*********       地图相关          *********/
    if (typeof AMap != "undefined") {
        //首先判断对象是否存在
        var longitude = $('#longitude').val();
        var latitude = $('#latitude').val();
        var map = new AMap.Map('innerMap', {
            resizeEnable: true,
            zoom: 12
        });

        var marker = '';
        if(longitude != '' && latitude != ''){
            map.setCenter([longitude,latitude]);
            marker = new AMap.Marker({map: map, position: [longitude,latitude], animation: 'AMAP_ANIMATION_DROP'});
        }

        map.on("click", function (e) {
            //预览页面禁止重新标记
            var disable = $('#innerMap').data('disable') || false;
            if (!disable) {
            } else {
                return false;
            }

            $('#longitude').val(e.lnglat.getLng());
            $('#latitude').val(e.lnglat.getLat());
            $('#baiduGps').attr("checked", true);

            if(marker != ''){
                marker.setMap(null);//清空已有的标记
            }
            marker = new AMap.Marker({map: map, position: e.lnglat, animation: 'AMAP_ANIMATION_DROP'});

            AMap.service('AMap.Geocoder',function(){//回调函数
                var geocoder = new AMap.Geocoder();
                geocoder.getAddress(e.lnglat, function (status, result) {
                    if (status === 'complete' && result.info === 'OK') {
                        $("#address").val(result.regeocode.formattedAddress);
                    }
                });
            })
        });
    }
    /*********       地图结束          *********/

    /*********       富文本编辑相关          *********/
    if(typeof UE !='undefined'){

        var ue_activity = UE.getEditor('activity_area', {
            serverUrl: "/basic/activity/ueditorcontroller.vpage",
            autoHeightEnabled: false,
            zIndex: 0,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });

        var ue_expense = UE.getEditor('expense_area', {
            serverUrl: "/basic/activity/ueditorcontroller.vpage",
            autoHeightEnabled: false,
            zIndex: 0,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });
    }
    /*********       富文本编辑结束          *********/

});