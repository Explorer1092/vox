<div id="review-detail" title="学校线索审核" style="font-size: small; display: none" clue-id="" updateTime="">
    <br>
    <table width="100%">
        <tr style="text-align: center; font-size: large">
            <td style="width: 60%"></td>
            <th style="width: 20%">审核信息<span id="status-clue" style="font-size: large"></span></th>
            <th>老学校线索</th>
        </tr>
        <tr>
            <td rowspan="12" style="text-align: left;">
                <div>
                    <span id="photo-clue"></span>
                    <div style="clear:both;">
                        照相机制造商：<span id="camera-make" style="color: #005580"></span><#--<br/>-->
                        照相机型号：<span id="camera-model" style="color: #005580"></span><#--<br/>-->
                        拍摄时间：<span id="photo-time" style="color: #005580"><#--<br/>-->
                    </div>
                </div>
                <div id="inner_map" style="width: 600px; height: 400px;">
                </div>
            </td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td><strong>学校ID：</strong><span id="schoolId-clue"></span></td>
            <td><strong>学校ID：</strong><span id="schoolId-school"></span></td>
        </tr>
    <#--<tr>-->
    <#--<td><strong>学校名称：</strong><span id="schoolName-clue"></span></td>-->
    <#--<td><strong>学校名称：</strong><span id="schoolName-school"></span></td>-->
    <#--</tr>-->
        <tr>
            <td><strong>主干名称：</strong><span id="cmainName-clue"></span></td>
            <td><strong>主干名称：</strong><span id="cmainName-school"></span></td>
        </tr>
        <tr>
            <td><strong>校区名称：</strong><span id="schoolDistrict-clue"></span></td>
            <td><strong>校区名称：</strong><span id="schoolDistrict-school"></span></td>
        </tr>
        <tr>
            <td><strong>学校简称：</strong><span id="shortName-clue"></span></td>
            <td><strong>学校简称：</strong><span id="shortName-school"></span></td>
        </tr>
        <tr>
            <td><strong>小学/初中/高中：</strong><span id="schoolPhase-clue"></span></td>
            <td><strong>小学/初中/高中：</strong><span id="schoolPhase-school"></span></td>
        </tr>

        <tr>
            <td><strong>地址：</strong><span id="address-clue"></span></td>
            <td><strong>地址：</strong><span id="address-school"></span></td>
        </tr>
        <tr>
            <td><strong>学制：</strong><span id="schoolingLength-clue"></span></td>
            <td><strong>学制：</strong><span id="schoolingLength-school"></span></td>
        </tr>
        <tr>
            <td><strong>英语起始年级：</strong><span id="englishStartGrade-clue"></span></td>
            <td><strong>英语起始年级：</strong><span id="englishStartGrade-school"></span></td>
        </tr>
        <tr>
            <td><strong>学校规模：</strong><span id="schoolSize-clue"></span></td>
            <td><strong>学校规模：</strong><span id="schoolSize-school"></span></td>
        </tr>
        <tr>
            <td><strong>关联学校：</strong><span id="branchSchoolNames-clue"></span></td>
            <td><strong>关联学校：</strong><span id="branchSchoolNames-school"></span></td>
        </tr>
        <tr>
            <td colspan="2" style="text-align: center">
                <input id="review-pass" type="button" value="通过" onclick="reviewPass()" style="display: none"/>
                <input id="review-reject" type="button" value="驳回" onclick="rejectNote()" style="display: none"/>
                <input id="review-edit" type="button" value="修改" onclick="reviewEdit()" style="display: none"/>
                <input type="button" value="关闭" onclick="closeDialog('review-detail')"/>
            </td>
        </tr>
    </table>
</div>

<div id="reject-note" title="驳回原因" style="font-size: small; display: none">
    <table width="100%">
        <tr>
            <td><textarea id="review-note" style="height: 120px; width: 400px"
                          placeholder="在此写明为什么驳回，如果遇到已有学校重复，写明ID"></textarea></td>
        </tr>
        <tr>
            <td style="text-align: right">
                <input type="button" value="提交" onclick="reviewReject()"/>
                <input type="button" value="取消" onclick="closeDialog('reject-note')"/>
            </td>
        </tr>
    </table>
</div>

<#include "clue_edit.ftl">
<style type="text/css">
    .clue-photo {
        margin: 0px 0px 15px 20px;
        height: 100px;
        width: 100px
    }
</style>
<script type="text/javascript">

    function review(clueId) {
        if (blankString(clueId)) {
            alert("无效的线索ID！");
            return false;
        }
        $.ajax({
            url: "/crm/school_clue/review_detail.vpage",
            type: "POST",
            data: {
                "id": clueId
            },
            success: function (data) {
                if (!data) {
                    alert("操作失败！");
                } else {
                    iReview(data);
                    highLight();
                    $("#review-detail").dialog({
                        height: "auto",
                        width: "1200",
                        modal: true,
                        autoOpen: true
                    });
                }
            }
        });
    }

    function iReview(data) {
        var clue = data.schoolClue;
        var school = data.school;
        var schoolExtInfo = data.schoolExtInfo;
        var photoList = data.photoList;
        var signPointList = data.signPoints;
        var status;
        if (clue.authenticateType == 2) {
            status = clue.infoStatus;
            $("#review-detail").title = "完善信息审核";
        } else {
            status = clue.status;
            $("#review-detail").title = "学校线索审核";
        }
        $("#review-detail").attr("clue-id", clue.id);
        $("#review-detail").attr("updateTime", clue.updateTime);
        var color = -1 === status ? "red" : (2 === status ? "green" : "orange");
        $("#status-clue").text(" (" + schoolStatus(status) + ")").css("color", color);
        // $("#region-clue").text(clue == null ? "" : clue.provinceName + " " + clue.cityName + " " + clue.countyName);
        //$("#region-school").text(region == null ? "" : region.provinceName + " " + region.cityName + " " + region.countyName);
        $("#schoolId-clue").text(clue == null ? "" : clue.schoolId);
        $("#schoolId-school").text(school == null ? "" : school.id);
//        $("#schoolName-clue").text(clue == null ? "" : clue.schoolName);
//        $("#schoolName-school").text(school == null ? "" : school.cname);
        $("#cmainName-clue").text(clue == null ? "" : clue.cmainName == null ? clue.schoolName : clue.cmainName);
        $("#cmainName-school").text(school == null ? "" : school.cmainName);
        $("#schoolDistrict-clue").text(clue == null ? "" : clue.schoolDistrict == null ? "" : clue.schoolDistrict);
        $("#schoolDistrict-school").text(school == null ? "" : school.schoolDistrict);
        $("#shortName-clue").text(clue == null ? "" : clue.shortName);
        $("#shortName-school").text(school == null ? "" : school.shortName);
        $("#schoolPhase-clue").text(clue == null ? "" : schoolPhase(clue.schoolPhase));
        $("#schoolPhase-school").text(school == null ? "" : schoolPhase(school.level));

//        $("#repeatSchool-clue").text(clue == null ? "" : repeatSchool(clue.repeatSchool));

//        $("#schoolLevel-clue").text(clue == null ? "" : schoolLevel(clue.schoolLevel));
//        $("#schoolLevel-school").text(school == null ? "" : schoolLevel(school.vip));
//        $("#schoolType-clue").text(clue == null ? "" : schoolType(clue.schoolType));
//        $("#schoolType-school").text(school == null ? "" : schoolType(school.type));
        $("#address-clue").text(clue == null ? "" : clue.address == null ? "" : clue.address);
        $("#address-school").text(schoolExtInfo == null ? "" : schoolExtInfo.address == null ? "" : schoolExtInfo.address);
        $("#schoolingLength-clue").text(clue == null ? "" : schoolingLength(clue.schoolingLength));
        $("#schoolingLength-school").text(schoolExtInfo == null ? "" : schoolingLength(schoolExtInfo.schoolingLength));
//        $("#externOrBoarder-clue").text(clue == null ? "" : externOrBoarder(clue.externOrBoarder));
//        $("#externOrBoarder-school").text(schoolExtInfo == null ? "" : externOrBoarder(schoolExtInfo.externOrBoarder));
        $("#englishStartGrade-clue").text(clue == null ? "" : englishStartGrade(clue.englishStartGrade));
        $("#englishStartGrade-school").text(schoolExtInfo == null ? "" : englishStartGrade(schoolExtInfo.englishStartGrade));
        $("#schoolSize-clue").text(clue == null ? "" : clue.schoolSize);
        $("#schoolSize-school").text(schoolExtInfo == null ? "" : schoolExtInfo.schoolSize);
        //  $("#gradeDistribution-clue").text(clue==null?"":clue.gradeDistribution==null?"":clue.gradeDistribution);
        //  $("#gradeDistribution-school").text(schoolExtInfo==null?"":schoolExtInfo.gradeDistribution==null?"":schoolExtInfo.gradeDistribution);
        $("#branchSchoolNames-clue").text(clue == null ? "" : branchSchoolName(clue.branchSchoolNames))
        $("#branchSchoolNames-school").text(schoolExtInfo == null ? "" : branchSchoolName(schoolExtInfo.branchSchoolNames))
        var photo = "无";
        if (!blankString(clue.photoUrl)) {
//            photo = "<a href='" + clue.photoUrl + "' target='_blank'><img src='" + clue.photoUrl + "' class='clue-photo' ></a>";
            photo = "<div style='float: left;'><a href='" + clue.photoUrl + "' target='_blank'><img src='" + clue.photoUrl + "' class='clue-photo' ></a></div>";
        }
        var points = [];
        if (photoList) {
            $.each(photoList, function (i) {
                if (photoList[i].longitude && photoList[i].longitude != null) {
                    var point = {
                        icon: 'http://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
                        position: [photoList[i].longitude, photoList[i].latitude],
                        content: "历史坐标，坐标生成时间" + (photoList[i].updateTime) + "。"
                    };
                    points.push(point);
                }
                photo += "<div style='float: left;'><a href='" + photoList[i].photoUrl + "' target='_blank'><img src='" + photoList[i].photoUrl + "' class='clue-photo' ></a><p>"+photoList[i].updateTime+";</p></div>";
            });
        }
        var signPoints = [];
        if (signPointList) {
            $.each(signPointList, function (i) {
                if (signPointList[i].longitude && signPointList[i].longitude != null) {
                    var point = {
                        icon: 'http://webapi.amap.com/theme/v1.3/m2.png',
                        position: [signPointList[i].longitude, signPointList[i].latitude],
                        content: "签到坐标，签到时间" + (signPointList[i].createTime) + "。"
                    };
                    signPoints.push(point);
                }
            });
        }
        $("#photo-clue").empty().append(photo);
        var photoMeta = data.photoMeta;
        $("#camera-make").text(photoMeta == null ? "" : $.trim(photoMeta.Make));
        $("#camera-model").text(photoMeta == null ? "" : $.trim(photoMeta.Model));
        $("#photo-time").text(photoMeta == null ? "" : $.trim(photoMeta["Date/Time"]));
        if (1 === status) {
            $("#review-pass").show();
            $("#review-reject").show();
            $("#review-edit").show();
        } else {
            $("#review-pass").hide();
            $("#review-reject").hide();
            $("#review-edit").hide();
        }
        var longitude = clue.longitude;
        var latitude = clue.latitude;
        renderMap(longitude, latitude, points, signPoints);
    }

    function schoolStatus(status) {
        if (-1 === status) {
            return "已驳回";
        } else if (0 === status) {
            return "暂存";
        } else if (1 === status) {
            return "待审核";
        } else if (2 === status) {
            return "已通过";
        }
        return "无效状态";
    }

    function schoolPhase(phase) {
        if (1 === phase) {
            return "小学";
        } else if (2 === phase) {
            return "中学";
        } else if (4 === phase) {
            return "高中";
        }
        return "";
    }

   /* function schoolLevel(level) {
        if (1 === level) {
            return "重点学校";
        } else if (2 === level) {
            return "非重点学校";
        }
        return "";
    }*/

   /* function schoolType(type) {
        if (1 === type) {
            return "公立制学校";
        } else if (2 === type) {
            return "自定义学校";
        } else if (3 === type) {
            return "私立学校";
        } else if (4 === type) {
            return "一起作业虚拟学校";
        } else if (5 === type) {
            return "培训学校";
        }
        return "";
    }*/

    /*function repeatSchool(rSchool) {
        var repeatSchoolStr = "";
        for (x in rSchool) {
            if (x > 10) {
                break;
            }
            repeatSchoolStr += rSchool[x] + "/";
        }
        return repeatSchoolStr;
    }*/
    function branchSchoolName(bSchool) {
        var branchSchool = "";
        for (x in bSchool) {
            if (x > 10) {
                break;
            }
            branchSchool += bSchool[x] + ",";
        }
        return branchSchool;
    }
    function schoolingLength(length) {
        if (1 === length) {
            return "五年制";
        } else if (2 === length) {
            return "六年制";
        } else if (3 === length) {
            return "三年制";
        } else if (4 === length) {
            return "四年制";
        }
        return "";
    }

    function englishStartGrade(startGrade) {
        if (1 === startGrade) {
            return "一年级";
        } else if (2 === startGrade) {
            return "二年级";
        } else if (3 === startGrade) {
            return "三年级";
        } else if (4 === startGrade) {
            return "四年级";
        } else if (5 === startGrade) {
            return "五年级";
        } else if (6 === startGrade) {
            return "六年级";
        }
        return "";
    }
    /*function externOrBoarder(eOrB) {
        if (1 === eOrB) {
            return "走读";
        } else if (2 === eOrB) {
            return "住宿";
        } else if (3 === eOrB) {
            return "走读/住宿";
        }
        return "";
    }*/

    function highLight() {
        diff("region");
        diff("schoolName");
        diff("shortName");
        diff("schoolPhase");
//        diff("schoolType");
        diff("address");
        diff("schoolingLength");
//        diff("externOrBoarder");
        diff("englishStartGrade");
        diff("gradeDistribution");
        diff("branchSchoolIds");
    }


    function diff(prop) {
        if ($("#" + prop + "-clue").text() != $("#" + prop + "-school").text()) {
            $("#" + prop + "-clue").css("color", "#FF8260");
        } else {
            $("#" + prop + "-clue").css("color", "");
        }
    }

    function reviewPass() {
        reviewClue(2);
    }

    function rejectNote() {
        $("#reject-note").dialog({
            height: "auto",
            width: "450",
            autoOpen: true
        });
    }

    function reviewReject() {
        var note = $("#review-note").val();
        if (blankString(note)) {
            alert("请填写驳回原因！");
            return false;
        }
        reviewClue(-1, note);
    }

    function reviewClue(status, note) {
        var clueId = $("#review-detail").attr("clue-id");
        var updateTime = $("#review-detail").attr("updateTime");

        if (blankString(clueId)) {
            alert("无效的线索ID！");
            return false;
        }
        $.ajax({
            url: "/crm/school_clue/review_clue.vpage",
            type: "POST",
            data: {
                "id": clueId,
                "reviewStatus": status,
                "reviewNote": note,
                "updateTime": updateTime
            },
            success: function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    if (confirm("操作成功，是否刷新页面查看最新记录状态？")) {
                        $("#iform").submit();
                    }
                    $("#review-detail").dialog("close");
                }
            }
        });
    }

    function reviewEdit() {
        closeDialog("review-detail");
        var clueId = $("#review-detail").attr("clue-id");
        editClue(clueId);
    }

    /*function renderMap(longitude, latitude, points, signPoints) {
        var map = new BMap.Map("inner_map");
        var point = new BMap.Point(longitude, latitude);
        var top_left_control = new BMap.ScaleControl({anchor: BMAP_ANCHOR_TOP_LEFT});// 左上角，添加比例尺
        var top_left_navigation = new BMap.NavigationControl();  //左上角，添加默认缩放平移控件

//        map.centerAndZoom(new BMap.Point(116.493341,40.006762),15);

        map.centerAndZoom(point, 15);
        map.enableScrollWheelZoom(true);
        map.addControl(top_left_control);
        map.addControl(top_left_navigation);
        var options = {
            size: BMAP_POINT_SIZE_BIG,
            shape: BMAP_POINT_SHAPE_STAR,
            color: '#d340c3'
        };
        var pointCollection = new BMap.PointCollection(points, options);
        pointCollection.addEventListener('click', function (e) {
            alert('该点的创建时间为：' + e.point.my_info);  // 监听点击事件
        });
        var singOptions = {
            size: BMAP_POINT_SIZE_BIG,
            shape: BMAP_POINT_SHAPE_STAR,
            color: '#7CFC00'
        };
        var signPointCollection = new BMap.PointCollection(signPoints, singOptions);
        map.addOverlay(pointCollection);
        map.addOverlay(signPointCollection);
        var marker = new BMap.Marker(point);
        map.addOverlay(marker);
    }*/
    var infoWindow = new AMap.InfoWindow({offset: new AMap.Pixel(0, -30)});
    function renderMap(longitude, latitude, points, signPoints){
        if (longitude && points) {
            var map = new AMap.Map('inner_map', {
                center: [longitude, latitude],
                zoom: 15
            });
            map.plugin(["AMap.ToolBar"], function() {
                map.addControl(new AMap.ToolBar());
            });
            marker = new AMap.Marker({
                icon: "http://webapi.amap.com/theme/v1.3/markers/n/mark_r.png",
                position: [longitude, latitude]
            });
            marker.setMap(map);
            points.forEach(function makerPoint(marker) {
                var markerInfo = new AMap.Marker({
                    map: map,
                    icon: marker.icon,
                    position: [marker.position[0], marker.position[1]],
                    opacity:0.7
                });
                markerInfo.content = marker.content;
                markerInfo.on('click', markerClick);
            });
            signPoints.forEach(function makerPoint(marker) {
                var markerInfo = new AMap.Marker({
                    map: map,
                    icon: marker.icon,
                    position: [marker.position[0], marker.position[1]],
                    opacity:0.7
                });
                markerInfo.content = marker.content;
                markerInfo.on('click', markerClick);
            });
            function markerClick(e) {
                infoWindow.setContent(e.target.content);
                infoWindow.open(map, e.target.getPosition());
            }
        }

    }
</script>
