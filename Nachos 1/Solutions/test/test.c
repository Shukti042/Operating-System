#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main()
{
    char s[100];
    char* argp[]= {"arg"};
    char buf[100];
    int i, n, j, id, status, stat2, num, id1, id2, id3;
    printf("------------CHECKING COMMAND LINE ARGUMENTS PASSING--------------\n");    
    printf("Please input an integer: ");
    readline(s, 100);
    argp[0] = s;    
    id = exec("factorial.coff", 1, argp);
    id = join(id, &status);
    printf("Factorial task has been ended with status: %d\n", status);    
    id = join(50, &status);
    if(status == 2){
        printf("Invalid Join\n");
    }

    printf("------------CHECKING Multi Level Exec--------------\n");        
    id = exec("child.coff", 1, argp);
    join(id, status);

    printf("Enter any string: ");
    readline(buf, 30);
    printf("INPUT by user : %s\n", buf);
   
    printf("------------CHECKING INVALID READ CALLS--------------\n");
    num = read(3, &buf, 10);
    printf("Return on invalid file descriptor: %d\n", num);
    num = read(0, -12, 10);
    printf("Return on invallid vaddr: %d\n", num);
    num = read(0, &buf, -2);
    printf("Return on invallid size: %d\n", num);

    printf("\n------------CHECKING INVALID WRITE CALLS--------------\n");
    num = write(3, &buf, 10);
    printf("Return on invalid file descriptor: %d\n", num);
    num = write(0, -12, 10);
    printf("Return on invallid vaddr: %d\n", num);
    num = write(0, &buf, -2);
    printf("Return on invallid size: %d\n", num);

    printf("\n------------CHECKING INVALID JOIN CALLS--------------\n");
    num = join(2, &num);
    printf("Return for join on pid 2 : %d\n", num);
    num = join(3, &num);
    printf("Return for join on pid 3 : %d\n", num);

    printf("\n------------CHECKING PARALLEL PROCESS--------------\n");    
    id1 = exec("abcd.coff", 0, argp);
    printf("1 started");
    id2 = exec("abcd.coff", 0, argp);
    printf("2 started");
    id3 = exec("abcd.coff", 0, argp);
    printf("3 started");
    join(id1, status);
    join(id2, status);
    join(id3, status);
    printf("\n------------END PARALLEL PROCESS--------------\n");        
    halt();
    printf("Halt is not working!!\n");
    return 0;
}