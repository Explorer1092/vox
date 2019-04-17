/**
 * Created by free on 2016/12/14.
 */
define(["jquery","prompt","paginator","template"],function ($) {

    //提交老师信息
    var saveTeacherInfo = function(tid){
        var postNode = $(".js-postData"),
            postData= {};
        if(postNode.length !=0){
            $.each(postNode,function(i,item){
                postData[item.name] = $(item).val();
            });
            if(tid){
                postData['id'] = tid;
            }
            $.post('saveteacher.vpage',postData,function(res){
                var text = '';
                if(res.success){
                    text= '操作成功';
                }else{
                    text = res.info ? res.info : '操作成功';
                }
                $.prompt('<p style="text-align: center;">'+text+'</p>',{
                    title: "温馨提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if (v) {
                            if(res.success){
                                location.reload();
                            }
                        }
                    }
                });
            });
        }
        console.log(postData);
    };

    //添加老师
    $(document).on('click','#addBtn',function(){
        var htmlTemp = template('teacherDialog',{type:'new'});
        $.prompt(htmlTemp,{
            title: "添加老师",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    saveTeacherInfo();
                }
            }
        });
    });

    //编辑
    $(document).on('click','.js-editBtn',function(){
        var pdata = $(this).parent('td').data(),
            taril = false;
        if(pdata.pic && pdata.pic.indexOf('oss-image') != 0){
            taril = true;
        }
        var htmlTemp = template('teacherDialog',{
            tName:pdata.name,
            tAccount:pdata.account,
            pic:pdata.pic,
            type:'edit',
            taril:taril
        });
        $.prompt(htmlTemp,{
            title: "编辑老师",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    saveTeacherInfo(pdata.tid);
                }
            }
        });
    });

    //编辑
    $(document).on('click','.js-portrait',function(){
        $('#upload-portrait').click();
    });

    //监听重复密码
    $(document).on('change','#code2',function(){
        var val = $("#code1").val();
        if($(this).val() != val){
            $(".js-errorInfo").remove();
            $(this).parent('div').append("<span style='color: orangered;' class='js-errorInfo'>两次密码不一致</span>")
        }else{
            $(".js-errorInfo").remove();
        }
    });

    //上传文件
    $(document).on('change','.js-userPic',function(){
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
                                    trail = '@1e_1c_0o_0l_200h_200w_80q'
                                }
                                $('#imgDiv').html('<img src="'+res.fileName+trail+'" width="98px;" height="98px;">');
                                $("#portrait").val(res.fileName);
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

    //删除
    $(document).on('click','.js-delBtn',function(){
        var tid = $(this).parent("td").data("tid");
        $.prompt('<p style="text-align: center;">确定要删除这个老师吗？</p>',{
            title: "删除老师",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post("deleteteacher.vpage",{
                        teacher:tid
                    },function(res){
                        $.prompt('<p style="text-align: center;">'+res.info+'</p>',{
                            title: "温馨提示",
                            buttons: {"确定": true},
                            focus: 1,
                            submit: function (e, v) {
                                if (v) {
                                    location.reload();
                                }
                            }
                        });
                    });
                }
            }
        });
    });

    //重置密码
    $(document).on('click','.js-resetBtn',function(){
        var tid = $(this).parent("td").data("tid"),
            resetDialog = template("restDialog",{});
        $.prompt(resetDialog,{
            title: "重置密码",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post("resetpwd.vpage",{
                        teacher:tid,
                        code1:$("#code1").val(),
                        code2:$("#code2").val()
                    },function(res){
                        var info = '';
                        if(res.success){
                            info = '重置成功，密码为：'+res.pwd;
                        }else{
                            info = res.info;
                        }
                        $.prompt('<p style="text-align: center;">'+info+'</p>',{
                            title: "温馨提示",
                            buttons: {"确定": true},
                            focus: 1,
                            submit: function (e, v) {
                                if (v) {
                                }
                            }
                        });
                    });
                }
            }
        });
    });

    // 注册欢拓老师
    $(document).on("click",".js-regBtn",function(){
        var tid = $(this).parent("td").data("tid"),
            resetDialog = template("registerDialog",{});
        $.prompt(resetDialog,{
            title: "欢拓后台注册老师",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post('register-teacher.vpage',{'id':tid, 'password':$("#code1").val()},function(res){
                        var info = '';
                        if(res.success){
                            info = '重置成功，密码为：'+res.pwd;
                        }else{
                            info = res.info;
                        }
                        $.prompt('<p style="text-align: center;">'+info+'</p>',{
                            title: "温馨提示",
                            buttons: {"确定": true},
                            focus: 1,
                            submit: function (e, v) {
                                if (v) {
                                }
                            }
                        });
                    });
                }
            }
        });
    //     var tid = $(this).parent("td").data("tid");
    //     $.prompt('<p style="text-align: center;">确定要在欢拓后台注册吗？</p>',{
    //         title: "注册欢拓老师",
    //         buttons: {"确定": true},
    //         focus: 1,
    //         submit: function (e, v) {
    //             if (v) {
    //                 $.post('register-teacher.vpage',{'id':tid, 'password':$("#code1").val()},function(res){
    //                     if(res.success){
    //                         $.prompt("<div style='text-align:center;'>注册成功</div>", {
    //                             title: "操作提示",
    //                             buttons: { "确定": true },
    //                             focus : 1,
    //                             useiframe:true,
    //                             submit: function (e, v) {
    //                                 if (v) {
    //                                     location.reload();
    //                                 }
    //                             }
    //                         });
    //                     }else{
    //                         $.prompt('<p style="text-align: center;">'+res.info+'</p>', {
    //                             title: "温馨提示",
    //                             buttons: {"确定": true},
    //                             focus: 1
    //                         });
    //                     }
    //                 })
    //             }
    //         }
    //     });
    });
});