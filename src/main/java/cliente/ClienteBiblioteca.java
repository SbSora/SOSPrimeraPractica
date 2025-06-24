package cliente;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class ClienteBiblioteca {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
    System.out.println("\n========= PRUEBA crear + modificar + eliminar USUARIO =========");

    // 1. Crear usuario
    int userId = crearUsuario("prueba_usuario", "X123456", "2002-12-12", "prueba@correo.com");

    // 2. Modificarlo
    System.out.println("\n--- Modificando usuario ---");
    modificarUsuario(userId, "usuario_modificado", "Y654321", "1999-01-01", "nuevo@correo.com");

    // 3. Consultar para comprobar cambios
    System.out.println("\n--- Consultando usuario modificado ---");
    consultarUsuario(userId);

    // 4. Eliminar usuario
    System.out.println("\n--- Eliminando usuario ---");
    eliminarUsuario(userId);

    // 5. Consultar para confirmar eliminación
    System.out.println("\n--- Consultando usuario tras eliminación ---");
    consultarUsuario(userId); // debería dar error 404

    System.out.println("========= FIN PRUEBA =========\n");
    }

    public static void test1() throws Exception {
        System.out.println("\n========= INICIO TEST 1 =========");

        int userId = crearUsuario("pepe_test", "1234567", "2000-01-01", "pepe@test.com");
        int bookId = crearLibro("Libro Test", "Autor1", "1ª", "123-TEST", "Editorial1", true);
        int prestamoId = crearPrestamo(userId, bookId);
        devolverPrestamo(prestamoId);

        System.out.println("========= FIN TEST 1 =========\n");
    }

    private static int crearUsuario(String username, String registrationNumber, String birthDate, String email) throws Exception {
        String json = String.format("""
            {\n                \"username\": \"%s\",\n                \"registrationNumber\": \"%s\",\n                \"birthDate\": \"%s\",\n                \"email\": \"%s\"\n            }\n        """, username, registrationNumber, birthDate, email);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[crearUsuario] Código: " + response.statusCode());
        int id = extraerId(response.body());
        System.out.println("[crearUsuario] ID: " + id);
        return id;
    }

    private static void modificarUsuario(int userId, String username, String registrationNumber, String birthDate, String email) throws Exception {
        String json = String.format("""
            {
                "username": "%s",
                "registrationNumber": "%s",
                "birthDate": "%s",
                "email": "%s"
            }
        """, username, registrationNumber, birthDate, email);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[modificarUsuario] Código: " + response.statusCode());
    }


    private static void eliminarUsuario(int userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[eliminarUsuario] Código: " + response.statusCode());
    }


    public static void listarUsuarios() throws Exception {
        String url = BASE_URL + "/usuarios?page=0&size=10";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[listarUsuarios] Código: " + response.statusCode());
        if (response.statusCode() != 200) {
            System.out.println("[listarUsuarios] Error: " + response.body());
            return;
        }

        String body = response.body();
        String[] partes = body.split("\\{\\\"id\\\":");

        for (int i = 1; i < partes.length; i++) {
            String usuario = partes[i];
            String[] datos = usuario.split(",");

            String id = datos[0];
                String username = datos[1].split(":")[1].replaceAll("\"", "");
                String registrationNumber = datos[2].split(":")[1].replaceAll("\"", "");
                String birthDate = datos[3].split(":")[1].replaceAll("\"", "");
                String email = datos[4].split(":")[1].replaceAll("\"", "");

            System.out.println("Usuario ID: " + id.trim() +
                    " | Nombre: " + username +
                    " | Matrícula: " + registrationNumber +
                    " | Nacimiento: " + birthDate +
                    " | Email: " + email);
        }
    }


    private static void consultarUsuario(int userId) throws Exception {
        System.out.println("\n--> Consultar Usuario por ID: " + userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[consultarUsuario] Código: " + response.statusCode());

        if (response.statusCode() != 200) {
            System.out.println("[consultarUsuario] Error: " + response.body());
            return;
        }

        String body = response.body();

            String id = body.split("\"id\":")[1].split(",")[0];
            String username = body.split("\"username\":\"")[1].split("\"")[0];
            String registrationNumber = body.split("\"registrationNumber\":\"")[1].split("\"")[0];
            String birthDate = body.split("\"birthDate\":\"")[1].split("\"")[0];
            String email = body.split("\"email\":\"")[1].split("\"")[0];

        System.out.println("Usuario ID: " + id +
                " | Nombre: " + username +
                " | Matrícula: " + registrationNumber +
                " | Fecha Nacimiento: " + birthDate +
                " | Email: " + email);
    }


    private static void consultarPrestamosActivos(int userId, String fechaDesde) throws Exception {
        System.out.println("\n--> Consultar Préstamos Activos de Usuario ID: " + userId);

        String url = BASE_URL + "/usuarios/" + userId + "/prestamos";
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            url += "?desde=" + URLEncoder.encode(fechaDesde, StandardCharsets.UTF_8);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[consultarPrestamosActivos] Código: " + response.statusCode());
        if (response.statusCode() != 200) {
            System.out.println("Error: " + response.body());
            return;
        }

        String body = response.body();
        String[] prestamos = body.split("\\{\\\"id\\\":");

        for (int i = 1; i < prestamos.length; i++) {
            String[] datos = prestamos[i].split(",");

            String id = datos[0];
            String loanDate = extraerValor(datos, "\"loanDate\":\"");
            String dueDate = extraerValor(datos, "\"dueDate\":\"");
            String returnDate = extraerValor(datos, "\"returnDate\":");

            System.out.println("Préstamo ID: " + id.trim() +
                    " | Fecha préstamo: " + loanDate +
                    " | Vence: " + dueDate +
                    " | Devuelto: " + (returnDate.contains("null") ? "No" : returnDate));
        }
    }

    private static void consultarHistorialPrestamos(int userId) throws Exception {
        System.out.println("\n--> Consultar Historial de Préstamos del Usuario ID: " + userId);

        String url = BASE_URL + "/usuarios/" + userId + "/historial";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[consultarHistorialPrestamos] Código: " + response.statusCode());
        if (response.statusCode() != 200) {
            System.out.println("Error: " + response.body());
            return;
        }

        String body = response.body();
        String[] prestamos = body.split("\\{\\\"id\\\":");

        for (int i = 1; i < prestamos.length; i++) {
            String[] datos = prestamos[i].split(",");

            String id = datos[0];
            String loanDate = extraerValor(datos, "\"loanDate\":\"");
            String dueDate = extraerValor(datos, "\"dueDate\":\"");
            String returnDate = extraerValor(datos, "\"returnDate\":\"");

            System.out.println("Préstamo ID: " + id.trim() +
                    " | Préstamo: " + loanDate +
                    " | Vence: " + dueDate +
                    " | Devolución: " + returnDate);
        }
    }

    private static void consultarActividadUsuario(int userId) throws Exception {
        System.out.println("\n--> Consultar Actividad Completa del Usuario ID: " + userId);

        String url = BASE_URL + "/usuarios/" + userId + "/actividad";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[consultarActividadUsuario] Código: " + response.statusCode());

        if (response.statusCode() != 200) {
            System.out.println("Error: " + response.body());
            return;
        }

        String body = response.body();

        System.out.println("\n=== Datos del Usuario ===");
        String username = extraerCampo(body, "\"username\":\"");
        String registrationNumber = extraerCampo(body, "\"registrationNumber\":\"");
        String birthDate = extraerCampo(body, "\"birthDate\":\"");
        String email = extraerCampo(body, "\"email\":\"");

        System.out.println("Nombre: " + username +
                " | Matrícula: " + registrationNumber +
                " | Nacimiento: " + birthDate +
                " | Email: " + email);

        System.out.println("\n=== Préstamos Activos ===");
        mostrarBloque(body, "\"activeLoans\":[", "]");

        System.out.println("\n=== Últimos 5 Préstamos Devueltos ===");
        mostrarBloque(body, "\"recentLoans\":[", "]");
    }



    private static int crearLibro(String title, String authors, String edition, String isbn, String publisher, boolean available) throws Exception {
            String json = String.format("""
        {
            "title": "%s",
            "authors": "%s",
            "edition": "%s",
            "isbn": "%s",
            "publisher": "%s",
            "available": %b
        }
        """, title, authors, edition, isbn, publisher, available);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[crearLibro] Código: " + response.statusCode());
        System.out.println("[crearLibro] Body: " + response.body());

        int id = extraerId(response.body());
        System.out.println("[crearLibro] ID: " + id);
        return id;
    }

    private static void modificarLibro(int bookId, String title, String authors, String edition, String isbn, String publisher, boolean available) throws Exception {
        String json = String.format("""
            {
                "title": "%s",
                "authors": "%s",
                "edition": "%s",
                "isbn": "%s",
                "publisher": "%s",
                "available": %b
            }
        """, title, authors, edition, isbn, publisher, available);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros/" + bookId))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[modificarLibro] Código: " + response.statusCode());
    }


    private static void eliminarLibro(int bookId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros/" + bookId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[eliminarLibro] Código: " + response.statusCode());
    }


    private static void listarLibros(String tituloFiltro, boolean soloDisponibles) throws Exception {
        System.out.println("\n--> Listar Libros");

        String url = BASE_URL + "/libros";
        boolean tieneQuery = false;

        if (tituloFiltro != null && !tituloFiltro.isEmpty()) {
            url += "?titulo=" + URLEncoder.encode(tituloFiltro, StandardCharsets.UTF_8);
            tieneQuery = true;
        }

        if (soloDisponibles) {
            url += (tieneQuery ? "&" : "?") + "disponible=true";
            tieneQuery = true;
        }

        url += (tieneQuery ? "&" : "?") + "page=0&size=10";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[listarLibros] Código: " + response.statusCode());
        if (response.statusCode() != 200) {
            System.out.println("Error: " + response.body());
            return;
        }

        String body = response.body();
        String[] partes = body.split("\\{\\\"id\\\":");

        for (int i = 1; i < partes.length; i++) {
            String libro = partes[i];
            String[] datos = libro.split(",");

            String id = datos[0];
            String title = datos[1].split(":")[1].replaceAll("\"", "");
            String authors = datos[2].split(":")[1].replaceAll("\"", "");
            String edition = datos[3].split(":")[1].replaceAll("\"", "");
            String isbn = datos[4].split(":")[1].replaceAll("\"", "");

            String available = "¿?";
            for (String dato : datos) {
                if (dato.contains("\"available\":")) {
                    available = dato.split(":")[1].trim();
                    break;
                }
            }

            System.out.println("Libro ID: " + id.trim() +
                    " | Título: " + title +
                    " | Autores: " + authors +
                    " | Edición: " + edition +
                    " | ISBN: " + isbn +
                    " | Disponible: " + available);
        }
    }


    private static int crearPrestamo(int userId, int bookId) throws Exception {
        String json = String.format("""
            {\n                \"userId\": %d,\n                \"bookId\": %d\n            }\n        """, userId, bookId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[crearPrestamo] Código: " + response.statusCode());
        int id = extraerId(response.body());
        System.out.println("[crearPrestamo] ID: " + id);
        return id;
    }

    private static void devolverPrestamo(int prestamoId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos/" + prestamoId + "/devolver"))
                .PUT(BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[devolverPrestamo] Código: " + response.statusCode());
        System.out.println("[devolverPrestamo] Respuesta: " + response.body());
    }

    private static void ampliarPrestamo(int prestamoId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos/" + prestamoId + "/ampliar"))
                .PUT(BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[ampliarPrestamo] Código: " + response.statusCode());
        System.out.println("[ampliarPrestamo] Respuesta: " + response.body());
    }


    private static int extraerId(String body) {
        try {
            String[] partes = body.split("\"id\":");
            String idStr = partes[1].split(",")[0].trim();
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            System.out.println("Error extrayendo ID del cuerpo: " + body);
            return -1;
        }
    }

    private static String extraerValor(String[] datos, String clave) {
        for (String dato : datos) {
            if (dato.contains(clave)) {
                return dato.split(":")[1].replaceAll("\"", "").trim();
            }
        }
        return "No disponible";
    }

    private static String extraerCampo(String json, String clave) {
        try {
            return json.split(clave)[1].split("\"")[0];
        } catch (Exception e) {
            return "No disponible";
        }
    }

    private static void mostrarBloque(String body, String claveInicio, String claveFin) {
        try {
            int inicio = body.indexOf(claveInicio);
            if (inicio == -1) {
                System.out.println("No hay datos.");
                return;
            }
    
            inicio += claveInicio.length();
            int fin = body.indexOf(claveFin, inicio);
            if (fin == -1) {
                System.out.println("No hay datos.");
                return;
            }
    
            String bloque = body.substring(inicio, fin);
            String[] prestamos = bloque.split("\\{\\\"id\\\":");
    
            for (int i = 1; i < prestamos.length; i++) {
                String[] datos = prestamos[i].split(",");
                String id = datos[0];
                String loanDate = extraerValor(datos, "\"loanDate\":\"");
                String dueDate = extraerValor(datos, "\"dueDate\":\"");
                String returnDate = extraerValor(datos, "\"returnDate\":");
    
                System.out.println("Préstamo ID: " + id.trim() +
                        " | Préstamo: " + loanDate +
                        " | Vence: " + dueDate +
                        " | Devuelto: " + (returnDate.contains("null") ? "No" : returnDate));
            }
        } catch (Exception e) {
            System.out.println("No hay datos.");
        }
    }

    
}