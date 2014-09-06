#!/bin/bash -e

if [ "$1" == "NEO" ]; then (

  lein cljsbuild once release
  cd resources/public/javascripts
  curl -F app.js=@app.js https://johannie:gsrbygb68@neocities.org/api/upload

  ) else (

  commit_message="$1"


  lein cljsbuild once release
  git add -A
  git commit -m "$commit_message"
  git push breathcenter master

)
fi
