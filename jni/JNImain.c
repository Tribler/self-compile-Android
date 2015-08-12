#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>

#define DEBUG_TAG "JNImain"

jint Java_nl_tudelft_selfcompileapp_Aapt_JNImain(JNIEnv * env, jobject this,
		jstring args) {

	jboolean isCopy;
	const char * szArgs = (*env)->GetStringUTFChars(env, args, &isCopy);
	char *ptr1, *ptr2;
	int i, idx, argc = 1, len;
	jint rc = 99;

	__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG,
			"Native method call: JNImain (%s)", szArgs);
	len = strlen(szArgs);
	for (i = 0; i < len; i++)
		if (szArgs[i] == '\t')
			argc++;
	char * argv[argc];
	ptr1 = ptr2 = (char*) szArgs;
	idx = 0;
	for (i = 0; i < len; i++) {
		if (*ptr2 == '\t') {
			*ptr2 = 0;
			argv[idx] = ptr1;
			idx++;
			ptr1 = ptr2 + 1;
		}
		ptr2++;
	} // for
	argv[idx] = ptr1;

	// redirect stderr and stdout
	freopen("/storage/emulated/0/.JNImain/native_stderr.txt", "w", stderr);
	freopen("/storage/emulated/0/.JNImain/native_stdout.txt", "w", stdout);

	fprintf(stdout, "Aapt arguments:\n");
	for (i = 1; i < argc; i++) {
		fprintf(stdout, "%s\n", argv[i]);
	}

	// call aapt
	rc = main(argc, argv);

	// stopping the redirection
	fclose(stderr);
	fclose(stdout);

	return rc;
}
