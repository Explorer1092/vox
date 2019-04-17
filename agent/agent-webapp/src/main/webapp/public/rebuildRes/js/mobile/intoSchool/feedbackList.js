var setTopBar = {
    show: true,
    rightText: '编辑',
    rightTextColor: "ff7d5a",
    rightImage: window.location.protocol + "//" + window.location.host + "/public/rebuildRes/image/mobile/researchers/add_btn.png",
    needCallBack: true
};
var topBarCallBack = function () {
    openSecond('/mobile/feedback/view/feedbackinfo.vpage');
};
setTopBarFn(setTopBar, topBarCallBack);