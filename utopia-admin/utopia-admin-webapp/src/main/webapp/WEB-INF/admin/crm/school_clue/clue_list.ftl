<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="学校线索审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=352b709bf5ce3f0212bc6ced5b641980&plugin=AMap.Geocoder"></script>
<div class="span11">
    <legend>
        学校信息审核&nbsp;&nbsp;
        <a href="/crm/teacher_fake/teacher_fakes.vpage">判假老师审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_appeal/index.vpage">老师申诉审核</a>&nbsp;&nbsp;
        <a href="/crm/teachertransfer/teacherTransferSchool.vpage">转校审核</a>
    </legend>

    <#if schoolAuthOperate || schoolInfoOperate>
        <form id="iform" action="/crm/school_clue/clue_list.vpage" method="post">
            <ul class="inline">
                <li>
                    <label for="schoolName">
                        学校名称
                        <input name="schoolName" id="schoolName" value="${querySchoolName!}" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="auth_status">
                        鉴定状态
                        <select id="auth_status" name="auth_status">
                            <option value="1" <#if authStatus?? && authStatus  == 1> selected="selected" </#if>>未鉴定
                            </option>
                            <option value="2" <#if authStatus?? && authStatus  == 2> selected="selected" </#if>>已鉴定
                            </option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="check_status">
                        审核状态
                        <select id="check_status" name="check_status">
                            <option value="1" <#if checkStatus?? && checkStatus  == 1> selected="selected" </#if>>待审核
                            </option>
                            <option value="2" <#if checkStatus?? && checkStatus  == 2> selected="selected" </#if>>已审核
                            </option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="updateTime">
                        更新日期：
                        <input name="updateStart" id="updateStart" value="${queryUpdateTimeStart!''}" type="text"
                               class="date"/> -
                        <input name="updateEnd" id="updateEnd" value="${queryUpdateTimeEnd!''}" type="text"
                               class="date"/>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button type="button" onclick="submitForm()">查询</button>
                </li>
                <li>
                    <input type="button" value="重置" onclick="formReset()"/>
                </li>
            </ul>
            <input id="PAGE" name="PAGE" type="hidden"/>
        </form>
        <#setting datetime_format="yyyy-MM-dd HH:mm"/>
        <div>
            <table class="table table-bordered">
                <tr>
                    <th>学校创建时间</th>
                    <th>更新时间</th>
                    <th>所属区域</th>
                    <th>主干名称</th>
                    <th>分校名称</th>
                    <th>学校简称</th>
                    <th>学校阶段</th>
                    <th>鉴定状态</th>
                    <th>操作</th>
                </tr>
                <tbody>
                    <#if schoolClues??>
                        <#list schoolClues as clue>
                        <tr>
                            <td>${clue.schoolCreateTime!}</td>
                            <td>${clue.schoolUpdateTime!}</td>
                            <td>${clue.provinceName!''} ${clue.cityName!''} ${clue.countyName!''}</td>
                            <td><a href="/crm/school/schoolhomepage.vpage?schoolId=${clue.id!''}" target="_blank">${clue.cmainName!''}</a></td>
                            <td>${clue.schoolDistrict!''}</td>
                            <td>${clue.shortName!''}</td>
                            <td>${clue.schoolPhase!''}</td>
                            <td>${clue.authenticationState!''}</td>
                            <td><input type="button" value="查看" onclick="review(${clue.id!})"/></td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
            <#include "../pager_foot.ftl">
        </div>
    </#if>
</div>
<#if schoolAuthOperate || schoolInfoOperate>
    <#include "clue_review.ftl">
</#if>

<script>
    var locationInfo = [];
    $(function () {
        dater.render();
    });

    function formReset() {
        $("#auth_status").val("1");
        $("#check_status").val("1");
        $("#schoolName").val("");
        $("#updateStart").val("");
        $("#updateEnd").val("");
    }

    function submitForm() {
        if (validate()) {
            $("#PAGE").val("0");
            $("#iform").submit();
        }
    }

    function validate() {
        var start = $("#updateStart").val();
        var end = $("#updateEnd").val();
        if (start > end) {
            alert("创建的开始时间不能大于结束时间");
            return false;
        }
        return true;
    }

    function review(schoolId) {
        $("#review-detail").dialog({
            height: "auto",
            width: "1200",
            modal: true,
            autoOpen: true
        });
        flushReviewDetail(schoolId);
    }
    function flushReviewDetail(schoolId) {
        var data = {};
        data.schoolId = schoolId;
        $("#review-detail").attr("school_Id", schoolId);
        $.post("/crm/school_clue/review_detail.vpage", data, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                $("#clue-review").html(template("review_detail", {view: data.view, clues: data.view.clues}));
                locationInfo = data.view.clues;
                if (data.view.clues) {
                    photoDetail(data.view.clues[0].latitude, data.view.clues[0].longitude, data.view.clues[0].photoUrl);
                }
            }
        });
    }
    function reviewClue(clueId, status, updateTime) {
        var data = {};
        data.id = clueId;
        data.updateTime = updateTime;
        data.reviewStatus = status;
        data.reviewNote = $("#reviewNote").val();

        if(status == 2) {  // 通过时
            // 人工调整位置
            var manualPosition = $("#manualPosition").val();
            if (!blankString(manualPosition)) {
                if (checkPosition(manualPosition)) {
                    var p = stringToArray(manualPosition, ",");
                    markPosition(p);
                    data.longitude = p[0];
                    data.latitude = p[1];
                    var address = $("#manualAddress").val();
                    if(blankString(address)){
                        alert("请确认位置信息！");
                        return;
                    }
                    data.address = address;
                } else {
                    return;
                }
            }
        }

        $.post("/crm/school_clue/review_clue.vpage", data, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                if (status == -1) {
                    closeDialog('reject-info');
                }
                flushReviewDetail($("#review-detail").attr("school_Id"));
            }
        });
    }
    function sureReject() {
        reviewClue($("#reject-info").data("clueId"),$("#reject-info").data("status"),$("#reject-info").data("update"));
    }

    function rejectClue(clueId, status, updateTime) {
        $("#reviewNote").val("");
        $("#reject-info").dialog({
            height: "auto",
            width: "800",
            modal: true,
            autoOpen: true
        });
        $("#reject-info").data("update", updateTime);
        $("#reject-info").data("clueId", clueId);
        $("#reject-info").data("status", status);
    }

    function closeReject() {
        closeDialog('reject-info');
        $("#reviewNote").val("");
    }

    function photoDetail(latitude, longitude, photoUrl) {
        $("#photo-clue").empty().append("<div style='float: left;'><a href='" + photoUrl + "' target='_blank'><img src='" + photoUrl + "'  ></a></div>");
        $("#manualPosition").val("");
        $("#manualAddress").val("");
        renderMap(latitude, longitude);
    }

    var map;
    function renderMap(latitude, longitude) {
        if (latitude && longitude) {
            if(map){
                manualMarkers.splice(0, manualMarkers.length);
                map = {};
            }

            map = new AMap.Map('inner_map', {
                center: [longitude, latitude],
                zoom: 15
            });


            map.plugin(["AMap.ToolBar"], function() {
                map.addControl(new AMap.ToolBar());
            });

            locationInfo.forEach(function makerPoint(marker) {
                if (marker.longitude && marker.latitude) {
                    var markerInfo = new AMap.Marker({
                        map: map,
                        icon: 'http://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
                        position: [marker.longitude, marker.latitude],
                        opacity:0.7
                    });
                }
                //markerInfo.content = marker.content;
                //markerInfo.on('click', markerClick);
            });

            new AMap.Marker({
                map: map,
                icon: "http://webapi.amap.com/theme/v1.3/markers/n/mark_r.png",
                position: [longitude, latitude],
                title:"当前申请的地点"
            });

        }
    }

    function closeApplyDetail(){
        closeDialog('review-detail');
        window.location.reload();
    }

    var manualMarkers = [];
    function useManualPosition(){
        var position = $("#manualPosition").val();
        if(blankString(position)){
            alert("请输入经纬度");
        }
        if(checkPosition(position)){
            var p = stringToArray(position, ",");
            markPosition(p);
        }
    }

    function checkPosition(position){
        var p = stringToArray(position, ",");
        if(p.length != 2){
            alert("请输入正确的经纬度");
            return false;
        }
        return true;
    }

    function markPosition(p){
        map.remove(manualMarkers);
        manualMarkers.splice(0, manualMarkers.length);
        var marker = new AMap.Marker({
            map: map,
            icon: "http://webapi.amap.com/theme/v1.3/markers/n/mark_r.png",
            position: p,
            title:"手动输入的地点"
        });

        regeocoder(p);
        manualMarkers.push(marker);
    }

    function regeocoder(location) {  //逆地理编码
        var geocoder = new AMap.Geocoder({
            radius: 1000,
            extensions: "all"
        });
        geocoder.getAddress(location, function(status, result) {
            if (status === 'complete' && result.info === 'OK') {
                var address = result.regeocode.formattedAddress;
                $("#manualAddress").val(address);
            }
        });
    }




</script>
</@layout_default.page>
