package com.kneelawk.animeservlet;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

/**
 * Created by Kneelawk on 7/25/19.
 */
@WebServlet(name = "AnimeServlet", urlPatterns = { "/tree/*" }, loadOnStartup = 1)
public class AnimeServlet extends HttpServlet {
    private static final Path ANIME_PREFIX = Paths.get("/media/jedidiah/anime/");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        String decPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        String context = req.getContextPath();
        String prefix = context + req.getServletPath();

        if (path == null) {
            handleWelcome(resp, context);
        } else {
            handlePath(resp, context, prefix, decPath, path);
        }
    }

    private ContainerTag template(String context, String title, DomContent... contents) {
        return html(
                head(
                        title(title),
                        link().withRel("stylesheet").withType("text/css").withHref(context + "/common.css")
                ),
                body(
                        contents
                )
        );
    }

    private ContainerTag files(String prefix, String path, String... files) {
        String parent = path.replaceFirst("/[^/]+/?$", "/");
        int offset;
        if (path.equals("/")) {
            offset = 0;
        } else {
            offset = 1;
        }
        ContainerTag[] links = new ContainerTag[files.length + offset];
        if (!path.equals("/")) {
            links[0] = div(a("..").withHref(prefix + parent));
        }
        for (int i = 1; i < links.length; i++) {
            links[i] = div(a(files[i - 1])
                    .withHref(prefix + path + URLEncoder.encode(files[i - 1], StandardCharsets.UTF_8)));
        }

        return div(attrs(".files"), links);
    }

    private void handleWelcome(HttpServletResponse resp, String context) throws IOException {
        resp.getWriter().println(template(context, "Anime Server",
                h1("Welcome to my anime server?????"),
                a("Filesystem root:/").withHref(context + "/tree/")
        ).render());
    }

    private void handlePath(HttpServletResponse resp, String context, String prefix, String decPath,
                            String path)
            throws IOException {
        Path file = ANIME_PREFIX.resolve(decPath.replaceFirst("/+", ""));
        if (Files.exists(file)) {
            if (Files.isReadable(file)) {
                if (Files.isDirectory(file)) {
                    if (!path.endsWith("/")) {
                        resp.sendRedirect(prefix + path + "/");
                    } else {
                        handleDirectory(resp, context, prefix, decPath, path, file);
                    }
                } else {
                    handleFile(resp, file);
                }
            } else {
                handleInaccessible(resp, context, prefix, decPath);
            }
        } else {
            handleMissingFile(resp, context, prefix, decPath);
        }
    }

    private void handleDirectory(HttpServletResponse resp, String context, String prefix,
                                 String decPath, String path, Path file) throws IOException {
        String[] children = Files.list(file).map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList())
                .toArray(String[]::new);
        String filename = decPath.equals("/") ? "/" : decPath.replaceFirst(".*/([^/]+/?)", "$1");
        resp.getWriter().println(template(context, filename,
                h1("Directory: " + filename),
                div(attrs(".status"), text("Directory: "), code(decPath)),
                files(prefix, path, children)
        ).render());
    }

    private void handleFile(HttpServletResponse resp, Path file) throws IOException {
        long length = Files.size(file);
        resp.setContentLengthLong(length);
        resp.setContentType(Files.probeContentType(file));
        Files.copy(file, resp.getOutputStream());
        resp.flushBuffer();
    }

    private void handleInaccessible(HttpServletResponse resp, String context, String prefix,
                                    String decPath) throws IOException {
        String filename = decPath.substring(decPath.lastIndexOf('/'));
        resp.getWriter().println(template(context, "Forbidden " + filename,
                h1("Forbidden file: " + filename),
                div(attrs(".status.forbidden"), text("Forbidden file: "), code(decPath)),
                files(prefix, decPath)
        ).render());
        resp.setStatus(403);
    }

    private void handleMissingFile(HttpServletResponse resp, String context, String prefix,
                                   String decPath) throws IOException {
        String filename = decPath.substring(decPath.lastIndexOf('/') + 1);
        resp.getWriter().println(template(context, "Unable to find " + filename,
                h1("Unable to find " + filename),
                div(attrs(".status"), text("Unable to find: "), code(decPath)),
                files(prefix, decPath)
        ).render());
        resp.setStatus(404);
    }
}
