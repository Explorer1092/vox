/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js","../../public/lib/weixin/jweixin-1.0.0.js"],function($,logger,Vue,wx){


    $(function(){

        var startTime = new Date().getTime();
        var isFirst = true;
        var isMiddle = true;
        var isFooter = true;

        $(document).on("scroll",function(){
            var screenHt = window.screen.height;

            // 吸顶效果
            var c = $(document).scrollTop();
            var topHt = $(".newHead").height();
            var ulHt = $(".newNav").height();
            if(c > topHt){
                $(".newNav").addClass("fixd_top")
                $(".paddingTop").css('padding-top',ulHt+'px');
            }else{
                $(".newNav").removeClass("fixd_top");
                $(".paddingTop").css('padding-top',0+'px');
            }

            // 滚动打点
            if(c > screenHt && isFirst){
                isFirst = false;
                // 正价广告页_首屏滚屏时长
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_1stscroll',
                    s2:new Date().getTime() - startTime
                });
                startTime = new Date().getTime();
            }else if(c > screenHt*2 && isMiddle){
                isMiddle = false;
                // 	正价广告页_二屏滚屏时长
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_2ndscroll',
                    s2:new Date().getTime() - startTime
                });
                startTime = new Date().getTime();
            }

            if($(window).height()+ $(window).scrollTop() >= $(document).height()-10 && isFooter){
                isFooter = false;
                // 购买_购买广告页_滑到底屏
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_lastscroll',
                    s2:new Date().getTime() - startTime
                });

            }
        });



        // 正价广告页_被加载
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'mainad_load'
        });
    });

    var vm = new Vue({
        el:'#adnew',
        data:{
            productData:{
                remaining:0,
                grade1:{
                    products:[],
                    remaining:0,
                },
                grade2:{
                    products:[],
                    remaining:0,
                },
                grade3:{
                    products:[],
                    remaining:0,
                },
            },
            productIndex:0,
            sign:'intro',
            productId:'',
            grade:1,
            degreeStatus:false,
            remaining:0,
            originalPrice:0,
            price:0,
            levelName:"123",
            course:{
                1:{
                    studyTime:'58天',
                    words:'300以内',
                    up:[
                        {
                            text1:'Week 1',
                            text2:'Say Hi!',
                            text3:'Make Friends'
                        },
                        {
                            text1:'Week 2',
                            text2:'Say Bye!',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 3',
                            text2:'Stationery',
                            text3:'Who\'s Rulers?'
                        },
                        {
                            text1:'Week 4',
                            text2:'New Stationery',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 5',
                            text2:'Body Parts',
                            text3:'What\'s this?'
                        },
                        {
                            text1:'Week 6',
                            text2:'Draw Pictures',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 7',
                            text2:'Follow me!',
                            text3:'Simon Says'
                        },
                        {
                            text1:'Week 8',
                            text2:'Number Games',
                            text3:'Mock Exam'
                        }
                    ],
                    down:[
                        {
                            text1:'Week 1',
                            text2:'Zoo!',
                            text3:'Wild Animals'
                        },
                        {
                            text1:'Week 2',
                            text2:'Farm',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 3',
                            text2:'Age',
                            text3:'Calculation'
                        },
                        {
                            text1:'Week 4',
                            text2:'Special Number',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 5',
                            text2:'Camping',
                            text3:'Rainbow'
                        },
                        {
                            text1:'Week 6',
                            text2:'The Magic of Color',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 7',
                            text2:'Costumes',
                            text3:'Bad Weather'
                        },
                        {
                            text1:'Week 8',
                            text2:'Costume Party',
                            text3:'Mock Exam'
                        }
                    ]
                },
                2:{
                    studyTime:'58天',
                    words:'300-700',
                    up:[
                        {
                            text1:'Week 1',
                            text2:'What is in my home?',
                            text3:'My New House!'
                        },
                        {
                            text1:'Week 2',
                            text2:'New Bedroom',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 3',
                            text2:'Host Family',
                            text3:'"Lost" Stationery'
                        },
                        {
                            text1:'Week 4',
                            text2:'Little Helper',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 5',
                            text2:'New Objects',
                            text3:'Special Bedroom'
                        },
                        {
                            text1:'Week 6',
                            text2:'Gift for New House',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 7',
                            text2:'Mike\'s Family',
                            text3:'Mike\'s Family Tree'
                        },
                        {
                            text1:'Week 8',
                            text2:'My Best Friends',
                            text3:'Mock Exam'
                        }
                    ],
                    down:[
                        {
                            text1:'Week 1',
                            text2:'Help Cats',
                            text3:'Teachers\' Pet'
                        },
                        {
                            text1:'Week 2',
                            text2:'I Want a Pet',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 3',
                            text2:'Where is my Stationery?',
                            text3:'What are you doing?'
                        },
                        {
                            text1:'Week 4',
                            text2:'Go Shopping',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 5',
                            text2:'When is the test?',
                            text3:'Math Review'
                        },
                        {
                            text1:'Week 6',
                            text2:'Different May',
                            text3:'Mock Exam'
                        },
                        {
                            text1:'Week 7',
                            text2:'My life in American Campus',
                            text3:'Go to the Cinema'
                        },
                        {
                            text1:'Week 8',
                            text2:'Find My Seat',
                            text3:'Mock Exam'
                        }
                    ]
                },
                3:{
                    studyTime:'72天',
                    words:'700-1000',
                    up: [
                        {
                        text1: 'Week 1',
                        text2: 'New Job',
                        text3: 'What to do in the future?'
                    }, {
                        text1: 'Week 2',
                        text2: 'Mock Interview',
                        text3: 'Part-time Job'
                    }, {
                        text1: 'Week 3',
                        text2: 'Mock Exam',
                        text3: 'Directions'
                    }, {
                        text1: 'Week 4',
                        text2: 'Way to the Museum',
                        text3: 'Way to the Hospital'
                    }, {
                        text1: 'Week 5',
                        text2: 'Visit Aunt Alice',
                        text3: 'Mock Exam'
                    }, {
                        text1: 'Week 6',
                        text2: 'My School',
                        text3: 'Open Time'
                    }, {
                        text1: 'Week 7',
                        text2: 'What\'s in School?',
                        text3: 'My Classroom'
                    }, {
                        text1: 'Week 8',
                        text2: 'Mock Exam',
                        text3: 'Good Weather'
                    }, {
                        text1: 'Week 9',
                        text2: 'Weather Forecast',
                        text3: 'Beijing\'s Weather'
                    }, {
                        text1: 'Week 10',
                        text2: 'Picnic Time',
                        text3: 'Mock Exam'
                    }],
                    down: [
                        {
                        text1: 'Week 1',
                        text2: 'My Family Mumbers',
                        text3: 'Only Child'
                    }, {
                        text1: 'Week 2',
                        text2: 'New Family',
                        text3: 'Family Life'
                    }, {
                        text1: 'Week 3',
                        text2: 'Mock Exam',
                        text3: 'Mike is Late.'
                    }, {
                        text1: 'Week 4',
                        text2: 'My Schedule',
                        text3: 'Dinner at Home'
                    }, {
                        text1: 'Week 5',
                        text2: 'Big Dinner',
                        text3: 'Mock Exam'
                    }, {
                        text1: 'Week 6',
                        text2: 'After-class Activities',
                        text3: 'Favorite Sports'
                    }, {
                        text1: 'Week 7',
                        text2: 'Tom is Bored.',
                        text3: 'LeBron James'
                    }, {
                        text1: 'Week 8',
                        text2: 'Mock Exam',
                        text3: 'TV Shows'
                    }, {
                        text1: 'Week 9',
                        text2: 'Talk about Courses',
                        text3: 'Schedule in the Summet'
                    }, {
                        text1: 'Week 10',
                        text2: 'Birthday Gift',
                        text3: 'Mock Exam'
                    }]
                }
            }
        },
        computed:{
            popup:function(){
                return this.degreeStatus ? 'fixed' : 'relative'
            },
            single_remaining:function(){
                var _this = this;
                return _this.productData['grade'+_this.grade].remaining
            },
            productNameUp:function(){
                var _this = this;
                return _this.productData['grade'+_this.grade].products[0].productName
            },
            productNameDown:function(){
                var _this = this;
                return _this.productData['grade'+_this.grade].products[1].productName
            },
            title:function(){
                var _this = this;

                switch(_this.grade){
                    case 1:
                        return "初到美国插班生活体验";
                        break;
                    case 2:
                        return "成为寄宿家庭一员";
                        break;
                    case 3:
                        return "在美国的第一个暑假";
                        break;
                }
            }
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            changeTab:function(sign,isBuy){
                this.sign = sign;

                var c = $(document).scrollTop();
                var topHt = $(".newHead").height();
                var ulHt = $(".newNav").height();
                if(c > topHt){
                    $("html,body").animate({scrollTop: topHt + 'px'}, 500);
                    $(".paddingTop").css('padding-top',ulHt+'px');
                }else{
                    $("html,body").animate({scrollTop: topHt + 'px'}, 500);
                    $(".paddingTop").css('padding-top',0+'px');
                }

                if(isBuy){
                    // 正价广告页_购买按钮1_被点击
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'mainad_purchasebutton1_click'
                    });
                }

                // 正价广告页_主标签_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_maintab_click',
                    s2:sign
                });

            },
            changeGrade:function(grade){
                var _this = this;
                _this.grade = grade;
                console.log(_this.productData['grade'+_this.grade])
                console.log(_this.productData['grade'+_this.grade].products[_this.productIndex])
                console.log(_this.productData['grade'+_this.grade].products[_this.productIndex].originalPrice)
                _this.originalPrice = _this.productData['grade'+_this.grade].products[_this.productIndex].originalPrice;
                _this.price = _this.productData['grade'+_this.grade].products[_this.productIndex].price;

                var s2 = "G"+grade;
                // 正价广告页_级别标签_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_leveltab_click',
                    s2:s2
                });
            },
            select:function(index){
                var _this = this;
                _this.productIndex = index;
                _this.originalPrice = _this.productData['grade'+_this.grade].products[index].originalPrice
                _this.price = _this.productData['grade'+_this.grade].products[index].price;

                // 正价广告页_购买按钮_级别_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_purchasebutton_level_click',
                    s2:index
                });

            },
            closePopup:function(){
                this.degreeStatus = false
            },
            buy:function(){
                var _this = this;
                var inviter = $("#inviter").val();
                if(_this.degreeStatus){
                    _this.productId = _this.productData['grade'+_this.grade].products[_this.productIndex].productId;
                    window.location.href = "/chips/order/create.vpage?productId="+_this.productId+"&inviter="+inviter;
                }else{
                    this.degreeStatus = true;
                }

                // 正价广告页_购买按钮2_被点击
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_purchasebutton2_click'
                });
            }
        },
        created:function(){
            var _this = this;
            $.get("/chips/center/officialproducts.vpage",function(res){
                if(res.success){
                    _this.productData = res;
                    _this.originalPrice = _this.productData['grade1'].products[0].originalPrice;
                    _this.price = _this.productData['grade1'].products[0].price;

                    var levelName = _this.getParams('levelName') || '123';
                    console.log(levelName);
                    _this.levelName = levelName;
                    switch(levelName){
                        case "1":
                            _this.grade = 1;
                            _this.changeGrade(1);
                            break;
                        case "2":
                            _this.grade = 2;
                            _this.changeGrade(2);
                            break;
                        case "3":
                            _this.grade = 3;
                            _this.changeGrade(3);
                            break;
                        default:
                            break;
                    }

                }
            });

        }
    })

});