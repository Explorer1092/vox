<script>
   $.post("grade_info_map.vpage",{schoolId:schoolId},function(res){
       if(res.success){
           var myChart = echarts.init(document.getElementById("container"));
           option = {
               tooltip : {
                   trigger: 'axis',
                   axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                       type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                   }
               },
               legend: {
                   data:res.grade_info['legend'],
                   x:'center',
                   y:'bottom',
                   show:true,
                   itemWidth:12,
                   itemHeight:12
               },
               grid: {
                   left: '3%',
                   right: '4%',
                   top:'32',
                   bottom: '14%',
                   containLabel: true
               },
               xAxis : [
                   {
                       type : 'category',
                       data : res.grade_info['xAxis']
                   }
               ],
               yAxis : [
                   {
                       type : 'value'
                   }
               ]
           };
           var series=[];
           var colorArray=['#69B273','#FF7D5A','#7ECEFF'];
           for (var i = 0;i<res.grade_info['legend'].length;i++){
               var item = res.grade_info['legend'][i];
               var obj={
                   type:'bar',
                   stack: 'data'+(i+1),
                   barWidth: 10
               };
               if (i<=colorArray.length -1){
                   obj.itemStyle ={
                       normal:{color:colorArray[i]}
                   }
               };
               obj.name=item;
               obj.data=res.grade_info[item];
               series.push(obj);
           }
           option.series= series;
           myChart.setOption(option);
       }
   })


</script>
