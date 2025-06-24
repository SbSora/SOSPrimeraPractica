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
        test1();
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
            {\n                \"username\": \"%s\",\n                \"registrationNumber\": \"%s\",\n                \"birthDate\": \"%s\",\n                \"email\": \"%s\"\n            }\n        """, username, registrationNumber, birthDate, email);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void eliminarUsuario(int userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .DELETE()
                .build();
        sendRequest(request);
    }

    private static void listarUsuarios() throws Exception {
        System.out.println("\n--> Listar Usuarios");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios?page=0&size=10"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Código HTTP: " + response.statusCode());
        System.out.println(response.body());
    }

    private static void consultarUsuario(int userId) throws Exception {
        System.out.println("\n--> Consultar Usuario por ID");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Código HTTP: " + response.statusCode());
        System.out.println(response.body());
    }

    private static int crearLibro(String title, String authors, String edition, String isbn, String publisher, boolean available) throws Exception {
        String json = String.format("""
            {\n                \"title\": \"%s\",\n                \"authors\": \"%s\",\n                \"edition\": \"%s\",\n                \"isbn\": \"%s\",\n                \"publisher\": \"%s\",\n                \"available\": %b\n            }\n        """, title, authors, edition, isbn, publisher, available);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[crearLibro] Código: " + response.statusCode());
        int id = extraerId(response.body());
        System.out.println("[crearLibro] ID: " + id);
        return id;
    }

    private static void modificarLibro(int bookId, String title, String authors, String edition, String isbn, String publisher, boolean available) throws Exception {
        String json = String.format("""
            {\n                \"title\": \"%s\",\n                \"authors\": \"%s\",\n                \"edition\": \"%s\",\n                \"isbn\": \"%s\",\n                \"publisher\": \"%s\",\n                \"available\": %b\n            }\n        """, title, authors, edition, isbn, publisher, available);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros/" + bookId))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void eliminarLibro(int bookId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros/" + bookId))
                .DELETE()
                .build();
        sendRequest(request);
    }

    private static void listarLibros() throws Exception {
        System.out.println("\n--> Listar Libros");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros?page=0&size=10"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Código HTTP: " + response.statusCode());
        System.out.println(response.body());
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

    private static void sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Codigo HTTP: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
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
    
}