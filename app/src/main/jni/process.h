#include <jni.h>
#include <sys/select.h>
#include <unistd.h>
#include <sys/socket.h>
#include <pthread.h>
#include <signal.h>
#include <sys/wait.h>
#include <android/log.h>
#include <sys/types.h>
#include <sys/un.h>
#include <errno.h>
#include <stdlib.h>

#define LOG_TAG "Native"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/**
 * 功能:对父子进程的一个抽象
 * @author wangqiang
 * @date 2014-03-14
 */
class ProcessBase {
public:

	ProcessBase();

	/**
	 * 父子进程要做的工作不相同,留出一个抽象接口由父子进程
	 * 自己去实现.
	 */
	virtual void do_work() = 0;

	/**
	 * 进程可以根据需要创建子进程,如果不需要创建子进程,可以给
	 * 此接口一个空实现即可.
	 */
	virtual bool create_child() = 0;

	/**
	 * 捕捉子进程死亡的信号,如果没有子进程此方法可以给一个空实现.
	 */
	virtual void catch_child_dead_signal() = 0;

	/**
	 * 在子进程死亡之后做任意事情.
	 */
	virtual void on_child_end() = 0;

	/**
	 * 创建父子进程通信通道.
	 */
	bool create_channel();

	/**
	 * 给进程设置通信通道.
	 * @param channel_fd 通道的文件描述
	 */
	void set_channel(int channel_fd);

	/**
	 * 向通道中写入数据.
	 * @param data 写入通道的数据
	 * @param len  写入的字节数
	 * @return 实际写入通道的字节数
	 */
	int write_to_channel(void* data, int len);

	/**
	 * 从通道中读数据.
	 * @param data 保存从通道中读入的数据
	 * @param len  从通道中读入的字节数
	 * @return 实际读到的字节数
	 */
	int read_from_channel(void* data, int len);

	/**
	 * 获取通道对应的文件描述符
	 */
	int get_channel() const;

	virtual ~ProcessBase();

protected:

	int m_channel;
};

/**
 * 功能：父进程的实现
 * @author wangqiang
 * @date 2014-03-14
 */
class Parent: public ProcessBase {
public:

	Parent(JNIEnv* env, jobject jobj);

	virtual bool create_child();

	virtual void do_work();

	virtual void catch_child_dead_signal();

	virtual void on_child_end();

	virtual ~Parent();

	bool create_channel();

	/**
	 * 获取父进程的JNIEnv
	 */
	JNIEnv *get_jni_env() const;

	/**
	 * 获取Java层的对象
	 */
	jobject get_jobj() const;

private:

	JNIEnv *m_env;

	jobject m_jobj;

};

/**
 * 子进程的实现
 * @author wangqiang
 * @date 2014-03-14
 */
class Child: public ProcessBase {
public:

	Child();

	virtual ~Child();

	virtual void do_work();

	virtual bool create_child();

	virtual void catch_child_dead_signal();

	virtual void on_child_end();

	bool create_channel();

private:

	/**
	 * 处理父进程死亡事件
	 */
	void handle_parent_die();

	/**
	 * 侦听父进程发送的消息
	 */
	void listen_msg();

	/**
	 * 重新启动父进程.
	 */
	void restart_parent();

	/**
	 * 处理来自父进程的消息
	 */
	void handle_msg(const char* msg);

	/**
	 * 线程函数，用来检测父进程是否挂掉
	 */
	void* parent_monitor();

	void start_parent_monitor();

	/**
	 * 这个联合体的作用是帮助将类的成员函数做为线程函数使用
	 */
	union {
		void* (*thread_rtn)(void*);

		void* (Child::*member_rtn)();
	} RTN_MAP;
};

extern ProcessBase *g_process;

extern const char* g_userId;

extern JNIEnv* g_env;

//子进程有权限访问父进程的私有目录,在此建立跨进程通信的套接字文件
static const char* PATH =
		"/data/data/com.zeasn.remotecontrol/my.sock";

//服务名称
static const char* SERVICE_NAME =
		"com.zeasn.remotecontrol/com.zeasn.remotecontrol.service.RemoteControlService";

int ProcessBase::write_to_channel(void* data, int len) {
	return write(m_channel, data, len);
}

int ProcessBase::read_from_channel(void* data, int len) {
	return read(m_channel, data, len);
}

int ProcessBase::get_channel() const {
	return m_channel;
}

void ProcessBase::set_channel(int channel_fd) {
	m_channel = channel_fd;
}

ProcessBase::ProcessBase() {

}

ProcessBase::~ProcessBase() {
	close(m_channel);
}

Parent::Parent(JNIEnv *env, jobject jobj) :
		m_env(env) {
	LOGE("<<new parent instance>>");

	m_jobj = env->NewGlobalRef(jobj);
}

Parent::~Parent() {
	LOGE("<<Parent::~Parent()>>");

	g_process = NULL;
}

void Parent::do_work() {
}

JNIEnv* Parent::get_jni_env() const {
	return m_env;
}

jobject Parent::get_jobj() const {
	return m_jobj;
}

/**
 * 父进程创建通道,这里其实是创建一个客户端并尝试
 * 连接服务器(子进程)
 */
bool Parent::create_channel() {
	int sockfd;

	sockaddr_un addr;

	while (1) {
		sockfd = socket( AF_LOCAL, SOCK_STREAM, 0);

		if (sockfd < 0) {
			LOGE("<<Parent create channel failed>>");

			return false;
		}

		memset(&addr, 0, sizeof(addr));

		addr.sun_family = AF_LOCAL;

		strcpy(addr.sun_path, PATH);

		if (connect(sockfd, (sockaddr*) &addr, sizeof(addr)) < 0) {
			close(sockfd);

			sleep(1);

			continue;
		}

		set_channel(sockfd);

		LOGE("<<parent channel fd %d>>", m_channel);

		break;
	}

	return true;
}

/**
 * 子进程死亡会发出SIGCHLD信号,通过捕捉此信号父进程可以
 * 知道子进程已经死亡,此函数即为SIGCHLD信号的处理函数.
 */
static void sig_handler(int signo) {
	pid_t pid;

	int status;

//调用wait等待子进程死亡时发出的SIGCHLD
//信号以给子进程收尸，防止它变成僵尸进程
	pid = wait(&status);

	if (g_process != NULL) {
		g_process->on_child_end();
	}
}

void Parent::catch_child_dead_signal() {
	LOGE("<<process %d install child dead signal detector!>>", getpid());

	struct sigaction sa;

	sigemptyset(&sa.sa_mask);

	sa.sa_flags = 0;

	sa.sa_handler = sig_handler;

	sigaction( SIGCHLD, &sa, NULL);
}

void Parent::on_child_end() {
	LOGE("<<on_child_end:create a new child process>>");

	create_child();
}

bool Parent::create_child() {
	pid_t pid;
	if ((pid = fork()) < 0) {
		LOGE("fork,false,pid=%d", getpid());
		return false;
	} else if (pid == 0) //子进程
			{
		LOGE("<<In child process,pid=%d>>", getpid());

		Child child;

		ProcessBase& ref_child = child;

		ref_child.do_work();
	} else //父进程
			{
		LOGE("<<In parent process,pid=%d>>", getpid());
	}

	return true;
}

bool Child::create_child() {
//子进程不需要再去创建子进程,此函数留空
	return false;
}

Child::Child() {
	RTN_MAP.member_rtn = &Child::parent_monitor;
}

Child::~Child() {
	LOGE("<<~Child(), unlink %s>>", PATH);

	unlink(PATH);
}

void Child::catch_child_dead_signal() {
//子进程不需要捕捉SIGCHLD信号
	return;
}

void Child::on_child_end() {
//子进程不需要处理
	return;
}

void Child::handle_parent_die() {
//子进程成为了孤儿进程,等待被Init进程收养后在进行后续处理
	while (getppid() != 1) {
		usleep(500); //休眠0.5ms
	}

	close(m_channel);

//重启父进程服务
	LOGE("<<parent died,restart now>>");

	restart_parent();
}

void Child::restart_parent() {
	LOGE("<<restart_parent enter>>");

	/**
	 * TODO 重启父进程,通过am启动Java空间的任一组件(service或者activity等)即可让应用重新启动
	 */
	execlp("am", "am", "startservice", "--user", g_userId, "-n", SERVICE_NAME, //注意此处的名称
			(char *) NULL);
}

void* Child::parent_monitor() {
	handle_parent_die();
	return NULL;
}

void Child::start_parent_monitor() {
	pthread_t tid;

	pthread_create(&tid, NULL, RTN_MAP.thread_rtn, this);
}

bool Child::create_channel() {
	int listenfd, connfd;

	struct sockaddr_un addr;

	listenfd = socket( AF_LOCAL, SOCK_STREAM, 0);

	unlink(PATH);

	memset(&addr, 0, sizeof(addr));

	addr.sun_family = AF_LOCAL;

	strcpy(addr.sun_path, PATH);

	if (bind(listenfd, (sockaddr*) &addr, sizeof(addr)) < 0) {
		LOGE("<<bind error,errno(%d)>>", errno);

		return false;
	}

	listen(listenfd, 5);

	while (true) {
		if ((connfd = accept(listenfd, NULL, NULL)) < 0) {
			if ( errno == EINTR)
				continue;
			else {
				LOGE("<<accept error>>");

				return false;
			}
		}

		set_channel(connfd);

		break;
	}

	LOGE("<<child channel fd %d>>", m_channel);

	return true;
}

void Child::handle_msg(const char* msg) {
//TODO How to handle message is decided by you.
}

void Child::listen_msg() {
	fd_set rfds;

	int retry = 0;

	while (1) {
		FD_ZERO(&rfds);

		FD_SET(m_channel, &rfds);

		timeval timeout = { 3, 0 };

		int r = select(m_channel + 1, &rfds, NULL, NULL, &timeout);

		if (r > 0) {
			char pkg[256] = { 0 };

			if (FD_ISSET(m_channel, &rfds)) {
				read_from_channel(pkg, sizeof(pkg));

				LOGE("<<A message comes:%s>>", pkg);

				handle_msg((const char*) pkg);
			}
		}
	}
}

void Child::do_work() {
	LOGE("child start monitor");
	start_parent_monitor(); //启动监视线程

	if (create_channel())  //等待并且处理来自父进程发送的消息
	{
		listen_msg();
	}
}
