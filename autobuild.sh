#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR=`readlink -f $DIR`
cd $DIR

stage=$1
stage_mode=`echo "$stage" | cut -d '-' -f 1`
stage_codename=`echo "$stage" | cut -d '-' -f 2`
output_dirname="build-$stage"

if [[ ! ( "test staging production" =~ (^| )$stage_mode($| ) ) ]]; then
    echo "stage: test staging production"
    exit -1
fi


if [ "$stage_mode" = "production" -o "$stage_mode" = "staging" ]; then
    svn_base_url="http://svn.dc.17zuoye.net/repo/$output_dirname"
else
    svn_base_url="http://192.168.100.8:8088/svn/$output_dirname"
fi


buildid=`git log --max-count=1 --pretty=format:%ad-%H --date=format-local:%Y%m%d-%H%M%S`


BUILD_ROOT=$DIR/build-repo/$output_dirname
echo "using $BUILD_ROOT build $buildid"

svn_status_do() {
   filter=$1
   svncmd=$2
   cmdline="svn status | $filter"
   files=`eval $cmdline`
   for file in $files; do
       svn $svncmd $file
   done
}



if [ -e $BUILD_ROOT/.svn ]; then
    cd $BUILD_ROOT
    svn_status_do "grep '^M' | sed 's/^M *//g'" revert
    svn up
else
    mkdir -p $BUILD_ROOT
    cd $BUILD_ROOT
    svn co $svn_base_url .
fi

if [ "$?" != "0" ]; then
    echo "svn checkout/update failed"
    exit 1
fi

cd $DIR

python tools/BuildPreProcess.py ${stage}

if ! gradle --no-parallel clean -Pstage=${stage} ; then
    echo "gradle failed"
    exit 1
fi
#not using "parallel", to save CPU time for other services
if gradle --parallel --refresh-dependencies assemble -Pstage=${stage} ; then

    python tools/BuildPostProcess.py ${stage}

    # http://wiki.17zuoye.net/pages/viewpage.action?pageId=9739936
    # prepare:
    #   curl -sL https://rpm.nodesource.com/setup | bash -
    #   yum install nodejs gcc-c++ make
    #   npm install -g gulp gulp-coffee gulp-compass gulp-concat gulp-jshint gulp-less gulp-rename gulp-seajs gulp-sourcemaps gulp-uglify gulp-util gulp-clean
    # compress js:
    echo "Code compression ... ..."
    npm install

    sed "s/__stage__/$stage/g" gulpsource.js > gulpfile.js

	if ! ./node_modules/.bin/gulp; then
	    echo "gulp failed"
        exit 1
    fi

    echo "prepare webappctl.sh for $BUILD_ROOT/env/ ..."
    cp tools/webappctl.sh $BUILD_ROOT/env/
    cp tools/preinit.sh $BUILD_ROOT/env/
    sed "s~SVN_BASE_URL=__dummy__~SVN_BASE_URL=$svn_base_url~g" -i $BUILD_ROOT/env/webappctl.sh
    sed "s/STAGE=__dummy__/STAGE=$stage/g" -i $BUILD_ROOT/env/webappctl.sh
    chmod a+x $BUILD_ROOT/env/webappctl.sh
    chmod a+x $BUILD_ROOT/env/preinit.sh


    dirs=`ls -d $BUILD_ROOT/*-provider`
    dirs="$dirs $BUILD_ROOT/utopia-schedule"
    dirs="$dirs $BUILD_ROOT/utopia-dubbo-proxy"
    dirs="$dirs $BUILD_ROOT/utopia-hydra-agent"
    dirs="$dirs $BUILD_ROOT/utopia-hydra-agent-http2"
    dirs="$dirs $BUILD_ROOT/utopia-surl"
    dirs="$dirs $BUILD_ROOT/utopia-voice-score"
    for d in $dirs; do
        fn_ctl="$d/svcappctl.sh"
        fn_preinit="$d/preinit.sh"
        echo "prepare svcappctl.sh for $fn_ctl ..."
        cp tools/svcappctl.sh $fn_ctl
        cp tools/preinit.sh $fn_preinit
        sed "s~SVN_BASE_URL=__dummy__~SVN_BASE_URL=$svn_base_url~g" -i $fn_ctl
        sed "s/STAGE=__dummy__/STAGE=$stage/g" -i $fn_ctl
        chmod a+x $fn_ctl
        chmod a+x $fn_preinit
    done



    cd $BUILD_ROOT
    rm -rf 000-build-*.txt
    echo "$buildid" > 000-$output_dirname.txt

    if [ "$stage_mode" = "production" ]; then
        #sed 's/<root level="INFO">/<root level="WARN">/g' -i $BUILD_ROOT/washington-webapp/WEB-INF/classes/logback.xml
        #sed 's/<logger name="com.google.code.ssm.spring.SSMCache" level="INFO" additivity="false">/<logger name="com.google.code.ssm.spring.SSMCache" level="WARN" additivity="false">/g' -i $BUILD_ROOT/washington-webapp/WEB-INF/classes/logback.xml
        true;
    fi

    # umpay private key
    if [ "$stage_mode" = "production" -o "$stage_mode" = "staging" ]; then
        cp -r $BUILD_ROOT/private/www-3318-mer.key.p8 $BUILD_ROOT/washington-webapp/webroot/WEB-INF/classes/resource/payment/umpay/
        cp -r $BUILD_ROOT/private/www-3318-umpay.cert.crt $BUILD_ROOT/washington-webapp/webroot/WEB-INF/classes/resource/payment/umpay/
    fi

    svn_status_do "grep '^\!' | sed 's/^\! *//g'" rm
    svn_status_do "grep '^\?' | sed 's/^\? *//g'" add

    svn ci -m "BUILD_STAGE:$stage BUILD_ID:$buildid"
else
    echo "gradle failed"
    exit 1
fi
