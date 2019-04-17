
<style>
    dd,dl,dt,form,h1,h2,h3,h4,h5,h6,li,ol,p,select,ul{margin:0;padding:0}
    html{font:14px/1.5 "宋体",Arial}
    /*body,html{width:100%;height:100%;color:#333}*/
    li,ol,ul{list-style-type:none}
    em,i{font-style:normal}
    a,a:visited{color:#333}
    a{text-decoration:none}
    a img{border:0 none}

    .main .options .teachers{height:260px;}
    .main{width:960px;margin:0 auto;background:#ccc;}
    .main>div{float:left;box-sizing: border-box;border-right:1px solid black;}
    .main .pictures{width:25%;}
    .main .pictures img{width:100%;}
    .main .school{width:25%;padding:12px;}
    .main .school h3{margin:15px 0;font-size:18px;}
    .main .school ul li:nth-child(n+6){color:#e33;}
    .main .school ul li{line-height:2em;}
    .main .options{width:50%;padding:24px 10px;}
    .main .options .teachers input{display:inline-block;margin:5px 50px;width:180px;text-align:center;padding:1px;}
    .main .options table{line-height:1.5em;margin-top:10px;}
    .main .options table label{display: inline;}
    .main .options table label,.main .options table input[type="checkbox"]{cursor:pointer;}
    .main .options table th{color:#e33;text-align:left;width:150px;padding-right:22px;font-weight:400;}
    .main .options table td{text-align:left;font-size:14px;}
    .main .options table th+td{width:50px;text-align:right;}
    .main .options table select,.main .options .input{width:80%;}
    .main .submit{padding:20px 0;display:flex;line-height:1.4em;justify-content: space-between;}
    .main .submit>input{width:23%;}

    i.clr{clear:both;display:block;}
    .disabled{pointer-events:none;opacity:0.4;}
</style>

<div class="main">
    <input id="schoolId" name="schoolId" type="hidden" <#if school??>value="${school.id!}"</#if>/>
    <div class="pictures">
        <div>
        <#if schoolExtInfo?? && schoolExtInfo.photoUrl??><img src="${schoolExtInfo.photoUrl!}" style='height: 200px; width: 200px'/>
        <#else>
        </#if>
            <div>
                照相机制造商：<#if photoMeta??>${photoMeta.Make!}</#if><br />
                照相机型号：<#if photoMeta??>${photoMeta.Model!}</#if><br />
                拍摄时间：<#if photoMeta??>${photoMeta["Date/Time"]!}</#if><br />
            </div>
        </div>
    <#if schoolExtInfo?? && schoolExtInfo.latitude?? && schoolExtInfo.longitude??>
        <div id="map" style="height: 200px; width: 200px;">
        </div>
        <script type="text/javascript">
            var map = new BMap.Map("map");          // 创建地图实例
            var point = new BMap.Point(${schoolExtInfo.longitude}, ${schoolExtInfo.latitude});  // 创建点坐标
            map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
        </script>
    </#if>
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
            <li>学校学生数量：${schoolExtInfo.grade1StudentCount!0 + schoolExtInfo.grade2StudentCount!0 + schoolExtInfo.grade3StudentCount!0 + schoolExtInfo.grade4StudentCount!0 + schoolExtInfo.grade5StudentCount!0 + schoolExtInfo.grade6StudentCount!0 + schoolExtInfo.grade7StudentCount!0 + schoolExtInfo.grade8StudentCount!0 + schoolExtInfo.grade9StudentCount!0}</li>
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
            <li></li>
            <li></li>
            <li></li>
            <li></li>
            <li></li>
        </ul>
    <#if reviewResult??>
        <table>
            <tr>
                <th>年级分布：</th>
                <#if reviewResult.gradeDistributionFlag?? && reviewResult.gradeDistributionFlag>
                    <td><label class="all">一致</label></td>
                <#else>
                    <td><label class="all">不一致</label></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>${detail.gradeDistribution!}</td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            <tr>

                <th>学制：</th>
                <#if reviewResult.schoolingLengthFlag?? && reviewResult.schoolingLengthFlag>
                    <td><label class="all">一致</label></td>
                <#else>
                    <td><label class="all">不一致</label></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>
                                <#if detail.schoolingLength == 1>五年制
                                <#elseif detail.schoolingLength == 2>六年制
                                <#elseif detail.schoolingLength == 3>三年制
                                <#elseif detail.schoolingLength == 4>四年制
                                </#if>
                            </td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            <#if school?? && school.level == 1>
            <tr>
                <th>英语起始年级：</th>
                <#if reviewResult.englishStartGradeFlag?? && reviewResult.englishStartGradeFlag>
                    <td><label class="all">一致</label></td>
                <#else>
                    <td><label class="all">不一致</label></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>
                                <#if detail.englishStartGrade == 1>一年级
                                <#elseif detail.englishStartGrade == 2>二年级
                                <#elseif detail.englishStartGrade == 3>三年级
                                <#elseif detail.englishStartGrade == 4>四年级
                                <#elseif detail.englishStartGrade == 5>五年级
                                <#elseif detail.englishStartGrade == 6>六年级
                                </#if>
                            </td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            </#if>
            <tr>
                <th>走住：</th>
                <#if reviewResult.externOrBoarderFlag?? && reviewResult.externOrBoarderFlag>
                    <td><label class="all">一致</label></td>
                <#else>
                    <td><label class="all">不一致</label></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>
                                <#if detail.externOrBoarder == 1>走读
                                <#elseif detail.externOrBoarder == 2>住宿
                                <#elseif detail.externOrBoarder == 3>走读/住宿
                                </#if>
                            </td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            <tr>
                <th>学校学生数量：</th>
                <#if reviewResult.schoolSizeFlag?? && reviewResult.schoolSizeFlag>
                    <td><label class="all">一致</label></td>
                <#else>
                    <td><label class="all">不一致</label></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>${detail.schoolSize!}</td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            <tr>
                <th>本年级班级数：</th>
                <#if reviewResult.schoolSizeFlag?? && reviewResult.schoolSizeFlag>
                    <td></td>
                <#else>
                    <td></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>${detail.gradeClassCount!}</td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            <tr>
                <th>本班人数：</th>
                <#if reviewResult.schoolSizeFlag?? && reviewResult.schoolSizeFlag>
                    <td></td>
                <#else>
                    <td></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>${detail.classStudentCount!}</td>
                        </#list>
                    </#if>
                </#if>
            </tr>
            <tr>
                <th>关联分校：</th>
                <#if reviewResult.branchSchoolIdsFlag?? && reviewResult.branchSchoolIdsFlag>
                    <td><label class="all">一致</label></td>
                <#else>
                    <td><label class="all">不一致</label></td>
                    <#if reviewResult.resultDetailList??>
                        <#list reviewResult.resultDetailList as detail>
                            <td>
                                <#if detail.branchSchoolIds??>
                                    <#list detail.branchSchoolIds as subSchoolId>
                                    ${subSchoolId},
                                    </#list>
                                </#if>
                            </td>
                        </#list>
                    </#if>
                </#if>
            </tr>
        </table>
    <#else>
        <#if schoolExtInfo?? && schoolExtInfo.reviewResult?? && schoolExtInfo.reviewResult == -1>无人应答</#if>
    </#if>
        <div class="submit">
            <input type="button" value="关闭" onclick="closeReviewDialog();"/>
        </div>
    </div>
    <i class="clr"></i>
</div>



