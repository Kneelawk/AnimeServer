package com.kneelawk.animeservlet.navigation;

import com.kneelawk.animeservlet.ForbiddenException;
import com.kneelawk.animeservlet.NotFoundException;
import com.kneelawk.animeservlet.filter.FileVisibilityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Kneelawk on 7/27/19.
 */
@PropertySource("classpath:application.properties")
@Controller
public class NavigationController {
    private static final URI FILE_SERVER_IDENTIFIER = URI.create("/files/");
    private static final URI TREE_SERVER_IDENTIFIER = URI.create("/tree/");

    @Value("${animeserver.filesystem.path}")
    private Path BASE_PATH;

    @Autowired
    private FileVisibilityFilter filter;

    @GetMapping("/tree/**")
    public String navigator(ServerHttpRequest request, Model model) throws IOException {
        URI requestUri = URI.create(request.getPath().pathWithinApplication().value());

        URI path;
        if (requestUri.equals(URI.create("/tree"))) {
            path = URI.create("");
        } else {
            path = TREE_SERVER_IDENTIFIER.relativize(requestUri);
        }

        URI fullPath = TREE_SERVER_IDENTIFIER.resolve(path);

        // decode the path
        String decodedPath = UriUtils.decode(path.toString(), StandardCharsets.UTF_8);

        // remove leading slashes
        if (decodedPath.startsWith("/")) {
            decodedPath = decodedPath.replaceFirst("/+", "");
        }

        // resolve the path
        Path systemPath = BASE_PATH.resolve(decodedPath);

        boolean hasParent = !decodedPath.isEmpty();
        String mimeType = Files.probeContentType(systemPath);

        // check if the path exists
        if (!Files.exists(systemPath)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // make sure it is within the legal area
        if (!systemPath.startsWith(BASE_PATH)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // filter entries that are configured to be filtered
        if (!filter.accept(systemPath)) {
            throw new NotFoundException(decodedPath + " does not exist");
        }

        // check if we can read it
        if (!Files.isReadable(systemPath)) {
            throw new ForbiddenException();
        }

        if (mimeType != null && mimeType.startsWith("video")) {
            URI parentUrl = fullPath.resolve(".");
            URI fileUrl = FILE_SERVER_IDENTIFIER.resolve(path);

            model.addAttribute("file_name", systemPath.getFileName());
            model.addAttribute("file_path", "/" + decodedPath);
            model.addAttribute("has_parent", hasParent);
            model.addAttribute("parent_url", parentUrl);
            model.addAttribute("file_href", fileUrl);

            return "media-file";
        } else if (Files.isDirectory(systemPath)) {
            if (!requestUri.toString().endsWith("/")) {
                return "redirect:" + requestUri.toString() + "/";
            }

            URI parentUrl = fullPath.resolve("..");

            List<DirectoryChild> children = Files.list(systemPath).sorted().filter(path1 -> {
                try {
                    return filter.accept(path1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).map(p -> new DirectoryChild(
                    TREE_SERVER_IDENTIFIER.resolve(BASE_PATH.toUri().relativize(p.toUri())).toString(),
                    p.getFileName().toString())).collect(Collectors.toList());

            model.addAttribute("directory_name", systemPath.getFileName());
            model.addAttribute("directory_path", "/" + decodedPath);
            model.addAttribute("has_parent", hasParent);
            model.addAttribute("parent_url", parentUrl);
            model.addAttribute("children", children);

            return "directory";
        } else {
            URI parentUrl = fullPath.resolve(".");

            model.addAttribute("file_name", systemPath.getFileName());
            model.addAttribute("file_path", "/" + decodedPath);
            model.addAttribute("has_parent", hasParent);
            model.addAttribute("parent_url", parentUrl);

            return "file";
        }
    }
}
