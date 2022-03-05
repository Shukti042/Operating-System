#include<iostream>
#include<cstdio>
#include<pthread.h>
#include<unistd.h>
#include<semaphore.h>
#include<cstring>
#include <time.h>
#include <cstdlib>
#include <ctime>

using namespace std;

#define number_of_cycles 20
#define number_of_serviceman 4
#define number_of_counters 3

pthread_mutex_t service[number_of_serviceman];
pthread_mutex_t L;
sem_t cash_counter;
int wishToLeave=0;

void* take_service(void* arg)
{
    for(int i=0;i<number_of_serviceman;i++)
    {
        if(i==0&&number_of_serviceman>1)
        {
            pthread_mutex_lock(&L);
            while(wishToLeave!=0);
            pthread_mutex_lock(&service[i]);
        }
        else
        {
            pthread_mutex_lock(&service[i]);
        }
        printf("%s started taking service from serviceman %d \n",(char*)arg,i+1);
        if(i!=0)
        {
            pthread_mutex_unlock(&service[i-1]);
            if(i==1)
            {
                pthread_mutex_unlock(&L);
            }
        }
        sleep((rand() % 5) + 1);
        printf("%s finished taking service from serviceman %d \n",(char*)arg,i+1);
        if(i==number_of_serviceman-1)
        {
            pthread_mutex_unlock(&service[i]);
        }

    }
    sem_wait(&cash_counter);
    printf("%s started paying the service bill \n",(char*)arg);
    sleep((rand() % 10) + 1);
    wishToLeave++;
    pthread_mutex_lock(&service[0]);
    printf("%s finished paying the service bill \n",(char*)arg);
    sem_post(&cash_counter);
    for(int i=0;i<number_of_serviceman;i++)
    {
        if(i!=0)
        {
             pthread_mutex_lock(&service[i]);
        }
        sleep((rand() % 2) + 1);
        if(i==number_of_serviceman-1)
        {
            printf("%s has departed\n",(char*)arg);
            wishToLeave--;
        }
        pthread_mutex_unlock(&service[i]);
    }

    pthread_exit((void*)strcat((char*)arg," Cyclist is leaving\n"));

}

int main(int argc, char* argv[])
{
    int res;
    res = sem_init(&cash_counter,0,number_of_counters);
    if(res != 0){
        printf("Failed\n");
    }
    srand((unsigned) time(0));
    for(int i=0;i<number_of_serviceman;i++)
    {
        res = pthread_mutex_init(&service[i],NULL);
        if(res != 0)
        {
            printf("Failed\n");
        }
    }
    res = pthread_mutex_init(&L,NULL);
    if(res != 0)
    {
        printf("Failed\n");
    }

    pthread_t cyclists[number_of_cycles];
    for(int i=0;i<number_of_cycles;i++)
    {
        char *id = new char[3];
        strcpy(id,to_string(i+1).c_str());
        res = pthread_create(&cyclists[i],NULL,take_service,(void *)id);
        if(res != 0){
            printf("Thread creation failed\n");
        }
    }
    for(int i=0;i<number_of_cycles;i++)
    {
        void *result;
        pthread_join(cyclists[i],&result);
    }

    if(res != 0){
        printf("Failed\n");
    }
    for(int i=0;i<number_of_serviceman;i++)
    {
        res = pthread_mutex_destroy(&service[i]);
        if(res != 0)
        {
            printf("Failed\n");
        }
    }
    res = sem_destroy(&cash_counter);
    if(res != 0){
        printf("Failed\n");
    }
    res = pthread_mutex_destroy(&L);
    if(res != 0)
    {
            printf("Failed\n");
    }

    return 0;


}
