package com.kneelawk.animeservlet.fileserver;

import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by Kneelawk on 7/26/19.
 */
@PropertySource("classpath:animeserver.properties")
@RestController
public class FileServerController {
    @Value("${fileserver.anime.path}")
    private Path BASE_PATH;

    @Value("${fileserver.anime.blacklist}")
    private Pattern FILE_BLACKLIST;

    @GetMapping("/files/**")
    public ResponseEntity<Resource> fileServerGet(HttpServletRequest request, @RequestHeader(HttpHeaders.IF_NONE_MATCH)
            Optional<String> ifNoneMatch, @RequestHeader(HttpHeaders.IF_MODIFIED_SINCE) Optional<Date> ifModifiedSince)
            throws IOException {
        String path = request.getRequestURI().replaceFirst("/files/", "");

        // decode the path
        String decodedPath = UriUtils.decode(path, StandardCharsets.UTF_8);

        // remove leading slashes
        if (decodedPath.startsWith("/")) {
            decodedPath = decodedPath.replaceFirst("/+", "");
        }

        // resolve the path
        Path fullPath = BASE_PATH.resolve(decodedPath);

        // check if the path exists
        if (!Files.exists(fullPath)) {
            throw new FileServerNotFoundException(decodedPath + " does not exist");
        }

        // make sure it is within the legal area
        if (!fullPath.startsWith(BASE_PATH)) {
            throw new FileServerNotFoundException(decodedPath + " does not exist");
        }

        // make sure it really is a file
        if (!Files.isRegularFile(fullPath)) {
            throw new FileServerNotFoundException(decodedPath + " does not exist");
        }

        // ignore files on the blacklist
        if (FILE_BLACKLIST.matcher(fullPath.toString()).matches()) {
            throw new FileServerNotFoundException(decodedPath + " does not exist");
        }

        // calculate file details
        String fileETag = getFileETag(fullPath);
        Instant lastModified = Files.getLastModifiedTime(fullPath).toInstant();
        long contentLength = Files.size(fullPath);
        MediaType contentType = MediaType.parseMediaType(Files.probeContentType(fullPath));

        // avoid re-sending files if their eTag matches
        if (ifNoneMatch.isPresent()) {
            if (fileETag.equals(ifNoneMatch.get())) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(fileETag).lastModified(lastModified)
                        .contentLength(contentLength).contentType(contentType).body(null);
            }
        }

        // avoid re-sending files if the client already has the latest version
        if (ifModifiedSince.isPresent()) {
            if (ifModifiedSince.get().toInstant().isAfter(lastModified)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(fileETag).lastModified(lastModified)
                        .contentLength(contentLength).contentType(contentType).body(null);
            }
        }

        return ResponseEntity.ok().eTag(fileETag).lastModified(lastModified).contentLength(contentLength)
                .contentType(contentType).body(new FileSystemResource(fullPath));
    }

    private String getFileETag(Path path) throws IOException {
        return "\"" + com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha512()).toString() + "\"";
    }
}
