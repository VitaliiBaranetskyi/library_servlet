package com.github.vitaliibaranetskyi.library.controller.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * If encoding for incoming request isn't set, set it to one specified in web.xml config
 * Should be used for all pages (also specified in web.xml)
 */
public class EncodingFilter implements Filter {
        private static final Logger logger = LogManager.getLogger(EncodingFilter.class);
        private String encoding;

        public void doFilter(ServletRequest req, ServletResponse resp,
                             FilterChain chain) throws IOException, ServletException {

            logger.debug("starts");

            HttpServletRequest httpRequest = (HttpServletRequest) req;
            logger.trace("Request uri={}", httpRequest.getRequestURI());

            String requestEncoding = req.getCharacterEncoding();
            if (requestEncoding == null) {
                logger.trace("Request encoding = null, set encoding to {}", encoding);
                req.setCharacterEncoding(encoding);
            }

            resp.setContentType("text/html; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");

            logger.debug("finished");
            chain.doFilter(req, resp);
        }

        @Override
        public void init(FilterConfig conf) {
            logger.debug("Filter initialization starts");
            encoding = conf.getInitParameter("encoding");
            logger.trace("Encoding from web.xml: {}", encoding);
            logger.debug("Filter initialization finished");
        }
}
