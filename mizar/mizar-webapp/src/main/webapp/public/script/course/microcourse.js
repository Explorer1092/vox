/**
 * Created by free on 2016/12/13.
 */
define(["jquery","prompt","paginator"],function ($) {

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
                    $('#searchBtn').click();
                }
            }
        });
    }

    //查询
    $(document).on('click','#searchBtn',function(){
        var $href = "/course/manage/index.vpage?"+"status="+$('#status').val()+"&course="+$("#course").val();
        var vc = $('#category').val();
        if (vc != undefined) {
            $href  += "&category="+vc;
        }
        $href  += "&page=" + $('#pageIndex').val();
        location.href = $href;
    });

    $(document).on('change','#status',function(){
       $('#pageIndex').val(1);
    });

    //下架/上线
    $(document).on('click','.js-changeStatusBtn',function(){
        var $this = $(this),
            text = $this.text(),
            status = $this.data("type").toUpperCase(),
            cid = $this.data("cid");
        $.prompt('<p style="text-align: center;">确定要'+text+'该课程吗？</p>',{
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('changestatus.vpage',{
                        'course':cid,
                        'status':status
                    },function(res){
                        var text = '操作成功';
                        if(!res.success){
                            text = res.info;
                        }
                        $.prompt('<p style="text-align: center;">'+text+'</p>',{
                            title: "温馨提示",
                            buttons: {"确定": true},
                            focus: 1,
                            submit: function (e, v) {
                                if(v){
                                    if(res.success){
                                        location.reload();
                                    }
                                }
                            }
                        });
                    });
                }
            }
        });
    });

    // 删除
    $(document).on('click','.js-delBtn',function(){
        var $this = $(this),
            cid = $this.data("cid");
        $.prompt('<p style="text-align: center;">确定要删除该课程吗？</p>',{
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('removecourse.vpage',{
                        'courseId':cid
                    },function(res){
                        var text = '删除成功';
                        if(!res.success){
                            text = res.info;
                        }
                        $.prompt('<p style="text-align: center;">'+text+'</p>',{
                            title: "温馨提示",
                            buttons: {"确定": true},
                            focus: 1,
                            submit: function (e, v) {
                                if(v){
                                    if(res.success){
                                        location.reload();
                                    }
                                }
                            }
                        });
                    });
                }
            }
        });
    });

});