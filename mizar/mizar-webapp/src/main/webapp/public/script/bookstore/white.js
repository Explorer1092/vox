/*-----------书店管理相关-----------*/
define(["jquery", "prompt","paginator"], function ($) {

    //获取回车事件
    $('#comments').keypress(function(event){
        var keycode = (event.keyCode ? event.keyCode : event.which);
        var areaRows = $("#comments").val().split("\n").length; //获取行数
        if(keycode != '' && areaRows<=10){ //小于一千行
            $('#areaRows').html(areaRows);//替换默认数量0
        }
        else{
            $('#errorText').show();//显示错误提示信息
            return false;
        }
    });

    //鼠标离开事件
    $('#comments').blur(function(){
        var areaRows = $("#comments").val().split("\n").length;
        $('#areaRows').html(areaRows);
        if($("#area").val() == 0){//如果为空时
            $('#areaRows').html(0);//恢复初始值
        }
    });
    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }
    $(document).ready(function(){

        $("#add-save-newpage").click(function () {
            // 发起请求
            // 匹配换行和字符
            var reg = /^(\d*\n*)*$/
            var textareaValue = $("#comments").val();
            if(textareaValue.length  == 0){
                $.prompt(("请输入名店ID"), {
                    title: "提示",
                    buttons: {"知道了": false}
                });
                return false
            }
            var c;
            for (var i = 0; i < textareaValue.length; i++)
            {
                c = textareaValue.substr(i, 1);
                if(!reg.test(c)){
                    $.prompt(("名店ID请输入数字"), {
                        title: "提示",
                        buttons: {"知道了": false}
                    });
                    return false
                }else{
                    if (c == "\n")
                    {
                        textareaValue = textareaValue.replace("\n", ",");
                    }
                }

            }
            textareaValue = textareaValue.replace(/,{2,}/g, "")
            var mydata = {
                "id":getQuery("id"),
                "content": textareaValue.substr(0, textareaValue.length ),
                "remark" :$("#remark").val()
            }

            $.ajax({
                url: '/bookstore/manager/whiteList/add.vpage',
                type: 'POST',
                data: mydata,
                success: function (res) {
                    if (res.success) {
                        location.href = '/bookstore/manager/whiteList/list.vpage';
                    } else {
                        alert(res.info);
                    }
                }
            });

        });


        //分页插件
        var paginator = $('#paginator');
        if (paginator.length > 0) {
            paginator.jqPaginator({
                totalPages: parseInt(paginator.attr("totalPages")),
                visiblePages: 5,
                currentPage: parseInt(paginator.attr("pageIndex") || 1),
                first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
                prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
                next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
                last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
                page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
                onPageChange: function (pageIndex, opType) {
                    if (opType == 'change') {
                        $('#pageIndex').val(pageIndex);
                        $('#js-filter').trigger("click");
                        location.href = '/bookstore/manager/whiteList/list.vpage?page='+pageIndex;
                    }
                }
            });
        }


    });


});