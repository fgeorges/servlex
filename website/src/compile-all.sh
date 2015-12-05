#!/bin/sh

# temporary marked's output file
tmp=tmp-marked.out

# loop over all markdown files
for md in *.md
do
    # the entry name, with no extension
    name=`echo $md | sed s/.md//`
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
