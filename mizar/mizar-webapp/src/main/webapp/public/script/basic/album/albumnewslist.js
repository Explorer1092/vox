define(["jquery", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {
    //查询
    $("#news_search").on("click", function (e) {

        var newsTitle = $("#newsTitle").val();
        var work_flow_status = $("#work_flow_status option:selected").val();
        var content_type = $("#content_type option:selected").val();
        var pageIndex = $("#pageIndex").val();
        if(e.hasOwnProperty("originalEvent")){
            pageIndex = 1;
        }
        location.href = '/basic/albumnews/news/index.vpage?newsTitle=' + newsTitle + "&workFlowStatus=" + work_flow_status + "&contentType=" + content_type + "&pageIndex=" + pageIndex;
    });
    //新建/编辑
    $("a[name='edit_news']").on("click", function () {
        var albumlist = $(this).data('album_list');
        if (albumlist == 'has') {
            var newsId = $(this).attr("data-newsId");
            location.href = '/basic/albumnews/news/albumnewsedit.vpage?newsId=' + newsId;
        } else {
            alert("您没有上线的专辑,不能新建文章！");
            return false;
        }
    });
    //提交审核
    $(".add_work_flow").on("click", function () {
        var newsId = $(this).attr("data-newsId");
        if (confirm("当前文章是否提交审核")) {
            $.post("/basic/albumnews/news/addworkflow.vpage", {"newsId": newsId}, function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    $("#news_search").trigger("click");
                }
            })
        }

    });
    //预览
    $("#pre_view").on("click", function () {
        var newsId = $(this).attr("data-newsId");
    });

    //手机预览
    $(document).on("click", ".JS-viewBtn", function(){
        var lcHost = location.host;
        if(lcHost == "mizar.test.17zuoye.net"){
            lcHost = "www.test.17zuoye.net";
        }else if(lcHost == "mizar.staging.17zuoye.net"){
            lcHost = "www.staging.17zuoye.net";
        }else{
            lcHost = "www.17zuoye.com";
        }

        var $this = $(this);
        var $dataLink =  location.protocol + '//' + lcHost + '/view/mobile/parent/information/detail?newsId=' + $this.attr("data-id");

        $.prompt('<iframe src="'+ $dataLink +'" style="margin: -40px -20px -20px; position: relative; z-index: 2;" allowtransparency="true" frameborder="0" width="320" height="480" scrolling="auto"></iframe>', {
            position: {width: 320},
            title: "手机预览",
            buttons: {}
        });
    });

    // 分页插件
    var paginator = $('#paginator');
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: parseInt(paginator.attr("totalPage")),
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
                    $('#news_search').trigger("click");
                }
            }
        });
    }
});