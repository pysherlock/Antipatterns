#########################################################################
# File Name: MiningChange.sh
# Author: ma6174
# mail: ma6174@163.com
# Created Time: 2015年04月09日 星期四 20时44分52秒
#########################################################################
#!/bin/bash
#a function


#function diff_change() {
	echo "Enter the file name:"
	read file_1
	read file_2
	echo ${file_1}
	echo ${file_2}
	diff $file_1 $file_2 -ry -B -w -X ex.txt --suppress-common-lines >Patch.txt
#}

#diff_change
echo "Fini"

