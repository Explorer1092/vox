//年级被点
function level_click(){
    CreateClazz.tempInfo.base.level = $(this).attr("data-level");
    CreateClazz.tempInfo.base.clazzName = null;
    CreateClazz.tempInfo.base.showType = "before";

    CreateClazz.refresh();

    return false;
}

//班级名称被点
function clazzName_click(){
    CreateClazz.tempInfo.base.clazzName = $(this).attr("data-clazzname");

    CreateClazz.refresh();

    return false;
}

//自定义被点
function before_click(){
    $(".v-clazzName").focus().parent().siblings().removeClass("active");

    return false;
}

//用户输入回车
function enter_key_up(event){
    var newName = $(this).val();
    CreateClazz.tempInfo.base.clazzName = $17.isBlank(newName) ? null : newName;
}

//自定义结束
function after_blur(){
    var newName = $(this).val();
    CreateClazz.tempInfo.base.clazzName = $17.isBlank(newName) ? null : newName;
    CreateClazz.tempInfo.base.showType  = $17.isBlank(newName) ? "before" : "after";

    CreateClazz.refresh();

    return false;
}

function free_join_click(){
    var $this = $(this);

    $this.addClass("active").siblings().removeClass("active");
    if($this.attr("data-type") == "yes"){
        CreateClazz.tempInfo.base.freeJoin = true;
    }else{
        CreateClazz.tempInfo.base.freeJoin = false;
    }
}

//下一步按钮
function next_button_click(){
    $17.tongji("教师端-班级管理-修改班级信息");

    App.postJSON("/teacher/clazz/editclazz.vpage", {
        freeJoin        : CreateClazz.tempInfo.base.freeJoin,
        clazzId         : CreateClazz.tempInfo.base.clazzId,
        classLevel      : CreateClazz.tempInfo.base.level,
        clazzName       : CreateClazz.tempInfo.base.clazzName,
        eduSystem       : 'P'+CreateClazz.tempInfo.base.schoolLength
    }, function(data){
        if(data.success){
            $.prompt("编辑班级成功", {
                title: "系统提示",
                button: { "知道了": true },
                submit: function(){
                    setTimeout(function(){location.href = "/teacher/clazz/clazzlist.vpage";}, 200);
                }
            });
        }else{
            $.prompt(data.info, {
                title: "系统提示",
                buttons: { "知道了": true }
            });
        }
    });
}

//减按钮
function minus_click(){
    CreateClazz.tempInfo.base.clazzNumMax       = CreateClazz.tempInfo.base.clazzNumMax == CreateClazz.tempInfo.base.clazzNum ? CreateClazz.tempInfo.base.clazzNum : CreateClazz.tempInfo.base.clazzNumMax - 1;
    CreateClazz.tempInfo.base.minus_disabled    = CreateClazz.tempInfo.base.clazzNumMax == CreateClazz.tempInfo.base.clazzNum;
    CreateClazz.tempInfo.base.plus_disabled     = false;
    CreateClazz.refresh();

    return false;
}

//加按钮
function plus_click(){
    CreateClazz.tempInfo.base.clazzNumMax       = CreateClazz.tempInfo.base.clazzNumMax == 90 ? 90 : CreateClazz.tempInfo.base.clazzNumMax + 1;
    CreateClazz.tempInfo.base.plus_disabled     = CreateClazz.tempInfo.base.clazzNumMax == 90;
    CreateClazz.tempInfo.base.minus_disabled    = false;
    CreateClazz.refresh();

    return false;
}

//人数输入框获得焦点
function clazzmax_focus(){
    $(this).val("");

    return false;
}

//人数输入框失去焦点
function clazzmax_blur(){
    var clazzMax = +$(this).val();

    if(!$17.isNumber(clazzMax)){
        $17.alert("请输入正确的上限值");
        $(this).val(CreateClazz.tempInfo.base.clazzNumMax);

        return false;
    }

    if(clazzMax < CreateClazz.tempInfo.base.clazzNum){
        $17.alert("班级上限不能低于当前人数");
        $(this).val(CreateClazz.tempInfo.base.clazzNumMax);

        return false;
    }

    if(clazzMax > 90){
        $17.alert("班级上限不能多于90哦");
        $(this).val(CreateClazz.tempInfo.base.clazzNumMax);

        return false;
    }

    CreateClazz.tempInfo.base.minus_disabled    = clazzMax == CreateClazz.tempInfo.base.clazzNum;
    CreateClazz.tempInfo.base.plus_disabled     = clazzMax == 90;

    CreateClazz.tempInfo.base.clazzNumMax = clazzMax;

    CreateClazz.refresh();

    return false;
}