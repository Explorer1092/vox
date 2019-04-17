<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="添加组会" pageJs="groupmeeting" footerIndex=4 navBar="hidden">
<@sugar.capsule css=['new_base','school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div id="addMeet" style="overflow: hidden">
    <div style="display:none;">
        <a href="javascript:void(0);" class="inner-right js-submitGroupMeetBtn">提交</a>
    </div>
    <div class="flow">
        <div class="item tip clearfix">
                <span class="inner-right">
                ${.now?string("yyyy年MM月dd日")}
                </span>
        </div>
        <div class="item GPS clearfix show">
            会议级别
            <div name="meetingType" class="inner-right js-ulItem js-meetingLevel show" style="padding:0" data-einfo="请选择组会级别">
                <div data-opvalue="1" class="btn-stroke fix-width" style="width:2.5rem;margin-right:0.3rem;">省级</div>
                <div data-opvalue="2" class="btn-stroke fix-width" style="width:2.5rem;margin-right:0.3rem;">市级</div>
                <div data-opvalue="3" class="btn-stroke fix-width" style="width:2.5rem;margin-right:0.3rem;"">区级</div>
                <div data-opvalue="4" class="btn-stroke fix-width" style="width:2.5rem;">校级</div>
            </div>
        </div>
        <div class="item GPS">
            请打开GPS
            <#if signType?? && signType == 1>
                <div class="inner-right btn-stroke fix-padding disabled">
                    签到成功
                </div>
            <#else>
                <div class="inner-right btn-stroke fix-padding js-signBtn">
                    签到
                </div>
            </#if>
        </div>
        <div class="item unschoolMeeting show">
            主题
            <div class="inner-right-text">
                <input type="text" placeholder="请填写，不超过10个字" class="js-postData" name="meetingTitle" maxlength="10" data-einfo="请填写主题">
            </div>
        </div>
        <div class="item js-chooseSchool schoolMeeting show">
            学校名称
            <div class="inner-right-text">
                <input type="text" readonly placeholder="请选择&gt;" class="js-postData" name="schoolName" maxlength="10" data-einfo="请选择学校名称">
                <input hidden type="text" readonly class="js-postData" name="schoolId" data-einfo="请选择学校名称">
            </div>
        </div>
        <div class="item js-chooseTeacher schoolMeeting show">
            参会老师
            <div class="inner-right-text">
                <input id="teacherLength" readonly type="tel" placeholder="请选择&gt;" class="side-fr side-time js-postData" name="meeterCount" maxlength="6" data-einfo="请选择参会老师">
                <input hidden type="tel" id="" placeholder="请选择&gt;" class="side-fr side-time js-postData" name="teacherIds" maxlength="6" data-einfo="请选择参会老师">
            </div>
        </div>
        <div class="item unschoolMeeting show">
            参会人数
            <div class="inner-right-text">
                <input type="tel" id="group_people_count" placeholder="请填写整数" class="side-fr side-time js-postData" name="meeterCount" maxlength="6" data-einfo="请填写参会人数">
            </div>
        </div>
        <div class="item unschoolMeeting show js-instructorName">
            教研员
            <div class="inner-right-text">
                <input type="text" id="group_key_name" readonly placeholder="请选择&gt;" class="side-fr side-time js-postData" name="instructorName" data-einfo="请填写教研员姓名">
                <input hidden type="text" id="group_key_name" readonly placeholder="请选择&gt;" class="side-fr side-time js-postData" name="instructorId" data-einfo="请填写教研员姓名">
            </div>
        </div>
        <div class="item GPS clearfix unschoolMeeting show">
            教研员是否在场
            <div name="instructorAttend" class="inner-right js-ulItem" style="padding:0" data-einfo="请选择教研员是否在场">
                <div data-opvalue="1" class="btn-stroke fix-width" style="width:3.75rem;margin-right:0.5rem;">是</div>
                <div data-opvalue="2" class="btn-stroke fix-width" style="width:3.75rem;">否</div>
            </div>
        </div>
        <div class="item show">
            讲师
            <div class="inner-right-text">
                <input type="text" id="" placeholder="请填写" class="side-fr side-time js-postData" name="lecturer" data-einfo="请填写讲师姓名">
            </div>
        </div>
        <div class="item GPS clearfix show">
            宣讲时长
            <div name="meetingLong" class="inner-right js-ulItem" style="padding:0" data-einfo="请选择宣讲时长">
                <div data-opvalue="1" class="btn-stroke fix-width" style="width:3.2rem;margin-right:0.5rem;"><15min</div>
                <div data-opvalue="2" class="btn-stroke fix-width" style="width:3.2rem;margin-right:0.5rem;font-size:0.65rem;">15-60min</div>
                <div data-opvalue="3" class="btn-stroke fix-width" style="width:3.2rem;">>1h</div>
            </div>
        </div>
        <div class="item GPS clearfix show">
            类型
            <div name="showFrom" class="inner-right js-ulItem" style="padding:0" data-einfo="请选择组会类型" >
                <div data-opvalue="1" class="btn-stroke fix-width" style="width:3.75rem;margin-right:0.5rem;">专场</div>
                <div data-opvalue="2" class="btn-stroke fix-width" style="width:3.75rem;">插播</div>
            </div>
        </div>
        <div class="item tip show">
            <div class="photo clearfix photo2">
                <div class="shot">
                    现场照片
                </div>
                <div class="pick">
                    <div class="file" id="getSchoolGate"><img src="" width="100%" height="100%" ></div>
                    <input type="hidden" id="photoUrl" name="photoUrl" value="">
                    <div class="text">
                        <p class="dt">点此添加照片</p>
                        <p class="dd">尽量包含横幅、电子屏、 一起作业讲师等内容</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="item tip show">
            会议内容及效果（选填）
            <textarea rows="5" class="content" id="meetingNote" name="meetingContent" data-einfo="请填写会议内容及效果" placeholder="请点击填写..."></textarea>
        </div>
    </div>
</div>
<div id="region" style="display:none;">
    <div class="head">
        <div class="return" id="region_back"><i class="return-icon"></i>返回</div>
        <span class="return-line"></span>
        <span class="h-title">选择区域</span>
    </div>
    <div id="regionList" class="s-list">

    </div>
</div>
<script>
    var vox = vox || {};
    vox.task = vox.task || {};
    var AT = new agentTool();
    var meetingLatitude = "";
    var meetingLongitude = "";
    AT.cleanAllCookie();
        store.clear();
    var doLocalData = function () {

    };
    vox.task.refreshData = function () {
        doLocalData()
    };

    vox.task.setLocation = function(res){
        visitData = {};
        var resJson = JSON.parse(res);
        //签到回调
        if(!resJson.errorCode){
            meetingLongitude = resJson.location.longitude;
            meetingLatitude  = resJson.location.latitude;
            if(meetingLatitude != '' && meetingLongitude != ''){
                $(".js-signBtn").addClass("disabled").text("签到成功");
            }
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
    $(".js-signBtn").on("click",function(){
        getSignLocation();
    });
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
    //图片完成回调
    vox.task.setImageToHtml = function(result){
        var resJson = JSON.parse(result);
        try{
        if(!resJson.errorCode){
            if(resJson.fileUrl){
                var url = resJson.fileUrl;
                setTimeout(function () {
                    $("#photoUrl").val(url);
                    $("#getSchoolGate").find("img").attr("src",url);
                },150)
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
        }catch(e){alert(e)}
    };



    $(document).ready(function() {
//        reloadCallBack();

        var postData = {};

        $("#getSchoolGate").on("click",function(){
            var carData =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:true,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(carData));
        });

        $(".js-submitGroupMeetBtn").on("click",function(){
            var _this = $(this);
            _this.removeClass("js-submitGroupMeetBtn");
            if (checkData(_this)) {
                if (selectLevel != 4 && !validNumber($("#group_people_count").val())) {
                    AT.alert("参会人数请填写数字");
                    _this.addClass("js-submitGroupMeetBtn");
                    return false;
                }

                postData["meetingContent"] = $("#meetingNote").val();
                postData["scenePhotoUrl"] = $("#photoUrl").val();
                postData["agencyId"]=$("#agencyId").val();
                postData["latitude"] = meetingLatitude;
                postData["longitude"]= meetingLongitude;
                postData["signType"]= 1;
                $.post("saveGroupMeetingRecord.vpage", postData, function (res) {
                    if (res.success) {
                        AT.alert("添加组会成功");
                        setTimeout("window.location.href='/view/mobile/crm/visit/visit_detail.vpage'",2000);
                    } else {
                        _this.addClass("js-submitGroupMeetBtn");
                        $(".js-signBtn").removeClass("disabled").text("签到");
                        AT.alert(res.info);
                    }
                });
            }
        });

        $("#meetingRegionShow").on("click",function(){
            location.href = "/mobile/work_record/load_region_page.vpage?regionCode=${regionCode!0}";
        });

        var provinceRegion = 0;
        var cityRegion;
        var countyRegion;
        var level = 0;

        $(document).on("click",".regionLevel",function(){
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
        });

        $(document).on("click","#region_back",function(){
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
        });

        $(document).on("click",".countyCodeName",function(){
            var countyStr = $(this).attr("data-idName").split("_");
            var ccode = countyStr[0];
            var cname = countyStr[1];
            level = 0;
            $("#meetingRegion").attr("value", ccode);
            $("#meetingRegionShow").html(cname);

           /* $.post("judgeRegionIsAgent.vpage", {regionCode: ccode}, function (res) {
                if (res.success) {
                    if (res.isAgencyRegion) {
                        $("#isAgentDiv").show();
                        $('[name="isAgencyClue"]').removeClass("disabled");
                    } else {
                        $("#isAgentDiv").hide();
                        $('[name="isAgencyClue"]').addClass("disabled");
                    }
                } else {
                    AT.alert(res.info);
                }
            });*/

            $("#region").hide();
            $("#addMeet").show();
        });

        $(".js-ulItem>div").on("click",function(){
            $(this).addClass("the").siblings("div").removeClass("the");
        });
        var selectLevel = 0;
        $(".js-meetingLevel div").on("click",function(){
            selectLevel = $(this).data("opvalue");
            if(selectLevel == 4){
                $(".schoolMeeting").addClass('show').removeClass("hide").siblings(".unschoolMeeting").removeClass("show").addClass('hide');
            }else{
                $(".schoolMeeting").removeClass('show').addClass("hide").siblings(".unschoolMeeting").addClass("show").removeClass("hide");
            }
        });


        //检测提交数据
        var checkData = function (_this) {
            var flag = true;
            if($(".show #group_people_count").val() != "" && $(".show #group_people_count").val()< 30){
                AT.alert("省市区级组会需要至少参与30位老师");
                flag = false;
                _this.addClass("js-submitGroupMeetBtn");
                return false;
            }
            if($(".show #teacherLength").val() != "" && $(".show #teacherLength").val() < 6){
                AT.alert("校级组会需大于等于6位老师");
                flag = false;
                _this.addClass("js-submitGroupMeetBtn");
                return false;
            }

            $.each($(".show .js-postData"), function (i, item) {
                postData[item.name] = $(item).val();
                if (!($(item).val())) {
                    AT.alert($(item).data("einfo"));
                    flag = false;
                    _this.addClass("js-submitGroupMeetBtn");
                    return false;
                }
            });


            if (flag) {
                if ($('[name="isAgencyClue"]').hasClass("disabled")) {
                    $('[name="isAgencyClue"]').removeClass("js-ulItem");
                }
                $.each($(".js-ulItem.show"), function (i, item) {
                    if ($(item).children("div.the").length == 0) {
                        AT.alert($(item).data("einfo"));
                        flag = false;
                        _this.addClass("js-submitGroupMeetBtn");
                        return false;
                    } else {
                        postData[$(item).attr("name")] = $(item).children("div.the").attr("data-opvalue");
                    }
                });
                $.each($(".show .js-ulItem"), function (i, item) {
                    if ($(item).children("div.the").length == 0) {
                        AT.alert($(item).data("einfo"));
                        flag = false;
                        _this.addClass("js-submitGroupMeetBtn");
                        return false;
                    } else {
                        postData[$(item).attr("name")] = $(item).children("div.the").attr("data-opvalue");
                    }
                });
            }

            if (flag) {

                if (!$("#photoUrl").val()) {
                    AT.alert("请上传现场照片");
                    flag = false;
                    _this.addClass("js-submitGroupMeetBtn");
                    return false;
                }
            }

            return flag;
        };
    });

    $("#selectAgencyId").on("change",function(){
        var type = $(this).val();
        $('#agencyId').val(type);
        $('#agencyDisplay').html($('#selectAgencyId option:selected').text());
    });
    $(document).on("click",".js-instructorName",function () {
        doLocalData = function () {
            var meetingInstructorId = store.get("meetingInstructorId",0);
            var meetingInstructorName = store.get("meetingInstructorName","");
            $('input[name="instructorId"]').val(meetingInstructorId);
            $('input[name="instructorName"]').val(meetingInstructorName);
        };
        openSecond('/mobile/researchers/researchers_list.vpage?backUrl=addMeeting');
    });
    $(document).on("click",".js-chooseSchool",function () {
        doLocalData = function () {
            var schoolId = store.get("meetingSchoolId",0);
            var schoolName = store.get("meetingSchoolName","");
            $('input[name="schoolId"]').val(schoolId);
            $('input[name="schoolName"]').val(schoolName);
        };
        openSecond('/mobile/work_record/chooseSchool.vpage?back=addMeeting');
    });
    $(document).on("click",".js-chooseTeacher",function () {
        var schoolId = store.get("meetingSchoolId",0);
        if(schoolId == 0){
            AT.alert("请先选择学校");
            return ;
        }
        doLocalData = function () {
            var teacherIds = store.get("meetingTeacherIds",[]);
            $('input[name="teacherIds"]').val(teacherIds);
            $('#teacherLength').val(teacherIds.length);
        };
        openSecond('/mobile/work_record/searchTeacherListPage.vpage?schoolId='+schoolId +"&backUrl=addMeeting")
    })
</script>
</@layout.page>
