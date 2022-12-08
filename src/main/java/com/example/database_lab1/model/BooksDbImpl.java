/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.database_lab1.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A mock implementation of the BooksDBInterface interface to demonstrate how to
 * use it together with the user interface.
 * <p>
 * Your implementation must access a real database.
 *
 * @author anderslm@kth.se
 */
public class BooksDbImpl implements BooksDbInterface {

    public static final String DB_NAME = "booksdb";
    private Connection con;
    private final List<Book> books;

    public BooksDbImpl() {
        books = Arrays.asList(DATA);
    }

    @Override
    public boolean connect(String database) throws BooksDbException {
        String server = "jdbc:mysql://localhost:3306/" + database+ "?UseClientEnc=UTF8";
        this.con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(server, "root", "1234");
            System.out.println("Connection done!");
            return true;
        } catch (ClassNotFoundException e) {
            throw new BooksDbException("Class could not be found, check your driver", e);
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with your connection", e);
        }
    }

    @Override
    public void disconnect() throws BooksDbException {
        try {
            this.con.close();
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the connection", e);
        }
    }

    @Override
    public List<Book> searchBooksByTitle(String searchTitle)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Book> result = new ArrayList<>();
            searchTitle = searchTitle.toLowerCase();
            String sql = "SELECT * FROM book WHERE title LIKE ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, "%"+searchTitle+"%");
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new Book(rs.getInt("book_id"), rs.getString("isbn"), rs.getString("title"), rs.getDate("published")));
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public List<Book> searchBooksByISBN(String searchISBN)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Book> result = new ArrayList<>();
            String sql = "SELECT * FROM book WHERE ISBN LIKE ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, "%"+searchISBN+"%");
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new Book(rs.getInt("book_id"), rs.getString("isbn"), rs.getString("title"), rs.getDate("published")));
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public List<Book> searchBooksByAuthor(String searchAuthor)
            throws BooksDbException {
        ResultSet rs = null;
        try{
            List<Book> result = new ArrayList<>();
            searchAuthor = searchAuthor.toLowerCase();
            String sql = "SELECT * FROM book b INNER JOIN book_author ba ON b.book_id = ba.book_id INNER JOIN author a ON ba.author_id = a.author_id WHERE a.name LIKE ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, "%"+searchAuthor+"%");
            rs = stmt.executeQuery();
            rs = stmt.executeQuery(sql);

            while (rs.next()){
                result.add(new Book(rs.getInt("book_id"), rs.getString("isbn"), rs.getString("title"), rs.getDate("published")));
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public List<Book> searchBooksByGenre(String searchGenre)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Book> result = new ArrayList<>();
            searchGenre = searchGenre.toLowerCase();

            String sql = "SELECT * FROM book b INNER JOIN book_genre bg ON b.book_id = bg.book_id INNER JOIN genre g ON bg.genre_id = g.genre_id WHERE g.genre_name LIKE '%"+searchGenre+"%'";
            Statement stmt = this.con.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.add(new Book(rs.getInt("book_id"), rs.getString("isbn"), rs.getString("title"), rs.getDate("published")));
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public List<Book> searchBooksByStars(String searchStars)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Book> result = new ArrayList<>();
            String sql = "SELECT * FROM book b, review r WHERE b.bookid = r.bookid AND stars = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, Integer.valueOf(searchStars));
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new Book(rs.getInt("book_id"), rs.getString("isbn"), rs.getString("title"), rs.getDate("published")));
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }
    @Override
    public void updateTitleBook(String newTitle, int bookId)
            throws BooksDbException{
        try{
            String sql = "UPDATE book SET title = ? WHERE book_id = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1,newTitle);
            stmt.setInt(2,bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("");
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    @Override
    public Book addBook(String title, String isbn, Date published, String authorName, String genre)
            throws BooksDbException {
        try {
            this.con.setAutoCommit(false);
            String sql = "INSERT INTO book (title, isbn, published) VALUES (?, ?, ?)";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, isbn);
            stmt.setDate(3, published);
            stmt.executeUpdate();
            addAuthor(authorName);
            addAuthorToBook(title, isbn, authorName);
            addGenreToBook(title, isbn, genre);
            int bookId = getBookIdByTitleAndISBN(title, isbn);
            this.con.commit();
            return new Book(bookId, title, isbn, published);
        } catch (SQLException e) {
            if (this.con != null){
                try {
                    this.con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }finally {
            try {
                this.con.setAutoCommit(true);
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the SQL statement", e);
            }
        }
    }

    @Override
    public void addGenreToBook(String title, String isbn, String genre) throws BooksDbException {
        try {
            String sql;
            PreparedStatement stmt;
            sql = "INSERT INTO book_genre (book_id, genre_id) VALUES (?,?)";
            stmt = this.con.prepareStatement(sql);
            int bookId = getBookIdByTitleAndISBN(title, isbn);
            int genreId = getGenreIdByGenreName(genre);
            stmt.setInt(1, bookId);
            stmt.setInt(2, genreId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    private int getGenreIdByGenreName(String genre)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            String sql = "SELECT genre_id FROM genre WHERE genre_name = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, genre);
            rs = stmt.executeQuery();
            int genreId = 0;
            while (rs.next()){
                genreId = rs.getInt("genre_id");
            }
            return genreId;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public void addAuthorToBook(String title, String isbn, String authorName) throws BooksDbException {
        try {
            String sql;
            PreparedStatement stmt;
            sql = "INSERT INTO book_author (book_id, author_id) VALUES (?,?)";
            stmt = this.con.prepareStatement(sql);
            int bookId = getBookIdByTitleAndISBN(title, isbn);
            int authorId = getAuthorByName(authorName).getAuthorId();
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    @Override
    public int getBookIdByTitleAndISBN(String title, String isbn)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM book WHERE title = ? AND isbn = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, isbn);
            rs = stmt.executeQuery();
            int bookId = 0;
            while (rs.next()){
                bookId = rs.getInt("book_id");
            }
            return bookId;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public void addAuthor(String authorName)
            throws BooksDbException {
        try {
            if (!(authorName.equals(""))) {
                System.out.println("test");
                String sql = "INSERT INTO author (name) VALUES (?)";
                PreparedStatement stmt = this.con.prepareStatement(sql);
                stmt.setString(1, authorName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    @Override
    public Author getAuthorByName(String authorName) throws BooksDbException{
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM author WHERE name = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, authorName);
            rs = stmt.executeQuery();
            int authorId = 0;
            String name = "";
            while (rs.next()){
                authorId = rs.getInt("author_id");
                name = rs.getString("name");
            }
            return new Author(authorId, name);
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }
    @Override
    public Book getBookById(int bookId) throws BooksDbException{
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM book WHERE book_id = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            String isbn = "";
            String title = "";
            Date published = null;
            while (rs.next()){
                bookId = rs.getInt("book_id");
                isbn = rs.getString("isbn");
                title = rs.getString("title");
                published = rs.getDate("published");
            }
            return new Book(bookId,isbn,title,published);
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public List<Author> getAuthorsByBookId(int bookId)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Author> result = new ArrayList<>();
            String sql = "SELECT a.author_id, a.name FROM book b, author a, book_author ba WHERE b.book_id = ba.book_id AND ba.author_id = a.author_id AND b.book_id = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new Author(rs.getInt("author_id"), rs.getString("name")));
            }
            return result;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public boolean removeBook(int bookId) throws BooksDbException {
        try {
            con.setAutoCommit(false);
            removeBookFromAuthor(bookId);
            removeBookFromGenre(bookId);
            String sql = "DELETE FROM book WHERE book_id = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null){
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the SQL statement", e);
            }
        }
    }

    private void removeBookFromAuthor(int bookId) throws BooksDbException{
        try {
            String sql = "DELETE FROM book_author WHERE book_id = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    private void removeBookFromGenre(int bookId) throws BooksDbException{
        try {
            String sql = "DELETE FROM book_genre WHERE book_id = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    @Override
    public User loginUser(String username, String password)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            User user = null;
            String sql = "SELECT * FROM user WHERE username = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            while (rs.next()) {
                user = new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("username"), rs.getString("password"));
            }
            return user.getPassword().equals(encryptPassword(password)) ? user : null;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }


    @Override
    public boolean registerUser(String name, String username, String password)
            throws BooksDbException {
        ResultSet rs = null;
        try {
            this.con.setAutoCommit(false);
            String sql = "SELECT * FROM user WHERE username = ?";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (!rs.next()){
                sql = "INSERT INTO user (name, username, password) VALUES (?, ?, ?)";
                stmt = this.con.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, username);
                stmt.setString(3, encryptPassword(password));
                stmt.executeUpdate();
                this.con.commit();
                return true;
            }else{
                return false;
            }
        } catch (SQLException e) {
            if (con != null){
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }finally {
            try {
                con.setAutoCommit(true);
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the SQL statement", e);
            }
        }
    }

    @Override
    public List<Book> getAllBooks() throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Book> books = new ArrayList<>();
            String sql = "SELECT * FROM book";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()){
                books.add(new Book(rs.getInt("book_id"), rs.getString("title"), rs.getString("isbn"), rs.getDate("published")));
            }
            return books;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public List<Author> getAllAuthors() throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Author> authors = new ArrayList<>();
            String sql = "SELECT * FROM author";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()){
                authors.add(new Author(rs.getInt("author_id"), rs.getString("name")));
            }
            return authors;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    @Override
    public void reviewBook(int bookId, int userId, double rating, String reviewText) throws BooksDbException {
        try {
            String sql = "INSERT INTO review (book_id, user_id, stars, review_text, review_date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            stmt.setDouble(3, rating);
            stmt.setString(4, reviewText);
            stmt.setDate(5, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        }
    }

    @Override
    public List<Book> getBooksNotReviewed(int userId) throws BooksDbException {
        ResultSet rs = null;
        try {
            List<Book> books = new ArrayList<>();
            String sql = "SELECT * FROM book WHERE book_id NOT IN (SELECT b.book_id FROM book b, review r WHERE r.book_id = b.book_id AND r.user_id = ?)";
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()){
                books.add(new Book(rs.getInt("book_id"), rs.getString("title"), rs.getString("isbn"), rs.getDate("published")));
            }
            return books;
        } catch (SQLException e) {
            throw new BooksDbException("There is something wrong with the SQL statement", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new BooksDbException("There is something wrong with the connection", e);
            }
        }
    }

    private String encryptPassword(String password){
        String encryptedpassword = null;
        try
        {
            /* MessageDigest instance for MD5. */
            MessageDigest m = MessageDigest.getInstance("MD5");

            /* Add plain-text password bytes to digest using MD5 update() method. */
            m.update(password.getBytes());

            /* Convert the hash value into bytes */
            byte[] bytes = m.digest();

            /* The bytes array has bytes in decimal form. Converting it into hexadecimal format. */
            StringBuilder s = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            /* Complete hashed password in hexadecimal format */
            encryptedpassword = s.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        /* Display the unencrypted and encrypted passwords. */
        System.out.println("Plain-text password: " + password);
        System.out.println("Encrypted password using MD5: " + encryptedpassword);
        return encryptedpassword;
    }


    private static final Book[] DATA = {
            new Book(1, "123456789", "Databases Illuminated", new Date(2018, 1, 1)),
            new Book(2, "234567891", "Dark Databases", new Date(1990, 1, 1)),
            new Book(3, "456789012", "The buried giant", new Date(2000, 1, 1)),
            new Book(4, "567890123", "Never let me go", new Date(2000, 1, 1)),
            new Book(5, "678901234", "The remains of the day", new Date(2000, 1, 1)),
            new Book(6, "234567890", "Alias Grace", new Date(2000, 1, 1)),
            new Book(7, "345678911", "The handmaids tale", new Date(2010, 1, 1)),
            new Book(8, "345678901", "Shuggie Bain", new Date(2020, 1, 1)),
            new Book(9, "345678912", "Microserfs", new Date(2000, 1, 1)),
    };
}
