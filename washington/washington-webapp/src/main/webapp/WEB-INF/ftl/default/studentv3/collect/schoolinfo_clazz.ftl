<script id="t:学生学校信息收集" type="text/html">
    <style>
        .dataCollection-popUps{}
        .dataCollection-popUps h1{font-size: 16px;color:#fa7252;font-weight: normal;text-align: center; padding: 3px 0 15px;}
        .dataCollection-popUps .hoot-box p{float:left;font-size:14px;line-height: 30px;}
        .dataCollection-popUps .hoot-box .c-2{padding:0 40px 0 7px;cursor: pointer;}
        .dataCollection-popUps li{clear: both;color:#333;}
        .dataCollection-popUps .title{line-height: 45px;font-size:14px;}
        .dataCollection-popUps .title .number{border-radius: 6px;border:1px solid #ccc;width:75px;padding:5px 0;line-height: 19px;text-align: center; outline: none;}
        .dataCollection-popUps .title .error{ border-color: #f00;}
        .dataCollection-popUps .title .sub{display:inline-block;border-radius: 6px;border:1px solid #fa7252;width:70px;padding:8px 0;line-height: 19px;text-align: center;color:#fa7252;background-color: #fdf7f6;}
        .dataCollection-popUps .w-checkbox{background: url(<@app.link href="public/skin/studentv3/images/publicbanner/checked-icon.png"/>) no-repeat;width: 12px;height: 12px;overflow: hidden;cursor: pointer;display: inline-block;*vertical-align: bottom;}
        .dataCollection-popUps .w-checkbox{background-position: 0 -27px;}
        .dataCollection-popUps .active .w-checkbox{background-position: 0 0;}
        .dataCollection-popUps .active .c-2{color:#57bd1b;}
    </style>
    <div class="dataCollection-popUps" id="ugcSchoolInfoSurvey">
        <h1>调查：你选对了班级吗？</h1>
        <ul>
            <li>
                <p class="title">
                    1、学校<%=curGrade%>年级有 <input type="text" class="number" placeholder="请填写数字" data-type="clazzNum" maxlength="4"/> 个班，是从 <input type="text" class="number" placeholder="请填写班名"  data-type="clazzCountStart" maxlength="10"/> 班到 <input type="text" class="number" placeholder="请填写班名" data-type="clazzCountEnd" maxlength="10"/> 班
                </p>
            </li>
            <li>
                <p class="title">
                    2、你在<%=curGrade%>年级 <input type="text" class="number" placeholder="请填写班名" data-type="clazzName" maxlength="10"/> 班
                </p>
            </li>
            <li>
                <p class="title">
                    3、你的学校名是：<input type="text" class="number" placeholder="请填写学校名字" data-type="schoolName" style="width: 150px;" maxlength="20"/>
                </p>
            </li>
            <li>
                <p class="title">
                    4、你的英语老师是： <input type="text" class="number" placeholder="请填写老师名字" data-type="englishName" style="width: 90px;" maxlength="10"/>，数学老师是：<input type="text" class="number" placeholder="请填写老师名字" data-type="mathName" style="width: 90px;" maxlength="10"/>
                </p>
            </li>
        </ul>
    </div>
</script>
<script type="text/javascript">
    (function($){
        function UgcClazzPopup(){
            var clazzLevelId = "${(currentStudentDetail.clazz.getClassLevel())!}";
            var clazzId = "${(currentStudentDetail.getClazzId())!}";
            $.prompt(template("t:学生学校信息收集", { curGrade : clazzLevelId}), {
                focus: 1,
                title: "安全提示",
                position : { width: 550},
                buttons: {"取消":false, "提交": true},
                submit: function (e, v) {// 发送关联请求
                    if (v) {
                        var $tableBox = $("#ugcSchoolInfoSurvey");
                        var $inputNotFlag = false;

                        $tableBox.find("input").each(function(){
                            var $int = $(this);
                            if( $17.isBlank($int.val()) ){
                                $inputNotFlag = true;
                                $int.focus().addClass("error");
                                return false;
                            }
                        });

                        $tableBox.find("input").on("keyup", function(){
                            $(this).removeClass("error");
                        });

                        //验证是否为空
                        if($inputNotFlag){ return false;}

                        function callIntVal(type){
                            return $tableBox.find("input[data-type='" + type + "']").val();
                        }

                        $17.voxLog({
                            app : "student",
                            module : "ugcSchoolInfoSurvey",
                            op : "popup",
                            clazzId : clazzId,
                            clazzLevelId : clazzLevelId,
                            clazzNum : callIntVal("clazzNum"),
                            clazzName : callIntVal("clazzName"),
                            clazzCountStart : callIntVal("clazzCountStart"),
                            clazzCountEnd : callIntVal("clazzCountEnd"),
                            schoolName : callIntVal("schoolName"),
                            englishName : callIntVal("englishName"),
                            mathName : callIntVal("mathName")
                        }, "student");

                        $17.setCookieOneDay("STUGCTJ", "60", 60);
                    }
                }
            });
        }

        $.extend({
            UgcClazzPopup : UgcClazzPopup
        });
    }($));
</script>
