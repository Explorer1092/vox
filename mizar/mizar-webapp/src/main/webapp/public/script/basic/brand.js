/*-----------品牌相关-----------*/
define(["jquery","commonJs","prompt", "datetimepicker", "paginator", "jqform", "template"], function ($,mz) {

    /*********       品牌列表相关          *********/
    // 时间控件
    var establishment = $('#establishment');
    establishment.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd hh:ii:ss',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        minView:0,
        changeMonth: false,
        changeYear: false,
        autoclose:true
    });

    // 查询
    $("#query-btn").on("click", function () {
        $("#pageIndex").val(0);
        $("#filter-form").submit();
    });

    // 分页插件
    mz.showPager($('#paginator'));

    // 选择品牌
    $(document).on("click", "tr[name='recordRow']", function () {
        var brandName = $(this).attr('brandName');
        var brandId = $(this).attr('brandId');
        parent.$('#brandName').val(brandName);
        parent.$('#brandId').val(brandId);
        parent.$.prompt.close();
    });
    /*********       品牌列表结束         *********/


    /*********       品牌详情相关          *********/
    // 上传照片
    var fileInput = $("#upload-image");
    var uploadTarget = null;
    $(document).on("click", ".upload-image", function () {
        uploadTarget = this;
        var maxImgCount = parseInt($(uploadTarget).attr('maxImgCount'));
        if (maxImgCount == 0) {
            alert('已到达最大数量,不可以再上传了!');
            return;
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
                //相应div $(uploadTarget).attr('fieldId')显示
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

    // 删除照片
    $(document).on("click", ".del-btn", function () {
        var $this = $(this);
        var src = $this.prev().attr("src");
        if ($this.hasClass("del-faculty")) {
            if (confirm("确认删除这张照片")) {
                var targetId = $this.attr('targetId');
                if (targetId == 'teacherPhoto') {
                    $('#teacherPhotoUploadBtn').attr("maxImgCount", 1)
                } else if (targetId == 'brandLogo') {
                    $('#brandLogoUploadBtn').attr("maxImgCount", 1)
                }
                // alert($this.attr('targetId'));
                $this.parent().remove();
            }
        } else {
            $.prompt("<div style='text-align:center;'>确认删除这张照片？<br /><img style='width:143px;height:109px;margin-top:20px;' src='"+src+"' /></div>", {
                title: "删除照片",
                buttons: { "取消": false, "确定": true },
                submit: function( e,v ){
                    if ( v ) {
                        var targetId = $this.attr('targetId');
                        if(targetId=='teacherPhoto'){
                            $('#teacherPhotoUploadBtn').attr("maxImgCount",1)
                        }else if(targetId=='brandLogo'){
                            $('#brandLogoUploadBtn').attr("maxImgCount",1)
                        }
                        // alert($this.attr('targetId'));
                        $this.parent().remove();
                    }
                },
                useiframe:true
            });
        }
    });

    // 校验输入
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

    $(document).on("focus", ".require.error", function () {
        $(this).removeClass('error').val('');
    });

    // 保存品牌
    $("#add-save-btn").on("click", function () {
        if (isEmptyInput()) {
            return false;
        }
        $.prompt("<div style='text-align:center;'>是否确认保存？</div>", {
            title: "操作提示",
            buttons: {"取消": false, "确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    $("#add-form").ajaxSubmit(function (res) {
                        if (res.success) {
                            $.prompt("<div style='text-align:center;'>保存成功！</div>", {
                                title: "提示",
                                buttons: {"确定": true},
                                focus: 1,
                                submit: function (e, v) {
                                    if (v) {
                                        location.href = "/basic/brand/index.vpage";
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
    /*********       品牌详情结束          *********/

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