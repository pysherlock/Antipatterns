#include<iostream>
#include<stdio.h>
#include<stdlib.h>
//#include<string.h>
#include<string>


int main() {

	std::string argv_1, argv_2;
	FILE *fp_anti, *fp_log, *out;
	int changed_class_num = 0, anti_class_num = 0;
	char line_anti[300] = { 0 }, line_log[300] = { 0 };
	//std::string line_class;

	std::cout << "Please enter the log file name: ";
	std::cin >> argv_1;
	std::cout << "Please enter the antipattern file name: ";
	std::cin >> argv_2;

	std::cout << argv_1<<"\n";
	std::cout << argv_2<<"\n";

	if ((fp_log = fopen(argv_1.c_str(), "r")) == NULL) {
		printf("Can't open %s\n", argv_1);
		exit(1);
	}

	if ((fp_anti = fopen(argv_2.c_str(), "r")) == NULL) {
		printf("Can't open %s\n", argv_2);
		exit(1);
	}

	if ((out = fopen("Changed_Class_Num.txt", "w")) == NULL) {
		printf("Can't create the log\n");
		exit(1);
	}

	fgets(line_anti, 300, fp_anti);

	while (!feof(fp_anti)) {
		if (strstr(line_anti, "org")) {   //找到一个反模式影响的类
			anti_class_num++;
			char *ptr = NULL;
			ptr = strrchr(line_anti, '.');
			ptr++;
			
			std::string line_class(ptr);
			line_class.erase(line_class.end()-1);
			line_class += ".java";
			std::cout << line_class <<'\n';

			fseek(fp_log, 0, SEEK_SET);
			fgets(line_log, 300, fp_log);
			while (!feof(fp_log)) {
				if (strstr(line_log, line_class.c_str())) { //发现class有修改
					memset(line_log, 0, 300);
					fgets(line_log, 300, fp_log); //判断是否为有效修改
					if (!strstr(line_log, "There is NO effctive change"))
						changed_class_num++;
					break;
				}
				memset(line_log, 0, 300);
				fgets(line_log, 300, fp_log);
			}

		}
		memset(line_anti, 0, 300);
		fgets(line_anti, 300, fp_anti);
	}

	fprintf(out, "\nTotal Anti_Class_Num: %d\n", anti_class_num);
	fprintf(out, "Changed Anti_Class_Num: %d\n", changed_class_num);
	fclose(fp_anti);
	fclose(fp_log);
	fclose(out);

	return 0;

}