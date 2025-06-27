//Karl Savinskiy

import java.io.*;
import java.util.Arrays;

////////// CLASSES \\\\\\\\\\\\\\\\\
class Movie implements Comparable<Movie> {
    private String title;
    private int year;
    private String genre;
    private int rating1, rating2, raiting3;
    private double averageRating;

    public Movie(String title, int year, String genre, int rating1, int rating2, int raiting3){
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.raiting3 = raiting3;
        //get the average
        this.averageRating = (rating1 + rating2 + raiting3) / 3.0; //double
    }


    public String getTitle() {return title;}
    public int getYear() {return year;}
    public String getGenre() {return genre;}
    public double getAverageRating() {return averageRating;}


    @Override
    public int compareTo(Movie other){
        return Double.compare(other.averageRating, this.averageRating);
    }

}



public class Analyzer {
    //constants
    private static final String[] EXPECTED_HEADERS = {"title", "year", "genre", "rating1", "rating2", "rating3"};
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
                System.err.println("Error: Invalid header format. Expected: Title,Year,Genre,Rating1,Rating2,Rating3");
                return;
            }

            //parse movies
            Movie[] movies = parseMovies(lines);
            if (movies.length == 0){
                System.err.println("Error: No valid movie records found");
                return;
            }

            Arrays.sort(movies);// uses the compareTo method
            displayMovies(movies);

        
        } catch (IOException e){
            System.err.println("Error reading file: " + e.getMessage());
        }
    }


                                ////////////METHODS\\\\\\\\\\\
//////READ
    // method that read the whole file into a string array
    private static String[] readFile(String filename) throws IOException {
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
    }

                                            //////////VALIDATIONS\\\\\\\\\\\\

//////////VALIDATION OF HEADER
    private static boolean validateHeader(String headerLine) {
        String[] headers = headerLine.split(",");
        if (headers.length != 6) {
            return false;
        }
            
        // Check each header matches expected (case-insensitive, trimmed)
        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].trim().toLowerCase().equals(EXPECTED_HEADERS[i])) {
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
        if (fields.length != 6){
            System.err.println("Error on line " + lineNumber + ": Expected 6 fields, found " + fields.length);
            return false;
        }

        try {
            // validation: year must be 1900 - 2099
            int year = Integer.parseInt(fields[1].trim());
            if (year < 1900 || year > 2099) {
                System.err.println("Error on line " + lineNumber + ": Year must be between 1900-2099, found " + year);
                return false;
            }

            //validation: rating must be 1 - 100
            for (int i = 3; i <= 5; i++){
                int rating = Integer.parseInt((fields[i].trim()));
                if ( rating < 1 || rating > 100){
                    System.err.println("Error on line " + lineNumber + ": Rating must be between 1-100, found " + rating);
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e){
            System.err.println(("Error on line " + lineNumber + ": Invalid number format"));
            return false;
        }
    }


    private static Movie[] parseMovies(String[] lines){
        int validCount = 0;
        for (int i = 1; i < lines.length; i++){
            if (isValidMovieRecord(lines[i], i + 1)){
                validCount++;
            }
        }

        //create the array
        Movie[] movies = new Movie[validCount];
        int index = 0; 


        for (int i = 1; i < lines.length; i++){
            Movie movie = parseMovieRecord(lines[i], i + 1);
            if (movie != null){
                movies[index++] = movie;
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
            String title = fields[0].trim();
            int year = Integer.parseInt(fields[1].trim());
            String genre = fields[2].trim();
            int raiting1 = Integer.parseInt(fields[3].trim());
            int raiting2 = Integer.parseInt(fields[4].trim());
            int raiting3 = Integer.parseInt(fields[5].trim());

            return new Movie(title, year, genre, raiting1, raiting2, raiting3);

    } catch (Exception e) {
        return null;
    }

}

    //displaying movies in format table

    private static void displayMovies(Movie[] movies){

        int titleWidth = "Title".length();
        int genreWidth = "Genre".length();

        for (Movie movie : movies) {
            titleWidth = Math.max(titleWidth, movie.getTitle().length());
            genreWidth = Math.max(genreWidth, movie.getGenre().length());
        }

        //header
        System.out.printf("%-" + titleWidth + "s | Year | %-" + genreWidth + "s | Avg Rating\n", "Title", "Genre");
        System.out.println("-".repeat(titleWidth + genreWidth + 25));


        //movies
        for (Movie movie : movies) {
            System.out.printf("%-" + titleWidth + "s | %4d | %-" + genreWidth + "s | %.1f\n",
                movie.getTitle(),
                movie.getYear(),
                movie.getGenre(),
                movie.getAverageRating());
        }
    }
}
