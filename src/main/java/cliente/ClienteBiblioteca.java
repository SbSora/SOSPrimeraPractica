package cliente;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

/**
 * Cliente Java que ejecuta todas las peticiones de prueba (al igual que vienen
 * en Postman).
 */
public class ClienteBiblioteca {

    private String baseUrl = "http://localhost:8080";
    private HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        ClienteBiblioteca cliente = new ClienteBiblioteca();
        System.out.println("==========================================");
        System.out.println("       INICIO DE LAS PRUEBAS API REST     ");
        System.out.println("==========================================");

        // 1. Crear Usuario
        cliente.post("/usuarios", "usuario.json");

        // 2. Obtener Usuario
        cliente.get("/usuarios/1");

        // 3. Actualizar Usuario
        cliente.put("/usuarios/1", "usuario_update.json");

        // 4. Obtener Usuario Actualizado
        cliente.get("/usuarios/1");

        // 5. Obtener Actividad Usuario (vacío)
        cliente.get("/usuarios/1/actividad");

        // 6. Crear Libro
        cliente.post("/libros", "libro.json");

        // 7. Obtener Libro
        cliente.get("/libros/978-1-56619-909-4");

        // 8. Actualizar Libro
        cliente.put("/libros/978-1-56619-909-4", "libro_update.json");

        // 9. Filtrar Libros (sin resultados)
        cliente.get("/libros?titulo=principito");

        // 10. Crear Préstamo
        cliente.post("/prestamos", "prestamo.json");

        // 11. Obtener Actividad Usuario (tras préstamo)
        cliente.get("/usuarios/1/actividad");

        // 12. Obtener Préstamo por ID
        cliente.get("/prestamos/1");

        // 13. Actualizar Préstamo
        cliente.put("/prestamos/1", "prestamo_update.json");

        // 14. Obtener Actividad Usuario (préstamo actualizado)
        cliente.get("/usuarios/1/actividad");

        // 15. Ampliar Préstamo
        cliente.put("/prestamos/1/ampliar", null);

        // 16. Devolver Préstamo
        cliente.put("/prestamos/1/devolver", null);

        // 17. Obtener Actividad Usuario (préstamo devuelto)
        cliente.get("/usuarios/1/actividad");

        // 18. Eliminar Préstamo
        cliente.delete("/prestamos/1");

        // 19. Eliminar Libro
        cliente.delete("/libros/978-1-56619-909-4");

        // 20. Eliminar Usuario
        cliente.delete("/usuarios/1");
    }

    /**
     * Realiza una petición POST al endpoint con el contenido de un archivo JSON.
     *
     * @param endpoint URI del recurso
     * @param jsonFile nombre del archivo JSON a enviar
     */
    private void post(String endpoint, String jsonFile) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofFile(Path.of("src/main/java/es/upm/sos/practica1/JSON/" + jsonFile)))
                .build();
        sendAndPrint(request, "POST " + endpoint);
    }

    /**
     * Realiza una petición GET al endpoint especificado.
     *
     * @param endpoint URI del recurso
     */
    private void get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .build();
        sendAndPrint(request, "GET " + endpoint);
    }

    /**
     * Realiza una petición PUT al endpoint especificado. Si el archivo JSON es
     * null, se envía sin cuerpo.
     *
     * @param endpoint URI del recurso
     * @param jsonFile nombre del archivo JSON a enviar, o null si no hay cuerpo
     */
    private void put(String endpoint, String jsonFile) throws Exception {
        HttpRequest.BodyPublisher body = (jsonFile == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofFile(Path.of("src/main/java/es/upm/sos/practica1/JSON/" + jsonFile));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .PUT(body)
                .build();
        sendAndPrint(request, "PUT " + endpoint);
    }

    /**
     * Realiza una petición DELETE al endpoint especificado.
     *
     * @param endpoint URI del recurso
     */
    private void delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .DELETE()
                .build();
        sendAndPrint(request, "DELETE " + endpoint);
    }

    /**
     * Envía la petición HTTP construida y muestra el código de estado y el cuerpo
     * de la respuesta.
     *
     * @param request la petición HTTP ya construida
     * @param label   texto identificativo de la operación realizada
     */
    private void sendAndPrint(HttpRequest request, String label) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println();
        System.out.println(" PETICIÓN: " + label);
        System.out.println("=================================================");
        System.out.println(" Código HTTP: " + response.statusCode());
        System.out.println(" Respuesta:");
        System.out.println(response.body());
    }
}
