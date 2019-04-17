/**
 * Created by free on 2016/12/13.
 */
define(["jquery","prompt","paginator",'template','datetimepicker'],function ($) {

    // 创建课程
    $(document).on('click','#createBtn',function(){
        var nodes = $(".js-create"),
            postData = {};
        $.each(nodes,function(i,item){
            postData[item.name] = $(item).val();
        });
        if(cursourceId){
            postData["id"] = cursourceId
        }
        $.post('savecourse.vpage',postData,function(res){
            if(res.success){
                location.href = "?cid="+res.courseId;
            }else{
                $.prompt('<p style="text-align: center;">'+res.info+'</p>',{
                    title: "温馨提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if (v) {

                        }
                    }
                });
            }
        });
    });

    // 点击老师
    $(document).on('click','.js-teacher_single',function(){
        $(this).addClass("active").siblings("span").removeClass("active");
    });

    // 点击助教
    $(document).on('click','.js-teacher',function(){
        if($(this).hasClass('active')){
            $(this).removeClass("active");
        }else{
            $(this).addClass("active");
        }
    });

    // 删除课时
    $(document).on("click",".js-delBtn",function(){
        var pid = $(this).parent('td').data("pid");
        $.prompt('<p style="text-align: center;">确定要删除该课时吗？</p>',{
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('removeperiod.vpage',{'pid':pid, "cid":cursourceId},function(res){
                        if(res.success){
                            $.prompt('<p style="text-align: center;">'+(res.info || "课时删除成功！")+'</p>', {
                                title: "温馨提示",
                                buttons: {"确定": true},
                                focus: 1,
                                submit: function (e,v) {
                                    location.reload();
                                }
                            });
                        }else{
                            $.prompt('<p style="text-align: center;">'+res.info+'</p>', {
                                title: "温馨提示",
                                buttons: {"确定": true},
                                focus: 1
                            });
                        }
                    })
                }
            }
        });
    });

    // 选择助教或者老师
    $(document).on("click",".js-sureBtn",function(){
        var type = $(this).data("type"),
            teacherList=[],
            teacherNodes = $(".js-"+type+"List").find('span.js-role.active');

        if(teacherNodes.length !=0){
            $.each(teacherNodes,function(i,item){
                teacherList.push($(item).data("sid"));
            })
        }
        $.post('appendteacher.vpage',{
            role:type,
            course:cursourceId,
            teachers:teacherList.join(',')
        },function(res){
            if(res.success){
                $.prompt('<p style="text-align: center;">'+(res.info || "老师添加成功！")+'</p>', {
                    title: "温馨提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e,v) {
                        location.href = "?cid="+cursourceId;
                    }
                });
            }else{
                $.prompt('<p style="text-align: center;">'+res.info +'</p>', {
                    title: "温馨提示",
                    buttons: {"确定": true},
                    focus: 1
                });
            }
        });
    });

    $(document).on("click",".panel-heading",function(){
        var colDiv = $(this).parent("div").find('.panel-collapse');
        if(colDiv.hasClass("in")){
            colDiv.removeClass("in").addClass("out");
        }else{
            colDiv.removeClass("out").addClass("in");
        }
    });

    // 购买课程
    $(document).on("change",'#buyByCourse',function(){
        setActiveInputs(this.checked);
    });

    // 注册欢拓课程
    $(document).on("click",".js-regBtn",function(){
        var pid = $(this).parent('td').data("pid");
        $.prompt('<p style="text-align: center;">确定要在欢拓后台注册吗？</p>',{
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('register-course.vpage',{'id':pid},function(res){
                        if(res.success){
                            $.prompt("<div style='text-align:center;'>注册成功</div>", {
                                title: "操作提示",
                                buttons: { "确定": true },
                                focus : 1,
                                useiframe:true,
                                submit: function (e, v) {
                                    location.reload();
                                }
                            });
                        }else{
                            $.prompt('<p style="text-align: center;">'+res.info+'</p>', {
                                title: "温馨提示",
                                buttons: {"确定": true},
                                focus: 1
                            });
                        }
                    })
                }
            }
        });
    });

    // 进入直播间
    $(document).on("click",".js-liveBtn",function(){
        var pid = $(this).parent('td').data("pid");
        $.prompt('<p style="text-align: center;">如果没有成功打开请确认浏览器没有拦截弹出窗口</p>', {
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('live.vpage', {'period': pid}, function (res) {
                        if (res.success) {
                            var funTalk = document.createElement('a');
                            var link;
                            // 模拟触发跳转
                            if (res.live) {
                                link = res.data.liveUrl;
                            } else {
                                link = res.data.playbackUrl;
                            }
                            if (link == undefined || link == '') {
                                alert('获取直播间地址失败!');
                            }
                            funTalk.setAttribute('href', link);
                            funTalk.setAttribute('target', '_blank');
                            funTalk.click();
                        } else {
                            $.prompt('<p style="text-align: center;">' + res.info + '</p>', {
                                title: "温馨提示",
                                buttons: {"确定": true},
                                focus: 1
                            });
                        }
                    });
                }
            }
        });
    });


    $(document).on("click",".valar-morghulis",function(){
        var pid = $(this).data("pid");
        var status = $(this).data("st");
        var str = (status != '')? ("<br>当前状态"+status):"";
        $.prompt('<p style="text-align: center;">前方高能，非战斗人员请迅速撤离'+ str + '</p>',{
            title: "不要回答！不要回答！！不要回答！！！",
            buttons: {"第二次冲击": "FINISHED", "人类补完计划":"REPLAY_DONE"},
            focus: 1,
            submit: function (e, v) {
                $.post('valar-morghulis.vpage',{'id':pid, 'op': v},function(res){
                    if(res.success){
                        $.prompt("<div style='text-align:center;'>"+res.info+"</div>", {
                            title: "温馨提示",
                            buttons: { "确定": true },
                            focus : 1,
                            useiframe:true,
                            submit: function (e, v) {
                                location.reload();
                            }
                        });
                    }else{
                        $.prompt('<p style="text-align: center;">'+res.info+'</p>', {
                            title: "温馨提示",
                            buttons: {"确定": true},
                            focus: 1
                        });
                    }
                })
            }
        });
    });

    //设置购买课程相关购买
    var setActiveInputs = function(flag) {
        $("#payAll").val(flag);
        var inputs = $(".js-buy_course_input");
        if(inputs.length >0){
            $.each(inputs,function(i,item){
                if(flag){
                    $(item).removeAttr('disabled');
                }else{
                    $(item).val('');
                    $(item).attr('disabled','');
                }
            });
        }
    }

});