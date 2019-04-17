/* global define : true, PM : true, $:true */
/**
 *  @date 2015/11/25
 *  @auto liluwei
 *  @description 该模块主要感恩节活动
 */

require(['template', "ajax","jqPopup"], function(template, promise){

    'use strict';

    $(function(){
        var doc = document,
            SID;

        var ajaxError = function(logMsg, errorMsg){
            $.alert(errorMsg);

            log(
                {
                    errMsg : logMsg,
                    op : "ajax"
                },
                "error"
            );
        };

        promise("/parentMobile/activity/getStudentList.vpage")
        .done(function(res){
            if(!res.success){
                return ajaxError(['感恩节 获取指定家长所有孩子 ajax出错  出错信息 %j', res], res.info)
            }
            var optsions = "";

            (res.students || []).forEach(function(student){
                optsions += '<option value="'+ student.id +'" data-img="'+student.img+'">'+ student.name +'</option>';
            });

            $("#doMyChilds").html( optsions).find("option:first").trigger("change");
        });

        var $studnetImg = $("#doStudentImg"),
            defaultStudentSrc = $studnetImg.data("default_src"),
            imgPre = $studnetImg.data("img_pre");

        $(doc)
            .on("change", "#doMyChilds", function(event){
                var $self = $(this),
                    $selectedOption = $self.find("option:selected"),
                    studentName = $selectedOption.text(),
                    sid = $self.val();

                var sidCondition = {
                    sid : sid
                };

                promise( "/parentMobile/activity/getthankstarget.vpage", sidCondition)
                .done(function(res){

                    if(!res.success){
                        return ajaxError(['感恩节获取学生目标信息ajax出错 出错信息 %j',  res], res.info)
                    }

                    SID = sid;

                    // 孩子姓名
                    $("#doStudentName").text(studentName||"");

                    // 孩子目标
                    $("#doTarget").text(res.target || "暂无");

                    // 孩子头像
                    var imgName = $selectedOption.data("img");
                    $studnetImg.attr("src", imgName ?  imgPre + imgName : defaultStudentSrc );

                    // 该孩子班级的学生信息
                    promise(
                        "/parentMobile/ucenter/isBindClazz.vpage",
                        sidCondition
                    ).done(function(bindRes){

                        if(!bindRes.success){
                            ajaxError("判断是否已经绑定班级出错", res.info);
                        }

                        $.extend(res, {isBindClazz: bindRes.isBindClazz||false});

                        $("#doClazzStudents").html(
                            template(
                                "doClazzStudentsTemp",
                                res
                            )
                        );
                    });


                    // 该孩子班级感谢老师数
                    $("#doThanksTeacherCount").text(res.parentCount || 0);

                    // 该孩子班级多少位家长贡献学豆?
                    $("#doIntegralParentCount").text(res.parentIntegralCount || 0);

                    // 该孩子班级贡献多少学豆?
                    $("#doIntegralCount").text(res.integralCount || 0);

                });
            })
            .on("click", ".doThankTeacher", function(){
                var $self = $(this),
                    className = "doThankTeacher";

                $self.removeClass(className);

                $.post("/parentMobile/activity/thanksteacher.vpage", {sid : SID}, function(res){
                    if(!res.success){
                        $self.addClass(className);
                        return ajaxError(['感恩节 感谢老师ajax出错  出错信息 %j', res], res.info)
                    }

                    $.alert('感谢成功').done(function(){
                        location.reload(true);
                    });

                });
            })
            .on("click", ".doSendIntegr", function(){
                location.href = "/parentMobile/homework/giveBean.vpage?isActivity=true&sid=" + SID;
            });

    });

});
