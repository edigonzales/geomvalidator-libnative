#include <stdlib.h>
#include <stdio.h>

#include <libgeomvalidator.h>

int main(int argc, char **argv) {
    graal_isolate_t *isolate = NULL;
    graal_isolatethread_t *thread = NULL;

    if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
        fprintf(stderr, "graal_create_isolate error\n");
        return 1;
    }

    char * layername = "my_layer_name";
    char * fid = "afid";
    char * wktGeom = "POLYGON ((2609000 1236700, 2609200 1236700, 2609200 1236700, 2609200 1236600, 2609000 1236600, 2609000 1236700))";
    printf("%d\n", geomvalidator(thread, layername, fid, wktGeom));

    if (graal_detach_thread(thread) != 0) {
        fprintf(stderr, "graal_detach_thread error\n");
        return 1;
    }

    return 0;
}