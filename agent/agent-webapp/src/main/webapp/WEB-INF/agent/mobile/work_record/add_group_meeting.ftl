<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="添加组会">
<div id="addMeet">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">添加组会</div>
            <a href="javascript:void(0);" class="headerBtn js-submitGroupMeetBtn">提交</a>
        </div>
    </div>
</div>
<form id="meeting_data" action="add_meeting_record.vpage" enctype="application/x-www-form-urlencoded">
<p>${.now?string("yyyy年MM月dd日")}</p>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list" id="schoolContainer">
        <li>
            <div class="side-fl">主题</div>
            <div class="side-fr">
                <input type="text" placeholder="请填写，不超过10个字" class="js-postData" name="meetingTitle" maxlength="10" data-einfo="请填写主题">
            </div>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="link link-ico">
                <div class="side-fl">地点</div>
                <div class="side-fr side-time js-place" id="meetingRegionShow">请选择</div>
                <input type="hidden" id="meetingRegion" class="js-postData" name="meetingRegion" data-einfo="请选择组会地点">
            </div>
        </li>
        <li>
            <div class="side-fl">参会人数</div>
            <input type="tel" id="group_people_count" placeholder="请填写整数" class="side-fr side-time js-postData" name="meeterCount" maxlength="6" data-einfo="请填写参会人数">
        </li>
        <li>
            <div class="side-fl">讲师</div>
            <input type="text" id="group_people_count" placeholder="请填写" class="side-fr side-time js-postData" name="lecturer" data-einfo="请填写讲师姓名">
        </li>
    </ul>
</div>
<style>
    .side-fr div{cursor: pointer;}
    .side-fr div.active{border: 1px solid #14e437;background-color: #9ee254;}
</style>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="side-fl">级别</div>
            <div class="side-fr js-ulItem" data-einfo="请选择组会级别" name="meetingType">
                <div data-opvalue="1">省级会议</div>
                <div data-opvalue="2">市级会议</div>
                <div data-opvalue="3">区级会议</div>
            </div>
        </li>
        <li>
            <div class="side-fl">宣讲时长</div>
            <div class="side-fr js-ulItem" data-einfo="请选择宣讲时长" name="meetingLong">
                <div data-opvalue="1"> < 15min</div>
                <div data-opvalue="2">15-60min</div>
                <div data-opvalue="3"> > 1h</div>
            </div>
        </li>
        <li>
            <div class="side-fl">类型</div>
            <div class="side-fr js-ulItem" data-einfo="请选择组会类型" name="showFrom">
                <div data-opvalue="1"> 专场 </div>
                <div data-opvalue="2"> 插播 </div>
            </div>
        </li>
        <li id="isAgentDiv" style="display:none;">
            <div class="side-fl">是否为代理提供的线索</div>
            <div class="side-fr js-ulItem" data-einfo="请选择是否为代理提供的线索" name="isAgencyClue">
                <div data-opvalue="1"> 是 </div>
                <div data-opvalue="2"> 否 </div>
            </div>
        </li>
    </ul>
</div>


<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
<ul class="mobileCRM-V2-list">
    <li>
        <div class="side-fl">教研员</div>
        <input type="text" id="group_key_name" placeholder="请填写教研员或者信息处" class="side-fr side-time js-postData" name="instructorName" data-einfo="请填写教研员姓名">
    </li>
    <li>
        <div class="side-fl">电话</div>
        <input type="tel" id="group_key_phone" placeholder="请填写" class="side-fr side-time js-postData" name="instructorMobile" maxlength="11" data-einfo="请填写教研员电话">
    </li>
    <li>
        <div class="side-fl">教研员是否在场</div>
        <div class="side-fr js-ulItem" name="instructorAttend" data-einfo="请选择教研员是否在场">
            <div data-opvalue="1"> 是 </div>
            <div data-opvalue="2"> 否 </div>
        </div>
    </li>
</ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="side-fl">现场照片</div>
            <div class="side-fr">
                <div class="side-fr photoSchoolShortIcon" id="getSchoolGate"><img src=""></div>
                <input type="hidden" id="photoUrl" name="photoUrl" value="">
            </div>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="box">
                <div class="side-fl">会议内容及效果（选填）</div>
            </div>
            <div class="text">
                <textarea placeholder="请填写" id="meetingNote" name="meetingContent" data-einfo="请填写会议内容及效果"></textarea>
            </div>
        </li>
    </ul>
</div>
</form>
</div>
<div id="region" style="display: none;">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <div class="headerBack" id="region_back" style="cursor: pointer;">&lt;&nbsp;返回</div>
                <div class="headerText">选择区域</div>
            </div>
        </div>
    </div>
    <div class="mobileCRM-V2-box mobileCRM-V2-info">
        <ul class="mobileCRM-V2-list" id="regionList">
        </ul>
    </div>
</div>
<script>

    var vox = vox || {};
    vox.task = vox.task || {};

    //图片完成回调
    vox.task.setImageWithGPS = function(data){
        /*前端展示*/
        $("#photoUrl").val("");
        setTimeout(function(){
            $("#photoUrl").val(data.url);
            $("#getSchoolGate").find("img").attr("src",data.url);
        },150);
    };

    $(function(){
        var postData = {};
        //检测提交数据
        var checkData = function(){
            var flag = true;
            $.each($(".js-postData"),function(i,item){
                postData[item.name] = $(item).val();
                if(!($(item).val())){
                    alert($(item).data("einfo"));
                    flag = false;
                    return false;
                }
            });

            if(flag){
                if($('[name="isAgencyClue"]').hasClass("disabled")){
                    $('[name="isAgencyClue"]').removeClass("js-ulItem");
                }
                $.each($(".js-ulItem"),function(i,item){
                    if($(item).children("div.active").length == 0){
                        alert($(item).data("einfo"));
                        flag = false;
                        return false;
                    }else{
                        postData[$(item).attr("name")] = $(item).children("div.active").attr("data-opvalue");
                    }
                });
            }

            if(flag){
                if(!$("#photoUrl").val()){
                    alert("请上传现场照片");
                    flag = false;
                    return false;
                }
            }

            return flag;
        };

        $(document).on("click",".js-submitGroupMeetBtn",function(){
            if(checkData()){

                if(!validNumber($("#group_people_count").val())){
                    alert("参会人数请填写数字");
                    return false;
                }

                if(!isMobile($("#group_key_phone").val())){
                    alert("请填写正确的关键人手机号");
                    return false;
                }

                postData["meetingContent"] = $("#meetingNote").val();
                postData["scenePhotoUrl"] = $("#photoUrl").val();

                $.post("saveGroupMeetingRecord.vpage",postData,function(res){
                    if(res.success){
                        alert("添加组会成功");
                        location.href = "/mobile/performance/index.vpage";
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        $(document).on("change","#selectMeetType",function(){
            var type = $(this).val();
            $('#meetingType').val(type);
            $('#meetingTypeDisplay').html($('#selectMeetType option:selected').text());
        });

        //地点(设计改后重写这一部分)
        $("#meetingRegionShow").on("click", function () {
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
                                provinceStr += "<li id='0_" + code + "' class='regionLevel'><a href='#' class='link link-ico'> <div class='side-fl'>" + province.name + "</div></a></li>";
                                var cityList = province.children;
                                for (var cityCode in cityList) {
                                    var city = cityList[cityCode];
                                    cityStr += "<li id='" + code + "_" + cityCode + "' class='regionLevel' style='display:none'><a href='#' class='link link-ico'> <div class='side-fl'>" + city.name + "</div></a></li>";
                                    var countyList = city.children;
                                    for (var countyCode in countyList) {
                                        var county = countyList[countyCode];
                                        countyStr += "<li class='countyCodeName' data-idName='" + countyCode + "_" + county.name + "' id='" + cityCode + "_" + countyCode + "'  style='display:none'><a href='#' class='link link-ico'><div class='side-fl'>" + county.name + "</div></a></li>";
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
        });

        var provinceRegion = 0;
        var cityRegion;
        var countyRegion;
        var level = 0;
        $(document).on("click", ".regionLevel", function () {
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
        $(document).on("click", "#region_back", function () {
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
        $(document).on("click", ".countyCodeName", function () {
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
                    alert(res.info);
                }
            });

            $("#region").hide();
            $("#addMeet").show();
        });
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                return false;
            }
        });

        $(document).on("click",".js-ulItem>div",function(){
            $(this).addClass("active").siblings("div").removeClass("active");
        });

        $(document).on("click","#getSchoolGate",function(){
            var data =  {
                NeedAlbum:false,
                NeedCamera:true
            };
            do_external('getImageWithGPS',JSON.stringify(data));
        });

    });
</script>
</@layout.page>
