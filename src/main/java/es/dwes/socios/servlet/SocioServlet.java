package es.dwes.socios.servlet;

import es.dwes.socios.dao.SocioDAO;
import es.dwes.socios.model.Socio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(urlPatterns = {
        "/", "/index", "/formularioSocio", "/grabaSocio", "/listadoSocios",
        "/pideNumeroSocio", "/borraSocio"
})
public class SocioServlet extends HttpServlet {
    private final SocioDAO socios = new SocioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/", "/index" -> mostrarInicio(request, response);
            case "/formularioSocio" -> mostrarFormulario(request, response);
            case "/listadoSocios", "/pideNumeroSocio" -> mostrarSocios(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/grabaSocio" -> guardarSocio(request, response);
            case "/borraSocio" -> borrarSocio(request, response);
            default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void mostrarInicio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = iniciarPagina(request, response, "Gestión de socios");
        out.println("<main><h1>Bienvenido a la aplicación de gestión de socios</h1>");
        out.println("<p>Seleccione una opción:</p><ul>");
        out.println("<li><a href=\"" + ruta(request, "/formularioSocio") + "\">Añadir nuevo socio</a></li>");
        out.println("<li><a href=\"" + ruta(request, "/listadoSocios") + "\">Ver listado de socios</a></li>");
        out.println("<li><a href=\"" + ruta(request, "/pideNumeroSocio") + "\">Consultar y borrar socios</a></li>");
        out.println("</ul></main>");
        finalizarPagina(out, request);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = iniciarPagina(request, response, "Nuevo socio");
        out.println("<h1>Introduzca los datos del nuevo socio</h1>");
        out.println("<form method=\"post\" action=\"" + ruta(request, "/grabaSocio") + "\">");
        out.println("<label>Nº socio <input type=\"number\" name=\"numero\" required></label><br>");
        out.println("<label>Nombre <input type=\"text\" name=\"nombre\" maxlength=\"40\" required></label><br>");
        out.println("<label>Estatura <input type=\"number\" name=\"estatura\" min=\"0\"></label><br>");
        out.println("<label>Edad <input type=\"number\" name=\"edad\" min=\"0\"></label><br>");
        out.println("<label>Localidad <input type=\"text\" name=\"localidad\" maxlength=\"30\"></label><br>");
        out.println("<button type=\"submit\">Aceptar</button></form>");
        finalizarPagina(out, request);
    }

    private void mostrarSocios(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = iniciarPagina(request, response, "Socios");
        out.println("<h1>Listado de socios</h1><table><thead><tr>");
        out.println("<th>Código</th><th>Nombre</th><th>Estatura</th><th>Edad</th><th>Localidad</th><th></th>");
        out.println("</tr></thead><tbody>");
        try {
            var listadoSocios = socios.listar();
            if (listadoSocios.isEmpty()) {
                out.println("<tr><td colspan=\"6\">No hay socios registrados.</td></tr>");
            }
            for (Socio socio : listadoSocios) {
                out.println("<tr><td>" + socio.numero() + "</td><td>" + escapar(socio.nombre()) + "</td>");
                out.println("<td>" + valor(socio.estatura()) + "</td><td>" + valor(socio.edad()) + "</td>");
                out.println("<td>" + escapar(socio.localidad()) + "</td><td>");
                out.println("<form method=\"post\" action=\"" + ruta(request, "/borraSocio") + "\">");
                out.println("<input type=\"hidden\" name=\"codigo\" value=\"" + socio.numero() + "\">");
                out.println("<button type=\"submit\">Borrar</button></form></td></tr>");
            }
        } catch (SQLException exception) {
            throw new ServletException("No se pudo consultar el listado de socios", exception);
        }
        out.println("</tbody></table>");
        finalizarPagina(out, request);
    }

    private void guardarSocio(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            Socio socio = new Socio(
                    Integer.parseInt(request.getParameter("numero")),
                    requerido(request, "nombre"),
                    enteroOpcional(request, "estatura"),
                    enteroOpcional(request, "edad"),
                    request.getParameter("localidad"));
            socios.insertar(socio);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Revise los datos introducidos.");
            return;
        } catch (SQLException exception) {
            throw new ServletException("No se pudo guardar el socio", exception);
        }

        PrintWriter out = iniciarPagina(request, response, "Alta de socio");
        out.println("<h1>Socio dado de alta.</h1><p><a href=\"" + ruta(request, "/pideNumeroSocio")
                + "\">Ver socios</a></p>");
        finalizarPagina(out, request);
    }

    private void borrarSocio(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            socios.borrar(Integer.parseInt(request.getParameter("codigo")));
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El número de socio no es válido.");
            return;
        } catch (SQLException exception) {
            throw new ServletException("No se pudo borrar el socio", exception);
        }
        response.sendRedirect(ruta(request, "/pideNumeroSocio"));
    }

    private PrintWriter iniciarPagina(HttpServletRequest request, HttpServletResponse response, String titulo)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<title>" + escapar(titulo) + "</title>");
        out.println("<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/estilos.css\">");
        out.println("</head><body>");
        return out;
    }

    private void finalizarPagina(PrintWriter out, HttpServletRequest request) {
        out.println("<p><a href=\"" + ruta(request, "/") + "\">Volver al inicio</a></p>");
        out.println("</body></html>");
    }

    private static String ruta(HttpServletRequest request, String ruta) {
        return request.getContextPath() + ruta;
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String valor(Integer valor) {
        return valor == null ? "" : valor.toString();
    }

    private static String requerido(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta " + nombre);
        }
        return valor.trim();
    }

    private static Integer enteroOpcional(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null || valor.isBlank() ? null : Integer.valueOf(valor);
    }
}