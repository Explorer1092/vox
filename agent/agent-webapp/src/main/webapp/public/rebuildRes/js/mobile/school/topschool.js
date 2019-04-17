var setTopBar = {
    show:true,
    rightText:userName,
    rightTextColor:"ff7d5a",
    needCallBack:true
};

var topBarCallBack = function () {
    window.location.href= "/mobile/performance/choose_agent.vpage?breakUrl=top_school_rankings&selectedUser=${user.id!0}&needCityManage=1";
};
setTopBarFn(setTopBar,topBarCallBack);