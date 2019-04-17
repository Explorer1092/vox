/*-----------功能权限管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform","fancytree"],function($){

    var isNew = $('[name="is-new"]').val();
    var pathId =  $("[name='id']").val();

    /*分页插件*/
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


    /*------------------------列表页------------------------------*/
    $("#index-filter").on("click",function(){
        $("#index-form").submit();
    });

    $("#index-add").on("click",function(){
        location.href = "addindex.vpage";
    });

    $("a[id^='delete_path_']").live('click',function(){
        var id = $(this).attr("id").substring("delete_path_".length);
        if(!confirm("确定要删除此条记录?")){
            return false;
        }
        $.post('delsyspath.vpage',{
            id:id
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                location.href = "index.vpage";
            }
        });
    });

    /*------------------------保存功能权限------------------------------*/
    var requireInputs = $(".require");
    function isEmptyInput(){
        var isTrue = false;
        requireInputs.each(function(){
            if($(this).val() == ''){
                $(this).addClass("error").val("请填写"+$(this).attr("data-title"));
                isTrue = true;
            }
        });
        return isTrue;
    }

    $("#save-btn").on("click",function(){
        if(isEmptyInput()){
            return false;
        }
        /*var roles = $(".role-checkbox");
        if(roles.length > 0){
            var roles = [];
            $(".role-checkbox:checked").each(function(){
                roles.push($(this).attr("data-value"));
            });
            $("[name='roles']").val(roles.join(','));
        }*/

        var roleGroups = [];
        var roleGroupTree = $("#role-tree").fancytree("getTree");
        var selectedNodes = roleGroupTree.getSelectedNodes();
        $.each(selectedNodes,function(index,node){
            roleGroups.push(node.key);
        });
        $("[name='roleGroupIds']").val(roleGroups.join(','));

        $("#detail-form").ajaxSubmit(function(res){
            if(res.success){
                location.href = "/config/syspath/index.vpage";
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

    $(".op-status").on("click",function(){
        var $this = $(this);
        var data = {
            gid : $this.attr("data-gid"),
            status : $this.attr("data-status")
        };
        $.post("/goods/changestatus.vpage",data,function(res){
            if(res.success){
                location.reload();
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

    $(document).on("focus",".require.error",function(){
        $(this).removeClass('error').val('');
    });



    $("#role-tree").fancytree({
        source: {
            url: "/config/syspath/loadroletree.vpage?pathId=" + pathId,
        },
        checkbox: true,
        selectMode: 2,
        icon:false,
        init:function(node,tree){
            var tree = $("#role-tree").fancytree("getTree");
            tree.visit(function(node){
                var nodeTitle = node.title;
                var type = node.data.type;
                node.icon = false;
                if(type && type == "role"){
                    node.setTitle('<i class="fa fa-user">'+nodeTitle+'</i>');
                }else{
                    node.setTitle('<i class="fa fa-cube">'+nodeTitle+'</i>');
                }
            });
        }
    });
});