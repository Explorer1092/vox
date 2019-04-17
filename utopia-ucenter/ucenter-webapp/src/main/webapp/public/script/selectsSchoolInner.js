(function($){
    var currentPageData = {
        currentRegion : ko.observableArray(),
        currentSchool : ko.observableArray(),
        ktwelve : [{key: "PRIMARY_SCHOOL", text : "小学"}, {key: "JUNIOR_SCHOOL", text : "初中"}, {key: "SENIOR_SCHOOL", text : "高中"}],
        // ktwelve : [{key: "PRIMARY_SCHOOL", text : "小学"}, {key: "JUNIOR_SCHOOL", text : "初中"}],
        subject : ko.observableArray([{key: "ENGLISH", text : "英语"}, {key: "MATH", text : "数学"}, {key: "CHINESE", text : "语文"}]),
        rootRegion : [{
            key : "A-G",
            list : [
                {code : 340000, text : "安徽"},
                {code : 110000, text : "北京"},
                {code : 500000, text : "重庆"},
                {code : 350000, text : "福建"},
                {code : 620000, text : "甘肃"},
                {code : 440000, text : "广东"},
                {code : 450000, text : "广西"},
                {code : 520000, text : "贵州"}
            ]
        },{
            key : "H-K",
            list : [
                {code : 460000, text : "海南"},
                {code : 130000, text : "河北"},
                {code : 230000, text : "黑龙江"},
                {code : 410000, text : "河南"},
                {code : 420000, text : "湖北"},
                {code : 430000, text : "湖南"},
                {code : 320000, text : "江苏"},
                {code : 360000, text : "江西"},
                {code : 220000, text : "吉林"}
            ]
        },{
            key : "L-S",
            list : [
                {code : 210000, text : "辽宁"},
                {code : 150000, text : "内蒙古"},
                {code : 640000, text : "宁夏"},
                {code : 630000, text : "青海"},
                {code : 370000, text : "山东"},
                {code : 310000, text : "上海"},
                {code : 140000, text : "山西"},
                {code : 610000, text : "陕西"},
                {code : 510000, text : "四川"}
            ]
        },{
            key : "T-Z",
            list : [
                {code : 120000, text : "天津"},
                {code : 650000, text : "新疆"},
                {code : 540000, text : "西藏"},
                {code : 530000, text : "云南"},
                {code : 330000, text : "浙江"},
                {code : 820000, text : "澳门"},
                {code : 710000, text : "台湾"},
                {code : 810000, text : "香港"}
            ]
        }],
        clazzNumber:[
            {key:"C1",text:"1"},
            {key:"C2",text:"2"},
            {key:"C3",text:"3"},
            {key:"C4",text:"4"},
            {key:"C5",text:"5"},
            {key:"C6",text:"6"},
            {key:"C7",text:"7"},
            {key:"C8",text:"8"},
            {key:"C9",text:"9"},
            {key:"C10",text:"10"},
            {key:"C11",text:"11"},
            {key:"C12",text:"12"}
        ]
    };

    $.extend(registerModule, currentPageData);

    ko.applyBindings(registerModule);

    // 邀请注册修改密码
    $("#confirm_validate_code").on("click", function(){
        var newPassword = $("#newPassword");
        var newPasswordVal = newPassword.val();
        var newPasswordConfirm = $("#newPasswordConfirm");
        var newPasswordConfirmVal = newPasswordConfirm.val();

        if( $17.isBlank(newPasswordVal)){
            newPassword.addClass("w-int-error");
            newPassword.siblings(".errorMsg").show();
            return false;
        }

        if( $17.isBlank(newPasswordConfirmVal) ){
            newPasswordConfirm.addClass("w-int-error");
            newPasswordConfirm.siblings(".errorMsg").show().find(".info").html("请输入确认新密码");
            return false;
        }

        if( newPasswordVal != newPasswordConfirmVal){
            newPasswordConfirm.addClass("w-int-error");
            newPasswordConfirm.siblings(".errorMsg").show().find(".info").html("密码不一致，请填写输入!")
            return false;
        }

        $.post("/ucenter/setmypw.vpage", {
            new_password : newPasswordVal
        }, function(data){
            if(data.success){
                $(".build_head_box_isInvite").hide();
                $(".build_down_box").show();

                //新密码设置成功-下一步
                $17.voxLog({
                    module: "reg",
                    op : "guide-click-setNewPassword",
                    step : 4
                });
            }else{
                $17.alert(data.info);
            }
        });
    });
    $("#setPasswordContainer input").on("keydown", function(){
        var $this = $(this);
        $this.removeClass("w-int-error");
        $this.siblings(".errorMsg").hide();
    });

    function selectInit($this){
        var step = $this.closest(".stepGuideGoToBox");
        var code = $this.closest(".stepGuideGoToBox").attr("data-code");
        var stepGuide = $("#stepGuide");
        var content = $this.text();

        step.hide();

        if(ktwelve == "SENIOR_SCHOOL"){//高中时只允许选择数学学科
            currentPageData.subject([{key: "MATH", text : "数学"}]);
        }else{
            currentPageData.subject([{key: "ENGLISH", text : "英语"}, {key: "MATH", text : "数学"}, {key: "CHINESE", text : "语文"}]);
        }

        $(".stepGuideContent[data-code='"+ (parseInt(code)+1) +"']").addClass("active").siblings().removeClass("active");

        $(".stepGuideContent[data-code='"+ code +"']").find("span[data-content]").html("<span class='contentMe' title='"+ content +"'>"+ content +"</span>" + "<i class='build_publicimg arrow'></i>");
        $("#myClazzNumber.stepGuideContent[data-code='"+ code +"']").find("span[data-content]").html("<span class='contentMe' title='"+ content +"'>任教"+ content +"个班级</span>" + "<i class='build_publicimg arrow'></i>");

        if(code <= 2){
            if(code < 1){
                $(".stepGuideContent[data-code='1'] span[data-content]").empty();
                stepGuide.find("li[data-code='1']").addClass("active");
            }
            if( code < 2){
                $(".stepGuideContent[data-code='2'] span[data-content]").empty();
                stepGuide.find("li[data-code='2']").addClass("active");
            }
            $(".stepGuideContent[data-code='3'] span[data-content]").empty();
            stepGuide.find("li[data-code='3']").addClass("active");

            $("#confirm_keti_register").addClass("gray");
        }
        // if(code == 4){ 2018-3-23 去掉任教班级数量 http://wiki.17zuoye.net/pages/viewpage.action?pageId=37407639
        if(code == 3){
            $("#confirm_keti_register").removeClass("gray").show();
        }

        $(".stepGuideGoToBox[data-code='"+ (parseInt(code)+1) +"']").show().siblings(".stepGuideGoToBox[data-code]").hide();

        stepGuide.find("li[data-code='"+ (parseInt(code)+1) +"']").removeClass("active");
    }

    /*fix time: 2014-07-22*/
    var stepGuide = $("#stepGuide");
    var _tempArea = {
        province : $("#shengList"),
        city : $("#shiList"),
        classify : $("#quList"),
        schoolName : $("#xuexiao")
    };

    var area = _tempArea;

    //点击选择
    stepGuide.find("li[data-code]").on("click", function(){
        var $this = $(this);
        var $code = $this.data("code");
        var stepBox = $(".stepGuideGoToBox[data-code='"+ $code +"']");

        if($(this).hasClass("active")){
            return false;
        }

        stepBox.show().siblings(".stepGuideGoToBox[data-code]").hide();

        $("#confirm_keti_register").show();
    });

    /*-------------------------------------*/
    var sheng   = null, shi = null, qu = null, xuexiao = null, subject = null, ktwelve = null,clazz=null;

    //选择学段
    $("#andLearn a").on("click", function(){
        var $self = $(this);

        //统计代码
        if(!$("#andLearn").isFreezing()){
            $("#andLearn").freezing();
        }

        $("#stepInfo").hide();
        $self.addClass("active").siblings().removeClass("active");
        ktwelve = $self.attr("data-ktwelve");

        selectInit($(this));

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-ktwelve"
        });
    });

    //选择学科
    $(document).on("click", "#subject a",function(){
        var $self = $(this);

        //统计代码
        if(!$("#subject").isFreezing()){
            $("#subject").freezing();
        }

        $("#stepInfo").hide();
        $self.addClass("active").siblings().removeClass("active");
        subject = $self.attr("data-subject");

        selectInit($(this));

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-subject"
        });
    });

    //选择任教班级数
    $("#clazzNumber a").on("click", function(){
        var $self = $(this);

        //统计代码
        if(!$("#clazzNumber").isFreezing()){
            $("#clazzNumber").freezing();
        }

        $("#stepInfo").hide();
        $self.addClass("active").siblings().removeClass("active");
        clazz = $self.attr("data-clazzNumber");

        selectInit($(this));

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-clazzNumber"
        });
    });

    function myFilter(items){
        var schoolBox = $("#xuexiao li");
        if(!items.key){
            items.key = $("#shaixuan li.selected");
        }

        if(!items.region){
            items.region = $("#regionMenuBox li.selected");
        }

        var key = items.key.attr("code");
        var region = items.region.attr("data-region");

        schoolBox.hide();

        var regionKey = "";

        if(region != 0){
            regionKey = "[data-region='"+ region +"']";
        }

        if(key == 0){
            $("#xuexiao li" + regionKey).show();
        }else if(key == 1){
            $(".A, .B, .C, .D").closest("li" + regionKey).show();
        }else if(key == 2){
            $(".E, .F, .G, .H").closest("li" + regionKey).show();
        }else if(key == 3){
            $(".I, .J, .K, .L").closest("li" + regionKey).show();
        }else if(key == 4){
            $(".M, .N, .O, .P").closest("li" + regionKey).show();
        }else if(key == 5){
            $(".Q, .R, .S, .T").closest("li" + regionKey).show();
        }else if(key == 6){
            $(".U, .V, .W, .X").closest("li" + regionKey).show();
        }else if(key == 7){
            $(".Y, .Z").closest("li" + regionKey).show();
        }

        if($("#xuexiao li:visible").length <= 0){
            $("#schoolNullContent").show();
        }else{
            $("#schoolNullContent").hide();
        }
    }

    //点击省事件
    $("#shengList a").live("click", function(){
        var $this = $(this);

        //统计代码
        if(!$("#shengList").isFreezing()){
            $("#shengList").freezing();
        }

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-rootRegion"
        });

        $this.parents("#shengList").find("a").removeClass("active");
        $this.addClass("active");
        sheng = $this.attr("code");
        shi     = null;
        qu      = null;
        xuexiao = null;
        area = _tempArea;

        $(".schoolType:visible").addClass("active");
        $("#shaixuan li").removeClass("selected").first().addClass("selected");

        if(sheng=="110000" || sheng == "120000" || sheng == "310000" || sheng=="500000"){
            getNodesCode({
                link : "/map/nodes.vpage?id=" + sheng,
                box : area.classify,
                hasGetSchool : true
            }, function(){
                selectInit($this);
            });

            $("#shiListBox").hide().find("#shiList").html("");
        }else{
            getNodesCode({
                link : "/map/nodes.vpage?id=" + sheng,
                box : area.city
            }, function(){
                $("#shiListBox").show();
            });
        }
    });

    function getNodesCode(items, callback){
        items.box.html("<div style='text-align: center; color: #999; padding: 0 0 10px;'>正在努力加载中，请稍等...</div>");

        $.getJSON(items.link, function(data){
            var _htmlData = "";

            if(data.length > 0){
                $.each(data, function(){
                    _htmlData += '<a code="' + this.id + '" href="javascript:void(0);" title="'+ this.text +'">' + this.text + '</a>';
                });
            }else{
                _htmlData = "<div style='padding: 0 0 10px;'>暂不支持此地区 ，作业君正在努力开拓中</div>"
            }

            items.box.html(_htmlData);

            //开启查询学校
            if(items.hasGetSchool){
                var $cityCode = [];
                for(var i = 0; i < data.length; i++){
                    $cityCode.push(data[i].id);
                }

                qu = $cityCode.join();
                currentPageData.currentRegion(data);

                $("#shaixuan li").removeClass("selected").first().addClass("selected");
                $("#regionMenuBox li").removeClass("selected").first().addClass("selected");
                area.schoolName.html("<div style='padding: 40px 0; text-align: center;'>学校加载中...</div>");
                $.getJSON('/school/areaschoolrs.vpage?regions=' + qu + '&level=' + ktwelve, function(data){
                    if(data.rows.length > 0){
                        var _html = "<ul>";
                        $.each(data.rows, function(){
                            _html += '<li data-id="' + this.id + '" data-region="' + this.region + '"><a class="school_item ';
                            for(var _i = 0, _l = this.letters.length; _i < _l; _i++){
                                _html += this.letters[_i] + " ";
                            }

                            _html += '" href="javascript:void(0);" title="' + this.name + '">' + this.name + '</a></li>';
                        });
                        _html += "</ul><div style='padding: 40px 0; text-align: center;' id='schoolNullContent'>暂无相关数据。</div>"
                        area.schoolName.html(_html);
                    }else{
                        area.schoolName.html("<div style='padding: 40px 0; text-align: center;'>暂不支持此地区 ，作业君正在努力开拓中。</div>");
                    }

                    if(data.rows.length > 24){
                        $(".build_info_box").css({height : "auto"});
                    }

                    //search school
                    var recordingSearchCount = 0;
                    $('#searchSchool').fastLiveFilter('#xuexiao ul', {
                        filter: function(){
                            var lis = $("#xuexiao li:visible"),$region = $("#regionMenuBox li.selected"),regionVal = (+$region.attr("data-region") || 0);
                            if(lis.length > 0 && regionVal > 0){
                                for(var m = 0,mLen = lis.length; m < mLen; m++){
                                    $(lis[m]).attr("data-region") != regionVal && $(lis[m]).hide();
                                }
                            }
                        },
                        callback: function() {
                            if($17.isBlank($("#searchSchool").val())){
                                $("#searchSchool").siblings("label").show();
                                myFilter({});
                            }else{
                                $("#searchSchool").siblings("label").hide();

                                if(recordingSearchCount < 1){
                                    $17.voxLog({
                                        module : "newTeacherRegStep",
                                        op : "create-searchSchool"
                                    });

                                    recordingSearchCount += 1;
                                }

                                if($("#xuexiao li:visible").length <= 0){
                                    $("#schoolNullContent").show();
                                }else{
                                    $("#schoolNullContent").hide();
                                }
                            }
                        }
                    });
                });
            }

            if(callback){
                callback();
            }
        });
    }

    //点击市事件
    $("#shiList a").live("click", function(){
        var $this = $(this);

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-cityCode"
        });

        //条件不是自定义添加学校
        $this.radioClass("active");
        shi = $this.attr("code");
        qu      = null;
        xuexiao = null;
        area = _tempArea;

        getNodesCode({
            link : "/map/nodes.vpage?id=" + shi,
            box : area.classify,
            hasGetSchool : true
        }, function(){
            selectInit($this);
        });
    });

    //学校筛选
    $("#shaixuan li").on("click", function(){
        var $self = $(this);
        var n = $self.attr("code");

        $("#searchSchool").val("").siblings("label").show();

        $self.radioClass("selected");
        myFilter($self);
    });

    //地区筛选
    $(document).on("click", "#regionMenuBox li", function(){
        var $self = $(this);
        var $region = $self.attr("data-region");

        $("#searchSchool").val("").siblings("label").show();

        $self.radioClass("selected");

        myFilter({
            key : $("#shaixuan li.selected"),
            region : $self
        });
    });

    //学校事件
    $("#xuexiao li").live("click", function(){
        //统计代码
        if(!$("#xuexiao").isFreezing()){
            $("#xuexiao").freezing();
        }

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-clickSchool"
        });

        var $self = $(this);
        $("#xuexiao li").removeClass("selected");

        $self.closest("li").addClass("selected");
        xuexiao = $self.attr("data-id");

        selectInit($(this));
    });

    //提交学校
    $("#confirm_keti_register").on("click", function(){
        //统计代码
        if($(this).hasClass("gray")){
            return false;
        }

        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-submitSubject"
        });

        if(!$("#confirm_keti_register").isFreezing()){
            $("#confirm_keti_register").freezing();
        }

        if($17.isBlank(subject)){
            $17.alert("请选择学科");
            return false;
        }
        if($17.isBlank(xuexiao)){
            $17.alert("请选择学校");
            return false;
        }

        $.get("/teacher/guide/selectschoolsubject.vpage", {
            schoolId : xuexiao,
            subject : subject,
            ktwelve : ktwelve
            // actualTeachClazzCount :clazz    <#--2018-3-23 去掉任教班级数量 http://wiki.17zuoye.net/pages/viewpage.action?pageId=37407639-->
        }, function(data){
            if(data.success){
                setTimeout(function(){
                    if (data.jumpHttpsUrl) {
                        location.href = data.jumpHttpsUrl;
                    } else {
                        location.href = "/teacher/systemclazz/clazzindex.vpage?step=showtip";
                    }
                }, 200);
            }else{
                $17.alert(data.info,function(){
                    if(!$17.isBlank(subject) && subject == "CHINESE"){
                        setTimeout(function(){ location.href = "/ucenter/logout.vpage"; }, 200);
                    }
                });
            }
        });
    });

    $17.voxLog({
        module : "newTeacherRegStep",
        op : "create-load"
    });

    //是否为移动端
    var isMobile = {
        Android: function() {
            return navigator.userAgent.match(/Android/i) ? true : false;
        },
        BlackBerry: function() {
            return navigator.userAgent.match(/BlackBerry/i) ? true : false;
        },
        iOS: function() {
            return navigator.userAgent.match(/iPhone|iPad|iPod/i) ? true : false;
        },
        Windows: function() {
            return navigator.userAgent.match(/IEMobile/i) ? true : false;
        },
        any: function() {
            return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Windows());
        }
    };
})($);