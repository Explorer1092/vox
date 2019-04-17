function actionManager(){
    var Self = this;
    var $self = $(this);

    Self.messages = null;
    Self.setMessage = function(url, clazzName, subject, teacher){
        switch(url){
            case "agreetakeoverapplication":
                Self.message = "TAKE_OVER";
                break;
            case "agreedelegateapplication":
                Self.message = "同意接管" + teacher + "老师的" + clazzName + "？";
                break;
            case "approveinviteapplication":
                Self.message = "同意加入" + teacher + "老师的" + clazzName + "？";
                break;
            case "agreesubstituteapplication":
            case "approvejoinapplication":
                Self.message = "同意" + teacher + "老师成为" + clazzName + "的" + subject + "老师？";
                break;
            case "approvehandoverapplication":
                Self.message = "同意加入" + teacher + "老师的" + clazzName + "？";
                break;
            case "approvelinkapp":
                Self.message = "同意" + teacher + "老师与您共享" + clazzName + "学生资源？";
                break;
            case "approvereplaceapp":
                Self.message = "同意" + teacher + "老师接管您的" + clazzName + "学生资源？";
                break;
            case "approvetransferapp":
                Self.message = "同意接管" + teacher + "老师的" + clazzName + "？";
                break;
            default:
                Self.message = null;
                break;
        }
    };
    Self.submit = function(url, recordid){
        var $this = this;
        if (url == "approvelinkapp"
            || url == "approvereplaceapp"
            || url == "approvetransferapp"
            || url == "rejecttransferapp"
            || url == "rejectlinkapp"
            || url == "rejectreplaceapp") {
            $.get("/teacher/systemclazz/" + url + ".vpage", {
                recordId: recordid
            }, function(data){
                if(data.success){
                    $17.alert("操作成功。", function(){
                        setTimeout(function(){ location.reload(); }, 200);
                    });
                }else{
                    $17.alert(data.info);
                }
            });
        } else {
            $.get("/teacher/clazz/alteration/" + url + ".vpage", {
                recordId: recordid
            }, function(data){
                if(data.success){
                    $17.alert("操作成功。", function(){
                        setTimeout(function(){ location.reload(); }, 200);
                    });
                }else{
                    $17.alert(data.info);
                }
            });
        }

    },

    Self.setMessage($self.data("url"), $self.data("clazzname"), $self.data("subject"), $self.data("teachername"));
    if(Self.message == "TAKE_OVER"){
        var states = {
            state0: {
                title: "系统确认",
                html:"<div class='spacing_vox_tb'>对方和您同学科，同意后您将退出" + $self.data("clazzname") + "。</div>"
                    + "<div class='text_orange'><i class='icon_vox icon_vox_orange icon_vox_311'></i> 如果同意转让，这个班级将从您的班级列表里消失，转给 " + $self.data("teachername") + " 进行管理，如确认请在下方输入框中输入 “确认”二字</div>"
                    + "<div class='text_center spacing_vox_tb'><input id='conform' type='text' class='int_vox'></div>",
                focus: 1,
                buttons: { "取消": false, "确定": true },
                submit: function(e, v){
                    if(v){
                        if($17.isBlank($("#conform").val()) || $("#conform").val() != "确认"){
                            e.preventDefault();
                            $.prompt.goToState('state1');
                        }else{
                            Self.submit($self.data("url"), $self.data("recordid"));
                        }
                    }
                }
            },
            state1: {
                title: "系统提示",
                html: "请输入提示文字 “确认”二字",
                buttons: { "知道了" : true },
                submit: function(e){
                    $("#conform").val("");
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            }
        };

        $.prompt(states);
    }else if(!$17.isBlank(Self.message)){
        $.prompt(Self.message, {
            title: "系统确认",
            focus: 1,
            buttons: { "取消": false, "确定": true },
            submit: function(e, v){
                if(v){
                    Self.submit($self.data("url"), $self.data("recordid"));
                }
            }
        });
    }else{
        Self.submit($self.data("url"), $self.data("recordid"));
    }
}