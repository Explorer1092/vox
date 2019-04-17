var setTopBar = {
    show:true,
    rightText:'编辑',
    rightTextColor:"ff7d5a",
    needCallBack:true
} ;
var topBarCallBack = function () {
    window.location.href = url;
};
setTopBarFn(setTopBar,topBarCallBack);