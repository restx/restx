#!/bin/sh
# script to update filename entry in all docs page, used for the pull request button
# thanks to brunosan for the code: http://brunosan.eu/2012/07/01/jekyll-pull-requests/
cd docs
for file in $(ls *.md)
do
	if grep -Fq "filename: " $file
	then
       # code if found
	   echo "File: $file already processed"
	else
	   	# code if not found
    	echo "Adding the line on file: $file"
        awk -v n=2 -v s="filename: $file" 'NR == n {print s} {print}' $file > tmp.txt
		mv tmp.txt $file
	fi
done