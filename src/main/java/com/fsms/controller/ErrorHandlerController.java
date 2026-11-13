package com.fsms.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global error handler for better error page display
 */
@Controller
public class ErrorHandlerController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = status != null ? (Integer) status : 500;
        
        logger.error("ERROR Status: {}, Message: {}, Path: {}", statusCode, message, path);
        if (exception != null && exception instanceof Exception) {
            logger.error("Exception Details: ", (Exception) exception);
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
        model.addAttribute("message", message != null ? message.toString() : "An error occurred");
        model.addAttribute("path", path != null ? path.toString() : "Unknown");
        
        // Add default values for layout fragments
        model.addAttribute("currentRole", "");
        model.addAttribute("currentUserName", "");

        return "error";
    }
}

