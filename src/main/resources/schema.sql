-- schema.sql
   -- Drop tables if they exist to ensure a clean state
   DROP TABLE IF EXISTS prestamos;
   DROP TABLE IF EXISTS libros;
   DROP TABLE IF EXISTS usuarios;

   -- Create tables
   CREATE TABLE usuarios (
       id BIGINT PRIMARY KEY,
       username VARCHAR(255) NOT NULL,
       registration_number VARCHAR(50) NOT NULL,
       birth_date DATE NOT NULL,
       email VARCHAR(255) NOT NULL
   );

   CREATE TABLE libros (
       id BIGINT PRIMARY KEY,
       title VARCHAR(255) NOT NULL,
       authors VARCHAR(255) NOT NULL,
       edition VARCHAR(50),
       isbn VARCHAR(13) NOT NULL,
       publisher VARCHAR(255),
       available BOOLEAN NOT NULL
   );

   CREATE TABLE prestamos (
       id BIGINT PRIMARY KEY,
       user_id BIGINT,
       book_id BIGINT,
       loan_date DATE NOT NULL,
       due_date DATE NOT NULL,
       return_date DATE,
       penalty_until DATE,
       CONSTRAINT prestamos_user_id_fkey
           FOREIGN KEY (user_id)
           REFERENCES usuarios(id)
           ON DELETE SET NULL,
       CONSTRAINT prestamos_book_id_fkey
           FOREIGN KEY (book_id)
           REFERENCES libros(id)
           ON DELETE SET NULL
   );