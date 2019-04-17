<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="学校线索">
<div id="contaier">
    <#include "../work_record/region_tree.ftl">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
                <a href="javascript:void(0);" class="headerBtn">提交</a>

                <div class="headerText">学校线索</div>
            </div>
        </div>
    </div>
    <form action="save_clue.vpage" method="POST" id="save-school-clue" enctype="multipart/form-data">
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="link link-ico">
                        <div class="side-fl">所属区域</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <input type="text" placeholder="请选择" class="side-fr side-time" readOnly="true" name="regionName" id="regionName" value="${(schoolClue.provinceName)!''}${(schoolClue.cityName)!''}${(schoolClue.countyName)!''}">
                        <input type="hidden" class="side-fr side-time" readOnly="true" name="regionCode" id="regionCode" value="${(schoolClue.countyCode)!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">学校名称</div>
                        <div class="side-fl side-orange">&nbsp;*</div>
                        <input type="text" placeholder="请填写正门照片中的名称" class="side-fr side-time" name="schoolName" id="schoolName" value="${(schoolClue.schoolName)!''}">
                        <input type="hidden" name="schoolId" id="schoolId" value="${(schoolClue.schoolId)!''}">
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
                        <input type="hidden" class="side-fr side-time js-lng" id="lng" name="longitude" value="${(schoolClue.longitude)!''}">
                    </div>
                </li>
            </ul>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">关键人姓名</div>
                        <input type="text" placeholder="请填写姓名" class="side-fr side-time" name="keyContactName" id="keyContactName" value="${(schoolClue.keyContactName)!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">关键人电话</div>
                        <input type="tel" placeholder="请填写电话" class="side-fr side-time" name="keyContactPhone" id="keyContactPhone" value="${(schoolClue.keyContactPhone)!''}">
                    </div>
                </li>
            </ul>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">英语老师</div>
                        <input type="tel" placeholder="请填写人数" class="side-fr side-time" name="englishTeacherCount" value="${(schoolClue.englishTeacherCount)!''}" id="englishTeacherCount">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">数学老师</div>
                        <input type="tel" placeholder="请填写人数" class="side-fr side-time" name="mathTeacherCount" value="${(schoolClue.mathTeacherCount)!''}" id="mathTeacherCount">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">语文老师</div>
                        <input type="tel" placeholder="请填写人数" class="side-fr side-time" name="chineseTeacherCount" value="${(schoolClue.chineseTeacherCount)!''}" id="chineseTeacherCount">
                    </div>
                </li>
            </ul>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">年级&amp;班级</div>
                        <div class="side-fl side-time">（无对应年级可不填）</div>
                    </div>
                </li>
                <li school-phase="JUNIOR">
                    <div class="box">
                        <div class="side-fl side-gray">1年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade1AvgStudentCount" value="${(schoolClue.grade1AvgStudentCount)!''}" id="grade1AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade1ClassCount" value="${(schoolClue.grade1ClassCount)!''}" id="grade1ClassCount"></div>
                    </div>
                </li>
                <li school-phase="JUNIOR">
                    <div class="box">
                        <div class="side-fl side-gray">2年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade2AvgStudentCount" value="${(schoolClue.grade2AvgStudentCount)!''}" id="grade2AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade2ClassCount" value="${(schoolClue.grade2ClassCount)!''}" id="grade2ClassCount"></div>
                    </div>
                </li>
                <li school-phase="JUNIOR">
                    <div class="box">
                        <div class="side-fl side-gray">3年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade3AvgStudentCount" value="${(schoolClue.grade3AvgStudentCount)!''}" id="grade3AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade3ClassCount" value="${(schoolClue.grade3ClassCount)!''}" id="grade3ClassCount"></div>
                    </div>
                </li>
                <li school-phase="JUNIOR">
                    <div class="box">
                        <div class="side-fl side-gray">4年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade4AvgStudentCount" value="${(schoolClue.grade4AvgStudentCount)!''}" id="grade4AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade4ClassCount" value="${(schoolClue.grade4ClassCount)!''}" id="grade4ClassCount"></div>
                    </div>
                </li>
                <li school-phase="JUNIOR">
                    <div class="box">
                        <div class="side-fl side-gray">5年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade5AvgStudentCount" value="${(schoolClue.grade5AvgStudentCount)!''}" id="grade5AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade5ClassCount" value="${(schoolClue.grade5ClassCount)!''}" id="grade5ClassCount"></div>
                    </div>
                </li>
                <li school-phase="JUNIOR">
                    <div class="box">
                        <div class="side-fl side-gray">6年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade6AvgStudentCount" value="${(schoolClue.grade6AvgStudentCount)!''}" id="grade6AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade6ClassCount" value="${(schoolClue.grade6ClassCount)!''}" id="grade6ClassCount"></div>
                    </div>
                </li>
                <li school-phase="MIDDLE">
                    <div class="box">
                        <div class="side-fl side-gray">7年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade7AvgStudentCount" value="${(schoolClue.grade7AvgStudentCount)!''}" id="grade7AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade7ClassCount" value="${(schoolClue.grade7ClassCount)!''}" id="grade7ClassCount"></div>
                    </div>
                </li>
                <li school-phase="MIDDLE">
                    <div class="box">
                        <div class="side-fl side-gray">8年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade8AvgStudentCount" value="${(schoolClue.grade8AvgStudentCount)!''}" id="grade8AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade8ClassCount" value="${(schoolClue.grade8ClassCount)!''}" id="grade8ClassCount"></div>
                    </div>
                </li>
                <li school-phase="MIDDLE">
                    <div class="box">
                        <div class="side-fl side-gray">9年级</div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班均人数" class="side-time" name="grade9AvgStudentCount" value="${(schoolClue.grade9AvgStudentCount)!''}" id="grade9AvgStudentCount"></div>
                        <div class="side-fr side-amount"><input type="tel" placeholder="班级数量" class="side-time" name="grade9ClassCount" value="${(schoolClue.grade9ClassCount)!''}" id="grade9ClassCount"></div>
                    </div>
                </li>
            </ul>
        </div>
        <input type="hidden" id="id" name="id" value="${(schoolClue.id)!''}">
        <input type="hidden" id="status" name="status" value="1">
    </form>
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

        $("#schoolPhase").change(function () {
            renderSchoolGrade($(this).val());
        });
        renderSchoolGrade($("#schoolPhase").val());

        function renderSchoolGrade(schoolPhase) {
            if (schoolPhase != null && schoolPhase == 2) {
                $("li[school-phase='JUNIOR']").hide();
                $("li[school-phase='MIDDLE']").show();
            } else {
                $("li[school-phase='MIDDLE']").hide();
                $("li[school-phase='JUNIOR']").show();
            }
        }

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
                $(".js-lat").val(data.points[0].lat);
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
            return true;
        }
    });
</script>
</@layout.page>