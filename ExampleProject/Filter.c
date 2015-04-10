
//Fliter the comment easily

#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<unistd.h>

int main(int argc, char *argv[]) {
	FILE *fp, *out;
	int change_line_count = 0;
	char line[300] = {0};

	printf("argc: %d\n", argc);
	if(argc != 2) {
		printf("No file\n");
		exit(1);
	}

	printf("argv: %s\n", argv[0]);
	if((fp = fopen((argv[1]), "r")) == NULL) {
		printf("Can't open %s\n", argv[0]);
		exit(1);
	}

	fgets(line, 300, fp);
	while(!feof(fp)) {
		if(strlen(line) > 1) {// && strstr(line, "diff -ry") == NULL) {   //非空行
			if(strstr(line, "/*") == NULL) {
				if(strstr(line, "//") == NULL) {
					if(strchr(line, '*') == NULL)
						change_line_count++;
				}
			}
		}
		if(strlen(line) > 250)
			printf("length of line: %d", strlen(line));
		memset(line, 0, 300);
		fgets(line, 300, fp);
	}

	if((out = fopen("Change_log", "w")) == NULL) {
		printf("Can't create the log\n");
		exit(1);
	}
	printf("Effctive changed lines: %d\n", change_line_count);
	fprintf(out, "Effctive changed lines: %d\n", change_line_count);

	return 0;
}

