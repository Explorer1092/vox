/*
 * 家长奖励添加新任务
 */
define(["jquery","$17","knockout","jbox"], function ($,$17,knockout) {
    /****************变量声明***********/
    var studentId = Number($("#sidValue").html());
    var setMissionModalAndView = {
        addTenReward: addTenReward,
        addSelfReward: addSelfReward,
        showAddBox: knockout.observable(false),
        showTenReward: knockout.observable(false),
        missionReward: knockout.observable(""),
        contents: knockout.observable(""),
        createMissionSub: createMissionSub
    };

    /****************方法声明***********/
    function addTenReward () {
        setMissionModalAndView.showAddBox(true);
        setMissionModalAndView.showTenReward(true);
    }
    function addSelfReward () {
        setMissionModalAndView.showAddBox(true);
        setMissionModalAndView.showTenReward(false);
    }

    function createMissionSub () {
        var wish_type,mission_reward;
        if(setMissionModalAndView.showTenReward()){
            wish_type = "INTEGRAL";
            mission_reward = "10学豆";
        }else{
            wish_type = "CUSTOMIZE";
            mission_reward = setMissionModalAndView.missionReward();
        }
        var mission_task = setMissionModalAndView.contents();
        var obj = $('#customize_mission_count li span.active');

        var mission_count = obj.closest('li').data('num');

        if(!mission_task){
            $17.jqmHintBox("请填写目标");
            return false;
        }
        if(!mission_reward){
            $17.jqmHintBox("请填写奖励");
            return false;
        }

        var data = {
            mission_count: mission_count,
            mission_task: mission_task,
            mission_reward:　mission_reward,
            mission_type:　"OTHER",
            studentId: studentId,
            wish_type: wish_type
        };
        $.post("setmissions.vpage",data,function(result){
            if(result.success){
                location.href = 'index.vpage?sid='+studentId;
            }else{
                $17.jqmHintBox(result.info);
            }
        });
    }

    /****************事件交互***********/
    knockout.applyBindings(setMissionModalAndView);

    $('#customize_mission_count li').on('click',function(){
        var $this = $(this);
        $this.find('span').addClass('active');
        $this.siblings().find('span').removeClass('active');
    });



});