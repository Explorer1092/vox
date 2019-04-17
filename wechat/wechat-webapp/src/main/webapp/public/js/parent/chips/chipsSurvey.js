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
                {
                    index:0,
                    sign:-1,
                    red_sign:'',
                    title:"您还是否记得，孩子最初学英语的年纪呢？",
                    ps:'',
                    type:"single_select",
                    options:[
                        {
                            score:0,
                            text:"还没有正式开始学习",
                            is_active:false
                        },
                        {
                            score:5,
                            text:"3岁",
                            is_active:false
                        },
                        {
                            score:10,
                            text:"4岁",
                            is_active:false
                        },{
                            score:10,
                            text:"5岁",
                            is_active:false
                        },{
                            score:10,
                            text:"6岁",
                            is_active:false
                        },{
                            score:5,
                            text:"6岁以上",
                            is_active:false
                        }
                    ]
                },
                {
                    index:1,
                    sign:-1,
                    red_sign:'',
                    title:"孩子目前上几年级呢？",
                    ps:'',
                    type:"single_select",
                    options:[
                        {
                            score:0,
                            text:"幼儿园",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"一年级",
                            is_active:false
                        },
                        {
                            score:5,
                            text:"二年级",
                            is_active:false
                        },{
                            score:10,
                            text:"三年级",
                            is_active:false
                        },{
                            score:10,
                            text:"四年级",
                            is_active:false
                        },{
                            score:10,
                            text:"五年级",
                            is_active:false
                        },
                        {
                            score:5,
                            text:"六年级",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"初中及以上",
                            is_active:false
                        }
                    ]
                },
                {
                    index:2,
                    sign:-1,
                    red_sign:'',
                    title:"在您的心里，哪些方面在孩子英语学习当中最为重要？",
                    ps:'（可多选）',
                    type:"multi_select",
                    options:[
                        {
                            score:0,
                            text:"词汇量",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"听说，交流能力",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"语法",
                            is_active:false
                        },{
                            score:0,
                            text:"发音准确",
                            is_active:false
                        }
                    ]
                },
                {
                    index:3,
                    sign:-1,
                    red_sign:'',
                    title:"在孩子的英语学习中，您觉得哪些地方是他/她目前不够完美的，需要努力加强的？",
                    ps:'（可多选）',
                    type:"multi_select",
                    options:[
                        {
                            score:0,
                            text:"词汇量，语法",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"英语的口语运用能力",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"发音问题",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"考试分数",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"同龄小伙伴当中英语能力不够突出，令人有些着急",
                            is_active:false
                        }
                    ]
                },
                // {
                //     index:4,
                //     sign:-1,
                //     red_sign:'',
                //     title:"孩子最近一次英语考试成绩",
                //     ps:'(换算为百分制)',
                //     type:"single_select",
                //     options:[
                //         {
                //             score:10,
                //             text:"90分以上",
                //             is_active:false
                //         },
                //         {
                //             score:5,
                //             text:"70-90分",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"70分以下",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"没考过",
                //             is_active:false
                //         }
                //     ]
                // },
                // {
                //     index:4,
                //     sign:-1,
                //     red_sign:'',
                //     title:"对孩子学习薯条英语的期待",
                //     ps:'',
                //     type:"single_select",
                //     options:[
                //         {
                //             score:5,
                //             text:"巩固课内所学英语教材的知识点",
                //             is_active:false
                //         },
                //         {
                //             score:0,
                //             text:"提升孩子的英语学习兴趣",
                //             is_active:false
                //         },
                //         {
                //             score:5,
                //             text:"其他",
                //             is_active:false
                //         }
                //     ]
                // },
                {
                    index:4,
                    sign:-1,
                    red_sign:'',
                    title:"孩子正在过参加哪些类型的英语课外培训班呢？",
                    ps:'',
                    type:"single_select",
                    options:[
                        {
                            score:0,
                            text:"线上1对1培训班",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"线上小班课（班里有4-12位同学）",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"线上大班课（12人以上）",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"线下英语培训班",
                            is_active:false
                        },
                        {
                            score:0,
                            text:"没有参加英语课外培训班",
                            is_active:false
                        }
                    ]
                },
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
                            tmpStr.push(arr[i].text)
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
                    expect:'',
                    grade:'',
                    studyDuration:'',
                    interest:'',
                    mentor:'',
                    weekPoints:'',
                    otherExtraRegistration:'',
                    recentlyScore:'',
                    serviceScore:0,
                };

                console.log(_this.list);

                if(_this.is_submit()){
                    var serviceScore = 0;
                    data.studyDuration = _this.getString(_this.list[0].options,_this.list[0].type);
                    data.grade = _this.getString(_this.list[1].options,_this.list[1].type),
                    data.expect = _this.getString(_this.list[2].options,_this.list[2].type);
                    data.weekPoints = _this.getString(_this.list[3].options,_this.list[3].type);
                    // data.recentlyScore =_this.getString(_this.list[4].options,_this.list[4].type);
                    data.otherExtraRegistration = _this.getString(_this.list[4].options,_this.list[4].type);


                    // for(var i = 0 ; i < _this.list.length ; i++ ){
                    //     serviceScore += _this.getScore(_this.list[i].options,_this.list[i].type)
                    // }
                    serviceScore += _this.getScore(_this.list[0].options,_this.list[0].type);
                    serviceScore += _this.getScore(_this.list[1].options,_this.list[1].type);
                    // serviceScore += _this.getScore(_this.list[4].options,_this.list[4].type);
                    data.serviceScore = serviceScore;
                    // console.log(data);
                    $.post("/chips/open/ugc/submit.vpage",data).then(function(res){
                        if(res.success){
                            alert("提交完成");
                            window.location.reload();
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
        created:function(){}
    })

});