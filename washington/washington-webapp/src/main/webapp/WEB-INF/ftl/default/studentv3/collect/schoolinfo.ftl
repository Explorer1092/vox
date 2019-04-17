<#if (needCollectInfo)!false>
    <script id="t:学生学校信息收集" type="text/html">
        <style>
            .dataCollection-popUps{}
            .dataCollection-popUps h1{font-size: 16px;color:#fa7252;font-weight: normal;text-align: center;}
            .dataCollection-popUps .hoot-box p{float:left;font-size:14px;line-height: 30px;}
            .dataCollection-popUps .hoot-box .c-2{padding:0 40px 0 7px;cursor: pointer;}
            .dataCollection-popUps li{clear: both; color:#333;}
            .dataCollection-popUps .title{line-height: 45px;font-size:14px;}
            .dataCollection-popUps .title .number{border-radius: 6px;border:1px solid #ccc;width:70px;padding:8px 0;line-height: 19px;text-align: center;}
            .dataCollection-popUps .title .sub{display:inline-block;border-radius: 6px;border:1px solid #fa7252;width:70px;padding:8px 0;line-height: 19px;text-align: center;color:#fa7252;background-color: #fdf7f6;}
            .dataCollection-popUps .title .error{ border-color: #f00;}
            .dataCollection-popUps .w-checkbox{background: url(<@app.link href="public/skin/studentv3/images/publicbanner/checked-icon.png"/>) no-repeat;width: 12px;height: 12px;overflow: hidden;cursor: pointer;display: inline-block;*vertical-align: bottom;}
            .dataCollection-popUps .w-checkbox{background-position: 0 -27px;}
            .dataCollection-popUps .active .w-checkbox{background-position: 0 0;}
            .dataCollection-popUps .active .c-2{color:#57bd1b;}
        </style>
        <div class="dataCollection-popUps" id="ugcPkLeakFairylandSurvey">
            <h1>安全提示：账号异常，请验证真实信息</h1>
            <ul>
                <li>
                    <p class="title">1、你的校区有哪些年级？<span id="v-grade-error" style="color: red; display: none">年级错误</span></p>
                    <div class="hoot-box" data-grade="1">
                        <p class="c-1">
                            <span class="w-checkbox"></span>
                        </p>
                        <p class="c-2">1年级</p>
                    </div>
                    <div class="hoot-box" data-grade="2">
                        <p class="c-1">
                            <span class="w-checkbox"></span>
                        </p>
                        <p class="c-2">2年级</p>
                    </div>
                    <div class="hoot-box" data-grade="3">
                        <p class="c-1">
                            <span class="w-checkbox"></span>
                        </p>
                        <p class="c-2">3年级</p>
                    </div>
                    </br></br>
                    <div class="hoot-box" data-grade="4">
                        <p class="c-1">
                            <span class="w-checkbox"></span>
                        </p>
                        <p class="c-2">4年级</p>
                    </div>
                    <div class="hoot-box" data-grade="5">
                        <p class="c-1">
                            <span class="w-checkbox"></span>
                        </p>
                        <p class="c-2">5年级</p>
                    </div>
                    <div class="hoot-box" data-grade="6">
                        <p class="c-1">
                            <span class="w-checkbox"></span>
                        </p>
                        <p class="c-2">6年级</p>
                    </div>
                </li>
                <li>
                    <p class="title">2、学校<%=curGrade%>年级有 <input data-type="clazzNum" type="text" placeholder="请填写数字" class="number" maxlength="2"/> 个班级，是从
                        <input data-type="clazzCountStart" type="text" placeholder="请填写班名" class="number" maxlength="8"/>班到
                        <input data-type="clazzCountEnd" type="text" placeholder="请填写班名" class="number" maxlength="8"/>班。
                    </p>
                </li>
                <li>
                    <p class="title">3、你的班级里有 <input data-type="clazzStudentCount" type="text" placeholder="请填写数字" class="number" maxlength="4"/> 名同学。</p>
                </li>
            </ul>
        </div>
    </script>
</#if>
<script type="text/javascript">
    (function($){
        function UgcPopup(){
            <#if (needCollectInfo)!false>
                var clazzLevelId = "${(currentStudentDetail.clazz.getClassLevel())!}";
                var clazzId = "${(currentStudentDetail.getClazzId())!}";
                var gradeItems = [];
                $.prompt(template("t:学生学校信息收集", { curGrade : clazzLevelId}), {
                    focus: 1,
                    title: "安全提示",
                    position : { width: 550},
                    buttons: {"取消":false, "提交": true},
                    submit: function (e, v) {// 发送关联请求
                        if (v) {
                            var $tableBox = $("#ugcPkLeakFairylandSurvey");
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
                            if($inputNotFlag || gradeItems.length < 1){ return false;}

                            function callIntVal(type){
                                return $tableBox.find("input[data-type='" + type + "']").val();
                            }

                            $17.voxLog({
                                app : "student",
                                module : "ugcPkLeakFairylandSurvey",
                                op : "submit",
                                clazzId : clazzId,
                                clazzLevelId : clazzLevelId,
                                gradeItems : gradeItems.join(),
                                clazzNum : callIntVal("clazzNum"),
                                clazzCountStart : callIntVal("clazzCountStart"),
                                clazzCountEnd : callIntVal("clazzCountEnd"),
                                clazzStudentCount : callIntVal("clazzStudentCount")
                            }, "student");

                            $17.setCookieOneDay("STUGCOLD", "60", 60);
                        }
                    }
                });

                $(document).on("click", "#ugcPkLeakFairylandSurvey [data-grade]", function(){
                    var $this = $(this);

                    if( $this.hasClass("active") ){
                        $this.removeClass("active");
                        gradeItems.splice($.inArray($this.data("grade") ,gradeItems), 1);
                    }else{
                        $this.addClass("active");
                        gradeItems.push($this.data("grade"));
                    }

                    console.info(gradeItems)
                });
            </#if>
        }

        $.extend({
            UgcPopup : UgcPopup
        });
    }($));
</script>
