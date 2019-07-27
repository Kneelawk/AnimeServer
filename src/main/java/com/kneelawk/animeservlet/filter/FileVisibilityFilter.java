package com.kneelawk.animeservlet.filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Kneelawk on 7/27/19.
 */
public class FileVisibilityFilter {
    private final FilterType filterType;
    private final Pattern filenameFilter;
    private final List<String> mimeTypes;

    public FileVisibilityFilter(FilterType filterType, Pattern filenameFilter, List<String> mimeTypes) {
        this.filterType = filterType;
        this.filenameFilter = filenameFilter;
        this.mimeTypes = mimeTypes;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public Pattern getFilenameFilter() {
        return filenameFilter;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public boolean accept(Path path) throws IOException {
        switch (filterType) {
        case FILENAME_WHITELIST:
            return filenameFilter.matcher(path.toString()).matches();
        case FILENAME_BLACKLIST:
            return !filenameFilter.matcher(path.toString()).matches();
        case MIME_TYPE_WHITELIST: {
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                if (Files.isDirectory(path)) {
                    return mimeTypes.contains("inode/directory");
                } else {
                    return mimeTypes.contains("application/octet-stream");
                }
            }
            return mimeTypes.contains(contentType);
        }
        case MIME_TYPE_BLACKLIST: {
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                if (Files.isDirectory(path)) {
                    return !mimeTypes.contains("inode/directory");
                } else {
                    return !mimeTypes.contains("application/octet-stream");
                }
            }
            return !mimeTypes.contains(contentType);
        }
        default:
            throw new IllegalStateException("Unexpected value: " + filterType);
        }
    }
}
