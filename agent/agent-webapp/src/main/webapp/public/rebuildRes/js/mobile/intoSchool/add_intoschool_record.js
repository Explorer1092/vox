/**
 * 添加进校计划
 * */
define(["dispatchEvent","common"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        if(selectedType.length != 0){
            $('.js-editTitle>div[data-type="'+selectedType+'"').addClass("the");
            $('.mainTitle>div[data-type="'+selectedType+'"').addClass("the");
        }
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-visitSchoolBtn",
                    eventType:"click",
                    callBack:function(){
                        saveInfoToNewPage("chooseSchool.vpage?back=add_intoSchool_record.vpage");
                    }
                },
                {
                    selector:".js-visitTeacherBtn",
                    eventType:"click",
                    callBack:function(){
                        var schoolId = $("#schoolId").val();
                        if(schoolId){
                            saveInfoToNewPage("searchTeacherListPage.vpage?schoolId="+schoolId);
                        }else{
                            AT.alert("请先选择学校");
                        }
                    }
                },
                {
                    selector:".js-writeVisitDetail",
                    eventType:"click",
                    callBack:function(){
                        var teacherName  = $(".js-visitTeacherBtn").html().trim();
                        if( (teacherName != "") && teacherName != "请选择"){
                            saveInfoToNewPage("modificationSchoolRecord.vpage");
                        }else{
                            AT.alert("请先选择陪访老师");
                        }
                    }
                },
                {
                    selector:"div.mainTitle>div",
                    eventType:"click",
                    callBack:function(){
                        $(this).addClass("the").siblings("div").removeClass("the");
                    }
                },
                {
                    selector: ".js-signBtn",
                    eventType: "click",
                    callBack: function () {
                        getSignLocation();
                    }
                },
                {
                    selector: "#reSignBtn",
                    eventType: "click",
                    callBack: function () {
                        $("#confirmSignDialog").hide();
                        getSignLocation();
                    }
                },
                {
                    selector: "#getSchoolGateImageBtn",
                    eventType: "click",
                    callBack: function () {
                        $("#confirmSignDialog").hide();
                        getSchoolImage();
                    }
                },
                {
                    selector: "#getSchoolGate",
                    eventType: "click",
                    callBack: function () {
                        getSchoolImage();
                    }
                },
                {
                    selector: "#selectPartner",
                    eventType: "change",
                    callBack: function () {
                        var type = $(this).val();
                        $('#partner').val(type);
                        $('#partnerDisplay').html($('#selectPartner option:selected').text());
                    }
                },
                {
                    selector: ".js-submitSchoolSRecord",
                    eventType: "click",
                    callBack: function () {
                        if(checkData()){
                            var postData = {};
                            $.each($(".js-postData"),function(i,item){
                                postData[item.name] = $(item).val();
                            });

                            postData["workTitle"] = $(".mainTitle>div.the").data("type");
                            postData["followingTime"] = $("#nextVisitTime").val();
                            postData["isAgencyRegion"] = $('[name="isAgencyClue"]>div.the').attr("data-opvalue");

                            $.post("saveSchoolRecord.vpage",postData, function (res) {
                                if(res.success){
                                    AT.alert("添加进校记录成功");
                                    location.href =  "/mobile/performance/index.vpage";
                                }else{
                                    AT.alert(res.info);
                                }
                            })
                        }
                    }
                },
                {
                    selector: ".js-editSchoolSRecord",
                    eventType: "click",
                    callBack: function () {
                        var rid = $(this).data("rid");
                        location.href = "modificationSchoolRecord.vpage?schoolRecordId="+rid;
                    }
                },
                {
                    //selector: "#nextVisitTime",
                    //eventType: "click",
                    //callBack: function () {
                    //    $("#visitDateDialog").show();
                    //}
                },
                {
                    selector: "#visDateSure",
                    eventType: "click",
                    callBack: function () {
                        var startVal = $("#nextVisitDate").val();
                        $("#nextVisitTimeDisplay").html(startVal);
                        $("#nextVisitTime").val(startVal);
                        $("#visitDateDialog").hide();
                    }
                },
                {
                    selector: "#visDateCancel",
                    eventType: "click",
                    callBack: function () {
                        $("#visitDateDialog").hide();
                    }
                },
                {
                    selector: ".js-ulItem>div",
                    eventType: "click",
                    callBack: function () {
                        $(this).addClass("the").siblings("div").removeClass("the");
                    }
                },
                {
                    selector: "#nextVisitTime",
                    eventType: "change",
                    callBack: function () {
                        $(".select-date").html($(this).val());
                    }
                }

            ]
        };

        new dispatchEvent(eventOption);

        //签到获取位置成功
        var locationSuccess = function(position)  {
            $(".js-signBtn").removeClass("disabled").text("签到");
            var coords = position.coords;
            var x = coords.longitude;
            var y = coords.latitude;
            $("#lat").val(y);
            $("#lng").val(x);
            $.post("signIn.vpage",{
                schoolId:$("#schoolId").val(),
                latitude:y,
                longitude:x,
                coordinateType:"wgs84"
            },function(res){
                if(res.success){
                    signSuccess = true;
                    $(".js-signBtn").addClass("disabled").text("签到成功");
                }else{
                    AT.alert(res.info);
                    $(".js-imageItem").show();
                    imageSuccess = false;

                    if(res.farAway){
                        $("#confirmSignDialog").show();
                    }else{
                        //初始化image
                        $("#getSchoolGate").find("img").attr("src","");
                    }
                }
            });
        };

        var locationError = function (error) {
            switch (error.code) {
                case error.TIMEOUT:
                    AT.alert("获取位置出错: 获取地理位置超时，请稍后重试");
                    break;
                case error.POSITION_UNAVAILABLE:
                    AT.alert("获取位置出错: 当前无法跟踪位置");
                    break;
                case error.PERMISSION_DENIED:
                    AT.alert("获取位置出错: 请允许应用获取地理位置信息");
                    break;
                case error.UNKNOWN_ERROR:
                    AT.alert("获取位置出错: 发生未知错误");
                    break;
            }
            $(".js-signBtn").attr("disabled", false).text("签到");
        };

        var getSignLocation = function(){
            if(!$(".js-signBtn").hasClass("disabled")){
                var schoolId = $("#schoolId").val();
                if(schoolId){
                    if (navigator.geolocation) {
                        $(".js-signBtn").attr("disabled", true).text("获取中...");
                        navigator.geolocation.getCurrentPosition(locationSuccess, locationError, {
                            // 指示浏览器获取高精度的位置，默认为false
                            enableHighAccuracy: true,
                            // 指定获取地理位置的超时时间，默认不限时，单位为毫秒
                            timeout: 5000,
                            // 最长有效期，在重复获取地理位置时，此参数指定多久再次获取位置。
                            maximumAge: 1000
                        });
                        $(".js-signBtn").addClass("disabled");
                    } else {
                        AT.alert("您的游览器不支持Html5 Geolocation,无法获取位置信息");
                    }
                }else{
                    AT.alert("请先选择学校");
                }
            }
        };

        var getSchoolImage = function(){
            var carData =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:false,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(carData));
        };

        //检测提交数据
        var checkData = function(){

            if(!$("#schoolId").val()){
                AT.alert("请选择学校");
                return false;
            }

            if(imageSuccess || $("#photoUrl").val()){
                imageSuccess = true;
                signSuccess = true;
            }

            if(!signSuccess){
                AT.alert("请签到获取学校位置");
                return false;
            }

            if(!imageSuccess){
                AT.alert("请拍摄学校正门照片");
                return false;
            }

            if($(".js-visitTeacherBtn").html().trim() == "请选择"){
                AT.alert("请选择陪访老师");
                return false;
            }

            if($(".mainTitle>div.the").length == 0){
                AT.alert("请选择拜访主题");
                return false;
            }

            if($('[name="isAgencyClue"]').length != 0){
                if($('[name="isAgencyClue"]>div.the').length == 0){
                    AT.alert("请选择选择是否为代理提供的线索");
                    return false;
                }
            }

            return true;
        };

        //跳页面之前保存信息
        var saveInfoToNewPage = function(url){
            var agencyClue = 0;
            if($('[name="isAgencyClue"]>div.the').length != 0){
                agencyClue = $('[name="isAgencyClue"]>div.the').attr("data-opvalue");
            }
            var postDate = {
                workTitle:$(".mainTitle>div.the").data("type"),
                partnerId:$("#selectPartner").val(),
                followingTime:$("#nextVisitTime").val(),
                followingPlan:$('#followingPlan').val(),
                agencyClue:agencyClue
            };
            $.post("saveSchoolRecordSession.vpage",postDate,function(res){
                if(res.success){
                    location.href = url;
                }else{
                    AT.alert(res.info);
                }
            });
        };


    });
});