import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());
        
        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI()) && httpRequest.getSession().getAttribute("employee") == null) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
        
        // Check if user is an employee
        if (httpRequest.getSession().getAttribute("employee") == null && httpRequest.getSession().getAttribute("user") != null && this.isUrlAllowedWithoutEmployeeLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
        
        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null && httpRequest.getSession().getAttribute("employee") == null) {
            httpResponse.sendRedirect("login.html");
        } else {
            chain.doFilter(request, response);
        }
        
    }

    // Setup your own rules here to allow accessing some resources without logging in
    // Always allow your own login related requests(html, js, servlet, etc..)
    // You might also want to allow some CSS files, etc..
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        requestURI = requestURI.toLowerCase();

        return true;
        /*
        return requestURI.endsWith("login.html") || requestURI.endsWith("login.js")
        		|| requestURI.endsWith("login.css") || requestURI.endsWith("loginbg.png")
        		|| requestURI.endsWith("api/login") || requestURI.endsWith("api/logout")
        		|| requestURI.endsWith("recaptchaconstants.java") || requestURI.endsWith("recaptchaverifyutils")
        		|| requestURI.endsWith("dashboard_login.jpg") || requestURI.endsWith("api/dashboard_login")
        		|| requestURI.endsWith("api/mobile-login");
		*/

    }

    
    private boolean isUrlAllowedWithoutEmployeeLogin(String requestURI) {
        requestURI = requestURI.toLowerCase();
        return true;
        /*
        return !requestURI.endsWith("_dashboard.html") && !requestURI.endsWith("_dashboard.css") 
        		&& !requestURI.endsWith("_dashboard.js") && !requestURI.endsWith("api/_dashboard");
        */
    }
    /**
     * We need to have these function because this class implements Filter.
     * But we don't need to put any code in them.
     *
     * @see Filter#init(FilterConfig)
     */

    public void init(FilterConfig fConfig) {
    }

    public void destroy() {
    }


}
