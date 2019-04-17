var signSuccess = false;
var imageSuccess = true;
var callBackFlag = "";
var latAfterLocation = 0,lngAfterLocation = 0;
var vox = vox || {};
vox.task = vox.task || {};
$('.top-holder').hide();
var AT = new agentTool();
AT.cleanAllCookie();

var testLog = function(msg){
};

function IsNum(num) {
    var reNum = /^\d*$/;
    return (reNum.test(num));
}

function checkIsNotNumAndLessZero(value) {
    return !IsNum(value) || value < 0;
}
var opName,S0Name,S1Name,S2Name,S3Name,S4Name;
var visitData = {};

var _loading;
//获取位置回调
vox.task.setLocation = function(res){
    visitData = {};
    var resJson = JSON.parse(res);
        //签到回调
    if(!resJson.errorCode){
        var x = resJson.location.longitude;
        var y = resJson.location.latitude;
        $("#lat").val(y);
        $("#lng").val(x);
        var schoolId = $("#schoolId").val();
        visitData.schoolId = schoolId;
        visitData.latitude = y;
        visitData.signType = 1;
        visitData.longitude = x;
        visitData.workRecordType = workRecordType;
        if(moduleName == "m_QnJGa29M"){
            opName = "o_CxLP3gQK" ;
        }else if(moduleName == "m_9HYOoJg7"){
            opName = "o_TYu2AWx2" ;
        }
        S0Name = schoolId;
        S2Name = x + ","+ y;
        $.post("signIn.vpage",visitData,function(res){
            if(res.success){
                signSuccess = true;
                S1Name = "成功";
                $(".js-signBtn").addClass("disabled").text("签到成功");
            }else{
                if(res.info){
                    S1Name = "失败";
                    AT.alert(res.info);
                }
                $(".js-imageItem").show();
                imageSuccess = false;

                if(res.farAway){
                    $("#confirmSignDialog").show();
                }else{
                    //初始化image
                    $("#getSchoolGate").find("img").attr("src","");
                }
                if (res.noLocation) {
                    $("#photoSignDialog").show();
                } else {
                    $("#getSchoolGate").find("img").attr("src", "");
                }
            }
            try{
            YQ.voxLogs({
                database : "marketing", //不设置database  , 默认库web_student_logs
                module : moduleName, //打点流程模块名
                op : opName ,//打点事件名
                userId:userId,
                s0 : S0Name ,
                s1 : S1Name,
                s2 : S2Name
            });
            }catch(e){
                alert(e)
            }
        });
    }else if(resJson.errorCode == 1){
        $(".js-signBtn").removeClass("disabled").text("签到");
        $(".js-signBtn").addClass("js-signBtn");
        setTimeout(function(){
            AT.alert("客户端无位置信息权限");
        },150);
    }else if(resJson.errorCode == 2){
        $(".js-signBtn").removeClass("disabled").text("签到");
        $(".js-signBtn").addClass("js-signBtn");
        setTimeout(function(){
            AT.alert("客户端获取地理位置失败");
        },150);
    }else if(resJson.errorCode == 3){
        $(".js-signBtn").removeClass("disabled").text("签到");
        $(".js-signBtn").addClass("js-signBtn");
        setTimeout(function(){
            AT.alert("客户端获取地理编码失败");
        },150);
    }
    layer.close(_loading);
};
//拍照回调
vox.task.setImageWithGPS = function(result){
    visitData = {};
    var resJson = JSON.parse(result);
    if(!resJson.errorCode){
        if(resJson.url){
            var url = resJson.url;
            var schoolId = $("#schoolId").val()
            visitData.schoolId = schoolId;
            visitData.workRecordType = workRecordType;
            visitData.url = url;
            visitData.signType = 2;
            // alert(JSON.stringify(resJson))
            latAfterLocation = JSON.stringify(resJson.info.Latitude);
            lngAfterLocation = JSON.stringify(resJson.info.Longitude);
            visitData.latitude = latAfterLocation;
            visitData.longitude = lngAfterLocation;
            if(moduleName == "m_QnJGa29M"){
                opName = "o_6RXDszj6" ;
            }else if(moduleName == "m_9HYOoJg7"){
                opName = "o_YlBe3S8v" ;
            }
            S0Name = schoolId;
            S1Name = latAfterLocation + "," + lngAfterLocation;
            S2Name = resJson.info.DateTime + ","+ resJson.info.Model + ","+resJson.info.Latitude  + "," +resJson.info.Longitude ;
            try{
            YQ.voxLogs({
                database : "marketing", //不设置database  , 默认库web_student_logs
                module : moduleName, //打点流程模块名
                op : opName ,//打点事件名
                userId:userId,
                s0 : S0Name ,
                s1 : S1Name,
                s2 : S2Name
            });
            }catch (e){
                alert(e)
            }

            $.post("saveSchoolPhoto.vpage",visitData,function(res){
                if(res.success){
                    $("#getSchoolGate").find("img").attr("src",url + "?x-oss-process=image/resize,w_48,h_48/auto-orient,1");
                    $("#photoUrl").val(url);
                    imageSuccess = true;
                    signSuccess = true;
                    $(".js-signBtn").hide(); //拍了照片视为签到成功,隐藏签到按钮,防止再操作!
                }else{
                    AT.alert(res.info);
                }
            });
        }else{
            setTimeout(function(){
                AT.alert("客户端未获取到图片");
            },150);
        }
    }else{
        setTimeout(function(){
            AT.alert("客户端出错");
        },150);
    }
};
$(function(){

    var getSchoolImage = function(){
        callBackFlag = "image";
        var data =  {
            NeedAlbum:false,
            NeedCamera:true
        };
        do_external('getImageWithGPS',JSON.stringify(data));
    };

    //检测提交数据


    //跳页面之前保存信息
    var saveInfoToNewPage = function(url){
        if(schoolLevel != 4){
            var postDate = {
                workTitle:$(".mainTitle>div.the").data("type"),
                agencyId:$("#agencyId").val()
            };
        }else{
            var postDate = {
                workTitle:$(".mainTitle>div.the").data("type"),
            };
        }

        $.post("saveSchoolRecordSession.vpage",postDate,function(res){
            if(res.success){
                openSecond(url);
            }else{
                AT.alert(res.info);
            }
        });
    };
    var getSignLocation = function(){
        if(!$(".js-signBtn").hasClass("disabled")){
            _loading = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });
            var schoolId = $("#schoolId").val();
                callBackFlag = "sign";
                do_external('getLocation');
            }else{
            layer.close(_loading);
            return false ;
            }

        };



    $(".js-editSchoolSRecord").on("click",function(){
        var rid = $(this).data("rid");
        openSecond("/mobile/work_record/modificationSchoolRecord.vpage?schoolRecordId="+rid);
    });

    $(".js-visitSchoolBtn").on("click",function(){
        saveInfoToNewPage("/mobile/work_record/chooseSchool.vpage?back=add_intoSchool_record.vpage");
    });

    $(".js-visitTeacherBtn").on("click",function(){
        var schoolId = $("#schoolId").val();
        if(schoolId){
            saveInfoToNewPage("/mobile/work_record/searchTeacherListPage.vpage?schoolId="+schoolId);
        }else{
            AT.alert("请先选择学校");
        }
    });

    $(".js-expected-data").on("click", function () {
        var schoolId = $("#schoolId").val();
        if (schoolId) {
            $(".schoolRecord-pop").show();
            $('#register_stu').val($('#register_stu_value').val());
            $('#auth_stu').val($('#auth_stu_value').val());
            $('#single_act_stu').val($('#single_act_stu_value').val());
            $('#double_act_stu').val($('#double_act_stu_value').val());
        } else {
            AT.alert("请先选择学校");
        }
    });

    $(".js-writeVisitDetail").on("click",function(){
        var teacherName  = $("#visitTeachListDiv").html().trim();
        if( (teacherName != "") && teacherName != "请选择"){
            saveInfoToNewPage("/mobile/work_record/modificationSchoolRecord.vpage");
        }else{
            AT.alert("请先选择拜访老师");
        }
    });

    $("div.mainTitle>div").on("click",function(){
        $(this).addClass("the").siblings("div").removeClass("the");
    });
    $(".js-signBtn").on("click",function(){
        testLog("sign btn click");
        getSignLocation();
    });

    $("#reSignBtn").on("click",function(){
        $("#confirmSignDialog").hide();
        getSchoolImage();
    });

    $("#getSchoolGateImageBtn").on("click",function(){
        $("#confirmSignDialog").hide();
        getSchoolImage();
    });
    $("#getSchoolGateImagePhotoBtn").on("click", function () {
        $("#photoSignDialog").hide();
        getSchoolImage();
    });

    $("#close_win").on("click", function () {
        $("#expected_data").hide();
    });
    $(".cancel_btn").on("click", function () {
        $(".schoolRecord-pop").hide();
    });

    $(".sure_btn").on("click", function () {
        $(".schoolRecord-pop").hide();
        var register_stu = $("#register_stu").val();
        var auth_stu = $("#auth_stu").val();
        var single_act_stu = $("#single_act_stu").val();
        $("#register_stu_value").val(register_stu);
        $("#auth_stu_value").val(auth_stu);
        $("#single_act_stu_value").val(single_act_stu);
        if(schoolLevel == 1) {
            var double_act_stu = $("#double_act_stu").val();
            $("#expectedDataDisplay").html(register_stu + "、" + auth_stu + "、" + single_act_stu + "、" + double_act_stu);
            $("#double_act_stu_value").val(double_act_stu);
        }else{
            $("#expectedDataDisplay").html(register_stu + "、" + auth_stu + "、" + single_act_stu );
        }
        $("#expected_data").hide();
    });

    $(document).on("click","#getSchoolGate",function(){
        getSchoolImage();
    });

    $("#selectPartner").on("change",function(){
        var type = $(this).val();
        $('#partner').val(type);
        $('#partnerDisplay').html($('#selectPartner option:selected').text());
    });

    $("#nextVisitTime").on("click",function(){
        $("#visitDateDialog").show();
    });
    $(".submitBtn").on("click",function(){
        disMissViewCallBack();
    });

    $("#visDateSure").on("click",function(){
        var startVal = $("#nextVisitDate").val();
        $("#nextVisitTimeDisplay").html(startVal);
        $("#nextVisitTime").val(startVal);
        $("#visitDateDialog").hide();
    });

    $("#visDateCancel").on("click",function(){
        $("#visitDateDialog").hide();
    });

    $(".js-ulItem>div").on("click",function(){
        $(this).addClass("the").siblings("div").removeClass("the");
    });

    $("#nextVisitTime").on("change",function(){
        $(".select-date").html($(this).val());
    });


    $("#nextVisitTime").width($("#js-selectDate").outerWidth());

});