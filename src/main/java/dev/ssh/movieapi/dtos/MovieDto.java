package dev.ssh.movieapi.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class MovieDto {

    private Integer movieId;

    @NotBlank(message = "Please provide movie title!")
    private String title;

    @NotBlank(message = "Please provide movie's director name!")
    private String director;

    @NotBlank(message = "Please provide movie's studio name!")
    private String studio;

    private Set<String> movieCast;

    @NotBlank(message = "Please provide movie's poster name!")
    private String poster;

    @NotBlank(message = "Please provide movie's release year!")
    private Integer releaseYear;

}
