#!/bin/bash

# $1 - first argument = commit #to go to
cd ..

cp -r team158/ oldteam
cd oldteam

# move to old commit
if [ $# -ne 0 ]
  then
    git checkout $1
fi

rm -rf .git
find . -type f -print0 | LANG=C xargs -0 sed -i '' 's/team158/oldteam/g'

cd ..

exit 0
