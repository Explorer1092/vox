<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_300,h_375/auto-orient,1">
<@layout.page title="照片库" pageJs="" footerIndex=2 navBar="hidden">
    <@sugar.capsule css=['school']/>
<style>
</style>
<div class="photoLibrary-box">
    <a href="javascript:void(0);" class="add_btn"><img width="100%" height="100%" src="" alt=""></a>
</div>
<script>
    var schoolId = ${schoolId!0};
    var teacherId = ${teacherId!0};
    var page = 1;
    var isLoad = false;
    var isOver = false;
    var vox = vox || {};
    vox.task = vox.task || {};
    $(function () {
        addmemorandum();
    });
   /* $(document).on('click','.js-submit',function(){

    });*/

    var saveMemorandum = function (content){
        $.post('add_memorandum.vpage',{
            schoolId: schoolId,
            teacherId: teacherId,
            content: content,
            type: "picture"
        },function(res){
            if (res.success){
                AT.alert('保存成功');
                remove();
                addmemorandum(1)
            }else{
                AT.alert(res.info)
            }
        });
    };

    var remove = function () {
        isLoad = false;
        isOver = false;
        $(".photoLibrary-box").html("");
        $(".photoLibrary-box").html('<a href="javascript:void(0);" class="add_btn"><img width="100%" height="100%" src="" alt=""></a>');
    };

    $(window).scroll(function () {
        var scrollTop = $(this).scrollTop();
        var scrollHeight = $(document).height();
        var windowHeight = $(this).height();
        clearTimeout($.data(this, 'scrollTimer'));
        if (scrollTop + windowHeight >= scrollHeight - 15 && !isLoad && !isOver) {
            addmemorandum();
        }
    });
    $(document).on('click','.m_content',function(){
        var id = $(this).data('id');
        location.href = "update_memorandum_page.vpage?id=" +id ;
    });
    var addmemorandum = function (newPage) {
        if (!isLoad && !isOver) {
            isLoad = true;
            var url = "";
            if (schoolId != 0) {
                url = "find_school_memorandum.vpage";
            }
            if (teacherId != 0) {
                url = "find_teacher_memorandum.vpage";
            }
            if(newPage){
                page = newPage;
            }
            $.post(url, {schoolId: schoolId, teacherId: teacherId, page: page, type: "picture"}, function (res) {
                if (res.success) {
                    isLoad = false;
                    page = res.page + 1;
                    isOver = res.isOver;
                    if (!isOver) {
                        //console.info(res);
                        if (res.list) {
                            res.list.forEach(function (value) {
                                var hasOrderFiled = false;
                                $(".orderFiled").each(function(){
                                    if(value.orderFiled == $(this).html()){
                                        hasOrderFiled = true;
                                        return false;
                                    }
                                });
                                if(!hasOrderFiled){
                                    $(".photoLibrary-box").append("<div class='time orderFiled'>" + (value.orderFiled) + "</div><ul class='photo-list'></ul>");
                                }
                                if (value.info) {
                                    value.info.forEach(function(infoValue){
                                        $('.photo-list').append("<li style='margin-top:.5rem'><div class='image' data-id='" + infoValue.id + "'><img src= '" + (infoValue.content) + "${shortIconTail}'></div></li>");
                                        if (infoValue.target) {
                                            $(".photoLibrary-box").append("<p >" + (infoValue.target) + "</p>");
                                        }
                                    });
                                }
                            })
                        }else{
                            alert('asd')
                        }
                    }
                } else {
                    AT.alert(res.info);
                }
            })
        }
    };
    $(document).on('click','.image',function(){
        var id = $(this).data('id');
        openSecond("/mobile/memorandum/update_memorandum_page.vpage?id=" +id );
    });
    $(document).on('click','.add_btn',function(){
        getSchoolImage();
    });
    var getSchoolImage = function(){
        var carData =  {
            uploadUrlPath:"mobile/file/upload.vpage",
            NeedAlbum:true,
            NeedCamera:true,
            uploadPara: {}
        };
        do_external('getImageByHtml',JSON.stringify(carData));
    };
    vox.task.setImageToHtml = function(res){
            var resJson = JSON.parse(res);
            if(!resJson.errorCode){
                /*前端展示*/
                if(resJson.fileUrl){
                    var url = resJson.fileUrl;
                    saveMemorandum(url);
                    //$(".add_btn").find("img").attr("src",url);
                }else{
                    AT.alert("客户端未获取到图片")
                }
            }else{
                AT.alert("客户端出错")
            }
    };
</script>
</@layout.page>