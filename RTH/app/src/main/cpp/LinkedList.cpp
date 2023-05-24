//
// Created by Jorge on 5/24/2023.
//
#include "LinkedList.h"


linkedlist_t *new_linkedlist(){
    linkedlist_t *list = (linkedlist_t*)malloc(sizeof(struct linkedList));
    //list->head = NULL;
    //list->tail = NULL;
    list->size = 0;
    return list;
}

void linkedlist_add(linkedlist_t *self, void *item){
    struct linkedList_Node *node = (linkedList_Node*)malloc(sizeof(struct linkedList_Node));
    node->value = item;
    if(self->size == 0){
        self->head = node;
        self->tail = node;
    }else{
        node->previous = self->tail;
        self->tail->next = node;
        self->tail = node;
    }
    self->size = self->size + 1;
}

void *linkedlist_get(linkedlist_t *self, int index){
    if(index > (self->size)-1){
        return NULL;
    }else{
        struct linkedList_Node *node = self->head;
        if(index == 0){
            return node->value;
        }
        for(int i = 0; i < index; i++){
            node = node->next;
        }
        return node->value;
    }
}

int linkedlist_set(linkedlist_t *self, int index, void *item){
    if(index > (self->size)-1){
        return -1;
    }else{
        struct linkedList_Node *node = self->head;
        for(int i = 0; i < index; i++){
            node = node->next;
        }
        node->value = item;
        return 0;
    }
}

/*remove the item at index and free its memory*/
int linkedlist_delete(linkedlist_t *self, int index, void *(*fi)(void*)){
    if(index > (self->size)-1){
        return -1;
    }else{
        if(self->size == 1){
            struct linkedList_Node *node = self->head;
            (*fi)((void*)node->value);
            free(node);
            self->head = NULL;
            self->tail = NULL;
        }else{
            if(index == 0){
                struct linkedList_Node *node = self->head;
                self->head = node->next;
                self->head->previous = NULL;
                (*fi)((void*)node->value);
                free(node);
            }else if(index == ((self->size)-1)){
                struct linkedList_Node *node = self->tail;
                self->tail = node->previous;
                self->tail->next = NULL;
                (*fi)((void*)node->value);
                free(node);
            }else{
                struct linkedList_Node *node = self->head;
                for(int i = 0; i < index; i++){
                    node = node->next;
                }
                node->previous->next = node->next;
                node->next->previous = node->previous;
                (*fi)((void*)node->value);
                free(node);
            }
        }
        self->size = self->size - 1;
        return 0;
    }
}

int linkedlist_remove(linkedlist_t *self, int index){
    if(index > (self->size)-1){
        return -1;
    }else{
        if(self->size == 1){
            struct linkedList_Node *node = self->head;
            self->head = NULL;
            self->tail = NULL;
        }else{
            if(index == 0){
                struct linkedList_Node *node = self->head;
                self->head = node->next;
                self->head->previous = NULL;
            }else if(index == ((self->size)-1)){
                struct linkedList_Node *node = self->tail;
                self->tail = node->previous;
                self->tail->next = NULL;
            }else{
                struct linkedList_Node *node = self->head;
                for(int i = 0; i < index; i++){
                    node = node->next;
                }
                node->previous->next = node->next;
                node->next->previous = node->previous;
            }
        }
        self->size = self->size - 1;
        return 0;
    }
}

int linkedList_isEmpty(linkedlist_t *self){
    return self->size == 0;
}