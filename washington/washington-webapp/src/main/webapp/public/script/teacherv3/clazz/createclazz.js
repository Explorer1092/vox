var _tempLogModule = "common";
if($17.getQuery("step") == "showtip"){
    _tempLogModule = "reg";
}

//学制被点
function schoolLength_click(){
    $17.tongji("老师端-添加班级-选择学制");

    ClazzInfo.tempInfo.base.schoolLength = $(this).attr("data-schoollength");
    ClazzInfo.tempInfo.base.level = null;
    ClazzInfo.tempInfo.base.clazzName = [];

    ClazzInfo.refresh();

    //添加班级页-选择学制
    $17.voxLog({
        module: _tempLogModule,
        op : "clazz-click-selectCase",
        step : 15
    });

    return false;
}

//年级被点
function level_click(){
    $17.tongji("老师端-添加班级-选择年级");

    ClazzInfo.tempInfo.base.level = $(this).attr("data-level");

    if(ClazzInfo.tempInfo.base.clazzName.length > 0){
        $.prompt("一次只能添加一个年级哦！确定清空已选择班级？", {
            title : "系统提示",
            focus : 1,
            buttons : {"取消" : false, "确定" : true},
            submit : function(e, v){
                if(v){
                    ClazzInfo.tempInfo.base.clazzName = [];
                    ClazzInfo.refresh();
                }
            }
        });
    }else{
        ClazzInfo.refresh();
    }

    //添加班级页-选择年级
    $17.voxLog({
        module: _tempLogModule,
        op : "clazz-click-selectGrade",
        step : 16
    });

    return false;
}

//班级名称被点
function clazzName_click(){
    ClazzInfo.setClazzName($(this).attr("data-clazzname"));
    ClazzInfo.refresh();

    $17.tongji("老师端-添加班级-选择班级");

    //添加班级页-选择班级
    $17.voxLog({
        module: _tempLogModule,
        op : "clazz-click-selectClass",
        step : 17
    });
    return false;
}

//经过
function clazzName_mouseenter(){
    var $this = $(this);

    if(!$this.hasClass("active")){
        $this.find(".w-red").show();
    }
}

//移出
function clazzName_mouseleave(){
    var $this = $(this);

    if(!$this.hasClass("active")){
        $this.find(".w-red").hide();
    }
}

//自定义被点
function autoAddClazz_click(){
    $17.tongji("老师端-添加班级-输入其他班级名称");
    var currentClazzName = $(this).siblings("input").val();

    if(currentClazzName == ""){
        $(this).siblings("input").addClass("w-int-error");
        return false;
    }

    ClazzInfo.setClazzName(currentClazzName + "班");
    ClazzInfo.refresh();

    //添加班级页-选择班级
    $17.voxLog({
        module: _tempLogModule,
        op : "clazz-click-selectClass",
        step : 17
    });

    return false;
}

//添加自定义班级获取焦点
function autoAddClazz_focus(){
    var $this = $(this);
    $this.siblings(".info-text").hide();
    $this.siblings(".v-auto-addClazz").show();
}

//下一步按钮
function next_button_click(){
    var $this = $(this);
    if($this.hasClass("w-btn-disabled")){
        return false;
    }

    if( ClazzInfo.tempInfo.base.clazzName.length < 1 ){
        $17.alert("请选择班级!");
        return false;
    }

    //添加班级页-点击下一步
    $17.voxLog({
        module: _tempLogModule,
        op :  $(this).data("step-content"),
        step : $(this).data("step-id")
    });

    $this.addClass("w-btn-disabled");
    $.post("/teacher/clazz/coraclazz.vpage", {
        clazzLevel : ClazzInfo.tempInfo.base.level,
        clazzName  : ClazzInfo.tempInfo.base.clazzName.join()
    }, function(data){
        if(data.success){
            StudentInfo.tempInfo.base.clazzName    = data.createList;

            //上传数据
            if(StudentInfo.tempInfo.base.clazzName.length > 0){
                for(var i = 0, len = StudentInfo.tempInfo.base.clazzName.length; i < len; i++){
                    uploadDealData(StudentInfo.tempInfo.base.clazzName[i])
                }
            }

            //直接创建
            if(data.addList.length < 1){
                create_btn_click();
            }else{
                JoinClazz.tempInfo.base         = data;
                $17.tongji("老师端-加入班级");

                if (data.addList[0].validClazzs.length > 0 && data.addList[0].validClazzs[0].creatorType == "SYSTEM") {
                    console.log(2);
                    for (var i = 0, len = data.addList.length; i < len; i++) {
                        for (var j = 0, tlen = data.addList[i].validClazzs.length; j < tlen; j++) {
                            // FIXME 同步加入班级
                            $.ajax({
                                type: "post",
                                url: "/teacher/clazz/alteration/substituteorsendsubstitutespplication.vpage",
                                data: {
                                    classId: data.addList[i].validClazzs[j].clazzId
                                },
                                async: false,
                                success: function (tdata) {
                                    if(tdata.success){
                                        //当最后一个班级加入时 && 无创建班级
                                        if(i == data.addList.length-1
                                            && j == data.addList[0].validClazzs.length - 1
                                            && data.createList.length < 1){
                                            $17.alert("加入班级成功！", function () {
                                                location.href = "/teacher/systemclazz/clazzindex.vpage";
                                            });
                                        }

                                        //新注册用户加入班级
                                        if($17.getQuery("step") == "showtip"){
                                            $17.voxLog({
                                                module: "reg",
                                                op : "newclazzsubmit1",
                                                step : 11
                                            });
                                        }
                                    }else{
                                        $17.alert("加入班级失败！");
                                    }
                                }
                            });
                        }
                    }
                } else {
                    JoinClazz.refresh();
                }
            }
        }else{
            $17.alert(data.info);
        }
        $this.removeClass("w-btn-disabled");
    });

    return false;
}

function check_invalid_click(){
    JoinClazz.tempInfo.invalidShow = "show";
    JoinClazz.refresh();
}

//上一步按钮
function prev_click(){
    JoinClazz.tempInfo.joinClazzId = null;
    JoinClazz.tempInfo.joinIndex = 0;
    StudentInfo.tempInfo.base.addList = [];

    ClazzInfo.refresh();

    //添加班级页-上一步
    $17.voxLog({
        module: _tempLogModule,
        op :  $(this).data("step-content"),
        step : $(this).data("step-id")
    });
    return false;
}

//选择要加入的班级
function select_join_clazz(){
    JoinClazz.tempInfo.joinClazzId = $(this).attr("data-clazzid");
    JoinClazz.refresh();

    //添加班级页-已有班级-选择班级
    $17.voxLog({
        module: _tempLogModule,
        op : "clazz-alreadyClass-selectClass",
        step : 20
    });
    return false;
}

//没找到可加入的班级
function not_found_clazz(){
    var $this = $(this);
    var $index = parseInt($this.attr("data-index"));

    JoinClazz.tempInfo.joinClazzId = null;
    JoinClazz.tempInfo.invalidShow = "hide";
    JoinClazz.tempInfo.joinIndex = $index+1;
    StudentInfo.tempInfo.base.clazzName.push(uploadDealData(JoinClazz.tempInfo.base.addList[$index]));

    if($index < JoinClazz.tempInfo.base.addList.length-1){
        $("html, body").animate({ scrollTop: 0 }, 200);
        JoinClazz.refresh();
    }else{
        //$.prompt.close();
        create_btn_click();
    }

}

//加入班级按钮
function join_btn_click(){
    var $this = $(this);
    var $index = parseInt($this.attr("data-index"));
    
    if($(this).hasClass("w-btn-disabled")){
        return false;
    }

    if(JoinClazz.tempInfo.joinClazzId == null){
        $this.siblings(".data-info").show().text("请选择班级!");
        return false;
    }

    //添加班级页-已有班级-加入班级
    $17.voxLog({
        module: _tempLogModule,
        op : "clazz-alreadyClass-joinSubmitBtn",
        step : 23
    });

    $.post("/teacher/clazz/alteration/substituteorsendsubstitutespplication.vpage", {
        classId : JoinClazz.tempInfo.joinClazzId
    }, function(data){
        if(data.success){
            //如果加入班级成功记录
            if(data.info == "加入班级成功"){
                JoinClazz.tempInfo.joinHasFlag = true;
                StudentInfo.tempInfo.base.clazzIdArr.push(JoinClazz.tempInfo.joinClazzId);
            }

            //发起加入通知列表
            JoinClazz.tempInfo.joinClazzId = null;
            JoinClazz.tempInfo.invalidShow = "hide";
            JoinClazz.tempInfo.joinIndex = $index+1;
            StudentInfo.tempInfo.base.addList.push(JoinClazz.tempInfo.base.addList[$index]);

            //当最后一个班级加入时 && 无创建班级
            if($index ==  JoinClazz.tempInfo.base.addList.length-1 && StudentInfo.tempInfo.base.clazzName.length < 1){
                //$.prompt.close();
                if($17.getQuery("step") == "showtip" && JoinClazz.tempInfo.joinHasFlag){
                    $17.tongji("注册流程—加入班级—加入成功");

                    $("#Anchor").remove();
                    $(".step-container-complete").show();
                    containerComplete();
                    //添加班级页-方式-提交
                    $17.voxLog({
                        module: "reg",
                        op :  "clazz-click-homeworkBtn",
                        step : 28
                    });
                }else{
                    // by changyuan.liu 天津新体系
                    // TODO 这块代码没有搞懂逻辑，只是知道这么改是ok的，回头再看看吧。。。
                    if (data.creatorType != "SYSTEM") {
                        $17.alert("加入班级申请已发送成功！", function () {
                            location.href = "/teacher/clazz/clazzlist.vpage";
                        });
                    } else {
                        $17.alert("加入班级成功！", function () {
                            location.href = "/teacher/systemclazz/clazzindex.vpage";
                        });
                    }
                }
            }else{
                //当前第几个班级加入中。是否可以继续加入.
                if($index < JoinClazz.tempInfo.base.addList.length-1){
                    $("html, body").animate({ scrollTop: 0 }, 200);
                    JoinClazz.refresh();
                }else{
                    //$.prompt.close();
                    create_btn_click();
                }
            }

            //新注册用户加入班级
            if($17.getQuery("step") == "showtip"){
                $17.voxLog({
                    module: "reg",
                    op : "newclazzsubmit1",
                    step : 11
                });
            }
        }else{
            $this.siblings(".data-info").show().text(data.info);
        }
    });
}

//减按钮
function minus_click(){
    var $this = $(this);
    var $index = $this.data("index");
    var clazzNumBox = $this.siblings(".v-student-num");
    var clazzNum = parseInt(clazzNumBox.val()) - 1;

    if(clazzNum <= 1){
        clazzNumBox.val(1);
        StudentInfo.tempInfo.base.clazzName[$index].classSize = 1;
        $this.addClass("w-btn-disabled");
        return false;
    }

    clazzNumBox.val(clazzNum);

    StudentInfo.tempInfo.base.clazzName[$index].classSize = clazzNum;
    $this.siblings(".v-plus-btn").removeClass("w-btn-disabled");
    return false;
}

//加按钮
function plus_click(){
    var $this = $(this);
    var $index = $this.data("index");
    var clazzNumBox = $this.siblings(".v-student-num");
    var clazzNum = parseInt(clazzNumBox.val()) + 1;

    if(clazzNum >= 90){
        clazzNumBox.val(90);
        StudentInfo.tempInfo.base.clazzName[$index].classSize = 90;
        $this.addClass("w-btn-disabled");
        return false;
    }

    clazzNumBox.val(clazzNum);

    StudentInfo.tempInfo.base.clazzName[$index].classSize = clazzNum;
    $this.siblings(".v-minus-btn").removeClass("w-btn-disabled");
    return false;
}

//人数输入框获得焦点
function clazzmax_focus(){
    $(this).select();

    return false;
}

//人数输入框失去焦点
function clazzmax_keyup(){
    var $this = $(this);
    var $index = $this.data("index");
    var clazzNum = $this.val();

    $this.siblings(".v-minus-btn").removeClass("w-btn-disabled");
    $this.siblings(".v-plus-btn").removeClass("w-btn-disabled");

    if(clazzNum <= 1){
        $this.on("blur", function(){
            if($(this).val() == ""){
                $this.val(1);
            }
        });
        StudentInfo.tempInfo.base.clazzName[$index].classSize = 1;
        $this.siblings(".v-minus-btn").addClass("w-btn-disabled");
        return false;
    }

    if(clazzNum >= 90){
        $this.val(90);
        StudentInfo.tempInfo.base.clazzName[$index].classSize = 90;
        $this.siblings(".v-plus-btn").addClass("w-btn-disabled");
        return false;
    }

    if(!$17.isNumber(clazzNum)){
        $this.val(StudentInfo.tempInfo.base.clazzName[$index].classSize);
        return false;
    }

    StudentInfo.tempInfo.base.clazzName[$index].classSize = clazzNum;
    return false;
}

//创建班级
function create_btn_click(){
    var $this = $(this);
    if($this.hasClass("w-btn-disabled")){
        return false;
    }

    //添加班级页-方式-提交
    $17.voxLog({
        module: _tempLogModule,
        op :  $(this).data("step-content"),
        step : $(this).data("step-id")
    });

    $this.addClass("w-btn-disabled");

    App.postJSON("/teacher/clazz/createclazz.vpage", {mappers : StudentInfo.tempInfo.base.clazzName}, function(data){
        if(data.success){
            //已创建班级的ID
            var $clazzIds = [];
            for(var i = 0, slList = data.sl; i < slList.length; i++){
                $clazzIds.push(slList[i].clazzId);
            }

            if($17.getQuery("step") == "showtip"){
                $17.tongji("注册流程—创建班级—生成学生账号");

                $("#Anchor").remove();
                $(".step-container-complete").show();
                containerComplete();

                //添加班级页-方式-提交
                $17.voxLog({
                    module: "reg",
                    op :  "clazz-click-homeworkBtn",
                    step : 28
                });
                return false;
            }else{
                //创建成功
                $("#Anchor").html(template("t:添加班级成功页面", {clazzIds : $clazzIds.join()}));
            }
        }else{
            $this.removeClass("w-btn-disabled");
            $17.alert(data.info);
        }
    });
}

//数据结构
function uploadDealData(key){
    delete key.invalidClazzs;
    delete key.validClazzs;
    delete key.clazzLevel;
    delete key.fullName;

    key.addStudentType = "common";
    key.classLevel = ClazzInfo.tempInfo.base.level;
    key.eduSystem = ClazzInfo.tempInfo.base.schoolLength;
    key.schoolId = ClazzInfo.tempInfo.base.schoolId;
    key.classSize = StudentInfo.tempInfo.base.clazzNum;
    key.names = [];

    return key;
}