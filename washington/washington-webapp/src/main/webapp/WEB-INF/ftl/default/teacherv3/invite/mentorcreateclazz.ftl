<script id="t:创建班级" type="text/html">
    <div class="w-base-title">
        <h3>添加班级</h3>
    </div>
    <div class="w-base-container">
        <!--//start-->
        <div class="t-addclass-case">
            <dl>
                <dt>选择学制：</dt>
                <dd class="clear">
                    <% for(var i = 5; i < 7; i++){ %>
                        <p <% if(schoolLength == "P" + i){ %>class="active"<% } %>>
                            <a data-schoollength="P<%= i %>" href="javascript:void (0)"><%= i %>年制</a>
                        </p>
                    <% } %>
                </dd>
                <dt>选择年级：</dt>
                <dd class="clear">
                    <div class="w-border-list t-homeworkClass-list">
                        <ul>
                            <% if(schoolLength == "P6"){ %>
                                <% for(var i = 1; i < 7; i++){ %>
                                    <li class="v-level <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= i %>年级</li>
                                <% } %>
                            <% }else{ %>
                                <% for(var i = 1; i < 6; i++){ %>
                                    <li class="v-level <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= i %>年级</li>
                                <% } %>
                            <% } %>
                            <% if(level != null){ %>
                            <li class="v-parent pull-down" style="width:629px;">
                                <% for(var i = 1; i < 11; i++){ %>
                                    <%var tempClazzName%>
                                    <% for(var j = 0; j < clazzName.length; j++){ %>
                                        <% if(clazzName[j] == i + '班'){ %>
                                            <%tempClazzName = clazzName[j]%>
                                        <% } %>
                                    <% } %>
                                    <p style="border:none;" data-clazzname="<%= i %>班" title="可多选" class="<% if(tempClazzName == i + '班'){ %>active<% } %>">
                                        <span class="w-checkbox"></span>
                                        <span class="w-icon-md"><%= i %>班 <span class="w-red" <% if(tempClazzName != i + '班'){ %>style="display: none;"<% } %>>可多选</span></span>
                                    </p>
                                <% } %>
                                <div class="define" style="padding: 10px;">
                                    <input type="text" placeholder="其他班级名称" class="v-auto-clazzName before" value="" style="width: 90px;">
                                    <span style="position: absolute; left: 106px; _left: 100px; top: 20px;">班</span>
                                    <a class="v-auto-addClazz w-btn w-btn-mini" href="javascript:void(0);" style="padding: 10px 20px 9px; width: auto; display:none;">添加</a>
                                    <span class="info-text">如果有其他班级名称请输入</span>
                                </div>
                            </li>
                            <% } %>
                        </ul>
                    </div>
                </dd>
                <% if(clazzName.length > 0){ %>
                <dt>我的班级：
                    <div style="text-align: center; line-height: 22px;">(共<%=clazzName.length%>个班)</div>
                </dt>
                <dd class="clear thisClass">
                    <% for(var i = 0; i < clazzName.length; i++){ %>
                        <p><a data-clazzname="<%=clazzName[i]%>" href="javascript:void (0)"><i class="w-icon w-icon-32"></i><%=clazzName[i]%></a></p>
                    <% } %>
                    <div class="w-clear"></div>
                </dd>
                <%}%>
            </dl>
            <div class="t-pubfooter-btn">
                <a class="w-btn w-btn-small w-btn-green v-cancel" href="javascript:void(0);">关闭</a>
                <a class="v-next w-btn w-btn-small" href="javascript:void(0);" data-step-id="18" data-step-content="clazz-click-next">下一步</a>
            </div>
        </div>
        <!--end//-->
    </div>
</script>
<script id="t:设置学生" type="text/html">
    <div class="w-base">
        <div class="w-base-title">
            <h3>任教班级</h3>
            <div class="w-base-ext">
                <span class="w-bast-ctn">
                    <a class="v-prev w-blue" href="javascript:void (0);" onclick="$17.tongji('老师端-创建班级-方式一输入名单-修改班级');" data-step-id="26" data-step-content="clazz-click-createMethodsOne-back">修改班级</a>
                </span>
            </div>
        </div>
        <div class="w-base-container">
            <div class="w-table w-table-border-bot w-table-pad20">
                <table>
                    <thead>
                        <tr>
                            <td style="width:39%;">班级</td>
                            <td>学生数</td>
                        </tr>
                    </thead>
                    <tbody>
                        <% for(var i = 0; i < clazzName.length; i++){ %>
                        <tr class="<%if(i%2 > 0){%>odd<%}%>">
                            <td><%==clazzName[i].classLevel%>年级<%==clazzName[i].clazzName%></td>
                            <td>
                                <a class="v-minus-btn w-btn w-btn-mini<% if(minus_disabled){ %> w-btn-disabled<% } %>" data-index="<%=i%>" href="javascript:void (0)" style="width: 25px;">-</a>
                                <input class="v-student-num w-int" type="text" value="<%= clazzNum %>" data-index="<%=i%>" style="width: 50px;">
                                <a class="v-plus-btn w-btn w-btn-mini<% if(plus_disabled){ %> w-btn-disabled<% } %>" data-index="<%=i%>" href="javascript:void (0)" style="width: 25px;">+</a>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
            <div class="t-pubfooter-btn">
                <a class="w-btn w-btn-small w-btn-green v-cancel" href="javascript:void(0);">关闭</a>
                <a class="v-create-btn w-btn w-btn-small" href="javascript:void(0);" data-step-id="28" data-guide="new" data-step-content="clazz-click-createMethodsTwo-back">生成学生账号</a>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="t:添加班级成功页面">
<div class="w-base">
    <div class="w-base-title">
        <h3>提示</h3>
    </div>
    <div class="w-base-container">
    <#--//start-->
        <div style="margin: 0 auto; width:670px;">
            <p class="w-green" style="text-align: center; padding: 30px; font-size: 20px;">创建班级成功！</p>
            <div>
                <div class="t-download-step"></div>
            </div>
            <div class="w-ag-center" style="padding: 50px 0 20px;">
                <a href="javascript:void(0);" class="w-btn w-btn-green w-btn-small v-cancel">关闭</a>
                <a href="javascript:void(0);" class="w-btn w-btn-small click-down-clazz" data-teacher-mhid="<%=mhid%>">下载学生账号</a>
            </div>
        </div>
    <#--end//-->
    </div>
</div>
</script>
<div class="step-container-textInfo-box" style="display: none; z-index: 155;" data-title="立即布置作业 ，终身免费使用">
    <div class="step-container-textInfo-0 PNG_24">
        <a href="/teacher/homework/batchassignhomework.vpage?step=showtip&ref=GenerationID"></a>
    </div>
    <#if (currentTeacherDetail.subject == "ENGLISH")!false>
        <div class="step-container-textInfo-0-text PNG_24"></div>
    <#else>
        <div class="step-container-textInfo-0-text step-container-textInfo-0-text-1 PNG_24"></div>
    </#if>
</div>
<div id="Anchor" class="w-base"></div>
<#include "../block/batchAddStudentName.ftl"/>
<script type="text/javascript">
    var teacherClazzMhId = null;
    var ClazzInfo = {
        tempInfo: {
            name : "t:创建班级",
            base : {
                schoolId  : ${(currentTeacherDetail.schoolId)!0},
                schoolLength : "P6",
                level        : null,
                clazzName    : []
            }
        },
        eventConfig : {
            "[data-schoollength] -> click"     : schoolLength_click,
            "[data-level] -> click"            : level_click,
            "[data-clazzname] -> click"        : clazzName_click,
            "[data-clazzname] -> mouseenter"        : clazzName_mouseenter,
            "[data-clazzname] -> mouseleave"        : clazzName_mouseleave,
            ".v-auto-addClazz -> click"          : autoAddClazz_click,
            ".v-auto-clazzName -> focus"          : autoAddClazz_focus,
            ".v-next -> click"                  : next_button_click
        },
        setClazzName : function(clazzName){
            if($.inArray(clazzName, this.tempInfo.base.clazzName) > -1 ){
                this.tempInfo.base.clazzName.splice($.inArray(clazzName, this.tempInfo.base.clazzName), 1)
            }else{
                if(ClazzInfo.tempInfo.base.clazzName.length > 5){
                    $17.alert("一次最多添加6个班级!");
                    return false;
                }

                this.tempInfo.base.clazzName.push(clazzName);
            }
        },
        refresh : function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo.base));

            $17.delegate(this.eventConfig);
        },
        init : function(){
            this.refresh();
        }
    };

    var StudentInfo = {
        tempInfo : {
            name : "t:设置学生",
            base : {
                clazzNum    : 60,
                clazzNumMax : 80,
                clazzIdArr : [],
                clazzLevel : null,
                clazzName   : []
            }
        },
        eventConfig : {
            "input.v-student-num -> keyup"   : clazzmax_keyup,
            "input.v-student-num -> focus"   : clazzmax_focus,
            "a.v-minus-btn -> click"        : minus_click,
            "a.v-plus-btn -> click"         : plus_click,
            "a.v-prev -> click"             : prev_click,
            ".v-create-btn -> click"        : create_btn_click,
            ".v-join-clazz -> click"        : next_button_click,
            "#batch_student_name -> click"  : function(){ $('.batch_student_text').hide(); },
            ".batch_student_text -> click"  : function(){ $('.batch_student_text').hide(); }
        },
        refresh : function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo.base));

            $17.delegate(this.eventConfig);

            $("html, body").animate({ scrollTop: 0 }, 200);
        }
    };

    //学制被点
    function schoolLength_click(){
        $17.tongji("老师端-添加班级-选择学制");

        ClazzInfo.tempInfo.base.schoolLength = $(this).attr("data-schoollength") ? $(this).attr("data-schoollength") : "P6";
        teacherClazzMhId = null;
        ClazzInfo.tempInfo.base.level = null;
        ClazzInfo.tempInfo.base.clazzName = [];

        ClazzInfo.refresh();
        return false;
    }

    //年级被点
    function level_click(){
        $17.tongji("老师端-添加班级-选择年级");

        ClazzInfo.tempInfo.base.level = $(this).attr("data-level");

        if(ClazzInfo.tempInfo.base.clazzName.length > 0){
            $.prompt("一次只能添加一个年级哦！确定清空已选择班级？", {
                title : "系统提示",
                focus : 1,
                buttons : {"取消" : false, "确定" : true},
                submit : function(e, v){
                    if(v){
                        ClazzInfo.tempInfo.base.clazzName = [];
                        ClazzInfo.refresh();
                    }
                }
            });
        }else{
            ClazzInfo.refresh();
        }

        return false;
    }

    //班级名称被点
    function clazzName_click(){
        ClazzInfo.setClazzName($(this).attr("data-clazzname"));
        ClazzInfo.refresh();

        $17.tongji("老师端-添加班级-选择班级");
        return false;
    }

    //经过
    function clazzName_mouseenter(){
        var $this = $(this);

        if(!$this.hasClass("active")){
            $this.find(".w-red").show();
        }
    }

    //移出
    function clazzName_mouseleave(){
        var $this = $(this);

        if(!$this.hasClass("active")){
            $this.find(".w-red").hide();
        }
    }

    //自定义被点
    function autoAddClazz_click(){
        $17.tongji("老师端-添加班级-输入其他班级名称");
        var currentClazzName = $(this).siblings("input").val();

        if(currentClazzName == ""){
            $(this).siblings("input").addClass("w-int-error");
            return false;
        }

        ClazzInfo.setClazzName(currentClazzName + "班");
        ClazzInfo.refresh();

        return false;
    }

    //添加自定义班级获取焦点
    function autoAddClazz_focus(){
        var $this = $(this);
        $this.siblings(".info-text").hide();
        $this.siblings(".v-auto-addClazz").show();
    }

    //下一步按钮
    function next_button_click(){
        var $this = $(this);
        if($this.hasClass("w-btn-disabled")){
            return false;
        }

        if( ClazzInfo.tempInfo.base.clazzName.length < 1 ){
            $17.alert("请选择班级!");
            return false;
        }

        //直接创建
        StudentInfo.tempInfo.base.clazzName = uploadDealData(ClazzInfo.tempInfo.base.clazzName);

        StudentInfo.refresh();
        return false;
    }

    //数据结构
    function uploadDealData(data){
        var _item = [];

        for(var i = 0; i < data.length; i++){
            var _temp = {};
            _temp.addStudentType = "common";
            _temp.classLevel = ClazzInfo.tempInfo.base.level;
            _temp.clazzName = data[i];
            _temp.eduSystem = ClazzInfo.tempInfo.base.schoolLength;
            _temp.schoolId = ClazzInfo.tempInfo.base.schoolId;
            _temp.classSize = 60;
            _item.push(_temp);
        }
        return _item;
    }

    //上一步按钮
    function prev_click(){
        ClazzInfo.refresh();
        return false;
    }

    //减按钮
    function minus_click(){
        var $this = $(this);
        var $index = $this.data("index");
        var clazzNumBox = $this.siblings(".v-student-num");
        var clazzNum = parseInt(clazzNumBox.val()) - 1;

        if(clazzNum <= 1){
            clazzNumBox.val(1);
            StudentInfo.tempInfo.base.clazzName[$index].classSize = 1;
            $this.addClass("w-btn-disabled");
            return false;
        }

        clazzNumBox.val(clazzNum);
        StudentInfo.tempInfo.base.clazzName[$index].classSize = clazzNum;
        $this.siblings(".v-plus-btn").removeClass("w-btn-disabled");
        return false;
    }

    //加按钮
    function plus_click(){
        var $this = $(this);
        var $index = $this.data("index");
        var clazzNumBox = $this.siblings(".v-student-num");
        var clazzNum = parseInt(clazzNumBox.val()) + 1;

        if(clazzNum >= 90){
            clazzNumBox.val(90);
            StudentInfo.tempInfo.base.clazzName[$index].classSize = 90;
            $this.addClass("w-btn-disabled");
            return false;
        }

        clazzNumBox.val(clazzNum);
        StudentInfo.tempInfo.base.clazzName[$index].classSize = clazzNum;
        $this.siblings(".v-minus-btn").removeClass("w-btn-disabled");
        return false;
    }

    //人数输入框获得焦点
    function clazzmax_focus(){
        $(this).select();

        return false;
    }

    //人数输入框失去焦点
    function clazzmax_keyup(){
        var $this = $(this);
        var $index = $this.data("index");
        var clazzNum = $this.val();

        $this.siblings(".v-minus-btn").removeClass("w-btn-disabled");
        $this.siblings(".v-plus-btn").removeClass("w-btn-disabled");

        if(clazzNum <= 1){
            $this.on("blur", function(){
                if($(this).val() == ""){
                    $this.val(1);
                }
            });
            StudentInfo.tempInfo.base.clazzName[$index].classSize = 1;
            $this.siblings(".v-minus-btn").addClass("w-btn-disabled");
            return false;
        }

        if(clazzNum >= 90){
            $this.val(90);
            StudentInfo.tempInfo.base.clazzName[$index].classSize = 90;
            $this.siblings(".v-plus-btn").addClass("w-btn-disabled");
            return false;
        }

        if(!$17.isNumber(clazzNum)){
            $this.val(StudentInfo.tempInfo.base.clazzName[$index].classSize);
            return false;
        }

        StudentInfo.tempInfo.base.clazzName[$index].classSize = clazzNum;
        return false;
    }

    //创建班级
    function create_btn_click(){
        var $this = $(this);
        if($this.hasClass("w-btn-disabled")){
            return false;
        }

        $this.addClass("w-btn-disabled");
        App.postJSON("/teacher/clazz/createclazz.vpage", {mappers : StudentInfo.tempInfo.base.clazzName, mhid : teacherClazzMhId}, function(data){
            if(data.success){
                //已创建班级的ID
                var $clazzIds = [];
                for(var i = 0, slList = data.sl; i < slList.length; i++){
                    $clazzIds.push(slList[i].clazzId);
                }

                //创建成功
                $("#Anchor").html(template("t:添加班级成功页面", {mhid : teacherClazzMhId,clazzIds : $clazzIds.join()}));
            }else{
                $this.removeClass("w-btn-disabled");
                $17.alert(data.info);
            }
        });
    }

    $(function(){
        ClazzInfo.init();
    });
</script>