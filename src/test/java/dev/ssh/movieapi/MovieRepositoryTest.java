package dev.ssh.movieapi;

import dev.ssh.movieapi.entities.Movie;
import dev.ssh.movieapi.repositories.MovieRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Set;

@DataJpaTest
public class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    private Movie movie;

    @BeforeEach
    public void setupData() {
        movie = Movie.builder()
                .title("movie1")
                .director("dir1")
                .poster("poster1")
                .releaseYear(2024)
                .studio("studio1")
                .movieCast(Set.of("P1", "P2"))
                .build();
    }

    @Test
    @DisplayName("JUnit test for saving entity to DB")
    public void movieRepositorySaveReturnMovie() {

        Movie savedMovie = movieRepository.save(movie);

        Assertions.assertThat(savedMovie).isNotNull();
        Assertions.assertThat(savedMovie.getMovieId()).isGreaterThan(0);
    }

    @Test
    public void movieRepositoryFindAllReturnAllData() {
        Movie movie1 = Movie.builder()
                .title("movie1")
                .director("dir1")
                .poster("poster1")
                .releaseYear(2024)
                .studio("studio1")
                .movieCast(Set.of("P1", "P2"))
                .build();

        Movie movie2 = Movie.builder()
                .title("movie2")
                .director("dir2")
                .poster("poster2")
                .releaseYear(2024)
                .studio("studio2")
                .movieCast(Set.of("P1", "P2"))
                .build();

        movieRepository.save(movie1);
        movieRepository.save(movie2);

        List<Movie> movies = movieRepository.findAll();

        Assertions.assertThat(movies).isNotNull();
        Assertions.assertThat(movies.size()).isEqualTo(2);
    }

    @Test
    public void movieRepositoryfindByMovieIdReturnMovie() {
        movieRepository.save(movie);

        Movie movie1 = movieRepository.findById(movie.getMovieId()).orElseGet(Movie::new);

        Assertions.assertThat(movie1).isNotNull();
    }
}
