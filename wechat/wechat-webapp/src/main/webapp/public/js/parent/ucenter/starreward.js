/*
 * 领星星学豆
 */
define(["jquery", "$17", 'knockout', 'userpopup'], function ($, $17, knockout, userpopup) {
    /****************变量声明***********/
    var starModalAndView = {
        currentTab: knockout.observable("month"),
        showMonthTab: showMonthTab,
        showTermTab: showTermTab,
        hasReceived: knockout.observable(false),
        hasReceivedLastTerm: knockout.observable(false),
        sid: knockout.observable(0),
        integral: knockout.observable(0),
        integralLastTerm: knockout.observable(0),
        mothDataAry: knockout.observableArray([]),
        termDataAry: knockout.observableArray([]),
        showMonthMore: showMonthMore,
        showTermMore: showTermMore,
        monthShowIndex: knockout.observable(1),
        termShowIndex: knockout.observable(1),
        getRewardMonth: getRewardMonth,
        getRewardTerm: getRewardTerm
    };

    /****************方法声明***********/
    function showMonthTab() {
        starModalAndView.currentTab("month");
    }

    function showTermTab() {
        starModalAndView.currentTab("term");
    }

    function showMonthMore() {
        starModalAndView.monthShowIndex(starModalAndView.monthShowIndex() + 1);
    }

    function showTermMore() {
        starModalAndView.termShowIndex(starModalAndView.termShowIndex() + 1);
    }

    function getRewardMonth() {
        $.post('reward.vpage', {sid: starModalAndView.sid(), rewardType: 0}, function (data) {
            if (data.success) {
                starModalAndView.hasReceived(true);
                $17.jqmHintBox("领取成功，请登录电脑查看");
            } else {
                $17.jqmHintBox(data.info);
            }
        });
    }

    function getRewardTerm() {
        $.post('reward.vpage', {sid: starModalAndView.sid(), rewardType: 1}, function (data) {
            if (data.success) {
                starModalAndView.hasReceivedLastTerm(true);
                $17.jqmHintBox("领取成功，请登录电脑查看");
            } else {
                $17.jqmHintBox(data.info);
            }
        });
    }

    /****************事件交互***********/
    userpopup.selectStudent("starreward");
    knockout.applyBindings(starModalAndView);

    $('#strategy_btn').jBox('Modal', {
        trigger: 'click',
        title: '星星攻略',
        position: {
            x: 'center',
            y: 'center'
        },
        content: $('#strategy_box'),
        closeButton: 'title',
        animation: 'pulse'
    });

    $('#reward_rule_btn').jBox('Modal', {
        trigger: 'click',
        title: '奖励规则',
        position: {
            x: 'center',
            y: 'center'
        },
        content: $('#reward_rule_box'),
        closeButton: 'title',
        animation: 'pulse'
    });

    return {
        loadMessageById: function (sid) {
            //alert(sid);
            starModalAndView.sid(sid);
            var data = {sid: sid};
            //初始化数据
            starModalAndView.mothDataAry([]);
            starModalAndView.termDataAry([]);
            starModalAndView.monthShowIndex(1);
            starModalAndView.termShowIndex(1);

            $.post("starreward/ranklist.vpage", data, function (result) {
                if (result.success) {
                    starModalAndView.hasReceived(result.hasReceived);
                    starModalAndView.hasReceivedLastTerm(result.hasReceivedLastTerm);
                    if (result.integral) {
                        starModalAndView.integral(result.integral);
                    }
                    if (result.integralLastTerm) {
                        starModalAndView.integralLastTerm(result.integralLastTerm);
                    }
                    if (result.monthRank) {
                        starModalAndView.mothDataAry(result.monthRank);
                    }
                    if (result.termRank) {
                        starModalAndView.termDataAry(result.termRank);
                    }
                } else {
                    $17.jqmHintBox(result.info);
                }
            })
        }
    };
});