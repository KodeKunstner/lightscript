svn update |grep "At revision" |sed -e s/"At revision /1.0./" | sed -e s/".$"//
