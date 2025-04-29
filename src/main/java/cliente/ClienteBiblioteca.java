package cliente;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.URI;

public class ClienteBiblioteca {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        System.out.println("========== CLIENTE BIBLIOTECA ==========");

        crearUsuario();
        listarUsuarios();
        modificarUsuario();
        consultarUsuario();
        crearLibro();
        listarLibros();
        modificarLibro();
        crearPrestamo();
        devolverPrestamo();
        ampliarPrestamo();
    }

    private static void crearUsuario() throws Exception {
        System.out.println("\n--> Crear Usuario");
        String json = """
            {
                \"username\": \"alfon_user\",
                \"registrationNumber\": \"REG123\",
                \"birthDate\": \"1999-06-15\",
                \"email\": \"alfon@example.com\"
            }
        """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void listarUsuarios() throws Exception {
        System.out.println("\n--> Listar Usuarios");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios?page=0&size=10"))
                .GET()
                .build();
        sendRequest(request);
    }

    private static void modificarUsuario() throws Exception {
        System.out.println("\n--> Modificar Usuario");
        String json = """
            {
                \"username\": \"alfon_user_updated\",
                \"registrationNumber\": \"REG123\",
                \"birthDate\": \"1999-06-15\",
                \"email\": \"alfon_updated@example.com\"
            }
        """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/1"))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void consultarUsuario() throws Exception {
        System.out.println("\n--> Consultar Usuario por ID");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/1"))
                .GET()
                .build();
        sendRequest(request);
    }

    private static void crearLibro() throws Exception {
        System.out.println("\n--> Crear Libro");
        String json = """
            {
                \"title\": \"El Principito\",
                \"authors\": \"Antoine de Saint-Exup\u00e9ry\",
                \"edition\": \"1\",
                \"isbn\": \"978-1234567890\",
                \"publisher\": \"Editorial Ejemplo\",
                \"available\": true
            }
        """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void listarLibros() throws Exception {
        System.out.println("\n--> Listar Libros");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros?page=0&size=10"))
                .GET()
                .build();
        sendRequest(request);
    }

    private static void modificarLibro() throws Exception {
        System.out.println("\n--> Modificar Libro");
        String json = """
            {
                \"title\": \"El Principito (Edicion 2)\",
                \"authors\": \"Antoine de Saint-Exup\u00e9ry\",
                \"edition\": \"2\",
                \"isbn\": \"978-1234567890\",
                \"publisher\": \"Editorial Mejorada\",
                \"available\": true
            }
        """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros/1"))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void crearPrestamo() throws Exception {
        System.out.println("\n--> Crear Pr\u00e9stamo");
        String json = """
            {
                \"userId\": 1,
                \"bookId\": 1
            }
        """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void devolverPrestamo() throws Exception {
        System.out.println("\n--> Devolver Pr\u00e9stamo");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos/1/devolver"))
                .PUT(BodyPublishers.noBody())
                .build();
        sendRequest(request);
    }

    private static void ampliarPrestamo() throws Exception {
        System.out.println("\n--> Ampliar Pr\u00e9stamo");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos/1/ampliar"))
                .PUT(BodyPublishers.noBody())
                .build();
        sendRequest(request);
    }

    private static void sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Codigo HTTP: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
    }
}
