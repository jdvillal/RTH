//
// Created by Jorge on 5/24/2023.
//

#ifndef RTH_LINKEDLIST_H
#define RTH_LINKEDLIST_H

#endif //RTH_LINKEDLIST_H

#include <malloc.h>
#include <stdlib.h>

struct linkedList_Node{
    void *value;
    struct linkedList_Node *next;
    struct linkedList_Node *previous;
};

typedef struct linkedList linkedlist_t;

struct linkedList{
    struct linkedList_Node *head;
    struct linkedList_Node *tail;
    int size;
};

linkedlist_t *new_linkedlist();

void linkedlist_add(linkedlist_t *self, void *item);

void *linkedlist_get(linkedlist_t *self, int index);

int linkedlist_set(linkedlist_t *self, int index, void *item);

/*removes an element from the list and also free the memory occupied by the element
A custom function must be provided in order to save free the memory opcupied for
the specific item removed*/
int linkedlist_delete(linkedlist_t *self, int index, void *(*fi)(void*));

/*removes an element from the list while keeping the element still accesible
from references aoutside the list*/
int linkedlist_remove(linkedlist_t *self, int index);

int linkedList_isEmpty(linkedlist_t *self);