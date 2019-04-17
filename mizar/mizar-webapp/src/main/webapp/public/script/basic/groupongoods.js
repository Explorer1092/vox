/*-----------课程管理相关-----------*/
define(["jquery","commonJs","prompt","datetimepicker","paginator","jqform"],function($,mz){
    var deployTime = $('#deployTime');
   var beginTime   = $('#beginTime');
   var endTime   = $('#endTime');
    /*时间控件*/
    deployTime.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd hh:ii:ss',
        autoclose:true
    });
    /*时间控件*/
    beginTime.datetimepicker({
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
    /*上传照片*/
    var fileInput = $("#upload-image");
    var uploadedCount = 0;
    $(".upload-image").on("click",function(){
        if(uploadedCount>=1){
            $.prompt("<div style='text-align:center;'>一次最多只能上传1张照片,请删除后再上传!</div>", {
                title: "温馨提示",
                buttons: { "确定": true },
                focus : 1,
                submit: function( e,v ){
                    if ( v ) {
                        $(".upload-image").removeClass("count-down").html("上传照片");
                    }
                },
                useiframe:true
            });
        }else{
            fileInput.click();
        }
    });
    fileInput.on("change",function(){
        $(".upload-image").addClass("count-down").html("上传中…");
        $(this).parent().ajaxSubmit(function(res){
            if(res.success){
                $(".upload-image").removeClass("count-down").html("上传照片");
                uploadedCount+=1;
                $(".image-preview").append('<div class="image"><img src="'+res.imgUrl+'" /><div class="del-btn">删除</div></div>');
                $('#detail-img').val(res.imgUrl)
                //alert( $('#detail-img').val());
            }else{
                $.prompt("<div style='text-align:center;'>"+res.info+"</div>", {
                    title: "提示",
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
    $('input[name="orderIndex"]').each(function(index) {
        $(this).bind("change", function () {
            var v = $(this).val();//获取选中option的vlaue
            var goodsId =$(this).attr('goodsId')
            $.ajax({
                type: 'POST',
                url: '/groupon/goods/addgoods.vpage' ,
                data: {'id':goodsId,'orderIndex':v} ,
                dataType: 'json',
                success:function(data) {
                    if(data.success){
                        return true;
                    }else{
                        return false;
                    }
                },
                error : function() {
                    alert("异常！");
                    return false;
                }
            });
        });
    });

    $(document).on("click",".del-btn",function(){
        var $this = $(this);
        var src = $this.prev().attr("src");
        $.prompt("<div style='text-align:center;'>确认删除这张照片？<br /><img style='width:143px;height:109px;margin-top:20px;' src='"+src+"' /></div>", {
            title: "删除照片",
            buttons: { "取消": false, "确定": true },
            submit: function( e,v ){
                if ( v ) {
                    uploadedCount--;
                    $this.parent().remove();
                }
            },
            useiframe:true
        });
    });

    $("#grab-btn").on("click",function(){
        var inputUrl = $("#originUrl").val();
        //alert(new Date().getTime()+" inputUrl="+inputUrl)
        if(inputUrl==''){
            $.prompt("<div style='text-align:center;'>请填写原始url</div>", {
                title: "提示",
                buttons: { "确定": true },
                focus : 1,
                useiframe:true
            });
            return false;
        }
        location.href = "/groupon/goods/detail.vpage?inputUrl="+encodeURIComponent(inputUrl);
    });


    /*** ueditor ************/
    if(typeof UE !='undefined'){

    var ue = UE.getEditor('content_area', {
        serverUrl: "/groupon/goods/ueditorcontroller.vpage",
        zIndex: 999,
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
    /*预约管理分页插件*/
    var paginator = $('#paginator');
    if(paginator.length>0){
        paginator.jqPaginator({
            totalPages:parseInt(paginator.attr("totalPage")),
            visiblePages: 10,
            currentPage: parseInt(paginator.attr("pageIndex")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页(1)<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页({{totalPages}})<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex,opType) {
                if(opType=='change'){
                    location.href=$('#filter-form').attr('action')+"?pageIndex="+(pageIndex-1)+"&"+encodeURIComponent($("#filter-form").serialize());
                }
            }
        });
    }

    //查询
    $("#js-filter").on("click",function(){
        $("#filter-form").submit();
    });
    
    /*------------------------新建课程------------------------------*/
    var gid = $("#goods-id").val();
    $("#save-btn").on("click",function(){
        if(mz.isRquireEmpty()){
            return false;
        }
        $("#detail-form").ajaxSubmit(function(res){
            if(res.success){
                location.href = "/group/category/list.vpage";
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"保存失败！")+"</div>", {
                    title: "提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    $(document).on("focus",".require.error",function(){
        $(this).removeClass('error').val('');
    });
});