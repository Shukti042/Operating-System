#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int fact(int n){
    if(n == 0) return 1;
    return n*fact(n-1);
}

int main(int argc, char **argv){
    int n, ans;
    printf("This is a factorial child Program\n");
    n = atoi(argv[0]);
    ans = fact(n);
    printf("Factorial of %d is %d\n", n, ans);
    return 0;
}