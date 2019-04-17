function notAccount_click(){
    Invite.tempInfo.base.showType = "notAccount";

    Invite.refresh();

    return false;
}

function hasAccount_click(){
    Invite.tempInfo.base.showType = "hasAccount";

    Invite.refresh();
    $("li[data-shownametype='ALL']").trigger("click");
    return false;
}

function teachername_keyup(){
    var $this = $(this);
    var newName = $this.val();

    Invite.tempInfo.base.tnerror        = $17.isCnString(newName) || newName == "" ? false : true;
    Invite.tempInfo.base.teacherName    = $17.isCnString(newName) || newName == "" ? newName : "";

    if(Invite.tempInfo.base.tnerror){
        $this.siblings(".w-form-info-error").show();
    }else{
        $this.siblings(".w-form-info-error").hide();
    }
    return false;
}

function teachermobile_keyup(){
    var $this = $(this);
    var newMobile = $this.val();

    Invite.tempInfo.base.tmerror        = $17.isMobile(newMobile) || newMobile == "" ? false : true;
    Invite.tempInfo.base.teacherMobile  = $17.isMobile(newMobile) || newMobile == "" ? newMobile : "";

    if(Invite.tempInfo.base.tmerror){
        $this.siblings(".w-form-info-error").show();
    }else{
        $this.siblings(".w-form-info-error").hide();
    }
    return false;
}

function send_invite_btn_click(){
    $17.tongji("教师端-添加老师-邀请无账号老师");
    if(Invite.tempInfo.base.tnerror || Invite.tempInfo.base.tmerror){
        $17.alert("请输入正确的教师姓名或手机号码")
        return false;
    }

    App.postJSON("/teacher/clazz/alteration/createteacherhandoverclass.vpage", {
        userName    : Invite.tempInfo.base.teacherName,
        mobile      : Invite.tempInfo.base.teacherMobile,
        clazzId     : $(this).attr("data-clazzid")
    }, function(data){
        if(data.success){
            $17.alert("邀请已发送！", function(){
                setTimeout(function(){ location.href = "/teacher/clazz/clazzlist.vpage"; }, 200);
            });
        }else{
            $17.alert(data.info);
        }
    });

    return false;
}

function first_names_click(){
    var $self = $(this);

    $self.radioClass("active");

    switch($self.attr("data-shownametype")){
        case "ALL":
            Invite.tempInfo.base.listType           = "ALL";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.ALL);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.ALL);
            break;
        case "ABCD":
            Invite.tempInfo.base.listType           = "ABCD";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.A, Invite.dataBase.authenticatedTeacherList.B, Invite.dataBase.authenticatedTeacherList.C, Invite.dataBase.authenticatedTeacherList.D);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.A, Invite.dataBase.unauthenticatedTeahcerList.B, Invite.dataBase.unauthenticatedTeahcerList.C, Invite.dataBase.unauthenticatedTeahcerList.D);
            break;
        case "EFGH":
            Invite.tempInfo.base.listType           = "EFGH";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.E, Invite.dataBase.authenticatedTeacherList.F, Invite.dataBase.authenticatedTeacherList.G, Invite.dataBase.authenticatedTeacherList.H);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.E, Invite.dataBase.unauthenticatedTeahcerList.F, Invite.dataBase.unauthenticatedTeahcerList.G, Invite.dataBase.unauthenticatedTeahcerList.H);
            break;
        case "IJKL":
            Invite.tempInfo.base.listType           = "IJKL";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.I, Invite.dataBase.authenticatedTeacherList.J, Invite.dataBase.authenticatedTeacherList.K, Invite.dataBase.authenticatedTeacherList.L);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.I, Invite.dataBase.unauthenticatedTeahcerList.J, Invite.dataBase.unauthenticatedTeahcerList.K, Invite.dataBase.unauthenticatedTeahcerList.L);
            break;
        case "MNOP":
            Invite.tempInfo.base.listType           = "MNOP";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.M, Invite.dataBase.authenticatedTeacherList.N, Invite.dataBase.authenticatedTeacherList.O, Invite.dataBase.authenticatedTeacherList.P);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.M, Invite.dataBase.unauthenticatedTeahcerList.N, Invite.dataBase.unauthenticatedTeahcerList.O, Invite.dataBase.unauthenticatedTeahcerList.P);
            break;
        case "QRST":
            Invite.tempInfo.base.listType           = "QRST";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.Q, Invite.dataBase.authenticatedTeacherList.R, Invite.dataBase.authenticatedTeacherList.S, Invite.dataBase.authenticatedTeacherList.T);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.Q, Invite.dataBase.unauthenticatedTeahcerList.R, Invite.dataBase.unauthenticatedTeahcerList.S, Invite.dataBase.unauthenticatedTeahcerList.T);
            break;
        case "UVWX":
            Invite.tempInfo.base.listType           = "UVWX";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.U, Invite.dataBase.authenticatedTeacherList.V, Invite.dataBase.authenticatedTeacherList.W, Invite.dataBase.authenticatedTeacherList.X);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.U, Invite.dataBase.unauthenticatedTeahcerList.V, Invite.dataBase.unauthenticatedTeahcerList.W, Invite.dataBase.unauthenticatedTeahcerList.X);
            break;
        case "YZ":
            Invite.tempInfo.base.listType           = "YZ";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.Y, Invite.dataBase.authenticatedTeacherList.Z);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.Y, Invite.dataBase.unauthenticatedTeahcerList.Z);
            break;
        case "OTHER":
            Invite.tempInfo.base.listType           = "OTHER";
            Invite.tempInfo.base.authenticated      = [].concat(Invite.dataBase.authenticatedTeacherList.OTHER);
            Invite.tempInfo.base.unauthenticated    = [].concat(Invite.dataBase.unauthenticatedTeahcerList.OTHER);
            break;
    }

    Invite.refresh();

    return false;
}

function teacher_radio_click(){
    var $self = $(this);

    Invite.tempInfo.base.targetId   = $self.attr("data-teacherid");
    Invite.tempInfo.base.targetName = $self.attr("data-teachername");

    Invite.refresh();

    return false;
}

function sendinviteapplication_click(){
    $.get("/teacher/clazz/alteration/sendhandoverapplication.vpage", {
        teacherId   : Invite.tempInfo.base.targetId,
        classId     : Invite.tempInfo.base.clazzId
    }, function(data){
        if(data.success){
            setTimeout(function(){ location.href = "/teacher/clazz/clazzlist.vpage"; }, 200);
        }else{
            $17.alert(data.info);
        }
    });

    return false;
}