//Karl Savinskiy

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

////////// CLASSES \\\\\\\\\\\\\\\\\
class Movie implements Comparable<Movie> {
    private String title;
    private int year;
    private String genre;
    private double averageRating;
    private int maxRating;

    public Movie(String title, int year, String genre, int[] ratings){
        this.title = title;
        this.year = year;
        this.genre = genre;
        
        // calculate average rating
        int sum = 0;
        for (int rating : ratings) {
            sum += rating;
        }
        this.averageRating = (double) sum / ratings.length;
        
        // find max rating
        this.maxRating = ratings[0];
        for (int rating : ratings) {
            if (rating > maxRating) {
                maxRating = rating;
            }
        }
    }

    public String getTitle() {return title;}
    public int getYear() {return year;}
    public String getGenre() {return genre;}
    public double getAverageRating() {return averageRating;}
    public int getMaxRating() {return maxRating;}

    @Override
    public int compareTo(Movie other){
        return Double.compare(other.averageRating, this.averageRating);
    }
}



public class Analyzer {
    //constants
    private static final String[] EXPECTED_HEADERS = {"title", "year", "genre", "rating1", "rating2", "rating3", "rating4", "rating5"};
    private static final String CSV_FILE = "movies.csv";

                            //////////////MAIN\\\\\\\\\\\\\\
    public static void main(String[] args){
        try {
            String[] lines = readFile(CSV_FILE);
            if (lines.length == 0){
                System.err.println("Error: CSV file has no movies!");
                return;
            }

            if (!validateHeader(lines[0])){
                System.err.println("Error: Invalid header format. Expected: Title,Year,Genre,Rating1,Rating2,Rating3,Rating4,Rating5");
                return;
            }

            //parse movies
            ArrayList<Movie> movies = parseMovies(lines);
            if (movies.size() == 0){
                System.err.println("Error: No valid movie records found");
                return;
            }

            Collections.sort(movies);// uses the compareTo method
            displayMovies(movies);

        } catch (IOException e){
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Exception e){
            System.err.println("Error: An unexpected error occurred");
        }
    }


                                ////////////METHODS\\\\\\\\\\\
//////READ
    // method that read the whole file into a string array
    private static String[] readFile(String filename) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            StringBuilder content = new StringBuilder();
            String line;

            //read all lines into one string
            while ((line = reader.readLine()) != null){
                content.append(line).append("\n");
            }
            reader.close();

            //edge case: empty
            if (content.length() == 0){
                return new String[0];
            }

            //split the string into array
            return content.toString().trim().split("\n");
        } catch (FileNotFoundException e) {
            throw new IOException("File not found: " + filename);
        } catch (IOException e) {
            throw new IOException("Error reading file: " + e.getMessage());
        }
    }

                                            //////////VALIDATIONS\\\\\\\\\\\\

//////////VALIDATION OF HEADER
    private static boolean validateHeader(String headerLine) {
        String[] headers = headerLine.split(",");
        if (headers.length < 6 || headers.length > 8) {
            return false;
        }
        
        // Check first 3 headers: title, year, genre; are required
        for (int i = 0; i < 3; i++) {
            if (!headers[i].trim().toLowerCase().equals(EXPECTED_HEADERS[i])) {
                return false;
            }
        }
        
        // Check rating headers: rating1, rating2, rating3
        //up to rating5
        for (int i = 3; i < headers.length; i++) {
            if (i >= EXPECTED_HEADERS.length || 
                !headers[i].trim().toLowerCase().equals(EXPECTED_HEADERS[i])) {
                return false;
            }
        }
        
        return true;
    }

///////////VALIDATION OF A MOVIE
    // Validate a single movie record
    private static boolean isValidMovieRecord(String line, int lineNumber){
        if (line.trim().isEmpty()){
            return false; // skip empty
        }

        String[] fields = line.split(",");
        if (fields.length < 6 || fields.length > 8){
            System.err.println("Error on line " + lineNumber + ": Expected 6-8 fields, found " + fields.length);
            return false;
        }

        try {
            // Check title is not empty 
            String title = fields[0].replaceAll("\t", " ").trim();
            if (title.isEmpty()) {
                System.err.println("Error on line " + lineNumber + ": Title cannot be empty");
                return false;
            }
            
            // Check title is not too short
            if (title.length() < 5) {
                System.err.println("Error on line " + lineNumber + ": Title must be at least 5 characters long");
                return false;
            }
            
            // validation: year must be 1900 - 2099
            int year = Integer.parseInt(fields[1].trim());
            if (year < 1900 || year > 2099) {
                System.err.println("Error on line " + lineNumber + ": Year must be between 1900-2099, found " + year);
                return false;
            }

            // Check genre is not empty 
            String genre = fields[2].replaceAll("\t", " ").trim();
            if (genre.isEmpty()) {
                System.err.println("Error on line " + lineNumber + ": Genre cannot be empty");
                return false;
            }
            
            // Check genre is not too short
            if (genre.length() < 5) {
                System.err.println("Error on line " + lineNumber + ": Genre must be at least 5 characters long");
                return false;
            }

            //validation: rating must be 1 - 100
            for (int i = 3; i < fields.length; i++){
                int rating = Integer.parseInt(fields[i].trim());
                if ( rating < 1 || rating > 100){
                    System.err.println("Error on line " + lineNumber + ": Rating must be between 1-100, found " + rating);
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e){
            System.err.println("Error on line " + lineNumber + ": Invalid number format");
            return false;
        }
    }


    private static ArrayList<Movie> parseMovies(String[] lines){
        ArrayList<Movie> movies = new ArrayList<>();

        for (int i = 1; i < lines.length; i++){
            Movie movie = parseMovieRecord(lines[i], i + 1);
            if (movie != null){
                movies.add(movie);
            }
        }

        return movies;
    }

    


    // parse a single valid movie record
    private static Movie parseMovieRecord(String line, int lineNumber){
        if (!isValidMovieRecord(line, lineNumber)){
            return null;
        }
        String[] fields = line.split(",");
        try {
            // Replace tab characters with spaces and trim
            String title = fields[0].replaceAll("\t", " ").trim();
            int year = Integer.parseInt(fields[1].trim());
            String genre = fields[2].replaceAll("\t", " ").trim();
            
            // Parse variable number of ratings (3-5)
            int[] ratings = new int[fields.length - 3];
            for (int i = 3; i < fields.length; i++) {
                ratings[i - 3] = Integer.parseInt(fields[i].trim());
            }

            return new Movie(title, year, genre, ratings);

        } catch (Exception e) {
            return null;
        }
    }

    //displaying movies in format table
    private static void displayMovies(ArrayList<Movie> movies){

        int titleWidth = "Title".length();
        int genreWidth = "Genre".length();

        for (Movie movie : movies) {
            titleWidth = Math.max(titleWidth, movie.getTitle().length());
            genreWidth = Math.max(genreWidth, movie.getGenre().length());
        }

        //header
        System.out.printf("%-" + titleWidth + "s | Year | %-" + genreWidth + "s | Avg Rating | Max Rating\n", "Title", "Genre");
        System.out.println("-".repeat(titleWidth + genreWidth + 38));

        //movies
        for (Movie movie : movies) {
            System.out.printf("%-" + titleWidth + "s | %4d | %-" + genreWidth + "s | %10.1f | %10d\n",
                movie.getTitle(),
                movie.getYear(),
                movie.getGenre(),
                movie.getAverageRating(),
                movie.getMaxRating());
        }
    }
}
