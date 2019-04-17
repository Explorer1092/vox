/**
 * Created by fengwei on 2016/12/14.
 */
define(["jquery","prompt","paginator",'template','datetimepicker','jqform'],function ($) {

    var initUE = function(id,url){
        if(typeof UE !='undefined'){
            UE.getEditor(id, {
                serverUrl: url,
                autoHeightEnabled: false,
                zIndex: 0,
                fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
                toolbars: [[
                    'fullscreen', 'source', '|', 'undo', 'redo', '|',
                    'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                    'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                    'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                    'directionalityltr', 'directionalityrtl', 'indent', '|',
                    'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                    'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                    'simpleupload', 'pagebreak', 'template', 'background', '|',
                    'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
                ]]
            });
        }
    };

    //初始化开始结束时间控件
    var initTimePlugin = function(start,end){
        $("#"+start).datetimepicker({
            language:  'zh-CN',
            format: 'yyyy-mm-dd hh:ii',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            // minView:3,
            autoclose:true
        });

        $("#"+end).datetimepicker({
            language:  'zh-CN',
            format: 'yyyy-mm-dd hh:ii',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            // minView:3,
            autoclose:true
        })
    };

    initTimePlugin('startTime','endTime');
    initUE('courseDetail','/common/ueditorcontroller.vpage');

    //上传文件
    $(document).on('change','.js-classPic',function(){
        var file = this.files[0];
        var type = $(this).data("type");
        if(file){
            var fileSize = file.size,fileType = file.type;
            if(fileType.indexOf('image') != -1){
                if(fileSize < 5*1024*1024){
                    var postData = new FormData();
                    postData.append('file', file);
                    $.ajax({
                        url: "/common/uploadfile.vpage",
                        type: "POST",
                        data: postData,
                        processData: false,
                        contentType: false,
                        success: function (res) {
                            if(res.success){
                                var trail = '';
                                // if(res.fileName.indexOf('oss-image') != -1){
                                //     trail = '@1e_1c_0o_0l_720h_300w_80q'
                                // }
                                $('#imgDiv-' + type).html('<img src="'+res.fileName+trail+'" width="720px" height="300px">');
                                $("#classPic-" + type).val(res.fileName);
                            }else{
                                alert(res.info);
                            }
                        },
                        error: function (e) {
                            console.log(e);
                        }
                    });
                }else{
                    $.prompt('请上传小于5M的图片',{
                        title: "温馨提示",
                        buttons: {"确定": true},
                        focus: 1,
                        submit: function (e, v) {
                            if (v) {}
                        }
                    });
                }
            }else{
                $.prompt('请上传图片文件',{
                    title: "温馨提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function (e, v) {
                        if (v) {}
                    }
                });
            }
        }
    });

    //提交课时
    $(document).on("click","#submitBtn",function(){
        //时间插件精确值未到秒，补全
        var changList = ["#startTime","#endTime"];
        for (var i=0;i<changList.length;i++){
            if($(changList[i]).val().indexOf(":00") != 16) {
                $(changList[i]).val($(changList[i]).val() + ":00");
            }
        }

        if($('js-smsBtn.active') && $('js-smsBtn.active').length > 0){
            var index = $($('js-smsBtn.active')[0]).data("index");
            if(index){
                $('#smsNotify').val(true);
            }else{
                $('#smsNotify').val(false);
            }
        }

        $("#periodForm").ajaxSubmit(function(res){
            if(res.success){
                $.prompt("<div style='text-align:center;'>"+ (res.info||"保存成功!") + "</div>", {
                    title: "操作提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true,
                    submit: function (e, v) {
                        if (v) {
                            location.href = "/course/manage/detail.vpage?cid="+courseId;
                        }
                    }
                });
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"保存失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    $(document).on("click",".js-smsBtn",function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        var index = $(this).data("index");
        if(index){
            $('#smsNotify').val(true);
        }else{
            $('#smsNotify').val(false);
        }
    });

    $(document).on("click","#talkFunBtn",function(){
        $.prompt("<div style='text-align:center;'><textarea style='height: 310px; width: 400px; resize: none; border:none;'>"+$('#talkFunVal').html()+"</textarea></div>", {
            title: "欢拓课程信息",
            buttons: { "确定": true },
            focus : 1,
            useiframe:true
        });
    });

});