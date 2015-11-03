/*
 * Copyright 2005 The Android Open Source Project
 *
 * Android "cp" replacement.
 *
 * The GNU/Linux "cp" uses O_LARGEFILE in its open() calls, utimes() instead
 * of utime(), and getxattr()/setxattr() instead of chmod().  These are
 * probably "better", but are non-portable, and not necessary for our
 * purposes.
 */
#include <host/CopyFile.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <getopt.h>
#include <dirent.h>
#include <fcntl.h>
#include <utime.h>
#include <limits.h>
#include <errno.h>
#include <assert.h>

#ifdef HAVE_MS_C_RUNTIME
#  define  mkdir(path,mode)   _mkdir(path)
#endif

#ifndef HAVE_SYMLINKS
#  define  lstat              stat
#  ifndef EACCESS   /* seems to be missing from the Mingw headers */
#    define  EACCESS            13
#  endif
#endif

#ifndef O_BINARY
#  define  O_BINARY  0
#endif

/*#define DEBUG_MSGS*/
#ifdef DEBUG_MSGS
# define DBUG(x) printf x
#else
# define DBUG(x) ((void)0)
#endif

#define FSSEP '/'       /* filename separator char */

static int copyFileRecursive(const char* src, const char* dst, bool isCmdLine, unsigned int options);

/*
 * Returns true if the source file is newer than the destination file.
 *
 * The check is based on the modification date, whole seconds only.  This
 * also returns true if the file sizes don't match.
 */
static bool isSourceNewer(const struct stat* pSrcStat, const struct stat* pDstStat)
{
    return (pSrcStat->st_mtime > pDstStat->st_mtime) ||
           (pSrcStat->st_size != pDstStat->st_size);
}

/*
 * Returns true if the source file has high resolution modification
 * date.  Cygwin doesn't support st_mtim in normal build, so always
 * return false.
 */
static bool isHiresMtime(const struct stat* pSrcStat)
{
#if defined(WIN32) || defined(USE_MINGW)
    return 0;
#elif defined(MACOSX_RSRC)
    return pSrcStat->st_mtimespec.tv_nsec > 0;
#else
    return pSrcStat->st_mtim.tv_nsec > 0;
#endif
}

/*
 * Returns true if the source and destination files are actually the
 * same thing.  We detect this by checking the inode numbers, which seems
 * to work on Cygwin.
 */
static bool isSameFile(const struct stat* pSrcStat, const struct stat* pDstStat)
{
#ifndef HAVE_VALID_STAT_ST_INO
    /* with MSVCRT.DLL, stat always sets st_ino to 0, and there is no simple way to */
	/* get the equivalent information with Win32 (Cygwin does some weird stuff in   */
	/* its winsup/cygwin/fhandler_disk_file.cc to emulate this, too complex for us) */
	return 0;
#else
    return (pSrcStat->st_ino == pDstStat->st_ino);
#endif
}

static void printCopyMsg(const char* src, const char* dst, unsigned int options)
{
    if ((options & COPY_VERBOSE_MASK) > 0)
        printf("    '%s' --> '%s'\n", src, dst);
}

static void printNotNewerMsg(const char* src, const char* dst, unsigned int options)
{
    if ((options & COPY_VERBOSE_MASK) > 1)
        printf("    '%s' is up-to-date\n", dst);
}

/*
 * Copy the contents of one file to another.
 *
 * The files are assumed to be seeked to the start.
 */
static int copyFileContents(const char* dst, int dstFd, const char* src, int srcFd)
{
    unsigned char buf[8192];
    ssize_t readCount, writeCount;

    /*
     * Read a chunk, write it, and repeat.
     */
    while (1) {
        readCount = read(srcFd, buf, sizeof(buf));
        if (readCount < 0) {
            fprintf(stderr,
                "acp: failed reading '%s': %s\n", src, strerror(errno));
            return -1;
        }

        if (readCount > 0) {
            writeCount = write(dstFd, buf, readCount);
            if (writeCount < 0) {
                fprintf(stderr,
                    "acp: failed writing '%s': %s\n", dst, strerror(errno));
                return -1;
            }
            if (writeCount != readCount) {
                fprintf(stderr, "acp: partial write to '%s' (%d of %d)\n",
                    dst, writeCount, readCount);
                return -1;
            }
        }

        if (readCount < (ssize_t) sizeof(buf))
            break;
    }

    return 0;
}

/*
 * Set the permissions, owner, and timestamps on the destination file
 * equal to those of the source file.
 *
 * Failures here are "soft"; they don't produce warning messages and don't
 * cause the cp command to report a failure.
 */
static int setPermissions(const char* dst, const struct stat* pSrcStat, unsigned int options)
{
    struct utimbuf ut;

    if (options & COPY_TIMESTAMPS) {
        /*
         * Start with timestamps.  The access and mod dates are not affected
         * by the next operations.
         */
        ut.actime = pSrcStat->st_atime;
        ut.modtime = pSrcStat->st_mtime;
        if (isHiresMtime(pSrcStat))
            ut.modtime += 1;
        if (utime(dst, &ut) != 0) {
            DBUG(("---   unable to set timestamps on '%s': %s\n",
                dst, strerror(errno)));
        }
    }

    if (options & COPY_PERMISSIONS) {
        /*
         * Set the permissions.
         */
        if (chmod(dst, pSrcStat->st_mode & ~(S_IFMT)) != 0) {
            DBUG(("---   unable to set perms on '%s' to 0%o: %s\n",
                dst, pSrcStat->st_mode & ~(S_IFMT), strerror(errno)));
        }
#ifndef HAVE_MS_C_RUNTIME
        /*
         * Set the owner.
         */
        if (chown(dst, pSrcStat->st_uid, pSrcStat->st_gid) != 0) {
            DBUG(("---   unable to set owner of '%s' to %d/%d: %s\n",
                dst, pSrcStat->st_uid, pSrcStat->st_gid, strerror(errno)));
        }
#endif
    }

    return 0;
}

/*
 * Copy a regular file.  If the destination file exists and is not a
 * regular file, we fail.  However, we use stat() rather than lstat(),
 * because it's okay to write through a symlink (the noDereference stuff
 * only applies to the source file).
 *
 * If the file doesn't exist, create it.  If it does exist, truncate it.
 */
static int copyRegular(const char* src, const char* dst, const struct stat* pSrcStat, unsigned int options)
{
    struct stat dstStat;
    int srcFd, dstFd, statResult, copyResult;

    DBUG(("--- copying regular '%s' to '%s'\n", src, dst));

    statResult = stat(dst, &dstStat);
    if (statResult == 0 && !S_ISREG(dstStat.st_mode)) {
        fprintf(stderr,
            "acp: destination '%s' exists and is not regular file\n",
            dst);
        return -1;
    } else if (statResult != 0 && errno != ENOENT) {
        fprintf(stderr, "acp: unable to stat destination '%s'\n", dst);
        return -1;
    }

    if (statResult == 0) {
        if (isSameFile(pSrcStat, &dstStat)) {
            fprintf(stderr, "acp: '%s' and '%s' are the same file\n",
                src, dst);
            return -1;
        }
        if (options & COPY_UPDATE_ONLY) {
            if (!isSourceNewer(pSrcStat, &dstStat)) {
                DBUG(("---  source is not newer: '%s'\n", src));
                printNotNewerMsg(src, dst, options);
                return 0;
            }
        }
    }

    /* open src */
    srcFd = open(src, O_RDONLY | O_BINARY, 0);
    if (srcFd < 0) {
        fprintf(stderr, "acp: unable to open '%s': %s\n", src, strerror(errno));
        return -1;
    }

    /* open dest with O_CREAT | O_TRUNC */
    DBUG(("---  opening '%s'\n", dst));
    dstFd = open(dst, O_CREAT | O_TRUNC | O_WRONLY | O_BINARY, 0644);

    if (dstFd < 0) {
        if (errno == ENOENT) {
            /* this happens if the target directory doesn't exist */
            fprintf(stderr,
                "acp: cannot create '%s': %s\n", dst, strerror(errno));
            (void) close(srcFd);
            return -1;
        }

        /* if "force" is set, try removing the destination file and retry */
        if (options & COPY_FORCE) {
            if (unlink(dst) != 0) {
#ifdef HAVE_MS_C_RUNTIME
				/* MSVCRT.DLL unlink will fail with EACCESS if the file is set read-only */
				/* so try to change its mode, and unlink again                           */
				if (errno == EACCESS) {
					if (chmod(dst, S_IWRITE|S_IREAD) == 0 && unlink(dst) == 0)
						goto Open_File;
				}
#endif		
                fprintf(stderr, "acp: unable to remove '%s': %s\n",
                    dst, strerror(errno));
                (void) close(srcFd);
                return -1;
            }
#ifdef HAVE_MS_C_RUNTIME
        Open_File:
#endif			
            dstFd = open(dst, O_CREAT | O_TRUNC | O_WRONLY | O_BINARY, 0644);
        }
    }
    if (dstFd < 0) {
        fprintf(stderr, "acp: unable to open '%s': %s\n",
            dst, strerror(errno));
        (void) close(srcFd);
        return -1;
    }

    copyResult = copyFileContents(dst, dstFd, src, srcFd);

    (void) close(srcFd);
    (void) close(dstFd);
    if (copyResult != 0)
        return -1;

#ifdef MACOSX_RSRC
    {
        char* srcRsrcName = NULL;
        char* dstRsrcName = NULL;
        struct stat rsrcStat;

        srcRsrcName = malloc(strlen(src) + 5 + 1);
        strcpy(srcRsrcName, src);
        strcat(srcRsrcName, "/rsrc");

        dstRsrcName = malloc(strlen(dst) + 5 + 1);
        strcpy(dstRsrcName, dst);
        strcat(dstRsrcName, "/rsrc");

        if (stat(srcRsrcName, &rsrcStat) == 0 && rsrcStat.st_size > 0) {
            DBUG(("---  RSRC: %s --> %s\n", srcRsrcName, dstRsrcName));

            srcFd = open(srcRsrcName, O_RDONLY);
            dstFd = open(dstRsrcName, O_TRUNC | O_WRONLY, 0);
            copyResult = -1;
            if (srcFd >= 0 && dstFd >= 0) {
                copyResult = copyFileContents(dstRsrcName, dstFd,
                    srcRsrcName, srcFd);
                (void) close(srcFd);
                (void) close(dstFd);
            }

            if (copyResult != 0)
                return -1;
        }

        free(srcRsrcName);
        free(dstRsrcName);
    }
#endif

    setPermissions(dst, pSrcStat, options);

    printCopyMsg(src, dst, options);

    return 0;
}


#ifdef HAVE_SYMLINKS
/*
 * Copy a symlink.  This only happens if we're in "no derefence" mode,
 * in which we copy the links rather than the files that are pointed at.
 *
 * We always discard the destination file.  If it's a symlink already,
 * we want to throw it out and replace it.  If it's not a symlink, we
 * need to trash it so we can create one.
 */
static int copySymlink(const char* src, const char* dst, const struct stat* pSrcStat, unsigned int options)
{
    struct stat dstStat;
    char linkBuf[PATH_MAX+1];
    int statResult, nameLen;

    assert(options & COPY_NO_DEREFERENCE);
    DBUG(("--- copying symlink '%s' to '%s'\n", src, dst));

    /* NOTE: we use lstat() here */
    statResult = lstat(dst, &dstStat);
    if (statResult == 0 && !S_ISREG(dstStat.st_mode)
                         && !S_ISLNK(dstStat.st_mode)
						 )
    {
        fprintf(stderr,
            "acp: destination '%s' exists and is not regular or symlink\n",
            dst);
        return -1;
    }

    if (statResult == 0) {
        if (isSameFile(pSrcStat, &dstStat)) {
            fprintf(stderr, "acp: '%s' and '%s' are the same file\n",
                src, dst);
            return -1;
        }
        if (options & COPY_UPDATE_ONLY) {
            if (!isSourceNewer(pSrcStat, &dstStat)) {
                DBUG(("---  source is not newer: '%s'\n", src));
                printNotNewerMsg(src, dst, options);
                return 0;
            }
        }
    }

    /* extract the symlink contents */
    nameLen = readlink(src, linkBuf, sizeof(linkBuf)-1);
    if (nameLen <= 0) {
        fprintf(stderr, "acp: unable to read symlink '%s': %s\n",
            src, strerror(errno));
        return -1;
    }
    linkBuf[nameLen] = '\0';
    DBUG(("--- creating symlink file '%s' (--> %s)\n", dst, linkBuf));

    if (statResult == 0) {
        DBUG(("---  removing '%s'\n", dst));
        if (unlink(dst) != 0) {
            fprintf(stderr, "acp: unable to remove '%s': %s\n",
                dst, strerror(errno));
            return -1;
        }
    }

    if (symlink(linkBuf, dst) != 0) {
        fprintf(stderr, "acp: unable to create symlink '%s' [%s]: %s\n",
            dst, linkBuf, strerror(errno));
        return -1;
    }

    /*
     * There's no way to set the file date or access permissions, but
     * it is possible to set the owner.
     */
    if (options & COPY_PERMISSIONS) {
        if (lchown(dst, pSrcStat->st_uid, pSrcStat->st_gid) != 0)
            DBUG(("---  lchown failed: %s\n", strerror(errno)));
    }

    printCopyMsg(src, dst, options);

    return 0;
}
#endif /* HAVE_SYMLINKS */

/*
 * Copy the contents of one directory to another.  Both "src" and "dst"
 * must be directories.  We will create "dst" if it does not exist.
 */
int copyDirectory(const char* src, const char* dst, const struct stat* pSrcStat, unsigned int options)
{
    int retVal = 0;
    struct stat dstStat;
    DIR* dir;
    int cc, statResult;

    DBUG(("--- copy dir '%s' to '%s'\n", src, dst));

    statResult = stat(dst, &dstStat);
    if (statResult == 0 && !S_ISDIR(dstStat.st_mode)) {
        fprintf(stderr,
            "acp: destination '%s' exists and is not a directory\n", dst);
        return -1;
    } else if (statResult != 0 && errno != ENOENT) {
        fprintf(stderr, "acp: unable to stat destination '%s'\n", dst);
        return -1;
    }

    if (statResult == 0) {
        if (isSameFile(pSrcStat, &dstStat)) {
            fprintf(stderr,
                "acp: cannot copy directory into itself ('%s' and '%s')\n",
                src, dst);
            return -1;
        }
    } else {
        DBUG(("---  creating dir '%s'\n", dst));
        cc = mkdir(dst, 0755);
        if (cc != 0) {
            fprintf(stderr, "acp: unable to create directory '%s': %s\n",
                dst, strerror(errno));
            return -1;
        }

        /* only print on mkdir */
        printCopyMsg(src, dst, options);
    }

    /*
     * Open the directory, and plow through its contents.
     */
    dir = opendir(src);
    if (dir == NULL) {
        fprintf(stderr, "acp: unable to open directory '%s': %s\n",
            src, strerror(errno));
        return -1;
    }

    while (1) {
        struct dirent* ent;
        char* srcFile;
        char* dstFile;
        int srcLen, dstLen, nameLen;

        ent = readdir(dir);
        if (ent == NULL)
            break;

        if (strcmp(ent->d_name, ".") == 0 ||
            strcmp(ent->d_name, "..") == 0)
        {
            continue;
        }

        nameLen = strlen(ent->d_name);
        srcLen = strlen(src);
        dstLen = strlen(dst);

        srcFile = malloc(srcLen +1 + nameLen +1);
        memcpy(srcFile, src, srcLen);
        srcFile[srcLen] = FSSEP;
        memcpy(srcFile + srcLen+1, ent->d_name, nameLen +1);

        dstFile = malloc(dstLen +1 + nameLen +1);
        memcpy(dstFile, dst, dstLen);
        dstFile[dstLen] = FSSEP;
        memcpy(dstFile + dstLen+1, ent->d_name, nameLen +1);

        if (copyFileRecursive(srcFile, dstFile, false, options) != 0)
            retVal = -1;        /* note failure and keep going */

        free(srcFile);
        free(dstFile);
    }
    closedir(dir);

    setPermissions(dst, pSrcStat, options);

    return retVal;
}

/*
 * Do the actual copy.  This is called recursively from copyDirectory().
 *
 * "dst" should only be a directory if "src" is also a directory.
 *
 * Returns 0 on success.
 */
static int copyFileRecursive(const char* src, const char* dst, bool isCmdLine, unsigned int options)
{
    char* srcExe = NULL;
    char* dstExe = NULL;
    char* dstDir = NULL;
    struct stat srcStat;
    int retVal = 0;
    int statResult, statErrno;

    /*
     * Stat the source file.  If it doesn't exist, fail.
     */
    if (options & COPY_NO_DEREFERENCE)
        statResult = lstat(src, &srcStat);
    else
        statResult = stat(src, &srcStat);
    statErrno = errno;        /* preserve across .exe attempt */

#ifdef WIN32_EXE
    /*
     * Here's the interesting part.  Under Cygwin, if you have a file
     * called "foo.exe", stat("foo", ...) will succeed, but open("foo", ...)
     * will fail.  We need to figure out what its name is supposed to be
     * so we can create the correct destination file.
     *
     * If we don't have the "-e" flag set, we want "acp foo bar" to fail,
     * not automatically find "foo.exe".  That way, if we really were
     * trying to copy "foo", it doesn't grab something we don't want.
     */
    if (isCmdLine && statResult == 0) {
        int tmpFd;
        tmpFd = open(src, O_RDONLY | O_BINARY, 0);
        if (tmpFd < 0) {
            statResult = -1;
            statErrno = ENOENT;
        } else {
            (void) close(tmpFd);
        }
    }

    /*
     * If we didn't find the file, try it again with ".exe".
     */
    if (isCmdLine && statResult < 0 && statErrno == ENOENT && (options & COPY_TRY_EXE)) {
        srcExe = malloc(strlen(src) + 4 +1);
        strcpy(srcExe, src);
        strcat(srcExe, ".exe");

        if (options & COPY_NO_DEREFERENCE)
            statResult = lstat(srcExe, &srcStat);
        else
            statResult = stat(srcExe, &srcStat);

        if (statResult == 0 && !S_ISREG(srcStat.st_mode))
            statResult = -1;        /* fail, use original statErrno below */

        if (statResult == 0) {
            /* found a .exe, copy that instead */
            dstExe = malloc(strlen(dst) + 4 +1);
            strcpy(dstExe, dst);
            strcat(dstExe, ".exe");

            src = srcExe;
            dst = dstExe;
        } else {
            DBUG(("---  couldn't find '%s' either\n", srcExe));
        }
    }
#endif
    if (statResult < 0) {
        if (statErrno == ENOENT)
            fprintf(stderr, "acp: file '%s' does not exist\n", src);
        else
            fprintf(stderr, "acp: unable to stat '%s': %s\n",
                src, strerror(statErrno));
        retVal = -1;
        goto bail;
    }

    /*
     * If "src" is a directory, ignore it if "recursive" isn't set.
     *
     * We want to create "dst" as a directory (or verify that it already
     * exists as a directory), and then copy its contents.
     */
    if (S_ISDIR(srcStat.st_mode)) {
        if (!(options & COPY_RECURSIVE)) {
            fprintf(stderr, "acp: omitting directory '%s'\n", src);
        } else {
            retVal = copyDirectory(src, dst, &srcStat, options);
        }
#ifdef HAVE_SYMLINKS
    } else if (S_ISLNK(srcStat.st_mode)) {
        retVal = copySymlink(src, dst, &srcStat, options);
#endif		
    } else if (S_ISREG(srcStat.st_mode)) {
        retVal = copyRegular(src, dst, &srcStat, options);
    } else {
        fprintf(stderr, "acp: skipping unusual file '%s' (mode=0%o)\n",
            src, srcStat.st_mode);
        retVal = -1;
    }

bail:
    free(srcExe);
    free(dstExe);
    free(dstDir);
    return retVal;
}

int copyFile(const char* src, const char* dst, unsigned int options)
{
    return copyFileRecursive(src, dst, true, options);
}


