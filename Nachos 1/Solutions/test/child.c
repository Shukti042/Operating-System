#include "syscall.h"

int main(int argc, char** argv)
{
    int id, status;
    printf("I am a child process On Level 1\n");
    id = exec("child2.coff", 0, argv);
    join(id, &status);
    if(status == 2){
        printf("Invalid Join\n");
    }
    printf("Child2 ended with status %d\n", status);    
    halt();
    printf("Halt not executed\n");
    return 0;
    /* not reached */
}
