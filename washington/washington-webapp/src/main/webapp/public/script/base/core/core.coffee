$_$ =
    version: "4.0.0"


__help = ->
    @__help__


extend = (child, parent)->
    child[key] = parent[key] for key of parent when parent.hasOwnProperty key
    return child

extend extend,
    help    : __help
    __help__: """
exnted 函数用于扩展对象
第一个参数：需要扩展的对象
第二个参数：需要增加的属性
返回：被扩展后的对象
"""


createFun = (newFun, helpStr)->
    extend newFun,
        help    : __help
        __help__: helpStr

extend createFun,
    help    : __help
    __help__: """
createFun 函数用于创建带帮助函数的函数
第一个参数：函数体
第二个参数：帮助说明
"""


include = createFun (child, parent)->
    child::[key] = parent[key] for key of parent when parent.hasOwnProperty key
    return child
,
                    """
include 函数用于扩展原型
第一个参数：需要扩展的原型
第二个参数：需要增加的属性
返回：被扩展后的原型
"""


lpad = createFun (pram...)->
    switch pram.length
        when 0, 1 then return false
        when 2
            while "#{pram[0]}".length < pram[1]
                pram[0] = " #{pram[0]}"
        else
            while "#{pram[0]}".length < pram[1]
                pram[0] = "#{pram[2]}#{pram[0]}"

    return pram[0]
,
                 """
lpad 补位函数(从左)
第一个参数：需要补位的字符串
第二个参数：最终补位后的长度
第三个[可选]：用于补位的字符串，默认空格
"""


rpad = createFun (pram...)->
    switch pram.length
        when 0, 1 then return false
        when 2
            while "#{pram[0]}".length < pram[1]
                pram[0] = "#{pram[0]} "
        else
            while "#{pram[0]}".length < pram[1]
                pram[0] = "#{pram[0]}#{pram[2]}"
    return pram[0]
,
                 """
lpad 补位函数(从右)
第一个参数：需要补位的字符串
第二个参数：最终补位后的长度
第三个[可选]：用于补位的字符串，默认空格
"""


if module? and exports?
    exports.__help = __help
    exports.extend = extend
    exports.createFun = createFun
    exports.include = include
    exports.lpad = lpad
    exports.rpad = rpad
else
    extend $_$,
        __help   : __help
        extend   : extend
        createFun: createFun
        include  : include
        lpad     : lpad
        rpad     : rpad

    @$17 = $_$
