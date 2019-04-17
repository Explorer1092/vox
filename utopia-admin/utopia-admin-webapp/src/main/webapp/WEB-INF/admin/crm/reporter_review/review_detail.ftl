
<style>
    dd,dl,dt,form,h1,h2,h3,h4,h5,h6,li,ol,p,select,ul{margin:0;padding:0}
    html{font:14px/1.5 "宋体",Arial}
    /*body,html{width:100%;height:100%;color:#333}*/
    li,ol,ul{list-style-type:none}
    em,i{font-style:normal}
    a,a:visited{color:#333}
    a{text-decoration:none}
    a img{border:0 none}

    .main .options .teachers{height:180px;}
    .main{width:960px;margin:0 auto;background:#ccc;}
    .main>div{float:left;box-sizing: border-box;border-right:1px solid black;}
    .main .school{width:25%;}
    .main .school img{width:100%;}
    .main .map{width:25%;padding:12px;}
    .main .map h3{margin:15px 0;font-size:18px;}
    .main .map ul li:nth-child(n+6){color:#e33;}
    .main .map ul li{line-height:2em;}
    .main .options{width:50%;padding:24px;}
    .main .options .teachers input{display:inline-block;margin:5px 50px;width:180px;text-align:center;padding:1px;}
    .main .options table{line-height:1.5em;margin-top:10px;font-size:14px;}
    .main .options table label{display: inline;}
    .main .options table label,.main .options table input[type="checkbox"]{cursor:pointer;}
    .main .options table th{color:#e33;text-align:left;width:100px;padding-right:22px;font-weight:400;}
    .main .options table td{text-align:center;}
    .main .options table th+td{width:50px;}
    .main .options table select,.main .options .input{width:80%;}
    .main .submit{padding:20px 0;display:flex;line-height:1.4em;justify-content: space-between;}
    .main .submit>input{width:23%;}

    i.clr{clear:both;display:block;}
    .disabled{pointer-events:none;opacity:0.4;}
</style>

<div class="main">
    <input id="schoolId" name="schoolId" type="hidden" <#if crmRecSchool??>value="${crmRecSchool.id!}"</#if>/>
    <div class="school">
        <h3>审核信息（推荐学校信息）</h3>
        <ul>
        <#if crmRecSchool??>
            <li>学校名称：<input id="schoolName" name="schoolName" type="text" <#if crmRecSchool??> value="${crmRecSchool.schoolName!''}"</#if>/></li>
            <li>所属区域：${crmRecSchool.provinceName!''} ${crmRecSchool.cityName!''} ${crmRecSchool.countyName!''}</li>
            <li>学校地址：<input id="schooladdr" name="schooladdr" type="text" <#if crmRecSchool??> value="${crmRecSchool.schooladdr!''}"</#if>/></li>
            <li>学校坐标：
                <input id="schoolblat" name="schoolblat" type="text" <#if crmRecSchool??> value="${crmRecSchool.schoolblat!''}"</#if> readonly="readonly"/>
                <input id="schoolblon" name="schoolblon" type="text" <#if crmRecSchool??> value="${crmRecSchool.schoolblon!''}"</#if> readonly="readonly"/>
            </li>
        </#if>
        </ul>
    </div>
    <div class="map">
    <#--<div>-->
    <#--<#if schoolExtInfo?? && schoolExtInfo.photoUrl??><img src="${schoolExtInfo.photoUrl!}" style='height: 200px; width: 200px'/>-->
    <#--<#else>-->
    <#--<div style="height: 200px; width: 200px"></div>-->
    <#--</#if>-->
    <#--<div>-->
    <#--照相机制造商：<#if photoMeta??>${photoMeta.Make!}</#if><br />-->
    <#--照相机型号：<#if photoMeta??>${photoMeta.Model!}</#if><br />-->
    <#--拍摄时间：<#if photoMeta??>${photoMeta["Date/Time"]!}</#if><br />-->
    <#--</div>-->
    <#--</div>-->
        <div id="innerMap" style="height: 200px; width: 200px;">
        </div>
    </div>
    <div class="options">
        <table class="table table-striped table-bordered">
            <tr>
                <th>选择</th>
                <th>编号</th>
                <th>学校名称</th>
                <th>照片</th>
                <th>学校描述</th>
            </tr>
            <tbody>
            <#if crmUgcStudentResultList??>
                <#list crmUgcStudentResultList as crmUgcStudentResult>
                    <#assign oldSchool = crmUgcStudentResult.id??/>
                <tr>
                    <td> <input type="checkbox" name="crmUgcStudentResultId" value="${crmUgcStudentResult.id}" /></td>
                    <td></td>
                    <td>${crmUgcStudentResult.schoolName}</td>
                    <td><a href="${crmUgcStudentResult.pictureUrl}">点击查看</a></td>
                    <td><a href="${crmUgcStudentResult.description}">点击查看</a></td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
        </div>
        <div class="submit">

            <input type="submit" value="通过" onclick="doReview();"/>
            <input type="button" value="驳回" onclick="notDail();"/>
            <input type="reset" value="关闭" onclick="cancalReview();" />
        </div>
    </div>
    <i class="clr"></i>
</div>

<script>
    $(document).on('click','.all',function(){
        if($(this).children()[0].checked){
            $(this).parent().siblings("td").addClass("disabled");
        }else{
            $(this).parent().siblings("td").removeClass("disabled");
        }
    });
    $(document).on('click','.special',function(){
        if($(this).children()[0].checked){
            $(".special-bind").addClass("disabled");
        }else{
            $(".special-bind").removeClass("disabled");
        }
    });

    $(document).ready(function(){
        <#if crmRecSchool?? && crmRecSchool.blat?? && crmRecSchool.blon??>
            var longitude = ${crmRecSchool.blon};
            var latitude = ${crmRecSchool.blat};
            if(longitude != 0 && latitude != 0){
                renderMap(longitude, latitude);
            }
        </#if>
    });

//    function getCheckBoxValues(checkBoxName){
//        var chk_value ="";
//        $("input[name='"+ checkBoxName +"']:checked").each(function(){
//            chk_value += $(this).val() +",";
//        });
//        if(chk_value.length > 0){
//            chk_value = chk_value.substring(0, chk_value.length -1);
//        }
//        return chk_value
//    }

    function doReview(){

        var rejectIds = $("input[name='crmUgcStudentResultId']").not("input:checked");
        var passIds = $("input[name='crmUgcStudentResultId']").is("input:checked");
        var schoolId=$("[name='schoolId']").val();
        var schooladdr=$("[name='schooladdr']").val();
        var schoolName=$("[name='schoolName']").val();
        var schoolblat=$("[name='schoolblat']").val();
        var schoolblon=$("[name='schoolblon']").val();
        $.ajax({
            url: "/crm/summer_reporter/doReview.vpage",
            type: "POST",
            data: {
                "schoolId":schoolId,
                "schoolName":schoolName,
                "schoolblat":schoolblat,
                "schoolblon":schoolblon,
                "schooladdr":schooladdr,
                "passIds": passIds,
                "rejectIds": rejectIds,
            },
            success: function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    closeReviewDialog();
                    location.reload();
                }
            }
        });
    }

    function doReject(){
        var schoolId=$("[name='schoolId']").val();
        var rejectIds = $("input[name='crmUgcStudentResultId']").not("input:checked");
        $.ajax({
            url: "/crm/summer_reporter/doReject.vpage",
            type: "POST",
            data: {
                "schoolId": schoolId,
                "rejectIds":rejectIds,
            },
            success: function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    closeReviewDialog();
                    location.reload();
                }
            }
        });
    }

    function cancalReview(){
                    closeReviewDialog();
                    location.reload();
    }

    function renderMap(longitude, latitude){
//        var map = new BMap.Map("innerMap");
//        var point = new BMap.Point(longitude, latitude);
//        map.centerAndZoom(point, 15);
//        var marker = new BMap.Marker(point);
//        map.addOverlay(marker);
        var map = new BMap.Map("map");          // 创建地图实例
        var point = new BMap.Point(longitude, latitude);  // 创建点坐标
        map.centerAndZoom(point, 15);
        map.enableScrollWheelZoom();   //启用滚轮放大缩小，默认禁用
        map.enableContinuousZoom();   //启用地图惯性拖拽，默认禁用
        var myIcon = new BMap.Icon("http://api.map.baidu.com/img/markers.png", new BMap.Size(23, 25), {
            offset: new BMap.Size(10, 25), // 指定定位位置
            imageOffset: new BMap.Size(0, 0 - 10 * 25) // 设置图片偏移
        });
        var marker = new BMap.Marker(point,{icon:myIcon});// 创建标注
        map.addOverlay(marker);
        marker.enableDragging();//学校点可拖拽
        //获取点的坐标
        marker.addEventListener("dragend",function(){
            var p=marker.getPosition();
            $("#schoolblat").val(p.lat);
            $("#schoolblon").val(p.lng);
        });
    }
    function markerpoint(longitude, latitude){
        var map = new BMap.Map("map");          // 创建地图实例
        var point = new BMap.Point(longitude, latitude);  // 创建点坐标
        var marker = new BMap.Marker(point);
        map.addOverlay(marker);
        marker.enableDragging();
    }

</script>


