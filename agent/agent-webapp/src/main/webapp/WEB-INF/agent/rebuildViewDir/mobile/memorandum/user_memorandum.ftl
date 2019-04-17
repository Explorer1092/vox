<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="备忘录" pageJs="userMemorandum" footerIndex=2 navBar="hidden">
    <@sugar.capsule css=['school']/>
<style>
    body{background:#f1f2f5}
    .memorandum-box li{padding:.5rem 1rem;margin:.5rem 0;font-size:.75rem;color:#636880;background-color:#fff;cursor:pointer}
    .memorandum-box li .time{padding:.5rem 0 0 0;font-size:.6rem;color:#898c91}
    .memorandum-box li .time span{color:#ff7d5a}
    .memorandum-box li .time .info{float:right;color:#898c91}
</style>
<div class="memorandum-box">
    <ul id="list"></ul>
</div>
<script>
    var isLoad = false;
    var isOver = false,
        postData = {page: 1,type:'text',month:""};
    $(function () {
        addmemorandum(postData);
    });
    $(document).ready(function () {
        reloadCallBack();
    });
    $(window).scroll(function () {
        var scrollTop = $(this).scrollTop();
        var scrollHeight = $(document).height();
        var windowHeight = $(this).height();
        clearTimeout($.data(this, 'scrollTimer'));
        if ((scrollTop + windowHeight >= scrollHeight - 15) && !isLoad && !isOver) {
            addmemorandum(postData);
        }
       /* clearTimeout($.data(this, 'scrollTimer'));
        if (!isLoad && !isOver) {
            $.data(this, 'scrollTimer', setTimeout(
                    addmemorandum(), 400));
        }*/
    });

    var addmemorandum = function (data) {
        if (!isLoad && !isOver) {
            isLoad = true;
            $.post("find_user_memorandum.vpage", data, function (res) {
                if (res.success) {
                    isLoad = false;
                    postData.page = res.page + 1;
                    isOver = res.isOver;
                    if (!isOver) {
                        console.info(res);
                        if (res.list) {
                            res.list.forEach(function (value) {
                                var hasOrderFiled = false;
                                $(".orderFiled").each(function () {
                                    if (value.orderFiled == $(this).html()) {
                                        hasOrderFiled = true;
                                        return false;
                                    }
                                });
                                if (!hasOrderFiled) {
                                }
                                if (value.info) {
                                    value.info.forEach(function (infoValue) {
                                        $("#list").append("<li class='orderFiled'><div class='m_content text' data-id='" + infoValue.id + "'>" + (infoValue.content) + "<div class='time'><a style='z-index=111' href='javascript:void(0);' class='info js-detail' data-id='"+ infoValue.targetId +"' data-type='"+infoValue.targetType+"'>" + infoValue.target +"</a><p>" + infoValue.time + "</p></div></div></li>");
                                    });
                                }
                            })
                        }
                    }
                } else {
                    AT.alert(res.info);
                }
            })
        }
    };
    $(document).on('click','.m_content',function(){
        openSecond("/mobile/memorandum/update_memorandum_page.vpage?id=" + $(this).data("id"));
    });
    $(document).on('click','.js-detail',function(event){
        var type = $(this).data('type');
        event.stopPropagation();
        if(type == 'school'){
            openSecond("/view/mobile/crm/school/school_basic_info.vpage?schoolId=" + $(this).data("id"));
        }else if(type == "teacher"){
            openSecond("/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=" + $(this).data("id"));
        }
    }).on("change","#chooseDate",function () {
        postData.page = 1;
        $('#list').html("");
        var month = $('#chooseDate').val();
        var displayValue = month || "选择月份";
        $('.js-displayMonth').text(displayValue);
        postData["month"] = month;
        isOver = false;
        addmemorandum(postData);
    });
</script>
</@layout.page>