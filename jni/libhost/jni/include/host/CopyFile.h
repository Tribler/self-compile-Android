#ifndef _HOST_COPYFILE_H
#define _HOST_COPYFILE_H

#include <stdbool.h>
#include <sys/stat.h>

#if __cplusplus
extern "C" {
#endif

// command line options
enum {
    COPY_NO_DEREFERENCE = 0x00010000, // copy symlink link instead of target
    COPY_TRY_EXE        = 0x00020000, // on Win32, try adding '.exe' to filename
    COPY_FORCE          = 0x00040000, // override access permissions
    COPY_PERMISSIONS    = 0x00080000, // preserve mode, ownership, timestamps
    COPY_TIMESTAMPS     = 0x00100000, // preserve mode, ownership, timestamps
    COPY_RECURSIVE      = 0x00200000, // copy directories
    COPY_UPDATE_ONLY    = 0x00400000, // only copy if source file is newer
    COPY_VERBOSE_MASK   = 0x000000ff  // talk lots
};

int copyFile(const char* src, const char* dst, unsigned int options);

#if __cplusplus
} // extern "C"
#endif

#endif // _HOST_COPYFILE_H

