define(["jquery","$17","prompt"],function ($,$17) {

    var optionList = $(".schoolListLink").find("option");
    for (var i=0;i<optionList.length;i++){
        if (optionList.eq(i).val() == getQuery("schoolId")){
            optionList.eq(i).attr("selected",true);
        }
    }

    $(".logout").on("click",function () {
        $.prompt("<p style='text-align: center'>确定退出登录？</p>",{
            title  : "提示",
            focus: 1,
            buttons:{"取消":false,"确定":true},
            submit : function(e,v){
                if(v){
                    location.href = "/auth/logout.vpage";
                }
            }
        });
    });

    $(".schoolListLink").on("change",function () {
        var link = $(this).find("option:selected").val();
        location.href = "/activity/tangram/studentlist.vpage?schoolId=" + link;
    });

    $(".gradeLabel").on("click","span",function () {
        $(this).addClass("active").siblings().removeClass("active");
    });

    $(".JS-comment").on("keyup",function () {
        var number = $(this).val().length;
        $(".JS-isNumber").val(number);
    });

    var nextStudentID = "", _index;
    $(".submitBtn").on("click",function () {
       var studentId = getQuery("student");
       var score = "";
       if ($(".gradeLabel").find(".active").length == 1){
           score = $(".gradeLabel").find(".active").text();
       }
       var comment = $(".JS-comment").val();

        if (score == ""){
            $17.alert("<p style='text-align:center;'>请对该学生评级！</p>");
            return ;
        }
        if ($.trim(comment) == ""){
            $17.alert("<p style='text-align:center;'>请对该学生文字评价！</p>");
            return ;
        }

        $.post('/activity/tangram/judge.vpage',{studentId:studentId,score:score,comment:comment}, function (data) {
            if (data.success) {
                if (data.next > 0){
                    nextStudentID = data.next;
                    $(".JS-success").show();
                }else{
                    $(".JS-successEnd").show();
                }
            } else {
                $17.alert("<p style='text-align:center;'>"+data.info+"</p>");
            }
        });
    });

    if (location.href.indexOf("tangram/student.vpage")>-1){
        if (picBox.length == 1){
            $(".arrowL,.arrowR").addClass("displayNone");
        }
        $(".JS-next").on("click",function () {
            location.href = "/activity/tangram/student.vpage?student="+nextStudentID;
        });

        $(".JS-goList").on("click",function () {
            location.href = "/activity/tangram/studentlist.vpage?schoolId="+schoolId;
        });

        $(".p-close").on("click",function () {
            $(".commonPopup").hide();
        });

        $(".picBox").on("click",".JS-pic",function (event) {
            event.stopPropagation();
            _index = $(this).index();
            $(".JS-reviewImg").attr("src", picBox[_index]);
            $(".picReview-pop").show();
        });
        
        var classList = "scrollWrap";
        $(".arrowL").on("click",function () {
            if (_index > 0){
                _index = _index - 1;
            }else{
                _index = picBox.length-1;
            }
            $(".JS-reviewImg").attr("src", picBox[_index]);
            $(".scrollWrap").attr("class","scrollWrap");
            classList = "scrollWrap";
        });

        $(".arrowR").on("click",function () {
            if (_index < picBox.length-1){
                _index = _index + 1;
            }else{
                _index = 0;
            }
            $(".JS-reviewImg").attr("src", picBox[_index]);
            $(".scrollWrap").attr("class","scrollWrap");
            classList = "scrollWrap";
        });

        $(".arrowB").on("click",function () {
            if (classList == "scrollWrap rotation90"){
                $(".scrollWrap").attr("class","scrollWrap");
            }else if (classList == "scrollWrap rotation180"){
                $(".scrollWrap").attr("class","scrollWrap rotation90");
            }else if (classList == "scrollWrap rotation270"){
                $(".scrollWrap").attr("class","scrollWrap rotation180");
            }else{
                $(".scrollWrap").attr("class","scrollWrap rotation270");
            }
            classList = $(".scrollWrap").attr("class");
        });

        var lenNow = $(".JS-comment").val().length;
        $(".JS-isNumber").text(lenNow);

        $(".JS-comment").on("keyup",function () {
            var number = $(this).val().length;
            $(".JS-isNumber").text(number);
        });

        $(document).on("click",function () {
            $(".picReview-pop").hide();
            $(".scrollWrap").attr("class","scrollWrap");
            classList = "scrollWrap";
        }).on("click",".reviewInner",function (event) {
            event.stopPropagation();
        });
    }

    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

});