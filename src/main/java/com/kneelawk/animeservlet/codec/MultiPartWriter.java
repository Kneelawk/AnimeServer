package com.kneelawk.animeservlet.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Created by Kneelawk on 7/31/19.
 */
public class MultiPartWriter implements HttpMessageWriter<MultiPartBody> {
    @Override
    public List<MediaType> getWritableMediaTypes() {
        return ImmutableList.of(MultiPartBody.MULTIPART_BYTERANGES);
    }

    @Override
    public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
        return MultiPartBody.class.isAssignableFrom(elementType.toClass()) &&
                (mediaType == null || MultiPartBody.MULTIPART_BYTERANGES.isCompatibleWith(mediaType));
    }

    @Override
    public Mono<Void> write(Publisher<? extends MultiPartBody> inputStream, ResolvableType elementType,
                            MediaType mediaType, ReactiveHttpOutputMessage message, Map<String, Object> hints) {
        return Mono.from(inputStream).flatMap(body -> {
            HttpHeaders headers = message.getHeaders();
            DataBufferFactory bufferFactory = message.bufferFactory();

            List<ResourceRegion> regions = body.getParts();
            int regionCount = regions.size();
            long fileSize = body.getContentLength();
            MediaType fileType = body.getContentType();

            if (regionCount == 1) {
                headers.setContentType(fileType);

                ResourceRegion region = regions.get(0);
                long len = region.getCount();
                headers.setContentLength(len);

                long start = region.getPosition();
                long end = start + len - 1;
                headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);

                return message.writeWith(writeResourceRegion(region, bufferFactory));
            } else if (regionCount > 1) {
                String boundary = MimeTypeUtils.generateMultipartBoundaryString();
                byte[] startBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes(StandardCharsets.US_ASCII);
                byte[] endBoundaryBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII);
                headers.setContentType(
                        new MediaType(MultiPartBody.MULTIPART_BYTERANGES, ImmutableMap.of("boundary", boundary)));

                // pre-generate the headers for each region so we can accurately guess the length of the entire content being sent
                List<byte[]> regionHeaders = regions.stream().map(region -> {
                    long len = region.getCount();
                    long start = region.getPosition();
                    long end = start + len - 1;
                    StringBuilder header = new StringBuilder();
                    header.append("Content-Type: ").append(fileType.toString()).append("\r\n");
                    header.append("Content-Range: bytes ").append(start).append('-').append(end).append('/')
                            .append(fileSize).append("\r\n");
                    header.append("\r\n");
                    return header.toString().getBytes(StandardCharsets.US_ASCII);
                }).collect(ImmutableList.toImmutableList());

                long contentLength =
                        // the length of all the headers:
                        regionHeaders.stream().mapToInt(a -> a.length).sum() +
                                // the length of every body part:
                                regions.stream().mapToLong(ResourceRegion::getCount).sum() +
                                // the length of every boundary:
                                startBoundaryBytes.length * regionCount +
                                // the length of the last boundary:
                                endBoundaryBytes.length;

                headers.setContentLength(contentLength);

                return message.writeWith(
                        Flux.zip(Flux.fromIterable(regions), Flux.fromIterable(regionHeaders)).concatMap(tup -> {
                            ResourceRegion region = tup.getT1();
                            byte[] header = tup.getT2();

                            Flux<DataBuffer> prefix = Flux.just(
                                    bufferFactory.wrap(startBoundaryBytes),
                                    bufferFactory.wrap(header));

                            return prefix.concatWith(writeResourceRegion(region, bufferFactory));
                        }).concatWithValues(bufferFactory.wrap(endBoundaryBytes))
                                .doOnDiscard(PooledDataBuffer.class, PooledDataBuffer::release));
            } else {
                headers.setContentLength(0);
                return message.writeWith(Mono.empty());
            }
        });
    }

    private Flux<DataBuffer> writeResourceRegion(ResourceRegion region, DataBufferFactory bufferFactory) {
        long start = region.getPosition();
        long length = region.getCount();
        return DataBufferUtils.takeUntilByteCount(
                DataBufferUtils.read(region.getResource(), start, bufferFactory, StreamUtils.BUFFER_SIZE), length);
    }
}
