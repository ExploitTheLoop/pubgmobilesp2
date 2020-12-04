#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <sys/un.h>

#define SOCKET_NAME "\0andyougo"
#define BACKLOG 8
int sock,clientD;
struct sockaddr_un addr_server;
char socket_name[108];



int Create();
void Close();
int Bind();
int Listen();
int Accept();
int sendit(void* inData, size_t size);
size_t receive(void* outData);

int height,width;
int ve=0,vechOn;
void updateOve(JNIEnv *env,jobject obj,char* s) {
    jclass cls = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(cls, "updateOver", "(Ljava/lang/String;)Z");
    jstring name = env->NewStringUTF(s);
    jboolean chk = env->CallBooleanMethod(obj, mid, name);
    env->DeleteLocalRef(cls);
    env->DeleteLocalRef(name);

    if (chk == 0)
        Close();
}
void clearCanvasN(JNIEnv *env,jobject obj) {
    jclass cls = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(cls,"clearCanvasNative", "()V");
    env->CallVoidMethod(obj, mid);
    env->DeleteLocalRef(cls);
}
void lobbyN(JNIEnv *env,jobject obj) {
    jclass cls = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(cls,"lobby", "()V");
    env->CallVoidMethod(obj, mid);
    env->DeleteLocalRef(cls);
}
int isRunning(JNIEnv *env,jobject obj){
    jclass cls = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(cls, "isRun", "()Z");
    env->DeleteLocalRef(cls);
    return env->CallBooleanMethod(obj, mid);


}

extern "C" JNIEXPORT void JNICALL
Java_com_tencent_desientity_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject obj,jint versn,jint ifvech/* this */) {
    ve = (int)versn;

    vechOn=(int)ifvech;


}

extern "C" JNIEXPORT void JNICALL
Java_com_tencent_desientity_MainActivity_closeSock(
        JNIEnv *env,
        jobject obj/* this */) {
  Close();
}


extern "C" JNIEXPORT void JNICALL
Java_com_tencent_desientity_Overlay_stringFromJNI(
        JNIEnv *env,
        jobject obj,jint jheight,jint jwidth/* this */) {
    height=(int)jheight;
    width=(int)jwidth;


    char sm[200] = "Starting..";
    updateOve(env,obj,sm);

    if (!Create()) {
        perror("Creation failed");
        return;
    }

    if (!Bind()) {
        perror("Bind failed");
        return;
    }

    if (!Listen()) {
        perror("Listen failed");
        return;
    }
    if (Accept()) {
puts("working");

        while (isRunning(env,obj)) {

            char msg[0x4000]="";
            int nByte = receive((void *) &msg);
            if (nByte < 1) {
                strcpy(msg, "false");
                sendit((void *) &msg, strlen(msg));
                break;
            }else if (strcmp(msg, "exit") == 0) {
                strcpy(msg, "");
                updateOve(env,obj,msg);
                strcpy(msg, "Exited");
                sendit((void *) &msg, strlen(msg));
                break;
            } else if (strncmp(msg, "lobby", (size_t) nByte) == 0) {
                lobbyN(env,obj);
                strcpy(msg, "true");
                sendit((void *) &msg, strlen(msg));
            } else if (strncmp(msg, "clear", (size_t) nByte) == 0) {
                clearCanvasN(env,obj);
                strcpy(msg, "true");
                sendit((void *) &msg, strlen(msg));
            } else if (strncmp(msg, "getConfig", (size_t) nByte) == 0) {
                sprintf(msg, "%d-%d-%d-%d", height, width, ve, vechOn);
                sendit((void *) &msg, strlen(msg));

            } else {

                updateOve(env,obj,msg);
                //memset(msg,0,sizeof(msg));
                strcpy(msg, "true");
                sendit((void *) &msg, strlen(msg));

            }
           /* if (nByte < 1) {
                strcpy(msg, "false");
                sendit((void *) &msg, strlen(msg));
                break;
            } else if (strcmp(msg, "exit") == 0) {
                strcpy(msg, "");
                updateOve(env,obj,msg);
                strcpy(msg, "Exited");
                sendit((void *) &msg, strlen(msg));
                break;
            } else if (strncmp(msg, "lobby", (size_t) nByte) == 0) {
                lobbyN(env,obj);
                //updateOve(env,obj,"lobby");
                strcpy(msg, "true");
                sendit((void *) &msg, strlen(msg));
            } else if (strncmp(msg, "clear", (size_t) nByte) == 0) {
                clearCanvasN(env,obj);
                //updateOve(env,obj,"clear");
                strcpy(msg, "true");
                sendit((void *) &msg, strlen(msg));
            } else if (strncmp(msg, "getConfig", (size_t) nByte) == 0) {
                sprintf(msg, "%d-%d-%d-%d", height, width, ve, vechOn);
                sendit((void *) &msg, strlen(msg));

            } else {

                updateOve(env,obj,msg);
                //memset(msg,0,sizeof(msg));
                strcpy(msg, "true");
                sendit((void *) &msg, strlen(msg));

            }
           */

        }

    }
    strcpy(sm,"");
    updateOve(env,obj,sm);
    Close();



    //std::string hello = "Hello from C++";

}


int Create() {
    int isCreated = (sock = socket(AF_UNIX, SOCK_STREAM, 0)) >= 0;
    return isCreated;
}
void Close() {
    if (clientD > 0)
        close(clientD);
    if (sock > 0)
        close(sock);
}
int Accept() {
    if ((clientD = accept(sock, NULL, NULL)) < 0) {
        Close();
        return 0;
    }
    return 1;
}
int Bind() {
    memset(socket_name, 0, sizeof(socket_name));
    memcpy(&socket_name[0], "\0", 1);
    strcpy(&socket_name[1], SOCKET_NAME);

    memset(&addr_server, 0, sizeof(addr_server));
    addr_server.sun_family = AF_UNIX; // Unix Domain instead of AF_INET IP domain
    strncpy(addr_server.sun_path, socket_name, sizeof(addr_server.sun_path) - 1); // 108 char max

    if (bind(sock, (struct sockaddr *) &addr_server, sizeof(addr_server)) < 0) {
        Close();
        return 0;
    }
    return 1;
}
int Listen() {
    if (listen(sock, BACKLOG) < 0) {
        Close();
        return 0;
    }
    return 1;
}

int sendData(void *inData, size_t size) {
    char *buffer = (char *) inData;
    int numSent = 0;

    while (size) {
        do {
            numSent = write(clientD, buffer, size);
        } while (numSent == -1 && EINTR == errno);

        if (numSent <= 0) {
            Close();
            break;
        }

        size -= numSent;
        buffer += numSent;
    }
    return numSent;
}

int sendit(void* inData, size_t size) {
    uint32_t length = htonl(size);
    if(sendData(&length, sizeof(uint32_t)) <= 0){
        return 0;
    }
    return sendData(inData, size) > 0;
}

int recvData(void *outData, size_t size) {
    char *buffer = (char *) outData;
    int numReceived = 0;

    while (size) {
        do {
            numReceived = read(clientD, buffer, size);
        } while (numReceived == -1 && EINTR == errno);

        if (numReceived <= 0) {
            Close();
            break;
        }

        size -= numReceived;
        buffer += numReceived;
    }
    return numReceived;
}

size_t receive(void* outData) {
    uint32_t length = 0;
    int code = recvData(&length, sizeof(uint32_t));
    if(code > 0){
        length = ntohl(length);
        recvData(outData, static_cast<size_t>(length));
    }
    return length;
}