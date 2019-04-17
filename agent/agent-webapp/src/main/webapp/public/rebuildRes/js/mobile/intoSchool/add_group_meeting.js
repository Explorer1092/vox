/**
 * 添加组会
 * */
define(["dispatchEvent","common"], function (dispatchEvent) {
    $(document).ready(function(){
        var postData = {};
        var AT = new agentTool();

        var provinceRegion = 0;
        var cityRegion;
        var countyRegion;
        var level = 0;
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:"#getSchoolGate",
                    eventType:"click",
                    callBack:function(){
                        var carData =  {
                            uploadUrlPath:"mobile/file/upload.vpage",
                            NeedAlbum:false,
                            NeedCamera:true,
                            uploadPara: {}
                        };
                        do_external('getImageByHtml',JSON.stringify(carData));
                    }
                },
                {//提交
                    selector:".js-submitGroupMeetBtn",
                    eventType:"click",
                    callBack:function(){
                        if(checkData()){

                            if(!validNumber($("#group_people_count").val())){
                                AT.alert("参会人数请填写数字");
                                return false;
                            }

                            if(!isMobile($("#group_key_phone").val())){
                                AT.alert("请填写正确的关键人手机号");
                                return false;
                            }

                            postData["meetingContent"] = $("#meetingNote").val();
                            postData["scenePhotoUrl"] = $("#photoUrl").val();

                            //alert(JSON.stringify(postData));

                            $.post("saveGroupMeetingRecord.vpage",postData,function(res){
                                if(res.success){
                                    AT.alert("添加组会成功");
                                    location.href = "/mobile/performance/index.vpage";
                                }else{
                                    AT.alert(res.info);
                                }
                            });
                        }
                    }
                },
                {
                    selector:"#meetingRegionShow",
                    eventType:"click",
                    callBack:function(){
                        $.ajax({
                            type: "post",
                            url: "regions_list.vpage",
                            dataType: "json",
                            success: function (data) {
                                if (data.success) {
                                    //构造区域级联关系=包括进入和回退
                                    var regionTree = data.regionTree;
                                    var provinceStr = "";
                                    var cityStr = "";
                                    var countyStr = "";
                                    if (regionTree) {
                                        for (var code in regionTree) {
                                            var province = regionTree[code];
                                            provinceStr += "<div id='0_" + code + "' class='regionLevel item'>" + province.name + "<span class='inner-right'></span></div>";
                                            var cityList = province.children;
                                            for (var cityCode in cityList) {
                                                var city = cityList[cityCode];
                                                cityStr += "<div id='" + code + "_" + cityCode + "' class='regionLevel item' style='display:none'>" + city.name + "<span class='inner-right'></span></div>";
                                                var countyList = city.children;
                                                for (var countyCode in countyList) {
                                                    var county = countyList[countyCode];
                                                    countyStr += "<div class='countyCodeName item' data-idName='" + countyCode + "_" + county.name + "' id='" + cityCode + "_" + countyCode + "'  style='display:none'>" + county.name + "<span class='inner-right'></span></div>";
                                                }
                                            }
                                        }
                                    }
                                    var htmlStr = provinceStr + cityStr + countyStr;
                                    $("#addMeet").hide();
                                    $("#meeting_title").hide();
                                    $("#regionList").html(htmlStr);
                                    $("#region_title").hide();
                                    $("#region").show();
                                }
                            }
                        });
                    }
                },
                {
                    selector: ".regionLevel",
                    eventType: "click",
                    callBack: function () {
                        var codes = $(this).attr("id").split("_");
                        var selfId = codes[0] + "_";
                        var childId = codes[1] + "_";
                        $('[id^=' + selfId + ']').hide();
                        $('[id^=' + childId + ']').show();
                        if (selfId == 0 + "_") {
                            provinceRegion = selfId;
                            cityRegion = childId;
                        } else {
                            cityRegion = selfId;
                            countyRegion = childId;
                        }
                        level++;
                    }
                },
                {
                    selector: "#region_back",
                    eventType: "click",
                    callBack: function () {
                        if (level == 2) {
                            $('[id^=' + countyRegion + ']').hide();
                            $('[id^=' + cityRegion + ']').show();
                            level--;
                        } else if (level == 1) {
                            $('[id^=' + cityRegion + ']').hide();
                            $('[id^=' + provinceRegion + ']').show();
                            level--;
                        } else if (level == 0) {
                            $("#region").hide();
                            $("#addMeet").show();
                        }
                    }
                },
                {
                    selector: ".countyCodeName",
                    eventType: "click",
                    callBack: function () {
                        var countyStr = $(this).attr("data-idName").split("_");
                        var ccode = countyStr[0];
                        var cname = countyStr[1];
                        level = 0;
                        $("#meetingRegion").attr("value", ccode);
                        $("#meetingRegionShow").html(cname);

                        $.post("judgeRegionIsAgent.vpage",{regionCode:ccode},function(res){
                            if(res.success){
                                if(res.isAgencyRegion){
                                    $("#isAgentDiv").show();
                                    $('[name="isAgencyClue"]').removeClass("disabled");
                                }else{
                                    $("#isAgentDiv").hide();
                                    $('[name="isAgencyClue"]').addClass("disabled");
                                }
                            }else{
                                AT.alert(res.info);
                            }
                        });

                        $("#region").hide();
                        $("#addMeet").show();
                    }
                },
                {
                    selector: ".js-ulItem>div",
                    eventType: "click",
                    callBack: function () {
                        $(this).addClass("the").siblings("div").removeClass("the");
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

        //检测提交数据
        var checkData = function(){
            var flag = true;
            $.each($(".js-postData"),function(i,item){
                postData[item.name] = $(item).val();
                if(!($(item).val())){
                    AT.alert($(item).data("einfo"));
                    flag = false;
                    return false;
                }
            });

            if(flag){
                if($('[name="isAgencyClue"]').hasClass("disabled")){
                    $('[name="isAgencyClue"]').removeClass("js-ulItem");
                }
                $.each($(".js-ulItem"),function(i,item){
                    if($(item).children("div.the").length == 0){
                        AT.alert($(item).data("einfo"));
                        flag = false;
                        return false;
                    }else{
                        postData[$(item).attr("name")] = $(item).children("div.the").attr("data-opvalue");
                    }
                });
            }

            if(flag){
                if(!$("#photoUrl").val()){
                    AT.alert("请上传现场照片");
                    flag = false;
                    return false;
                }
            }

            return flag;
        };


    });
});