(function (window) {
    "use strict";
    var $WIN = window;
    if (typeof $WIN === "undefined") {
        $WIN = {};
    }
    $WIN.MZ = {};
    function isRquireEmpty() {
        var isTrue = false;
        var requireInputs = $(".require");
        requireInputs.each(function(){
            if($(this).val() == ''){
                if($(this).attr('tipId')){
                    var that = $('#'+$(this).attr('tipId'));
                    $(that).addClass("error");
                }else{
                    $(this).addClass("error").val("请填写"+$(this).attr("data-title"));
                }
                isTrue = true;
            }else{
                if($(this).attr('tipId')){
                    var that = $('#'+$(this).attr('tipId'));
                    $(that).removeClass("error");
                }else{
                    $(this).removeClass("error");
                }
            }

        });
        return isTrue;
    }
    function showPager(paginator){
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
    }
    function _extend(child, parent) {
        var $key;
        for ($key in parent) {
            if (parent.hasOwnProperty($key)) {
                child[$key] = parent[$key];
            }
        }
    }

    _extend($WIN.MZ, {
        isRquireEmpty: isRquireEmpty,
        showPager: showPager
    });

    if (typeof(module) !== 'undefined') {
        module.exports = $WIN.MZ;
    } else if (typeof define === 'function' && define.amd) {
        define([], function () {
            'use strict';
            return $WIN.MZ;
        });
    }
}(window));
