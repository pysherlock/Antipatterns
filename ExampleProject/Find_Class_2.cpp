#include<iostream>
#include<stdio.h>
#include<stdlib.h>
//#include<string.h>
#include<string>


int main() {

	std::string argv_1, argv_2, argv_3;
	FILE *fp_anti_1, *fp_anti_2, *fp_log, *out;
	int changed_class_num = 0, anti_class_num = 0, x = 1;
	char line_anti_1[300] = {0}, line_anti_2[300] = {0}, line_log[300] = { 0 };
	//std::string line_class;
	while (1) {

		changed_class_num = 0;
		anti_class_num = 0;
		std::cout << "Please enter the log file name: ";
		std::cin >> argv_1;
		std::cout << "Please enter the antipattern_1 file name: ";
		std::cin >> argv_2;
		std::cout << "Please enter the antipattern_2 file name: ";
		std::cin >> argv_3;

		/*	std::cout << argv_1 << "\n";
			std::cout << argv_2 << "\n";
			std::cout << argv_3 << "\n";*/

		if ((fp_log = fopen(argv_1.c_str(), "r")) == NULL) {
			printf("Can't open %s\n", argv_1);
			exit(1);
		}

		if ((fp_anti_1 = fopen(argv_2.c_str(), "r")) == NULL) {
			printf("Can't open %s\n", argv_2);
			exit(1);
		}

		if ((fp_anti_2 = fopen(argv_3.c_str(), "r")) == NULL) {
			printf("Can't open %s\n", argv_3);
			exit(1);
		}

		if ((out = fopen("Changed_Class.txt", "w")) == NULL) {
			printf("Can't create the log\n");
			exit(1);
		}

		fgets(line_anti_1, 300, fp_anti_1);
		fgets(line_log, 300, fp_log);
		char *org = NULL;

		while (!feof(fp_anti_1)) {
			if ((org = strstr(line_anti_1, "org")) != NULL) {   //从第一个反模式文件中找到受该反模式影响的
				//	std::cout << "Org: " << org;

				fseek(fp_anti_2, 0, SEEK_SET);
				fgets(line_anti_2, 300, fp_anti_2);
				while (!feof(fp_anti_2)) {   //从第二个反模式文件中寻找共同影响的类

					if (strstr(line_anti_2, org)) {		//找到共同影响的类，在log里面检查该类是否发生有效修改
						anti_class_num++;
						char *ptr = NULL;
						ptr = strrchr(line_anti_1, '.');
						ptr++;
						std::string line_class(ptr);
						line_class.erase(line_class.end() - 1);
						line_class += ".java";

						std::cout << "Together Class: " << line_class << '\n';

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
					memset(line_anti_2, 0, 300);
					fgets(line_anti_2, 300, fp_anti_2);
				}
			}
			memset(line_anti_1, 0, 300);
			fgets(line_anti_1, 300, fp_anti_1);
		}
		printf("\nTotal Together Anti_Class_Num: %d\n", anti_class_num);
		printf("Changed Together Anti_Class_Num : %d\n", changed_class_num);
		fprintf(out, "\nTotal Together Anti_Class_Num: %d\n", anti_class_num);
		fprintf(out, "Changed Together Anti_Class_Num: %d\n", changed_class_num);
		fclose(fp_anti_1);
		fclose(fp_anti_2);
		fclose(fp_log);
		fclose(out);
		std::cin >> x;
		if (x == 0)
			break;
	}
/*	fprintf(out, "\nTotal Together Anti_Class_Num: %d\n", anti_class_num);
	fprintf(out, "Changed Together Anti_Class_Num: %d\n", changed_class_num);*/
/*	fclose(fp_anti_1);
	fclose(fp_anti_2);
	fclose(fp_log);
	fclose(out);*/
	return 0;

}