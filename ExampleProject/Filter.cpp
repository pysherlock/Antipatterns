//Fliter the comment easily

#include<iostream>
#include<stdio.h>
#include<stdlib.h>
//#include<string.h>
#include<string>

int main() {
	
	std::string argv;
	FILE *fp, *out;
	int change_line_count = 0, change_class = 0, class_num = 0, changed_class_num = 0;
	char line[300] = {0};
	bool change = false;//记录类是否存在修改
	
	std::cout<<"Please enter the file name: ";
	std::cin>>argv;

	printf("argv: %s\n", argv);
	if ((fp = fopen(argv.c_str(), "r")) == NULL) {
		printf("Can't open %s\n", argv);
		exit(1);
	}

	if ((out = fopen("Change_log.txt", "w")) == NULL) {
		printf("Can't create the log\n");
		exit(1);
	}

	fgets(line, 300, fp);
	while (!feof(fp)) {
		if (strlen(line)>1 && strstr(line, "diff -ry") == NULL) {   //非空行，非diff命令
			if (strstr(line, "* $Id")) {
				class_num++;
				fprintf(out, "\n%s", line);
				if (change)  //输出上一个类的修改行数
					fprintf(out, "Effctive changed lines in this class: %d\n", change_class);
				else
					fprintf(out, "There is NO effctive change in this class\n");
				change_class = 0;
				change = false;
			}
			if (strstr(line, "/*") == NULL) {  //简易过滤注释
				if (strstr(line, "//") == NULL) {
					if (strchr(line, '*') == NULL) {
						change_line_count++;
						change_class++;
						if (!change) {  //类存在修改
							change = true;
							changed_class_num++;
						}
					}
				}
			}
		}
		if (strlen(line) > 250)
			printf("length of line: %d", strlen(line));
		memset(line, 0, 300);
		fgets(line, 300, fp);
	}

	printf("Effctive changed lines: %d\n", change_line_count);
	fprintf(out, "\nDifferent Classes Num: %d\n", class_num);
	fprintf(out, "Changed Classses Num: %d\n", changed_class_num);
	fprintf(out, "Total Effctive Changed Lines: %d\n", change_line_count);

	return 0;
}

