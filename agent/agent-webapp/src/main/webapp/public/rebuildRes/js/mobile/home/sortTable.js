// colIndex 从 0 开始
function sortTable(table, colIndex){
    var tbHeadTr = table.find('thead tr'); //获取table对象下的thead
    var tbHeadTds = tbHeadTr.find('td'); //获取thead下的tr下的td
    if(tbHeadTds.length -1 < colIndex || colIndex < 0){ // 指定的列号不存在
        return;
    }

    // 判断该列是否可以排序
    var sortable = tbHeadTds.eq(colIndex).hasClass("sortable");
    if(!sortable){
        return;
    }
    var tbBody = table.children('tbody'); //获取table对象下的tbody
    var tbBodyTrs = tbBody.find('tr'); //获取tbody下的tr
    if(tbBodyTrs.length < 2){ // 一条数据，无需处理
        return;
    }
    // 如果该列是有序的，直接反转
    var sorted = tbHeadTds.eq(colIndex).hasClass("sorted");
    if(sorted){
        tbBody.html("");
        for(var t = tbBodyTrs.length -1; t >= 0; t--){
            tbBody.append(tbBodyTrs[t]);
        }
        // 原来是降序排列， 反转后调整为升序
        if(tbHeadTds.eq(colIndex).hasClass("desc")){
            tbHeadTds.eq(colIndex).removeClass("desc").addClass("asc");
        }else{
            tbHeadTds.eq(colIndex).removeClass("asc").addClass("desc");
        }
        return;
    }
    var dataList = new Array();
    for(var i = 0; i< tbBodyTrs.length; i++){
        var dataItem = new Array();
        var sortValue = tbBodyTrs.eq(i).find("td:nth-child(" + (colIndex + 1) +")").data("info") || tbBodyTrs.eq(i).find("td:nth-child(" + (colIndex + 1) +")").text();
        dataItem[0] = parseFloat(sortValue || 0);
        dataItem[1] = i;
        dataList.push(dataItem);
    }
    tbBody.html("");

    quickSort(dataList, 0, dataList.length -1);
    for(var x in dataList){
        tbBody.append(tbBodyTrs.eq(dataList[x][1]));
    }

    tbHeadTds.eq(colIndex).addClass("sorted desc").siblings().removeClass("sorted desc asc" );
}


// 快速排序算法  （倒序）
function quickSort(dataList, low, high){
    if(low < high){
        var pivotPosition = partition(dataList, low, high);  //将表一分为二
        quickSort(dataList, low, pivotPosition -1);//递归对低子表递归排序
        quickSort(dataList, pivotPosition +1, high);//递归对高子表递归排序
    }

}
function swap(dataList, a, b){
    var tmp = dataList[a];
    dataList[a] = dataList[b];
    dataList[b] = tmp;
}
function partition(dataList, low, high){
    var pivotKey = dataList[low];    // 基准元素
    while (low < high){           //从表的两端交替地向中间扫描
        while(low < high && parseFloat(dataList[high][0]) <= parseFloat(pivotKey[0])){  //从high 所指位置向前搜索，至多到low+1 位置。将比基准元素大的交换到低端
            high--;
        }
        swap(dataList, low, high);
        while(low < high && parseFloat(dataList[low][0]) >= parseFloat(pivotKey[0])){  //从 low 所指位置向后搜索，至多到high - 1 位置。将比基准元素小的交换到高端
            low++;
        }
        swap(dataList, low, high);
    }
    return low;
}
