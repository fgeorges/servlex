#!/bin/sh

# temporary marked's output file
tmp=tmp-marked.out

sources=../src/*.md

if [ -n "$1" ]; then
    sources="../src/${1}.md"
    if [ \! -f "$sources" ]; then
	echo "Source Markdown file does not exist: $sources"
	exit 1
    fi
    if [ -n "$2" ]; then
	echo "Extra option, only one (optional) source file permitted: $2"
	exit 1
    fi
fi

# loop over all markdown files
for md in $sources
do
    # the entry name, with no extension
    name=`echo $md | sed s/\\\\.\\\\.\\\\/src\\\\/// | sed s/.md//`
    html=../${name}.html
    # index's href is "."
    if [ "$name" = index ]; then
        href=\\.
    else
        href=$name
    fi
    echo Compiling $name
    ./compile-md.js $md > $tmp
    sed \
        -e "/class='__active__' href='${href}'/ {
          s/class='__active__'/class='active'/
        }" \
        -e "/class='__active__' href='.*'/ {
          s/class='__active__' //
        }" \
        -e "/__CONTENT__/ {
          r $tmp
          d
        }" < template.html > $html
done

rm $tmp
