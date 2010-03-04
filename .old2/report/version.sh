svn update |grep "At revision" |sed -e s/"At revision /1.1./" | sed -e s/".$"//
