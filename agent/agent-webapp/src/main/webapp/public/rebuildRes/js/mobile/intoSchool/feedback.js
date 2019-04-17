var setTopBar = {
    show: true,
    rightText: showRight,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var topBarCallBack = function () {
    if(needCallBackFn){
        $(".js-submitSchoolSRecord").click();
    }
};
setTopBarFn(setTopBar, topBarCallBack);
