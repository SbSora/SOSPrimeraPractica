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
        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n========== CLIENTE BIBLIOTECA ==========");
            System.out.println("\n");
            System.out.println("------De momento funcionan todas las secciones excepto la 6, 7, y 8 ");
            System.out.println("1.  Crear Usuario");
            System.out.println("2.  Eliminar Usuario");
            System.out.println("3.  Modificar Usuario");
            System.out.println("4.  Listar todos los Usuarios");
            System.out.println("5.  Obtener usuario por ID");            
            System.out.println("6.  Obtener préstamos activos de un usuario desde una fecha");
            System.out.println("7.  Obtener historial de préstamos de un usuario");
            System.out.println("8.  Obtener últimos 5 préstamos de un usuario");

            System.out.println("9.  Crear Libro");
            System.out.println("10. Eliminar Libro");
            System.out.println("11. Modificar Libro");
            System.out.println("12. Listar Libro por patrón de título y disponibilidad");
            
            System.out.println("13. Crear Préstamo");
            System.out.println("14. Devolver Préstamo");
            System.out.println("15. Ampliar Préstamo");
            System.out.println("16. Salir");
            System.out.print("Elige una opción: ");

            int opcion = sc.nextInt();
            sc.nextLine(); // limpiar salto de línea

            switch (opcion) {
                case 1 -> crearUsuario();
                case 2 -> eliminarUsuario();
                case 3 -> modificarUsuario();
                case 4 -> listarUsuarios();
                case 5 -> consultarUsuario();
                case 6 -> consultarPrestamosActivos();
                case 7 -> consultarHistorialPrestamos();
                case 8 -> consultarActividadUsuario();
                case 9 -> crearLibro();
                case 10 -> eliminarLibro();
                case 11 -> modificarLibro();
                case 12 -> listarLibros();
                case 13 -> crearPrestamo();
                case 14 -> devolverPrestamo();
                case 15 -> ampliarPrestamo();
                case 0 -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }

        sc.close();
        System.out.println("Programa finalizado.");
    }

    private static void crearUsuario() throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n--> Crear Usuario");
    
        System.out.print("Introduce nombre de usuario: ");
        String username = sc.nextLine();
    
        System.out.print("Introduce matrícula: ");
        String registrationNumber = sc.nextLine();
    
        System.out.print("Introduce fecha de nacimiento (yyyy-MM-dd): ");
        String birthDate = sc.nextLine();
    
        System.out.print("Introduce email: ");
        String email = sc.nextLine();
    
        String json = String.format("""
            {
                "username": "%s",
                "registrationNumber": "%s",
                "birthDate": "%s",
                "email": "%s"
            }
            """, username, registrationNumber, birthDate, email);
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        sendRequest(request);
    }

    private static void eliminarUsuario() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Eliminar Usuario");
    
        System.out.print("Introduce ID del usuario a eliminar: ");
        int userId = Integer.parseInt(sc.nextLine());
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .DELETE()
                .build();
        sendRequest(request);
    }
    
    private static void modificarUsuario() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Modificar Usuario");
    
        System.out.print("Introduce ID del usuario a modificar: ");
        int userId = Integer.parseInt(sc.nextLine());
    
        System.out.print("Introduce nuevo nombre de usuario: ");
        String username = sc.nextLine();
    
        System.out.print("Introduce nueva matrícula: ");
        String registrationNumber = sc.nextLine();
    
        System.out.print("Introduce nueva fecha de nacimiento (yyyy-MM-dd): ");
        String birthDate = sc.nextLine();
    
        System.out.print("Introduce nuevo email: ");
        String email = sc.nextLine();
    
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
    
        if (response.statusCode() == 200) {
            String body = response.body();
    
            // Buscamos cada usuario dentro del JSON
            String[] partes = body.split("\\{\\\"id\\\":");
    
            for (int i = 1; i < partes.length; i++) {
                String usuario = partes[i];
                String[] datos = usuario.split(",");
    
                String id = datos[0];
                String username = datos[1].split(":")[1].replaceAll("\"", "");
                String registrationNumber = datos[2].split(":")[1].replaceAll("\"", "");
                String birthDate = datos[3].split(":")[1].replaceAll("\"", "");
                String email = datos[4].split(":")[1].replaceAll("\"", "");
    
                System.out.println("Usuario ID: " + id.trim() + " | Nombre: " + username + " | Matrícula: " + registrationNumber + " | Fecha Nacimiento: " + birthDate + " | Email: " + email);
            }
        } else {
            System.out.println("Error al listar usuarios: " + response.body());
        }
    }

    

    private static void consultarUsuario() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Consultar Usuario por ID");
    
        System.out.print("Introduce ID del usuario a consultar: ");
        int userId = Integer.parseInt(sc.nextLine());
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/" + userId))
                .GET()
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
    
        if (response.statusCode() == 200) {
            String body = response.body();
    
            // Sacamos manualmente los campos
            String id = body.split("\"id\":")[1].split(",")[0];
            String username = body.split("\"username\":\"")[1].split("\"")[0];
            String registrationNumber = body.split("\"registrationNumber\":\"")[1].split("\"")[0];
            String birthDate = body.split("\"birthDate\":\"")[1].split("\"")[0];
            String email = body.split("\"email\":\"")[1].split("\"")[0];
    
            System.out.println("Usuario ID: " + id.trim() +
                    " | Nombre: " + username +
                    " | Matrícula: " + registrationNumber +
                    " | Fecha Nacimiento: " + birthDate +
                    " | Email: " + email);
        } else {
            System.out.println("Error al consultar usuario: " + response.body());
        }
    }
    
    

    private static void crearLibro() throws Exception {
        Scanner sc = new Scanner(System.in);

    System.out.println("\n--> Crear Libro");

    System.out.print("Introduce título: ");
    String title = sc.nextLine();

    System.out.print("Introduce autores: ");
    String authors = sc.nextLine();

    System.out.print("Introduce edición: ");
    String edition = sc.nextLine();

    System.out.print("Introduce ISBN: ");
    String isbn = sc.nextLine();

    System.out.print("Introduce editorial: ");
    String publisher = sc.nextLine();

    System.out.print("¿Está disponible? (true/false): ");
    boolean available = Boolean.parseBoolean(sc.nextLine());

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
    sendRequest(request);
    }

    private static void eliminarLibro() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Eliminar Libro");
    
        System.out.print("Introduce ID del libro a eliminar: ");
        int bookId = Integer.parseInt(sc.nextLine());
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/libros/" + bookId))
                .DELETE()
                .build();
        sendRequest(request);
    }
    

    private static void listarLibros() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Listar Libros");
    
        System.out.print("¿Deseas aplicar un filtro por título? (Pulsa ENTER para omitir): ");
        String patronTitulo = sc.nextLine().trim();
    
        String url = BASE_URL + "/libros";
    
        boolean tieneQuery = false;
    
        if (!patronTitulo.isEmpty()) {
            url += "?titulo=" + URLEncoder.encode(patronTitulo, StandardCharsets.UTF_8);
            tieneQuery = true;
        }
    
        System.out.print("¿Deseas ver solo los libros disponibles? (Pulsa ENTER para omitir, escribe 'si', 's', 'yes' o 'y' para filtrar): ");
        String disponible = sc.nextLine().trim().toLowerCase();
    
        if (disponible.equals("si") || disponible.equals("s") || disponible.equals("yes") || disponible.equals("y")) {
            if (tieneQuery) {
                url += "&disponible=true";
            } else {
                url += "?disponible=true";
                tieneQuery = true;
            }
        }
    
        // Siempre añadimos paginación al final
        if (tieneQuery) {
            url += "&page=0&size=10";
        } else {
            url += "?page=0&size=10";
        }
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
    
        if (response.statusCode() == 200) {
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
                String available = "Desconocido";
    
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
        } else {
            System.out.println("Error al listar libros: " + response.body());
        }
    }
    
    

    private static void modificarLibro() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Modificar Libro");
    
        System.out.print("Introduce ID del libro a modificar: ");
        int bookId = Integer.parseInt(sc.nextLine());
    
        System.out.print("Introduce nuevo título: ");
        String title = sc.nextLine();
    
        System.out.print("Introduce nuevos autores: ");
        String authors = sc.nextLine();
    
        System.out.print("Introduce nueva edición: ");
        String edition = sc.nextLine();
    
        System.out.print("Introduce nuevo ISBN: ");
        String isbn = sc.nextLine();
    
        System.out.print("Introduce nueva editorial: ");
        String publisher = sc.nextLine();
    
        System.out.print("¿Está disponible? (true/false): ");
        boolean available = Boolean.parseBoolean(sc.nextLine());
    
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
    
        sendRequest(request);
    }
    

    private static void crearPrestamo() throws Exception {
        Scanner sc = new Scanner(System.in);

    System.out.println("\n--> Crear Préstamo");

    System.out.print("Introduce ID del usuario: ");
    int userId = Integer.parseInt(sc.nextLine());

    System.out.print("Introduce ID del libro: ");
    int bookId = Integer.parseInt(sc.nextLine());

    String json = String.format("""
        {
            "userId": %d,
            "bookId": %d
        }
        """, userId, bookId);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/prestamos"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(json))
            .build();
    sendRequest(request);
    }

    private static void devolverPrestamo() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Devolver Préstamo");
    
        System.out.print("Introduce ID del préstamo a devolver: ");
        int prestamoId = Integer.parseInt(sc.nextLine());
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos/" + prestamoId + "/devolver"))
                .PUT(BodyPublishers.noBody())
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
    }
    

    private static void ampliarPrestamo() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Ampliar Préstamo");
    
        System.out.print("Introduce ID del préstamo a ampliar: ");
        int prestamoId = Integer.parseInt(sc.nextLine());
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prestamos/" + prestamoId + "/ampliar"))
                .PUT(BodyPublishers.noBody())
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
    }
    

    private static void sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Codigo HTTP: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
    }


    private static void consultarPrestamosActivos() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Consultar Préstamos Activos de un Usuario");
    
        System.out.print("Introduce ID del usuario: ");
        int userId = Integer.parseInt(sc.nextLine());
    
        System.out.print("¿Deseas filtrar por fecha desde? (formato yyyy-MM-dd, pulsa ENTER para omitir): ");
        String fecha = sc.nextLine().trim();
    
        String url = BASE_URL + "/usuarios/" + userId + "/prestamos";
    
        if (!fecha.isEmpty()) {
            url += "?desde=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8);
        }
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
    
        if (response.statusCode() == 200) {
            String body = response.body();
    
            String[] partes = body.split("\\{\\\"id\\\":");
    
            for (int i = 1; i < partes.length; i++) {
                String prestamo = partes[i];
                String[] datos = prestamo.split(",");
    
                String id = datos[0];
                String loanDate = extraerValor(datos, "\"loanDate\":\"");
                String dueDate = extraerValor(datos, "\"dueDate\":\"");
                String returnDate = extraerValor(datos, "\"returnDate\":");
    
                System.out.println("Préstamo ID: " + id.trim() +
                        " | Fecha préstamo: " + loanDate +
                        " | Fecha vencimiento: " + dueDate +
                        " | Devuelto: " + (returnDate.contains("null") ? "No" : returnDate));
            }
        } else {
            System.out.println("Error al consultar préstamos: " + response.body());
        }
    }
    
    private static void consultarHistorialPrestamos() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Consultar Historial de Préstamos de un Usuario");
    
        System.out.print("Introduce ID del usuario: ");
        int userId = Integer.parseInt(sc.nextLine());
    
        String url = BASE_URL + "/usuarios/" + userId + "/historial";
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
    
        if (response.statusCode() == 200) {
            String body = response.body();
    
            String[] partes = body.split("\\{\\\"id\\\":");
    
            for (int i = 1; i < partes.length; i++) {
                String prestamo = partes[i];
                String[] datos = prestamo.split(",");
    
                String id = datos[0];
                String loanDate = extraerValor(datos, "\"loanDate\":\"");
                String dueDate = extraerValor(datos, "\"dueDate\":\"");
                String returnDate = extraerValor(datos, "\"returnDate\":\"");
    
                System.out.println("Préstamo ID: " + id.trim() +
                        " | Fecha préstamo: " + loanDate +
                        " | Fecha vencimiento: " + dueDate +
                        " | Fecha devolución: " + returnDate);
            }
        } else {
            System.out.println("Error al consultar historial: " + response.body());
        }
    }
    
    
    private static void consultarActividadUsuario() throws Exception {
        Scanner sc = new Scanner(System.in);
    
        System.out.println("\n--> Consultar Actividad Completa del Usuario");
    
        System.out.print("Introduce ID del usuario: ");
        int userId = Integer.parseInt(sc.nextLine());
    
        String url = BASE_URL + "/usuarios/" + userId + "/actividad";
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Código HTTP: " + response.statusCode());
    
        if (response.statusCode() == 200) {
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
            mostrarBloque(body, "\"prestamosActivos\":\\[", "]");
    
            System.out.println("\n=== Últimos 5 Préstamos Devueltos ===");
            mostrarBloque(body, "\"historialReciente\":\\[", "]");
        } else {
            System.out.println("Error al consultar actividad: " + response.body());
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

    private static void mostrarBloque(String body, String inicioClave, String finClave) {
        try {
            String bloque = body.split(inicioClave)[1].split(finClave)[0];
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