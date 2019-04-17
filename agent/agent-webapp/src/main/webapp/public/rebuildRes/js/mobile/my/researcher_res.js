var setTopBar = {
    show:true,
    rightTextColor:"ff7d5a",
    rightImage:window.location.protocol+ "//" + window.location.host + "/public/rebuildRes/image/mobile/researchers/add_btn.png",
    needCallBack:true
};
var topBarCallBack = function () {
    openSecond('/view/mobile/crm/researcher/add_researcher.vpage');
};
setTopBarFn(setTopBar,topBarCallBack);