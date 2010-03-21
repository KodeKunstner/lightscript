make clean
git add `find com testsuite examples -name "*.java"` `find com testsuite examples -name "*.ls"` Makefile README.md TODO Changelog push.sh COPYING doc/static/* doc/source/* doc/app.yaml
git commit -m "$*"
curl -u lightscript:twitterkodeord -d status="$*" http://api.twitter.com/1/statuses/update.json
git push
