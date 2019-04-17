/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js"],function($,logger,Vue){

    var vm = new Vue({
        el:'#chips_survey',
        data:{
            toast_status:false,
            list:[
                {
                    index:0,
                    sign:-1,
                    red_sign:'',
                    title:"收货人姓名",
                    ps:'',
                    type:"single_select",
                    placeholder:"",
                    options:[{
                        sTitle:'',
                        regionList:[]
                    }]


                },
                {
                    index:1,
                    sign:-1,
                    red_sign:'',
                    title:"收货地址",
                    ps:'',
                    placeholder:"",
                    type:"single_select",
                    options:[
                        {
                            sTitle:"所在街道"
                        },
                    ]
                },
                {
                    index:2,
                    sign:-1,
                    red_sign:'',
                    title:"收货人电话",
                    ps:'（可多选）',
                    type:"multi_select",
                    options:[
                        {
                            sTitle:"请输入手机号",
                            regionList:[]

                        }
                    ]
                },
                {
                    index:3,
                    sign:-1,
                    red_sign:'',
                    title:"后续课程级别",
                    ps:'（可多选）',
                    type:"multi_select",
                    options:[
                        {
                            sTitle:"",
                            regionList:[]
                        }

                    ]
                },
            ],
            regionList:[],
            prov:[],
            pIndex:'',
            choice:[
                {
                    name:'',
                    code:''
                }

            ],
            proveShow:false,
            cityName:[],
            name:'',
            courseList:["G1初级班","G1保过班","G2初级班","G2保过班","G3初级班","G3保过班"],
            courseShow:false,

        },
        methods:{
            choiceProv:function(index){
                var _this=this;
                if($(".proveTitle")[0].innerHTML==''){
                }
                var code=_this.regionList[index].code;
                _this.cityName.push(_this.regionList[index].name);
                _this.name=_this.name+_this.regionList[index].name+"/";
                $(".proveTitle")[0].innerHTML=_this.name
                var url = '/chips/regionlist.vpage?regionCode='+code;
                $.get(url,
                    function(res) {
                        if(res.success){
                            _this.regionList=res.regionList;
                        }else{
                            alert(res.info);
                        }
                    });
            },
            clickProv:function(){
                var _this=this;
                var addressBox=$(".proveTitle")[0].innerHTML;
                addressBox='';
                _this.proveShow=true;
                    var url = '/chips/regionlist.vpage?regionCode=0';
                    $.get(url,
                        function(res) {
                            if(res.success){
                                _this.regionList=res.regionList;
                            }else{
                                alert(res.info);
                            }
                        });
                    $(".proveTitle")[0].innerHTML='';
                    console.log($(".proveTitle")[0].innerHTML)
                    _this.name=''
            },
            clickCourse:function(){
                this.courseShow=true;

            },
            choiceCourse:function(index){
                var _this=this;
                $('.courseTitle')[0].innerHTML = _this.courseList[index];
                _this.courseShow=false
            },
            submit:function(){
                var _this=this;
                var recipientName=$('#text00').val();
                var recipientTel=$('#text20').val();
                var recipientAddr=$('.proveTitle')[0].innerHTML+$('.detailSite')[0].value;
                var courseLevel=$('.courseTitle')[0].innerHTML;
                if(recipientName && recipientTel && recipientAddr && recipientAddr!=='所有省份'&& courseLevel){

                    $.post('/chips/ugc/email_save.vpage ',{
                        recipientName:recipientName,
                        recipientTel:recipientTel,
                        recipientAddr:recipientAddr,
                        courseLevel:courseLevel
                    },function(res){
                        if(res.success){
                            alert("提交完成");
                            window.location.reload();
                        } else {
                            alert(res.info)
                        }
                    })
                }else{
                    alert('请补全信息');
                    event.preventDefault();
                }
            }

        },

        created:function(){
            var _this = this;
            var url = '/chips/regionlist.vpage?regionCode=0';
            $.get(url,
                function(res) {
                    if(res.success){
                        _this.regionList=res.regionList;
                    }else{
                        alert(res.info);
                    }
                });
        }
    })

});