/**
 * Created by free on 2016/12/13.
 */
define(["jquery","prompt","paginator","jqform","fancytree"],function ($) {

    // 分页插件
    var paginator = $('#paginator');
    if (paginator.length > 0) {
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
                    $('#pageNum').val(pageIndex);
                    $('#pagerForm').submit();
                }
            }
        });
    }

    $('#searchBtn').on('click', function () {
        $('#pagerForm').submit();
    });

    $(document).on('click','#chooseBtn',function(){
        $('#choose-form').submit();
    });

    // 选择课时
    $('#chooseId').on("click", function () {
        $.prompt('<iframe src="/course/manage/choose.vpage" width="100%" height="560px" style="border:none;"/>', {
            title: "选择课时",
            buttons: {"关闭": false},
            position: {width: 1000}
        });
    });

    // 选择课时
    $(document).on("click", "tr[name='recordRow']", function () {
        var theme = $(this).data('theme');
        var periodId = $(this).data('pid');
        parent.$('#chooseId').val(periodId);
        parent.$('#theme').val(theme);
        parent.$.prompt.close();
    });

    var cPaginator = $('#choose-paginator');
    if (cPaginator.length > 0) {
        cPaginator.jqPaginator({
            totalPages:parseInt(cPaginator.attr("totalPage")||1),
            visiblePages: 10,
            currentPage: parseInt(cPaginator.attr("pageIndex")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex,opType) {
                if(opType=='change'){
                    $('#choosePage').val(pageIndex);
                    $('#choose-form').submit();
                }
            }
        });
    }

    $(document).on("click",".js-catBtn",function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        $('#category').val($(this).data("cat"));
    });

    $(document).on("click",".js-colorBtn",function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        $('#background').val($(this).data("color"));
    });

    $(document).on("click",".js-tagBtn",function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        $('#subTitle').val($(this).data("tag"));
    });

    $(document).on("click",".js-stBtn",function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        $('#status').val($(this).data("st"));
    });

    //上传图片
    $(document).on('change','.js-classPic',function(){
        var file = this.files[0];
        if(file){
            var fileSize = file.size,fileType = file.type;
            if(fileType.indexOf('image') != -1){
                if(fileSize < 5*1024*1024){
                    var postData = new FormData();
                    postData.append('file', file);
                    $.ajax({
                        url: "/common/uploadfile.vpage",
                        type: "POST",
                        data: postData,
                        processData: false,
                        contentType: false,
                        success: function (res) {
                            if(res.success){
                                var trail = '';
                                if(res.fileName.indexOf('oss-image') != -1){
                                    trail = '@1e_1c_0o_0l_720h_300w_80q'
                                }
                                $('#imgDiv').html('<img src="'+res.fileName+trail+'" width="720px" height="300px">');
                                $("#classPic").val(res.fileName);
                            }else{
                                alert(res.info);
                            }
                        },
                        error: function (e) {
                            console.log(e);
                        }
                    });
                }else{
                    $.prompt('请上传小于5M的图片',{
                        title: "温馨提示",
                        buttons: {"确定": true},
                        focus: 1,
                        submit: function (e, v) {
                            if (v) {}
                        }
                    });
                }
            }else{
                $.prompt('请上传图片文件',{
                    title: "温馨提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if (v) {}
                    }
                });
            }
        }
    });

    //提交保存
    $(document).on("click","#submitBtn",function(){
        $("#courseForm").ajaxSubmit(function(res){
            if(res.success){
                $.prompt("<div style='text-align:center;'>保存成功</div>", {
                    title: "操作提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true,
                    submit: function (e, v) {
                        if (v) {
                            location.href = "/course/manage/itemdetail.vpage?itemId="+res.courseId;
                        }
                    }
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

    // 删除列表
    $(document).on("click",".js-delBtn",function(){
       var itemId = $(this).data('item');
        $.prompt('<p style="text-align: center;">确定要删除吗？</p>',{
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('deleteitem.vpage',{
                        'itemId':itemId
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

    // 选择投放策略
    $('#configBtn').on("click", function () {
        $.prompt('<iframe src="/course/manage/itemconfig.vpage?itemId='+$(this).data("item")+ '" width="100%" height="400px;" style="border:none;"/>', {
            title: "配置投放策略",
            buttons: {"关闭": false},
            position: {width: 600},
            submit: function (e, v) {
                parent.window.location.reload();
            }
        });
    });

    var regionTree = $("#regionTree");
    if(regionTree.length > 0){
        regionTree.fancytree({
            source: targetRegion,
            checkbox: true,
            selectMode: 2
        });
    }

    $(document).on('click', ".configTab", function() {
        var $this = $(this);
        $('#targetType').val($this.data('type'));
        $this.addClass("active").siblings().removeClass("active");
        reload();
    });

    var reload = function () {
       $(".configTab").each(function() {
          var $this = $(this);
          if ($this.hasClass("active")) {
              $('#'+$this.data("modal")).show();
          } else {
              $('#'+$this.data("modal")).hide();
          }
       });
    };

    $(document).on('click', "#saveConfig", function() {
       var type = $('#targetType').val();
        if (type == 1) {
            var regionList = [];
            var regionTree = $("#regionTree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            $.map(regionNodes, function (node) {
                regionList.push(node.key);
            });
            $.post('saveregion.vpage', {
                itemId: courseId,
                type: type,
                regionList: regionList.join(",")
            }, function (data) {
                if (data.success) {
                    alert("保存区域成功！");
                    window.location.reload();
                } else {
                    alert("保存区域失败:" + data.info);
                }
            });
        } else if (type == 2) {
            var schoolIds = $('#schools').val().trim();
            $.post('savetarget.vpage', {
                itemId: courseId,
                type: type,
                targetIds: schoolIds,
                append:false
            }, function (data) {
                if (data.success) {
                    alert("保存学校成功！");
                    window.location.reload();
                } else {
                    alert("保存学校失败:" + data.info);
                }
            });
        } else if (type == 3) {
            $.post('savetarget.vpage', {
                itemId: courseId,
                type: type,
                targetIds: "true"
            }, function (data) {
                if (data.success) {
                    alert("投放所有用户保存成功！");
                    window.location.reload();
                } else {
                    alert("投放所有用户保存失败:" + data.info);
                }
            });
        } else {
            alert("请选择投放策略");
        }
    });

    $(document).on('click', "#clearConfig", function() {
        var type = $('#targetType').val();
        $.post('cleartargets.vpage', {
            courseId: courseId,
            type: type
        }, function (data) {
            if (data.success) {
                alert("清除成功！");
                window.location.reload();
            } else {
                alert("清除失败:" + data.info);
            }
        });
    });

});