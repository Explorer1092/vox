define(["jquery", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {
    $("#album_search").on("click", function (e) {
        var albumName = $("#albumName").val();
        var pageIndex = $("#pageIndex").val();
        if(e.hasOwnProperty("originalEvent")){
            pageIndex = 1;
        }
        location.href = '/basic/albumnews/index.vpage?albumName=' + albumName + "&pageIndex=" + pageIndex;
    });
    $("a[name='edit_album']").on("click", function () {
        var albumId = $(this).attr("data-albumId");
        location.href = '/basic/albumnews/albumedit.vpage?albumId=' + albumId;
    });
    // 分页插件
    var paginator = $('#paginator');
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages:parseInt(paginator.attr("totalPage")),
            visiblePages: 5,
            currentPage: parseInt(paginator.attr("pageIndex")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex,opType) {
                if(opType=='change'){
                    $('#pageIndex').val(pageIndex);
                    $('#album_search').trigger("click");
                }
            }
        });
    }
});