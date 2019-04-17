$(function(){
    LeftMenu.focus("clazzmanager");

    function deleteteacher(clazzId, teacherId){
        $17.tongji("教师端-班级管理-删除任课老师");

        $.get("/teacher/clazz/alteration/deleteteacher.vpage", {
            clazzId     : clazzId,
            teacherId   : teacherId
        }, function(data){
            if(data.success){
                $17.alert("删除成功。", function(){
                    setTimeout(function(){ location.reload(); }, 200);
                });
            }else{
                $17.alert(data.info);
            }
        });
    }

    //删除任课老师
    $(".v-delete-one").on("click", function(){
        var $self = $(this);

        $.prompt("确定要删除" + $self.attr("data-teachername"), {
            title: "系统提示",
            focus: 1,
            buttons: { "取消": false, "确定": true },
            submit: function(e, v){
                if(v){
                    deleteteacher($self.attr("data-clazzid"), $self.attr("data-teacherid"));
                }
            }
        });

        return false;
    });

    //班级数量超标提示
    $(".v-cannot-tip").on("click", function(){
        $17.alert("<div>您的班级数量已达到上限，不能再添加新班级。</div><div>如有删除班级问题请拨打客服电话</div>");

        return false;
    });

    //查看已毕业班级
    $("#showMoreGraduated").on("click", function(){
        var $self = $(this);
        if($self.hasClass("active")){
            $self.removeClass("active");
            $self.find("span.w-icon-md").text("查看已毕业班级");
            $("#graduated_clazz_list").hide();
        }else{
            $self.addClass("active");
            $self.find("span.w-icon-md").text("收起");
            $("#graduated_clazz_list").show();
        }
    });
});