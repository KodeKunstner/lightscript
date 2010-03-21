echo committing with message: "$*"
echo git commit -m "$*"
echo curl -u lightscript:twitterkodeord -d status="code commit: $*" http://api.twitter.com/1/statuses/update.json
echo git push
