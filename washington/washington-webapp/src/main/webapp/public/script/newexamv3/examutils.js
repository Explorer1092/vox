(function($17){
    //给artTemplate添加辅助方法,判断是否数学公式
    template.helper("isMathJax",function(content){
        var regExp = /\\\w+(\[[^\]]+\])?({[^}]+})*/g;
        var value = regExp.test(content);
        // console.info(value);
        return value;
    });

    template.helper("optimizeAnswer",function (content) {
        //对模考学生答案分数的优化
        if((/\\frac\{.*?\}\{.*?\}/g).test(content)){
            content = content.replace(/\\frac\{.*?\}\{.*?\}/g,function(word){
                return "$"+word+"$";
            });
        }else if(/\\\w+(\[[^\]]+\])?({[^}]+})*/g.test(content)){
            content = '$' + content + '$';
        }
        return content;

    })
}($17));