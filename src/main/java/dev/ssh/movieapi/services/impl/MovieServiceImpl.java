package dev.ssh.movieapi.services.impl;

import dev.ssh.movieapi.dtos.MovieDto;
import dev.ssh.movieapi.dtos.MoviePageResponse;
import dev.ssh.movieapi.entities.Movie;
import dev.ssh.movieapi.exceptions.FileExistsException;
import dev.ssh.movieapi.exceptions.MovieNotFoundException;
import dev.ssh.movieapi.repositories.MovieRepository;
import dev.ssh.movieapi.services.FileService;
import dev.ssh.movieapi.services.MovieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final FileService fileService;

    private final MovieRepository movieRepository;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(FileService fileService, MovieRepository movieRepository) {
        this.fileService = fileService;
        this.movieRepository = movieRepository;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // 1. upload file -> returns file name
        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExistsException("File already exists! Please give another file!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        // 2. set poster value to file name
        movieDto.setPoster(uploadedFileName);

        // 3. convert to Movie object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 4. save Movie object to DB -> return Movie object
        Movie savedMovie = movieRepository.save(movie);

        // 6. Get base url and construct poster's Url
        var posterUrl = baseUrl + "/file/" + uploadedFileName;

        // 5. convert to MovieDto object, and return this object
        var responseObj = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );

        return responseObj;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        // 1. check if any record exists in DB with given 'movieId'
        // 2. get the data if exists, else throw/handle exception
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));

        // 3. generate 'posterUrl' with help of value from 'poster' field
        var posterUrl = baseUrl + "/file/" + movie.getPoster();

        // 4. map the data to MovieDto object and return the object
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // 1. get all data from DB
        List<Movie> movies = movieRepository.findAll();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2.1 iterate the list, generate posterUrl for each data,
        // 2.2 and map to MovieDto object -> return the object
        for(Movie movie: movies) {
            var posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        // 1. check if movie exists in DB, and fetch the data if exists
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));

        // 2. check if file is null, then no need to do anything,
        // else upload file, and replace existing
        String fileName = mv.getPoster();
        if (file != null) {
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        // 3. set poster's value according to step 2
        movieDto.setPoster(fileName);

        // 4. map to Movie object
        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 5. save the Movie object -> return saved Movie object
        Movie updatedMovie = movieRepository.save(movie);

        // 6. generate posterUrl
        var posterUrl = baseUrl + "/file/" + updatedMovie.getPoster();

        // 7. map to MovieDto object and return it
        var responseObj = new MovieDto(
                updatedMovie.getMovieId(),
                updatedMovie.getTitle(),
                updatedMovie.getDirector(),
                updatedMovie.getStudio(),
                updatedMovie.getMovieCast(),
                updatedMovie.getReleaseYear(),
                updatedMovie.getPoster(),
                posterUrl
        );

        return responseObj;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        // 1. check if movie record exists in DB with given movieID
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));
        Integer id = mv.getMovieId();
        // 2. if exists delete the movie object in DB, and file associated with in file path
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));
        movieRepository.delete(mv);

        return "Movie object deleted with id = " + id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        // 1. create Pageable object
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // 2. Get the data from DB
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        // 3. Convert to MovieDto object and return it
        List<MovieDto> movieDtos = new ArrayList<>();
        for (Movie movie : movies) {
            var posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos,
                                    pageNumber,
                                    pageSize,
                                    moviePages.getTotalElements(),
                                    moviePages.getTotalPages(),
                                    moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                                  String sortBy, String sortDir) {
        // 1. create Sort and Pageable object
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                                                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // 2. Get the data from DB
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        // 3. Convert to MovieDto object and return it
        List<MovieDto> movieDtos = new ArrayList<>();
        for (Movie movie : movies) {
            var posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos,
                                    pageNumber,
                                    pageSize,
                                    moviePages.getTotalElements(),
                                    moviePages.getTotalPages(),
                                    moviePages.isLast());
    }
}
