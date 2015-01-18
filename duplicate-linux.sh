#!/bin/bash

# $1 - first argument = commit #to go to
cd ..

if [ -d "oldteam" ]
  then
    rm -r oldteam
fi

cp -r team158/ oldteam
cd oldteam

# move to old commit
if [ $# -ne 0 ]
  then
    git checkout $1
fi

rm -rf .git
find * -type f -exec sed -i 's/team158/oldteam/g' {} \;

cd ..

exit 0
