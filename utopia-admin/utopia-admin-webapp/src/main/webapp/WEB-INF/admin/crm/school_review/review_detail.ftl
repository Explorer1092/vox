
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
    .main .pictures{width:25%;}
    .main .pictures img{width:100%;}
    .main .school{width:25%;padding:12px;}
    .main .school h3{margin:15px 0;font-size:18px;}
    .main .school ul li:nth-child(n+6){color:#e33;}
    .main .school ul li{line-height:2em;}
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
    <input id="schoolId" name="schoolId" type="hidden" <#if school??>value="${school.id!}"</#if>/>
    <input id="schoolLevel" name="schoolLevel" type="hidden" <#if school??>value="${school.level!}"</#if>/>
    <div class="pictures">
        <div>
            <#if schoolExtInfo?? && schoolExtInfo.photoUrl??><img src="${schoolExtInfo.photoUrl!}" style='height: 200px; width: 200px'/>
            <#else>
                <div style="height: 200px; width: 200px"></div>
            </#if>
            <div>
                照相机制造商：<#if photoMeta??>${photoMeta.Make!}</#if><br />
                照相机型号：<#if photoMeta??>${photoMeta.Model!}</#if><br />
                拍摄时间：<#if photoMeta??>${photoMeta["Date/Time"]!}</#if><br />
            </div>
        </div>
        <div id="innerMap" style="height: 200px; width: 200px;">
        </div>
    </div>
    <div class="school">
        <h3>学校线索（审核通过）</h3>
        <ul>
            <#if school??>
                <li>学校ID：${school.id!}</li>
                <li>学校名称：${school.cname!}</li>
                <li>学校简称：${school.shortName!}</li>
                <li>小学/中学：<#if school.level == 1>小学<#else>中学</#if></li>
                <li>学校性质：
                <#if school.type??>
                    <#if school.type == 1>公立制学校
                    <#elseif school.type == 2>自定义学校
                    <#elseif school.type == 3>私立学校
                    <#elseif school.type == 4>一起作业虚拟学校
                    <#elseif school.type == 5>培训学校
                    </#if>
                </#if>
                </li>
            </#if>
            <#if schoolExtInfo??>
                <li>年级分布：${schoolExtInfo.gradeDistribution!}</li>
                <li>学制：
                    <#if schoolExtInfo.schoolingLength??>
                        <#if schoolExtInfo.schoolingLength == 1>五年制
                        <#elseif schoolExtInfo.schoolingLength == 2>六年制
                        <#elseif schoolExtInfo.schoolingLength == 3>三年制
                        <#elseif schoolExtInfo.schoolingLength == 4>四年制
                        </#if>
                    </#if>
                </li>
                <#if school?? && school.level == 1>
                <li>英语起始年级：
                    <#if schoolExtInfo.englishStartGrade??>
                        <#if schoolExtInfo.englishStartGrade == 1>一年级
                        <#elseif schoolExtInfo.englishStartGrade == 2>二年级
                        <#elseif schoolExtInfo.englishStartGrade == 3>三年级
                        <#elseif schoolExtInfo.englishStartGrade == 4>四年级
                        <#elseif schoolExtInfo.englishStartGrade == 5>五年级
                        <#elseif schoolExtInfo.englishStartGrade == 6>六年级
                        </#if>
                    </#if>
                </li>
                </#if>
                <li>走住：
                    <#if schoolExtInfo.externOrBoarder??>
                        <#if schoolExtInfo.externOrBoarder == 1>走读
                        <#elseif schoolExtInfo.externOrBoarder == 2>住宿
                        <#elseif schoolExtInfo.externOrBoarder == 3>走读/住宿
                        </#if>
                    </#if>
                </li>
                <li>学校学生数量：${schoolExtInfo.schoolSize!0}</li>
                <li>关联分校：
                    <#if schoolExtInfo.branchSchoolIds??>
                        <#list schoolExtInfo.branchSchoolIds as subSchoolId>
                            ${subSchoolId},
                        </#list>
                    </#if>
                </li>
            </#if>
        </ul>
    </div>
    <div class="options">
        <ul class="teachers">
            <#if teacherList??>
                <#list teacherList as teacher>
                    <li>${teacher.realName!}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${teacher.sensitiveMobile!}</li>
                </#list>
            <#else>
                <li></li>
                <li></li>
                <li></li>
                <li></li>
                <li></li>
            </#if>
        </ul>
        <table>
            <tr>
                <th>年级分布：</th>
                <td><label class="all"><input type="checkbox" name="gradeDistributionFlag" value="true"/>一致</label></td>
                <td>
                    <label><input type="checkbox" name="gradeDistribution1" value="1" />1</label>
                    <label><input type="checkbox" name="gradeDistribution1" value="2" />2</label>
                    <label><input type="checkbox" name="gradeDistribution1" value="3" />3</label><br />
                    <label><input type="checkbox" name="gradeDistribution1" value="4" />4</label>
                    <label><input type="checkbox" name="gradeDistribution1" value="5" />5</label>
                    <label><input type="checkbox" name="gradeDistribution1" value="6" />6</label><br />
                    <label><input type="checkbox" name="gradeDistribution1" value="7" />7</label>
                    <label><input type="checkbox" name="gradeDistribution1" value="8" />8</label>
                    <label><input type="checkbox" name="gradeDistribution1" value="9" />9</label>
                </td>
                <td>
                    <label><input type="checkbox" name="gradeDistribution2" value="1" />1</label>
                    <label><input type="checkbox" name="gradeDistribution2" value="2" />2</label>
                    <label><input type="checkbox" name="gradeDistribution2" value="3" />3</label><br />
                    <label><input type="checkbox" name="gradeDistribution2" value="4" />4</label>
                    <label><input type="checkbox" name="gradeDistribution2" value="5" />5</label>
                    <label><input type="checkbox" name="gradeDistribution2" value="6" />6</label><br />
                    <label><input type="checkbox" name="gradeDistribution2" value="7" />7</label>
                    <label><input type="checkbox" name="gradeDistribution2" value="8" />8</label>
                    <label><input type="checkbox" name="gradeDistribution2" value="9" />9</label>
                </td>
            </tr>
            <tr>
                <th>学制：</th>
                <td><label class="all"><input type="checkbox" name="schoolingLengthFlag" value="true" />一致</label></td>
                <td>
                    <select name="schoolingLength1" >
                        <option value="">请选择</option>
                        <option value="1" >五年制</option>
                        <option value="2" >六年制</option>
                        <option value="3" >三年制</option>
                        <option value="4" >四年制</option>
                    </select>
                </td>
                <td>
                    <select name="schoolingLength2" >
                        <option value="">请选择</option>
                        <option value="1" >五年制</option>
                        <option value="2" >六年制</option>
                        <option value="3" >三年制</option>
                        <option value="4" >四年制</option>
                    </select>
                </td>
            </tr>
            <#if school?? && school.level == 1>
            <tr>
                <th>英语起始年级：</th>
                <td><label class="all"><input type="checkbox" name="englishStartGradeFlag" value="true" />一致</label></td>
                <td>
                    <select name="englishStartGrade1" >
                        <option value="">请选择</option>
                        <option value="1">一年级</option>
                        <option value="2">二年级</option>
                        <option value="3">三年级</option>
                        <option value="4">四年级</option>
                        <option value="5">五年级</option>
                        <option value="6">六年级</option>
                    </select>
                </td>
                <td>
                    <select name="englishStartGrade2" >
                        <option value="">请选择</option>
                        <option value="1">一年级</option>
                        <option value="2">二年级</option>
                        <option value="3">三年级</option>
                        <option value="4">四年级</option>
                        <option value="5">五年级</option>
                        <option value="6">六年级</option>
                    </select>
                </td>
            </tr>
            </#if>
            <tr>
                <th>走住：</th>
                <td><label class="all"><input type="checkbox" name="externOrBoarderFlag" value="true"/>一致</label></td>
                <td>
                    <select name="externOrBoarder1">
                        <option value="">请选择</option>
                        <option value="1">走读</option>
                        <option value="2">住宿</option>
                        <option value="3">走读/住宿</option>
                    </select>
                </td>
                <td>
                    <select name="externOrBoarder2">
                        <option value="">请选择</option>
                        <option value="1">走读</option>
                        <option value="2">住宿</option>
                        <option value="3">走读/住宿</option>
                    </select>
                </td>
            </tr>
            <tr>
                <th>学校学生数量：</th>
                <td><label class="special"><input type="checkbox" name="schoolSizeFlag" value="true"/>一致</label></td>
                <td><input class="special-bind input"  placeholder="填写" name="schoolSize1"/></td>
                <td><input class="special-bind input"  placeholder="填写" name="schoolSize2"/></td>
            </tr>
            <tr>
                <th>本年级班级数：</th>
                <td></td>
                <td><input class="special-bind input"  placeholder="填写" name="gradeClassCount1"/></td>
                <td><input class="special-bind input"  placeholder="填写" name="gradeClassCount2"/></td>
            </tr>
            <tr>
                <th>本班人数：</th>
                <td></td>
                <td><input class="special-bind input"  placeholder="填写" name="classStudentCount1"/></td>
                <td><input class="special-bind input"  placeholder="填写" name="classStudentCount2"/></td>
            </tr>
            <tr>
                <th>关联分校：</th>
                <td><label class="all"><input type="checkbox" name="branchSchoolIdsFlag" value="true"/>一致</label></td>
                <td><input class="input"  placeholder="填写" name="branchSchoolIds1"/></td>
                <td><input class="input"  placeholder="填写" name="branchSchoolIds2"/></td>
            </tr>
        </table>
        <div class="submit">
            <#if schoolExtInfo?? && canReview?? && canReview>
            <input type="submit" value="提交" onclick="doReview();"/>
            <input type="button" value="无人应答" onclick="notDail();"/>
            </#if>
            <input type="reset" value="取消" onclick="cancalReview();" />
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
        <#if schoolExtInfo?? && schoolExtInfo.latitude?? && schoolExtInfo.longitude??>
            var longitude = ${schoolExtInfo.longitude};
            var latitude = ${schoolExtInfo.latitude};
            if(longitude != 0 && latitude != 0){
                renderMap(longitude, latitude);
            }
        </#if>
    });

    function getCheckBoxValues(checkBoxName){
        var chk_value ="";
        $("input[name='"+ checkBoxName +"']:checked").each(function(){
            chk_value += $(this).val() +",";
        });
        if(chk_value.length > 0){
            chk_value = chk_value.substring(0, chk_value.length -1);
        }
        return chk_value
    }


    function checkReview(){
        var gradeDistributionFlag = $("[name='gradeDistributionFlag']").is(':checked');
        if(!gradeDistributionFlag){
            var gradeDistribution1 = getCheckBoxValues("gradeDistribution1");
            if(gradeDistribution1 == ""){
                alert("年级分布与学校线索不一致，请填写");
                return false;
            }
        }

        var schoolingLengthFlag = $("[name='schoolingLengthFlag']").is(':checked');
        if(!schoolingLengthFlag){
            var schoolingLength1 = $("[name='schoolingLength1']").val();
            if(schoolingLength1 == ""){
                alert("学制与学校线索不一致，请填写");
                return false;
            }
        }

        var schoolLevel = $("#schoolLevel").val();
        if(schoolLevel == "1"){
            var englishStartGradeFlag = $("[name='englishStartGradeFlag']").is(':checked');
            if(!englishStartGradeFlag){
                var englishStartGrade1 = $("[name='englishStartGrade1']").val();
                if(englishStartGrade1 == ""){
                    alert("英语起始年级与学校线索不一致，请填写");
                    return false;
                }
            }
        }

        var externOrBoarderFlag = $("[name='externOrBoarderFlag']").is(':checked');
        if(!externOrBoarderFlag){
            var externOrBoarder1 = $("[name='externOrBoarder1']").val();
            if(externOrBoarder1 == ""){
                alert("走住与学校线索不一致，请填写");
                return false;
            }
        }

        var schoolSizeFlag = $("[name='schoolSizeFlag']").is(':checked');
        if(!schoolSizeFlag){
            var schoolSize1 = $("[name='schoolSize1']").val();
            if(schoolSize1 == ""){
                alert("学校学生数量与学校线索不一致，请填写");
                return false;
            }
        }
        return true;
    }

    function doReview(){
        var checkResult = checkReview();
        if(checkResult){
            var schoolLevel = $("#schoolLevel").val();

            var schoolId = $("#schoolId").val();
            var gradeDistributionFlag = $("[name='gradeDistributionFlag']").is(':checked');
            var schoolingLengthFlag = $("[name='schoolingLengthFlag']").is(':checked');

            var englishStartGradeFlag;
            if(schoolLevel == "1") {
                englishStartGradeFlag = $("[name='englishStartGradeFlag']").is(':checked');
            }else{
                englishStartGradeFlag = true;
            }

            var externOrBoarderFlag = $("[name='externOrBoarderFlag']").is(':checked');
            var schoolSizeFlag = $("[name='schoolSizeFlag']").is(':checked');
            var branchSchoolIdsFlag = $("[name='branchSchoolIdsFlag']").is(':checked');

            var gradeDistribution1 = getCheckBoxValues("gradeDistribution1");
            var gradeDistribution2 = getCheckBoxValues("gradeDistribution2");

            var schoolingLength1 = $("[name='schoolingLength1']").val();
            var schoolingLength2 = $("[name='schoolingLength2']").val();

            var englishStartGrade1;
            var englishStartGrade2;
            if(schoolLevel == "1") {
                englishStartGrade1 = $("[name='englishStartGrade1']").val();
                englishStartGrade2 = $("[name='englishStartGrade2']").val();
            }else{
                englishStartGrade1 = "";
                englishStartGrade2 = "";
            }

            var externOrBoarder1 = $("[name='externOrBoarder1']").val();
            var externOrBoarder2 = $("[name='externOrBoarder2']").val();

            var schoolSize1 = $("[name='schoolSize1']").val();
            var schoolSize2 = $("[name='schoolSize2']").val();

            var gradeClassCount1 = $("[name='gradeClassCount1']").val();
            var gradeClassCount2 = $("[name='gradeClassCount2']").val();

            var classStudentCount1 = $("[name='classStudentCount1']").val();
            var classStudentCount2 = $("[name='classStudentCount2']").val();

            var branchSchoolIds1 = $("[name='branchSchoolIds1']").val();
            var branchSchoolIds2 = $("[name='branchSchoolIds2']").val();


            $.ajax({
                url: "/crm/school_review/doReview.vpage",
                type: "POST",
                data: {
                    "schoolId": schoolId,
                    "gradeDistributionFlag": gradeDistributionFlag,
                    "schoolingLengthFlag": schoolingLengthFlag,
                    "englishStartGradeFlag": englishStartGradeFlag,
                    "externOrBoarderFlag": externOrBoarderFlag,
                    "schoolSizeFlag": schoolSizeFlag,
                    "branchSchoolIdsFlag": branchSchoolIdsFlag,

                    "gradeDistribution1": gradeDistribution1,
                    "gradeDistribution2":gradeDistribution2,

                    "schoolingLength1": schoolingLength1,
                    "schoolingLength2": schoolingLength2,

                    "englishStartGrade1": englishStartGrade1,
                    "englishStartGrade2": englishStartGrade2,

                    "externOrBoarder1": externOrBoarder1,
                    "externOrBoarder2": externOrBoarder2,

                    "schoolSize1": schoolSize1,
                    "schoolSize2": schoolSize2,

                    "gradeClassCount1": gradeClassCount1,
                    "gradeClassCount2": gradeClassCount2,

                    "classStudentCount1": classStudentCount1,
                    "classStudentCount2": classStudentCount2,

                    "branchSchoolIds1": branchSchoolIds1,
                    "branchSchoolIds2": branchSchoolIds2
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
    }

    function notDail(){
        var schoolId = $("#schoolId").val();
        $.ajax({
            url: "/crm/school_review/doReview.vpage",
            type: "POST",
            data: {
                "schoolId": schoolId,
                "notDial": "true"
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
        <#if schoolExtInfo?? && canReview?? && canReview>
            var schoolId = $("#schoolId").val();
            $.ajax({
                url: "/crm/school_review/cancalReview.vpage",
                type: "POST",
                data: {
                    "schoolId": schoolId
                },
                success: function (data) {
                    if (!data.success) {
                        alert(data.info);
                    }
                    closeReviewDialog();
                    location.reload();
                }
            });
        <#else>
            closeReviewDialog();
            location.reload();
        </#if>

    }

    function renderMap(longitude, latitude){
        var map = new BMap.Map("innerMap");
        var point = new BMap.Point(longitude, latitude);
        map.centerAndZoom(point, 15);
        var marker = new BMap.Marker(point);
        map.addOverlay(marker);
    }
</script>


