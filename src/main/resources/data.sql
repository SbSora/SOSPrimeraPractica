-- data.sql
   INSERT INTO usuarios (id, username, registration_number, birth_date, email) VALUES
   (1, 'john_doe', '12345', '1990-01-01', 'john@example.com'),
   (2, 'jane_doe', '67890', '1995-05-05', 'jane@example.com');

   INSERT INTO libros (id, title, authors, edition, isbn, publisher, available) VALUES
   (1, 'The Great Gatsby', 'F. Scott Fitzgerald', '1st', '9780743273565', 'Scribner', true),
   (2, '1984', 'George Orwell', '1st', '9780451524935', 'Signet Classics', true);