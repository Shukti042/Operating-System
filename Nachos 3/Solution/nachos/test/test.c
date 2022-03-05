#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
    char b[10];
    char *execArgs[256] = {"mypgr"};
    int status1,processID, processID1, processID2, status2;
    printf("\n\n********************************** mypgr Program Loading-test **********************************\n\n");
    printf("mypgr forking echo.coff and joining... \n");
    processID = exec("abcd.coff", 0,  execArgs);
    int k = join(processID, &status1);

    printf("********* Join On Process %d Finished\nStatus Value:  %d    ***************\n", processID, status1);
    
    printf("mypgr forking halt.coff and joining... \n");
    processID = exec("halt.coff", 0,  execArgs);
    k = join(processID, &status1);
    printf("********* Join On Process %d Finished\nStatus Value:  %d    ***************\n", processID, status1);
    
    printf("mypr forking echo.coff, halt.coff and joining... \n");
    processID1 = exec("halt.coff", 0,  execArgs);
    int l = join(processID1, &status1);
    processID2 = exec("echo.coff", 0,  execArgs);
    int m = join(processID2, &status2);
    printf("*********   Join On Process %d Finished\nStatus Value:  %d   ***************\n", processID1, status1);
    printf("*********   Join On Process %d Finished\nStatus Value:  %d   ***************\n", processID2, status2);
    l = exec("factorial.coff", 0, argv);
    join(l, &status1);
    return 0;
}
