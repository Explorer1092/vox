<#assign subjectFlag = studentList??/>
<#if subjectFlag>
    <#import "../../layout/project.module.ftl" as temp />
<#else>
    <#import "../../layout/project.module.student.ftl" as temp />
</#if>
<@temp.page title="世界数学趣味挑战赛">
<@app.css href="public/skin/project/funchallenge/skin.css" />
<div class="main">
    <!--head-->
    <div class="head">
        <div class="inner"></div>
    </div>
    <!--content-->
    <div class="content">
        <div class="inner"></div>
    </div>
    <!--luck-draw-content-->
    <div class="luck-draw-content">
        <div class="inner"></div>
    </div>
    <!--enlarge-content-->
    <div class="enlarge-content">
        <div class="inner">
            <!--点击放大box-->
            <div class="big-box js-challenge-demo"></div>
            <!--报名参赛-->
            <a class="sign-up-but js-click-competed ${(isSigned)?string("sign-up-but-disabled", "")}" data-subject="${(subjectFlag)?string("teacher", "student")}" href="javascript:void (0);">报名参赛</a>
        </div>
    </div>
    <!--myTeam-content-->
    <#if studentList?? && studentList?size gt 0>
    <div class="myTeam-content">
        <div class="inner">
            <!--我的团队-->
            <div class="myTeam-list-content">
                <span class="title">我的团队</span>
                <div class="my-l">
                    <ul>
                        <#list studentList as sl>
                            <li data-index="${sl_index}" data-clazzid="${sl.CLAZZ_ID}" class="${(sl_index == 0)?string("active", "")} js-select-student">${sl.CLAZZ_NAME}</li>
                        </#list>
                    </ul>
                </div>
            </div>
            <!--tips-->
            <div class="my-team-listDetail-box">
                <#list studentList as sl>
                    <div class="my-t js-student-item" data-index="${sl_index}" style="display: ${(sl_index == 0)?string("block", "none")};">
                        <#if sl.STUDENTLIST?size gt 0>
                        <ul>
                            <#list sl.STUDENTLIST as student>
                                <li>
                                    <div class="actor">
                                        <img src="${(student.IMG_URL?has_content)?string("${student.IMG_URL}", "<@app.avatar href=''/>")}"/>
                                        <span class="name">${student.STUDENT_NAME}</span>
                                    </div>
                                </li>
                            </#list>
                        </ul>
                        <#else>
                            <div style="padding:100px 30px; text-align: center; font-size: 18px;">${sl.CLAZZ_NAME}还没有学生加入</div>
                        </#if>
                    </div>
                </#list>
            </div>
        </div>
    </div>
    </#if>
</div>
<script type="text/html" id="T:团体老师奖状">
    <div class="math-activity-pop" id="challengePopupBox">
        <span class="icon-arrow pull-left prev"></span>
        <div class="math-con">
            <h3 class="js-card-title" data-title="0" style="display: block;">团体赛老师奖状</h3>
            <h3 class="js-card-title" data-title="1" style="display: none;">团体赛学生奖状</h3>
            <h3 class="js-card-title" data-title="2" style="display: none;">个人赛学生奖状</h3>
            <div class="pull-list">
                <ul style="width: 2130px;">
                    <li style="float: left;"><img src="<@app.link href='public/skin/project/funchallenge/temp-1.jpg'/>" width="710" height="515"></li>
                    <li style="float: left;"><img src="<@app.link href='public/skin/project/funchallenge/temp-2.jpg'/>" width="710" height="515"></li>
                    <li style="float: left;"><img src="<@app.link href='public/skin/project/funchallenge/temp-3.jpg'/>" width="710" height="515"></li>
                </ul>
            </div>
        </div>
        <span class="icon-arrow pull-right next"></span>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        $(document).on("click", ".js-challenge-demo", function(){
            $.prompt(template("T:团体老师奖状", {}),{
                prefix : "challenge-demo-popup",
                buttons : { },
                loaded: function(){
                    //左点击
                    var idx		= $("#challengePopupBox");
                    var index = 0;
                    idx.find(".prev, .next").on("click", function(){
                        var $this = $(this);

                        if($this.hasClass("prev-dis") || $this.hasClass("prev-dis")){
                            return false;
                        }

                        if($this.hasClass("prev")){
                            if(index > 0){
                                index--;
                            }

                            if(index == 0){
                                $this.addClass("prev-dis");
                            }
                        }

                        if($this.hasClass("next")){
                            if(index < 2){
                                index++;
                                $17.tongji("注册流程-创建班级-痛点-" + (index+1));
                            }

                            if(index == 2){
                                $this.addClass("next-dis");
                            }
                        }

                        if(index > 0){
                            $this.siblings(".prev").removeClass("prev-dis");
                        }

                        if(index < 2){
                            $this.siblings(".next").removeClass("next-dis");
                        }

                        $(".js-card-title[data-title='"+index+"']").show().siblings(".js-card-title").hide();
                        idx.find("ul").animate({ "margin-left" : "-" + (index*710) + "px" }, 200);
                    });
                },
                classes : {
                    fade: 'jqifade'
                }
            });
        });

        $(document).on("click", ".js-select-student", function(){
            var $this = $(this);
            var $thisIndex = $this.attr("data-index");
            $this.addClass("active").siblings().removeClass("active");

            $(".js-student-item[data-index='"+$thisIndex+"']").show().siblings().hide();
        });

        $(document).on("click", ".js-click-competed", function(){
            var $this = $(this);

            if($this.hasClass("sign-up-but-disabled") || $this.hasClass("dis")){
                return false;
            }

            var $thisSubject = $this.attr("data-subject");

            $this.addClass("dis");
            //老师为True
            <#if subjectFlag>
                $.prompt("<div class='w-ag-center'>请确认您的班级，报名后将不可更改</div>", {
                    title : "系统提示",
                    buttons : { "确认报名" : true },
                    position : {width: 500},
                    submit: function(e, v){
                        if(v){
                            $.post("/"+$thisSubject+"/challenge/signup.vpage", {}, function(data){
                                if(data.success){
                                    $this.addClass("sign-up-but-disabled");
                                    $17.alert("报名成功！请${(subjectFlag)?string("组织学生", "")}于9月27日参加比赛");
                                }else{
                                    $17.alert(data.info);
                                }
                                $this.removeClass("dis");
                            });
                        }
                    }
                });
            <#else>
                $.post("/"+$thisSubject+"/challenge/signup.vpage", {}, function(data){
                    if(data.success){
                        $this.addClass("sign-up-but-disabled");
                        $17.alert("报名成功！请${(subjectFlag)?string("组织学生", "")}于9月27日参加比赛");
                    }else{
                        $17.alert(data.info);
                    }
                    $this.removeClass("dis");
                });
            </#if>
        });
    });
</script>
</@temp.page>