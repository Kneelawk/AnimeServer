package com.kneelawk.animeservlet.codec;

import com.google.common.collect.ImmutableMap;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Kneelawk on 7/29/19.
 */
public class MultiPartResponseBodyMessageConverter extends AbstractHttpMessageConverter<MultiPartResponseBody> {

    private HttpMessageConverter<?> converter;

    public MultiPartResponseBodyMessageConverter() {
        this(new ResourceRegionHttpMessageConverter());
    }

    public MultiPartResponseBodyMessageConverter(HttpMessageConverter<?> converter) {
        super(MultiPartResponseBody.MULTIPART_BYTERANGES);
        this.converter = converter;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return MultiPartResponseBody.class.isAssignableFrom(clazz);
    }

    @Override
    protected MultiPartResponseBody readInternal(Class<? extends MultiPartResponseBody> clazz,
                                                 HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("MultiPartResponseBody reading is not supported");
    }

    @Override
    protected void writeInternal(MultiPartResponseBody multiPartResponseBody, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        List<ResourceRegion> regions = multiPartResponseBody.getParts();
        int regionCount = regions.size();
        long fileSize = multiPartResponseBody.getContentLength();
        MediaType fileType = multiPartResponseBody.getContentType();
        HttpHeaders headers = outputMessage.getHeaders();
        if (regionCount == 1) {
            headers.setContentType(fileType);

            ResourceRegion region = regions.get(0);
            long len = region.getCount();
            long start = region.getPosition();
            long end = start + len - 1;
            headers.set("Content-Range", start + "-" + end + "/" + fileSize);
            headers.setContentLength(len);

            writeResourceRegion(region, outputMessage.getBody());
        } else if (regionCount > 1) {
            String boundary = MimeTypeUtils.generateMultipartBoundaryString();
            String boundaryString = "\r\n--" + boundary + "\r\n";
            String endBoundaryString = "\r\n--" + boundary + "--\r\n";
            headers.setContentType(
                    new MediaType(MultiPartResponseBody.MULTIPART_BYTERANGES, ImmutableMap.of("boundary", boundary)));

            List<String> regionHeaders = regions.stream().map(region -> {
                long len = region.getCount();
                long start = region.getPosition();
                long end = start + len - 1;
                StringBuilder header = new StringBuilder();
                header.append("Content-Type: ").append(fileType.toString()).append("\r\n");
                header.append("Content-Range: ").append(start).append('-').append(end).append('/').append(fileSize)
                        .append("\r\n");
                // don't forget the 2nd carriage return
                header.append("\r\n");
                return header.toString();
            }).collect(Collectors.toList());

            long contentLength =
                    // the length of all the headers:
                    regionHeaders.stream().mapToInt(String::length).sum() +
                            // the length of every body part:
                            regions.stream().mapToLong(ResourceRegion::getCount).sum() +
                            // the length of every boundary:
                            boundaryString.length() * regionCount +
                            // the length of the last boundary:
                            endBoundaryString.length();

            headers.setContentLength(contentLength);

            try (OutputStream os = outputMessage.getBody();
                 PrintStream osPrinter = new PrintStream(os)) {

                for (int i = 0; i < regionCount; i++) {
                    osPrinter.print(boundaryString);
                    osPrinter.print(regionHeaders.get(i));
                    osPrinter.flush();

                    writeResourceRegion(regions.get(i), os);
                }

                osPrinter.print(endBoundaryString);
                osPrinter.flush();
            }
        } else {
            headers.setContentLength(0);
        }
    }

    private void writeResourceRegion(ResourceRegion region, OutputStream os) throws IOException {
        long start = region.getPosition();
        long length = region.getCount();

        try (InputStream is = region.getResource().getInputStream()) {
            long skipped = is.skip(start);
            if (skipped < start) {
                throw new IOException("Skipped only " + skipped + " bytes out of " + start + " required");
            }

            byte[] buf = new byte[StreamUtils.BUFFER_SIZE];
            long copied = 0;
            while (copied < length) {
                int bytesToRead = (int) Math.min(length - copied, buf.length);
                int read = is.read(buf, 0, bytesToRead);
                os.write(buf, 0, read);
                copied += read;

                if (read < bytesToRead) {
                    // we've reached the end of the file
                    break;
                }
            }
            // we close the resource's stream when we're done because we're the ones managing it
        }
    }
}
