#!/bin/bash
# (C) 2019 Apollo Foundation 
# Starts Apollo GUI  in foreground

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

#"
 . "${DIR}"/apl-desktop-common.sh 

if [[ ! -d "${APPLICATION}" ]] ; then
  mkdir -p  ${APPLICATION}
fi

unamestr=`uname`
xdock=''

if [[ "$unamestr" == 'Darwin' ]]; then
  xdock=-Xdock:icon=../favicon.ico
fi
# uncomment when GUI will start standalone
# ${JAVA_CMD} $xdock  -jar ${MAIN_GUI_JAR}

if [[ $1 == 'tor'  ]]
then
    "${DIR}"/../../apollo-blockchain/bin/apl-run-tor.sh $@ &
elif [[ $1 == 'secure-transport'  ]]
then
    "${DIR}"/../../apollo-blockchain/bin/apl-run-secure-transport.sh $@ &
else
    "${DIR}"/../../apollo-blockchain/bin/apl-start.sh $@ &
fi
nohup "${JAVA_CMD}" $xdock -jar "${MAIN_GUI_JAR}" > /dev/null 2>&1 &
echo $! > ${APPLICATION}/apl-desktop.pid
#cd - > /dev/null
