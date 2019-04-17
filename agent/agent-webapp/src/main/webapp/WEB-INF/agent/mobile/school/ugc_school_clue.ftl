<#import "../layout_new.ftl" as layout>
<#assign groupName="task">
<@layout.page group=groupName title="学校信息">
<div id="contaier">
    <#include "../work_record/region_tree.ftl">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <#if masterSchoolId??>
                    <a href="/mobile/school_clue/ugc_school_clue.vpage?schoolId=${masterSchoolId}&branchSchool=true" class="headerBack">&lt;&nbsp;返回</a>
                <#else>
                    <a href="/mobile/task/user_ugc_school_task.vpage" class="headerBack">&lt;&nbsp;返回</a>
                </#if>
                <a href="javascript:void(0);" class="headerBtn">提交</a>

                <div class="headerText">学校信息</div>
            </div>
        </div>
    </div>
    <form action="save_ugc_school_clue.vpage" method="POST" id="save-school-clue" enctype="multipart/form-data">
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="link link-ico">
                        <div class="side-fl">所属区域</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <input type="text" placeholder="请选择" class="side-fr side-time" readonly="readonly" name="regionName" id="regionName" value="${(schoolClue.provinceName)!''}${(schoolClue.cityName)!''}${(schoolClue.countyName)!''}">
                        <input type="hidden" class="side-fr side-time" readonly="readonly" name="regionCode" id="regionCode" value="${(schoolClue.countyCode)!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">学校名称</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <input type="text" placeholder="请填写正门照片中的名称" class="side-fr side-time" name="schoolName" id="schoolName" value="${(schoolClue.schoolName)!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">学校简称</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <input type="text" placeholder="请填写" class="side-fr side-time" id="shortName" name="shortName" value="${(schoolClue.shortName)!''}">
                    </div>
                </li>
                <li>
                    <div class="link link-ico">
                        <div class="side-fl">学校阶段</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <div class="side-fr side-time" name="schoolPhase"><#if schoolClue?? && schoolClue.schoolPhase == 2>中学<#else>小学</#if></div>
                    </div>
                    <select id="schoolPhase" name="schoolPhase">
                        <option <#if schoolClue?? && schoolClue.schoolPhase == 1>selected</#if> value="1">小学</option>
                        <option <#if schoolClue?? && schoolClue.schoolPhase == 2>selected</#if> value="2">中学</option>
                    </select>
                </li>
                <li>
                    <div class="link link-ico">
                        <div class="side-fl">学校等级</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <div class="side-fr side-time" name="schoolLevel"><#if schoolClue?? && schoolClue.schoolPhase == 1>重点<#else>非重点</#if></div>
                    </div>
                    <select id="schoolLevel" name="schoolLevel">
                        <option <#if schoolClue?? && schoolClue.schoolLevel == 2>selected</#if> value="2">非重点</option>
                        <option <#if schoolClue?? && schoolClue.schoolLevel == 1>selected</#if> value="1">重点</option>
                    </select>
                </li>
                <li>
                    <div class="link link-ico">
                        <div class="side-fl">学校类型</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <div class="side-fr side-time" name="schoolType"><#if schoolClue?? && schoolClue.schoolLevel == 3>私立学校<#else>公立学校</#if></div>
                    </div>
                    <select id="schoolType" name="schoolType">
                        <option <#if schoolClue?? && schoolClue.schoolLevel == 1>selected</#if> value="1">公立学校</option>
                        <option <#if schoolClue?? && schoolClue.schoolLevel == 3>selected</#if> value="3">私立学校</option>
                    </select>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">年级分布</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <input type="text" placeholder="如: 1,2,3" class="side-fr side-time" id="gradeDistribution" name="gradeDistribution" value="${(schoolClue.gradeDistribution)!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl" style="margin:2.1335rem 0;">正门照片</div>
                        <div class="side-fl side-orange" style="margin:2.1335rem 0;">&nbsp;*</div>
                        <div class="side-fr photoShow" id="photoShow"><img src="${(schoolClue.photoUrl)!''}"></div>
                        <input type="hidden" id="photoUrl" name="photoUrl" value="${(schoolClue.photoUrl)!''}">
                        <input type="file" id="photo" name="photo" value="" placeholder="添加图片" class="photoClick">
                    </div>
                </li>
            </ul>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">学校位置</div>
                        <div class="side-fr side-time">
                            <button id="loc-btn" class="side-fr" style="text-align:center;width:68px;cursor:pointer;border:none;line-height:22px;color:#fff;border-radius:4px;background-color: #39AC6A;padding:4px 4px;">点击获取</button>
                        </div>
                    </div>
                </li>
                <li>
                    <div class="box">
                        <input type="text" placeholder="点击获取当前位置" class="side-fr side-time" name="address" id="address" value="${(schoolClue.address)!''}" readonly="readonly" style="width: 100%;">
                        <input type="hidden" class="side-fr side-time js-lat" id="lat" name="latitude" value="${(schoolClue.latitude)!''}">
                        <input type="hidden" class="side-fr side-time js-lng" id="lng" name="longitude" value="${(schoolClue.latitude)!''}">
                    </div>
                </li>
            </ul>
        </div>
        <input type="hidden" id="id" name="id" value="${(schoolClue.id)!''}">
        <input type="hidden" id="status" name="status" value="1">
        <input type="hidden" id="schoolId" name="schoolId" value="${(schoolClue.schoolId)!''}">
        <input type="hidden" id="masterSchoolId" name="masterSchoolId" value="${masterSchoolId!''}">
        <input type="hidden" id="taskId" name="taskId" value="${taskId!''}">
    </form>
    <#if branchSchool?? && branchSchool>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <#if branchSchoolClues?has_content>
                    <#list branchSchoolClues as branch>
                        <li>
                            <div class="link link-ico">
                                <div class="side-fl">${branch.schoolName!}</div>
                                <input type="text" placeholder="请填写" class="side-fr side-time branch" readonly="readonly" clue-id="${branch.id!}">
                            </div>
                        </li>
                    </#list>
                </#if>
                <li>
                    <div class="link">
                        <div class="side-fl">
                            <a href="/mobile/school_clue/ugc_branch_school_clue.vpage?masterSchoolId=${(schoolClue.schoolId)!}" style="color: #0099FF">+添加分校</a>
                        </div>
                    </div>
                </li>
            </ul>
        </div>
    </#if>
</div>
<div id="areaTree"></div>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>
<script type="text/javascript">
    $(function () {
        // 绑定选择事件
        var selects = ['schoolPhase', 'schoolType', 'schoolLevel'];
        for (var i = 0; i < selects.length; i++) {
            addChange($("#" + selects[i]));
        }

        function addChange(elem) {
            elem.change(function () {
                var name = elem.attr("name");
                $("div[name='" + name + "']").html($("#" + name + " option:selected").html());
            });
        }

        function validate() {
            if (blankString($("#regionCode").val())) {
                alert("请选择所属区域");
                return false;
            }
            if (blankString($("#schoolName").val())) {
                alert("请输入学校名称");
                return false;
            }
            if (blankString($("#shortName").val())) {
                alert("请输入学校简称");
                return false;
            }
            if (blankString($("#schoolPhase option:selected").val())) {
                alert("请选择学校阶段");
                return false;
            }
            if (blankString($("#schoolLevel option:selected").val())) {
                alert("请选择学校等级");
                return false;
            }
            if (blankString($("#schoolType option:selected").val())) {
                alert("请选择学校类型");
                return false;
            }
            if (blankString($("#gradeDistribution").val())) {
                alert("请输入年级分布");
                return false;
            }
            if (blankString($("#photoUrl").val()) && blankString($("#photo").val())) {
                alert("请上传学校正门照片");
                return false;
            }
            /*if (blankString($(".js-lat").val()) && blankString($(".js-lng").val())) {
                alert("请点击学校位置按钮获取位置信息");
                return false;
            }
            if (blankString($("#address").val())) {
                alert("请点击学校位置按钮获取学校位置");
                return false;
            }*/
            if (!hasBranchSchool()) {
                alert("请添加分校");
                return false;
            }
            return true;
        }

        function hasBranchSchool() {
            var branch = ${(branchSchool?? && branchSchool)?string("true", "false")};
            var branches = ${(branchSchoolClues?has_content)?string("true", "false")};
            return !branch || branches;
        }

        $(".headerBtn").click(function () {
            if (validate()) {
                $("#save-school-clue").submit();
            }
        });
        $("#photoShow").click(function () {
            $("#photo").click();
        });
        $("#photo").change(function () {
            var reader = new FileReader();
            reader.onload = function (e) {
                $("#photoUrl").val("");
                $("#photoShow").empty().append("<img src='" + this.result + "'>");
            };
            reader.readAsDataURL(document.getElementById("photo").files[0]);
        });
        $("#regionName").click(function () {
            $("#contaier").hide();
            $("#areaTree").html("");
            $("#areaTree").show();
            $.ajax({
                url: "/mobile/common/user_regions.vpage",
                type: "POST",
                success: function (data) {
                    var content = template("regionTreeTest", {regionTree: data});
                    $("#areaTree").html(content);
                }
            })
        });
        window.regionTreeOK = function (name, code) {
            $("#regionName").attr("value", name);
            $("#regionCode").attr("value", code);
            $("#contaier").show();
            $("#areaTree").hide();
        };
        window.regionTreeReturn = function () {
            $("#contaier").show();
            $("#areaTree").hide();
        };

        function locationSuccess(position) {
            var coords = position.coords;
            var x = coords.longitude;
            var y = coords.latitude;
            var ggPoint = new BMap.Point(x, y);
            var convertor = new BMap.Convertor();
            var pointArr = [];
            pointArr.push(ggPoint);
            convertor.translate(pointArr, 1, 5, translateCallback);
            $("#loc-btn").attr("disabled", false).text("点击获取");
        }

        //gps转baidu
        function translateCallback(data) {
            if (data.status === 0) {
                $(".js-lng").val(data.points[0].lng);
                $(".js-lat").val(data.points[0].lng);
                var geoc = new BMap.Geocoder();
                var pt = new BMap.Point(data.points[0].lng, data.points[0].lat);
                geoc.getLocation(pt, function (rs) {
                    var addComp = rs.addressComponents;
                    var gpsAddress = addComp.province + addComp.city + addComp.district + addComp.street + addComp.streetNumber;
                    $("#address").val(gpsAddress);
                });
            } else {
                alert("gps转百度坐标发生错误");
            }
        }

        function locationError(error) {
            switch (error.code) {
                case error.TIMEOUT:
                    alert("获取位置出错: 获取地理位置超时，请稍后重试");
                    break;
                case error.POSITION_UNAVAILABLE:
                    alert("获取位置出错: 当前无法跟踪位置");
                    break;
                case error.PERMISSION_DENIED:
                    alert("获取位置出错: 请允许应用获取地理位置信息");
                    break;
                case error.UNKNOWN_ERROR:
                    alert("获取位置出错: 发生未知错误");
                    break;
            }
            $("#loc-btn").attr("disabled", false).text("点击获取");
        }

        $("#loc-btn").on("click", function () {
            if (navigator.geolocation) {
                $(this).attr("disabled", true).text("获取中...");
                navigator.geolocation.getCurrentPosition(locationSuccess, locationError, {
                    // 指示浏览器获取高精度的位置，默认为false
                    enableHighAccuracy: true,
                    // 指定获取地理位置的超时时间，默认不限时，单位为毫秒
                    timeout: 5000,
                    // 最长有效期，在重复获取地理位置时，此参数指定多久再次获取位置。
                    maximumAge: 1000
                });
            } else {
                alert("您的游览器不支持Html5 Geolocation,无法获取位置信息");
            }
        });

        var masterSchoolId = $("#schoolId").val();
        $(".branch").on("click", function () {
            var id = $(this).attr("clue-id");
            window.location.href = "/mobile/school_clue/ugc_branch_school_clue.vpage?masterSchoolId=" + masterSchoolId + "&id=" + id;
        });
    });
</script>
</@layout.page>