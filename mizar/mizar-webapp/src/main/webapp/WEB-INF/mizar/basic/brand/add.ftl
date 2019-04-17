<#import "../../module.ftl" as module>
<@module.page
title="品牌管理"
pageJsFile={"siteJs" : "public/script/basic/brand", "commonJs" :"public/script/common/common"}
pageJs=["siteJs"]
leftMenu="品牌管理"
>
<style>
    .input-control > label{width: 120px;}
    .input-control > input, .input-control > textarea{width: 680px; }
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/brand/index.vpage">品牌列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">添加品牌</a>
</div>
<h3 class="h3-title">
    添加品牌
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span>为必填项</span>
</h3>
<form action="/basic/brand/uploadimg.vpage" method="post" style="display: none;">
    <input type="hidden" id="" name="" title="标识上传图片字段">
    <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
    <input id="upload-image" style="display:none;" class="file" name="file" type="file" accept="image/*" />
</form>

<form id="add-form"   action="/basic/brand/save.vpage" method="post">
    <div style="float:left;">
        <div class="input-control">
            <label><span class="red-mark">*</span>品牌名称：</label>
            <input id="brandName" name="brandName" data-title="品牌名称" class="require item"  placeholder="请输入品牌名称"/>
        </div>
        <div class="input-control" >
            <label>是否显示在列表：</label>
            <input type="checkbox" name="showList" id="showList" data-title="showList" class="require checkTxt" placeholder="请输入选择是否显示在品牌管列表">
        </div>
        <div class="input-control">
            <label>排序：</label>
            <input type="text" name="orderIndex" id="orderIndex" class="item" data-title="orderIndex" placeholder="请输入排序值">
        </div>

        <div class="input-control">
         <label>品牌规模：</label>
         <input id="shopScale" name="shopScale" data-title="品牌规模" class="item" placeholder="请输入品牌规模"/>
        </div>
        <div class="input-control">
            <label>品牌介绍：</label>
            <textarea name="introduction" id="introduction"  data-title="品牌介绍" placeholder="品牌介绍" style="resize: none;" ></textarea>
        </div>
        <div class="input-control">
            <label>品牌特点：</label>
            <textarea name="points" id="points"  data-title="品牌特点" placeholder="品牌特点,逗号分隔" style="resize: none;" ></textarea>
        </div>
        <div class="input-control">
            <label>获奖证书描述：</label>
            <textarea name="certificationName" id="certificationName"  data-title="获奖证书描述" placeholder="获奖证书描述" style="resize: none;"  ></textarea>
        </div>

        <div class="input-control">
            <label>创立时间：</label>
            <input name="establishment" id="establishment" class="item" data-title="创立时间" placeholder="创立时间" />
        </div>

        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>品牌LOGO：</label><span class="upload-tip">请上传品牌LOGO</span>
                <a class="blue-btn upload-image" href="javascript:void(0)" id="brandLogoUploadBtn" fieldId="brandLogo"  maxImgCount="1"  title="只能上传一张">上传</a>
            </div>
            <div class="image-preview brandLogo  clearfix">
            </div>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>中心图片：</label><span class="upload-tip">请上传中心图片</span>
                <a class="blue-btn upload-image" href="javascript:void(0)"  id="brandPhotoUploadBtn" fieldId="brandPhoto"  maxImgCount="-1"  title="可上传多张">上传</a>
            </div>
            <div class="image-preview brandPhoto clearfix">
            </div>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>获奖证书：</label><span class="upload-tip">请上传获奖证书图片</span>
                <a class="blue-btn upload-image" href="javascript:void(0)" id="certificationPhotosUploadBtn" fieldId="certificationPhotos"  maxImgCount="-1"  title="可上传多张">上传</a>
            </div>
            <div class="image-preview certificationPhotos clearfix">
            </div>
        </div>

        <#--师资力量字段隐藏-->
        <#--<div class="clearfix" style="clear:both;">-->
            <#--<div class="input-control">-->
                <#--<label>师资力量：</label><span class="upload-tip">请编辑师资力量</span>-->
                <#--<a class="blue-btn upload-image-teacher" id="faculty-upload-btn" href="javascript:void(0)">添加</a>-->
            <#--</div>-->
            <#--<div class="image-preview clearfix">-->
                <#--<input id="faculty"  name="faculty" style="display: none;" />-->
                <#--<div id="facultyBox">-->

                <#--</div>-->
            <#--</div>-->
        <#--</div>-->

        <div class=" submit-box">
            <a id="add-save-btn" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
            <a id="abandon-btn" class="submit-btn abandon-btn" href="/basic/brand/index.vpage">返回</a>
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
                    <div targetid="teacherPhoto" class="del-btn del-faculty" teacherphoto="">删除</div>
                </div>
                <%}%>
            </div>
        </div>
    </div>
</script>

</@module.page>

