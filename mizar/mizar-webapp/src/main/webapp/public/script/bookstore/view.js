/*-----------书店管理相关-----------*/
define(["jquery", "echarts"], function ($, echarts) {


    $(function () {
        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        };


        var bookStoreId = $.getUrlParam('id');
        getWeixinImage(bookStoreId);
        base64=[];//用于保存生成之后的base64
        weixin=[];//用于保存微信地址
        function goPAGE() {
            if ((navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i))) {
                /*window.location.href="你的手机版地址";*/

                return "mobile"
            }
            else {
                /*window.location.href="你的电脑版地址";    */
                return "pc"
            }
        }

        var codeImg = null;
        var isCodeloadOver = false;
        //动态获得二维码
        function getWeixinImage(bookStoreId,callback){
            $.ajax({
                type: 'POST',
                data: {
                    bookStoreId:bookStoreId
                },
                url: '/bookstore/manager/downloadQrCode.vpage',
                success: function (res) {
                    if(res) {
                        codeImg = new Image();
                        codeImg.crossOrigin = 'Anonymous'; //解决跨域问
                        codeImg.src = res.data.imgUrl;
                        codeImg.width = 105;
                        $("#erwei").append(codeImg);
                        codeImg.onload = function () {
                            isCodeloadOver = true;
                        };
                        var type = goPAGE();
                        //判断是否是pc 端，如果是在进行图片合并
                        if (type == 'pc'){
                            weixin.push(res.data.imgUrl);
                        }

                    }

                }

            });

        }

        function draw(url) {
            if(!isCodeloadOver){
                console.info("code loading")
                return false;
            }

                var data=[];
                data.push("/public/skin/images/baseImage4.jpg");
                data.push(url);
                if(data[1]){
                    var img1= new Image();
                    img1.setAttribute('crossOrigin', 'anonymous');
                    img1.onload = function () {//这步必须,因为图片加载是异步的,必须等图片加载完成才开始下面的这些步骤
                        var c = document.createElement('canvas');
                        ctx = c.getContext('2d');
                        c.width = img1.naturalWidth;//6.92
                        c.height = img1.naturalHeight;//11.17
                        ctx.rect(0, 0, c.width, c.height);
                        ctx.fillStyle = '#fff';
                        ctx.fill();
                        ctx.drawImage(img1, 0, 0, c.width, c.height);
                        //设置字体样式
                        ctx.font = "bold 300px Arial";
                        //设置字体填充颜色
                        ctx.fillStyle = "#ffffff";
                        //从坐标点(92,800)开始绘制文字
                        var bookStoreId = $.getUrlParam('id');
                        ctx.fillText(bookStoreId,5090,9300);
                        /*上面是增加文字,可以无限加*/
                            if(codeImg){
                               // clearInterval(time);
                                ctx.drawImage(codeImg, 2020, 3310, 1900, 1900);
                                base64.push(c.toDataURL("image/jpeg", 1));//如果绘制完成了,就把base64数据填进数组,然后回调,没完成则继续这步
                                var blob=dataURLtoFile(base64[0]);
                                var a = document.createElement('a');
                                a.innerHTML = "海报.jpg";
                                // 指定生成的文件名
                                a.download = "海报.jpg";
                                a.href = URL.createObjectURL(blob);
                                document.body.appendChild(a);
                                var evt = document.createEvent("MouseEvents");
                                evt.initEvent("click", false, false);
                                a.dispatchEvent(evt);
                                document.body.removeChild(a);
                            }
                    };
                    img1.src = data[0];
                }





        };
        function drawWinxin() {

        }

        //base64 转成blob 格式
        function dataURLtoFile(dataurl) {
            var arr = dataurl.split(',');
            var mime = arr[0].match(/:(.*?);/)[1];
            var bstr = atob(arr[1]);
            var n = bstr.length;
            var u8arr = new Uint8Array(n);
            while(n--){
                u8arr[n] = bstr.charCodeAt(n);
            }
            //转换成成blob对象
            return new Blob([u8arr],{type:mime});
        };



        function download() {

            draw(weixin[0]);
        }

            $("#downBtn").click(function () {
                download();
            });



        //指定图标的配置和数据
        function setOption() {
            var xAxisData = [];
            var sData = [];
            if($("#chartData").text() == ''){return;}
            var data = $.parseJSON( $("#chartData").text() );
            var width = $("#chart").width();
            $("#chart").css("width",width+"px")
            for (x in data)
            {
                xAxisData.push(x);
                sData.push(data[x]);

            }
            var option = {
                color:['#27a9bf'],
                title:{
                    text:''
                },
                tooltip:{},
                legend:{
                    data:['用户来源']
                },
                xAxis:{
                    axisLine: {show:false},
                    axisTick: {show:false},
                    splitLine:{show:false},
                    data:xAxisData
                },
                yAxis:{
                    show:true,
                    axisLine: {show:false},
                    axisTick: {show:false},
                    splitLine:{show:false},
                    splitArea:{show:false},
                    axisLabel : {
                        formatter: function(){
                            return "";
                        }
                    }

                },
                series:[{
                    name:'订单数',
                    type:'bar',
                    data:sData,
                    label: {
                        normal: {
                            show: true,
                            position: 'top'
                        }
                    }
                }]
            };
            //初始化echarts实例
            var myChart = echarts.init(document.getElementById('chart'));
            myChart.setOption(option);

            window.onresize = function () {
                myChart.resize();

            }

        }
        setOption();
    });

});