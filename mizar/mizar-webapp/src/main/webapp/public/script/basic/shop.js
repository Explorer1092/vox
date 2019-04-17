/*-----------品牌相关-----------*/
define(["jquery", "prompt", "datetimepicker", "paginator", "jqform","template"], function ($) {
    /*********       机构列表相关          *********/
    // 查询
    $("#query-btn").on("click", function () {
        $("#query-form").submit();
    });

    // 分页插件
    var paginator = $('#paginator');
    var currentPage = 1;
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: parseInt(paginator.attr("totalPage")),
            visiblePages: 10,
            currentPage: parseInt(paginator.attr("data-startPage") || 1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex, opType) {
                currentPage = pageIndex;
                if (opType == 'change') {
                    $('#pageIndex').val(pageIndex);
                    $('#query-form').submit();
                }
            }

        });
    }

    // 变更状态
    $(".op-status").on("click", function () {
        var $this = $(this);
        var data = {
            sid: $this.data().sid,
            status: $this.data().status
        };
        $.prompt("<div style='text-align:center;'>是否确认"+$this.html()+"该机构</div>", {
            title: "操作提示",
            buttons: {"取消":false, "确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $.post("/basic/shop/changestatus.vpage", data, function (res) {
                        if (res.success) {
                            $.prompt("<div style='text-align:center;'>状态更新成功</div>", {
                                title: "操作提示",
                                buttons: {"确定": true},
                                focus: 1,
                                submit: function (e, v) {
                                    location.reload();
                                },
                                useiframe: true
                            });
                        } else {
                            $.prompt("<div style='text-align:center;'>" + (res.info || "状态更新失败！") + "</div>", {
                                title: "错误提示",
                                buttons: {"确定": true},
                                focus: 1,
                                useiframe: true
                            });
                        }
                    });
                }
            },
            useiframe: true
        });

    });
    /*********       机构列表结束          *********/


    /*********       机构详情相关          *********/

    /**
     * 初始化省市区三级联动 和 一二级分类两级联动
     * **/
    var addressInit = function(_cmbProvince, _cmbCity, _cmbArea, defaultProvince, defaultCity, defaultArea, allData) {
        var cmbProvince = document.getElementById(_cmbProvince);
        var cmbCity = document.getElementById(_cmbCity);
        var cmbArea = document.getElementById(_cmbArea);

        function cmbSelect(cmb, str) {
            if(cmb){
                for (var i = 0; i < cmb.options.length; i++) {
                    if (cmb.options[i].value == str) {
                        cmb.selectedIndex = i;
                        return;
                    }
                }
            }
        }

        function cmbAddOption(cmb, str, obj) {
            var option = document.createElement("OPTION");
            if(cmb){
                cmb.options.add(option);
                option.innerHTML = str;
                if(!obj){ //第三级最后参数为空
                    option.value = str.code;
                    option.obj = str;
                    option.innerHTML = str.name;
                }else{
                    option.innerHTML = str;
                    option.value = obj.code;
                    option.obj = obj;
                }
            }
        }

        function changeCity() {
            if(cmbArea){ //兼容只有两级菜单的情况
                cmbArea.options.length = 0;
                if (cmbCity.selectedIndex == -1) return;
                var item = cmbCity.options[cmbCity.selectedIndex].obj;
                for (var i = 0; i < item.children.length; i++) {
                    cmbAddOption(cmbArea, item.children[i], null);
                }
                cmbSelect(cmbArea, defaultArea);
            }
        }

        function changeProvince() {
            if(cmbCity){
                cmbCity.options.length = 0;
                cmbCity.onchange = null;
                if (cmbProvince.selectedIndex == -1) return;
                var item = cmbProvince.options[cmbProvince.selectedIndex].obj;
                for (var i = 0; i < item.children.length; i++) {
                    cmbAddOption(cmbCity, item.children[i].name, item.children[i]);
                }
                cmbSelect(cmbCity, defaultCity);
                changeCity();
                cmbCity.onchange = changeCity;
            }
        }

        for (var i = 0; i < allData.length; i++) {
            cmbAddOption(cmbProvince, allData[i].name, allData[i]);
        }
        cmbSelect(cmbProvince, defaultProvince);
        changeProvince();
        if(cmbProvince){
            cmbProvince.onchange = changeProvince;
        }
    };

    //初始化省市区,一二级分类
    var pageUrl = location.href;
    if(pageUrl.indexOf("basic/shop/add") != -1 || pageUrl.indexOf("basic/shop/edit") != -1){
        if(initParam){
            addressInit('cmbProvince', 'cmbCity', 'cmbArea', initParam.provCode, initParam.cityCode, initParam.countyCode, regionList);
            addressInit('firstCategoryLevel', 'secondCategoryLevel', '',initParam.firstCat, initParam.secondCat, '', categoryList);
        }else{
            addressInit('cmbProvince', 'cmbCity', 'cmbArea', '','','',regionList);
            addressInit('firstCategoryLevel', 'secondCategoryLevel', '','', '', '', categoryList);
        }
    }

    // 校验输入项
    var requireInputs = $(".require");
    function isEmptyInput() {
        var isTrue = false;
        requireInputs.each(function () {
            if ($(this).val() == '') {
                $(this).addClass("error").val("请填写" + $(this).attr("data-title"));
                isTrue = true;
            }
        });
        if ($(".require.error").length > 0) {
            return true;
        }
        return isTrue;
    }

    $(document).on("click", ".js-gradeItem", function () {
        $(this).toggleClass("active");
    });

    $(document).on("focus", ".require.error", function () {
        $(this).removeClass('error').val('');
    });

    // 上传照片
    var fileInput = $("#upload-image");
    var uploadTarget = null;
    $(document).on("click", ".upload-image", function () {
        uploadTarget = this;
        if($(uploadTarget).attr('maxImgCount')){
            var maxImgCount = parseInt($(uploadTarget).attr('maxImgCount'));
            if (maxImgCount == 0) {
                alert('已到达最大数量,不可以再上传了!');
                return;
            }
        }
        fileInput.click();
    });

    $(document).on("change", "#upload-image", function () {
        $(uploadTarget).addClass("count-down").html("上传中…");

        $(this).parent().ajaxSubmit(function (res) {
            if (res.success) {
                //每上传一张照片,append 一段html,删除时,将这一段html删除
                var html='<div id=img-preview-' + $(uploadTarget).attr("fieldId") + ' class="image">' +
                    '<input id="'+$(uploadTarget).attr("fieldId")+'" type="hidden" name="'+$(uploadTarget).attr("fieldId")+'" value="'+res.imgUrl+'">'+
                    '<img src="' + res.imgUrl + '" />' +
                    '<div targetId=' + $(uploadTarget).attr("fieldId") + ' class="del-btn-img del-btn "' + $(uploadTarget).attr("fieldId") + '>删除</div>' +
                    '</div>';
                $(uploadTarget).removeClass("count-down").html("上传");
                $('.' + $(uploadTarget).attr('fieldId')).append(html);
                var maxImgCount = parseInt($(uploadTarget).attr('maxImgCount'));
                $(uploadTarget).attr('maxImgCount', --maxImgCount);//上传图片数量减少
            } else {
                $.prompt("<div style='text-align:center;'>" + res.info + "</div>", {
                    title: "提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if (v) {
                            $(uploadTarget).removeClass("count-down").html("上传照片");
                        }
                    }
                });
            }
        });
    });

   /*
     $(".upload-image").on("click", function () {
        fileInput.click();
     });

     fileInput.on("change", function () {
        $(".upload-image").addClass("count-down").html("上传中…");
        $(this).parent().ajaxSubmit(function (res) {
            if (res.success) {
                $(".upload-image").removeClass("count-down").html("上传照片");
                $(".image-preview").append('<div class="image"><input type="hidden" name="photo" value="' + res.imgUrl + '" title="隐藏图片地址"/><img src="' + res.imgUrl + '" /><div class="del-btn">删除</div></div>');
            } else {
                $.prompt("<div style='text-align:center;'>" + res.info + "</div>", {
                     title: "错误提示",
                     buttons: {"确定": true},
                     focus: 1,
                     submit: function (e, v) {
                         if (v) {
                            $(".upload-image").removeClass("count-down").html("上传照片");
                         }
                     },
                    useiframe: true
                 });
            }
        });
     });
     */

    // 删除照片
    $(document).on("click", ".del-btn", function () {
        var $this = $(this);
        var src = $this.prev().attr("src");
        if ($this.hasClass("del-faculty")) {
            if (confirm("确认删除这张照片")) {
                var targetId = $this.attr('targetId');
                if (targetId == 'teacherPhoto') {
                    $('#teacherPhotoUploadBtn').attr("maxImgCount", 1)
                }
                $this.parent().remove();
            }
        } else {
            $.prompt("<div style='text-align:center;'>确认删除这张照片？<br /><img style='width:143px;height:109px;margin-top:20px;' src='" + src + "' /></div>", {
                title: "删除照片",
                buttons: {"取消": false, "确定": true},
                submit: function (e, v) {
                    if(v){
                        var targetId = $this.attr('targetId');
                        if(targetId=='teacherPhoto'){
                            $('#teacherPhotoUploadBtn').attr("maxImgCount",1)
                        }
                        $this.parent().remove();
                    }
                },
                useiframe: false
            });
        }
    });

    // 选择品牌
    $('#chooseBrand').on("click", function () {
        $.prompt('<iframe src="/basic/brand/choose.vpage" width="100%" height="510px" style="border:none;"/>', {
            title: "选择品牌",
            buttons: {"关闭": false},
            position: {width: 1000}
        });
    });

    // 保存机构
    $("#add-save-btn").on("click", function () {
        if (isEmptyInput()) {
            return false;
        }
        $.prompt("<div style='text-align:center;'>是否确认保存？</div>", {
            title: "操作提示",
            buttons: {"取消":false, "确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $("#regionCode").val($("#cmbArea").val());
                    var gradeNode = $(".js-gradeItem.active"),
                        gradelist = [];
                    if(gradeNode && gradeNode.length>0){
                        $.each(gradeNode,function(i,item){
                            gradelist.push($(item).data("index"));
                        });
                    }
                    if(gradelist.length >0){
                        $("#matchGrade").val(gradelist.join(","));
                    }

                    $("#add-form").ajaxSubmit(function (res) {
                        if (res.success) {
                            $.prompt("<div style='text-align:center;'>保存成功</div>", {
                                title: "提示",
                                buttons: {"确定": true},
                                focus: 1,
                                submit: function (e, v) {
                                    if(v) {
                                        location.href='/basic/shop/index.vpage';
                                    }
                                },
                                useiframe: true
                            });
                        } else {
                            $.prompt("<div style='text-align:center;'>" + (res.info || "保存失败！") + "</div>", {
                                title: "提示",
                                buttons: {"确定": true},
                                focus: 1,
                                useiframe: true
                            });
                        }
                    });
                }
            },
            useiframe: true
        });
    });

    /*********       机构详情结束          *********/

    /*********       地图相关          *********/
    if (typeof AMap != "undefined") {
        //首先判断对象是否存在
        var longitude = $('#longitude').val();
        var latitude = $('#latitude').val();
        var map = new AMap.Map('innerMap', {
            resizeEnable: true,
            zoom: 12
        });

        var marker = '';
        if(longitude != '' && latitude != ''){
            map.setCenter([longitude,latitude]);
            marker = new AMap.Marker({map: map, position: [longitude,latitude], animation: 'AMAP_ANIMATION_DROP'});
        }

        map.on("click", function (e) {
            //预览页面禁止重新标记
            var disable = $('#innerMap').data('disable') || false;
            if (!disable) {
            } else {
                return false;
            }

            $('#longitude').removeClass('error').val(e.lnglat.getLng());
            $('#latitude').removeClass('error').val(e.lnglat.getLat());
            $('#baiduGps').attr("checked", true);

            if(marker != ''){
                marker.setMap(null);//清空已有的标记
            }
            marker = new AMap.Marker({map: map, position: e.lnglat, animation: 'AMAP_ANIMATION_DROP'});
        });

        $('.gps-input').on('change', function() {
            var longitude = $('#longitude').val();
            var latitude = $('#latitude').val();
            if(marker != ''){
                marker.setMap(null);//清空已有的标记
            }
            marker = new AMap.Marker({map: map, position: [longitude, latitude], animation: 'AMAP_ANIMATION_DROP'});
            map.setZoomAndCenter(13, [longitude, latitude]);
        });
    }
    /*********       地图结束          *********/

    /*********       师资力量相关          *********/
        // 师资力量列表
    var teacherInfoList = [];
    if(window.mizarBrandList){
        teacherInfoList = mizarBrandList;
        $('#facultyBox').html(template("facultyBox_tem", {list : teacherInfoList}));
    }

    // 新增师资力量
    $(document).on("click", "#faculty-upload-btn", function () {
        facultyDialog({}, "add", -1);
    });

    // 编辑师资力量
    $(document).on('click','.facultyEditBtn',function () {
        var ts = $(this).parent().siblings();
        var $index = $(this).parent().parent().index();
        var teacher = {
            tName:ts.find("[name=tName]").val(),
            tSeniority:ts.find("[name=tSeniority]").val(),
            tCourse:ts.find("[name=tCourse]").val(),
            tIntroduction:ts.find("[name=tIntroduction]").val(),
            tPhoto:ts.find("[name=tPhoto]").val()
        };
        // console.info(teacher);
        facultyDialog(teacher, "edit", $index);
        $('#teacherPhotoUploadBtn').attr("maxImgCount",0);
    });

    // 删除师资力量
    $(document).on('click','.facultyDeleteBtn',function () {
        $(this).parent().parent().remove();
    });

    function facultyDialog(faculty, mode, index) {
        $.prompt(template("uploaderDialog_tem", {teacher:faculty}), {
            title: "师资力量",
            buttons: {"确定": true},
            position: {width: 600},
            submit: function (e, v, m, f) {
                var teacherName = $('#teacherName').val();
                var teacherIntroduction = $('#teacherIntroduction').val();
                var teacherPhoto = $('#teacherPhoto').val();
                var teacherSeniority = $('#teacherSeniority').val();
                var teacherCourse = $('#teacherCourse').val();
                var inputs = $(".teacher.require");
                // console.info(inputs);
                var isTrue = false;
                inputs.each(function () {
                    if ($(this).val() == '') {
                        $(this).addClass("error").val("请填写" + $(this).attr("data-title"));
                        isTrue = true;
                    }
                    if ($(".teacher.require.error").length > 0) {
                        isTrue = true;
                    }
                });
                if (v) {
                    if (isTrue) {
                        return false;
                    }
                    if (teacherName == '') {
                        alert('请填写教师名称');
                        return false;
                    }
                    if (teacherSeniority == '' ||isNaN(teacherSeniority)) {
                        alert('请填写教师教龄,必须为数字');
                        return false;
                    }
                    if (teacherCourse == ''||teacherCourse.length > 5) {
                        alert('请填写教师科目,不长于5个字符');
                        return false;
                    }
                    if (teacherIntroduction == '') {
                        alert('请填写教师描述');
                        return false;
                    }
                    if (teacherPhoto == '') {
                        alert('请上传教师图片');
                        return false;
                    }
                    if (mode == 'edit') {
                        teacherInfoList[index]["teacherName"] = teacherName;
                        teacherInfoList[index]["teacherSeniority"] = teacherSeniority;
                        teacherInfoList[index]["teacherCourse"] = teacherCourse;
                        teacherInfoList[index]["introduction"] = teacherIntroduction;
                        teacherInfoList[index]["teacherPhoto"] = teacherPhoto;
                    } else {
                        teacherInfoList.push({
                            teacherName: teacherName,
                            teacherSeniority: teacherSeniority,
                            teacherCourse: teacherCourse,
                            introduction: teacherIntroduction,
                            teacherPhoto: teacherPhoto
                        });
                    }
                    $('#facultyBox').html(template("facultyBox_tem", {list : teacherInfoList}));
                }
            }
        });
    }
    /*********       师资力量结束          *********/

});