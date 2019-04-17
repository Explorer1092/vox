<#import "../../module.ftl" as module>
<@module.page
title="机构管理"
pageJsFile={"siteJs" : "public/script/basic/shop"}
pageJs=["siteJs"]
leftMenu="机构管理"
>
<style>
    .input-control > label{width: 109px;}
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/shop/index.vpage">机构列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">编辑机构</a>
</div>
<#--<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>-->
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>
<form action="/common/uploadphoto.vpage" method="post" style="display: none;">
    <input type="hidden" id="" name="" title="标识上传图片字段">
    <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
    <input id="upload-image" style="display:none;" class="file" name="file" type="file" accept="image/*"/>
</form>
<h3 class="h3-title">
    新增机构
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span>为必填项</span>
</h3>
<form id="add-form" action="/basic/shop/save.vpage" method="post">
    <div style="margin-top: 10px;">
         <input type="hidden"  name="id"  value="${mizarShop.id!}" title="机构编号"/>
         <div class="input-control">
            <label><span class="red-mark">*</span>关联品牌：</label>
             <input type="hidden" name="brandId" id="brandId"  data-title="品牌Id" class="require" <#if mizarShop??>value="${mizarShop.brandId!''}"</#if> />
             <input name="brandName" id="brandName" style="width: 60%" data-title="品牌全称" class="readonly" readonly <#if mizarBrand??>value="${mizarBrand.brandName!''}</#if>"/>
             <#--只有运营和BD可以更改品牌-->
             <#if currentUser.isBD() || currentUser.isOperator()>
                <a class="blue-btn " id="chooseBrand" href="javascript:void(0)"  >选择品牌</a>
             </#if>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>机构全称：</label>
            <input name="fullName" id="fullName" data-title="机构全称" class="require item" <#if mizarShop??>value="${mizarShop.fullName!''}</#if>" placeholder="机构全称(必填)+分店名称(选填),例如 瑞思学科英语/瑞思学科英语（望京校区）"/>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>VIP：</label>
                <input type="checkbox" name="vip" id="vip"  data-title="VIP" class="require checkTxt" value="1" <#if (mizarShop.vip)??><#if mizarShop.vip==true>checked="checked"</#if></#if>">
            </div>
            <div class="input-control">
                <label>是否合作机构：</label>
                <input type="checkbox" name="cooperator" id="cooperator"  data-title="cooperator" class="require checkTxt" value="1" <#if (mizarShop.cooperator)??><#if mizarShop.cooperator==true>checked="checked"</#if></#if>">
            </div>
            <div class="input-control">
                <label><span class="red-mark">*</span>机构简称：</label>
                <input name="shortName" id="shortName" data-title="机构简称" class="require item" <#if mizarShop??>value="${mizarShop.shortName!''}</#if>"  placeholder="请填写机构简称"/>
            </div>
        <#else>
            <input type="hidden" name="vip" id="vip" value="<#if (mizarShop.vip)??>${(mizarShop.vip)?string(1,0)}<#else>0</#if>"/>
            <input type="hidden" name="cooperator" id="cooperator" value="<#if (mizarShop.cooperator)??>${(mizarShop.cooperator)?string(1,0)}<#else>0</#if>" />
            <input type="hidden" name="shortName" id="shortName" <#if mizarShop??>value="${mizarShop.shortName!''}</#if>"/>
        </#if>
        <div class="input-control">
            <label><span class="red-mark">*</span>机构介绍：</label>
            <textarea name="introduction" id="introduction" data-title="机构介绍" class="require" style="resize: none;" placeholder="请填写机构介绍"><#if mizarShop??>${mizarShop.introduction!''}</#if></textarea>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label><span class="red-mark">*</span>机构类型：</label>
                <input name="shopType" id="shopType" data-title="机构类型" class="require item" <#if mizarShop??>value="${mizarShop.shopType!''}</#if>" placeholder="请填写机构类型"/>
            </div>
        <#else>
            <input type="hidden" name="shopType" id="shopType" value="${(mizarShop.shopType)!''}" />
        </#if>
        <div class="input-control">
            <label><span class="red-mark">*</span>地区编码：</label>
            <div class="container">
                <select class="sel" style="width: 223px;" id="cmbProvince"></select>
                <select class="sel" style="width: 223px;" id="cmbCity"></select>
                <select class="sel" style="width: 225px;" id="cmbArea"></select>
            </div>
            <input type="hidden" name="regionCode" id="regionCode" data-title="所属地区编码" class="item" <#if mizarShop??>value="${mizarShop.regionCode!''}</#if>"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>所属商圈：</label>
            <input name="tradeArea" id="tradeArea" data-title="所属商圈" class="require item" <#if mizarShop??>value="${mizarShop.tradeArea!''}</#if>" placeholder="必填:所属商圈"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>详细地址：</label>
            <input name="address" id="address" data-title="详细地址" class="require item" <#if mizarShop??>value="${mizarShop.address!''}</#if>" placeholder="请填写详细地址"/>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>适配年级：</label>
                <#assign grade_list = (mizarShop.matchGrade)!?split(",")/>
                <#list ['一','二','三','四','五','六','七','八','九'] as g>
                    <span class="js-gradeItem grade_btn <#list grade_list as gl>
                        <#if gl == (g_index+1)?string>active</#if>
                    </#list>" style="" data-index="${g_index+1}">${g}年级</span>
                </#list>
                <input type="hidden" name="matchGrade" id="matchGrade" class="item" data-title="适配年级"/>
            </div>
            <div class="input-control">
                <label><span class="red-mark">*</span>合作等级分数：</label>
                <input name="cooperationLevel" id="cooperationLevel" class="require item" data-title="合作等级分数"  <#if mizarShop??>value="${mizarShop.cooperationLevel!''}</#if>" placeholder="请填写合作等级分数,1~10"/>
            </div>
            <div class="input-control">
                <label><span class="red-mark">*</span>人工调整分数：</label>
                <input name="adjustScore" id="adjustScore" class="require item" data-title="人工调整分数"  <#if mizarShop??>value="${mizarShop.adjustScore!''}</#if>" placeholder="请填写人工调整分数,1~10"/>
            </div>
        <#else>
            <input type="hidden" name="matchGrade" value="${(mizarShop.matchGrade)!''}">
            <input type="hidden" name="cooperationLevel" id="cooperationLevel" value="${(mizarShop.cooperationLevel)!'0'}" />
            <input type="hidden" name="adjustScore" id="adjustScore" value="${(mizarShop.adjustScore)!'0'}" />
        </#if>
        <div class="input-control">
            <label><span class="red-mark">*</span>联系电话：</label>
            <input name="contactPhone" id="contactPhone" class="require item"  data-title="联系电话" <#if (mizarShop.contactPhone)??>value="<#list mizarShop.contactPhone as phone>${phone}<#if phone_has_next>,</#if></#list>"</#if>/>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label><span class="red-mark">*</span>是否线上机构：</label>
                <select id="type" name="type" class="sel">
                    <option value="1"  <#if (mizarShop.type)??&&mizarShop.type==1>selected</#if>>线上</option>
                    <option value="0" <#if  (mizarShop.type)??&&mizarShop.type==0>selected</#if>>线下</option>
                </select>
            </div>
            <#--<div class="input-control">-->
                <#--<label><span class="red-mark">*</span>状态：</label>-->
                <#--<select id="shopStatus" name="shopStatus" class="form-control sel">-->
                    <#--<option value="ONLINE" <#if (mizarShop.shopStatus)??&&mizarShop.shopStatus=='ONLINE'>selected</#if> >ONLINE</option>-->
                    <#--<option value="OFFLINE" <#if (mizarShop.shopStatus)??&&mizarShop.shopStatus=='OFFLINE'>selected</#if>>OFFLINE</option>-->
                    <#--<option value="SUSPEND" <#if (mizarShop.shopStatus)??&&mizarShop.shopStatus=='SUSPEND'>selected</#if>>SUSPEND</option>-->
                <#--</select>-->
            <#--</div>-->
        <#else>
            <input type="hidden" name="type" id="type" value="${(mizarShop.type)!'0'}" />
        </#if>
        <input type="hidden" name="shopStatus" id="shopStatus" value="${(mizarShop.shopStatus)!'PENDING'}" />

        <div class="input-control">
            <label>GPS信息：</label>
            经度: <input type="text" id="longitude" name="longitude" class="require item gps-input" style="width: 300px;" data-title="经度" <#if mizarShop??>value="${mizarShop.longitude!}</#if>"/>&nbsp;
            纬度: <input type="text" id="latitude" name="latitude" class="require item gps-input" style="width: 300px;" data-title="纬度" <#if mizarShop??>value="${mizarShop.latitude!}</#if>"/>
            <#--是否高德GPS: <input type="checkbox" id="baiduGps" name="baiduGps" value="1" <#if mizarShop??>checked="${mizarShop.baiduGps?string('checked','')}"</#if>">-->
        </div>
        <div class="input-control">
            <label class="col-sm-2 control-label">高德地图：</label>
            <div>
                <div id="innerMap" class="inner-map"></div>
            </div>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label><span class="red-mark">*</span>机构分类：</label>
                <select id="firstCategoryLevel" name="firstCategory" class="sel" style="width: 338px;"></select>
                <select id="secondCategoryLevel" name="secondCategory" class="sel" style="width: 338px;"></select>
            </div>
        <#else>
            <input type="hidden" name="firstCategory" id="firstCategory" <#if mizarShop?? && mizarShop.firstCategory?? && mizarShop.firstCategory?has_content>value="<#list mizarShop.firstCategory as c>${c}<#if c_has_next>,</#if></#list>"</#if> />
            <input type="hidden" name="secondCategory" id="secondCategory" <#if mizarShop?? && mizarShop.secondCategory?? && mizarShop.secondCategory?has_content>value="<#list mizarShop.secondCategory as c>${c}<#if c_has_next>,</#if></#list>"</#if> />
        </#if>
        <div class="input-control">
            <label>到店礼：</label>
            <input name="welcomeGift" id="welcomeGift" class="item" <#if mizarShop??>value="${(mizarShop.welcomeGift)!''}</#if>"/>
        </div>

        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>机构图片：</label><span class="upload-tip">请上传机构图片</span>
                <a class="blue-btn upload-image" href="javascript:void(0)" fieldId="photo"  maxImgCount="-1"  title="可上传多张">上传</a>
            </div>
            <div class="image-preview photo clearfix">
                <#if mizarShop?? && mizarShop.photo?? >
                    <#list mizarShop.photo as p>
                        <div class="image">
                            <input name="photo" value="${p!}" title="隐藏图片地址" type="hidden">
                            <img class="" src="${p!}" />
                            <div class="del-btn">删除</div>
                        </div>
                    </#list>
                </#if>
            </div>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>师资力量：</label><span class="upload-tip">请编辑师资力量</span>
                <a class="blue-btn upload-image-teacher" id="faculty-upload-btn" href="javascript:void(0)"  maxImgCount="1" >添加</a>
            </div>
            <div class="image-preview clearfix">
                <input id="faculty"  name="faculty" style="display: none;" />
                <div id="facultyBox">

                </div>
            </div>
        </div>

        <div class=" submit-box">
            <a id="add-save-btn" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
            <a id="abandon-btn" class="submit-btn abandon-btn" href="/basic/shop/index.vpage">返回</a>
        </div>
    </div>
</form>


<script type="text/html" id="facultyBox_tem">
    <%if(list.length > 0){%>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th>教师名称</th>
            <th>教师教龄</th>
            <th>教师科目</th>
            <th>教师描述</th>
            <th>教师图片</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <%for(var i = 0; i < list.length;i++){%>
        <tr>
            <td><%=list[i].teacherName%><input type="hidden" name="tName" value="<%=list[i].teacherName%>"/></td>
            <td><%=list[i].teacherSeniority%><input type="hidden" name="tSeniority" value="<%=list[i].teacherSeniority%>"/></td>
            <td><%=list[i].teacherCourse%><input type="hidden" name="tCourse" value="<%=list[i].teacherCourse%>"/></td>
            <td><%=list[i].introduction%><input type="hidden" name="tIntroduction" value="<%=list[i].introduction%>"/></td>
            <td>
                <input type="hidden" name="tPhoto" value="<%=list[i].teacherPhoto%>"/>
                <div class="image">
                    <img src="<%=list[i].teacherPhoto%>">
                </div>
            </td>
            <td>
                <a class="facultyEditBtn" href="javascript:void (0);" style="color: #00a0e9;float:left;" data-index="<%=i%>">编辑</a>
                <a class="facultyDeleteBtn" href="javascript:void (0);" style="color: #00a0e9;float:right;" data-index="<%=i%>">删除</a>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <%}%>
</script>

<script id="uploaderDialog_tem" type="text/html">
    <div id="uploaderDialog">
        <h3 class="h3-title">
            <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span>为必填项</span>
        </h3>
        <div style="float: left;">
            <div class="input-control">
                <label><span class="red-mark">*</span>教师名称：</label>
                <input type="text"  id="teacherName"  name="teacherName" style="width: 130px;" data-title="教师名称" placeholder="教师名称" class="teacher item require" value="<%=teacher.tName%>"/>
            </div>

            <div class="input-control">
                <label><span class="red-mark">*</span>教师教龄：</label>
                <input type="text"  id="teacherSeniority"  name="teacherSeniority" style="width: 130px;" data-title="教师教龄" placeholder="必须为数字" class="teacher item require" value="<%=teacher.tSeniority%>"/>
            </div>
            <div class="input-control">
                <label><span class="red-mark">*</span>教师科目：</label>
                <input type="text"  id="teacherCourse"  name="teacherCourse" style="width: 130px;" data-title="教师科目" placeholder="不长于5个字符" class="teacher item require" value="<%=teacher.tCourse%>"/>
            </div>
        </div>
        <div style="float: right;">
            <div class="input-control">
                <label><span class="red-mark">*</span>教师描述：</label>
                <textarea  id="teacherIntroduction"  name="teacherIntroduction" style="width: 180px;resize: none;" data-title="教师描述" placeholder="教师描述" class="teacher require"><%=teacher.tIntroduction%></textarea>
            </div>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>教师图片：</label><span class="upload-tip">请上传教师图片</span>
                <a class="blue-btn upload-image teacherPhoto_upload-image" id="teacherPhotoUploadBtn" href="javascript:void(0)" fieldId="teacherPhoto" maxImgCount="1"  title="可上传一张">上传</a>
            </div>
            <div class="image-preview teacherPhoto  clearfix">
                <%if (teacher.tPhoto) {%>
                <div id="img-preview-teacherPhoto" class="image">
                    <input id="teacherPhoto" type="hidden" name="teacherPhoto" value="<%=teacher.tPhoto%>">
                    <img src="<%=teacher.tPhoto%>">
                    <div targetid="teacherPhoto" class="del-btn-img del-btn del-faculty" teacherphoto="">删除</div>
                </div>
                <%}%>
            </div>
        </div>
    </div>
</script>

</@module.page>

<script type="text/javascript">
    <#if (mizarShop.faculty)??>
    var mizarShopList = [];
        <#list mizarShop.faculty as facultyMap>
        mizarShopList.push({
            teacherName : '${facultyMap["name"]!''}',
            teacherSeniority : '${facultyMap["experience"]!''}',
            teacherCourse : '${facultyMap["course"]!''}',
            introduction : '${facultyMap["description"]!''}',
            teacherPhoto : '${facultyMap["photo"]!''}'
        });
        console.info(mizarShopList);
        </#list>
    </#if>
    var regionList = [],categoryList=[],initParam;
    <#if initParam?has_content>
        initParam = ${json_encode(initParam)};
    </#if>
    <#if regions?has_content>
        regionList = ${json_encode(regions)};
    </#if>
    <#if categoryList?has_content>
        categoryList = ${json_encode(categoryList)};
    </#if>

</script>