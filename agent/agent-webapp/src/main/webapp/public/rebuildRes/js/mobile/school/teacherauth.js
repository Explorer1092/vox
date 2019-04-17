    $(document).on('click','.js-teacher',function(){
        var nameId = $(this).attr('nameId');
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_VjelQG49", //打点流程模块名
            op : "o_OjDa5p23",
            userId:userId
        });
        openSecond("/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId="+nameId);
    });

    $(document).on("click",".js-togBtn",function(){
        var $this=$(this);
        $this.parent("li").siblings().removeClass("active").find(".resCom-table").hide();
        $this.next(".resCom-table").slideToggle(function(){
            $this.toggleClass("dashed");
            $this.parent("li").toggleClass("active");
        });
    });
    var setTopBar = {
        show:true,
        rightText:userName,
        rightTextColor:"ff7d5a",
        needCallBack:true
    };
    var resFn = function(){
        window.location.href = url;
    };
    setTopBarFn(setTopBar,resFn);