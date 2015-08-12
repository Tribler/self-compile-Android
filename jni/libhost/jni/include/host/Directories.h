#ifndef HOST_MKDIRS_H
#define HOST_MKDIRS_H

#include <string>

std::string parent_dir(const std::string& path);

extern "C" int mkdirs(const char* path);

#endif // HOST_MKDIRS_H
