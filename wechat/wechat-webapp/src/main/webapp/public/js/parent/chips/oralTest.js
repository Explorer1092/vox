/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){


    $(function(){

        //页面加载 打点
        // logger.log({
        //     module: 'm_XzBS7Wlh',
        //     op: 'purchase_adpage_load'
        // });
    });

    var vm = new Vue({
        el:'#chips_survey',
        data:{
            toast_status:false,
            list:[
                // {
                //     index:0,
                //     sign:-1,
                //     red_sign:'',
                //     title:"您还是否记得，孩子最初学英语的年纪呢？",
                //     ps:'',
                //     type:"single_select",
                //     options:[
                //         {
                //             score:0,
                //             text:"还没有正式开始学习",
                //             is_active:false
                //         },
                //         {
                //             score:5,
                //             text:"3岁",
                //             is_active:false
                //         },
                //         {
                //             score:10,
                //             text:"4岁",
                //             is_active:false
                //         },{
                //             score:10,
                //             text:"5岁",
                //             is_active:false
                //         },{
                //             score:10,
                //             text:"6岁",
                //             is_active:false
                //         },{
                //             score:5,
                //             text:"6岁以上",
                //             is_active:false
                //         }
                //     ]
                // },
                // {
                //     index:1,
                //     sign:-1,
                //     red_sign:'',
                //     title:"孩子目前上几年级呢？",
                //     ps:'',
                //     type:"single_select",
                //     options:[
                //         {
                //             score:0,
                //             text:"幼儿园",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"一年级",
                //             is_active:false
                //         },
                //         {
                //             score:5,
                //             text:"二年级",
                //             is_active:false
                //         },{
                //             score:10,
                //             text:"三年级",
                //             is_active:false
                //         },{
                //             score:10,
                //             text:"四年级",
                //             is_active:false
                //         },{
                //             score:10,
                //             text:"五年级",
                //             is_active:false
                //         },
                //         {
                //             score:5,
                //             text:"六年级",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"初中及以上",
                //             is_active:false
                //         }
                //     ]
                // },
                // {
                //     index:2,
                //     sign:-1,
                //     red_sign:'',
                //     title:"在您的心里，哪些方面在孩子英语学习当中最为重要？",
                //     ps:'（可多选）',
                //     type:"multi_select",
                //     options:[
                //         {
                //             score:0,
                //             text:"词汇量",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"听说，交流能力",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"语法",
                //             is_active:false
                //         },{
                //             score:0,
                //             text:"发音准确",
                //             is_active:false
                //         }
                //     ]
                // },
                // {
                //     index:3,
                //     sign:-1,
                //     red_sign:'',
                //     title:"在孩子的英语学习中，您觉得哪些地方是他/她目前不够完美的，需要努力加强的？",
                //     ps:'（可多选）',
                //     type:"multi_select",
                //     options:[
                //         {
                //             score:0,
                //             text:"词汇量，语法",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"英语的口语运用能力",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"发音问题",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"考试分数",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"同龄小伙伴当中英语能力不够突出，令人有些着急",
                //             is_active:false
                //         }
                //     ]
                // },
                // {
                //     index:4,
                //     sign:-1,
                //     red_sign:'',
                //     title:"孩子正在过参加哪些类型的英语课外培训班呢？",
                //     ps:'',
                //     type:"single_select",
                //     options:[
                //         {
                //             score:0,
                //             text:"线上1对1培训班",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"线上小班课（班里有4-12位同学）",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"线上大班课（12人以上）",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"线下英语培训班",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"没有参加英语课外培训班",
                //             is_active:false
                //         }
                //     ]
                // },
            ]
        },
        methods:{
            select:function(question_index,option_index,type){
                var _this = this;
                if(type == 'multi_select'){
                    _this.list[question_index].sign = option_index;
                    _this.list[question_index].red_sign = '';
                    _this.list[question_index].options[option_index].is_active = !_this.list[question_index].options[option_index].is_active;

                    var option_arr = _this.list[question_index].options;
                    for(var j = 0 ; j < option_arr.length ; j++){
                        if(option_arr[j].is_active) {
                            _this.list[question_index].sign = 1;
                            break;
                        }else{
                            _this.list[question_index].sign = -1;
                        }
                    }
                }else{
                    _this.list[question_index].sign = option_index;
                    _this.list[question_index].red_sign = '';
                    for(var i = 0 ; i <  _this.list[question_index].options.length ; i++){
                        _this.list[question_index].options[i].is_active = false;
                    }
                    _this.list[question_index].options[option_index].is_active = true;
                }
            },
            getString:function(arr,type){
                var tmpStr = [];
                if(type == 'single_select'){
                    for(var i = 0 ; i < arr.length ; i++){
                        if(arr[i].is_active){
                            tmpStr.push(arr[i].score)
                        }
                    }
                }else{
                    for(var i = 0 ; i < arr.length ; i++){
                        if(arr[i].is_active){
                            tmpStr.push(arr[i].text)
                        }
                    }
                }
                return tmpStr.join()
            },
            getScore:function(arr,type){
                var tmpScore = 0;
                if(type == 'single_select'){
                    for(var i = 0 ; i < arr.length ; i++){
                        if(arr[i].is_active){
                            tmpScore = arr[i].score
                        }
                    }
                }else{
                    for(var i = 0 ; i < arr.length ; i++){
                        if(arr[i].is_active){
                            tmpScore += arr[i].score
                        }
                    }
                }
                return tmpScore
            },
            is_submit:function(){
                var _this = this;
                for(var k = 0 ; k < _this.list.length ; k++){
                    if(_this.list[k].sign <= -1){
                        return false;
                    }
                }
                return true
            },
            submit:function(){
                var _this = this;
                var data = {
                    beginDate:'',
                    userId:261901,
                    // grade:'',
                    // studyDuration:'',
                    // interest:'',
                    // mentor:'',
                    // weekPoints:'',
                    // otherExtraRegistration:'',
                    // recentlyScore:'',
                    // serviceScore:0,
                };

                console.log(_this.list);

                if(_this.is_submit()){
                    var serviceScore = 0;
                    console.log(_this.getString(_this.list[0].options,_this.list[0].type))
                    console.log(_this.getString(_this.list[1].options,_this.list[1].type))
                    data.beginDate = _this.getString(_this.list[0].options,_this.list[0].type) + " " + _this.getString(_this.list[1].options,_this.list[1].type)
                    // data.studyDuration = _this.getString(_this.list[0].options,_this.list[0].type);
                    // data.grade = _this.getString(_this.list[1].options,_this.list[1].type),
                    // data.serviceScore = serviceScore;
                    // console.log(data);
                    $.post("/chips/ugc/oral_submit.vpage",data).then(function(res){
                        if(res.success){
                            alert("提交完成");
                            window.location.reload();
                        } else {
                            alert(res.info)
                        }
                    })
                }else{
                    for(var k = 0 ; k < _this.list.length ; k++){
                        _this.list[k].red_sign = '';
                        if(_this.list[k].sign <= -1){
                            _this.list[k].red_sign = 'red';
                            _this.toast_status = true;
                            setTimeout(function(){
                                _this.toast_status = false;
                            },3000)
                        }
                    }
                }
            }
        },
        created:function(){
            var _this = this;
            $.get('/chips/ugc/oral_data.vpage', {
            }, function (res) {
                if (res.success) {
                  console.log(res);
                  var dayList = res.dayList;

                  var firstOptions=[];
                    // dayList.forEach(function(e){
                    //     firstOptions.push({"score": 0, "text": e, "is_active": false});
                    // });
                    for(var key in dayList){
                        firstOptions.push({"score": key, "text": dayList[key], "is_active": false});
                    }
                    console.log(firstOptions)
                    var first = {
                        "index": 0,
                        "sign": -1,
                        "red_sign": '',
                        "title": "请选择口语测试的日期",
                        "ps": '',
                        "type": "single_select",
                        "options": firstOptions
                    };
                    _this.list.push(first)
                    console.log(first)
                    var regionList = res.timeRegionList;
                    var secondOptions=[];
                    // regionList.forEach(function(e){
                    //     secondOptions.push({"score": e[0], "text": e[1], "is_active": false});
                    // });
                    for(var key in regionList){
                        secondOptions.push({"score": key, "text": regionList[key], "is_active": false});
                    }
                    var second = {
                        "index": 1
                        ,
                        "sign": -1,
                        "red_sign": '',
                        "title": "请选择可以和孩子一起口语测试的时间",
                        "ps": '',
                        "type": "single_select",
                        "options": secondOptions
                    };
                    _this.list.push(second)
                } else {
                    alert(res.info)
                }
            });

        }
    })

});