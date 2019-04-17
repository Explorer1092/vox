<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="备忘录" pageJs="" footerIndex=2 navBar="hidden">
    <@sugar.capsule css=['school']/>
<style>
    body{background:#f1f2f5}
    .memorandum-box li{padding:.5rem 1rem;margin:.5rem 0;font-size:.75rem;color:#636880;background-color:#fff;cursor:pointer}
    .memorandum-box li .time{padding:.5rem 0 0 0;font-size:.6rem;color:#898c91}
    .memorandum-box li .time span{color:#ff7d5a}
    .memorandum-box li .time .info{float:right;color:#898c91}
</style>
<a href="javascript:void(0);" class="inner-right js-newlyBuild" style="display: none;">新建</a>
<div class="memorandum-box"><ul id="list"></ul></div>
<script type="application/javascript">
    $(function () {
        var schoolId = ${schoolId!0};
        var teacherId = ${teacherId!0};
        var page = 1;
        var isLoad = false;
        var isOver = false;

        var addmemorandum = function () {
            if (!isLoad && !isOver) {
                isLoad = true;
                var url = "";
                if (schoolId != 0) {
                    url = "find_school_memorandum.vpage";
                }
                if (teacherId != 0) {
                    url = "find_teacher_memorandum.vpage";
                }
                $.post(url, {schoolId: schoolId, teacherId: teacherId, page: page, type: "text"}, function (res) {
                    if (res.success) {
                        isLoad = false;
                        page = res.page + 1;
                        isOver = res.isOver;
                        if (!isOver) {
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
                                        /*$("#list").append("<ul><ul>");*/
                                    }
                                    if (value.info) {
                                        value.info.forEach(function(infoValue){
                                            $("#list").append("<li class='orderFiled'><div class='m_content text' data-id='" + infoValue.id + "'>" + (infoValue.content.split("\n").join("<br/>")) + "<div class='time'><p>" + infoValue.time
                                                    + ((infoValue.userName) ? ('<a href="javascript:void(0);" class="info" data-id="">' + infoValue.userName + '</a>') : '')
                                                    + ((infoValue.isIntoSchool) ? '<a href="javascript:void(0);" class="info" data-id="">【进校】</a>' : "") + "</p></div> </div></li>");

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


        var setTopBar = {
            show: true,
            rightText:"新建" ,
            rightTextColor: "ff7d5a",
            needCallBack: true
        };
        var topBarCallBack =  function(){
            $('.js-newlyBuild').click();
        };
        setTopBarFn(setTopBar, topBarCallBack);

        addmemorandum();

        $(window).scroll(function () {
            var scrollTop = $(this).scrollTop();
            var scrollHeight = $(document).height();
            var windowHeight = $(this).height();
            clearTimeout($.data(this, 'scrollTimer'));
            if ((scrollTop + windowHeight >= scrollHeight - 15) && !isLoad && !isOver) {
                addmemorandum();
            }
        });
        $(document).on('click','.m_content',function(){
            var id = $(this).data('id');
            location.href = "update_memorandum_page.vpage?id=" +id ;
        });
        $(document).on('click','.js-newlyBuild',function(){
            location.href = "add_memorandum_page.vpage?teacherId=${teacherId!0}&schoolId=${schoolId!0}" ;
        });
    });
</script>
</@layout.page>