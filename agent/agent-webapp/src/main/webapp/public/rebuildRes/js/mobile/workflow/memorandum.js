var setTopBar = {
    show: true,
    rightText:"保存" ,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var topBarCallBack =  function(){
    var content = $("#context").val();
    $.post("add_memorandum.vpage", {
        schoolId: schoolId,
        teacherId: teacherId,
        content: content,
        type: "text"
    }, function (res) {
        if (res.success) {
            AT.alert("添加成功");
            setTimeout(function () {
                location.href = "school_memorandum_page.vpage?schoolId=" + schoolId;
            },1500);
        } else {
            AT.alert(res.info);
        }
    })
};
setTopBarFn(setTopBar, topBarCallBack);