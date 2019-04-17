var setTopBar = {
    show: true,
    rightText:"开通" ,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var topBarCallBack =  function(){
    if($(".clazz").children(".the").length){
        var clazz=$(".clazz").children(".the").eq(0);
        subject=$(".subject").children(".the").eq(0);
        data={
            teacherId : teacher.id,
            subject   : subject.data().subject,
            clazzId   : clazz.data().cid
        };
        if(confirmBoolean){
            $("#repatePane").show();
            confirmBoolean = false;
        }
    }else{
        AT.alert("请先选择班级!");
        confirmBoolean = true;
    }
};
setTopBarFn(setTopBar, topBarCallBack);