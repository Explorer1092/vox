<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑机构" page_num=17>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>
<#--edit-->
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet" />
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<#--edit-->
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑机构&nbsp;&nbsp;
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
        <#if shop??>
        <div  style="float: right;">
            <a title="添加" href="${requestContext.webAppContextPath}/mizar/goods/info.vpage?sid=${shop.id}" class="btn btn-success">
                <i class="icon-plus icon-white"></i> 添加课程
            </a>
            <a title="添加" href="${requestContext.webAppContextPath}/mizar/rating/info.vpage?shopId=${shop.id}" class="btn btn-success">
                <i class="icon-plus icon-white"></i> 添加点评
            </a>
            <a title="查看关联品牌" href="${requestContext.webAppContextPath}/mizar/brand/info.vpage?bid=${shop.brandId}" class="btn btn-info">
                <i class="icon-th icon-white"></i> 查看关联品牌
            </a>
            <a title="查看关联课程" href="${requestContext.webAppContextPath}/mizar/goods/index.vpage?sid=${shop.id}" class="btn btn-warning">
                <i class="icon-leaf icon-white"></i> 查看关联课程
            </a>
            <a title="查看点评" href="${requestContext.webAppContextPath}/mizar/rating/index.vpage?shopId=${shop.id}" class="btn btn-warning">
                <i class="icon-fire icon-white"></i> 查看点评
            </a>
        </div>
        </#if>
    </legend>
    <div id="uploader"></div>
    <div class="row-fluid"><div class="span12"><div class="well">
        <form id="info_frm" name="info_frm" enctype="multipart/form-data" action="saveshop.vpage" method="post">
            <input id="sid" name="sid" value="${sid}" type="hidden">
            <div class="form-horizontal">
                <#if shop??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构ID</label>
                    <div class="controls">
                        <input type="text"  class="form-control input_txt" value="${shop.id!}" readonly/>
                    </div>
                </div>
                </#if>
                <div class="control-group">
                    <label class="col-sm-2 control-label">品牌ID</label>
                    <div class="controls">
                        <input type="text" id="bid" name="bid" class="form-control input_txt"
                               <#if new?? && !new> <#if shop??>value="${shop.brandId!}"</#if> <#else> <#if bid??>value="${bid!}"</#if> </#if>/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构全称</label>
                    <div class="controls">
                        <input type="text" id="fullName" name="fullName" class="form-control input_txt" value="<#if shop??>${shop.fullName!}</#if>" />
                        <input type="checkbox" id="vip" name="vip" <#if shop?? && shop.isVip()> checked </#if>>&nbsp;&nbsp;VIP
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构简称</label>
                    <div class="controls">
                        <input type="text" id="shortName" name="shortName" class="form-control input_txt" value="<#if shop??>${shop.shortName!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构介绍</label>
                    <div class="controls">
                        <textarea id="intro" name="intro" class="intro"  placeholder="请填写机构介绍" style="display: none;"><#if shop??>${shop.introduction}</#if></textarea>
                        <script id="container" type="text/plain"></script>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构类型</label>
                    <div class="controls">
                        <input type="text" id="shopType" name="shopType" class="form-control input_txt" value="<#if shop??>${shop.shopType!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">所属地区编码</label>
                    <div class="controls">
                        <input type="text" id="region" name="region" class="form-control input_txt" value="<#if shop??>${shop.regionCode!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">所属商圈</label>
                    <div class="controls">
                        <textarea id="area" name="area" class="intro_small" placeholder="请填写所属商圈"><#if shop??>${shop.tradeArea}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">详细地址</label>
                    <div class="controls">
                        <textarea id="address" name="address" class="intro_middle" placeholder="请填写详细地址"><#if shop??>${shop.address}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">适配年级</label>
                    <div class="controls">
                        <input type="text" id="matchGrade" name="matchGrade" class="intro_middle" placeholder="请填写适配年级,多个年级以逗号分隔" <#if shop??>value="${shop.matchGrade!}"</#if>></input>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">合作等级分数</label>
                    <div class="controls">
                        <input type="text" id="cooperationLevel" name="cooperationLevel" class="intro_middle" placeholder="请填写合作等级分数" <#if shop??>value="${shop.cooperationLevel!}"</#if>></input>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">人工调整分数</label>
                    <div class="controls">
                        <input type="text" id="adjustScore" name="adjustScore" class="intro_middle" placeholder="请填写人工调整分数" <#if shop??>value="${shop.adjustScore!}"</#if>></input>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">是否线上机构</label>
                    <div class="controls">
                        <select id="type" name="type" class="form-control">
                            <option value="1" <#if (shop.type)?? && shop.type == 1>selected</#if>>线上</option>
                            <option value="0" <#if (shop.type)?? && shop.type == 0>selected</#if>>线下</option>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">状态</label>
                    <div class="controls">
                        <select id="shopStatus" name="shopStatus" class="form-control">
                            <option value="ONLINE" <#if (shop.shopStatus)?? && shop.shopStatus == 'ONLINE'>selected</#if>>ONLINE</option>
                            <option value="OFFLINE" <#if (shop.shopStatus)?? && shop.shopStatus == 'OFFLINE'>selected</#if>>OFFLINE</option>
                            <option value="SUSPEND" <#if (shop.shopStatus)?? && shop.shopStatus == 'SUSPEND'>selected</#if>>SUSPEND</option>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">GPS信息</label>
                    <div class="controls">
                        经度: <input type="text" id="longitude" name="longitude" class="form-control input_txt_small"
                                   value="<#if shop??>${shop.longitude!}</#if>" onchange="updateMap()" />&nbsp;
                        纬度: <input type="text" id="latitude" name="latitude" class="form-control input_txt_small"
                                   value="<#if shop??>${shop.latitude!}</#if>" onchange="updateMap()"/>
                        <input type="checkbox" id="baiduGps" name="baiduGps" <#if shop?? && shop.isBaiduGps()> checked </#if>>&nbsp;&nbsp;是否百度GPS
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">百度地图</label>
                    <div class="controls">
                      <div id="innerMap" style="width: 50%; height: 250px;"></div>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">联系电话</label>
                    <div class="controls">
                        <textarea id="phone" name="phone" class="intro_small"
                                  placeholder="请填写联系电话,以逗号分隔"><#if shop?? && shop.contactPhone?has_content><#list shop.contactPhone as phone><#if phone_index!=0>,</#if>${phone}</#list></#if></textarea>
                    </div>
                </div>
                <#if shop??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构图片</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_photo" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                            <#if shop?? && shop.photo?? && shop.photo?has_content>
                                <#list shop.photo as p>
                                    <#if p_index%3== 0> <tr></#if>
                                    <td class="img_td">
                                        <div class="img_box">
                                            <span class="img_x_alt" id="del_photo" data-file="${p!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                            <img src="${p!}" />
                                        </div>
                                    </td>
                                    <#if p_index%3==2 || !p_has_next>
                                        <#if shop.photo?size % 3 == 0><td></td><td></td></#if>
                                        <#if shop.photo?size % 3 == 1><td></td></#if>
                                    </tr>
                                    </#if>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
                </#if>
                <div class="control-group">
                    <label class="col-sm-2 control-label">一级分类</label>
                    <div class="controls">
                        <select id="firstcat" name="firstcat" class="input_txt">
                            <#if categoryList??>
                            <#list categoryList as category>
                                <option value="${category!}" <#if shop?? && shop.firstCategory?has_content><#if shop.firstCategory[0] == category> selected </#if> </#if> >${category!}</option>
                            </#list>
                            </#if>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">二级分类</label>
                    <div class="controls">
                        <textarea id="secondcat" name="secondcat" class="intro_small"
                                  placeholder="请填写二级分类,以逗号分隔"><#if shop?? && shop.secondCategory?has_content><#list shop.secondCategory as s><#if s_index!=0>,</#if>${s}</#list></#if></textarea>
                    </div>
                </div>
                <#if shop??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">评价体系</label>
                    <div class="controls">
                        评论条数: <input type="text" id="ratingCount" name="ratingCount" class="form-control input_txt_small" value="${shop.ratingCount!0}" readonly />&nbsp;
                        评论星级: <input type="text" id="ratingStar" name="ratingStar" class="form-control input_txt_small" value="${shop.ratingStar!0}" readonly/>
                        <span>(延迟一天 任务刷新)</span>
                    </div>
                </#if>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">到店礼</label>
                    <div class="controls">
                        <input type="text" id="welcomeGift" name="welcomeGift" class="form-control input_txt" value="<#if shop??>${shop.welcomeGift!}</#if>" />
                    </div>
                </div>
            </div>
        </form>
    </div></div></div>
</div>
<div id="uploaderDialog" class="modal fade hide" style="width:550px; height: 300px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div style="float: left; width: 280px;">
            <div style="height: 200px; width: 280px;">
                <img id="imgSrc" src="" alt="预览" style="height: 200px; width: 280px;"/>
            </div>
        </div>
        <div style="float: right">
            <div style="display: block;">
                <textarea placeholder="请填写描述" id="uploadDesc" style="resize: none;"></textarea>
            </div>
            <div style="display: block;">
                <a href="javascript:void(0);" class="uploader">
                    <input type="file" name="file" id="uploadFile" accept="image/*" onchange="previewImg(this)">选择素材
                </a>
            </div>
        </div>
        <input type="hidden" id="uploadField" value="photo">
    </div>
    <div class="modal-footer">
        <button title="确认上传" class="uploader" id="upload_confirm">
            <i class="icon-ok"></i>
        </button>
        <button class="uploader" data-dismiss="modal" aria-hidden="true"><i class="icon-trash"></i></button>
    </div>
</div>
<script type="text/javascript">
    var map = new BMap.Map("innerMap");
    map.enableScrollWheelZoom();   //启用滚轮放大缩小
    map.enableContinuousZoom();    //启用地图惯性拖拽
    renderMap();
    $(function () {
        $('#info_frm').on('submit', function () {
            var content = ue.getContent();
            content=content.replace(/\n/g,"");
            content=content.replace(/>\s+?</g,"><");
            if (content.length == 0) {
                alert("内容不能为空");
                return false;
            }

            $("#intro").val(content);


            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'saveshop.vpage',
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = 'info.vpage?sid=' + res.sid;
                    } else {
                        alert("保存失败:" + res.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_info').on('click', function () {

            if (confirm("是否确认保存？")) {
                var cooperationLevel = $('#cooperationLevel').val();
                if(isNaN(cooperationLevel) ){
                     alert('合作等级必须为数字');
                    return false;
                }
                var adjustScore = $('#adjustScore').val();
                if(isNaN(adjustScore) ){
                    alert('人工调节必须为数字');
                    return false;
                }
                $('#info_frm').submit();
            }
        });

        $("[id^='del_']").on('click', function() {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var $this = $(this);
            var field = $this.attr("id").substring("del_".length);
            var file = $(this).data("file");
            var sid = $('#sid').val();
            $.post('deletephoto.vpage', {sid : sid, field: field, file: file}, function(res) {
               if (res.success) {
                   window.location.reload();
               } else {
                   alert(res.info);
               }
            });
        });

        $("[id^='upload_btn_']").on('click', function() {
            $('#uploadFile').val("");
            $('#imgSrc').attr("src", "");
            $('#uploadDesc').val("");
            $('#uploaderDialog').modal("show");
        });

        $('#upload_confirm').on('click', function() {
            // 获取参数
            var field = $('#uploadField').val();
            var desc = $('#uploadDesc').val();
            // 拼formData
            var formData = new FormData();
            var file = $('#uploadFile')[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('field', field);
            formData.append('desc', desc);
            formData.append('sid', $('#sid').val());
            // 发起请求
            $.ajax({
                url: 'uploadphoto.vpage' ,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        alert("上传成功");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        //edit
        var ue = UE.getEditor('container',{
            serverUrl:"/advisory/ueditorcontroller.vpage",
            zIndex:1040,
            fontsize:[16,18,20,22,24,26,28,30,32,34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink',  '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen',   '|', 'preview', 'searchreplace'
            ]]
        });

        ue.ready(function(){
            ue.setContent('<#if shop??>${shop.introduction}</#if>'.replace(/\n/g, '<p><br/></p>'));
        })
    });

    function renderMap() {
        var longitude = $('#longitude').val();
        var latitude = $('#latitude').val();
        var overlays = map.getOverlays();
        for (var i = 0; i < overlays.length; ++i) {
            map.removeOverlay(overlays[i]);
        }
        if (longitude != '' && latitude != '') {
            var point = new BMap.Point(longitude, latitude);
            map.centerAndZoom(point, 15);
            var marker = new BMap.Marker(point);
            map.addOverlay(marker);
        } else {
            map.centerAndZoom("北京", 15);
        }
        //单击获取点击的经纬度
        map.addEventListener("click", function(e){
            var overlays = map.getOverlays();
            for (var i = 0; i < overlays.length; ++i) {
                map.removeOverlay(overlays[i]);
            }
            $('#longitude').val(e.point.lng);
            $('#latitude').val(e.point.lat);
            $('#baiduGps').attr("checked", true);
            marker = new BMap.Marker(e.point);
            map.addOverlay(marker);
        });
    }

    function updateMap() {
        var longitude = $('#longitude').val();
        var latitude = $('#latitude').val();
        var overlays = map.getOverlays();
        for (var i = 0; i < overlays.length; ++i) {
            map.removeOverlay(overlays[i]);
        }
        if (longitude != '' && latitude != '') {
            var point = new BMap.Point(longitude, latitude);
            map.centerAndZoom(point, 15);
            var marker = new BMap.Marker(point);
            map.addOverlay(marker);
        } else {
            map.centerAndZoom("北京", 15);
        }
    }

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }

</script>
</@layout_default.page>