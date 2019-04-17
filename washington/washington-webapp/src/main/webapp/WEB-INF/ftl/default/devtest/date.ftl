<#-- 如果是 sql.Timestamp/sql.Date 等类型，freemarker能识别出是时间类型，可以直接引用 -->
<#-- 但是，经过hessian之后，这个字段也有可能是 util.Date 导致不识别 -->
${currentUser.createTime}<br />
<br />
<br />
<#-- 如果是 util.Date 类型，用 ${d} freemarker无法识别会报错，需要用 ${d?date} 才安全 -->
<#-- 虽然 "?is_date" 是 true， 但是如果试图引用 ${d} 还是会报错 -->
is_date:${d?is_date?string}<br />
date: ${d?date}<br />