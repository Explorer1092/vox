<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='接口测试' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .uploadBox{ height: 100px;}
    .uploadBox .addBox{cursor: pointer; width: 170px; height: 124px;border: 1px solid #ccc; text-align: center; color: #ccc; float: left; margin-right: 20px;}
    .uploadBox .addBox .addIcon{ vertical-align: middle; display: inline-block; font-size: 80px;line-height: 95px;}
    .uploadBox .addBox img{ width: 170px; height: 124px;}
</style>

<div class="span9">
    <fieldset>
        <legend>接口测试</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form id="zhaoying_test" class="well form-horizontal" action="zhaoying_test.vpage" style="background-color: #fff;" method="post">
                <fieldset>
                    <div class="control-group">
                        <div class="controls">
                            <label for="name">
                                APP
                                <select id="app" name="app">
                                    <option value="17Parent">家长端</option>
                                    <option value="17Student">学生端</option>
                                    <option value="17Teacher">老师端</option>
                                </select>
                            </label>
                            <label for="name">
                                <input type="hidden" name="paramCount" id="paramCount" value="0">
                            </label>
                            <label for="name">
                                接口地址默认Get
                            </label>
                            <label for="name">
                                <input type="text" name="url" id="url" value=""><input type="checkbox" name="post" id="post"  value="0"/>Post
                            </label>
                            <label for="name">
                                参数列表
                            </label>
                            <label for="name" id="params">

                            </label>
                            <label for="name">
                                <input type="button" name="addParam" id="addParam" value="增加一个参数">
                            </label>
                            <label for="name">
                                <input type="button" name="test" id="test" value="测试">
                            </label>
                        </div>
                    </div>
                </fieldset>
            </form>
            <label for="name">
                返回结果:
                <div id="response" value="" readonly></div>
            </label>

        </div>
    </div>
</div>

<script type="text/javascript">

    $("#post").on("click",function(){
        if(this.checked == true){
            this.value=1;
        }else{
            this.value=0;
        }
    });

    $(document).ready(function() {
        var MaxInputs       = 10; //maximum input boxes allowed
        var params   = $("#params"); //Input boxes wrapper ID
        var addParam       = $("#addParam"); //Add button ID

        var x = params.length; //initlal text box count
        var FieldCount=$("#paramCount").val(); //to keep track of text box added

        $(addParam).click(function (e)  //on add input button click
        {
            if(x <= MaxInputs) //max input box allowed
            {
                FieldCount++; //text box added increment
                //add input box
                $(params).append('<div><input type="text" name="param_'+ FieldCount +'" id="param_'+ FieldCount +'" value=""/><input type="text" name="value_'+ FieldCount +'" id="value_'+ FieldCount +'" value=""/><a href="#" class="removeclass">删除</a></div>');
                x++; //text box increment
                $("#paramCount").val(FieldCount);
            }
            return false;
        });

        $("body").on("click",".removeclass", function(e){ //user click on remove text
            if( x > 1 ) {
                $(this).parent('div').remove(); //remove text box
                x--; //decrement textbox
                FieldCount--;
                $("#paramCount").val(FieldCount);
            }
            return false;
        })

    });

    //保存
    $('#test').on('click', function () {
        $.post('zhaoying_test.vpage', $("#zhaoying_test").serialize(), function (data) {
            if(data.success){
//                $("#response").html("");
                $("#response").append(data.resStr);
            }else{
                alert(data.info);
            }
        });
    });

</script>
</@layout_default.page>