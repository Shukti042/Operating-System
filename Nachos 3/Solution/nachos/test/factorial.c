#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int fact(int n){
    if(n == 0) return 1;
    return n*fact(n-1);
}

int main(int argc, char **argv){
    int n, ans;
    char buf[100];
    printf("This is a factorial child Program\n");
    printf("Enter an integer number: " );
    readline(buf, 20);
    n = atoi(buf);
    ans = fact(n);
    printf("Factorial of %d is %d\n", n, ans);
    printf(" ********** Factorial Program Finished *******\n\n");
    return 0;
}