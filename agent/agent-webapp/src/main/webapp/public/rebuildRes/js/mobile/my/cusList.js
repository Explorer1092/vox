var setTopBar = {
    show:true,
    rightText:"+",
    rightTextColor:"ff7d5a",
    needCallBack:true
};
var topBarCallBack = function () {
    window.location.href= "/mobile/feedback/view/searchteacher.vpage?back=customer.vpage";
};
setTopBarFn(setTopBar,topBarCallBack);