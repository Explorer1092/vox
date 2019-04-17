<#--学生小调查POPUP-->
<script type="text/html" id="T:学生小调查POPUP">
    <div id="teachingSurveyPopup">
        <div style="color: #fc714c; padding: 10px;  text-align: center;">小调查：我的老师</div>
        <dl style="margin-bottom: 15px;">
            <dt>1、你的班主任是谁呢？</dt>
            <dd style="padding: 7px 20px;">
                <a href="javascript:void(0);" class="js-surveySelectTeacher"><i class="radios"></i>语文老师</a>
                <a href="javascript:void(0);" class="js-surveySelectTeacher"><i class="radios"></i>数学老师</a>
                <a href="javascript:void(0);" class="js-surveySelectTeacher"><i class="radios"></i>英语老师</a>
                <a href="javascript:void(0);" class="js-surveySelectTeacher"><i class="radios"></i>其他老师</a>
            </dd>
        </dl>
        <dl>
            <dt>2、你的数学和语文老师是同一位老师吗？</dt>
            <dd style="padding: 7px 20px;">
                <a href="javascript:void(0);" class="js-surveySelectSame"><i class="radios"></i>是同一位老师</a>
                <a href="javascript:void(0);" class="js-surveySelectSame"><i class="radios"></i>不是，是两位老师</a>
            </dd>
        </dl>
        <div class="alertInfoBox" style="text-align: center;clear: both; color: #f00; display: none;"></div>
    </div>
</script>
<script type="text/javascript">
    (function($){
        function getStudentSurvey(){
            $.prompt(template("T:学生小调查POPUP", {}),{
                title  : '安全提示',
                buttons : {"确定" : true},
                position : { width : 550},
                submit: function(e, v){
                    if(v){
                        if( $17.isBlank(getIntVal(".js-surveySelectTeacher")) ){
                            alertInfo("请选择你的班主任是谁", "#teachingSurveyPopup .alertInfoBox");
                            return false;
                        }else if( $17.isBlank(getIntVal(".js-surveySelectSame")) ){
                            alertInfo("请选择你的数学和语文老师是同一位老师吗", "#teachingSurveyPopup .alertInfoBox");
                            return false;
                        }

                        $17.voxLog({
                            app : "student",
                            module : "teachingSurveyPopup",
                            op : "submit",
                            headteacher : getIntVal(".js-surveySelectTeacher"),
                            sameteacher : getIntVal(".js-surveySelectSame")
                        }, "student");

                        $17.setCookieOneDay("teachst", "35", 35);
                    }
                }
            });

            $(document).on("click", ".js-surveySelectTeacher, .js-surveySelectSame", function(){
                var $that = $(this);

                $that.siblings().removeClass("active").find(".radios").removeClass("radios_active");
                $that.addClass("active").find(".radios").addClass("radios_active");
            });

            function alertInfo(msg, eleName){
                $(eleName).text(msg).slideDown();

                setTimeout(function(){
                    $(eleName).slideUp(function(){
                        $(eleName).text("");
                    });
                }, 3000);
            }

            function getIntVal(name){
                return $(name + ".active").text();
            }
        }

        $.extend($, {
            getStudentSurvey : getStudentSurvey
        });
    })($);
</script>