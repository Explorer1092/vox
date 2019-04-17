/**
 * Created by free on 2016/12/6.
 */
define(["jquery","prompt","jqform"],function($){
    var filesList = [];

    //附件
    $(document).on("change",".js-file",function(){
        var $this = $(this);
        var file = this.files[0];
        if(file){
            var fileSize = file.size;
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
                            $this.attr("data_val",res.fileName);
                            $this.parent('div').append('<span class="alertInfo js-alertInfo">上传成功</span>');
                            setTimeout(function(){
                                $('.js-alertInfo').remove();
                            },3000)
                        }else{
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        console.log(e);
                    }
                });
            }else{
                alert('请上传小于5M的附件');
            }
        }
    });

    //更多附件
    $(document).on("click",".js-moreFileBtn",function(){
        var tempHtml='',
            fileNum = $('#fileList').find('input').length,
            nextName = 'file'+(fileNum +1);

        if(fileNum < 3){
            tempHtml = '<div class="js-fileCon"><input type="file" class="js-file" name="'+nextName+'" id="'+nextName+'"> <span class="grade_btn js-moreFileBtn" style="width: 26px;">+</span><span class="grade_btn js-removeFileBtn" style="width: 26px;">-</span></div>';
        }

        if(fileNum < 3){
            $('#fileList').append(tempHtml);
        }else{
            $.prompt("<div style='text-align:center;'>最多支持3个附件</div>", {
                title: "提示",
                buttons: { "确定": true }
            });
        }
    });

    $(document).on("click",".js-removeFileBtn",function(){
        var fileN = $('#fileList').find('input').length;
        if(fileN == 1){
            $(".js-file")[0].value = '';
            $("#file_list_val").val('');
        }else{
            $(this).parent("div").remove();
            refreshFiles();
        }
    });


    $(document).on("click", ".js-gradeItem", function () {
        $(this).toggleClass("active");
    });

    var refreshFiles = function(){
        var filesNode = $(".js-file");
        if(filesNode && filesNode.length > 0){
            var filesObj = [];
            for(var i=0;i<filesNode.length;i++){
                filesNode[i].name = 'file'+(i+1);
                filesNode[i].id = 'file'+(i+1);
                if($(filesNode[i]).attr('data_val')){
                    filesObj.push(
                        {
                            "name":"附件"+(i+1),
                            "url":$(filesNode[i]).attr('data_val')
                        }
                    );
                }
            }
            $("#file_list_val").val(JSON.stringify(filesObj));
        }else{
            $("#file_list_val").val('');
        }
    };

    //确定发送
    $(document).on("click",".#add-save-btn",function(){
        var gradeNode = $(".js-gradeItem.active"),
            gradelist = [];
        if(gradeNode && gradeNode.length>0){
            $.each(gradeNode,function(i,item){
                gradelist.push($(item).data("uid"));
            });
        }
        if(gradelist.length >0){
            $("#users").val(gradelist.join(","));
        }

        refreshFiles();

        $("#msgForm").ajaxSubmit(function(res){
            if(res.success){
                $.prompt("<div style='text-align:center;'>发送成功</div>", {
                    title: "提示",
                    buttons: { "确定": true },
                    submit:function(e,v){
                        if(v){
                            location.href = "/basic/notify/index.vpage";
                        }
                    }
                });
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"发送失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

});