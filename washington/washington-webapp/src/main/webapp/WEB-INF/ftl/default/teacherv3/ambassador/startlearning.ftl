<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>一起作业，一起作业网，一起作业学生</title>
    <style>
        html, body{ margin: 0; text-align: center; background-color: #313131; height: 100%; width: 100%;}
        .step{ position: relative; width: 996px; margin: 0 auto; overflow: hidden; }
        .step-btn{ width: 130px; height: 62px; position: absolute; left: 510px; top: 8px; cursor: pointer; overflow: hidden; z-index: 180; background: url(<@app.link href="public/skin/teacherv3/images/w-point.png"/>) no-repeat -50px -50px;}
        .step-btn span{ display: block; width: 130px; height: 62px; float: left; cursor: pointer;}
        .step_1 .step-btn{ left: 510px; top: 8px; }
        .step_2 .step-btn{ left: 430px; top: 397px;}
        .step_3 .step-btn{ left: 373px; top: 637px;}
        .step_4 .step-btn{ left: 460px; top: 613px;}
        .step_5 .step-btn{ left: 493px; top: 903px;}
        .step_6 .step-btn{ left: 376px; top: 987px;}
    </style>
    <@sugar.capsule js=["jquery"] css=[] />
</head>
<body>
    <div class="step step_1" style="display: block;">
        <div class="step-btn" data-type="1"></div>
        <img src="//cdn.17zuoye.com/static/project/startlearning/v1.jpg">
    </div>
    <div class="step step_2" style="display: none;">
        <div class="step-btn" data-type="2"></div>
        <img src="//cdn.17zuoye.com/static/project/startlearning/v2.jpg">
    </div>
    <div class="step step_3" style="display: none;">
        <div class="step-btn" data-type="3"></div>
        <img src="//cdn.17zuoye.com/static/project/startlearning/v3.jpg">
    </div>
    <div class="step step_4" style="display: none;">
        <div class="step-btn" data-type="4"></div>
        <img src="//cdn.17zuoye.com/static/project/startlearning/v4.jpg">
    </div>
    <div class="step step_5" style="display: none;">
        <div class="step-btn" data-type="5"></div>
        <img src="//cdn.17zuoye.com/static/project/startlearning/v5.jpg">
    </div>
    <div class="step step_6" style="display: none;">
        <div class="step-btn" data-type="6"></div>
        <img src="//cdn.17zuoye.com/static/project/startlearning/v6.jpg">
    </div>
    <script type="text/javascript">
        $(function(){
            $(".step-btn").on("click", function(){
                var $this = $(this);
                var $dType = ($this.attr("data-type") * 1 + 1);

                if($dType > 6){
                    $.post("/ambassador/saverecord.vpage", {recordType : 3}, function(data){
                        if(data.success){
                            //成功
                            location.href = "/ambassador/academy.vpage?step=3";
                        }else{
                            location.href = "/ambassador/academy.vpage";
                        }
                    });
                }else{
                    $(".step_" + $dType).show().siblings().hide();

                    $("html, body").animate({ scrollTop: ($(".step-btn[data-type='"+ $dType +"']").offset().top) - 300 }, 200);
                }
            });
        });
    </script>
</body>
</html>