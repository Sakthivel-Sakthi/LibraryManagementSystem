package books;

import java.sql.*;
import java.util.Scanner;

public class LibraryManagementSystem {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/library_management";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "Sakthimax@45";
    
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    public static void addBook(String title, String author) throws SQLException {
        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.executeUpdate();
            System.out.println("Book added successfully!");
        }
    }

    public static void deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book deleted successfully!");
            } else {
                System.out.println("No book found with the given ID.");
            }
        }
    }

    public static void issueBook(int bookId, String issuedTo) throws SQLException {
        String checkAvailability = "SELECT available FROM books WHERE book_id = ?";
        String issueBookSQL = "INSERT INTO issued_books (book_id, issued_to) VALUES (?, ?)";
        String updateBookStatusSQL = "UPDATE books SET available = FALSE WHERE book_id = ?";
        
        try (Connection conn = connect()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(checkAvailability)) {
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getBoolean("available")) {
                    try (PreparedStatement issueStmt = conn.prepareStatement(issueBookSQL);
                         PreparedStatement updateStmt = conn.prepareStatement(updateBookStatusSQL)) {
                        issueStmt.setInt(1, bookId);
                        issueStmt.setString(2, issuedTo);
                        issueStmt.executeUpdate();

                        updateStmt.setInt(1, bookId);
                        updateStmt.executeUpdate();
                        System.out.println("Book issued successfully!");
                    }
                } else {
                    System.out.println("Book is not available for issue.");
                }
            }
        }
    }

    public static void returnBook(int bookId) throws SQLException {
        String sql = "DELETE FROM issued_books WHERE book_id = ?";
        String updateBookStatusSQL = "UPDATE books SET available = TRUE WHERE book_id = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql);
             PreparedStatement updateStmt = conn.prepareStatement(updateBookStatusSQL)) {
            pstmt.setInt(1, bookId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update book status
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                System.out.println("Book returned successfully!");
            } else {
                System.out.println("No record found for this book being issued.");
            }
        }
    }

    public static void viewBooks() throws SQLException {
        String sql = "SELECT * FROM books";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("book_id") +
                                   ", Title: " + rs.getString("title") +
                                   ", Author: " + rs.getString("author") +
                                   ", Available: " + rs.getBoolean("available"));
            }
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nLibrary Management System");
                System.out.println("1. Add Book");
                System.out.println("2. Delete Book");
                System.out.println("3. Issue Book");
                System.out.println("4. Return Book");
                System.out.println("5. View Books");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        System.out.print("Enter book title: ");
                        String title = scanner.next();
                        System.out.print("Enter book author: ");
                        String author = scanner.next();
                        addBook(title, author);
                        break;
                    case 2:
                        System.out.print("Enter book ID to delete: ");
                        int bookId = scanner.nextInt();
                        deleteBook(bookId);
                        break;
                    case 3:
                        System.out.print("Enter book ID to issue: ");
                        int issueBookId = scanner.nextInt();
                        System.out.print("Enter the name of the person: ");
                        String issuedTo = scanner.next();
                        issueBook(issueBookId, issuedTo);
                        break;
                    case 4:
                        System.out.print("Enter book ID to return: ");
                        int returnBookId = scanner.nextInt();
                        returnBook(returnBookId);
                        break;
                    case 5:
                        viewBooks();
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

