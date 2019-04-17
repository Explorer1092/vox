<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="暑期小记者学校数据审核" page_num=1>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/region.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>

<div class="span11">
    <legend>
        暑期小记者学校数据审核&nbsp;&nbsp;<h5 id="error" style="color: #dd0000">${cityError!''} ${schoolError!''}</h5>
    </legend>

    <form id="iform" action="/crm/summer_reporter/school_list.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="provinceName">
                    所属省
                    <select id="selProvance" name="provinceName" onchange="chgProvinces(selProvance,selCity,selArea)">
                        <option></option>
                    </select>
                </label>
            </li>
            <li>
                <label for="cityName">
                    市/区
                    <select id="selCity" name="cityName" onchange="chgCitys(selCity,selArea)">
                        <option></option>
                    </select>
                </label>
            </li>
            <li>
                <label for="countyName">
                    <select id="selArea" name="countyName" style="display: none">
                        <option></option>
                    </select>
                </label>
            </li>
            <li>
                <label for="schoolName">
                    学校名称
                    <input name="schoolName" id="schoolName" type="text"/>
                </label>
            </li>
            <li>
                <label for="status">
                    审核状态
                    <select id="status" name="status">
                        <option value="待审核">待审核
                        </option>
                        <option value="已通过">已通过
                        </option>
                        <option value="已驳回">已驳回
                        </option>
                    </select>
                </label>
            </li>

        </ul>

        <ul class="inline">
            <li>
                <input type="submit" value="查询"/>
            </li>
            <li>
                <input type="button" value="重置" onclick="formReset()"/>
            </li>
        </ul>
    </form>

    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>更新时间</th>
                <th>所属区域</th>
                <th>学校名称</th>
                <th>审核状态</th>
                <th>审核时间</th>
                <th>操作</th>
            </tr>
            <tbody>
                <#if crmRecSchoolList??>
                    <#list crmRecSchoolList as crmRecSchool>
                    <tr>
                        <td id="ut_${crmRecSchool.id}">${crmRecSchool.updateTime!''}</td>
                        <td id="pn_${crmRecSchool.id}">${crmRecSchool.provinceName!''} ${crmRecSchool.cityName!''} ${crmRecSchool.countyName!''}</td>
                        <td id="cn_${crmRecSchool.id}" hidden>${crmRecSchool.cityName!''}</td>
                        <td id="con_${crmRecSchool.id}" hidden>${crmRecSchool.countyName!''}</td>
                        <td id="sn_${crmRecSchool.id}">${crmRecSchool.schoolName!''}</td>
                        <td id="st_${crmRecSchool.id}">${crmRecSchool.status!''}</td>
                        <td id="ret_${crmRecSchool.id}">${crmRecSchool.updateTime!''}</td>
                        <td id="addr_${crmRecSchool.id}" hidden>${crmRecSchool.addr!''}</td>
                        <td id="bla_${crmRecSchool.id}" hidden>${crmRecSchool.blat!''}</td>
                        <td id="blo_${crmRecSchool.id}" hidden>${crmRecSchool.blon!''}</td>
                        <td id="sid_${crmRecSchool.id}" hidden>${crmRecSchool.schoolId!''}</td>

                        <td>
                            <#if crmRecSchool.status == "待审核">
                                <input id="ObjectId" name="ObjectId" type="hidden" value="${crmRecSchool.id}"/>
                                <button id="add_slot_btn_${crmRecSchool.id}" class="btn btn-info" style="float: right"
                                        value="${crmRecSchool.id}">审核
                                </button>
                            </#if>
                        </td>


                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>


    </div>


</div>

<div id="review_detail"></div>
<div id="edit_dialog" class="modal hide fade" style="width: auto;height: auto">
    <div class="modal-header" style="width: auto;height: auto">
        <div id="map" style="height: 200px; width: 860px;"></div>
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true" id="closeEdit">×</button>
        <h3>审核信息（推荐学校信息）</h3>
    </div>
    <div class="modal-body dl-horizontal" style="width: auto;height: auto">
        <input id="mode" type="hidden" value="add"/>
        <input id="schoolId" name="schoolId" type="hidden" value=""/>
        <dl>
            <dt>学校名称</dt>
            <dd>
                <input id="schoolNameDetail" name="schoolNameDetail" type="text" <#if crmRecSchool??>
                       value="${crmRecSchool.schoolName!''}"</#if>/>
            </dd>
        </dl>
        <dl>
            <dt>所属区域</dt>
            <dd>
                <input type="text" id="provinceNameDetail" name="provinceNameDetail" readonly="readonly"
                       <#if crmRecSchool??>value="${crmRecSchool.provinceName!''}"</#if>/>&nbsp;&nbsp;
                <input type="text" id="cityNameDetail" name="cityNameDetail" readonly="readonly"
                       <#if crmRecSchool??>value="${crmRecSchool.cityName!''} "</#if>/>&nbsp;&nbsp;
                <input type="text" id="countyNameDetail" name="countyNameDetail" readonly="readonly"
                       <#if crmRecSchool??>value="${crmRecSchool.countyName!''}"</#if>/>
            </dd>
        </dl>
        <dl>
            <dt>学校地址</dt>
            <dd>
                <input id="schooladdr" name="schooladdr" type="text" <#if crmRecSchool??>
                       value="${crmRecSchool.schooladdr!''}"</#if>/>
            </dd>
        </dl>
        <dl>
            <dt>学校坐标</dt>
            <dd>
                <input id="schoolblat" name="schoolblat" type="text" <#if crmRecSchool??>
                       value="${crmRecSchool.blat!''}"</#if> readonly="readonly"/>&nbsp;&nbsp;
                <input id="schoolblon" name="schoolblon" type="text" <#if crmRecSchool??>
                       value="${crmRecSchool.blon!''}"</#if> readonly="readonly"/>
            </dd>
        </dl>
        <div class="options" style="width: 800px;height: 600px">
            <table class="table table-bordered" id="resultTab">
                <tr>
                    <th>选择</th>
                    <th>编号</th>
                    <th>学校名称</th>
                    <th>照片</th>
                    <th>学校描述</th>
                </tr>
            </table>
        </div>
        <div class="modal-footer" style="width: auto;height: auto">
            <button class="btn btn-success" id="save_slot_btn">通过</button>
            <button class="btn btn-warning" data-dismiss="modal" aria-hidden="true" id="reject_slot_btn">驳回</button>
        </div>


    </div>
</div>

<div id="descriptionModal" class="modal hide fade">
    <div class="modal-header" style="width: auto;height: auto">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>学校描述</h3>
    </div>
    <div class="modal-body dl-horizontal" style="width: auto;height: auto">
        <textarea readonly id="description"></textarea>
    </div>
</div>

<script type="text/javascript">

    $(function () {

        $("#closeEdit").on('click', function () {
            location.reload();
        });
        $("[data-toggle='tooltip']").tooltip();


        $("button[id^='add_slot_btn_']").on('click', function () {
            var id = $(this).val();
            console.log(id);
            var schoolId = $("#sid_" + id).html().trim();
            var slot = {
                id: id,
                schoolId: schoolId,
                provinceName: $("#pn_" + id).html().trim(),
                cityName: $("#cn_" + id).html().trim(),
                countyName: $("#con_" + id).html().trim(),
                schoolName: $("#sn_" + id).html().trim(),
                addr: $("#addr_" + id).html().trim(),
                blat: $("#bla_" + id).html().trim(),
                blon: $("#blo_" + id).html().trim()
            };
            $('#schoolId').val(slot.schoolId);
            $('#schoolNameDetail').val(slot.schoolName);
            console.log(slot.schoolName);
            $('#provinceNameDetail').val(slot.provinceName);
            $('#cityNameDetail').val(slot.cityName);
            $('#countyNameDetail').val(slot.countyName);
            $('#schooladdr').val(slot.addr);
            $('#schoolblat').val(slot.blat);
            $('#schoolblon').val(slot.blon);
            $("#edit_dialog").modal('show');
            renderMap(slot.blon, slot.blat);

            $.ajax({
                url: "/crm/summer_reporter/review_detail.vpage",
                type: "post",
                data: {
                    "schoolId": schoolId
                },
                success: function (data) {
                    if (!data.success) {
                        alert(data.info);
                    } else {
                        addtoLine(data.crmUgcStudentResultList);
                    }
                }
            })

        });
        function addtoLine(crmUgcStudentResultList) {
            $.each(crmUgcStudentResultList, function (index, item) {

                var description = item['description'];
                $("#resultTab").append($('<tr/>')
                        .append($('<td/>').html("<input name='crmUgcStudentResultId' id='check" + index + "' type='checkbox'value='" + item["id"] + "'/>"))
                        .append($('<td/>').html(index + 1))
                        .append($('<td/>').html(item["schoolName"]))
                        .append($('<td/>').html("<a href='" + item["pictureUrl"] + "' target='_blank'>" + '点击查看' + "<a/>"))
                        .append($('<td/>').html(item['description']))
                );

                markerpoint(item["blon"], item["blat"], index);
            });
        }


        $("#save_slot_btn").on('click', function () {
            var rejectIds = "";
            $("input[name='crmUgcStudentResultId']").not("input:checked").each(function () {
                rejectIds += $(this).val() + ",";
            });
            var passIds = "";
            $("input:checkbox[name=crmUgcStudentResultId]:checked").each(function (i) {
                passIds += $(this).val() + ",";
            });
            var schoolId = $("[name='schoolId']").val();
            var recSchoolId = $("[name='recSchoolId']").val();
            var schooladdr = $("[name='schooladdr']").val();
            var schoolName = $("[name='schoolNameDetail']").val();
            var schoolblat = $("[name='schoolblat']").val();
            var schoolblon = $("[name='schoolblon']").val();
            $.ajax({
                url: "/crm/summer_reporter/doReview.vpage",
                type: "post",
                async: false,
                data: {
                    "schoolId": schoolId,
                    "recSchoolId": recSchoolId,
                    "schoolName": schoolName,
                    "schoolblat": schoolblat,
                    "schoolblon": schoolblon,
                    "schooladdr": schooladdr,
                    "passIds": passIds,
                    "rejectIds": rejectIds,
                },
                success: function (data) {
                    if (!data.success) {
                        alert(data.info);
                    } else {
                        alert(data.info);
                        location.reload();
                    }
                }
            })
        });


        $("#reject_slot_btn").on('click', function () {
            var rejectIds = "";
            $("input[name='crmUgcStudentResultId']").not("input:checked").each(function () {
                rejectIds += $(this).val() + ",";
            });
            var passIds = "";
            $("input:checkbox[name=crmUgcStudentResultId]:checked").each(function (i) {
                passIds += $(this).val() + ",";
            });
            var schoolId = $("[name='schoolId']").val();
            if (passIds != "") {
                alert("有勾选项时不能驳回");
                location.reload();
            } else {
                $.ajax({
                    url: "/crm/summer_reporter/doReject.vpage",
                    type: "post",
                    async: false,
                    data: {
                        "schoolId": schoolId,
                        "rejectIds": rejectIds,
                    },
                    success: function (data) {
                        if (!data.success) {
                            alert(data.info);
                        } else {
                            alert(data.info);
                            location.reload();
                        }
                    }
                })
            }

        });

    });


    function descriptionModalShow() {

        $("#descriptionModal").show();
    }

    function renderMap(longitude, latitude) {
        window.bmap = new BMap.Map("map");// 创建地图实例
        var point = new BMap.Point(longitude, latitude);// 创建点坐标
        bmap.centerAndZoom(point, 15);
        bmap.enableScrollWheelZoom();   //启用滚轮放大缩小，默认禁用
        var myIcon = new BMap.Icon("http://api.map.baidu.com/img/markers.png", new BMap.Size(23, 25), {
            offset: new BMap.Size(10, 25),
            imageOffset: new BMap.Size(0, 0 - 10 * 25) // 设置图片偏移
        });
        var marker = new BMap.Marker(point);// 创建标注
        bmap.addOverlay(marker);
        marker.enableDragging();//学校点可拖拽
        //获取点的坐标
        marker.addEventListener("dragend", function () {
            var p = marker.getPosition();
            $("#schoolblat").val(p.lat);
            $("#schoolblon").val(p.lng);
        });
    }


    function markerpoint(longitude, latitude, index) {
        var point = new BMap.Point(longitude, latitude);  // 创建点坐标
        bmap.centerAndZoom(point, 15);
        var myIcon = new BMap.Icon("http://api.map.baidu.com/img/markers.png", new BMap.Size(23, 25), {
            offset: new BMap.Size(10, 25),
            imageOffset: new BMap.Size(0, 0 - 10 * 25) // 设置图片偏移
        });
        var marker = new BMap.Marker(point);
        marker.addEventListener("click", function () {
            var label = new BMap.Label(index + 1, {offset: new BMap.Size(20, -10)});
            marker.setLabel(label);
        })
        bmap.addOverlay(marker);
    }
</script>
</@layout_default.page>