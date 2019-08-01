package com.kneelawk.animeservlet.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by Kneelawk on 7/29/19.
 */
public class MultiPartBody {
    public static final MediaType MULTIPART_BYTERANGES = MediaType.parseMediaType("multipart/byteranges");

    private final List<ResourceRegion> parts;
    private final long contentLength;
    private final MediaType contentType;

    private MultiPartBody(List<ResourceRegion> parts, long contentLength,
                          MediaType contentType) {
        this.parts = parts;
        this.contentLength = contentLength;
        this.contentType = contentType;
    }

    public List<ResourceRegion> getParts() {
        return parts;
    }

    public long getContentLength() {
        return contentLength;
    }

    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiPartBody body = (MultiPartBody) o;
        return contentLength == body.contentLength &&
                parts.equals(body.parts) &&
                contentType.equals(body.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, contentLength, contentType);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("parts", parts)
                .append("contentLength", contentLength)
                .append("contentType", contentType)
                .toString();
    }

    public static MultiPartBody create(Iterable<ResourceRegion>
                                               parts, long contentLength,
                                       MediaType contentType) {
        if (contentType == null)
            throw new NullPointerException("ContentType cannot be null");
        return new MultiPartBody(ImmutableList.copyOf(parts), contentLength,
                contentType);
    }

    public static class Builder {
        private List<ResourceRegion> parts =
                Lists.newArrayList();
        private long contentLength;
        private MediaType contentType;

        public Builder() {
        }

        public Builder(List<ResourceRegion> parts, long contentLength,
                       MediaType contentType) {
            this.parts.addAll(parts);
            this.contentLength = contentLength;
            this.contentType = contentType;
        }

        public MultiPartBody build() {
            if (contentType == null)
                throw new IllegalStateException("No contentType specified");
            return new MultiPartBody(ImmutableList.copyOf(parts), contentLength,
                    contentType);
        }

        public List<ResourceRegion> getParts() {
            return parts;
        }

        public Builder setParts(List<ResourceRegion> parts) {
            this.parts.clear();
            this.parts.addAll(parts);
            return this;
        }

        public Builder addPart(ResourceRegion part) {
            parts.add(part);
            return this;
        }

        public Builder addPart(ResourceRegion... parts) {
            this.parts.addAll(Arrays.asList(parts));
            return this;
        }

        public Builder addParts(Collection<ResourceRegion> parts) {
            this.parts.addAll(parts);
            return this;
        }

        public long getContentLength() {
            return contentLength;
        }

        public Builder setContentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public MediaType getContentType() {
            return contentType;
        }

        public Builder setContentType(MediaType contentType) {
            this.contentType = contentType;
            return this;
        }
    }
}
