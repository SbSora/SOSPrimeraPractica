    package cliente;

    import java.net.URI;
    import java.net.URLEncoder;
    import java.net.http.HttpClient;
    import java.net.http.HttpRequest;
    import java.net.http.HttpRequest.BodyPublishers;
    import java.net.http.HttpResponse;
    import java.nio.charset.StandardCharsets;


    public class ClienteBiblioteca {

        private static final String BASE_URL = "http://localhost:8080";
        private static final HttpClient client = HttpClient.newHttpClient();

public static void main(String[] args) throws Exception {

    System.out.println("\n=========== INICIO DE PRUEBAS AUTOMÁTICAS ==========\n");

    test1(); // Crear usuario + libro + préstamo + devolver
    test2(); // Crear usuario + consultar + listar usuarios
    test3(); // Crear libro + consultar + listar libros
    test4(); // Crear + modificar + eliminar usuario
    test5(); // Filtrado por título + disponibilidad
    test6(); // Crear + devolver préstamo
    test7(); // Crear + ampliar préstamo
    test8(); // Consultar actividad completa (datos + préstamos)


    System.out.println("\n=========== FIN DE PRUEBAS ==========\n");

}


        public static void test1() throws Exception {
            System.out.println("\n========= INICIO TEST 1 =========");

            int userId = crearUsuario("carlos_test", "934719", "2004-03-24", "carlos@test.com");
            int bookId = crearLibro("Libro Test", "Autor1", "1ª", "123-TEST", "Editorial1", true);
            int prestamoId = crearPrestamo(userId, bookId);
            devolverPrestamo(prestamoId);

            System.out.println("========= FIN TEST 1 =========\n");
        }

        public static void test2() throws Exception {
            System.out.println("\n========= INICIO TEST 2: Usuario Básico =========");

            int userId = crearUsuario("juan_test", "AA001", "1995-05-05", "juan@test.com");
            consultarUsuario(userId);
            listarUsuarios();

            System.out.println("========= FIN TEST 2 =========\n");
        }

        public static void test3() throws Exception {
            System.out.println("\n========= INICIO TEST 3: Préstamo Básico =========");

            // Crear usuario
            int userId = crearUsuario("maria_test", "BB002", "1990-09-09", "maria@test.com");

            // Crear libro disponible
            int bookId = crearLibro("RESTful para principiantes", "Autor X", "1ª", "ISBN-REST-001", "Editorial X", true);

            // Crear préstamo
            int prestamoId = crearPrestamo(userId, bookId);

            // Consultar préstamos activos
            consultarPrestamosActivos(userId, null);

            System.out.println("========= FIN TEST 3 =========\n");
        }

        
        public static void test4() throws Exception {
            System.out.println("\n========= INICIO TEST 4: Ampliar y Devolver Préstamo =========");

            // 1. Crear usuario
            int userId = crearUsuario("ana_test", "ZZ999", "1985-03-15", "ana@test.com");

            // 2. Crear libro disponible
            int bookId = crearLibro("Java REST Intermedio", "Autor Intermedio", "2ª", "ISBN-REST-INT", "Editorial INT", true);

            // 3. Crear préstamo
            int prestamoId = crearPrestamo(userId, bookId);

            // 4. Ampliar el préstamo
            System.out.println("\n--- Ampliando préstamo ---");
            ampliarPrestamo(prestamoId);

            // 5. Devolver el préstamo
            System.out.println("\n--- Devolviendo préstamo ---");
            devolverPrestamo(prestamoId);

            // 6. Consultar historial
            System.out.println("\n--- Consultando historial ---");
            consultarHistorialPrestamos(userId);

            // 7. Consultar actividad completa
            System.out.println("\n--- Consultando actividad completa ---");
            consultarActividadUsuario(userId);

            System.out.println("========= FIN TEST 4 =========\n");
        }


        public static void test5() throws Exception {
            System.out.println("\n========= INICIO TEST 5: Filtrado por título + disponibilidad =========");

            // Paso 1: listar todos los libros cuyo título contenga "Progra", sin importar si están disponibles
            System.out.println("\n--- Libros con 'Progra' en el título (todos) ---");
            listarLibros("Progra", false);

            // Paso 2: listar solo los disponibles que contengan "Progra"
            System.out.println("\n--- Libros con 'Progra' en el título (solo disponibles) ---");
            listarLibros("Progra", true);

            // Paso 3: hacer préstamo de uno de los disponibles que contengan "Progra"
            int userId = 1;  // Por ejemplo, usuario con ID 1 ya existente
            int bookId = 10; // Título: "Programación en Java (Edición 3)", disponible

            System.out.println("\n--- Realizando préstamo del libro ID 10 ---");
            crearPrestamo(userId, bookId);

            // Paso 4: volver a listar con filtro de título "Progra" (todos)
            System.out.println("\n--- Libros con 'Progra' en el título tras préstamo (todos) ---");
            listarLibros("Progra", false);

            // Paso 5: volver a listar con filtro "Progra" y solo disponibles
            System.out.println("\n--- Libros con 'Progra' en el título tras préstamo (solo disponibles) ---");
            listarLibros("Progra", true);

            System.out.println("========= FIN TEST 5 =========\n");
        }

        public static void test6() throws Exception {
            System.out.println("\n========= INICIO TEST 6: Consultar Préstamos Activos e Historial =========");

            // Crear usuario y dos libros
            int userId = crearUsuario("mario_test", "M123", "1990-02-02", "mario@test.com");
            int libro1 = crearLibro("Libro Mario 1", "Autor M", "1ª", "ISBN-M1", "Editorial M", true);
            int libro2 = crearLibro("Libro Mario 2", "Autor M", "2ª", "ISBN-M2", "Editorial M", true);

            // Hacer dos préstamos
            int prestamoActivo = crearPrestamo(userId, libro1);
            int prestamoDevuelto = crearPrestamo(userId, libro2);

            // Devolver uno de los préstamos
            devolverPrestamo(prestamoDevuelto);

            // Consultar préstamos activos
            System.out.println("\n--- Préstamos activos del usuario ---");
            consultarPrestamosActivos(userId, "");

            // Consultar historial completo
            System.out.println("\n--- Historial completo del usuario ---");
            consultarHistorialPrestamos(userId);

            System.out.println("========= FIN TEST 6 =========\n");
        }

        public static void test7() throws Exception {
            System.out.println("\n========= INICIO TEST 7: Ampliar Préstamo =========");

            // Crear usuario y libro
            int userId = crearUsuario("laura_test", "L321", "1993-04-04", "laura@test.com");
            int libroId = crearLibro("Libro Laura", "Autor L", "1ª", "ISBN-L", "Editorial L", true);

            // Crear préstamo
            int prestamoId = crearPrestamo(userId, libroId);

            // Consultar préstamos activos antes de ampliar
            System.out.println("\n--- Antes de la ampliación ---");
            consultarPrestamosActivos(userId, "");

            // Ampliar el préstamo
            System.out.println("\n--- Ampliando préstamo ID: " + prestamoId + " ---");
            ampliarPrestamo(prestamoId);

            // Consultar préstamos activos después de ampliar
            System.out.println("\n--- Después de la ampliación ---");
            consultarPrestamosActivos(userId, "");

            System.out.println("========= FIN TEST 7 =========\n");
        }


        public static void test8() throws Exception {
            System.out.println("\n========= INICIO TEST 8: Consultar Actividad de Usuario =========");

            // Crear usuario y tres libros
            int userId = crearUsuario("ana_test", "A999", "1988-08-08", "ana@test.com");
            int libro1 = crearLibro("Libro Ana 1", "Autor A", "1ª", "ISBN-A1", "Editorial A", true);
            int libro2 = crearLibro("Libro Ana 2", "Autor A", "2ª", "ISBN-A2", "Editorial A", true);
            int libro3 = crearLibro("Libro Ana 3", "Autor A", "3ª", "ISBN-A3", "Editorial A", true);

            // Crear tres préstamos
            int p1 = crearPrestamo(userId, libro1);
            int p2 = crearPrestamo(userId, libro2);
            int p3 = crearPrestamo(userId, libro3);

            // Devolver dos préstamos
            devolverPrestamo(p1);
            devolverPrestamo(p2);

            // Consultar actividad completa
            consultarActividadUsuario(userId);

            System.out.println("========= FIN TEST 8 =========\n");
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
            //System.out.println("[crearLibro] Body: " + response.body());

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
                    .uri(URI.create(BASE_URL + "/prestamos/" + prestamoId + "/estado"))
                    .PUT(BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[devolverPrestamo] Código: " + response.statusCode());
            System.out.println("[devolverPrestamo] Respuesta: " + response.body());
        }

        private static void ampliarPrestamo(int prestamoId) throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/prestamos/" + prestamoId + "/fecha"))
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