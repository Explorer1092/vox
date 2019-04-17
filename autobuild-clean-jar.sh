#!/bin/bash

#依赖库等升级可能导致jar的manifest版本不对
#目前一直没搞定gradle的自动编译，所以先准备这个脚本
#如果线上遇到 GroovyObject 找不到的异常，或者依赖库版本不对，只需要删除包含main class的jar，重新编译即可

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR=`readlink -f $DIR`
cd $DIR

cd build-*
cd build-*

svn rm */*.jar
svn ci -m clean
