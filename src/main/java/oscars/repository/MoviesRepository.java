package oscars.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import oscars.entity.Movie;


/**
 * This is the repository for movies
 */
public interface MoviesRepository extends
		JpaRepository<Movie, String> {

	/**
	 * This method gets the Movie object based upon a Title
	 * 
	 * @param title
	 *            title of the movie
	 * @return Movie object
	 */
	Movie findByTitle(String title);
	
	@Query("FROM Movie  WHERE votes = (SELECT MAX(votes) FROM Movie)")
    List<Movie> findMaxVoted();
	
	@Transactional
	@Modifying
	@Query("Update  Movie  set votes = 0")
    void clearVotes();
}
	
