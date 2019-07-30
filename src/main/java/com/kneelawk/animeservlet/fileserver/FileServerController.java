package com.kneelawk.animeservlet.fileserver;

import com.google.common.hash.Hashing;
import com.kneelawk.animeservlet.NotFoundException;
import com.kneelawk.animeservlet.codec.MultiPartResponseBody;
import com.kneelawk.animeservlet.filter.FileVisibilityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by Kneelawk on 7/26/19.
 */
@PropertySource("classpath:animeserver.properties")
@RestController
public class FileServerController {
    private static final URI FILE_SERVER_IDENTIFIER = URI.create("files/");
    private static final Pattern RANGE_RANGES = Pattern.compile("([0-9]*)-([0-9]*)");

    @Value("${animeserver.filesystem.path}")
    private Path BASE_PATH;

    @Autowired
    private FileVisibilityFilter filter;

    @GetMapping("/files/**")
    public ResponseEntity<Object> fileServerGet(HttpServletRequest request, @RequestHeader(HttpHeaders.IF_NONE_MATCH)
            Optional<String> ifNoneMatch, @RequestHeader(HttpHeaders.IF_MODIFIED_SINCE) Optional<Date> ifModifiedSince,
                                                @RequestHeader(HttpHeaders.IF_RANGE) Optional<String> ifRange,
                                                @RequestHeader(HttpHeaders.RANGE) Optional<String> rangeHeader)
            throws IOException {
        String contextString = request.getContextPath();
        if (!contextString.endsWith("/")) {
            contextString = contextString + "/";
        }
        URI context = URI.create(contextString);
        URI fileServerBase = context.resolve(FILE_SERVER_IDENTIFIER);

        URI requestUri = URI.create(request.getRequestURI());

        URI path;
        if (requestUri.equals(context.resolve("files"))) {
            path = URI.create("");
        } else {
            path = fileServerBase.relativize(requestUri);
        }

        // decode the path
        String decodedPath = UriUtils.decode(path.toString(), StandardCharsets.UTF_8);

        // remove leading slashes
        if (decodedPath.startsWith("/")) {
            decodedPath = decodedPath.replaceFirst("/+", "");
        }

        // resolve the path
        Path fullPath = BASE_PATH.resolve(decodedPath);

        // check if the path exists
        if (!Files.exists(fullPath)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // make sure it is within the legal area
        if (!fullPath.startsWith(BASE_PATH)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // make sure it really is a file
        if (!Files.isRegularFile(fullPath)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // ignore files on the blacklist
        if (!filter.accept(fullPath)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // calculate file details
        String fileETag = getFileETag(fullPath);
        Instant lastModified = Files.getLastModifiedTime(fullPath).toInstant();
        long contentLength = Files.size(fullPath);
        MediaType contentType = Optional.ofNullable(Files.probeContentType(fullPath)).map(MediaType::parseMediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        // avoid re-sending files if their eTag matches
        if (ifNoneMatch.isPresent()) {
            if (fileETag.equals(ifNoneMatch.get())) {
                return setupResponse(HttpStatus.NOT_MODIFIED, fileETag, lastModified, contentLength, contentType)
                        .body(null);
            }
        }

        // avoid re-sending files if the client already has the latest version
        if (ifModifiedSince.isPresent()) {
            if (ifModifiedSince.get().toInstant().isAfter(lastModified)) {
                return setupResponse(HttpStatus.NOT_MODIFIED, fileETag, lastModified, contentLength, contentType)
                        .body(null);
            }
        }

        if (rangeHeader.isPresent()) {
            if (ifRange.isPresent()) {
                try {
                    Instant rangeLastModified = DateFormat.getDateTimeInstance().parse(ifRange.get()).toInstant();
                    if (rangeLastModified.isAfter(lastModified)) {
                        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader.get());
                        if (isSane(ranges, contentLength)) {
                            return rangeResponse(fullPath, fileETag, lastModified, contentLength, contentType, ranges);
                        }
                    }
                } catch (ParseException e) {
                    String rangeETag = ifRange.get();
                    if (fileETag.equals(rangeETag)) {
                        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader.get());
                        if (isSane(ranges, contentLength)) {
                            return rangeResponse(fullPath, fileETag, lastModified, contentLength, contentType, ranges);
                        }
                    }
                }
            } else {
                List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader.get());
                if (isSane(ranges, contentLength)) {
                    return rangeResponse(fullPath, fileETag, lastModified, contentLength, contentType, ranges);
                }
            }
        }

        return setupResponse(HttpStatus.OK, fileETag, lastModified, contentLength, contentType)
                .body(new FileSystemResource(fullPath));
    }

    private ResponseEntity.BodyBuilder setupResponse(HttpStatus status, String fileETag, Instant lastModified,
                                                     long contentLength, MediaType contentType) {
        return ResponseEntity.status(status).eTag(fileETag).lastModified(lastModified).contentLength(contentLength)
                .contentType(contentType).header("Accept-Ranges", "bytes");
    }

    private ResponseEntity<Object> rangeResponse(Path fullPath, String fileETag, Instant lastModified,
                                                 long contentLength, MediaType contentType, List<HttpRange> ranges) {
        MultiPartResponseBody.Builder bodyBuilder = new MultiPartResponseBody.Builder();
        for (HttpRange range : ranges) {
            ResourceRegion resourceRegion = range.toResourceRegion(new FileSystemResource(fullPath));
            bodyBuilder.addPart(resourceRegion);
        }
        bodyBuilder.setContentLength(contentLength);
        bodyBuilder.setContentType(contentType);
        return setupResponse(HttpStatus.PARTIAL_CONTENT, fileETag, lastModified, contentLength,
                MultiPartResponseBody.MULTIPART_BYTERANGES).body(bodyBuilder.build());
    }

    private boolean isSane(List<HttpRange> ranges, long contentLength) {
        if (ranges.size() < 4) {
            return true;
        }
        long location = 0;
        for (HttpRange r : ranges) {
            if (r.getRangeStart(contentLength) < location) {
                return false;
            }
            location = r.getRangeEnd(contentLength);
        }
        return true;
    }

    private String getFileETag(Path path) throws IOException {
        return "\"" + com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha512()).toString() + "\"";
    }
}
