var setTopBar = {
    show:true,
    rightText:'提交',
    rightTextColor:"ff7d5a",
    needCallBack:true
} ;
var topBarCallBack = function () {
    $('.js-submit').click();
};
setTopBarFn(setTopBar,topBarCallBack);