(function(){
    //广告
    var id = "#a-headSwitchBanner-box";
    var clazz = "even";
    var second = 4000;
    var index = 0;					//default
    var idx = $(id);				//id

    $.get("/be/info.vpage?p=2", function (data) {
        if (data.success) {
            idx.html(template("T:学生端学生中心广告", {dataInfo: data.data}));

            var pic = idx.find("li");		//listBox
            var time = setInterval(function () {
                index++;
                initSwitch();
            }, second);

            if (pic.prevAll().length > 0) {
                //遍历
                pic.eq(0).show().siblings().hide();
                pic.each(function (i) {
                    idx.find(".tab").append("<span class='prve " + (i == index ? clazz : '') + "'>" + (i + 1) + "</span>");
                });
            }

            //通用
            function initSwitch() {
                if (index >= pic.length) {
                    index = 0;
                }
                if (index < 0) {
                    index = pic.length - 1;
                }
                idx.find(".prve").eq(index).addClass(clazz).siblings().removeClass(clazz);
                pic.eq(index).fadeIn(60).siblings().hide();
            }

            //经过
            idx.find(".prve, li").on("mouseover", function () {
                clearInterval(time);
                index = $(this).prevAll().length;
                initSwitch();
            }).on("mouseout", function () {
                time = setInterval(function () {
                    index++;
                    initSwitch();
                }, second);
            });

            //左点击
            idx.find(".back, .next").on("click", function () {
                switch ($(this).attr("class")) {
                    case "back":
                        index--;
                        break;
                    case "next":
                        index++;
                        break;
                }
                initSwitch();
                clearInterval(time);
                time = setInterval(function () {
                    index++;
                    initSwitch();
                }, second);
            });
        } else {
            //加载错误
        }
    });

    $(document).on("click", "li[data-banner-voxlog]", function () {
        $17.voxLog({
            module: "student-ad",
            op: "click",
            position: "student-learning", //位置
            adId: $(this).data("banner-voxlog"), //所属广告id
            regionCode: "${(currentStudentDetail.studentSchoolRegionCode)!''}" //用户所属区
        }, "student");
    });
})();